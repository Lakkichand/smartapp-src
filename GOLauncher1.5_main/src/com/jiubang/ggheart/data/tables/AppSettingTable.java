package com.jiubang.ggheart.data.tables;

import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.plugin.mediamanagement.MediaManagementOpenChooser;

/**
 * 功能设置表
 * 
 * @author huyong
 * 
 */
public class AppSettingTable {
	public final static String TABLENAME = "appSettingTable";
	public final static String INFOTYPE = "infoType";
	public final static String INFOVALUE = "value";

	public final static String MENUAPPSTYLE = "menuappstyle";
	public final static String TURNSCREENDIRECTION = "turnscreendirection";
	public final static String APPNAMEVISIABLE = "appnamevisiable";
	public final static String LINECOLUMNNUM = "linecolumnnum";
	public final static String BACKGROUNDPICPATH = "backgroundpicpath";
	public final static String BACKGROUNDVISIABLE = "backgroundvisiable";
	public final static String SORTTYPE = "sorttype";
	public final static String SHOWNEGLECTAPPS = "showneglectapps";
	public final static String INOUTEFFECT = "inouteffect";
	public final static String ICONEFFECT = "iconeffect";
	public final static String SCROLL_LOOP = "scrollLoop";
	public final static String BLUR_BACKGROUND = "blurBackground";
	public final static String SHOW_TAB_ROW = "showTabRow";
	public final static String VERTICAL_SCROLL_EFFECT = "verticalScrollEffect";
	public final static String THEMEPACKAGENAME = "themepackagename";
	public final static String SHOW_SEARCH = "showsearch";
	public final static String TAB_HOME_BG = "tabhomebackground";
	public final static String INDICATOR_STYLE = "indicatorstyle";
	public final static String ROWNUM = "rownum";
	public final static String COLNUM = "colnum";
	public final static String PROUPDATEAPP = "proupdateapp";
	public final static String CUSTOMINOUTEFFECTITEMS = "custominouteffectitems";
	public final static String CUSTOMICONEFFECT = "customiconeffect";
	public final static String SHOW_HOME_KEY_ONLY = "showhomekeyonly";
	public final static String SHOW_ACTION_BAR = "showactionbar";
	public final static String IMAGE_OPEN_WAY = "imageopenway";
	public final static String AUDIO_OPEN_WAY = "audioopenway";

	public final static String CREATETABLESQL = "create table " + TABLENAME + "(" + THEMEPACKAGENAME
			+ " text, " + MENUAPPSTYLE + " text, " + TURNSCREENDIRECTION + " text, "
			+ APPNAMEVISIABLE + " text, " + LINECOLUMNNUM + " text, " + BACKGROUNDPICPATH
			+ " text, " + BACKGROUNDVISIABLE + " text, " + SORTTYPE + " text, " + SHOWNEGLECTAPPS
			+ " text, " + INOUTEFFECT + " text, " + ICONEFFECT + " text, " + SCROLL_LOOP
			+ " numeric, " + BLUR_BACKGROUND + " numeric, " + SHOW_TAB_ROW + " numeric, "
			+ VERTICAL_SCROLL_EFFECT + " numeric, " + SHOW_SEARCH + " numeric, " + TAB_HOME_BG
			+ " text, " + INDICATOR_STYLE + " text," + ROWNUM + " text, " + COLNUM + " text, "
			+ PROUPDATEAPP + " numerictext, " + CUSTOMINOUTEFFECTITEMS + " text, "
			+ CUSTOMICONEFFECT + " text, " + SHOW_HOME_KEY_ONLY + " text, " + SHOW_ACTION_BAR
			+ " text, " + IMAGE_OPEN_WAY + " text, " + AUDIO_OPEN_WAY + " text" + ");";

	final static String THEME_PACKAGE_NAME = ThemeManager.DEFAULT_THEME_PACKAGE;
	public final static String INSERTAPPSETTINGVALUES = "insert into " + AppSettingTable.TABLENAME
			+ " values(" + "'" + THEME_PACKAGE_NAME + "', " + AppSettingDefault.MENUAPPSTYLE + ", "
			+ AppSettingDefault.TURNSCREENDIRECTION + ", " + AppSettingDefault.APPNAMEVISIABLE
			+ ", " + AppSettingDefault.LINECOLUMNNUM + ", " + AppSettingDefault.BACKGROUNDPICPATH
			+ ", " + AppSettingDefault.BACKGROUNDVISIABLE + ", " + AppSettingDefault.SORTTYPE
			+ ", " + AppSettingDefault.SHOWNEGLECTAPPS + ", " + AppSettingDefault.INOUTEFFECT
			+ ", " + AppSettingDefault.ICONEFFECT + ", " + AppSettingDefault.SCROLL_LOOP + ", "
			+ AppSettingDefault.BLUR_BACKGROUND + ", " + AppSettingDefault.SHOW_TAB_ROW + ", "
			+ AppSettingDefault.VERTICAL_SCROLL_EFFECT + ", "
			+ AppSettingDefault.APP_SEARCH_VISIABLE + ", '" + THEME_PACKAGE_NAME + "','"
			+ THEME_PACKAGE_NAME + "'," + AppSettingDefault.APPFUNC_ROWNUM + ", "
			+ AppSettingDefault.APPFUNC_COLNUM + ", " + AppSettingDefault.PROUPDATEAPP + ", "
			+ "'-1;' ," + "'-1;'" + ", " + AppSettingDefault.SHOW_HOME_KEY_ONLY + ", "
			+ AppSettingDefault.SHOW_ACTION_BAR + ", '" + MediaManagementOpenChooser.APP_NONE
			+ "', '" + MediaManagementOpenChooser.APP_NONE + "')";

	// NOTE:勿删，3版到4版的升级中用到以下语句
	public final static String INSERTMENUAPPSTYLESQL = " insert into appSettingTable( infoType, value ) values( 0, "
			+ AppSettingDefault.MENUAPPSTYLE + " );";
	public final static String INSERTTURNSCREENDIRECTIONSQL = " insert into appSettingTable( infoType, value ) values( 1, "
			+ AppSettingDefault.TURNSCREENDIRECTION + " );";
	public final static String INSERTAPPNAMEVISIABLESQL = " insert into appSettingTable( infoType, value ) values( 2, "
			+ AppSettingDefault.APPNAMEVISIABLE + " );";
	public final static String INSERTLINECOLUMNNUMSQL = " insert into appSettingTable( infoType, value ) values( 3, "
			+ AppSettingDefault.LINECOLUMNNUM + " );";
	public final static String INSERTBACKGROUNDPICPATHSQL = " insert into appSettingTable( infoType, value ) values( 4, "
			+ AppSettingDefault.BACKGROUNDPICPATH + " );";
	public final static String INSERTBACKGROUNDVISIABLESQL = " insert into appSettingTable( infoType, value ) values( 5, "
			+ AppSettingDefault.BACKGROUNDVISIABLE + " );";
	public final static String INSERTSORTTYPESQL = " insert into appSettingTable( infoType, value ) values( 6, "
			+ AppSettingDefault.SORTTYPE + " );";
	public final static String INSERTSHOWNEGLECTAPPSSQL = " insert into appSettingTable( infoType, value ) values( 7, "
			+ AppSettingDefault.SHOWNEGLECTAPPS + " );";

}