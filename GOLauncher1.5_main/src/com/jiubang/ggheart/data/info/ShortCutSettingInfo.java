package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.database.Cursor;
import android.widget.Toast;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.tables.ShortcutSettingTable;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 快捷相关信息定义
 * 
 * @author masanbing
 * 
 */
//CHECKSTYLE:OFF
public class ShortCutSettingInfo {
	/**
	 * 与主题无关的设置
	 */
	public static boolean sEnable = true; // 显示／隐藏

	public boolean mAutoRevolve; // 自动循环
	public int mRows; // 行数
	public boolean mAutoFit; // 自适应模式
	public static boolean mAutoMessageStatistic = false;
	public static boolean mAutoMisscallStatistic = false;
	public static boolean mAutoMissmailStatistic = false;
	public static boolean mAutoMissk9mailStatistic = false;
	public static boolean mAutoMissfacebookStatistic = false;
	public static boolean mAutoMissSinaWeiboStatistic = false;

	/**
	 * 主题相关的设置
	 */
	public String mStyle;
	public boolean mBgPicSwitch;
	public boolean mCustomBgPicSwitch = false;

	public String mBgtargetthemename;
	public String mBgresname;
	public boolean mBgiscustompic;

	/**
	 * 构造函数
	 */
	public ShortCutSettingInfo() {
		mAutoRevolve = true;
		mRows = 2;
		mStyle = DockUtil.DOCK_DEFAULT_STYLE_STRING;
		mBgPicSwitch = true;
		mBgtargetthemename = null;
		mBgresname = null;
		mBgiscustompic = false;
		mAutoFit = true;
	}

	public void initWithDefaultData() {
		initNonIndependenceThemeInfo();

		// 主题相关
		final String curThemeName = GOLauncherApp.getThemeManager().getCurThemePackage();
		boolean defaultTheme = (curThemeName.equals(ThemeManager.DEFAULT_THEME_PACKAGE));
		mStyle = (defaultTheme) ? DockUtil.DOCK_DEFAULT_STYLE_STRING : curThemeName;

		initBgInfo();
	}

	private void initNonIndependenceThemeInfo() {
		String[] pkgNames = new String[] { ThemeManager.DEFAULT_THEME_PACKAGE,
				ThemeManager.DEFAULT_THEME_PACKAGE_3, ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER };
		for (String pkg : pkgNames) {
			ShortCutSettingInfo defaultthemeInfo = GOLauncherApp.getSettingControler()
					.getDefaultThemeShortCutSettingInfo(pkg);
			if (null != defaultthemeInfo) {
				mAutoRevolve = defaultthemeInfo.mAutoRevolve;
				mRows = defaultthemeInfo.mRows;
				mAutoFit = defaultthemeInfo.mAutoFit;
				return;
			}
		}
	}

	private void initBgInfo() {
		try {
			DeskThemeBean bean = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean();

			mBgPicSwitch = bean.mDock.mDockSetting.mIsBackground;
			String path = bean.mDock.mDockSetting.mBackground;

			// String dockBgpath = DockLogicControler.getDockBgReadFilePath();
			//
			// //找原来版本的自定义背景图片
			// File file = new File(dockBgpath);
			// if(file.exists())
			// {
			// mBgtargetthemename = null;
			// mBgresname = dockBgpath;
			// mBgiscustompic = true;
			// }else {
			mBgtargetthemename = GOLauncherApp.getThemeManager().getCurThemePackage();
			mBgresname = path;
			mBgiscustompic = false;
			// }

			return;
		} catch (Throwable e) {
		}

		mBgPicSwitch = true;
		mBgtargetthemename = null;
		mBgresname = null;
		mBgiscustompic = false;
	}

	/**
	 * 加入键值对
	 * 
	 * @param values
	 *            键值对
	 */
	public void contentValues(ContentValues values) {
		if (null == values) {
			return;
		}
		// NOTE:只为2.20向下降级
		values.put(ShortcutSettingTable.STYLE, 4);

		// 全局相关
		contentValues_NonIndependenceTheme(values);

		// 主题相关
		contentValues_IndependenceTheme(values);
	}

