package com.zero.web.controller.systemTools;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import com.zero.common.annotation.Log;
import com.zero.common.annotation.RequiresPermissions;
import com.zero.common.core.controller.BaseController;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.core.page.PageDomain;
import com.zero.common.core.page.TableSupport;
import com.github.pagehelper.PageHelper;
import com.zero.common.enums.BusinessType;
import com.zero.common.exception.ServiceException;
import com.zero.system.domain.SysMemoQuery;
import com.zero.system.domain.SysMemoRequest;
import com.zero.system.service.ISysMemoService;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/11/17 21:20
 * 说明: 系统工具备忘录接口；普通账号读取公开及本人私有内容并维护本人记录，超级管理员可管理全部
 */
@RestController
@RequestMapping("/systemTools/memo")
public class SysMemoController extends BaseController
{
    /** 备忘录单页最多500条，禁止用零值或超大pageSize绕过分页。 */
    private static final int MAX_PAGE_SIZE = 500;
    /** 备忘录业务服务。 */
    private final ISysMemoService memoService;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 注入备忘录业务服务
     * @param memoService 备忘录服务
     * @return
     * 返回值: 构造方法
     * 返回值说明: 初始化接口依赖
     */
    public SysMemoController(ISysMemoService memoService)
    {
        this.memoService = memoService;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 禁止浏览器及代理缓存备忘录响应
     * @param response 当前HTTP响应
     * @return
     * 返回值: void
     * 返回值说明: 设置no-store，正文仅保留在当前查看界面
     */
    @ModelAttribute
    public void disableCache(HttpServletResponse response)
    {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17
     * 说明: 限制备忘录分页大小，不接受客户端排序覆盖创建时间和ID倒序规则
     * @return
     * 返回值: void
     * 返回值说明: 初始化有界PageHelper上下文，由列表接口finally清理
     */
    @Override
    protected void startPage()
    {
        PageDomain page = TableSupport.buildPageRequest();
        int pageNum = page.getPageNum() == null ? 1 : Math.max(1, page.getPageNum());
        int pageSize = page.getPageSize() == null ? 10 : Math.max(1, Math.min(MAX_PAGE_SIZE, page.getPageSize()));
        PageHelper.startPage(pageNum, pageSize).setReasonable(true);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 分页查询可见备忘录，普通账号公开及本人、超级管理员全部，列表不包含正文
     * @param query 标题、地址及可见范围筛选
     * @return
     * 返回值: TableDataInfo
     * 返回值说明: 当前用户可见元数据及总条数，需要菜单权限
     */
    @RequiresPermissions("systemTools:memo:list")
    @GetMapping("/list")
    public TableDataInfo list(SysMemoQuery query)
    {
        startPage();
        try
        {
            return getDataTable(memoService.selectMemoList(query));
        }
        finally
        {
            clearPage();
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 查看可见正文，超级管理员包含他人私有记录，沿用菜单权限
     * @param memoId 备忘录ID
     * @return
     * 返回值: AjaxResult
     * 返回值说明: data为详情及解密正文，不记录操作响应日志
     */
    @RequiresPermissions("systemTools:memo:list")
    @GetMapping("/{memoId}")
    public AjaxResult getInfo(@PathVariable Long memoId)
    {
        return success(memoService.selectMemoById(memoId));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 新增备忘录，创建人由服务端设置
     * @param request 标题、地址、正文、备注及可见范围
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 新增结果，不返回敏感正文
     */
    @RequiresPermissions({"systemTools:memo:list", "systemTools:memo:add"})
    @Log(title = "备忘录", businessType = BusinessType.INSERT, isSaveRequestData = false, isSaveResponseData = false)
    @PostMapping
    public AjaxResult add(@RequestBody SysMemoRequest request)
    {
        return toAjax(memoService.insertMemo(request));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 创建人或超级管理员修改备忘录，需菜单及修改权限
     * @param request 修改内容、ID及读取时的版本
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 修改结果，非创建人或版本冲突时拒绝
     */
    @RequiresPermissions({"systemTools:memo:list", "systemTools:memo:edit"})
    @Log(title = "备忘录", businessType = BusinessType.UPDATE, isSaveRequestData = false, isSaveResponseData = false)
    @PutMapping
    public AjaxResult edit(@RequestBody SysMemoRequest request)
    {
        return toAjax(memoService.updateMemo(request));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 创建人或超级管理员删除备忘录，需菜单及删除权限，前端确认后调用
     * @param memoId 备忘录ID
     * @param version 确认删除时的记录版本
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 删除结果，不保存正文副本
     */
    @RequiresPermissions({"systemTools:memo:list", "systemTools:memo:remove"})
    @Log(title = "备忘录", businessType = BusinessType.DELETE, isSaveRequestData = false, isSaveResponseData = false)
    @DeleteMapping("/{memoId}")
    public AjaxResult remove(@PathVariable Long memoId, @RequestParam Long version)
    {
        return toAjax(memoService.deleteMemo(memoId, version));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 仅返回业务层固定提示，避免通用异常处理输出正文或SQL参数
     * @param exception 业务校验异常，不包含用户输入值
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 固定业务错误提示，保留401等业务状态码供前端处理登录失效
     */
    @ExceptionHandler(ServiceException.class)
    public AjaxResult handleServiceException(ServiceException exception)
    {
        return exception.getCode() == null ? error(exception.getMessage())
                : AjaxResult.error(exception.getCode(), exception.getMessage());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 菜单或按钮权限不足时返回统一禁止访问状态
     * @param exception 权限异常，不返回内部权限实现
     * @return
     * 返回值: AjaxResult
     * 返回值说明: code为403的固定响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    public AjaxResult handleAccessDenied(AccessDeniedException exception)
    {
        return AjaxResult.error(403, "没有备忘录操作权限");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/17 21:20
     * 说明: 隔离JSON解析及数据库异常，不输出正文、密文、SQL或原始堆栈
     * @param exception 接口处理异常
     * @return
     * 返回值: AjaxResult
     * 返回值说明: 通用错误，日志仅记录异常类型
     */
    @ExceptionHandler(Exception.class)
    public AjaxResult handleException(Exception exception)
    {
        logger.error("备忘录处理失败，异常类型：{}", exception.getClass().getSimpleName());
        return error("备忘录处理失败，请检查请求或联系管理员");
    }
}

