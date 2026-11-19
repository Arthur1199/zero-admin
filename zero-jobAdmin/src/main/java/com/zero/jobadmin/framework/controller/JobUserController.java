package com.zero.jobadmin.framework.controller;

import com.zero.jobadmin.framework.constant.Consts;
import com.zero.jobadmin.business.mapper.XxlJobGroupMapper;
import com.zero.jobadmin.framework.mapper.XxlJobUserMapper;
import com.zero.jobadmin.business.model.XxlJobGroup;
import com.zero.jobadmin.framework.model.XxlJobUser;
import com.zero.jobadmin.framework.util.I18nUtil;
import com.zero.jobadmin.framework.util.PageQueryUtil;
import com.xxl.sso.core.annotation.XxlSso;
import com.xxl.sso.core.helper.XxlSsoHelper;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.crypto.Sha256Tool;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 提供调度控制台的用户和执行器组授权管理入口。
 */
@Controller
@RequestMapping("/user")
public class JobUserController {

    @Resource
    private XxlJobUserMapper xxlJobUserMapper;
    @Resource
    private XxlJobGroupMapper xxlJobGroupMapper;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为调度管理员打开用户页，并加载用户授权表单使用的执行器组列表。
     * @param model 用于提供执行器组列表的视图模型
     * @return
     * 返回值: String
     * 返回值说明: 用户管理页的视图名
     */
    @RequestMapping
    @XxlSso(role = Consts.ADMIN_ROLE)
    public String index(Model model) {

        // 执行器列表
        List<XxlJobGroup> groupList = xxlJobGroupMapper.findAll();
        model.addAttribute("groupList", groupList);

        return "business/user.list";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:06
     * 说明: 管理员在同一数据库快照中查询用户列表和总数，仅返回管理所需字段。
     * @param offset 分页起始位置，默认 0
     * @param pagesize 每页条数，默认 10，最大 500
     * @param username 账号筛选值
     * @param role 角色筛选值，-1 为全部角色
     * @return
     * 返回值: Response<PageModel<XxlJobUser>>
     * 返回值说明: 用户分页结果，移除密码和登录令牌
     */
    @RequestMapping("/pageList")
    @ResponseBody
    @XxlSso(role = Consts.ADMIN_ROLE)
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Response<PageModel<XxlJobUser>> pageList(@RequestParam(required = false, defaultValue = "0") int offset,
                                                    @RequestParam(required = false, defaultValue = "10") int pagesize,
                                                    @RequestParam String username,
                                                    @RequestParam int role) {

        if (PageQueryUtil.isInvalid(offset, pagesize)) {
            return Response.ofFail(I18nUtil.getString("system_page_invalid"));
        }
        // page list
        List<XxlJobUser> list = xxlJobUserMapper.pageList(offset, pagesize, username, role);
        int list_count = xxlJobUserMapper.pageListCount(offset, pagesize, username, role);

        // filter
        if (list!=null && !list.isEmpty()) {
            for (XxlJobUser item: list) {
                item.setPassword(null);
                item.setToken(null);
            }
        }

        // package result
        PageModel<XxlJobUser> pageModel = new PageModel<>();
        pageModel.setData(list);
        pageModel.setTotal(list_count);

        return Response.ofSuccess(pageModel);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:06
     * 说明: 管理员新增用户，沿用现有密码摘要格式，并处理并发新增同名账号。
     * @param xxlJobUser 账号、初始密码、角色和执行器权限
     * @return
     * 返回值: Response<String>
     * 返回值说明: 新增结果或输入错误提示
     */
    @RequestMapping("/insert")
    @ResponseBody
    @XxlSso(role = Consts.ADMIN_ROLE)
    public Response<String> insert(XxlJobUser xxlJobUser) {
        if (xxlJobUser.getRole() != 0 && xxlJobUser.getRole() != 1) {
            return Response.ofFail(I18nUtil.getString("user_role") + I18nUtil.getString("system_invalid"));
        }

        // valid username
        if (StringTool.isBlank(xxlJobUser.getUsername())) {
            return Response.ofFail(I18nUtil.getString("system_please_input")+I18nUtil.getString("user_username") );
        }
        xxlJobUser.setUsername(xxlJobUser.getUsername().trim());
        if (!(xxlJobUser.getUsername().length()>=4 && xxlJobUser.getUsername().length()<=20)) {
            return Response.ofFail(I18nUtil.getString("system_length_limit")+"[4-20]" );
        }
        // valid password
        if (StringTool.isBlank(xxlJobUser.getPassword())) {
            return Response.ofFail(I18nUtil.getString("system_please_input")+I18nUtil.getString("user_password") );
        }
        xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
        if (!(xxlJobUser.getPassword().length()>=4 && xxlJobUser.getPassword().length()<=20)) {
            return Response.ofFail(I18nUtil.getString("system_length_limit")+"[4-20]" );
        }
        // 沿用登录校验使用的 SHA-256 摘要格式。
        String passwordHash = Sha256Tool.sha256(xxlJobUser.getPassword());
        xxlJobUser.setPassword(passwordHash);

        // check repeat
        XxlJobUser existUser = xxlJobUserMapper.loadByUserName(xxlJobUser.getUsername());
        if (existUser != null) {
            return Response.ofFail( I18nUtil.getString("user_username_repeat") );
        }

        try {
            return xxlJobUserMapper.save(xxlJobUser) > 0
                    ? Response.ofSuccess() : Response.ofFail(I18nUtil.getString("system_opt_fail"));
        } catch (DuplicateKeyException e) {
            return Response.ofFail(I18nUtil.getString("user_username_repeat"));
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/18 20:06
     * 说明: 管理员更新其他用户的角色、执行器权限或密码，以用户 ID 拦截本人操作。
     * @param request 当前登录请求
     * @param xxlJobUser 要更新的用户配置，空密码表示保留已有密码
     * @return
     * 返回值: Response<String>
     * 返回值说明: 更新结果，目标不存在或更新没有命中时返回失败
     */
    @RequestMapping("/update")
    @ResponseBody
    @XxlSso(role = Consts.ADMIN_ROLE)
    public Response<String> update(HttpServletRequest request, XxlJobUser xxlJobUser) {

        Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
        LoginInfo loginUser = loginInfoResponse.getData();
        if (!loginInfoResponse.isSuccess() || loginUser == null) {
            return Response.ofFail(I18nUtil.getString("system_permission_limit"));
        }
        if (String.valueOf(xxlJobUser.getId()).equals(loginUser.getUserId())) {
            return Response.ofFail(I18nUtil.getString("user_update_loginuser_limit"));
        }
        if (xxlJobUser.getId() <= 0) {
            return Response.ofFail(I18nUtil.getString("system_opt_fail"));
        }
        if (xxlJobUser.getRole() != 0 && xxlJobUser.getRole() != 1) {
            return Response.ofFail(I18nUtil.getString("user_role") + I18nUtil.getString("system_invalid"));
        }

        // valid password
        if (StringTool.isNotBlank(xxlJobUser.getPassword())) {
            xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
            if (!(xxlJobUser.getPassword().length()>=4 && xxlJobUser.getPassword().length()<=20)) {
                return Response.ofFail(I18nUtil.getString("system_length_limit")+"[4-20]" );
            }
            // 沿用登录校验使用的 SHA-256 摘要格式。
            String passwordHash = Sha256Tool.sha256(xxlJobUser.getPassword());
            xxlJobUser.setPassword(passwordHash);
        } else {
            xxlJobUser.setPassword(null);
        }

        return xxlJobUserMapper.update(xxlJobUser) > 0
                ? Response.ofSuccess() : Response.ofFail(I18nUtil.getString("system_opt_fail"));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 由调度管理员删除一个目标用户，禁止删除当前登录账号。
     * @param request 携带当前调度登录信息的请求
     * @param ids 待删除的用户 ID 列表，只接受一项
     * @return
     * 返回值: Response<String>
     * 返回值说明: 删除结果或无法删除的原因
     */
    @RequestMapping("/delete")
    @ResponseBody
    @XxlSso(role = Consts.ADMIN_ROLE)
    public Response<String> delete(HttpServletRequest request, @RequestParam("ids[]") List<Integer> ids) {

        // valid
        if (CollectionTool.isEmpty(ids) || ids.size()!=1) {
            return Response.ofFail(I18nUtil.getString("system_please_choose") + I18nUtil.getString("system_one") + I18nUtil.getString("system_data"));
        }

        // avoid opt login seft
        Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
        if (ids.contains(Integer.parseInt(loginInfoResponse.getData().getUserId()))) {
            return Response.ofFail(I18nUtil.getString("user_update_loginuser_limit"));
        }

        xxlJobUserMapper.delete(ids.get(0));
        return Response.ofSuccess();
    }

    /*@RequestMapping("/updatePwd")
    @ResponseBody
    public Response<String> updatePwd(HttpServletRequest request,
                                     @RequestParam("password") String password,
                                     @RequestParam("oldPassword") String oldPassword){

        // valid
        if (oldPassword==null || oldPassword.trim().isEmpty()){
            return Response.ofFail(I18nUtil.getString("system_please_input") + I18nUtil.getString("change_pwd_field_oldpwd"));
        }
        if (password==null || password.trim().isEmpty()){
            return Response.ofFail(I18nUtil.getString("system_please_input") + I18nUtil.getString("change_pwd_field_oldpwd"));
        }
        password = password.trim();
        if (!(password.length()>=4 && password.length()<=20)) {
            return Response.ofFail(I18nUtil.getString("system_length_limit")+"[4-20]" );
        }

        // md5 password
        String oldPasswordHash = Sha256Tool.sha256(oldPassword);
        String passwordHash = Sha256Tool.sha256(password);

        // valid old pwd
        Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
        XxlJobUser existUser = xxlJobUserMapper.loadByUserName(loginInfoResponse.getData().getUserName());
        if (!oldPasswordHash.equals(existUser.getPassword())) {
            return Response.ofFail(I18nUtil.getString("change_pwd_field_oldpwd") + I18nUtil.getString("system_invalid"));
        }

        // write new
        existUser.setPassword(passwordHash);
        xxlJobUserMapper.update(existUser);

        return Response.ofSuccess();
    }*/

}
