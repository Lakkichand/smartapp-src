package com.jiubang.ggheart.data.tables;

public class SysFolderTable {
	public static final String TABLENAME = "sysfolder";

	public static final String INTENT = "intent";
	public static final String URI = "uri";
	public static final String DISPLAYMODE = "displaymode";
	public static final String NAME = "name";
	public static final String ICON = "icon";
	public static final String REFCOUNT = "refcount";

	public static final String CREATETABLESQL = "create table sysfolder" + "(" + "intent text, "
			+ "uri text, " + "displaymode numeric, " + "name text, " + "icon blob, "
			+ "refcount numeric" + ")";
}
