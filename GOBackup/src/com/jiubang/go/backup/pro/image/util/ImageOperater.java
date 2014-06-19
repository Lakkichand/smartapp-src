package com.jiubang.go.backup.pro.image.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;

import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.util.Util;

/**
 * @author jiangpeihe
 *照片操作类
 */
public class ImageOperater {

	//	final static String WHERECOLOUM = Media.BUCKET_ID+">"+0;
	public static List<File> getImageParentFil(Context context) {
		if (context == null) {
			return null;
		}
		Cursor cursor = null;
		int imageIndex = -1;
		String projectColumns[] = new String[] { Media.DATA, Media._ID };
		try {
			cursor = context.getContentResolver().query(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projectColumns, null, null, null);
			imageIndex = cursor.getColumnIndexOrThrow(Media.DATA);
		} catch (Exception e) {
			e.printStackTrace();
			cursor = null;
		}
		if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst() || imageIndex == -1) {
			return null;
		}
		return getFileList(cursor, imageIndex);

	}

	private static List<File> getFileList(Cursor cursor, int tempImageIndex) {
		int imageIndex = tempImageIndex;
		List<File> parentFileList = new ArrayList<File>();
		String path = "";
		try {
			File file = new File("");
			File parentFile = new File("");
			do {
				path = cursor.getString(imageIndex);
				file = new File(path);
				if (!parentFile.equals(file.getParentFile())
						&& !parentFileList.contains(file.getParentFile())) {
					parentFile = file.getParentFile();
					parentFileList.add(parentFile);
				}
			} while (cursor.moveToNext());
			return parentFileList;
		} catch (Exception e) {
			e.printStackTrace();
			return parentFileList;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}

	//获取缩略图map
	public static Map<Integer, String> getThumbnailsMap(Context context) {
		Map<Integer, String> thumbnailsMap = new HashMap<Integer, String>();
		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID, Thumbnails.DATA };
		try {
			ContentResolver cr = context.getContentResolver();
			Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection, null, null, null);
			if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return null;
			}
			int idColumn = cursor.getColumnIndex(Thumbnails._ID);
			int image_idColumn = cursor.getColumnIndex(Thumbnails.IMAGE_ID);
			int dataColumn = cursor.getColumnIndex(Thumbnails.DATA);
			do {
				int thumbnailId = cursor.getInt(idColumn);
				int imageId = cursor.getInt(image_idColumn);
				String thumbnailPath = cursor.getString(dataColumn);
				//				Log.i("Thumbnails", thumbnailId + " imageId:" + imageId + " thumbnailPath:"
				//						+ thumbnailPath + "---");
				String imageIdAndThumbnailPath = imageId + "#" + thumbnailPath;
				thumbnailsMap.put(imageId, imageIdAndThumbnailPath);
			} while (cursor.moveToNext());
			return thumbnailsMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return thumbnailsMap;
	}

	//	获取图片列表，包括必要的图片信息以及图片缩略图
	public static List<ImageBean> getImageList(Context context) {
		List<ImageBean> imageEntityList = new ArrayList<ImageBean>();
		Cursor cursor = null;
		String columns[] = new String[] { Media.BUCKET_DISPLAY_NAME, Media.DATA, Media._ID,
				Media.DISPLAY_NAME, Media.SIZE };
		try {
			cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, columns, null,
					null, Media.DATE_TAKEN + " DESC");
			if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return null;
			}
			// 获取指定列的索引
			int imagePathIndex = cursor.getColumnIndexOrThrow(Media.DATA);
			int imageDisplayNameIndex = cursor.getColumnIndexOrThrow(Media.DISPLAY_NAME);
			int imageIdIndex = cursor.getColumnIndexOrThrow(Media._ID);
			int imageSizeIndex = cursor.getColumnIndexOrThrow(Media.SIZE);
			do {
				long size = cursor.getInt(imageSizeIndex);
				String imagePath = cursor.getString(imagePathIndex);
				File file = new File(imagePath);
				if (!file.exists()) {
					continue;
				}
				if (size <= 0) {
					continue;
				}
				ImageBean imageBean = new ImageBean();
				int imageId = cursor.getInt(imageIdIndex);
				imageBean.mImageId = imageId;
				imageBean.mImageSize = size;
				String disPlayName = cursor.getString(imageDisplayNameIndex);
				if (TextUtils.isEmpty(disPlayName)) {
					disPlayName = imagePath.substring(imagePath.lastIndexOf(File.separator) + 1);
				}
				imageBean.mImageDisplayName = disPlayName;
				imageBean.mImagePath = imagePath;
				imageBean.mImageParentFilePath = imagePath.substring(0,
						imagePath.lastIndexOf(File.separator));
				if (imageBean != null) {
					imageEntityList.add(imageBean);
				}
			} while (cursor.moveToNext());
			return imageEntityList;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return imageEntityList;
	}
	//	从备份数据库中获取图片列表，包括必要的图片信息以及图片缩略图
	public static List<ImageBean> queryAllImageinfo(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return null;
		}
		Cursor cursor = null;
		List<ImageBean> imageDatas = new ArrayList<ImageBean>();
		try {
			cursor = dbHelper.query(DataTable.TABLE_NAME, null, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_IMAGE, null, null);
			if (cursor == null || cursor.getCount() <= 0 || !cursor.moveToFirst()) {
				return null;
			}
			do {
				try {
					ImageBean image = new ImageBean();
					image.mImageDisplayName = cursor.getString(cursor
							.getColumnIndex(DataTable.DATA1));
					image.mImageSize = Long.parseLong(cursor.getString(cursor
							.getColumnIndex(DataTable.DATA2)));
					//					image.mThumbnailBitmap = Util.byteArrayToBitmap(cursor.getBlob(cursor
					//							.getColumnIndex(DataTable.DATA3)));
					image.mImageParentFilePath = cursor.getString(cursor
							.getColumnIndex(DataTable.DATA5));
					image.mImageShape = cursor.getString(cursor.getColumnIndex(DataTable.DATA6));
					image.mImagePath = cursor.getString(cursor.getColumnIndex(DataTable.DATA7));
					imageDatas.add(image);
				} catch (Exception e) {
				}
			} while (cursor.moveToNext());
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return !Util.isCollectionEmpty(imageDatas) ? imageDatas : null;
	}

	//根据文件夹名获取文件夹里的图片
	public static Map<String, List<ImageBean>> getImageMapByParentFileName(String parentFileName,
			List<ImageBean> imageEntityList) {
		Map<String, List<ImageBean>> imageMapByParentFileName = new HashMap<String, List<ImageBean>>();
		List<ImageBean> imageListByParentFileName = new ArrayList<ImageBean>();
		for (ImageBean imageBean : imageEntityList) {
			if (parentFileName.equals(imageBean.mImageParentFilePath)) {
				imageListByParentFileName.add(imageBean);
			}
		}
		imageMapByParentFileName.put(parentFileName, imageListByParentFileName);
		return imageMapByParentFileName;
	}

	//获得图片总张数
	public static int getAllImageCount(Context context) {
		int count = 0;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
					new String[] { Media._ID }, null, null, null);
			if (cursor != null) {
				count = cursor.getCount();
			}
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return count;
	}

	//获取文件夹以及文件夹中相片的个数
	public static Map<String, Integer> getParentFileMap(Context context) {
		if (context == null) {
			return null;
		}
		Map<String, Integer> imageParentFileMap = new LinkedHashMap<String, Integer>();
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI,
					new String[] { Media.DISPLAY_NAME }, null, null, null);
			if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return null;
			}
			do {
				String imageParentPath = cursor.getString(0);
				if (imageParentFileMap.size() == 0
						|| (imageParentFileMap != null && !imageParentFileMap
								.containsKey(imageParentPath))) {
					imageParentFileMap.put(imageParentPath, 1);
				} else if (imageParentFileMap.containsKey(imageParentPath)) {
					int count = imageParentFileMap.get(imageParentPath) + 1;
					imageParentFileMap.put(imageParentPath, count);
				}
			} while (cursor.moveToNext());
			return imageParentFileMap;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return imageParentFileMap;
	}
	//为了测试先看看
	public static List<ImageBean> getSelectedImageList(Context context) {
		List<ImageBean> imageSelectedList = getImageMapByParentFileName("recommend",
				getImageList(context)).get("recommend");
		return imageSelectedList;
	}

	//获得所有文件夹列表
	public static List<String> getImageFolderPathListFromLocalDB(Context context) {
		Cursor cursor = null;
		String columns[] = new String[] { Media.DATA };
		List<String> imageFolderPathList = new ArrayList<String>();
		try {
			cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, columns, null,
					null, null);
			if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return null;
			}
			// 获取指定列的索引
			do {
				String imagePath = cursor.getString(0);
				File file = new File(imagePath);
				if (!file.exists() && file.length() <= 0) {
					continue;
				}
				String imageFolderPath = imagePath.substring(0,
						imagePath.lastIndexOf(File.separator));
				if (!TextUtils.isEmpty(imageFolderPath)
						&& !imageFolderPathList.contains(imageFolderPath)) {
					imageFolderPathList.add(imageFolderPath);
				}
			} while (cursor.moveToNext());
			//排序
			//			 Collections.sort(imageFolderPathList);
			return imageFolderPathList;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return imageFolderPathList;
	}

	//获得备份数据库所有文件夹列表
	public static List<String> getImageFolderPathListFromBackupTable(BackupDBHelper dbHelper) {
		Cursor cursor = null;
		String columns[] = new String[] { DataTable.DATA5 };
		List<String> imageFolderPathList = new ArrayList<String>();
		try {
			cursor = dbHelper.query(DataTable.TABLE_NAME, columns, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_IMAGE, null, null);
			if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return null;
			}
			// 获取指定列的索引
			do {
				String imageFolderPath = cursor.getString(0);
				//				String imageFolderPath = imagePath.substring(0,
				//						imagePath.lastIndexOf(File.separator));
				if (!TextUtils.isEmpty(imageFolderPath)
						&& !imageFolderPathList.contains(imageFolderPath)) {
					imageFolderPathList.add(imageFolderPath);
				}
			} while (cursor.moveToNext());
			//排序
			//			 Collections.sort(imageFolderPathList);
			return imageFolderPathList;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return imageFolderPathList;
	}

	//根据图片路径取得存在数据库中的缩略图
	public static Bitmap getThumbnailBitmap(BackupDBHelper dbHelper, String imagePath) {
		Cursor cursor = null;
		String columns[] = new String[] { DataTable.DATA3, DataTable.DATA7 };
		Bitmap thumbnailBitmap = null;
		try {
			cursor = dbHelper.query(DataTable.TABLE_NAME, columns, DataTable.MIME_TYPE + "="
					+ MimetypeTable.MIMETYPE_VALUE_IMAGE + " AND " + DataTable.DATA7 + "=?",
					new String[] { imagePath }, null);
			if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return null;
			}
			// 获取指定列的索引
			thumbnailBitmap = Util.byteArrayToBitmap(cursor.getBlob(0));
			return thumbnailBitmap;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return thumbnailBitmap;
	}

	public static Bitmap getScaleBitmap(String path) {
		if (path == null) {
			return null;
		}
		File imageFile = new File(path);
		if (!imageFile.exists()) {
			return null;
		}
		final int sampleSize = 80;
		Bitmap scaleBitmap = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = computeSampleSize(opts, sampleSize, sampleSize);
		opts.inJustDecodeBounds = false;
		Bitmap orignalbitmap = BitmapFactory.decodeFile(path, opts);
		if (orignalbitmap == null) {
			return null;
		}
		int width = orignalbitmap.getWidth();
		int height = orignalbitmap.getHeight();
		int desWid = sampleSize;
		int desHei = sampleSize;
		int startWidth = 0;
		int startHeight = 0;
		if (width > sampleSize && height > sampleSize) {
			desWid = sampleSize;
			desHei = sampleSize;
			startWidth = (width - sampleSize) / 2;
			startHeight = (height - sampleSize) / 2;
		} else if (width > sampleSize) {
			desWid = height;
			desHei = height;
			startWidth = (width - sampleSize) / 2;
			startHeight = 0;
		} else if (height > sampleSize) {
			desHei = width;
			desWid = width;
			startWidth = 0;
			startHeight = (height - sampleSize) / 2;
		} else {
			if (height > width) {
				desHei = width;
				desWid = width;
				startWidth = 0;
				startHeight = (height - width) / 2;
			} else {
				desWid = height;
				desHei = height;
				startWidth = (width - height) / 2;
				startHeight = 0;
			}
		}
		scaleBitmap = Bitmap.createBitmap(orignalbitmap, startWidth, startHeight, desWid, desHei);
		if (scaleBitmap != orignalbitmap) {
			orignalbitmap.recycle();
			orignalbitmap = null;
		}
		return scaleBitmap;
	}

	public static int computeSampleSize(BitmapFactory.Options options, int width, int height) {
		int roundedSize = 1;
		int srcWidth = options.outWidth;
		int srcHeight = options.outHeight;
		int minSize = Math.min(srcWidth, srcHeight);
		if (srcWidth > width || srcHeight > height) {
			roundedSize = minSize / Math.min(width, height);
		}
		return roundedSize;
	}

	//	判断是否有照片
	public static boolean hasImage(Context context) {
		Cursor cursor = null;
		String columns[] = new String[] { Media.DATA };
		try {
			cursor = context.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, columns, null,
					null, null);
			if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
				return false;
			}
			// 获取指定列的索引
			do {
				String imagePath = cursor.getString(0);
				File file = new File(imagePath);
				if (file.exists() && file.length() > 0) {
					return true;
				}
			} while (cursor.moveToNext());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return false;
	}

}
