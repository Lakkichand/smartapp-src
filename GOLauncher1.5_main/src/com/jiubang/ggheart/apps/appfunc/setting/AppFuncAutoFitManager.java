package com.jiubang.ggheart.apps.appfunc.setting;

import android.content.Context;

import com.gau.go.launcherex.R;
import com.go.util.Utilities;
import com.go.util.device.ConfigurationInfo;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.common.component.AppSingleLineContainer;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 功能表模块自适应管理员
 * @author yangguanxiang
 *
 */
public class AppFuncAutoFitManager {
	private static final String NEED_HIDE_GO_MUSIC_PLAYER = "NEED_HIDE_GO_MUSIC_PLAYER";
	private static final String NEED_HIDE_GO_IMAGE_BROWSER = "NEED_HIDE_GO_IMAGE_BROWSER";
	private int mAppDrawerRowsH = -1;
	private int mAppDrawerColumnsH = -1;
	private int mAppDrawerRowsV = -1;
	private int mAppDrawerColumnsV = -1;
	private int mFolderQuickAddBarItemCountV = -1;
	private int mFolderQuickAddBarItemCountH = -1;
	private static AppFuncAutoFitManager sInstance;
	private static final int ICON_STANDARD_SIZE = 56;
	private Context mContext;
	private PreferencesManager mPrefManager;
	private AppFuncUtils mUtils;

	/**
	 * 是否低端手机
	 */
	private boolean mIsLowDevice = false;

	private AppFuncAutoFitManager(Context context) {
		mContext = context;
		mPrefManager = new PreferencesManager(mContext);
		mUtils = AppFuncUtils.getInstance(context);
		if (ConfigurationInfo.getDeviceLevel() == ConfigurationInfo.LOW_DEVICE) {
			mIsLowDevice = true;
		}
	}

