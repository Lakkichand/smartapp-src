/*
 * 文 件 名:  PriceAlertAdapter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-12-14
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsSectionIndexer;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView.PinnedHeaderAdapter;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.AppGameInstallingValidator;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-12-14]
 *  
 */
public class PriceAlertAdapter extends BaseAdapter implements PinnedHeaderAdapter {
	private static final int TYPE_GROUP = 0;

	private static final int TYPE_INFO = 1;

	private static final int TYPE_COUNT = 2;
	/**
	 * 火焰动画帧集合
	 */
	private List<Bitmap> mBurningFrame = null;
	/**
	 * 数据源
	 */
	private ArrayList<BoutiqueApp> mList = null;

	private boolean mIsActive = false;

	private LayoutInflater mInflater = null;
	/**
	 * 默认图标
	 */
	private Bitmap mDefaultBitmap = null;
	/**
	 * 图片管理器
	 */
	private AsyncImageManager mImgManager = null;
	/**
	 * 图标保存路径
	 */
	private String mPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;

	private Context mContext = null;
	/**
	 * 图标有火焰时要图标设置padding
	 */
	private int mBurningPadding = DrawUtils.dip2px(1);

	private AppsSectionIndexer mIndexer;

	public PriceAlertAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mImgManager = AsyncImageManager.getInstance();
		initBurningFrame();
	}

	/**
	 * 设置列表展现的默认图标
	 */
	public void setDefaultIcon(Drawable drawable) {
		if (drawable != null && drawable instanceof BitmapDrawable) {
			mDefaultBitmap = ((BitmapDrawable) drawable).getBitmap();
		}
	}

	/**
	 * 初始化火焰动画帧
	 */
	private void initBurningFrame() {
		mBurningFrame = new ArrayList<Bitmap>();
		Resources res = mContext.getResources();
		Bitmap bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_1)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_2)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_3)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_4)).getBitmap();
		mBurningFrame.add(bm);
	}

	/**
	 * 功能简述:更新adapter的数据 
	 * 功能详细描述: 
	 * 注意:
	 * @param list
	 * @param loadImage
	 *            是否加载图片
	 */
	public void updateList(ArrayList<BoutiqueApp> data) {
		if (mList == null) {
			mList = new ArrayList<BoutiqueApp>();
		} else {
			mList.clear();
		}
		ArrayList<String> strList = new ArrayList<String>();
		ArrayList<Integer> intList = new ArrayList<Integer>();
		int count = 1;
		for (int i = 0; i < data.size(); i++) {
			BoutiqueApp app = data.get(i);
			// 检索出分组头信息
			if (app.info.appid == null || app.info.appid.equals("")) {
				strList.add(app.info.changetime);
				if (i != 0) {
					intList.add(count);
					count = 1;
				}
			} else {
				count++;
			}
			if (i == data.size() - 1) {
				intList.add(count);
			}
			// 初始化应用的图片路径
			String icon = app.info.icon;
			if (!(icon == null || icon.equals(""))) {
				String fileName = String.valueOf(icon.hashCode());
				app.picLocalPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
				app.picLocalFileName = fileName;
			}
			if (!TextUtils.isEmpty(app.info.ficon)) {
				app.localFeatureFileName = String.valueOf(app.info.ficon.hashCode());
			}
			mList.add(app);
		}
		// 确保长度相同，否则会出错
		while (strList.size() != intList.size()) {
			if (strList.size() > intList.size()) {
				strList.remove(strList.size());
			} else {
				intList.remove(intList.size() - 1);
			}
		}
		// 分组节点
		String[] sections = strList.toArray(new String[strList.size()]);
		int[] counts = new int[intList.size()];
		for (int i = 0; i < intList.size(); i++) {
			counts[i] = intList.get(i).intValue();
		}
		mIndexer = new AppsSectionIndexer(sections, counts);
	}

	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
	}

	@Override
	public int getViewTypeCount() {
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		BoutiqueApp app = (BoutiqueApp) getItem(position);
		if (app == null || app.info.appid == null || app.info.appid.equals("")) {
			return TYPE_GROUP;
		} else {
			return TYPE_INFO;
		}
	}

	@Override
	public boolean isEnabled(int position) {
		if (getItemViewType(position) == TYPE_GROUP) {
			return false;
		} else {
			return true;
		}
	}

	/** {@inheritDoc} */

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		} else {
			return 0;
		}
	}

	/** {@inheritDoc} */

	@Override
	public Object getItem(int position) {
		if (mList != null && position >= 0 && position < mList.size()) {
			return mList.get(position);
		} else {
			return null;
		}
	}

	/** {@inheritDoc} */

	@Override
	public long getItemId(int position) {
		return position;
	}

	/** {@inheritDoc} */

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewholder = null;
		int type = getItemViewType(position);
		if (convertView == null) {
			viewholder = new ViewHolder();
			if (type == TYPE_INFO) {
				convertView = mInflater.inflate(R.layout.appgame_price_alert_item, null);
				viewholder.mImageSwitcher = (ImageSwitcher) convertView
						.findViewById(R.id.price_alert_imageswitcher);
				viewholder.mIcon = (BurningIcon) convertView.findViewById(R.id.price_alert_icon);
				viewholder.mIconAnother = (BurningIcon) convertView
						.findViewById(R.id.price_alert_icon_another);
				viewholder.mName = (TextView) convertView.findViewById(R.id.price_alert_name);
				viewholder.mPreviousPrice = (TextView) convertView
						.findViewById(R.id.price_alert_previous);
				viewholder.mPreviousPrice.getPaint().setFlags(
						Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
				viewholder.mCurrentPrice = (TextView) convertView
						.findViewById(R.id.price_alert_price);
				viewholder.mCommentCount = (TextView) convertView
						.findViewById(R.id.price_alert_Comment_count);
				viewholder.mDownloadCount = (TextView) convertView
						.findViewById(R.id.price_alert_download_count);
				viewholder.mTypeInfo = (TextView) convertView
						.findViewById(R.id.price_alert_typeinfo);
				viewholder.mSummary = (TextView) convertView.findViewById(R.id.price_alert_summary);
				viewholder.mRatingBar = (RatingBar) convertView
						.findViewById(R.id.price_alert_progressbar);
			} else {
				convertView = mInflater.inflate(R.layout.recomm_appsmanagement_list_head, null);
				viewholder.mName = (TextView) convertView.findViewById(R.id.nametext);
				viewholder.mName.setBackgroundResource(R.drawable.list_head_bg);
				viewholder.mName.setTextColor(0xff838382);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.FILL_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				viewholder.mDivider = (ImageView) convertView.findViewById(R.id.divider);
				viewholder.mDivider.setBackgroundResource(R.drawable.listview_divider);
				int padding = mContext.getResources().getDimensionPixelSize(
						R.dimen.download_manager_text_padding);
				viewholder.mName.setPadding(padding * 2, padding, 0, padding);
				viewholder.mName.setLayoutParams(lp);
				convertView.setTag(viewholder);
			}
			convertView.setTag(viewholder);
		} else {
			viewholder = (ViewHolder) convertView.getTag();
		}
		BoutiqueApp app = mList.get(position);
		if (app != null) {
			// 分组信息
			if (type == TYPE_GROUP) {
				viewholder.mName.setText(app.info.changetime);
			} else {
				viewholder.mName.setText(app.info.name);
				// 旧的价格
				viewholder.mPreviousPrice.setText(app.info.oldprice);
				// 价格
				if (app.info.isfree == 0) {
					viewholder.mCurrentPrice.setTextColor(0xff6ba001);
				} else {
					viewholder.mCurrentPrice.setTextColor(0xffdd0000);
				}
				viewholder.mCurrentPrice.setText(app.info.price);
				viewholder.mDownloadCount.setText(app.info.dlcs);
				// 评论数
				if (app.info.commentsnum != null && !app.info.commentsnum.equals("")) {
					viewholder.mCommentCount.setVisibility(View.VISIBLE);
					viewholder.mCommentCount.setText("(" + app.info.commentsnum + ")");
				} else {
					viewholder.mCommentCount.setVisibility(View.GONE);
				}
				// 类型
				if (app.info.typeinfo != null && !app.info.typeinfo.equals("")) {
					viewholder.mTypeInfo.setVisibility(View.VISIBLE);
					viewholder.mTypeInfo.setText(app.info.typeinfo);
				} else {
					viewholder.mTypeInfo.setVisibility(View.GONE);
				}
				// 简介
				if (app.info.summary != null && !app.info.summary.equals("")) {
					viewholder.mSummary.setVisibility(View.VISIBLE);
					viewholder.mSummary.setText(app.info.summary);
				} else {
					viewholder.mSummary.setVisibility(View.GONE);
				}
				// 星级
				float grade = app.info.grade / 2.0f;
				// 星级显示
				viewholder.mRatingBar.setRating(grade);
				if (mIsActive) {
					// 判断用户是否已经点击过该应用
					int effect = 0; // 是否显示特效
					boolean isInstall = AppGameInstallingValidator.getInstance().isAppExist(mContext, app.info.packname);
					if (app.info.effect == 1 && !isInstall
							&& !DownloadUtil.checkViewedEffectApp(mContext, app.info.packname)) {
						effect = 1;
					}
					setIcon(position, viewholder.mImageSwitcher, app.info.icon, mPath,
							app.picLocalFileName, mDefaultBitmap, effect, true);
				} else {
					((ImageView) viewholder.mImageSwitcher.getCurrentView())
							.setImageBitmap(mDefaultBitmap);
				}
				convertView.setTag(R.id.appgame, app);
			}
		}
		return convertView;
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final int position, final ImageSwitcher switcher, String imgUrl,
			String imgPath, String imgName, final Bitmap defaultBitmap, final int effect,
			boolean isMask) {
		final int padding = mBurningPadding;
		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			ImageView image = (ImageView) switcher.getCurrentView();
			Drawable drawable = image.getDrawable();
			if (drawable != null && drawable instanceof BitmapDrawable) {
				BitmapDrawable bDrawable = (BitmapDrawable) drawable;
				if (bDrawable.getBitmap() != null && bDrawable.getBitmap() != mDefaultBitmap) {
					if (effect == 1 && image instanceof BurningIcon) {
						((BurningIcon) image).setBurningFrame(mBurningFrame);
						((BurningIcon) image).setBurning(true);
						image.setPadding(padding, padding, padding, padding);
					}
					return;
				}
			}
		}
		switcher.setTag(imgUrl);
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				isMask ? AppGameDrawUtils.getInstance().mMaskIconOperator : null,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (switcher.getTag().equals(imgUrl)) {
							Drawable drawable = ((ImageView) switcher.getCurrentView())
									.getDrawable();
							if (drawable instanceof BitmapDrawable) {
								Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
								if (bm == defaultBitmap) {
									switcher.setImageDrawable(new BitmapDrawable(imageBitmap));
								}
								ImageView imageView = (ImageView) switcher.getCurrentView();
								if (effect == 1 && imageView instanceof BurningIcon) {
									((BurningIcon) imageView).setBurningFrame(mBurningFrame);
									((BurningIcon) imageView).setBurning(true);
									imageView.setPadding(padding, padding, padding, padding);
								}
							}
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null && mIsActive) {
			imageView.setImageBitmap(bm);
			if (effect == 1 && imageView instanceof BurningIcon) {
				((BurningIcon) imageView).setBurningFrame(mBurningFrame);
				((BurningIcon) imageView).setBurning(true);
				imageView.setPadding(padding, padding, padding, padding);
			}
		} else {
			imageView.setImageBitmap(defaultBitmap);
		}
	}

	/** {@inheritDoc} */

	@Override
	public int getPinnedHeaderState(int position) {
		if (getCount() <= 0) {
			return PINNED_HEADER_GONE;
		}
		int realPosition = getRealPosition(position);
		if (realPosition < 0) {
			return PINNED_HEADER_GONE;
		}
		// The header should get pushed up if the top item shown
		// is the last item in a section for a particular letter.
		int section = getSectionForPosition(realPosition);
		int nextSectionPosition = getPositionForSection(section + 1);
		if (nextSectionPosition != -1 && realPosition == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}
		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View header, int position) {
		// 计算位置
		int realPosition = getRealPosition(position);
		int section = getSectionForPosition(realPosition);
		TextView headText = (TextView) header.findViewById(R.id.nametext);
		headText.setText(getSections(section));
	}

	private int getRealPosition(int pos) {
		//　有一个headerview,所以减1
		return pos - 1;
	}

	private int getSectionForPosition(int pos) {
		if (mIndexer == null) {
			return -1;
		}
		return mIndexer.getSectionForPosition(pos);
	}

	private int getPositionForSection(int pos) {
		if (mIndexer == null) {
			return -1;
		}
		return mIndexer.getPositionForSection(pos);
	}

	public String getSections(int pos) {
		if (mIndexer == null || pos < 0 || pos >= mIndexer.getSections().length) {
			return " ";
		} else {
			return (String) mIndexer.getSections()[pos];
		}
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-12-17]
	 */
	private class ViewHolder {
		public BurningIcon mIcon;
		public BurningIcon mIconAnother;
		public ImageSwitcher mImageSwitcher;
		public TextView mTypeInfo;
		public TextView mName;
		public TextView mCommentCount;
		public TextView mPreviousPrice;
		public TextView mCurrentPrice;
		public TextView mDownloadCount;
		public TextView mSummary;
		public RatingBar mRatingBar;
		public ImageView mDivider;
	}
}
