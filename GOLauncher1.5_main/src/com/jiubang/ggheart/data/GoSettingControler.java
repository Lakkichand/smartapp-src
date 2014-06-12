package com.jiubang.ggheart.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.jiubang.core.framework.ICleanable;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.data.info.DeskMenuSettingInfo;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.GravitySettingInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ScreenStyleConfigInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.model.GoSettingDataModel;
import com.jiubang.ggheart.data.theme.ThemeManager;

//CHECKSTYLE:OFF
public class GoSettingControler extends Controler implements ICleanable {
	// 桌面设置
	private DeskMenuSettingInfo mDeskMenuSettingInfo; // 弹出菜单设置
	private DesktopSettingInfo mDesktopSettingInfo; // 桌面设置
	private EffectSettingInfo mEffectSettingInfo; // 桌面特效设置
	private GravitySettingInfo mGravitySettingInfo; // 重力感应设置
	private ScreenSettingInfo mScreenSettingInfo; // 屏幕设置
	private ThemeSettingInfo mThemeSettingInfo; // 主题设置

	// 手势设置
	private GestureSettingInfo mHomeGestureSettingInfo; // home键设置
	private GestureSettingInfo mUpGestureSettingInfo; // 上滑手势
	private GestureSettingInfo mDownGestureSettingInfo; // 下滑手势
	private GestureSettingInfo mDoubleClickGestureSettingInfo; // 双击空白处手势

	// 快捷条设置
	private ShortCutSettingInfo mDockSettingInfo;

	private ScreenStyleConfigInfo mScreenStyleConfigInfo;

	// 字体
	private FontBean mUsedFontBean;

	// 功能表
	private FunAppSetting mFunAppSetting;
	private GoSettingDataModel mSettingDataModel = null;

	private static GoSettingControler sInstance = null;
	private Context mContext = null;

