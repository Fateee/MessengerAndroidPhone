package com.yineng.ynmessager.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;

/**
 * @author YUTANG
 * 
 */
public class TimeUtil
{
	/**
	 * "yyyy-MM-dd hh:mm:ss" 12小时制
	 */
	public static final String FORMAT_DATETIME_12 = "yyyy-MM-dd hh:mm:ss";

	/**
	 * "yyyy-MM-dd HH:mm:ss" 24小时制
	 */
	public static final String FORMAT_DATETIME_24 = "yyyy-MM-dd HH:mm:ss";

	/**
	 * "yyyy-MM-dd HH:mm:ss.SSS" 24小时制,含有毫秒
	 */
	public static final String FORMAT_DATETIME_24_mic = "yyyy-MM-dd HH:mm:ss.SSS";

	/**
	 * "yyyy-MM-dd"
	 */

	public static final String FORMAT_DATE1 = "yyyy-MM-dd";

	/**
	 * "yyyy-MM"
	 */
	public static final String FORMAT_DATE2 = "yyyy-MM";
	private final static String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

	/**
	 * @param datetimeformate
	 *            "yyyy-MM-dd" "yyyy-MM-dd hh:mm:ss"
	 * @return 根据传入的时间格式，返回时间字符串
	 */
	@SuppressLint("SimpleDateFormat")
	public static String getCurrenDateTime(String datetimeformate)
	{
		String currentTimestr = null;
		SimpleDateFormat sDateFormat = new SimpleDateFormat(datetimeformate);
		Date curDateTime = new Date(System.currentTimeMillis());
		currentTimestr = sDateFormat.format(curDateTime);
		return currentTimestr;
	}

	/**
	 * 返回系统毫秒值
	 * 
	 * @return
	 */
	public static String getCurrentMillisecond()
	{
		return String.valueOf(System.currentTimeMillis());
	}

