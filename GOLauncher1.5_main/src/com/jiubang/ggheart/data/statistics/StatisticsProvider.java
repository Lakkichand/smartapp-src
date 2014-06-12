package com.jiubang.ggheart.data.statistics;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 统计ContentProvider
 * 
 * @author wangzhuobin
 * 
 */
public class StatisticsProvider extends ContentProvider {

	private static final UriMatcher mUriMatcher;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(StatisticsProviderConstants.AUTHORITY,
				StatisticsProviderConstants.DATA_WITH_ID_PATH,
				StatisticsProviderConstants.DATA_WITH_ID_CASE);
		mUriMatcher.addURI(StatisticsProviderConstants.AUTHORITY,
				StatisticsProviderConstants.DATA_PATH, StatisticsProviderConstants.DATA_CASE);

	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		// TODO Auto-generated method stub
		Cursor cursor = null;
		if (mUriMatcher != null) {
			switch (mUriMatcher.match(uri)) {
			// 如果是带ID的数据查询
				case StatisticsProviderConstants.DATA_WITH_ID_CASE : {
					Long id = ContentUris.parseId(uri);
					if (StatisticsProviderConstants.IMEI_ID == id) {
						// 如果查询ID为imei的ID
						MatrixCursor matrixCursor = new MatrixCursor(new String[] {
								BaseColumns._ID, BaseColumns._COUNT,
								StatisticsProviderConstants._IMEI });
						String imei = Statistics.getVirtualIMEI(getContext());
						matrixCursor.addRow(new Object[] { StatisticsProviderConstants.IMEI_ID,
								new Long(1), imei });
						cursor = matrixCursor;
					}
				}
					break;
				// 如果是不带ID的数据查询
				case StatisticsProviderConstants.DATA_CASE : {

				}
					break;

				default :
					break;
			}
		}
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		String type = null;
		if (mUriMatcher != null) {
			switch (mUriMatcher.match(uri)) {
				case StatisticsProviderConstants.DATA_WITH_ID_CASE : {
					type = StatisticsProviderConstants.CONTENT_ITEM_DATA_TYPE;
				}
					break;
				case StatisticsProviderConstants.DATA_CASE : {
					type = StatisticsProviderConstants.CONTENT_DIR_DATA_TYPE;
				}
					break;
				default : {
					type = StatisticsProviderConstants.CONTENT_ERROR_DATA_TYPE;
				}
					break;
			}
		}
		return type;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
