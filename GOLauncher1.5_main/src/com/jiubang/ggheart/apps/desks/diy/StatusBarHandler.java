package com.jiubang.ggheart.apps.desks.diy;

import java.lang.reflect.Field;

import android.graphics.Rect;
import android.os.Bundle;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.go.util.window.WindowControl;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 桌面状态栏处理模块
 * 
 * @author yuankai
 * @version 1.0
 */
public class StatusBarHandler {
	public final static String FIELD_FULL_SCREEN_WIDTH = "full_screen_width";
	public final static String FIELD_FULL_SCREEN_HEIGHT = "full_screen_height";
	public final static int STATUS_BAR_HEIGHT = GOLauncherApp.getContext().getResources()
			.getInteger(R.integer.status_bar_height);
	private static int sStatusbarHeight = STATUS_BAR_HEIGHT;
	private boolean mHasFirstNotify = false; // 第一次必须发送屏幕状态

	/**
	 * 状态栏管理器构造方法
	 * 
	 * @param activity
	 *            活动
	 * @param messageSender
	 *            消息发送者
	 * @param timerManager
	 *            定时器管理器
	 */
	public StatusBarHandler() {
		//如果是魅族手机，用反射方式初始化状态栏高度
		if (Machine.isMeizu()) {
			Class<?> c = null;
			Object obj = null;
			Field field = null;
			int x = 0, sbar = 0;
			try {
				c = Class.forName("com.android.internal.R$dimen");
				obj = c.newInstance();
				field = c.getField("status_bar_height");
				x = Integer.parseInt(field.get(obj).toString());
				sStatusbarHeight = GoLauncher.getContext().getResources()
						.getDimensionPixelSize(x);
			} catch (Exception e1) {
				if (DrawUtils.sVirtualDensity == -1) {
					sStatusbarHeight = DrawUtils.dip2px(STATUS_BAR_HEIGHT);
				} else {
					sStatusbarHeight = (int) (STATUS_BAR_HEIGHT
							* DrawUtils.sVirtualDensity + 0.5f);
				}
			}

		} else {
			if (DrawUtils.sVirtualDensity == -1) {
				sStatusbarHeight = DrawUtils.dip2px(STATUS_BAR_HEIGHT);
			} else {
				sStatusbarHeight = (int) (STATUS_BAR_HEIGHT
						* DrawUtils.sVirtualDensity + 0.5f);
			}
		}
		// Log.i("jiang", "状态栏：===="+sStatusbarHeight);
	}

	/**
	 * 设置全屏
	 * 
	 * @param isFullScreen
	 *            是否全屏
	 * @param updateData
	 *            是否更新数据库
	 */
	public void setFullScreen(final boolean isFullScreen, boolean updateData) {
		WindowControl.setIsFullScreen(GoLauncher.getContext(), isFullScreen);

		// 获取最新的宽高
		final Rect displayRect = WindowControl.getDisplayRect(GoLauncher.getContext());
		if (!isFullScreen) {
			displayRect.bottom -= DrawUtils.dip2px(STATUS_BAR_HEIGHT);
		}

		// 广播变更
		Bundle bundle = new Bundle();
		bundle.putInt(FIELD_FULL_SCREEN_WIDTH, displayRect.width());
		bundle.putInt(FIELD_FULL_SCREEN_HEIGHT, displayRect.height());
		GoLauncher.sendBroadcastMessage(this, IFrameworkMsgId.SYSTEM_FULL_SCREEN_CHANGE,
				isFullScreen ? 1 : 0, bundle, null);
		// 如果是屏幕层操作使通知栏隐藏，即不修改数据库里的数据
		// if(SensePreviewFrame.previewOperate == true){
		// SensePreviewFrame.previewOperate = false;
		// return;
		// }
		if (updateData) {
			updateHideSetting(isFullScreen);
		}
	}

	/**
	 * 设置全屏
	 * 
	 * @param isFullScreen
	 *            是否全屏
	 */
	public void setFullScreen(final boolean isFullScreen) {
		setFullScreen(isFullScreen, true);
	}

	/**
	 * 获取是否全屏
	 * 
	 * @return 是否全屏
	 */
	public boolean isFullScreen() {
		return WindowControl.getIsFullScreen(GoLauncher.getContext());
	}

	/**
	 * 检验设置情况
	 */
	public void checkForStatusBar() {
		setFullScreen(isHide());
		if (!mHasFirstNotify) {
			mHasFirstNotify = true;
		}
	}

	// private boolean isAutoHide()
	// {
	// return GOLauncherApp.getSettingControler()
	// .getDesktopSettingInfo().mAutoHideStatusbar;
	// }

	public static boolean isHide() {
		// 此处意义是反的，打钩代表不隐藏
		return !GOLauncherApp.getSettingControler().getDesktopSettingInfo().mShowStatusbar;
	}

	private synchronized void updateHideSetting(boolean isHide) {
		GoSettingControler settingControler = GOLauncherApp.getSettingControler();
		DesktopSettingInfo info = settingControler.getDesktopSettingInfo();
		if (info.mShowStatusbar == isHide) {
			info.mShowStatusbar = !isHide;
			settingControler.updateDesktopSettingInfo(info);
		}
	}

	public static int getStatusbarHeight() {
		return sStatusbarHeight;
	}
}
