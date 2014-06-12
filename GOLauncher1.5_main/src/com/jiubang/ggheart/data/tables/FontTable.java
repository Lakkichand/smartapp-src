package com.jiubang.ggheart.data.tables;

public class FontTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "font";

	/**
	 * 表字段: FONTFILETYPE
	 */
	static public String FONTFILETYPE = "fontfiletype";
	/**
	 * 表字段: FONTPACKAGE
	 */
	static public String FONTPACKAGE = "fontpackage";
	/**
	 * 表字段: FONTTITLE
	 */
	static public String FONTTITLE = "fonttitle";
	/**
	 * 表字段: FONTFILE
	 */
	static public String FONTFILE = "fontfile";
	/**
	 * 表字段: FONTSTYLE
	 */
	static public String FONTSTYLE = "fontstyle";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table font " + "(" + "fontfiletype numeric, "
			+ "fontpackage text, " + "fonttitle text, " + "fontfile text, " + "fontstyle text"
			+ ")";
}
