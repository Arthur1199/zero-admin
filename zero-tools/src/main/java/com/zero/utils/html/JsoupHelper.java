package com.zero.utils.html;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson2.JSON;
import com.zero.utils.http.HttpClientHelper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/17 00:10
 * 说明: 从网页刷抓取html数据或者解析xml文件的工具类。爬虫工具
 */
public class JsoupHelper {

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 00:10
	 * @param httpUrl 网络地址
	 * @return
	 * 返回值:Document
	 * 返回值说明：发送不带参数的get请求，并且抓取网络数据，将抓取回来的数据转换成Document对象。
	 */
	public static Document getDocumentByGet(String httpUrl) {
		String responseContent = HttpClientHelper.sendHttpGet(httpUrl);
		if (responseContent != null && !responseContent.trim().isEmpty()) {
            return Jsoup.parse(responseContent);
        }
		return null;
	}

	/**
	 * Author:Jack Time:2017年9月27日上午10:49:57
	 *
	 * @param httpUrl
	 * @param maps
	 *            如果不需要传入数据，传入null即可
	 * @return Return:Document
	 *         Description:发送get请求，并且抓取网络数据，将抓取回来的数据转换成Document对象。
	 */
	public static Document getDocumentByGet(String httpUrl, Map<String, String> maps) {
		String responseContent = HttpClientHelper.sendHttpGet(httpUrl, maps);
		if (responseContent != null && !responseContent.trim().isEmpty()) {
            return Jsoup.parse(responseContent);
        }
		return null;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 00:11
	 * @param httpUrl 请求地址
	 * @return
	 * 返回值:Document
	 * 返回值说明：发送不带参数的Post请求，并且抓取网络数据，将抓取回来的数据转换成Document对象。
	 */
	public static Document getDocumentByPost(String httpUrl) {
		String responseContent = HttpClientHelper.sendHttpPost(httpUrl);
		if (responseContent != null && !responseContent.trim().isEmpty()) {
            return Jsoup.parse(responseContent);
        }
		return null;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 00:11
	 * @param httpUrl 请求地址
	 * @param maps 请求参数
	 * @return
	 * 返回值:Document
	 * 返回值说明：发送带参数的Post请求，并且抓取网络数据，将抓取回来的数据转换成Document对象。
	 */
	public static Document getDocumentByPost(String httpUrl, Map<String, String> maps) {
		String responseContent = HttpClientHelper.sendHttpPost(httpUrl, maps);
		if (responseContent != null && !responseContent.trim().isEmpty()) {
            return Jsoup.parse(responseContent);
        }
		return null;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 00:12
	 * @param httpUrl 请求地址
	 * @param maps 请求参数
	 * @return
	 * 返回值:Document
	 * 返回值说明：:发送带参数并且参数是Json格式数据的Post请求，并且抓取网络数据，将抓取回来的数据转换成Document对象。
	 */
	public static Document getDocumentByPostJson(String httpUrl, Map<String, String> maps) {
		String responseContent = HttpClientHelper.sendHttpPostJson(httpUrl,
				maps == null ? null : JSON.toJSONString(maps));
		if (responseContent != null && !responseContent.trim().isEmpty()) {
            return Jsoup.parse(responseContent);
        }
		return null;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 00:12
	 * @param filePath 文件路径
	 * @param charset 字符集
	 * @return
	 * 返回值:Document
	 * 返回值说明：从本地路径中读取xml文件，并且转换成Document对象
	 */
	public static Document getDocumentFromXmlFile(String filePath, String charset) {
		File xmlFile = new File(filePath);
		try {
            return Jsoup.parse(xmlFile, charset);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/17 00:12
	 * @param xmlFile 文件路径
	 * @param charset 字符集
	 * @return
	 * 返回值:Document
	 * 返回值说明：从本地文件中读取xml文件，并且转换成Document对象
	 */
	public static Document getDocumentFromXmlFile(File xmlFile, String charset) {
		try {
			Document doc = Jsoup.parse(xmlFile, charset);
			return doc;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
