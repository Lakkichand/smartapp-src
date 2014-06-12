package com.go.util.file.media;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 播放列表ContentProvider信息类
 * @author yejijiong
 *
 */
public class MediaDataProviderConstants {
	public static final String AUTHORITY = "com.jiubang.media.data.MediaDataProvider";
	public static final String CONTENT_DIR_DATA_TYPE = "vnd.android.cursor.dir/vnd.com.jiubang.media.data.database.MediaDataProvider";
	public static final String CONTENT_ITEM_DATA_TYPE = "vnd.android.cursor.item/vnd.com.jiubang.media.data.database.MediaDataProvider";
	public static final String CONTENT_ERROR_DATA_TYPE = "vnd.android.cursor.error/vnd.com.jiubang.media.data.database.MediaDataProvider";

	/**
	 * 播放列表
	 * @author yejijiong
	 */
	public static final class PlayList implements BaseColumns {
		public static final String DATA_PATH = "playList";
		public static final String DATA_WITH_ID_PATH = "playList/#";
		public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/"
				+ DATA_PATH);
		public static final int DATA_CASE = 1;
		public static final int DATA_WITH_ID_CASE = 2;
	}

	/**
	 * 播放列表文件
	 * @author yejijiong
	 */
	public static final class PlayListFile implements BaseColumns {
		public static final String DATA_PATH = "playListFile";
		public static final String DATA_WITH_ID_PATH = "playListFile/#";
		public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/"
				+ DATA_PATH);
		public static final int DATA_CASE = 3;
		public static final int DATA_WITH_ID_CASE = 4;
	}

	/**
	 * 隐藏数据
	 * @author yejijiong
	 */
	public static final class HideData implements BaseColumns {
		public static final String DATA_PATH = "hideData";
		public static final String DATA_WITH_ID_PATH = "hideData/#";
		public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/"
				+ DATA_PATH);
		public static final int DATA_CASE = 5;
		public static final int DATA_WITH_ID_CASE = 6;
	}

	/**
	 * 设置数据
	 * @author yangguanxiang
	 */
	public static final class SettingData implements BaseColumns {
		public static final String DATA_PATH = "settingData";
		public static final String DATA_WITH_ID_PATH = "settingData/#";
		public static final Uri CONTENT_DATA_URI = Uri.parse("content://" + AUTHORITY + "/"
				+ DATA_PATH);
		public static final int DATA_CASE = 7;
		public static final int DATA_WITH_ID_CASE = 8;
	}
}
