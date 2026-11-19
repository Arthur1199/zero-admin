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
 * common exception resolver
 *
 * @author xuxueli 2016-1-6 19:22:18
 */
@Slf4j
@Component
public class WebHandlerExceptionResolver implements HandlerExceptionResolver {
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