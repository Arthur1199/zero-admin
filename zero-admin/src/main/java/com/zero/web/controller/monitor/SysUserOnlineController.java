package com.zero.web.controller.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import com.zero.common.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.annotation.Log;
import com.zero.common.annotation.SystemModule;
import com.zero.common.constant.CacheConstants;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.core.redis.RedisCache;
import com.zero.common.enums.BusinessType;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.SysUserOnline;
import com.zero.system.service.ISysUserOnlineService;
import com.zero.system.service.AdminUserLogoutService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 在线用户监控
 */
@RestController
@RequestMapping("/monitor/online")
@SystemModule(SystemModuleEnum.MONITOR)
public class SysUserOnlineController extends BaseController
{
    /**
     * 用户在线用户服务字段
     */
    @Autowired
    private ISysUserOnlineService userOnlineService;
    /**
     * Redis缓存字段
     */

    @Autowired
    private RedisCache redisCache;

    /** 精确撤销所选会话，并跨节点通知其全部标签页退出。 */
    @Autowired
    private AdminUserLogoutService userLogoutService;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 向具有在线用户菜单权限的账号返回所有会话及完整JWT，响应禁止缓存且不记录响应日志
     * @param ipaddr IP地址参数
     * @param userName 用户名称参数
     * @param response 响应对象，用于禁止浏览器和代理缓存授权令牌
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：在线会话列表，包含不遮挡的完整授权令牌
     */

    @RequiresPermissions("monitor:online:list")
    @GetMapping("/list")
    public TableDataInfo list(String ipaddr, String userName, HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys)
        {
            LoginUser user = redisCache.getCacheObject(key);
            // 扫描后会话可能并发过期或被强退，跳过失效缓存。
            if (user == null || user.getUser() == null)
            {
                continue;
            }
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName))
            {
                userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
            }
            else if (StringUtils.isNotEmpty(ipaddr))
            {
                userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
            }
            else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser()))
            {
                userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
            }
            else
            {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return getDataTable(userOnlineList);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 强退选中的登录会话并通知其客户端退出，不影响同账号其它登录会话
     * @param tokenId 令牌ID参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：强退用户
     */
    @RequiresPermissions("monitor:online:forceLogout")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@PathVariable String tokenId)
    {
        userLogoutService.forceLogout(tokenId);
        return success();
    }
}
