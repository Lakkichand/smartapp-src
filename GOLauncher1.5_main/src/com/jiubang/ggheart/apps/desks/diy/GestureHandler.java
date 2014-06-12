package com.jiubang.ggheart.apps.desks.diy;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.log.LogConstants;
import com.go.util.window.WindowControl;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DockFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenFrame;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageView;
import com.jiubang.ggheart.data.GlobalSetConfig;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.statistics.StaticTutorial;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;

/**
 * 桌面手势处理器
 * 
 * @author yuankai
 * @version 1.0
 */
public class GestureHandler {
	/**
	 * 帧控制器
	 */
	private FrameControl mFrameControl;

	/**
	 * 窗口控制器
	 */
	private Activity mActivity;

	/**
	 * 手势处理器构造方法
	 * 
	 * @param frameControl
	 *            帧控制器
	 * @param messageSender
	 *            消息发送器
	 * @param activity
	 *            上下文
	 */
	public GestureHandler(FrameControl frameControl, Activity activity) {
		mFrameControl = frameControl;
		mActivity = activity;
	}

	/**
	 * 处理手势方法
	 * 
	 * @param info
	 *            手势标识数据结构
	 * @param isFromOut
	 *            是否在其他activity界面触发
	 */
	public void handleGesture(GestureSettingInfo info, boolean isFromOut) {
		handleGesture(info, isFromOut, false);
	}

	/**
	 * 处理手势方法
	 * 
	 * @param info
	 *            手势标识数据结构
	 * @param isFromOut
	 *            是否在其他activity界面触发
	 * @param isClickHomeKey
	 *            是否点击home键触发
	 */
	public void handleGesture(GestureSettingInfo info, boolean isFromOut, boolean isClickHomeKey) {
		if (info == null) {
			return;
		}

		switch (info.mGestureAction) {
		case GlobalSetConfig.GESTURE_DISABLE:
			// do nothing
			break;
		case GlobalSetConfig.GESTURE_GOSHORTCUT:
			handleGoShortCutGesture(info.mGoShortCut, isFromOut, isClickHomeKey);
			break;
		// case GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN:
		// {
		// // 发送给屏幕层，要求显示主屏幕
		// GoLauncher.sendBroadcastMessage(this,
		// IDiyMsgIds.BACK_TO_MAIN_SCREEN,
		// -1, null, null);
		//
		// if (!isFromOut)
		// {
		// // 屏幕层显示主屏
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.SCREEN_SHOW_HOME, -1, null, null);
		// }
		// }
		// break;

		// case GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN_OR_PREVIEW:
		// {
		// boolean isPreviewTop =
		// mFrameControl.isForeground(IDiyFrameIds.SCREEN_PREVIEW_FRAME);
		// GoLauncher.sendBroadcastMessage(this,
		// IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null, null);
		//
		// if(!isPreviewTop)
		// {
		// // 从其他activity返回仅需要显示主屏幕
		// if (!isFromOut)
		// {
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.SCREEN_SHOW_MAIN_SCREEN_OR_PREVIEW, -1, null, null);
		// }
		// }
		// }
		// break;

