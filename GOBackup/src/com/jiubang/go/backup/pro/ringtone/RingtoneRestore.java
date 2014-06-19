package com.jiubang.go.backup.pro.ringtone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.util.Util;

/**
 * 铃声恢复
 *
 * @author chenchangming
 */
public class RingtoneRestore {

	// public static String ALARM_DATA = "alarm_data.loc";
	// public static String NOTIFICATION_DATA = "notification_data.loc";
	// public static String RINGTONE_DATA = "ringtone_data.loc";
	// public static String ALL_DATA = "all_data.loc";
	public static final String MANAGER_DATA = "manager.loc";

	private boolean mStopFlag = false;
	private Object mLock = new Object();
	private Context mContext = null;
	private RingtoneRestoreArgs mArgs = null;

	/*
	 * private List<ThreadUpdateStruct> mRingtoneThreadUpdateStructs = null;
	 */
	public boolean restoreRingtone(Context context, RingtoneRestoreArgs args) {
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
		result = startRestoreRingtones();
		return result;
	}

	/**
	 * 铃声恢复参数
	 *
	 * @author chenchangming
	 */
	public static class RingtoneRestoreArgs {
		public Handler mHandler;
		public String mRestoreFilePath;
	}

	/*
	 * private static class ThreadUpdateStruct { public Uri uri; public long
	 * threadId; public boolean read; }
	 */
	/**
	 * 铃声恢复消息
	 *
	 * @author chenchangming
	 */
	public interface RingtoneRestoreMsg {
		public static int RINGTONE_RESTORE_START = 0x2001;
		public static int RINGTONE_RESTORE_END = 0x2002;
		public static int RINGTONE_RESTORE_PROCEEDING = 0x2003;
		public static int RINGTONE_RESTORE_USER_CANCEL = 0x2005;
		public static int RINGTONE_RESTORE_ERROR_OCCUR = 0x2006;
		public static int RINGTONE_RESTORE_FILE_NOT_EXIT = 0x2007;
	}

	public void stopRestoreRingtone() {
		synchronized (mLock) {
			mStopFlag = true;
		}
	}

