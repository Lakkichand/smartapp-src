package com.zhidian.wifibox.file.video;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Video.Media;
import android.util.Log;

public class VideoHelper {
	
	final String TAG = getClass().getSimpleName();
	
	private Context mContext;
	private ContentResolver cr;
	
	private List<VideoItem> bucketList = new ArrayList<VideoItem>();
	
	/**
	 * 是否创建了图片集
	 */
	boolean hasBuildVideoBucketList = false;
	
	private static VideoHelper mInstance;
	
	private VideoHelper() {}
	
	public static VideoHelper getInstance() {
		if (mInstance == null) {
			mInstance = new VideoHelper();
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
	
	/** 获取视频集 */
	public List<VideoItem> getVideoBucketList(boolean refresh) {
		if (refresh || (!refresh && !hasBuildVideoBucketList)) {
			buildVideoBucketList();
		}
		
		return bucketList;
	}

	private void buildVideoBucketList() {
		long startTime = System.currentTimeMillis();
		
		if (bucketList != null) {
			bucketList.clear();
		} else {
			bucketList = new ArrayList<VideoItem>();
		}
		
		String[] projection = new String[] { 
				Media._ID,
				Media.DATA,
				Media.DISPLAY_NAME,
				Media.SIZE,
				Media.MIME_TYPE,
				Media.DATE_ADDED,
				Media.DATE_MODIFIED,
				Media.TITLE,
				Media.DURATION,
				Media.ARTIST,
				Media.ALBUM,
				Media.RESOLUTION,
				Media.DATE_TAKEN,
				Media.MINI_THUMB_MAGIC,
				Media.BUCKET_ID,
				Media.BUCKET_DISPLAY_NAME
		};
		String selection = "(" + Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? or " +
				Media.DATA + " like ? ) AND " +
				Media.DATA + " not like ? ";
		String[] selectionArgs = new String[] { 
			"%.avi", "%.mov", "%.wmv", "%.asf",
			"%.navi", "%.mp4", "%.3gp", "%.flv",
			"%.rm", "%.m4v", "%.dat", "%.mkv", 
			"%.vob", "%.mpg", "%.mpeg", "%.mpe", "%.asx", "%.rmvb",
			File.separator + "data" + File.separator + "data" + File.separator + "%"
		};
		String sortOrder = "date_added DESC";
		
		Cursor cur = null;
		
		try {
			cur = cr.query(Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
			if (cur.moveToFirst()) {
				do {
					String id = cur.getString(cur.getColumnIndex(Media._ID));
					String data = cur.getString(cur.getColumnIndex(Media.DATA));
					String displayName = cur.getString(cur.getColumnIndex(Media.DISPLAY_NAME));
					String filePath = data.substring(0, ((data.length()) - (displayName.length())));
					int size = cur.getInt(cur.getColumnIndex(Media.SIZE));
					String mimeType = cur.getString(cur.getColumnIndex(Media.MIME_TYPE));
					String dateAdded = cur.getString(cur.getColumnIndex(Media.DATE_ADDED));
					String dateModified = cur.getString(cur.getColumnIndex(Media.DATE_MODIFIED));
					String title = cur.getString(cur.getColumnIndex(Media.TITLE));
					int duration = cur.getInt(cur.getColumnIndex(Media.DURATION));
					String artist = cur.getString(cur.getColumnIndex(Media.ARTIST));
					String album = cur.getString(cur.getColumnIndex(Media.ALBUM));
					String resolution = cur.getString(cur.getColumnIndex(Media.RESOLUTION));
					int datetaken = cur.getInt(cur.getColumnIndex(Media.DATE_TAKEN));
					String miniThumbMagic = cur.getString(cur.getColumnIndex(Media.MINI_THUMB_MAGIC));
					String bucketId = cur.getString(cur.getColumnIndex(Media.BUCKET_ID));
					String bucketDisplayName = cur.getString(cur.getColumnIndex(Media.BUCKET_DISPLAY_NAME));
					
					
					VideoItem item = new VideoItem();
					item.setId(id);
					item.setData(data);
					item.setDisplayName(displayName);
					item.setFilePath(filePath);
					item.setSize(size);
					item.setMimeType(mimeType);
					item.setDateAdded(dateAdded);
					item.setDateModified(dateModified);
					item.setTitle(title);
					item.setDuration(duration);
					item.setArtist(artist);
					item.setAlbum(album);
					item.setResolution(resolution);
					item.setDatetaken(datetaken);
					item.setMiniThumbMagic(miniThumbMagic);
					item.setBucketId(bucketId);
					item.setBucketDisplayName(bucketDisplayName);
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
		long endTime = System.currentTimeMillis();
		Log.d(TAG, "use time: " + (endTime - startTime) + " ms");
		
	}
	
	/**
	 * 根据ID删除相关的数据
	 * @param id
	 */
	public void delete(String id) {
		String where = Media._ID + "=?";
		String[] selectionArgs = new String[] { id };
		try {
			cr.delete(Media.EXTERNAL_CONTENT_URI, where, selectionArgs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
