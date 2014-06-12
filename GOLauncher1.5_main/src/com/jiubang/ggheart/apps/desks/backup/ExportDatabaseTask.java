package com.jiubang.ggheart.apps.desks.backup;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;

import com.gau.go.launcherex.R;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.data.AppClassifyDatabaseHelper;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.statistics.StatisticsDataBaseHelper;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:桌面备份数据导出
 * <br>功能详细描述:
 * 
 * @author  
 * @date  
 */
public class ExportDatabaseTask extends AsyncTask<Void, Void, String> {
	public static final int ENCRYPTBYTE = 10; // 加密字节长度
	public static final String GOOGLE_ANALYTICS_FILENAME = "google_analytics";

	public static final int TYPE_LOCAL = 1; //本地备份
	public static final int TYPE_FACEBOOK = 2; //facebook备份
	private int mType = TYPE_LOCAL; // 备份类型

	private Activity mActivity;
	private IBackupDBListner mListner; //监听者
	private ProgressDialog mDialog = null;

	public void setActivity(Activity activity) {
		mActivity = activity;
	}

	public void setType(int type) {
		mType = type;
	}

	public void setListner(IBackupDBListner listner) {
		mListner = listner;
	}

	// can use UI thread here
	@Override
	protected void onPreExecute() {
		this.mDialog = new ProgressDialog(mActivity);
		this.mDialog.setMessage(mActivity.getResources().getString(R.string.dbfile_export_dialog));
		this.mDialog.show();
		DataProvider.getInstance(mActivity).close();

		mListner.onExportPreExecute();
	}

