package com.jiubang.go.backup.pro.calendar;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.net.Uri;

import com.jiubang.go.backup.pro.calendar.CalendarOperator.CalendarStruct;

/**
 * 日历Json持久化实现
 * @author wencan
 *
 */

public class CalendarJsonParser implements ICalendarParser {

	private static final String CALENDAR_DB_VERSION = "sdk_version";
	private static final String CALENDAR_ARRAY = "calendar_array";
	private static final String CALENDAR = "calendar";
	private static final String EVENT = "event";
	private static final String EVENT_SUB = "event_sub";
	private static final String EVENT_SUB_TYPE_URI = "event_sub_type_uri";
	private static final String EVENT_SUB_TYPE_VALUE = "event_sub_type_value";

	private Context mContext;
	private List<CalendarStruct> mCalendars;
	private File mSrcFile;

	private OnCalendarPersistListener mListener;

	private int mTotalCalendarCount;
	private int mCurCalendarIndex;
	private int mTotalEventCount;
	private int mCurEventIndex;
	private int mSuccessCalendarCount;
	private int mSuccessEventCount;

	/**
	 * 从日历对象构建
	 * @param context
	 * @param calendars
	 */
	public CalendarJsonParser(Context context, List<CalendarStruct> calendars) {
		mContext = context;
		mCalendars = calendars;
		reset();
	}

	/**
	 * 从文件中构建
	 * @param context
	 * @param srcFile
	 */
	public CalendarJsonParser(Context context, File srcFile) {
		mSrcFile = srcFile;
		reset();
	}

	/**
	 * 设置日历持久化监听
	 * @param listener
	 */
	public void setOnCalendarPersistListener(OnCalendarPersistListener listener) {
		mListener = listener;
	}

	private void reset() {
		mTotalCalendarCount = getCalendarCount();
		mTotalEventCount = getEventCount();
		mCurCalendarIndex = 0;
		mCurEventIndex = 0;
		mSuccessCalendarCount = 0;
		mSuccessEventCount = 0;
	}

	private int getCalendarCount() {
		return mCalendars != null ? mCalendars.size() : 0;
	}

	private int getEventCount() {
		if (mCalendars == null || mCalendars.size() == 0) {
			mTotalEventCount = 0;
			return mTotalEventCount;
		}

		int eventCount = 0;
		for (CalendarStruct calendar : mCalendars) {
			eventCount += calendar.getEventCount();
		}
		mTotalEventCount = eventCount;
		return mTotalEventCount;
	}

	@Override
	public List<CalendarStruct> parser() {
		if (mSrcFile == null || !mSrcFile.exists()) {
			return null;
		}

		FileInputStream fis = null;
		DataInputStream dis = null;
		String strCalendar = null;
		try {
			fis = new FileInputStream(mSrcFile);
			dis = new DataInputStream(fis);
			strCalendar = dis.readUTF();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
				if (dis != null) {
					dis.close();
					dis = null;
				}
			} catch (IOException e) {
			}
		}

		if (strCalendar == null) {
			return null;
		}

