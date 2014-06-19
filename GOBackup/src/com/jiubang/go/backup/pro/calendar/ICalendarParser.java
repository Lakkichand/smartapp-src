package com.jiubang.go.backup.pro.calendar;

import java.io.File;
import java.util.List;

import com.jiubang.go.backup.pro.calendar.CalendarOperator.CalendarStruct;

/**
 * 日历持久化接口
 * @author wencan
 *
 */

public interface ICalendarParser {
	/**
	 * 日历解析接口
	 * @return 返回解析的日历对象
	 */
	public List<CalendarStruct> parser();
	
	/**
	 * 日历持久接口
	 * @param destFile
	 * @return 成功返回ture，失败返回false
	 */
	public boolean persist(File destFile);
}
