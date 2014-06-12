package com.go.util.file.media;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.Video;

/**
 * 缩略图管理者
 * 
 * @author yangguanxiang
 * 
 */
public class ThumbnailManager extends MediaBroadCaster {
	public static final int MSG_ID_LOAD_IMAGE_COMPLETED = 0;
	public static final int MSG_ID_LOAD_IMAGE_FAILED = 1;
	public static final String ID_KEY = "_id";
	public static final String TYPE_IMAGE = "image_";
	public static final String TYPE_VIDEO = "video_";
	public static final String TYPE_ALBUM = "album_";
	public static final String TYPE_PLAYLIST = "playlist_";
	public static final String THREAD_POOL_MANAGER_NAME = "ThumbnailManager_Thread_Pool";
	private static final int DEFAULT_BITMAP_WIDTH = 140;
	private static ThumbnailManager sInstance;

	private Context mContext;

	private ConcurrentHashMap<String, SoftReference<Bitmap>> mImageThumbnailMap = new ConcurrentHashMap<String, SoftReference<Bitmap>>();
	private ConcurrentHashMap<String, Runnable> mLoadingImageRunableHashMap = new ConcurrentHashMap<String, Runnable>();

	private Handler mHandler;

	private Uri mArtworkUri = Uri
			.parse("content://media/external/audio/albumart");
	private BitmapFactory.Options mBitmapOptions = new BitmapFactory.Options();

	private ThumbnailManager(Context context) {
		mContext = context;
		initHandler();
		mBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
		mBitmapOptions.inDither = false;
	}

