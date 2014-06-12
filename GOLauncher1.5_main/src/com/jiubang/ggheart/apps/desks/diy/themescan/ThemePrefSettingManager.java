package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.Setting;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.ScreenStyleConfigInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.broadcastReceiver.MyThemeReceiver;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 判断主题是否被修改
 * 
 * 
 * @author yangbing
 * */
public class ThemePrefSettingManager {

	private Context mContext;
	private GoSettingControler mSettingControler;
	private ScreenStyleConfigInfo mScreenStyleConfigInfo;
	private String mPackageName;
	private String mCurPackageName; // 当前正在使用的主题
	private boolean mIsAppNameColorModifyed; // 程序名颜色设置

	private boolean mIsShortCutBgModifyed; // 快捷条背景
	private boolean mIsFuncBgModifyed; // 功能表背景
	private boolean mIsTabHomeBgModifyed; // 选项卡和底座背景
	private boolean mIsIconStyleModifyed; // 主题图标
	private boolean mIsShortCutStyleModifyed; // 快捷条图标风格
	private boolean mIsFolderStyleModifyed; // 文件夹图标风格
	private boolean mIsGGMenuStyleModifyed; // 菜单项图标风格
	private boolean mIsIndicatorStyleModifyed; // 指示器风格

	public ThemePrefSettingManager(Context mContext, String packageName) {
		this.mContext = mContext;
		this.mPackageName = packageName;
		mSettingControler = GOLauncherApp.getSettingControler();
		mScreenStyleConfigInfo = mSettingControler.getScreenStyleSettingInfo();
		mCurPackageName = ThemeManager.getInstance(mContext).getCurThemePackage();
	}

	/**
	 * 判断主题是否被修改
	 * 
	 * @param packageName
	 * */
	public boolean isModifyed() {

		mIsIconStyleModifyed = isIconStyleModifyed();
		mIsShortCutStyleModifyed = isShortCutStyleModifyed();
		mIsFolderStyleModifyed = isFolderStyleModifyed();
		mIsGGMenuStyleModifyed = isGGMenuStyleModifyed();
		mIsIndicatorStyleModifyed = isIndicatorStyleModifyed();
		mIsTabHomeBgModifyed = isTabHomeBgModifyed();
		mIsShortCutBgModifyed = isShortCutBgModifyed();
		mIsFuncBgModifyed = isFuncBgModifyed();
		// mIsAppNameColorModifyed = isAppNameColorModifyed();

		return mIsIconStyleModifyed || mIsShortCutStyleModifyed || mIsFolderStyleModifyed
				|| mIsGGMenuStyleModifyed || mIsIndicatorStyleModifyed || mIsShortCutBgModifyed
				|| mIsFuncBgModifyed || mIsTabHomeBgModifyed;

	}

	private boolean isShortCutStyleModifyed() {
		String shortCutStyle = mSettingControler.getShortCutStyleByPackage(mPackageName);
		if (ThemeManager.DEFAULT_THEME_PACKAGE.equals(mPackageName)
				&& "defaultstyle".equals(shortCutStyle)) {
			return false;
		}
		if (shortCutStyle == null || mPackageName.equals(shortCutStyle)) {

			return false;
		}
		return true;

	}

	/**
	 * 程序名称颜色
	 * */
	private boolean isAppNameColorModifyed() {
		DesktopSettingInfo desktopSettingInfo = mSettingControler.getDesktopSettingInfo();
		if (!desktopSettingInfo.mCustomTitleColor) {
			mIsAppNameColorModifyed = true;
			return true;
		}
		return false;
	}

	/**
	 * 指示器风格
	 * */
	private boolean isIndicatorStyleModifyed() {
		String indicatorStyle = mScreenStyleConfigInfo.getStyleSetting(mPackageName,
				Setting.INDICATOR);
		if (indicatorStyle == null || mPackageName.equals(indicatorStyle)) {

			return false;
		}
		return true;
	}

	/**
	 * 菜单项图标风格
	 * */
	private boolean isGGMenuStyleModifyed() {
		String ggmenuStyle = mScreenStyleConfigInfo.getStyleSetting(mPackageName,
				Setting.GGMENUPACKAGE);
		if (ggmenuStyle == null || mPackageName.equals(ggmenuStyle)
				|| ThemeManager.DEFAULT_THEME_PACKAGE.equals(ggmenuStyle)) {

			return false;
		}
		return true;
	}

	/**
	 * 文件夹图标风格
	 * */
	private boolean isFolderStyleModifyed() {
		String folderStyle = mScreenStyleConfigInfo.getStyleSetting(mPackageName,
				Setting.FOLDERSTYLEPACKAGE);
		if (folderStyle == null || mPackageName.equals(folderStyle)) {
			mIsFolderStyleModifyed = true;
			return false;
		}
		return true;
	}

	/**
	 * 主题图标
	 * */
	private boolean isIconStyleModifyed() {
		String iconStyle = mScreenStyleConfigInfo.getStyleSetting(mPackageName,
				Setting.ICONSTYLEPACKAGE);
		if (iconStyle == null || mPackageName.equals(iconStyle)) {

			return false;
		}
		return true;
	}

	/**
	 * 选项卡和底座背景
	 * */
	private boolean isTabHomeBgModifyed() {
		FunAppSetting mFunAppSetting = mSettingControler.getFunAppSetting();
		String bgName = mFunAppSetting.getAppFuncSetting(mPackageName, Setting.TAB_HOME_BG, null);
		if (bgName == null || mPackageName.equals(bgName)) {
			return false;
		}
		return true;
	}