	/**
	 * 全局相关
	 * 
	 * @param values
	 */
	public void contentValues_NonIndependenceTheme(ContentValues values) {
		if (null == values) {
			return;
		}
		values.put(ShortcutSettingTable.ENABLE, ConvertUtils.boolean2int(sEnable));

		values.put(ShortcutSettingTable.AUTOREVOLVE, ConvertUtils.boolean2int(mAutoRevolve));
		values.put(ShortcutSettingTable.ROWS, mRows);
		values.put(ShortcutSettingTable.AUTOMESSAGESTATICS,
				ConvertUtils.boolean2int(mAutoMessageStatistic));
		values.put(ShortcutSettingTable.AUTOMISSCALLSTATICS,
				ConvertUtils.boolean2int(mAutoMisscallStatistic));
		values.put(ShortcutSettingTable.AUTOMISSMAILSTATICS,
				ConvertUtils.boolean2int(mAutoMissmailStatistic));
		values.put(ShortcutSettingTable.AUTOMISSK9MAILSTATICS,
				ConvertUtils.boolean2int(mAutoMissk9mailStatistic));
		values.put(ShortcutSettingTable.AUTOMISSFACEBOOKSTATICS,
				ConvertUtils.boolean2int(mAutoMissfacebookStatistic));
		values.put(ShortcutSettingTable.AUTOMISSSINAWEIBOSTATICS,
				ConvertUtils.boolean2int(mAutoMissSinaWeiboStatistic));
		values.put(ShortcutSettingTable.AUTOFIT, ConvertUtils.boolean2int(mAutoFit));
	}

	/**
	 * 主题相关
	 * 
	 * @param values
	 */
	private void contentValues_IndependenceTheme(ContentValues values) {
		values.put(ShortcutSettingTable.STYLE_STRING, mStyle);
		values.put(ShortcutSettingTable.BGPICSWITCH, ConvertUtils.boolean2int(mBgPicSwitch));
		values.put(ShortcutSettingTable.CUSTOMBGPICSWITCH,
				ConvertUtils.boolean2int(mCustomBgPicSwitch));
		values.put(ShortcutSettingTable.BG_TARGET_THEME_NAME, mBgtargetthemename);
		values.put(ShortcutSettingTable.BG_RESNAME, mBgresname);
		values.put(ShortcutSettingTable.CUSTOM_PIC_OR_NOT, ConvertUtils.boolean2int(mBgiscustompic));
	}

