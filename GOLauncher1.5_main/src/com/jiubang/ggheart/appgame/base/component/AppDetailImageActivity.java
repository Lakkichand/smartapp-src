/*
 * 文 件 名:  AppDetailImageActivity.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-1
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.apps.gowidget.gostore.ImageManager;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.components.IndicatorView;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-1]
 */
public class AppDetailImageActivity extends Activity implements ScreenScrollerListener {

	// private ImageView mPreImageView = null; //前一张按钮
	// private ImageView mNextImageView = null; //后一张按钮
	// private TextView mInforTextView = null; //图片切换信息文本
	private final int mMsgLoadInit = 1;
	private final int mMsgLoadFinish = 2;

	private AppDetailImageActivity mInstance = null;
	private boolean mIsShowNavigation = false; // 是否显示导向按钮
	private ArrayList<String> mScanImageUrlsArrayList = null; // 浏览图片的Url集合
	private int mCurImageIndex = 0; // 当前显示的图片下标
	private int mScanImageCount = 0; // 要显示的图片数目
	private RelativeLayout mBottomRelativeLayout = null;
	private IndicatorView mIndicatorView = null;

	private final int mTimerDelay = 2500; // 定时间隔
//	private final String NAVIGATION_TIME = "navigation_time";
	private BroadcastReceiver mBroadcastReceiver = null;
	private AlarmManager mAlarmManager = null;
	private PendingIntent mLastTimeIntent = null;

	private RelativeLayout mNoDataTipRelativeLayout; // 没有结果提示
	private boolean mHasAlreadyLoad = false; // 至少已经load了一张图片的标志

	private LinearLayout mProgressLinearLayout = null; // 图片加载进度条
	private BroadCasterObserver mImgBroadCasterObserver = null;

	private ScrollerViewGroup mScrollerViewGroup = null; // 图片滚动器
	private ArrayList<ImageView> mImageViews = null; // 图片ImageView的集合
	private ImageView mCurrentImageView = null; // 当前显示的ImageView
	private OnTouchListener mImageViewTouchListener = null;
	private ArrayList<String> mLoadingImgIds = null; // 正在加载图片的ID
//	private String mHost = "";

	private AsyncImageManager mImgManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 初始化计时器
		mAlarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

