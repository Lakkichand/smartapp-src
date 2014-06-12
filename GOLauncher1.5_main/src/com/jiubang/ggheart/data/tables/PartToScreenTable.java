package com.jiubang.ggheart.data.tables;

public interface PartToScreenTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "parttoscreen";

	/**
	 * 表字段：ID
	 */
	static public String ID = "itemInScreenId";
	/**
	 * 表字段：SCREENID
	 */
	static public String SCREENID = "screenid";
	/**
	 * 表字段：PARTID : 外键引用
	 */
	static public String PARTID = "partid";
	/**
	 * 表字段：SCREENX
	 */
	static public String SCREENX = "screenx";
	/**
	 * 表字段：SCREENY
	 */
	static public String SCREENY = "screeny";
	/**
	 * 表字段：SPANX
	 */
	static public String SPANX = "spanx";
	/**
	 * 表字段：SPANY
	 */
	static public String SPANY = "spany";

	/**
	 * 用户自定义名称
	 */
	static public String USERTITLE = "usertitle";

	/**
	 * 用户自定义图标(废弃，保存图标属性) TODO: 改为保存属性
	 */
	static public String USERICON = "usericon";
	// 类型：0 ：资源 需要配合ID
	// 1: 文件 需要配合路径
	// 2： 默认
	static public String USERICONTYPE = "usericontype";
	static public String USERICONID = "usericonid";
	static public String USERICONPACKAGE = "usericonpackage";
	static public String USERICONPATH = "usericonpath";
	/**
	 * 表字段：ITEMTYPE
	 */
	static public String ITEMTYPE = "itemtype";

	// /**
	// * 表字段：FOLDERID
	// */
	// static public String FOLDERID = "folderid";
	//
	// /**
	// * 表字段：FOLDERID
	// */
	// static public String FOLDERINDEX = "folderindex";

	/**
	 * 表字段：WIDGETID
	 */
	static public String WIDGETID = "widgetid";

	/**
	 * 关联后台应用键 TODO: 系统文件夹考虑和系统应用统一处理
	 */
	static public String INTENT = "intent";
	static public String URI = "uri";

	// 3.10升级字段：文件夹的排序方式
	static public String SORTTYPE = "sorttype";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table " + TABLENAME + "("
			+ "itemInScreenId numeric, " + "screenid numeric, " + "partid numeric, "
			+ "screenx numeric, " + "screeny numeric, " + "spanx numeric, " + "spany numeric, "
			+ "usertitle text, " + "usericon blob, " + "itemtype numeric, " + "widgetid numeric, "
			+ "intent text, "
			+ "uri text, "
			+
			// "folderid numeric, " +
			// "folderindex numeric, " +
			"usericontype numeric, " + "usericonid numeric, " + "usericonpackage text, "
			+ "usericonpath text, " + "sorttype numeric, " + "PRIMARY KEY (itemInScreenId)" + ")";
}
