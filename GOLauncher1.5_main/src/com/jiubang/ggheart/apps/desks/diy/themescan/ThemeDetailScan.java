package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;

/**
 * @author chenguanyu
 */
public class ThemeDetailScan extends DetailScan {
	private int mMarkInfoScreen = 0;
	private boolean mIsThemeModifyed;
	private ThemeDetailView mParent = null;
	private boolean mIsInfoTextView;
	private Context mContext;
	public static final int ITEMS_H = 3;
	public static final float DESNT15 = 1.5f;
	public static final float DESNT075 = 0.75f;
	public ThemeDetailScan(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mParent = (ThemeDetailView) this.getParent();
	}

	public void setmIsThemeModifyed(boolean mIsThemeModifyed) {
		this.mIsThemeModifyed = mIsThemeModifyed;
	}

	public void setmIsInfoTextView(boolean mIsInfoTextView) {
		this.mIsInfoTextView = mIsInfoTextView;
	}

	@Override
	protected void initData() {
		if (null != mInfoBean && null != mScroller) {
			// 旧主题包只有一张预览图，UI2.0新主题包有多张预览图，第一张用在“我的主题”界面显示，在主题详情不显示
			int prePicsCount = ((ThemeInfoBean) mInfoBean).getPreViewDrawableNames().size();
			String vimgUrl = ((ThemeInfoBean) mInfoBean).getVimgUrl();
			boolean hasVideo = false;
			if (vimgUrl != null && !vimgUrl.equals("")) {
				hasVideo = true;
				mTotalScreenNum = prePicsCount > 1 ? prePicsCount : prePicsCount + 1;
			} else {
				mTotalScreenNum = (prePicsCount > 1) ? prePicsCount - 1 : prePicsCount;
			}
//			mCurrentScreen = 0;
			if (SpaceCalculator.sPortrait) {
				mScroller.setScreenCount(mTotalScreenNum);
				mScroller.setCurrentScreen(mCurrentScreen);
			} else {
				// 横屏的时候一屏显示3个
				int pages = 0;
				pages = mTotalScreenNum / ITEMS_H;
				if (mTotalScreenNum % ITEMS_H > 0) {
					pages++;
				}
				mTotalScreenNum = pages;
				mScroller.setScreenCount(mTotalScreenNum);
				mScroller.setCurrentScreen(mCurrentScreen);
			}

			// init themsviews
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
//			String pkgName = ((ThemeInfoBean) mInfoBean).getPackageName();
			String previewName = null;
			int i = (prePicsCount > 1 && !hasVideo) ? 1 : 0;
			if (hasVideo && prePicsCount < 2) {
				prePicsCount = prePicsCount + 1;
			}
			for (; i < prePicsCount; i++) {
				final SingleThemeView singleView = (SingleThemeView) inflater.inflate(
						R.layout.theme_detail_singletheme, null);
				setImageDefaultSize(singleView);
				if (hasVideo && i == 0) {
					previewName = ((ThemeInfoBean) mInfoBean).getVimgUrl();
					singleView.setHasVideo(true);
				} else {
					previewName = ((ThemeInfoBean) mInfoBean).getPreViewDrawableNames().get(i);
					singleView.setHasVideo(false);
				}
				if (prePicsCount > 1) {
					// 有多张图片
					if (mIsThemeModifyed) {
						if (SpaceCalculator.sPortrait) {
							// 竖屏的是只在第一张的右上角加已修改
							if (i == 1) {
								singleView.setmThemeDetailData((ThemeInfoBean) mInfoBean, previewName, true,
										isFeaturedThemeBean());
							} else {
								singleView.setmThemeDetailData((ThemeInfoBean) mInfoBean, previewName, false,
										isFeaturedThemeBean());
							}
						} else {
							// 横屏的时候第一屏的图片都要加
							if (i <= ITEMS_H) {
								singleView.setmThemeDetailData((ThemeInfoBean) mInfoBean, previewName, true,
										isFeaturedThemeBean());
							} else {
								singleView.setmThemeDetailData((ThemeInfoBean) mInfoBean, previewName, false,
										isFeaturedThemeBean());
							}

						}
					} else {
						singleView.setmThemeDetailData((ThemeInfoBean) mInfoBean, previewName, false,
								isFeaturedThemeBean());
					}
				} else {
					if (mIsThemeModifyed) {
						singleView.setmThemeDetailData((ThemeInfoBean) mInfoBean, previewName, true,
								isFeaturedThemeBean());
					} else {
						singleView.setmThemeDetailData((ThemeInfoBean) mInfoBean, previewName, false,
								isFeaturedThemeBean());
					}
				}
				final int index = i;
				final boolean has = hasVideo;
				ImageView image = singleView.getImageView();
				ImageView videoImage = singleView.getVideoFlagView();
				if (has && index == 0) {
					if (videoImage != null) {
						videoImage.setVisibility(VISIBLE);
						videoImage.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								String vurl = ((ThemeInfoBean) mInfoBean).getVurl();
								if (vurl != null && !vurl.equals("")) {
									gotoBrowser(vurl);
								}

							}
						});
					}
				} else {
					if (image != null) {
						image.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
//								if (has && index == 0) {
//									String vurl = ((ThemeInfoBean) mInfoBean).getVurl();
//									if (vurl != null && !vurl.equals("")) {
//										gotoBrowser(vurl);
//									}
//								} else {
									//已经加载完毕
									Intent intent = new Intent();
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									intent.putExtra(ThemeConstants.FULLSCREEN_THEME_INFO, (ThemeInfoBean) mInfoBean);
									intent.putExtra(ThemeConstants.FULLSCREEN_CURRENT_INDEX, index);
									intent.setClass(getContext(), ThemePreviewFullScreenViewActivity.class);
									mContext.startActivity(intent);
//								}
							}
						});
					}
				}
				addView(singleView);
			}
		}
	}
	
	private void gotoBrowser(String uriString) {
		// 跳转intent
		Uri uri = Uri.parse(uriString);
		Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);

		// 1:已安装的浏览器列表
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> resolveList = pm.queryIntentActivities(myIntent, 0);
		boolean hasRun = false;

		if (resolveList != null && !resolveList.isEmpty()) {
			// 2:获取当前运行程序列表
			ArrayList<FunTaskItemInfo> curRunList = null;
			try {
				curRunList = AppCore.getInstance().getTaskMgrControler().getProgresses();
			} catch (Throwable e) {
			}
			int curRunSize = (curRunList != null) ? curRunList.size() : 0;

			// 两个列表循环遍历
			for (int i = curRunSize - 1; i > 0; i--) {
				FunTaskItemInfo funTaskItemInfo = curRunList.get(i);
				Intent funIntent = funTaskItemInfo.getAppItemInfo().mIntent;
				ComponentName funComponentName = funIntent.getComponent();
				for (ResolveInfo resolveInfo : resolveList) {
					if (resolveInfo.activityInfo.packageName != null
							&& resolveInfo.activityInfo.packageName.equals(funComponentName
									.getPackageName())) {
						// 找到正在运行的浏览器，直接拉起
						if (funIntent.getComponent() != null) {
							String pkgString = funIntent.getComponent().getPackageName();
							if (pkgString != null) {
								if (pkgString.equals("com.android.browser")
										|| pkgString.equals("com.dolphin.browser.cn")
										|| pkgString.equals("com.android.chrome")
										|| pkgString.equals("com.qihoo.browser")) {
									//上述浏览器后台拉起会跳转浏览器首页，而非保存的用户原来页面
									hasRun = true;
									funIntent.setAction(Intent.ACTION_VIEW);
									funIntent.setData(uri);
									mContext.startActivity(funIntent);
								}
							}
						}
					}
				}
			}
			//无正在运行的浏览器，直接取浏览器列表的第1个打开
			if (!hasRun) {
				ResolveInfo resolveInfo = resolveList.get(0);
				String pkgString = resolveInfo.activityInfo.packageName;
				String activityName = resolveInfo.activityInfo.name;
				myIntent.setClassName(pkgString, activityName);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(myIntent);
			}
		}
	}

	/**
	 * 精选主题 当主题信息未加载完成时只显示背景
	 */
	public void setOnlyImageBackgroud() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		SingleThemeView singleView = (SingleThemeView) inflater.inflate(
				R.layout.theme_detail_singletheme, null);
		setImageDefaultSize(singleView);
		singleView.setOnlyImageBackgroud();
		addView(singleView);
	}
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		if (metrics.widthPixels <= metrics.heightPixels) {
			SpaceCalculator.setIsPortrait(true);
			SpaceCalculator.getInstance(getContext()).calculateItemViewInfo();
		} else {
			SpaceCalculator.setIsPortrait(false);
			SpaceCalculator.getInstance(getContext()).calculateThemeListItemCount();
		}
		if (SpaceCalculator.sPortrait || mIsInfoTextView) {
			onLayoutPortrait(changed, l, t, r, b);
		} else {
			onLayoutLandscape(changed, l, t, r, b);
		}
	}

	protected void onLayoutPortrait(boolean changed, int l, int t, int r, int b) {
		// 主题信息文本两边空白的宽度总和
		int textWidth = (int) getResources().getDimension(R.dimen.theme_info_text_width);
		int onethemewidth = mLayoutWidth;
		int count = getChildCount();
		int page = 0;
		
		for (int i = 0; i < count; i++) {
			page = i;
			View childView = getChildAt(i);
			View view = childView.findViewById(R.id.themeview).findViewById(R.id.image);
			if (view == null) {
				ScrollView.LayoutParams scrollViewparams = new ScrollView.LayoutParams(
						onethemewidth - textWidth, LayoutParams.WRAP_CONTENT);
				view = childView.findViewById(R.id.themeview).findViewById(R.id.relative);

				view.setLayoutParams(scrollViewparams);
			}
			int left = page * mLayoutWidth;
			int top = 0;
			int right = left + mLayoutWidth;
			int bottom = top + mLayoutHeight;
			childView.measure(mLayoutWidth, mLayoutHeight);
			childView.layout(left, top, right, bottom);
		}
	}

	protected void onLayoutLandscape(boolean changed, int l, int t, int r, int b) {
		// 主题信息文本两边空白的宽度总和
		int textWidth = (int) getResources().getDimension(R.dimen.theme_info_text_width);
		int onethemewidth = mLayoutWidth / ITEMS_H;
		int onethemeheight = mLayoutHeight;
		int count = getChildCount();
		int page = 0;
		
				
		for (int i = 0; i < count; i++) {
			page = i;
			View childView = getChildAt(i);	
			View view = childView.findViewById(R.id.themeview).findViewById(R.id.image);
			if (view == null) {
				ScrollView.LayoutParams scrollviewParams = new ScrollView.LayoutParams(
						onethemewidth - textWidth, onethemeheight);
				view = childView.findViewById(R.id.themeview).findViewById(R.id.relative);
				view.setLayoutParams(scrollviewParams);
			}
			int left = page * onethemewidth;
			int top = 0;
			int right = left + onethemewidth;
			int bottom = top + onethemeheight;
			childView.measure(onethemewidth, onethemeheight);
			childView.layout(left, top, right, bottom);

		}
	}
	
	/**
	 * added by liulix
	 * 判断如果屏幕高度大于一屏，则根据图片的宽计算长度
	 */
	private int getHeightBasedWidth(int width) {
		int screenheight = getContext().getResources()
				.getDisplayMetrics().heightPixels;
		int top = DrawUtils.dip2px(48); //主题详情页title高度
		int bottom = SpaceCalculator.sPortrait ? DrawUtils.dip2px(53) : 0; //主题详情页应用按钮高度
		int indicator = SpaceCalculator.sPortrait ? DrawUtils.dip2px(52) : DrawUtils.dip2px(13);
		int maxHeight = screenheight - top - bottom - indicator;
		int layoutHeight = 822 * width / 498;
		return maxHeight < layoutHeight ? maxHeight : layoutHeight;
	}
	
	private int getSingleViewWidth() {
		int layoutWidth = 0;
		if (SpaceCalculator.sPortrait) {
			// 竖屏时图片的宽和高
			layoutWidth = (int) mContext.getResources().getDimension(
					R.dimen.singletheme_detail_pic_width);
			// 对不同density的机型机型处理
			if (DrawUtils.sDensity == DESNT075) {
				layoutWidth = (int) mContext.getResources().getDimension(
						R.dimen.singletheme_detail_pic_width_ldpi);
			} else if (DrawUtils.sDensity == 1.0) {
				layoutWidth = (int) mContext.getResources().getDimension(
						R.dimen.singletheme_detail_pic_width_mdpi);
			}
		} else {
			// 横屏时图片的宽和高
			layoutWidth = (int) mContext.getResources().getDimension(
					R.dimen.singletheme_detail_pic_land_width_hdpi);
			// 对不同density的机型机型处理
			if (DrawUtils.sDensity == DESNT075) {
				layoutWidth = (int) mContext.getResources().getDimension(
						R.dimen.singletheme_detail_pic_land_width_ldpi);
			} else if (DrawUtils.sDensity == 1.0) {
				layoutWidth = (int) mContext.getResources().getDimension(
						R.dimen.singletheme_detail_pic_land_width_mdpi);
			}
		}
		return layoutWidth;
	}
	
	private void setImageDefaultSize(SingleThemeView singleView) {
		int width = getSingleViewWidth();
		singleView.setDefaultWidth(width);
		int height = getHeightBasedWidth(width);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		singleView.getImageView().setLayoutParams(params);
	}
	
	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mMarkInfoScreen = oldScreen;
		super.onScreenChanged(newScreen, oldScreen);
	}

	@Override
	public void cleanup() {

	}

	@Override
	public void setInfoBean(Object bean) throws IllegalArgumentException {
		if (null == bean || !(bean instanceof ThemeInfoBean)) {
			throw new IllegalArgumentException();
		} else {
			super.setInfoBean(bean);
		}
	}

	public int markInfoScreen() {
		return mMarkInfoScreen;
	}

	public void setScreenNum() {
		mScroller.setScreenCount(1);
		mScroller.setCurrentScreen(0);

	}

	public void setParent(ThemeDetailView parent) {
		mParent = parent;
	}

	public boolean isFeaturedThemeBean() {
		return ((ThemeInfoBean) mInfoBean).getFeaturedId() != 0 ;
	}
}
