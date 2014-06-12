package com.jiubang.ggheart.apps.desks.Preferences;

import java.io.File;
import java.text.SimpleDateFormat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.facebook.Session;
import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingTitleView;
import com.jiubang.ggheart.apps.desks.backup.IBackupDBListner;
import com.jiubang.ggheart.apps.desks.backup.ImportDatabaseTask;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.facebook.FacebookBackupUtil;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.DatabaseHelper;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:设置备份&恢复Activity
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date [2012-9-10]
 */
public class DeskSettingBackupActivity extends DeskSettingBaseActivity implements OnClickListener, IBackupDBListner {
	/**
	 * GO桌面备份
	 */
	private DeskSettingItemBaseView mSettingBackup;

	/**
	 * 恢复备份
	 */
	private DeskSettingItemBaseView mRestoreDBItem;

	/**
	 * 恢复默认
	 */
	private DeskSettingItemBaseView mResetDefaultItem;
	
	/**
	 * facebook标题
	 */
	private DeskSettingTitleView mFBTitle;
	
	/**
	 * facebook桌面备份
	 */
	private DeskSettingItemBaseView mFBSettingBackup;

	/**
	 * facebook恢复备份
	 */
	private DeskSettingItemBaseView mFBRestoreDBItem;

	/**
	 * facebook登出
	 */
//	private DeskSettingItemBaseView mFBLogoutItem;

	/**
	 * GO备份
	 */
	private DeskSettingItemBaseView mMoreGoBackupItem;
	
	private boolean mCanClickFBBackup = true; // 是否可点击facebook备份，防止快速连击
	private boolean mCanClickFBRestore = true; // 是否可点击facebook恢复备份，防止快速连击

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initViews();
		updateFacebookView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initViews() {
		setContentView(R.layout.desk_setting_backup);

		boolean isBackUp = checkIsBackUp(); //检查是否已经备份

		mSettingBackup = (DeskSettingItemBaseView) findViewById(R.id.backup_desk_item);
		mSettingBackup.setOnClickListener(this);
		if (isBackUp) {
			setBackUpSummary();
		}

		mRestoreDBItem = (DeskSettingItemBaseView) findViewById(R.id.restore_db_item);
		mRestoreDBItem.setOnClickListener(this);
		mRestoreDBItem.setEnabled(isBackUp); //设置恢复备份不可点

		mResetDefaultItem = (DeskSettingItemBaseView) findViewById(R.id.reset_to_default_item);
		mResetDefaultItem.setOnClickListener(this);
		
		mFBTitle = (DeskSettingTitleView) findViewById(R.id.facebook_title);
		mFBSettingBackup = (DeskSettingItemBaseView) findViewById(R.id.facebook_backup_desk_item);
		mFBRestoreDBItem = (DeskSettingItemBaseView) findViewById(R.id.facebook_restore_db_item);
//		mFBLogoutItem = (DeskSettingItemBaseView) findViewById(R.id.facebook_logout_item);
		if (!GoFacebookUtil.isEnable()) {
			mFBTitle.setVisibility(View.GONE);
			mFBSettingBackup.setVisibility(View.GONE);
			mFBRestoreDBItem.setVisibility(View.GONE);
//			mFBLogoutItem.setVisibility(View.GONE);
		} else {
			mFBSettingBackup.setOnClickListener(this);
			mFBRestoreDBItem.setOnClickListener(this);
//			mFBLogoutItem.setOnClickListener(this);
		}

		mMoreGoBackupItem = (DeskSettingItemBaseView) findViewById(R.id.more_go_backup_item);
		mMoreGoBackupItem.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.backup_desk_item :
				StatisticsData.saveUseRecordPreferences(this, StatisticsData.BACKUP_ITEM); //统计
				showBackupDialog();
				break;
			case R.id.restore_db_item :
				StatisticsData.saveUseRecordPreferences(this, StatisticsData.RESETBACKUP_ITEM); //统计
				showRestoreDBDialog();
				break;
			case R.id.reset_to_default_item :
				StatisticsData.saveUseRecordPreferences(this, StatisticsData.RESETDEFAULT_ITEM); //统计
				showResetDefaultDialog();
				break;
			case R.id.facebook_backup_desk_item :
				if (mCanClickFBBackup) {
					boolean backup = GoFacebookUtil.backupDB(DeskSettingBackupActivity.this);
					if (backup) {
						mCanClickFBBackup = false;
					}
				} else {
					Session session = Session.getActiveSession();
					if (session != null && session.isOpened()) {
						Toast.makeText(this, R.string.facebook_toast_uploading_backup, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(this, R.string.facebook_toast_loging, Toast.LENGTH_SHORT).show();
					}
				}
				StatisticsData.countStatData(this,
						StatisticsData.KEY_FACEBOOK_BACKUP_CLICK_TIMES);
				break;
			case R.id.facebook_restore_db_item :
				if (mCanClickFBRestore) {
					boolean restore = GoFacebookUtil.restoreDB(DeskSettingBackupActivity.this);
					if (restore) {
						mCanClickFBRestore = false;
					}
				}
				StatisticsData.countStatData(this,
						StatisticsData.KEY_FACEBOOK_RESTORE_BACKUP_CLICK_TIMES);
				break;
//			case R.id.facebook_logout_item :
//				PreferencesManager preferencesManager = new PreferencesManager(this,
//						IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
//				String name = preferencesManager.getString(IPreferencesIds.FACEBOOK_LOGIN_AS_USER, null);
//				if (name == null) {
//					Toast.makeText(this, R.string.facebook_user_notlogin_summary, Toast.LENGTH_SHORT).show();
//				} else {
//					//FB登出
//					GoFacebookUtil.logout(this);
//					updateFacebookView();
//				}
//				
//				StatisticsData.countStatData(this, StatisticsData.KEY_FACEBOOK_ACOUNT_CHECKOUT);
//				break;
			case R.id.more_go_backup_item :
				StatisticsData.saveUseRecordPreferences(this, StatisticsData.GOBACKUP_ITEM); //统计
				AppUtils.gotoGobackup(this); // GO备份接入
				break;
			default :
				break;
		}
	}

