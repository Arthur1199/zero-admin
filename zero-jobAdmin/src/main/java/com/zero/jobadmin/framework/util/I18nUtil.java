package com.zero.jobadmin.framework.util;

import lombok.extern.slf4j.Slf4j;
import com.xxl.job.core.constant.ExecutorBlockStrategyEnum;
import com.xxl.tool.core.PropTool;
import com.xxl.tool.freemarker.FtlTool;
import com.xxl.tool.json.GsonTool;
import freemarker.template.Configuration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 读取调度控制台的国际化资源，供页面和接口生成本地化提示。
 */
@Slf4j
@Component
public class I18nUtil implements InitializingBean {
    // ---------------------- for i18n config ----------------------

    /**
     * i18n config
     */
    @Value("${xxl.job.i18n}")
    private String i18n;

    /**
     * freemarker config
     */
    @Autowired
    private Configuration configuration;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在 Spring 完成配置后记录当前国际化工具实例。
     * @return
     * 返回值: void
     * 返回值说明: 初始化静态查找入口
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // init freemarker shared variable
        configuration.setSharedVariable("I18nUtil", FtlTool.generateStaticModel(I18nUtil.class.getName()));
        // init single
        single = this;

        // init i18n-enum
        initI18nEnum();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: get i18n
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public String getI18n() {
        if (!Arrays.asList("zh_CN", "zh_TC", "en").contains(i18n)) {
            return "zh_CN";
        }
        return i18n;
    }

    private static I18nUtil single = null;
    private static I18nUtil getSingle() {
        return single;
    }

    // ---------------------- tool ----------------------

    private static Properties prop = null;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 从国际化资源读取当前语言配置。
     * @return
     * 返回值: Properties
     * 返回值说明: 当前语言的键值配置
     */
    public static Properties loadI18nProp(){
        if (prop != null) {
            return prop;
        }
        // build i18n filepath
        String i18n = getSingle().getI18n();
        String i18nFile = MessageFormat.format("i18n/message_{0}.properties", i18n);

        // load prop
        prop = PropTool.loadProp(i18nFile);
        return prop;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: get val of i18n key
     * @param key 用于查找的键
     * @return
     * 返回值: String
     * 返回值说明: 按上述规则读取或解析得到的文本
     */
    public static String getString(String key) {
        if (single==null) {
            log.debug(">>>>>>>>>>> xxl-job i18n fail, i18n not init.");
            return null;
        }

        // load
        return loadI18nProp().getProperty(key);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 一次读取多个国际化文案，未指定键时读取全部。
     * @param keys 要读取的国际化键，可省略
     * @return
     * 返回值: String
     * 返回值说明: 国际化文案映射的 JSON 字符串，组件未初始化时返回 null
     */
    public static String getMultString(String... keys) {
        if (single==null) {
            log.debug(">>>>>>>>>>> xxl-job i18n fail, i18n not init.");
            return null;
        }

        // build
        Map<String, String> map = new HashMap<>();

        Properties prop = loadI18nProp();
        if (keys!=null && keys.length>0) {
            for (String key: keys) {
                map.put(key, prop.getProperty(key));
            }
        } else {
            for (String key: prop.stringPropertyNames()) {
                map.put(key, prop.getProperty(key));
            }
        }

        return GsonTool.toJson(map);
    }


    // ---------------------- init I18n-enum ----------------------

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将执行器阻塞策略的展示标题替换为当前语言的文案。
     * @return
     * 返回值: void
     * 返回值说明: 更新相关阻塞策略枚举的显示标题
     */
    private void initI18nEnum(){
        for (ExecutorBlockStrategyEnum item : ExecutorBlockStrategyEnum.values()) {
            item.setTitle(I18nUtil.getString("jobconf_block_".concat(item.name())));
        }
    }

}
