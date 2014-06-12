package com.jiubang.ggheart.data.tables;

public class ShortcutTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "shortcut";

	/**
	 * 表字段：PARTID
	 */
	static public String PARTID = "partid";

	/**
	 * 表字段：PARTID
	 */
	static public String REFID = "refid";

	/**
	 * 表字段：MINDEX NOTE:2.87升级后，意义改变为：所在行的索引
	 */
	static public String MINDEX = "mindex";

	/**
	 * 表字段：MINTENT
	 */
	static public String INTENT = "intent";

	/**
	 * 
	 */
	static public String UPINTENT = "upintent";
	static public String DOWNINTENT = "downintent";
	static public String UPACTION = "upaction";
	static public String DOWNACTION = "downaction";
	// static public String USERICON = "usericon";
	static public String USERICONID = "usericonid";
	static public String USEPACKAGE = "usepackage";
	static public String USERICONPACKAGE = "usericonpackage";
	static public String USERICONPATH = "usericonpath";
	static public String UPINNERACTION = "upinneraction";
	static public String DOWNINNERACTION = "downinneraction";
	// v3.11前usericontype
	static public String USERICONTYPE = "usericontype";
	// v3.11后usericontype
	static public String ICONTYPE = "icontype";

	// 1.5版本追加主题字段
	static public String THEMENAME = "themename";

	// 2.52加字段：组件类型 itemtype
	static public final String ITEMTYPE = "itemtype";

	// 2.52第二次升级加字段：组件定义名称
	static public final String USERTITLE = "usertitle";

	// 2.87升级加字段：图标在dock第几行
	static public final String ROWSID = "rowsid";

	// 3.10升级字段：文件夹的排序方式
	static public String SORTTYPE = "sorttype";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table shortcut " + "(" + "partid numeric, "
			+ "refid numeric, " + "mindex numeric, " + "intent text, " + "upintent text, "
			+ "downintent text, " + "upaction text, " + "downaction text, "
			+ "usericontype numeric, " + "icontype numeric, " + "usepackage text, "
			+ "usericonid numeric, " + "usericonpackage text, " + "usericonpath text, "
			+ "upinneraction numeric, " + "downinneraction numeric, " + "themename text, "
			+ "itemtype numeric, " + "usertitle text, " + "rowsid numeric, " + "sorttype numeric"
			+ ")";
}