	/**
	 * <br>功能简述:显示备份桌面对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private synchronized void showBackupDialog() {
		DialogConfirm mNormalDialog = new DialogConfirm(this);
		mNormalDialog.show();
		mNormalDialog.setTitle(this.getString(R.string.attention_title));
		mNormalDialog.setMessage(this.getString(R.string.backup_db_summary));
		mNormalDialog.setPositiveButton(null, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					String msg = getResources().getString(R.string.import_export_sdcard_unmounted);
					DeskToast.makeText(DeskSettingBackupActivity.this, msg, Toast.LENGTH_SHORT)
							.show();
				} else {
					com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask task = new com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask();
					task.setActivity(DeskSettingBackupActivity.this);
					task.setListner(DeskSettingBackupActivity.this);
					task.setType(com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask.TYPE_LOCAL);
					task.execute();
					
					
					StatisticsData.saveStringData(DeskSettingBackupActivity.this,
							IPreferencesIds.BACKUP, "1"); //统计
					StatisticsData.backUpLanguageSetting(DeskSettingBackupActivity.this);
				}
			}
		});
	}

	/**
	 * <br>功能简述:恢复备份对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private synchronized void showRestoreDBDialog() {
		DialogConfirm mNormalDialog = new DialogConfirm(this);
		mNormalDialog.show();
		mNormalDialog.setTitle(this.getString(R.string.attention_title));
		mNormalDialog.setMessage(this.getString(R.string.restore_db_dialog_summary));
		mNormalDialog.setPositiveButton(null, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					String msg = getResources().getString(R.string.import_export_sdcard_unmounted);
					DeskToast.makeText(DeskSettingBackupActivity.this, msg, Toast.LENGTH_SHORT)
							.show();
				} else {
//					new ImportDatabaseTask().execute();
					
					com.jiubang.ggheart.apps.desks.backup.ImportDatabaseTask task = new com.jiubang.ggheart.apps.desks.backup.ImportDatabaseTask();
					task.setActivity(DeskSettingBackupActivity.this);
					task.setListner(DeskSettingBackupActivity.this);
					task.setType(com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask.TYPE_LOCAL);
					task.execute();
					
					
					StatisticsData.restoreLanguageSetting(DeskSettingBackupActivity.this);
				}
			}
		});
	}

	/**
	 * <br>功能简述:恢复默认对话框
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private synchronized void showResetDefaultDialog() {
		DialogConfirm mNormalDialog = new DialogConfirm(this);
		mNormalDialog.show();
		mNormalDialog.setTitle(this.getString(R.string.attention_title));
		mNormalDialog.setMessage(this.getString(R.string.resetDefault));
		mNormalDialog.setPositiveButton(null, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new RestoreDefaultTask().execute();
			}
		});
	}

	/**
	 * 
	 * 类描述: 功能详细描述:恢复桌面默认数据
	 * 
	 * @author chenguanyu
	 * @date [2012-8-8]
	 */
	private class RestoreDefaultTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog mDialog = new ProgressDialog(DeskSettingBackupActivity.this);
		private boolean mIsRestart = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog.setMessage(getString(R.string.restore_default_golauncher));
			mDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				delDB();
				//clearTutorial();
				delDiygesture();
				//clearDBImport();
				// 通知gowidget清理数据库
				AppCore.getInstance().getGoWidgetManager().onResetDefault();
				// 删除所有SharedPreferences文件
				PreferencesManager.clearPreferences(DeskSettingBackupActivity.this);
				ThemeManager.clearThemeSharedpreference(DeskSettingBackupActivity.this);
				DeskSettingBackupActivity.this.sendBroadcast(new Intent(
						ICustomAction.ACTION_LAUNCHER_RESETDEFAULT));
				mIsRestart = true;
			} catch (Throwable e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			mDialog.dismiss();
			if (mIsRestart) {
				exitAndRestart();
			}
		}
	}

	private void delDB() {
		String dbName = DatabaseHelper.getDBName();
		this.deleteDatabase(dbName);
	}

	private void clearTutorial() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}

	/**
	 * 清理自定义手势文件
	 */
	private void delDiygesture() {
		String filepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH
				+ "diyGestures";
		File file = new File(filepath);
		if (file.exists()) {
			file.delete();
		}
	}

