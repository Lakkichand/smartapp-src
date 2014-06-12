package com.go.util.file.media;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.util.Log;

import com.go.util.file.media.FileManageTask.TaskCallBack;

/**
 * 手机文件信息获取引擎
 * @author yangguanxiang
 *
 */
public class FileEngine {
	private static final int MAX_VARIABLE_NUM = 500; //limitation of variable number of sqlite is 999
	// by album
	private ConcurrentHashMap<String, Category> mAudioCategory = null;

	// by path
	private ConcurrentHashMap<String, Category> mVideoCategory = null;

	// by path
	private ConcurrentHashMap<String, Category> mImageCategory = null;

	private ArrayList<FileManageTask> mTaskList = new ArrayList<FileManageTask>();

	private static final int DELAY_SCAN_TIMES = 2000;

	private Context mContext;

	// file type
	public static final int TYPE_ALL = 0;
	public static final int TYPE_AUDIO = 1;
	public static final int TYPE_VIDEO = 2;
	public static final int TYPE_IMAGE = 3;
	public static final int TYPE_DOC = 4;
	public static final int TYPE_ZIP = 5;
	public static final int TYPE_APK = 6;
	public static final int TYPE_CUSTOM = 7;
	public static final int TYPE_OTHER = 8;
	public static final int TYPE_PLAYLIST = 9;

	public FileObserver mFileObserver;
	public Object mLocker = new Object();

	private ContentObserverAudio mAudioObv;
	private ContentObserverImage mImagesObv;
	private ContentObserverVideo mVideoObv;
	private MediaScanReceiver mMediaScanReceiver;

	private ArrayList<String> mDeletedImageList = new ArrayList<String>();
	private ArrayList<String> mDeletedAudioList = new ArrayList<String>();
	private ArrayList<String> mDeletedVideoList = new ArrayList<String>();

	private ArrayList<String> mFilesNotDeleted = null;
	private ArrayList<String> mFilesDeleted = null;

	private boolean mRefreshing;
	/**
	 * 是否正在初始化音乐数据
	 */
	private boolean mInitingAudioData;
	/**
	 * 是否正在初始化图片数据
	 */
	private boolean mInitingImageData;
	/**
	 * 是否正在初始化视频数据
	 */
	private boolean mInitingVideoData;
	private Handler mHandler;

	public FileEngine(Context context) {
		mContext = context;
		initHandler();
		registerReceiver();
	}

	public FileEngine(Context context, FileObserver obv) {
		mContext = context;
		mFileObserver = obv;
		initHandler();
		registerReceiver();
	}

	private void initHandler() {
		Thread thread = new Thread("FileEngine_asyn_thread") {
			@Override
			public void run() {
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
				Looper.prepare();
				mHandler = new Handler();
				Looper.loop();
			}
		};
		thread.start();
	}

	private synchronized void cleanup() {
		if (mAudioCategory != null) {
			mAudioCategory.clear();
		}

		if (mVideoCategory != null) {
			mVideoCategory.clear();
		}

		if (mImageCategory != null) {
			mImageCategory.clear();
		}

		if (null != mTaskList) {
			for (FileManageTask task : mTaskList) {
				task.cancel();
			}
			mTaskList.clear();
		}
		unregisterReceiver();
		mDeletedImageList.clear();
		mDeletedAudioList.clear();
		mDeletedVideoList.clear();
	}

