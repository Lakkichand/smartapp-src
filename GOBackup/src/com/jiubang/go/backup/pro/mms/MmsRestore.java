package com.jiubang.go.backup.pro.mms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.jiubang.go.backup.pro.mms.telephony.GenericPdu;
import com.jiubang.go.backup.pro.mms.telephony.MmsException;
import com.jiubang.go.backup.pro.mms.telephony.PduParser;
import com.jiubang.go.backup.pro.mms.telephony.PduPersister;
import com.jiubang.go.backup.pro.mms.telephony.Telephony;
import com.jiubang.go.backup.pro.mms.telephony.Telephony.Mms;
import com.jiubang.go.backup.pro.mms.telephony.Telephony.Mms.Draft;
import com.jiubang.go.backup.pro.mms.telephony.Telephony.Mms.Inbox;
import com.jiubang.go.backup.pro.mms.telephony.Telephony.Mms.Sent;
import com.jiubang.go.backup.pro.sms.Sms;

/**
 * 彩信恢复
 * 
 * @author ReyZhang
 */
public class MmsRestore {
	private static final String MMS_DIR_NAME = "MMS_Backup";
	
	private boolean mStopFlag = false;
	private Object mLock = new Object();
	private Context mContext = null;
	private MmsRestoreArgs mArgs = null;
	private final int mBufferSpace = 1024;

	// key是会话id, 即ThreadId
	private Map<Long, ThreadStruct> mThreadNeedToUpdate = null;
 
	public boolean restoreMms(Context context, MmsRestoreArgs args) {
		boolean result = true;
		if (context == null) {
			result = false;
			return result;
		}
		if (args == null || args.mHandler == null || args.mRestoreFilePath == null) {
			result = false;
			return result;
		}

		mContext = context;
		mArgs = args;
		mmsRestoreStart();
		return result;
	}

	/**
	 * 彩信恢复参数
	 * 
	 * @author ReyZhang
	 */
	public static class MmsRestoreArgs {
		public Handler mHandler;
		public String mRestoreFilePath;
	}

	/**
	 * 彩信恢复消息通知接口
	 * 
	 * @author ReyZhang
	 */
	public interface MmsRestoreMsg {
		public static int MMS_RESTORE_START = 0x2001;
		public static int MMS_RESTORE_END = 0x2002;
		public static int MMS_RESTORE_PROCEEDING = 0x2003;
		public static int MMS_RESTORE_SMS_COUNT_ZERO = 0x2004;
		public static int MMS_RESTORE_USER_CANCEL = 0x2005;
		public static int MMS_RESTORE_ERROR_OCCUR = 0x2006;
		public static int MMS_RESTORE_FILE_NOT_EXIT = 0x2007;
		public static int MMS_RESTORE_START_UPDATE_CONVERSATION = 0x2008;
		public static int MMS_RESTORE_UPDATING_CONVERSATION = 0x2009;
		public static int MMS_RESTORE_UPDAGE_CONVERSATION_FINISH = 0x2010;
	}

	public void stopRestoreMms() {
		synchronized (mLock) {
			mStopFlag = true;
		}
	}

	private void mmsRestoreStart() {

		// 通知外部准备恢复彩信
		mArgs.mHandler.sendEmptyMessage(MmsRestoreMsg.MMS_RESTORE_START);

		// 判断文件是否存在
		File file = new File(mArgs.mRestoreFilePath);
		File pduFileDir = new File(mArgs.mRestoreFilePath + MMS_DIR_NAME);
		if (!file.exists() || !pduFileDir.exists()) {
			// 通知外部备份文件不存在
			mArgs.mHandler.sendEmptyMessage(MmsRestoreMsg.MMS_RESTORE_FILE_NOT_EXIT);
			return;
		}
		if (pduFileDir.list() == null) {
			mArgs.mHandler.sendEmptyMessage(MmsRestoreMsg.MMS_RESTORE_SMS_COUNT_ZERO);
			return;
		}

		// 恢复
		boolean result = false;
		result = restoreMmsFromBackupDir(mContext, pduFileDir);

		// 更新会话
		if (result) {
			result = updateConversation(mContext);
		}

		if (!result) {
			mArgs.mHandler.sendEmptyMessage(MmsRestoreMsg.MMS_RESTORE_ERROR_OCCUR);
		} else {
			mArgs.mHandler.sendEmptyMessage(MmsRestoreMsg.MMS_RESTORE_END);
		}

	}

