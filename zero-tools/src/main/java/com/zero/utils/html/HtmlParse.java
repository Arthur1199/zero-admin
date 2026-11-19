package com.zero.utils.html;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 使用 dom4j 解析符合 XML 格式的表单文本，也提供将指定标签改为自闭合形式的辅助方法。
 */
public class HtmlParse {

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 解析根元素下的 form，读取它的 action 属性及直接子 input 的 name、value 属性。
     * @param htmlStr 可按 XML 解析且根元素包含 form 子元素的表单文本
     * @return
     * 返回值: Map<String, Object>
     * 返回值说明: 以 action 和各输入名称为键的映射，同名输入保留后出现的值
     * @throws Exception XML 文本解析失败时向上传递
     */
    @SuppressWarnings("rawtypes")
	public static Map<String,Object> readAttributeMap(String htmlStr) throws Exception {
        Map<String,Object> resultMap=new HashMap<>();

        Document document= DocumentHelper.parseText(htmlStr);
        Element eleRoot = document.getRootElement();
        Element fromElement=eleRoot.element("form");

        // 单独保存表单提交地址，输入项以各自的 name 作为键。
        resultMap.put("action",fromElement.attributeValue("action"));
        List nodes = fromElement.elements("input");
        for (Iterator it = nodes.iterator(); it.hasNext();) {
            Element elm = (Element) it.next();
            resultMap.put(elm.attributeValue("name"),elm.attributeValue("value"));
        }

        return resultMap;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 读取根元素下 form 的文本及根元素的 name 子元素文本。现有实现仅演示读取步骤。
     * @param htmlStr 可按 XML 解析且根元素包含 form 子元素的表单文本
     * @return
     * 返回值: Map<String, Object>
     * 返回值说明: 空映射，读取到的文本没有写入返回值
     * @throws Exception XML 文本解析失败时向上传递
     */
    @SuppressWarnings("unused")
	public static Map<String,Object> readNodeMap(String htmlStr) throws Exception {
        Map<String,Object> resultMap=new HashMap<>();

        Document document= DocumentHelper.parseText(htmlStr);
        Element eleRoot = document.getRootElement();
        Element fromElement=eleRoot.element("form");

        // 当前实现只读取两处节点的文本，结果映射保持为空。
        String text2=fromElement.getText();
        String text1=eleRoot.elementText("name");

        return resultMap;
    }


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 给匹配到的指定标签补上自闭合斜杠，供后续解析使用。
     * @param str 含完整起始标签的 HTML 文本
     * @param node 待处理的标签名，例如 input
     * @return
     * 返回值: String
     * 返回值说明: 补全指定标签后的原文本
     */
    public static String closeHTML(String str,String node) {
        for (int j = 0; j < str.length(); ) {
            int tagStart = str.indexOf("<" + node ,j);
            if (tagStart >= 0) {
                int tagEnd = str.indexOf(">", tagStart);
                j = tagEnd;
                String preCloseTag = str.substring(tagEnd - 1, tagEnd);
                if (!"/".equals(preCloseTag)) {
                    String preStr = str.substring(0, tagEnd);
                    String afterStr = str.substring(tagEnd);
                    str = preStr + "/" + afterStr;
                }
            } else {
                break;
            }
        }
        return str;
    }
}
