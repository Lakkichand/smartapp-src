package com.jiubang.ggheart.data.tables;

/**
 * 全局配置表，从sharedpreferance中迁移过来
 * 
 * @author huyong
 * 
 */
public class ConfigTable {

	/**
	 * 表名
	 */
	static public String TABLENAME = "config";

	/**
	 * 主题
	 */
	static public String THEMENAME = "themename";

	static public String TIPFRAMETIMECURVERSION = "tipframetimecurversion";
	static public String VERSIONCODE = "versioncode";

	/**
	 *  是否3.12版本之前的老用户，用于判断dock默认浏览器跳转
	 *  <li>注意：字段用"isversionbefore313"，原因：
	 *  过滤规则原定用3.13作判断分界线，但在3.13发布前改为3.12为分界线，因为发了内测，所以字段3.13不能修改了
	 */
	static public String ISVERSIONBEFORE312 = "isversionbefore313";

	/**
	 * 建表语句
	 */
	static public String CREATETABLESQL = "create table " + TABLENAME + "(" + THEMENAME + " text, "
			+ TIPFRAMETIMECURVERSION + " text, " + VERSIONCODE + " numeric, " + ISVERSIONBEFORE312
			+ " numeric " + ");";
}
