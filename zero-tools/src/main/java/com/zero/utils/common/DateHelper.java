package com.zero.utils.common;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 作者: Arthur 亚瑟
 * 时间: 2026/5/16 23:12
 * 说明: 日期时间处理工具类，提供日期格式化、解析、比较、偏移、时区转换和常用业务日期计算能力
 */
public class DateHelper {

	private static final Logger logger = LoggerFactory.getLogger(DateHelper.class);

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	
	
	/**
	 * 标准日期格式，格式为yyyy-MM-dd。
	 */
	public static final String pattern_date = "yyyy-MM-dd";

	
	
	/**
	 * 标准日期时间格式，格式为yyyy-MM-dd HH:mm:ss。
	 */
	public static final String pattern_time = "yyyy-MM-dd HH:mm:ss";

	
	
	/**
	 * 紧凑日期时间格式，格式为yyyyMMddHHmmss。
	 */
	public static final String YYYYMMDD_HHMMSS = "yyyyMMddHHmmss";

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDate，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formatDate(Date date) {
		return formatDate(date, pattern_time);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDate，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param pattern 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formatDate(Date date, String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		return dateFormat.format(date);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法parseString，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date parseString(String dateStr) {
		return parseString(dateStr, "yyyy-MM-dd HH:mm:ss");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法parseString，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @param pattern 日期格式表达式
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date parseString(String dateStr, String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		try {
			if (!StringHelper.isEmpty(dateStr)) {
		return dateFormat.parse(dateStr);
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
			System.err.println(dateStr + "转换成日期失败，可能是不符合格式：" + pattern);
		}
		return null;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getWeekStr，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getWeekStr(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int week = calendar.get(7);
		--week;
		String weekStr = "";
		switch (week) {
		case 0:
			weekStr = "星期日";
			break;
		case 1:
			weekStr = "星期一";
			break;
		case 2:
			weekStr = "星期二";
			break;
		case 3:
			weekStr = "星期三";
			break;
		case 4:
			weekStr = "星期四";
			break;
		case 5:
			weekStr = "星期五";
			break;
		case 6:
			weekStr = "星期六";
		}
		return weekStr;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateMiliDispersion，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @param date2 日期字符串
	 * @return
	 * 返回值: long
	 * 返回值说明: 计算后的数值
	 */
	public static long getDateMiliDispersion(Date date1, Date date2) {
		if ((date1 == null) || (date2 == null)) {
		return 0L;
		}

		long time1 = date1.getTime();
		long time2 = date2.getTime();

		return time1 - time2;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateDiff，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @param date2 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int getDateDiff(Date date1, Date date2) {
		if ((date1 == null) || (date2 == null)) {
		return 0;
		}
		long time1 = date1.getTime();
		long time2 = date2.getTime();

		long diff = time1 - time2;

		Long longValue = Long.valueOf(diff / 86400000L);
		return longValue.intValue();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDataDiff，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param day 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getDataDiff(Date date, int day) {
		if (date == null) {
		return null;
		}
		long time = date.getTime();
		time -= 86400000L * day;
		return new Date(time);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentWeek，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int getCurrentWeek() {
		Calendar calendar = Calendar.getInstance();
		int week = calendar.get(7);
		--week;
		if (week == 0) {
			week = 7;
		}
		return week;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentWeekStr，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getCurrentWeekStr() {
		return getWeekStr(new Date());
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentYear，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int getCurrentYear() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(1);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentMonth，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int getCurrentMonth() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(2) + 1;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentDay，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int getCurrentDay() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(5);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getUnixTime，用于日期时间格式化、解析、比较或计算
	 * @param str 方法参数
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int getUnixTime(String str) {
		if ((str == null) || ("".equals(str))) {
		return 0;
		}
		try {
			long utime = Long.parseLong(str) * 1000L;
			Date date1 = new Date(utime);

			Date date = new Date();

			long nowtime = (date.getTime() - date1.getTime()) / 1000L;
		return (int) nowtime;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("获取时差失败");
		}
		return 0;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatString，用于日期时间格式化、解析、比较或计算
	 * @param dateTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formatString(String dateTime) {
		if ((dateTime != null) && (dateTime.length() >= 8)) {
			String formatDateTime = dateTime.replaceAll("-", "");
			formatDateTime = formatDateTime.replaceAll(":", "");
			String date = formatDateTime.substring(0, 8);
		return date;
		}

		return "";
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getTimesper，用于日期时间格式化、解析、比较或计算
	 * @param str 方法参数
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int getTimesper(String str) {
		if ((str == null) || ("".equals(str))) {
		return 0;
		}
		try {
			Date date1 = new Date(Long.parseLong(str));
			Date date = new Date();
			long nowtime = (date.getTime() - date1.getTime()) / 1000L;
		return (int) nowtime;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("日期转换出错");
		}
		return 0;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDateTime，用于日期时间格式化、解析、比较或计算
	 * @param dateTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formatDateTime(String dateTime) {
		if ((dateTime != null) && (dateTime.length() >= 8)) {
			String formatDateTime = dateTime.replaceAll("-", "");
			formatDateTime = formatDateTime.replaceAll(":", "");
			String date = formatDateTime.substring(0, 8);
			String time = formatDateTime.substring(8).trim();
			for (int i = time.length(); i < 6; ++i) {
				time = time + "0";
			}
		return date + time;
		}

		return "";
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDateTime，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formatDateTime(Date date) {
		String dateTime = formatDate(date);
		return formatDateTime(dateTime);
	}
	
	 

	 
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDays，用于日期时间格式化、解析、比较或计算
	 * @param startTime 日期字符串
	 * @param endTime 日期字符串
	 * @return
	 * 返回值: List<String>
	 * 返回值说明: 日期字符串列表
	 */
	public static List<String> getDays(String startTime, String endTime) {

		List<String> days = new ArrayList<String>();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date start = dateFormat.parse(startTime);
			Date end = dateFormat.parse(endTime);

			Calendar tempStart = Calendar.getInstance();
			tempStart.setTime(start);

			Calendar tempEnd = Calendar.getInstance();
			tempEnd.setTime(end);
			tempEnd.add(Calendar.DATE, +1);
			while (tempStart.before(tempEnd)) {
				days.add(dateFormat.format(tempStart.getTime()));
				tempStart.add(Calendar.DAY_OF_YEAR, 1);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return days;
	}

	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法isEffectiveDate，用于日期时间格式化、解析、比较或计算
	 * @param nowTime 日期字符串
	 * @param startTime 日期字符串
	 * @param endTime 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
		if (nowTime.getTime() == startTime.getTime() || nowTime.getTime() == endTime.getTime()) {
		return true;
		}

		Calendar date = Calendar.getInstance();
		date.setTime(nowTime);

		Calendar begin = Calendar.getInstance();
		begin.setTime(startTime);

		Calendar end = Calendar.getInstance();
		end.setTime(endTime);

		if (date.after(begin) && date.before(end)) {
		return true;
		} else {
		return false;
		}
	}
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法checkTimeFormat，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean checkTimeFormat(String dateStr) {
		boolean flag = false;
		if(StringHelper.isNotEmpty(dateStr)){
			String datePattern1 = "\\d{4}-\\d{2}-\\d{2}";  
			String datePattern2 = "^((\\d{2}(([02468][048])|([13579][26]))" 
	              + "[\\-\\/\\s]?((((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|" 
	              + "(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?" 
	              + "((0?[1-9])|([1-2][0-9])))))|(\\d{2}(([02468][1235679])|([13579][01345789]))[\\-\\/\\s]?(" 
	              + "(((0?[13578])|(1[02]))[\\-\\/\\s]?((0?[1-9])|([1-2][0-9])|(3[01])))|(((0?[469])|(11))[\\-\\/\\s]?" 
	              + "((0?[1-9])|([1-2][0-9])|(30)))|(0?2[\\-\\/\\s]?((0?[1-9])|(1[0-9])|(2[0-8]))))))";  
			
			if (dateStr != null) {  
		          Pattern pattern = Pattern.compile(datePattern1);  
		          Matcher match = pattern.matcher(dateStr);  
		          if (match.matches()) {  
		              pattern = Pattern.compile(datePattern2);  
		              match = pattern.matcher(dateStr);  

		return flag = true;
		          }  
		          else {  
		return flag;  
		          }  
		      } 
		}
		return flag;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentTime，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: Timestamp
	 * 返回值说明: 计算后的数值
	 */
	public static Timestamp getCurrentTime(){
		return Timestamp.valueOf(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法timeTransfer，用于日期时间格式化、解析、比较或计算
	 * @param time 日期字符串
	 * @param NowTimeZone 日期字符串
	 * @param TargetTimeZone 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String timeTransfer(String time, String NowTimeZone, String TargetTimeZone) {
		if(StringHelper.isEmpty(time)){
		return "";
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(NowTimeZone));
		Date date;
		try {
			date = simpleDateFormat.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		return "0";
		}
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone(TargetTimeZone));
		return simpleDateFormat.format(date);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法timeTransferCCTtoAEST，用于日期时间格式化、解析、比较或计算
	 * @param time 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String timeTransferCCTtoAEST(String time) {
		return timeTransfer(time, "Asia/Shanghai", "Australia/Sydney");
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法timeTransferAESTtoCCT，用于日期时间格式化、解析、比较或计算
	 * @param time 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String timeTransferAESTtoCCT(String time) {
		return timeTransfer(time, "Australia/Sydney", "Asia/Shanghai");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCreateTime，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getCreateTime() {
		return getCurrentTime("yyyy-MM-dd HH:mm:ss");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentTime，用于日期时间格式化、解析、比较或计算
	 * @param fmt 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getCurrentTime(String fmt) {
		Calendar calendar = Calendar.getInstance();
		return getTime(calendar.getTime(), fmt);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getTime，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getTime(Date date) {
		return getTime(date, "yyyy-MM-dd HH:mm:ss");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getTime，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param fmt 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getTime(Date date, String fmt) {
		if (org.apache.commons.lang3.StringUtils.isBlank(fmt)) {
			fmt = YYYYMMDD_HHMMSS;
		}
		SimpleDateFormat myFormat = new SimpleDateFormat(fmt);
		return myFormat.format(date);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getTimeStr，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param fmt 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getTimeStr(Date date, String fmt) {
		if (org.apache.commons.lang3.StringUtils.isBlank(fmt)) {
			fmt = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat myFormat = new SimpleDateFormat(fmt);
		return myFormat.format(date);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentDate，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getCurrentDate() {
		return getCurrentDate("yyyy-MM-dd HH:mm:ss");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentDateForYMD，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getCurrentDateForYMD() {
		return getCurrentDate("yyyy-MM-dd");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentDate，用于日期时间格式化、解析、比较或计算
	 * @param fmt 日期格式表达式
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getCurrentDate(String fmt) {
		if (org.apache.commons.lang3.StringUtils.isBlank(fmt)) {
			fmt = YYYYMMDD_HHMMSS;
		}
		Calendar calendar = Calendar.getInstance();
		Date currentTime = calendar.getTime();
		SimpleDateFormat formatter = new SimpleDateFormat(fmt);
		String strDate = formatter.format(currentTime);
		return formatter.parse(strDate, new java.text.ParsePosition(0));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDate，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date formatDate(String dateStr) {
		return formatDate(dateStr, "yyyy-MM-dd HH:mm:ss");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDate，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @param fmt 日期格式表达式
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date formatDate(String dateStr, String fmt) {
		if (org.apache.commons.lang3.StringUtils.isBlank(dateStr)) {
		return null;
		}
		if (fmt == null) {
			fmt = "yyyy-MM-dd";
		}
		SimpleDateFormat myFormat = new SimpleDateFormat(fmt);
		try {
		return myFormat.parse(dateStr);
		} catch (ParseException e) {
			logger.error("时间转换异常！", e);
		}
		return null;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDateTimeMillis，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @return
	 * 返回值: Long
	 * 返回值说明: 计算后的数值
	 */
	public static Long formatDateTimeMillis(String dateStr) {
		return formatDateTimeMillis(dateStr, "yyyy-MM-dd HH:mm:ss");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDateTimeMillis，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @param fmt 日期格式表达式
	 * @return
	 * 返回值: Long
	 * 返回值说明: 计算后的数值
	 */
	public static Long formatDateTimeMillis(String dateStr, String fmt) {
		if (org.apache.commons.lang3.StringUtils.isBlank(fmt)) {
			fmt = YYYYMMDD_HHMMSS;
		}
		if (org.apache.commons.lang3.StringUtils.isNotBlank(dateStr)) {
			Date date = formatDate(dateStr, fmt);
			if (date != null) {
		return date.getTime();
			}
		}
		return 0L;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatTime，用于日期时间格式化、解析、比较或计算
	 * @param time 日期字符串
	 * @param fmt 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formatTime(Long time, String fmt) {
		return getTime(new Date(time), fmt);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getTime，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getTime(String dateStr) {
		return getTime(dateStr, "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getTime，用于日期时间格式化、解析、比较或计算
	 * @param dateStr 日期字符串
	 * @param from 方法参数
	 * @param to 方法参数
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getTime(String dateStr, String from, String to) {
		Date date = formatDate(dateStr, from);
		return getTime(date, to);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateBeginStr，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getDateBeginStr() {
		Calendar calendar = Calendar.getInstance();
		return getDateBeginStr(calendar.getTime());
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateBeginStr，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getDateBeginStr(Date date) {
		return getTime(date, "yyyy-MM-dd 00:00:00");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateEndStr，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getDateEndStr(Date date) {
		return getTime(date, "yyyy-MM-dd 23:59:59");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateBegin，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getDateBegin() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurDateEnd，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getCurDateEnd() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateEnd，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getDateEnd() {
		Calendar calendar = Calendar.getInstance();
		return getTime(calendar.getTime(), "yyyy-MM-dd 23:59:59");
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getMonth，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getMonth() {
		Calendar calendar = Calendar.getInstance();
		return getTime(calendar.getTime(), "yyyyMM");
	}

	
	static String simpleRangeTime(Long time) {
		Long cTime = new Date().getTime() - time;
		if (cTime < 60000L)
		return "刚刚";
		if (cTime < 3600000L)
		return (int) (cTime / (1000 * 60)) + "分钟前";
		if (cTime < 86400000L)
		return (int) (cTime / (1000 * 60 * 60)) + "小时前";
		if (cTime < 2592000000L)
		return (int) (cTime / (1000 * 60 * 60 * 24)) + "天前";
		else
		return "很久以前";
	}

	
	static Boolean beforeTime48h(Long time) {
		Long cTime = new Date().getTime() - time;
		return cTime < 172800000L;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getMonthSpace，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @param date2 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static int getMonthSpace(String date1, String date2) throws ParseException {
		int result = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(sdf.parse(date1));
		c2.setTime(sdf.parse(date2));
		result = c2.get(Calendar.MONTH) - c1.get(Calendar.MONTH);
		return result == 0 ? 1 : Math.abs(result);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateSpace，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @param date2 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static int getDateSpace(String date1, String date2) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return daysBetween(sdf.parse(date1), sdf.parse(date2));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDateSpaceForHHMMSS，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @param date2 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static int getDateSpaceForHHMMSS(String date1, String date2) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return daysBetweenForCompare(sdf.parse(date1), sdf.parse(date2));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法daysBetween，用于日期时间格式化、解析、比较或计算
	 * @param early 方法参数
	 * @param late 方法参数
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int daysBetween(Date early, Date late) {
		return Math.abs(daysBetweenForCompare(early, late));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法daysBetweenForCompare，用于日期时间格式化、解析、比较或计算
	 * @param early 方法参数
	 * @param late 方法参数
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int daysBetweenForCompare(Date early, Date late) {
		int days = 0;
		Calendar calst = Calendar.getInstance();
		Calendar caled = Calendar.getInstance();
		calst.setTime(early);
		caled.setTime(late);
		calst.set(Calendar.HOUR_OF_DAY, 0);
		calst.set(Calendar.MINUTE, 0);
		calst.set(Calendar.SECOND, 0);
		caled.set(Calendar.HOUR_OF_DAY, 0);
		caled.set(Calendar.MINUTE, 0);
		caled.set(Calendar.SECOND, 0);
		days = ((int) (caled.getTime().getTime() / 1000) - (int) (calst.getTime().getTime() / 1000)) / 3600 / 24;
		return days == 0 ? 0 : days;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextHours，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getNextHours(Date date, int temp) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(date);
		rightNow.set(Calendar.HOUR_OF_DAY, rightNow.get(Calendar.HOUR_OF_DAY) + temp);
		return rightNow.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextDay，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getNextDay(Date date, int temp) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(date);
		rightNow.set(Calendar.DATE, rightNow.get(Calendar.DATE) + temp);
		return rightNow.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getUpDay，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getUpDay(Date date, int temp) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(date);
		rightNow.set(Calendar.DATE, rightNow.get(Calendar.DATE) - temp);
		return rightNow.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextDay，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @param formatter 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNextDay(String date, int temp, String formatter) {
		return LocalDateTime.parse(date, DATE_TIME_FORMATTER).plusDays(temp).format(DateTimeFormatter.ofPattern(formatter));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextMonth，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getNextMonth(Date date, int temp) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(date);
		rightNow.set(Calendar.MONTH, rightNow.get(Calendar.MONTH) + temp);
		return rightNow.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextMonth，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNextMonth(String date, int temp) {
		return LocalDateTime.parse(date, DATE_TIME_FORMATTER).plusMonths(temp).format(DATE_TIME_FORMATTER);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getUpMonth，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @param formatter 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getUpMonth(String date, int temp, String formatter) {
		return LocalDateTime.parse(date, DATE_TIME_FORMATTER).minusMonths(temp).format(DateTimeFormatter.ofPattern(formatter));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextWeek，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getNextWeek(Date date, int temp) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(date);
		rightNow.set(Calendar.WEEK_OF_YEAR, rightNow.get(Calendar.WEEK_OF_YEAR) + temp);
		return rightNow.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextWeek，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNextWeek(String date, int temp) {
		return LocalDateTime.parse(date, DATE_TIME_FORMATTER).plusWeeks(temp).format(DATE_TIME_FORMATTER);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextYear，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getNextYear(Date date, int temp) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(date);
		rightNow.set(Calendar.YEAR, rightNow.get(Calendar.YEAR) + temp);
		return rightNow.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextYear，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNextYear(String date, int temp) {
		return LocalDateTime.parse(date, DATE_TIME_FORMATTER).plusYears(temp).format(DATE_TIME_FORMATTER);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getProDay，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getProDay(Date date, int temp) {
		return getUpDay(date, temp);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getProDay，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @param formatter 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getProDay(String date, int temp, String formatter) {
		return LocalDateTime.parse(date, DATE_TIME_FORMATTER).minusDays(temp).format(DateTimeFormatter.ofPattern(formatter));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getProDay2，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @param formatter 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getProDay2(String date, int temp, String formatter) {
		return LocalDate.parse(date, DATE_FORMATTER).atStartOfDay().minusDays(temp).format(DateTimeFormatter.ofPattern(formatter));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法isMon，用于日期时间格式化、解析、比较或计算
	 * @param curDate 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean isMon(Date curDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getLastTimeOfMonth，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getLastTimeOfMonth() {
		return YearMonth.now().atEndOfMonth().atTime(23, 59, 59).format(DATE_TIME_FORMATTER);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getRemainSeconds，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int getRemainSeconds() {
		int currentSeconds = LocalTime.now().toSecondOfDay();
		int lastSecondsOfDay = 24 * 60 * 60 - 1;
		return lastSecondsOfDay - currentSeconds;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getUpMonth，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date getUpMonth(Date date, int temp) {
		Calendar rightNow = Calendar.getInstance();
		rightNow.setTime(date);
		rightNow.set(Calendar.MONTH, rightNow.get(Calendar.MONTH) - temp);
		return rightNow.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextDay2，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNextDay2(String date, int temp) {
		return LocalDateTime.parse(date, DATE_TIME_FORMATTER).plusDays(temp).format(DATE_TIME_FORMATTER);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextDay3，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param temp 时间偏移量
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNextDay3(String date, int temp) {
		return LocalDateTime.parse(date, DATE_TIME_FORMATTER).plusDays(temp).toLocalDate().format(DATE_FORMATTER) + " 23:59:59";
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getTimeZone，用于日期时间格式化、解析、比较或计算
	 * @param timeZone 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getTimeZone(String timeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar chinaCalendar = Calendar.getInstance();
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		return sdf.format(chinaCalendar.getTime());
	}
	

	

	
	
	/**
	 * 计费期日期格式，格式为yyyyMM。
	 */
	public static final String DATE_JFP_STR = "yyyyMM";
	
	/**
	 * 完整日期时间格式，格式为yyyy-MM-dd HH:mm:ss。
	 */
	public static final String DATE_FULL_STR = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * 时间格式，格式为HH:mm:ss。
	 */
	public static final String TIME_FULL_STR = "HH:mm:ss";
	
	/**
	 * 标准日期格式，格式为yyyy-MM-dd。
	 */
	public static final String DATE_SMALL_STR = "yyyy-MM-dd";
	
	/**
	 * 月份格式，格式为yyyy-MM。
	 */
	public static final String MONTH_SMALL_STR = "yyyy-MM";
	
	/**
	 * 紧凑日期时间格式，格式为yyyyMMddHHmmss。
	 */
	public static final String DATE_KEY_STR = "yyyyMMddHHmmss";
	
	/**
	 * 短年份日期格式，格式为yy-MM-dd。
	 */
	public static final String Date_MON_DAY = "yy-MM-dd";

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法parse，用于日期时间格式化、解析、比较或计算
	 * @param strDate 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date parse(String strDate) {
		return parse(strDate, DATE_FULL_STR);
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法parseDateToDateFullStr，用于日期时间格式化、解析、比较或计算
	 * @param strDate 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String parseDateToDateFullStr(Date strDate) {
		SimpleDateFormat df = new SimpleDateFormat(DATE_SMALL_STR);
		try {
		return df.format(strDate);
		} catch (Exception e) {
			e.printStackTrace();
		return null;
		}
	}
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法generateDateTime，用于日期时间格式化、解析、比较或计算
	 * @param dayTime 日期字符串
	 * @param nowDate 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws Exception 日期解析或计算失败时抛出异常
	 */
	public static Date generateDateTime(String dayTime,Date nowDate) throws Exception {
		
		SimpleDateFormat sf = new SimpleDateFormat(DateHelper.TIME_FULL_STR);
		String format = sf.format(new Date());
		String newTime = dayTime + " " + format;
		sf = new SimpleDateFormat(DateHelper.DATE_FULL_STR);
		Date parse = sf.parse(newTime);
		return parse;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法generateLastWeekMondayAndThisWeekMonday，用于日期时间格式化、解析、比较或计算
	 * @param dayTime 日期字符串
	 * @return
	 * 返回值: List<String>
	 * 返回值说明: 日期字符串列表
	 * @throws Exception 日期解析或计算失败时抛出异常
	 */
	public static List<String> generateLastWeekMondayAndThisWeekMonday(String dayTime) throws Exception {
        List<String> resultString = new ArrayList<>();
		SimpleDateFormat sf = new SimpleDateFormat(DateHelper.DATE_SMALL_STR);
		Date parse = sf.parse(dayTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(parse);

		cal.add(Calendar.DATE, -1*7);
		cal.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
		String lastWeekMonday = DateHelper.formateDate(cal.getTime());

		cal.add(Calendar.DATE, 1*7);
		cal.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
		String thisWeekMonday = DateHelper.formateDate(cal.getTime());

		resultString.add(lastWeekMonday);
		resultString.add(thisWeekMonday);
		return resultString;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法generateThisWeekMondayAndSunday，用于日期时间格式化、解析、比较或计算
	 * @param dayTime 日期字符串
	 * @return
	 * 返回值: List<String>
	 * 返回值说明: 日期字符串列表
	 * @throws Exception 日期解析或计算失败时抛出异常
	 */
	public static List<String> generateThisWeekMondayAndSunday(String dayTime) throws Exception {
		List<String> resultString = new ArrayList<>();
		SimpleDateFormat sf = new SimpleDateFormat(DateHelper.DATE_SMALL_STR);
		Date parse = sf.parse(dayTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(parse);

		cal.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
		String thisWeekMonday = DateHelper.formateDate(cal.getTime());

		cal.add(Calendar.WEEK_OF_MONTH,1);

		cal.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
		String thisWeekSunday = DateHelper.formateDate(cal.getTime());

		resultString.add(thisWeekMonday);
		resultString.add(thisWeekSunday);
		return resultString;
	}
	

	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法generateSpringActivityDate，用于日期时间格式化、解析、比较或计算
	 * @param startDate 日期字符串
	 * @param endDate 日期字符串
	 * @return
	 * 返回值: List<String>
	 * 返回值说明: 日期字符串列表
	 * @throws Exception 日期解析或计算失败时抛出异常
	 */
	public static List<String> generateSpringActivityDate(String startDate,String endDate) throws Exception {
		List<String> result = new ArrayList<String>();
		if(startDate.equals(endDate)) {
		return result;
		}
		SimpleDateFormat sdf = new SimpleDateFormat(DateHelper.DATE_SMALL_STR);
		Calendar cal = Calendar.getInstance();
		
		while(!endDate.equals(startDate)) {
		
            result.add(startDate);
           
			Date parse = sdf.parse(startDate);
			cal.setTime(parse);
			cal.add(Calendar.DATE, +1);
			
			startDate = DateHelper.formateDate(cal.getTime());
            
		}
		return result;
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextSunday，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws Exception 日期解析或计算失败时抛出异常
	 */
	public static String getNextSunday(Date date) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int week = cal.get(Calendar.DAY_OF_WEEK);
		if (week > 1) {
			cal.add(Calendar.DAY_OF_MONTH, -(week - 1) + 7);
		} else {
			cal.add(Calendar.DAY_OF_MONTH, 1 - week + 7);
		}
		return DateHelper.formateDate(cal.getTime());
	}
 
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法confirmSunday，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	private static boolean confirmSunday(Date date) {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
	}
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法isLastDayOfMonth，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean isLastDayOfMonth(Date date) {
		String currentDate = DateHelper.formateDate(date);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, 1);
	
		cal.set(Calendar.DAY_OF_MONTH, 0);

		System.out.println(DateHelper.formateDate(cal.getTime()));
		return currentDate.equals(DateHelper.formateDate(cal.getTime()));
	}
	
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getLastDayOfLastTwoMonth，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getLastDayOfLastTwoMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 0);
		return DateHelper.formateDate(cal.getTime());
	}
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getLastDayOfBeforeMonth，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getLastDayOfBeforeMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, 0);
		return DateHelper.formateDate(cal.getTime());
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getMonthOfYearByDayDate，用于日期时间格式化、解析、比较或计算
	 * @param dayTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getMonthOfYearByDayDate(String dayTime) {
		Date parse = DateHelper.parse(dayTime,DATE_SMALL_STR);
		SimpleDateFormat sf = new SimpleDateFormat(MONTH_SMALL_STR);
		return sf.format(parse);
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getLastMonthOfYearByDayDate，用于日期时间格式化、解析、比较或计算
	 * @param dayTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getLastMonthOfYearByDayDate(String dayTime) {
		Date parse = DateHelper.parse(dayTime,DATE_SMALL_STR);
		Calendar cal = Calendar.getInstance();
		cal.setTime(parse);
		cal.add(Calendar.MONTH, -1);
		SimpleDateFormat sf = new SimpleDateFormat(MONTH_SMALL_STR);
		return sf.format(cal.getTime());
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getLastSunday，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws Exception 日期解析或计算失败时抛出异常
	 */
	public static String getLastSunday() throws Exception {
		Date date = new Date();
		if(confirmSunday(date)) {
		return DateHelper.formateDate(date);
		}
		return getNextSunday(date);
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法comfirmMonthFirstDay，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean comfirmMonthFirstDay() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.DAY_OF_MONTH) == 1;
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法comfirmMonthSpecificDay，用于日期时间格式化、解析、比较或计算
	 * @param currentDate 日期字符串
	 * @param day 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean comfirmMonthSpecificDay(Date currentDate,Integer day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		return cal.get(Calendar.DAY_OF_MONTH) == day;
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentMonthAndLastMonthFirstDay，用于日期时间格式化、解析、比较或计算
	 * @param currentDate 日期字符串
	 * @return
	 * 返回值: Map<String,String>
	 * 返回值说明: 日期范围映射结果
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static Map<String,String> getCurrentMonthAndLastMonthFirstDay(String currentDate) throws ParseException {
		Date formateDate = DateHelper.parse(currentDate, DateHelper.DATE_SMALL_STR);
		return getCurrentMonthAndLastMonthFirstDay(formateDate);
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentMonthAndLastMonthFirstDay，用于日期时间格式化、解析、比较或计算
	 * @param currentDate 日期字符串
	 * @return
	 * 返回值: Map<String,String>
	 * 返回值说明: 日期范围映射结果
	 */
	public static Map<String,String> getCurrentMonthAndLastMonthFirstDay(Date currentDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		int year = cal.get(Calendar.YEAR);
		int currentMonth = cal.get(Calendar.MONTH )+1;
		

		String currentMonthFirstDay = getSpecificDayDateByYearAndMonthAndDay(year,currentMonth,1);
		
		

		if(currentMonth == 1) {
			year = year - 1;
			currentMonth = 12;
		}else {
			currentMonth = currentMonth -1;
		}

		String lastMonthFirstDay = getSpecificDayDateByYearAndMonthAndDay(year,currentMonth,1);
		
		
		Map<String, String> resultMap = new HashMap<String, String>();
		resultMap.put("currentMonthFirstDay",currentMonthFirstDay);
		resultMap.put("lastMonthFirstDay", lastMonthFirstDay);
		return resultMap;
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getSpecificDayDateByYearAndMonthAndDay，用于日期时间格式化、解析、比较或计算
	 * @param year 日期字符串
	 * @param month 日期字符串
	 * @param day 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getSpecificDayDateByYearAndMonthAndDay(int year,int month,int day) {
		
		StringBuilder specificDayDateBuilder = new StringBuilder(year);
		String specificMonth = "";
		if(month >=10 ) {
			specificMonth = ""+month;
		}else {
			specificMonth = "0"+month;
		}
		String specificDay = "";
		if(day >=10) {
			specificDay = ""+day;
		}else {
			specificDay = "0"+day;
		}
		specificDayDateBuilder = specificDayDateBuilder.append(year).append("-").append(specificMonth).append("-").append(specificDay);
		return specificDayDateBuilder.toString();
		
	}
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextFirstDay，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNextFirstDay() {
		
		Calendar cal = Calendar.getInstance();
		int nextMonth =cal.get(Calendar.MONTH)+1+1;
		int year = cal.get(Calendar.YEAR);
		int targetYear = 0;
		int targetMonth =nextMonth;
		String targetDate = "";
		if(nextMonth >=13) {
			targetYear = year+1;
			targetDate = targetYear +"-01-01";
			
		}else {
			targetYear = year;
			StringBuilder builder = new StringBuilder();
			if(targetMonth>=10) {
				builder =builder.append(year).append("-").append(targetMonth).append("-").append("01");
			}else {
				builder =builder.append(year).append("-").append("0").append(targetMonth).append("-").append("01");
			}
			targetDate = builder.toString();
		}
		return targetDate;
	}
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getLastFirstDay，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getLastFirstDay() {
		if(comfirmMonthFirstDay()) {
		return DateHelper.formateDate(new Date());
		}
		return getNextFirstDay();
	}
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法parse，用于日期时间格式化、解析、比较或计算
	 * @param strDate 日期字符串
	 * @param pattern 日期格式表达式
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date parse(String strDate, String pattern) {
		SimpleDateFormat df = new SimpleDateFormat(pattern);
		try {
		return df.parse(strDate);
		} catch (ParseException e) {
			e.printStackTrace();
		return null;
		}
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法compareDateWithNow，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @param date2 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int compareDateWithNow(Date date1, Date date2) {
		int rnum = date1.compareTo(date2);
		return rnum;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法compareDateByNow，用于日期时间格式化、解析、比较或计算
	 * @param beginTime 日期字符串
	 * @param endTime 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean compareDateByNow(Date beginTime, Date endTime) {
		int rnum = beginTime.compareTo(new Date());
		if (rnum <= 0) {
			rnum = endTime.compareTo(new Date());
		} else {
		return false;
		}

		if (rnum > 0) {
		return true;
		} else {
		return false;
		}
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法compareDateByNow，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @param end 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean compareDateByNow(String begin, String end) {

		Date beginTime = parse(begin, DATE_FULL_STR);
		Date endTime = parse(end, DATE_FULL_STR);

		return compareDateByNow(beginTime, endTime);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法compareDateWithNow，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int compareDateWithNow(long date1) {
		long date2 = dateToUnixTimestamp();
		if (date1 > date2) {
		return 1;
		} else if (date1 < date2) {
		return -1;
		} else {
		return 0;
		}
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNowTime，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNowTime() {
		SimpleDateFormat df = new SimpleDateFormat(DATE_FULL_STR);
		return df.format(new Date());
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNowTime，用于日期时间格式化、解析、比较或计算
	 * @param type 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNowTime(String type) {
		SimpleDateFormat df = new SimpleDateFormat(type);
		return df.format(new Date());
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getJFPTime，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getJFPTime() {
		SimpleDateFormat df = new SimpleDateFormat(DATE_JFP_STR);
		return df.format(new Date());
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法dateToUnixTimestamp，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: long
	 * 返回值说明: 计算后的数值
	 */
	public static long dateToUnixTimestamp(String date) {
		long timestamp = 0;
		try {
			timestamp = new SimpleDateFormat(DATE_FULL_STR).parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timestamp;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法dateToUnixTimestamp，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @param dateFormat 日期字符串
	 * @return
	 * 返回值: long
	 * 返回值说明: 计算后的数值
	 */
	public static long dateToUnixTimestamp(String date, String dateFormat) {
		long timestamp = 0;
		try {
			timestamp = new SimpleDateFormat(dateFormat).parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timestamp;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法dateToUnixTimestamp，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: long
	 * 返回值说明: 计算后的数值
	 */
	public static long dateToUnixTimestamp() {
		long timestamp = new Date().getTime();
		return timestamp;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法unixTimestampToDate，用于日期时间格式化、解析、比较或计算
	 * @param timestamp 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String unixTimestampToDate(long timestamp) {
		SimpleDateFormat sd = new SimpleDateFormat(DATE_FULL_STR);
		sd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return sd.format(new Date(timestamp));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法hoursBetweenForCompare，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @param end 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int hoursBetweenForCompare(Date begin, Date end) {
		int hour = 0;

		Long second = end.getTime() - begin.getTime();

		hour = (int) (second / 1000 / 60 / 60);

		return hour;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法differentDays，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @param date2 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int differentDays(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		int day1 = cal1.get(Calendar.DAY_OF_YEAR);
		int day2 = cal2.get(Calendar.DAY_OF_YEAR);

		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);
		if (year1 != year2)
		{
			int timeDistance = 0;
			for (int i = year1; i < year2; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0)
				{
					timeDistance += 366;
				} else
				{
					timeDistance += 365;
				}
			}

		return timeDistance + (day2 - day1);
		} else
		{
		return day2 - day1;
		}
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法differentDays，用于日期时间格式化、解析、比较或计算
	 * @param date1 日期字符串
	 * @param date2 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int differentDays(String date1, String date2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(parse(date1, DATE_SMALL_STR));

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(parse(date2, DATE_SMALL_STR));
		int day1 = cal1.get(Calendar.DAY_OF_YEAR);
		int day2 = cal2.get(Calendar.DAY_OF_YEAR);

		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);
		if (year1 != year2)
		{
			int timeDistance = 0;
			for (int i = year1; i < year2; i++) {
				if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0)
				{
					timeDistance += 366;
				} else
				{
					timeDistance += 365;
				}
			}

		return timeDistance + (day2 - day1);
		} else
		{
		return day2 - day1;
		}
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法addTime，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @param second 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date addTime(Date begin, int second) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(begin);
		cal.add(Calendar.SECOND, second);
		Date time = cal.getTime();

		return time;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法addTimeByDay，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @param day 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date addTimeByDay(Date begin, int day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(begin);
		cal.add(Calendar.DATE, day);
		Date time = cal.getTime();

		return time;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法addTimeByMonth，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @param month 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date addTimeByMonth(Date begin, int month) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(begin);
		cal.add(Calendar.MONTH, month);
		Date time = cal.getTime();

		return time;
	}

	
	@SuppressWarnings("deprecation")
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateTimeBySecond，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date formateTimeBySecond(Date begin) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, begin.getHours());
		cal.set(Calendar.MINUTE, begin.getMinutes());
		cal.set(Calendar.SECOND, begin.getSeconds());
		Date time = cal.getTime();

		return time;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateTimeBySecond，用于日期时间格式化、解析、比较或计算
	 * @param hour 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date formateTimeBySecond(String hour) {
		String date = formateDate(new Date()) + " " + hour;
		return parse(date);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateTimeBySecond，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 */
	public static Date formateTimeBySecond() {
		Calendar ca = Calendar.getInstance();
		ca.set(ca.get(Calendar.YEAR), (ca.get(Calendar.MONTH)), ca.get(Calendar.DAY_OF_MONTH));
		return ca.getTime();
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法secondBetweenForCompare，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @param end 日期字符串
	 * @return
	 * 返回值: Long
	 * 返回值说明: 计算后的数值
	 */
	public static Long secondBetweenForCompare(Date begin, Date end) {

		Long second = end.getTime() - begin.getTime();

		second = second / 1000;

		return second;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法minuteBetweenForCompare，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @param end 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int minuteBetweenForCompare(Date begin, Date end) {
		int minute = 0;
		Long second = end.getTime() - begin.getTime();

		minute = (int) (second / 1000 / 60);

		return minute;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法hoursBetweenForCompare，用于日期时间格式化、解析、比较或计算
	 * @param begin 日期字符串
	 * @param end 日期字符串
	 * @return
	 * 返回值: int
	 * 返回值说明: 计算后的数值
	 */
	public static int hoursBetweenForCompare(String begin, String end) {
		return hoursBetweenForCompare(parse(begin, DATE_FULL_STR), parse(end, DATE_FULL_STR));
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法startTime，用于日期时间格式化、解析、比较或计算
	 * @param startTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String startTime(Date startTime) {

		SimpleDateFormat df = new SimpleDateFormat(DATE_SMALL_STR);

		return df.format(startTime) + " 00:00:00";

	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法startTimeForString，用于日期时间格式化、解析、比较或计算
	 * @param startTime 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws Exception 日期解析或计算失败时抛出异常
	 */
	public static Date startTimeForString(String startTime) throws Exception {

		SimpleDateFormat df = new SimpleDateFormat(DATE_SMALL_STR);

		return df.parse(startTime);

	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法endTime，用于日期时间格式化、解析、比较或计算
	 * @param endTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String endTime(Date endTime) {

		SimpleDateFormat df = new SimpleDateFormat(DATE_SMALL_STR);

		return df.format(endTime) + " 23:59:59";

	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateTime，用于日期时间格式化、解析、比较或计算
	 * @param formateTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formateTime(Date formateTime) {

		SimpleDateFormat df = new SimpleDateFormat(DATE_FULL_STR);

		return df.format(formateTime);

	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateDate，用于日期时间格式化、解析、比较或计算
	 * @param formateTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formateDate(Date formateTime) {

		SimpleDateFormat df = new SimpleDateFormat(DATE_SMALL_STR);

		return df.format(formateTime);

	}
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateDate，用于日期时间格式化、解析、比较或计算
	 * @param formateTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static String formateDate(String formateTime) throws ParseException {

		
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FULL_STR);
		Date parse = sdf.parse(formateTime);
		SimpleDateFormat df = new SimpleDateFormat(DATE_SMALL_STR);

		return df.format(parse);

	}

	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateMonth，用于日期时间格式化、解析、比较或计算
	 * @param formateTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formateMonth(Date formateTime) {

		SimpleDateFormat df = new SimpleDateFormat(MONTH_SMALL_STR);

		return df.format(formateTime);

	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateFullTime，用于日期时间格式化、解析、比较或计算
	 * @param formateTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formateFullTime(Date formateTime) {

		SimpleDateFormat df = new SimpleDateFormat(TIME_FULL_STR);

		return df.format(formateTime);

	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateFullTimeToDate，用于日期时间格式化、解析、比较或计算
	 * @param formateTime 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static Date formateFullTimeToDate(String formateTime) throws ParseException {

		SimpleDateFormat df = new SimpleDateFormat(TIME_FULL_STR);

		return df.parse(formateTime);

	}
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formateopenIssue，用于日期时间格式化、解析、比较或计算
	 * @param formateTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formateopenIssue(Date formateTime) {

		SimpleDateFormat df = new SimpleDateFormat(DATE_KEY_STR);

		return df.format(formateTime);

	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getYesterdayDay，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getYesterdayDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		return yesterday;
	}
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getConfirmDate，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @param days 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getConfirmDate(Date schedule,Integer days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);
		String yesterday = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		return yesterday;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getNextDay，用于日期时间格式化、解析、比较或计算
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String getNextDay() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, +1);
		String nextDay = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		return nextDay;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getScheduleNextDay，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static Date getScheduleNextDay(Date schedule) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(sf.format(schedule));
		Calendar cal = Calendar.getInstance();
		cal.setTime(dayTime);
		cal.add(Calendar.DATE, +1);
		Date nextDay = cal.getTime();
		return nextDay;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getScheduleNextDay，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static String getScheduleNextDay(String schedule) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(schedule);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dayTime);
		cal.add(Calendar.DATE, +1);
		Date nextDay = cal.getTime();
		return DateHelper.formateDate(nextDay);
	}
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentTime，用于日期时间格式化、解析、比较或计算
	 * @param CurrentTime 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static Date getCurrentTime(Date CurrentTime) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(sf.format(CurrentTime));
		return dayTime;
	}

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentDayString，用于日期时间格式化、解析、比较或计算
	 * @param CurrentTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static String getCurrentDayString(Date CurrentTime)throws ParseException  {
		Calendar cal = Calendar.getInstance();
		cal.setTime(CurrentTime);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		SimpleDateFormat sf = new SimpleDateFormat(DATE_FULL_STR);
		return sf.format(cal.getTime());
	}
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentDate，用于日期时间格式化、解析、比较或计算
	 * @param CurrentTime 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static String getCurrentDate(Date CurrentTime)throws ParseException{
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		return sf.format(CurrentTime);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getSystemCurrentTime，用于日期时间格式化、解析、比较或计算
	 * @param currentTime 日期字符串
	 * @param isTestEnvironment 业务标识
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static Date getSystemCurrentTime(Date currentTime, Boolean isTestEnvironment) throws ParseException {
		Date dayTime = currentTime;
		if (isTestEnvironment) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dayTime);
			cal.add(Calendar.DATE, +1);
			dayTime = cal.getTime();
		}
		return dayTime;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getDayTime，用于日期时间格式化、解析、比较或计算
	 * @param isTestEnvironment 业务标识
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static String getDayTime(Boolean isTestEnvironment) throws ParseException {
		String dayTime = getYesterdayDay();
		if (!isTestEnvironment) {
		return dayTime;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(dayTime));
		cal.add(Calendar.DATE, +1);
		dayTime = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
		return dayTime;
	}

	

    
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getTheDayBeforeYesterDay，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
    public static Date getTheDayBeforeYesterDay(Date schedule) throws ParseException{
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date dayTime = sf.parse(sf.format(schedule));
        Calendar cal = Calendar.getInstance();
        cal.setTime(dayTime);
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getCurrentDayYesterDay，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
    public static String getCurrentDayYesterDay(Date schedule) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(sf.format(schedule));
		Calendar cal = Calendar.getInstance();
		cal.setTime(dayTime);
		cal.add(Calendar.DATE, -1);
		String yesterDay = sf.format(cal.getTime());
		return yesterDay;
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getSpecifiedDateSomeDaysAgo，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @param num 时间偏移量
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static Date getSpecifiedDateSomeDaysAgo(Date schedule, Integer num) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(sf.format(schedule));
		Calendar cal = Calendar.getInstance();
		cal.setTime(dayTime);
		cal.add(Calendar.DATE, -num);
		return cal.getTime();
	}
	
	
	
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getSpecifiedDateWeekAgo，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static String getSpecifiedDateWeekAgo(String schedule) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(schedule);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dayTime);
		cal.add(Calendar.WEEK_OF_MONTH, -1);
		return sf.format(cal.getTime());
	}
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getSpecifiedDateMonthAgo，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static String getSpecifiedDateMonthAgo(String schedule) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(schedule);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dayTime);
		cal.add(Calendar.MONTH ,-1);
		return sf.format(cal.getTime());
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getScheduleYesterDay，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @return
	 * 返回值: Date
	 * 返回值说明: 日期对象，解析失败时按原方法语义返回null或抛出异常
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static Date getScheduleYesterDay(Date schedule) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(sf.format(schedule));
		Calendar cal = Calendar.getInstance();
		cal.setTime(dayTime);
		cal.add(Calendar.DATE, -1);
		Date yesterDay = cal.getTime();
		return yesterDay;
	}
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getScheduleYesterDay，用于日期时间格式化、解析、比较或计算
	 * @param schedule 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 * @throws ParseException 日期字符串不符合指定格式时抛出异常
	 */
	public static String getScheduleYesterDay(String schedule) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		Date dayTime = sf.parse(schedule);
		Calendar cal = Calendar.getInstance();
		cal.setTime(dayTime);
		cal.add(Calendar.DATE, -1);
		return sf.format(cal.getTime());
	}
	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法dateToString，用于日期时间格式化、解析、比较或计算
	 * @param formateTime 日期字符串
	 * @param fomat 日期格式表达式
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String dateToString(Date formateTime, String fomat) {

		SimpleDateFormat df = new SimpleDateFormat(fomat);

		return df.format(formateTime);

	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法compareDate，用于日期时间格式化、解析、比较或计算
	 * @param start 日期字符串
	 * @param end 日期字符串
	 * @return
	 * 返回值: long
	 * 返回值说明: 计算后的数值
	 */
	public static long compareDate(String start, String end) {
		return dateToUnixTimestamp(start, DATE_SMALL_STR) - dateToUnixTimestamp(end, DATE_SMALL_STR);

	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法compareMonth，用于日期时间格式化、解析、比较或计算
	 * @param start 日期字符串
	 * @param end 日期字符串
	 * @return
	 * 返回值: long
	 * 返回值说明: 计算后的数值
	 */
	public static long compareMonth(String start, String end) {

		return dateToUnixTimestamp(start, MONTH_SMALL_STR) - dateToUnixTimestamp(end, MONTH_SMALL_STR);
	}

	
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法belongCalendar，用于日期时间格式化、解析、比较或计算
	 * @param nowTime 日期字符串
	 * @param beginTime 日期字符串
	 * @param endTime 日期字符串
	 * @return
	 * 返回值: boolean
	 * 返回值说明: true-满足条件，false-不满足条件
	 */
	public static boolean belongCalendar(Date nowTime, Date beginTime, Date endTime) {
		Calendar date = Calendar.getInstance();
		date.setTime(nowTime);

		Calendar begin = Calendar.getInstance();
		begin.setTime(beginTime);

		Calendar end = Calendar.getInstance();
		end.setTime(endTime);

		if (date.after(begin) && date.before(end)) {
		return true;
		} else {
		return false;
		}
	}

	
    
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDateByT，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
    public static String formatDateByT(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd'T'HH:mm");
        String dateStr = sdf.format(date);
        return dateStr;
    }

	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法formatDateToYMD，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: String
	 * 返回值说明: 格式化后的日期时间字符串
	 */
	public static String formatDateToYMD(Date date) {

		SimpleDateFormat sdf = new SimpleDateFormat(Date_MON_DAY);
		String dateStr = sdf.format(date);
		return dateStr;

	}

	
    
	
	/**
	 *
	 * 作者: Arthur 亚瑟
	 * 时间: 2026/5/16 23:12
	 * 说明: 日期工具方法getRemainSecondsOneDay，用于日期时间格式化、解析、比较或计算
	 * @param date 日期字符串
	 * @return
	 * 返回值: Integer
	 * 返回值说明: 计算后的数值
	 */
    public static Integer getRemainSecondsOneDay(Date date) {
    	
    	LocalDateTime withNano = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    	
    	LocalDateTime ofInstant = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    	
    	long between = ChronoUnit.SECONDS.between(ofInstant,withNano);
    		return (int) between;
    }
}
