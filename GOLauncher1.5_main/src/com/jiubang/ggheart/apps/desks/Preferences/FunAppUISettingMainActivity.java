package com.jiubang.ggheart.apps.desks.Preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingPageTitleView;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;

/**
 * 
 * @author YeJijiong
 * @version 创建时间：2012-10-15 上午11:07:23
 * 功能表设置界面主Activity
 */
public class FunAppUISettingMainActivity extends DeskSettingBaseActivity implements OnClickListener {
	/**
	 * 标题栏
	 */
	private DeskSettingPageTitleView mTitleView;

	/**
	 * 个性化设置
	 */
	private DeskSettingItemBaseView mSettingVisual;

	/**
	 * 界面设置
	 */
	private DeskSettingItemBaseView mSettingScreen;

	/**
	 * 应用设置
	 */
	private DeskSettingItemBaseView mSettingApp;
	
	/**
	 * 应用管理插件
	 */
	private DeskSettingItemBaseView mMediaPlugin;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fun_app_ui_setting_main);

		mTitleView = (DeskSettingPageTitleView) findViewById(R.id.fun_app_ui_setting_main_title);
		mTitleView.getBackLayout().setVisibility(View.GONE);
		mTitleView.getTitleTextView().setPadding(
				getResources().getDimensionPixelSize(R.dimen.media_open_setting_title_paddingleft),
				0, 0, 0);

		//个性化设置
		mSettingVisual = (DeskSettingItemBaseView) findViewById(R.id.fun_app_ui_setting_visual);
		mSettingVisual.setOnClickListener(this);

		//桌面设置
		mSettingScreen = (DeskSettingItemBaseView) findViewById(R.id.fun_app_ui_setting_screen);
		mSettingScreen.setOnClickListener(this);
		mSettingScreen.setOpenIntent(new Intent(this, DeskSettingScreenActivity.class));

		//应用设置
		mSettingApp = (DeskSettingItemBaseView) findViewById(R.id.fun_app_ui_setting_app);
		mSettingApp.setOnClickListener(this);
		//资源管理插件
		mMediaPlugin = (DeskSettingItemBaseView) findViewById(R.id.fun_app_ui_media_plugin);
		mMediaPlugin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fun_app_ui_setting_visual :
				startActivity(new Intent(this, FunAppUISettingVisualActivity.class));
				break;

			case R.id.fun_app_ui_setting_screen :
				Intent intent = new Intent(this, DeskSettingScreenActivity.class);
				intent.putExtra(DeskSettingScreenActivity.IS_ONLY_SHOW_AFF_FUN, true);
				startActivity(intent);
				break;

			case R.id.fun_app_ui_setting_app :
				startActivity(new Intent(this, FunAppUISettingAppActivity.class));
				break;
				
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
			default :
				break;
		}
	}
}
