package com.jiubang.ggheart.data.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jiubang.ggheart.data.DatabaseHelper;
import com.jiubang.ggheart.data.Setting;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.plugin.mediamanagement.MediaManagementOpenChooser;

/**
 * 功能表设置表,提供了一系列设置和获取基本数据类型设置项的静态方法，DataProvider中直接调用
 * 
 * @author huangshaotao
 * 
 */
public class AppFuncSettingTable {

	public static final String TABLENAME = "appfuncsettingtable";

	public static final String SETTINGKEY = "settingkey";
	public static final String SETTINGVALUE = "settingvalue";
	public static final String PKNAME = "pkname";

	public final static String CREATETABLESQL = "create table " + TABLENAME + "(" + SETTINGKEY
			+ " numeric, " + SETTINGVALUE + " text, " + PKNAME + " text " + ");";

	// /**
	// * 返回key对应的整数类型设置项的值
	// * @param dbHelper
	// * @param key
	// * @param defaultValue 如果数据库中找不到对应的设置项则返回默认值
	// * @return
	// */
	// public static int getIntValue(DatabaseHelper dbHelper,String pkname, int
	// key, int defaultValue){
	// int value = defaultValue;
	// Cursor cursor = getCursor(dbHelper,pkname, key);
	// if (cursor!= null) {
	// try {
	// if (cursor.moveToFirst()) {
	// value = cursor.getInt(cursor.getColumnIndex(SETTINGVALUE));
	// }
	// } finally {
	// cursor.close();
	// }
	// }
	// return value;
	// }
	//
	// /**
	// * 返回key对应的boolean类型设置项的值
	// * @param dbHelper
	// * @param key
	// * @param defaultValue 如果数据库中找不到对应的设置项则返回默认值
	// * @return
	// */
	// public static boolean getBooleanValue(DatabaseHelper dbHelper, String
	// pkname,int key, boolean defaultValue){
	// boolean value = defaultValue;
	// Cursor cursor = getCursor(dbHelper, pkname, key);
	// if (cursor!= null) {
	// try {
	// if (cursor.moveToFirst()) {
	// int temp = cursor.getInt(cursor.getColumnIndex(SETTINGVALUE));
	// value = (temp==0?false:true);
	// }
	// } finally {
	// cursor.close();
	// }
	// }
	// return value;
	// }
	// /**
	// * 返回key对应的String类型设置项的值
	// * @param dbHelper
	// * @param key
	// * @param defaultValue 如果数据库中找不到对应的设置项则返回默认值
	// * @return
	// */
	// public static String getStringValue(DatabaseHelper dbHelper,String
	// pkname, int key, String defaultValue){
	// String value = defaultValue;
	// Cursor cursor = getCursor(dbHelper, pkname, key);
	// if (cursor!= null) {
	// try {
	// if (cursor.moveToFirst()) {
	// value = cursor.getString(cursor.getColumnIndex(SETTINGVALUE));
	// }
	// } finally {
	// cursor.close();
	// }
	// }
	// return value;
	// }
	// /**
	// * 设置整数类型值
	// * @param dbHelper
	// * @param key
	// * @return 成功返回ture，失败false
	// */
	// public static boolean setIntValue(DatabaseHelper dbHelper, int key, int
	// value){
	// ContentValues values = new ContentValues();
	// values.put(SETTINGKEY, key);
	// values.put(SETTINGVALUE, value);
	//
	// return setValues(dbHelper, key, values);
	// }
	// /**
	// * 设置boolean类型值
	// * @param dbHelper
	// * @param key
	// * @return 成功返回ture，失败false
	// */
	// public static boolean setBooleanValue(DatabaseHelper dbHelper,String
	// pkname, int key, boolean value){
	// ContentValues values = new ContentValues();
	// values.put(SETTINGKEY, key);
	// values.put(SETTINGVALUE, value);
	// values.put(PKNAME, pkname);
	// return setValues(dbHelper, key, values);
	// }
	/**
	 * 设置String类型值
	 * 
	 * @param dbHelper
	 * @param key
	 * @return 成功返回ture，失败false
	 */
	public static boolean setStringValue(DatabaseHelper dbHelper, String pkname, int key,
			String value) {
		ContentValues values = new ContentValues();
		values.put(SETTINGKEY, key);
		values.put(SETTINGVALUE, value);
		values.put(PKNAME, pkname);
		boolean ret = false;
		try {
			dbHelper.insert(TABLENAME, values);
			ret = true;
		} catch (Exception e) {
			ret = false;
		}
		return ret;
	}

