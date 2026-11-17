package com.zero.system.service;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 图文通知HTML白名单、图片链接和大小边界回归，禁止脚本和Base64进入消息
 */
class SystemNoticeContentTest {
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 保留编辑器基本排版、相对图片地址和安全链接，剔除样式中的定位
     * @return
     * 返回值: void
     * 返回值说明: 白名单格式正常，未知属性拒绝
     */
    @Test void preservesFormattingImagesAndLinks() {
        String html=SystemNoticeContent.clean("<h2 class='ql-align-center'>标题</h2><p><strong>正文</strong><span style='color:rgb(255, 0, 0);position:fixed'>红字</span></p><img src='/dev-api/profile/test.png' onerror='alert(1)'><a href='https://example.test/a'>链接</a>");
        assertThat(html).contains("<h2","ql-align-center","<strong>正文</strong>","rgb(255, 0, 0)","/dev-api/profile/test.png","noopener noreferrer","_blank");
        assertThat(html).doesNotContain("onerror","position");
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 多种脚本、内嵌页面和危险URL均不能成为可执行HTML
     * @return
     * 返回值: void
     * 返回值说明: 有效正文保留，攻击标记和属性移除
     */
    @Test void stripsExecutableMarkupAndUnsafeUrls() {
        for(String attack:List.of("<script>alert(1)</script>","<img src='javascript:alert(1)' onerror='alert(1)'>",
            "<a href='&#106;avascript:alert(1)'>链接</a>","<iframe src='https://example.test'></iframe>",
            "<svg onload='alert(1)'><foreignObject><p>攻击</p></foreignObject></svg>",
            "<span style='background-image:url(https://evil.test);color:expression(alert(1))'>文本</span>",
            "<img src='data:image/svg+xml;base64,PHN2Zz4='>","<img src='//evil.test/x'>","<img src='https://user:pass@example.test/x'>")) {
            String html=SystemNoticeContent.clean("<p>正文</p>"+attack);
            assertThat(html).contains("正文").doesNotContain("<script","javascript:","onerror","onload","<iframe","<svg","expression","background-image","data:","user:pass","//evil.test");
        }
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 纯图片通知允许，空编辑器及危险图片独占正文时拒绝
     * @return
     * 返回值: void
     * 返回值说明: 不允许发送看起来成功的空通知
     */
    @Test void acceptsImageOnlyAndRejectsEmptyEditor() {
        assertThat(SystemNoticeContent.clean("<p><img src='https://example.test/image.png'></p>")).contains("<img");
        for(String value:List.of("<p><br></p>","<p>&nbsp;</p>","<script>alert(1)</script>","<img src='data:image/png;base64,abc'>", "<img src='/profile/a b.png'>", "<img src='/profile/a&#32;b.png'>"))
            assertThatThrownBy(()->SystemNoticeContent.clean(value)).isInstanceOf(IllegalArgumentException.class);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 按可见文字、图片数量与UTF-8体积限制消息，不把图片文件放入WebSocket
     * @return
     * 返回值: void
     * 返回值说明: 超限在发布前拒绝
     */
    @Test void enforcesContentLimits() {
        assertThatThrownBy(()->SystemNoticeContent.clean("<p>"+"字".repeat(2001)+"</p>")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->SystemNoticeContent.clean("<img src='/profile/a.png'>".repeat(11))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->SystemNoticeContent.clean("<p>"+"字".repeat(11000)+"</p>")).isInstanceOf(IllegalArgumentException.class);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: Quill代码块转换为通用代码结构，保留换行并转义尖括号
     * @return
     * 返回值: void
     * 返回值说明: 不将代码内容作为HTML执行
     */
    @Test void preservesCodeBlockLines() {
        String html=SystemNoticeContent.clean("<div class='ql-code-block-container'><div class='ql-code-block'>line1</div><div class='ql-code-block'>&lt;script&gt;</div></div>");
        assertThat(html).contains("<pre><code>line1\n&lt;script&gt;</code></pre>").doesNotContain("ql-code-block-container");
    }
}
