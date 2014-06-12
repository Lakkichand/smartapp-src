package com.jiubang.ggheart.data.tables;

public class FolderTable {

	static public String TABLENAME = "folder";
	static public String ID = "mid"; // 该字段保存ItemInfo.mInScreenId
	static public String FOLDERID = "folderid";
	static public String INTENT = "intent";
	static public String INDEX = "mindex"; // 不能使用"index"
	static public String TYPE = "type";
	static public String USERTITLE = "usertitle";
	static public String USERICONTYPE = "usericontype";
	static public String USERICONID = "usericonid";
	static public String USERICONPACKAGE = "usericonpackage";
	static public String USERICONPATH = "usericonpath";
	static public String FROMAPPDRAWER = "fromappdrawer";
	static public String TIMEINFOLDER = "timeinfolder";

	static public String CREATETABLESQL = "create table folder " + "(" + "mid numeric, "
			+ "folderid numeric, " + "intent text, " + "mindex numeric, " + "type numeric, "
			+ "usertitle text, " + "usericontype numeric, " + "usericonid numeric, "
			+ "usericonpackage text, " + "usericonpath text, " + "fromappdrawer numeric, "
			+ "timeinfolder numeric" + ");";
}
