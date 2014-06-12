/*
 * 文 件 名:  AppGameSettingTable.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-25
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.setting;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-25]
 */
public class AppGameSettingTable {
	public static String TABLENAME = "appgametable";
	public static String LOADIMAGESSTYLE = "load_images_style";

	public static String TRAFFIC_SAVING_MODE = "traffic_saving_mode";
	public static String CREATETABLESQL = "create table " + TABLENAME + "( " + LOADIMAGESSTYLE
			+ " numeric);";

	public static String CREATETABLE = "create table " + TABLENAME + "( " + TRAFFIC_SAVING_MODE
			+ " numeric);";
	public static String INSERTSQL = "INSERT INTO " + TABLENAME + "( " + TRAFFIC_SAVING_MODE + ")"
			+ " VALUES( " + AppGameSettingData.LOADING_ALL_IMAGES + " );";
}
