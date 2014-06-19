package com.jiubang.go.backup.pro.mms;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.jiubang.go.backup.pro.mms.telephony.GenericPdu;
import com.jiubang.go.backup.pro.mms.telephony.MmsException;
import com.jiubang.go.backup.pro.mms.telephony.PduComposer;
import com.jiubang.go.backup.pro.mms.telephony.PduPersister;
import com.jiubang.go.backup.pro.mms.telephony.Telephony.BaseMmsColumns;
import com.jiubang.go.backup.pro.mms.telephony.Telephony.Mms;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author ReyZhang Mms彩信备份类
 */
public class MmsBackup {
	private static final String MMS_DIR = "MMS_Backup";

	private boolean mStopFlag = false;
	private Object mLock = new Object();
	private Context mContext = null;
	private MmsBackupArgs mArgs = null;

	public boolean backupMms(Context context, MmsBackupArgs args) {
		boolean result = true;
		if (null == context) {
			result = false;
			return result;
		}
		if (null == args || null == args.mBackupFilePath || null == args.mHandler) {
			result = false;
		}
		mContext = context;
		mArgs = args;
		backupMmsStart();
		return result;
	}

	private void backupMmsStart() {
		boolean result = true;

		// 通知外部开始备份彩信
		mArgs.mHandler.sendEmptyMessage(MmsBackupMsg.MMS_BACKUP_START);

		// 获取彩信
		Cursor cursor = mContext.getContentResolver()
				.query(Mms.CONTENT_URI, null, BaseMmsColumns.TRANSACTION_ID + " IS NOT NULL", null, null);
		if (null == cursor || cursor.getCount() == 0) {
			// 通知外部未能找到彩信数据或彩信数据为0;
			Message.obtain(mArgs.mHandler, MmsBackupMsg.MMS_BACKUP_COUNT_ZERO).sendToTarget();
			if (cursor != null) {
				cursor.close();
			}
			return;
		}

		// 创建文件夹，并写入文件
		String dirPath = mArgs.mBackupFilePath + MMS_DIR;
		if (!Util.createDir(dirPath)) {
			// 创建文件夹失败
			mArgs.mHandler.sendEmptyMessage(MmsBackupMsg.MMS_BACKUP_ERROR_OCCUR);
			return;
		}

		// 创建文件夹成功则在该文件夹中创建pdu文件
		try {
			if (!writeMmsToFiles(cursor, dirPath, mArgs.mHandler)) {
				// 创建文件失败
				mArgs.mHandler.sendEmptyMessage(MmsBackupMsg.MMS_BACKUP_ERROR_OCCUR);
				return;
			}
		} finally {
			cursor.close();
		}

		// 如果用户取消，则删除文件夹和里面的文件
		synchronized (mLock) {
			if (mStopFlag) {
				File file = new File(mArgs.mBackupFilePath);
				deleteDir(file);
				result = false;
			}
		}
		if (!result) {
			mArgs.mHandler.sendEmptyMessage(MmsBackupMsg.MMS_BACKUP_ERROR_OCCUR);
			return;
		}

		mArgs.mHandler.sendEmptyMessage(MmsBackupMsg.MMS_BACKUP_END);
	}

	/**
	 * 彩信备份参数接口
	 *
	 * @author ReyZhang
	 */
	public static class MmsBackupArgs {
		// 彩信备份handler
		public Handler mHandler;
		// 彩信备份路径
		public String mBackupFilePath;
	}

	/**
	 * 彩信备份消息接口
	 *
	 * @author ReyZhang
	 */
	public interface MmsBackupMsg {
		// 开始备份
		public static int MMS_BACKUP_START = 0x1001;
		// 备份结束
		public static int MMS_BACKUP_END = 0x1002;
		// 备份中
		public static int MMS_BACKUP_PROCEEDING = 0x1003;
		// 备份总数为0
		public static int MMS_BACKUP_COUNT_ZERO = 0x1004;
		// 备份错误
		public static int MMS_BACKUP_ERROR_OCCUR = 0x1006;
	}

	// 将彩信PDU数据写入文件夹中,每一条彩信建立一个彩信文件，以date保存
	private boolean writeMmsToFiles(Cursor cursor, String dirPath, Handler handler) {
		boolean result = true;
		if (cursor == null || cursor.getCount() == 0) {
			result = false;
			return result;
		}
		if (!cursor.moveToFirst()) {
			cursor.close();
			return false;
		}
		
		final int mmsCount = cursor.getCount();
		for (int i = 0; i < mmsCount; i++) {
			// 判断是否停止备份彩信
			synchronized (mLock) {
				if (mStopFlag) {
					break;
				}
			}
			
			// 通知当前备份第几条彩信
			Message.obtain(handler, MmsBackupMsg.MMS_BACKUP_PROCEEDING, i + 1, mmsCount)
				.sendToTarget();
			
			// 用PduPersister load出pdu对象，转化成字节流，存入以date命名的pdu文件当中
			int id = cursor.getInt(cursor.getColumnIndex(BaseMmsColumns._ID));
			int date = cursor.getInt(cursor.getColumnIndex(BaseMmsColumns.DATE));
			int read = cursor.getInt(cursor.getColumnIndex(BaseMmsColumns.READ));
			int lock = cursor.getInt(cursor.getColumnIndex(BaseMmsColumns.LOCKED));
			int messageType = cursor.getInt(cursor.getColumnIndex(BaseMmsColumns.MESSAGE_BOX));
			String msgBoxInd = null;
			if (messageType == BaseMmsColumns.MESSAGE_BOX_SENT) {
				msgBoxInd = "S_";
			} else if (messageType == BaseMmsColumns.MESSAGE_BOX_DRAFTS) {
				msgBoxInd = "D_";
			} else {
				msgBoxInd = "I_";
			}
			
//			File file = createFile(dirPath + "/" + msgBoxInd + date + "(" + read + ")" + "["
//					+ lock + "]" + ".pdu");
			File file = new File(dirPath, msgBoxInd + date + "(" + read + ")" + "["
					+ lock + "]" + ".pdu");
			PduPersister pduPersister = PduPersister.getPduPersister(mContext
					.getApplicationContext());
			Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, id);
			// 通过load URI地址得到一个pdu对象
			GenericPdu gPdu = null;
			try {
				gPdu = pduPersister.load(uri);
			} catch (MmsException e) {
				e.printStackTrace();
				continue;
			}
			
			PduComposer pc = new PduComposer(mContext.getApplicationContext(), gPdu);
			try {
				byte[] pduBytes = pc.make();
				if (pduBytes != null && pduBytes.length > 0) {
					try {
						OutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
						try {
							bos.write(pduBytes);
						} finally {
							bos.close();
						}
					} catch (IOException e) {
					}
				}
			} finally {
				pc.release();
			}
			cursor.moveToNext();
		}

		return result;
	}


	// 创建pdu文件
	private File createFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return file;
		}
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	// 删除文件夹
	private void deleteDir(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				// 声明目录下所以文件
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					this.deleteDir(files[i]);
				}
			}
			file.delete();
		}
	}

	public static int getMmsCount(Context context) {
		int totalMmsCount = 0;
		Cursor cursor = context.getContentResolver().query(Mms.CONTENT_URI, null, null, null, null);
		if (cursor != null) {
			totalMmsCount = cursor.getCount();
			cursor.close();
		}
		return totalMmsCount;
	}

}
