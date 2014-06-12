package com.jiubang.ggheart.data.tables;

/**
 * dock非自适应模式数据表，仅保存此模式下的特有数据（例如：空白显示） v3.11
 * 
 * @author ruxueqin
 * 
 */
public class ShortcutUnfitTable {

	public static String TABLENAME = "shortcutunfit";// 表名

	public static String ROWID = "rowid"; // 行id

	public static String INDEX = "mindex"; // 行内索引

	public static String INTENT = "intent"; // 响应

	public static String CREATETABLESQL = "create table shortcutunfit" + "(" + "rowid numeric, "
			+ "mindex numeric, " + "intent text " + ")";
}
