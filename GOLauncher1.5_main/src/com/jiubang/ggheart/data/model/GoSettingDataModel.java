package com.jiubang.ggheart.data.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.go.util.ConvertUtils;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.apps.font.FontTypeface;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.Setting;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.info.DeskMenuSettingInfo;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.GravitySettingInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.tables.AppFuncSettingTable;
import com.jiubang.ggheart.data.tables.AppSettingTable;
import com.jiubang.ggheart.data.tables.DeskMenuTable;
import com.jiubang.ggheart.data.tables.DesktopTable;
import com.jiubang.ggheart.data.tables.DynamicEffectTable;
import com.jiubang.ggheart.data.tables.GestureTable;
import com.jiubang.ggheart.data.tables.GravityTable;
import com.jiubang.ggheart.data.tables.ScreenSettingTable;
import com.jiubang.ggheart.data.tables.ScreenStyleConfigTable;
import com.jiubang.ggheart.data.tables.ShortcutSettingTable;
import com.jiubang.ggheart.data.tables.ThemeTable;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class GoSettingDataModel extends DataModel implements ICleanable {
	public GoSettingDataModel(Context context) {
		super(context);
	}

	public DeskMenuSettingInfo getDeskMenuSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		Cursor cursor = dataProvider.queryDeskMenuSetting();
		if (null != cursor) {
			try {
				DeskMenuSettingInfo info = new DeskMenuSettingInfo();
				boolean bOk = info.parseFromCursor(cursor);
				if (bOk) {
					return info;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public void updateDeskMenuSettingInfo(DeskMenuSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.updateDeskMenuSetting(values);
		values = null;
	}

	public void insertDeskMenuSettingInfo(DeskMenuSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.insertDeskMenuSetting(values);
		values = null;
	}

	public void cleanDeskMenuSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		dataProvider.clearTable(DeskMenuTable.TABLENAME);
	}

	// 桌面设置
	public DesktopSettingInfo getDesktopSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		Cursor cursor = dataProvider.queryDesktopSetting();
		if (null != cursor) {
			try {
				DesktopSettingInfo info = new DesktopSettingInfo();
				boolean bOk = info.parseFromCursor(cursor);
				if (bOk) {
					return info;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public void updateDesktopSettingInfo(DesktopSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.updateDesktopSetting(values);
		values = null;
	}

	public void insertDesktopSettingInfo(DesktopSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.insertDesktopSetting(values);
		values = null;
	}

	public void cleanDesktopSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		dataProvider.clearTable(DesktopTable.TABLENAME);
	}

	// 特效设置
	public EffectSettingInfo getEffectSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		Cursor cursor = dataProvider.queryEffectSetting();
		if (null != cursor) {
			try {
				EffectSettingInfo info = new EffectSettingInfo();
				boolean bOk = info.parseFromCursor(cursor);
				if (bOk) {
					return info;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public void updateEffectSettingInfo(EffectSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.updateEffectSetting(values);
		values = null;
	}

	public void insertEffectSettingInfo(EffectSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.insertEffectSetting(values);
		values = null;
	}

	public void cleanEffectSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		dataProvider.clearTable(DynamicEffectTable.TABLENAME);
	}

	// 手势设置
	public GestureSettingInfo getGestureSettingInfo(int type) {
		final DataProvider dataProvider = mDataProvider;
		Cursor cursor = dataProvider.queryGestureSetting(type);
		if (null != cursor) {
			try {
				GestureSettingInfo info = new GestureSettingInfo();
				boolean bOk = info.parseFromCursor(cursor);
				if (bOk) {
					return info;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public void updateGestureSettingInfo(int type, GestureSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.updateGestureSetting(type, values);
		values = null;
	}

	public void insertGestureSettingInfo(int type, GestureSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.insertGestureSetting(type, values);
		values = null;
	}

	public void cleanGestureSettingInfo(int type) {
		final DataProvider dataProvider = mDataProvider;
		String whereStr = GestureTable.GESTUREID + " = " + type;
		dataProvider.clearTable(GestureTable.TABLENAME, whereStr);
		whereStr = null;
	}

	// 重力感应设置
	public GravitySettingInfo getGravitySettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		Cursor cursor = dataProvider.queryGravitySetting();
		if (null != cursor) {
			try {
				GravitySettingInfo info = new GravitySettingInfo();
				boolean bOk = info.parseFromCursor(cursor);
				if (bOk) {
					return info;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public void updateGravitySettingInfo(GravitySettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.updateGravitySetting(values);
		values = null;
	}

	public void insertGravitySettingInfo(GravitySettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.insertGravitySetting(values);
		values = null;
	}

	public void cleanGravitySettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		dataProvider.clearTable(GravityTable.TABLENAME);
	}

	// 屏幕设置
	public ScreenSettingInfo getScreenSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		Cursor cursor = dataProvider.queryScreenSetting();
		if (null != cursor) {
			try {
				ScreenSettingInfo info = new ScreenSettingInfo();
				boolean bOk = info.parseFromCursor(cursor);
				if (bOk) {
					return info;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public void updateScreenSettingInfo(ScreenSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.updateScreenSetting(values);
		values = null;
	}

	public void insertScreenSettingInfo(ScreenSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.insertScreenSetting(values);
		values = null;
	}

	public void cleanScreenSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		dataProvider.clearTable(ScreenSettingTable.TABLENAME);
	}

	// 主题设置
	public ThemeSettingInfo getThemeSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		Cursor cursor = dataProvider.queryThemeSetting();
		if (null != cursor) {
			try {
				ThemeSettingInfo info = new ThemeSettingInfo();
				boolean bOk = info.parseFromCursor(cursor);
				if (bOk) {
					return info;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return null;
	}

	public void updateThemeSettingInfo(ThemeSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.updateThemeSetting(values);
		values = null;
	}

	public void insertThemeSettingInfo(ThemeSettingInfo info) {
		final DataProvider dataProvider = mDataProvider;
		ContentValues values = new ContentValues();
		info.contentValues(values);
		dataProvider.insertThemeSetting(values);
		values = null;
	}

	public void cleanThemeSettingInfo() {
		final DataProvider dataProvider = mDataProvider;
		dataProvider.clearTable(ThemeTable.TABLENAME);
	}

	/**
	 * 获取功能表设置信息
	 * 
	 * @author huyong
	 * @param settingItem
	 * @return 返回对应设置项的值
	 */
	// public String getAppSetting(final String packageName, int settingItem)
	// {
	// String columnName = null;
	// switch (settingItem)
	// {
	// case Setting.MENUAPPSTYLE:
	// columnName = AppSettingTable.MENUAPPSTYLE;
	// break;
	// case Setting.TURNSCREENDIRECTION:
	// columnName = AppSettingTable.TURNSCREENDIRECTION;
	// break;
	// case Setting.APPNAMEVISIABLE:
	// columnName = AppSettingTable.APPNAMEVISIABLE;
	// break;
	// case Setting.LINECOLUMNNUM:
	// columnName = AppSettingTable.LINECOLUMNNUM;
	// break;
	// case Setting.BACKGROUNDPICPATH:
	// columnName = AppSettingTable.BACKGROUNDPICPATH;
	// break;
	// case Setting.BGVISIABLE:
	// columnName = AppSettingTable.BACKGROUNDVISIABLE;
	// break;
	// case Setting.SORTTYPE:
	// columnName = AppSettingTable.SORTTYPE;
	// break;
	// case Setting.SHOWNEGLECTAPP:
	// columnName = AppSettingTable.SHOWNEGLECTAPPS;
	// break;
	// case Setting.INOUTEFFECT:
	// columnName = AppSettingTable.INOUTEFFECT;
	// break;
	// case Setting.ICONEFFECT:
	// columnName = AppSettingTable.ICONEFFECT;
	// break;
	// case Setting.SCROLL_LOOP:
	// columnName = AppSettingTable.SCROLL_LOOP;
	// break;
	// case Setting.BLUR_BACKGROUND:
	// columnName = AppSettingTable.BLUR_BACKGROUND;
	// break;
	// case Setting.SHOW_TAB_ROW:
	// columnName = AppSettingTable.SHOW_TAB_ROW;
	// break;
	// case Setting.VERTICAL_SCROLL_EFFECT:
	// columnName = AppSettingTable.VERTICAL_SCROLL_EFFECT;
	// break;
	// case Setting.SHOW_SEARCH:
	// columnName = AppSettingTable.SHOW_SEARCH;
	// break;
	// case Setting.TAB_HOME_BG:
	// columnName = AppSettingTable.TAB_HOME_BG;
	// break;
	// case Setting.INDICATOR_STYLE:
	// columnName = AppSettingTable.INDICATOR_STYLE;
	// break;
	// case Setting.APPFUNC_ROWNUM:
	// columnName = AppSettingTable.ROWNUM;
	// break;
	// case Setting.APPFUNC_COLNUM:
	// columnName = AppSettingTable.COLNUM;
	// break;
	// case Setting.APPUPDATE:
	// columnName = AppSettingTable.PROUPDATEAPP;
	// break;
	// case Setting.APPINOUTCUSTOMRANDOMEFFECT:
	// columnName = AppSettingTable.CUSTOMINOUTEFFECTITEMS;
	// break;
	// case Setting.APPICONCUSTOMEFFECTSETTING:
	// columnName = AppSettingTable.CUSTOMICONEFFECT;
	// break;
	// case Setting.SHOW_ACTION_BAR:
	// columnName = AppSettingTable.SHOW_ACTION_BAR;
	// break;
	// case Setting.SHOW_HOME_KEY_ONLY:
	// columnName = AppSettingTable.SHOW_HOME_KEY_ONLY;
	// break;
	// case Setting.IMAGE_OPEN_WAY:
	// columnName = AppSettingTable.IMAGE_OPEN_WAY;
	// break;
	// case Setting.AUDIO_OPEN_WAY:
	// columnName = AppSettingTable.AUDIO_OPEN_WAY;
	// break;
	// default:
	// break;
	// }
	//
	// Cursor cursor = mDataProvider.getAppSetting(packageName, columnName);
	// String value = null;
	// if (cursor != null)
	// {
	// if (cursor.moveToFirst())
	// {
	// int valueIndex = cursor.getColumnIndex(columnName);
	// value = cursor.getString(valueIndex);
	// }
	// cursor.close();
	// }
	// columnName = null;
	// cursor = null;
	// return value;
	// }

	/**
	 * 获取功能表设置信息
	 * 
	 * @author yangguanxiang
	 * @param key
	 * @return 返回对应设置项的值
	 */
	public String getAppFuncSetting(String pkname, int key) {
		Cursor cursor = mDataProvider.getAppFuncSetting(pkname, key);
		String value = null;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					int valueIndex = cursor.getColumnIndex(AppFuncSettingTable.SETTINGVALUE);
					value = cursor.getString(valueIndex);
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return value;
	}

	/**
	 * 获取功能表设置信息
	 * 
	 * @author yangguanxiang
	 * @param key
	 * @return 返回对应设置项的值
	 */
	public void setAppFuncSetting(String pkname, int key, String value) {
		mDataProvider.setAppFuncSetting(pkname, key, value);
	}

	/**
	 * 更新功能表设置信息
	 * 
	 * @author huyong
	 * @param settingItem
	 * @param value
	 */
	// public void setAppSetting(final String packageName, final int
	// settingItem,
	// final String value)
	// {
	// String columnName = null;
	// switch (settingItem)
	// {
	// case Setting.MENUAPPSTYLE:
	// columnName = AppSettingTable.MENUAPPSTYLE;
	// break;
	// case Setting.TURNSCREENDIRECTION:
	// columnName = AppSettingTable.TURNSCREENDIRECTION;
	// break;
	// case Setting.APPNAMEVISIABLE:
	// columnName = AppSettingTable.APPNAMEVISIABLE;
	// break;
	// case Setting.LINECOLUMNNUM:
	// columnName = AppSettingTable.LINECOLUMNNUM;
	// break;
	// case Setting.BACKGROUNDPICPATH:
	// columnName = AppSettingTable.BACKGROUNDPICPATH;
	// break;
	// case Setting.BGVISIABLE:
	// columnName = AppSettingTable.BACKGROUNDVISIABLE;
	// break;
	// case Setting.SORTTYPE:
	// columnName = AppSettingTable.SORTTYPE;
	// break;
	// case Setting.SHOWNEGLECTAPP:
	// columnName = AppSettingTable.SHOWNEGLECTAPPS;
	// // add by huyong 2011-03-17
	// break;
	// // add by huyong 2011-03-17 end
	// case Setting.INOUTEFFECT:
	// columnName = AppSettingTable.INOUTEFFECT;
	// break;
	// case Setting.ICONEFFECT:
	// columnName = AppSettingTable.ICONEFFECT;
	// break;
	// case Setting.SCROLL_LOOP:
	// columnName = AppSettingTable.SCROLL_LOOP;
	// break;
	// case Setting.BLUR_BACKGROUND:
	// columnName = AppSettingTable.BLUR_BACKGROUND;
	// break;
	// case Setting.SHOW_TAB_ROW:
	// columnName = AppSettingTable.SHOW_TAB_ROW;
	// break;
	// case Setting.VERTICAL_SCROLL_EFFECT:
	// columnName = AppSettingTable.VERTICAL_SCROLL_EFFECT;
	// break;
	// case Setting.SHOW_SEARCH:
	// columnName = AppSettingTable.SHOW_SEARCH;
	// break;
	// case Setting.TAB_HOME_BG:
	// columnName = AppSettingTable.TAB_HOME_BG;
	// break;
	// case Setting.INDICATOR_STYLE:
	// columnName = AppSettingTable.INDICATOR_STYLE;
	// break;
	// case Setting.APPFUNC_ROWNUM:
	// columnName = AppSettingTable.ROWNUM;
	// break;
	// case Setting.APPFUNC_COLNUM:
	// columnName = AppSettingTable.COLNUM;
	// break;
	// case Setting.APPUPDATE:
	// columnName = AppSettingTable.PROUPDATEAPP;
	// break;
	// case Setting.APPINOUTCUSTOMRANDOMEFFECT:
	// columnName = AppSettingTable.CUSTOMINOUTEFFECTITEMS;
	// break;
	// case Setting.APPICONCUSTOMEFFECTSETTING:
	// columnName = AppSettingTable.CUSTOMICONEFFECT;
	// break;
	// case Setting.SHOW_ACTION_BAR:
	// columnName = AppSettingTable.SHOW_ACTION_BAR;
	// break;
	// case Setting.SHOW_HOME_KEY_ONLY:
	// columnName = AppSettingTable.SHOW_HOME_KEY_ONLY;
	// break;
	// case Setting.IMAGE_OPEN_WAY:
	// columnName = AppSettingTable.IMAGE_OPEN_WAY;
	// break;
	// case Setting.AUDIO_OPEN_WAY:
	// columnName = AppSettingTable.AUDIO_OPEN_WAY;
	// break;
	// default:
	// break;
	// }
	//
	// ContentValues values = new ContentValues();
	// values.put(columnName, value);
	// mDataProvider.setAppSetting2(packageName, values);
	// values = null;
	// }
	/**
	 * 更新个性搭配设置信息
	 * 
	 * @param item
	 * @param value
	 */
	public void updateScreenStyleSetting(final String packageName, final int item,
			final String value) {
		String columnName = null;
		switch (item) {
			case Setting.THEMEPACKAGE :
				columnName = ScreenStyleConfigTable.THEMEPACKAGE;
				break;
			case Setting.ICONSTYLEPACKAGE :
				columnName = ScreenStyleConfigTable.ICONSTYLEPACKAGE;
				break;
			case Setting.FOLDERSTYLEPACKAGE :
				columnName = ScreenStyleConfigTable.FOLDERSTYLEPACKAGE;
				break;
			case Setting.GGMENUPACKAGE :
				columnName = ScreenStyleConfigTable.GGMENUPACKAGE;
				break;
			case Setting.INDICATOR :
				columnName = ScreenStyleConfigTable.INDICATOR;
				break;
			default :
				break;
		}

		ContentValues values = new ContentValues();
		values.put(columnName, value);
		mDataProvider.updateScreenStyleSetting(packageName, values);
		values = null;
	}

	/**
	 * 检查主题搭配中是否有卸载的主题，有恢复默认
	 * 
	 * @param item
	 * @param value
	 */
	public void clearDirtyScreenStyleSetting(final String uninstallpackage) {

		mDataProvider.clearDirtyStyleSetting(uninstallpackage);
	}

	/**
	 * 获取screenstyle设置信息
	 * 
	 * @param settingItem
	 * @return 返回对应设置项的值
	 */
	public String getScreenStyleSetting(final String packageName, int item) {
		String columnName = null;
		switch (item) {
			case Setting.THEMEPACKAGE :
				columnName = ScreenStyleConfigTable.THEMEPACKAGE;
				break;
			case Setting.ICONSTYLEPACKAGE :
				columnName = ScreenStyleConfigTable.ICONSTYLEPACKAGE;
				break;
			case Setting.FOLDERSTYLEPACKAGE :
				columnName = ScreenStyleConfigTable.FOLDERSTYLEPACKAGE;
				break;
			case Setting.GGMENUPACKAGE :
				columnName = ScreenStyleConfigTable.GGMENUPACKAGE;
				break;
			case Setting.INDICATOR :
				columnName = ScreenStyleConfigTable.INDICATOR;
				break;
			default :
				break;
		}

		Cursor cursor = mDataProvider.getScreenStyleSetting(packageName, columnName);
		String value = null;
		if (cursor != null) {
			try {
				if (cursor.moveToFirst()) {
					int valueIndex = cursor.getColumnIndex(columnName);
					value = cursor.getString(valueIndex);
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}
		return value;
	}

	/**
	 * 添加设置项
	 * 
	 * @param packageName
	 * @param value
	 */
	public void addAppFuncSetting(final String packageName) {
		// 功能表旧设置表初始设置，为了防止降级操作导致数据不对保留这些数据
		ContentValues value = new ContentValues();
		value.put(AppSettingTable.THEMEPACKAGENAME, packageName);
		value.put(AppSettingTable.MENUAPPSTYLE, AppSettingDefault.MENUAPPSTYLE);
		value.put(AppSettingTable.TURNSCREENDIRECTION, AppSettingDefault.TURNSCREENDIRECTION);
		value.put(AppSettingTable.APPNAMEVISIABLE, AppSettingDefault.APPNAMEVISIABLE);
		value.put(AppSettingTable.LINECOLUMNNUM, AppSettingDefault.LINECOLUMNNUM);
		value.put(AppSettingTable.BACKGROUNDPICPATH, AppSettingDefault.BACKGROUNDPICPATH);
		if (packageName.compareTo(ThemeManager.DEFAULT_THEME_PACKAGE) == 0) {
			value.put(AppSettingTable.BACKGROUNDVISIABLE, AppSettingDefault.BACKGROUNDVISIABLE - 1);
		} else {
			value.put(AppSettingTable.BACKGROUNDVISIABLE, AppSettingDefault.BACKGROUNDVISIABLE);
		}
		value.put(AppSettingTable.SORTTYPE, AppSettingDefault.SORTTYPE);
		value.put(AppSettingTable.SHOWNEGLECTAPPS, AppSettingDefault.SHOWNEGLECTAPPS);
		value.put(AppSettingTable.INOUTEFFECT, AppSettingDefault.INOUTEFFECT);
		value.put(AppSettingTable.ICONEFFECT, AppSettingDefault.ICONEFFECT);
		value.put(AppSettingTable.SCROLL_LOOP, AppSettingDefault.SCROLL_LOOP);
		value.put(AppSettingTable.BLUR_BACKGROUND, AppSettingDefault.BLUR_BACKGROUND);
		value.put(AppSettingTable.SHOW_TAB_ROW, AppSettingDefault.SHOW_TAB_ROW);
		value.put(AppSettingTable.VERTICAL_SCROLL_EFFECT, AppSettingDefault.VERTICAL_SCROLL_EFFECT);
		value.put(AppSettingTable.TAB_HOME_BG, packageName);
		value.put(AppSettingTable.INDICATOR_STYLE, packageName);

		mDataProvider.addAppSetting(packageName, value);
		value = null;

		// 功能表新设置表数据初始化
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.MENUAPPSTYLE),
				String.valueOf(AppSettingDefault.MENUAPPSTYLE));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.TURNSCREENDIRECTION),
				String.valueOf(AppSettingDefault.TURNSCREENDIRECTION));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.APPNAMEVISIABLE),
				String.valueOf(AppSettingDefault.APPNAMEVISIABLE));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.LINECOLUMNNUM),
				String.valueOf(AppSettingDefault.LINECOLUMNNUM));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.BACKGROUNDPICPATH),
				String.valueOf(AppSettingDefault.BACKGROUNDPICPATH));
		if (packageName.compareTo(ThemeManager.DEFAULT_THEME_PACKAGE) == 0) {
			mDataProvider.addAppFuncSetting(packageName,
					AppFuncSettingTable.getKeyByColumnName(AppSettingTable.BACKGROUNDVISIABLE),
					String.valueOf(AppSettingDefault.BACKGROUNDVISIABLE - 1));
		} else {
			mDataProvider.addAppFuncSetting(packageName,
					AppFuncSettingTable.getKeyByColumnName(AppSettingTable.BACKGROUNDVISIABLE),
					String.valueOf(AppSettingDefault.BACKGROUNDVISIABLE));
		}
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.SORTTYPE),
				String.valueOf(AppSettingDefault.SORTTYPE));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.SHOWNEGLECTAPPS),
				String.valueOf(AppSettingDefault.SHOWNEGLECTAPPS));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.INOUTEFFECT),
				String.valueOf(AppSettingDefault.INOUTEFFECT));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.ICONEFFECT),
				String.valueOf(AppSettingDefault.ICONEFFECT));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.SCROLL_LOOP),
				String.valueOf(AppSettingDefault.SCROLL_LOOP));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.BLUR_BACKGROUND),
				String.valueOf(AppSettingDefault.BLUR_BACKGROUND));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.SHOW_TAB_ROW),
				String.valueOf(AppSettingDefault.SHOW_TAB_ROW));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.VERTICAL_SCROLL_EFFECT),
				String.valueOf(AppSettingDefault.VERTICAL_SCROLL_EFFECT));
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.TAB_HOME_BG), packageName);
		mDataProvider.addAppFuncSetting(packageName,
				AppFuncSettingTable.getKeyByColumnName(AppSettingTable.INDICATOR_STYLE),
				packageName);
	}

	/**
	 * 添加屏幕个性搭配设置项
	 * 
	 * @param packageName
	 * @param value
	 */
	public void addScreenStyleSetting(final String packageName) {
		ContentValues value = new ContentValues();
		value.put(ScreenStyleConfigTable.THEMEPACKAGE, packageName);
		value.put(ScreenStyleConfigTable.ICONSTYLEPACKAGE, packageName);
		value.put(ScreenStyleConfigTable.FOLDERSTYLEPACKAGE, packageName);
		value.put(ScreenStyleConfigTable.GGMENUPACKAGE, packageName);
		value.put(ScreenStyleConfigTable.INDICATOR, packageName);
		mDataProvider.addScreenStyleSetting(packageName, value);
		value = null;
	}

	/**
	 * 重置功能表设置
	 * 
	 * @author huyong
	 */
	public void resetAppSetting() {
		String value = Integer.toString(0);

		mDataProvider.setAppSetting(Setting.MENUAPPSTYLE, value);
		mDataProvider.setAppSetting(Setting.TURNSCREENDIRECTION, value);
		mDataProvider.setAppSetting(Setting.APPNAMEVISIABLE, value);
		mDataProvider.setAppSetting(Setting.LINECOLUMNNUM, value);
		mDataProvider.setAppSetting(Setting.BACKGROUNDPICPATH, value);
	}

	/**
	 * ********************************添加统计模块********************************
	 */
	/**
	 * 更新统计
	 * 
	 * @param key
	 *            键
	 * @param value
	 *            值
	 */
	void updateStatistics(final int key, final long value) {
		final DataProvider dataProvider = mDataProvider;
		dataProvider.updateStatistics(key, value);
	}

	/**
	 * 查询统计
	 * 
	 * @return 数据
	 */
	Cursor queryStatistics() {
		final DataProvider dataProvider = mDataProvider;
		return dataProvider.queryStatistics();
	}

	@Override
	public void cleanup() {
		// mDataProvider = null;
	}

	/**********************
	 * 快捷条数据库操作部分*
	 **********************/

	/**
	 * 更新快捷设置,全局性，与主题无关的信息
	 * 
	 * @param info
	 *            设置信息
	 */
	public boolean updateShortCutSetting_NonIndepenceTheme(ShortCutSettingInfo info) {
		ContentValues values = new ContentValues();
		info.contentValues_NonIndependenceTheme(values);

		// 全局设置修改
		mDataProvider.updateShortCutSetting(values);
		return true;
	}

	public boolean updateShortCutSettingStyle(String themeName, String style) {
		ContentValues values = new ContentValues();
		values.put(ShortcutSettingTable.STYLE_STRING, style);

		mDataProvider.updateShortCutSetting(values, themeName);
		return true;
	}

	public boolean updateShortCutSettingBgSwitch(String themeName, boolean isOn) {
		ContentValues values = new ContentValues();
		values.put(ShortcutSettingTable.BGPICSWITCH, ConvertUtils.boolean2int(isOn));

		mDataProvider.updateShortCutSetting(values, themeName);
		return true;
	}

	public boolean updateShortCutSettingCustomBgSwitch(String themeName, boolean isOn) {
		ContentValues values = new ContentValues();
		values.put(ShortcutSettingTable.CUSTOMBGPICSWITCH, ConvertUtils.boolean2int(isOn));

		mDataProvider.updateShortCutSetting(values, themeName);
		return true;
	}

	/**
	 * 更新DOCK背景相关信息,针对主题修改
	 * 
	 * @param useThemeName
	 *            修改设置针对的主题名
	 * @param targetThemeName
	 *            选择图片是哪个主题包的资源
	 * @param isCustomPic
	 *            是否用户自定义图片（裁减）
	 * @param resName
	 *            如果非用户自定义图片，存为资源名；如果是用户自定义图片，存文件路径名
	 */
	public boolean updateShortCutBG(String useThemeName, String targetThemeName, String resName,
			boolean isCustomPic) {
		ContentValues values = new ContentValues();

		values.put(ShortcutSettingTable.BG_TARGET_THEME_NAME, targetThemeName);
		values.put(ShortcutSettingTable.BG_RESNAME, resName);
		values.put(ShortcutSettingTable.CUSTOM_PIC_OR_NOT, ConvertUtils.boolean2int(isCustomPic));

		mDataProvider.updateShortCutSetting(values, useThemeName);
		return true;
	}

	public boolean updateIsCustomBg(boolean isCustomPic) {
		ContentValues values = new ContentValues();
		values.put(ShortcutSettingTable.CUSTOM_PIC_OR_NOT, ConvertUtils.boolean2int(isCustomPic));

		mDataProvider.updateShortCutSetting(values, ThemeManager.getInstance(mContext)
				.getCurThemePackage());

		return true;
	}

	public boolean updateShortCutSettingEnable(boolean bool) {
		ContentValues values = new ContentValues();

		values.put(ShortcutSettingTable.ENABLE, ConvertUtils.boolean2int(bool));
		mDataProvider.updateShortCutSetting(values);
		return true;
	}

	/**
	 * 插入快捷设置，为默认主题设置
	 * 
	 * @param info
	 *            设置信息
	 */
	public void insertShortCutSetting(ShortCutSettingInfo info) {
		ContentValues values = new ContentValues();
		info.contentValues(values);
		String themeName = GOLauncherApp.getThemeManager().getCurThemePackage();
		if (null != themeName) {
			values.put(ShortcutSettingTable.THEMENAME, themeName);
		}

		// NOTE:2.16加入的新的3个字段如果要修改默认值，可以在此修改

		mDataProvider.insertShortCutSetting(values);
	}

	/**
	 * 获取设置
	 * 
	 * @return 设置信息
	 */
	public ShortCutSettingInfo getShortCurSetting(String themeName) {
		// 2.16修改：设置时用当前主题名，而非原来的默认主题
		Cursor cursor = mDataProvider.queryShortCutSetting(themeName);

		if (cursor != null) {
			try {
				// 原来数据库shortcuttable中有查找到此信息
				ShortCutSettingInfo info = new ShortCutSettingInfo();
				boolean bOK = info.parseFromCursor(cursor);
				if (bOK) {
					return info;
				}
			} catch (SQLiteException e) {
				e.printStackTrace();
			} finally {
				cursor.close();
			}
		}

		return null;
	}

	// 字体
	public FontBean createUsedFontBean() {
		FontBean bean = mDataProvider.getUsedFont();
		if (null == bean) {
			bean = new FontBean();
			mDataProvider.insertUsedFont(bean);
		}
		return bean;
	}

	public void updateUsedFontBean(FontBean bean) {
		mDataProvider.updateUsedFont(bean);
	}

	public ArrayList<FontBean> createFontBeans() {
		ArrayList<FontBean> beans = mDataProvider.getAllFont();
		if (null == beans || beans.size() == 0) {
			// 初始化系统字体
			beans = new ArrayList<FontBean>();
			FontBean bean = new FontBean();
			bean.mFileName = FontTypeface.DEFAULT;
			beans.add(bean);
			bean = new FontBean();
			bean.mFileName = FontTypeface.DEFAULT_BOLD;
			beans.add(bean);
			bean = new FontBean();
			bean.mFileName = FontTypeface.SANS_SERIF;
			beans.add(bean);
			bean = new FontBean();
			bean.mFileName = FontTypeface.SERIF;
			beans.add(bean);
			bean = new FontBean();
			bean.mFileName = FontTypeface.MONOSPACE;
			beans.add(bean);
			updateFontBeans(beans);
		}
		return beans;
	}

	public void updateFontBeans(ArrayList<FontBean> beans) {
		mDataProvider.updateAllFont(beans);
	}

	/**
	 * 恢复快捷条背景至默认
	 * 
	 * @author yangbing
	 * */
	public boolean resetShortCutBg(String useThemeName, String targetThemeName, String resName) {
		ContentValues values = new ContentValues();
		values.put(ShortcutSettingTable.BGPICSWITCH, ConvertUtils.boolean2int(true));
		values.put(ShortcutSettingTable.CUSTOMBGPICSWITCH, ConvertUtils.boolean2int(false));
		values.put(ShortcutSettingTable.CUSTOM_PIC_OR_NOT, ConvertUtils.boolean2int(false));
		values.put(ShortcutSettingTable.BG_TARGET_THEME_NAME, targetThemeName);
		values.put(ShortcutSettingTable.BG_RESNAME, resName);

		mDataProvider.updateShortCutSetting(values, useThemeName);
		return true;
	}
}
