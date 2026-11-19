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
 * 时间: 2026/11/20 01:36
 * 说明: 分页查询参数基类。
 */
@Getter
@Setter
public class BaseQuery
{
    private static final long DEFAULT_PAGE_NUM = 1L;

    private static final long DEFAULT_PAGE_SIZE = 10L;

    private static final long DEFAULT_MAX_PAGE_SIZE = 500L;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    private Long pageNum = DEFAULT_PAGE_NUM;

    private Long pageSize = DEFAULT_PAGE_SIZE;

    private String beginTime;

    private String endTime;

    protected long defaultPageSize()
    {
        return DEFAULT_PAGE_SIZE;
    }

    protected long maxPageSize()
    {
        return DEFAULT_MAX_PAGE_SIZE;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将空值或小于 1 的页码归到首页。
     * @return
     * 返回值: long
     * 返回值说明: 用于数据库查询的页码
     */
    public long safePageNum()
    {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 将无效的条数和上限替换为默认值，并限制本页条数。
     * @param maxPageSize 当前业务允许的每页上限
     * @return
     * 返回值: long
     * 返回值说明: 有效的每页查询条数
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
     * 时间: 2026/11/20 01:36
     * 说明: 使用有效页码和指定条数上限创建分页对象。
     * @param <T> 分页记录类型
     * @param maxPageSize 当前业务允许的每页上限
     * @return
     * 返回值: Page<T>
     * 返回值说明: 供 MyBatis-Plus 使用的分页对象
     */
    public <T> Page<T> toPage(long maxPageSize)
    {
        return new Page<>(safePageNum(), safePageSize(maxPageSize));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 按当前查询类型的每页上限生成分页参数。
     * @param <T> 分页记录类型
     * @return
     * 返回值: Page<T>
     * 返回值说明: 供 MyBatis-Plus 使用的分页对象
     */
    public <T> Page<T> toPage()
    {
        return toPage(maxPageSize());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在构造查询条件前检查开始日期是否有文本。
     * @return
     * 返回值: boolean
     * 返回值说明: 开始日期包含非空白字符时返回 true
     */
    public boolean hasBeginTime()
    {
        return StringUtils.hasText(beginTime);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 把查询起始日期转换为该日的起始时刻，用于包含式起点。
     * @return
     * 返回值: Date
     * 返回值说明: 起始时刻，未提供日期时返回 null
     */
    public Date getBeginDateTime()
    {
        LocalDate localDate = parseDate(beginTime, "开始日期");
        return localDate == null ? null : toDate(localDate);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 在构造查询条件前检查结束日期是否有文本。
     * @return
     * 返回值: boolean
     * 返回值说明: 结束日期包含非空白字符时返回 true
     */
    public boolean hasEndTime()
    {
        return StringUtils.hasText(endTime);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 把结束日期转换为次日零点，用于排除式上界，包含结束当天的全部时间。
     * @return
     * 返回值: Date
     * 返回值说明: 结束日期次日零点，未提供日期时返回 null
     */
    public Date getEndExclusiveDateTime()
    {
        LocalDate localDate = parseDate(endTime, "结束日期");
        return localDate == null ? null : toDate(localDate.plusDays(1));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/11/20 01:36
     * 说明: 解析查询日期，格式错误时提示具体出错字段。
     * @param value 原始日期文本
     * @param fieldName 用于提示的字段名称
     * @return
     * 返回值: LocalDate
     * 返回值说明: 解析的日期，无文本时返回 null
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

    private Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
    }
}
