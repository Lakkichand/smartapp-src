package com.jiubang.ggheart.data.tables;

public interface PartToFolderTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "parttofolder";

	/**
	 * 表字段: PARTID
	 */
	static public String PARTID = "partid";
	/**
	 * 表字段: FOLDERID
	 */
	static public String FOLDERID = "folderid";
	/**
	 * 表字段: MINDEX
	 */
	static public String MINDEX = "mindex";
	/**
	 * 表字段: ITEMTYPE
	 */
	static public String ITEMTYPE = "itemtype";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table parttofolder " + "(" + "partid numeric, "
			+ "folderid numeric, " + "mindex numeric, " + "itemtype numeric" + ")";
}