	// automatically done on worker thread (separate from UI thread)
	@Override
	protected String doInBackground(final Void... args) {
		// add by huyong 2011.02.15 for 保存数据库时，一并保存主题
		// 主题SharedPreferences文件路径
		/*
		 * String themeSharedPreferencesFile =
		 * ThemeManager.SHAREDPREFERENCES_THEME + ".xml"; File themeSPFile =
		 * new File(Environment.getDataDirectory() + "/data/" +
		 * mActivity.getPackageName() + "/shared_prefs/" +
		 * themeSharedPreferencesFile); //增加一个路径 String themePath =
		 * LauncherEnv.Path.SDCARD + LocalPath.PREFERENCESFILE_PATH; File
		 * tmpThemePath = new File(themePath); File backupThemeSPFiles = new
		 * File(themePath + "/" + themeSharedPreferencesFile);
		 * 
		 * //note:此处为避免因SharedPreferences文件的拷贝出错，而影响到数据库文件的备份与提示工作 try {
		 * tmpThemePath.mkdirs(); //拷贝主题SharedPreferences文件
		 * backupThemeSPFiles.createNewFile(); copyOutPutFile(themeSPFile,
		 * backupThemeSPFiles, 0); //不需要加密 } catch (Exception e) { // TODO:
		 * handle exception e.printStackTrace(); }
		 */

		synchronized (DataProvider.DB_LOCK) {
			String fbpath = null;
			if (mType == TYPE_FACEBOOK) {
				String id = GoFacebookUtil.getUserInfo().getId();
				fbpath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_FACEBOOK_DIR + "/"
						+ id;
				File file = new File(fbpath);
				if (file != null && file.exists() && file.isDirectory()) {
					//如果有旧id文件夹,则先删除
					FileUtil.deleteDirectory(fbpath);
				}
			}
			// add by chenguanyu 2012.06.15 增加对用户向导的SharedPreferences进行备份
			String guidePreferencesName = IPreferencesIds.USERTUTORIALCONFIG + ".xml";
			String foler = null;
			if (mType == TYPE_LOCAL) {
				foler = LauncherEnv.Path.SDCARD + LauncherEnv.Path.PREFERENCESFILE_PATH;
			} else if (mType == TYPE_FACEBOOK) {
				String id = GoFacebookUtil.getUserInfo().getId();
				if (id == null) {
					return null;
				}
				foler = fbpath + LauncherEnv.Path.PREFERENCESFILE_NAME;
			}
			PreferencesManager.backUpPreference(mActivity, guidePreferencesName, foler);
			//				exportSharedPreferences(guidePreferencesName);
			// add by chenguanyu 2012.06.15 增加对用户向导的SharedPreferences进行备份 end

			if (mType == TYPE_LOCAL) {
				// 增加一个路径
				String themePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.PREFERENCESFILE_PATH;
				String themeSharedPreferencesFile = IPreferencesIds.SHAREDPREFERENCES_THEME
						+ ".xml";
				File backupThemeSPFiles = new File(themePath + "/" + themeSharedPreferencesFile);
				// note:此处为避免因SharedPreferences文件的拷贝出错，而影响到数据库文件的备份与提示工作
				try {
					// 拷贝主题SharedPreferences文件
					if (backupThemeSPFiles != null && backupThemeSPFiles.exists()) {
						// 删除SD卡上xml文件
						backupThemeSPFiles.delete();
					}
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}

			// add by ruxueqin 2012.6.27 备份自定义手势文件
			backupDiygesture();

			// add by huyong 2011.02.15 for保存数据库时，一并保存主题 end

			try {
				String srcFolderPath = Environment.getDataDirectory() + "/data/"
						+ mActivity.getPackageName() + "/databases";
				String destFolderpath = null;
				if (mType == TYPE_LOCAL) {
					destFolderpath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DBFILE_PATH;
				} else if (mType == TYPE_FACEBOOK) {
					destFolderpath = fbpath + LauncherEnv.Path.DBFILE_NAME;
				}
				copyFolder(srcFolderPath, destFolderpath, true, ENCRYPTBYTE);
				return mActivity.getResources().getString(R.string.dbfile_export_success);
			} catch (IOException e) {
				return mActivity.getResources().getString(R.string.dbfile_export_error);
			} catch (Exception e) {
				e.printStackTrace();
				return mActivity.getResources().getString(R.string.dbfile_export_error);
			}
		}
	}

	// can use UI thread here
	@Override
	protected void onPostExecute(final String msg) {
		if (this.mDialog.isShowing()) {
			try {
				this.mDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		mListner.onExportPostExecute(mType, msg);
	}

	/**
	 * 备份自定义手势文件
	 */
	private void backupDiygesture() {
		String filepath = null; //backup源文件
		String backupfilepath = null; //backup目标文件

		filepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH + "diyGestures";
		if (mType == TYPE_LOCAL) {
			backupfilepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH + "backup";
		} else if (mType == TYPE_FACEBOOK) {
			String id = GoFacebookUtil.getUserInfo().getId();
			backupfilepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_FACEBOOK_DIR + "/"
					+ id + LauncherEnv.Path.DIY_GESTURE_NAME + "backup";
		}
		File backupfile = new File(backupfilepath);
		File file = new File(filepath);
		if (file.exists()) {
			try {
				FileUtil.createFile(backupfilepath, true);
				DeskSettingConstants.copyInputFile(file, backupfile, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 文件夹内的内容备份，若目标文件夹，未生成，则首先生成目标文件夹
	 * 
	 * @author huyong
	 * @param srcFolderPath ： 源文件夹目录
	 * @param destFolderpath：目标文件夹目录
	 * @param encrypt ：true for加密，false for 解密
	 * @param encryptByte ：加密字节数
	 * @throws IOException
	 */
	public static void copyFolder(String srcFolderPath, String destFolderpath, boolean encrypt,
			int encryptByte) throws IOException {
		if (srcFolderPath == null || destFolderpath == null) {
			return;
		}
		File srcFolder = new File(srcFolderPath);
		if (srcFolder == null || !srcFolder.exists() || !srcFolder.isDirectory()) {
			return;
		}

		if (encryptByte < 0) {
			encryptByte = 0;
		}

		// 构造目标文件夹
		File destFolder = new File(destFolderpath);
		destFolder.mkdirs();

		File[] srcFolderFiles = null; // 源文件夹
		srcFolderFiles = srcFolder.listFiles();
		if (srcFolderFiles == null) {
			return;
		}

		int count = srcFolderFiles.length;
		File srcFile = null;
		File destFile = null;
		String fileName = null;
		for (int i = 0; i < count; i++) {
			srcFile = srcFolderFiles[i];
			if (srcFile.isFile()) {
				// 开始拷贝
				fileName = srcFile.getName();
				if (fileName.contains(GOOGLE_ANALYTICS_FILENAME)) {
					continue;
				}
				if (fileName.contains(StatisticsDataBaseHelper.getDBName())) {
					// 统计数据库不用拷贝
					continue;
				}
				if (fileName.contains(AppClassifyDatabaseHelper.getDBName())) {
					// 应用分类数据库不用拷贝
					continue;
				}
				destFile = new File(destFolderpath + "/" + fileName);
				if (destFile.exists()) {
					destFile.delete();
				}
				destFile.createNewFile();
				if (encrypt) {
					DeskSettingConstants.copyOutPutFile(srcFile, destFile, encryptByte);
				} else {
					DeskSettingConstants.copyInputFile(srcFile, destFile, encryptByte);
				}
			}
		}
	}

}
