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
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.enums.BusinessType;
import com.zero.common.utils.StringUtils;
import com.zero.system.domain.GoogleBindBody;
import com.zero.system.domain.SysGoogle;
import com.zero.system.service.ISysGoogleService;
import com.zero.system.service.ISysUserService;

/**
 * 用户谷歌动态码绑定
 */
@RestController
public class SysGoogleController extends BaseController
{
    @Autowired
    private ISysGoogleService googleService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询谷歌绑定列表
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
     * 查看谷歌密钥明文
     *
     * 明文只允许具备专门权限的管理员查看，并且不记录响应内容到操作日志。
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
     * 解绑谷歌动态码
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
     * 生成登录绑定二维码
     *
     * 该接口用于未登录用户首次绑定，安全校验放在 Service 中按用户名和绑定状态处理。
     */
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
     * 登录页绑定谷歌动态码
     *
     * 只把通过动态码验证的临时密钥写入绑定表，避免用户只打开二维码就完成绑定。
     */
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
