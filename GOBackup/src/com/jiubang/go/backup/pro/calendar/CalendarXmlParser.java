package com.jiubang.go.backup.pro.calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Xml;

import com.jiubang.go.backup.pro.calendar.CalendarOperator.CalendarStruct;

/**
 * 日历的xml解析实现
 * @author wencan
 *
 */

public class CalendarXmlParser implements ICalendarParser {

	private static final String CALENDAR_DB_VERSION = "sdk_version";
	private static final String CALENDAR_ARRAY = "calendar_array";
	private static final String CALENDAR = "calendar";
	private static final String EVENT = "event";
	private static final String EVENT_SUB = "event_sub";
	private static final String EVENT_SUB_ITEM = "event_sub_item";

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
	public CalendarXmlParser(Context context, List<CalendarStruct> calendars) {
		mContext = context;
		mCalendars = calendars;
		reset();
	}

	/**
	 * 从文件对象构建
	 * @param context
	 * @param srcFile
	 */
	public CalendarXmlParser(Context context, File srcFile) {
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

	@Override
	public List<CalendarStruct> parser() {
		if (mSrcFile == null || !mSrcFile.exists()) {
			return null;
		}

		boolean ret = false;
		FileInputStream fis = null;
		XmlPullParser parser = null;
		try {
			fis = new FileInputStream(mSrcFile);
			parser = Xml.newPullParser();
			parser.setInput(fis, "UTF-8");
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

			mCalendars = parserInternal(parser);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		reset();
		return mCalendars;
	}

	private List<CalendarStruct> parserInternal(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		boolean ret = false;

		String name = null;
		List<CalendarStruct> allCalendars = null;
		CalendarStruct calendarStruct = null;
		Entity calendarEntity = null;
		Entity eventEntity = null;

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
				case XmlPullParser.START_DOCUMENT :
					break;

				case XmlPullParser.START_TAG : {
					name = parser.getName();
					if (TextUtils.isEmpty(name)) {
						break;
					}

					if (name.equalsIgnoreCase(CALENDAR_ARRAY)) {
						// 日历根标签
						allCalendars = new ArrayList<CalendarOperator.CalendarStruct>();
					} else if (name.equalsIgnoreCase(CALENDAR)) {
						// 日历
						calendarStruct = new CalendarStruct();
						calendarEntity = new Entity(new ContentValues());
						ret = parserCalendarTag(parser, calendarStruct, calendarEntity);
						if (!ret) {
							// 解析Calendar失败，退出
							return null;
						}
					} else if (name.equalsIgnoreCase(EVENT)) {
						eventEntity = new Entity(new ContentValues());
						ret = parserEventTag(parser, eventEntity);
						if (!ret) {
							// 解析event属性出错，退出
							return null;
						}
					} else if (name.equalsIgnoreCase(EVENT_SUB)) {
						// TODO
					} else if (name.equalsIgnoreCase(EVENT_SUB_ITEM)) {
						// Event sub item
						if (eventEntity == null) {
							// 出错
							return null;
						}
						ret = parserEventSubItem(parser, eventEntity);
						if (!ret) {
							// 失败
							return null;
						}
					}
				}
					break;

				case XmlPullParser.END_TAG : {
					name = parser.getName();
					if (TextUtils.isEmpty(name)) {
						break;
					}

					if (name.equalsIgnoreCase(CALENDAR_ARRAY)) {
						// 日历根标签
						calendarStruct = null;
					} else if (name.equalsIgnoreCase(CALENDAR)) {
						// 日历
						if (calendarStruct == null || calendarEntity == null) {
							return null;
						}
						calendarStruct.setCalendarEntity(calendarEntity);
						allCalendars.add(calendarStruct);
						calendarStruct = null;
						calendarEntity = null;
					} else if (name.equalsIgnoreCase(EVENT)) {
						// 事件
						if (calendarStruct == null || eventEntity == null) {
							return null;
						}
						calendarStruct.addEventEntity(eventEntity);
						eventEntity = null;
					} else if (name.equalsIgnoreCase(EVENT_SUB)) {
						// TODO
					} else if (name.equalsIgnoreCase(EVENT_SUB_ITEM)) {
						// Event sub item
					}
				}
					break;

				case XmlPullParser.END_DOCUMENT :
					break;

				default :
					break;
			} // end switch

			eventType = parser.next();
		} // end whle

		if (allCalendars != null && allCalendars.size() > 0) {
			return allCalendars;
		}
		return null;
	}

	private boolean parserCalendarTag(XmlPullParser parser, CalendarStruct calendar,
			Entity calendarEntity) {
		final int attrCount = parser.getAttributeCount();
		if (attrCount == 0) {
			// 出错
			return false;
		}

		ContentValues cv = null;
		String key = null;
		String value = null;
		int calendarSdkInt = 0;

		cv = calendarEntity.getEntityValues();
		cv.clear();
		for (int i = 0; i < attrCount; i++) {
			key = parser.getAttributeName(i);
			value = parser.getAttributeValue(i);
			if (key.equalsIgnoreCase(CALENDAR_DB_VERSION)) {
				calendarSdkInt = Integer.valueOf(value);
				continue;
			}
			cv.put(key, value);
		}
		calendar.setCalendarSdkVersion(calendarSdkInt);
		return true;
	}

