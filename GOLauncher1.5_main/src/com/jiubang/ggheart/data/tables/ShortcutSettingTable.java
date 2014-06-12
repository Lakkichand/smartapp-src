package com.jiubang.ggheart.data.tables;

public interface ShortcutSettingTable {
	/**
	 * 表名
	 */
	static public final String TABLENAME = "shortcutsetting";

	/**
	 * 表字段: ENABLE
	 */
	static public final String ENABLE = "enable";

	/**
	 * 表字段: BGPICSWITCH
	 */
	static public final String BGPICSWITCH = "bgpicswitch";
	/**
	 * 表字段: AUTOREVOLVE
	 */
	static public final String AUTOREVOLVE = "autorevolve";

	/**
	 * 表字段: STYLE
	 */
	static public final String STYLE = "style";
	/**
	 * 表字段: ROWS
	 */
	static public final String ROWS = "rows";
	/**
	 * 表字段: AUTOMESSAGESTATICS
	 */
	static public final String AUTOMESSAGESTATICS = "automessagestatics";
	/**
	 * 表字段: AUTOMISSCALLSTATICS
	 */
	static public final String AUTOMISSCALLSTATICS = "automisscallstatics";
	/**
	 * 表字段: AUTOMISSMAILSTATICS
	 */
	static public final String AUTOMISSMAILSTATICS = "automissmailstatics";

	/**
	 * 1.5版本 增加主题字段 表字段: AUTOMISSMAILSTATICS 设置生效时的当前主题名
	 */
	static public final String THEMENAME = "themename";

	/**
	 * 2.20新增字段:自定义背景开关
	 */
	static public final String CUSTOMBGPICSWITCH = "custombgpicswitch";

	/**
	 * 2.20新增字段：风格（字符串类型，原来是数字类型）
	 */
	static public final String STYLE_STRING = "stylestring";

	/**
	 * 表字段：bgtargetthemename 指定背景图片所在主题包名
	 */
	static public final String BG_TARGET_THEME_NAME = "bgtargetthemename";

	/**
	 * 表字段：bgresname 指定背景图片名
	 */
	static public final String BG_RESNAME = "bgresname";

	/**
	 * 表字段：bgtargetthemename 是否是用户自定义（裁减）图片
	 */
	static public final String CUSTOM_PIC_OR_NOT = "bgiscustompic";

	static public final String AUTOMISSK9MAILSTATICS = "automissk9mailstatics";
	static public final String AUTOMISSFACEBOOKSTATICS = "automissfacebookstatics";

	static public final String AUTOMISSSINAWEIBOSTATICS = "automisssinaweibostatics";

	/**
	 * 3.02新增表字段: AUTOFIT
	 */
	static public final String AUTOFIT = "autofit";

	/**
	 * 表语句
	 */
	static public String CREATETABLESQL = "create table shortcutsetting " + "("
			+ "enable numeric, " + "autohide numeric, " + "custombgpicswitch numeric, "
			+ "bgpicswitch numeric, " + "autorevolve numeric, " + "hidetitle numeric, "
			+ "style numeric, " + "stylestring text, " + "rows numeric, "
			+ "automessagestatics numeric, " + "automisscallstatics numeric, "
			+ "automissmailstatics numeric, " + "themename text, " + "bgtargetthemename text, "
			+ "bgresname text, " + "bgiscustompic numeric, " + "automissk9mailstatics numeric, "
			+ "automissfacebookstatics numeric, " + "automisssinaweibostatics numeric, "
			+ "autofit numeric " + ")";
}