	// private static void delByKey(DatabaseHelper dbHelper, int key) throws
	// DatabaseException{
	// String selection = SETTINGKEY + " = " + key + " ";
	// dbHelper.delete(TABLENAME, selection, null);
	// }

	// private static boolean setValues(DatabaseHelper dbHelper, int key,
	// ContentValues values){
	// boolean ret = false;
	// try {
	// dbHelper.beginTransaction();
	// delByKey(dbHelper,key);
	// dbHelper.insert(TABLENAME, values);
	//
	// dbHelper.setTransactionSuccessful();
	// ret = true;
	// } catch (DatabaseException e) {
	// }finally{
	// dbHelper.endTransaction();
	// }
	// return ret;
	// }

	// private static Cursor getCursor(DatabaseHelper dbHelper, String pkname,
	// int key){
	// return dbHelper.query(TABLENAME, null, PKNAME + "='"+pkname + "' and "
	// +SETTINGKEY + "=" + key, null, null);
	// }

	
	public static void loadDataFromOldSettingTable(Cursor c, SQLiteDatabase db) throws Exception {
		if (c != null) {
			try {
				if (c.moveToFirst()) {
					do {
						int idx = c.getColumnIndex(AppSettingTable.THEMEPACKAGENAME);
						String pkname = c.getString(idx);
						loadData(c, db, pkname, AppSettingTable.MENUAPPSTYLE);
						loadData(c, db, pkname, AppSettingTable.TURNSCREENDIRECTION);
						loadData(c, db, pkname, AppSettingTable.APPNAMEVISIABLE);
						loadData(c, db, pkname, AppSettingTable.LINECOLUMNNUM);
						loadData(c, db, pkname, AppSettingTable.BACKGROUNDPICPATH);
						loadData(c, db, pkname, AppSettingTable.BACKGROUNDVISIABLE);
						loadData(c, db, pkname, AppSettingTable.SORTTYPE);
						loadData(c, db, pkname, AppSettingTable.SHOWNEGLECTAPPS);
						loadData(c, db, pkname, AppSettingTable.INOUTEFFECT);
						loadData(c, db, pkname, AppSettingTable.ICONEFFECT);
						loadData(c, db, pkname, AppSettingTable.SCROLL_LOOP);
						loadData(c, db, pkname, AppSettingTable.BLUR_BACKGROUND);
						loadData(c, db, pkname, AppSettingTable.SHOW_TAB_ROW);
						loadData(c, db, pkname, AppSettingTable.VERTICAL_SCROLL_EFFECT);
						loadData(c, db, pkname, AppSettingTable.SHOW_SEARCH);
						loadData(c, db, pkname, AppSettingTable.TAB_HOME_BG);
						loadData(c, db, pkname, AppSettingTable.INDICATOR_STYLE);
						loadData(c, db, pkname, AppSettingTable.ROWNUM);
						loadData(c, db, pkname, AppSettingTable.COLNUM);
						loadData(c, db, pkname, AppSettingTable.PROUPDATEAPP);
						loadData(c, db, pkname, AppSettingTable.CUSTOMINOUTEFFECTITEMS);
						loadData(c, db, pkname, AppSettingTable.CUSTOMICONEFFECT);
						loadData(c, db, pkname, AppSettingTable.SHOW_HOME_KEY_ONLY);
						loadData(c, db, pkname, AppSettingTable.SHOW_ACTION_BAR);
						loadData(c, db, pkname, AppSettingTable.IMAGE_OPEN_WAY);
						loadData(c, db, pkname, AppSettingTable.AUDIO_OPEN_WAY);

					} while (c.moveToNext());
				}
			} catch (Exception e) {
				throw e;
			} finally {
				c.close();
			}
		}
	}

