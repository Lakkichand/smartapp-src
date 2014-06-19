package com.jiubang.go.backup.pro.sms;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneNumberUtils;
import android.util.Xml;

import com.jiubang.go.backup.pro.mergerecord.NewSmsBatchMergeAction.NewSmsStruct;
import com.jiubang.go.backup.pro.mms.telephony.Telephony.Threads;
import com.jiubang.go.backup.pro.sms.Sms.SmsStruct;
import com.jiubang.go.backup.pro.sms.Sms.ThreadsInfo;
import com.jiubang.go.backup.pro.util.EncryptDecrypt;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 彩信恢复
 *
 * @author WenCan
 */
public class SmsRestore {
	public static final String TEMP_FILE_NAME = "sms.temp";
	private static final String FINAL_TEMP_FILE_NAME = "finalsms.temp";

	private boolean mStopFlag = false;
	private Object mLock = new Object();
	private List<String> mAddressList = null;

	/**
	 * 彩信恢复参数
	 *
	 * @author WenCan
	 */
	public static class SmsRestoreArgs {
		public Handler mHandler;
		public String mRestoreFilePath;
		public boolean mNeedDecrypte;
		public String mDecryptPassword;
	}

	/**
	 * 彩信恢复消息
	 *
	 * @author WenCan
	 */
	public interface SmsRestoreMsg {
		public static int MSG_RESTORE_START = 0x2001;
		public static int MSG_RESTORE_END = 0x2002;
		public static int MSG_RESTORE_PROCEEDING = 0x2003;
		public static int MSG_RESTORE_SMS_COUNT_ZERO = 0x2004;
		public static int MSG_RESTORE_USER_CANCEL = 0x2005;
		public static int MSG_RESTORE_ERROR_OCCUR = 0x2006;
		public static int MSG_RESTORE_FILE_NOT_EXIT = 0x2007;
		public static int MSG_RESTORE_START_UPDATE_CONVERSATION = 0x2008;
		public static int MSG_RESTORE_UPDATING_CONVERSATION = 0x2009;
		public static int MSG_RESTORE_UPDAGE_CONVERSATION_FINISH = 0x2010;
	}

	private boolean ensureArgsValid(SmsRestoreArgs args) {
		if (args == null) {
			return false;
		}
		if (args.mHandler == null || args.mRestoreFilePath == null) {
			return false;
		}
		return true;
	}

	public boolean restoreSms(Context context, SmsRestoreArgs args) {
		if (context == null) {
			return false;
		}
		if (!ensureArgsValid(args)) {
			return false;
		}

		restoreSmsInternal(context, args);
		return true;
	}

	public void stopRestoreSms() {
		synchronized (mLock) {
			mStopFlag = true;
		}
	}

	private void restoreSmsInternal(Context context, SmsRestoreArgs args) {
		// 通知开始恢复短信
		args.mHandler.sendEmptyMessage(SmsRestoreMsg.MSG_RESTORE_START);
		// 判断文件是否存在
		File file = new File(args.mRestoreFilePath);
		if (!file.exists()) {
			// 文件不存在
			args.mHandler.sendEmptyMessage(SmsRestoreMsg.MSG_RESTORE_FILE_NOT_EXIT);
			return;
		}
		if (file.length() == 0) {
			args.mHandler.sendEmptyMessage(SmsRestoreMsg.MSG_RESTORE_SMS_COUNT_ZERO);
			return;
		}

		// 解密
		File decryptFile = null;
		if (args.mNeedDecrypte) {
			decryptFile = new File(file.getParent(), "sms.temp");
			if (!Util.decryptFile(file, decryptFile, args.mDecryptPassword)) {
				// 解密失败
				decryptFile.delete();
				args.mHandler.sendEmptyMessage(SmsRestoreMsg.MSG_RESTORE_ERROR_OCCUR);
				return;
			}
			file = decryptFile;
		}

		// 恢复
		boolean ret = false;
		ret = readDatFileAndInsertToDb(context, file, args.mHandler);

		// 更新会话
		ret = updateConversation(context, args.mHandler);

		// 删除临时文件
		if (decryptFile != null) {
			decryptFile.delete();
			decryptFile = null;
		}

		if (!ret) {
			args.mHandler.sendEmptyMessage(SmsRestoreMsg.MSG_RESTORE_ERROR_OCCUR);
		} else {
			args.mHandler.sendEmptyMessage(SmsRestoreMsg.MSG_RESTORE_END);
		}
	}

