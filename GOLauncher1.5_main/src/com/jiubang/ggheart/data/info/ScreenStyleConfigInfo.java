package com.jiubang.ggheart.data.info;

import android.content.Context;

import com.jiubang.ggheart.data.Setting;
import com.jiubang.ggheart.data.model.GoSettingDataModel;
import com.jiubang.ggheart.data.theme.ThemeManager;

public class ScreenStyleConfigInfo {

	private GoSettingDataModel mDataModel;
	private ThemeManager mThemeManager;

	public ScreenStyleConfigInfo(Context context, GoSettingDataModel dataModel,
			ThemeManager themeManager) {

		mDataModel = dataModel;
		mThemeManager = themeManager;
		initSetting();
	}

	public void initSetting() {
		// 主题相关
		if (mThemeManager != null) {
			// mDataModel.addScreenStyleSetting(mThemeManager.getCurThemePackage());
			mDataModel.addScreenStyleSetting(mThemeManager.getCurThemePackage());
		} else {
			mDataModel.addScreenStyleSetting(ThemeManager.DEFAULT_THEME_PACKAGE);
		}
	}

	public void setIconStyle(String stylePackage) {
		setStyleSetting(mThemeManager.getCurThemePackage(), Setting.ICONSTYLEPACKAGE, stylePackage);
	}

	public void setFolderStyle(String stylePackage) {
		setStyleSetting(mThemeManager.getCurThemePackage(), Setting.FOLDERSTYLEPACKAGE,
				stylePackage);
	}

	public void setGGmenuStyle(String stylePackage) {
		setStyleSetting(mThemeManager.getCurThemePackage(), Setting.GGMENUPACKAGE, stylePackage);
	}

	public void setIndicatorStyle(String stylePackage) {
		setStyleSetting(mThemeManager.getCurThemePackage(), Setting.INDICATOR, stylePackage);
	}

	public void setStyleSetting(final String packageName, final int settingItem, final String value) {
		mDataModel.updateScreenStyleSetting(packageName, settingItem, value);
	}

	public String getIconStyle() {
		String str = null;
		if (null != mThemeManager) {
			// 其他主题
			str = getStyleSetting(mThemeManager.getCurThemePackage(), Setting.ICONSTYLEPACKAGE);
		} else {
			// 默认主题
			str = getStyleSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.ICONSTYLEPACKAGE);
		}

		if (null == str) {
			return ThemeManager.DEFAULT_THEME_PACKAGE;
		}

		return str;
	}

	public String getFolderStyle() {
		String str = null;
		if (null != mThemeManager) {
			// 其他主题
			str = getStyleSetting(mThemeManager.getCurThemePackage(), Setting.FOLDERSTYLEPACKAGE);
		} else {
			// 默认主题
			str = getStyleSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.FOLDERSTYLEPACKAGE);
		}

		if (null == str) {
			return ThemeManager.DEFAULT_THEME_PACKAGE;
		}

		return str;
	}

	public String getGGmenuStyle() {
		String str = null;
		if (null != mThemeManager) {
			// 其他主题
			str = getStyleSetting(mThemeManager.getCurThemePackage(), Setting.GGMENUPACKAGE);
		} else {
			// 默认主题
			str = getStyleSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.GGMENUPACKAGE);
		}

		if (null == str) {
			return ThemeManager.DEFAULT_THEME_PACKAGE;
		}

		return str;
	}

	public String getIndicatorStyle() {
		String str = null;
		if (null != mThemeManager) {
			// 其他主题
			str = getStyleSetting(mThemeManager.getCurThemePackage(), Setting.INDICATOR);
		} else {
			// 默认主题
			str = getStyleSetting(ThemeManager.DEFAULT_THEME_PACKAGE, Setting.INDICATOR);
		}

		if (null == str) {
			return ThemeManager.DEFAULT_THEME_PACKAGE;
		}

		return str;
	}

	public String getStyleSetting(final String packageName, final int settingItem) {
		return mDataModel.getScreenStyleSetting(packageName, settingItem);
	}

}