	public static GoSettingControler getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new GoSettingControler(context);
		}
		return sInstance;
	}

	private GoSettingControler(Context context) {
		super(context);
		mContext = context;
		mSettingDataModel = new GoSettingDataModel(context);
		mFunAppSetting = new FunAppSetting(context, mSettingDataModel,
				ThemeManager.getInstance(context));
	}

	public FunAppSetting getFunAppSetting() {
		return mFunAppSetting;
	}

	@Override
	public void cleanup() {
		mContext = null;
		clearAllObserver();
	}

	// 桌面菜单设置控制
	public DeskMenuSettingInfo getDeskMenuSettingInfo() {
		if (null == mDeskMenuSettingInfo) {
			mDeskMenuSettingInfo = createDeskMenuSettingInfo();
		}
		return mDeskMenuSettingInfo;
	}

	public DeskMenuSettingInfo createDeskMenuSettingInfo() {
		DeskMenuSettingInfo info = mSettingDataModel.getDeskMenuSettingInfo();
		if (null == info) {
			info = new DeskMenuSettingInfo();
			mSettingDataModel.cleanDeskMenuSettingInfo();
			mSettingDataModel.insertDeskMenuSettingInfo(info);
		}
		return info;
	}

	public void updateDeskMenuSettingInfo(DeskMenuSettingInfo info) {
		mDeskMenuSettingInfo = info;
		broadCast(IDiyMsgIds.APPCORE_DATACHANGE, DataType.DATATYPE_DESKMENUSETTING, info, null);
		mSettingDataModel.updateDeskMenuSettingInfo(info);
	}

	// 桌面设置
	public DesktopSettingInfo getDesktopSettingInfo() {
		if (null == mDesktopSettingInfo) {
			mDesktopSettingInfo = createDesktopSettingInfo();
		}
		return mDesktopSettingInfo;
	}

	public DesktopSettingInfo createDesktopSettingInfo() {
		DesktopSettingInfo info = mSettingDataModel.getDesktopSettingInfo();
		if (null == info) {
			info = new DesktopSettingInfo();
			mSettingDataModel.cleanDesktopSettingInfo();
			mSettingDataModel.insertDesktopSettingInfo(info);
		}
		return info;
	}

	public void updateDesktopSettingInfo(DesktopSettingInfo info) {
		updateDesktopSettingInfo(info, true);
	}

	public void updateDesktopSettingInfo(DesktopSettingInfo info, boolean broadCast) {
		// boolean bReCreatGGmenu=false;
		// 下面没有保存主题风格，考虑到耗时操作，这里把它细分开来
		// if (null != info.mThemeIconStylePackage)
		// {
		// if (!info.mThemeIconStylePackage
		// .equals(mDesktopSettingInfo.mThemeIconStylePackage))
		// {
		// // 主题风格变化
		// AppDataEngine.getInstance(mContext).onHandleThemeIconStyleChanged(info.mThemeIconStylePackage);
		// }
		// }
		// if (null != info.mFolderThemeIconStylePackage)
		// {
		// if (!info.mFolderThemeIconStylePackage
		// .equals(mDesktopSettingInfo.mFolderThemeIconStylePackage))
		// {
		// // 主题风格变化
		// AppDataEngine.getInstance(mContext).onHandleFolderThemeIconStyleChanged(info.mFolderThemeIconStylePackage);
		//
		// }
		// }
		// if (null != info.mGGmenuThemeIconStylePackage)
		// {
		// if (!info.mGGmenuThemeIconStylePackage
		// .equals(mDesktopSettingInfo.mGGmenuThemeIconStylePackage))
		// {
		// // 主题风格变化
		// bReCreatGGmenu = true;
		// }
		// }
		mDesktopSettingInfo = info;
		if (broadCast) {
			broadCast(IDiyMsgIds.APPCORE_DATACHANGE, DataType.DATATYPE_DESKTOPSETING, info, null);
		}
		mSettingDataModel.updateDesktopSettingInfo(info);
		// if(bReCreatGGmenu){
		// GoLauncher.sendBroadcastHandler(this,
		// IDiyMsgIds.REFRESH_GGMENU_THEME, -1, null, null);
		// }
	}

	// 特效设置
	public EffectSettingInfo getEffectSettingInfo() {
		if (null == mEffectSettingInfo) {
			mEffectSettingInfo = createEffectSettingInfo();
		}
		return mEffectSettingInfo;
	}

	public EffectSettingInfo createEffectSettingInfo() {
		EffectSettingInfo info = mSettingDataModel.getEffectSettingInfo();
		if (null == info) {
			info = new EffectSettingInfo();
			mSettingDataModel.cleanEffectSettingInfo();
			mSettingDataModel.insertEffectSettingInfo(info);
		}
		return info;
	}

	public void updateEffectSettingInfo(EffectSettingInfo info) {
		mEffectSettingInfo = info;
		broadCast(IDiyMsgIds.APPCORE_DATACHANGE, DataType.DATATYPE_EFFECTSETTING, info, null);
		mSettingDataModel.updateEffectSettingInfo(info);
	}

	// 手势设置
	public GestureSettingInfo getGestureSettingInfo(int type) {
		GestureSettingInfo info = getGestureSetting(type);
		if (null == info) {
			info = createGestureSettingInfo(type);
			setGestureSetting(type, info);
		}
		return info;
	}

	public GestureSettingInfo createGestureSettingInfo(int type) {
		GestureSettingInfo info = mSettingDataModel.getGestureSettingInfo(type);
		if (null == info) {
			info = new GestureSettingInfo();
			info.mGestureId = type;
			switch (type) {
				case GestureSettingInfo.GESTURE_HOME_ID :
					info.mGestureAction = GlobalSetConfig.GESTURE_GOSHORTCUT;
					info.mGoShortCut = GlobalSetConfig.GESTURE_SHOW_MAIN_SCREEN;
					break;

				case GestureSettingInfo.GESTURE_UP_ID :
					info.mGestureAction = GlobalSetConfig.GESTURE_GOSHORTCUT;
					info.mGoShortCut = GlobalSetConfig.GESTURE_SHOW_MENU;
					break;

				case GestureSettingInfo.GESTURE_DOWN_ID :
					info.mGestureAction = GlobalSetConfig.GESTURE_GOSHORTCUT;
					info.mGoShortCut = GlobalSetConfig.GESTURE_SHOW_HIDE_NOTIFICATIONEXPAND;
					break;
				case GestureSettingInfo.GESTURE_DOUBLLE_CLICK_ID :
					info.mGestureAction = GlobalSetConfig.GESTURE_GOSHORTCUT;
					info.mGoShortCut = GlobalSetConfig.GESTURE_SHOW_DIYGESTURE;
					break;
				default :
					break;
			}
			mSettingDataModel.cleanGestureSettingInfo(type);
			mSettingDataModel.insertGestureSettingInfo(type, info);
		}
		return info;
	}

	public void updateGestureSettingInfo(int type, GestureSettingInfo info) {
		setGestureSetting(type, info);
		broadCast(IDiyMsgIds.APPCORE_DATACHANGE, DataType.DATATYPE_GESTURESETTING, info, null);
		mSettingDataModel.updateGestureSettingInfo(type, info);
	}

	private GestureSettingInfo getGestureSetting(int type) {
		GestureSettingInfo info = null;
		switch (type) {
			case GestureSettingInfo.GESTURE_HOME_ID :
				info = mHomeGestureSettingInfo;
				break;

			case GestureSettingInfo.GESTURE_UP_ID :
				info = mUpGestureSettingInfo;
				break;

			case GestureSettingInfo.GESTURE_DOWN_ID :
				info = mDownGestureSettingInfo;
				break;
			case GestureSettingInfo.GESTURE_DOUBLLE_CLICK_ID :
				info = mDoubleClickGestureSettingInfo;
				break;
			default :
				break;
		}
		return info;
	}

	private void setGestureSetting(int type, GestureSettingInfo info) {
		switch (type) {
			case GestureSettingInfo.GESTURE_HOME_ID :
				mHomeGestureSettingInfo = info;
				break;

			case GestureSettingInfo.GESTURE_UP_ID :
				mUpGestureSettingInfo = info;
				break;

			case GestureSettingInfo.GESTURE_DOWN_ID :
				mDownGestureSettingInfo = info;
				break;
			case GestureSettingInfo.GESTURE_DOUBLLE_CLICK_ID :
				mDoubleClickGestureSettingInfo = info;
				break;
			default :
				break;
		}
	}

	// 重力感应设置
	public GravitySettingInfo getGravitySettingInfo() {
		if (null == mGravitySettingInfo) {
			mGravitySettingInfo = createGravitySettingInfo();
		}
		return mGravitySettingInfo;
	}

	public GravitySettingInfo createGravitySettingInfo() {
		GravitySettingInfo info = mSettingDataModel.getGravitySettingInfo();
		if (null == info) {
			info = new GravitySettingInfo();
			mSettingDataModel.cleanGravitySettingInfo();
			mSettingDataModel.insertGravitySettingInfo(info);
		}
		return info;
	}

	public void updateGravitySettingInfo(GravitySettingInfo info) {
		mGravitySettingInfo = info;
		broadCast(IDiyMsgIds.APPCORE_DATACHANGE, DataType.DATATYPE_GRAVITYSETTING, info, null);
		mSettingDataModel.updateGravitySettingInfo(info);
	}

	// 屏幕设置
	public ScreenSettingInfo getScreenSettingInfo() {
		if (null == mScreenSettingInfo) {
			mScreenSettingInfo = createScreenSettingInfo();
		}
		return mScreenSettingInfo;
	}

	public ScreenSettingInfo createScreenSettingInfo() {
		ScreenSettingInfo info = mSettingDataModel.getScreenSettingInfo();
		if (null == info) {
			info = new ScreenSettingInfo();
			mSettingDataModel.cleanScreenSettingInfo();
			mSettingDataModel.insertScreenSettingInfo(info);
		}
		return info;
	}

	public void updateScreenSettingInfo(ScreenSettingInfo info) {
		updateScreenSettingInfo(info, true);
	}

	public void updateScreenIndicatorThemeBean(String packageName) {
		if (!packageName.equals(ScreenIndicator.SHOWMODE_NORMAL)
				&& !packageName.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
			AppDataEngine.getInstance(mContext).onHandleScreenIndicatorThemeIconStyleChanged(
					packageName);
		} else {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_PREVIEW_FRAME,
					IDiyMsgIds.INDICATOR_CHANGE_SHOWMODE, -1, null, null);
		}
	}

	public void updateScreenSettingInfo(ScreenSettingInfo info, boolean broadCaset) {
		mScreenSettingInfo = info;
		mSettingDataModel.updateScreenSettingInfo(info);
		if (broadCaset) {
			broadCast(IDiyMsgIds.APPCORE_DATACHANGE, DataType.DATATYPE_SCREENSETTING, info, null);
		}
	}

	// 主题设置
	public ThemeSettingInfo getThemeSettingInfo() {
		if (null == mThemeSettingInfo) {
			mThemeSettingInfo = createThemeSettingInfo();
		}
		return mThemeSettingInfo;
	}

	public ThemeSettingInfo createThemeSettingInfo() {
		ThemeSettingInfo info = mSettingDataModel.getThemeSettingInfo();
		if (null == info) {
			info = new ThemeSettingInfo();
			mSettingDataModel.cleanThemeSettingInfo();
			mSettingDataModel.insertThemeSettingInfo(info);
		}
		return info;
	}

	public void updateThemeSettingInfo(ThemeSettingInfo info) {
		mThemeSettingInfo = info;
		broadCast(IDiyMsgIds.APPCORE_DATACHANGE, DataType.DATATYPE_THEMESETTING, info, null);
		mSettingDataModel.updateThemeSettingInfo(info);
	}

	// 字体
	public FontBean getUsedFontBean() {
		if (null != mUsedFontBean) {
			return mUsedFontBean;
		}

		mUsedFontBean = createUsedFontBean();
		mUsedFontBean.initTypeface(mContext);
		return mUsedFontBean;
	}

	public FontBean createUsedFontBean() {
		return mSettingDataModel.createUsedFontBean();
	}

	public void updateUsedFontBean(FontBean bean) {
		if (null != mUsedFontBean && mUsedFontBean.equals(bean)) {
			// 没有修改
			return;
		}

		mUsedFontBean = bean;
		mUsedFontBean.initTypeface(mContext);
		mSettingDataModel.updateUsedFontBean(bean);
		broadCast(IDiyMsgIds.APPCORE_DATACHANGE, DataType.DATATYPE_DESKFONTCHANGED, mUsedFontBean,
				null);
	}

	public ArrayList<FontBean> createFontBeans() {
		return mSettingDataModel.createFontBeans();
	}

	public void updateFontBeans(ArrayList<FontBean> beans) {
		mSettingDataModel.updateFontBeans(beans);
	}

	/********************
	 * 快捷条设置控制部分*
	 ********************/

	public ShortCutSettingInfo getShortCutSettingInfo() {
		if (null == mDockSettingInfo) {
			mDockSettingInfo = createShortcutsettingInfo();
		}
		return mDockSettingInfo;
	}

	public void updateShortcutSettingInfo() {
		mDockSettingInfo = createShortcutsettingInfo();
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_SETTING_NEED_UPDATE,
				-1, null, null);
		GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME, IDiyMsgIds.DOCK_SETTING_NEED_UPDATE,
				-1, null, null);
	}

	public ShortCutSettingInfo getDefaultThemeShortCutSettingInfo(String themeName) {
		return mSettingDataModel.getShortCurSetting(themeName);
	}

	/**
	 * 更新快捷设置,全局性，与主题无关的信息
	 * 
	 * @param info
	 *            设置信息
	 */
	public int updateShortCutSetting_NonIndepenceTheme(ShortCutSettingInfo info) {
		if (null == info) {
			return DockUtil.ERROR_BAD_PARAM;
		}
		boolean bResult = mSettingDataModel.updateShortCutSetting_NonIndepenceTheme(info);
		if (bResult) {
			mDockSettingInfo = info;
			GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.DOCK_FRAME,
					IDiyMsgIds.DOCK_SETTING_CHANGED, -1, null, null);
			GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.SHELL_FRAME,
					IDiyMsgIds.DOCK_SETTING_CHANGED, -1, null, null);
		} else {
			return DockUtil.ERROR_KEEPDATA_FAILD;
		}
		return DockUtil.ERROR_NONE;
	}

	public void updateCurThemeShortCutSettingStyle(String style) {
		if (mDockSettingInfo != null) {
			mDockSettingInfo.mStyle = style;
		}
		GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.DOCK_FRAME,
				IDiyMsgIds.DOCK_SETTING_CHANGED_STYLE, -1, style, null);

		final String themPkg = ThemeManager.getInstance(mContext).getCurThemePackage();
		mSettingDataModel.updateShortCutSettingStyle(themPkg, style);
	}

	public void updateCurThemeShortCutSettingBgSwitch(boolean isOn) {
		if (mDockSettingInfo != null) {
			mDockSettingInfo.mBgPicSwitch = isOn;
		}

		final String themPkg = ThemeManager.getInstance(mContext).getCurThemePackage();
		mSettingDataModel.updateShortCutSettingBgSwitch(themPkg, isOn);
	}

	public void updateCurThemeShortCutSettingCustomBgSwitch(boolean isOn) {
		if (mDockSettingInfo != null) {
			mDockSettingInfo.mCustomBgPicSwitch = isOn;
		}

		final String themPkg = ThemeManager.getInstance(mContext).getCurThemePackage();
		mSettingDataModel.updateShortCutSettingCustomBgSwitch(themPkg, isOn);
	}

	public boolean updateShortCutBg(String useThemeName, String targetThemeName, String resName,
			boolean isCustomPic) {
		if (mDockSettingInfo != null) {
			mDockSettingInfo.mBgtargetthemename = targetThemeName;
			mDockSettingInfo.mBgresname = resName;
			mDockSettingInfo.mBgiscustompic = isCustomPic;
		}
		return mSettingDataModel.updateShortCutBG(useThemeName, targetThemeName, resName,
				isCustomPic);
	}

	public boolean updateShortCutCustomBg(boolean iscustom) {
		if (mDockSettingInfo != null) {
			mDockSettingInfo.mBgiscustompic = iscustom;
		}
		return mSettingDataModel.updateIsCustomBg(iscustom);
	}

	public void updateEnable(boolean bool) {
		boolean updateResult = mSettingDataModel.updateShortCutSettingEnable(bool);
		if (updateResult) {
			ShortCutSettingInfo.setEnable(bool);
		}
	}

	private ShortCutSettingInfo createShortcutsettingInfo() {
		ShortCutSettingInfo info = null;
		final String themPkg = ThemeManager.getInstance(mContext).getCurThemePackage();
		info = mSettingDataModel.getShortCurSetting(themPkg);
		if (null == info) {
			info = new ShortCutSettingInfo();
			info.initWithDefaultData();
			mSettingDataModel.insertShortCutSetting(info);
		} else if (null == info.mStyle) {
			// 2.20数据库升级，加入text属性的style字段,赋默认值
			info.mStyle = (ThemeManager.DEFAULT_THEME_PACKAGE.equals(themPkg))
					? DockUtil.DOCK_DEFAULT_STYLE_STRING
					: themPkg;
			updateCurThemeShortCutSettingStyle(info.mStyle);
		}
		return info;
	}

	public ScreenStyleConfigInfo getScreenStyleSettingInfo() {
		if (null == mScreenStyleConfigInfo) {
			mScreenStyleConfigInfo = createScreenStyleConfig(mContext);
		}
		return mScreenStyleConfigInfo;
	}

	private ScreenStyleConfigInfo createScreenStyleConfig(Context context) {
		mScreenStyleConfigInfo = new ScreenStyleConfigInfo(context, mSettingDataModel,
				ThemeManager.getInstance(context));
		return mScreenStyleConfigInfo;
	}

	public void addScreenStyleSetting(String packageName) {
		mSettingDataModel.addScreenStyleSetting(packageName);
	}

	public void clearDirtyStyleSetting(String uninstallPackageName) {
		mSettingDataModel.clearDirtyScreenStyleSetting(uninstallPackageName);
	}

	@Override
	protected void onHandleBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.EVENT_THEME_CHANGED :
				mFunAppSetting.checkAppSetting();
				// mFunAppSetting.setTabHomeBgSetting(ThemeManager.getInstance(mContext).getCurThemePackageName());
				// mFunAppSetting.setIndicatorSetting(ThemeManager.getInstance(mContext).getCurThemePackageName());
				break;

			default :
				break;
		}
	}

	/**
	 * 根据包名获取ShortCut信息
	 * 
	 * @author yangbing
	 * */
	public ShortCutSettingInfo getShortCutSettingInfoByPackageName(String packageName) {

		ShortCutSettingInfo info = mSettingDataModel.getShortCurSetting(packageName);
		if (info == null) {
			info = new ShortCutSettingInfo();
		}
		return info;
	}

	/**
	 * 恢复快捷条背景
	 * 
	 * @author yangbing
	 * */
	public void resetShortCutBg(String useThemeName, String targetThemeName, String resName) {
		mSettingDataModel.resetShortCutBg(useThemeName, targetThemeName, resName);
	}

	/**
	 * 清除缓存
	 * 
	 * @author yangbing
	 * */
	public void clearDockSettingInfo() {
		mDockSettingInfo = null;
	}

	/**
	 * 获取快捷条图标风格
	 * 
	 * @author yangbing
	 * */
	public String getShortCutStyleByPackage(String packageName) {
		ShortCutSettingInfo info = mSettingDataModel.getShortCurSetting(packageName);
		return info == null ? null : info.mStyle;
	}

	/**
	 * 更新快捷条图标风格
	 * 
	 * @author yangbing
	 * */
	public void updateShortCutStyleByPackage(String packageName, String style) {
		mSettingDataModel.updateShortCutSettingStyle(packageName, style);
	}
}
