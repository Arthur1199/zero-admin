package com.zero.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/6/28 05:26
 * 说明: 报表服务分页查询参数父类，统一承载 pageNum、pageSize 和分页安全兜底逻辑
 */
@Getter
@Setter
public class BaseQuery
{
    /**
     * 默认页码，空值或小于1时使用第1页
     */
    private static final long DEFAULT_PAGE_NUM = 1L;

    /**
     * 默认每页条数，普通列表查询默认10条
     */
    private static final long DEFAULT_PAGE_SIZE = 10L;

    /**
     * 默认最大每页条数，避免后台误传超大 pageSize 压垮业务库
     */
    private static final long DEFAULT_MAX_PAGE_SIZE = 500L;

    /**
     * 日期查询参数格式，前端日期范围选择器按 yyyy-MM-dd 传入
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 日期转换时使用服务所在时区，保证 java.util.Date 可直接用于 MyBatis-Plus 条件参数
     */
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    /**
     * 当前页码，默认第1页
     */
    private Long pageNum = DEFAULT_PAGE_NUM;

    /**
     * 每页条数，默认10条；特殊查询可通过构造方法或重写 defaultPageSize 调整默认值
     */
    private Long pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 查询开始日期，格式 yyyy-MM-dd；用于按创建时间等日期字段筛选
     */
    private String beginTime;

    /**
     * 查询结束日期，格式 yyyy-MM-dd；用于按创建时间等日期字段筛选
     */
    private String endTime;

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:26
     * 说明: 获取当前查询对象的默认每页条数，子类可按业务场景覆盖
     * @return
     * 返回值: long
     * 返回值说明: 每页条数默认值
     */
    protected long defaultPageSize()
    {
        return DEFAULT_PAGE_SIZE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:43
     * 说明: 获取当前查询对象允许的最大每页条数，子类可按业务风险覆盖
     * @return
     * 返回值: long
     * 返回值说明: 每页条数最大值
     */
    protected long maxPageSize()
    {
        return DEFAULT_MAX_PAGE_SIZE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:26
     * 说明: 规范化页码，避免空值或非法值进入分页插件
     * @return
     * 返回值: long
     * 返回值说明: 大于0的页码
     */
    public long safePageNum()
    {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:26
     * 说明: 规范化每页条数，并按业务允许的最大值做保护
     * @param maxPageSize 当前业务允许的最大每页条数
     * @return
     * 返回值: long
     * 返回值说明: 1到maxPageSize之间的每页条数
     */
    public long safePageSize(long maxPageSize)
    {
        long safeMaxPageSize = maxPageSize < 1 ? defaultPageSize() : maxPageSize;
        long safePageSize = pageSize == null || pageSize < 1 ? defaultPageSize() : pageSize;
        return Math.min(safePageSize, safeMaxPageSize);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:39
     * 说明: 创建 MyBatis-Plus 分页对象，统一完成页码、每页条数和业务最大条数兜底
     * @param maxPageSize 当前业务允许的最大每页条数
     * @return
     * 返回值: Page<T>
     * 返回值说明: 可直接传入 MyBatis-Plus page 方法的分页对象
     */
    public <T> Page<T> toPage(long maxPageSize)
    {
        return new Page<>(safePageNum(), safePageSize(maxPageSize));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:43
     * 说明: 创建 MyBatis-Plus 分页对象，使用当前查询对象自身的最大每页条数
     * @return
     * 返回值: Page<T>
     * 返回值说明: 可直接传入 MyBatis-Plus page 方法的分页对象
     */
    public <T> Page<T> toPage()
    {
        return toPage(maxPageSize());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:56
     * 说明: 获取开始日期的完整起始时间，供 MyBatis-Plus ge 条件使用
     * @return
     * 返回值: Date
     * 返回值说明: 开始日期当天 00:00:00；未传入开始日期时返回 null
     */
    public Date getBeginDateTime()
    {
        LocalDate localDate = parseDate(beginTime, "开始日期");
        return localDate == null ? null : toDate(localDate);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:56
     * 说明: 判断是否传入开始日期，避免 Service 层重复书写 StringUtils.hasText
     * @return
     * 返回值: boolean
     * 返回值说明: true-已传入开始日期，false-未传入
     */
    public boolean hasBeginTime()
    {
        return StringUtils.hasText(beginTime);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:56
     * 说明: 获取结束日期下一天的起始时间，供 MyBatis-Plus lt 条件组成半开区间，避免遗漏毫秒数据
     * @return
     * 返回值: Date
     * 返回值说明: 结束日期下一天 00:00:00；未传入结束日期时返回 null
     */
    public Date getEndExclusiveDateTime()
    {
        LocalDate localDate = parseDate(endTime, "结束日期");
        return localDate == null ? null : toDate(localDate.plusDays(1));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 05:56
     * 说明: 判断是否传入结束日期，避免 Service 层重复书写 StringUtils.hasText
     * @return
     * 返回值: boolean
     * 返回值说明: true-已传入结束日期，false-未传入
     */
    public boolean hasEndTime()
    {
        return StringUtils.hasText(endTime);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 06:06
     * 说明: 解析日期查询参数，非法日期直接抛出业务提示，避免错误格式进入 SQL 条件
     * @param value 日期字符串，格式 yyyy-MM-dd
     * @param fieldName 字段名称，用于错误提示
     * @return
     * 返回值: LocalDate
     * 返回值说明: 解析后的日期；空值返回 null
     */
    private LocalDate parseDate(String value, String fieldName)
    {
        if (!StringUtils.hasText(value))
        {
            return null;
        }
        try
        {
            return LocalDate.parse(value.trim(), DATE_FORMATTER);
        }
        catch (DateTimeParseException e)
        {
            throw new IllegalArgumentException(fieldName + "格式错误，请使用 yyyy-MM-dd");
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/6/28 06:06
     * 说明: 将 LocalDate 转换为当天零点 Date，匹配当前实体 createTime 的 java.util.Date 类型
     * @param localDate 日期对象
     * @return
     * 返回值: Date
     * 返回值说明: 服务所在时区当天零点时间
     */
    private Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
    }
}
