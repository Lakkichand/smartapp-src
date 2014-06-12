package com.jiubang.ggheart.data.tables;

/**
 * 推荐应用表
 * @author yejijiong
 *
 */
public class RecommendAppTable {

	public final static String TABLENAME = "recommendapp";
	public final static String INTENT = "intent"; // intent信息
	public final static String SHOWNEW = "shownew"; // 是否显示new标识

	public final static String CREATETABLESQL = "create table " + TABLENAME + " ( " + INTENT + " text, "
			+ SHOWNEW + " numeric " + " ) ";
}