	/**
	 * 功能表背景是否被修改
	 * */
	private boolean isFuncBgModifyed() {
		FunAppSetting mFunAppSetting = mSettingControler.getFunAppSetting();
		String mFuncBg = mFunAppSetting.getAppFuncSetting(mPackageName, Setting.BGVISIABLE, null);
		if (mFuncBg == null || FunAppSetting.BG_DEFAULT == Integer.parseInt(mFuncBg)) {
			return false;
		}
		return true;
	}

	/**
	 * 快捷条背景是否修改
	 * */
	private boolean isShortCutBgModifyed() {
		ShortCutSettingInfo shortCutSettingInfo = mSettingControler
				.getShortCutSettingInfoByPackageName(mPackageName);
		if (!shortCutSettingInfo.mBgPicSwitch) {
			// 无背景
			return true;
		}
		if (shortCutSettingInfo.mBgtargetthemename == null
				&& shortCutSettingInfo.mBgresname == null) {
			return false;
		}
		if (mPackageName.equals(shortCutSettingInfo.mBgtargetthemename)) {
			return false;
		}
		return true;
	}

	/**
	 * 恢复主题到默认设置
	 * 
	 * */
	public void resetSetting() {

		if (mIsIconStyleModifyed) {
			resetIconStyle();
		}
		if (mIsTabHomeBgModifyed) {
			resetTabHomeBg();
		}
		if (mIsShortCutStyleModifyed) {
			resetShortCutStyle();
		}
		if (mIsFolderStyleModifyed) {
			resetFolderStyle();
		}
		if (mIsGGMenuStyleModifyed) {
			resetGGMenuStyle();
		}
		if (mIsIndicatorStyleModifyed) {
			resetIndicatorStyle();
		}
		if (mIsShortCutBgModifyed) {
			resetShortCutBg();
		}
		if (mIsFuncBgModifyed) {
			resetFuncBg();
		}
		// if (mIsAppNameColorModifyed) {
		// resetAppNameColor();
		// }
	}

	private void resetTabHomeBg() {
		FunAppSetting mFunAppSetting = mSettingControler.getFunAppSetting();
		mFunAppSetting.setAppFuncSetting(mPackageName, Setting.TAB_HOME_BG, mPackageName);
		if (mPackageName.equals(mCurPackageName)) {
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING,
					IDiyMsgIds.APPDRAWER_TAB_HOME_THEME_CHANGE);
			mContext.sendBroadcast(intent);
		}

	}

	private void resetFuncBg() {
		FunAppSetting mFunAppSetting = mSettingControler.getFunAppSetting();
		mFunAppSetting.setAppFuncSetting(mPackageName, Setting.BGVISIABLE,
				String.valueOf(FunAppSetting.BG_DEFAULT));
		if (mPackageName.equals(mCurPackageName)) {
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, FunAppSetting.INDEX_BGSWITCH);
			mContext.sendBroadcast(intent);

		}
	}

	private void resetAppNameColor() {
		DesktopSettingInfo desktopSettingInfo = mSettingControler.getDesktopSettingInfo();
		if (desktopSettingInfo != null) {
			desktopSettingInfo.mCustomTitleColor = false;
			desktopSettingInfo.mTitleColor = 0xffffffff;
			mSettingControler.updateDesktopSettingInfo(desktopSettingInfo);
		}

	}

	private void resetIndicatorStyle() {
		mScreenStyleConfigInfo.setStyleSetting(mPackageName, Setting.INDICATOR, mPackageName);
		mSettingControler.getFunAppSetting().setAppFuncSetting(mPackageName,
				Setting.INDICATOR_STYLE, mPackageName);
		if (mPackageName.equals(mCurPackageName)) {
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING,
					IDiyMsgIds.REFRESH_SCREENINDICATOR_THEME);
			mContext.sendBroadcast(intent);

		}

	}

	private void resetGGMenuStyle() {
		mScreenStyleConfigInfo.setStyleSetting(mPackageName, Setting.GGMENUPACKAGE, mPackageName);
		if (mPackageName.equals(mCurPackageName)) {
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, IDiyMsgIds.REFRESH_GGMENU_THEME);
			mContext.sendBroadcast(intent);
		}

	}

	private void resetFolderStyle() {
		mScreenStyleConfigInfo.setStyleSetting(mPackageName, Setting.FOLDERSTYLEPACKAGE,
				mPackageName);
		if (mPackageName.equals(mCurPackageName)) {
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, IDiyMsgIds.REFRESH_FOLDER_THEME);
			mContext.sendBroadcast(intent);

		}

	}

	private void resetIconStyle() {
		mScreenStyleConfigInfo
				.setStyleSetting(mPackageName, Setting.ICONSTYLEPACKAGE, mPackageName);
		if (mPackageName.equals(mCurPackageName)) {
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, IDiyMsgIds.REFRESH_SCREENICON_THEME);
			mContext.sendBroadcast(intent);
		}
	}

	/**
	 * 重置快捷条背景
	 * */
	private void resetShortCutBg() {
		mSettingControler.resetShortCutBg(mPackageName, mPackageName, "dock");
		if (mPackageName.equals(mCurPackageName)) {
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, IDiyMsgIds.UPDATE_DOCK_BG);
			mContext.sendBroadcast(intent);

		}
	}

	/**
	 * 重置快捷条图标风格
	 * */
	private void resetShortCutStyle() {
		mSettingControler.updateShortCutStyleByPackage(mPackageName, mPackageName);
		if (mPackageName.equals(mCurPackageName)) {
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING,
					IDiyMsgIds.DOCK_SETTING_CHANGED_STYLE);
			mContext.sendBroadcast(intent);

		}
	}

}
