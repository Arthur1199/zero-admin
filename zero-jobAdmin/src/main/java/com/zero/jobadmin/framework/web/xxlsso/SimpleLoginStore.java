package com.zero.jobadmin.framework.web.xxlsso;

import com.zero.jobadmin.framework.constant.Consts;
import com.zero.jobadmin.framework.mapper.XxlJobUserMapper;
import com.zero.jobadmin.framework.model.XxlJobUser;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.sso.core.store.LoginStore;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 使用数据库保存和查询 XXL-SSO 登录会话，供独立控制台验证登录状态。
 *
 * Simple LoginStore
 *
 * 1、store by database；
 * 2、If you have higher performance requirements, it is recommended to use RedisLoginStore；
 */
@Component
public class SimpleLoginStore implements LoginStore {


    @Resource
    private XxlJobUserMapper xxlJobUserMapper;


    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按用户 ID 保存登录会话签名。
     * @param loginInfo 包含用户 ID 和签名的 SSO 登录信息
     * @return
     * 返回值: Response<String>
     * 返回值说明: 写入到用户记录时返回成功
     */
    @Override
    public Response<String> set(LoginInfo loginInfo) {

        // parse token-signature
        String token_sign = loginInfo.getSignature();

        // write token by UserId
        int ret = xxlJobUserMapper.updateToken(Integer.parseInt(loginInfo.getUserId()), token_sign);
        return ret > 0 ? Response.ofSuccess() : Response.ofFail("token set fail");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 接收通用登录存储接口的会话更新请求，当前实现未提供更新入口。
     * @param loginInfo 调用方传入的会话更新内容
     * @return
     * 返回值: Response<String>
     * 返回值说明: 固定返回不支持更新
     */
    @Override
    public Response<String> update(LoginInfo loginInfo) {
        return Response.ofFail("not support");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按用户 ID 清空数据库中存储的会话签名。
     * @param userId 可转换为整型的用户主键文本
     * @return
     * 返回值: Response<String>
     * 返回值说明: 更新到用户记录时返回成功
     */
    @Override
    public Response<String> remove(String userId) {
        // delete token-signature
        int ret = xxlJobUserMapper.updateToken(Integer.parseInt(userId), "");
        return ret > 0 ? Response.ofSuccess() : Response.ofFail("token remove fail");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按用户 ID 查询账号及已保存的登录信息。
     * @param userId 可转换为整型的用户主键文本
     * @return
     * 返回值: Response<LoginInfo>
     * 返回值说明: 带会话信息的响应，账号不存在时返回失败
     */
    @Override
    public Response<LoginInfo> get(String userId) {

        // load login-user
        XxlJobUser user = xxlJobUserMapper.loadById(Integer.parseInt(userId));
        if (user == null) {
            return Response.ofFail("userId invalid.");
        }

        // parse role
        List<String> roleList = user.getRole()==1? List.of(Consts.ADMIN_ROLE):null;

        // parse jobGroup permission
        Map<String, String> extraInfo = MapTool.newMap(
                "jobGroups", user.getPermission()
        );

        // build LoginInfo
        LoginInfo loginInfo = new LoginInfo(userId, user.getToken());
        loginInfo.setUserName(user.getUsername());
        loginInfo.setRoleList(roleList);
        loginInfo.setExtraInfo(extraInfo);

        return Response.ofSuccess(loginInfo);
    }

}