	private void putInUpdateThread(Context context, Uri uri, boolean read) {
		if (mThreadNeedToUpdate == null) {
			mThreadNeedToUpdate = new HashMap<Long, MmsRestore.ThreadStruct>();
		}
		long threadId = getThreadId(context, uri);
		ThreadStruct thread = mThreadNeedToUpdate.get(threadId);
		if (thread != null) {
			// 只要有一条彩信未读，整个会话都是未读状态
			if (!read) {
				thread.readState = read;
			}
		} else if (threadId > 0) {
			String threadAddress = Telephony.getFrom(context, uri);
			mThreadNeedToUpdate.put(threadId, new ThreadStruct(threadAddress, read));
		}
	}

	private boolean restoreMmsFromBackupDir(Context context, File mmsBackupDir) {
		if (mmsBackupDir == null || !mmsBackupDir.exists() || !mmsBackupDir.isDirectory()) {
			return false;
		}
		
		File[] pduFiles = mmsBackupDir.listFiles();
		if (pduFiles == null || pduFiles.length <= 0) {
			return false;
		}

		Set<String> existedMmsDates = new HashSet<String>();
		// 第三步，读取数据库中的date字段值，装入set中
		Cursor cursor = mContext.getContentResolver()
				.query(Mms.CONTENT_URI, new String[] {Mms.DATE}, null, null, null);
		if (cursor != null) {
			try {
				final int mmsCount = cursor != null && cursor.getCount() > 0 ? cursor.getCount() : 0;
				if (mmsCount > 0) {
					if (cursor.moveToFirst()) {
						for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
							String date = cursor.getString(0);
							existedMmsDates.add(date);
						}
					}
				}
			} finally {
				cursor.close();
			}
		}
		