	private static void loadData(Cursor c, SQLiteDatabase db, String kpName, String columnName)
			throws Exception {
		int idx = c.getColumnIndex(columnName);
		String value = c.getString(idx);
		int key = getKeyByColumnName(columnName);
		ContentValues values = new ContentValues();
		values.put(SETTINGKEY, key);
		values.put(SETTINGVALUE, value);
		values.put(PKNAME, kpName);
		long id = db.insert(TABLENAME, null, values);
		if (id == -1) {
			throw new Exception("AppFunSettingTable loadData error!");
		}
	}

	public static int getKeyByColumnName(String columnName) {

		if (AppSettingTable.MENUAPPSTYLE.equals(columnName)) {
			return Setting.MENUAPPSTYLE;
		}
		if (AppSettingTable.TURNSCREENDIRECTION.equals(columnName)) {
			return Setting.TURNSCREENDIRECTION;
		}
		if (AppSettingTable.APPNAMEVISIABLE.equals(columnName)) {
			return Setting.APPNAMEVISIABLE;
		}
		if (AppSettingTable.LINECOLUMNNUM.equals(columnName)) {
			return Setting.LINECOLUMNNUM;
		}
		if (AppSettingTable.BACKGROUNDPICPATH.equals(columnName)) {
			return Setting.BACKGROUNDPICPATH;
		}
		if (AppSettingTable.BACKGROUNDVISIABLE.equals(columnName)) {
			return Setting.BGVISIABLE;
		}
		if (AppSettingTable.SORTTYPE.equals(columnName)) {
			return Setting.SORTTYPE;
		}
		if (AppSettingTable.SHOWNEGLECTAPPS.equals(columnName)) {
			return Setting.SHOWNEGLECTAPP;
		}
		if (AppSettingTable.INOUTEFFECT.equals(columnName)) {
			return Setting.INOUTEFFECT;
		}
		if (AppSettingTable.ICONEFFECT.equals(columnName)) {
			return Setting.ICONEFFECT;
		}
		if (AppSettingTable.SCROLL_LOOP.equals(columnName)) {
			return Setting.SCROLL_LOOP;
		}
		if (AppSettingTable.BLUR_BACKGROUND.equals(columnName)) {
			return Setting.BLUR_BACKGROUND;
		}
		if (AppSettingTable.SHOW_TAB_ROW.equals(columnName)) {
			return Setting.SHOW_TAB_ROW;
		}
		if (AppSettingTable.VERTICAL_SCROLL_EFFECT.equals(columnName)) {
			return Setting.VERTICAL_SCROLL_EFFECT;
		}
		if (AppSettingTable.SHOW_SEARCH.equals(columnName)) {
			return Setting.SHOW_SEARCH;
		}
		if (AppSettingTable.TAB_HOME_BG.equals(columnName)) {
			return Setting.TAB_HOME_BG;
		}
		if (AppSettingTable.INDICATOR_STYLE.equals(columnName)) {
			return Setting.INDICATOR_STYLE;
		}
		if (AppSettingTable.ROWNUM.equals(columnName)) {
			return Setting.APPFUNC_ROWNUM;
		}
		if (AppSettingTable.COLNUM.equals(columnName)) {
			return Setting.APPFUNC_COLNUM;
		}
		if (AppSettingTable.PROUPDATEAPP.equals(columnName)) {
			return Setting.APPUPDATE;
		}
		if (AppSettingTable.CUSTOMINOUTEFFECTITEMS.equals(columnName)) {
			return Setting.APPINOUTCUSTOMRANDOMEFFECT;
		}
		if (AppSettingTable.CUSTOMICONEFFECT.equals(columnName)) {
			return Setting.APPICONCUSTOMEFFECTSETTING;
		}
		if (AppSettingTable.SHOW_HOME_KEY_ONLY.equals(columnName)) {
			return Setting.SHOW_HOME_KEY_ONLY;
		}
		if (AppSettingTable.SHOW_ACTION_BAR.equals(columnName)) {
			return Setting.SHOW_ACTION_BAR;
		}
		if (AppSettingTable.IMAGE_OPEN_WAY.equals(columnName)) {
			return Setting.IMAGE_OPEN_WAY;
		}
		if (AppSettingTable.AUDIO_OPEN_WAY.equals(columnName)) {
			return Setting.AUDIO_OPEN_WAY;
		}
		return -1;
	}

