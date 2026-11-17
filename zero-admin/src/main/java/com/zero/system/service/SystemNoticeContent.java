package com.zero.system.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 系统通知图文正文白名单，只保留编辑器排版及HTTP图片，不抓取外部资源、不记录原文
 */
public final class SystemNoticeContent {
    /** 图文HTML最大UTF-8字节数，为现有WebSocket信封和批次目标预留空间。 */
    public static final int MAX_HTML_BYTES = 32768;
    /** 纯文本可见内容上限，不将HTML标签算作正文字符。 */
    public static final int MAX_TEXT_LENGTH = 2000;
    /** 单条通知图片数量上限，图片通过URL传递，不嵌入文件内容。 */
    public static final int MAX_IMAGES = 10;
    /** 仅用于解析相对链接，不发起网络请求。 */
    private static final String PARSE_BASE = "https://notice.invalid/";
    /** Quill的安全排版类，不接受自定义类名、定位或脚本属性。 */
    private static final Pattern SAFE_CLASS = Pattern.compile("ql-align-(center|right|justify)|ql-indent-[1-8]|ql-size-(small|large|huge)|ql-font-(serif|monospace)");
    /** 颜色值不允许CSS函数、URL或表达式，仅保留基础颜色语法。 */
    private static final Pattern SAFE_COLOR = Pattern.compile("(?i)#[0-9a-f]{3,8}|rgba?\\([0-9.,% ]{1,60}\\)|black|white|red|green|blue|yellow|orange|purple|gray|grey|transparent|inherit");
    /** 白名单建成后只读，每次清理使用独立文档，避免并发修改共享状态。 */
    private static final Safelist ALLOWED = new Safelist()
        .addTags("p","div","br","strong","b","em","i","u","s","blockquote","pre","code","ol","ul","li","span","h1","h2","h3","h4","h5","h6","a","img")
        .addAttributes(":all","class","style")
        .addAttributes("a","href","title")
        .addAttributes("img","src","alt","title")
        .addAttributes("li","data-list")
        .addProtocols("a","href","http","https")
        .addProtocols("img","src","http","https")
        .addEnforcedAttribute("a","target","_blank")
        .addEnforcedAttribute("a","rel","noopener noreferrer")
        .preserveRelativeLinks(true);

    private SystemNoticeContent() {}

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 解析并清理图文HTML，空内容、过大正文或过多图片拒绝，禁止Base64图片进入频道
     * @param raw 编辑器HTML，不能为空，最多20000字符且不超过32KB
     * @return
     * 返回值: String
     * 返回值说明: 仅含安全排版的HTML片段，纯图片通知允许发送
     */
    public static String clean(String raw) {
        if (raw == null || raw.length() > 20000 || raw.getBytes(StandardCharsets.UTF_8).length > MAX_HTML_BYTES)
            throw new IllegalArgumentException("图文通知内容过大，请减少格式或图片链接");
        Document dirty = Jsoup.parseBodyFragment(raw,PARSE_BASE);
        dirty.select("script,style,iframe,object,embed,svg,math,form,input,button,textarea,link,meta,base,span.ql-ui").remove();
        // Quill代码块转换成通用pre/code，避免展示页依赖编辑器DOM或丢失换行。
        for (var block : dirty.select("div.ql-code-block-container")) {
            String code = block.children().stream().map(element -> element.wholeText()).reduce((a,b)->a+"\n"+b).orElse(block.wholeText());
            var pre = new org.jsoup.nodes.Element("pre");
            pre.appendElement("code").text(code);
            block.replaceWith(pre);
        }
        Document safe = new Cleaner(ALLOWED).clean(dirty);
        safe.outputSettings().prettyPrint(false);
        for (var element : safe.body().getAllElements()) {
            if (element.hasAttr("class")) {
                String classes = Arrays.stream(element.className().split("\\s+"))
                    .filter(value -> SAFE_CLASS.matcher(value).matches()).reduce((a,b)->a+" "+b).orElse("");
                element.removeAttr("class");
                if (!classes.isEmpty()) element.attr("class",classes);
            }
            if (element.hasAttr("style")) {
                List<String> styles = new ArrayList<>();
                for (String declaration : element.attr("style").split(";")) {
                    String[] pair = declaration.split(":",2);
                    if (pair.length != 2) continue;
                    String key=pair[0].trim().toLowerCase(Locale.ROOT), value=pair[1].trim();
                    if ((key.equals("color") || key.equals("background-color")) && SAFE_COLOR.matcher(value).matches()) styles.add(key+":"+value);
                    if (key.equals("text-align") && Set.of("left","center","right","justify").contains(value)) styles.add(key+":"+value);
                }
                element.removeAttr("style");
                if (!styles.isEmpty()) element.attr("style",String.join(";",styles));
            }
            if (element.hasAttr("data-list") && !Set.of("ordered","bullet").contains(element.attr("data-list"))) element.removeAttr("data-list");
            if (element.normalName().equals("img") && !safeUrl(element.attr("src"))) element.remove();
            if (element.normalName().equals("a") && !safeUrl(element.attr("href"))) element.removeAttr("href");
        }
        String text = safe.body().text().replace('\u00a0',' ').trim();
        int images = safe.select("img[src]").size();
        if (text.isEmpty() && images == 0) throw new IllegalArgumentException("请输入通知正文或插入有效图片");
        if (text.length() > MAX_TEXT_LENGTH) throw new IllegalArgumentException("通知正文最多2000字");
        if (images > MAX_IMAGES) throw new IllegalArgumentException("每条通知最多10张图片");
        String html=safe.body().html();
        if (html.getBytes(StandardCharsets.UTF_8).length > MAX_HTML_BYTES) throw new IllegalArgumentException("图文通知内容过大");
        return html;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 拒绝脚本、Base64、凭证URL及含空白的地址，与浏览器白名单一致，不执行远程请求
     * @param value 已由HTML解析器解码的地址
     * @return
     * 返回值: boolean
     * 返回值说明: 仅HTTP、HTTPS或以单斜杠开头的站内路径允许
     */
    private static boolean safeUrl(String value) {
        if (value == null || value.isBlank() || value.contains("\\")
                || value.chars().anyMatch(character -> character <= 0x20 || Character.isISOControl(character))) return false;
        if (value.startsWith("/") && !value.startsWith("//")) return true;
        try {
            URI uri=URI.create(value);
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                && uri.getHost()!=null && uri.getRawUserInfo()==null;
        } catch (IllegalArgumentException e) { return false; }
    }
}