		final int count = pduFiles.length;
		for (int i = 0; i < count; i++) {
			synchronized (mLock) {
				if (mStopFlag) {
					break;
				}
			}
			final File pduFile = pduFiles[i];
			final String fileName = pduFile.getName();
			String date = fileName.substring(2, fileName.indexOf("("));
			// TODO 以日期作为彩信的唯一标识，可能并不可靠
			if (!existedMmsDates.contains(date)) {
				restoreSingleMms(context, pduFile);
			}
			
			// 通知当前恢复第几个
			Message.obtain(mArgs.mHandler, MmsRestoreMsg.MMS_RESTORE_PROCEEDING, i + 1,
					count).sendToTarget();
		}
		return true;
	}
	
	private boolean restoreSingleMms(Context context, File pduFile) {
		if (pduFile == null || !pduFile.exists()) {
			return false;
		}
		final String fileName = pduFile.getName();
		String flag = fileName.substring(0, 1);
		Uri uri = null;
		if ("S".equals(flag)) {
			uri = Sent.CONTENT_URI;
		} else if ("D".equals(flag)) {
			uri = Draft.CONTENT_URI;
		} else {
			uri = Inbox.CONTENT_URI;
		}
		
		try {
			byte[] bytes = getPdu(pduFile);
			PduParser parser = new PduParser(bytes);
			GenericPdu pdu = parser.parse();
			if (pdu != null) {
				PduPersister pduPersister = PduPersister.getPduPersister(context);
				Uri mmsUri = pduPersister.persist(pdu, uri);
				// 根据返回的uri再次更新读取状态
				if (mmsUri != null) {
					ContentValues tempValue = new ContentValues();
					boolean read = Integer.valueOf(fileName.substring(fileName.indexOf("(") + 1,
							fileName.indexOf(")"))) > 0;
					String lock = fileName.substring(fileName.indexOf("[") + 1,
							fileName.indexOf("]"));
					tempValue.put(Mms.READ, read);
					tempValue.put(Mms.LOCKED, lock);
					mContext.getContentResolver().update(mmsUri, tempValue, null, null);
					putInUpdateThread(context, mmsUri, read);
				}
			}
		} catch (MmsException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 更新会话结构
	 * 
	 * @author maiyongshen
	 */
	private static class ThreadStruct {
//		public Uri uri;
//		public long threadId;
		
		public String threadAddress;
		public boolean readState;
		
		public ThreadStruct(String address, boolean read) {
			threadAddress = address;
			readState = read;
		}
	}

	private boolean updateConversation(Context context) {
		if (mThreadNeedToUpdate == null || mThreadNeedToUpdate.size() < 1) {
			return true;
		}

		Message.obtain(mArgs.mHandler, MmsRestoreMsg.MMS_RESTORE_START_UPDATE_CONVERSATION)
				.sendToTarget();
		
		try {
			final Set<Entry<Long, ThreadStruct>> entries = mThreadNeedToUpdate.entrySet();
			if (entries == null) {
				return true;
			}
			int index = 0;
			for (Entry<Long, ThreadStruct> e : entries) {
				index++;
				Message.obtain(mArgs.mHandler, MmsRestoreMsg.MMS_RESTORE_UPDATING_CONVERSATION, index,
						entries.size()).sendToTarget();
				final ThreadStruct threadStruct = e.getValue();
				if (threadStruct != null && threadStruct.threadAddress != null) {
					updateThread(context, threadStruct.threadAddress, threadStruct.readState);
				}
			}
		} finally {
			mThreadNeedToUpdate.clear();
		}
		
		Message.obtain(mArgs.mHandler, MmsRestoreMsg.MMS_RESTORE_UPDAGE_CONVERSATION_FINISH)
				.sendToTarget();
		return true;
	}
	
	private void updateThread(Context context, String address, boolean read) {
		// 增删一条临时短信促使会话更新
		ContentValues tempvalue = new ContentValues();
		tempvalue.put(Sms.TextBasedSmsColumns.ADDRESS, address);
		
		tempvalue.put(Telephony.Sms.TYPE, "1");
		tempvalue.put(Telephony.Sms.READ, read ? "1" : "0");
		tempvalue.put(Telephony.Sms.BODY, "Smsbackup Temp Message, delete it please");
		long time = System.currentTimeMillis();
		tempvalue.put(Telephony.Sms.DATE, time);

		Uri tempUri = Sms.insertSms(context, Sms.CONTENT_URI, tempvalue);
		if (tempUri != null) {
			Log.d("GOBackup", "tempUri = " + tempUri);
			Sms.deleteSms(context, tempUri, null, null);
		}
	}
	
	private long getThreadId(Context context, Uri mmsUri) {
		long mmsId = ContentUris.parseId(mmsUri);
		Cursor cursor = context.getContentResolver().query(Mms.CONTENT_URI,
				new String[] { Mms.THREAD_ID }, Mms._ID + "=" + mmsId, null, null);
		if (cursor == null) {
			return -1;
		}
		try {
			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
				return cursor.getLong(0);
			}
		} finally {
			cursor.close();
		}
		return -1;
	}

	private byte[] getPdu(File file) {

		byte[] data = null;
		byte[] buffer = new byte[mBufferSpace];

		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		int rc = 0;

		try {
			fis = new FileInputStream(file);
			bos = new ByteArrayOutputStream();

			while ((rc = fis.read(buffer, 0, mBufferSpace)) != -1) {
				bos.write(buffer, 0, rc);
			}
			data = bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			data = null;
		} finally {
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
				if (bos != null) {
					bos.close();
					bos = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}
}
