package com.zero.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.zero.common.annotation.Log;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.annotation.SystemModule;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.enums.SystemModuleEnum;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.GoogleBindBody;
import com.zero.system.domain.SysGoogle;
import com.zero.system.service.ISysGoogleService;
import com.zero.system.service.ISysUserService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/20 01:36
 * 说明: 管理用户的谷歌动态码绑定，并提供登录页首次绑定所需接口。
 */
@RestController
@SystemModule(SystemModuleEnum.USER)
public class SysGoogleController extends BaseController
{
    @Autowired
    private ISysGoogleService googleService;

    @Autowired
    private ISysUserService userService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 具备谷歌绑定列表权限时，分页查询用户的绑定状态。
     * @param sysGoogle 绑定列表的筛选条件
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 谷歌动态码绑定列表及总数
     */
    @RequiresPermissions("system:google:list")
    @GetMapping("/system/google/list")
    public TableDataInfo list(SysGoogle sysGoogle)
    {
        startPage();
        List<SysGoogle> list = googleService.selectGoogleList(sysGoogle);
        return getDataTable(list);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在校验密钥查看权限和用户数据权限后返回绑定密钥，操作日志关闭响应记录。
     * @param userId 待查看密钥的用户 ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 成功时 data 为所查用户的绑定密钥
     */
    @RequiresPermissions("system:google:viewKey")
    @Log(title = "查看谷歌Key", businessType = BusinessType.OTHER, isSaveResponseData = false)
    @GetMapping("/system/google/key/{userId}")
    public AjaxResult getGoogleAuthKey(@PathVariable Long userId)
    {
        // 查看密钥同样受用户数据权限限制，避免越权查看其它部门用户密钥。
        userService.checkUserDataScope(userId);
        AjaxResult ajax = AjaxResult.success();
        ajax.put(AjaxResult.DATA_TAG, googleService.selectGoogleAuthKey(userId));
        return ajax;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验解绑权限及目标用户的数据权限后，解除其谷歌动态码绑定。
     * @param sysGoogle 包含待解绑的用户 ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 解绑结果或参数错误
     */
    @RequiresPermissions("system:google:unbind")
    @Log(title = "谷歌绑定", businessType = BusinessType.UPDATE)
    @PutMapping("/system/google/unbind")
    public AjaxResult unbind(@RequestBody SysGoogle sysGoogle)
    {
        if (StringUtils.isNull(sysGoogle) || StringUtils.isNull(sysGoogle.getUserId()))
        {
            return error("用户ID不能为空");
        }
        // 解绑属于敏感操作，先校验当前管理员是否有该用户的数据权限。
        userService.checkUserDataScope(sysGoogle.getUserId());
        return toAjax(googleService.unbindGoogle(sysGoogle.getUserId(), getUsername()));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 为登录页生成首次绑定用的 PNG 二维码，绑定资格由服务层校验。
     * @param userName 待绑定的用户账号
     * @param response 用于输出图片的 HTTP 响应
     * @return
     * 返回值: void
     * 返回值说明: 向响应流写入二维码并设置禁止缓存的响应头
     */
    // 登录页绑定不经过后台菜单权限，归入认证模块，避免与后台谷歌绑定管理混在同一统计分组。
    @SystemModule(SystemModuleEnum.AUTH)
    @GetMapping("/google/generateGaImage")
    public void generateGaImage(String userName, HttpServletResponse response) throws Exception
    {
        response.setHeader("Expires", "-1");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "-1");
        response.setContentType("image/png");
        googleService.writeBindQRCode(userName, response.getOutputStream());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 校验用户提交的动态码，验证通过后将临时密钥保存为正式绑定。
     * @param bindBody 登录页提交的账号和六位动态码
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 绑定结果或验证失败原因
     */
    // 登录页提交绑定属于登录链路，与后台解绑分开统计。
    @SystemModule(SystemModuleEnum.AUTH)
    @PostMapping("/google/bind")
    public AjaxResult bind(@RequestBody GoogleBindBody bindBody)
    {
        if (StringUtils.isNull(bindBody))
        {
            return error("绑定参数不能为空");
        }
        googleService.bindGoogle(bindBody.getUserName(), bindBody.getGoogleCode());
        return success("谷歌动态码绑定成功");
    }
}
