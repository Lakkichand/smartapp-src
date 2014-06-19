package com.jiubang.go.backup.pro.sms;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * 短信
 *
 * @author WenCan
 */
public class Sms {

	// GO短信原来备份恢复的字段
	public static final int SMS_ADDRESS = 0;
	public static final int SMS_DATE = 1;
	public static final int SMS_READ = 2;
	public static final int SMS_STATUS = 3;
	public static final int SMS_TYPE = 4;
	public static final int SMS_REPLY_PATH_PRESENT = 5;
	public static final int SMS_BODY = 6;
	public static final int SMS_ID = 7;
	public static final int SMS_LOCK = 8;
	public static final int SMS_THREAD_ID = 9;

	// xml的标签定义
	/**
	 * 短信sml标签
	 */
	public interface SmsXmlTag {
		public static final String GOSMS = "GoSms";
		public static final String COUNT = "SMSCount";
		public static final String SMS = "SMS";
		public static final String ID = "_id";
		public static final String ADDRESS = "address";
		public static final String DATE = "date";
		public static final String READ = "read";
		public static final String STATUS = "status";
		public static final String TYPE = "type";
		public static final String REPLY = "reply_path_present";
		public static final String BODY = "body";
		public static final String THREAD_ID = "threadId";
	}

	/**
	 * 短信结构
	 *
	 * @author WenCan
	 */
	public static class SmsStruct {
		HashMap<String, String> mData;

		public SmsStruct() {
			mData = new HashMap<String, String>();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof SmsStruct)) {
				return false;
			}

			SmsStruct sms = (SmsStruct) o;
			return mData.equals(sms.mData);
		}

		@Override
		public int hashCode() {
			return mData.hashCode();
		}

		public void putFiled(String key, String value) {
			mData.put(key, value);
		}

		public String getFiled(String key) {
			return mData.get(key);
		}

		public int getFieldCount() {
			return mData != null ? mData.size() : 0;
		}

		public Set<String> getKeySets() {
			return mData.keySet();
		}

		public String getValue(String key) {
			return mData.get(key);
		}

		@Override
		public String toString() {
			return mData.toString();
		}

		private int getThreadId() {
			String strThreadId = getFiled(Sms.TextBasedSmsColumns.THREAD_ID);
			if (strThreadId == null) {
				return -1;
			}
			try {
				return Integer.valueOf(strThreadId).intValue();
			} catch (Exception e) {
			}
			return -1;
		}

		private long getDate() {
			String strDate = getFiled(Sms.TextBasedSmsColumns.DATE);
			if (strDate == null) {
				return -1;
			}
			try {
				return Long.valueOf(strDate).longValue();
			} catch (Exception e) {
			}
			return -1;
		}

		/**
		 *
		 * @author WenCan
		 */
		public static class SmsComparator implements Comparator<SmsStruct> {

			@Override
			public int compare(SmsStruct lhs, SmsStruct rhs) {
				int lThreadId = lhs.getThreadId();
				int rThreadId = rhs.getThreadId();
				long lDate = lhs.getDate();
				long rDate = rhs.getDate();

				if (lThreadId != rThreadId) {
					return lThreadId - rThreadId;
				}

				long ret = lDate - rDate;
				return ret < 0 ? -1 : ret == 0 ? 0 : 1;
			}
		}
	}

	/**
	 * 线程消息
	 *
	 * @author WenCan
	 */
	public static class ThreadsInfo {
		String mThreadId;
		String mAddress;

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof ThreadsInfo)) {
				return false;
			}
			ThreadsInfo info = (ThreadsInfo) o;
			if (mThreadId.equals(info.mThreadId) && mAddress.equals(info.mAddress)) {
				return true;
			}
			return false;
		}
	}

	public static final String[] THREADS_PROJECTION = { "thread_id", "address" };

	/**
	 * utf-8字体格式字符串
	 */
	public static final String STR_FONTCODE_UTF8 = "UTF-8";

	/**
	 * 长度为0的空字符串
	 */
	public static final String STR_EMPTY_STRING = "";

	public static final String AUTHRITY = "sms";

	/**
	 * The content:// style URL for this table
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://sms");

	/**
	 * The default sort order for this table
	 */
	public static final String DEFAULT_SORT_ORDER = "thread_id, date ASC";

	/**
	 * Base columns for tables that contain text based SMSs.
	 */
	public interface TextBasedSmsColumns {

		public static final String SMS_ID = "_id";

		/**
		 * The type of the message
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String TYPE = "type";

		/**
		 * The thread ID of the message
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String THREAD_ID = "thread_id";

		/**
		 * The address of the other party
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String ADDRESS = "address";

		/**
		 * The person ID of the sender
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String PERSON_ID = "person";

		/**
		 * The date the message was received
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String DATE = "date";

		/**
		 * The date the message was sent
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String DATE_SENT = "date_sent";

		/**
		 * Has the message been read
		 * <P>
		 * Type: INTEGER (boolean)
		 * </P>
		 */
		public static final String READ = "read";

		/**
		 * Indicates whether this message has been seen by the user. The "seen"
		 * flag will be used to figure out whether we need to throw up a
		 * statusbar notification or not.
		 */
		public static final String SEEN = "seen";

		/**
		 * The TP-Status value for the message, or -1 if no status has been
		 * received
		 */
		public static final String STATUS = "status";

		public static final int STATUS_NONE = -1;
		public static final int STATUS_COMPLETE = 0;
		public static final int STATUS_PENDING = 32;
		public static final int STATUS_FAILED = 64;

		/**
		 * The subject of the message, if present
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String SUBJECT = "subject";

		/**
		 * The body of the message
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String BODY = "body";

		/**
		 * The id of the sender of the conversation, if present
		 * <P>
		 * Type: INTEGER (reference to item in content://contacts/people)
		 * </P>
		 */
		public static final String PERSON = "person";

		/**
		 * The protocol identifier code
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String PROTOCOL = "protocol";

		/**
		 * Whether the <code>TP-Reply-Path</code> bit was set on this message
		 * <P>
		 * Type: BOOLEAN
		 * </P>
		 */
		public static final String REPLY_PATH_PRESENT = "reply_path_present";

		/**
		 * The service center (SC) through which to send the message, if present
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String SERVICE_CENTER = "service_center";

		/**
		 * Has the message been locked?
		 * <P>
		 * Type: INTEGER (boolean)
		 * </P>
		 */
		public static final String LOCKED = "locked";

		/**
		 * Error code associated with sending or receiving this message
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String ERROR_CODE = "error_code";
	}

	public static Uri insertSms(Context context, Uri url, ContentValues cv) {
		if (context == null || url == null || cv == null) {
			return null;
		}
		Uri result = null;
		try {
			result = context.getContentResolver().insert(url, cv);
		} catch (Exception e) {
			e.printStackTrace();
			result = null;
		}
		return result;
	}

	public static int deleteSms(Context context, Uri url, String where, String[] selectionArgs) {
		if (context == null || url == null) {
			return 0;
		}

		int count = 0;
		try {
			count = context.getContentResolver().delete(url, where, selectionArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count;
	}

	public static int mBulkInsertSms(Context context, Uri url, ContentValues[] cvs) {
		if (context == null || url == null || cvs == null) {
			return 0;
		}

		int count = 0;
		try {
			count = context.getContentResolver().bulkInsert(url, cvs);
		} catch (Exception e) {
			e.printStackTrace();
			count = 0;
		}
		return count;
	}

	public static Cursor querySms(Context context, Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					sortOrder);
		} catch (Exception e) {
		}
		return cursor;
	}
}
