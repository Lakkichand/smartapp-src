package com.zhidian.wifibox.file.other;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;

public class OtherHelper {
	
final String TAG = OtherHelper.class.getSimpleName();
	
	private Context mContext;
	private ContentResolver cr;
	
	private List<OtherItem> bucketList = new ArrayList<OtherItem>();
	
	/**
	 * 是否创建了图片集
	 */
	boolean hasBuildVideoBucketList = false;
	
	private static OtherHelper mInstance;
	
	public OtherHelper() {}
	
	public static OtherHelper getInstance() {
		if (mInstance == null) {
			mInstance = new OtherHelper();
		}
		return mInstance;
	}
	
	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (this.mContext == null) {
			this.mContext = context;
			cr = mContext.getContentResolver();
		}
	}
	
	/** 获取文件 */
	public List<OtherItem> getOtherBucketList(boolean refresh) {
		if (refresh || (!refresh && !hasBuildVideoBucketList)) {
			buildOtherBucketList();
		}
		
		return bucketList;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void buildOtherBucketList() {
		long startTime = System.currentTimeMillis();
		
		// 版本控制
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if (bucketList != null) {
				bucketList.clear();
			} else {
				bucketList = new ArrayList<OtherItem>();
			}
	
			String[] projection = new String[] { 
					FileColumns._ID, 
					FileColumns.DATA,
					FileColumns.SIZE,
					FileColumns.MIME_TYPE,
					FileColumns.DATE_ADDED,
					FileColumns.DATE_MODIFIED,
					FileColumns.TITLE,
					FileColumns.DISPLAY_NAME,
			};
			String selection = "(" + FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? or " +
								FileColumns.DATA + " like ? ) AND " +
								FileColumns.DATA + " not like ? ";
			String[] selectionArgs = new String[] { 
					"%.dox", "%.docx", "%.docm", "%.dotm",
					"%.xls", "%.xlsx", "%.xlsm", "%.xltm",
					"%.rar", "%.zip.gz", "%.7z", "%.cab", "%.gz", "%.jar", "%.ace", "%.iso", "%.mpq", "%.zip", 
					"%.ppt", "%.pptx", "%.ppsx",
					"%.txt", 
					"%.pdf",
					File.separator + "data" + File.separator + "data" + File.separator + "%"
			};
			String sortOrder = "date_added DESC";
			
			Uri uri = Files.getContentUri("external");
			Cursor cur = null;
			try {
				cur = cr.query(uri, projection, selection, selectionArgs, sortOrder);
				if (cur.moveToFirst()) {
					do {
						int id = cur.getInt(cur.getColumnIndex(FileColumns._ID));
						String data = cur.getString(cur.getColumnIndex(FileColumns.DATA));
						int size = cur.getInt(cur.getColumnIndex(FileColumns.SIZE));
						String mimeType = cur.getString(cur.getColumnIndex(FileColumns.MIME_TYPE));
						int dateAdded = cur.getInt(cur.getColumnIndex(FileColumns.DATE_ADDED));
						int dateModified = cur.getInt(cur.getColumnIndex(FileColumns.DATE_MODIFIED));
						String title = cur.getString(cur.getColumnIndex(FileColumns.TITLE));
						String displayName = cur.getString(cur.getColumnIndex(FileColumns.DISPLAY_NAME));
						
						OtherItem item = new OtherItem();
						item.setId(id);
						item.setData(data);
						item.setSize(size);
						item.setMimeType(mimeType);
						item.setDisplayName(displayName);
						item.setDateAdded(dateAdded);
						item.setDateModified(dateModified);
						item.setTitle(title);
						bucketList.add(item);
						
					} while (cur.moveToNext());
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cur != null) {
					cur.close();
				}
			}
			
			hasBuildVideoBucketList = true;
		} else {
			
		}
		long endTime = System.currentTimeMillis();
		Log.d(TAG, "use time: " + (endTime - startTime) + " ms");

	}
	
	/**
	 * 根据ID删除相关的数据
	 * @param id
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void delete(String id) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			Uri uri = Files.getContentUri("external");
			String where = FileColumns._ID + "=?";
			String[] selectionArgs = new String[] { id };
			try {
				cr.delete(uri, where, selectionArgs);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
