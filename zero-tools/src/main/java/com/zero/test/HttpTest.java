package com.zero.test;

import com.zero.utils.http.HttpClientHelper;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: ??: Arthur ??
 *
 * ??: 2026/5/16 14:05
 * ??: HTTP??????????????????????????????
 */
public class HttpTest {

	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 用示例地址调用 HTTP GET。
	 * @param args 命令行参数，示例不读取
	 * @return
	 * 返回值: void
	 * 返回值说明: 输出示例请求返回内容
	 */
	public static void main(String[] args) {

//		while(true) {
//			String sendGet = HttpClientHelper.sendHttpGet("https://t78910.com/app/member/include/validatecode/captcha.php?v=0.3658085575831137", null);
			String sendGet = HttpClientHelper.sendHttpGet("https://t78910.com/app/member/include/validatecode/captcha.php?v=0.3658085575831137");
			System.out.println(sendGet);

//		}

//		for(int i = 0;i<100;i++) {
//
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//			String sendHttpPost = HttpClientHelper.sendHttpPost("http://khgateway.feixuncloud.com/codeComputer/betOrder");
//
//			System.out.println(sendHttpPost);
//
//
//		}

	}

}
