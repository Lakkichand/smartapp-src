package com.jiubang.ggheart.data.statistics.tables;

/**
 * 精品应用统计数据记录表
 * 
 * @author zhouxuewen
 * 
 */
public class GoStoreAppTable {
	public static String TABLENAME = "gostoreapp"; // 表名
	public static String PKG_NAME = "pkgname"; // 应用包名的key值
	public static String APP_ID = "appid"; // 应用ID的key值
	public static String APP_NAME = "appname"; // 应用名的key值
	public static String POSTION = "postion"; // 推荐位的key值
	public static String SHOW_COUNT = "showcount"; // 展示量的key值
	public static String CLICK_COUNT = "clickcount"; // 点击数的key值
	public static String DETAIL_SHOW = "detailshow"; // 详情界面点击数的key值
	public static String UPDATE_CLICK = "updateclick"; // 更新点击数的key值
	public static String INSTALL_COUNT = "installcount"; // 安装数的key值
	public static String UPDATE_COUNT = "updatecount"; // 更新数的key值
	public static String ENTRY = "entry"; // 入口的key值
	public static String CLASSIFY = "classify"; // 分类ID的key值
	public static String CLICK_TIME = "clicktime"; // 点击时间

	public static String CREATE_TABLE_SQL = "create table " + TABLENAME + "( " + PKG_NAME
			+ " text, " + APP_ID + " text, " + APP_NAME + " text, " + POSTION + " numeric, "
			+ SHOW_COUNT + " numeric, " + CLICK_COUNT + " numeric, " + DETAIL_SHOW + " numeric, "
			+ UPDATE_CLICK + " numeric, " + INSTALL_COUNT + " numeric, " + UPDATE_COUNT
			+ " numeric, " + ENTRY + " text," + CLASSIFY + " text, " + CLICK_TIME + " text" + ")";

}
