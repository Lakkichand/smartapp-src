package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.ThreadPoolManager;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.MainDataHttpOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ImagesBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ImagesBean.ImageData;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.theme.GoLockerThemeManager;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 主题图片资源管理
 * 
 * @author yangbing
 * */
public class ThemeImageManager {

	public static final int GET_IMG_BY_ID = 0;
	public static final int GET_IMG_BY_URL = 1;

	public static final int EVENT_LOCAL_ICON_EXIT = 930000;
	public static final int EVENT_LOCAL_ICON_NOT_EXIT = 930001;
	public static final int EVENT_NETWORK_ICON_CHANGE = 930002;
	public static final int EVENT_NETWORK_EXCEPTION = 930003;
	public static final int EVENT_NETWORK_ICON_URL_CHANGE = 930004; //通过url下载完成
	public static final int FINISH_LOAD_IMAGE = 930005; // 图片加载完成

	private static final int MAX_IMAGE_COUNT = 100; // 最多图片个数
	private static final int MAX_IMAGE_TMP_COUNT = 3;

	//	private static final long IMG_EXPIRED_TIME = 15 * AlarmManager.INTERVAL_DAY; // 图片过期时间15天
	private Context mContext;
	private static ThemeImageManager sInstance = null;

	private LinkedHashMap<String, ThemeImage> mFeaturedImageCache; // 精选图片缓存
	//	private LinkedHashMap<String, ThemeImage> mHotImageCache; //热门图片缓存
	private LinkedHashMap<String, ThemeImage> mInstalledImageCache; // 本地图片缓存

	private GoLockerThemeManager mGoLockerThemeManager;
	private ImageExplorer mImageExplorer;
	private ThreadPoolManager mThreadPoolManager;
	private ThreadPoolManager mDownLoadThreadPoolManager;
	ConcurrentHashMap<String, ThemeImage> mBitmapHashMap = new ConcurrentHashMap<String, ThemeImage>(
			MAX_IMAGE_COUNT); // 图片存储
	// private HashMap<String, Runnable> mLoadingImgRunableHashMap = null;

	// private ArrayList<String> mLoadingImgIdArrayList = null;
	private Handler mHandler = null;
	private ArrayList<String> mNetImgIdList = new ArrayList<String>(MAX_IMAGE_TMP_COUNT + 2);
	// 循环判断线程
	private LooperThread mLooperThread = null;

	//	private BroadcastReceiver mDownloadReceiver = null; // 进度接收器

	private ThemeImageManager(Context context) {
		mContext = context;
		mGoLockerThemeManager = new GoLockerThemeManager(mContext);
		mImageExplorer = ImageExplorer.getInstance(mContext);
		mFeaturedImageCache = new LinkedHashMap<String, ThemeImage>();
		//		mHotImageCache = new LinkedHashMap<String, ThemeImage>();
		mInstalledImageCache = new LinkedHashMap<String, ThemeImage>();
		ThreadPoolManager.buildInstance("theme", 6, 6, 0, TimeUnit.SECONDS);
		mThreadPoolManager = ThreadPoolManager.getInstance("theme");
		ThreadPoolManager.buildInstance("themedownload", 3, 3, 0, TimeUnit.SECONDS);
		mDownLoadThreadPoolManager = ThreadPoolManager.getInstance("themedownload");
		// mLoadingImgRunableHashMap = new HashMap<String, Runnable>();
		// mLoadingImgIdArrayList = new ArrayList<String>();
		initHandler();
	}

