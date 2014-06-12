package com.jiubang.ggheart.data.tables;

/**
 * 在功能表资源管理中隐藏资源的表，存放的是被隐藏的文件夹uri和类型（类型的值为FileEngine中定义的图片，视频和音乐类型值）
 * 
 * @author huangshaotao
 * 
 */
public class MediaManagementHideTable {

	public static final String TABLENAME = "mediamanagementhideTable";

	public static final String URI = "uri";
	public static final String TYPE = "type";

	public static String CREATETABLESQL = "create table " + TABLENAME + "(" + URI + " text, "
			+ TYPE + " numeric " + ");";
}