		List<CalendarStruct> allCalendarStructs = null;
		JSONObject calendarBackup = null;
		try {
			calendarBackup = new JSONObject(strCalendar);
			Iterator<String> keys = calendarBackup.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				if (CALENDAR_ARRAY.equals(key)) {
					// 解析日历JsonArray
					JSONArray calendarArray = calendarBackup.optJSONArray(key);
					allCalendarStructs = parserCalendarArray(calendarArray);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mCalendars = allCalendarStructs;
		reset();
		return allCalendarStructs;
	}

	@Override
	public boolean persist(File destFile) {
		if (destFile == null) {
			return false;
		}

		if (mCalendars == null || mCalendars.size() == 0) {
			return false;
		}

		boolean ret = true;
		JSONObject calendarBackup = null;
		reset();
		try {
			onPersistStart(mTotalCalendarCount, mTotalEventCount);
			calendarBackup = new JSONObject();
			// 保存日历
			JSONArray calendarArray = new JSONArray();
			for (CalendarStruct calendar : mCalendars) {
				mCurCalendarIndex++;
				ret = writeCalendar(calendarArray, calendar);
				if (!ret) {
					break;
				}
			}
			calendarBackup.put(CALENDAR_ARRAY, calendarArray);
		} catch (JSONException e) {
			e.printStackTrace();
			ret = false;
		}

		if (!ret) {
			return ret;
		}

		// 保存到文件
		String strCalendar = calendarBackup.toString();

		FileOutputStream fos = null;
		DataOutputStream dos = null;
		try {
			fos = new FileOutputStream(destFile);
			dos = new DataOutputStream(fos);
			dos.writeUTF(strCalendar);
			dos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			ret = false;
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (dos != null) {
					dos.close();
					dos = null;
				}
				if (fos != null) {
					fos.close();
					fos = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		onPersistEnd(ret, mSuccessCalendarCount, mTotalCalendarCount, mSuccessEventCount,
				mTotalEventCount);
		return ret;
	}

	private List<CalendarStruct> parserCalendarArray(JSONArray calendarArray) {
		if (calendarArray == null) {
			return null;
		}

		List<CalendarStruct> calendarStructs = new ArrayList<CalendarOperator.CalendarStruct>();
		final int lenght = calendarArray.length();
		for (int i = 0; i < lenght; i++) {
			JSONObject calendar = calendarArray.optJSONObject(i);
			//解析Calendar的JsonObject
			CalendarStruct calendarStruct = parserCalendar(calendar);
			if (calendarStruct != null) {
				calendarStructs.add(calendarStruct);
			}
		}
		return calendarStructs;
	}

	private CalendarStruct parserCalendar(JSONObject calendar) {
		if (calendar == null) {
			return null;
		}

		CalendarStruct calendarStruct = new CalendarStruct();

		Iterator<String> keys = calendar.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key == null) {
				continue;
			}

			if (CALENDAR_DB_VERSION.equals(key)) {
				calendarStruct.setCalendarSdkVersion(calendar.optInt(key));
				continue;
			} else if (CALENDAR.equals(key)) {
				JSONObject calendarInstance = calendar.optJSONObject(key);
				if (!parserCalendarInstance(calendarStruct, calendarInstance)) {
					return null;
				}
			}
		}

		return calendarStruct.isCalendarStructValid() ? calendarStruct : null;
	}

	private boolean parserCalendarInstance(CalendarStruct calendarStruct,
			JSONObject calendarInstance) {
		if (calendarStruct == null || calendarInstance == null) {
			return false;
		}

		ContentValues cv = null;
		Iterator<String> keys = calendarInstance.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key == null) {
				continue;
			}

			if (EVENT.equals(key)) {
				// 解析Event对象
				JSONArray eventArray = calendarInstance.optJSONArray(key);
				List<Entity> events = parseEventArray(eventArray);
				if (events != null) {
					calendarStruct.addEventEntities(events);
				}
				continue;
			} else {
				// 解析普通calendar的属性
				if (cv == null) {
					cv = new ContentValues();
				}
				cv.put(key, calendarInstance.optString(key));
			}
		}
		if (cv != null) {
			calendarStruct.setCalendarEntity(new Entity(cv));
		}
		return true;
	}

	private List<Entity> parseEventArray(JSONArray eventArray) {
		if (eventArray == null) {
			return null;
		}

		List<Entity> eventEntities = new ArrayList<Entity>();
		final int length = eventArray.length();
		for (int i = 0; i < length; i++) {
			JSONObject eventObject = eventArray.optJSONObject(i);
			Entity event = parserEvent(eventObject);
			if (event != null) {
				int eventId = CalendarOperator.getEventId(event);
				if (eventId == 0) {
					continue;
				}
				eventEntities.add(event);
			}
		}
		return eventEntities;
	}

	private Entity parserEvent(JSONObject eventObject) {
		if (eventObject == null) {
			return null;
		}

		Entity event = null;
		ContentValues cv = null;
		List<Map<Uri, ContentValues>> subValues = null;
		Iterator<String> keys = eventObject.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key == null) {
				continue;
			}

			if (EVENT_SUB.equals(key)) {
				// 解析Event的sub属性，如remider等
				JSONArray subArray = eventObject.optJSONArray(key);
				subValues = parserEventSubValuesArray(subArray);
				continue;
			} else {
				if (cv == null) {
					cv = new ContentValues();
				}
				cv.put(key, eventObject.optString(key));
			}
		}

		if (cv != null) {
			event = new Entity(cv);
		}
		if (subValues != null) {
			for (Map<Uri, ContentValues> subValue : subValues) {
				Iterator<Uri> subKeys = subValue.keySet().iterator();
				while (subKeys.hasNext()) {
					Uri uri = subKeys.next();
					event.addSubValue(uri, subValue.get(uri));
				}
			}
		}
		return event;
	}

	private List<Map<Uri, ContentValues>> parserEventSubValuesArray(JSONArray subArray) {
		if (subArray == null || subArray.length() == 0) {
			return null;
		}

		List<Map<Uri, ContentValues>> result = new ArrayList<Map<Uri, ContentValues>>();
		final int length = subArray.length();
		for (int i = 0; i < length; i++) {
			Map<Uri, ContentValues> subValue = new HashMap<Uri, ContentValues>();
			JSONObject subObject = subArray.optJSONObject(i);
			parserSubObject(subValue, subObject);
			result.add(subValue);
		}
		return result;
	}

	private boolean parserSubObject(Map<Uri, ContentValues> parent, JSONObject subObject) {
		if (parent == null || subObject == null) {
			return false;
		}

		Uri uri = null;
		ContentValues cv = null;
		Iterator<String> keys = subObject.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key == null) {
				continue;
			}

			if (EVENT_SUB_TYPE_URI.equals(key)) {
				// uri字段
				uri = Uri.parse(subObject.optString(key));
			} else if (EVENT_SUB_TYPE_VALUE.equals(key)) {
				// value字段
				JSONObject subValues = subObject.optJSONObject(key);
				cv = parserCommentObject(subValues);
			}
		}
		if (uri != null && cv != null) {
			parent.put(uri, cv);
			return true;
		}
		return false;
	}

	private ContentValues parserCommentObject(JSONObject object) {
		if (object == null) {
			return null;
		}

		ContentValues cv = null;
		Iterator<String> keys = object.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			if (key == null) {
				continue;
			}
			if (cv == null) {
				cv = new ContentValues();
			}
			cv.put(key, object.optString(key));
		}
		return cv;
	}

	private boolean writeCalendar(JSONArray parent, CalendarStruct calendar) throws JSONException {
		if (parent == null || calendar == null) {
			return false;
		}

		JSONObject calendarObject = new JSONObject();
		//写入辅助信息
		calendarObject.put(CALENDAR_DB_VERSION, calendar.getCalendarSdkVersion());

		//写入calendar属性
		JSONObject calendarInstance = new JSONObject();
		Entity calendarEntity = calendar.getCalendarEntity();
		if (!writeCalendarEntity(calendarInstance, calendarEntity)) {
			return false;
		}

		//写入event
		List<Entity> eventEntities = calendar.getEventEntities();
		if (eventEntities != null && eventEntities.size() > 0) {
			JSONArray eventArray = new JSONArray();
			if (!writeEvent(eventArray, eventEntities)) {
				return false;
			}
			calendarInstance.put(EVENT, eventArray);
		}

		calendarObject.put(CALENDAR, calendarInstance);
		parent.put(calendarObject);
		return true;
	}

	private boolean writeCalendarEntity(JSONObject calendarObject, Entity calendar)
			throws JSONException {
		if (calendar == null) {
			return false;
		}
		ContentValues cv = calendar.getEntityValues();
		if (cv == null) {
			return false;
		}

		return writeContentValues(calendarObject, cv);
	}

	private boolean writeEvent(JSONArray eventArray, List<Entity> eventEntities)
			throws JSONException {
		if (eventArray == null || eventEntities == null) {
			return false;
		}

		for (Entity event : eventEntities) {
			mCurEventIndex++;
			onPersistProgress(mCurCalendarIndex, mTotalCalendarCount, mCurEventIndex,
					mTotalEventCount);
			if (writeEventEntity(eventArray, event)) {
				mSuccessEventCount++;
			}
		}
		return true;
	}

	private boolean writeEventEntity(JSONArray eventArray, Entity event) throws JSONException {
		if (eventArray == null || event == null) {
			return false;
		}

		ContentValues cv = event.getEntityValues();
		if (cv == null) {
			return false;
		}

		//写入event的属性
		JSONObject eventObject = new JSONObject();
		if (!writeContentValues(eventObject, cv)) {
			return false;
		}

		//写入event相关的sub属性
		List<NamedContentValues> subValues = event.getSubValues();
		if (subValues != null && subValues.size() > 0) {
			JSONArray subArray = new JSONArray();
			for (NamedContentValues item : subValues) {
				Uri uri = item.uri;
				ContentValues subCv = item.values;

				JSONObject subObject = new JSONObject();
				subObject.put(EVENT_SUB_TYPE_URI, uri.toString());

				JSONObject subValuesObject = new JSONObject();
				if (!writeContentValues(subValuesObject, subCv)) {
					return false;
				}
				subObject.put(EVENT_SUB_TYPE_VALUE, subValuesObject);

				subArray.put(subObject);
			}
			eventObject.put(EVENT_SUB, subArray);
		}
		eventArray.put(eventObject);
		return true;
	}

	private boolean writeContentValues(JSONObject object, ContentValues cv) throws JSONException {
		if (object == null || cv == null) {
			return false;
		}

		Iterator<Entry<String, Object>> keys = cv.valueSet().iterator();
		while (keys.hasNext()) {
			Entry<String, Object> valueEntry = keys.next();
			String key = valueEntry.getKey();
			String value = valueEntry.getValue() != null ? valueEntry.getValue().toString() : null;
			if (value != null) {
				object.put(key, value);
			}
		}
		return true;
	}

	private void onPersistProgress(int curCalendar, int totalCalendar, int curEvent, int totalEvent) {
		if (mListener != null) {
			mListener.onPersistProgress(curCalendar, totalCalendar, curEvent, totalEvent);
		}
	}

	private void onPersistEnd(boolean success, int successCalendarCount, int totalCalendar,
			int successEventCount, int totalEvent) {
		if (mListener != null) {
			mListener.onPersistEnd(success, successCalendarCount, totalCalendar, successEventCount,
					totalEvent);
		}
	}

	private void onPersistStart(int totalCalendarCount, int totalEventCount) {
		if (mListener != null) {
			mListener.onPersistStart(totalCalendarCount, totalEventCount);
		}
	}
}
