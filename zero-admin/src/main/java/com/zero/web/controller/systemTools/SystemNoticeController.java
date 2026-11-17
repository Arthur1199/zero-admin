package com.zero.web.controller.systemTools;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import com.zero.common.annotation.*;
import com.zero.common.enums.BusinessType;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.system.domain.SystemNoticeRequest;
import com.zero.system.service.SystemNoticeService;
/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17
 * 说明: 系统通知管理，候选查询要求菜单权限，预检查和发送要求独立发送权限
 */

@RestController @RequestMapping("/systemTools/systemNotice") @RequiredArgsConstructor
public class SystemNoticeController extends BaseController {
    /** 在线通知业务，不提供匿名推送。 */ private final SystemNoticeService service;
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 分页搜索在线账号，响应禁止缓存，不包含会话凭证
     * @param keyword 账号昵称搜索词
     * @param pageNum 页码默认1
     * @param pageSize 条数默认20最大100
     * @param response 禁止浏览器缓存在线账号
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 最小账号字段分页列表
     */

    @GetMapping("/onlineUsers") @RequiresPermissions("systemTools:systemNotice:list")
    public TableDataInfo onlineUsers(@RequestParam(required=false) String keyword,
        @RequestParam(defaultValue="1") int pageNum,@RequestParam(defaultValue="20") int pageSize,
        jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("Cache-Control","no-store");
        if (pageNum<1 || pageSize<1 || pageSize>100) throw new IllegalArgumentException("分页参数无效");
        var users=service.onlineUsers(null,keyword);
        int from=(int)Math.min(users.size(),(long)(pageNum-1)*pageSize);
        TableDataInfo result=new TableDataInfo(users.subList(from,Math.min(from+pageSize,users.size())),users.size());
        result.setCode(200); result.setMsg("操作成功"); return result;
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 读取通知筛选与展示选项
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data内为选项，不受操作人部门数据范围限制
     */

    @GetMapping("/options") @RequiresPermissions("systemTools:systemNotice:list")
    public AjaxResult options() { return success(service.options()); }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 发送前预检查，禁止产生消息
     * @param request 通知及目标参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data.matchedCount为当前匹配数
     */

    @PostMapping("/preview") @RequiresPermissions("systemTools:systemNotice:send")
    public AjaxResult preview(@Validated @RequestBody SystemNoticeRequest request) {
        return success(java.util.Map.of("matchedCount",service.preview(request)));
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 发布在线通知，来源身份取当前登录，日志不记录正文或请求参数
     * @param request 通知内容和范围
     * @return
     * 返回值: AjaxResult
     * 返回值说明: msg为服务端结果提示，data保留发布状态和数量，不表示浏览器确认送达
     */

    @PostMapping("/send") @RequiresPermissions("systemTools:systemNotice:send")
    @Log(title="系统通知",businessType=BusinessType.OTHER,isSaveRequestData=false)
    public AjaxResult send(@Validated @RequestBody SystemNoticeRequest request) {
        var result = service.send(request,getUserId());
        String message = switch (String.valueOf(result.get("status"))) {
            case "SUBMITTED" -> "系统通知发送成功";
            case "PARTIAL" -> "系统通知部分发送成功，已提交发送 " + result.get("submittedCount") + " 个账号，请勿直接重复发送。";
            case "FAILED" -> "系统通知发送失败，请稍后重试。";
            default -> "系统通知发送结果异常，请核实发送情况。";
        };
        return AjaxResult.success(message, result);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 通知校验失败使用固定提示，避免全局校验异常日志输出通知正文或拒绝值
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 参数错误，不发送、不记录正文
     */
    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class,
        org.springframework.http.converter.HttpMessageNotReadableException.class})
    public AjaxResult invalidRequest() { return error("通知参数无效，请检查目标、标题、内容和展示配置"); }
}
