package com.zero.util;

import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;



/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 按类加载器和本地目录查找监控服务使用的配置文件。
 */
@Slf4j
public class PropHelper
{
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 依次从类路径根目录、指定类所在包、本地工作目录和类路径目录寻找配置，最后尝试直接读取类路径资源。
	 * @param cls 用于定位类加载器及搜索包目录的类
	 * @param propFile 要查找的配置文件名
	 * @return
	 * 返回值: InputStream
	 * 返回值说明: 找到时返回配置输入流，由调用方关闭，未读到时返回 null
	 */
	public static InputStream guessPropFile(Class<?> cls, String propFile)
	{
		InputStream inputStream = null;
		File f = null;
		try
		{
			//得到类的类装载器
			ClassLoader loader = cls.getClassLoader();
			//先从当前类所处路径的根目录中寻找属性文件
			URL url = loader.getResource(propFile);
			if (url != null)
			{
				f = new File(url.getPath());
				if (f != null && f.exists() && f.isFile())
				{
					return new FileInputStream(f);
				}
			}
			//没有找到，就从该类所处的包目录中查找属性文件
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
			//如果没有找到，再从当前系统的用户目录中进行查找
			String curDir = System.getProperty("user.dir");
			f = new File(curDir, propFile);
			if (f != null && f.exists() && f.isFile())
			{
				return new FileInputStream(f);
			}
			//如果还是没有找到，则从系统所有的类路径中查找
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
			log.info("PropHelper本地文件File:{}", f!= null ? f.getName() : "NULL");
			if(f == null){
				try {
					Resource resource = new ClassPathResource(propFile);
					if(resource != null){
						inputStream = resource.getInputStream();
						File file2 = resource.getFile();
						log.info("Resource本地文件File:{}", file2!= null ? file2.getName() : "NULL");
						if(inputStream != null){
							return inputStream;
						}
					}
				} catch (Exception e) {
//					e.printStackTrace();
//					log.error("获取文件出错，文件不存在或者加载错误！", e);
					log.error("获取文件出错，文件不存在或者加载错误！");
				}
			}else{
				return new FileInputStream(f);
			}
		}
		catch (Exception e)
		{
//			log.error("获取文件出错，文件不存在或者加载错误！", e);
			log.error("获取文件出错，文件不存在或者加载错误！");
			f = null;
		}
		return inputStream;
	}
}
