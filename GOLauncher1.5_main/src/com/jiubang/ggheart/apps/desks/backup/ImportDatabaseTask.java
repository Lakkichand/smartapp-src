package com.jiubang.ggheart.apps.desks.backup;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingMainActivity;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:桌面备份数据导入
 * <br>功能详细描述:
 * 
 */
public class ImportDatabaseTask extends AsyncTask<Void, Void, String> {

	public static final String CONFIG_BACKUP_FILENAME = "gauGO";

	private Activity mActivity;
	private IBackupDBListner mListner; //监听者

	private ProgressDialog mDialog = null;
	private boolean mIsRestart = false;

	private int mType = ExportDatabaseTask.TYPE_LOCAL; // 备份类型

	public void setActivity(Activity activity) {
		mActivity = activity;
	}

	public void setType(int type) {
		mType = type;
	}

	public void setListner(IBackupDBListner listner) {
		mListner = listner;
	}

	@Override
	protected void onPreExecute() {
		this.mDialog = new ProgressDialog(mActivity);
		this.mDialog.setMessage(mActivity.getResources().getString(R.string.dbfile_import_dialog));
		this.mDialog.show();
		DataProvider.getInstance(mActivity).close();

		mListner.onImportPreExecute();
	}

	@Override
	protected String doInBackground(final Void... args) {

		synchronized (DataProvider.DB_LOCK) {
			String fbpath = null; // facebook读取目录
			if (mType == ExportDatabaseTask.TYPE_FACEBOOK) {
				String id = GoFacebookUtil.getUserInfo().getId();
				fbpath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_FACEBOOK_DIR + "/"
						+ id + "_download";
			}

			if (mType == ExportDatabaseTask.TYPE_LOCAL) {
				// add by huyong 2011.02.15 for 恢复数据库时，一并恢复主题
				// 主题SharedPreferences文件路径
				String themeSharedPreferencesFile = IPreferencesIds.SHAREDPREFERENCES_THEME
						+ ".xml";
				File backupThemeSPFile = new File(LauncherEnv.Path.SDCARD
						+ LauncherEnv.Path.PREFERENCESFILE_PATH + "/" + themeSharedPreferencesFile);
				if (backupThemeSPFile == null || !backupThemeSPFile.exists()) {
					// 首先查找新版preferences目录下是否存在，若未找到，则再查找老版DB目录下是否存在主题文件
					backupThemeSPFile = new File(LauncherEnv.Path.SDCARD
							+ LauncherEnv.Path.DBFILE_PATH + "/" + themeSharedPreferencesFile);
				}
				if (backupThemeSPFile != null && backupThemeSPFile.exists()) {
					File themeSPFile = new File(Environment.getDataDirectory() + "/data/"
							+ mActivity.getPackageName() + "/shared_prefs/"
							+ themeSharedPreferencesFile);
					if (themeSPFile.exists()) {
						themeSPFile.delete();
					}
					try {
						themeSPFile.createNewFile();
						DeskSettingConstants.copyInputFile(backupThemeSPFile, themeSPFile, 0);

						// add by huyong 2011.03.31 for 恢复数据库时，更新主题包名到数据库中
						String themeName = ThemeManager
								.getPackageNameFromSharedpreference(mActivity);
						DataProvider.getInstance(mActivity).saveThemeName(themeName);
						// add by huyong 2011.03.31 for 恢复数据库时，更新主题包名到数据库中 end

					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
				// add by huyong 2011.02.15 for 恢复数据库时，一并恢复主题 end
			}

			// add by chenguanyu 2012.06.15 增加对用户向导的SharedPreferences恢复
			String guidePreferencesName = IPreferencesIds.USERTUTORIALCONFIG + ".xml";
			PreferencesManager.importSharedPreferences(mActivity, guidePreferencesName, mType);
			//				importSharedPreferences(guidePreferencesName);
			// add by chenguanyu 2012.06.15 增加对用户向导的SharedPreferences恢复 end

			// add by ruxueqin 恢复自定义手势文件备份
			restoreDiygesture();

			File dbBackupFile = null;
			if (mType == ExportDatabaseTask.TYPE_LOCAL) {
				String path = LauncherEnv.Path.DBFILE_PATH;
				// 首先在GO_Launcher文件夹目录下查找androidheart.db，若未找到，则查找gauGo，若仍未找到，则直接使用根目录
				dbBackupFile = new File(LauncherEnv.Path.SDCARD + path + "/" + "androidheart.db");
				if (dbBackupFile == null || !dbBackupFile.exists()) {
					dbBackupFile = new File(LauncherEnv.Path.SDCARD + path + "/"
							+ CONFIG_BACKUP_FILENAME);
					if (dbBackupFile == null || !dbBackupFile.exists()) {
						dbBackupFile = new File(LauncherEnv.Path.SDCARD, CONFIG_BACKUP_FILENAME);
					}
				}
			} else if (mType == ExportDatabaseTask.TYPE_FACEBOOK) {
				dbBackupFile = new File(fbpath + LauncherEnv.Path.DBFILE_NAME + "/androidheart.db");
			}

			if (dbBackupFile == null || !dbBackupFile.exists()) {
				return mActivity.getResources().getString(R.string.dbfile_not_found);
			} else if (!dbBackupFile.canRead()) {
				return mActivity.getResources().getString(R.string.dbfile_not_readable);
			}

			File dbFile = deleteDbFile(Environment.getDataDirectory() + "/data/"
					+ mActivity.getPackageName() + "/databases/androidheart.db");
			deleteDbWalFiles(LauncherEnv.Path.SDCARD + LauncherEnv.Path.DBFILE_PATH
					+ "/androidheart.db");
			try {
				if (dbBackupFile.getName().equals(CONFIG_BACKUP_FILENAME)) {
					// 旧版名称为gauGo的还原
					dbFile.createNewFile();
					DeskSettingConstants.copyInputFile(dbBackupFile, dbFile,
							ExportDatabaseTask.ENCRYPTBYTE);
				} else {
					String srcFolderPath = null;
					if (mType == ExportDatabaseTask.TYPE_LOCAL) {
						srcFolderPath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DBFILE_PATH;
					} else if (mType == ExportDatabaseTask.TYPE_FACEBOOK) {
						srcFolderPath = fbpath + LauncherEnv.Path.DBFILE_NAME;
					}
					String destFolderpath = Environment.getDataDirectory() + "/data/"
							+ mActivity.getPackageName() + "/databases";
					ExportDatabaseTask.copyFolder(srcFolderPath, destFolderpath, false,
							ExportDatabaseTask.ENCRYPTBYTE);
					DataProvider.getInstance(mActivity).checkBackDB();
				}

				// clearTutorial();
				mIsRestart = true;

				saveFacebookRestoreData();

				// return
				// getResources().getString(R.string.dbfile_import_success);
				return null;
			} catch (IOException e) {
				return mActivity.getResources().getString(R.string.dbfile_import_error);
			}

		}
	}

	private void saveFacebookRestoreData() {
		// facebook备份成功处理
		if (mType == ExportDatabaseTask.TYPE_FACEBOOK) {
			String name = GoFacebookUtil.getUserInfo().getName();
			boolean isNewUser = true;
			if (name != null) {
				PreferencesManager sp = new PreferencesManager(mActivity,
						IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
				String recordname = sp.getString(IPreferencesIds.FACEBOOK_RESTORE_NAME_LIST, null);
				if (recordname != null) {
					String[] recordnamesarray = recordname.split("#");
					int length = recordnamesarray != null ? recordnamesarray.length : 0;
					for (int i = 0; i < length; i++) {
						if (name.equals(recordnamesarray[i])) {
							isNewUser = false;
							break;
						}
					}
				}
				if (isNewUser) {
					recordname = recordname != null ? recordname : "";
					recordname += name + "#";
					sp.putString(IPreferencesIds.FACEBOOK_RESTORE_NAME_LIST, recordname);
					sp.putBoolean(IPreferencesIds.FACEBOOK_RESTART_AFTER_RESTORE, true);
					sp.commit();
				}
			}
		}
	}

	@Override
	protected void onPostExecute(final String msg) {
		if (this.mDialog.isShowing()) {
			try {
				this.mDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (msg != null && msg.length() > 0) {
			DeskToast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
		}
		if (mIsRestart) {
			exitAndRestart();
		}

		mListner.onImportPostExecute(mType, msg);
	}

	/**
	 * <br>功能简述:退出重新启动桌面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void exitAndRestart() {
		mActivity.setResult(DeskSettingMainActivity.RESULT_CODE_RESTART_GO_LAUNCHER,
				mActivity.getIntent());
		mActivity.finish();
	}

	/**
	 * 恢复自定义手势备份
	 */
	private void restoreDiygesture() {
		// 1：删除手势文件
		String filepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH
				+ "diyGestures";
		File file = new File(filepath);
		if (file.exists()) {
			file.delete();
		}
		// 2：导入手势备份文件
		String backupfilepath = null;
		if (mType == ExportDatabaseTask.TYPE_LOCAL) {
			backupfilepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH + "backup";
		} else if (mType == ExportDatabaseTask.TYPE_FACEBOOK) {
			String fbpath = null;
			String id = GoFacebookUtil.getUserInfo().getId();
			fbpath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_FACEBOOK_DIR + "/" + id
					+ "_download" + LauncherEnv.Path.DIY_GESTURE_NAME;

			backupfilepath = fbpath + "backup";
		}
		File backupfile = new File(backupfilepath);
		if (backupfile.exists()) {
			try {
				file.createNewFile();
				DeskSettingConstants.copyInputFile(backupfile, file, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private File deleteDbFile(String path) {
		File dbFile = new File(path);
		FileUtil.deleteFile(path);
		deleteDbWalFiles(path);
		return dbFile;
	}

	private void deleteDbWalFiles(String path) {
		String tmpFilePath = path + "-shm";
		FileUtil.deleteFile(tmpFilePath);
		tmpFilePath = path + "-wal";
		FileUtil.deleteFile(tmpFilePath);
	}
}
