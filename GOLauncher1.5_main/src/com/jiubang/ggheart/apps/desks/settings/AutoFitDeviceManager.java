package com.jiubang.ggheart.apps.desks.settings;

import com.gau.go.launcherex.R;
import com.go.util.device.ConfigurationInfo;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.data.statistics.StaticScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

import android.content.res.Resources;
/**
 * 自适应管理类
 * @author dengdazhong
 *
 */
public class AutoFitDeviceManager {
	public static void autoFit() {
		final int deviceLevel = ConfigurationInfo.getDeviceLevel();
		// 如果是高级配置（内存比重比较大）则开启常驻内存
		if (deviceLevel == ConfigurationInfo.HIGH_DEVICE) {
			StaticScreenSettingInfo.sIsPemanentMemory = true;
		}
		// 如果是低配置（内存比重比较大）则默认屏幕为3屏
		else if (deviceLevel == ConfigurationInfo.LOW_DEVICE) {
			StaticScreenSettingInfo.sNeedDelScreen = true;
		}
		// 默认先从配置读取
		final Resources resources = GOLauncherApp.getContext().getResources();
		StaticScreenSettingInfo.sScreenRow = resources
				.getInteger(R.integer.screen_default_row);
		StaticScreenSettingInfo.sScreenCulumn = resources
				.getInteger(R.integer.screen_default_column);
		StaticScreenSettingInfo.sColRowStyle = resources
				.getInteger(R.integer.screen_col_row_style);
		StaticScreenSettingInfo.sAutofit = resources
				.getBoolean(R.bool.screen_appicon_autofit);
		int width = GoLauncher.isPortait() ? DrawUtils.sWidthPixels : DrawUtils.sHeightPixels;
		int height = GoLauncher.isPortait() ? DrawUtils.sHeightPixels : DrawUtils.sWidthPixels;
		// 再进行行列数的自适应
		final int statusBarHeight = StatusBarHandler.getStatusbarHeight();
		final int indicatorHeight_Port = resources
				.getDimensionPixelSize(R.dimen.dots_indicator_height);
		final int indicatorHeight_Land = resources
				.getDimensionPixelSize(R.dimen.dots_indicator_height_land1);
		final int dockHeight = resources
				.getDimensionPixelSize(R.dimen.dock_bg_height);
		final int cellWidthPort = resources
				.getDimensionPixelSize(R.dimen.cell_width_port_auto_fit);
		final int cellHeightPort = resources
				.getDimensionPixelSize(R.dimen.cell_height_port_auto_fit);
		final float a = ((float) DrawUtils.sHeightPixels / DrawUtils.sWidthPixels) / 1.5f;
		final int textSize = DrawUtils.dip2px(12);
		final int cellHeightLandMin = (int) (resources
				.getDimensionPixelSize(R.dimen.screen_icon_large_size) + textSize * 0.5);
		final int columnPort = width / cellWidthPort;
		final int rowPort = (int) ((height - statusBarHeight
				- indicatorHeight_Port - dockHeight) / (cellHeightPort * a));
		final int columnLand = (height - dockHeight)
				/ cellWidthPort;
		final int rowLand = (width - indicatorHeight_Land - statusBarHeight)
				/ cellHeightLandMin;
		if (columnPort < columnLand) {
			StaticScreenSettingInfo.sScreenCulumn = Math.max(columnPort,
					StaticScreenSettingInfo.sScreenCulumn);
		} else {
			StaticScreenSettingInfo.sScreenCulumn = Math.max(columnLand,
					StaticScreenSettingInfo.sScreenCulumn);
		}
		if (rowPort < rowLand) {
			StaticScreenSettingInfo.sScreenRow = Math.max(rowPort,
					StaticScreenSettingInfo.sScreenRow);
		} else {
			StaticScreenSettingInfo.sScreenRow = Math.max(rowLand,
					StaticScreenSettingInfo.sScreenRow);
		}
		final int column = StaticScreenSettingInfo.sScreenCulumn;
		final int row = StaticScreenSettingInfo.sScreenRow;
		if (column == 4 && row == 4) {
			StaticScreenSettingInfo.sColRowStyle = 1;
		} else if (column == 5 && row == 4) {
			StaticScreenSettingInfo.sColRowStyle = 2;
		} else if (column == 5 && row == 5) {
			StaticScreenSettingInfo.sColRowStyle = 3;
		} else {
			StaticScreenSettingInfo.sColRowStyle = 4;
		}
	}
}
