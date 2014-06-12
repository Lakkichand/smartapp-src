package com.jiubang.ggheart.apps.desks.Preferences;

import java.util.Locale;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.KeyEvent;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogLanguageChoice;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemCheckBoxView;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.data.AppService;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;

/**
 * 
 * <br>
 * 类描述:桌面设置-高级设置Activity <br>
 * 功能详细描述:
 * 
 * @author licanhui
 * @date [2012-9-10]
 */
public class DeskSettingAdvancedActivity extends DeskSettingBaseActivity {
	private GoSettingControler mGoSettingControler;
	private ThemeSettingInfo mThemeSettingInfo;

	/**
	 * 高质量绘图
	 */
	private DeskSettingItemCheckBoxView mSettingHighQualityDraw;

	/**
	 * 支持透明通知栏
	 */
	private DeskSettingItemCheckBoxView mSettingTransparentStatusbar;

	/**
	 * 常驻内存
	 */
	private DeskSettingItemCheckBoxView mSettingPermanentMemory;

	/**
	 * 阻止强制关闭
	 */
	private DeskSettingItemCheckBoxView mSettingPreventFC;

	/**
	 * 检查垃圾数据
	 */
	private DeskSettingItemBaseView mSettingCleanDirtyData;

	/**
	 * 语言设置
	 */
	private DeskSettingItemBaseView mSettingLanguage;

