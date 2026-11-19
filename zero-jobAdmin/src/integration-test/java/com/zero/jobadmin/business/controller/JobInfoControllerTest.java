package com.zero.jobadmin.business.controller;

import lombok.extern.slf4j.Slf4j;
import com.xxl.sso.core.constant.Const;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

// 官方集成用例可能操作数据库或执行器，普通打包按标签排除。
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 通过 MockMvc 登录调度控制台，并使用登录会话调用新增任务接口。运行时会访问集成测试配置的数据源。
 */
@Slf4j
@org.junit.jupiter.api.Tag("job-integration")
public class JobInfoControllerTest extends AbstractSpringMvcTest {
  private Cookie cookie;

  @BeforeEach
  public void login() throws Exception {
    MvcResult ret = mockMvc.perform(
        post("/auth/doLogin")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("userName", "admin")
            .param("password", "123456")
    ).andReturn();
    cookie = ret.getResponse().getCookie(Const.XXL_SSO_TOKEN);
  }

  @Test
  public void testAdd() throws Exception {
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
    parameters.add("jobGroup", "1");
    parameters.add("triggerStatus", "-1");

    MvcResult ret = mockMvc.perform(
        post("/jobinfo/pageList")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            //.content(paramsJson)
            .params(parameters)
            .cookie(cookie)
    ).andReturn();

    log.info(ret.getResponse().getContentAsString());
  }

}
