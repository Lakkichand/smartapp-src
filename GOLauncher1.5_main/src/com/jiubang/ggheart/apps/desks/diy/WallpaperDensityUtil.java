package com.jiubang.ggheart.apps.desks.diy;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.util.Log;
import android.view.Display;

import com.go.util.graphics.DrawUtils;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class WallpaperDensityUtil {
	static final int WALLPAPER_SCREENS_SPAN_WIDTH_1 = 1;
	static final int WALLPAPER_SCREENS_SPAN_WIDTH_2 = 2;

	/**
	 * 试图解决壁纸缩放问题 说明：如果是关了“壁纸滚动”设置且为竖屏,就单屏壁纸，其他情况为双屏壁纸
	 */
	public static void setWallpaperDimension(Activity activity) {
		if (null == activity) {
			return;
		}
		boolean isWallpaperScroll = GOLauncherApp.getSettingControler().getScreenSettingInfo().mWallpaperScroll;
		WallpaperManager wpm = (WallpaperManager) activity
				.getSystemService(Context.WALLPAPER_SERVICE);

		Display display = activity.getWindowManager().getDefaultDisplay();
		boolean isPortrait = display.getWidth() < display.getHeight();

		final int width = isPortrait ? display.getWidth() : display.getHeight();
		final int height = DrawUtils.sStatusHeight
				+ (isPortrait ? display.getHeight() : display.getWidth());
		final int wallpaperSpan = (!isWallpaperScroll && isPortrait)
				? WALLPAPER_SCREENS_SPAN_WIDTH_1
				: WALLPAPER_SCREENS_SPAN_WIDTH_2;
		try {
			wpm.suggestDesiredDimensions(width * wallpaperSpan, height);
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "fail to setWallpaperDimension");
		}
	}
}
