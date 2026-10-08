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
 * 分页查询参数基类。
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

    public long safePageNum()
    {
        return pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
    }

    public long safePageSize(long maxPageSize)
    {
        long safeMaxPageSize = maxPageSize < 1 ? defaultPageSize() : maxPageSize;
        long safePageSize = pageSize == null || pageSize < 1 ? defaultPageSize() : pageSize;
        return Math.min(safePageSize, safeMaxPageSize);
    }

    public <T> Page<T> toPage(long maxPageSize)
    {
        return new Page<>(safePageNum(), safePageSize(maxPageSize));
    }

    public <T> Page<T> toPage()
    {
        return toPage(maxPageSize());
    }

    public boolean hasBeginTime()
    {
        return StringUtils.hasText(beginTime);
    }

    public Date getBeginDateTime()
    {
        LocalDate localDate = parseDate(beginTime, "开始日期");
        return localDate == null ? null : toDate(localDate);
    }

    public boolean hasEndTime()
    {
        return StringUtils.hasText(endTime);
    }

    public Date getEndExclusiveDateTime()
    {
        LocalDate localDate = parseDate(endTime, "结束日期");
        return localDate == null ? null : toDate(localDate.plusDays(1));
    }

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
