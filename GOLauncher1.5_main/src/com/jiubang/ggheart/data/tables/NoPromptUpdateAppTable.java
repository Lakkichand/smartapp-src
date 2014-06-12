package com.jiubang.ggheart.data.tables;

/**
 * 忽略更新应用名单
 * 
 * @author zhoujun
 * 
 */
public class NoPromptUpdateAppTable {
	static public String TABLENAME = "nopromptupdateapp";

	static public String INTENT = "intent";

	static public String NOUPDATE = "noupdate";

	static public String CREATETABLESQL = "create table " + TABLENAME + " ( " + INTENT + " text, "
			+ NOUPDATE + " numeric " + " ) ";
}
