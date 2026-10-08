package com.zero.common.core.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zero.common.constant.HttpStatus;
import com.zero.common.core.domain.AjaxResult;
import com.zero.common.core.domain.model.LoginUser;
import com.zero.common.core.page.PageDomain;
import com.zero.common.core.page.TableDataInfo;
import com.zero.common.core.page.TableSupport;
import com.zero.common.utils.DateUtils;
import com.zero.common.utils.PageUtils;
import com.zero.common.utils.SecurityUtils;
import com.zero.common.utils.StringUtils;
import com.zero.common.utils.sql.SqlUtil;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: web层通用数据处理
 */
public class BaseController
{
    /**
     * logger字段
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 将前台传递过来的日期格式的字符串，自动转化为Date类型
     * @param binder binder参数
     * @return
     * 返回值: void
     * 返回值说明：将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport()
        {
            /**
             *
             * 作者: Arthur 亚瑟
             * 时间: 2026/5/14 00:58
             * 说明: 设置AsText
             * @param text text参数
             * @return
             * 返回值: void
             * 返回值说明：设置AsText
             */
            @Override
            public void setAsText(String text)
            {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置请求分页数据
     * @return
     * 返回值: void
     * 返回值说明：设置请求分页数据
     */
    protected void startPage()
    {
        PageUtils.startPage();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 设置请求排序数据
     * @return
     * 返回值: void
     * 返回值说明：设置请求排序数据
     */
    protected void startOrderBy()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy()))
        {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 清理分页的线程变量
     * @return
     * 返回值: void
     * 返回值说明：清理分页的线程变量
     */
    protected void clearPage()
    {
        PageUtils.clearPage();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 响应请求分页数据
     * @param list 列表参数
     * @return
     * 返回值: TableDataInfo
     * 返回值说明：响应请求分页数据
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableDataInfo getDataTable(List<?> list)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回成功
     * @return
     * 返回值: AjaxResult
     * 返回值说明：返回成功
     */
    public AjaxResult success()
    {
        return AjaxResult.success();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回失败消息
     * @return
     * 返回值: AjaxResult
     * 返回值说明：返回失败消息
     */
    public AjaxResult error()
    {
        return AjaxResult.error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回成功消息
     * @param message 消息参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：返回成功消息
     */
    public AjaxResult success(String message)
    {
        return AjaxResult.success(message);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回成功消息
     * @param data 数据参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：返回成功消息
     */
    public AjaxResult success(Object data)
    {
        return AjaxResult.success(data);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回失败消息
     * @param message 消息参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：返回失败消息
     */
    public AjaxResult error(String message)
    {
        return AjaxResult.error(message);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 返回警告消息
     * @param message 消息参数
     * @return
     * 返回值: AjaxResult
     * 返回值说明：返回警告消息
     */
    public AjaxResult warn(String message)
    {
        return AjaxResult.warn(message);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 响应返回结果
     * @param rows 影响行数
     * @return
     * 返回值: 操作结果
     * 返回值说明：响应返回结果
     */
    protected AjaxResult toAjax(int rows)
    {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 响应返回结果
     * @param result 结果
     * @return
     * 返回值: 操作结果
     * 返回值说明：响应返回结果
     */
    protected AjaxResult toAjax(boolean result)
    {
        return result ? success() : error();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 页面跳转
     * @param url 地址参数
     * @return
     * 返回值: String
     * 返回值说明：页面跳转
     */
    public String redirect(String url)
    {
        return StringUtils.format("redirect:{}", url);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取用户缓存信息
     * @return
     * 返回值: LoginUser
     * 返回值说明：获取用户缓存信息
     */
    public LoginUser getLoginUser()
    {
        return SecurityUtils.getLoginUser();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取登录用户id
     * @return
     * 返回值: Long
     * 返回值说明：获取登录用户id
     */
    public Long getUserId()
    {
        return getLoginUser().getUserId();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取登录部门id
     * @return
     * 返回值: Long
     * 返回值说明：获取登录部门id
     */
    public Long getDeptId()
    {
        return getLoginUser().getDeptId();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取登录用户名
     * @return
     * 返回值: String
     * 返回值说明：获取登录用户名
     */
    public String getUsername()
    {
        return getLoginUser().getUsername();
    }
}
