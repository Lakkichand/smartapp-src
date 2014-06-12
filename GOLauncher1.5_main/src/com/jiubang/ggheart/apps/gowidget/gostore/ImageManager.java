package com.jiubang.ggheart.apps.gowidget.gostore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlarmManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.MainDataHttpOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ImagesBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ImagesBean.ImageData;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 图片资源管理器，负责图片资源的维护，包括本地获取及网络获取。
 * 
 * @author huyong
 * 
 */
public class ImageManager {

	public static final int EVENT_NETWORK_ICON_CHANGE = 0;
	public static final int EVENT_NETWORK_EXCEPTION = 1;
	public static final int EVENT_LOCAL_ICON_EXIT = 2;
	public static final int EVENT_LOCAL_ICON_NOT_EXIT = 3;
	public static final String IMAGE_ID_KEY = "imgId";
	public static final String IS_SYN_GET_KEY = "isSynGet";
	public static final String THREAD_POOL_MANAGER_NAME = "imageManager_threadPoolManager";

	private static final int MAX_IMAGE_COUNT = 100; // 最多图片个数
	// private final int MAX_IMAGE_TMP_COUNT = 3;

	// private final long IMG_EXPIRED_TIME = 12*60*60*1000; //图片过期时间12h
	private static final long IMG_EXPIRED_TIME = 15 * AlarmManager.INTERVAL_DAY; // 图片过期时间15天

	ConcurrentHashMap<String, SimpleImage> mBitmapHashMap = new ConcurrentHashMap<String, SimpleImage>(
			MAX_IMAGE_COUNT); // 图片存储

	// private ArrayList<String> mNetImgIdList = new
	// ArrayList<String>(MAX_IMAGE_TMP_COUNT + 2);

	private Handler mHandler = null;

	private static ImageManager sSelf = null;

	// 循环判断线程
//	private LooperThread mLooperThread = null;

	private Context mContext = null;

	private HashMap<String, Runnable> mLoadingImgRunableHashMap = null;

	private ArrayList<String> mLoadingImgIdArrayList = null;

	public synchronized static ImageManager getInstance() {
		if (sSelf == null) {
			sSelf = new ImageManager(GOLauncherApp.getContext());
		}
		return sSelf;
	}

	public synchronized static ImageManager getInstance(Context context) {
		if (sSelf == null) {
			sSelf = new ImageManager(context);
		}
		return sSelf;
	}

	private ImageManager(Context context) {
		mContext = context;
		mLoadingImgRunableHashMap = new HashMap<String, Runnable>();
		mLoadingImgIdArrayList = new ArrayList<String>();
		initHandler();
		// 开启线程清理无效图片
		ThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME)
				.execute(new ClearInvalidImgThread());
	}