	private boolean parserEventTag(XmlPullParser parser, Entity eventEntity) {
		final int attrCount = parser.getAttributeCount();
		if (attrCount == 0) {
			// 出错
			return false;
		}

		ContentValues cv = null;
		String key = null;
		String value = null;

		cv = eventEntity.getEntityValues();
		cv.clear();
		for (int i = 0; i < attrCount; i++) {
			key = parser.getAttributeName(i);
			value = parser.getAttributeValue(i);
			if (key != null && value != null) {
				cv.put(key, value);
			}
		}
		return true;
	}

	private boolean parserEventSubItem(XmlPullParser parser, Entity eventEntity)
			throws XmlPullParserException, IOException {
		final int attrCount = parser.getAttributeCount();
		if (attrCount > 0) {
			ContentValues cv = null;
			String key = null;
			String value = null;

			cv = new ContentValues();
			for (int i = 0; i < attrCount; i++) {
				key = parser.getAttributeName(i);
				value = parser.getAttributeValue(i);
				if (key != null && value != null) {
					cv.put(key, value);
				}
			}

			Uri uri = Uri.parse(parser.nextText());
			eventEntity.addSubValue(uri, cv);
			return true;
		}
		return false;
	}

	@Override
	public boolean persist(File destFile) {
		if (destFile == null) {
			return false;
		}

		if (mCalendars == null || mCalendars.size() == 0) {
			return false;
		}

		boolean ret = false;
		XmlSerializer xmlSerializer = null;
		FileOutputStream fos = null;
		reset();
		try {
			onPersistStart(mTotalCalendarCount, mTotalEventCount);
			fos = new FileOutputStream(destFile);
			xmlSerializer = Xml.newSerializer();

			xmlSerializer.setOutput(fos, Xml.Encoding.UTF_8.name());
			ret = persistInternal(xmlSerializer);
			fos.flush();

			ret = true;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		onPersistEnd(ret, mSuccessCalendarCount, mTotalCalendarCount, mSuccessEventCount,
				mTotalEventCount);
		return ret;
	}

	private boolean persistInternal(XmlSerializer xmlSerializer) throws IllegalArgumentException,
			IllegalStateException, IOException {
		boolean ret = false;

		xmlSerializer.startDocument(Xml.Encoding.UTF_8.name(), true);
		xmlSerializer.startTag(null, CALENDAR_ARRAY);

		for (CalendarStruct calendar : mCalendars) {
			mCurCalendarIndex++;
			ret = persistCalendar(xmlSerializer, calendar);
			if (!ret) {
				// 失败，退出
				return ret;
			}
			mSuccessCalendarCount++;
		}

		xmlSerializer.endTag(null, CALENDAR_ARRAY);
		xmlSerializer.endDocument();

		return ret;
	}

	private boolean persistCalendar(XmlSerializer xmlSerializer, CalendarStruct calendar)
			throws IllegalArgumentException, IllegalStateException, IOException {
		boolean ret = false;
		xmlSerializer.startTag(null, CALENDAR);

		// 写入日历的属性
		xmlSerializer.attribute(null, CALENDAR_DB_VERSION,
				String.valueOf(calendar.getCalendarSdkVersion()));
		Entity entity = calendar.getCalendarEntity();
		ContentValues cv = entity != null ? entity.getEntityValues() : null;
		ret = persistContentValues(xmlSerializer, cv);

		// 写入日历的event
		List<Entity> events = calendar.getEventEntities();
		if (events != null && events.size() > 0) {
			for (Entity eventEntity : events) {
				mCurEventIndex++;
				ret = persistEvent(xmlSerializer, eventEntity);
				if (!ret) {
					// 失败，退出
					return false;
				}
				mSuccessEventCount++;
				onPersistProgress(mCurCalendarIndex, mTotalCalendarCount, mCurEventIndex,
						mTotalEventCount);
			}
		}

		xmlSerializer.endTag(null, CALENDAR);
		return ret;
	}

	private boolean persistEvent(XmlSerializer xmlSerializer, Entity event)
			throws IllegalArgumentException, IllegalStateException, IOException {
		boolean ret = false;
		xmlSerializer.startTag(null, EVENT);

		// 写入event的属性
		ContentValues cv = event.getEntityValues();
		ret = persistContentValues(xmlSerializer, cv);
		if (!ret) {
			// 写入event属性失败，退出
			return false;
		}

		// 写入event的sub
		List<NamedContentValues> subValues = event.getSubValues();
		if (subValues != null && subValues.size() > 0) {
			xmlSerializer.startTag(null, EVENT_SUB);

			for (NamedContentValues item : subValues) {
				Uri uri = item.uri;
				ContentValues subCv = item.values;
				xmlSerializer.startTag(null, EVENT_SUB_ITEM);
				ret = persistContentValues(xmlSerializer, subCv);
				if (!ret) {
					// 写入sub的属性失败，退出
					return false;
				}
				xmlSerializer.text(uri.toString());
				xmlSerializer.endTag(null, EVENT_SUB_ITEM);
			}

			xmlSerializer.endTag(null, EVENT_SUB);
		}
		xmlSerializer.endTag(null, EVENT);
		return ret;
	}

	private boolean persistContentValues(XmlSerializer xmlSerializer, ContentValues cv)
			throws IllegalArgumentException, IllegalStateException, IOException {
		if (xmlSerializer == null || cv == null) {
			return false;
		}

		Iterator<Entry<String, Object>> keys = cv.valueSet().iterator();
		while (keys.hasNext()) {
			Entry<String, Object> valueEntry = keys.next();
			String key = valueEntry.getKey();
			String value = valueEntry.getValue() != null ? valueEntry.getValue().toString() : null;
			if (value != null) {
				xmlSerializer.attribute(null, key, value);
			}
		}
		return true;
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
