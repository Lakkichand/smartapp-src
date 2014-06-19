package com.jiubang.go.backup.pro.calendar;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.jiubang.go.backup.pro.calendar.CustonCalendarContract.CalendarEntity;
import com.jiubang.go.backup.pro.calendar.CustonCalendarContract.Events;

/**
 * 日历操作类 用于操作日历相关，包括保存、恢复日历等
 *
 * @author wencan
 *
 */

public class CalendarOperator {
	private static final String LOG_TAG = CalendarOperator.class.getSimpleName();

	/**
	 * 日历操作监听接口
	 *
	 * @author wencan
	 *
	 */
	public interface OnCalendarOperateListener {
		void onCalendarOperateStart(CalendarOperator co);
		void onCalendarOperateProcess(CalendarOperator co, int totalEvent, int eventIndex);
		void onCalendarOperateEnd(CalendarOperator co, boolean success, int totalEventCount,
				int successOperateCount);
	}

	private static final int LOCAL_SDK = Build.VERSION.SDK_INT;

	private List<CalendarStruct> mCalendars;
	private OnCalendarOperateListener mListener;
	private Account[] mAccounts;
	private int mCalendarCount = 0;
	private int mEventCount = 0;
	private int mCalendarIndex = 0;
	private int mEventIndex = 0;
	private int mSuccessOperateEventCount = 0;

	private boolean mStopRestoreFlag = false;
	private boolean mRestoreing = false;

	/**
	 * 从本地数据库中构造日历
	 */
	public CalendarOperator(Context context) {
		loadLocalCalendar(context);
		reset();
	}

	/**
	 * 从已有日历列表中构建
	 *
	 * @param calendars
	 */
	public CalendarOperator(List<CalendarStruct> calendars) {
		mCalendars = calendars;
		reset();
	}

	/**
	 * 从文件中构建
	 * @param context
	 * @param srcFile
	 */
	public CalendarOperator(Context context, File srcFile) {
		CalendarXmlParser calendarXmlParser = new CalendarXmlParser(context, srcFile);
		mCalendars = calendarXmlParser.parser();
		if (mCalendars == null) {
			// 兼容解析json
			CalendarJsonParser calendarJsonParser = new CalendarJsonParser(context, srcFile);
			mCalendars = calendarJsonParser.parser();
		}
		reset();
	}

	public static List<CalendarStruct> loadCalendarFromFile(Context context, File srcFile) {
		if (context == null || srcFile == null) {
			return null;
		}

		List<CalendarStruct> calendars = null;
		CalendarXmlParser calendarXmlParser = new CalendarXmlParser(context, srcFile);
		calendars = calendarXmlParser.parser();
		if (calendars == null) {
			calendars = new CalendarJsonParser(context, srcFile).parser();
		}
		return calendars;
	}

	private void reset() {
		mCalendarCount = 0;
		mEventCount = 0;
		mCalendarIndex = 0;
		mEventIndex = 0;
		setRestoreStopFlag(false);
		mRestoreing = false;

		resetEventCount();
		resetCalendarCount();
	}

	/**
	 * 释放资源，释放后不能再调用相关方法
	 */
	public void release() {
		if (mCalendars != null) {
			mCalendars.clear();
			mCalendars = null;
		}
		mCalendarCount = 0;
		mEventCount = 0;
		mCalendarIndex = 0;
		mEventIndex = 0;
		mSuccessOperateEventCount = 0;
		mListener = null;
	}

	/**
	 * 获取日历帐号个数
	 *
	 * @return
	 */
	public int getCalendarCount() {
		return mCalendarCount;
	}

	private void resetCalendarCount() {
		mCalendarCount = mCalendars != null ? mCalendars.size() : 0;
	}

	/**
	 * 获取事件个数，包含全部日历的所有事件
	 *
	 * @return
	 */
	public int getEventCount() {
		return mEventCount;
	}
	
