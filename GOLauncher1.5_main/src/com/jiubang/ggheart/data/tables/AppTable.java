package com.jiubang.ggheart.data.tables;

/**
 * 功能表
 * 
 * @author huyong
 * 
 */
public class AppTable {
	static public String TABLENAME = "application";
	static public String ID = "id";
	static public String PARTID = "partid";
	static public String INDEX = "mindex";
	static public String ISEXIST = "isExist";
	static public String ISSYSAPP = "isSysApp";
	static public String INTENT = "intent";
	static public String FOLDERID = "folderId"; // 若为0，则为程序；否则为文件夹
	static public String TITLE = "title";
	static public String FOLDERICONPATH = "folderIconPath";

	static public String CREATETABLESQL = "create table application " + "(" + "id numeric, "
			+ "partid numeric, " + "mindex numeric, " + "isExist numeric, " + "isSysApp numeric, "
			+ "intent text, " + "folderId numeric, " + "title text, " + "folderIconPath text" + ")";
}