	/**
	 * 桌面搬家
	 */
	private DeskSettingItemBaseView mSettingDeskMigrate;
	/**
	 * 应用管理插件
	 */
	private DeskSettingItemBaseView mMediaPlugin;
	// onex升级向导
	private DeskSettingItemBaseView mOneXGuid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desk_setting_advanced);

		mGoSettingControler = GOLauncherApp.getSettingControler();
		mThemeSettingInfo = mGoSettingControler.getThemeSettingInfo();

		initViews();
		load();
	}

	/**
	 * <br>
	 * 功能简述:初始化View <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void initViews() {
		mSettingHighQualityDraw = (DeskSettingItemCheckBoxView) findViewById(R.id.high_quality_drawing);
		mSettingHighQualityDraw.setOnValueChangeListener(this);

		mSettingTransparentStatusbar = (DeskSettingItemCheckBoxView) findViewById(R.id.transparent_statusbar);
		mSettingTransparentStatusbar.setOnValueChangeListener(this);

		mSettingPermanentMemory = (DeskSettingItemCheckBoxView) findViewById(R.id.permanentmemory_enable);
		mSettingPermanentMemory.setOnValueChangeListener(this);

		mSettingPreventFC = (DeskSettingItemCheckBoxView) findViewById(R.id.prevent_force_close);
		mSettingPreventFC.setOnValueChangeListener(this);

		mSettingCleanDirtyData = (DeskSettingItemBaseView) findViewById(R.id.clean_dirty_data);
		mSettingCleanDirtyData.setOnClickListener(this);

		mSettingLanguage = (DeskSettingItemBaseView) findViewById(R.id.language_setting);
		mSettingLanguage.setOnClickListener(this);

		mSettingDeskMigrate = (DeskSettingItemBaseView) findViewById(R.id.migrate_app_tip_title);
		mSettingDeskMigrate.setOnClickListener(this);

		mOneXGuid = (DeskSettingItemBaseView) findViewById(R.id.onexguide);
		if (Machine.isONE_X() && !Machine.IS_ICS_MR1) {
			mOneXGuid.setOnClickListener(this);
		} else {
			mOneXGuid.setVisibility(View.GONE);
		}
		//资源管理插件
		mMediaPlugin = (DeskSettingItemBaseView) findViewById(R.id.fun_app_ui_media_plugin);
		mMediaPlugin.setOnClickListener(this);
	}

	@Override
	public void load() {
		super.load();
		if (mThemeSettingInfo != null) {
			mSettingHighQualityDraw
					.setIsCheck(mThemeSettingInfo.mHighQualityDrawing);
			mSettingTransparentStatusbar
					.setIsCheck(mThemeSettingInfo.mTransparentStatusbar);
			mSettingPermanentMemory
					.setIsCheck(mThemeSettingInfo.mIsPemanentMemory);
			mSettingPreventFC.setIsCheck(mThemeSettingInfo.mPreventForceClose);
		}
	}

	@Override
	public void save() {
		super.save();
		if (mThemeSettingInfo != null) {
			boolean isChangeTheme = false;

			if (mThemeSettingInfo.mHighQualityDrawing != mSettingHighQualityDraw
					.getIsCheck()) {
				mThemeSettingInfo.mHighQualityDrawing = mSettingHighQualityDraw
						.getIsCheck();
				isChangeTheme = true;
			}

			if (mThemeSettingInfo.mTransparentStatusbar != mSettingTransparentStatusbar
					.getIsCheck()) {
				mThemeSettingInfo.mTransparentStatusbar = mSettingTransparentStatusbar
						.getIsCheck();
				isChangeTheme = true;
			}

			if (mThemeSettingInfo.mIsPemanentMemory != mSettingPermanentMemory
					.getIsCheck()) {
				mThemeSettingInfo.mIsPemanentMemory = mSettingPermanentMemory
						.getIsCheck();
				isChangeTheme = true;
			}

			if (mThemeSettingInfo.mPreventForceClose != mSettingPreventFC
					.getIsCheck()) {
				mThemeSettingInfo.mPreventForceClose = mSettingPreventFC
						.getIsCheck();
				isChangeTheme = true;
			}

			if (isChangeTheme) {
				GOLauncherApp.getSettingControler().updateThemeSettingInfo(
						mThemeSettingInfo);
			}
		}
	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView baseView, Object value) {
		// 支持透明通知栏
		if (baseView == mSettingTransparentStatusbar) {
			if (mSettingTransparentStatusbar.getIsCheck()) {
				showTransparentStatusbarDialog();
			}
		}

		// 常驻内存
		if (baseView == mSettingPermanentMemory) {
			showPermanentMemoryDialog();
		}

		return true;
	}

	/**
	 * <br>
	 * 功能简述:支持透明通知栏提示对话框 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void showTransparentStatusbarDialog() {
		DialogConfirm mTransparentStatusbarDialog = new DialogConfirm(this);
		mTransparentStatusbarDialog.show();
		mTransparentStatusbarDialog
				.setTitle(R.string.tran_statusbar_dialog_title);
		mTransparentStatusbarDialog
				.setMessage(getString(R.string.tran_statusbar_dialog_content));
		mTransparentStatusbarDialog.setNegativeButton(null,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						mSettingTransparentStatusbar.setIsCheck(false);
					}
				});

		mTransparentStatusbarDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				mSettingTransparentStatusbar.setIsCheck(false);
				dialog.dismiss();
				return false;
			}
		});

	}

	/**
	 * <br>
	 * 功能简述:常驻内存提示对话框 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void showPermanentMemoryDialog() {
		DialogConfirm mPermanentMemoryDialog = new DialogConfirm(this);
		mPermanentMemoryDialog.show();
		mPermanentMemoryDialog
				.setTitle(getString(R.string.setSystemPersistentTitle));
		mPermanentMemoryDialog
				.setMessage(getString(R.string.setSystemPersistentRestart));
		mPermanentMemoryDialog.setPositiveButton(R.string.reboot_right_now,
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						save();
						Intent intent = new Intent();
						intent.setClass(GOLauncherApp.getContext(),
								AppService.class);
						GOLauncherApp.getContext().getApplicationContext()
								.stopService(intent);
						exitAndRestart();
					}
				});
		mPermanentMemoryDialog.setNegativeButton(R.string.reboot_next_time, null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 检查垃圾数据
		case R.id.clean_dirty_data:
			StatisticsData.saveUseRecordPreferences(this,
					StatisticsData.CLEANDIRTYDATA_ITEM); // 统计
			checkDirtyData();
			break;

		// 语言设置
		case R.id.language_setting:
			showInstallLanguageTip(this);
			break;

		// 桌面搬家
		case R.id.migrate_app_tip_title:
			StatisticsData.saveUseRecordPreferences(this,
					StatisticsData.DESKMIGRATE_ITEM); // 统计
			deskMigrate(); // 桌面搬家
			break;

		// oneX
		case R.id.onexguide:
			oneXGuide();
			break;
			//资源管理插件	
		case R.id.fun_app_ui_media_plugin :
			if (MediaPluginFactory.isMediaPluginExist(getApplicationContext())) {
				startActivity(new Intent(this, GoLauncher.class));
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
				switch (AppFuncContentTypes.sType_for_setting) {
					case AppFuncContentTypes.IMAGE :
						DeliverMsgManager
								.getInstance()
								.onChange(
										AppFuncConstants.APP_FUNC_MAIN_VIEW,
										AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
										new Object[] { AppFuncContentTypes.IMAGE });
						break;
					case AppFuncContentTypes.MUSIC :
						DeliverMsgManager.getInstance().onChange(
								AppFuncConstants.APP_FUNC_MAIN_VIEW,
								AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
								new Object[] { AppFuncContentTypes.MUSIC });
						break;
					case AppFuncContentTypes.VIDEO :
						DeliverMsgManager
								.getInstance()
								.onChange(
										AppFuncConstants.APP_FUNC_MAIN_VIEW,
										AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
										new Object[] { AppFuncContentTypes.VIDEO });
						break;
					default :
						DeliverMsgManager
								.getInstance()
								.onChange(
										AppFuncConstants.APP_FUNC_MAIN_VIEW,
										AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
										new Object[] { AppFuncContentTypes.IMAGE });
						break;
				}
				} else {
				final Context context = GOLauncherApp.getContext();
				String textFirst = context.getString(R.string.download_mediamanagement_plugin_dialog_text_first);
				String textMiddle = context.getString(R.string.download_mediamanagement_plugin_dialog_text_middle);
				String textLast = context.getString(R.string.download_mediamanagement_plugin_dialog_text_last);
				SpannableStringBuilder messageText = new SpannableStringBuilder(textFirst + textMiddle + textLast);
				messageText.setSpan(new RelativeSizeSpan(0.8f), textFirst.length(), messageText.length() - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				messageText.setSpan(
						new ForegroundColorSpan(context.getResources().getColor(
								R.color.snapshot_tutorial_notice_color)), textFirst.length(),
						textFirst.length() + textMiddle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  //设置提示为绿色
					
				DialogConfirm dialog = new DialogConfirm(this);
				dialog.show();
				dialog.setTitle(R.string.download_mediamanagement_plugin_dialog_title);
				dialog.setMessage(messageText);
				dialog.setPositiveButton(R.string.download_mediamanagement_plugin_dialog_download_btn_text, 
						new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// 跳转进行下载
						Context context = GOLauncherApp.getContext();
						String packageName = PackageName.MEDIA_PLUGIN;
						String url = LauncherEnv.Url.MEDIA_PLUGIN_FTP_URL; // 插件包ftp地址
						String linkArray[] = { packageName, url };
						String title = context
								.getString(R.string.mediamanagement_plugin_download_title);
						boolean isCnUser = Machine.isCnUser(context);

						CheckApplication.downloadAppFromMarketFTPGostore(context, "",
								linkArray, LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK, title,
								System.currentTimeMillis(), isCnUser,
								CheckApplication.FROM_MEDIA_DOWNLOAD_DIGLOG);	
					}
				});
				dialog.setNegativeButton(R.string.download_mediamanagement_plugin_dialog_later_btn_text, null);
			
			}
			break;
		default:
			break;
		}

	}

	/**
	 * <br>
	 * 功能简述:检查是否存在垃圾数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	synchronized void checkDirtyData() {
		boolean isScreenDirty = GoLauncher.sendMessage(this,
				IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.IS_EXIST_TRASH_DATA, -1,
				null, null);

		boolean dockDirty = GoLauncher.sendMessage(this,
				IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.IS_EXIST_DOCK_TRASH_DATA,
				-1, null, null);

		boolean funcDirty = GoLauncher.sendMessage(this,
				IDiyFrameIds.APPFUNC_FRAME, IDiyMsgIds.IS_EXIST_TRASH_DATA, -1,
				null, null);

		if (isScreenDirty || dockDirty || funcDirty) {
			showCleanListDialog(isScreenDirty, dockDirty, funcDirty);
		} else {
			showNoDirtyDataDialog();
		}
	}

	/**
	 * <br>
	 * 功能简述:没有垃圾数据提示对话框 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void showNoDirtyDataDialog() {
		DialogConfirm mNoDirtyDataDialog = new DialogConfirm(this);
		mNoDirtyDataDialog.show();
		mNoDirtyDataDialog.setTitle(getString(R.string.clean_dirty_data));
		mNoDirtyDataDialog.setMessage(getString(R.string.no_dirty_data));
		mNoDirtyDataDialog.setNegativeButtonVisible(View.GONE);

	}

	/**
	 * <br>
	 * 功能简述:清除垃圾数据列表对话框 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void showCleanListDialog(final boolean screenDirty,
			final boolean dockDirty, final boolean funcDirty) {
		StringBuffer messageBuffer = new StringBuffer();
		if (screenDirty) {
			messageBuffer
					.append(getString(R.string.desk_setting_tab_title_desk));
		}

		if (dockDirty) {
			if (messageBuffer.length() != 0) {
				messageBuffer.append("、");
			}
			messageBuffer
					.append(getString(R.string.desk_setting_tab_title_dock));
		}
		if (funcDirty) {
			if (messageBuffer.length() != 0) {
				messageBuffer.append("、");
			}
			messageBuffer
					.append(getString(R.string.desk_setting_tab_title_function));
		}
		String messageString = String.format(
				this.getString(R.string.desk_setting_have_dirty_data),
				messageBuffer);

		DialogConfirm mCleanListDialog = new DialogConfirm(this);
		mCleanListDialog.show();
		mCleanListDialog.setTitle(getString(R.string.clean_dirty_data));
		mCleanListDialog.setMessage(messageString);
		mCleanListDialog.setPositiveButton(null, new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					boolean reboot = false;
					if (screenDirty) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.CLEAN_TRASH_DATA, -1, null, null);
						reboot = true;
					}

					if (dockDirty) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.CLEAN_DOCK_TRASH_DATA, -1, null,
								null);
						reboot = true;
					}

					if (funcDirty) {
						GoLauncher.sendMessage(this,
								IDiyFrameIds.APPFUNC_FRAME,
								IDiyMsgIds.CLEAN_TRASH_DATA, -1, null, null);
						reboot = true;
					}

					// 重启桌面
					if (reboot) {
						exitAndRestart();
					}
				} catch (Exception e) {
				}
			}
		});
	}

	/**
	 * <br>
	 * 功能简述:显示语言设置对话框 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param context
	 */
	public void showInstallLanguageTip(final Context context) {
		DialogLanguageChoice mLanguageDialog = new DialogLanguageChoice(this);
		mLanguageDialog.show();
	}

	/**
	 * <br>
	 * 功能简述:桌面搬家 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	public void deskMigrate() {
		StatisticsData.saveUseRecordPreferences(this,
				StatisticsData.DESKMIGRATE_ITEM);
		if (AppUtils.isAppExist(this,
				LauncherEnv.Plugin.DESKMIGRATE_PACKAGE_NAME)) {
			Intent intent = new Intent("com.ma.deskmigrate.DeskMigrate");
			Bundle bundle = new Bundle();
			bundle.putInt("code", IRequestCodeIds.REQUEST_MIGRATE_DESK);
			intent.putExtras(bundle);
			try {
				startActivity(intent);
				finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
//			Intent it = new Intent();
//			it.setClass(this, ItemDetailActivity.class);
//			it.putExtra("pkgname", LauncherEnv.Plugin.DESKMIGRATE_PACKAGE_NAME);
//			startActivity(it);
//			Intent it = new Intent();
//			it.setClass(this, AppDetailActivity.class);
//			it.putExtra("pkgname", LauncherEnv.Plugin.DESKMIGRATE_PACKAGE_NAME);
//			startActivity(it);
			AppsDetail.gotoDetailDirectly(this, AppsDetail.START_TYPE_APPRECOMMENDED, LauncherEnv.Plugin.DESKMIGRATE_PACKAGE_NAME);
		}
	}

	/**
	 * <br>
	 * 功能简述:退出重新启动桌面 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void exitAndRestart() {
		setResult(DeskSettingMainActivity.RESULT_CODE_RESTART_GO_LAUNCHER,
				getIntent());
		this.finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void oneXGuide() {
		String url = "http://golauncher.goforandroid.com/2012/10/htc-one-xs-update-guide/";
		if (Locale.getDefault().getLanguage().equals("zh")) {
			url = "http://golauncher.goforandroid.com/zh/2012/10/htc-one-xs-update-guide/";
		}
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		saveStartActivity(intent);
	}

	public void saveStartActivity(Intent intent) {
		try {
			super.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
