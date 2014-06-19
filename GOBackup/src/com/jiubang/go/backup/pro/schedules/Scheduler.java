package com.jiubang.go.backup.pro.schedules;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.schedules.BackupPlan.RepeatType;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 */
public class Scheduler {
	public static final String ACTION_SCHEDULED_BACKUP = "com.jiubang.go.backup.intent.action.schduled_backup";
	public static final String ACTION_SCHEDULED_PLAN_ADVANCE_NOTICE = "com.jiubang.go.backup.intent.action.advance_notice";
	public static final String EXTRA_PLAN_DATA = "intent.extra.plan_raw_data";
	private static final long ONE_DAY_IN_MILLS = 24 * 60 * 60 * 1000;

	private static Scheduler sInstance;
	private BackupPlanDBHelper mPlanDBHelper;
	private Context mContext;

	private Scheduler(Context context) {
		mPlanDBHelper = new BackupPlanDBHelper(context);
		mContext = context.getApplicationContext();
	}

	public static Scheduler getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Scheduler(context.getApplicationContext());
		}
		return sInstance;
	}

	/**
	 * 系统启动，时区/时间改变，用户计划修改时触发，激活下一次的备份计划
	 *
	 * @param plan
	 */
	/*
	 * public void scheduleNextPlan() { final BackupPlan nextPlan =
	 * getNextBackupPlan(); if (nextPlan == null) { cancelNextBackupPlan(); }
	 * else { readyToExecutePlan(nextPlan); } }
	 */

	/**
	 * 系统启动，时区/时间改变，用户计划修改时触发，激活所有有效的备份计划
	 *
	 * @param plan
	 */
	public void scheduleAllActivePlans() {
		Set<BackupPlan> validePlans = getAllValidePlans();
		if (validePlans != null) {
			for (BackupPlan plan : validePlans) {
				// 禁用掉旧的计时
				cancelScheduledPlan(plan);
				schedulePlan(plan);
			}
		}
	}

	public long schedulePlan(BackupPlan plan) {
		if (plan == null) {
			return -1;
		}
		long now = System.currentTimeMillis();
		if (plan.repeatType == RepeatType.ONE_OFF && plan.startTime <= now) {
			enablePlanInternal(plan, false);
			return -1;
		}
		long startTime = calNextPlanTimeInMillis(plan);
		scheduleAdvanceNotification(plan, startTime);
		readyToExecutePlan(plan, startTime);
		return startTime;
	}

	private void scheduleAdvanceNotification(BackupPlan plan, long atTimeInMillis) {
		if (plan == null || plan.reminder == 0) {
			return;
		}
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		// 提前通知的广播
		Intent intent = new Intent(ACTION_SCHEDULED_PLAN_ADVANCE_NOTICE);
		intent.putExtra(EXTRA_PLAN_DATA, plan);
		PendingIntent reminder = PendingIntent.getBroadcast(mContext, plan.id, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis - plan.reminder, reminder);
	}

	private void readyToExecutePlan(BackupPlan plan, long atTimeInMillis) {
		if (plan == null) {
			return;
		}
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_SCHEDULED_BACKUP);
		intent.putExtra(EXTRA_PLAN_DATA, plan);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, plan.id, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);
	}

	public void cancelScheduledPlan(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		cancelAdvanceNotification(plan);

		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_SCHEDULED_BACKUP);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, plan.id, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
	}

	private void cancelAdvanceNotification(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_SCHEDULED_PLAN_ADVANCE_NOTICE);
		PendingIntent reminder = PendingIntent.getBroadcast(mContext, plan.id, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(reminder);
	}

	/*
	 * private void cancelNextBackupPlan() { AlarmManager am = (AlarmManager)
	 * mContext.getSystemService(Context.ALARM_SERVICE); PendingIntent sender =
	 * PendingIntent.getBroadcast( mContext, 0, new
	 * Intent(ACTION_SCHEDULED_BACKUP), PendingIntent.FLAG_CANCEL_CURRENT);
	 * am.cancel(sender); }
	 */
	
	public List<BackupPlan> getAllPlans() {
		final Cursor cursor = getAllScheduledPlansCursor();
		if (cursor == null) {
			return null;
		}
		List<BackupPlan> result = new ArrayList<BackupPlan>();
		try {
			if (!cursor.moveToFirst()) {
				return null;
			}
			do {
				BackupPlan plan = new BackupPlan(cursor);
				result.add(plan);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		return Util.isCollectionEmpty(result) ? null : result;
	}

	public Set<BackupPlan> getAllValidePlans() {
		final Cursor cursor = getEnabledPlansCursor();
		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		long now = System.currentTimeMillis();
		Set<BackupPlan> plans = new HashSet<BackupPlan>();
		try {
			do {
				final BackupPlan plan = new BackupPlan(cursor);
				long startTime = calNextPlanTimeInMillis(plan);
				// 已过期, 对于重复性计划，不可能出现startTime < now的情况
				if (startTime < now) {
					enablePlanInternal(plan, false);
					continue;
				}
				plans.add(plan);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		return plans;
	}

	/**
	 * 获取距离现在时间最近的一次计划
	 *
	 * @return
	 */
	public BackupPlan getNextBackupPlan() {
		final Cursor cursor = getEnabledPlansCursor();
		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		Set<BackupPlan> plans = new HashSet<BackupPlan>();
		try {
			do {
				final BackupPlan plan = new BackupPlan(cursor);
				plans.add(plan);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		long now = System.currentTimeMillis();
		long minTime = Long.MAX_VALUE;
		BackupPlan dest = null;
		for (BackupPlan p : plans) {
			long startTime = calNextPlanTimeInMillis(p);
			// 已过期, 对于重复性计划，不可能出现startTime < now的情况
			if (startTime < now) {
				enablePlanInternal(p, false);
				continue;
			}
			if (startTime < minTime) {
				minTime = startTime;
				dest = p;
			}
		}
		return dest;
	}

	// 禁用已过时的备份计划
	public void disableExpiredPlan() {
		Cursor cursor = getEnabledPlansCursor();
		if (cursor == null) {
			return;
		}
		if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
			cursor.close();
			return;
		}

		try {
			do {
				BackupPlan plan = new BackupPlan(cursor);
				if (isPlanExpired(plan)) {
					enablePlan(plan, false);
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
	}

	public void enablePlan(BackupPlan plan, boolean enabled) {
		enablePlanInternal(plan, enabled);
		// scheduleNextPlan();
		if (enabled) {
			schedulePlan(plan);
		} else {
			cancelScheduledPlan(plan);
		}
	}

	private void enablePlanInternal(BackupPlan plan, boolean enabled) {
		if (plan == null) {
			return;
		}
		if (plan.id < 0) {
			if (plan.repeatType == RepeatType.ONE_OFF) {
				plan.startTime = calNextPlanTimeInMillis(plan);
			}
			plan.enabled = enabled;
			plan.id = (int) mPlanDBHelper.insertPlan(plan);
		} else {
			ContentValues values = new ContentValues(2);
			values.put(BackupPlan.Columns.ENABLED, enabled ? 1 : 0);
			if (enabled) {
				if (plan.repeatType == RepeatType.ONE_OFF) {
					values.put(BackupPlan.Columns.START_TIME, calNextPlanTimeInMillis(plan));
				}
			}
			mPlanDBHelper.updatePlan(plan.id, values);
		}
	}

	/**
	 * 获取下一次计划的启动时间，以当前时间为参照点，返回值直接用于AlarmManager的设置
	 *
	 * @param plan
	 * @return
	 */
	public static long calNextPlanTimeInMillis(BackupPlan plan) {
		if (plan == null) {
			return 0;
		}
		final long now = System.currentTimeMillis();
		if (plan.repeatType == RepeatType.ONE_OFF || plan.startTime > now) {
			return plan.startTime;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(now);
		Date nowDate = calendar.getTime();
		final int hour = plan.hour;
		final int minutes = plan.minutes;
		int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
		int nowMinute = calendar.get(Calendar.MINUTE);
		if (hour < nowHour || hour == nowHour && minutes <= nowMinute) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		// 计算天数的偏移
		int dayOffset = calNextPlanDayOffsetFromDate(calendar.getTime(), plan);
		calendar.add(Calendar.DAY_OF_YEAR, dayOffset);

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date date = calendar.getTime();
		return calendar.getTimeInMillis();
	}

	private static int calNextPlanDayOffsetFromDate(Date date, BackupPlan plan) {
		if (plan == null || date == null) {
			return 0;
		}
		final int m7 = 7;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		final int hour = plan.hour;
		final int minutes = plan.minutes;
		final int hourNow = calendar.get(Calendar.HOUR_OF_DAY);
		final int minuteNow = calendar.get(Calendar.MINUTE);
		int dayOffset = 0;
		switch (plan.repeatType) {
			case WEEKLY :
				int dayOfWeekNow = calendar.get(Calendar.DAY_OF_WEEK);
				int dayOfWeek = plan.dayOfWeek;
				for (dayOffset = 0; dayOffset < m7; dayOffset++) {
					if ((dayOfWeekNow + dayOffset) % m7 == dayOfWeek) {
						break;
					}
				}
				break;
			case MONTHLY :
				int dayOfMonth = plan.dayOfMonth;
				int dayOfMonthNow = calendar.get(Calendar.DAY_OF_MONTH);
				if (hour < hourNow && minutes < minuteNow && dayOfMonth == dayOfMonthNow) {
					dayOffset = 0;
					break;
				}
				// Date dateNow = calendar.getTime();
				int dayCountOfThisMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				if (dayOfMonthNow > dayOfMonth || dayOfMonthNow < dayOfMonth
						&& dayCountOfThisMonth < dayOfMonth) {
					do {
						calendar.add(Calendar.MONTH, 1);
					} while (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) < dayOfMonth);
				}
				calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				Date nextDate = calendar.getTime();
				dayOffset = (int) ((nextDate.getTime() - date.getTime()) / ONE_DAY_IN_MILLS);
				break;
			case DAILY :
			default :
				dayOffset = 0;
				break;
		}
		return dayOffset;
	}

	public void addPlan(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		plan.id = (int) mPlanDBHelper.insertPlan(plan);
	}

	public void deletePlan(int planId) {
		if (planId < 0) {
			return;
		}
		// mPlanDBHelper.deletePlan(planId);
		// scheduleNextPlan();
		if (mPlanDBHelper.deletePlan(planId)) {
			BackupPlan plan = new BackupPlan();
			plan.id = planId;
			cancelScheduledPlan(plan);
		}
	}

	private void updatePlan(BackupPlan plan) {
		if (plan == null) {
			return;
		}
		ContentValues values = new ContentValues();
		values.put(BackupPlan.Columns.BACKUP_TYPE, plan.type.getBackupType());
		values.put(BackupPlan.Columns.HOUR, plan.hour);
		values.put(BackupPlan.Columns.MINUTES, plan.minutes);
		values.put(BackupPlan.Columns.START_TIME, plan.startTime);
		values.put(BackupPlan.Columns.REPEAT_TYPE, plan.repeatType.ordinal());
		values.put(BackupPlan.Columns.DAY_OF_WEEK, plan.dayOfWeek);
		values.put(BackupPlan.Columns.DAY_OF_MONTH, plan.dayOfMonth);
		values.put(BackupPlan.Columns.REMINDER, plan.reminder);
		values.put(BackupPlan.Columns.ENABLED, plan.enabled ? 1 : 0);
		values.put(BackupPlan.Columns.RUN_TIMES, plan.runTimes);
		mPlanDBHelper.updatePlan(plan.id, values);
	}

	public long savePlan(BackupPlan plan) {
		if (plan == null) {
			return -1;
		}
		if (plan.id < 0) {
			addPlan(plan);
		} else {
			updatePlan(plan);
		}
		// scheduleNextPlan();
		return plan.enabled ? schedulePlan(plan) : -1;
	}

	public Cursor getAllScheduledPlansCursor() {
		return mPlanDBHelper.getAllScheduledPlansCursor();
	}

	public Cursor getEnabledPlansCursor() {
		return mPlanDBHelper.getEnabledPlansCursor();
	}

	public boolean isPlanExpired(BackupPlan plan) {
		if (plan == null) {
			return false;
		}
		if (plan.repeatType == RepeatType.ONE_OFF && plan.startTime <= System.currentTimeMillis()) {
			return true;
		}
		return false;
	}

	public boolean isTimeConflictedWithOthers(BackupPlan plan) {
		if (plan == null) {
			return false;
		}
		final Cursor cursor = getEnabledPlansCursor();
		if (cursor == null) {
			return false;
		}

		if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
			cursor.close();
			return false;
		}

		Set<BackupPlan> plans = new HashSet<BackupPlan>();
		try {
			do {
				BackupPlan otherPlan = new BackupPlan(cursor);
				if (otherPlan.id == plan.id) {
					continue;
				}
				plans.add(otherPlan);
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}

		final long thisPlanStartTime = calNextPlanTimeInMillis(plan);
		for (BackupPlan otherPlan : plans) {
			final long otherPlanStartTime = calNextPlanTimeInMillis(otherPlan);
			if (thisPlanStartTime == otherPlanStartTime) {
				return true;
			}
		}
		return false;
	}

	public void release() {
		closeDatabase();
	}

	private void closeDatabase() {
		if (mPlanDBHelper != null) {
			mPlanDBHelper.close();
		}
	}

	// 格式化下次启动定时备份剩余时间展示
	public static String formatRemainingTimeToast(Context context, long timeInMillis) {
		if (context == null) {
			return null;
		}
		long delta = timeInMillis - System.currentTimeMillis();
		if (delta < 0) {
			return context.getString(R.string.msg_schedule_expired);
		}

		long hours = delta / (60 * 60 * 1000);
		long minutes = delta / (60 * 1000) % 60;
		long days = hours / 24;
		hours = hours % 24;

		String daySeg = (days == 0) ? "" : (days == 1)
				? context.getString(R.string.one_day)
				: context.getString(R.string.days, Long.toString(days));
		String hourSeg = (hours == 0) ? "" : (hours == 1)
				? context.getString(R.string.one_hour)
				: context.getString(R.string.hours, Long.toString(hours));
		String minSeg = (minutes == 0) ? "" : (minutes == 1) ? context
				.getString(R.string.one_minute) : context.getString(R.string.minutes,
				Long.toString(minutes));

		boolean displayDay = days > 0;
		boolean displayHour = hours > 0;
		boolean displayMinute = minutes > 0;
		int index = (displayDay ? 1 : 0) | (displayHour ? 2 : 0) | (displayMinute ? 4 : 0);
		String[] formats = context.getResources().getStringArray(R.array.advance_remind_time_toast);
		return String.format(formats[index], daySeg, hourSeg, minSeg);
	}
}
