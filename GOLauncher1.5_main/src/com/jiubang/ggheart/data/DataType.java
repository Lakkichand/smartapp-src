package com.jiubang.ggheart.data;

/**
 * 数据类型
 * 
 * @author masanbing
 * 
 */
public interface DataType {
	/**
	 * 桌面显示设置 Object: DesktopSettingInfo
	 */
	public static final int DATATYPE_DESKTOPSETING = 0;
	/**
	 * 快捷键设置 Object: ShortCutSettingInfo
	 */
	public static final int DATATYPE_SHORTCUTSETTING = 1;
	/**
	 * 特效设置 Object: EffectSettingInfo
	 */
	public static final int DATATYPE_EFFECTSETTING = 2;
	/**
	 * 手势设置 Objects: List<GestureSettingInfo>
	 */
	public static final int DATATYPE_GESTURESETTING = 3;
	/**
	 * 重力感应设置 Object: GravitySettingInfo
	 */
	public static final int DATATYPE_GRAVITYSETTING = 4;
	/**
	 * 屏幕设置 Object: ScreenSettingInfo
	 */
	public static final int DATATYPE_SCREENSETTING = 5;
	/**
	 * 主题设置 Object: ThemeSettingInfo
	 */
	public static final int DATATYPE_THEMESETTING = 6;
	/**
	 * 应用程序数据 Object: Bundle Key: DataKey.DATAKEY_REMOVEAPPITEM Value: Long
	 * 即itemId
	 */
	public static final int DATATYPE_APPDATA_REMOVE = 7;
	/**
	 * 应用程序数据 新添加了数据
	 */
	public static final int DATATYPE_APPDATA_ADDAPPITEMS = 8;
	/**
	 * 应用程序数据 加载了前count个
	 */
	public static final int DATATYPE_APPDATA_FINISHLOADINGCCOUNT = 9;
	/**
	 * 应用程序数据 完成加载
	 */
	public static final int DATATYPE_APPDATA_FINISHLOADING = 10;
	/**
	 * 桌面菜单设置 Object: DeskMenuSettingInfo
	 */
	public static final int DATATYPE_DESKMENUSETTING = 11;
	/**
	 * 桌面字体设置 Object: FontBean
	 */
	public static final int DATATYPE_DESKFONTCHANGED = 12;

	/**
	 * 应用更改 ,与{@link #DATATYPE_THEMESETTING}一起使用
	 */
	public static final int APPLY_CHANGE = 100;

	/**
	 * 退出桌面,与{@link #DATATYPE_THEMESETTING}一起使用
	 */
	public static final int EXIT_GOLAUNCHER = 101;

	/**
	 * 重启桌面,与{@link #DATATYPE_THEMESETTING}一起使用
	 */
	public static final int RESTART_GOLAUNCHER = 102;

}
