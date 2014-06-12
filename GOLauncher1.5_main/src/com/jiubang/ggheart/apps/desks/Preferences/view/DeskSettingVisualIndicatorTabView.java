package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.util.AttributeSet;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ScreenStyleConfigInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-9-7]
 */
public class DeskSettingVisualIndicatorTabView extends DeskSettingVisualAbsTabView {

	//设置信息
	private ScreenStyleConfigInfo mScreenStyleInfo;
	private ScreenSettingInfo mScreenInfo;
	private FunAppSetting mFunAppSetting;

	//主题信息
	String[] mAllThemePackage; //包名
	String[] mAllThemeName; //主题名

	//views
	private DeskSettingItemListView mMode; //指示器风格
	private DeskSettingItemListView mPosition; //指示器位置
	private DeskSettingItemListView mScreenIndicator; //桌面指示器

	public DeskSettingVisualIndicatorTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setInfos(ScreenSettingInfo screenSettingInfo, FunAppSetting funAppSetting,
			ScreenStyleConfigInfo screenStyleConfigInfo) {
		mScreenInfo = screenSettingInfo;
		mFunAppSetting = funAppSetting;
		mScreenStyleInfo = screenStyleConfigInfo;
	}

	@Override
	protected void findView() {
		mMode = (DeskSettingItemListView) findViewById(R.id.mode);
		mMode.setOnValueChangeListener(this);
		mPosition = (DeskSettingItemListView) findViewById(R.id.position);
		mPosition.setOnValueChangeListener(this);
		mScreenIndicator = (DeskSettingItemListView) findViewById(R.id.screen_indicator);
		mScreenIndicator.setOnValueChangeListener(this);
	}

	@Override
	public void load() {
		if (null != mScreenInfo) {
			DeskSettingConstants.updateSingleChoiceListView(mScreenIndicator,
					getScreenIndicatorValue());
			DeskSettingConstants.updateSingleChoiceListView(mPosition,
					mScreenInfo.mIndicatorPosition);
		}
	}

	@Override
	public void save() {
		boolean bChanged = false;

		String indicatormode = mMode.getDeskSettingInfo().getSingleInfo().getSelectValue();
		String indicatorPackage = mScreenStyleInfo.getIndicatorStyle();
		String indicatorposition = mPosition.getDeskSettingInfo().getSingleInfo().getSelectValue();
		String screenindicator = mScreenIndicator.getDeskSettingInfo().getSingleInfo()
				.getSelectValue();

		if (!getScreenIndicatorValue().equals(screenindicator)) {
			setScreenIndicatorValue(screenindicator);
			bChanged = true;
		}

		// 设置指示器位置
		if (!mScreenInfo.mIndicatorPosition.equals(indicatorposition)) {
			mScreenInfo.mIndicatorPosition = indicatorposition;
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.SCREEN_INDICRATOR_POSITION, -1, indicatorposition, null);
			//3D插件
			GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
					IDiyMsgIds.SCREEN_INDICRATOR_POSITION, -1, indicatorposition, null);

			bChanged = true;
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.INDICATOR_CHANGE_POSITION, -1, indicatorposition, null);
		}

		// 设置指示器模式
		if (!indicatorPackage.equals(indicatormode)) {
			mScreenStyleInfo.setIndicatorStyle(indicatormode);
			mFunAppSetting.setIndicatorSetting(indicatormode);
			GoLauncher.sendBroadcastHandler(this, IDiyMsgIds.REFRESH_SCREENINDICATOR_THEME, -1,
					null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
					IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
		}

		if (bChanged) {
			GOLauncherApp.getSettingControler().updateScreenSettingInfo(mScreenInfo);
		}
	}

	/**
	 * <br>功能简述:获取桌面指示器菜单项的值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private String getScreenIndicatorValue() {
		String items[] = getResources().getStringArray(R.array.screen_indicator_values);
		if (items != null && items.length >= 3) {
			if (mScreenInfo.mAutoHideIndicator) {
				return items[2];
			} else if (mScreenInfo.mEnableIndicator) {
				return items[0];
			} else {
				return items[1];
			}
		}

		return "";
	}

	/**
	 * <br>功能简述:设置桌面指示器菜单项的值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param value
	 */
	private void setScreenIndicatorValue(String value) {
		String items[] = getResources().getStringArray(R.array.screen_indicator_values);
		mScreenInfo.mEnableIndicator = true;
		mScreenInfo.mAutoHideIndicator = false;
		if (items != null && items.length >= 3) {
			if (value.equals(items[2])) {
				mScreenInfo.mAutoHideIndicator = true;
			} else if (value.equals(items[0])) {
				mScreenInfo.mEnableIndicator = true;
			} else {
				mScreenInfo.mEnableIndicator = false;
			}
		}
	}

	/**
	 * <br>功能简述:设置指示器风格数据
	 * <br>功能详细描述:
	 * <br>注意:在主题数据扫描完成后设置
	 */
	private void initDeskIndicatorList() {
		int size = mAllThemeName.length;
		int curThemeIndex = 0;
		String curThemePkg = ThemeManager.getInstance(getContext()).getCurThemePackage(); // 当前主题

		String[] tmpEntris = getResources().getStringArray(R.array.screen_indicator_showmode_array);
		String[] tmpValues = getResources()
				.getStringArray(R.array.screen_indicator_showmode_values);
		String[] values = new String[size + tmpValues.length];
		String[] entries = new String[size + tmpEntris.length];
		if (tmpValues.length != tmpEntris.length) {
			return;
		}
		for (int i = 0; i < tmpEntris.length; i++) {
			entries[i] = tmpEntris[i];
			values[i] = tmpValues[i];
		}

		if (curThemePkg.equals(LauncherEnv.PACKAGE_NAME)) {
			curThemeIndex = 0;
		}

		for (int i = 0; i < mAllThemePackage.length; i++) {

			values[i + tmpEntris.length] = mAllThemePackage[i];
			if (curThemePkg.equals(values[i + tmpEntris.length])) {
				curThemeIndex = i + tmpEntris.length;
			}
		}

		mMode.getDeskSettingInfo().getSingleInfo().setEntryValues(values);

		if (0 == curThemeIndex) {
			entries[0] = entries[0] + "(" + getResources().getString(R.string.current) + ")";
		}

		for (int i = 0; i < mAllThemeName.length; i++) {
			entries[i + tmpValues.length] = mAllThemeName[i];
			if (i + tmpValues.length == curThemeIndex) {
				entries[i + tmpValues.length] = entries[i + tmpValues.length] + "("
						+ getResources().getString(R.string.current) + ")";
			}
		}

		mMode.getDeskSettingInfo().getSingleInfo().setEntries(entries);

		if (null != mScreenInfo) {
			DeskSettingConstants.updateSingleChoiceListView(mMode,
					mScreenStyleInfo.getIndicatorStyle());
		}
	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView baseView, Object value) {
		DeskSettingConstants.updateSingleChoiceListView((DeskSettingItemListView) baseView,
				(String) value);

		return true;
	}

	/**
	 * <br>功能简述:设置所有主题包数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param themePkgs
	 * @param themeNames
	 */
	public void setThemesData(String[] themePkgs, String[] themeNames) {
		mAllThemePackage = themePkgs;
		mAllThemeName = themeNames;

		initDeskIndicatorList();
	}

	public void changeOrientation() {
//		mMode.dismissDialog();
//		mPosition.dismissDialog();
//		mScreenIndicator.dismissDialog();
	}
}