//	/**
//	 * 循环判断线程
//	 * 
//	 * @author huyong
//	 * 
//	 */
//	private class LooperThread extends Thread {
//		private static final int LOOPCOUNT = 4;
//		private static final long MILLIS = 50; // 每次睡眠25ms
//		private int mCount = 0;
//
//		public LooperThread(String string) {
//			// TODO Auto-generated constructor stub
//			super(string);
//		}
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			super.run();
//			while (true) {
//				try {
//					if (mCount > LOOPCOUNT) {
//						mCount = 0;
//						// getImageDataFromNetWithWait(null);
//						synchronized (this) {
//							wait();
//						}
//					} else {
//						++mCount;
//						sleep(MILLIS);
//					}
//
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					continue;
//				}
//			}
//
//		}
//	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				// 图片ID
				String imgId = null;
				switch (msg.what) {
					case EVENT_LOCAL_ICON_EXIT : {
						if (msg.obj != null && msg.obj instanceof Bitmap) {
							Bitmap bitmap = (Bitmap) msg.obj;
							Bundle bundle = msg.getData();
							if (bundle != null) {
								imgId = bundle.getString(IMAGE_ID_KEY);
								if (imgId != null && mBitmapHashMap != null) {
									SimpleImage simpleImage = mBitmapHashMap.get(imgId);
									if (simpleImage != null) {
										simpleImage.setBitmap(bitmap);
										simpleImage.broadCast(EVENT_LOCAL_ICON_EXIT, 0,
												simpleImage.getBitmap(), imgId);
									}
								}
							}
						}
					}
						break;
					case EVENT_LOCAL_ICON_NOT_EXIT : {
						Bundle bundle = msg.getData();
						imgId = bundle.getString(IMAGE_ID_KEY);
						if (GoStorePhoneStateUtil.isNetWorkAvailable(mContext)) {
							// 如果网络可用
							if (imgId != null) {
								// boolean isSynGet =
								// bundle.getBoolean(IS_SYN_GET_KEY, true);
								// //从网络获取，获取成功后，通知图片加载完成
								// if (isSynGet)
								// {
								// getImageDataFromNetWithoutWait(imgId);
								// } else {
								// getImageDataFromNetWithWait(imgId);
								// }
								getImageDataFromNetWithoutWait(imgId);
							}
						} else {
							// 如果网络不可用
							if (mBitmapHashMap != null && imgId != null) {
								SimpleImage simpleImage = mBitmapHashMap.get(imgId);
								if (simpleImage != null) {
									simpleImage.broadCast(EVENT_NETWORK_EXCEPTION, 0, null, imgId);
								}
							}
						}
					}
						break;
					case EVENT_NETWORK_ICON_CHANGE : {
						if (msg.obj != null) {
							SimpleImage image = (SimpleImage) msg.obj;
							if (image != null) {
								imgId = image.mImgId;
								image.broadCast(EVENT_NETWORK_ICON_CHANGE, 0, image.getBitmap(),
										imgId);
							}
						}
					}
						break;
					case EVENT_NETWORK_EXCEPTION : {
						if (msg.obj != null) {
							SimpleImage image = (SimpleImage) msg.obj;
							if (image != null) {
								imgId = image.mImgId;
								image.broadCast(EVENT_NETWORK_EXCEPTION, 0, null, imgId);
							}
						}
					}
						break;
					default :

						break;
				}
				if (imgId != null) {
					if (mLoadingImgRunableHashMap != null) {
						mLoadingImgRunableHashMap.remove(imgId);
					}
					if (mLoadingImgIdArrayList != null) {
						mLoadingImgIdArrayList.remove(imgId);
					}
				}
			}
		};
	}

	/**
	 * 释放指定的图片缓存 获取指定ID的SimpleImage,然后反注册observer
	 * 如果该指定ID的SimpleImage已经没有observer,则释放该图片.也就是说图片不一定释放掉掉
	 * 
	 * @author huyong
	 * @param observer
	 * @param imgId
	 */
	public void releaseBitmap(BroadCasterObserver observer, String imgId) {
		if (imgId == null || mBitmapHashMap == null) {
			return;
		}
		SimpleImage image = mBitmapHashMap.get(imgId);
		if (image != null) {
			image.unRegisterObserver(observer);
			ArrayList<BroadCasterObserver> observers = image.getObserver();
			if (observers != null && observers.size() <= 0) {
				// 无监听者，可以回收该图片.
				mBitmapHashMap.remove(imgId);
				image.recyleBitmap();
				image = null;
			}
		}

	}

	/**
	 * 释放指定的图片缓存 获取指定ID的SimpleImage,
	 * 如果该指定ID的SimpleImage已经没有observer,则释放该图片.也就是说图片不一定释放掉掉
	 * 
	 * @param imgId
	 */
	public void releaseBitmap(String imgId) {
		if (imgId == null || mBitmapHashMap == null) {
			return;
		}
		SimpleImage image = mBitmapHashMap.get(imgId);
		if (image != null) {
			ArrayList<BroadCasterObserver> observers = image.getObserver();
			if (observers != null && observers.size() <= 0) {
				// 无监听者，可以回收该图片.
				mBitmapHashMap.remove(imgId);
				image.recyleBitmap();
				image = null;
			}
		}
	}

	/**
	 * 反注册图片的监听器
	 * 
	 * @author huyong
	 * @param observer
	 *            要反注册的监听器
	 * @param imgId
	 *            图片ID
	 */
	public void unRegisterObserverFromSimpleImage(BroadCasterObserver observer, String imgId) {
		if (imgId == null || mBitmapHashMap == null) {
			return;
		}
		SimpleImage image = mBitmapHashMap.get(imgId);
		if (image != null) {
			image.unRegisterObserver(observer);
		}
	}

	/**
	 * 
	 * @author huyong
	 * @param observer
	 * @param imgId
	 * @param isSynGet
	 *            :是否同步获取
	 * @return
	 */
	private Bitmap getBitmap(BroadCasterObserver observer, final String imgId,
			final boolean isSynGet) {
		if (imgId == null || mBitmapHashMap == null) {
			return null;
		}

		Bitmap bmp = null;
		// 先从内存里面取
		SimpleImage image = mBitmapHashMap.get(imgId);
		if (image == null) {
			image = new SimpleImage();
			image.mImgId = imgId;
			mBitmapHashMap.put(image.mImgId, image);
		}
		image.registerObserver(observer);
		// 图片先从内存里面取
		bmp = image.getBitmap();

		if (bmp == null) {
			// 如果内存取到的图片为空
			if (mLoadingImgRunableHashMap != null && !mLoadingImgRunableHashMap.containsKey(imgId)
					&& !mLoadingImgIdArrayList.contains(imgId)) {
				mLoadingImgRunableHashMap.put(imgId, new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Message message = new Message();
							Bundle bundle = new Bundle();
							bundle.putString(IMAGE_ID_KEY, imgId);
							Bitmap bitmap = null;
							if (imageCheck(imgId)) {
								// 如果本地验证通过
								// 去本地图片
								// bitmap =
								// BitmapFactory.decodeFile(LauncherEnv.Path.GOSTORE_ICON_PATH
								// + imgId);
								byte[] bs = FileUtil
										.getByteFromSDFile(LauncherEnv.Path.GOSTORE_ICON_PATH
												+ imgId);
								Options options = new Options();
								options.inPurgeable = true;
								options.inPreferredConfig = Bitmap.Config.RGB_565;
								bitmap = BitmapFactory.decodeByteArray(bs, 0, bs.length, options);
								if (bitmap != null) {
									// 如果本地图片取得到
									message.what = EVENT_LOCAL_ICON_EXIT;
									message.obj = bitmap;
								}
							}
							if (bitmap == null) {
								// 如果本地图片验证不通过或者本地图片取不到
								message.what = EVENT_LOCAL_ICON_NOT_EXIT;
								bundle.putBoolean(IS_SYN_GET_KEY, isSynGet);
							}
							message.setData(bundle);
							if (mHandler != null) {
								mHandler.sendMessage(message);
							}
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				});
				ThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME).execute(
						mLoadingImgRunableHashMap.get(imgId));
			}
		}

		return bmp;
	}

	/**
	 * 取消加载图片的方法
	 * 
	 * @param imgId
	 */
	public void cancelLoadImg(String imgId) {
		if (imgId != null && !TextUtils.isEmpty(imgId) && mLoadingImgRunableHashMap != null) {
			Runnable runnable = mLoadingImgRunableHashMap.remove(imgId);
			if (runnable != null) {
				ThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME).cancel(runnable);
			}
		}
	}

	/**
	 * 若本地不存在，则默认异步从网络获取数据，首先加入请求队列，可能会需要等待请求数量达到一定量开始请求。
	 * 
	 * @author huyong
	 * @param observer
	 * @param imgId
	 * @return
	 */
	public final Bitmap getBitmap(BroadCasterObserver observer, String imgId) {
		return getBitmap(observer, imgId, false);
	}

	/**
	 * 若本地不存在，则默认异步从网络获取数据，不加入请求队列。
	 * 
	 * @author huyong
	 * @param observer
	 * @param imgId
	 * @return
	 */
	public final Bitmap getBitmapWithoutWait(BroadCasterObserver observer, String imgId) {
		return getBitmap(observer, imgId, true);
	}

	/**
	 * 立即开始进行网络获取图片数据，不缓存到图片队列。
	 * 
	 * @author huyong
	 * @param url
	 * @param imgId
	 */
	private void getImageDataFromNetWithoutWait(final String imgId) {
		if (imgId == null) {
			return;
		}
		ArrayList<String> singleImgIdList = new ArrayList<String>(1);
		singleImgIdList.add(imgId);
		getImageDataFromNet(singleImgIdList);
		singleImgIdList = null;
	}

	/**
	 * 批量获取网络图片数据，可能需要等待。
	 * 
	 * @author huyong
	 * @param imgId
	 */
	// private synchronized void getImageDataFromNetWithWait(final String imgId)
	// {
	// if (imgId != null)
	// {
	// //添加到任务队列中，imgId为空，则直接使用mNetImgIdList中的请求进行
	// if(mNetImgIdList != null){
	// mNetImgIdList.add(imgId);
	// }
	// if (mNetImgIdList != null && mNetImgIdList.size() < MAX_IMAGE_TMP_COUNT)
	// {
	// //TODO:循环线程进行检测
	// if (mLooperThread == null)
	// {
	// mLooperThread = new LooperThread("LooperThread");
	// mLooperThread.setPriority(Thread.NORM_PRIORITY - 2);
	// mLooperThread.start();
	// }
	// synchronized (mLooperThread)
	// {
	// mLooperThread.notify();
	// }
	// return;
	// }
	// }
	//
	// int size = 0;
	// if(mNetImgIdList != null){
	// size = mNetImgIdList.size();
	// }
	// if (size <= 0)
	// {
	// //已无待请求的图片
	// return;
	// }
	//
	// //网络批量获取图片
	// ArrayList<String> cloneImgIdArrayList = null;
	// if(mNetImgIdList != null){
	// cloneImgIdArrayList = (ArrayList<String>)mNetImgIdList.clone();
	// mNetImgIdList.clear();
	// }
	// getImageDataFromNet(cloneImgIdArrayList);
	// }

	/**
	 * 从网络获取数据接口
	 * 
	 * @author huyong
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

			THttpRequest request = new THttpRequest(GoStorePublicDefine.URL_HOST3, postData,
					new IConnectListener() {

						@Override
						public void onStart(THttpRequest request) {
						}

						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
								ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) response
										.getResponse();
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
														// 如果下发的图片数据不为空，而且读取到的数据长度跟下发的数据长度一致的话，就进行保存，并加载到内存
														// 如果读到的数据跟下发的长度不一致，就认为网络读取图片数据时出错，不进行保存，也不加载到内存
														if (GoStorePhoneStateUtil.isSDCardAccess()) {
															// 保存图片到sd卡中
															if (FileUtil
																	.saveByteToCommonIconSDFile(
																			imgData.mImgData,
																			imgData.mImgId)) {
																// 如果图片保存成功
																// 设置当前时间为文件创建时间
																setImgFileCreatedTime(imgData.mImgId);
																// 记录图片数据长度
																saveImgSizeRecord(imgData.mImgId,
																		imgData.mDataLength);
															}
														}
														// 为图片增加Options从而减少占用内存
														Options options = new Options();
														options.inPurgeable = true;
														options.inPreferredConfig = Bitmap.Config.RGB_565;
														// 加载文件到内存
														Bitmap bmp = BitmapFactory.decodeByteArray(
																imgData.mImgData, 0,
																imgData.mDataLength, options);
														// 清除掉内存中的图片数据值
														imgData.mImgData = null;

														// 下载完成后
														if (mBitmapHashMap != null
																&& mHandler != null) {
															SimpleImage image = mBitmapHashMap
																	.get(imgData.mImgId);
															if (image != null) {
																image.setBitmap(bmp);

																// TODO:通知异步更新
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
							}
						}

						@Override
						public void onException(THttpRequest request, int reason) {
							StatisticsData.saveHttpExceptionDate(mContext, request, reason);
							if (mBitmapHashMap != null && imgIdList != null) {
								// 逐个通知各图片，网络获取异常。
								for (String imgId : imgIdList) {
									SimpleImage image = mBitmapHashMap.get(imgId);
									if (image != null && mHandler != null) {
										Message message = mHandler.obtainMessage();
										message.what = EVENT_NETWORK_EXCEPTION;
										message.obj = image;
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

			SimpleHttpAdapter httpAdapter = SimpleHttpAdapter.getInstance(mContext);
			if (httpAdapter != null) {
				httpAdapter.addTask(request);
				if (mLoadingImgIdArrayList != null) {
					mLoadingImgIdArrayList.addAll(imgIdList);
				}
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
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-9-27]
	 */
	class SimpleImage extends BroadCaster {
		public String mImgId = null;
		private WeakReference<Bitmap> mBmpReference = null;

		public SimpleImage() {

		}

		public SimpleImage(String imgId, Bitmap bitmap) {
			mImgId = imgId;
			mBmpReference = new WeakReference<Bitmap>(bitmap);
		}

		public void setBitmap(Bitmap bitmap) {
			mBmpReference = new WeakReference<Bitmap>(bitmap);
		}

		public final Bitmap getBitmap() {
			Bitmap bmp = null;
			if (mBmpReference != null) {
				bmp = mBmpReference.get();
			}
			return bmp;
		}

		/**
		 * 回收图片
		 * 
		 * @author huyong
		 */
		public void recyleBitmap() {
			mBmpReference = null;
			mImgId = null;
		}
	}

	/**
	 * 验证图片是否过期
	 * 
	 * @author huyong
	 * @param imgId
	 * @return
	 */
	private boolean isImgExpired(String imgId) {
		boolean result = false;
		if (imgId != null && !"".equals(imgId.trim())) {
			String imgPath = LauncherEnv.Path.GOSTORE_ICON_PATH + imgId;
			try {

				Date curDate = new Date();
				long curTime = curDate.getTime(); // 当前时间值

				File file = new File(imgPath);
				long fileTime = file.lastModified(); // 文件创建时间值

				if ((curTime - fileTime) > IMG_EXPIRED_TIME) {
					// 文件已过期
					result = true;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 图片验证的方法 验证图片是否在本地有缓存 如果有缓存，缓存是否已经过期，缓存文件是否完整
	 * 如果本地有缓存，而且缓存图片已经过期或者不完整，将会删除缓存图片
	 * 
	 * @author huyong
	 * @param imgId
	 * @return false，本地没有图片缓存，或者图片缓存已经过期，或者缓存数据不完整。true，有缓存并且验证通过。
	 */
	private boolean imageCheck(String imgId) {
		boolean result = false;
		if (imgId != null && !"".equals(imgId.trim())) {
			String imgPath = LauncherEnv.Path.GOSTORE_ICON_PATH + imgId;
			File file = new File(imgPath);
			if (file.exists()) {
				// 如果图片文件存在
				if (!isImgExpired(imgId) && checkImgSize(imgId, file)) {
					// 如果文件没有过期,并且数据完整
					result = true;
				} else {
					// 如果文件过期，或者数据不完整
					if (file.delete()) {
						// 删除该文件
						// 清除文件大小记录
						removeImgSizeRecord(imgId);
					}
				}
			}
		}
		return result;
	}

	/**
	 * 设置当前时间为文件创建时间。
	 * 
	 * @author huyong
	 * @param imgId
	 */
	private void setImgFileCreatedTime(String imgId) {
		if (imgId != null && !"".equals(imgId.trim())) {
			String imgPath = LauncherEnv.Path.GOSTORE_ICON_PATH + imgId;
			File file = new File(imgPath);
			if (file == null || !file.exists()) {
				// 文件不存在，则直接返回.
				file = null;
				return;
			}
			try {
				Date curDate = new Date();
				long curTime = curDate.getTime();
				file.setLastModified(curTime);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 刷新图片的方法
	 * 
	 * @param newBitmap
	 * @param imgId
	 */
	public void refreshBitmap(final Bitmap newBitmap, final String imgId) {
		if (mBitmapHashMap != null && newBitmap != null && imgId != null) {
			final SimpleImage image = mBitmapHashMap.get(imgId);
			if (image != null) {
				Bitmap oldBitmap = image.getBitmap();
				if (oldBitmap != null && oldBitmap.equals(newBitmap)) {
					// 图片相同，则不进行回收。
					return;
				}
				image.setBitmap(newBitmap);
				// 释放内存中原来的图片
				if (oldBitmap != null) {
					int size = 0;
					if (image.getObserver() != null) {
						size = image.getObserver().size();
					}
					if (size <= 1) {
						oldBitmap.recycle();
					}
				}
				oldBitmap = null;

				if (GoStorePhoneStateUtil.isSDCardAccess()) {
					final String filePathName = LauncherEnv.Path.GOSTORE_ICON_PATH + imgId;
					ThreadPoolManager.getInstance(THREAD_POOL_MANAGER_NAME).execute(new Runnable() {
						@Override
						public void run() {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							Bitmap bitmap = image.getBitmap();
							if (bitmap != null && !bitmap.isRecycled()) {
								bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
								if (FileUtil.saveByteToSDFile(baos.toByteArray(), filePathName)) {
									// 如果刷新数据成功
									setImgFileCreatedTime(imgId);
									// 重新设置图片数据长度
									File file = new File(filePathName);
									if (file.exists()) {
										saveImgSizeRecord(imgId, file.length());
									}
								}
							}
						}
					});
				}
			}
		}
	}

	/**
	 * 根据记录检查文件数据是否完整的方法
	 * 
	 * @param imgId
	 */
	private boolean checkImgSize(String imgId, File imgFile) {
		boolean result = false;
		if (mContext != null && imgId != null && !"".equals(imgId.trim()) && imgFile != null
				&& imgFile.exists()) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					GoStorePublicDefine.CACHE_IMAGE_SIZE_FILE, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				long recordLength = sharedPreferences.getLong(imgId, -1);
				if (recordLength > 0 && recordLength == imgFile.length()) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * 保存图片文件的大小记录
	 * 
	 * @param imgId
	 */
	private synchronized void saveImgSizeRecord(String imgId, long imgDataLength) {
		if (mContext != null && imgId != null && !"".equals(imgId.trim()) && imgDataLength > 0) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					GoStorePublicDefine.CACHE_IMAGE_SIZE_FILE, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.putLong(imgId, imgDataLength);
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 清理图片文件的大小记录
	 * 
	 * @param imgId
	 */
	private synchronized void removeImgSizeRecord(String imgId) {
		if (mContext != null && imgId != null && !"".equals(imgId.trim())) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					GoStorePublicDefine.CACHE_IMAGE_SIZE_FILE, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.remove(imgId);
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 清理无效图片的线程
	 */
	private class ClearInvalidImgThread extends Thread {
		@Override
		public void run() {
			File file = new File(LauncherEnv.Path.GOSTORE_ICON_PATH);
			if (file.exists() && file.isDirectory()) {
				File[] imgFiles = file.listFiles();
				if (imgFiles != null && imgFiles.length > 0) {
					Date curDate = new Date();
					long curTime = curDate.getTime(); // 当前时间值
					long fileTime = Long.MIN_VALUE; // 文件创建时间值
					for (File imgFile : imgFiles) {
						if (imgFile != null && imgFile.exists() && imgFile.isFile()) {
							fileTime = imgFile.lastModified();
							if ((curTime - fileTime) > IMG_EXPIRED_TIME) {
								// 文件已过期
								imgFile.delete();
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 回收方法
	 */
	private void recycle() {
		if (mBitmapHashMap != null) {
			Set<String> keySet = mBitmapHashMap.keySet();
			SimpleImage simpleImage = null;
			for (String key : keySet) {
				simpleImage = mBitmapHashMap.get(key);
				if (simpleImage != null) {
					simpleImage.clearAllObserver();
					simpleImage.recyleBitmap();
				}
			}
			mBitmapHashMap.clear();
			mBitmapHashMap = null;
		}

		// if(mNetImgIdList != null){
		// mNetImgIdList.clear();
		// mNetImgIdList = null;
		// }
		if (mLoadingImgRunableHashMap != null) {
			mLoadingImgRunableHashMap.clear();
			mLoadingImgRunableHashMap = null;
		}
		if (mLoadingImgIdArrayList != null) {
			mLoadingImgIdArrayList.clear();
			mLoadingImgIdArrayList = null;
		}
		mHandler = null;
//		mLooperThread = null;
		mContext = null;
	}

	/**
	 * 销毁方法
	 */
	public static synchronized void destory() {
		if (sSelf != null) {
			sSelf.recycle();
			sSelf = null;
		}
	}

	/**
	 * 清理内存中的缓存图片
	 */
	public void clearCache() {
		if (mBitmapHashMap != null) {
			mBitmapHashMap.clear();
		}
	}

	public int getCacheSize() {
		if (mBitmapHashMap != null) {
			return mBitmapHashMap.size();
		}
		return 0;
	}
}
