package com.zero.utils.xml.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 21:30
 * 说明: 配置文件查找工具，按类路径、类所在包、当前工作目录和系统classpath顺序查找配置文件
 */
public class PropHelper
{
    private static final Logger logger = LoggerFactory.getLogger(PropHelper.class);

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/16 21:30
     * 说明: 猜测并打开属性文件输入流，找不到文件或读取异常时返回null
     * @param cls 与配置文件相关的类，用于推断包路径和类加载器
     * @param propFile 需要查找的配置文件名
     * @return
     * 返回值: InputStream
     * 返回值说明: 配置文件输入流，查找失败时返回null
     */
    public static InputStream guessPropFile(Class<?> cls, String propFile)
    {
        InputStream inputStream = null;
        File f = null;
        try
        {
            ClassLoader loader = cls.getClassLoader();
            URL url = loader.getResource(propFile);
            if (url != null)
            {
                f = new File(url.getPath());
                if (f != null && f.exists() && f.isFile())
                {
                    return new FileInputStream(f);
                }
            }

            Package pack = cls.getPackage();
            if (pack != null)
            {
                String packName = pack.getName();
                String path = "";
                if (packName.indexOf(".") < 0)
                {
                    path = packName + "/";
                }
                else
                {
                    int start = 0, end = 0;
                    end = packName.indexOf(".");
                    while (end != -1)
                    {
                        path = path + packName.substring(start, end) + "/";
                        start = end + 1;
                        end = packName.indexOf(".", start);
                    }
                    path = path + packName.substring(start) + "/";
                }
                url = loader.getResource(path + propFile);
                if (url != null)
                {
                    f = new File(url.getPath());
                    if (f != null && f.exists() && f.isFile())
                    {
                        return new FileInputStream(f);
                    }
                }
            }

            String curDir = System.getProperty("user.dir");
            f = new File(curDir, propFile);
            if (f != null && f.exists() && f.isFile())
            {
                return new FileInputStream(f);
            }

            String classpath = System.getProperty("java.class.path");
            String[] cps = classpath.split(System.getProperty("path.separator"));
            for (int i = 0; i < cps.length; i++)
            {
                f = new java.io.File(cps[i], propFile);
                if (f != null && f.exists() && f.isFile())
                {
                    break;
                }
                f = null;
            }
            logger.info("PropHelper local file: {}", f != null ? f.getName() : "NULL");
            if(f == null){
                try {
                    Resource resource = new ClassPathResource(propFile);
                    if(resource.exists()){
                        inputStream = resource.getInputStream();
                        logger.info("Resource classpath file: {}", propFile);
                        return inputStream;
                    }
                } catch (Exception e) {
                    logger.error("get config file failed, file does not exist or load failed");
                }
            }else{
                return new FileInputStream(f);
            }
        }
        catch (Exception e)
        {
            logger.error("get config file failed, file does not exist or load failed");
            f = null;
        }
        return inputStream;
    }
}
