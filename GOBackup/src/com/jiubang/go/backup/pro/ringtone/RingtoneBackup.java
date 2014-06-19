package com.jiubang.go.backup.pro.ringtone;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.jiubang.go.backup.pro.util.Util;

/**
 * 铃声备份
 *
 * @author chenchangming
 */
public class RingtoneBackup {

	// public static String ALARM_DATA = "alarm_data.loc";
	// public static String NOTIFICATION_DATA = "notification_data.loc";
	// public static String RINGTONE_DATA = "ringtone_data.loc";
	// public static String ALL_DATA = "all_data.loc";
	public static String MANAGER_DATA = "manager.loc";
	public static String RINGTONE_SUFFIX = ".ring";

	private Context mContext = null;
	private RingtoneBackupArgs mArgs = null;
	// private List<File> mRingtoneFiles;
	private int mRingtoneCount = 0;
	private Map<String, File> mAllRingtoneFile;

	public boolean backupRingtone(Context context, RingtoneBackupArgs args) {
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
		// mRingtoneFiles = new ArrayList<File>();
		mAllRingtoneFile = new HashMap<String, File>();
		result = startBackupRingtones();
		if (!result) {
			// 删除铃声文件
			deleteRingtoneFileWhenBackupFailed();
		}
		return result;
	}

	public int getRingtoneCount() {
		return mRingtoneCount;
	}

	public Map<String, File> getRingtoneFiles() {
		return mAllRingtoneFile;
	}

	private void deleteRingtoneFileWhenBackupFailed() {
		if (mArgs == null || mArgs.mBackupFilePath == null) {
			return;
		}
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.getName().equals(RingtoneBackup.RINGTONE_SUFFIX)) {
					return true;
				}
				return false;
			}
		};

		File backupDir = new File(mArgs.mBackupFilePath);
		File[] files = backupDir.listFiles(filter);
		if (files != null) {
			for (File file : files) {
				Util.deleteFile(file.getAbsolutePath());
			}
		}

		File managerFile = new File(mArgs.mBackupFilePath, RingtoneBackup.MANAGER_DATA);
		managerFile.delete();
	}

	private boolean backupSimpleRintones(int type, RingtoneDataController controller,
			JSONObject manager) {
		Uri uri = RingtoneManager.getActualDefaultRingtoneUri(mContext, type);
		if (uri == null) {
			return false;
		}

		JSONObject json = controller.getRingtoneJson(uri);
		String dataLoc = controller.getData();
		if (json == null || dataLoc == null) {
			return false;
		}

		File mRingtoneFile = new File(dataLoc);
		if (!mRingtoneFile.exists()) {
			// 文件不存在，当作成功
			return false;
		}

		String fileName = mRingtoneFile.getName();
		File newFile = new File(mArgs.mBackupFilePath, fileName + RINGTONE_SUFFIX);
		if (!hasBackupTheSameFile(newFile)) {
			if (!Util.copyFile(mRingtoneFile.getPath(), newFile.getPath())) {
				return false;
			}
		}

		try {
			manager.put(String.valueOf(type), json);
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		mAllRingtoneFile.put(String.valueOf(type), newFile);
		return true;
	}

	private boolean hasBackupTheSameFile(File file) {
		if (mAllRingtoneFile == null) {
			return false;
		}
		Collection<File> allFiles = mAllRingtoneFile.values();
		if (allFiles.contains(file)) {
			return true;
		}
		return false;
	}

	private boolean startBackupRingtones() {
		// 通知外部开始备份铃声
		mArgs.mHandler.sendEmptyMessage(RingtoneBackupMsg.RINGTONE_BACKUP_START);

		boolean ret = true;
		JSONObject managerJson = new JSONObject();
		RingtoneDataController controller = RingtoneDataController.getInstance(mContext);

		// 不需要备份All类型
		int[] ringType = new int[] { RingtoneManager.TYPE_ALARM, RingtoneManager.TYPE_NOTIFICATION,
				RingtoneManager.TYPE_RINGTONE /* , RingtoneManager.TYPE_ALL */};
		final int ringTypeLenght = ringType.length;
		for (int i = 0; i < ringTypeLenght; i++) {
			if (backupSimpleRintones(ringType[i], controller, managerJson)) {
				// 成功
				mRingtoneCount++;
				Message.obtain(mArgs.mHandler, RingtoneBackupMsg.RINGTONE_BACKUP_PROCEEDING, i + 1,
						ringTypeLenght).sendToTarget();
			}
		}
		File managerFile = new File(mArgs.mBackupFilePath, MANAGER_DATA);
		saveFile(managerFile, managerJson.toString());
		mAllRingtoneFile.put(MANAGER_DATA, managerFile);

		if (mRingtoneCount <= 0) {
			ret = false;
		}

		if (!ret) {
			mArgs.mHandler.sendEmptyMessage(RingtoneBackupMsg.RINGTONE_BACKUP_ERROR_OCCUR);
		} else {
			mArgs.mHandler.sendEmptyMessage(RingtoneBackupMsg.RINGTONE_BACKUP_END);
		}
		return ret;
	}

	/**
	 * 铃声备份参数
	 *
	 * @author chenchangming
	 */
	public static class RingtoneBackupArgs {
		// 铃声备份handler
		public Handler mHandler;
		// 铃声备份路径
		public String mBackupFilePath;
	}

	/**
	 * 铃声备份消息
	 *
	 * @author chenchangming
	 */
	public interface RingtoneBackupMsg {
		// 开始备份
		public static int RINGTONE_BACKUP_START = 0x1001;
		// 备份结束
		public static int RINGTONE_BACKUP_END = 0x1002;
		// 备份中
		public static int RINGTONE_BACKUP_PROCEEDING = 0x1003;
		// 备份错误
		public static int RINGTONE_BACKUP_ERROR_OCCUR = 0x1004;
	}

	private boolean saveFile(File file, String data) {
		boolean result = false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(data.getBytes("utf-8"));
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
