package com.zero.common.config;

import lombok.extern.slf4j.Slf4j;
import com.zero.common.utils.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 07:16
 * 说明: XML配置文件读取工具，读取classpath下的configuration.xml，并按category.item形式提供配置值；配置文件可能包含机器人令牌等敏感内容，日志只记录加载状态和数量，不输出配置值
 */
@Slf4j
public class XmlConfig
{
    /**
     * 默认XML配置文件名，放在resources根目录。
     */
    private static final String CONFIG_FILE_NAME = "configuration.xml";

    /**
     * XML配置项缓存，key格式为categoryName.itemName。
     */
    private static final Map<String, String> ITEMS = new ConcurrentHashMap<>();

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 读取默认XML配置文件，多次调用只在首次加载，避免运行期重复解析覆盖配置
     * @return
     * 返回值: void
     * 返回值说明: 将configuration.xml中的配置项加载到内存缓存
     */
    public static synchronized void loadConfig()
    {
        if (!ITEMS.isEmpty())
        {
            log.info("[{}] 配置文件已加载，当前配置项数量：{}", CONFIG_FILE_NAME, ITEMS.size());
            return;
        }
        loadConfig(CONFIG_FILE_NAME);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 读取指定XML配置文件，解析成功后替换当前配置缓存
     * @param configFileName classpath下的XML文件名，不允许为空
     * @return
     * 返回值: void
     * 返回值说明: 将指定XML文件中的配置项加载到内存缓存
     */
    public static synchronized void loadConfig(String configFileName)
    {
        if (StringUtils.isEmpty(configFileName))
        {
            log.warn("XML配置文件名为空，跳过加载");
            return;
        }
        Map<String, String> loadedItems = parseConfig(configFileName);
        ITEMS.clear();
        ITEMS.putAll(loadedItems);
        log.info("[{}] 配置文件加载完成，配置项数量：{}", configFileName, ITEMS.size());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 重新加载默认XML配置文件，刷新期间先解析到临时集合，成功后再替换缓存
     * @return
     * 返回值: void
     * 返回值说明: 用configuration.xml中的配置项刷新内存缓存
     */
    public static synchronized void reloadConfig()
    {
        loadConfig(CONFIG_FILE_NAME);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取字符串配置值
     * @param name 配置key，格式为categoryName.itemName
     * @return
     * 返回值: String
     * 返回值说明: 配置值；不存在时返回空字符串
     */
    public static String getString(String name)
    {
        return getString(name, "");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取字符串配置值，缺失或为空时返回默认值
     * @param name 配置key，格式为categoryName.itemName
     * @param defaultValue 默认值
     * @return
     * 返回值: String
     * 返回值说明: 配置值；不存在或为空时返回defaultValue
     */
    public static String getString(String name, String defaultValue)
    {
        String value = ITEMS.get(name);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取整型配置值
     * @param name 配置key，格式为categoryName.itemName
     * @return
     * 返回值: int
     * 返回值说明: 配置值；不存在或格式错误时返回0
     */
    public static int getInt(String name)
    {
        return getInt(name, 0);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取整型配置值，缺失或格式错误时返回默认值
     * @param name 配置key，格式为categoryName.itemName
     * @param defaultValue 默认值
     * @return
     * 返回值: int
     * 返回值说明: 整型配置值；不存在或格式错误时返回defaultValue
     */
    public static int getInt(String name, int defaultValue)
    {
        String value = getString(name);
        if (StringUtils.isEmpty(value))
        {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            log.warn("XML配置项[{}]不是有效整数，使用默认值：{}", name, defaultValue);
            return defaultValue;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取布尔配置值
     * @param name 配置key，格式为categoryName.itemName
     * @return
     * 返回值: boolean
     * 返回值说明: true-配置值为true，false-不存在或不是true
     */
    public static boolean getBoolean(String name)
    {
        return Boolean.parseBoolean(getString(name));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取双精度配置值，缺失或格式错误时返回默认值
     * @param name 配置key，格式为categoryName.itemName
     * @param defaultValue 默认值
     * @return
     * 返回值: double
     * 返回值说明: 双精度配置值；不存在或格式错误时返回defaultValue
     */
    public static double getDouble(String name, double defaultValue)
    {
        String value = getString(name);
        if (StringUtils.isEmpty(value))
        {
            return defaultValue;
        }
        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            log.warn("XML配置项[{}]不是有效小数，使用默认值：{}", name, defaultValue);
            return defaultValue;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 获取全部配置项快照
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: 只读配置快照，避免外部直接修改缓存
     */
    public static Map<String, String> getItems()
    {
        return Collections.unmodifiableMap(new HashMap<>(ITEMS));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 解析XML配置文件，解析异常时返回空集合并记录错误日志
     * @param configFileName classpath下的XML文件名
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: 按category.item组织的配置项
     */
    private static Map<String, String> parseConfig(String configFileName)
    {
        Resource resource = new ClassPathResource(configFileName);
        if (!resource.exists())
        {
            log.warn("[{}] 配置文件不存在，跳过加载", configFileName);
            return Collections.emptyMap();
        }

        try (InputStream inputStream = resource.getInputStream())
        {
            DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            document.getDocumentElement().normalize();
            return parseDocument(document);
        }
        catch (ParserConfigurationException | SAXException | IOException e)
        {
            log.error("[{}] 配置文件读取失败，请检查XML格式和文件编码", configFileName, e);
            return Collections.emptyMap();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 创建安全的XML解析器，禁止外部实体，避免读取本地文件或远程资源
     * @return
     * 返回值: DocumentBuilderFactory
     * 返回值说明: 已开启安全限制的XML解析器工厂
     * @throws ParserConfigurationException 解析器初始化失败
     */
    private static DocumentBuilderFactory createSecureDocumentBuilderFactory() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setFeature(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
        setFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 设置XML解析器安全特性，兼容不同JDK解析器的特性支持情况
     * @param factory XML解析器工厂
     * @param feature 安全特性名称
     * @param value 安全特性开关
     * @return
     * 返回值: void
     * 返回值说明: 支持该安全特性时完成设置，不支持时记录debug日志
     */
    private static void setFeature(DocumentBuilderFactory factory, String feature, boolean value)
    {
        try
        {
            factory.setFeature(feature, value);
        }
        catch (ParserConfigurationException e)
        {
            log.debug("当前XML解析器不支持安全特性：{}", feature);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 解析XML文档中的category和item节点
     * @param document XML文档
     * @return
     * 返回值: Map<String, String>
     * 返回值说明: 按category.item组织的配置项
     */
    private static Map<String, String> parseDocument(Document document)
    {
        Map<String, String> loadedItems = new HashMap<>();
        NodeList categoryNodes = document.getDocumentElement().getElementsByTagName("category");
        for (int i = 0; i < categoryNodes.getLength(); i++)
        {
            Node categoryNode = categoryNodes.item(i);
            if (!(categoryNode instanceof Element categoryElement))
            {
                continue;
            }
            String categoryName = categoryElement.getAttribute("name");
            if (StringUtils.isEmpty(categoryName))
            {
                continue;
            }
            parseCategoryItems(loadedItems, categoryName, categoryElement);
        }
        return loadedItems;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 07:16
     * 说明: 解析单个分类下的配置项
     * @param loadedItems 配置项收集结果
     * @param categoryName 分类名称
     * @param categoryElement 分类节点
     * @return
     * 返回值: void
     * 返回值说明: 将当前分类下的item节点写入loadedItems
     */
    private static void parseCategoryItems(Map<String, String> loadedItems, String categoryName, Element categoryElement)
    {
        NodeList itemNodes = categoryElement.getElementsByTagName("item");
        for (int i = 0; i < itemNodes.getLength(); i++)
        {
            Node itemNode = itemNodes.item(i);
            if (!(itemNode instanceof Element itemElement))
            {
                continue;
            }
            String itemName = itemElement.getAttribute("name");
            if (StringUtils.isEmpty(itemName))
            {
                continue;
            }
            loadedItems.put(categoryName + "." + itemName, itemElement.getAttribute("value"));
        }
    }
}