	/**
	 * 获取本地日历事件个数
	 * @param context
	 * @return
	 */
	public static int getLocalEventCount(Context context) {
		if (context == null) {
			return 0;
		}

		int count = 0;
		final ContentResolver cr = context.getContentResolver();

		Cursor calendarCursor = null;
		String[] projection = new String[] { CustonCalendarContract.Calendars._ID };
		try {
			calendarCursor = cr.query(CustonCalendarContract.Calendars.CONTENT_URI, projection,
					CustonCalendarContract.Calendars.CALENDAR_NOT_DELETE_WHERE, null, null);
			if (calendarCursor != null && calendarCursor.moveToFirst()) {
				do {
					int calendarId = calendarCursor.getInt(calendarCursor
							.getColumnIndex(CustonCalendarContract.Calendars._ID));
					String where = CustonCalendarContract.Events.CALENDAR_ID + "=" + calendarId
							+ " AND " + CustonCalendarContract.Events.DELETED + "=" + "0" + " AND "
							+ CustonCalendarContract.Events.EVENT_TIMEZONE + " IS NOT NULL";
					projection = new String[] { CustonCalendarContract.Events._ID/*,
							CustonCalendarContract.Events.DELETED,
							CustonCalendarContract.Events.EVENT_TIMEZONE*/ };

					Cursor eventCursor = null;
					try {
						eventCursor = cr.query(CustonCalendarContract.Events.CONTENT_URI,
								projection, where, null, null);
						if (eventCursor != null) {
							count += eventCursor.getCount();
						}
					} finally {
						if (eventCursor != null) {
							eventCursor.close();
							eventCursor = null;
						}
					}
				} while (calendarCursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (calendarCursor != null) {
				calendarCursor.close();
				calendarCursor = null;
			}
		}

		return count;
	}

	private void resetEventCount() {
		if (mCalendars == null || mCalendars.size() == 0) {
			mEventCount = 0;
			return;
		}

		int eventCount = 0;
		for (CalendarStruct calendar : mCalendars) {
			eventCount += calendar.getEventCount();
		}
		mEventCount = eventCount;
	}

	/**
	 * 持久化日历
	 * @param context
	 * @param destFile
	 * @param listener
	 * @return
	 */
	public boolean persistCalendar(Context context, File destFile,
			OnCalendarPersistListener listener) {
		if (context == null || destFile == null) {
			return false;
		}

		if (mCalendars == null || mCalendars.size() == 0) {
			return false;
		}

		CalendarXmlParser calendarParser = new CalendarXmlParser(context, mCalendars);
		calendarParser.setOnCalendarPersistListener(listener);
		return calendarParser.persist(destFile);
	}

	/**
	 * 从文件中恢复日历到本地
	 *
	 * @param context
	 * @return
	 */
	public boolean restoreCalendar(Context context) {
		if (mListener != null) {
			mListener.onCalendarOperateStart(this);
		}
		if (context == null || mCalendars == null || mCalendars.size() == 0) {
			if (mListener != null) {
				mListener.onCalendarOperateEnd(this, false, getEventCount(),
						mSuccessOperateEventCount);
			}
			return false;
		}

		boolean ret = true;
		mCalendarIndex = 0;
		mEventIndex = 0;
		mRestoreing = true;
		setRestoreStopFlag(false);
		for (CalendarStruct calendarStruct : mCalendars) {
			if (getRestoreStopFlag()) {
				// 停止
				break;
			}
			try {
				ret = restoreCalendarInternal(context, calendarStruct);
			} catch (Exception e) {
				e.printStackTrace();
				ret = false;
			}
			if (!ret) {
				break;
			}
			mCalendarIndex++;
		}
		if (mListener != null) {
			mListener.onCalendarOperateEnd(this, ret, getEventCount(), mSuccessOperateEventCount);
		}
		mRestoreing = false;
		return ret;
	}

	/**
	 * 停止恢复日历
	 */
	public void stopRestoreCalendar() {
		if (mRestoreing) {
			setRestoreStopFlag(true);
		}
	}

	private synchronized void setRestoreStopFlag(boolean stop) {
		mStopRestoreFlag = stop;
	}

	private synchronized boolean getRestoreStopFlag() {
		return mStopRestoreFlag;
	}

	/**
	 * 注册日历操作回调监听
	 *
	 * @param listener
	 */
	public void registerOnCalendarOperateListener(OnCalendarOperateListener listener) {
		mListener = listener;
	}

	/**
	 * 反注册日历操作回调监听
	 *
	 * @param listener
	 */
	public void unregisterOnCalendarOperateListener(OnCalendarOperateListener listener) {
		if (mListener == listener) {
			mListener = null;
		}
	}

	/**
	 * 合并日历
	 *
	 * @param calendars
	 * @return
	 */
	public boolean mergeCalendar(List<CalendarStruct> calendars) {
		if (calendars == null || calendars.size() == 0) {
			return false;
		}

		boolean change = false;
		boolean contain = false;
		List<CalendarStruct> temp = new ArrayList<CalendarOperator.CalendarStruct>();
		for (CalendarStruct toCalendarStruct : calendars) {
			contain = false;
			for (CalendarStruct beCalendarStruct : mCalendars) {
				if (beCalendarStruct.isSameCalendarAccount(toCalendarStruct)) {
					// 相同日历帐号, 做适配
					toCalendarStruct = adapteCalendarInternal(toCalendarStruct,
							beCalendarStruct.getCalendarSdkVersion());

					List<Entity> toEvents = toCalendarStruct.getEventEntities();
					List<Entity> beEvents = beCalendarStruct.getEventEntities();

					for (Entity event : toEvents) {
						if (beCalendarStruct.containEvent(event)) {
							// 已经存在相同事件，不处理
							continue;
						}

						// 不存在该事件，合并
						beEvents.add(event);
						change = true;
					}
					contain = true;
					break;
				}
			}

			if (!contain) {
				// 不相同日历帐号，直接添加
				temp.add(toCalendarStruct);
				change = true;
			}
		}
		if (temp.size() > 0) {
			mCalendars.addAll(temp);
		}
		temp.clear();
		reset();
		return change;
	}

	/**
	 * 判断日历是否为空，为空的条件是所有的日历帐号都没有事件
	 *
	 * @param context
	 * @return
	 */
	public static boolean isCalendarEmpty(Context context) {
		return getLocalEventCount(context) > 0 ? false : true;
	}

	private boolean isAccountExist(Account[] accounts, Account account) {
		if (accounts == null || account == null) {
			return false;
		}
		final int lenght = accounts.length;
		for (int i = 0; i < lenght; i++) {
			Account tempAccount = accounts[i];
			if (tempAccount != null && tempAccount.equals(account)) {
				return true;
			}
		}
		return false;
	}

	private Account getAccountByType(Account[] accounts, String type) {
		if (accounts == null || type == null) {
			return null;
		}
		final int lenght = accounts.length;
		for (int i = 0; i < lenght; i++) {
			Account tempAccount = accounts[i];
			if (tempAccount != null && TextUtils.equals(tempAccount.type, type)) {
				return tempAccount;
			}
		}
		return null;
	}

	private int queryCalendarIdByAccount(Context context, Account account) {
		if (context == null || account == null) {
			return -1;
		}

		final ContentResolver cr = context.getContentResolver();
		String accountTypeKey = CustonCalendarContract.Calendars.getAccountTypeKey(LOCAL_SDK);
		String accountNameKey = CustonCalendarContract.Calendars.getAccountNameKey(LOCAL_SDK);

		String[] projection = { accountTypeKey, accountNameKey,
				CustonCalendarContract.Calendars._ID };
		String where = accountNameKey + "='" + account.name + "'" + " AND " + accountTypeKey + "='"
				+ account.type + "'";
		Cursor cursor = null;
		int calendarId = -1;
		try {
			cursor = cr.query(CustonCalendarContract.Calendars.CONTENT_URI, projection, where,
					null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				calendarId = cursor.getInt(cursor
						.getColumnIndex(CustonCalendarContract.Calendars._ID));
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return calendarId;
	}

	private CalendarStruct adapterCalendarWithAccount(CalendarStruct calendarStruct,
			Account account, int calendarId) {
		if (calendarStruct == null || account == null || calendarId == -1) {
			return null;
		}

		Entity calendarEntity = calendarStruct.getCalendarEntity();
		String localAccountTypeKey = CustonCalendarContract.Calendars.getAccountTypeKey(LOCAL_SDK);
		String localAccountNameKey = CustonCalendarContract.Calendars.getAccountNameKey(LOCAL_SDK);

		ContentValues cv = calendarEntity.getEntityValues();
		cv.put(localAccountTypeKey, account.type);
		cv.put(localAccountNameKey, account.name);
		cv.put(CustonCalendarContract.Calendars._ID, calendarId);

		List<Entity> events = calendarStruct.getEventEntities();
		if (events != null) {
			for (Entity event : events) {
				ContentValues eventCv = event.getEntityValues();
				eventCv.put(CustonCalendarContract.Events.CALENDAR_ID, calendarId);

				// 去掉提醒邮件
				List<NamedContentValues> allSub = event.getSubValues();
				if (allSub != null && allSub.size() > 0) {
					ListIterator<NamedContentValues> iterator = allSub.listIterator();
					while (iterator.hasNext()) {
						NamedContentValues value = iterator.next();
						if (value.uri.equals(CustonCalendarContract.Attendees.CONTENT_URI)) {
							iterator.remove();
						}
					}
				}
			}
		}
		return calendarStruct;
	}

	private boolean restoreCalendarInternal(Context context, CalendarStruct calendar) {
		if (context == null || calendar == null) {
			return false;
		}

		final ContentResolver cr = context.getContentResolver();

		// 根据本地sdk版本，做适配转换
		calendar = adapteCalendarInternal(calendar, LOCAL_SDK);
		if (calendar == null) {
			return false;
		}

		Entity calendarEntity = calendar.getCalendarEntity();

		String localAccountTypeKey = CustonCalendarContract.Calendars.getAccountTypeKey(LOCAL_SDK);
		String localAccountNameKey = CustonCalendarContract.Calendars.getAccountNameKey(LOCAL_SDK);
		Account account = getCalendarAccountFromEntity(calendarEntity, localAccountTypeKey,
				localAccountNameKey);
		if (account == null) {
			return false;
		}

		boolean needUpdateCalendar = true;
		boolean needCreateCalendar = false;
		boolean needChangeAccount = false;

		Account tempAccount = null;
		Account[] localCalendarAccount = queryLocalCalendarAccount(context);
		if (LOCAL_SDK >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// 如果是大于等于14，则创建账户
			needChangeAccount = false;
			needCreateCalendar = true;
			needUpdateCalendar = true;
		} else {
			// 小于14
			if (localCalendarAccount == null) {
				needChangeAccount = false;
				needCreateCalendar = true;
				needUpdateCalendar = true;
			} else if (!isAccountExist(localCalendarAccount, account)) {
				needChangeAccount = true;
				needUpdateCalendar = false;
				needCreateCalendar = false;
			}
		}

		if (needChangeAccount) {
			// 本地日历不存在该帐号
			tempAccount = getAccountByType(localCalendarAccount,
					CustonCalendarContract.ACCOUNT_TYPE_LOCAL);
			if (tempAccount == null) {
				// 本地帐号不存在
				tempAccount = getAccountByType(localCalendarAccount,
						CustonCalendarContract.ACCOUNT_TYPE_GOOGLE);
			}

			if (tempAccount == null) {
				// 找出一个本地日历帐号，恢复到该帐号中
				tempAccount = localCalendarAccount[0];
			}

			int calendarId = queryCalendarIdByAccount(context, tempAccount);
			if (calendarId == -1) {
				return false;
			}
			// 做帐号转换
			calendar = adapterCalendarWithAccount(calendar, tempAccount, calendarId);
			if (calendar == null) {
				return false;
			}
		}

		// 获取备份文件中备份的日历id
		int srcCalendarId = getCalendarId(calendarEntity);
		// 获取备份文件中日历对应在本地的id
		int localCalendarId = getLocalCalendarId(cr, calendarEntity);
		if (localCalendarId != -1) {
			if (needUpdateCalendar) {
				//存在相同帐号日历id,更新 TODO
				Uri.Builder builder = CustonCalendarContract.Calendars.CONTENT_URI.buildUpon();
				builder.appendQueryParameter(
						android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true");
				builder.appendQueryParameter(localAccountTypeKey, account.type);
				builder.appendQueryParameter(localAccountNameKey, account.name);
				ContentUris.appendId(builder, localCalendarId);

				ContentValues updateValues = new ContentValues(calendarEntity.getEntityValues());
				updateValues.remove(CustonCalendarContract.Calendars._ID);
				updateValues = validateCalendarDataWhenUpdate(updateValues);
				if (updateValues == null) {
					return false;
				}
				int rowCount = cr.update(builder.build(), updateValues, null, null);
				if (rowCount < 1) {
					return false;
				}
			}
		} else {
			if (needCreateCalendar) {
				ContentValues insertValues = new ContentValues(calendarEntity.getEntityValues());
				insertValues.remove(CustonCalendarContract.Calendars._ID);
				insertValues = validateCalendarDataWhenInsert(insertValues);
				if (insertValues == null) {
					return false;
				}

				Uri targetUri = CustonCalendarContract.Calendars.CONTENT_URI
						.buildUpon()
						.appendQueryParameter(
								android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true")
						.appendQueryParameter(localAccountTypeKey, account.type)
						.appendQueryParameter(localAccountNameKey, account.name).build();
				Uri uri = cr.insert(targetUri, insertValues);
				if (uri == null) {
					Log.d(LOG_TAG, "restoreCalendarInternal : calendar is not exist, insert faild");
					return false;
				}
				try {
					localCalendarId = (int) ContentUris.parseId(uri);
				} catch (Exception e) {
					localCalendarId = -1;
				}
			}
		}

		if (localCalendarId == -1) {
			return false;
		}

		if (srcCalendarId != localCalendarId) {
			List<Entity> events = calendar.getEventEntities();
			if (events != null) {
				for (Entity event : events) {
					event.getEntityValues().put(CustonCalendarContract.EventsEntity.CALENDAR_ID,
							localCalendarId);
				}
			}
		}

		// 恢复event
		List<Entity> events = calendar.getEventEntities();
		if (events == null) {
			// 该日历没有事件
			return true;
		}

		for (Entity event : events) {
			if (getRestoreStopFlag()) {
				// 停止
				break;
			}
			if (restoreEvent(cr, account, event)) {
				mSuccessOperateEventCount++;
			}
			mEventIndex++;
			if (mListener != null) {
				mListener.onCalendarOperateProcess(this, getEventCount(), mEventIndex);
			}
		}
		return true;
	}

	private Account[] queryLocalCalendarAccount(Context context) {
		if (context == null) {
			return null;
		}

		final ContentResolver cr = context.getContentResolver();
		String[] projection = { CustonCalendarContract.Calendars.getAccountTypeKey(LOCAL_SDK),
				CustonCalendarContract.Calendars.getAccountNameKey(LOCAL_SDK),
				CustonCalendarContract.Calendars._ID };
		Cursor cursor = null;
		List<Account> accounts = new ArrayList<Account>();
		try {
			cursor = cr.query(CustonCalendarContract.Calendars.CONTENT_URI, projection, null, null,
					null);
			if (cursor != null && cursor.getCount() > 0) {
				String accountType = null;
				String accountName = null;

				cursor.moveToFirst();
				do {
					accountType = cursor.getString(cursor
							.getColumnIndex(CustonCalendarContract.Calendars
									.getAccountTypeKey(LOCAL_SDK)));
					accountName = cursor.getString(cursor
							.getColumnIndex(CustonCalendarContract.Calendars
									.getAccountNameKey(LOCAL_SDK)));
					if (accountType != null && accountName != null) {
						accounts.add(new Account(accountName, accountType));
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		if (accounts == null || accounts.size() == 0) {
			return null;
		}

		Account[] result = new Account[accounts.size()];
		return accounts.toArray(result);
	}

	private Account getCalendarAccountFromEntity(Entity calendarEntity, String accountTypeKey,
			String accountNameKey) {
		if (calendarEntity == null || accountTypeKey == null || accountNameKey == null) {
			return null;
		}

		String accountType = getEntityValue(calendarEntity, accountTypeKey);
		String accountName = getEntityValue(calendarEntity, accountNameKey);
		if (accountName == null || accountType == null) {
			return null;
		}
		Account account = new Account(accountName, accountType);
		return account;
	}

	private boolean restoreEvent(ContentResolver resolver, Account account, Entity event) {
		int localEventId = getLocalEventId(resolver, event);
		if (localEventId != -1) {
			// 存在event TODO 更新
			ContentValues updateValues = new ContentValues(event.getEntityValues());
			updateValues.remove(CustonCalendarContract.EventsEntity._ID);
			updateValues = validateEventDataWhenUpdate(updateValues);
			if (updateValues == null) {
				return false;
			}

			Uri.Builder builder = CustonCalendarContract.Events.CONTENT_URI.buildUpon();
			String localAccountTypeKey = CustonCalendarContract.Calendars
					.getAccountTypeKey(LOCAL_SDK);
			String localAccountNameKey = CustonCalendarContract.Calendars
					.getAccountNameKey(LOCAL_SDK);
			//			builder.appendQueryParameter(
			//					android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true");
			//			builder.appendQueryParameter(localAccountTypeKey, account.type);
			//			builder.appendQueryParameter(localAccountNameKey, account.name);
			ContentUris.appendId(builder, localEventId);

			int rowCount = resolver.update(builder.build(), updateValues, null, null);
			if (rowCount < 1) {
				Log.d(LOG_TAG, "restoreEvent update faild");
				return false;
			}

			// 删除subvalues
			deleteSubValuesForEvent(resolver, account, localEventId);

			ArrayList<NamedContentValues> subValues = event.getSubValues();
			if (subValues != null) {
				for (NamedContentValues item : subValues) {
					item.values.remove(BaseColumns._ID);
					reflashEventIdForSubValues(item, localEventId);

					Uri uri = null;
					if (item.uri.equals(CustonCalendarContract.ExtendedProperties.CONTENT_URI)) {
						builder = item.uri.buildUpon();
						builder.appendQueryParameter(
								android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true");
						builder.appendQueryParameter(localAccountTypeKey, account.type);
						builder.appendQueryParameter(localAccountNameKey, account.name);
						uri = resolver.insert(builder.build(), item.values);
					} else {
						uri = resolver.insert(item.uri, item.values);
					}
				}
			}
		} else {
			ContentValues insertValues = new ContentValues(event.getEntityValues());
			insertValues.remove(CustonCalendarContract.EventsEntity._ID);
			insertValues = validateEventDataWhenInsert(insertValues);
			if (insertValues == null) {
				Log.d(LOG_TAG, "restoreEvent validateEventDataWhenInsert faild");
				return false;
			}

			Uri uri = resolver.insert(Events.CONTENT_URI, insertValues);
			if (uri == null) {
				Log.d(LOG_TAG, "restoreEvent : insert faild");
				return false;
			}

			int newEventId;
			try {
				newEventId = (int) ContentUris.parseId(uri);
			} catch (Exception e) {
				newEventId = -1;
			}

			if (newEventId == -1) {
				Log.d(LOG_TAG, "restoreEvent : insert success, parseId faild");
				return false;
			}

			// 删除subvalues
			deleteSubValuesForEvent(resolver, account, newEventId);

			ArrayList<NamedContentValues> subValues = event.getSubValues();
			if (subValues != null) {
				for (NamedContentValues item : subValues) {
					item.values.remove(BaseColumns._ID);
					reflashEventIdForSubValues(item, newEventId);
					Uri subUri = null;
					if (item.uri.equals(CustonCalendarContract.ExtendedProperties.CONTENT_URI)) {
						int localSdk = Build.VERSION.SDK_INT;
						String localAccountTypeKey = CustonCalendarContract.Calendars
								.getAccountTypeKey(localSdk);
						String localAccountNameKey = CustonCalendarContract.Calendars
								.getAccountNameKey(localSdk);
						Uri.Builder builder = item.uri.buildUpon();
						builder.appendQueryParameter(
								android.provider.CalendarContract.CALLER_IS_SYNCADAPTER, "true");
						builder.appendQueryParameter(localAccountTypeKey, account.type);
						builder.appendQueryParameter(localAccountNameKey, account.name);
						subUri = resolver.insert(builder.build(), item.values);
					} else {
						subUri = resolver.insert(item.uri, item.values);
					}
				}
			}
		}
		return true;
	}

	private ContentValues validateEventDataWhenUpdate(ContentValues values) {
		values.remove(CustonCalendarContract.Events._ID);
		// 过滤SELF_ATTENDEE_STATUS 字段
		if (values.containsKey(CustonCalendarContract.Events.SELF_ATTENDEE_STATUS)) {
			values.remove(CustonCalendarContract.Events.SELF_ATTENDEE_STATUS);
		}
		if (values.containsKey(CustonCalendarContract.Events._SYNC_ID)) {
			values.remove(CustonCalendarContract.Events._SYNC_ID);
		}
		if (values.containsKey(CustonCalendarContract.Events.HTML_URI_FROYO)) {
			values.remove(CustonCalendarContract.Events.HTML_URI_FROYO);
		}

		boolean hasDtend = values.getAsLong(CustonCalendarContract.Events.DTEND) != null;
		boolean hasDuration = !TextUtils.isEmpty(values
				.getAsString(CustonCalendarContract.Events.DURATION));
		boolean hasRrule = !TextUtils.isEmpty(values
				.getAsString(CustonCalendarContract.Events.RRULE));
		boolean hasRdate = !TextUtils.isEmpty(values
				.getAsString(CustonCalendarContract.Events.RDATE));
		boolean hasOriginalEvent = !TextUtils.isEmpty(values
				.getAsString(CustonCalendarContract.Events.ORIGINAL_EVENT_FROYO));
		boolean hasOriginalInstanceTime = values.getAsLong(Events.ORIGINAL_INSTANCE_TIME) != null;
		if (hasRrule || hasRdate) {
			if (hasDtend || !hasDuration || hasOriginalEvent || hasOriginalInstanceTime) {
				values.remove(Events.DTEND);
				values.remove(Events.ORIGINAL_EVENT_FROYO);
				values.remove(Events.ORIGINAL_INSTANCE_TIME);
			}
		} else if (hasOriginalEvent || hasOriginalInstanceTime) {
			if (!hasDtend || hasDuration || !hasOriginalEvent || !hasOriginalInstanceTime) {
				values.remove(Events.DURATION);
			}
		} else {
			if (!hasDtend || hasDuration) {
				values.remove(Events.DURATION);
			}
		}
		return values;
	}

	private ContentValues validateCalendarDataWhenUpdate(ContentValues values) {
		return values;
	}

	private ContentValues validateEventDataWhenInsert(ContentValues values) {
		if (values.containsKey(CustonCalendarContract.EventsEntity._SYNC_ID)) {
			values.remove(CustonCalendarContract.Events._SYNC_ID);
		}

		boolean hasDtend = values.getAsLong(CustonCalendarContract.Events.DTEND) != null;
		boolean hasDuration = !TextUtils.isEmpty(values
				.getAsString(CustonCalendarContract.Events.DURATION));
		boolean hasRrule = !TextUtils.isEmpty(values
				.getAsString(CustonCalendarContract.Events.RRULE));
		boolean hasRdate = !TextUtils.isEmpty(values
				.getAsString(CustonCalendarContract.Events.RDATE));
		boolean hasOriginalEvent = !TextUtils.isEmpty(values
				.getAsString(CustonCalendarContract.Events.ORIGINAL_EVENT_FROYO));
		boolean hasOriginalInstanceTime = values.getAsLong(Events.ORIGINAL_INSTANCE_TIME) != null;
		if (hasRrule || hasRdate) {
			if (hasDtend || !hasDuration || hasOriginalEvent || hasOriginalInstanceTime) {
				values.remove(Events.DTEND);
				values.remove(Events.ORIGINAL_EVENT_FROYO);
				values.remove(Events.ORIGINAL_INSTANCE_TIME);
			}
		} else if (hasOriginalEvent || hasOriginalInstanceTime) {
			if (!hasDtend || hasDuration || !hasOriginalEvent || !hasOriginalInstanceTime) {
				values.remove(Events.DURATION);
			}
		} else {
			if (!hasDtend || hasDuration) {
				values.remove(Events.DURATION);
			}
		}

		// 插入event必须包含evenTimezone字段
		if (!values.containsKey(CustonCalendarContract.Events.EVENT_TIMEZONE)) {
			return null;
		}
		return values;
	}

	private ContentValues validateCalendarDataWhenInsert(ContentValues values) {
		return values;
	}

	private void deleteSubValuesForEvent(ContentResolver resolver, Account account, int eventId) {
		if (resolver == null || account == null) {
			return;
		}
		int count = 0;
		count = resolver.delete(CustonCalendarContract.Attendees.CONTENT_URI,
				CustonCalendarContract.Attendees.EVENT_ID + "=" + eventId, null);
		count = resolver.delete(CustonCalendarContract.Reminders.CONTENT_URI,
				CustonCalendarContract.Reminders.EVENT_ID + "=" + eventId, null);

		int localSdk = Build.VERSION.SDK_INT;
		String localAccountTypeKey = CustonCalendarContract.Calendars.getAccountTypeKey(localSdk);
		String localAccountNameKey = CustonCalendarContract.Calendars.getAccountNameKey(localSdk);
		Uri.Builder builder = CustonCalendarContract.ExtendedProperties.CONTENT_URI.buildUpon();
		builder.appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,
				"true");
		builder.appendQueryParameter(localAccountTypeKey, account.type);
		builder.appendQueryParameter(localAccountNameKey, account.name);
		count = resolver.delete(/*
								 * CustonCalendarContract.ExtendedProperties.
								 * CONTENT_URI
								 */builder.build(),
				CustonCalendarContract.ExtendedProperties.EVENT_ID + "=" + eventId, null);
	}

	private void reflashEventIdForSubValues(NamedContentValues subValues, int newEventId) {
		if (subValues == null) {
			return;
		}
		if (CustonCalendarContract.Attendees.CONTENT_URI.equals(subValues.uri)) {
			subValues.values.put(CustonCalendarContract.Attendees.EVENT_ID, newEventId);
		} else if (CustonCalendarContract.Reminders.CONTENT_URI.equals(subValues.uri)) {
			subValues.values.put(CustonCalendarContract.Reminders.EVENT_ID, newEventId);
		} else if (CustonCalendarContract.ExtendedProperties.CONTENT_URI.equals(subValues.uri)) {
			subValues.values.put(CustonCalendarContract.ExtendedProperties.EVENT_ID, newEventId);
		}
	}

	private int getLocalEventId(ContentResolver resolver, Entity event) {
		if (resolver == null || event == null) {
			return -1;
		}

		//TODO 如何判断两个event是否相同
		String strCalendarId = getEntityValue(event, CustonCalendarContract.Events.CALENDAR_ID);
		final int calendarId = strCalendarId != null ? Integer.valueOf(strCalendarId) : -1;

		String title = getEntityValue(event, CustonCalendarContract.Events.TITLE);
		String dtStart = getEntityValue(event, CustonCalendarContract.Events.DTSTART);
		String dtEnd = getEntityValue(event, CustonCalendarContract.Events.DTEND);
		String describe = getEntityValue(event, CustonCalendarContract.Events.DESCRIPTION);
		String where = CustonCalendarContract.Events.CALENDAR_ID + "=" + calendarId;
		if (!TextUtils.isEmpty(dtStart)) {
			where += " AND " + CustonCalendarContract.Events.DTSTART + "=" + dtStart;
		}
		if (!TextUtils.isEmpty(dtEnd)) {
			where += " AND " + CustonCalendarContract.Events.DTEND + "=" + dtEnd;
		}
		if (!TextUtils.isEmpty(title)) {
			title = title.replace("'", "''");
			where += " AND " + CustonCalendarContract.Events.TITLE + "='" + title + "'";
		}
		if (!TextUtils.isEmpty(describe)) {
			describe = describe.replace("'", "''");
			where += " AND " + CustonCalendarContract.Events.DESCRIPTION + "='" + describe + "'";
		}

		Cursor cursor = null;

		int localEventId = -1;
		try {
			cursor = resolver.query(CustonCalendarContract.Events.CONTENT_URI, null, where, null,
					null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				localEventId = cursor.getInt(cursor
						.getColumnIndex(CustonCalendarContract.Events._ID));
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return localEventId;
	}

	private int getDefaultLocalCalendarId(ContentResolver resolver) {
		final int localSdk = Build.VERSION.SDK_INT;
		String localAccountTypeKey = CustonCalendarContract.Calendars.getAccountTypeKey(localSdk);
		Cursor cursor = null;
		String where = localAccountTypeKey + "='" + CustonCalendarContract.ACCOUNT_TYPE_LOCAL + "'";
		cursor = resolver.query(CustonCalendarContract.Calendars.CONTENT_URI, null, where, null,
				null);
		if (cursor == null) {
			return -1;
		}
		if (cursor.getCount() == 0) {
			cursor.close();
			cursor = null;
			return -1;
		}

		int calendarId = -1;
		try {
			cursor.moveToFirst();
			calendarId = cursor.getInt(cursor.getColumnIndex(CustonCalendarContract.Calendars._ID));
		} catch (Exception e) {
		}

		cursor.close();
		cursor = null;
		return calendarId;
	}

	private int getLocalCalendarId(ContentResolver resolver, Entity calendarEntity) {
		if (resolver == null || calendarEntity == null) {
			return -1;
		}

		final int localSdk = Build.VERSION.SDK_INT;
		String localAccountTypeKey = CustonCalendarContract.Calendars.getAccountTypeKey(localSdk);
		String localAccountNameKey = CustonCalendarContract.Calendars.getAccountNameKey(localSdk);
		String accountType = getEntityValue(calendarEntity, localAccountTypeKey);
		String accountName = getEntityValue(calendarEntity, localAccountNameKey);

		String where = localAccountTypeKey + "='" + accountType + "' AND " + localAccountNameKey
				+ "='" + accountName + "'";
		Cursor cursor = null;
		int calendarId = -1;
		try {
			cursor = resolver.query(CustonCalendarContract.Calendars.CONTENT_URI, null, where,
					null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				calendarId = cursor.getInt(cursor
						.getColumnIndex(CustonCalendarContract.Calendars._ID));
			}
		} catch (Exception e) {
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
		return calendarId;
	}

	private String getEntityValue(Entity entity, String key) {
		if (entity == null || key == null) {
			return null;
		}

		ContentValues cv = entity.getEntityValues();
		if (cv == null || !cv.containsKey(key)) {
			return null;
		}
		return cv.getAsString(key);
	}

	private int getCalendarId(Entity calendarEntity) {
		String strCalendarId = getEntityValue(calendarEntity, CustonCalendarContract.Calendars._ID);
		if (strCalendarId == null) {
			return -1;
		}
		return Integer.valueOf(strCalendarId).intValue();
	}

	private CalendarStruct adapteCalendarInternal(CalendarStruct calendarStruct, int targetSdk) {
		if (calendarStruct == null) {
			return null;
		}

		final int srcSdk = calendarStruct.getCalendarSdkVersion();
		if (srcSdk == targetSdk) {
			return calendarStruct;
		}
		if ((srcSdk < Build.VERSION_CODES.ICE_CREAM_SANDWICH && targetSdk < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				|| (srcSdk >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && targetSdk >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
			calendarStruct.setCalendarSdkVersion(targetSdk);
			return calendarStruct;
		}

		// 适配CalendarEntity
		adapteCalendarEntity(calendarStruct.getCalendarEntity(), srcSdk, targetSdk);

		// 适配EventEntity
		List<Entity> events = calendarStruct.getEventEntities();
		if (events == null) {
			return calendarStruct;
		}

		for (Entity event : events) {
			adapteEventEntity(event, srcSdk, targetSdk);
		}
		calendarStruct.setCalendarSdkVersion(targetSdk);
		return calendarStruct;
	}

	private Entity adapteCalendarEntity(Entity calendarEntity, int srcSdk, int targetSdk) {
		if (calendarEntity == null) {
			return null;
		}

		String key = null;
		String value = null;
		String replaceableKey = null;
		ContentValues updateValues = calendarEntity.getEntityValues();
		ContentValues originalValues = new ContentValues(updateValues);

		// 适配数据库表字段名字修改的字段
		Iterator<Entry<String, Object>> keys = originalValues.valueSet().iterator();
		while (keys.hasNext()) {
			Entry<String, Object> valueEntry = keys.next();
			key = valueEntry.getKey();
			value = valueEntry.getValue().toString();
			if (key == null) {
				continue;
			}

			// 是否可替换key
			replaceableKey = getCalendarReplaceableFieldKey(key, srcSdk, targetSdk);
			if (replaceableKey != null) {
				replaceKeys(updateValues, key, replaceableKey, value);
				continue;
			}

			// 是否可转化
			if (isConvertible(updateValues, key, value, srcSdk, targetSdk)) {
				continue;
			}
		}

		validateCalendarEntityContainApdateField(updateValues, targetSdk);

		originalValues.clear();
		originalValues = new ContentValues(updateValues);

		//过滤数据库表字段
		keys = originalValues.valueSet().iterator();
		while (keys.hasNext()) {
			Entry<String, Object> valueEntry = keys.next();
			key = valueEntry.getKey();
			if (!verifyCalendarFieldValid(key, targetSdk)) {
				updateValues.remove(key);
			}
		}
		originalValues.clear();

		return calendarEntity;
	}

	private Entity adapteEventEntity(Entity event, int srcSdk, int targetSdk) {
		if (event == null) {
			return null;
		}

		String key = null;
		String value = null;
		String replaceableKey = null;

		// 处理Event
		ContentValues updateValues = event.getEntityValues();
		ContentValues originalValues = new ContentValues(updateValues);

		// 适配数据库表字段名字修改的字段
		Iterator<Entry<String, Object>> keys = originalValues.valueSet().iterator();
		while (keys.hasNext()) {
			Entry<String, Object> valueEntry = keys.next();
			key = valueEntry.getKey();
			value = valueEntry.getValue().toString();
			if (key == null) {
				continue;
			}

			// 是否可替换key
			replaceableKey = getEventReplaceableFieldKey(key, srcSdk, targetSdk);
			if (replaceableKey != null) {
				replaceKeys(updateValues, key, replaceableKey, value);
				continue;
			}

			// 是否可转化
			if (isConvertible(updateValues, key, value, srcSdk, targetSdk)) {
				continue;
			}
		}

		validateEventEntityContainApdateField(updateValues, targetSdk);

		originalValues.clear();
		originalValues = new ContentValues(updateValues);

		//过滤数据库表字段
		keys = originalValues.valueSet().iterator();
		while (keys.hasNext()) {
			Entry<String, Object> valueEntry = keys.next();
			key = valueEntry.getKey();
			if (!verifyEventFieldValid(key, targetSdk)) {
				updateValues.remove(key);
			}
		}
		originalValues.clear();

		// TODO 目前Event的子字段例如Remider等各个版本是一样的，不需要适配

		return event;
	}

	private void validateEventEntityContainApdateField(ContentValues values, int targetSdk) {
	}

	private void validateCalendarEntityContainApdateField(ContentValues values, int targetSdk) {
		if (values == null) {
			return;
		}

		if (targetSdk < Build.VERSION_CODES.HONEYCOMB) {

		} else if (targetSdk < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			values.remove(CustonCalendarContract.Calendars.HIDDEN_FROYO);
		} else {
			values.put(CustonCalendarContract.CalendarEntity.ALLOWED_REMINDERS_ICE_CREAM, true);
		}
	}

	private boolean verifyCalendarFieldValid(String key, int targetSdk) {
		if (key == null) {
			return false;
		}

		if (targetSdk < Build.VERSION_CODES.HONEYCOMB) {
			final int length = CALENDAR_PROJECTION_FROYO.length;
			for (int i = 0; i < length; i++) {
				if (key.equals(CALENDAR_PROJECTION_FROYO[i])) {
					return true;
				}
			}
		} else if (targetSdk < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final int length = CALENDAR_PROJECTION_HONEYCOMB.length;
			for (int i = 0; i < length; i++) {
				if (key.equals(CALENDAR_PROJECTION_HONEYCOMB[i])) {
					return true;
				}
			}
		} else {
			final int length = CALENDAR_PROJECTION_ICE_CREAM.length;
			for (int i = 0; i < length; i++) {
				if (key.equals(CALENDAR_PROJECTION_ICE_CREAM[i])) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean verifyEventFieldValid(String key, int targetSdk) {
		if (key == null) {
			return false;
		}

		if (targetSdk < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			final int length = EVENT_PROJECTION_FROYO.length;
			for (int i = 0; i < length; i++) {
				if (key.equals(EVENT_PROJECTION_FROYO[i])) {
					return true;
				}
			}
		} else {
			final int length = EVENT_PROJECTION_ICE_CREAM.length;
			for (int i = 0; i < length; i++) {
				if (key.equals(EVENT_PROJECTION_ICE_CREAM[i])) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isConvertible(ContentValues values, String oldKey, String oldValue, int oldSdk,
			int newSdk) {
		if (values == null || oldKey == null || oldValue == null) {
			return false;
		}

		boolean convaertible = false;
		if (oldSdk < newSdk) {
			if (oldKey.equals(CustonCalendarContract.CalendarEntity.HIDDEN_FROYO)) {
				int reversValue = Integer.valueOf(oldValue) == 0 ? 1 : 0;
				replaceKeys(values, CustonCalendarContract.CalendarEntity.HIDDEN_FROYO,
						CustonCalendarContract.CalendarEntity.VISIBLE_ICE_CREAM,
						String.valueOf(reversValue));
				convaertible = true;
			}
		} else {
			if (oldKey.equals(CustonCalendarContract.CalendarEntity.VISIBLE_ICE_CREAM)) {
				int reversValue = Integer.valueOf(oldValue) == 0 ? 1 : 0;
				replaceKeys(values, CustonCalendarContract.CalendarEntity.VISIBLE_ICE_CREAM,
						CustonCalendarContract.CalendarEntity.HIDDEN_FROYO,
						String.valueOf(reversValue));
				convaertible = true;
			}
		}
		return convaertible;
	}

	private String getCalendarReplaceableFieldKey(String oldKey, int oldSdk, int newSdk) {
		if (oldKey == null) {
			return null;
		}

		String newKey = null;
		if (oldSdk < newSdk) {
			if (oldKey.equals(CustonCalendarContract.CalendarEntity.ACCOUNT_NAME_FROYO)) {
				newKey = CustonCalendarContract.CalendarEntity.ACCOUNT_NAME_ICE_CREAM;
			} else if (oldKey.equals(CustonCalendarContract.CalendarEntity.ACCOUNT_TYPE_FROYO)) {
				newKey = CustonCalendarContract.CalendarEntity.ACCOUNT_TYPE_ICE_CREAM;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.CALENDAR_ACCESS_LEVEL_FROYO)) {
				newKey = CustonCalendarContract.CalendarEntity.CALENDAR_ACCESS_LEVEL_ICE_CREAM;
			} else if (oldKey.equals(CustonCalendarContract.CalendarEntity.CALENDAR_COLOR_FROYO)) {
				newKey = CustonCalendarContract.CalendarEntity.CALENDAR_COLOR_ICE_CREAM;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.CALENDAR_DISPLAY_NAME_FROYO)) {
				newKey = CustonCalendarContract.CalendarEntity.CALENDAR_DISPLAY_NAME_ICE_CREAM;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.CALENDAR_TIME_ZONE_FROYO)) {
				newKey = CustonCalendarContract.CalendarEntity.CALENDAR_TIME_ZONE_ICE_CREAM;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.ORGANIZER_CAN_RESPOND_FROYO)) {
				newKey = CustonCalendarContract.CalendarEntity.CAN_ORGANIZER_RESPOND_ICE_CREAM;
			} else if (oldKey.equals(CustonCalendarContract.CalendarEntity.URL_FROYO)) {
				newKey = CustonCalendarContract.CalendarEntity.CAL_SYNC1;
			}
		} else {
			if (oldKey.equals(CustonCalendarContract.CalendarEntity.ACCOUNT_NAME_ICE_CREAM)) {
				newKey = CustonCalendarContract.CalendarEntity.ACCOUNT_NAME_FROYO;
			} else if (oldKey.equals(CustonCalendarContract.CalendarEntity.ACCOUNT_TYPE_ICE_CREAM)) {
				newKey = CustonCalendarContract.CalendarEntity.ACCOUNT_TYPE_FROYO;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.CALENDAR_ACCESS_LEVEL_ICE_CREAM)) {
				newKey = CustonCalendarContract.CalendarEntity.CALENDAR_ACCESS_LEVEL_FROYO;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.CALENDAR_COLOR_ICE_CREAM)) {
				newKey = CustonCalendarContract.CalendarEntity.CALENDAR_COLOR_FROYO;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.CALENDAR_DISPLAY_NAME_ICE_CREAM)) {
				newKey = CustonCalendarContract.CalendarEntity.CALENDAR_DISPLAY_NAME_FROYO;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.CALENDAR_TIME_ZONE_ICE_CREAM)) {
				newKey = CustonCalendarContract.CalendarEntity.CALENDAR_TIME_ZONE_FROYO;
			} else if (oldKey
					.equals(CustonCalendarContract.CalendarEntity.CAN_ORGANIZER_RESPOND_ICE_CREAM)) {
				newKey = CustonCalendarContract.CalendarEntity.ORGANIZER_CAN_RESPOND_FROYO;
			} else if (oldKey.equals(CustonCalendarContract.CalendarEntity.CAL_SYNC1)) {
				newKey = CustonCalendarContract.CalendarEntity.URL_FROYO;
			}
		}
		return newKey;
	}

	private String getEventReplaceableFieldKey(String oldKey, int oldSdk, int newSdk) {
		if (oldKey == null) {
			return null;
		}

		String newKey = null;
		if (oldSdk < newSdk) {
			// Event
			if (oldKey.equals(CustonCalendarContract.EventsEntity.ACCOUNT_NAME_FROYO)) {
				newKey = CustonCalendarContract.EventsEntity.ACCOUNT_NAME_ICE_CREAM;
			} else if (oldKey.equals(CustonCalendarContract.EventsEntity.ACCOUNT_TYPE_FROYO)) {
				newKey = CustonCalendarContract.EventsEntity.ACCOUNT_TYPE_ICE_CREAM;
			}
		} else {
			if (oldKey.equals(CustonCalendarContract.EventsEntity.ACCOUNT_NAME_ICE_CREAM)) {
				newKey = CustonCalendarContract.EventsEntity.ACCOUNT_NAME_FROYO;
			} else if (oldKey.equals(CustonCalendarContract.EventsEntity.ACCOUNT_TYPE_ICE_CREAM)) {
				newKey = CustonCalendarContract.EventsEntity.ACCOUNT_TYPE_FROYO;
			}
		}
		return newKey;
	}

	private ContentValues replaceKeys(ContentValues values, String oldKey, String newKey,
			String value) {
		if (values == null || oldKey == null || newKey == null || value == null) {
			return null;
		}
		values.remove(oldKey);
		values.put(newKey, value);
		return values;
	}

	private List<CalendarStruct> getLocalCalendar() {
		return mCalendars;
	}

	private void loadLocalCalendar(Context context) {
		if (context == null) {
			return;
		}

		final ContentResolver cr = context.getContentResolver();
		String[] projection = LOCAL_SDK < Build.VERSION_CODES.HONEYCOMB
				? CALENDAR_PROJECTION_FROYO
				: LOCAL_SDK < Build.VERSION_CODES.ICE_CREAM_SANDWICH
						? CALENDAR_PROJECTION_HONEYCOMB
						: CALENDAR_PROJECTION_ICE_CREAM;
		Cursor cursor = null;
		try {
			cursor = cr.query(CustonCalendarContract.Calendars.CONTENT_URI, projection,
					CustonCalendarContract.Calendars.CALENDAR_NOT_DELETE_WHERE, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				Entity calendar = null;
				CalendarStruct calendarStruct = null;
				EntityIterator iterator = CalendarEntity.newEntityIterator(cursor, LOCAL_SDK);
				while (iterator.hasNext()) {
					calendar = iterator.next();
					calendarStruct = getCalendarInternal(cr, calendar);
					if (calendarStruct != null) {
						if (mCalendars == null) {
							mCalendars = new ArrayList<CalendarOperator.CalendarStruct>();
						}
						mCalendars.add(calendarStruct);
					}
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	private CalendarStruct getCalendarInternal(ContentResolver resolver, Entity calendar) {
		if (resolver == null || calendar == null) {
			return null;
		}

		ContentValues cv = calendar.getEntityValues();
		if (cv == null) {
			return null;
		}
		if (!cv.containsKey(CustonCalendarContract.Calendars._ID)) {
			return null;
		}

		CalendarStruct calendarStruct = new CalendarStruct();
		final Integer calendarId = cv.getAsInteger(CustonCalendarContract.Calendars._ID);
		if (calendarId == null) {
			return null;
		}
		calendarStruct.setCalendarEntity(calendar);

		//添加event
		String where = CustonCalendarContract.EventsEntity.CALENDAR_ID + "="
				+ calendarId.intValue() + " AND " + CustonCalendarContract.Events.DELETED + "="
				+ "0" + " AND " + CustonCalendarContract.Events.EVENT_TIMEZONE + " IS NOT NULL";
		String[] projection = LOCAL_SDK < Build.VERSION_CODES.ICE_CREAM_SANDWICH
				? EVENT_PROJECTION_FROYO
				: EVENT_PROJECTION_ICE_CREAM;
		Cursor cursor = null;

		try {
			cursor = resolver.query(CustonCalendarContract.Events.CONTENT_URI, projection, where,
					null, null);
			if (cursor != null) {
				EntityIterator iterator = CustonCalendarContract.EventsEntity.newEntityIterator(
						cursor, resolver, LOCAL_SDK);
				while (iterator.hasNext()) {
					Entity event = iterator.next();
					if (event != null) {
						calendarStruct.addEventEntity(event);
					}
				}
			}
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}

		if (calendarStruct.getEventCount() == 0) {
			// 根据产品需求，没有事件的日历不需要备份
			return null;
		}
		calendarStruct.setCalendarSdkVersion(LOCAL_SDK);
		return calendarStruct;
	}

	/**
	 * 日历数据结构 描述一个日历结构
	 *
	 * @author wencan
	 *
	 */
	public static class CalendarStruct {
		private int mCalendarSdkVersion;
		private Entity mCalendarEntity;
		private List<Entity> mEventEntities;

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("mCalendarSdkVersion = ");
			sb.append(mCalendarSdkVersion);

			if (mCalendarEntity != null) {
				sb.append("\n");
				sb.append(mCalendarEntity.getEntityValues().toString());
			}
			if (mEventEntities != null) {
				for (Entity entity : mEventEntities) {
					sb.append("\n");
					sb.append(entity.getEntityValues().toString());
					if (entity.getSubValues() != null) {
						List<NamedContentValues> subValues = entity.getSubValues();
						for (NamedContentValues value : subValues) {
							sb.append("\n");
							sb.append(value.uri.toString());
							sb.append(":");
							sb.append(value.values.toString());
						}
					}
				}
			}
			return sb.toString();
		}

		public boolean isSameCalendarSdkVersion(CalendarStruct other) {
			return other.mCalendarSdkVersion == this.mCalendarSdkVersion;
		}

		public boolean isSameCalendarAccount(CalendarStruct other) {
			if (other == null) {
				return false;
			}

			Entity otherCalendarEntity = other.getCalendarEntity();
			if (otherCalendarEntity == null) {
				return false;
			}

			Account otherAccount = other.getCalendarAccount();
			Account account = getCalendarAccount();
			if (otherAccount == null && account == null) {
				return true;
			}
			if (otherAccount != null && account != null) {
				return otherAccount.equals(account);
			}
			return false;
		}

		private String getEntityValue(Entity entity, String key) {
			if (entity == null || key == null) {
				return null;
			}

			ContentValues cv = entity.getEntityValues();
			if (cv == null || !cv.containsKey(key)) {
				return null;
			}
			return cv.getAsString(key);
		}

		public boolean containEvent(Entity otherEventEntity) {
			String otherTitle = getEntityValue(otherEventEntity,
					CustonCalendarContract.Events.TITLE);
			String otherDtStart = getEntityValue(otherEventEntity,
					CustonCalendarContract.Events.DTSTART);
			String otherDtEnd = getEntityValue(otherEventEntity,
					CustonCalendarContract.Events.DTEND);
			String otherDescribe = getEntityValue(otherEventEntity,
					CustonCalendarContract.Events.DESCRIPTION);

			for (Entity event : mEventEntities) {
				String title = getEntityValue(event, CustonCalendarContract.Events.TITLE);
				String dtStart = getEntityValue(event, CustonCalendarContract.Events.DTSTART);
				String dtEnd = getEntityValue(event, CustonCalendarContract.Events.DTEND);
				String describe = getEntityValue(event, CustonCalendarContract.Events.DESCRIPTION);
				if (TextUtils.equals(dtStart, otherDtStart) && TextUtils.equals(dtEnd, otherDtEnd)
						&& TextUtils.equals(title, otherTitle)
						&& TextUtils.equals(describe, otherDescribe)) {
					return true;
				}
			}
			return false;
		}

		private Account getCalendarAccount() {
			ContentValues cv = mCalendarEntity.getEntityValues();
			String accountType = null;
			String accountName = null;
			accountType = cv.getAsString(CustonCalendarContract.Calendars
					.getAccountTypeKey(mCalendarSdkVersion));
			accountName = cv.getAsString(CustonCalendarContract.Calendars
					.getAccountNameKey(mCalendarSdkVersion));
			if (accountName == null || accountType == null) {
				return null;
			}
			return new Account(accountName, accountType);
		}

		public Entity getCalendarEntity() {
			return mCalendarEntity;
		}

		public void setCalendarSdkVersion(int sdkVersion) {
			mCalendarSdkVersion = sdkVersion;
		}

		public int getCalendarSdkVersion() {
			return mCalendarSdkVersion;
		}

		public boolean isCalendarStructValid() {
			return mCalendarEntity != null;
		}

		public List<Entity> getEventEntities() {
			return mEventEntities;
		}

		public void setCalendarEntity(Entity calendarEntity) {
			mCalendarEntity = calendarEntity;
		}

		public void addEventEntities(List<Entity> eventEntites) {
			if (eventEntites == null) {
				return;
			}
			if (mEventEntities == null) {
				mEventEntities = new ArrayList<Entity>();
			}
			mEventEntities.addAll(eventEntites);
		}

		public void addEventEntity(Entity event) {
			if (mEventEntities == null) {
				mEventEntities = new ArrayList<Entity>();
			}

			mEventEntities.add(event);
		}

		public int getEventCount() {
			if (mEventEntities == null) {
				return 0;
			}
			return mEventEntities.size();
		}
	}

	public static int getEventId(Entity event) {
		if (event == null) {
			return 0;
		}
		ContentValues cv = event.getEntityValues();
		if (cv == null || !cv.containsKey(CustonCalendarContract.EventsEntity._ID)) {
			return 0;
		}
		return cv.getAsInteger(CustonCalendarContract.EventsEntity._ID);
	}

	/**
	 * SDK8的日历的投影
	 */
	public static final String[] CALENDAR_PROJECTION_FROYO = {
			CustonCalendarContract.Calendars._ID,
			CustonCalendarContract.Calendars.ACCOUNT_NAME_FROYO,
			CustonCalendarContract.Calendars.ACCOUNT_TYPE_FROYO,
			CustonCalendarContract.Calendars.CALENDAR_LOCATION_FROYO,
			CustonCalendarContract.Calendars.NAME, CustonCalendarContract.Calendars._SYNC_ID,
			CustonCalendarContract.Calendars.CALENDAR_ACCESS_LEVEL_FROYO,
			CustonCalendarContract.Calendars.CALENDAR_COLOR_FROYO,
			CustonCalendarContract.Calendars.CALENDAR_DISPLAY_NAME_FROYO,
			CustonCalendarContract.Calendars.CALENDAR_TIME_ZONE_FROYO,
			CustonCalendarContract.Calendars.SYNC_EVENTS_FROYO,
			CustonCalendarContract.Calendars.OWNER_ACCOUNT_FROYO,

			CustonCalendarContract.Calendars.ORGANIZER_CAN_RESPOND_FROYO,

			CustonCalendarContract.Calendars.HIDDEN_FROYO,

			CustonCalendarContract.Calendars.SELECTED_FROYO,
			CustonCalendarContract.Calendars.SYNC_DIRTY_FROYO,
			CustonCalendarContract.Calendars.SYNC_LOCAL_ID_FROYO,
			CustonCalendarContract.Calendars.SYNC_MARK_FROYO,
			CustonCalendarContract.Calendars.SYNC_TIME_FROYO,
			CustonCalendarContract.Calendars.SYNC_VERSION_FROYO,
			CustonCalendarContract.Calendars.URL_FROYO };

	/**
	 * SDK11的日历的投影
	 */
	public static final String[] CALENDAR_PROJECTION_HONEYCOMB = {
			CustonCalendarContract.Calendars._ID,
			CustonCalendarContract.Calendars.ACCOUNT_NAME_FROYO,
			CustonCalendarContract.Calendars.ACCOUNT_TYPE_FROYO,
			CustonCalendarContract.Calendars.CALENDAR_LOCATION_FROYO,
			CustonCalendarContract.Calendars.NAME, CustonCalendarContract.Calendars._SYNC_ID,
			CustonCalendarContract.Calendars.CALENDAR_ACCESS_LEVEL_FROYO,
			CustonCalendarContract.Calendars.CALENDAR_COLOR_FROYO,
			CustonCalendarContract.Calendars.CALENDAR_DISPLAY_NAME_FROYO,
			CustonCalendarContract.Calendars.CALENDAR_TIME_ZONE_FROYO,
			CustonCalendarContract.Calendars.SYNC_EVENTS_FROYO,
			CustonCalendarContract.Calendars.OWNER_ACCOUNT_FROYO,

			CustonCalendarContract.Calendars.ORGANIZER_CAN_RESPOND_FROYO,

			CustonCalendarContract.Calendars.DELETED,

			CustonCalendarContract.Calendars.SELECTED_FROYO,
			CustonCalendarContract.Calendars.SYNC_DIRTY_FROYO,
			CustonCalendarContract.Calendars.SYNC_LOCAL_ID_FROYO,
			CustonCalendarContract.Calendars.SYNC_MARK_FROYO,
			CustonCalendarContract.Calendars.SYNC_TIME_FROYO,
			CustonCalendarContract.Calendars.SYNC_VERSION_FROYO,
			CustonCalendarContract.Calendars.SYNC1_HONEYCOMB };

	/**
	 * SDK14的日历的投影
	 */
	public static final String[] CALENDAR_PROJECTION_ICE_CREAM = {
			CustonCalendarContract.Calendars._ID,
			CustonCalendarContract.Calendars.ACCOUNT_NAME_ICE_CREAM,
			CustonCalendarContract.Calendars.ACCOUNT_TYPE_ICE_CREAM,
			CustonCalendarContract.Calendars.CALENDAR_LOCATION_ICE_CREAM,
			CustonCalendarContract.Calendars.NAME, CustonCalendarContract.Calendars._SYNC_ID,
			CustonCalendarContract.Calendars.CALENDAR_ACCESS_LEVEL_ICE_CREAM,
			CustonCalendarContract.Calendars.CALENDAR_COLOR_ICE_CREAM,
			CustonCalendarContract.Calendars.CALENDAR_DISPLAY_NAME_ICE_CREAM,
			CustonCalendarContract.Calendars.CALENDAR_TIME_ZONE_ICE_CREAM,
			CustonCalendarContract.Calendars.OWNER_ACCOUNT_ICE_CREAM,
			CustonCalendarContract.Calendars.SYNC_EVENTS_ICE_CREAM,

			CustonCalendarContract.Calendars.CAN_ORGANIZER_RESPOND_ICE_CREAM,
			CustonCalendarContract.Calendars.VISIBLE_ICE_CREAM,
			CustonCalendarContract.Calendars.CAL_SYNC1,

			CustonCalendarContract.Calendars.ALLOWED_REMINDERS_ICE_CREAM,
			CustonCalendarContract.Calendars.CAN_MODIFY_TIME_ZONE_ICE_CREAM,
			CustonCalendarContract.Calendars.CAN_PARTIALLY_UPDATE_ICE_CREAM,
			CustonCalendarContract.Calendars.DELETED,
			CustonCalendarContract.Calendars.MAX_REMINDERS_ICE_CREAM,

			CustonCalendarContract.Calendars.DIRTY_ICE_CREAM };

	/**
	 * SDK8的事件的投影
	 */
	public static final String[] EVENT_PROJECTION_FROYO = {
			CustonCalendarContract.EventsEntity._ID,
			CustonCalendarContract.EventsEntity.CALENDAR_ID,
			CustonCalendarContract.EventsEntity.ORGANIZER,
			CustonCalendarContract.EventsEntity.TITLE,
			CustonCalendarContract.EventsEntity.EVENT_LOCATION,
			CustonCalendarContract.EventsEntity.DESCRIPTION,
			CustonCalendarContract.EventsEntity.DTEND, CustonCalendarContract.EventsEntity.DTSTART,
			CustonCalendarContract.EventsEntity.DURATION,
			CustonCalendarContract.EventsEntity.EVENT_TIMEZONE,
			CustonCalendarContract.EventsEntity.STATUS,
			CustonCalendarContract.EventsEntity.ALL_DAY, CustonCalendarContract.EventsEntity.RDATE,
			CustonCalendarContract.EventsEntity.RRULE, CustonCalendarContract.EventsEntity.EXDATE,
			CustonCalendarContract.EventsEntity.EXRULE,
			CustonCalendarContract.EventsEntity.ORIGINAL_INSTANCE_TIME,
			CustonCalendarContract.EventsEntity.ORIGINAL_ALL_DAY,
			CustonCalendarContract.EventsEntity.GUESTS_CAN_MODIFY,
			CustonCalendarContract.EventsEntity.GUESTS_CAN_INVITE_OTHERS,
			CustonCalendarContract.EventsEntity.GUESTS_CAN_SEE_GUESTS,
			CustonCalendarContract.EventsEntity.SELF_ATTENDEE_STATUS,
			CustonCalendarContract.EventsEntity.DELETED,
			CustonCalendarContract.EventsEntity.HAS_ATTENDEE_DATA,
			CustonCalendarContract.EventsEntity.HAS_EXTENDED_PROPERTIES,
			CustonCalendarContract.EventsEntity.LAST_DATE,
			CustonCalendarContract.EventsEntity.HAS_ALARM,
			CustonCalendarContract.EventsEntity._SYNC_ID,

			CustonCalendarContract.EventsEntity.ACCOUNT_NAME_FROYO,
			CustonCalendarContract.EventsEntity.ACCOUNT_TYPE_FROYO,

			CustonCalendarContract.EventsEntity.TRANSPARENCY_FROYO,
			CustonCalendarContract.EventsEntity.SYNC_LOCAL_ID_FROYO,
			CustonCalendarContract.EventsEntity.HTML_URI_FROYO,
			CustonCalendarContract.EventsEntity.ORIGINAL_EVENT_FROYO,
			CustonCalendarContract.EventsEntity.VISIBILITY_FROYO,
			CustonCalendarContract.EventsEntity.COMMENTS_URI_FROYO };

	/**
	 * SDK14的事件的投影
	 */
	public static final String[] EVENT_PROJECTION_ICE_CREAM = {
			CustonCalendarContract.EventsEntity._ID,
			CustonCalendarContract.EventsEntity.CALENDAR_ID,
			CustonCalendarContract.EventsEntity.ORGANIZER,
			CustonCalendarContract.EventsEntity.TITLE,
			CustonCalendarContract.EventsEntity.EVENT_LOCATION,
			CustonCalendarContract.EventsEntity.DESCRIPTION,
			CustonCalendarContract.EventsEntity.DTEND, CustonCalendarContract.EventsEntity.DTSTART,
			CustonCalendarContract.EventsEntity.DURATION,
			CustonCalendarContract.EventsEntity.EVENT_TIMEZONE,
			CustonCalendarContract.EventsEntity.STATUS,
			CustonCalendarContract.EventsEntity.ALL_DAY,
			CustonCalendarContract.EventsEntity.RDATE,
			CustonCalendarContract.EventsEntity.RRULE,
			CustonCalendarContract.EventsEntity.EXDATE,
			CustonCalendarContract.EventsEntity.EXRULE,
			CustonCalendarContract.EventsEntity.ORIGINAL_INSTANCE_TIME,
			CustonCalendarContract.EventsEntity.ORIGINAL_ALL_DAY,
			CustonCalendarContract.EventsEntity.GUESTS_CAN_MODIFY,
			CustonCalendarContract.EventsEntity.GUESTS_CAN_INVITE_OTHERS,
			CustonCalendarContract.EventsEntity.GUESTS_CAN_SEE_GUESTS,
			CustonCalendarContract.EventsEntity.SELF_ATTENDEE_STATUS,
			CustonCalendarContract.EventsEntity.DELETED,
			CustonCalendarContract.EventsEntity.HAS_ATTENDEE_DATA,
			CustonCalendarContract.EventsEntity.HAS_EXTENDED_PROPERTIES,
			CustonCalendarContract.EventsEntity.LAST_DATE,
			CustonCalendarContract.EventsEntity.HAS_ALARM,
			CustonCalendarContract.EventsEntity._SYNC_ID,

			CustonCalendarContract.EventsEntity.ACCESS_LEVEL,
			CustonCalendarContract.EventsEntity.AVAILABILITY_ICE_CREAM,
			//		AdapterCalendarContract.EventsEntity.DIRTY,
			CustonCalendarContract.EventsEntity.EVENT_COLOR,
			CustonCalendarContract.EventsEntity.EVENT_END_TIMEZONE_ICE_CREAM,
			CustonCalendarContract.EventsEntity.LAST_SYNCED_ICE_CREAM,
			CustonCalendarContract.EventsEntity.ORIGINAL_ID_ICE_CREAM,
			CustonCalendarContract.EventsEntity.ORIGINAL_SYNC_ID_ICE_CREAM };
}
