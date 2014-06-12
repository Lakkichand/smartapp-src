package com.go.util.window;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Display;
import android.view.Surface;

import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 旋转控制器
 * 
 * @author yuankai
 * @version 1.0
 */
public class OrientationControl {

	public static final int AUTOROTATION = 0;
	public static final int VERTICAL = 1;
	public static final int HORIZONTAL = 2;
	public static final int IGNOREBORAD = 3;

	public static final int ORIENTATION_REVERSE_PORTRAIT = 9;
	public static final int ORIENTATION_REVERSE_LANDSCAPE = 8;

	private static boolean sSmallModle = false;

	/**
	 * 获取当前手机屏幕方向
	 * 
	 * @param activity
	 *            活动
	 * @return ActivityInfo的常量值
	 */
	public static int getRequestOrientation(Activity activity) {
		return activity.getResources().getConfiguration().orientation;
	}

	/**
	 * 设置当前手机屏幕方向
	 * 
	 * @param activity
	 *            活动
	 * @param requestedOrientation
	 *            要求的方向
	 */
	public static void setRequestOrientation(Activity activity, int requestedOrientation) {
		activity.setRequestedOrientation(requestedOrientation);
	}

	/**
	 * 根据用户数据设置 activity屏幕方向
	 * 
	 * @param activity
	 */
	public static void setOrientation(Activity activity) {
		setOrientation(activity, -1);
	}

	/**
	 * 设置当前手机屏幕方向
	 * 
	 * @param activity
	 *            活动
	 * @param orientationType
	 *            为-1时则读取数据库的状态
	 */
	public static void setOrientation(Activity activity, int orientationType) {
		Configuration configuration = activity.getResources().getConfiguration();
		boolean boradHide = configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES;
		int oriType = orientationType == -1 ? GOLauncherApp.getSettingControler()
				.getGravitySettingInfo().mOrientationType : orientationType;

		if (sSmallModle) {
			oriType = VERTICAL;
		}

		if (oriType == AUTOROTATION) {
			setRequestOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		} else if (oriType == HORIZONTAL) {
			setRequestOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else if (oriType == VERTICAL) {
			if (boradHide || sSmallModle) {
				setRequestOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		} else if (oriType == IGNOREBORAD) {
			// if(boradHide && Configuration.ORIENTATION_UNDEFINED !=
			// configuration.orientation){
			// setRequestOrientation(activity,
			// ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			// }
			// else
			if (/* !boradHide && */Configuration.ORIENTATION_PORTRAIT != configuration.orientation) {
				setRequestOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
		}
	}

	public static void changeOrientationByKeyboard(Activity activity, boolean keyboardOpen,
			Configuration newConfig) {
		if (activity == null) {
			return;
		}
		//
		// // 检查屏幕翻转设置，并应用
		// boolean isGravityEnable = GOLauncherApp.getSettingControler()
		// .getGravitySettingInfo().mEnable;
		// // 检查屏幕翻转设置，并应用
		// int orgType = GOLauncherApp.getSettingControler()
		// .getGravitySettingInfo().mOrientationType;
		// if(islandscape)
		// {
		// setRequestOrientation(activity,
		// ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// }
		// else
		// {
		// if(!isGravityEnable)
		// {
		// if(keyboardOpen && newConfig.orientation !=
		// Configuration.ORIENTATION_LANDSCAPE)
		// {
		// setRequestOrientation(activity,
		// ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		// }
		// else if(!keyboardOpen && newConfig.orientation !=
		// Configuration.ORIENTATION_PORTRAIT)
		// {
		// setRequestOrientation(activity,
		// ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// }
		// }
		// }
		int oriType = GOLauncherApp.getSettingControler().getGravitySettingInfo().mOrientationType;
		if (sSmallModle) {
			oriType = VERTICAL;
			keyboardOpen = false;
		}
		if (oriType != AUTOROTATION) {
			if (keyboardOpen && oriType != IGNOREBORAD
					&& newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
				setRequestOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else if (oriType == VERTICAL && !keyboardOpen && newConfig.orientation != oriType)// 如果原来是竖屏模式，键盘收起后恢复竖屏
			{
				setRequestOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}

			if (/* keyboardOpen && */oriType == IGNOREBORAD
					&& newConfig.orientation != Configuration.ORIENTATION_PORTRAIT) {
				setRequestOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			}
			// else if(!keyboardOpen && oriType == IGNOREBORAD
			// && newConfig.orientation != Configuration.ORIENTATION_UNDEFINED)
			// {
			// setRequestOrientation(activity,
			// ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			// }
		} else if (!keyboardOpen && newConfig.orientation != Configuration.ORIENTATION_UNDEFINED) {
			setRequestOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}
	}

	public static int mapConfigurationOriActivityInfoOri(Activity activity, int configOri) {
		final Display d = activity.getWindowManager().getDefaultDisplay();
		int naturalOri = Configuration.ORIENTATION_LANDSCAPE;
		switch (d.getRotation()) {
			case Surface.ROTATION_0 :
			case Surface.ROTATION_180 :
				// We are currently in the same basic orientation as the natural
				// orientation
				naturalOri = configOri;
				break;
			case Surface.ROTATION_90 :
			case Surface.ROTATION_270 :
				// We are currently in the other basic orientation to the
				// natural
				// orientation
				naturalOri = (configOri == Configuration.ORIENTATION_LANDSCAPE)
						? Configuration.ORIENTATION_PORTRAIT
						: Configuration.ORIENTATION_LANDSCAPE;
				break;
		}

		int[] oriMap = { ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
				ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, ORIENTATION_REVERSE_PORTRAIT,
				ORIENTATION_REVERSE_LANDSCAPE };
		// Since the map starts at portrait, we need to offset if this device's
		// natural orientation
		// is landscape.
		int indexOffset = 0;
		if (naturalOri == Configuration.ORIENTATION_LANDSCAPE) {
			indexOffset = 1;
		}
		return oriMap[(d.getRotation() + indexOffset) % 4];
	}

	/**
	 * 设置是否为屏幕编辑态（小屏幕状态）
	 * 
	 * @param bool
	 */
	public static void setSmallModle(boolean bool) {
		sSmallModle = bool;
	}

	/**
	 * 判断是否为屏幕编辑状态
	 * 
	 * @return
	 */
	public static boolean isSmallModle() {
		return sSmallModle;
	}

	/**
	 * 保持当前屏幕状态
	 * 
	 */
	public static void keepCurrentOrientation(Activity activity) {
		// 2.2以上用这个方法
		int orientation = activity.getResources().getConfiguration().orientation;
		if (Build.VERSION.SDK_INT >= 8) {
			int mOrientation = mapConfigurationOriActivityInfoOri(activity, orientation);
			activity.setRequestedOrientation(mOrientation);
		}
		// 以下用另外的方法2.1
		else {
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
		}
	}
}
