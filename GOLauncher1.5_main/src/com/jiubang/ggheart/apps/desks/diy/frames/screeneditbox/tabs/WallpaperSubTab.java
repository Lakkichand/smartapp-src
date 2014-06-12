package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.window.WindowControl;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.DrawableCacheManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.IDrawableLoader;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditTabView;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.WallpaperItemInfo;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.WallpaperSubInfo;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 
 * @author licanhui GO壁纸TAB
 */
public class WallpaperSubTab extends BaseTab implements IDrawableLoader {

	private ArrayList<WallpaperSubInfo> mThumbs; // 缩略图
	private ArrayList<WallpaperSubInfo> mImages; // 墙纸应用大图
	private DrawableCacheManager mCacheManager;
	private boolean mFinishChange = true; // 换壁纸是否完成的标识

	private static final long CLICK_TIME = 800;
	private long mLastTime; // 上次的点击时间
	WallPaperTabManager mWallpaperTabmanager;
	WallpaperItemInfo mDto;

	private GoProgressBar mGoProgressBar;

	public WallpaperSubTab(Context context, String tag, int level) {
		super(context, tag, level);
		mLastTime = System.currentTimeMillis();
		mWallpaperTabmanager = new WallPaperTabManager(context, null);
		mMutex = new Object();
		mCacheManager = DrawableCacheManager.getInstance();
		mIsNeedAsyncLoadData = true;
		initData();
	}

	/**
	 * 初始化
	 */
	private void initData() {
		mThumbs = new ArrayList<WallpaperSubInfo>();
		mImages = new ArrayList<WallpaperSubInfo>();
		initListByLoading();
	}

	@Override
	public ArrayList<Object> getDtataList() {
		return null;
	}

	@Override
	public int getItemCount() {
		return mThumbs.size();
	}

	@Override
	public Drawable loadDrawable(int position, Object arg) {
		try {
			WallpaperSubInfo imageItem = mThumbs.get(position);
			if (imageItem == null) {
				return null;
			}
			final Resources resources = imageItem.getResource();
			final int resId = imageItem.getImageResId();
			// 根据包名和resId作为键值，防止重复
			Drawable drawable = mCacheManager
					.getDrawableFromCache(DrawableCacheManager.CACHE_WALLPAPERSUBTAB
							+ imageItem.getPackageName() + resId);
			if (drawable != null) {
				return drawable;
			}
			if (resources != null && resId > 0) {
				Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
				BitmapDrawable bmd = new BitmapDrawable(bitmap);
				mCacheManager.saveToCache(
						DrawableCacheManager.CACHE_WALLPAPERSUBTAB + imageItem.getPackageName()
								+ resId, bmd);
				return bmd;
			}
		} catch (Throwable e) {
			// TODO: handle exception
		}
		return null;
	}

	@Override
	public void displayResult(View view, Drawable drawable) {
		try {
			if (drawable != null) {
				ImageView image = (ImageView) view;
				image.setImageDrawable(getFitIcon(drawable, true));
			}
		} catch (Throwable e) {
			// TODO: handle exception
		}
	}

	@Override
	public View getView(int position) {
		View view = mInflater.inflate(R.layout.screen_edit_item, null);
		// ImageView image = (ImageView) view.findViewById(R.id.thumb);
		TextView mText = (TextView) view.findViewById(R.id.title);
		WallpaperSubInfo imageItem = mThumbs.get(position);
		if (imageItem == null) {
			return null;
		}
		mText.setText(imageItem.getImageResName());
		/*
		 * if(position==0){ image.setImageResource(imageItem.mImageResId); }else
		 */

		WallpaperSubInfo imageItem2 = mImages.get(position);
		view.setTag(imageItem2);

		return view;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		long curTime = System.currentTimeMillis();
		if (curTime - mLastTime < CLICK_TIME) {
			v.setPressed(false);
			return;
		}
		mLastTime = curTime;
		final WallpaperSubInfo imageItem = (WallpaperSubInfo) v.getTag();
		if (imageItem == null) {
			return;
		}
		if (imageItem.getType() == 0) {
			// 获取屏幕编辑底层
			ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
					.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
			if (screenEditBoxFrame != null) {
				screenEditBoxFrame.getTabView().setTap(BaseTab.TAB_WALLPAPER);
			}
		} else {
			WindowControl
					.setWallpaper(mContext, imageItem.getResource(), imageItem.getImageResId());
		}
	}

	@Override
	public void clearData() {
		super.clearData();
		if (mWallpaperTabmanager != null) {
			mWallpaperTabmanager.clear();
			mWallpaperTabmanager = null;
		}
		if (mThumbs != null) {
			mThumbs.clear();
			mThumbs = null;
		}
		if (mImages != null) {
			mImages.clear();
			mImages = null;
		}
	}

	@Override
	public void resetData() {
	}

	private Object mMutex;

	@SuppressWarnings("rawtypes")
	private void initListByLoading() {
		mThumbs.clear();
		mImages.clear();
		// 显示提示框
		showProgressDialog();
		// 异步初始化
		new Thread(ThreadName.SCREEN_EDIT_THEMETAB) {
			@Override
			public void run() {
				// 初始化数据
				synchronized (mMutex) {

					Map map = mWallpaperTabmanager.loadDrawables("wallpaperlist");
					mThumbs = (ArrayList<WallpaperSubInfo>) map.get("mThumbs");
					mImages = (ArrayList<WallpaperSubInfo>) map.get("mImages");
					Message msg = new Message();
					msg.what = LIST_INIT_OK;
					mHandler.sendMessage(msg);
				}
			};
		}.start();
	}

	private final static int LIST_INIT_OK = 1000;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LIST_INIT_OK :
					// 刷新
					if (mTabActionListener != null) {
						mTabActionListener.onRefreshTab(BaseTab.TAB_GOWALLPAPER, 0);
					}
					dismissProgressDialog();
					break;

				default :
					break;
			}
		};
	};

	private void showProgressDialog() {
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditTabView mLayOutView = screenEditBoxFrame.getTabView();
			mGoProgressBar = (GoProgressBar) mLayOutView.findViewById(R.id.edit_tab_progress);
			if (mGoProgressBar != null) {
				mGoProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void dismissProgressDialog() {
		if (mGoProgressBar != null) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

}
