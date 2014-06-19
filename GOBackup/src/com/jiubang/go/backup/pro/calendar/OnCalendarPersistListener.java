package com.jiubang.go.backup.pro.calendar;

/**
 * 日历持久化监听接口
 * @author wencan
 *
 */

public interface OnCalendarPersistListener {
	/**
	 * 日历持久化开始回调
	 * @param totalCalendarCount  日历个数
	 * @param totalEventCount 事件个数
	 */
	public void onPersistStart(int totalCalendarCount, int totalEventCount);

	/**
	 * 日历持久化过程回调
	 * @param curCalendar	当前持久话日历index
	 * @param totalCalendar	日历个数
	 * @param curEvent	当前持久化时间index
	 * @param totalEvent	日历个数
	 */
	public void onPersistProgress(int curCalendar, int totalCalendar, int curEvent, int totalEvent);

	/**
	 * 日历持久化结束回调
	 * @param success	日历持久化结果
	 * @param successCalendarCount	日历持久化成功个数
	 * @param totalCalendar	日历个数
	 * @param successEventCount	事件持久化成功个数
	 * @param totalEvent	事件个数
	 */
	public void onPersistEnd(boolean success, int successCalendarCount, int totalCalendar,
			int successEventCount, int totalEvent);
}