	private boolean updateConversation(Context context, Handler handler) {
		if (mAddressList == null || mAddressList.size() <= 0) {
			return true;
		}

		Message.obtain(handler, SmsRestoreMsg.MSG_RESTORE_START_UPDATE_CONVERSATION).sendToTarget();
		int index = 0;
		final int length = mAddressList.size();
		for (String item : mAddressList) {
			// 通知外部正在更新第几个会话
			index++;
			Message.obtain(handler, SmsRestoreMsg.MSG_RESTORE_UPDATING_CONVERSATION, index, length)
					.sendToTarget();

			ContentValues tempvalue = new ContentValues();
			tempvalue.put(Sms.TextBasedSmsColumns.ADDRESS, item);
			tempvalue.put("type", "1");
			tempvalue.put("read", "1");
			tempvalue.put("body", "Smsbackup Temp Message, delete it please");
			long time = System.currentTimeMillis();
			tempvalue.put("date", time);

			Uri tempUri = Sms.insertSms(context, Sms.CONTENT_URI, tempvalue);
			if (tempUri != null) {
				Sms.deleteSms(context, tempUri, null, null);
			}
		}
		Message.obtain(handler, SmsRestoreMsg.MSG_RESTORE_UPDAGE_CONVERSATION_FINISH)
				.sendToTarget();

		if (mAddressList != null) {
			mAddressList.clear();
			mAddressList = null;
		}
		return true;
	}

	public static boolean getSmsDatFile(File srcFile, File desFile, boolean encrypt, String password) {
		if (srcFile == null || desFile == null) {
			return false;
		}

		if (!srcFile.exists()) {
			return false;
		}

		boolean ret = false;
		if (!encrypt) {
			ret = Util.copyFile(srcFile.getAbsolutePath(), desFile.getAbsolutePath());
		} else {
			ret = Util.decryptFile(srcFile, desFile, password);
		}
		return ret;
	}

