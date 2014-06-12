package com.jiubang.ggheart.components.diygesture.model;

/***
 * 数据库中自定义手势表
 * 
 */
public class DiyGestureTable {

	public static final String TABLENAME = "diygesture"; // 表名

	public static final String ID = "mid"; // 手势id,唯一标识符
	public static final String NAME = "name"; // 手势名称，用于view中显示手势名称
	public static final String INTENT = "intent"; // 手势intent，用于起程序
	public static final String TYPE = "itemtype"; // iitemtype

	/**
	 * 表语句
	 */
	public static final String CREATETABLESQL = "create table " + TABLENAME + "(" + ID
			+ " numeric, " + NAME + " text, " + INTENT + " text, " + TYPE + " numeric" + ")";
}