		mImgManager = AsyncImageManager.getInstance();
		mInstance = this;
		// 正在加载图片的ID
		mLoadingImgIds = new ArrayList<String>();
		// 从Intent中获取要显示的图片ID的集合
		getScanImageIdsFromIntent();
		// 初始化广播接收器
		initBroadcastReceiver();
		// 初始化加载图片的监听器
		initImgBroadCasterObserver();
		// 初始化图片点击处理
		// initImgTouchListener();
		// 初始化View
		initView();
	}

	/**
	 * 初始化图片点击处理的方法
	 */
	private void initImgTouchListener() {
		// TODO Auto-generated method stub
		mImageViewTouchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				int action = event.getAction();
				if (action == MotionEvent.ACTION_UP) {
					if (mIsShowNavigation) {
						hideNavigation();
					} else {
						showNavigation();
					}
				}
				return true;
			}
		};
	}

	/**
	 * 初始化View的方法
	 */
	private void initView() {
		// 去掉title部分
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 去掉信息栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.themestore_imagescanactivity);
		initProgress();
		// 初始化没有结果提示
		initNoDataTipRelativeLayout();
		// 初始化图片滚动界面
		initImageViews();
		// 默认首次进入，则隐藏导向。
		// hideNavigation();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// 注册广播接收器
		if (mBroadcastReceiver != null) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(ICustomAction.ACTION_NAVIGATION_TIME);
			registerReceiver(mBroadcastReceiver, intentFilter);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 反注册广播接收器
		if (mBroadcastReceiver != null) {
			unregisterReceiver(mBroadcastReceiver);
		}
	}

	/**
	 * 重置计时器的方法
	 */
	private void resetNavigationTime() {
		if (mAlarmManager != null) {
			if (mLastTimeIntent != null) {
				// 取消原来的计时
				mAlarmManager.cancel(mLastTimeIntent);
			}
			// 重新计时
			Intent intent = new Intent();
			intent.setAction(ICustomAction.ACTION_NAVIGATION_TIME);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + mTimerDelay,
					pendingIntent);
			mLastTimeIntent = pendingIntent;
		}
	}

	/**
	 * 初始化广播接收器的方法
	 */
	private void initBroadcastReceiver() {
		// TODO Auto-generated method stub
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				hideNavigation();
			}
		};
	}

	/**
	 * 初始化进度条的方法
	 */
	private void initProgress() {
		mProgressLinearLayout = (LinearLayout) findViewById(R.id.loadImageProgressBar);
		if (mProgressLinearLayout != null) {
			ProgressBar progressBar = (ProgressBar) mProgressLinearLayout
					.findViewById(R.id.themestore_btmprogress);
			progressBar.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
			Drawable drawable = this.getResources().getDrawable(R.drawable.go_progress_green);
			progressBar.setIndeterminateDrawable(drawable);

		}
	}

	/**
	 * 初始化没有结果提示文本的方法
	 */
	private void initNoDataTipRelativeLayout() {
		// TODO Auto-generated method stub
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		mNoDataTipRelativeLayout = (RelativeLayout) layoutInflater.inflate(
				R.layout.themestore_nodata_tip_full, null);
		mNoDataTipRelativeLayout.setBackgroundColor(Color.WHITE);
	}

	/**
	 * 隐藏导航键
	 * 
	 * @author huyong
	 */
	private void hideNavigation() {
		mIsShowNavigation = false;
		// if (mPreImageView != null)
		// {
		// mPreImageView.setVisibility(View.GONE);
		// }
		// if (mNextImageView != null)
		// {
		// mNextImageView.setVisibility(View.GONE);
		// }
		if (mBottomRelativeLayout != null) {
			mBottomRelativeLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 显示导航键
	 * 
	 * @author huyong
	 */
	private void showNavigation() {
		mIsShowNavigation = true;
		// if (mPreImageView != null)
		// {
		// mPreImageView.setVisibility(View.VISIBLE);
		// }
		// if (mNextImageView != null)
		// {
		// mNextImageView.setVisibility(View.VISIBLE);
		// }
		if (mBottomRelativeLayout != null) {
			mBottomRelativeLayout.setVisibility(View.VISIBLE);
		}
		resetNavigationTime();
	}

	/**
	 * 从Intent中获取要显示图片ID集合的方法
	 */
	private void getScanImageIdsFromIntent() {
		Intent intent = getIntent();
		if (intent != null) {
			mScanImageUrlsArrayList = (ArrayList<String>) intent.getStringArrayListExtra(GoStorePublicDefine.SCAN_IMAGE_IDS_KEY);
			if (mScanImageUrlsArrayList != null && mScanImageUrlsArrayList.size() > 0) {
				mScanImageCount = mScanImageUrlsArrayList.size();
			}
			// 当前图片索引值
			mCurImageIndex = intent.getIntExtra(GoStorePublicDefine.SCAN_IMAGE_CUR_INDEX_KEY, 0);
//			mHost = intent.getStringExtra("host");
		}
	}

	/**
	 * 切换至前一图片
	 * 
	 * @author huyong
	 */
	private void changeToPreImg() {
		mCurImageIndex--;
		if (mCurImageIndex < 0) {
			mCurImageIndex = 0;
			// 显示导航栏
			showNavigation();
			return;
		}
		mCurImageIndex = (mCurImageIndex + mScanImageCount) % mScanImageCount;
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.gotoViewByIndex(mCurImageIndex);
		}
	}

	/**
	 * 切换至后一图片
	 * 
	 * @author huyong
	 */
	private void changeToNextImg() {
		mCurImageIndex++;
		if (mCurImageIndex >= mScanImageCount) {
			mCurImageIndex = mScanImageCount - 1;
			// 显示导航栏
			showNavigation();
			return;
		}
		mCurImageIndex = mCurImageIndex % mScanImageCount;
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.gotoViewByIndex(mCurImageIndex);
		}
	}

	/**
	 * 初始化界面的方法
	 */
	private void initImageViews() {
		// 图片切换器
		LinearLayout linearLayout = (LinearLayout) this.findViewById(R.id.scrollLinearLayout);
		mScrollerViewGroup = new ScrollerViewGroup(this, this);
		if (linearLayout != null && mScrollerViewGroup != null) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			linearLayout.addView(mScrollerViewGroup, params);
			if (mScanImageUrlsArrayList != null && mScanImageUrlsArrayList.size() > 0) {
				int size = mScanImageUrlsArrayList.size();
				mScrollerViewGroup.setScreenCount(size);
				mImageViews = new ArrayList<ImageView>();
				ImageView imageView = null;
				for (int i = 0; i < size; i++) {
					imageView = new ImageView(this);
					imageView.setOnTouchListener(mImageViewTouchListener);
					mImageViews.add(imageView);
					mScrollerViewGroup.addScreenView(imageView);
				}
				if (mCurImageIndex >= 0 && mCurImageIndex < size) {
					mScrollerViewGroup.gotoViewByIndex(mCurImageIndex);
					// 如果是index为0，ScrollView不会刷新，必须手动刷新
					refreshScrollView();
				}
			}
		}
		// //前一张按钮
		// mPreImageView = (ImageView)this.findViewById(R.id.preImageView);
		// mPreImageView.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// int action = event.getAction();
		// if(action == MotionEvent.ACTION_DOWN){
		// mPreImageView.setBackgroundResource(R.drawable.themestore_imagescan_preview_button_light);
		// } else if(action == MotionEvent.ACTION_UP)
		// {
		// mPreImageView.setBackgroundResource(R.drawable.themestore_imagescan_preview_button);
		//
		// changeToPreImg();
		//
		// resetNavigationTime();
		// }
		// return true;
		// }
		// });
		//
		// //后一张按钮
		// mNextImageView = (ImageView)this.findViewById(R.id.nextImageView);
		// mNextImageView.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// int action = event.getAction();
		// if(action == MotionEvent.ACTION_DOWN){
		// mNextImageView.setBackgroundResource(R.drawable.themestore_imagescan_next_button_light);
		// }else if(action == MotionEvent.ACTION_UP)
		// {
		// mNextImageView.setBackgroundResource(R.drawable.themestore_imagescan_next_button);
		//
		// changeToNextImg();
		//
		// resetNavigationTime();
		// }
		// return true;
		// }
		// });

		// 图片切换信息文本
		mIndicatorView = (IndicatorView) findViewById(R.id.indicator);
		// mInforTextView =
		// (TextView)this.findViewById(R.id.imageScanInforTextView);
		// mInforTextView.setText((mCurImageIndex+1)+"/"+mScanImageCount);
		mIndicatorView.setSize(mScanImageCount);
		mIndicatorView.setFocusItem(mCurImageIndex);
		mBottomRelativeLayout = (RelativeLayout) this
				.findViewById(R.id.imageScanBottomRelativeLayout);
	}

	private void initImgBroadCasterObserver() {
		mImgBroadCasterObserver = new BroadCasterObserver() {
			// 注册监听器
			// 如果本地获取不到图片，需要从网络下载
			// 图片回来后进行处理
			@Override
			public void onBCChange(int msgId, int param, Object object, Object object2) {
				if (mCurrentImageView == null) {
					// 本界面已被回收
					return;
				}
				switch (msgId) {
					case ImageManager.EVENT_NETWORK_ICON_CHANGE : {
						if (object == null || object2 == null) {
							break;
						}
						// TODO:优化点：保存缩放后的图片内存回收
						Bitmap bitmap = (Bitmap) object;
						String imgId = String.valueOf(object2);
						handlerImageChange(bitmap, imgId);
					}
						break;
					case ImageManager.EVENT_NETWORK_EXCEPTION : {
						String imgUrl = null;
						if (object2 != null) {
							imgUrl = String.valueOf(object2);
						}
						if (mHasAlreadyLoad) {
							if (imgUrl != null) {
								if (mScanImageUrlsArrayList != null
										&& mScanImageUrlsArrayList.contains(imgUrl)) {
									int index = mScanImageUrlsArrayList.indexOf(imgUrl);
									if (mImageViews != null && index < mImageViews.size()) {
										ImageView imageView = mImageViews.get(index);
										if (imageView != null) {
											imageView
													.setImageResource(R.drawable.themestore_nodata_all);
											imageView.setScaleType(ScaleType.CENTER);
										}
									}
								}
							}
						} else {
							showNoDataTip();
						}

						if (mLoadingImgIds != null) {
							mLoadingImgIds.remove(imgUrl);
						}

						dismissProgress();
						Toast.makeText(AppDetailImageActivity.this, R.string.http_exception,
								Toast.LENGTH_SHORT).show();
					}
						break;
					case ImageManager.EVENT_LOCAL_ICON_EXIT : {

						if (object == null || object2 == null) {
							break;
						}
						// TODO:优化点：保存缩放后的图片内存回收
						Bitmap bitmap = (Bitmap) object;
						String imgId = String.valueOf(object2);
						handlerImageChange(bitmap, imgId);
					}
						break;
					default :
						break;
				}
			}
		};
	}

	/**
	 * 处理图片改变的方法
	 * 
	 * @param bitmap
	 */
	private void handlerImageChange(Bitmap bitmap, String imgUrl) {
		if (bitmap == null || imgUrl == null) {
			return;
		}
		// 对图片进行匹配机型的缩放
		Bitmap tmpCurBitmap = GoStoreDisplayUtil.scaleBitmapToScreen(getApplicationContext(),
				bitmap);
		try {
			BitmapDrawable bitmapDrawable = new BitmapDrawable(tmpCurBitmap);
			if (!bitmap.equals(tmpCurBitmap)) {
				// 更新原图
				ImageManager imageManager = ImageManager.getInstance();
				if (imageManager != null) {
					// 更新新图
					imageManager.refreshBitmap(tmpCurBitmap, imgUrl);
				}
			}
			bitmapDrawable.setTargetDensity(AppDetailImageActivity.this.getResources()
					.getDisplayMetrics());
			// 设置图片切换器的显示图片
			if (mScanImageUrlsArrayList != null && mScanImageUrlsArrayList.contains(imgUrl)) {
				int index = mScanImageUrlsArrayList.indexOf(imgUrl);
				if (mImageViews != null && index < mImageViews.size()) {
					ImageView imageView = mImageViews.get(index);
					if (imageView != null) {
						imageView.setImageDrawable(bitmapDrawable);
						imageView.setScaleType(ScaleType.FIT_XY);
					}
				}
			}
			bitmap = null;
			tmpCurBitmap = null;
			bitmapDrawable = null;
			mHasAlreadyLoad = true;

		} catch (Throwable e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (mLoadingImgIds != null) {
			mLoadingImgIds.remove(imgUrl);
		}
		// 隐藏进度条
		dismissProgress();
	}

	/**
	 * 使用AsyncImageManage异步获取图片
	 * 
	 * @param curImageIndex
	 *            当前要显示的图片ID的下标
	 * @return
	 */
	private void loadImage(final ImageView imgView, int curImageIndex) {

		if (mScanImageUrlsArrayList != null && mScanImageUrlsArrayList.size() > 0
				&& curImageIndex < mScanImageUrlsArrayList.size()) {
			final String imgUrl = mScanImageUrlsArrayList.get(curImageIndex);
			if (mLoadingImgIds != null) {
				if (mLoadingImgIds.contains(imgUrl)) {
					// 如果已经正在加载了，就返回
					return;
				} else {
					mLoadingImgIds.add(imgUrl);
				}
			}
			if (imgView.getDrawable() == null) {
				mHandler.sendEmptyMessage(mMsgLoadInit);
			}
			if (TextUtils.isEmpty(imgUrl)) {
				return;
			}
			String imgName = String.valueOf(imgUrl.hashCode());
			imgView.setTag(imgUrl);
			Bitmap bm = mImgManager.loadImage(LauncherEnv.Path.APP_MANAGER_ICON_PATH, imgName, imgUrl, true, true, null,
					new AsyncImageLoadedCallBack() {
						@Override
						public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
							// TODO Auto-generated method stub
							Message msg = new Message();
							msg.what = mMsgLoadFinish;
							Bundle bundle = new Bundle();
							bundle.putString("url", imgUrl);
							msg.setData(bundle);
							msg.obj = imageBitmap;
							mHandler.sendMessage(msg);
						}
					});
			if (bm != null) {
				Message msg = new Message();
				msg.what = mMsgLoadFinish;
				Bundle bundle = new Bundle();
				bundle.putString("url", imgUrl);
				msg.setData(bundle);
				msg.obj = bm;
				mHandler.sendMessage(msg);
			} else {
				return;
			}
		}
	}

	/**
	 * 显示没有数据提示
	 */
	private void showNoDataTip() {
		if (mNoDataTipRelativeLayout != null) {
			this.setContentView(mNoDataTipRelativeLayout);
		}
	}

	/**
	 * 显示进度条
	 */
	private void showProgress() {
		if (mProgressLinearLayout != null) {
			mProgressLinearLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 隐藏进度条
	 */
	private void dismissProgress() {
		if (mProgressLinearLayout != null) {
			mProgressLinearLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 资源回收的方法
	 */
	private void recycle() {
		if (mCurrentImageView != null) {
			mCurrentImageView.setImageDrawable(null);
			mCurrentImageView = null;
		}
		mIndicatorView = null;
		// mInforTextView = null;
		// if (mNextImageView != null)
		// {
		// mNextImageView.setOnTouchListener(null);
		// mNextImageView.setBackgroundDrawable(null);
		// mNextImageView = null;
		// }
		// if (mPreImageView != null)
		// {
		// mPreImageView.setOnTouchListener(null);
		// mPreImageView.setBackgroundDrawable(null);
		// mPreImageView = null;
		// }
		if (mScanImageUrlsArrayList != null) {
			for (String bitmapId : mScanImageUrlsArrayList) {
				// 释放原图
				ImageManager.getInstance().releaseBitmap(mImgBroadCasterObserver, bitmapId);
				bitmapId = null;
			}
			mScanImageUrlsArrayList.clear();
			mScanImageUrlsArrayList = null;
		}
		mBottomRelativeLayout = null;
		mNoDataTipRelativeLayout = null;
		mProgressLinearLayout = null;
		mBroadcastReceiver = null;
		mAlarmManager = null;
		mLastTimeIntent = null;
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.removeAllViews();
			mScrollerViewGroup.destory();
			mScrollerViewGroup = null;
		}
		if (mImageViews != null) {
			for (ImageView imageView : mImageViews) {
				if (imageView != null) {
					imageView.setOnTouchListener(null);
					imageView.setImageDrawable(null);
				}
			}
			mImageViews.clear();
			mImageViews = null;
		}
		if (mLoadingImgIds != null) {
			mLoadingImgIds.clear();
			mLoadingImgIds = null;
		}
		mImageViewTouchListener = null;

		if (mIndicatorView != null) {
			mIndicatorView.recycle();
			mIndicatorView = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		recycle();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	// ///////////////////////////////////////////////////////////图片滚动的回调方法//////////////////////////////////////////////////////////
	@Override
	public ScreenScroller getScreenScroller() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub
		dismissProgress();
	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub
		if (mIndicatorView != null) {
			mIndicatorView.setFocusItem(mScrollerViewGroup.getCurrentViewIndex());
		}
	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// TODO Auto-generated method stub
		refreshScrollView();
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void scrollBy(int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getScrollX() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getScrollY() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Resources getResources() {
		DeskResourcesConfiguration configuration = DeskResourcesConfiguration.createInstance(this
				.getApplicationContext());
		if (null != configuration) {
			Resources resources = configuration.getDeskResources();
			if (null != resources) {
				return resources;
			}
		}

		return super.getResources();
	}

	private void refreshScrollView() {
		if (mScrollerViewGroup != null) {
			mCurImageIndex = mScrollerViewGroup.getCurrentViewIndex();
			if (mCurImageIndex >= 0 && mImageViews != null && mCurImageIndex < mImageViews.size()) {
				mCurrentImageView = mImageViews.get(mCurImageIndex);
				if (mCurrentImageView != null) {
					// mCurrentImageView.setImageDrawable(loadBitmap(mCurImageIndex));
					loadImage(mCurrentImageView, mCurImageIndex);
				}
				// if(mInforTextView != null){
				// mInforTextView.setText((mCurImageIndex+1)+"/"+mScanImageCount);
				// }

			}
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case mMsgLoadInit :
					showProgress();
					break;
				case mMsgLoadFinish :
					dismissProgress();
					String url = msg.getData().getString("url");
					Bitmap bitmap = (Bitmap) msg.obj;

					if (bitmap != null) {
						if (mLoadingImgIds != null) {
							mLoadingImgIds.remove(url);
						}
						// TODO:内存回收
						// 如果能直接从本地获取到图片
						// 对图片进行匹配机型的缩放
						Bitmap tmpCurBitmap = GoStoreDisplayUtil.scaleBitmapToScreen(mInstance,
								bitmap);
						BitmapDrawable bitmapDrawable = null;
						try {
							bitmapDrawable = new BitmapDrawable(tmpCurBitmap);
							bitmapDrawable.setTargetDensity(mInstance.getResources()
									.getDisplayMetrics());
							bitmap = null;
							tmpCurBitmap = null;
							// TODO:释放原图
							mHasAlreadyLoad = true;

						} catch (Throwable e) {
							// TODO: handle exception
							e.printStackTrace();
						}
						if (mImageViews != null) {
							for (ImageView imgView : mImageViews) {
								if (imgView != null && imgView.getTag() != null) {
									if (imgView.getTag().equals(url)) {
										imgView.setImageDrawable(bitmapDrawable);
									}
								}
							}
						} else {
							if (bitmapDrawable != null) {
								bitmapDrawable = null;
							}
						}
					}
					break;
			}
		};
	};
}
