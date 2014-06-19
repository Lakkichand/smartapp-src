package com.jiubang.go.backup.pro.schedules;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.provider.ContactsContract.RawContacts;

import com.jiubang.go.backup.pro.PreferenceManager;

/**
 * 联系人变更备份推送
 *
 * @author wencan
 *
 */
public class ContactCheckerSchedule {
	public static final String ACTION_CONTACT_CHECK = "com.jiubang.go.backup.ex.ACTION_CONTACT_CHECK";
	public static final int CONTACT_CHECK_NOTIFICATION_ID = 0xFF0F3001;
	private static final String SP_CONTACT_VERSION_MAP_FILE_NAME = "contact_version_file";
	private static final int DEFAULT_CHECK_HOUR = 20;
	private static final int DEFAULT_CHECK_PERIOD = 1 * 24 * 60 * 60 * 1000;
	private static ContactCheckerSchedule sInstance = null;

	private Context mContext;

	private ContactCheckerSchedule(Context context) {
		if (!(context instanceof Application)) {
			throw new IllegalArgumentException("context must be application context");
		}
		mContext = context;
	}

	/**
	 * 获取ContactPushSchedule实例
	 *
	 * @param context
	 *            必须是application类型的context
	 * @return
	 */
	public static ContactCheckerSchedule getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ContactCheckerSchedule(context);
		}
		return sInstance;
	}

	public void scheduleNextCheck() {
		AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_CONTACT_CHECK);
		PendingIntent sender = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP, getNextCheckTime(), sender);
	}

	private long getNextCheckTime() {
		PreferenceManager pm = PreferenceManager.getInstance();
		long preTime = pm.getLong(mContext, PreferenceManager.KEY_NEXT_CONTACT_CHAGE_CHECK_TIME, 0);
		Calendar calendar = Calendar.getInstance();
		long now = System.currentTimeMillis();
		calendar.setTimeInMillis(now);

		if (preTime < calendar.getTimeInMillis() || preTime - now > DEFAULT_CHECK_PERIOD) {
			// 如果下次检查时间已过，或者下次检查时间大于检查周期，则重新设置
			if (calendar.get(Calendar.HOUR_OF_DAY) >= DEFAULT_CHECK_HOUR) {
				// 如果当前时间已经超过默认检查时间，则设置为第二天的检查时间
				calendar.add(Calendar.DAY_OF_YEAR, 1);
			}
			calendar.set(Calendar.HOUR_OF_DAY, DEFAULT_CHECK_HOUR);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			preTime = calendar.getTimeInMillis();
			pm.putLong(mContext, PreferenceManager.KEY_NEXT_CONTACT_CHAGE_CHECK_TIME, preTime);
		}
		return preTime;
	}

	public boolean isContactChange() {
		//读取Preference中记录的RawContact对应的version数据
		Map<Integer, Integer> oldVersionMap = getRawContactVersionMapFromPreference();
		if (oldVersionMap == null || oldVersionMap.size() == 0) {
			//如果没有记录，则当作全新的，默认不通知
			return false;
		}

		Map<Integer, Integer> localVersionMap = getLocalRawContactVersionMap();
		if (localVersionMap == null) {
			// 如果本地联系人为空，原来不是空，则是改变
			return true;
		}

		boolean change = false;
		Integer key = null;
		Iterator<Integer> iterator = localVersionMap.keySet().iterator();
		while (iterator.hasNext()) {
			key = iterator.next();
			if (!oldVersionMap.containsKey(key)) {
				//新增了联系人
				change = true;
				break;
			}
			if (!localVersionMap.get(key).equals(oldVersionMap.get(key))) {
				//version不一致，改变
				change = true;
				break;
			}
		}

		// 修复某些机型在删除了联系人后，会删除数据库相对应的数据而引起检测不到联系人改变的bug
		if (!change) {
			Iterator<Integer> oldIterator = oldVersionMap.keySet().iterator();
			while (oldIterator.hasNext()) {
				key = oldIterator.next();
				if (!localVersionMap.containsKey(key)) {
					change = true;
					break;
				}
			}
		}

		oldVersionMap.clear();
		oldVersionMap = null;
		localVersionMap.clear();
		localVersionMap = null;
		return change;
	}

	private Map<Integer, Integer> getLocalRawContactVersionMap() {
		ContentResolver cr = mContext.getContentResolver();
		String[] projection = new String[] { RawContacts._ID, RawContacts.VERSION };
		Cursor cursor = cr.query(RawContacts.CONTENT_URI, projection, null, null, null);
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		Map<Integer, Integer> localContactMap = new HashMap<Integer, Integer>();
		try {
			do {
				try {
					int id = cursor.getInt(cursor.getColumnIndex(RawContacts._ID));
					int version = cursor.getInt(cursor.getColumnIndex(RawContacts.VERSION));
					localContactMap.put(id, version);
				} catch (Exception e) {
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		return localContactMap;
	}

	private Map<Integer, Integer> getRawContactVersionMapFromPreference() {
		SharedPreferences sp = mContext.getSharedPreferences(SP_CONTACT_VERSION_MAP_FILE_NAME,
				Context.MODE_PRIVATE);
		Map<String, ?> oldMap = null;
		try {
			oldMap = sp.getAll();
		} catch (NullPointerException e) {
		}
		if (oldMap == null || oldMap.size() == 0) {
			return null;
		}

		Set<String> keys = oldMap.keySet();
		if (keys == null || keys.size() == 0) {
			return null;
		}

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		String key = null;
		Integer value = null;
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			key = iterator.next();
			if (key == null) {
				continue;
			}

			try {
				value = (Integer) oldMap.get(key);
			} catch (Exception e) {
			}

			if (key != null && value != null) {
				result.put(Integer.valueOf(key), value);
			}
		}
		keys.clear();
		keys = null;
		oldMap.clear();
		oldMap = null;
		return result;
	}

	public void reflashContactToPreference() {
		SharedPreferences sp = mContext.getSharedPreferences(SP_CONTACT_VERSION_MAP_FILE_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.clear();
		editor.commit();

		Map<Integer, Integer> localVersionMap = getLocalRawContactVersionMap();
		if (localVersionMap == null) {
			return;
		}

		Integer key = null;
		Integer version = null;
		Iterator<Integer> keys = localVersionMap.keySet().iterator();
		while (keys.hasNext()) {
			key = keys.next();
			version = localVersionMap.get(key);
			editor.putInt(key.toString(), version);
		}
		editor.commit();

		localVersionMap.clear();
		localVersionMap = null;
	}
}
