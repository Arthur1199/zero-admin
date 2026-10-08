package com.zero.utils.xml.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zero.utils.common.StringHelper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 21:30
 * 说明: XML配置读取工具，默认读取configuration.xml并按category.item格式缓存配置项
 */
public class XmlConfig
{

    private static final Logger logger = LoggerFactory.getLogger(PropHelper.class);

    /**
     * 配置项缓存，key格式为category.item
     */
    private static final Map<String,String> items = new HashMap<String,String>();

    /**
     * 默认XML配置文件名
     */
    private static final String CONFIG_FILE_NAME = "configuration.xml";

    static
    {
//      loadConfig();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 加载XML配置文件到内存缓存，已加载时不重复覆盖
     * @return
     * 返回值: void
     * 返回值说明: 读取configuration.xml中的category/item节点并写入items缓存
     */
    @SuppressWarnings("rawtypes")
    public static synchronized void loadConfig()
    {
        if(items.isEmpty()){
            logger.info("start load xml config: {}", CONFIG_FILE_NAME);
            InputStream inputStream = PropHelper.guessPropFile(XmlConfig.class, CONFIG_FILE_NAME);
            if (inputStream != null)
            {
                InputStream is = inputStream;
                try
                {
                    SAXReader reader = new SAXReader();

                    Document document = reader.read(is);
                    Element systemElement = document.getRootElement();
                    List catList = systemElement.elements("category");
                    for (Iterator catIter = catList.iterator(); catIter.hasNext();)
                    {
                        Element catElement = (Element) catIter.next();
                        String catName = catElement.attributeValue("name");
                        if (StringHelper.isEmpty(catName))
                        {
                            continue;
                        }
                        List itemList = catElement.elements("item");
                        for (Iterator itemIter = itemList.iterator(); itemIter.hasNext();)
                        {
                            Element itemElement = (Element) itemIter.next();
                            String itemName = itemElement.attributeValue("name");
                            String value = itemElement.attributeValue("value");
                            if (!StringHelper.isEmpty(itemName))
                            {
                                items.put(catName + "." + itemName, value);
                            }
                        }
                    }

                }
                catch (Exception ex)
                {
                    logger.error("load xml config failed", ex);
                }
                finally
                {
                    if (is != null)
                    {
                        try
                        {
                            is.close();
                            is = null;
                        }
                        catch (IOException ex)
                        {
                            logger.error("close xml config input stream failed", ex);
                        }
                    }
                }
            }
            logger.info("xml config loaded: {}, item count: {}", CONFIG_FILE_NAME, items.size());
        }else{
            logger.info("xml config already loaded, skip: {}, item count: {}", CONFIG_FILE_NAME, items.size());
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 获取字符串配置值
     * @param name 配置项名称，格式通常为category.item
     * @return
     * 返回值: String
     * 返回值说明: 配置值不存在时返回空字符串
     */
    public static String getString(String name)
    {
        String value = (String) items.get(name);
        return (value == null) ? "" : value;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 获取字符串配置值，配置不存在时返回默认值
     * @param name 配置项名称，格式通常为category.item
     * @param defaultValue 默认值
     * @return
     * 返回值: String
     * 返回值说明: 配置值存在时返回配置值，否则返回默认值
     */
    public static String getString(String name, String defaultValue)
    {
        String value = (String) items.get(name);
        if (value != null && !value.isEmpty())
            return value;
        else
            return defaultValue;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 获取整数配置值，解析失败时返回0
     * @param name 配置项名称，格式通常为category.item
     * @return
     * 返回值: int
     * 返回值说明: 配置值解析后的整数，解析失败时返回0
     */
    public static int getInt(String name)
    {
        String value = getString(name);
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex)
        {
            logger.error("parse int config failed, key[{}] return 0", name, ex);
            return 0;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 获取整数配置值，配置不存在或解析失败时返回默认值
     * @param name 配置项名称，格式通常为category.item
     * @param defaultValue 默认值
     * @return
     * 返回值: int
     * 返回值说明: 配置值解析后的整数，配置不存在或解析失败时返回默认值
     */
    public static int getInt(String name, int defaultValue)
    {
        String value = getString(name);
        if(value.isEmpty()) {
            return defaultValue;
        }
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex)
        {
            logger.error("parse int config failed, key[{}] return {}", name, defaultValue, ex);
        }
        return defaultValue;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 获取布尔配置值
     * @param name 配置项名称，格式通常为category.item
     * @return
     * 返回值: boolean
     * 返回值说明: 配置值为true时返回true，其它值返回false
     */
    public static boolean getBoolean(String name)
    {
        String value = getString(name);
        return Boolean.parseBoolean(value);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 获取小数配置值，配置不存在或解析失败时返回默认值
     * @param name 配置项名称，格式通常为category.item
     * @param defaultValue 默认值
     * @return
     * 返回值: double
     * 返回值说明: 配置值解析后的小数，配置不存在或解析失败时返回默认值
     */
    public static double getDouble(String name, double defaultValue)
    {
        String value = getString(name);
        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException ex)
        {
            logger.error("parse double config failed, key[{}] return {}", name, defaultValue, ex);
        }
        return defaultValue;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 获取全部XML配置缓存
     * @return
     * 返回值: Map
     * 返回值说明: 当前已加载的配置项缓存
     */
    @SuppressWarnings("rawtypes")
    public static Map getItems()
    {
        return items;
    }
}