	/**
	 * 根据传入的时间，返回是星期几
	 * 
	 * @Title: getWeekIndexByDate
	 * @param str
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static int getWeekIndexByDate(String str)
	{
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		int weeks = -1;
		try
		{
			date = dateFormatter.parse(str);
			dateFormatter.applyPattern("w");
			weeks = Integer.valueOf(dateFormatter.format(date));
		}catch(ParseException e)
		{
			e.printStackTrace();
		}
		return weeks;
	}

	/**
	 * 根据传入的时间字符串，返回毫秒值
	 * 
	 * 
	 * @Title: getMillisecondByDate
	 * @Description:
	 * @param date
	 * @param dateFormat
	 * @return
	 */
	@SuppressLint("SimpleDateFormat")
	public static long getMillisecondByDate(String date, String dateFormat)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		try
		{
			long millionSeconds = sdf.parse(date).getTime();
			return millionSeconds;
		}catch(ParseException e)
		{
			e.printStackTrace();
		}
		return -1L;
	}

	/**
	 * 
	 * 根据毫秒值和时间格式，返回时间字符串"yyyy-MM-dd""yyyy-MM-dd hh:mm:ss""
	 * 
	 * @param date
	 * @param dateFormat
	 * @return
	 */
	public static String getDateByMillisecond(String millionSeconds, String dateFormat)
	{
		String currentTimestr = null;
		SimpleDateFormat sDateFormat = new SimpleDateFormat(dateFormat);
		Date curDateTime = new Date(Long.valueOf(millionSeconds));
		currentTimestr = sDateFormat.format(curDateTime);
		return currentTimestr;
	}

	/**
	 * 
	 * 根据日期，返回星期几
	 * 
	 * @Title: getwhatDayByDate
	 * @param str
	 * @return
	 */
	public static String getWeekdayByDate(String date)
	{
		long milliseconds = getMillisecondByDate(date,"yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(milliseconds));
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if(w < 0)
		{
			w = 0;
		}
		return weekDays[w];
	}

	/**
	 * 
	 * 根据日期，返回是第几月份
	 * 
	 * @param date
	 */
	public static int getMonthByDate(String date)
	{
		long milliseconds = getMillisecondByDate(date,"yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(milliseconds));
		return (cal.get(Calendar.MONTH) + 1);
	}

	/**
	 * 
	 * 当前系统时间是星期几
	 * 
	 * @Title: getCurrentWeek
	 * @return
	 */
	public static String getCurrentWeekday()
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
		if(w < 0)
		{
			w = 0;
		}
		return weekDays[w];
	}

	/**
	 * 将 7:5这样的时间格式转换成 07:05 的格式
	 * 
	 * @param hour
	 *            小时，比如 7
	 * @param minute
	 *            分钟，比如 5
	 * @return 格式化后的时间格式字符串，比如07:05<br>
	 *         数组第0个是hour，第1个是minute
	 */
	public static String[] betterTimeDisplay(int hour, int minute)
	{
		String[] betterTime = new String[2];
		betterTime[0] = hour >= 10 ? String.valueOf(hour) : "0" + hour;
		betterTime[1] = minute >= 10 ? String.valueOf(minute) : "0" + minute;
		return betterTime;
	}

	/**
	 * 判断当前系统时间是否在指定时间的范围内
	 * 
	 * @param beginHour
	 *            开始小时
	 * @param beginMin
	 *            开始小时的分钟数
	 * @param endHour
	 *            结束小时
	 * @param endMin
	 *            结束小时的分钟数
	 * @return true表示在范围内，否则false
	 */
	public static boolean isCurrentInTimeScope(int beginHour, int beginMin, int endHour, int endMin)
	{
		Calendar cal = Calendar.getInstance();// 当前日期
		int hour = cal.get(Calendar.HOUR_OF_DAY);// 获取小时
		int minute = cal.get(Calendar.MINUTE);// 获取分钟
		int minuteOfDayNow = hour * 60 + minute;// 从0:00分开是到目前为止的分钟数
		final int begin = beginHour * 60 + beginMin;// 起始时间 17:20的分钟数
		final int end = endHour * 60 + endMin;// 结束时间 19:00的分钟数
		if(minuteOfDayNow >= begin && minuteOfDayNow <= end)
		{
			return true;
		}else
		{
			return false;
		}
	}

	/**
	 * 判断Date的时间关系字符串（传入的Date对象与当前时间的关系）
	 * 
	 * @param date
	 *            想要判断的Date对象
	 * @return 返回时间关系字符串，比如：
	 *         <ul>
	 *         <li>当天，会直接返回具体时间，比如“19:48”</li>
	 *         <li>昨天，会返回“昨天”</li>
	 *         <li>前天到前一周内，会返回其所在的星期，比如“星期三”</li>
	 *         <li>超出前一周范围，返回其日期，比如“2015-5-8”</li>
	 *         </ul>
	 */
	public static String getTimeRelativeFromNow(Date date)
	{
		Context context = AppController.getInstance().getApplicationContext();
		String relative = "";
		long today = date.getTime();
		long weekFromToday[] = {today, today + DateUtils.DAY_IN_MILLIS, today + DateUtils.DAY_IN_MILLIS * 2,
				today + DateUtils.DAY_IN_MILLIS * 3, today + DateUtils.DAY_IN_MILLIS * 4,
				today + DateUtils.DAY_IN_MILLIS * 5, today + DateUtils.DAY_IN_MILLIS * 6,
				today + DateUtils.DAY_IN_MILLIS * 7};

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		if(DateUtils.isToday(weekFromToday[0])) // 判断是否为今天
		{
			String timeDisplay[] = betterTimeDisplay(calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE));
			relative = timeDisplay[0] + ":" + timeDisplay[1];
		}else if(DateUtils.isToday(weekFromToday[1])) // 是否为昨天
		{
			relative = context.getString(R.string.session_yesterday);
		}else if(DateUtils.isToday(weekFromToday[2]) || DateUtils.isToday(weekFromToday[3])
				|| DateUtils.isToday(weekFromToday[4]) || DateUtils.isToday(weekFromToday[5])
				|| DateUtils.isToday(weekFromToday[6]) || DateUtils.isToday(weekFromToday[7])) // 是否为前天到上一周内的时间范围
		{
			// 这里判断周是上周的还是这周的
			// if(calendar.get(Calendar.WEEK_OF_YEAR) !=
			// Calendar.getInstance().get(Calendar.WEEK_OF_YEAR))
			// {
			// relative += "上周";
			// }
			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			switch(dayOfWeek)
			{
				case 1:
					relative += context.getString(R.string.common_sunday);
					break;
				case 2:
					relative += context.getString(R.string.common_monday);
					break;
				case 3:
					relative += context.getString(R.string.common_tuesday);
					break;
				case 4:
					relative += context.getString(R.string.common_wednesday);
					break;
				case 5:
					relative += context.getString(R.string.common_tuesday);
					break;
				case 6:
					relative += context.getString(R.string.common_friday);
					break;
				case 7:
					relative += context.getString(R.string.common_saturday);
					break;
				default:
					relative = "";
					break;
			}
		}else  // 超出一周以外的
		{
			relative = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
					+ calendar.get(Calendar.DAY_OF_MONTH);
		}
		return relative;
	}

}