//	/**
//	 * 
//	 * 类描述:导入备份的异步任务
//	 * 功能详细描述:
//	 * 
//	 * @author  guoyiqing
//	 * @date  [2012-9-13]
//	 */
//	private class ImportDatabaseTask extends AsyncTask<Void, Void, String> {
//		private final ProgressDialog mDialog = new ProgressDialog(DeskSettingBackupActivity.this);
//		private boolean mIsRestart = false;
//
//		@Override
//		protected void onPreExecute() {
//			this.mDialog.setMessage(getResources().getString(R.string.dbfile_import_dialog));
//			this.mDialog.show();
//			DataProvider.getInstance(DeskSettingBackupActivity.this).close();
//		}
//
//		// could pass the params used here in AsyncTask<String, Void, String> -
//		// but not being re-used
//		@Override
//		protected String doInBackground(final Void... args) {
//
//			synchronized (DataProvider.DB_LOCK) {
//				// add by huyong 2011.02.15 for 恢复数据库时，一并恢复主题
//				// 主题SharedPreferences文件路径
//				String themeSharedPreferencesFile = IPreferencesIds.SHAREDPREFERENCES_THEME
//						+ ".xml";
//				File backupThemeSPFile = new File(LauncherEnv.Path.SDCARD
//						+ LauncherEnv.Path.PREFERENCESFILE_PATH + "/" + themeSharedPreferencesFile);
//				if (backupThemeSPFile == null || !backupThemeSPFile.exists()) {
//					// 首先查找新版preferences目录下是否存在，若未找到，则再查找老版DB目录下是否存在主题文件
//					backupThemeSPFile = new File(LauncherEnv.Path.SDCARD
//							+ LauncherEnv.Path.DBFILE_PATH + "/" + themeSharedPreferencesFile);
//				}
//				if (backupThemeSPFile != null && backupThemeSPFile.exists()) {
//					File themeSPFile = new File(Environment.getDataDirectory() + "/data/"
//							+ DeskSettingBackupActivity.this.getPackageName() + "/shared_prefs/"
//							+ themeSharedPreferencesFile);
//					if (themeSPFile.exists()) {
//						themeSPFile.delete();
//					}
//					try {
//						themeSPFile.createNewFile();
//						DeskSettingConstants.copyInputFile(backupThemeSPFile, themeSPFile, 0);
//
//						// add by huyong 2011.03.31 for 恢复数据库时，更新主题包名到数据库中
//						String themeName = ThemeManager
//								.getPackageNameFromSharedpreference(DeskSettingBackupActivity.this);
//						DataProvider.getInstance(DeskSettingBackupActivity.this).saveThemeName(themeName);
//						// add by huyong 2011.03.31 for 恢复数据库时，更新主题包名到数据库中 end
//
//					} catch (Exception e) {
//						// TODO: handle exception
//						e.printStackTrace();
//					}
//				}
//				// add by huyong 2011.02.15 for 恢复数据库时，一并恢复主题 end
//
//				// add by chenguanyu 2012.06.15 增加对用户向导的SharedPreferences恢复
//				String guidePreferencesName = IPreferencesIds.USERTUTORIALCONFIG + ".xml";
//				PreferencesManager.importSharedPreferences(DeskSettingBackupActivity.this, guidePreferencesName);
//				//				importSharedPreferences(guidePreferencesName);
//				// add by chenguanyu 2012.06.15 增加对用户向导的SharedPreferences恢复 end
//
//				// add by ruxueqin 恢复自定义手势文件备份
//				restoreDiygesture();
//
//				String path = LauncherEnv.Path.DBFILE_PATH;
//				// 首先在GO_Launcher文件夹目录下查找androidheart.db，若未找到，则查找gauGo，若仍未找到，则直接使用根目录
//				File dbBackupFile = new File(LauncherEnv.Path.SDCARD + path + "/"
//						+ "androidheart.db");
//				if (dbBackupFile == null || !dbBackupFile.exists()) {
//					dbBackupFile = new File(LauncherEnv.Path.SDCARD + path + "/"
//							+ CONFIG_BACKUP_FILENAME);
//					if (dbBackupFile == null || !dbBackupFile.exists()) {
//						dbBackupFile = new File(LauncherEnv.Path.SDCARD, CONFIG_BACKUP_FILENAME);
//					}
//				}
//
//				if (dbBackupFile == null || !dbBackupFile.exists()) {
//					return getResources().getString(R.string.dbfile_not_found);
//				} else if (!dbBackupFile.canRead()) {
//					return getResources().getString(R.string.dbfile_not_readable);
//				}
//
//				// File dbFile = new File(Environment.getDataDirectory() + "/data/"
//				// + mActivity.getPackageName() + "/databases/androidheart.db");
//				//
//				// if (dbFile.exists()) {
//				// dbFile.delete();
//				// }
//
//				File dbFile = deleteDbFile(Environment.getDataDirectory() + "/data/"
//						+ DeskSettingBackupActivity.this.getPackageName() + "/databases/androidheart.db");
//				deleteDbWalFiles(LauncherEnv.Path.SDCARD + LauncherEnv.Path.DBFILE_PATH
//						+ "/androidheart.db");
//				try {
//					if (dbBackupFile.getName().equals(CONFIG_BACKUP_FILENAME)) {
//						// 旧版名称为gauGo的还原
//						dbFile.createNewFile();
//						DeskSettingConstants.copyInputFile(dbBackupFile, dbFile, ENCRYPTBYTE);
//					} else {
//						String srcFolderPath = LauncherEnv.Path.SDCARD
//								+ LauncherEnv.Path.DBFILE_PATH;
//						String destFolderpath = Environment.getDataDirectory() + "/data/"
//								+ DeskSettingBackupActivity.this.getPackageName() + "/databases";
//						copyFolder(srcFolderPath, destFolderpath, false, ENCRYPTBYTE);
//						DataProvider.getInstance(DeskSettingBackupActivity.this).checkBackDB();
//					}
//
//					// clearTutorial();
//					mIsRestart = true;
//					// return
//					// getResources().getString(R.string.dbfile_import_success);
//					return null;
//				} catch (IOException e) {
//					return getResources().getString(R.string.dbfile_import_error);
//				}
//
//			}
//		}
//
//		@Override
//		protected void onPostExecute(final String msg) {
//			if (this.mDialog.isShowing()) {
//				try {
//					this.mDialog.dismiss();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			if (msg != null && msg.length() > 0) {
//				DeskToast.makeText(DeskSettingBackupActivity.this, msg, Toast.LENGTH_SHORT).show();
//			}
//			if (mIsRestart) {
//				exitAndRestart();
//			}
//		}
//	}
	

	/**
	 * <br>功能简述:退出重新启动桌面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void exitAndRestart() {
		setResult(DeskSettingMainActivity.RESULT_CODE_RESTART_GO_LAUNCHER, getIntent());
		this.finish();
	}

	private void deleteDbWalFiles(String path) {
		String tmpFilePath = path + "-shm";
		FileUtil.deleteFile(tmpFilePath);
		tmpFilePath = path + "-wal";
		FileUtil.deleteFile(tmpFilePath);
	}

	private File deleteDbFile(String path) {
		File dbFile = new File(path);
		FileUtil.deleteFile(path);
		deleteDbWalFiles(path);
		return dbFile;
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
		String backupfilepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH
				+ "backup";
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

	/**
	 * 恢复SharedPreferences文件备份
	 * 
	 * @param preferencesName SharedPreferences文件名
	 */
	private void importSharedPreferences(String preferencesName) {
		File backupSPFile = new File(LauncherEnv.Path.SDCARD
				+ LauncherEnv.Path.PREFERENCESFILE_PATH + "/" + preferencesName);
		if (backupSPFile == null || !backupSPFile.exists()) {
			// 首先查找新版preferences目录下是否存在，若未找到，则再查找老版DB目录下是否存在SharedPreferences文件
			backupSPFile = new File(LauncherEnv.Path.SDCARD + LauncherEnv.Path.DBFILE_PATH + "/"
					+ preferencesName);
		}
		if (backupSPFile != null && backupSPFile.exists()) {
			File tmpSPFile = new File(Environment.getDataDirectory() + "/data/"
					+ DeskSettingBackupActivity.this.getPackageName() + "/shared_prefs/"
					+ preferencesName);
			if (tmpSPFile.exists()) {
				tmpSPFile.delete();
			}
			try {
				tmpSPFile.createNewFile();
				DeskSettingConstants.copyInputFile(backupSPFile, tmpSPFile, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 备份SharedPreferences文件
	 * 
	 * @param preferencesName
	 *            SharedPreferences文件名
	 */
	private void exportSharedPreferences(String preferencesName) {
		File sharedPreferenceFile = new File(Environment.getDataDirectory() + "/data/"
				+ DeskSettingBackupActivity.this.getPackageName() + "/shared_prefs/"
				+ preferencesName);
		// 增加一个路径
		String tmpSPPath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.PREFERENCESFILE_PATH;
		File tmpSPFile = new File(tmpSPPath);
		File backupSPFiles = new File(tmpSPPath + "/" + preferencesName);

		// note:此处为避免因SharedPreferences文件的拷贝出错，而影响到数据库文件的备份与提示工作
		try {
			tmpSPFile.mkdirs();
			// 拷贝SharedPreferences文件
			backupSPFiles.createNewFile();
			DeskSettingConstants.copyOutPutFile(sharedPreferenceFile, backupSPFiles, 0); // 不需要加密
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	/**
//	 * 备份数据库
//	 */
//	private class ExportDatabaseTask extends AsyncTask<Void, Void, String> {
//		private final ProgressDialog mDialog = new ProgressDialog(DeskSettingBackupActivity.this);
//
//		// can use UI thread here
//		@Override
//		protected void onPreExecute() {
//			this.mDialog.setMessage(getResources().getString(R.string.dbfile_export_dialog));
//			this.mDialog.show();
//			DataProvider.getInstance(DeskSettingBackupActivity.this).close();
//		}
//
//		// automatically done on worker thread (separate from UI thread)
//		@Override
//		protected String doInBackground(final Void... args) {
//			// add by huyong 2011.02.15 for 保存数据库时，一并保存主题
//			// 主题SharedPreferences文件路径
//			/*
//			 * String themeSharedPreferencesFile =
//			 * ThemeManager.SHAREDPREFERENCES_THEME + ".xml"; File themeSPFile =
//			 * new File(Environment.getDataDirectory() + "/data/" +
//			 * mActivity.getPackageName() + "/shared_prefs/" +
//			 * themeSharedPreferencesFile); //增加一个路径 String themePath =
//			 * LauncherEnv.Path.SDCARD + LocalPath.PREFERENCESFILE_PATH; File
//			 * tmpThemePath = new File(themePath); File backupThemeSPFiles = new
//			 * File(themePath + "/" + themeSharedPreferencesFile);
//			 * 
//			 * //note:此处为避免因SharedPreferences文件的拷贝出错，而影响到数据库文件的备份与提示工作 try {
//			 * tmpThemePath.mkdirs(); //拷贝主题SharedPreferences文件
//			 * backupThemeSPFiles.createNewFile(); copyOutPutFile(themeSPFile,
//			 * backupThemeSPFiles, 0); //不需要加密 } catch (Exception e) { // TODO:
//			 * handle exception e.printStackTrace(); }
//			 */
//
//			synchronized (DataProvider.DB_LOCK) {
//				// add by chenguanyu 2012.06.15 增加对用户向导的SharedPreferences进行备份
//				String guidePreferencesName = IPreferencesIds.USERTUTORIALCONFIG + ".xml";
//				PreferencesManager.backUpPreference(DeskSettingBackupActivity.this, guidePreferencesName,
//						LauncherEnv.Path.SDCARD + LauncherEnv.Path.PREFERENCESFILE_PATH);
//				//				exportSharedPreferences(guidePreferencesName);
//				// add by chenguanyu 2012.06.15 增加对用户向导的SharedPreferences进行备份 end
//
//				// 增加一个路径
//				String themePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.PREFERENCESFILE_PATH;
//				String themeSharedPreferencesFile = IPreferencesIds.SHAREDPREFERENCES_THEME
//						+ ".xml";
//				File backupThemeSPFiles = new File(themePath + "/" + themeSharedPreferencesFile);
//				// note:此处为避免因SharedPreferences文件的拷贝出错，而影响到数据库文件的备份与提示工作
//				try {
//					// 拷贝主题SharedPreferences文件
//					if (backupThemeSPFiles != null && backupThemeSPFiles.exists()) {
//						// 删除SD卡上xml文件
//						backupThemeSPFiles.delete();
//					}
//				} catch (Exception e) {
//					// TODO: handle exception
//					e.printStackTrace();
//				}
//
//				// add by ruxueqin 2012.6.27 备份自定义手势文件
//				backupDiygesture();
//
//				// add by huyong 2011.02.15 for保存数据库时，一并保存主题 end
//
//				try {
//					String srcFolderPath = Environment.getDataDirectory() + "/data/"
//							+ DeskSettingBackupActivity.this.getPackageName() + "/databases";
//					String destFolderpath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DBFILE_PATH;
//					copyFolder(srcFolderPath, destFolderpath, true, ENCRYPTBYTE);
//					return getResources().getString(R.string.dbfile_export_success);
//				} catch (IOException e) {
//					return getResources().getString(R.string.dbfile_export_error);
//				} catch (Exception e) {
//					e.printStackTrace();
//					return getResources().getString(R.string.dbfile_export_error);
//				}
//			}
//		}
//		
//		// can use UI thread here
//		@Override
//		protected void onPostExecute(final String msg) {
//			if (this.mDialog.isShowing()) {
//				try {
//					this.mDialog.dismiss();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			DeskToast.makeText(DeskSettingBackupActivity.this, msg, Toast.LENGTH_SHORT).show();
//			setBackUpSummary();
//			boolean bool = checkIsBackUp();
//			// mRestoreDBItem.setSelectable(bool);
//			mRestoreDBItem.setEnabled(bool);
//		}
//	}
	

	/**
	 * 备份自定义手势文件
	 */
	private void backupDiygesture() {
		// 1:删除旧备份文件
		String backupfilepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH
				+ "backup";
		File backupfile = new File(backupfilepath);
		if (backupfile.exists()) {
			backupfile.delete();
		}
		// 2:backup文件
		String filepath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.DIY_GESTURE_PATH
				+ "diyGestures";
		File file = new File(filepath);
		if (file.exists()) {
			try {
				backupfile.createNewFile();
				DeskSettingConstants.copyInputFile(file, backupfile, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

//	/**
//	 * 文件夹内的内容备份，若目标文件夹，未生成，则首先生成目标文件夹
//	 * 
//	 * @author huyong
//	 * @param srcFolderPath ： 源文件夹目录
//	 * @param destFolderpath：目标文件夹目录
//	 * @param encrypt ：true for加密，false for 解密
//	 * @param encryptByte ：加密字节数
//	 * @throws IOException
//	 */
//	public void copyFolder(String srcFolderPath, String destFolderpath, boolean encrypt,
//			int encryptByte) throws IOException {
//		if (srcFolderPath == null || destFolderpath == null) {
//			return;
//		}
//		File srcFolder = new File(srcFolderPath);
//		if (srcFolder == null || !srcFolder.exists() || !srcFolder.isDirectory()) {
//			return;
//		}
//
//		if (encryptByte < 0) {
//			encryptByte = 0;
//		}
//
//		// 构造目标文件夹
//		File destFolder = new File(destFolderpath);
//		destFolder.mkdirs();
//
//		File[] srcFolderFiles = null; // 源文件夹
//		srcFolderFiles = srcFolder.listFiles();
//		if (srcFolderFiles == null) {
//			return;
//		}
//
//		int count = srcFolderFiles.length;
//		File srcFile = null;
//		File destFile = null;
//		String fileName = null;
//		for (int i = 0; i < count; i++) {
//			srcFile = srcFolderFiles[i];
//			if (srcFile.isFile()) {
//				// 开始拷贝
//				fileName = srcFile.getName();
//				if (fileName.contains(GOOGLE_ANALYTICS_FILENAME)) {
//					continue;
//				}
//				if (fileName.contains(StatisticsDataBaseHelper.getDBName())) {
//					// 统计数据库不用拷贝
//					continue;
//				}
//				destFile = new File(destFolderpath + "/" + fileName);
//				if (destFile.exists()) {
//					destFile.delete();
//				}
//				destFile.createNewFile();
//				if (encrypt) {
//					DeskSettingConstants.copyOutPutFile(srcFile, destFile, encryptByte);
//				} else {
//					DeskSettingConstants.copyInputFile(srcFile, destFile, encryptByte);
//				}
//			}
//		}
//	}

	/**
	 * <br>功能简述:检查是否已经备份
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private boolean checkIsBackUp() {
		boolean bRet = true;
		String path = LauncherEnv.Path.DBFILE_PATH;
		// 首先在GO_Launcher文件夹目录下查找androidheart.db，若未找到，则查找gauGo，若仍未找到，则直接使用根目录
		File dbBackupFile = new File(LauncherEnv.Path.SDCARD + path + "/" + "androidheart.db");
		if (dbBackupFile == null || !dbBackupFile.exists()) {
			dbBackupFile = new File(LauncherEnv.Path.SDCARD + path + "/" + ImportDatabaseTask.CONFIG_BACKUP_FILENAME);
			if (dbBackupFile == null || !dbBackupFile.exists()) {
				dbBackupFile = new File(LauncherEnv.Path.SDCARD, ImportDatabaseTask.CONFIG_BACKUP_FILENAME);
			}
		}

		if (dbBackupFile == null || !dbBackupFile.exists()) {
			return false;
		}
		return bRet;
	}

	/**
	 * <br>功能简述:设置Go备份桌面的summary
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void setBackUpSummary() {
		String date = getBackUpDate(); //获取最近备份时间
		if (date != null) {
			String summary = this.getResources().getString(R.string.summary_backupdetail) + date;
			mSettingBackup.setSummaryText(summary);
		}
	}

	/**
	 * <br>功能简述:获取最近备份时间
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private String getBackUpDate() {
		String path = LauncherEnv.Path.DBFILE_PATH;
		// 首先在GO_Launcher文件夹目录下查找androidheart.db，若未找到，则查找gauGo，若仍未找到，则直接使用根目录
		File dbBackupFile = new File(LauncherEnv.Path.SDCARD + path + "/" + "androidheart.db");
		if (dbBackupFile == null || !dbBackupFile.exists()) {
			dbBackupFile = new File(LauncherEnv.Path.SDCARD + path + "/" + ImportDatabaseTask.CONFIG_BACKUP_FILENAME);
			if (dbBackupFile == null || !dbBackupFile.exists()) {
				dbBackupFile = new File(LauncherEnv.Path.SDCARD, ImportDatabaseTask.CONFIG_BACKUP_FILENAME);
			}
		}

		if (dbBackupFile != null && dbBackupFile.exists()) {
			long time = dbBackupFile.lastModified();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return dateFormat.format(time);
		}
		return null;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	/**
	 * <br>功能简述:清除已导入DB标识
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void clearDBImport() {
		SharedPreferences sharedPreferences = getSharedPreferences(IPreferencesIds.DB_PROVIDER_SUPPORT,
				Context.MODE_WORLD_READABLE);
		Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}

	@Override
	public void onExportPreExecute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onExportPostExecute(int type, String msg) {
		if (type == com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask.TYPE_LOCAL) {
			DeskToast.makeText(DeskSettingBackupActivity.this, msg, Toast.LENGTH_SHORT).show();
			setBackUpSummary();
			boolean bool = checkIsBackUp();
			// mRestoreDBItem.setSelectable(bool);
			mRestoreDBItem.setEnabled(bool);
		} else if (type == com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask.TYPE_FACEBOOK) {
			FacebookBackupUtil.backupFBData(DeskSettingBackupActivity.this, new Handler() {
				@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case FacebookBackupUtil.HANDLER_UPLOAD_SUCCESS :
							Toast.makeText(DeskSettingBackupActivity.this, R.string.facebook_taost_uploadsuccess, Toast.LENGTH_SHORT).show();
							break;
							
						case FacebookBackupUtil.HANDLER_UPLOAD_FAIL :
							Toast.makeText(DeskSettingBackupActivity.this, R.string.facebook_taost_uploadfail, Toast.LENGTH_SHORT).show();
							break;

						default :
							break;
					}
					updateFacebookView();
				}
			});
		}
	}

	@Override
	public void onImportPreExecute() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onImportPostExecute(int type, String msg) {
	}
	
	public void updateFacebookView() {
//		PreferencesManager preferencesManager = new PreferencesManager(this,
//				IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
//		String name = preferencesManager.getString(IPreferencesIds.FACEBOOK_LOGIN_AS_USER, null);
		
//		if (name != null) {
//			mFBLogoutItem.setSummaryText(getString(R.string.facebook_user_summary) + name);
//		} else {
//			mFBLogoutItem.setSummaryText(R.string.facebook_user_notlogin_summary);
//		}
		
		String lastuploadtime = getFacebookLastBackUpDate();
		if (lastuploadtime != null) {
			String summary = this.getResources().getString(R.string.summary_backupdetail) + lastuploadtime;
			mFBSettingBackup.setSummaryText(summary);
		}
		mCanClickFBBackup = true;
		mCanClickFBRestore = true;
	}
	
	private String getFacebookLastBackUpDate() {
		PreferencesManager sp = new PreferencesManager(GoLauncher.getContext(),
				IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
		long localtime = sp.getLong(IPreferencesIds.FACEBOOK_LAST_BACKUP_TIME, 0);
		
		if (localtime != 0) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return dateFormat.format(localtime);
		} else {
			return null;
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//facebook
		Session session = Session.getActiveSession();
		if (session != null) {
			session.onActivityResult(this, requestCode, resultCode, data);
		}
	}
}