	private boolean startRestoreRingtones() {
		// 通知外部准备恢复铃声
		mArgs.mHandler.sendEmptyMessage(RingtoneRestoreMsg.RINGTONE_RESTORE_START);

		File fileManager = new File(mArgs.mRestoreFilePath, MANAGER_DATA);
		String managerData = null;
		if (fileManager.exists()) {
			managerData = readFile(fileManager.getPath());
		}
		if (TextUtils.isEmpty(managerData)) {
			mArgs.mHandler.sendEmptyMessage(RingtoneRestoreMsg.RINGTONE_RESTORE_FILE_NOT_EXIT);
			return false;
		}

		JSONObject mManagerJson = null;
		try {
			mManagerJson = new JSONObject(managerData);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (mManagerJson == null) {
			return false;
		}

		boolean result = true;
		final int ringtonetypeCount = mManagerJson.length();
		int totalRestoreRingtoneCount = 0;
		int[] ringtoneType = new int[] { RingtoneManager.TYPE_RINGTONE,
				RingtoneManager.TYPE_NOTIFICATION, RingtoneManager.TYPE_ALARM /*
																				* ,
																				* RingtoneManager
																				* .
																				* TYPE_ALL
																				*/};
		final int ringtoneTypeLenght = ringtoneType.length;
		for (int i = 0; i < ringtoneTypeLenght; i++) {
			JSONObject js = mManagerJson.optJSONObject(String.valueOf(ringtoneType[i]));
			if (js == null) {
				continue;
			}

			if (restoreSingleRingtone(js, ringtoneType[i])) {
				totalRestoreRingtoneCount++;
				Message.obtain(mArgs.mHandler, RingtoneRestoreMsg.RINGTONE_RESTORE_PROCEEDING,
						totalRestoreRingtoneCount, ringtonetypeCount).sendToTarget();
			} else {
				result = false;
				break;
			}
		}

		if (!result) {
			mArgs.mHandler.sendEmptyMessage(RingtoneRestoreMsg.RINGTONE_RESTORE_ERROR_OCCUR);
		} else {
			mArgs.mHandler.sendEmptyMessage(RingtoneRestoreMsg.RINGTONE_RESTORE_END);
		}
		return result;
	}

	private Cursor queryAudio(Context context, String dataAddr) {
		if (context == null || dataAddr == null) {
			return null;
		}

		Cursor cursor = null;
		dataAddr = dataAddr.replace("'", "''");
		String where = MediaStore.MediaColumns.DATA + "='" + dataAddr + "'";
		cursor = mContext.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, null, where, null,
				null);
		if (cursor == null) {
			return null;
		}
		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	private String getRingtonePath() {
		return Util.getInternalSdPath() + "/Ringtone/";
	}

	private boolean restoreSingleRingtone(JSONObject ringtoneJson, int type) {
		String ringtoneName = ringtoneJson.optString("_display_name");
		if (TextUtils.isEmpty(ringtoneName)) {
			return false;
		}

		File ringtoneFile = new File(mArgs.mRestoreFilePath, ringtoneName
				+ RingtoneBackup.RINGTONE_SUFFIX);
		if (!ringtoneFile.exists()) {
			return false;
		}

		// 拷贝文件到指定目录，目前为/sdcard/Ringtone/下
		File ringtoneDestFile = new File(getRingtonePath(), ringtoneName);
		if (!Util.copyFile(ringtoneFile.getAbsolutePath(), ringtoneDestFile.getAbsolutePath())) {
			return false;
		}

		String dataAddr = ringtoneDestFile.getAbsolutePath();
		ContentValues cv = generateInsertContentValues(ringtoneJson);
		if (cv == null) {
			return false;
		}
		cv.put(MediaStore.Audio.Media.DATA, dataAddr);

		Uri uri = null;
		// 查询系统contentprovider是否存在该audio
		Cursor cursor = queryAudio(mContext, dataAddr);
		if (cursor != null) {
			// 存在，更新
			try {
				cursor.moveToFirst();
				String id = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
				if (mContext.getContentResolver().update(Media.EXTERNAL_CONTENT_URI, cv,
						"_id =" + id, null) > 0) {
					uri = Uri.withAppendedPath(Media.EXTERNAL_CONTENT_URI, id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			cursor.close();
			cursor = null;
		} else {
			uri = mContext.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					cv);
		}

		if (uri == null) {
			return false;
		}

		RingtoneManager.setActualDefaultRingtoneUri(mContext, type, uri);
		return true;
	}

	private String readFile(String path) {
		String data = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			String r = br.readLine();
			while (r != null) {
				data += r;
				r = br.readLine();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return data;
		}
		return data;
	}

	private ContentValues generateInsertContentValues(JSONObject json) {
		if (json == null) {
			return null;
		}
		ContentValues cv = new ContentValues();
		String[] parameters = getMediaColumnStrings();

		String value = null;
		for (String parameter : parameters) {
			try {
				value = json.getString(parameter);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (value != null) {
				cv.put(parameter, value);
			}
		}
		return cv;
	}

	public String[] getMediaColumnStrings() {
		return new String[] { MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DURATION,
				MediaStore.Audio.Media.IS_ALARM, MediaStore.Audio.Media.IS_MUSIC,
				MediaStore.Audio.Media.IS_NOTIFICATION, MediaStore.Audio.Media.IS_PODCAST,
				MediaStore.Audio.Media.IS_RINGTONE, MediaStore.Audio.Media.MIME_TYPE,
				MediaStore.Audio.Media.SIZE, MediaStore.Audio.Media.TITLE,
		// MediaStore.Audio.Media.YEAR
		};
	}
}
