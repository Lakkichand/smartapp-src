/**
 * 
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp.BoutiqueAppInfo;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 专题ListView
 * 
 * @author liguoliang
 * 
 */
//CHECKSTYLE:OFF
public class SpecialSubjectListView extends ListView {
	private Context mContext;
	private RecommendListAdapter mAdapter;
	private onItemClickListener mListener;
	private AsyncImageManager mImgManager;
	/**
	 * 判断当前页是否处于激活状态，用于应用中心/游戏中心的topfree,topnew等页面，其他页面都是true
	 */
	private boolean mIsActive = false;
	private LayoutInflater mInflater;
	private RelativeLayout mFooterLayout;
	private RelativeLayout mProgressLayout;
	private RelativeLayout mRetryLayout;
	private RelativeLayout mEndLayout;

	private Drawable mDefaultIcon;

	public interface onItemClickListener {
		void onItemClick(BoutiqueApp app, int index);
	}

	public SpecialSubjectListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		mContext = context;
		initFooter();
		mImgManager = AsyncImageManager.getInstance();
		mDefaultIcon = mContext.getResources().getDrawable(R.drawable.default_icon);
	}

	/**
	 * 
	 * @param appList
	 *            更新的数据源
	 * @param loadImage
	 *            是否需要加载图标，主要用于ExtraContainer在非激活状态时节省内存
	 */
	public void updateList(List<BoutiqueApp> appList, boolean loadImage) {
		// 初始化所有app的本地保存文件名
		if (loadImage) {
			for (BoutiqueApp app : appList) {
				if (app == null) {
					continue;
				}
				BoutiqueAppInfo info = app.info;
				if (info == null) {
					continue;
				}
				if (!TextUtils.isEmpty(info.icon)) {
					app.picLocalFileName = String.valueOf(info.icon.hashCode());
				}
				if (!TextUtils.isEmpty(info.ficon)) {
					app.localFeatureFileName = String.valueOf(info.ficon.hashCode());
				}
			}
		}
		mIsActive = loadImage;
		if (mAdapter == null) {
			mAdapter = new RecommendListAdapter((ArrayList<BoutiqueApp>) appList);
			addMoreFooter();
			setAdapter(mAdapter);
			setFootertVisibility(View.GONE);
		} else {
			mAdapter.updateList((ArrayList<BoutiqueApp>) appList);
		}
	}

	public void addMoreFooter() {
		if (mFooterLayout != null && getFooterViewsCount() < 1) {
			addFooterView(mFooterLayout);
		}
	}

	public void removeMoreFooter() {
		if (mFooterLayout != null && getFooterViewsCount() > 0) {
			removeFooterView(mFooterLayout);
		}
	}

	public void setItemClickListener(onItemClickListener listener) {
		mListener = listener;
	}

	public void setFootertVisibility(int visibility) {
		if (mFooterLayout != null) {
			mFooterLayout.setVisibility(View.VISIBLE);
		}
	}

	private void initFooter() {
		mInflater = LayoutInflater.from(mContext);
		mFooterLayout = (RelativeLayout) mInflater.inflate(R.layout.apps_mgr_listview_foot_more,
				null);
		mProgressLayout = (RelativeLayout) mFooterLayout
				.findViewById(R.id.apps_mgr_listview_foot_loading);
		mEndLayout = (RelativeLayout) mFooterLayout.findViewById(R.id.apps_mgr_listview_foot_end);
		mRetryLayout = (RelativeLayout) mFooterLayout
				.findViewById(R.id.apps_mgr_listview_foot_retry);
	}

	public void showLoadingAnotherPage() {
		if (mFooterLayout != null && mProgressLayout != null && mEndLayout != null
				&& mRetryLayout != null) {
			addMoreFooter();
			mProgressLayout.setVisibility(View.VISIBLE);
			mEndLayout.setVisibility(View.INVISIBLE);
			mRetryLayout.setVisibility(View.INVISIBLE);
		}
	}

	public void showEndPage() {
		if (mFooterLayout != null && mProgressLayout != null && mEndLayout != null
				&& mRetryLayout != null) {
			addMoreFooter();
			mProgressLayout.setVisibility(View.INVISIBLE);
			mEndLayout.setVisibility(View.VISIBLE);
			mRetryLayout.setVisibility(View.INVISIBLE);
		}
	}

	public void showLoadRetry(OnClickListener listener) {
		if (mFooterLayout != null && mProgressLayout != null && mEndLayout != null
				&& mRetryLayout != null) {
			addMoreFooter();
			mProgressLayout.setVisibility(View.INVISIBLE);
			mEndLayout.setVisibility(View.INVISIBLE);
			Button retryBtn = (Button) mRetryLayout
					.findViewById(R.id.apps_mgr_listview_foot_retry_btn);
			retryBtn.setOnClickListener(listener);
			mRetryLayout.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 读取图标，然后设到imageview里
	 * 
	 * @author xiedezhi
	 */
	private void setIcon(final ImageView imageView, String imgUrl, String imgPath, String imgName,
			boolean setDefaultIcon) {
		imageView.setTag(imgUrl);
		// TODO:XIEDEZHI 这里能不能不要每次load图片都生成一个回调对象？
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true, false,null,
				new AsyncImageLoadedCallBack() {
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
		if (bm != null) {
			imageView.setImageBitmap(bm);
		} else {
			if (setDefaultIcon) {
				imageView.setImageDrawable(mDefaultIcon);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}

	private class RecommendListAdapter extends BaseAdapter {
		private ArrayList<BoutiqueApp> mAppList;
		private LayoutInflater mInflater;

		public RecommendListAdapter(ArrayList<BoutiqueApp> appList) {
			if (appList != null) {
				mAppList = (ArrayList<BoutiqueApp>) appList.clone();
			}
			mInflater = LayoutInflater.from(mContext);
		}

		public void updateList(ArrayList<BoutiqueApp> appList) {
			if (appList != null) {
				mAppList = (ArrayList<BoutiqueApp>) appList.clone();
				notifyDataSetChanged();
			} else {
				this.mAppList = null;
				notifyDataSetChanged();
			}

		}

		@Override
		public int getCount() {
			if (mAppList == null) {
				return 0;
			}
			int size = mAppList.size();
			if (size % 2 == 0) {
				return size / 2;
			}
			return mAppList.size() / 2 + 1;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (mAppList == null) {
				return null;
			}
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.layout_special_subject_conainer, null);

				View left = convertView.findViewById(R.id.container_left);
				ImageView iconLeft = (ImageView) left.findViewById(R.id.app_icon);
				TextView nameLeft = (TextView) left.findViewById(R.id.app_name);
				TextView SizeLeft = (TextView) left.findViewById(R.id.app_size);
				TextView priceLeft = (TextView) left.findViewById(R.id.app_price);
				RatingBar ratingLeft = (RatingBar) left.findViewById(R.id.app_ratingbar);
				ImageView featureLeft = (ImageView) left.findViewById(R.id.app_feature);

				View right = convertView.findViewById(R.id.container_right);
				ImageView iconRight = (ImageView) right.findViewById(R.id.app_icon);
				TextView nameRight = (TextView) right.findViewById(R.id.app_name);
				TextView SizeRight = (TextView) right.findViewById(R.id.app_size);
				TextView priceRight = (TextView) right.findViewById(R.id.app_price);
				RatingBar ratingRight = (RatingBar) right.findViewById(R.id.app_ratingbar);
				ImageView featureRight = (ImageView) right.findViewById(R.id.app_feature);

				holder.mIvIconLeft = iconLeft;
				holder.mTvNameLeft = nameLeft;
				holder.mTvSizeLeft = SizeLeft;
				holder.mTvPriceLeft = priceLeft;
				holder.mRatingBarLeft = ratingLeft;
				holder.mIvFeatureLeft = featureLeft;

				holder.mIvIconRight = iconRight;
				holder.mTvNameRight = nameRight;
				holder.mTvSizeRight = SizeRight;
				holder.mTvPriceRight = priceRight;
				holder.mRatingBarRight = ratingRight;
				holder.mIvFeatureRight = featureRight;

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			// 左边
			final BoutiqueApp leftApp = mAppList.get(position * 2);
			if (leftApp == null) {
				return null;
			}
			BoutiqueAppInfo leftAppInfo = leftApp.info;
			if (leftAppInfo == null) {
				return null;
			}
			ViewGroup left = (ViewGroup) convertView.findViewById(R.id.container_left);
			final int leftIndex = position * 2 + 1;
			// TODO:XIEDEZHI 这里为什么每次getView都生成一个listener
			left.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (leftApp != null && mListener != null) {
						mListener.onItemClick(leftApp, leftIndex);
					}
				}
			});
			if (mIsActive) {
				// 判断图片是否存在
				String fileName = leftApp.picLocalFileName;
				String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
				setIcon(holder.mIvIconLeft, leftAppInfo.icon, localPath, fileName, true);
			} else {
				if (holder.mIvIconLeft != null) {
					holder.mIvIconLeft.setImageDrawable(mDefaultIcon);
				}
			}

			holder.mTvNameLeft.setText(leftAppInfo.name);
			// // 设置中文名字加粗
			// TextPaint tp = holder.mTvNameLeft.getPaint();
			// tp.setFakeBoldText(true);

			holder.mTvSizeLeft.setText(leftAppInfo.size);
			if (RecommAppsUtils.isInstalled(mContext, leftAppInfo.packname, null)) {
				holder.mTvPriceLeft.setText(R.string.themestore_already_install);
			} else {
				holder.mTvPriceLeft.setText(leftAppInfo.price);
			}
			float grade = (float) leftAppInfo.grade / 2;
			holder.mRatingBarLeft.setRating(grade);
			int feature = Integer.MIN_VALUE;
			if (TextUtils.isEmpty(leftAppInfo.ficon)
					|| feature == BoutiqueApp.BoutiqueAppInfo.FEATURE_TYPE_DEFAULT) {
				holder.mIvFeatureLeft.setVisibility(View.INVISIBLE);
			} else if (feature > 0) {
				String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
				holder.mIvFeatureLeft.setVisibility(View.VISIBLE);
				setIcon(holder.mIvFeatureLeft, leftAppInfo.ficon, localPath,
						leftApp.localFeatureFileName, false);
			} else {
				holder.mIvFeatureLeft.setVisibility(View.INVISIBLE);
			}

			// 右边
			// 如果为单数并且是在最后一行
			if (mAppList.size() % 2 != 0 && position == getCount() - 1) {
				// // 取消右边的点击响应图片,防止点击时左边也会响应的情况
				// 暂时未想到好的解决方案，暂时这样改
				View right = convertView.findViewById(R.id.container_right);
				right.setBackgroundDrawable(new ColorDrawable(0));
				right.setOnClickListener(null);

				// 将子元素设为不可见
				holder.mIvIconRight.setVisibility(View.INVISIBLE);
				holder.mTvNameRight.setVisibility(View.INVISIBLE);
				holder.mTvPriceRight.setVisibility(View.INVISIBLE);
				holder.mTvSizeRight.setVisibility(View.INVISIBLE);
				holder.mRatingBarRight.setVisibility(View.INVISIBLE);
				holder.mIvFeatureRight.setVisibility(View.INVISIBLE);
			} else {
				final BoutiqueApp rightApp = mAppList.get(position * 2 + 1);
				if (rightApp == null) {
					return null;
				}
				BoutiqueAppInfo rightAppInfo = rightApp.info;
				if (rightAppInfo == null) {
					return null;
				}
				ViewGroup right = (ViewGroup) convertView.findViewById(R.id.container_right);
				right.setBackgroundDrawable(mContext.getResources().getDrawable(
						R.drawable.special_subject_child_selector));
				final int rightIndex = position * 2 + 2;
				right.setOnClickListener(new Button.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (rightApp != null && mListener != null) {
							mListener.onItemClick(rightApp, rightIndex);
						}
					}
				});
				// 将子元素设为可见，防止右边不存在时设为空后不可见
				holder.mIvIconRight.setVisibility(View.VISIBLE);
				holder.mTvNameRight.setVisibility(View.VISIBLE);
				holder.mTvPriceRight.setVisibility(View.VISIBLE);
				holder.mTvSizeRight.setVisibility(View.VISIBLE);
				holder.mRatingBarRight.setVisibility(View.VISIBLE);

				if (mIsActive) {
					// 判断图片是否存在
					String fileName = rightApp.picLocalFileName;
					String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
					setIcon(holder.mIvIconRight, rightAppInfo.icon, localPath, fileName, true);
				} else {
					holder.mIvIconRight.setImageDrawable(mDefaultIcon);
				}
				holder.mTvNameRight.setText(rightAppInfo.name);
				// // 设置中文名字加粗
				// tp = holder.mTvNameRight.getPaint();
				// tp.setFakeBoldText(true);
				holder.mTvSizeRight.setText(rightAppInfo.size);
				if (RecommAppsUtils.isInstalled(mContext, rightAppInfo.packname, null)) {
					holder.mTvPriceRight.setText(R.string.themestore_already_install);
				} else {
					holder.mTvPriceRight.setText(rightAppInfo.price);
				}
				grade = (float) rightAppInfo.grade / 2;
				holder.mRatingBarRight.setRating(grade);

				feature = Integer.MIN_VALUE;
				if (TextUtils.isEmpty(rightAppInfo.ficon)
						|| feature == BoutiqueApp.BoutiqueAppInfo.FEATURE_TYPE_DEFAULT) {
					holder.mIvFeatureRight.setVisibility(View.INVISIBLE);
				} else if (feature > 0) {
					String localPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
					holder.mIvFeatureRight.setVisibility(View.VISIBLE);
					setIcon(holder.mIvFeatureRight, rightAppInfo.ficon, localPath,
							rightApp.localFeatureFileName, false);
				} else {
					holder.mIvFeatureRight.setVisibility(View.INVISIBLE);
				}
			}

			return convertView;
		}
	}

	private class ViewHolder {
		// 左边
		ImageView mIvIconLeft;
		TextView mTvNameLeft;
		TextView mTvSizeLeft;
		TextView mTvPriceLeft;
		RatingBar mRatingBarLeft;
		ImageView mIvFeatureLeft;

		// 右边
		ImageView mIvIconRight;
		TextView mTvNameRight;
		TextView mTvSizeRight;
		TextView mTvPriceRight;
		RatingBar mRatingBarRight;
		ImageView mIvFeatureRight;
	}
}
