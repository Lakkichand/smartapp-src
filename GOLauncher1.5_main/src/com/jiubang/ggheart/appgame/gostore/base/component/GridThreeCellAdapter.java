package com.jiubang.ggheart.appgame.gostore.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
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
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-11-19]
 */
public class GridThreeCellAdapter extends BaseAdapter {
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	/**
	 * 主题标签动画帧集合
	 */
	private List<Bitmap> mHotFrame = null;
	/**
	 * 主题标签动画帧集合
	 */
	private List<Bitmap> mNewFrame = null;
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

	private GridViewController mController = null;

	private ArrayList<String> mAppids = null;

	private ArrayList<String> mPkgs = null;

	private ArrayList<String> mIcons = null;

	private int mShowList;

	public GridThreeCellAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mImgManager = AsyncImageManager.getInstance();
		mAppids = new ArrayList<String>();
		mPkgs = new ArrayList<String>();
		mIcons = new ArrayList<String>();
		initShiningFrame();
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
	public void setGridController(GridViewController controller) {
		mController = controller;
	}
	
	/**
	 * 初始化火焰动画帧
	 */
	private void initShiningFrame() {
		mHotFrame = new ArrayList<Bitmap>();
		Resources res = mContext.getResources();
		Bitmap bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_hot_1)).getBitmap();
		mHotFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_hot_2)).getBitmap();
		mHotFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_hot_3)).getBitmap();
		mHotFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_hot_4)).getBitmap();
		mHotFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_hot_3)).getBitmap();
		mHotFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_hot_2)).getBitmap();
		mHotFrame.add(bm);
		
		mNewFrame = new ArrayList<Bitmap>();
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_new_1)).getBitmap();
		mNewFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_new_2)).getBitmap();
		mNewFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_new_3)).getBitmap();
		mNewFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_new_4)).getBitmap();
		mNewFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_new_3)).getBitmap();
		mNewFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appcenter_theme_new_2)).getBitmap();
		mNewFrame.add(bm);
	}

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag(R.id.appgame);
			if (tag != null && tag instanceof BoutiqueApp) {
				BoutiqueApp app = (BoutiqueApp) tag;
				if (app != null) {
					mController.onItemClick(mContext, app, mAppids, mPkgs, mIcons, mShowList);
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
//				Log.e("XIEDEZHI", "GridThreeCellAdapter getview position = " + position);
		// TODO:XIEDEZHI getview是不要生成对象
		if (position < 0 || position >= mDataSource.size()) {
			return convertView;
		}
		FeatureViewHolder holder = null;
		if (convertView == null) {
//			convertView = mInflater.inflate(R.layout.apps_mgr_feature_threecell, null);
			convertView = new GridThreeCellItem(mContext);
			convertView.setOnClickListener(null);
			holder = new FeatureViewHolder();
//			View leftView = convertView.findViewById(R.id.container_left);
//			View midView = convertView.findViewById(R.id.container_mid);
//			View rightView = convertView.findViewById(R.id.container_right);

//			holder.mRelativeLayoutLeft = (RelativeLayout) leftView
//					.findViewById(R.id.container_left);
//			holder.mSwitcherLeft = (ImageSwitcher) leftView.findViewById(R.id.app_icon_switcher);
//			holder.mAppNameLeft = (TextView) leftView.findViewById(R.id.feature_name);
//
//			holder.mRelativeLayoutMid = (RelativeLayout) midView.findViewById(R.id.container_mid);
//			holder.mSwitcherMid = (ImageSwitcher) midView.findViewById(R.id.app_icon_switcher);
//			holder.mAppNameMid = (TextView) midView.findViewById(R.id.feature_name);
//
//			holder.mRelativeLayoutRight = (RelativeLayout) rightView
//					.findViewById(R.id.container_right);
//			holder.mSwitcherRight = (ImageSwitcher) rightView.findViewById(R.id.app_icon_switcher);
//			holder.mAppNameRight = (TextView) rightView.findViewById(R.id.feature_name);
			holder.mRelativeLayoutLeft = ((GridThreeCellItem) convertView).getLeftView();
			holder.mSwitcherLeft = ((GridThreeCellItem) convertView).getLeftImageSwitcher();
			holder.mAppNameLeft = ((GridThreeCellItem) convertView).getLeftTextView();
			holder.mFeatureLeft = ((GridThreeCellItem) convertView).getLeftImageView();

			holder.mRelativeLayoutMid = ((GridThreeCellItem) convertView).getMidView();
			holder.mSwitcherMid = ((GridThreeCellItem) convertView).getMidImageSwitcher();
			holder.mAppNameMid = ((GridThreeCellItem) convertView).getMidTextView();
			holder.mFeatureMid = ((GridThreeCellItem) convertView).getMidImageView();

			holder.mRelativeLayoutRight = ((GridThreeCellItem) convertView).getRightView();
			holder.mSwitcherRight = ((GridThreeCellItem) convertView).getRightImageSwitcher();
			holder.mAppNameRight = ((GridThreeCellItem) convertView).getRightTextView();
			holder.mFeatureRight = ((GridThreeCellItem) convertView).getRightImageView();
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
						if (holder.mFeatureLeft != null) {
							holder.mFeatureLeft.setImageDrawable(null);
						}
						// 设置特性图标，“必备”，“首发”，“最新”等等
						if (app.info.effect == 2 || app.info.effect == 3) {
							if (holder.mFeatureLeft != null) {
								holder.mFeatureLeft.setVisibility(View.VISIBLE);
								holder.mFeatureLeft.setTag(String.valueOf(holder.mFeatureLeft
										.toString().hashCode()));
								setShiningIcon(position, holder.mFeatureLeft,
										holder.mFeatureLeft.toString(), app.info.effect);
							}
						} else if (!TextUtils.isEmpty(app.info.ficon)) {
							if (holder.mFeatureLeft != null) {
								holder.mFeatureLeft.setVisibility(View.VISIBLE);
								LayoutParams imgLayoutParams = new LayoutParams(
										LayoutParams.WRAP_CONTENT, 
										LayoutParams.WRAP_CONTENT);
								int padding = GoStoreDisplayUtil.scalePxToMachine(mContext, 5);
								holder.mFeatureLeft.setPadding(padding, padding, padding, padding);
								imgLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
								imgLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
								holder.mFeatureLeft.setLayoutParams(imgLayoutParams);
								setFeatureIcon(position, holder.mFeatureLeft, app.info.ficon,
										app.picLocalPath, app.localFeatureFileName);
							}
						}
					} else {
						ImageView image = (ImageView) holder.mSwitcherLeft.getCurrentView();
						image.setImageBitmap(null);
						image.setBackgroundDrawable(mDefaultDrawable);
					}
					// 名字
					holder.mAppNameLeft.setText(app.info.name);
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
						if (holder.mFeatureMid != null) {
							holder.mFeatureMid.setImageDrawable(null);
						}
						// 设置特性图标，“必备”，“首发”，“最新”等等
						if (app.info.effect == 2 || app.info.effect == 3) {
							if (holder.mFeatureMid != null) {
								holder.mFeatureMid.setVisibility(View.VISIBLE);
								holder.mFeatureMid.setTag(String.valueOf(holder.mFeatureMid
										.toString().hashCode()));
								setShiningIcon(position, holder.mFeatureMid,
										holder.mFeatureMid.toString(), app.info.effect);
							}
						} else if (!TextUtils.isEmpty(app.info.ficon)) {
							if (holder.mFeatureMid != null) {
								holder.mFeatureMid.setVisibility(View.VISIBLE);
								LayoutParams imgLayoutParams = new LayoutParams(
										LayoutParams.WRAP_CONTENT, 
										LayoutParams.WRAP_CONTENT);
								int padding = GoStoreDisplayUtil.scalePxToMachine(mContext, 5);
								holder.mFeatureMid.setPadding(padding, padding, padding, padding);
								imgLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
								imgLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
								holder.mFeatureMid.setLayoutParams(imgLayoutParams);
								setFeatureIcon(position, holder.mFeatureMid, app.info.ficon,
										app.picLocalPath, app.localFeatureFileName);
							}
						}
					} else {
						ImageView image = (ImageView) holder.mSwitcherMid.getCurrentView();
						image.setImageBitmap(null);
						image.setBackgroundDrawable(mDefaultDrawable);
					}
					// 名字
					holder.mAppNameMid.setText(app.info.name);
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
						if (holder.mFeatureRight != null) {
							holder.mFeatureRight.setImageDrawable(null);
						}
						// 设置特性图标，“必备”，“首发”，“最新”等等
						if (app.info.effect == 2 || app.info.effect == 3) {
							if (holder.mFeatureRight != null) {
								holder.mFeatureRight.setVisibility(View.VISIBLE);
								holder.mFeatureRight.setTag(String.valueOf(holder.mFeatureRight
										.toString().hashCode()));
								setShiningIcon(position, holder.mFeatureRight,
										holder.mFeatureRight.toString(), app.info.effect);
							}
						} else if (!TextUtils.isEmpty(app.info.ficon)) {
							if (holder.mFeatureRight != null) {
								holder.mFeatureRight.setVisibility(View.VISIBLE);
								LayoutParams imgLayoutParams = new LayoutParams(
										LayoutParams.WRAP_CONTENT, 
										LayoutParams.WRAP_CONTENT);
								int padding = GoStoreDisplayUtil.scalePxToMachine(mContext, 5);
								holder.mFeatureRight.setPadding(padding, padding, padding, padding);
								imgLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
								imgLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
								holder.mFeatureRight.setLayoutParams(imgLayoutParams);
								setFeatureIcon(position, holder.mFeatureRight, app.info.ficon,
										app.picLocalPath, app.localFeatureFileName);
							}
						}
					} else {
						ImageView image = (ImageView) holder.mSwitcherRight.getCurrentView();
						image.setImageBitmap(null);
						image.setBackgroundDrawable(mDefaultDrawable);
					}
					// 名字
					holder.mAppNameRight.setText(app.info.name);
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
	private void setFeatureIcon(final int position, final ImageView imageView, String imgUrl,
			String imgPath, String imgName) {
		// TODO:XIEDEZHI 修改接口，不要每次setIcon都要生成一个回调
		imageView.setTag(imgUrl);
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl) && mIsActive) {
							imageView.setImageBitmap(imageBitmap);
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null && mIsActive) {
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageBitmap(null);
		}
	}
	
	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setShiningIcon(final int position, final ThemesFeatureTag imageView,
			String imgName, int effect) {
		// TODO:XIEDEZHI 修改接口，不要每次setIcon都要生成一个回调
		if (imageView.getTag().equals(String.valueOf(imageView.toString().hashCode())) && mIsActive) {
			if (effect == 2) {
				//最新动态
				imageView.setShiningFrame(mNewFrame);
			} else if (effect == 3) {
				//最热动态
				imageView.setShiningFrame(mHotFrame);
			}
			imageView.setShining(true);
		}
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
		mPkgs.clear();
		if (data != null) {
			for (BoutiqueApp app : data) {
				// 初始化应用的图片路径
				String icon = app.info.icon;
				String pic = app.pic;
				if (!mIcons.contains(icon) && !mAppids.contains(app.info.appid)
						&& !mPkgs.contains(app.info.packname)) {
					mIcons.add(icon);
					mAppids.add(app.info.appid);
					mPkgs.add(app.info.packname);
				}
				if (!TextUtils.isEmpty(app.info.ficon)) {
					app.localFeatureFileName = String.valueOf(app.info.ficon.hashCode());
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
		public TextView mAppNameLeft;
		public ThemesFeatureTag mFeatureLeft;

		public RelativeLayout mRelativeLayoutMid;
		public ImageSwitcher mSwitcherMid;
		public TextView mAppNameMid;
		public ThemesFeatureTag mFeatureMid;

		public RelativeLayout mRelativeLayoutRight;
		public ImageSwitcher mSwitcherRight;
		public TextView mAppNameRight;
		public ThemesFeatureTag mFeatureRight;

		public void setVisibility() {
			mRelativeLayoutLeft.setVisibility(View.VISIBLE);
			mSwitcherLeft.setVisibility(View.VISIBLE);
			mAppNameLeft.setVisibility(View.VISIBLE);
			mFeatureLeft.setVisibility(View.GONE); // add by zzf
			
			mRelativeLayoutMid.setVisibility(View.VISIBLE);
			mSwitcherMid.setVisibility(View.VISIBLE);
			mAppNameMid.setVisibility(View.VISIBLE);
			mFeatureMid.setVisibility(View.GONE);
			
			mRelativeLayoutRight.setVisibility(View.VISIBLE);
			mSwitcherRight.setVisibility(View.VISIBLE);
			mAppNameRight.setVisibility(View.VISIBLE);
			mFeatureRight.setVisibility(View.GONE);
		}
	}

	/**
	 * 在应用详情中是否展现该列表页
	 */
	public void setShowList(int showlist) {
		mShowList = showlist;
	}
}