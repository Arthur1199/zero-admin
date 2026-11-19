package com.zero.jobadmin.framework.web.error;

import lombok.extern.slf4j.Slf4j;
import com.zero.jobadmin.business.scheduler.exception.XxlJobException;
import com.xxl.tool.json.GsonTool;
import com.xxl.tool.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 将调度控制台的异常转换为对应的 Web 响应或错误页面。
 */
@Slf4j
@Component
public class WebHandlerExceptionResolver implements HandlerExceptionResolver {
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/11/20 01:36
	 * 说明: 根据处理方法是否声明响应体决定返回 JSON 错误还是控制台错误页面。
	 * @param request 当前控制台请求
	 * @param response 用于输出异常数据的响应
	 * @param handler 请求匹配到的处理器
	 * @param ex 控制台处理过程中发生的异常
	 * @return
	 * 返回值: ModelAndView
	 * 返回值说明: 视图响应，已直接写 JSON 时返回空视图对象
	 */
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

		if (!(ex instanceof XxlJobException)) {
			log.error("WebExceptionResolver:{}", ex);
		}

		// parse isJson
		boolean isJson = false;
		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod)handler;
			isJson = method.getMethodAnnotation(ResponseBody.class)!=null;
		}

		// process error
		ModelAndView mv = new ModelAndView();
		if (isJson) {
			try {
				// errorMsg
				String errorMsg = GsonTool.toJson(Response.ofFail(ex.toString()));

				// write response
				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().println(errorMsg);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
			return mv;
		} else {

			mv.addObject("exceptionMsg", ex.toString());
			mv.setViewName("framework/common/common.errorpage");
			return mv;
		}

	}
	
}