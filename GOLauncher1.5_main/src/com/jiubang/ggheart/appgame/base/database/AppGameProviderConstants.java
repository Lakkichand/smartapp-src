package com.jiubang.ggheart.appgame.base.database;

import android.net.Uri;
import android.provider.BaseColumns;
/**
 * 
 * <br>类描述:应用游戏中心ContentProvider常量
 * <br>功能详细描述:
 * 
 * @author  wangzhuobin
 * @date  [2012-11-9]
 */
public class AppGameProviderConstants implements BaseColumns {
	public static final String AUTHORITY = "com.jiubang.ggheart.appgame.base.database.AppGameContentProvider";
	public static final String DATA_PATH = "data";
	public static final String DATA_WITH_ID_PATH = "data/#";
	public static final Uri CONTENT_DATA_URI = Uri
			.parse("content://" + AUTHORITY + "/" + DATA_PATH);
	public static final String CONTENT_DIR_DATA_TYPE = "vnd.android.cursor.dir/vnd.com.jiubang.ggheart.appgame.base.database.AppGameContentProvider";
	public static final String CONTENT_ITEM_DATA_TYPE = "vnd.android.cursor.item/vnd.com.jiubang.ggheart.appgame.base.database.AppGameContentProvider";
	public static final String CONTENT_ERROR_DATA_TYPE = "vnd.android.cursor.error/vnd.com.jiubang.ggheart.appgame.base.database.AppGameContentProvider";
	public static final int DATA_CASE = 1;
	public static final int DATA_WITH_ID_CASE = 2;
}
