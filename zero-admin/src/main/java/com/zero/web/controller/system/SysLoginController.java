package com.zero.web.controller.system;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.constant.Constants;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.domain.entity.SysMenu;
import com.zero.common.core.domain.entity.SysUser;
import com.zero.common.core.domain.model.LoginBody;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.text.Convert;
import com.zero.common.exception.ServiceException;
import com.zero.common.utils.DateUtils;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.annotation.SystemModule;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.framework.web.service.SysLoginService;
import com.zero.framework.web.service.SysPermissionService;
import com.zero.framework.web.service.TokenService;
import com.zero.system.service.ISysConfigService;
import com.zero.system.service.ISysUserService;
import com.zero.system.service.ISysMenuService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 登录验证
 */
@RestController
@SystemModule(SystemModuleEnum.AUTH)
public class SysLoginController
{
    /**
     * 登录服务字段
     */
    @Autowired
    private SysLoginService loginService;
    /**
     * 菜单服务字段
     */

    @Autowired
    private ISysMenuService menuService;
    /**
     * 权限服务字段
     */

    @Autowired
    private SysPermissionService permissionService;
    /**
     * 令牌服务字段
     */

    @Autowired
    private TokenService tokenService;
    /**
     * 参数配置服务字段
     */

    @Autowired
    private ISysConfigService configService;

    /** 刷新页面时读取最新用户部门和角色，避免沿用旧登录快照。 */
    @Autowired
    private ISysUserService userService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 登录方法
     * @param loginBody 登录信息
     * @return
     * 返回值: AjaxResult
     * 返回值说明：登录方法
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // loginService 内部会在生成令牌前完成账号密码、验证码和谷歌动态码校验。
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid(), loginBody.getGoogleCode());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 读取最新用户部门与角色并返回权限，变化时通过当前HTTP请求更新登录缓存
     * @return
     * 返回值: AjaxResult
     * 返回值说明：获取用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser previous = loginUser.getUser();
        SysUser user = userService.selectUserById(loginUser.getUserId());
        if (user == null)
        {
            throw new ServiceException("用户不存在，请重新登录");
        }
        // 计算每个角色权限后再比较，合并权限相同也可能发生角色数据范围变更。
        Set<String> roles = permissionService.getRolePermission(user);
        Set<String> permissions = permissionService.getMenuPermission(user);
        boolean userChanged = !Objects.equals(loginUser.getDeptId(), user.getDeptId())
                || !SysPermissionService.sameRoleAuthorization(previous == null ? null : previous.getRoles(), user.getRoles());
        loginUser.setUser(user);
        loginUser.setDeptId(user.getDeptId());
        if (userChanged || !Objects.equals(loginUser.getPermissions(), permissions))
        {
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        ajax.put("isDefaultModifyPwd", initPasswordIsModify(user.getPwdUpdateDate()));
        ajax.put("isPasswordExpired", passwordIsExpiration(user.getPwdUpdateDate()));
        return ajax;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取路由信息
     * @return
     * 返回值: 路由信息
     * 返回值说明：获取路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }

    // 检查初始密码是否提醒修改
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 初始化密码IsModify
     * @param pwdUpdateDate 密码修改日期参数
     * @return
     * 返回值: boolean
     * 返回值说明：初始化密码IsModify
     */
    public boolean initPasswordIsModify(Date pwdUpdateDate)
    {
        Integer initPasswordModify = Convert.toInt(configService.selectConfigByKey("sys.account.initPasswordModify"));
        return initPasswordModify != null && initPasswordModify == 1 && pwdUpdateDate == null;
    }

    // 检查密码是否过期
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 密码IsExpiration
     * @param pwdUpdateDate 密码修改日期参数
     * @return
     * 返回值: boolean
     * 返回值说明：密码IsExpiration
     */
    public boolean passwordIsExpiration(Date pwdUpdateDate)
    {
        Integer passwordValidateDays = Convert.toInt(configService.selectConfigByKey("sys.account.passwordValidateDays"));
        if (passwordValidateDays != null && passwordValidateDays > 0)
        {
            if (StringUtils.isNull(pwdUpdateDate))
            {
                // 如果从未修改过初始密码，直接提醒过期
                return true;
            }
            Date nowDate = DateUtils.getNowDate();
            return DateUtils.differentDaysByMillisecond(nowDate, pwdUpdateDate) > passwordValidateDays;
        }
        return false;
    }
}
