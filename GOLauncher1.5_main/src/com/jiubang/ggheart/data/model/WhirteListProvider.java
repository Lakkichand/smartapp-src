package com.jiubang.ggheart.data.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.DatabaseHelper;
import com.jiubang.ggheart.data.tables.AppWhiteListTable;

public class WhirteListProvider extends ContentProvider {
	public static final String TABLE_NAME = AppWhiteListTable.TABLENAME;
	public static final String AUTHORITIE = "com.jiubang.ggheart.data.model.wirteprovider";
	private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	public static final int CODE = 1;
	static {
		// 添加相应的匹配URI 参数1: 主机域名,参数2: 路径(需要访问的资源)参数3: 当URI匹配将返回一个int返回码,表示匹配成功
		uriMatcher.addURI(AUTHORITIE, "appwhitelist", CODE);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		DatabaseHelper dbHelper = null;
		SQLiteDatabase db = null;
		DataProvider dp = DataProvider.getInstance(this.getContext());
		if (dp != null) {
			dbHelper = dp.getmDBOpenHelper();
		}
		if (dbHelper != null) {
			db = dbHelper.getReadableDatabase();
		}
		if (db != null) {
			if ((uriMatcher.match(uri) == CODE)) {
				return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, null);
			}
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
}
