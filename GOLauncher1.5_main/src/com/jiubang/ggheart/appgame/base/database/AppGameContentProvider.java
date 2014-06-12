/*
 * 文 件 名:  AppGameDataProvider.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-9
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.jiubang.ggheart.appgame.base.setting.AppGameSettingTable;
import com.jiubang.ggheart.data.DatabaseException;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-9]
 */
public class AppGameContentProvider extends ContentProvider {

	private AppGameDataBaseHelper mDBOpenHelper = null;

	private static UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AppGameProviderConstants.AUTHORITY, AppGameProviderConstants.DATA_PATH,
				AppGameProviderConstants.DATA_CASE);
		sUriMatcher.addURI(AppGameProviderConstants.AUTHORITY,
				AppGameProviderConstants.DATA_WITH_ID_PATH,
				AppGameProviderConstants.DATA_WITH_ID_CASE);
	}

	@Override
	public boolean onCreate() {
		mDBOpenHelper = new AppGameDataBaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		Cursor cursor = null;
		switch (sUriMatcher.match(uri)) {
			case AppGameProviderConstants.DATA_CASE : {
				cursor = getAppGameValue();
			}
				break;
			case AppGameProviderConstants.DATA_WITH_ID_CASE : {
			}
				break;
			default :
				break;
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		String type = null;
		switch (sUriMatcher.match(uri)) {
			case AppGameProviderConstants.DATA_CASE : {
				type = AppGameProviderConstants.CONTENT_DIR_DATA_TYPE;
			}
				break;
			case AppGameProviderConstants.DATA_WITH_ID_CASE : {
				type = AppGameProviderConstants.CONTENT_ITEM_DATA_TYPE;
			}
				break;
			default : {
				type = AppGameProviderConstants.CONTENT_ERROR_DATA_TYPE;
			}
				break;
		}
		return type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Uri returnUri = null;
		switch (sUriMatcher.match(uri)) {
			case AppGameProviderConstants.DATA_CASE : {
				insertAppGame(values);
				getContext().getContentResolver().notifyChange(uri, null);
				returnUri = uri;
			}
				break;
			case AppGameProviderConstants.DATA_WITH_ID_CASE : {
			}
				break;
			default :
				break;
		}
		return returnUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
			case AppGameProviderConstants.DATA_CASE : {
			}
				break;
			case AppGameProviderConstants.DATA_WITH_ID_CASE : {
			}
				break;
			default :
				break;
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int rowCount = 0;
		switch (sUriMatcher.match(uri)) {
			case AppGameProviderConstants.DATA_CASE : {
				rowCount = updateAppGame(values);
				getContext().getContentResolver().notifyChange(uri, null);
			}
				break;
			case AppGameProviderConstants.DATA_WITH_ID_CASE : {
			}
				break;
			default :
				break;
		}
		return rowCount;
	}

	/**
	 * 功能简述:应用游戏中心设置数据插入数据库 功能详细描述: 注意:
	 * 
	 * @param values
	 */
	public void insertAppGame(ContentValues values) {
		try {
			mDBOpenHelper.insert(AppGameSettingTable.TABLENAME, values);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 功能简述:应用游戏中心设置数据的更新 功能详细描述: 注意:
	 * 
	 * @param Column
	 * @param value
	 * @return true更新数据成功
	 */
	public int updateAppGame(ContentValues values) {
		int count = 0;
		try {
			count = mDBOpenHelper.update(AppGameSettingTable.TABLENAME, values, null, null);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return count;
	}

	/**
	 * 功能简述:应用游戏中心数据库数据的查询，返回Cursor 功能详细描述: 注意:
	 * 
	 * @return
	 */
	public Cursor getAppGameValue() {
		String table = AppGameSettingTable.TABLENAME;
		return mDBOpenHelper.query(table, null, null, null, null, null, null);
	}

}
