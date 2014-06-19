package com.jiubang.go.backup.pro.statistics;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Go桌面统计信息
 * 
 * @author GoBackup Dev Team
 */
public class GOLauncherStatisticsProviderConstants implements BaseColumns {
	public static final String AUTHORITY = "com.gau.go.launcherex.statistics.provider";
	public static final String DATA_PATH = "data";
	public static final String DATA_WITH_ID_PATH = "data/#";
	public static final Uri CONTENT_DATA_URI = Uri
			.parse("content://" + AUTHORITY + "/" + DATA_PATH);
	public static final String CONTENT_DIR_DATA_TYPE = "vnd.android.cursor.dir/vnd.com.gau.go.launcherex.statistics.data";
	public static final String CONTENT_ITEM_DATA_TYPE = "vnd.android.cursor.item/vnd.com.gau.go.launcherex.statistics.data";
	public static final String CONTENT_ERROR_DATA_TYPE = "vnd.android.cursor.error/vnd.com.gau.go.launcherex.statistics.data";
	public static final int DATA_CASE = 1;
	public static final int DATA_WITH_ID_CASE = 2;
	/**
	 * imei的列名
	 */
	public static final String IMEI = "imei";
	/**
	 * imei的id
	 */
	public static final long IMEI_ID = 1L;

}