	/**
	 * 初始化handler处理图片加载完成事件
	 * */
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case FINISH_LOAD_IMAGE : {
						Object object = msg.obj;
						if (object != null) {
							if (object instanceof LoadImageData) {
								LoadImageData data = (LoadImageData) object;
								ThemeImage themeImage = getThemeImageFromCache(data.themeInfoBean);
								if (themeImage != null) {
									themeImage.setDrawable(data.drawable);
									themeImage.broadCast(FINISH_LOAD_IMAGE, 0, data.drawable, null);
								}
							} else if (object instanceof ThemeImage) {
								ThemeImage themeImage = (ThemeImage) object;
								themeImage.broadCast(FINISH_LOAD_IMAGE, 0, themeImage.mDrawable,
										null);
							}
						}
					}
						break;
					case EVENT_LOCAL_ICON_EXIT :
					case EVENT_NETWORK_ICON_CHANGE :
						if (msg.obj != null) {
							ThemeImage image = (ThemeImage) msg.obj;
							if (image != null) {
								String imgId = image.mImageId;
								image.broadCast(EVENT_NETWORK_ICON_CHANGE, 0, image.getDrawable(),
										imgId);
							}
						}
						break;
					case EVENT_NETWORK_ICON_URL_CHANGE : {
						if (msg.obj != null) {
							ThemeImage image = (ThemeImage) msg.obj;
							if (image != null) {
								image.broadCast(EVENT_NETWORK_ICON_CHANGE, 0, image.getDrawable(),
										image.mImageId);
							}
						}
					}
						break;
					case EVENT_LOCAL_ICON_NOT_EXIT : {
						if (msg.obj != null) {
							if (msg.obj instanceof ThemeInfoBean) {
								ThemeInfoBean bean = (ThemeInfoBean) msg.obj;
								if (bean.getImgSource() == 0 || bean.getImgUrls() == null) {
									getImageDataFromNetWithWait(getThemeImageFromCache(bean));
								} else {
									downLoadFileByUrl(getThemeImageFromCache(bean));
								}
							} else if (msg.obj instanceof ThemeImage) {
								if (msg.arg1 == GET_IMG_BY_ID) {
									getImageDataFromNetWithWait((ThemeImage) msg.obj);
								} else {
									downLoadFileByUrl((ThemeImage) msg.obj);
								}
							}
						}
					}
						break;
					case EVENT_NETWORK_EXCEPTION : {
						if (msg.obj != null) {
							ThemeImage image = (ThemeImage) msg.obj;
							if (image != null) {
								String imgId = image.mImageId;
								image.broadCast(EVENT_NETWORK_EXCEPTION, 0, image.getDrawable(),
										imgId);
							}
						}
					}
						break;
					default :
						break;
				}
			};
		};
	}

	public synchronized static ThemeImageManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ThemeImageManager(context);
		}
		return sInstance;
	}

	private synchronized static void releaseSelfInstance() {
		sInstance = null;
	}

	/**
	 * 加载图片
	 * */
	public Drawable getImage(ThemeInfoBean infoBean) {

		// 取精选图片
		if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			return loadFeaturedImage(infoBean.getFirstPreViewDrawableName());
		} else if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_INSTALLED_THEME_ID) {
			// 取桌面本地图片
			Drawable drawable = loadLauncherInstalledImage(infoBean.getPackageName(),
					infoBean.getFirstPreViewDrawableName());
			if (drawable == null && mContext != null && mContext.getResources() != null) {
				drawable = mContext.getResources().getDrawable(R.drawable.theme_default_bg);
			}
			return drawable;

		} else if (infoBean.getBeanType() == ThemeConstants.LOCKER_INSTALLED_THEME_ID) {
			// 取锁屏本地图片
			Drawable drawable = loadLockerInstalledImage(infoBean.getPackageName());
			if (drawable == null) {
				drawable = mContext.getResources().getDrawable(R.drawable.theme_default_bg);
			}
			return drawable;
		}
		return null;
	}

	public Drawable getImageByUrl(final String url, final BroadCasterObserver observer,
			final String path, final String id) {
		Drawable drawable = null;
		ThemeImage image = null;
		if (mFeaturedImageCache != null) {
			image = mFeaturedImageCache.get(id);
		}
		if (image != null && image.mDrawable != null) {
			return image.mDrawable;
		}
		mThreadPoolManager.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				ThemeImage newimage = new ThemeImage();
				newimage.mImageId = id;
				Drawable imgDrawable = loadFeaturedImage(path + newimage.mImageId);
				if (imgDrawable != null && mFeaturedImageCache.containsKey(newimage.mImageId)) {
					newimage = mFeaturedImageCache.get(newimage.mImageId);
				}
				newimage.mDrawable = imgDrawable;
				newimage.mStorePath = path;
				newimage.mUrl = url;
				newimage.registerObserver(observer);
				if (mHandler != null) {
					Message message = mHandler.obtainMessage();
					if (imgDrawable != null) {
						message.what = EVENT_LOCAL_ICON_EXIT;
					} else {
						if (!Machine.isNetworkOK(mContext)) {
							return;
						}
						message.what = EVENT_LOCAL_ICON_NOT_EXIT;
					}
					message.obj = newimage;
					message.arg1 = GET_IMG_BY_URL;
					mHandler.sendMessage(message);
				}

			}
		});

		return drawable;
	}

	public Drawable getImageById(String id, String storePath, BroadCasterObserver observer) {
		Drawable drawable = getImageFromCache(id);
		if (null == drawable) {

			String url = null;
			ThemeImage themeImage = new ThemeImage(null, null, id, url, storePath);
			themeImage.registerObserver(observer);
			mFeaturedImageCache.put(id, themeImage);
			mThreadPoolManager.execute(new LoadImageRunable(themeImage));
		}
		return drawable;
	}

	/**
	 * 加载图片
	 * */
	public Drawable getImageByThemeInfo(final ThemeInfoBean infoBean, BroadCasterObserver observer) {
		Drawable drawable = getImageFromCache(infoBean);
		if (null == drawable) {

			String path = null;
			String url = null;
			String id = infoBean.getFirstPreViewDrawableName();
			if (!isInstallTheme(infoBean)) {
				if (infoBean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
					path = LauncherEnv.Path.GOTHEMES_PATH + "lockericon/";
				} else {
					path = LauncherEnv.Path.GOTHEMES_PATH + "icon/";
				}
				if (infoBean.getImgSource() == 1 && infoBean.getImgUrls() != null
						&& !infoBean.getImgUrls().isEmpty()) {
					url = infoBean.getImgUrls().get(0);
				}

				if (url != null && id != null && id.lastIndexOf("/") > 0) {
					id = id.substring(id.lastIndexOf("/") + 1);
				}
			}
			if ((infoBean.getBeanType() != ThemeConstants.LAUNCHER_INSTALLED_THEME_ID
					&& infoBean.getBeanType() != ThemeConstants.LOCKER_INSTALLED_THEME_ID)
					&& (id == null || id.equals(""))) {
				return null;
			}
			ThemeImage themeImage = new ThemeImage(infoBean.getPackageName(), null, id, url, path);
			themeImage.registerObserver(observer);
			if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
					|| infoBean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID
					|| infoBean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID
					|| infoBean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
				mFeaturedImageCache.put(infoBean.getPackageName(), themeImage);
			} else if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_INSTALLED_THEME_ID
					|| infoBean.getBeanType() == ThemeConstants.LOCKER_INSTALLED_THEME_ID) {
				mInstalledImageCache.put(infoBean.getPackageName(), themeImage);
			}
			mThreadPoolManager.execute(new LoadImageRunable(infoBean));
		}
		return drawable;
	}

	/**
	 * 加载桌面主题本地图片
	 * */
	private Drawable loadLauncherInstalledImage(String packageName, String imgName) {
		if (mImageExplorer != null) {
			return mImageExplorer.getDrawable(packageName, imgName);
		} else {
			return null;
		}
	}

	/**
	 * 加载锁屏主题本地图片
	 * */
	private Drawable loadLockerInstalledImage(String packageName) {
		if ("com.jiubang.goscreenlock.theme.random".equals(packageName)) {
			return mGoLockerThemeManager.getRandomPreView(AppUtils.getCurLockerPkgName(mContext));
		}
		return mGoLockerThemeManager.getPreView(packageName);

	}

	/**
	 * 加载精选图片
	 * */
	private Drawable loadFeaturedImage(String imgName) {
		Drawable drawable = null;
		try {
			Bitmap bitmap = BitmapFactory.decodeFile(imgName);
			if (bitmap != null) {
				drawable = new BitmapDrawable(bitmap);
			}
			if (drawable == null && mImageExplorer != null) {
				drawable = mImageExplorer.getDrawable(ThemeManager.DEFAULT_THEME_PACKAGE, imgName);
			}

		} catch (OutOfMemoryError e) {

		} catch (Exception e) {
			// TODO: handle exception
		}
		return drawable;

	}

	/**
	 * 从缓存里面取图片
	 * */
	public Drawable getImageFromCache(String id) {
		Drawable drawable = null;
		if (mFeaturedImageCache != null) {
			ThemeImage themeImage = mFeaturedImageCache.get(id);
			if (null != themeImage) {
				drawable = themeImage.getDrawable();
			}
		}
		return drawable;
	}

	/**
	 * 从缓存里面取图片
	 * */
	public Drawable getImageFromCache(ThemeInfoBean mThemeData) {
		Drawable drawable = null;
		ThemeImage themeImage = getThemeImageFromCache(mThemeData);
		if (null != themeImage) {
			drawable = themeImage.getDrawable();
		}
		return drawable;
	}

	/**
	 * 从缓存里面取ThemeImage
	 * */
	public ThemeImage getThemeImageFromCache(ThemeInfoBean infoBean) {
		ThemeImage themeImage = null;
		if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			themeImage = mFeaturedImageCache.get(infoBean.getPackageName());
		} else {
			themeImage = mInstalledImageCache.get(infoBean.getPackageName());
		}
		return themeImage;

	}

	/**
	 * 取消监听
	 * */
	public void unregisterImageObverser(ThemeInfoBean ThemeInfoBean, BroadCasterObserver observer) {
		if (ThemeInfoBean == null || observer == null) {
			return;
		}
		ThemeImage themeImage = getThemeImageFromCache(ThemeInfoBean);
		if (themeImage != null) {
			themeImage.unRegisterObserver(observer);
		}
	}

	/**
	 * 清理操作
	 * */
	public void clearup() {
		if (mFeaturedImageCache != null) {
			mFeaturedImageCache.clear();
		}
		if (mInstalledImageCache != null) {
			mInstalledImageCache.clear();
		}
		//		if (mHotImageCache != null) {
		//			mHotImageCache.clear();
		//		}

	}

	/**
	 * 图片加载线程
	 * */
	private class LoadImageRunable implements Runnable {

		private ThemeInfoBean mThemeInfoBean;
		private ThemeImage mImg;
		public LoadImageRunable(ThemeInfoBean ThemeInfoBean) {
			mThemeInfoBean = ThemeInfoBean;
		}
		public LoadImageRunable(ThemeImage image) {
			mImg = image;
		}

		@Override
		public void run() {
			Drawable drawable = null;
			if (mThemeInfoBean != null && mImg == null) {
				mImg = getThemeImageFromCache(mThemeInfoBean);

			}
			if (mImg != null && !isInstallTheme(mThemeInfoBean)) {
				String path = null;
				if (mImg.mImageId != null && mImg.mImageId.lastIndexOf("/") > 0) {
					path = mImg.mImageId;
				} else {
					path = mImg.mStorePath + mImg.mImageId;
				}

				drawable = loadFeaturedImage(path); // 首先使用ID从缓存中查找

				//begin 如果ID查找不到在使用URL最后的文件名查找，该段代码的原因：3.20使用URL最后的文件名作为保存，
				//3.21后全部使用ID保存，这样做是为了降低3.20-》3.21时的取图流量，以后可以去掉该段代码
				if (drawable == null && mThemeInfoBean != null
						&& mThemeInfoBean.getImgUrls() != null
						&& !mThemeInfoBean.getImgUrls().isEmpty()) {
					String url = mThemeInfoBean.getImgUrls().get(0);
					if (url != null && url.lastIndexOf("/") > 0) {
						String name = url.substring(url.lastIndexOf("/") + 1);
						drawable = loadFeaturedImage(mImg.mStorePath + name);
					}
				}
				//end
			} else if (mThemeInfoBean != null) {
				drawable = getImage(mThemeInfoBean);
			}
			if (mHandler != null) {
				if (drawable == null && !isInstallTheme(mThemeInfoBean)
						&& Machine.isNetworkOK(mContext)) {
					Message message = mHandler.obtainMessage();
					message.what = EVENT_LOCAL_ICON_NOT_EXIT;
					if (mImg != null) {
						message.obj = mImg;
						if (mImg.mUrl != null) {
							message.arg1 = GET_IMG_BY_URL;
						} else {
							message.arg1 = GET_IMG_BY_ID;
						}
					} else if (mThemeInfoBean != null) {
						message.obj = mThemeInfoBean;
						message.arg1 = GET_IMG_BY_ID;
					}
					mHandler.sendMessage(message);;
				} else if (drawable != null) {
					Message message = mHandler.obtainMessage();
					message.what = FINISH_LOAD_IMAGE;
					if (mImg != null) {
						mImg.mDrawable = drawable;
						message.obj = mImg;
					} else if (mThemeInfoBean != null) {
						LoadImageData data = new LoadImageData();
						data.drawable = drawable;
						data.themeInfoBean = mThemeInfoBean;
						message.obj = data;
					}
					mHandler.sendMessage(message);;
				}
			}
		}
	}

	/**
	 * 图片封装类
	 * */
	public static class ThemeImage extends BroadCaster {

		private String mPkgName = null;
		private Drawable mDrawable;
		private String mImageId;
		private String mUrl;
		private String mStorePath;
		public ThemeImage() {
		}

		public ThemeImage(String pkgName, Drawable drawable, String imgId, String url, String path) {
			mPkgName = pkgName;
			mDrawable = drawable;
			mImageId = imgId;
			mUrl = url;
			mStorePath = path;
		}

		public String getPkgName() {
			return mPkgName;
		}

		public void setPkgName(String pkgName) {
			this.mPkgName = pkgName;
		}

		public void setDrawable(Drawable drawable) {
			mDrawable = drawable;
		}

		public final Drawable getDrawable() {
			return mDrawable;
		}

		public String getImageId() {
			return mImageId;
		}
		public String getImageUrl() {
			return mUrl;
		}
		public String getStorePath() {
			return mStorePath;
		}
		public void recyle() {
			mDrawable = null;
			mPkgName = null;
		}

	}

	/**
	 * 图片数据传递类
	 * */
	public static class LoadImageData {
		public Drawable drawable;
		public ThemeInfoBean themeInfoBean;
	}

	/**
	 * 缓存图片
	 * */
	public void putImageCache(List<ThemeInfoBean> themeInfoBeans) {
		for (ThemeInfoBean themeInfoBean : themeInfoBeans) {
			String packageName = themeInfoBean.getPackageName();
			String path = null;
			if (themeInfoBean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
				path = LauncherEnv.Path.GOTHEMES_PATH + "lockericon/";
			} else {
				path = LauncherEnv.Path.GOTHEMES_PATH + "icon/";
			}
			String url = null;
			if (themeInfoBean.getImgSource() == 1 && themeInfoBean.getImgUrls() != null
					&& !themeInfoBean.getImgUrls().isEmpty()) {
				url = themeInfoBean.getImgUrls().get(0);
			}
			ThemeImage themeImage = new ThemeImage(packageName, getImage(themeInfoBean),
					themeInfoBean.getFirstPreViewDrawableName(), url, path);
			if (themeInfoBean.getBeanType() == ThemeConstants.LAUNCHER_INSTALLED_THEME_ID
					|| themeInfoBean.getBeanType() == ThemeConstants.LOCKER_INSTALLED_THEME_ID) {
				mInstalledImageCache.put(packageName, themeImage);
			} else {
				mFeaturedImageCache.put(packageName, themeImage);
			}
		}
	}
	/**
	 * 是否存在缓存图片
	 * 
	 * @return true:存在；false：不存在
	 * */
	public boolean isExsitImageCache(int type) {
		if (type == ThemeConstants.LAUNCHER_INSTALLED_THEME_ID
				|| type == ThemeConstants.LOCKER_INSTALLED_THEME_ID) {
			return !mInstalledImageCache.isEmpty();
		} else {
			return !mFeaturedImageCache.isEmpty();
		}

	}
	//	private boolean isImgExpired(String filePath) {
	//		boolean result = false;
	//		if (filePath != null && !"".equals(filePath.trim())) {
	//			try {
	//
	//				Date curDate = new Date();
	//				long curTime = curDate.getTime(); // 当前时间值
	//
	//				File file = new File(filePath);
	//				if (file.exists()) {
	//					long fileTime = file.lastModified(); // 文件创建时间值
	//					if ((curTime - fileTime) > IMG_EXPIRED_TIME) {
	//						// 文件已过期
	//						result = true;
	//						file.delete();
	//					}
	//				}
	//
	//			} catch (Exception e) {
	//				e.printStackTrace();
	//			}
	//		}
	//		return result;
	//	}

	/**
	 * <br>功能简述:通过url下载单张图片
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void downLoadFileByUrl(final ThemeImage image) {
		if (image != null && image.mUrl != null) {
			mDownLoadThreadPoolManager.execute(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					URL url_im;
					InputStream in = null;
					try {
						url_im = new URL(image.mUrl);
						URLConnection conn = (HttpURLConnection) url_im.openConnection();
						conn.connect();
						in = conn.getInputStream();
						Bitmap bmp = BitmapFactory.decodeStream(in);
						if (mBitmapHashMap != null && mHandler != null && bmp != null) {
							if (!FileUtil.isFileExist(image.mImageId)) {
								// TODO Auto-generated method stub
								String path = image.mImageId;
								if (!image.mImageId.contains("/") && image.mStorePath != null) {
									path = image.mStorePath + image.mImageId;
								}
								FileUtil.saveBitmapToSDFile(bmp, path, CompressFormat.PNG);
							}
							// TODO:通知异步更新
							image.setDrawable(new BitmapDrawable(bmp));
							Message message = mHandler.obtainMessage();
							message.what = EVENT_NETWORK_ICON_CHANGE;
							message.obj = image;
							mHandler.sendMessage(message);

						}
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OutOfMemoryError e) {
						// TODO: handle exception
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (in != null) {
							try {
								in.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}

			});
		}
	}

	/**
	 * 批量获取网络图片数据，可能需要等待。
	 * 
	 * @author rongjinsong
	 * @param imgId
	 */
	private synchronized void getImageDataFromNetWithWait(ThemeImage image) {
		if (image != null) {
			if (mBitmapHashMap == null) {
				return;
			}

			String imageName = image.mImageId;
			if (imageName != null) {
				int start = imageName.lastIndexOf("/") + 1;
				if (start > 0) {
					imageName = imageName.substring(start, imageName.length());
				}
				if (!mBitmapHashMap.contains(image)) {
					mBitmapHashMap.put(imageName, image);
				}
			}
			// 添加到任务队列中，imgId为空，则直接使用mNetImgIdList中的请求进行
			if (mNetImgIdList != null && !mNetImgIdList.contains(imageName)) {
				mNetImgIdList.add(imageName);
			}
			if (mNetImgIdList != null && mNetImgIdList.size() < MAX_IMAGE_TMP_COUNT) {
				// TODO:循环线程进行检测
				if (mLooperThread == null) {
					mLooperThread = new LooperThread("LooperThread", GET_IMG_BY_ID);
					mLooperThread.setPriority(Thread.NORM_PRIORITY - 2);
					mLooperThread.start();
				}
				synchronized (mLooperThread) {
					mLooperThread.notify();
				}
				return;
			}
		}

		int size = 0;
		if (mNetImgIdList != null) {
			size = mNetImgIdList.size();
		}
		if (size <= 0) {
			// 已无待请求的图片
			return;
		}

		// 网络批量获取图片
		ArrayList<String> cloneImgIdArrayList = null;
		if (mNetImgIdList != null) {
			cloneImgIdArrayList = (ArrayList<String>) mNetImgIdList.clone();
			mNetImgIdList.clear();
		}
		getImageDataFromNet(cloneImgIdArrayList);
	}

	/**
	 * 从网络获取数据接口
	 * 
	 * @author rongjinsong
	 * @param url
	 * @param imgIdList
	 */
	private void getImageDataFromNet(final List<String> imgIdList) {
		if (imgIdList == null || imgIdList.size() <= 0) {
			return;
		}
		int size = imgIdList.size();
		StringBuffer imgidStringBuffer = new StringBuffer();
		for (int i = 0; i < size; i++) {
			imgidStringBuffer.append(imgIdList.get(i)).append(";");
		}
		try {
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("imgids", imgidStringBuffer.toString()));
			// 获取POST请求数据
			byte[] postData = ThemeHttp.getPostData(mContext, nameValuePairs,
					GoStorePublicDefine.FUNID_IMAGELIST);

			// 下载图片网络信息收集
			//			TestAppGameNetLogControll.getInstance().setUrl(TestAppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, GoStorePublicDefine.URL_HOST3);

			THttpRequest request = new THttpRequest(GoStorePublicDefine.URL_HOST3, postData,
					new IConnectListener() {
						//						// 下载图片网络信息收集
						//						long mTime = 0;
						@Override
						public void onStart(THttpRequest request) {
							// 下载图片网络信息收集
							//							 mTime = System.currentTimeMillis();
						}

						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {

								ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) response
										.getResponse();
								//								long time2 = System.currentTimeMillis() - mTime;
								// listBeans.size应为1
								if (listBeans != null) {
									int size = listBeans.size();
									for (int i = 0; i < size; i++) {
										BaseBean baseBean = listBeans.get(i);
										if (baseBean instanceof ImagesBean) {
											ImagesBean imagesBean = (ImagesBean) baseBean;
											if (imagesBean != null && imagesBean.mImageList != null) {
												// 下发图片数量
												int imgCount = imagesBean.mImageList.size();
												for (int j = 0; j < imgCount; j++) {
													ImageData imgData = imagesBean.mImageList
															.get(j);
													if (imgData != null
															&& imgData.mImgData != null
															&& imgData.mDataLength == imgData.mImgData.length) {
														// 下载图片网络信息收集
														//														String speed = String.valueOf(imgData.mDataLength / time2);
														//														TestAppGameNetLogControll.getInstance().setDownloadSpeed(TestAppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, speed);

														// 加载文件到内存
														Bitmap bmp = BitmapFactory.decodeByteArray(
																imgData.mImgData, 0,
																imgData.mDataLength);
														// 清除掉内存中的图片数据值
														imgData.mImgData = null;

														// 下载完成后
														if (mBitmapHashMap != null
																&& mHandler != null) {
															ThemeImage image = mBitmapHashMap
																	.get(imgData.mImgId);
															if (image != null) {
																if (!FileUtil
																		.isFileExist(image.mImageId)) {
																	// TODO Auto-generated method stub
																	String path = image.mImageId;
																	if (!image.mImageId
																			.contains("/")
																			&& image.mStorePath != null) {
																		path = image.mStorePath
																				+ image.mImageId;
																	}
																	FileUtil.saveBitmapToSDFile(
																			bmp, path,
																			CompressFormat.PNG);
																}
																// TODO:通知异步更新
																image.setDrawable(new BitmapDrawable(
																		bmp));
																Message message = mHandler
																		.obtainMessage();
																message.what = EVENT_NETWORK_ICON_CHANGE;
																message.obj = image;
																mHandler.sendMessage(message);
															}
														}
													}
												}
											}
										}
									}

								}
								//								// 下载图片网络信息收集
								//								TestAppGameNetLogControll.getInstance().stopRecord(
								//										TestAppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, mContext);
							}
						}

						@Override
						public void onException(THttpRequest request, int reason) {
							if (mBitmapHashMap != null && imgIdList != null) {
								// 逐个通知各图片，网络获取异常。
								for (String imgId : imgIdList) {
									if (imgId == null) {
										return;
									}
									ThemeImage image = mBitmapHashMap.get(imgId);
									if (image != null && mHandler != null) {
										Message message = mHandler.obtainMessage();
										message.what = EVENT_NETWORK_EXCEPTION;
										message.obj = image;
										image.setDrawable(mContext.getResources().getDrawable(
												R.drawable.theme_default_bg));
										mHandler.sendMessage(message);
									}
								}
							}
						}
					});
			// 设置POST请求头
			request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);
			// 设置图片网络请求优先级
			request.setRequestPriority(Thread.NORM_PRIORITY - 2);

			MainDataHttpOperator operator = new MainDataHttpOperator();
			request.setOperator(operator);

			//			// 下载图片网络信息收集
			//			CopyOfAppGameNetRecord themeNetRecord = new CopyOfAppGameNetRecord(mContext, false);
			//			request.setNetRecord(themeNetRecord);

			SimpleHttpAdapter httpAdapter = SimpleHttpAdapter.getInstance(mContext);
			if (httpAdapter != null) {
				httpAdapter.addTask(request);
				// if(mLoadingImgIdArrayList != null){
				// mLoadingImgIdArrayList.addAll(imgIdList);
				// }
			} else {
				// Log.i("GoStore",
				// "SimpleHttpAdapter in ImageManager is null");
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * 循环判断线程
	 * 
	 * @author rongjinsong
	 * 
	 */
	private class LooperThread extends Thread {
		private final int mloopCount = 4;
		private long mMills = 50; // 每次睡眠25ms
		private int mCount = 0;
		private int mType;
		public LooperThread(String string, int type) {
			// TODO Auto-generated constructor stub
			super(string);
			mType = type;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while (true) {
				try {
					if (mCount > mloopCount) {
						mCount = 0;
						if (mType == GET_IMG_BY_URL) {
							downLoadFileByUrl(null);
						} else {
							getImageDataFromNetWithWait(null);
						}
						synchronized (this) {
							wait();
						}
					} else {
						++mCount;
						sleep(mMills);
					}

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					continue;
				}
			}

		}
	}

	public void destory() {
		clearup();
		if (mBitmapHashMap != null) {
			mBitmapHashMap.clear();
		}
		mGoLockerThemeManager = null;
		mImageExplorer = null;
		mThreadPoolManager = null;
		mDownLoadThreadPoolManager = null;
		mContext = null;
		releaseSelfInstance();
	}

	private boolean isInstallTheme(ThemeInfoBean bean) {
		boolean bRet = false;
		if (bean != null
				&& (bean.getBeanType() == ThemeConstants.LAUNCHER_INSTALLED_THEME_ID || bean
						.getBeanType() == ThemeConstants.LOCKER_INSTALLED_THEME_ID)) {
			bRet = true;
		}
		return bRet;
	}

}