		// case GlobalSetConfig.GESTURE_SHOW_PREVIEW: // 显示预览
		// {
		// if(mFrameControl.isScreenOnTop())
		// {
		// // 从其他activity返回仅需要显示主屏幕
		// if (!isFromOut)
		// {
		// // 发送给屏幕层，要求显示屏幕预览
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.SCREEN_SHOW_PREVIEW, 0, null, null);
		// }
		// }
		// else
		// {
		// // 发送给屏幕层，要求显示主屏幕
		// GoLauncher.sendBroadcastMessage(this,
		// IDiyMsgIds.BACK_TO_MAIN_SCREEN,
		// -1, null, null);
		// }
		// }
		// break;

		// case GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONBAR: //
		// 显示/不显示状态栏
		// {
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IDiyMsgIds.SHOW_HIDE_STATUSBAR, -1, null, null);
		// }
		// break;

		// case GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONEXPAND: //
		// 显示通知扩展栏
		// {
		// try
		// {
		// WindowControl.setIsFullScreen(mActivity, false);
		// WindowControl.expendNotification(mActivity);
		// }
		// catch (Exception e)
		// {
		// Log.i(LogConstants.HEART_TAG, e.toString());
		// }
		// }
		// break;

		// case GlobalSetConfig.GESTURE_OPEN_CLOSE_APPFUNC: // 打开/关闭功能表
		// {
		// if(!isFromOut)
		// {
		// // 特殊处理，动画过程中不处理打开/关闭功能表时间
		// if(mFrameControl.isExits(IDiyFrameIds.ANIMATION_FRAME))
		// {
		// return;
		// }
		//
		// // 如果当前顶层frame不是桌面，就返回桌面，再由桌面进入功能表
		// if (!mFrameControl.isScreenOnTop())
		// {
		// // 发送给屏幕层，要求显示主屏幕
		// GoLauncher.sendBroadcastMessage(this,
		// IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null, null);
		// }
		//
		// if (mFrameControl.isExits(IDiyFrameIds.APPFUNC_FRAME))
		// {
		// mFrameControl.hideFrame(IDiyFrameIds.APPFUNC_FRAME);
		// }
		// else
		// {
		// mFrameControl.showFrame(IDiyFrameIds.APPFUNC_FRAME);
		// }
		// }
		// }
		// break;

		case GlobalSetConfig.GESTURE_SELECT_SHORTCUT: {
			// 打开应用程序
			String uriString = info.mAction;
			if (uriString != null && uriString.length() > 0) {
				Intent launchIntent = null;
				try {
					launchIntent = Intent.parseUri(uriString, 0);
					// mMessageSender.sendMessage(IDiyFrameIds.SCHEDULE_FRAME,
					// IDiyMsgIds.START_ACTIVITY, -1, launchIntent, null);
					Rect rect = new Rect(0, 0, GoLauncher.getScreenWidth(),
							GoLauncher.getScreenHeight());
					ArrayList<Rect> posArrayList = new ArrayList<Rect>();
					posArrayList.add(rect);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.START_ACTIVITY, -1, launchIntent, posArrayList);
					posArrayList.clear();
					posArrayList = null;
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					return;
				}
			}
		}
			break;

		case GlobalSetConfig.GESTURE_SELECT_APP: {
			// 打开应用程序
			String uriString = info.mAction;
			if (uriString != null && uriString.length() > 0) {
				Intent launchIntent = null;
				try {
					launchIntent = Intent.parseUri(uriString, 0);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.START_ACTIVITY, -1, launchIntent, null);
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					return;
				}
			}
		}
			break;

		// case GlobalSetConfig.GESTURE_SHOW_HIDE_DOCK:
		// {
		// if(ShortCutSettingInfo.sEnable)
		// {
		// GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
		// IDiyMsgIds.DOCK_HIDE, -1, null, null);
		// }else{
		// GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
		// IDiyMsgIds.DOCK_SHOW, -1, null, null);
		// }
		// }
		// break;

		default:
			break;
		}
	}

	/**
	 * <br>
	 * 用来显示状态栏和指示器 <br>
	 * 功能详细描述:
	 * 
	 * @author maxiaojun
	 * @date [2012-9-19]
	 */
	class ResetStatusThread extends Thread {
		private boolean mIsVib;
		private DesktopIndicator mIndicator;

		public ResetStatusThread(boolean isVib, DesktopIndicator indicator) {
			this.mIsVib = isVib;
			this.mIndicator = indicator;
		}

		@Override
		public void run() {
			if (!mIsVib && null != mIndicator) {
				mIndicator.show();
			}
		}
	};

	private void handleGoShortCutGesture(int gestureAction, boolean isFromOut,
			boolean isClickHomeKey) {
		switch (gestureAction) {
		case GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN: {

			// 发送给屏幕层，要求显示主屏幕
			GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null, null);

			if (!isFromOut) {
				// 屏幕层显示主屏
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_SHOW_HOME, -1, null, null);
			} else {
				// 如果是外部(如桌面设置)跳转则将是否跳转主屏设为0
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 100, null, null);
			}
		}
			break;

		case GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN_OR_PREVIEW: {
			boolean isPreviewTop = mFrameControl.isForeground(IDiyFrameIds.SCREEN_PREVIEW_FRAME);
			GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null, null);

			if (!isPreviewTop) {
				// 从其他activity返回仅需要显示主屏幕
				if (!isFromOut) {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREEN_SHOW_MAIN_SCREEN_OR_PREVIEW, -1, null, null);
				}
			}
		}
			break;

		case GlobalSetConfig.GESTURE_SHOW_PREVIEW: // 显示预览
		{
			if (mFrameControl.isScreenOnTop()) {
				// 从其他activity返回仅需要显示主屏幕
				if (!isFromOut) {
					// 发送给屏幕层，要求显示屏幕预览
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREEN_SHOW_PREVIEW, 0, null, null);
				}
			} else {
				// 发送给屏幕层，要求显示主屏幕
				GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null,
						null);
			}
		}
			break;

		case GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONBAR: // 显示/不显示状态栏
		{

			boolean isVib = true;
			DesktopIndicator indicator = null;
			ScreenFrame screenFrame = null;
			AbstractFrame frame = mFrameControl.getFrame(IDiyFrameIds.SCREEN_FRAME);
			if (frame != null) {
				screenFrame = (ScreenFrame) frame;
				if (screenFrame != null) {
					indicator = screenFrame.getIndicator();
					isVib = indicator.getVisible();
					if (isVib) {
						indicator.hide();
						isVib = false;
					}
				}
			}
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SHOW_HIDE_STATUSBAR, -1, null, null);

			if (screenFrame != null) {
				ResetStatusThread resetStatusAndIndicator = new ResetStatusThread(isVib, indicator);
				screenFrame.mWorkspace.postDelayed(resetStatusAndIndicator, 100);
			} else {
				if (!isVib && null != indicator) {
					indicator.show();
				}
			}
		}
			break;

		case GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONEXPAND: // 显示通知扩展栏
		{
			try {
				WindowControl.setIsFullScreen(mActivity, false);
				WindowControl.expendNotification(mActivity);

			} catch (Exception e) {
				Log.i(LogConstants.HEART_TAG, e.toString());
			}
		}
			break;

		case GlobalSetConfig.GESTURE_OPEN_CLOSE_APPFUNC: // 打开/关闭功能表
		{
			if (!isFromOut) {
				// 特殊处理，动画过程中不处理打开/关闭功能表时间
				if (mFrameControl.isExits(IDiyFrameIds.ANIMATION_FRAME)) {
					return;
				}

				// 如果当前顶层frame不是桌面，就返回桌面，再由桌面进入功能表
				if (!mFrameControl.isScreenOnTop()) {
					// 发送给屏幕层，要求显示主屏幕
					GoLauncher.sendBroadcastMessage(this, IDiyMsgIds.BACK_TO_MAIN_SCREEN, -1, null,
							null);
				}

				if (mFrameControl.isExits(IDiyFrameIds.APPFUNC_FRAME)) {
					mFrameControl.hideFrame(IDiyFrameIds.APPFUNC_FRAME);
				} else {
					mFrameControl.showFrame(IDiyFrameIds.APPFUNC_FRAME);
				}
			}
		}
			break;

		case GlobalSetConfig.GESTURE_SHOW_HIDE_DOCK: {
			if (ShortCutSettingInfo.sEnable) {
				final int type = isClickHomeKey ? DockFrame.HIDE_ANIMATION_NO
						: DockFrame.HIDE_ANIMATION;
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_HIDE, type,
						null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_HIDE, type,
						null, null);
			} else {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_SHOW,
						DockFrame.HIDE_ANIMATION, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_SHOW,
						DockFrame.HIDE_ANIMATION, null, null);
			}
		}
			break;
		case GlobalSetConfig.GESTURE_LOCK_SCREEN: {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.ENABLE_KEYGUARD,
					-1, null, null);
		}
			break;
		case GlobalSetConfig.GESTURE_GOSTORE: {
//			Intent intent = new Intent();
//			intent.setClass(mActivity, GoStore.class);
//			mActivity.startActivity(intent);
			AppsManagementActivity.startAppCenter(mActivity,
					MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
		}
			break;
		case GlobalSetConfig.GESTURE_OPEN_THEME_SETTING: {
			Intent mythemesIntent = new Intent();
			mythemesIntent.putExtra("entrance", ThemeManageView.LAUNCHER_THEME_VIEW_ID);
			mythemesIntent.setClass(mActivity, ThemeManageActivity.class);
			mActivity.startActivity(mythemesIntent);
		}
			break;
		case GlobalSetConfig.GESTURE_OPEN_DESK_PREFENECE: {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
					IDiyMsgIds.SCREEN_PREFERENCES, -1, null, null);
		}
			break;
		case GlobalSetConfig.GESTURE_SHOW_MENU: {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.SHOW_HIDE_MENU,
					-1, null, null);
			updateMenuSharedPreferencesValue();
		}
			break;
		case GlobalSetConfig.GESTURE_SHOW_DIYGESTURE: {
			Intent intent = new Intent(ICustomAction.ACTION_SHOW_DIYGESTURE);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.START_ACTIVITY,
					-1, intent, null);
		}
			break;
		case GlobalSetConfig.GESTURE_SHOW_PHOTO: {
			if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
				if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
					// 打开功能表并进入图片管理界面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_IMAGE);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.IMAGE });
				}
			} else {
				showMedPlugDownDialog();
			}
		}
			break;
		case GlobalSetConfig.GESTURE_SHOW_MUSIC: {
			if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
				if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
					// 打开功能表并进入音乐管理界面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_AUDIO);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.MUSIC });
				}
			} else {
				showMedPlugDownDialog();
			}
		}
			break;
		case GlobalSetConfig.GESTURE_SHOW_VIDEO: {
			if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
				if (AppFuncUtils.getInstance(mActivity).isMediaPluginCompatible()) {
					// 打开功能表并进入视频管理界面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
					StatisticsData.countMenuData(mActivity, StatisticsData.FUNTAB_KEY_VIDEO);
					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
							new Object[] { AppFuncContentTypes.VIDEO });
				}
			} else {
				showMedPlugDownDialog();
			}
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 更新菜单SharedPreferences记录的值，经过手势滑动之后，改为false，按下menu不再需要打开用户提示
	 */
	private void updateMenuSharedPreferencesValue() {
		if (StaticTutorial.sCheckScreenMenuOpen) {
			StaticTutorial.sCheckScreenMenuOpen = false;
			PreferencesManager sharedPreferences = new PreferencesManager(mActivity,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			sharedPreferences.putBoolean(IPreferencesIds.SHOULD_SHOW_SCREEN_MENU_OPEN_GUIDE, false);
			sharedPreferences.commit();
		}
	}
	
	private void showMedPlugDownDialog() {
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
		
		DialogConfirm dialog = new DialogConfirm(mActivity);
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
}
