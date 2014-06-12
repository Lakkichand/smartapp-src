package com.go.util.log;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUnit {
	public final static String LONGEST_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	public final static String LONG_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public final static String SHORT_FORMAT = "yyyy-MM-dd";
	public final static String TIME_FORMAT = "HH:mm:ss";

	private static SimpleDateFormat formatter = new SimpleDateFormat();

	// ///////////////////////////////////////////////////////////////
	/**
	 * 获取现在时间
	 * 
	 * @return 返回时间类型 yyyy-MM-dd HH:mm:ss.SSS
	 */
	public static Date getNowDateLongest() {
		return getNowDate(LONGEST_FORMAT);
	}

	/**
	 * 获取现在时间
	 * 
	 * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
	 */
	public static Date getNowDate() {
		return getNowDate(LONG_FORMAT);
	}

	/**
	 * 获取现在时间
	 * 
	 * @return 返回短时间字符串格式yyyy-MM-dd
	 */
	public static Date getNowDateShort() {
		return getNowDate(SHORT_FORMAT);
	}

	/**
	 * 获取时间 小时:分;秒 HH:mm:ss
	 * 
	 * @return
	 */
	public static Date getNowTimeShort() {
		return getNowDate(TIME_FORMAT);
	}

	/**
	 * 获取现在时间
	 * 
	 * @param timeFormat
	 *            返回时间格式
	 */
	public static Date getNowDate(String timeFormat) {
		Date currentTime = new Date();
		Date currentTime_2 = null;
		synchronized (formatter) {
			formatter.applyPattern(timeFormat);
			String dateString = formatter.format(currentTime);
			ParsePosition pos = new ParsePosition(0);
			currentTime_2 = formatter.parse(dateString, pos);
		}
		return currentTime_2;
	}

	// /////////////////////////////////////////////////////////////////////////////
	/**
	 * 获取现在时间
	 * 
	 * @return 返回字符串格式 yyyy-MM-dd HH:mm:ss.SSS
	 */
	public static String getStringDateLongest() {
		return getStringDate(LONGEST_FORMAT);
	}

	/**
	 * 获取现在时间
	 * 
	 * @return 返回字符串格式 yyyy-MM-dd HH:mm:ss
	 */
	public static String getStringDate() {
		return getStringDate(LONG_FORMAT);
	}

	/**
	 * 获取现在时间
	 * 
	 * @return 返回短时间字符串格式yyyy-MM-dd
	 */
	public static String getStringDateShort() {
		return getStringDate(SHORT_FORMAT);
	}

	/**
	 * 获取时间 小时:分;秒 HH:mm:ss
	 * 
	 * @return
	 */
	public static String getTimeShort() {
		return getStringDate(TIME_FORMAT);
	}

	/**
	 * 获取现在时间
	 * 
	 * @param 返回字符串格式
	 */
	public static String getStringDate(String timeFormat) {
		java.util.Date currentTime = new java.util.Date();
		String dateString = null;
		synchronized (formatter) {
			formatter.applyPattern(timeFormat);
			dateString = formatter.format(currentTime);
		}
		return dateString;
	}

	// //////////////////////////////////////////////////////////////////////////////
	/**
	 * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss.SSS
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToLongDateLongest(String strDate) {
		return strToDate(strDate, LONGEST_FORMAT);
	}

	/**
	 * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToLongDate(String strDate) {
		return strToDate(strDate, LONG_FORMAT);
	}

	/**
	 * 将短时间格式字符串转换为时间 yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToShortDate(String strDate) {
		return strToDate(strDate, SHORT_FORMAT);
	}

	/**
	 * 将时间格式字符串转换为时间 HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToTimeDate(String strDate) {
		return strToDate(strDate, TIME_FORMAT);
	}

	/**
	 * 按指定的时间格式字符串转换为时间
	 * 
	 * @param strDate
	 * @param timeFormat
	 * @return
	 */
	public static Date strToDate(String strDate, String timeFormat) {
		Date strtodate = null;
		synchronized (formatter) {
			formatter.applyPattern(timeFormat);
			ParsePosition pos = new ParsePosition(0);
			strtodate = formatter.parse(strDate, pos);
		}
		return strtodate;
	}

	// ///////////////////////////////////////////////////////////////////////////////
	/**
	 * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss.SSS
	 * 
	 * @param dateDate
	 * @return
	 */
	public static String dateToLongestStr(Date dateDate) {
		return dateToStr(dateDate, LONGEST_FORMAT);
	}

	/**
	 * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dateDate
	 * @return
	 */
	public static String dateToLongStr(Date dateDate) {
		return dateToStr(dateDate, LONG_FORMAT);
	}

	/**
	 * 将短时间格式字符串转换为时间 yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
	public static String dateToShortStr(Date dateDate) {
		return dateToStr(dateDate, SHORT_FORMAT);
	}

	/**
	 * 将时间格式字符串转换为时间 HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
	public static String dateToTimeStr(Date dateDate) {
		return dateToStr(dateDate, TIME_FORMAT);
	}

	/**
	 * 按指定的时间格式时间转换为字符串
	 * 
	 * @param dateDate
	 * @param timeFormat
	 * @return
	 */
	public static String dateToStr(Date dateDate, String timeFormat) {
		String dateString = null;
		synchronized (formatter) {
			formatter.applyPattern(timeFormat);
			dateString = formatter.format(dateDate);
		}
		return dateString;
	}

	public static String LongToStr(long m, String timeFormat) {
		String dateString = null;
		synchronized (formatter) {
			formatter.applyPattern(timeFormat);
			dateString = formatter.format(new Date(m));
		}
		return dateString;
	}

	// ////////////////////////////////////////////////////////////////////////////////
}