	/**
	 * 解析数据
	 * 
	 * @param cursor
	 *            数据集
	 */
	public boolean parseFromCursor(Cursor cursor) {
		if (null == cursor) {
			return false;
		}

		boolean bData = cursor.moveToFirst();
		if (bData) {
			int enableIndex = cursor.getColumnIndex(ShortcutSettingTable.ENABLE);
			int bgpicswitchIndex = cursor.getColumnIndex(ShortcutSettingTable.BGPICSWITCH);
			int custombgpicswitchIndex = cursor
					.getColumnIndex(ShortcutSettingTable.CUSTOMBGPICSWITCH);
			int autorevolveIndex = cursor.getColumnIndex(ShortcutSettingTable.AUTOREVOLVE);
			int styleIndex = cursor.getColumnIndex(ShortcutSettingTable.STYLE_STRING);
			int rowsIndex = cursor.getColumnIndex(ShortcutSettingTable.ROWS);
			int automessageIndex = cursor.getColumnIndex(ShortcutSettingTable.AUTOMESSAGESTATICS);
			int automisscallIndex = cursor.getColumnIndex(ShortcutSettingTable.AUTOMISSCALLSTATICS);
			int automissmailIndex = cursor.getColumnIndex(ShortcutSettingTable.AUTOMISSMAILSTATICS);
			int automissk9mailIndex = cursor
					.getColumnIndex(ShortcutSettingTable.AUTOMISSK9MAILSTATICS);
			int automissfacebookIndex = cursor
					.getColumnIndex(ShortcutSettingTable.AUTOMISSFACEBOOKSTATICS);
			int automissSinaWeiboIndex = cursor
					.getColumnIndex(ShortcutSettingTable.AUTOMISSSINAWEIBOSTATICS);
			int autoFitIndex = cursor.getColumnIndex(ShortcutSettingTable.AUTOFIT);

			int bgtargetthemename = cursor
					.getColumnIndex(ShortcutSettingTable.BG_TARGET_THEME_NAME);
			int bgresname = cursor.getColumnIndex(ShortcutSettingTable.BG_RESNAME);
			int bgiscustompic = cursor.getColumnIndex(ShortcutSettingTable.CUSTOM_PIC_OR_NOT);

			if (-1 == enableIndex || -1 == bgpicswitchIndex || -1 == custombgpicswitchIndex
					|| -1 == autorevolveIndex || -1 == styleIndex || -1 == rowsIndex
					|| -1 == automessageIndex || -1 == automisscallIndex || -1 == automissmailIndex
					|| -1 == automissk9mailIndex || -1 == automissfacebookIndex
					|| -1 == automissSinaWeiboIndex || -1 == bgtargetthemename || -1 == bgresname
					|| -1 == bgiscustompic || -1 == autoFitIndex) {
				Toast.makeText(GOLauncherApp.getContext(), "DockSetting DataBase Error!",
						Toast.LENGTH_LONG).show();
				return false;
			}

			sEnable = ConvertUtils.int2boolean(cursor.getInt(enableIndex));
			mBgPicSwitch = ConvertUtils.int2boolean(cursor.getInt(bgpicswitchIndex));
			mCustomBgPicSwitch = ConvertUtils.int2boolean(cursor.getInt(custombgpicswitchIndex));
			mAutoRevolve = ConvertUtils.int2boolean(cursor.getInt(autorevolveIndex));
			mAutoFit = ConvertUtils.int2boolean(cursor.getInt(autoFitIndex));
			mStyle = cursor.getString(styleIndex);
			mRows = cursor.getInt(rowsIndex);
			mAutoMessageStatistic = ConvertUtils.int2boolean(cursor.getInt(automessageIndex));
			mAutoMisscallStatistic = ConvertUtils.int2boolean(cursor.getInt(automisscallIndex));
			mAutoMissmailStatistic = ConvertUtils.int2boolean(cursor.getInt(automissmailIndex));
			mAutoMissk9mailStatistic = ConvertUtils.int2boolean(cursor.getInt(automissk9mailIndex));
			mAutoMissfacebookStatistic = ConvertUtils.int2boolean(cursor
					.getInt(automissfacebookIndex));
			mAutoMissSinaWeiboStatistic = ConvertUtils.int2boolean(cursor
					.getInt(automissSinaWeiboIndex));

			mBgtargetthemename = cursor.getString(bgtargetthemename);
			mBgresname = cursor.getString(bgresname);
			mBgiscustompic = ConvertUtils.int2boolean(cursor.getInt(bgiscustompic));

			return true;
		}
		return false;
	}
	
	public static void setEnable(boolean enable) {
		ShortCutSettingInfo.sEnable = enable;
	}
	
	public static void setAutoMessageStatistic(boolean autoMessageStatistic) {
		ShortCutSettingInfo.mAutoMessageStatistic = autoMessageStatistic;
	}

	public static void setAutoMisscallStatistic(boolean autoMisscallStatistic) {
		ShortCutSettingInfo.mAutoMisscallStatistic = autoMisscallStatistic;
	}

	public static void setAutoMissmailStatistic(boolean autoMissmailStatistic) {
		ShortCutSettingInfo.mAutoMissmailStatistic = autoMissmailStatistic;
	}

	public static void setAutoMissk9mailStatistic(boolean autoMissk9mailStatistic) {
		ShortCutSettingInfo.mAutoMissk9mailStatistic = autoMissk9mailStatistic;
	}

	public static void setAutoMissfacebookStatistic(boolean autoMissfacebookStatistic) {
		ShortCutSettingInfo.mAutoMissfacebookStatistic = autoMissfacebookStatistic;
	}


	public static void setAutoMissSinaWeiboStatistic(boolean autoMissSinaWeiboStatistic) {
		ShortCutSettingInfo.mAutoMissSinaWeiboStatistic = autoMissSinaWeiboStatistic;
	}
}
