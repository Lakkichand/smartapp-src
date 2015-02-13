package com.zhidian.wifibox.file.album;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.zhidian.wifibox.util.TimeTool;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.MediaStore.Images.Media;
import android.util.Log;

/**
 * 图片专辑帮助类
 * 
 * @author shihuajian
 *
 */

public class AlbumHelper {
	
	private final String TAG = getClass().getSimpleName();
	private Context context;
	private ContentResolver cr;
	
	Map<String, ImageBucket> bucketList = new TreeMap<String, ImageBucket>();
	private int section = 0;
	private Map<String, Integer> sectionMap = new HashMap<String, Integer>();

	private static AlbumHelper instance;

	private AlbumHelper() {}

	public static AlbumHelper getInstance() {
		if (instance == null) {
			instance = new AlbumHelper();
		}
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (this.context == null) {
			this.context = context;
			cr = context.getContentResolver();
		}
	}

	/**
	 * 是否创建了图片集
	 */
	boolean hasBuildImagesBucketList = false;

	/**
	 * 得到图片集
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void buildImagesBucketList() {
		
		if (bucketList != null) {
			bucketList.clear();
		} else {
			bucketList = new TreeMap<String, ImageBucket>();
		}

		// 构造相册索引
		String columns[] = new String[] { 
				Media._ID,
				Media.DATA,
				Media.DISPLAY_NAME,
				Media.TITLE,
				Media.SIZE,
				Media.BUCKET_DISPLAY_NAME,
				Media.BUCKET_ID,
				Media.PICASA_ID,
				Media.MIME_TYPE,
				Media.DATE_ADDED,
				Media.DATE_MODIFIED,
				Media.DATE_TAKEN
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
				Media.DATA + " like ? ) AND " +
				Media.DATA + " not like ? ";
		String[] selectionArgs = new String[] { 
			"%.jpg", "%.jpeg", "%.gif", "%.png",
			"%.bmp", "%.psd", "%.pcx", "%.tiff",
			"%.dxf", "%.exif", "%.wmf", "%.cdr", 
			"%.eps", "%.emf", "%.pict",
			File.separator + "data" + File.separator + "data" + File.separator + "%"
		};
		// 排序规则
		String sortOrder = " datetaken DESC";
		// 得到一个游标
		Cursor cur = null;
		try {
			cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns, selection, selectionArgs, sortOrder);
			if (cur.moveToFirst()) {
				// 获取指定列的索引
				int photoIDIndex = cur.getColumnIndex(Media._ID);
				int photoPathIndex = cur.getColumnIndex(Media.DATA);
				int photoNameIndex = cur.getColumnIndex(Media.DISPLAY_NAME);
				int photoTitleIndex = cur.getColumnIndex(Media.TITLE);
				int photoSizeIndex = cur.getColumnIndex(Media.SIZE);
				int bucketDisplayNameIndex = cur.getColumnIndex(Media.BUCKET_DISPLAY_NAME);
				int bucketIdIndex = cur.getColumnIndex(Media.BUCKET_ID);
				int picasaIdIndex = cur.getColumnIndex(Media.PICASA_ID);
				int mimeTypeIndex = cur.getColumnIndex(Media.MIME_TYPE);
				int dateAddedIndex = cur.getColumnIndex(Media.DATE_ADDED);
				int dateModifiedIndex = cur.getColumnIndex(Media.DATE_MODIFIED);
				int dateTakenIndex = cur.getColumnIndex(Media.DATE_TAKEN);

				do {
					String _id = cur.getString(photoIDIndex);
					String name = cur.getString(photoNameIndex);
					String path = cur.getString(photoPathIndex);
					String filePath = path.substring(0, ((path.length()) - (name.length())));
					String title = cur.getString(photoTitleIndex);
					int size = cur.getInt(photoSizeIndex);
					String bucketName = cur.getString(bucketDisplayNameIndex);
					String bucketId = cur.getString(bucketIdIndex);
					String picasaId = cur.getString(picasaIdIndex);
					String mimeType = cur.getString(mimeTypeIndex);
					int dateAdded = cur.getInt(dateAddedIndex);
					int dateModified = cur.getInt(dateModifiedIndex);
					String dateTaken = cur.getString(dateTakenIndex);

					Log.i(TAG, _id + ", bucketId: " + bucketId + ", picasaId: "
							+ picasaId + " name:" + name + " path:" + path
							+ " title: " + title + " size: " + size
							+ " bucket: " + bucketName + "---");

					ImageBucket bucket = bucketList.get(bucketId);
					if (bucket == null) {
						bucket = new ImageBucket();
						bucketList.put(bucketId, bucket);
						bucket.imageList = new ArrayList<ImageChildItem>();
						bucket.bucketName = bucketName;
						bucket.bucketPath = filePath;
						bucket.id = bucketId;
					}
					bucket.count++;
					ImageChildItem imageItem = new ImageChildItem();
					imageItem.setImageId(_id);
					imageItem.setDisplayName(name);
					imageItem.setImagePath(path);
					imageItem.setFilePath(filePath);
					imageItem.setTitle(title);
					imageItem.setSize(size);
					imageItem.setBucketDisplayName(bucketName);
					imageItem.setBucketId(bucketId);
					imageItem.setPicasaId(picasaId);
					imageItem.setMimeType(mimeType);
					imageItem.setDateAdded(dateAdded);
					imageItem.setDateModified(dateModified);
					imageItem.setDateTaken(dateTaken);
					bucket.imageList.add(imageItem);

				} while (cur.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		
		hasBuildImagesBucketList = true;
	}
	
	/**
	 * 通过相册ID获取照片
	 * @param bucketId
	 * @return
	 */
	public List<ImageChildItem> getImageItemList(String bucketId, boolean isSelected) {
		section = 0;
		sectionMap.clear();
		List<ImageChildItem> item = new ArrayList<ImageChildItem>();
		
		String[] projection = new String[] { 
				Media._ID,
				Media.DATA,
				Media.DISPLAY_NAME,
				Media.TITLE,
				Media.SIZE,
				Media.BUCKET_DISPLAY_NAME,
				Media.BUCKET_ID,
				Media.PICASA_ID,
				Media.MIME_TYPE,
				Media.DATE_ADDED,
				Media.DATE_MODIFIED,
				Media.DATE_TAKEN
		};
		String selection = Media.BUCKET_ID + "=?";
		String[] selectionArgs = new String[] { bucketId };
		String sortOrder = " datetaken DESC";
		
		Cursor cur = null;
		
		try {
			cur = cr.query(Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
			if (cur.moveToFirst()) {
				// 获取指定列的索引
				int photoIDIndex = cur.getColumnIndex(Media._ID);
				int photoPathIndex = cur.getColumnIndex(Media.DATA);
				int photoNameIndex = cur.getColumnIndex(Media.DISPLAY_NAME);
				int photoTitleIndex = cur.getColumnIndex(Media.TITLE);
				int photoSizeIndex = cur.getColumnIndex(Media.SIZE);
				int bucketDisplayNameIndex = cur.getColumnIndex(Media.BUCKET_DISPLAY_NAME);
				int bucketIdIndex = cur.getColumnIndex(Media.BUCKET_ID);
				int picasaIdIndex = cur.getColumnIndex(Media.PICASA_ID);
				int mimeTypeIndex = cur.getColumnIndex(Media.MIME_TYPE);
				int dateAddedIndex = cur.getColumnIndex(Media.DATE_ADDED);
				int dateModifiedIndex = cur.getColumnIndex(Media.DATE_MODIFIED);
				int dateTakenIndex = cur.getColumnIndex(Media.DATE_TAKEN);
//				// 获取图片总数
//				int totalNum = cur.getCount();

				do {
					String _id = cur.getString(photoIDIndex);
					String name = cur.getString(photoNameIndex);
					String path = cur.getString(photoPathIndex);
					String filePath = path.substring(0, ((path.length()) - (name.length())));
					String title = cur.getString(photoTitleIndex);
					int size = cur.getInt(photoSizeIndex);
					String bucketName = cur.getString(bucketDisplayNameIndex);
					String bucketIds = cur.getString(bucketIdIndex);
					String picasaId = cur.getString(picasaIdIndex);
					String mimeType = cur.getString(mimeTypeIndex);
					int dateAdded = cur.getInt(dateAddedIndex);
					int dateModified = cur.getInt(dateModifiedIndex);
					String dateTaken = cur.getString(dateTakenIndex);

					ImageChildItem imageItem = new ImageChildItem();
					imageItem.setImageId(_id);
					imageItem.setDisplayName(name);
					imageItem.setImagePath(path);
					imageItem.setIsSelected(isSelected);
					imageItem.setFilePath(filePath);
					imageItem.setTitle(title);
					imageItem.setSize(size);
					imageItem.setBucketDisplayName(bucketName);
					imageItem.setBucketId(bucketIds);
					imageItem.setPicasaId(picasaId);
					imageItem.setMimeType(mimeType);
					imageItem.setDateAdded(dateAdded);
					imageItem.setDateModified(dateModified);
					imageItem.setDateTaken(dateTaken);
					item.add(imageItem);

				} while (cur.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		
		// 把信息分组
		for (ListIterator<ImageChildItem> it = item.listIterator(); it.hasNext();) {
			ImageChildItem child = it.next();
			String time = child.getDateTaken() + "";
			time = TimeTool.timestampToString(time);
			if (!sectionMap.containsKey(time)) {
				child.setSection(section);
				sectionMap.put(time, section);
				section++;
			} else {
				child.setSection(sectionMap.get(time));
			}
		}
		
		hasBuildImagesBucketList = true;
		
		return item;
	}

	/**
	 * 得到图片集
	 * 
	 * @param refresh
	 * @return
	 */
	public List<ImageBucket> getImagesBucketList(boolean refresh) {
		if (refresh || (!refresh && !hasBuildImagesBucketList)) {
			buildImagesBucketList();
		}
		List<ImageBucket> tmpList = new ArrayList<ImageBucket>();
		Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<String, ImageBucket> entry = (Map.Entry<String, ImageBucket>) itr.next();
			tmpList.add(entry.getValue());
		}
		return tmpList;
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