	public void destroy() {
		if (mHandler != null) {
			mHandler.getLooper().quit();
		}
		//避免因为同步机制导致等待过久做成UI线程假死，因此使用异步线程来清除数据
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				cleanup();
			}
		});
		thread.start();
	}

	private void registerReceiver() {
		ContentResolver resolver = mContext.getContentResolver();
		String volumeName = "external";
		Uri uriAudio = Audio.Media.getContentUri(volumeName);
		mAudioObv = new ContentObserverAudio();
		resolver.registerContentObserver(uriAudio, true, mAudioObv);

		Uri uriImages = Images.Media.getContentUri(volumeName);
		mImagesObv = new ContentObserverImage();
		resolver.registerContentObserver(uriImages, true, mImagesObv);

		Uri uriVideo = Video.Media.getContentUri(volumeName);
		mVideoObv = new ContentObserverVideo();
		resolver.registerContentObserver(uriVideo, true, mVideoObv);

		mMediaScanReceiver = new MediaScanReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file");
		mContext.registerReceiver(mMediaScanReceiver, filter);
	}

	/**
	 * 图片数据监听者
	 * @author yangguanxiang
	 *
	 */
	private class ContentObserverImage extends ContentObserver {

		public ContentObserverImage() {
			super(null);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (!mInitingImageData && !mRefreshing && mHandler != null) {
				// Log.d("onChange", "ContentObserverImage " + selfChange);
				mHandler.removeCallbacks(mCompareImagesRunnable);
				mHandler.postDelayed(mCompareImagesRunnable, DELAY_SCAN_TIMES);
			}
		}
	}

	/**
	 * 音乐数据监听者
	 * @author yangguanxiang
	 *
	 */
	private class ContentObserverAudio extends ContentObserver {

		public ContentObserverAudio() {
			super(null);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (!mInitingAudioData && !mRefreshing && mHandler != null) {
				// Log.d("onChange", "ContentObserverAudio " + selfChange);
				mHandler.removeCallbacks(mCompareAudioRunnable);
				mHandler.postDelayed(mCompareAudioRunnable, DELAY_SCAN_TIMES);
			}
		}
	}

	/**
	 * 视频数据监听者
	 * @author yangguanxiang
	 *
	 */
	private class ContentObserverVideo extends ContentObserver {

		public ContentObserverVideo() {
			super(null);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (!mInitingVideoData && !mRefreshing && mHandler != null) {
				mHandler.removeCallbacks(mCompareVideoRunnable);
				mHandler.postDelayed(mCompareVideoRunnable, DELAY_SCAN_TIMES);
				// Log.d("onChange", "ContentObserverVideo " + selfChange);
			}
		}
	}

	private Runnable mCompareAudioRunnable = new Runnable() {
		@Override
		synchronized public void run() {
			// Log.d("compares", "mCompareAudioRunnable");
			//Duration.setStart(FileEngine.class.getName());
			compareAudioFiles();
			//Duration.setEnd(FileEngine.class.getName());
			// Log.d("compares", "compares cost: " + Duration.getDuration());
		}
	};

	private Runnable mCompareVideoRunnable = new Runnable() {
		@Override
		synchronized public void run() {
			// Log.d("compares", "mCompareVideoRunnable");
			//Duration.setStart(FileEngine.class.getName());
			compareVideoFiles();
			//Duration.setEnd(FileEngine.class.getName());
			// Log.d("compares", "compares cost: " + Duration.getDuration());
		}
	};

	private Runnable mCompareImagesRunnable = new Runnable() {
		@Override
		synchronized public void run() {
			// Log.d("compares", "compareImagesFiles");
			//Duration.setStart(FileEngine.class.getName());
			compareImagesFiles();
			//Duration.setEnd(FileEngine.class.getName());
			// Log.d("compares", "compares cost: " + Duration.getDuration());
		}
	};

	synchronized private void compareAudioFiles() {
//		Log.i("Test", "----------compareAudioFiles");
		if (null == mAudioCategory) {
			return;
		}
		String volumeName = "external";
		Uri uri = Audio.Media.getContentUri(volumeName);

		String[] columns = new String[] { BaseColumns._ID, MediaColumns.DATA,
				MediaColumns.DISPLAY_NAME, MediaColumns.SIZE, MediaColumns.DATE_ADDED,
				MediaColumns.DATE_MODIFIED, AudioColumns.DURATION, AudioColumns.ARTIST,
				AudioColumns.ALBUM, AudioColumns.ALBUM_ID, MediaColumns.MIME_TYPE,
				MediaColumns.TITLE };
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns,
					buildSelectionByCategory(TYPE_AUDIO), null, null);
			if (cur == null) {
				return;
			}

			if (!cur.moveToFirst()) {
				return;
			}
			compareFile(cur, mAudioCategory, mDeletedAudioList, TYPE_AUDIO);
		} catch (SQLiteException ex) {
			ex.printStackTrace();
		}
		finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	synchronized private void compareVideoFiles() {
//		Log.i("Test", "----------compareVideoFiles");
		if (null == mVideoCategory) {
			return;
		}

		String volumeName = "external";
		Uri uri = Video.Media.getContentUri(volumeName);

		String[] columns = new String[] { BaseColumns._ID, MediaColumns.DATA,
				MediaColumns.DISPLAY_NAME, MediaColumns.SIZE, MediaColumns.DATE_ADDED,
				MediaColumns.DATE_MODIFIED, VideoColumns.DURATION, MediaColumns.MIME_TYPE };
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns,
					buildSelectionByCategory(TYPE_VIDEO), null, null);

			if (cur == null) {
				return;
			}

			if (!cur.moveToFirst()) {
				return;
			}
			compareFile(cur, mVideoCategory, mDeletedVideoList, TYPE_VIDEO);
		} catch (SQLiteException ex) {
			ex.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	synchronized private void compareImagesFiles() {
//		Log.i("Test", "----------compareImagesFiles");
		if (null == mImageCategory) {
			return;
		}

		String volumeName = "external";
		Uri uri = Images.Media.getContentUri(volumeName);
		String[] columns = new String[] { BaseColumns._ID, MediaColumns.DATA,
				MediaColumns.DISPLAY_NAME, MediaColumns.SIZE, MediaColumns.DATE_ADDED,
				MediaColumns.DATE_MODIFIED, ImageColumns.BUCKET_DISPLAY_NAME,
				MediaColumns.MIME_TYPE };
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns,
					buildSelectionByCategory(TYPE_IMAGE), null, null);
			if (cur == null) {
				return;
			}

			if (!cur.moveToFirst()) {
				return;
			}

			compareFile(cur, mImageCategory, mDeletedImageList, TYPE_IMAGE);
		} catch (SQLiteException ex) {
			ex.printStackTrace();
		} finally {
			if (cur != null) {
				cur.close();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private void compareFile(Cursor cur, ConcurrentHashMap<String, Category> categoryMap,
			ArrayList<String> deletedFileList, int type) {
		ArrayList<FileInfo> filesRemoved = new ArrayList<FileInfo>();
		ArrayList<FileInfo> filesAdded = new ArrayList<FileInfo>();

		// 把缓存中的拷贝一份到tmp中，方便比对
		HashMap<String, Category> tmp = new HashMap<String, Category>();

		Iterator<Category> values = categoryMap.values().iterator();
		while (values.hasNext()) {
			Category cat = values.next();
			Category newCat = new Category();

			newCat.filePath = cat.filePath;
			newCat.files = (ArrayList<FileInfo>) cat.files.clone();
			newCat.size = cat.size;

			tmp.put(cat.filePath, newCat);
		}
		HashSet<String> filesInDB = new HashSet<String>();
		do {
			FileInfo file = extractFileInfo(cur, type);
			if (null == file) {
				continue;
			}

			// 查找是否有这个文件夹
			if (tmp.containsKey(getCategoryKey(file, type))) {
				Category category = tmp.get(getCategoryKey(file, type));
				int size = category.files.size();
				boolean bFind = false;
				// 找到这个文件则从tmp中删除，说明原来已经有了
				for (int i = size - 1; i >= 0; --i) {
					FileInfo info = category.files.get(i);
					if (0 == info.fullFilePath.compareTo(file.fullFilePath)) {
						bFind = true;
						category.files.remove(i);
						break;
					}
				}
				if (!bFind) {
					// 没有找到则加入缓存
					if (!deletedFileList.contains(file.fullFilePath)) {
						if (categoryMap.get(getCategoryKey(file, type)).addFile(file)) {
							filesAdded.add(file);
							// Log.d("onChange", "add " + file.fullFilePath);
						}
					}
				}
			} else {
				if (categoryMap.containsKey(getCategoryKey(file, type))) {
					if (!deletedFileList.contains(file.fullFilePath)) {
						if (categoryMap.get(getCategoryKey(file, type)).addFile(file)) {
							filesAdded.add(file);
							// Log.d("onChange", "add " + file.fullFilePath);
						}
					}
				} else {
					if (!deletedFileList.contains(file.fullFilePath)) {
						// 没有这个文件夹则新建一个文件夹，加入缓存
						// Log.d("onChange", "add category " + file.filePath);
						// Log.d("onChange", "add " + file.fullFilePath);
						Category category = makeCategory(file, type);
						filesAdded.add(file);
						categoryMap.put(category.filePath, category);
					}
				}

			}
			filesInDB.add(file.fullFilePath);
		} while (cur.moveToNext());
		if (mFileObserver != null && filesAdded != null && !filesAdded.isEmpty()) {
			mFileObserver.filesAdded(filesAdded, type);
		}
		// tmp中剩下的则是被删除的文件
		Collection<Category> categories = tmp.values();
		Object[] ar = categories.toArray();
		int size = ar.length;
		for (int i = 0; i < size; ++i) {
			Object category = ar[i];
			filesRemoved.addAll(((Category) category).files);
		}

		// 把删除的文件从缓存中删除
		for (FileInfo info : filesRemoved) {
			Category category = categoryMap.get(getCategoryKey(info, type));
			if (null != category) {
				// Log.d("onChange", "delete file " + info.fullFilePath);
				category.deleteFile(info.fullFilePath);
				if (category.files.isEmpty()) {
					categoryMap.remove(category.filePath);
				}
			}
		}

		Iterator<String> it = deletedFileList.iterator();
		while (it.hasNext()) {
			if (!filesInDB.contains(it.next())) {
				it.remove();
			}
		}

		if (mFileObserver != null && filesRemoved != null && !filesRemoved.isEmpty()) {
			mFileObserver.filesRemoved(filesRemoved, type);
		}
	}

	private Category makeCategory(FileInfo file, int type) {
		Category category = new Category();
		switch (type) {
			case TYPE_IMAGE :
				category.filePath = file.filePath;
				category.alias = ((ImageFile) file).bucketName;
				break;
			case TYPE_AUDIO :
				category.alias = ((AudioFile) file).album;
				category.filePath = ((AudioFile) file).album;
				break;
			case TYPE_VIDEO :
				category.filePath = file.filePath;
				break;
		}
		category.uri = category.filePath;
		category.init();
		category.addFile(file);
		return category;
	}

	private FileInfo extractFileInfo(Cursor cur, int type) {
		FileInfo fileInfo = null;
		switch (type) {
			case TYPE_IMAGE :
				fileInfo = ImageFile.getInfo(cur);
				break;
			case TYPE_AUDIO :
				fileInfo = AudioFile.getInfo(cur);
				break;
			case TYPE_VIDEO :
				fileInfo = VideoFile.getInfo(cur);
				break;
		}
		return fileInfo;
	}

	private String getCategoryKey(FileInfo fileInfo, int type) {
		String key = null;
		switch (type) {
			case TYPE_IMAGE :
				key = fileInfo.filePath;
				break;
			case TYPE_AUDIO :
				key = ((AudioFile) fileInfo).album;
				break;
			case TYPE_VIDEO :
				key = fileInfo.filePath;
				break;
		}
		return key;
	}

	private void unregisterReceiver() {
		ContentResolver resolver = mContext.getContentResolver();
		resolver.unregisterContentObserver(mAudioObv);
		resolver.unregisterContentObserver(mImagesObv);
		resolver.unregisterContentObserver(mVideoObv);
		mContext.unregisterReceiver(mMediaScanReceiver);
	}

	/**
	 * 获取所有专辑
	 * @return
	 */
	public synchronized ArrayList<Category> getAlbums() {
		ArrayList<Category> albums = new ArrayList<Category>();
		if (mAudioCategory != null) {
			Collection<Category> collection = mAudioCategory.values();
			if (collection != null && !collection.isEmpty()) {
				try {
					albums.addAll(collection);
				} catch (NoSuchElementException ex) {
					ex.printStackTrace();
				}
			}
		}
		return albums;
	}

	/**
	 * 获取所有指定专辑的音乐
	 * @param album
	 * @return
	 */
	public synchronized Category getAudioByAlbum(String album) {
		if (mAudioCategory != null) {
			return mAudioCategory.get(album);
		} else {
			return null;
		}
	}

	/**
	 * 获取所有音乐
	 * @return
	 */
	public ArrayList<FileInfo> getAllAudio() {
		ArrayList<Category> categories = getAlbums();
		if (categories != null) {
			int size = categories.size();

			ArrayList<FileInfo> files = new ArrayList<FileInfo>();
			for (int i = 0; i < size; ++i) {
				Category category = categories.get(i);
				files.addAll(category.files);
			}
			return files;
		} else {
			return null;
		}
	}

	/**
	 * 获取所有图片文件夹
	 * @return
	 */
	public synchronized ArrayList<Category> getImagePaths() {
		ArrayList<Category> paths = new ArrayList<Category>();
		if (mImageCategory != null) {
			Collection<Category> collection = mImageCategory.values();
			if (collection != null && !collection.isEmpty()) {
				try {
					paths.addAll(collection);
				} catch (NoSuchElementException ex) {
					ex.printStackTrace();
				}
			}
		}
		return paths;
	}

	/**
	 * 获取指定文件夹下的图片
	 * @param path
	 * @return
	 */
	public synchronized Category getImageByPath(String path) {
		if (mImageCategory != null) {
			return mImageCategory.get(path);
		} else {
			return null;
		}
	}

	/**
	 * 获取全部图片
	 * @return
	 */
	public ArrayList<FileInfo> getAllImage() {
		ArrayList<Category> categories = getImagePaths();
		if (categories != null) {
			int size = categories.size();
			ArrayList<FileInfo> files = new ArrayList<FileInfo>();
			for (int i = 0; i < size; ++i) {
				Category category = categories.get(i);
				files.addAll(category.files);
			}
			return files;
		}
		return null;
	}

	/**
	 * 获取所有视频文件夹
	 * @return
	 */
	public synchronized ArrayList<Category> getVideoPaths() {
		ArrayList<Category> paths = new ArrayList<Category>();
		if (mVideoCategory != null) {
			Collection<Category> collection = mVideoCategory.values();
			if (collection != null && !collection.isEmpty()) {
				try {
					paths.addAll(collection);
				} catch (NoSuchElementException ex) {
					ex.printStackTrace();
				}
			}
		}
		return paths;
	}

	/**
	 * 获取所有视频
	 * @return
	 */
	public ArrayList<FileInfo> getAllVideo() {
		ArrayList<Category> categories = getVideoPaths();
		if (categories != null) {
			int size = categories.size();
			ArrayList<FileInfo> files = new ArrayList<FileInfo>();
			for (int i = 0; i < size; ++i) {
				Category category = categories.get(i);
				files.addAll(category.files);
			}
			return files;
		}
		return null;
	}

	/**
	 * 获取指定文件夹下的所有视频
	 * @param path
	 * @return
	 */
	public synchronized Category getVideoByPath(String path) {
		// if (null == videoCategory)
		// refreshVideo();
		if (mVideoCategory != null) {
			return mVideoCategory.get(path);
		} else {
			return null;
		}
	}

	private String buildDocSelection() {
		StringBuilder selection = new StringBuilder();
		Iterator<String> iter = MediaFileUtil.sDocMimeTypesSet.iterator();
		while (iter.hasNext()) {
			selection.append("(" + "mime_type" + "=='" + iter.next() + "') OR ");
		}
		return selection.substring(0, selection.lastIndexOf(")") + 1);
	}

	private String buildSelectionByCategory(int cat) {
		String selection = null;
		switch (cat) {
			case TYPE_DOC :
				selection = buildDocSelection();
				break;
			case TYPE_ZIP :
				selection = "(" + "mime_type" + " == '" + MediaFileUtil.sZipFileMimeType + "')";
				break;
			case TYPE_APK :
				selection = "_data" + " LIKE '%.apk'";
				break;
			default :
				selection = null;
		}
		return selection;
	}

	private Runnable mInitAudioRunnable = new Runnable() {

		@Override
		public void run() {
			initAudio();
		}

	};

	private synchronized void refreshAudio() {
		if (!checkIfAsyncTaskRunning(mInitAudioRunnable) && mAudioCategory == null) {
			setInitingData(TYPE_AUDIO, true);
			asyncExecute(TYPE_AUDIO, mInitAudioRunnable, mInitDataTaskCallback);
		}
	}

	private Runnable mInitImageRunnable = new Runnable() {

		@Override
		public void run() {
			initImage();
		}
	};

	private synchronized void refreshImage() {
		if (!checkIfAsyncTaskRunning(mInitImageRunnable) && mImageCategory == null) {
			setInitingData(TYPE_IMAGE, true);
			asyncExecute(TYPE_IMAGE, mInitImageRunnable, mInitDataTaskCallback);
		}
	}

	private Runnable mInitVideoRunnable = new Runnable() {

		@Override
		public void run() {
			initVideo();
		}

	};

	private synchronized void refreshVideo() {
		if (!checkIfAsyncTaskRunning(mInitVideoRunnable) && mVideoCategory == null) {
			setInitingData(TYPE_VIDEO, true);
			asyncExecute(TYPE_VIDEO, mInitVideoRunnable, mInitDataTaskCallback);
		}
	}

	/**
	 * 获取视频缩略图
	 * @param dbId
	 * @return
	 */
	public MediaThumbnail getVideoThumbnail(int dbId) {
		String volumeName = "external";
		String selection = Video.Thumbnails.VIDEO_ID + "='" + dbId + "'";
		String[] columns = new String[] { Video.Thumbnails.DATA, Video.Thumbnails.WIDTH,
				Video.Thumbnails.HEIGHT };
		Uri uri = Video.Thumbnails.getContentUri(volumeName);
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns, selection, null, null);
			if (null == cur) {
				return null;
			}

			if (!cur.moveToFirst()) {
				return null;
			}

			MediaThumbnail thumbnail = new MediaThumbnail();
			thumbnail.path = MediaDbUtil.getString(cur, Video.Thumbnails.DATA);
			thumbnail.width = MediaDbUtil.getInt(cur, Video.Thumbnails.WIDTH);
			thumbnail.height = MediaDbUtil.getInt(cur, Video.Thumbnails.HEIGHT);
			thumbnail.dbId = dbId;
			thumbnail.type = MediaThumbnail.TYPE_VIDEO;
			return thumbnail;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	/**
	 * 获取图片缩略图
	 * @param dbId
	 * @return
	 */
	public MediaThumbnail getImageThumbnail(int dbId) {
		String volumeName = "external";
		String selection = Images.Thumbnails.IMAGE_ID + "='" + dbId + "'";
		String[] columns = new String[] { Images.Thumbnails.DATA, Images.Thumbnails.WIDTH,
				Images.Thumbnails.HEIGHT };
		Uri uri = Images.Thumbnails.getContentUri(volumeName);
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns, selection, null, null);
			if (null == cur) {
				return null;
			}

			if (!cur.moveToFirst()) {
				return null;
			}

			MediaThumbnail thumbnail = new MediaThumbnail();
			thumbnail.path = MediaDbUtil.getString(cur, Images.Thumbnails.DATA);
			thumbnail.width = MediaDbUtil.getInt(cur, Images.Thumbnails.WIDTH);
			thumbnail.height = MediaDbUtil.getInt(cur, Images.Thumbnails.HEIGHT);
			thumbnail.dbId = dbId;
			thumbnail.type = MediaThumbnail.TYPE_IMAGE;
			return thumbnail;
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	/**
	 * 删除文件
	 * @param path
	 * @param type
	 */
	public void deleteFile(String path, int type) {
		File file = new File(path);
		boolean ret = false;
		if (file.exists()) {
			ret = file.delete();
		} else {
			ret = true;
		}

		if (ret) {
			Category category = null;
			String key = MediaFileUtil.getPathFromFilepath(path);
			if (type == TYPE_AUDIO) {
				category = mAudioCategory.get(key);
				mDeletedAudioList.add(path);
			}

			if (type == TYPE_IMAGE) {
				category = mImageCategory.get(key);
				mDeletedImageList.add(path);
			}

			if (type == TYPE_VIDEO) {
				category = mVideoCategory.get(key);
				mDeletedVideoList.add(path);
			}

			if (null != category) {
				category.deleteFile(path);
			}

			removeDataFromDB(path, type);
		}

		mFileObserver.deleteFinished(path, ret);
	}

	private synchronized void asyncExecute(int type, final Runnable runnable, final TaskCallBack callback) {
		if (mTaskList.size() >= 10) {
			FileManageTask task = mTaskList.remove(0);
			task.cancel();
		}
		FileManageTask task = new FileManageTask(type, runnable, callback);
		mTaskList.add(task);
		task.execute();
	}

	/**
	 * 批量删除文件
	 * @param files
	 * @param type
	 */
	public void deleteFiles(final List<String> files, final int type) {
		if (null == mFilesNotDeleted) {
			mFilesNotDeleted = new ArrayList<String>();
		}
		if (null == mFilesDeleted) {
			mFilesDeleted = new ArrayList<String>();
		}
		mFilesNotDeleted.clear();
		mFilesDeleted.clear();
		asyncExecute(type, new Runnable() {
			@Override
			public void run() {
				synchronized (mFilesDeleted) {
					int size = files.size();
					ArrayList<String> pathList = new ArrayList<String>(size);
					for (int i = 0; i < size; ++i) {
						String path = files.get(i);
						File file = new File(path);
						if (file.exists() && !file.delete()) {
							mFilesNotDeleted.add(path);
						} else {
							mFilesDeleted.add(path);
							Category category = null;
							String key = MediaFileUtil.getPathFromFilepath(path);
							if (type == TYPE_AUDIO) {
								category = mAudioCategory.get(key);
								mDeletedAudioList.add(path);
							}

							if (type == TYPE_IMAGE) {
								category = mImageCategory.get(key);
								mDeletedImageList.add(path);
							}

							if (type == TYPE_VIDEO) {
								category = mVideoCategory.get(key);
								mDeletedVideoList.add(path);
							}

							if (null != category) {
								category.deleteFile(path);
							}
							pathList.add(path);
							//							removeDataFromDB(path, type);
						}
					}
					removeDataFromDB(pathList, type);
				}
			}
		}, mFileDeleteTaskCallback);
	}

	/**
	 * 批量删除音乐文件
	 * @param files
	 * @param album
	 */
	public void deleteAudioFiles(final List<String> files, final String album) {
		if (null == mFilesNotDeleted) {
			mFilesNotDeleted = new ArrayList<String>();
		}
		if (null == mFilesDeleted) {
			mFilesDeleted = new ArrayList<String>();
		}
		mFilesNotDeleted.clear();
		mFilesDeleted.clear();
		asyncExecute(TYPE_AUDIO, new Runnable() {
			@Override
			public void run() {
				synchronized (mFilesDeleted) {
					int size = files.size();
					ArrayList<String> pathList = new ArrayList<String>(size);
					for (int i = 0; i < size; ++i) {
						String path = files.get(i);
						File file = new File(path);
						if (file.exists() && !file.delete()) {
							mFilesNotDeleted.add(path);
						} else {
							mFilesDeleted.add(path);
							Category category = null;
							category = mAudioCategory.get(album);

							if (null != category) {
								category.deleteFile(path);
							}
							mDeletedAudioList.add(path);
							pathList.add(path);
							//							removeDataFromDB(path, TYPE_AUDIO);
						}
					}
					removeDataFromDB(pathList, TYPE_AUDIO);
				}
			}
		}, mFileDeleteTaskCallback);
	}

	/**
	 * 批量删除音乐文件
	 * @param fileMap
	 */
	public void deleteAudioFiles(final Map<String, ArrayList<String>> fileMap) {
		if (null == mFilesNotDeleted) {
			mFilesNotDeleted = new ArrayList<String>();
		}
		if (null == mFilesDeleted) {
			mFilesDeleted = new ArrayList<String>();
		}
		mFilesNotDeleted.clear();
		mFilesDeleted.clear();
		asyncExecute(TYPE_AUDIO, new Runnable() {
			@Override
			public void run() {
				synchronized (mFilesDeleted) {
					ArrayList<String> pathList = new ArrayList<String>();
					Set<Entry<String, ArrayList<String>>> set = fileMap.entrySet();
					for (Entry<String, ArrayList<String>> entry : set) {
						String album = entry.getKey();
						ArrayList<String> files = entry.getValue();
						for (String path : files) {
							File file = new File(path);
							if (file.exists() && !file.delete()) {
								mFilesNotDeleted.add(path);
							} else {
								mFilesDeleted.add(path);
								Category category = null;
								category = mAudioCategory.get(album);

								if (null != category) {
									category.deleteFile(path);
								}
								mDeletedAudioList.add(path);
								//								removeDataFromDB(path, TYPE_AUDIO);
								pathList.add(path);
							}
						}
					}
					removeDataFromDB(pathList, TYPE_AUDIO);
				}
			}
		}, mFileDeleteTaskCallback);
	}

	private void removeDataFromDB(String path, int type) {
		if (path == null || path.trim().length() == 0) {
			return;
		}
		Uri uri = null;
		String where = null;
		switch (type) {
			case TYPE_IMAGE :
				uri = Images.Media.EXTERNAL_CONTENT_URI;
				break;
			case TYPE_AUDIO :
				uri = Audio.Media.EXTERNAL_CONTENT_URI;
				break;
			case TYPE_VIDEO :
				uri = Video.Media.EXTERNAL_CONTENT_URI;
				break;
		}
		where = MediaColumns.DATA + "=?";
		if (uri != null && where != null) {
			mContext.getContentResolver().delete(uri, where, new String[] { path });
		}
	}

	private void removeDataFromDB(ArrayList<String> pathList, int type) {
		if (pathList == null || pathList.isEmpty()) {
			return;
		}
		Uri uri = null;
		switch (type) {
			case TYPE_IMAGE :
				uri = Images.Media.EXTERNAL_CONTENT_URI;
				break;
			case TYPE_AUDIO :
				uri = Audio.Media.EXTERNAL_CONTENT_URI;
				break;
			case TYPE_VIDEO :
				uri = Video.Media.EXTERNAL_CONTENT_URI;
				break;
		}
		int pathSize = pathList.size();
		if (pathSize > MAX_VARIABLE_NUM) {
			int count = pathSize % MAX_VARIABLE_NUM == 0 ? pathSize / MAX_VARIABLE_NUM : pathSize
					/ MAX_VARIABLE_NUM + 1;
			int start = 0;
			for (int i = 0; i < count; i++) {
				int end = start + MAX_VARIABLE_NUM;
				if (end > pathSize) {
					end = pathSize;
				}
				ArrayList<String> subPathList = new ArrayList<String>(end - start);
				for (int j = start; j < end; j++) {
					subPathList.add(pathList.get(j));
				}
				removeData(uri, subPathList);
				start = end;
			}
		} else {
			removeData(uri, pathList);
		}

	}

	private void removeData(Uri uri, ArrayList<String> pathList) {
		if (uri == null || pathList.isEmpty()) {
			return;
		}
		String where = null;
		String[] args = new String[pathList.size()];
		StringBuilder whereBuf = new StringBuilder();
		whereBuf.append(MediaColumns.DATA).append(" in (");
		int i = 0;
		for (String path : pathList) {
			whereBuf.append("?,");
			args[i++] = path;
		}
		whereBuf.delete(whereBuf.lastIndexOf(","), whereBuf.length()).append(")");
		where = whereBuf.toString();
//		Log.i("Test", "--------where: " + where);
//		for (int j = 0; j < args.length; j++) {
//			Log.i("Test", "--------args[" + j + "]: " + args[j]);
//		}
		mContext.getContentResolver().delete(uri, where, args);
	}

	/**
	 * 文件状态观察者
	 * @author yangguanxiang
	 *
	 */
	public interface FileObserver {
		/**
		 * @param ret
		 *            ：是否删除成功
		 */
		public void deleteFinished(String path, boolean ret);

		/**
		 * @param path
		 *            :删掉成功的文件列表
		 * @param path
		 *            :删掉失败的文件列表
		 * @param ret
		 *            ：是否成功
		 */
		public void deleteFinished(ArrayList<String> filesDeleted, ArrayList<String> filesNotDeleted);

		/**
		 * @param info:增加的文件
		 * 
		 * @param type :文件类型
		 */
		public void filesAdded(ArrayList<FileInfo> infos, int type);

		/**
		 * @param infos:删除的文件集合
		 * 
		 * @params type：删除文件的类型
		 */
		public void filesRemoved(ArrayList<FileInfo> infos, int type);

		/**
		 * @param refreshing:告诉监听者，后台正在刷新
		 */
		public void setRefreshing(boolean refreshing);

		/**
		 * 通知监听者刷新页面
		 */
		public void notifyDataChanged();
	}

	/**
	 * 获取专辑名称
	 * 
	 * @param dbId
	 * @return
	 */
	public String getAlbumBucket(int dbId) {
		String volumeName = "external";
		String selection = BaseColumns._ID + "='" + dbId + "'";
		String[] columns = new String[] { AudioColumns.ALBUM };
		Uri uri = Audio.Media.getContentUri(volumeName);
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns, selection, null, null);
			if (null == cur) {
				return null;
			}

			if (!cur.moveToFirst()) {
				return null;
			}
			return MediaDbUtil.getString(cur, AudioColumns.ALBUM);
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	/**
	 * 获取文件的MimeType
	 * 
	 * @param dbId
	 * @param type
	 * @return
	 */
	public String getMimeType(int dbId, int type) {
		String volumeName = "external";
		String selection = BaseColumns._ID + "='" + dbId + "'";
		String[] columns = new String[] { MediaColumns.MIME_TYPE };
		Uri uri = null;
		switch (type) {
			case TYPE_IMAGE :
				uri = Images.Media.getContentUri(volumeName);
				break;
			case TYPE_AUDIO :
				uri = Audio.Media.getContentUri(volumeName);
				break;
			case TYPE_VIDEO :
				uri = Video.Media.getContentUri(volumeName);
				break;
			default :
				break;
		}
		if (uri == null) {
			return null;
		}
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns, selection, null, null);
			if (null == cur) {
				return null;
			}

			if (!cur.moveToFirst()) {
				return null;
			}
			return MediaDbUtil.getString(cur, MediaColumns.MIME_TYPE);
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	/**
	 * 设置文件状态监听者
	 * @param observer
	 */
	public void setFileObserver(FileObserver observer) {
		mFileObserver = observer;
	}

	private void showProgressDialog() {
//		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
//				AppFuncConstants.PROGRESSBAR_SHOW, null);
	}

	private void dismissProgressDialog() {
//		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
//				AppFuncConstants.PROGRESSBAR_HIDE, null);
	}

	/**
	 * 扫描SD卡
	 */
	public void scanSDCard() {
		setRefreshing(true);
		Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
		MediaStore.getMediaScannerUri();
		intent.setClassName("com.android.providers.media",
				"com.android.providers.media.MediaScannerReceiver");
		intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
		mContext.sendBroadcast(intent);
	}

	/**
	 * 多媒体扫描广播接收者
	 * @author yangguanxiang
	 *
	 */
	private class MediaScanReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mRefreshing) {
				String action = intent.getAction();
				if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
					showProgressDialog();
				} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
					compareImagesFiles();
					compareAudioFiles();
					compareVideoFiles();
					dismissProgressDialog();
					setRefreshing(false);
				}
			}
		}
	}

	private void setRefreshing(boolean refreshing) {
		mRefreshing = refreshing;
		if (mFileObserver != null) {
			mFileObserver.setRefreshing(mRefreshing);
		}
	}

	public void setInitingData(int type, boolean initing) {
		if (type == TYPE_AUDIO) {
			mInitingAudioData = initing;
		} else if (type == TYPE_IMAGE) {
			mInitingImageData = initing;
		} else {
			mInitingVideoData = initing;
		}
		if (mFileObserver != null) {
			if (!initing) {
				mFileObserver.notifyDataChanged();
			}
		}
	}

	private TaskCallBack mFileDeleteTaskCallback = new TaskCallBack() {

		@Override
		public void onPreExecute(FileManageTask task) {
			showProgressDialog();
		}

		@Override
		public Object doInBackground(FileManageTask task) {
			return null;
		}

		@Override
		public void onPostExecute(FileManageTask task) {
			if (mFileObserver != null) {
				mFileObserver.deleteFinished(mFilesDeleted, mFilesNotDeleted);
			}
			dismissProgressDialog();
			if (mTaskList != null) {
				mTaskList.remove(task);
			}
		}
	};

	private TaskCallBack mInitDataTaskCallback = new TaskCallBack() {

		@Override
		public void onPreExecute(FileManageTask task) {
			showProgressDialog();
		}

		@Override
		public Object doInBackground(FileManageTask task) {
			return null;
		}

		@Override
		public void onPostExecute(FileManageTask task) {
			setInitingData(task.mType, false);
			dismissProgressDialog();
			mTaskList.remove(task);
		}
	};

	/**
	 * 多媒体数据是否已经初始化
	 * @param type
	 * @return
	 */
	public boolean isMediaDataInited(int type) {
		switch (type) {
			case TYPE_IMAGE :
				return !mInitingImageData && mImageCategory != null;
			case TYPE_AUDIO :
				return !mInitingAudioData && mAudioCategory != null;
			case TYPE_VIDEO :
				return !mInitingVideoData && mVideoCategory != null;
			default :
				return false;
		}
	}
	
	/**
	 * 多媒体数据是否正在初始化
	 * @param type
	 * @return
	 */
	public boolean isMidiaDataIniting(int type) {
		switch (type) {
		case TYPE_IMAGE :
			return mInitingImageData;
		case TYPE_AUDIO :
			return mInitingAudioData;
		case TYPE_VIDEO :
			return mInitingVideoData;
		default :
			return false;
		}
	}

	/**
	 * 初始化多媒体数据
	 * @param type
	 */
	public void refreshMediaData(int type) {
		switch (type) {
			case TYPE_IMAGE :
				refreshImage();
				break;
			case TYPE_AUDIO :
				refreshAudio();
				break;
			case TYPE_VIDEO :
				refreshVideo();
				break;
		}
	}

	private boolean checkIfAsyncTaskRunning(Runnable runnable) {
		if (runnable != null) {
			for (FileManageTask task : mTaskList) {
				if (task.runnable == runnable) {
					return true;
				}
			}
		}
		return false;
	}

	public synchronized void initAudio() {
		if (null == mAudioCategory) {
			mAudioCategory = new ConcurrentHashMap<String, Category>();
		}
		mAudioCategory.clear();
		Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;

		String[] columns = new String[] { BaseColumns._ID, MediaColumns.DATA,
				MediaColumns.DISPLAY_NAME, MediaColumns.SIZE, MediaColumns.DATE_ADDED,
				MediaColumns.DATE_MODIFIED, AudioColumns.DURATION, AudioColumns.ARTIST,
				AudioColumns.ALBUM, AudioColumns.ALBUM_ID, MediaColumns.MIME_TYPE,
				MediaColumns.TITLE };

		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns,
					buildSelectionByCategory(TYPE_AUDIO), null, null);
			if (cur == null) {
				return;
			}

			if (!cur.moveToFirst()) {
				return;
			}

			do {
				AudioFile file = AudioFile.getInfo(cur);
				if (null == file) {
					continue;
				}
				// 取一首歌曲的路径，add by yangbing
//				if (MediaOpenSettingConstants.sMusicPath == null) {
//					if (file.fullFilePath != null && !"".equals(file.fullFilePath)) {
//						MediaOpenSettingConstants.sMusicPath = file.fullFilePath;
//					}
//				}
				if (mAudioCategory.containsKey(file.album)) {
					Category category = mAudioCategory.get(file.album);
					category.addFile(file);
				} else {
					Category category = new Category();
					category.filePath = file.album;
					category.uri = category.filePath;
					category.alias = file.album;
					category.addFile(file);
					mAudioCategory.put(category.filePath, category);
				}
			} while (cur.moveToNext());
		} catch (SQLiteException e) {
			Log.e("FileEngine", "init audio error");
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public synchronized void initImage() {
		if (null == mImageCategory) {
			mImageCategory = new ConcurrentHashMap<String, Category>();
		}
		mImageCategory.clear();

		String volumeName = "external";
		Uri uri = Images.Media.getContentUri(volumeName);
		String[] columns = new String[] { BaseColumns._ID, MediaColumns.DATA,
				MediaColumns.DISPLAY_NAME, MediaColumns.SIZE, MediaColumns.DATE_ADDED,
				MediaColumns.DATE_MODIFIED, ImageColumns.BUCKET_DISPLAY_NAME,
				MediaColumns.MIME_TYPE };
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns,
					buildSelectionByCategory(TYPE_IMAGE), null, null);
			if (cur == null) {
				return;
			}

			if (!cur.moveToFirst()) {
				return;
			}

			do {
				ImageFile file = ImageFile.getInfo(cur);
				if (null == file) {
					continue;
				}
				// 取一张图片的路径，add by yangbing
//				if (MediaOpenSettingConstants.sImagePath == null) {
//					if (file.fullFilePath != null && !"".equals(file.fullFilePath)) {
//						MediaOpenSettingConstants.sImagePath = file.fullFilePath;
//					}
//				}
				if (mImageCategory.containsKey(file.filePath)) {
					Category category = mImageCategory.get(file.filePath);
					category.addFile(file);
				} else {
					Category category = new Category();
					category.filePath = file.filePath;
					category.uri = category.filePath;
					category.alias = file.bucketName;
					category.init();
					category.addFile(file);
					mImageCategory.put(file.filePath, category);
				}
			} while (cur.moveToNext());
		} catch (SQLiteException e) {
			Log.e("FileEngine", "init image error");
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}

	public synchronized void initVideo() {
		if (null == mVideoCategory) {
			mVideoCategory = new ConcurrentHashMap<String, Category>();
		}
		mVideoCategory.clear();

		String volumeName = "external";
		Uri uri = Video.Media.getContentUri(volumeName);

		String[] columns = new String[] { BaseColumns._ID, MediaColumns.DATA,
				MediaColumns.DISPLAY_NAME, MediaColumns.SIZE, MediaColumns.DATE_ADDED,
				MediaColumns.DATE_MODIFIED, VideoColumns.DURATION, MediaColumns.MIME_TYPE };
		Cursor cur = null;
		try {
			cur = mContext.getContentResolver().query(uri, columns,
					buildSelectionByCategory(TYPE_AUDIO), null, null);

			if (cur == null) {
				return;
			}

			if (!cur.moveToFirst()) {
				return;
			}

			do {
				VideoFile file = VideoFile.getInfo(cur);
				if (null == file) {
					continue;
				}

				if (mVideoCategory.containsKey(file.filePath)) {
					Category category = mVideoCategory.get(file.filePath);
					category.addFile(file);
				} else {
					Category category = new Category();
					category.filePath = file.filePath;
					category.uri = category.filePath;
					category.init();
					category.addFile(file);
					mVideoCategory.put(file.filePath, category);
				}
			} while (cur.moveToNext());
		} catch (SQLiteException e) {
			Log.e("FileEngine", "init video error");
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
	}
}