	public static Set<SmsStruct> loadSmsFromDatFile(String path, boolean encrypt, String password) {
		if (path == null) {
			return null;
		}

		Set<SmsStruct> allSms = null;
		File originalFile = new File(path);
		File tempFile = new File(originalFile.getParent(), TEMP_FILE_NAME);
		if (getSmsDatFile(originalFile, tempFile, encrypt, password)) {
			FileInputStream fis = null;
			DataInputStream dis = null;

			try {
				fis = new FileInputStream(tempFile);
				dis = new DataInputStream(fis);

				// 文件中短信总个数
				int smsCount = dis.readInt();
				for (int i = 0; i < smsCount; i++) {
					SmsStruct sms = new SmsStruct();

					int columncount = dis.readInt();
					for (int j = 0; j < columncount; j++) {
						int columnName = dis.readInt();
						String colvalue = dis.readUTF();

						String column = null;
						switch (columnName) {
							case Sms.SMS_ID :
								column = Sms.TextBasedSmsColumns.SMS_ID;
								break;
							case Sms.SMS_ADDRESS :
								column = Sms.TextBasedSmsColumns.ADDRESS;
								break;
							case Sms.SMS_DATE :
								column = Sms.TextBasedSmsColumns.DATE;
								break;
							case Sms.SMS_READ :
								column = Sms.TextBasedSmsColumns.READ;
								break;
							case Sms.SMS_STATUS :
								column = Sms.TextBasedSmsColumns.STATUS;
								break;
							case Sms.SMS_TYPE :
								column = Sms.TextBasedSmsColumns.TYPE;
								break;
							case Sms.SMS_REPLY_PATH_PRESENT :
								column = Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT;
								break;
							case Sms.SMS_BODY :
								column = Sms.TextBasedSmsColumns.BODY;
								break;
							case Sms.SMS_LOCK :
								column = Sms.TextBasedSmsColumns.LOCKED;
								break;
							case Sms.SMS_THREAD_ID :
								//								column = Sms.TextBasedSmsColumns.THREAD_ID;
								break;
							default :
								break;
						}
						if (column == null) {
							continue;
						} else {
							sms.putFiled(column, colvalue);
						}
					}
					// end for( int j=0; j<columncount; j++ )

					if (allSms == null) {
						allSms = new HashSet<Sms.SmsStruct>();
					}
					if (!allSms.contains(sms)) {
						allSms.add(sms);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// 删除临时文件
				if (tempFile != null && tempFile.exists()) {
					tempFile.delete();
				}
				try {
					if (fis != null) {
						fis.close();
						fis = null;
					}
					if (dis != null) {
						dis.close();
						dis = null;
					}
				} catch (Exception e) {

				}
			}
		}
		return allSms;
	}

	public static Set<SmsStruct> loadAllSmsSetFromTempFile(File destFile, File tempFile, int count) {
		if (!tempFile.exists()) {
			return null;
		}
		TreeSet<SmsStruct> allSms = null;
		FileInputStream fis = null;
		DataInputStream dis = null;
		try {
			fis = new FileInputStream(tempFile);
			dis = new DataInputStream(fis);
			if (destFile.exists()) {
				int unavaliable = dis.readInt();
			}
			// 文件中短信总个数
			int smsCount = count;
			for (int i = 0; i < smsCount; i++) {
				SmsStruct sms = new SmsStruct();

				int columncount = dis.readInt();
				for (int j = 0; j < columncount; j++) {
					int columnName = dis.readInt();
					String colvalue = dis.readUTF();

					String column = null;
					switch (columnName) {
						case Sms.SMS_ID :
							column = Sms.TextBasedSmsColumns.SMS_ID;
							break;
						case Sms.SMS_ADDRESS :
							column = Sms.TextBasedSmsColumns.ADDRESS;
							break;
						case Sms.SMS_DATE :
							column = Sms.TextBasedSmsColumns.DATE;
							break;
						case Sms.SMS_READ :
							column = Sms.TextBasedSmsColumns.READ;
							break;
						case Sms.SMS_STATUS :
							column = Sms.TextBasedSmsColumns.STATUS;
							break;
						case Sms.SMS_TYPE :
							column = Sms.TextBasedSmsColumns.TYPE;
							break;
						case Sms.SMS_REPLY_PATH_PRESENT :
							column = Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT;
							break;
						case Sms.SMS_BODY :
							column = Sms.TextBasedSmsColumns.BODY;
							break;
						case Sms.SMS_LOCK :
							column = Sms.TextBasedSmsColumns.LOCKED;
							break;
						case Sms.SMS_THREAD_ID :
							//								column = Sms.TextBasedSmsColumns.THREAD_ID;
							break;
						default :
							break;
					}
					if (column == null) {
						continue;
					} else {
						sms.putFiled(column, colvalue);
					}
				}
				if (allSms == null) {
					allSms = new TreeSet<SmsStruct>(new SmsStruct.SmsComparator());
				}
				if (!allSms.contains(sms)) {
					allSms.add(sms);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 删除临时文件
			tempFile.delete();
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
				if (dis != null) {
					dis.close();
					dis = null;
				}
			} catch (Exception e) {

			}
		}
		return allSms;
	}

	public static Set<NewSmsStruct> firstloadSmsFromDatFile(String path, String rootCachePath,
			boolean encrypt, String password) {
		if (path == null) {
			return null;
		}
		Set<NewSmsStruct> allSms = new HashSet<NewSmsStruct>();;
		File originalFile = new File(path);
		if (!originalFile.exists()) {
			return null;
		}
		File tempFile = new File(rootCachePath, FINAL_TEMP_FILE_NAME);
		if (getSmsDatFile(originalFile, tempFile, encrypt, password)) {
			FileInputStream fis = null;
			DataInputStream dis = null;
			try {
				fis = new FileInputStream(tempFile);
				dis = new DataInputStream(fis);

				// 文件中短信总个数
				int smsCount = dis.readInt();
				for (int i = 0; i < smsCount; i++) {
					NewSmsStruct sms = new NewSmsStruct();
					int columncount = dis.readInt();
					for (int j = 0; j < columncount; j++) {
						int columnName = dis.readInt();
						String colvalue = dis.readUTF();
						switch (columnName) {
							case Sms.SMS_ADDRESS :
								sms.mAdress = colvalue;
								break;
							case Sms.SMS_DATE :
								sms.mDate = colvalue;
								break;
							default :
								break;
						}
					}
					if (!allSms.contains(sms)) {
						allSms.add(sms);
					}
				}
			} catch (Exception e) {
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
				} catch (Exception e) {

				}
			}
		}
		return allSms;
	}

	/**
	 * 按照dat方式恢复短信
	 *
	 * @param context
	 * @param file
	 * @param handler
	 * @return
	 */
	private boolean readDatFileAndInsertToDb(Context context, File file, Handler handler) {
		FileInputStream fis = null;
		DataInputStream dis = null;
		boolean ret = true;
		final int m7 = 7;
		// 查询系统已有短信，用于在内存匹配短信是否存在
		ArrayList<HashMap<String, String>> allExitSms = queryAllSmsWithSpecialField(context);
		//		ArrayList<HashMap<String, String>> allExitSms = null;

		List<ThreadsInfo> threadsInfo = getThreadsInfo(context);
		if (threadsInfo == null) {
			threadsInfo = new ArrayList<Sms.ThreadsInfo>();
		}

		if (mAddressList != null) {
			mAddressList.clear();
		} else {
			mAddressList = new ArrayList<String>();
		}

		try {
			fis = new FileInputStream(file);
			dis = new DataInputStream(fis);

			ContentValues cv = new ContentValues();

			// 文件中短信总个数
			int smsCount = dis.readInt();
			for (int i = 0; i < smsCount; i++) {
				// 判断是否停止恢复
				synchronized (mLock) {
					if (mStopFlag) {
						break;
					}
				}
				// 通知当前恢复第几个
				Message.obtain(handler, SmsRestoreMsg.MSG_RESTORE_PROCEEDING, i + 1, smsCount)
						.sendToTarget();

				cv.clear();
				int columncount = dis.readInt();

				for (int j = 0; j < columncount; j++) {
					int columnName = dis.readInt();
					String colvalue = dis.readUTF();

					String column = null;
					switch (columnName) {
						case Sms.SMS_ID :
							column = Sms.TextBasedSmsColumns.SMS_ID;
							break;
						case Sms.SMS_ADDRESS :
							column = Sms.TextBasedSmsColumns.ADDRESS;
							break;
						case Sms.SMS_DATE :
							column = Sms.TextBasedSmsColumns.DATE;
							break;
						case Sms.SMS_READ :
							column = Sms.TextBasedSmsColumns.READ;
							break;
						case Sms.SMS_STATUS :
							column = Sms.TextBasedSmsColumns.STATUS;
							break;
						case Sms.SMS_TYPE :
							column = Sms.TextBasedSmsColumns.TYPE;
							break;
						case Sms.SMS_REPLY_PATH_PRESENT :
							column = Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT;
							break;
						case Sms.SMS_BODY :
							column = Sms.TextBasedSmsColumns.BODY;
							break;
						case Sms.SMS_LOCK :
							column = Sms.TextBasedSmsColumns.LOCKED;
							break;
						case Sms.SMS_THREAD_ID :
							//							column = Sms.TextBasedSmsColumns.THREAD_ID;
							break;
						default :
							break;
					}
					if (column == null) {
						continue;
					} else {
						cv.put(column, colvalue);
					}
				}
				// end for( int j=0; j<columncount; j++ )

				String address = cv.getAsString(Sms.TextBasedSmsColumns.ADDRESS);
				ThreadsInfo info = getThreadId(threadsInfo, address);
				if (info == null && Build.VERSION.SDK_INT > m7) {
					// Log.d("GOBackup", "isThreadIdExist == false, address = "
					// + address/* + ", threadId = " + threadId*/);
					long thread = Threads.getOrCreateThreadId(context, address);
					info = new ThreadsInfo();
					info.mAddress = address;
					info.mThreadId = String.valueOf(thread);
					threadsInfo.add(info);
				}

				if (info != null) {
					cv.put(Sms.TextBasedSmsColumns.THREAD_ID, info.mThreadId);
				}

				if (!isSmsHasExistInDb(allExitSms, cv)) {
					Sms.insertSms(context, Sms.CONTENT_URI, cv);

					if (!mAddressList.contains(address)) {
						mAddressList.add(address);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
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
				if (allExitSms != null) {
					allExitSms.clear();
					allExitSms = null;
				}
				if (threadsInfo != null) {
					threadsInfo.clear();
					threadsInfo = null;
				}
			} catch (Exception e) {

			}
		}
		return ret;
	}

	private ThreadsInfo getThreadId(List<ThreadsInfo> threads, String address/*
																				* ,
																				* String
																				* threadId
																				*/) {
		if (threads == null || address == null /* || threadId == null */) {
			return null;
		}

		for (ThreadsInfo item : threads) {
			if (item.mAddress == null) {
				continue;
			}

			// Log.d("GOBackup", "item.address = " + item.address +
			// ", item.threadid = " + item.threadId + ", address = " + address +
			// ", threadid = " + threadId);
			if (PhoneNumberUtils.compare(item.mAddress, address) /*
																	* &&
																	* item.threadId
																	* .
																	* equals(threadId
																	* )
																	*/) {
				return item;
			}
		}
		return null;
	}

	private List<ThreadsInfo> getThreadsInfo(Context context) {
		Cursor cursor = context.getContentResolver().query(Sms.CONTENT_URI, Sms.THREADS_PROJECTION,
				null, null, "thread_id");
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() <= 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		List<ThreadsInfo> result = new ArrayList<Sms.ThreadsInfo>();
		try {
			do {
				try {
					ThreadsInfo info = new ThreadsInfo();
					info.mThreadId = cursor.getString(cursor.getColumnIndex("thread_id"));
					info.mAddress = cursor.getString(cursor.getColumnIndex("address"));
					if (info.mAddress == null) {
						continue;
					}

					if (!result.contains(info)) {
						result.add(info);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		return !Util.isCollectionEmpty(result) ? result : null;
	}

	/**
	 * 判断短信是否存在
	 *
	 * @param smsInDb
	 * @param cv
	 * @return
	 */
	private boolean isSmsHasExistInDb(ArrayList<HashMap<String, String>> smsInDb, ContentValues cv) {
		if (smsInDb == null || smsInDb.size() < 1) {
			return false;
		}
		if (cv == null) {
			return false;
		}

		String cvDate = null;
		String cvAddress = null;
		String address = null;
		String date = null;
		String threadId = null;
		boolean ret = false;

		cvDate = cv.getAsString(Sms.TextBasedSmsColumns.DATE);
		cvAddress = cv.getAsString(Sms.TextBasedSmsColumns.ADDRESS);
		if (cvDate == null || cvAddress == null) {
			return false;
		}

		for (HashMap<String, String> item : smsInDb) {
			date = item.get(Sms.TextBasedSmsColumns.DATE);
			address = item.get(Sms.TextBasedSmsColumns.ADDRESS);
			threadId = item.get(Sms.TextBasedSmsColumns.THREAD_ID);

			if (date != null && address != null && threadId != null) {
				if (date.equals(cvDate)
						&& (address.equals(cvAddress) || PhoneNumberUtils.compare(address,
								cvAddress)) && threadId != null) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * 查询所有短信的特殊字段，用于匹配短信是否已存在
	 *
	 * @param context
	 * @return
	 */
	private ArrayList<HashMap<String, String>> queryAllSmsWithSpecialField(Context context) {
		Cursor cursor = null;
		String[] projection = new String[] { Sms.TextBasedSmsColumns.SMS_ID,
				Sms.TextBasedSmsColumns.DATE, Sms.TextBasedSmsColumns.ADDRESS,
				Sms.TextBasedSmsColumns.THREAD_ID };
		Uri url = Uri.parse("content://mms-sms/complete-conversations");
		String where = "thread_id != 0";
		cursor = Sms.querySms(context, url, projection, where, null, Sms.DEFAULT_SORT_ORDER);
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() == 0 || !cursor.moveToFirst()) {
			cursor.close();
			return null;
		}

		ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		try {
			String date = null;
			String address = null;
			String threadID = null;
			do {
				try {
					date = cursor.getString(cursor.getColumnIndex(Sms.TextBasedSmsColumns.DATE));
					address = cursor.getString(cursor
							.getColumnIndex(Sms.TextBasedSmsColumns.ADDRESS));
					threadID = cursor.getString(cursor
							.getColumnIndex(Sms.TextBasedSmsColumns.THREAD_ID));

					HashMap<String, String> item = new HashMap<String, String>();
					item.put(Sms.TextBasedSmsColumns.DATE, date);
					item.put(Sms.TextBasedSmsColumns.ADDRESS, address);
					item.put(Sms.TextBasedSmsColumns.THREAD_ID, threadID);
					result.add(item);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} while (cursor.moveToNext());
		} finally {
			cursor.close();
		}
		return result;
	}

	/**
	 * 导出短信dat文件到xml文件
	 *
	 * @param sourceFilePath
	 *            dat文件路径
	 * @param desFilePath
	 *            目的路径
	 * @param password
	 *            dat文件的加密密码
	 * @return
	 */
	public static boolean exportDatFileToSd(String sourceFilePath, String desFilePath,
			String password) {
		if (sourceFilePath == null || desFilePath == null) {
			return false;
		}
		boolean ret = false;
		boolean encrypt = password == null ? false : true;
		File sourceFile = new File(sourceFilePath);
		if (!sourceFile.exists()) {
			return false;
		}

		File tempDecryptFile = null;
		if (encrypt) {
			// 解密
			String curDir = sourceFile.getParent();
			if (!curDir.endsWith(File.separator)) {
				curDir += File.separator;
			}
			// 创建临时解密文件
			tempDecryptFile = new File(curDir + "sms.temp");
			EncryptDecrypt ed = new EncryptDecrypt();
			if (!ed.decrypt(sourceFile, tempDecryptFile, password)) {
				// 解密失败
				// if(tempDecryptFile.exists()){
				// tempDecryptFile.delete();
				// }
				// return false;
				// 修复1.0版本由于在备份短信恢复短信的加密之短代码失误，加密字段设置为false引起的bug（1.0版本的短信是没有加密的）
				encrypt = false;
			}
		}

		File desFile = new File(desFilePath);
		if (encrypt && tempDecryptFile != null) {
			ret = dat2Xml(tempDecryptFile, desFile);
		} else {
			ret = dat2Xml(sourceFile, desFile);
		}
		// 删除缓存文件
		if (tempDecryptFile != null && tempDecryptFile.exists()) {
			tempDecryptFile.delete();
		}
		return ret;
	}

	private static boolean dat2Xml(File datFile, File xmlFile) {
		if (datFile == null || xmlFile == null) {
			return false;
		}
		FileInputStream inTrace = null;
		DataInputStream datainput = null;

		XmlSerializer serializer = null;
		FileOutputStream outPut = null;

		boolean ret = true;
		try {
			inTrace = new FileInputStream(datFile);
			datainput = new DataInputStream(inTrace);
			outPut = new FileOutputStream(xmlFile);
			serializer = Xml.newSerializer();
			serializer.setOutput(outPut, "UTF-8");

			int smscount = datainput.readInt();

			// 开始document
			serializer.startDocument(Sms.STR_FONTCODE_UTF8, true);
			serializer.startTag(Sms.STR_EMPTY_STRING, Sms.SmsXmlTag.GOSMS);

			// 记录短信数量
			serializer.startTag(Sms.STR_EMPTY_STRING, Sms.SmsXmlTag.COUNT);
			serializer.text(smscount + Sms.STR_EMPTY_STRING);
			serializer.endTag(Sms.STR_EMPTY_STRING, Sms.SmsXmlTag.COUNT);

			for (int i = 0; i < smscount; i++) {
				serializer.startTag(Sms.STR_EMPTY_STRING, Sms.SmsXmlTag.SMS);
				int columncount = datainput.readInt();
				for (int j = 0; j < columncount; j++) {
					int columnNameId = datainput.readInt();
					String colvalue = datainput.readUTF();
					String columnName = null;
					switch (columnNameId) {
						case Sms.SMS_ID :
							columnName = Sms.TextBasedSmsColumns.SMS_ID;
							break;
						case Sms.SMS_ADDRESS :
							columnName = Sms.TextBasedSmsColumns.ADDRESS;
							break;
						case Sms.SMS_DATE :
							columnName = Sms.TextBasedSmsColumns.DATE;
							break;
						case Sms.SMS_READ :
							columnName = Sms.TextBasedSmsColumns.READ;
							break;
						case Sms.SMS_STATUS :
							columnName = Sms.TextBasedSmsColumns.STATUS;
							break;
						case Sms.SMS_TYPE :
							columnName = Sms.TextBasedSmsColumns.TYPE;
							break;
						case Sms.SMS_REPLY_PATH_PRESENT :
							columnName = Sms.TextBasedSmsColumns.REPLY_PATH_PRESENT;
							break;
						case Sms.SMS_BODY :
							columnName = Sms.TextBasedSmsColumns.BODY;
							break;
						case Sms.SMS_LOCK :
							columnName = Sms.TextBasedSmsColumns.LOCKED;
							break;
						case Sms.SMS_THREAD_ID :
							columnName = Sms.TextBasedSmsColumns.THREAD_ID;
							break;
					}

					serializer.startTag(Sms.STR_EMPTY_STRING, columnName);
					try {
						serializer.text(colvalue);
					} catch (IllegalArgumentException e) {
						// 短信内容中有XML关键字
						serializer.text(Sms.STR_EMPTY_STRING);
					}
					serializer.endTag(Sms.STR_EMPTY_STRING, columnName);
				}
				// end for(j)
				serializer.endTag(Sms.STR_EMPTY_STRING, Sms.SmsXmlTag.SMS);
			}
			// end for(i)

			serializer.endTag(Sms.STR_EMPTY_STRING, Sms.SmsXmlTag.GOSMS);
			serializer.endDocument();
			ret = true;
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (inTrace != null) {
					inTrace.close();
					inTrace = null;
				}
				if (datainput != null) {
					datainput.close();
					datainput = null;
				}
				if (outPut != null) {
					outPut.close();
					outPut = null;
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}
}
