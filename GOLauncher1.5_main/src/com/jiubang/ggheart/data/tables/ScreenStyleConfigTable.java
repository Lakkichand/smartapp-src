package com.jiubang.ggheart.data.tables;

import com.jiubang.ggheart.data.theme.ThemeManager;

public class ScreenStyleConfigTable {
	public static String TABLENAME = "screenstyleconfigtable";
	public static String THEMEPACKAGE = "themepackage";
	public static String ICONSTYLEPACKAGE = "iconstylepackage";
	public static String FOLDERSTYLEPACKAGE = "folderstylepackage";
	public static String GGMENUPACKAGE = "ggmenustylepackage";
	public static String INDICATOR = "indicatorstylepackage";

	public static String CREATETABLESQL = "create table " + TABLENAME + "(" + THEMEPACKAGE
			+ " text, " + ICONSTYLEPACKAGE + " text, " + FOLDERSTYLEPACKAGE + " text, "
			+ GGMENUPACKAGE + " text, " + INDICATOR + " text " + ");";
	static String themepackageName = ThemeManager.DEFAULT_THEME_PACKAGE;
	public static String INSERTDEFAULTVALUES = "insert into " + TABLENAME + " values(" + "'"
			+ themepackageName + "', '" + themepackageName + "','" + themepackageName + "','"
			+ themepackageName + "','" + themepackageName + "')";
}