	public static AppFuncAutoFitManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new AppFuncAutoFitManager(context);
		}
		return sInstance;
	}

	/**
	 * 自适应所有配置
	 */
	public void autoFitDeviceSetting() {
		autoFitAppDrawer();
		autoFitInOutEffect();
		autoFitMediaManagementGrid();
		autoFitImageBrowser();
		autoFitMusicPlayer();
	}

	/**
	 * 自适应功能表行列数
	 */
	private void autoFitAppDrawer() {
		final boolean isVertical = GoLauncher.isPortait();
		int tabSize = mUtils.getDimensionPixelSize(isVertical
				? R.dimen.appfunc_tabheight_v
				: R.dimen.appfunc_tabheight_h);
		int actionBarSize = mUtils.getDimensionPixelSize(R.dimen.appfunc_home_heigth);
		int indicatorSize = mUtils.getDimensionPixelSize(R.dimen.appfunc_indicator_height);
		int iconStandardSize = DrawUtils.dip2px(ICON_STANDARD_SIZE);
		int iconHeight = Math.round(1.0f * Utilities.getStandardIconSize(mContext)
				* mUtils.getDimensionPixelSize(R.dimen.appfunc_icon_height_auto_fit)
				/ iconStandardSize);
		int iconWidth = Math.round(1.0f * Utilities.getStandardIconSize(mContext)
				* mUtils.getDimensionPixelSize(R.dimen.appfunc_icon_width_auto_fit)
				/ iconStandardSize);
		int gridHeightV = (isVertical ? GoLauncher.getScreenHeight() : GoLauncher.getScreenWidth())
				- tabSize - actionBarSize - indicatorSize;
		int gridWidthV = isVertical ? GoLauncher.getScreenWidth() : GoLauncher.getScreenHeight();
		mAppDrawerRowsV = gridHeightV / iconHeight;
		mAppDrawerColumnsV = gridWidthV / iconWidth;
		mFolderQuickAddBarItemCountV = (gridWidthV - 2 * mUtils
				.getStandardSize(AppSingleLineContainer.PADDING_H))
				/ mUtils.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_grid_size);

		int heightH = isVertical ? GoLauncher.getScreenWidth() : GoLauncher.getScreenHeight();
		int gridHeightH = heightH - indicatorSize;
		int gridWidthH = (isVertical ? GoLauncher.getScreenHeight() : GoLauncher.getScreenWidth())
				- tabSize - actionBarSize;
		mAppDrawerRowsH = gridHeightH / iconHeight;
		mAppDrawerColumnsH = gridWidthH / iconWidth;
		mFolderQuickAddBarItemCountH = (heightH - 2 * mUtils
				.getStandardSize(AppSingleLineContainer.PADDING_H))
				/ mUtils.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_grid_size);

	}

	/**
	 * <br>功能简述:功能表进出特效自适应
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private void autoFitInOutEffect() {
		FunAppSetting setting = GOLauncherApp.getSettingControler().getFunAppSetting();
		int inOutEffect = AppSettingDefault.INOUTEFFECT;
		if (!AppSettingDefault.APPFUNC_OPEN_EFFECT) {
			inOutEffect = 6;
		}

		setting.setInOutEffect(inOutEffect);
	}

	/**
	 * 自适应资源管理行列数
	 */
	private void autoFitMediaManagementGrid() {

	}

	/**
	 * 自适应是否隐藏GO图片浏览器
	 */
	private void autoFitImageBrowser() {
		// 当手机为低端手机且为全新安装的时候，不显示GO音乐播放器和GO图片浏览器
		if (mPrefManager != null) {
			if (mIsLowDevice) {
				mPrefManager.putBoolean(NEED_HIDE_GO_IMAGE_BROWSER, true);
				mPrefManager.commit();
			}
		}
	}

	/**
	 * 自适应是否隐藏GO音乐播放器
	 */
	private void autoFitMusicPlayer() {
		// 当手机为低端手机且为全新安装的时候，不显示GO音乐播放器和GO图片浏览器
		if (mPrefManager != null) {
			if (mIsLowDevice) {
				mPrefManager.putBoolean(NEED_HIDE_GO_MUSIC_PLAYER, true);
				mPrefManager.commit();
			}
		}
	}

	/**
	 * 是否需要隐藏GO图片浏览器
	 */
	public boolean needHideImageBrowser() {
		if (mIsLowDevice && mPrefManager != null) {
			return mPrefManager.getBoolean(NEED_HIDE_GO_IMAGE_BROWSER, false);
		}
		return false;
	}

	/**
	 * 是否需要隐藏GO音乐播放器
	 */
	public boolean needHideMusicPlayer() {
		if (mIsLowDevice && mPrefManager != null) {
			return mPrefManager.getBoolean(NEED_HIDE_GO_MUSIC_PLAYER, false);
		}
		return false;
	}

	/**
	 * 获取功能表自适应行（竖屏）
	 */
	public int getAppDrawerRowsV() {
		if (mAppDrawerRowsV == -1) {
			autoFitAppDrawer();
		}
		return mAppDrawerRowsV;
	}

	/**
	 * 获取功能表自适应列（竖屏）
	 */
	public int getAppDrawerColumnsV() {
		if (mAppDrawerColumnsV == -1) {
			autoFitAppDrawer();
		}
		return mAppDrawerColumnsV;
	}

	/**
	 * 获取功能表自适应行（横屏）
	 */
	public int getAppDrawerRowsH() {
		if (mAppDrawerRowsH == -1) {
			autoFitAppDrawer();
		}
		return mAppDrawerRowsH;
	}

	/**
	 * 获取功能表自适应列（横屏）
	 */
	public int getAppDrawerColumnsH() {
		if (mAppDrawerColumnsH == -1) {
			autoFitAppDrawer();
		}
		return mAppDrawerColumnsH;
	}

	/**
	 * 获取文件夹快捷栏一版的元素个数（竖屏）
	 */
	public int getFolderQuickAddBarItemCountV() {
		if (mFolderQuickAddBarItemCountV == -1) {
			autoFitAppDrawer();
		}
		return mFolderQuickAddBarItemCountV;
	}

	/**
	 * 获取文件夹快捷栏一版的元素个数（横屏）
	 */
	public int getFolderQuickAddBarItemCountH() {
		if (mFolderQuickAddBarItemCountH == -1) {
			autoFitAppDrawer();
		}
		return mFolderQuickAddBarItemCountH;
	}
}