	/**
	 * 创建初始数据
	 * 
	 * @param db
	 */
	public static void initDefaultDatas(SQLiteDatabase db) {
		String pkName = ThemeManager.DEFAULT_THEME_PACKAGE;

		insertData(db, pkName, AppSettingTable.MENUAPPSTYLE,
				String.valueOf(AppSettingDefault.MENUAPPSTYLE));
		insertData(db, pkName, AppSettingTable.TURNSCREENDIRECTION,
				String.valueOf(AppSettingDefault.TURNSCREENDIRECTION));
		insertData(db, pkName, AppSettingTable.APPNAMEVISIABLE,
				String.valueOf(AppSettingDefault.APPNAMEVISIABLE));
		insertData(db, pkName, AppSettingTable.LINECOLUMNNUM,
				String.valueOf(AppSettingDefault.LINECOLUMNNUM));
		insertData(db, pkName, AppSettingTable.BACKGROUNDPICPATH,
				String.valueOf(AppSettingDefault.BACKGROUNDPICPATH));
		insertData(db, pkName, AppSettingTable.BACKGROUNDVISIABLE,
				String.valueOf(AppSettingDefault.BACKGROUNDVISIABLE));
		insertData(db, pkName, AppSettingTable.SORTTYPE, String.valueOf(AppSettingDefault.SORTTYPE));
		insertData(db, pkName, AppSettingTable.SHOWNEGLECTAPPS,
				String.valueOf(AppSettingDefault.SHOWNEGLECTAPPS));
		insertData(db, pkName, AppSettingTable.INOUTEFFECT,
				String.valueOf(AppSettingDefault.INOUTEFFECT));
		insertData(db, pkName, AppSettingTable.ICONEFFECT,
				String.valueOf(AppSettingDefault.ICONEFFECT));
		insertData(db, pkName, AppSettingTable.SCROLL_LOOP,
				String.valueOf(AppSettingDefault.SCROLL_LOOP));
		insertData(db, pkName, AppSettingTable.BLUR_BACKGROUND,
				String.valueOf(AppSettingDefault.BLUR_BACKGROUND));
		insertData(db, pkName, AppSettingTable.SHOW_TAB_ROW,
				String.valueOf(AppSettingDefault.SHOW_TAB_ROW));
		insertData(db, pkName, AppSettingTable.VERTICAL_SCROLL_EFFECT,
				String.valueOf(AppSettingDefault.VERTICAL_SCROLL_EFFECT));
		insertData(db, pkName, AppSettingTable.SHOW_SEARCH,
				String.valueOf(AppSettingDefault.APP_SEARCH_VISIABLE));
		insertData(db, pkName, AppSettingTable.TAB_HOME_BG, pkName);
		insertData(db, pkName, AppSettingTable.INDICATOR_STYLE, pkName);
		insertData(db, pkName, AppSettingTable.ROWNUM,
				String.valueOf(AppSettingDefault.APPFUNC_ROWNUM));
		insertData(db, pkName, AppSettingTable.COLNUM,
				String.valueOf(AppSettingDefault.APPFUNC_COLNUM));
		insertData(db, pkName, AppSettingTable.PROUPDATEAPP,
				String.valueOf(AppSettingDefault.PROUPDATEAPP));
		insertData(db, pkName, AppSettingTable.CUSTOMINOUTEFFECTITEMS, "-1;");
		insertData(db, pkName, AppSettingTable.CUSTOMICONEFFECT, "-1;");
		insertData(db, pkName, AppSettingTable.SHOW_HOME_KEY_ONLY,
				String.valueOf(AppSettingDefault.SHOW_HOME_KEY_ONLY));
		insertData(db, pkName, AppSettingTable.SHOW_ACTION_BAR,
				String.valueOf(AppSettingDefault.SHOW_ACTION_BAR));
		insertData(db, pkName, AppSettingTable.IMAGE_OPEN_WAY, MediaManagementOpenChooser.APP_NONE);
		insertData(db, pkName, AppSettingTable.AUDIO_OPEN_WAY, MediaManagementOpenChooser.APP_NONE);

	}

	private static void insertData(SQLiteDatabase db, String pkName, String columnName, String value) {
		int key = getKeyByColumnName(columnName);
		ContentValues values = new ContentValues();
		values.put(SETTINGKEY, key);
		values.put(SETTINGVALUE, value);
		values.put(PKNAME, pkName);
		db.insert(TABLENAME, null, values);
	}
}
