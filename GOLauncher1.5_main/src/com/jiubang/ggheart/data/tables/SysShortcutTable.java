package com.jiubang.ggheart.data.tables;

public class SysShortcutTable {
	public static final String TABLENAME = "sysshortcut";

	public static final String INTENT = "intent";
	public static final String NAME = "name";
	public static final String ICON = "icon";
	public static final String REFCOUNT = "refcount";

	public static final String CREATETABLESQL = "create table sysshortcut" + "(" + "intent text, "
			+ "name text, " + "icon blob, " + "refcount numeric" + ")";
}