	public synchronized static ThumbnailManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ThumbnailManager(context);
		}
		return sInstance;
	}

	private void initHandler() {
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				String imageId = msg.getData().getString(ID_KEY);
				int id = msg.arg1;
				switch (msg.what) {
				case MSG_ID_LOAD_IMAGE_COMPLETED:
					Bitmap bitmap = (Bitmap) msg.obj;
					SoftReference<Bitmap> imageRef = new SoftReference<Bitmap>(
							bitmap);
					if (mImageThumbnailMap.containsKey(imageId)) {
						mImageThumbnailMap.replace(imageId, imageRef);
					} else {
						mImageThumbnailMap.put(imageId, imageRef);
					}
					mLoadingImageRunableHashMap.remove(imageId);
					ThumbnailManager.this.broadCast(
							MSG_ID_LOAD_IMAGE_COMPLETED, id, bitmap, null);
					break;
				case MSG_ID_LOAD_IMAGE_FAILED:
					mLoadingImageRunableHashMap.remove(imageId);
					ThumbnailManager.this.broadCast(MSG_ID_LOAD_IMAGE_FAILED,
							id, null, null);
					break;
				default:
					break;
				}
			}
		};
	}

	/**
	 * 获取缩略图
	 * 
	 * @param observer
	 * @param type
	 * @param id
	 * @param filePath
	 * @return
	 */
	public Bitmap getThumbnail(MediaBroadCasterObserver observer, String type,
			int id, String filePath) {
		Bitmap bitmap = null;
		String imageId = type + id;
		if (mImageThumbnailMap.containsKey(imageId)) {
			SoftReference<Bitmap> ref = mImageThumbnailMap.get(imageId);
			if (ref != null) {
				bitmap = ref.get();
			}
		}
		if (bitmap == null) {
			registerObserver(observer);
			startLoadThumbnail(type, id, filePath, DEFAULT_BITMAP_WIDTH);
		}
		return bitmap;
	}

	/**
	 * 获取缩略图
	 * 
	 * @param observer
	 * @param type
	 * @param id
	 * @param filePath
	 * @param imgWidth
	 * @return
	 */
	public Bitmap getThumbnail(MediaBroadCasterObserver observer, String type,
			int id, String filePath, int imgWidth) {
		Bitmap bitmap = null;
		String imageId = type + id;
		if (mImageThumbnailMap.containsKey(imageId)) {
			SoftReference<Bitmap> ref = mImageThumbnailMap.get(imageId);
			if (ref != null) {
				bitmap = ref.get();
			}
		}
		if (bitmap == null) {
			registerObserver(observer);
			startLoadThumbnail(type, id, filePath, imgWidth);
		}
		return bitmap;
	}

	/**
	 * 获取播放列表封面缩略图
	 * 
	 * @param observer
	 * @param type
	 * @param playlistid
	 * @param files
	 * @return
	 */
	public Bitmap getPlayListThumbnail(MediaBroadCasterObserver observer,
			String type, int playlistid, ArrayList<FileInfo> files) {
		Bitmap bitmap = null;
		String imageId = type + playlistid;
		if (mImageThumbnailMap.containsKey(imageId)) {
			SoftReference<Bitmap> ref = mImageThumbnailMap.get(imageId);
			if (ref != null) {
				bitmap = ref.get();
			}
		}
		if (bitmap == null) {
			registerObserver(observer);
			startLoadPlayListThumbnail(type, playlistid, files);
		}
		return bitmap;
	}

	private void startLoadPlayListThumbnail(final String type, final int id,
			final ArrayList<FileInfo> files) {
		final String imageId = type + id;
		if (mLoadingImageRunableHashMap != null
				&& !mLoadingImageRunableHashMap.containsKey(imageId)) {
			mLoadingImageRunableHashMap.put(imageId, new Runnable() {
				@Override
				public void run() {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
					// Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putString(ID_KEY, imageId);
					// message.setData(bundle);
					try {
						Bitmap bitmap = null;
						for (FileInfo fileInfo : files) {
							if (fileInfo instanceof AudioFile) {
								AudioFile file = (AudioFile) fileInfo;
								bitmap = getArtwork(mContext, -1, file.albumId);
								if (bitmap != null) {
									break;
								}
							}
						}

						if (bitmap != null) {
							if (mHandler != null) {
								Message message = mHandler
										.obtainMessage(MSG_ID_LOAD_IMAGE_COMPLETED);
								message.arg1 = id;
								message.obj = bitmap;
								message.setData(bundle);
								mHandler.sendMessage(message);
							}
						} else {
							if (mHandler != null) {
								Message message = mHandler
										.obtainMessage(MSG_ID_LOAD_IMAGE_FAILED);
								message.arg1 = id;
								message.setData(bundle);
								mHandler.sendMessage(message);
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
						if (mHandler != null) {
							Message message = mHandler
									.obtainMessage(MSG_ID_LOAD_IMAGE_FAILED);
							message.arg1 = id;
							message.setData(bundle);
							mHandler.sendMessage(message);
						}
					}
				}
			});
			MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME)
					.execute(mLoadingImageRunableHashMap.get(imageId));
		}
	}

	private void startLoadThumbnail(final String type, final int id,
			final String filePath, final int imgWidth) {
		final String imageId = type + id;
		if (mLoadingImageRunableHashMap != null
				&& !mLoadingImageRunableHashMap.containsKey(imageId)) {
			mLoadingImageRunableHashMap.put(imageId, new Runnable() {
				@Override
				public void run() {
					android.os.Process
							.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
//					Message message = new Message();
					Bundle bundle = new Bundle();
					bundle.putString(ID_KEY, imageId);
					
					try {
						Bitmap bitmap = null;
						// MediaThumbnail thumbnailInfo = null;
						// FileEngine fileEngine =
						// AppFuncFrame.getFunControler()
						// .getFileEngine();
						if (TYPE_IMAGE.equals(type)) {
							// if (fileEngine != null) {
							// thumbnailInfo = fileEngine.getImageThumbnail(id);
							// }
							// if (thumbnailInfo != null) {
							// bitmap = thumbnailInfo.getThumbnail(mContext);
							// } else {
							bitmap = getImageThumbnail(filePath, imgWidth);
							// }
						} else if (TYPE_VIDEO.equals(type)) {
							// if (fileEngine != null) {
							// thumbnailInfo = fileEngine.getVideoThumbnail(id);
							// }
							// if (thumbnailInfo != null) {
							// bitmap = thumbnailInfo.getThumbnail(mContext);
							// } else {
							bitmap = getVideoThumbnail(filePath);
							// }
						} else if (TYPE_ALBUM.equals(type)) {
							// bitmap = getAlbumThumbnail(filePath);
							bitmap = getArtwork(mContext, -1, id);
						}
						if (bitmap != null) {
							if (mHandler != null) {
								Message message = mHandler.obtainMessage(MSG_ID_LOAD_IMAGE_COMPLETED);
								message.arg1 = id;
								message.obj = bitmap;
								message.setData(bundle);
								mHandler.sendMessage(message);
							}
						} else {
							if (mHandler != null) {
								Message message = mHandler.obtainMessage(MSG_ID_LOAD_IMAGE_FAILED);
								message.arg1 = id;
								message.setData(bundle);
								mHandler.sendMessage(message);
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
						if (mHandler != null) {
							Message message = mHandler.obtainMessage(MSG_ID_LOAD_IMAGE_FAILED);
							message.arg1 = id;
							message.setData(bundle);
							mHandler.sendMessage(message);
						}
					}
				}
			});
			MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME)
					.execute(mLoadingImageRunableHashMap.get(imageId));
		}
	}

	/**
	 * 取消缩略图加载
	 * 
	 * @param type
	 * @param id
	 */
	public void cancelLoadThumbnail(MediaBroadCasterObserver observer,
			String type, int id) {
		String imageId = type + id;
		if (mLoadingImageRunableHashMap != null) {
			Runnable runnable = mLoadingImageRunableHashMap.remove(imageId);
			if (runnable != null) {
				MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME)
						.cancel(runnable);
			}
		}
		unRegisterObserver(observer);
	}

	private Bitmap getImageThumbnail(String filePath, int imgWidth) {
		Bitmap bitmap = null;
		Options options = new Options();
		options.inSampleSize = 1;
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(filePath, options);
		int width = options.outWidth;
		float scale = width / imgWidth;
		options.inSampleSize = Math.round(scale);
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(filePath, options);
		return bitmap;
	}

	private Bitmap getVideoThumbnail(String filePath) {
		Bitmap bitmap = null;
		if (Build.VERSION.SDK_INT >= 8) {
			bitmap = ThumbnailUtils.createVideoThumbnail(filePath,
					Video.Thumbnails.MICRO_KIND);
		}
		return bitmap;
	}

	public synchronized static void destory() {
		if (sInstance != null) {
			sInstance.mImageThumbnailMap.clear();
			Collection<Runnable> loadingRunables = sInstance.mLoadingImageRunableHashMap
					.values();
			for (Runnable runnable : loadingRunables) {
				MediaThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME)
						.cancel(runnable);
			}
			sInstance.mLoadingImageRunableHashMap.clear();
			sInstance.mContext = null;
			sInstance = null;
		}
	}

	private Bitmap getArtwork(Context context, long song_id, long album_id) {
		if (album_id < 0) {
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}

			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(mArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, mBitmapOptions);
			} catch (FileNotFoundException ex) {
				// Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				// if (bm != null) {
				// if (bm.getConfig() == null) {
				// bm = bm.copy(Bitmap.Config.RGB_565, false);
				// }
				// }
				// return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
				}
			}
		}

		return null;
	}

	private Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}
		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(mArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd, null,
							mBitmapOptions);
				}
			}
		} catch (FileNotFoundException ex) {
		}
		return bm;
	}

	/**
	 * 手动删除一张缩略图
	 * 
	 * @param type
	 * @param id
	 */
	public void removeThumbnai(String type, int id) {
		String imageId = type + id;
		try {
			mImageThumbnailMap.remove(imageId);
		} catch (Exception e) {
		}

	}
}
