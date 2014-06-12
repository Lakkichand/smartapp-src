package com.jiubang.ggheart.appgame.gostore.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-11-19]
 */
public class WallpaperThreeCellAdapter extends BaseAdapter {
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	/**
	 * 该adapter对应的页面是否在激活状态
	 */
	private boolean mIsActive;
	/**
	 * 数据源，与container的数据不是同一个对象
	 */
	private List<BoutiqueApp> mDataSource = new ArrayList<BoutiqueApp>();
	/**
	 * 默认图标
	 */
	private Drawable mDefaultDrawable = null;

	private AsyncImageManager mImgManager = null;

	private WallpaperGridController mWallController = null;
	
	private ArrayList<String> mAppids = null;

	private ArrayList<String> mPics = null;

	private ArrayList<String> mIcons = null;
	
	private ArrayList<String> mDownloadUrls = null;

	public WallpaperThreeCellAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mImgManager = AsyncImageManager.getInstance();
		mAppids = new ArrayList<String>();
		mPics = new ArrayList<String>();
		mIcons = new ArrayList<String>();
		mDownloadUrls = new ArrayList<String>();
	}

	/**
	 * 设置列表展现的默认图标
	 */
	public void setDefaultIcon(Drawable drawable) {
		mDefaultDrawable = drawable;
	}

	/**
	 * 设置controller,用于跳转详情
	 */
	public void setWallController(WallpaperGridController controller) {
		mWallController = controller;
	}

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag(R.id.appgame);
			if (tag != null && tag instanceof BoutiqueApp) {
				BoutiqueApp app = (BoutiqueApp) tag;
				if (app != null) {
					mWallController.onItemClick(v.getContext(), app, mAppids, mPics, mIcons, mDownloadUrls);
				}
			}
		}
	};

	@Override
	public int getCount() {
		if (mDataSource == null) {
			return 0;
		}
		if (mDataSource.size() % 3 != 0) {
			return mDataSource.size() / 3 + 1;
		} else {
			return mDataSource.size() / 3;
		}
	}

	@Override
	public Object getItem(int position) {
		try {
			return mDataSource.get(position / 3);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
//		Log.e("XIEDEZHI", "WallpaperThreeCellAdapter getview position = " + position);
		// TODO:XIEDEZHI getview是不要生成对象
		if (position < 0 || position >= mDataSource.size()) {
			return convertView;
		}
		FeatureViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.apps_mgr_wallpaper_threecell, null);
			convertView.setOnClickListener(null);
			holder = new FeatureViewHolder();
			View leftView = convertView.findViewById(R.id.container_left);
			View midView = convertView.findViewById(R.id.container_mid);
			View rightView = convertView.findViewById(R.id.container_right);
			
			holder.mRelativeLayoutLeft = (RelativeLayout) leftView
					.findViewById(R.id.container_left);
			holder.mSwitcherLeft = (ImageSwitcher) leftView.findViewById(R.id.app_icon_switcher);
			
			holder.mRelativeLayoutMid = (RelativeLayout) midView
					.findViewById(R.id.container_mid);
			holder.mSwitcherMid = (ImageSwitcher) midView.findViewById(R.id.app_icon_switcher);

			holder.mRelativeLayoutRight = (RelativeLayout) rightView
					.findViewById(R.id.container_right);
			holder.mSwitcherRight = (ImageSwitcher) rightView.findViewById(R.id.app_icon_switcher);
			convertView.setTag(holder);
		} else {
			holder = (FeatureViewHolder) convertView.getTag();
		}
		//恢复内部子view的可见性
		holder.setVisibility();
		for (int i = 0; i < 3; i++) {
			final int nPosition = position * 3 + i;
			BoutiqueApp app = null;
			if (nPosition < mDataSource.size()) {
				app = mDataSource.get(nPosition);
			}
			// 左边
			if (i == 0) {
				if (app == null) {
					holder.mRelativeLayoutLeft.setVisibility(View.INVISIBLE);
				} else {
					if (mIsActive) {
						setIcon(position, holder.mSwitcherLeft, app.pic, app.picLocalPath,
								app.picLocalFileName, mDefaultDrawable);
					} else {
						ImageView image = (ImageView) holder.mSwitcherLeft.getCurrentView();
						image.setImageBitmap(null);
						image.setBackgroundDrawable(mDefaultDrawable);
					}
					// 名字
					holder.mRelativeLayoutLeft.setTag(R.id.appgame, app);
					holder.mRelativeLayoutLeft.setOnClickListener(mClickListener);
				}
			} else if (i == 1) {
				if (app == null) {
					holder.mRelativeLayoutMid.setVisibility(View.INVISIBLE);
				} else {
					if (mIsActive) {
						setIcon(position, holder.mSwitcherMid, app.pic, app.picLocalPath,
								app.picLocalFileName, mDefaultDrawable);
					} else {
						ImageView image = (ImageView) holder.mSwitcherMid.getCurrentView();
						image.setImageBitmap(null);
						image.setBackgroundDrawable(mDefaultDrawable);
					}
					// 名字
					holder.mRelativeLayoutMid.setTag(R.id.appgame, app);
					holder.mRelativeLayoutMid.setOnClickListener(mClickListener);
				} 
			} else if (i == 2) {
				if (app == null) {
					holder.mRelativeLayoutRight.setVisibility(View.INVISIBLE);
				} else {
					if (mIsActive) {
						setIcon(position, holder.mSwitcherRight, app.pic, app.picLocalPath,
								app.picLocalFileName, mDefaultDrawable);
					} else {
						ImageView image = (ImageView) holder.mSwitcherRight.getCurrentView();
						image.setImageBitmap(null);
						image.setBackgroundDrawable(mDefaultDrawable);
					}
					// 名字
					holder.mRelativeLayoutRight.setTag(R.id.appgame, app);
					holder.mRelativeLayoutRight.setOnClickListener(mClickListener);
				}
			}
		}
		return convertView;
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final int position, final ImageSwitcher switcher, String imgUrl,
			String imgPath, String imgName, Drawable defaultDrawable) {
		// TODO:XIEDEZHI 修改接口，不要每次setIcon都要生成一个回调
		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			ImageView image = (ImageView) switcher.getCurrentView();
			Drawable drawable = image.getBackground();
			if (drawable == null) {
				return;
			}
		}
		switcher.setTag(imgUrl);
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (switcher.getTag().equals(imgUrl)) {
							Drawable drawable = ((ImageView) switcher
									.getCurrentView()).getBackground();
							if (drawable != null) {
								ImageView imageView = (ImageView) switcher.getNextView();
								imageView.setBackgroundDrawable(null);
								switcher.setImageDrawable(new BitmapDrawable(imageBitmap));
							}
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null) {
			imageView.setBackgroundDrawable(null);
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageBitmap(null);
			imageView.setBackgroundDrawable(mDefaultDrawable);
		}
	}

	/**
	 * 更新adapter数据源，并调用notifyDataSetChanged
	 * 
	 * @param data
	 *            新数据
	 */
	public void update(List<BoutiqueApp> data) {
		mDataSource.clear();
		mIcons.clear();
		mAppids.clear();
		mPics.clear();
		mDownloadUrls.clear();
		if (data != null) {
			for (BoutiqueApp app : data) {
				// 初始化应用的图片路径
				String icon = app.info.icon;
				String pic = app.pic;
				if (!mIcons.contains(icon) && !mAppids.contains(app.info.appid)
						&& !mPics.contains(app.info.pics)
						&& !mDownloadUrls.contains(app.info.downloadurl)) {
					mIcons.add(icon);
					mAppids.add(app.info.appid);
					mPics.add(app.info.pics);
					mDownloadUrls.add(app.info.downloadurl);
				}
				if (!TextUtils.isEmpty(pic)) {
					String fileName = String.valueOf(pic.hashCode());
					app.picLocalPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
					app.picLocalFileName = fileName;
				}
				mDataSource.add(app);
			}
		}
		// notifyDataSetChanged();
	}

	/**
	 * 更改激活状态，如果是true则getview时会加载图标，否则不加载图标
	 * 
	 * @param isActive
	 *            是否为激活状态
	 */
	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 */
	public class FeatureViewHolder {
		public RelativeLayout mRelativeLayoutLeft;
		public ImageSwitcher mSwitcherLeft;
		
		public RelativeLayout mRelativeLayoutMid;
		public ImageSwitcher mSwitcherMid;

		public RelativeLayout mRelativeLayoutRight;
		public ImageSwitcher mSwitcherRight;

		public void setVisibility() {
			mRelativeLayoutLeft.setVisibility(View.VISIBLE);
			mSwitcherLeft.setVisibility(View.VISIBLE);
			
			mRelativeLayoutMid.setVisibility(View.VISIBLE);
			mSwitcherMid.setVisibility(View.VISIBLE);

			mRelativeLayoutRight.setVisibility(View.VISIBLE);
			mSwitcherRight.setVisibility(View.VISIBLE);
		}
	}
}
