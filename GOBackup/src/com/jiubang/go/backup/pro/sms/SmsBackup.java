package com.jiubang.go.backup.pro.sms;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import com.jiubang.go.backup.pro.util.Util;

/**
 * 短信备份
 *
 * @author WenCan
 */
public class SmsBackup {

	private boolean mStopFlag = false;
	private Object mLock = new Object();

	/**
	 * 短信备份参数
	 *
	 * @author WenCan
	 */
	public static class SmsBackupArgs {
		public Handler mHandler; // 必须字段
		public String mBackupFilePath; // 必须字段
		public boolean mNeedEncrypt;
		public String mEncryptPassword;
	}

	/**
	 * 短信备份消息
	 *
	 * @author WenCan
	 */
	public interface SmsBackupMsg {
		public static int MSG_BACKUP_START = 0x1001;
		public static int MSG_BACKUP_END = 0x1002;
		public static int MSG_BACKUP_PROCEEDING = 0x1003;
		public static int MSG_SMS_COUNT_ZERO = 0x1004;
		public static int MSG_BACKUP_ERROR_OCCUR = 0x1006;
	}

	public boolean backupSms(Context context, SmsBackupArgs args) {
		if (context == null) {
			return false;
		}

		if (!ensureArgsValid(args)) {
			return false;
		}

		backupSmsInternal(context, args);
		return true;
	}

	private boolean ensureArgsValid(SmsBackupArgs args) {
		if (args == null) {
			return false;
		}
		if (args.mHandler == null || args.mBackupFilePath == null) {
			return false;
		}
		return true;
	}

	private void backupSmsInternal(Context context, SmsBackupArgs args) {
		boolean ret = true;
		// 通知外部开始备份短信
		args.mHandler.sendEmptyMessage(SmsBackupMsg.MSG_BACKUP_START);

		// 获取短信
		Cursor cursor = getAllSmsFromDB(context);
		File file = new File(args.mBackupFilePath);
		try {
			if (cursor == null || cursor.getCount() <= 0) {
				Message.obtain(args.mHandler, SmsBackupMsg.MSG_SMS_COUNT_ZERO).sendToTarget();
				return;
			}

			// 写文件
			if (!writeSmsToDatFile(cursor, file, args.mHandler)) {
				// 写文件失败
				args.mHandler.sendEmptyMessage(SmsBackupMsg.MSG_BACKUP_ERROR_OCCUR);
				return;
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		// 按照需求，如果用户取消，则删除文件
		synchronized (mLock) {
			if (mStopFlag) {
				file.delete();
				return;
			}
		}

		// 加密
		if (args.mNeedEncrypt && args.mEncryptPassword != null) {
			File tempFile = new File(file.getParent(), "sms.temp");
			String srcFileName = file.getAbsolutePath();
			if (!Util.encryFile(file, tempFile, args.mEncryptPassword)) {
				// 删除所有文件
				tempFile.delete();
				file.delete();
				ret = false;
			} else {
				// 删除源文件
				file.delete();
				tempFile.renameTo(new File(srcFileName));
				ret = true;
			}
		}

		if (!ret) {
			args.mHandler.sendEmptyMessage(SmsBackupMsg.MSG_BACKUP_ERROR_OCCUR);
			return;
		}

		args.mHandler.sendEmptyMessage(SmsBackupMsg.MSG_BACKUP_END);
	}

	// 增加字段从这里添加
	private Cursor getAllSmsFromDB(Context context) {
		String[] projection = new String[] { Sms.TextBasedSmsColumns.THREAD_ID,
				Sms.TextBasedSmsColumns.ADDRESS, Sms.TextBasedSmsColumns.DATE,
				Sms.TextBasedSmsColumns.TYPE, Sms.TextBasedSmsColumns.LOCKED,
				Sms.TextBasedSmsColumns.BODY, Sms.TextBasedSmsColumns.READ,
				Sms.TextBasedSmsColumns.STATUS, Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT };
		String where = Sms.TextBasedSmsColumns.TYPE + " != '3'";
		return Sms.querySms(context, Sms.CONTENT_URI, projection, where, null,
				Sms.DEFAULT_SORT_ORDER);
	}

	private boolean writeSmsToDatFile(Cursor cursor, File file, Handler handler) {
		if (cursor == null || handler == null) {
			return false;
		}

		FileOutputStream fos = null;
		DataOutputStream dataoutput = null;
		boolean ret = true;
		try {
			fos = new FileOutputStream(file);
			dataoutput = new DataOutputStream(fos);

			int curSmsIndex = 0;
			int smsCount = cursor.getCount();
			if (cursor.moveToFirst()) {
				// 写入短信个数
				dataoutput.writeInt(smsCount);

				do {
					// 判断是否停止备份短信
					synchronized (mLock) {
						if (mStopFlag) {
							break;
						}
					}

					curSmsIndex++;
					// 通知当前正在备份第几条短信
					Message.obtain(handler, SmsBackupMsg.MSG_BACKUP_PROCEEDING, curSmsIndex,
							smsCount).sendToTarget();

					// 一短信短信备份所包含的字段个数
					dataoutput.writeInt(cursor.getColumnCount());

					String columnName = null;
					int columnID = 0;
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						columnName = cursor.getColumnName(i);

						if (columnName.equals(Sms.TextBasedSmsColumns.SMS_ID)) {
							columnID = Sms.SMS_ID;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.ADDRESS)) {
							columnID = Sms.SMS_ADDRESS;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.DATE)) {
							columnID = Sms.SMS_DATE;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.READ)) {
							columnID = Sms.SMS_READ;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.STATUS)) {
							columnID = Sms.SMS_STATUS;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.TYPE)) {
							columnID = Sms.SMS_TYPE;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT)) {
							columnID = Sms.SMS_REPLY_PATH_PRESENT;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.BODY)) {
							columnID = Sms.SMS_BODY;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.LOCKED)) {
							columnID = Sms.SMS_LOCK;
						} else if (columnName.equals(Sms.TextBasedSmsColumns.THREAD_ID)) {
							columnID = Sms.SMS_THREAD_ID;
						}

						String value = (cursor.getString(i) != null)
								? cursor.getString(i)
								: Sms.STR_EMPTY_STRING;
						dataoutput.writeInt(columnID);
						dataoutput.writeUTF(value);
					}
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (fos != null) {
					fos.close();
					fos = null;
				}
				if (dataoutput != null) {
					dataoutput.close();
					dataoutput = null;
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}
}
