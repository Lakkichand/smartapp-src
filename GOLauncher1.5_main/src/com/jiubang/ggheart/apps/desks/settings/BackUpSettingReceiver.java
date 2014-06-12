package com.jiubang.ggheart.apps.desks.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;

import com.jiubang.ggheart.apps.desks.net.CryptTool;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class BackUpSettingReceiver extends BroadcastReceiver {

	private Context mContext;
	private static final int ENCRYPTBYTE = 10; // 加密字节长度
	private static final String ANDROIDHEART_FILENAME = "androidheart.db";

	// 恢复桌面
	private final String RESTORE_DB = "com.jiubang.goback.restore_db";
	// 备份桌面
	private final String BACKUP_DB = "com.jiubang.goback.backup_db";
	// 无法完成备份
	private final String EXPORT_ERROR = "com.jiubang.goback.export_error";
	// 备份成功
	private final String EXPORT_SUCCESS = "com.jiubang.goback.export_success";
	// 恢复备份成功
	private final String DFILE_IMPORT_SUCCESS = "com.jiubang.goback.dbfile_import_success";
	// 无法恢复备份
	private final String DFILE_IMPORT_ERROR = "com.jiubang.goback.dbfile_import_error";
	// 无法读取SD卡
	private final String SDCARD_UNMOUNTED = "com.jiubang.goback.sdcard_unmounted";
	// 数据库内容不存在
	private final String DATABASE_NOT_EXIT = "com.jiubang.goback.databasenoexit";
	// 无法找到备份文件
	private final String DBFILE_NOT_FOUND = "com.jiubang.goback.db_not_found";
	// 无法读取备份文件
	private final String DBFILE_NOT_READABLE = "com.jiubang.goback.db_not_readable";
	// 备份/恢复命令
	private final String BACKCMD = "com.jiubang.goback.backCMD";
	// 备份路径
	private final String BACKPATH = "com.jiubang.goback.backPath";
	// 随机字符串
	private final String RANDOMSTR = "com.jiubang.goback.randomStr";
	// 返回的备份信息
	private final String BACKINFO = "com.jiubang.goback.backInfo";
	// base64算法的key值
	private final String packagekey = "com.gau.go.launcherex";
	// 随机生成的字符串
	private String randomsrc = null;
	// 从GO备份返回的字符串
	private String reStoresrc = null;
	// GO备份广播的action
//	public static final String ACTION_RESTORE = "com.jiubang.go.backup.ACTION_RESTORE_GOLAUNCHER_FINISH";
//	public static final String ACTION_BACKUP = "com.jiubang.go.backup.ACTION_BACKUP_GOLAUNCHER_FINISH";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null) {
			return;
		}
		mContext = context;
		if (intent == null) {
			return;
		}
		// 备份桌面
		String backCmd = intent.getStringExtra(BACKCMD);
		if (backCmd == null) {
			return;
		}
		if (backCmd.equals(BACKUP_DB)) {
			// 备份路径
			String backPath = intent.getStringExtra(BACKPATH);
			if (backPath == null) {
				return;
			}
			BackupDB(backPath);
		}
		// 恢复桌面
		else if (backCmd.equals(RESTORE_DB)) {
			reStoresrc = intent.getStringExtra(RANDOMSTR);
			// 备份路径
			String backPath = intent.getStringExtra(BACKPATH);
			if (backPath == null) {
				return;
			}
			RestoreDB(backPath);
		}
	}

	synchronized void BackupDB(String backPath) {
		new ExportDatabaseTask().execute(backPath);
	}

	synchronized void RestoreDB(String backPath) {
		new ImportDatabaseTask().execute(backPath);
	}

	private class ExportDatabaseTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(final String... args) {
			// 动态生成一个字符串
			randomsrc = randomString(15);
			// 获得备份路径
			String destFolderpath = args[0];
			if (destFolderpath == null) {
				return EXPORT_ERROR;
			}
			// 如果当前SD卡不存在,即返回SDCARD_UNMOUNTED
			if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				return SDCARD_UNMOUNTED;
			}
			try {
				// GO桌面数据库所在位置
				String srcFolderPath = Environment.getDataDirectory() + "/data/"
						+ mContext.getPackageName() + "/databases";

				File srcFolder = new File(srcFolderPath);
				if (srcFolder == null || !srcFolder.exists() || !srcFolder.isDirectory()) {
					return DATABASE_NOT_EXIT;
				}

				// 进行备份 ,成功则返回成功,失败则返回备份失败
				if (copyFolder(srcFolderPath, destFolderpath, true, randomsrc)) {
					return EXPORT_SUCCESS;
				}
				return EXPORT_ERROR;
			} catch (IOException e) {
				return EXPORT_ERROR;
			} catch (Exception e) {
				e.printStackTrace();
				return EXPORT_ERROR;
			}
		}

		@Override
		protected void onPostExecute(final String msg) {
			Intent intent = new Intent(ICustomAction.ACTION_BACKUP);
			intent.putExtra(BACKINFO, msg);
			intent.putExtra(RANDOMSTR, randomsrc);
			// 发送广播,广播内容包括备份信息和随机字符串
			mContext.sendBroadcast(intent);
		}
	}

	private class ImportDatabaseTask extends AsyncTask<String, Void, String> {
		private boolean isRestart = false;

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(final String... args) {
			String srcFolderPath = args[0];
			if (srcFolderPath == null) {
				return DFILE_IMPORT_ERROR;
			}
			if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				return SDCARD_UNMOUNTED;
			}
			try {
				String destFolderpath = Environment.getDataDirectory() + "/data/"
						+ mContext.getPackageName() + "/databases";

				// 找不到文件夹
				File dbBackupFile = new File(srcFolderPath + "/" + "androidheart.db");
				if (dbBackupFile == null || !dbBackupFile.exists()) {
					return DBFILE_NOT_FOUND;
				} else if (!dbBackupFile.canRead()) {
					return DBFILE_NOT_READABLE;
				}
				if (copyFolder(srcFolderPath, destFolderpath, false, null)) {
					isRestart = true;
					return DFILE_IMPORT_SUCCESS;
				} else {
					return DFILE_IMPORT_ERROR;
				}
			} catch (IOException e) {
				return DFILE_IMPORT_ERROR;
			}
		}

		@Override
		protected void onPostExecute(final String msg) {
			Intent intent = new Intent(ICustomAction.ACTION_RESTORE);
			intent.putExtra(BACKINFO, msg);
			// 广播内容包括备份信息
			mContext.sendBroadcast(intent);
			if (isRestart) {
				exit(true);
			}
		}
	}

	public boolean copyFolder(String srcFolderPath, String destFolderpath, boolean encrypt,
			String randomSrc) throws IOException {
		if (srcFolderPath == null || destFolderpath == null) {
			return false;
		}
		String firstTenStr = null;
		File srcFolder = new File(srcFolderPath);
		if (srcFolder == null || !srcFolder.exists() || !srcFolder.isDirectory()) {
			return false;
		}

		// 构造目标文件夹
		File destFolder = new File(destFolderpath);
		destFolder.mkdirs();

		if (randomSrc != null) {
			// 生成密钥 用于写进数据库前十字节用于加密
			firstTenStr = CryptTool.encrypt(randomSrc, packagekey).substring(0, ENCRYPTBYTE);
		}

		File[] srcFolderFiles = null; // 源文件夹
		srcFolderFiles = srcFolder.listFiles();
		if (srcFolderFiles == null) {
			return false;
		}
		int count = srcFolderFiles.length;
		File srcFile = null;
		File destFile = null;
		String fileName = null;
		for (int i = 0; i < count; i++) {
			srcFile = srcFolderFiles[i];
			if (srcFile != null && srcFile.isFile()) {
				// 开始拷贝
				fileName = srcFile.getName();
				if (fileName.equals(ANDROIDHEART_FILENAME)) {
					destFile = new File(destFolderpath + "/" + fileName);
					// true
					if (encrypt) {
						if (copyOutPutFile(srcFile, destFile, firstTenStr)) {
							return true;
						} else {
							return false;
						}
					} else {
						if (copyInputFile(srcFile, destFile)) {
							return true;
						} else {
							return false;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean copyOutPutFile(File src, File dst, String mStr) throws IOException {

		if (mStr == null) {
			return false;
		}
		// 写出数据,备份GO桌面 如果原有文件夹里有该文件.先删除
		if (dst.exists()) {
			dst.delete();
		}
		dst.createNewFile();
		FileChannel inChannel = null;
		FileChannel outChannel = null;
		FileOutputStream fos = null;

		try {
			inChannel = new FileInputStream(src).getChannel();
			outChannel = new FileOutputStream(dst).getChannel();
			// 先写入密钥 10位
			fos = new FileOutputStream(dst);
			fos.write(mStr.getBytes());
			fos.close();
			// 再写入数据库内容
			outChannel.transferFrom(inChannel, outChannel.size(), inChannel.size());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
		return false;
	}

	public boolean copyInputFile(File src, File dst) throws IOException {
		// 读取10个字节 与密钥进行比较确定是否为备份的GO桌面数据库
		String firstTenStr = null;
		byte[] buffer = new byte[10];
		FileInputStream fis = null;
		String dbFirstTen = null;
		try {
			fis = new FileInputStream(src);
			fis.read(buffer, 0, ENCRYPTBYTE);
			dbFirstTen = new String(buffer, 0, ENCRYPTBYTE);
			fis.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (reStoresrc != null) {
			firstTenStr = CryptTool.encrypt(reStoresrc, packagekey).substring(0, ENCRYPTBYTE);
		} else {
			return false;
		}
		reStoresrc = null;
		if (firstTenStr != null && firstTenStr.equals(dbFirstTen)) {
			if (dst.exists()) {
				dst.delete();
			}
			dst.createNewFile();
		} else {
			return false;
		}

		FileChannel inChannel = new FileInputStream(src).getChannel();
		FileChannel outChannel = new FileOutputStream(dst).getChannel();

		try {
			inChannel.transferTo(ENCRYPTBYTE, inChannel.size(), outChannel);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
		return false;
	}

	// 重启桌面
	private void exit(boolean restart) {
		// 通知周边插件桌面退出
		GOLauncherApp.getApplication().exit(restart);
	}

	/**
	 * 产生随机字符串
	 * */

	public static final String randomString(int length) {
		Random randGen = null;
		char[] numbersAndLetters = null;
		if (length < 1) {
			return null;
		}
		if (randGen == null) {
			randGen = new Random();
			numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz"
					+ "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();
		}
		char[] randBuffer = new char[length];
		for (int i = 0; i < randBuffer.length; i++) {
			randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
		}
		return new String(randBuffer);
	}

}
