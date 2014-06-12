package com.jiubang.ggheart.data.tables;

/**
 * 主题相关表
 * @author yangguanxiang
 *
 */
public class ThemeTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "theme";

	/**
	 * 表字段: THEMENAME
	 */
	static public String THEMENAME = "themename";
	/**
	 * 表字段: VERSION
	 */
	static public String VERSION = "version";
	/**
	 * 表字段: AUTOCHECKVERSION
	 */
	static public String AUTOCHECKVERSION = "autocheckversion";
	/**
	 * 表字段: BACKGROUNDIMAGE
	 */
	static public String BACKGROUNDIMAGE = "backgroundimage";
	/**
	 * 表字段: ISPEMANENTMEMORY
	 */
	static public String ISPEMANENTMEMORY = "ispemanentmemory";
	/**
	 * 表字段: ISCACHEDESK
	 */
	static public String ISCACHEDESK = "iscachedesk";
	/**
	 * 表字段: FONT
	 */
	static public String FONT = "font";

	static public String LASTSHOWTIME = "lastshowtime";

	static public String PREVENTFORCECLOSE = "preventforceclose";

	public static String HIGHQUALITYDRAWING = "highqualitydrawing";

	public static String TRANSPARENTSTATUSBAR = "transparentstatusbar";

	/*
	 * 第一次运行
	 */
	public static String FIRSTRUN = "firstrun";

	public static String TIPCANCELDEFAULTDESK = "tipcanceldefaultdesk";

	public static String CLOUD_SECURITY = "cloudsecurity";
	
	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table theme " + "(" + "themename text, "
			+ "version numeric, " + "autocheckversion numeric, " + "backgroundimage text, "
			+ "ispemanentmemory numeric, " + "iscachedesk numeric, " + "font text, "
			+ "lastshowtime numeric, " + "preventforceclose numeric, "
			+ "highqualitydrawing numeric, " + "transparentstatusbar numeric, "
			+ "firstrun numeric, " + "tipcanceldefaultdesk numeric, " + "cloudsecurity numeric"
			+ ")";
}
