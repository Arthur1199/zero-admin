package com.zero.common.utils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/14 00:58
 * 说明: 日期工具类日期工具
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils
{
    /**
     * YYYY字段
     */
    public static String YYYY = "yyyy";
    /**
     * YYYYMM字段
     */

    public static String YYYY_MM = "yyyy-MM";
    /**
     * YYYYMMDD字段
     */

    public static String YYYY_MM_DD = "yyyy-MM-dd";
    /**
     * YYYYMMDDHHMMSS字段
     */

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    /**
     * YYYYMMDDHHMMSS字段
     */

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取当前Date型日期
     * @return
     * 返回值: Date() 当前日期
     * 返回值说明：获取当前Date型日期
     */
    public static Date getNowDate()
    {
        return new Date();
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取当前日期, 默认格式为yyyy-MM-dd
     * @return
     * 返回值: String
     * 返回值说明：获取当前日期, 默认格式为yyyy-MM-dd
     */
    public static String getDate()
    {
        return dateTimeNow(YYYY_MM_DD);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取时间
     * @return
     * 返回值: String
     * 返回值说明：获取时间
     */

    public static final String getTime()
    {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 日期时间Now
     * @return
     * 返回值: String
     * 返回值说明：日期时间Now
     */

    public static final String dateTimeNow()
    {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 日期时间Now
     * @param format format参数
     * @return
     * 返回值: String
     * 返回值说明：日期时间Now
     */

    public static final String dateTimeNow(final String format)
    {
        return parseDateToStr(format, new Date());
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 日期时间
     * @param date 日期参数
     * @return
     * 返回值: String
     * 返回值说明：日期时间
     */

    public static final String dateTime(final Date date)
    {
        return parseDateToStr(YYYY_MM_DD, date);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 解析日期ToStr
     * @param format format参数
     * @param date 日期参数
     * @return
     * 返回值: String
     * 返回值说明：解析日期ToStr
     */

    public static final String parseDateToStr(final String format, final Date date)
    {
        return new SimpleDateFormat(format).format(date);
    }
    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 日期时间
     * @param format format参数
     * @param ts ts参数
     * @return
     * 返回值: Date
     * 返回值说明：日期时间
     */

    public static final Date dateTime(final String format, final String ts)
    {
        try
        {
            return new SimpleDateFormat(format).parse(ts);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 日期路径 即年/月/日 如2018/08/08
     * @return
     * 返回值: String
     * 返回值说明：日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 日期路径 即年/月/日 如20180808
     * @return
     * 返回值: String
     * 返回值说明：日期路径 即年/月/日 如20180808
     */
    public static final String dateTime()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 日期型字符串转化为日期 格式
     * @param str str参数
     * @return
     * 返回值: Date
     * 返回值说明：日期型字符串转化为日期 格式
     */
    public static Date parseDate(Object str)
    {
        if (str == null)
        {
            return null;
        }
        try
        {
            return parseDate(str.toString(), parsePatterns);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 获取服务器启动时间
     * @return
     * 返回值: Date
     * 返回值说明：获取服务器启动时间
     */
    public static Date getServerStartDate()
    {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 计算相差天数
     * @param date1 date1参数
     * @param date2 date2参数
     * @return
     * 返回值: int
     * 返回值说明：计算相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2)
    {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 计算时间差
     * @param endDate 最后时间
     * @param startTime 开始时间
     * @return
     * 返回值: 时间差（天/小时/分钟）
     * 返回值说明：计算时间差
     */
    public static String timeDistance(Date endDate, Date startTime)
    {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - startTime.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 增加 LocalDateTime ==> Date
     * @param temporalAccessor temporalAccessor参数
     * @return
     * 返回值: Date
     * 返回值说明：增加 LocalDateTime ==> Date
     */
    public static Date toDate(LocalDateTime temporalAccessor)
    {
        ZonedDateTime zdt = temporalAccessor.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }

    /**
     *
     * 作者: Arthur 亚瑟
     * 时间: 2026/5/14 00:58
     * 说明: 增加 LocalDate ==> Date
     * @param temporalAccessor temporalAccessor参数
     * @return
     * 返回值: Date
     * 返回值说明：增加 LocalDate ==> Date
     */
    public static Date toDate(LocalDate temporalAccessor)
    {
        LocalDateTime localDateTime = LocalDateTime.of(temporalAccessor, LocalTime.of(0, 0, 0));
        ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zdt.toInstant());
    }
}
