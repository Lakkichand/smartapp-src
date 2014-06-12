package com.jiubang.ggheart.data.tables;

public interface DynamicEffectTable {
	/**
	 * 表名
	 */
	static public String TABLENAME = "dynamiceffect";

	/**
	 * 表字段: EABLE
	 */
	static public String EABLE = "enable";
	/**
	 * 表字段: CSROLLSPEED
	 */
	static public String SCROLLSPEED = "scrollspeed";
	/**
	 * 表字段: BACKSPEED
	 */
	static public String BACKSPEED = "backspeed";
	/**
	 * 表字段: EFFECT
	 */
	static public String EFFECT = "effect";
	/**
	 * 表字段: EFFECTORTYPE
	 */
	static public String EFFECTORTYPE = "effectortype";

	/**
	 * 表字段: AUTOTWEAKELASTICITY
	 */
	static public String AUTOTWEAKELASTICITY = "autotweakelasticity";
	/**
	 * 表字段: EFFECTORITEM
	 */
	// static public String EFFECTORITEM = "effectoritem";
	/**
	 * 表字段: EFFECTORRANDOMITEMS
	 */
	static public String EFFECTORRANDOMITEMS = "effectorrandomitems";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table dynamiceffect " + "(" + "enable numeric, "
			+ "scrollspeed numeric, " + "backspeed numeric, " + "effect numeric, "
			+ "effectortype numeric, " + "autotweakelasticity numeric, "
			+ "effectorrandomitems text" + ")";
}
