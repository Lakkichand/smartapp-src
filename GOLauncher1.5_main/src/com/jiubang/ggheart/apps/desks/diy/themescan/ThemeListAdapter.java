package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.gostore.base.component.AppsThemeDetailActivity;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.billing.ThemeAppInBillingManager;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.GoLockerThemeManager;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.SpecThemeViewConfig;
import com.jiubang.ggheart.data.theme.bean.ThemeBannerBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBannerBean.BannerElement;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.broadcastReceiver.MyThemeReceiver;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 主题数据适配器
 * 
 * @author yangbing
 * */
public class ThemeListAdapter extends BaseAdapter implements BroadCasterObserver {

	public static final String GOOGLEMARKET_PREFIX = "market://";
	public static final String HTTP_PREFIX = "http://";
	public static final String HTTPS_PREFIX = "https://";

	public static final int MSG_INAPP_PAID_FINISHED = 1;

	private static final int TAB_FEATURED = 0;
	private static final int TAB_INSTALLED = 1;

	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private int mItemThemeCount = 0; // item项显示主题个数
	private boolean mIsDealSpecialResolution; // 是否处理特殊分辨率
	private int mSpecialResolutionWidth; // 特殊分辨率宽
	private int mSpecialResolutionHight; // 特殊分辨率高
	private ArrayList<ThemeInfoBean[]> mThemeDataArrays; // 数据
	private View.OnClickListener mItemClickListener;
	private View.OnClickListener mItemControlClickListener;
	private View.OnClickListener mItemImageClickListener;
	private RelativeLayout mGoStoreLayout = null; // 去go精品下载 布局
	private RelativeLayout mBannerLayout = null; // 主题Banner的布局
	private SpaceCalculator mSpaceCalculator;
	private boolean mIsOverScreen;
	private int mTabType;
	private ThemePurchaseManager mPurchaseManager;
	private BroadcastReceiver mDownloadReceiver = null;
	private HashMap<Long, Integer> mDoloadingMap;
	private boolean mIsLauncherFeature = false;
	private ThemeBannerBean mBannerBean;
	private HashMap<String, Bitmap> mBannerImageMap;
	private int mThemeType;
	private boolean mIsSpecTheme = false;
	public ThemeListAdapter(Context context) {
		mContext = context;
		mPurchaseManager = ThemePurchaseManager.getInstance(mContext);
		mLayoutInflater = LayoutInflater.from(mContext);
		mSpaceCalculator = SpaceCalculator.getInstance(mContext);
		mDoloadingMap = new HashMap<Long, Integer>();
		mBannerImageMap = new HashMap<String, Bitmap>();
		initDownloadReceiver();
		initClickListener();
		isDealSpecialResolution();
		initGoStoreView();
	}

	/**
	 * <br>
	 * 功能简述:banner条 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initThemeBannerView() {
		mBannerLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.theme_banner_view, null);
		mBannerLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBannerBean != null && mBannerBean.mElements != null
						&& mBannerBean.mElements.size() > 0) {
					BannerElement element = mBannerBean.mElements.get(0);
					if (element.mGroup != null && element.mGroup.startsWith(GOOGLEMARKET_PREFIX)) {
						if (AppUtils.isMarketExist(mContext)) {
							AppUtils.gotoMarket(mContext, element.mGroup);
						}
					} else if (element.mGroup != null
							&& (element.mGroup.startsWith(HTTP_PREFIX) || element.mGroup
									.startsWith(HTTPS_PREFIX))) {
						AppUtils.gotoBrowserInRunTask(mContext, element.mGroup);
					} else {
						((ThemeManageActivity) mContext).gotoBannerList(
								mBannerBean.mElements.get(0).mId,
								mBannerBean.mElements.get(0).mName);
					}
				}
			}
		});
		android.widget.AbsListView.LayoutParams layoutParams = new android.widget.AbsListView.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mBannerLayout.setLayoutParams(layoutParams);
	}

	/**
	 * 去gostore下载更多界面
	 * */
	private void initGoStoreView() {
		mGoStoreLayout = (RelativeLayout) mLayoutInflater.inflate(R.layout.theme_gostore, null);
		mGoStoreLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mContext instanceof ThemeManageActivity) {
					((ThemeManageActivity) mContext).gotoGoStore();
				} else if (mContext instanceof BannerDetailActivity) {
					((BannerDetailActivity) mContext).gotoGoStore();
				}
			}
		});
		android.widget.AbsListView.LayoutParams layoutParams = new android.widget.AbsListView.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.height = mSpaceCalculator.getGoStoreBarHeight();
		mGoStoreLayout.setLayoutParams(layoutParams);
	}

	/**
	 * 判断是否要对特殊分辨率处理,例如：moto me860 density=1.5 540*960
	 * */
	private void isDealSpecialResolution() {

		int width = mContext.getResources().getDimensionPixelSize(R.dimen.mytheme_pic_width);
		int height = mContext.getResources().getDimensionPixelSize(R.dimen.mytheme_pic_height);
		final int screenwidth = SpaceCalculator.sPortrait
				? mContext.getResources().getDisplayMetrics().widthPixels
				: mContext.getResources().getDisplayMetrics().heightPixels;
		final int normal_HDPI_Width = 480;
		if (DrawUtils.sDensity == 1.5f && screenwidth != normal_HDPI_Width) {
			mIsDealSpecialResolution = true;
			float scale = (float) screenwidth / (float) normal_HDPI_Width;
			mSpecialResolutionWidth = (int) (width * scale);
			mSpecialResolutionHight = (int) (height * scale);
		}

	}

	/**
	 * 设置item视图单击事件监听
	 * */
	public void setmItemClickListener(View.OnClickListener mItemClickListener) {
		this.mItemClickListener = mItemClickListener;
	}

	/**
	 * 应用锁屏主题后，更改数据状态
	 * 
	 * */
	public ArrayList<ThemeInfoBean> updateInstalledLockerList(String packageName,
			String oldPackageName) {
		ArrayList<ThemeInfoBean> mThemeDatas = ThemeDataManager.getInstance(mContext).getThemeData(
				ThemeConstants.LOCKER_INSTALLED_THEME_ID);
		if (mThemeDatas == null || mThemeDatas.size() <= 0 || oldPackageName == null) {
			return null;
		}
		for (ThemeInfoBean bean : mThemeDatas) {
			if (oldPackageName.equals(bean.getPackageName())) {
				bean.setIsCurTheme(false);
			}
			if (packageName.equals(bean.getPackageName())) {
				bean.setIsCurTheme(true);
			}
		}
		return mThemeDatas;
	}

	/**
	 * 设置主题数据
	 * */
	public void setThemeDatas(ArrayList<ThemeInfoBean> mThemeDatas, ThemeBannerBean banner) {
		if (mThemeDatas == null) {
			return;
		}
		// 因为大范围报空指针问题，但查不到原因这里做一次检查
		Iterator iterator = mThemeDatas.iterator();
		while (iterator.hasNext()) {
			ThemeInfoBean info = (ThemeInfoBean) iterator.next();
			if (info.getPackageName() == null) {
				iterator.remove();
				continue;
			}
		}
		mBannerBean = banner;
		clearThemeDatas();
		mThemeDataArrays = new ArrayList<ThemeInfoBean[]>();
		mItemThemeCount = SpaceCalculator.getThemeListItemCount();
		ThemeInfoBean infoBean = mThemeDatas.get(0);
		mThemeType = infoBean.getBeanType();
		if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID
				|| infoBean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			mPurchaseManager.registerObserver(this);
			mTabType = TAB_FEATURED;
		} else {
			mTabType = TAB_INSTALLED;
		}
		if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
				|| ThemeConstants.LOCKER_FEATURED_THEME_ID == infoBean.getBeanType()) {
			mIsLauncherFeature = true;
		} else {
			mIsLauncherFeature = false;
		}

		if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID) {
			mIsSpecTheme = true;
		}
		ArrayList<ThemeInfoBean> beanList = new ArrayList<ThemeInfoBean>();
		for (ThemeInfoBean bean : mThemeDatas) {
			beanList.add(bean);
			if (beanList.size() == mItemThemeCount) {
				mThemeDataArrays.add(beanList.toArray(new ThemeInfoBean[] {}));
				beanList.clear();
			}
		}
		if (beanList.size() > 0) {
			mThemeDataArrays.add(beanList.toArray(new ThemeInfoBean[] {}));
			beanList.clear();
			beanList = null;
		}
		int count = mThemeDataArrays.size();
		mIsOverScreen = mSpaceCalculator.isOverscreen(count, isShowBanner());
		notifyDataSetChanged();
	}

	/**
	 * 清空数据
	 * */
	public void clearThemeDatas() {
		if (mThemeDataArrays != null) {
			mThemeDataArrays.clear();
			mThemeDataArrays = null;
			notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		int count = 0;
		if (mThemeDataArrays == null || mThemeDataArrays.isEmpty()) {
			return 0;
		} else {
			count = mThemeDataArrays.size();
			if (isShowBanner()) {
				++count;
			}
			if (mIsSpecTheme) {
				return count;
			}
			if (mIsOverScreen) {
				return count + 1;
			}
			int themeListItemCount = count;
			if (isShowBanner()) {
				--themeListItemCount;
			}
			if (mSpaceCalculator.calculateIsCover(themeListItemCount, isShowBanner())) {
				return count + 1;
			}
		}

		return count;
	}

	@Override
	public Object getItem(int position) {

		return mThemeDataArrays == null ? null : mThemeDataArrays.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// 当第一个的时候&&当主题Banner里面有缓存数据的时候
		if (position == 0 && isShowBanner()) {

			initThemeBannerView();
			ImageView img = (ImageView) mBannerLayout.findViewById(R.id.banner_view);
			if (mBannerBean != null) {
				BannerElement element = mBannerBean.mElements.get(0);
				String id = null;
				for (int i = 0; element.mImgids != null && i < element.mImgids.length; i++) {
					Bitmap bmp = null;
					id = element.mImgids[i];
					bmp = mBannerImageMap.get(id);
					if (element.mSource == 0 || element.mImgUrl == null
							|| element.mImgUrl.isEmpty()
							&& (element.mImgids != null && element.mImgids.length > 0)) {
						if (bmp == null) {
							Drawable drawable = ThemeImageManager.getInstance(mContext)
									.getImageById(id, LauncherEnv.Path.GOTHEMES_PATH + "icon/",
											this);
							if (drawable != null) {
								bmp = ((BitmapDrawable) drawable).getBitmap();
							}
						}
					} else if (element.mImgUrl != null && !element.mImgUrl.isEmpty()) {
						String url = element.mImgUrl.get(0);
						if (bmp == null) {

							Drawable drawable = ThemeImageManager.getInstance(mContext)
									.getImageByUrl(url, this,
											LauncherEnv.Path.GOTHEMES_PATH + "icon/", id);
							if (drawable != null) {
								bmp = ((BitmapDrawable) drawable).getBitmap();
							}
						}
					}
					if (bmp != null) {
						mBannerImageMap.put(id, bmp);
					}
					if (bmp != null) {
						img.setImageBitmap(bmp);
						break;
					}
				}
			}
			return mBannerLayout;
		}
		if (isShowGoStore(position)) {
			return mGoStoreLayout;
		} else {
			if (isShowBanner()) {
				position = position - 1;
			}
			ItemThemeScanView itemScan = null;
			ThemeInfoBean[] itemBeanArrays; // 数据
			if (mThemeDataArrays != null) {
				itemBeanArrays = mThemeDataArrays.get(position);
			} else {
				itemBeanArrays = new ThemeInfoBean[mItemThemeCount];
			}
			if (convertView != null && convertView instanceof ItemThemeScanView) {
				// itemview重用
				itemScan = (ItemThemeScanView) convertView;
				int height = mSpaceCalculator.calculateItemThemeScanViewHeight();
				if (height != itemScan.getHeight()) {
					ViewGroup.LayoutParams params = itemScan.getLayoutParams();
					params.height = height;
					itemScan.setLayoutParams(params);
				}
				// itemScan.cleanup();
				ArrayList<ItemThemeView> itemThemeViews = itemScan.getmItemThemeViews();
				if (itemThemeViews.size() == itemBeanArrays.length) {
					itemScan.cleanupItemThemeView();
					for (int m = 0; m < itemBeanArrays.length; m++) {
						ItemThemeView itemThemeView = itemThemeViews.get(m);
						itemThemeView.setItemThemeViewProperties();
						setDownloadPriveView(itemThemeViews.get(m), itemBeanArrays[m]);

						itemThemeViews.get(m).setThemeData(itemBeanArrays[m],
								mItemThemeCount * position + m + 1);
					}
					return itemScan;
				} else {
					itemScan.cleanup();
				}
			} else {
				itemScan = inflateConvertView();
			}

			for (int m = 0; m < itemBeanArrays.length; m++) {
				itemScan.addItemView(inflateItemThemeView(itemBeanArrays[m], mItemThemeCount
						* position + m + 1));
			}
			return itemScan;
		}
	}
	/**
	 * 生成listview的item项视图
	 * */
	private ItemThemeScanView inflateConvertView() {
		android.widget.AbsListView.LayoutParams layoutParams = new android.widget.AbsListView.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		ItemThemeScanView itemScan = new ItemThemeScanView(mContext, !mIsSpecTheme);
		layoutParams.height = mSpaceCalculator.calculateItemThemeScanViewHeight();
		itemScan.setLayoutParams(layoutParams);
		return itemScan;
	}

	/**
	 * 生成每个主题的显示视图
	 * */
	private ItemThemeView inflateItemThemeView(ThemeInfoBean bean, int position) {
		ItemThemeView itemView = (ItemThemeView) mLayoutInflater.inflate(R.layout.item_theme_view,
				null);
		itemView.setThemeData(bean, position);
		if (bean != null && bean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID) {
			ImageView img = (ImageView) itemView.findViewById(R.id.image);
			if (img != null) {
				img.setBackgroundResource(R.drawable.spec_theme_bg);
			}
		}
		if (mIsDealSpecialResolution) {
			try {
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						mSpecialResolutionWidth, mSpecialResolutionHight);
				ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
				imageView.setLayoutParams(params);
			} catch (Exception e) {
			}
		}
		setDownloadPriveView(itemView, bean);
		return itemView;

	}

	/**
	 * 横竖屏切换
	 * */
	public void changeOrientation() {
		mItemThemeCount = SpaceCalculator.getThemeListItemCount();
		// if (SpaceCalculator.sPortrait) {
		// mItemThemeCount = ITEM_DATA_COUNT_V;
		// } else {
		// mItemThemeCount = ITEM_DATA_COUNT_H;
		// }

	}

	/**
	 * <br>
	 * 功能简述:控制下载按钮的显示以及点击响应 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param itemView
	 * @param infoBean
	 */
	private void setDownloadPriveView(ItemThemeView itemView, ThemeInfoBean infoBean) {
		if (mPurchaseManager == null || itemView == null || infoBean == null) {
			return;
		}
		boolean isVip = ThemePurchaseManager.getCustomerLevel(mContext) == ThemeConstants.CUSTOMER_LEVEL0
				? false
				: true;
		boolean isSpecTheme = infoBean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID;
		//		Button button = (Button) itemView.findViewById(R.id.imgbtn_get);
		TextView button = (TextView) itemView.findViewById(R.id.imgbtn_get);
		ProgressBar progressBar = (ProgressBar) itemView
				.findViewById(R.id.theme_detail_download_progress);
		View image = itemView.findViewById(R.id.image);
		Drawable drawable = null;
		//		int padding = mContext.getResources().getDimensionPixelSize(R.dimen.mytheme_button_drawable_padding);
		if (mTabType == TAB_FEATURED) {
			if (button.getVisibility() != View.VISIBLE
					&& !isShowProgressBar(infoBean.getFeaturedId())) {
				button.setVisibility(View.VISIBLE);
			}
			drawable = button.getBackground();
			if (mPurchaseManager.hasDownloaded(infoBean.getThemeName(), infoBean.getPackageName())) {
				if (isSpecTheme) {
					button.setBackgroundResource(R.drawable.spec_theme_btn_selector);
					//					button.setGravity(Gravity.CENTER);
				} else {
					drawable = mContext.getResources().getDrawable(R.drawable.item_theme_apply);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					// 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
					//					button.setCompoundDrawablePadding(padding);
					button.setCompoundDrawables(drawable, null, null, null);
				}

				button.setText(R.string.theme_pages_apply);
				progressBar.setVisibility(View.GONE);
			} else if (isShowProgressBar(infoBean.getFeaturedId())) {
				if (progressBar.getVisibility() != View.VISIBLE) {
					progressBar.setVisibility(View.VISIBLE);
				}
				button.setVisibility(View.GONE);
				updateProgressBar(progressBar, infoBean.getFeaturedId());
			} else if (isVip
					|| ThemePurchaseManager.queryPurchaseState(mContext, infoBean.getPackageName()) != null) {
				progressBar.setVisibility(View.GONE);
				if (isSpecTheme) {
					button.setBackgroundResource(R.drawable.spec_theme_btn_selector);
					//					button.setGravity(Gravity.CENTER);
				} else {
					//					button.setBackgroundResource(R.drawable.item_theme_get_bg_selector);
					//					button.setPadding(padding.left, 0, 0, 0);
					drawable = mContext.getResources().getDrawable(R.drawable.item_theme_get_now);
					drawable.setBounds(0, 0, drawable.getMinimumWidth(),
							drawable.getMinimumHeight());
					//					button.setCompoundDrawablePadding(padding);
					button.setCompoundDrawables(drawable, null, null, null);
				}
				button.setText(R.string.theme_detail_download);
			} else {
				progressBar.setVisibility(View.GONE);
				if (isSpecTheme) {
					button.setBackgroundResource(R.drawable.spec_theme_btn_selector);
					//					button.setGravity(Gravity.CENTER);
					button.setText(R.string.theme_featured_get);
				} else {
					if (infoBean.getFeeType() == ThemeInfoBean.FEETYPE_GETJAR
							&& !infoBean.isZipTheme()) {
						drawable = mContext.getResources()
								.getDrawable(R.drawable.item_theme_getjar);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						//						button.setCompoundDrawablePadding(padding);
						button.setCompoundDrawables(drawable, null, null, null);
						button.setText(R.string.theme_detail_getfree); //getjar
					} else if (infoBean.getFeeType() == ThemeInfoBean.FEETYPE_PAID) {
						drawable = mContext.getResources().getDrawable(R.drawable.item_theme_buy);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						//						button.setCompoundDrawablePadding(padding);
						button.setCompoundDrawables(drawable, null, null, null);
						button.setText(R.string.theme_detail_buynow); //收费
					} else if (infoBean.getFeeType() == ThemeInfoBean.FEETYPE_FREE) {
						drawable = mContext.getResources().getDrawable(
								R.drawable.item_theme_get_now);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						//						button.setCompoundDrawablePadding(padding);
						button.setCompoundDrawables(drawable, null, null, null);
						button.setText(R.string.theme_detail_download); //免费
					} else if (infoBean.getPayType() != null && infoBean.getPayType().size() > 1) {
						drawable = mContext.getResources().getDrawable(
								R.drawable.item_theme_download);
						drawable.setBounds(0, 0, drawable.getMinimumWidth(),
								drawable.getMinimumHeight());
						//						button.setCompoundDrawablePadding(padding);
						button.setCompoundDrawables(drawable, null, null, null);
						button.setText(R.string.theme_detail_getnow); //多种收费
					}
				}
			}
			itemView.setOnClickListener(null);
			button.setClickable(true);
			button.setOnClickListener(mItemControlClickListener);
			button.setTag(itemView);
			if (mContext instanceof BannerDetailActivity) {
				SpecThemeViewConfig config = ((BannerDetailActivity) mContext).getViewConfig();
				if (config != null) {
					button.setTextColor(config.mBtnTextColor);
				} else {
					button.setTextColor(Color.BLACK);
				}
			}
			image.setClickable(true);
			image.setTag(itemView);
			image.setOnClickListener(mItemImageClickListener);
			// }
		} else {
			//modified by liulixia 本地主题也添加button，直接应用主题
			if (progressBar.getVisibility() != View.GONE) {
				progressBar.setVisibility(View.GONE);
			}
			if (button.getVisibility() != View.VISIBLE) {
				button.setVisibility(View.VISIBLE);
			}
			drawable = mContext.getResources().getDrawable(R.drawable.item_theme_apply);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
			// 调用setCompoundDrawables时，必须调用Drawable.setBounds()方法,否则图片不显示
			//			button.setCompoundDrawablePadding(padding);
			button.setCompoundDrawables(drawable, null, null, null);
			button.setText(R.string.theme_pages_apply);
			button.setClickable(true);
			button.setOnClickListener(mItemControlClickListener);
			button.setTag(itemView);

			itemView.setOnClickListener(mItemClickListener);
			image.setOnClickListener(null);
			image.setClickable(false);
		}
	}

	private void initClickListener() {
		mItemControlClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ItemThemeView itemThemeView = (ItemThemeView) v.getTag();
				ThemeInfoBean infoBean = itemThemeView.getThemeData();

				//				if (infoBean.isInAppPay()) {
				//					mPurchaseManager.handleInAppClick(infoBean, (Activity) mContext,
				//							itemThemeView.getmPosition());
				//				} else {
				//					mPurchaseManager.handleNormalFeaturedClickEvent(mContext, infoBean,
				//							itemThemeView.getmPosition());
				//				}
				//				
				//				PreferencesManager pm = new PreferencesManager(mContext,
				//						IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
				//				boolean bool = pm.getBoolean(IPreferencesIds.HAD_SHOW_VIP_TIP, false);
				//				if (!bool
				//						&& infoBean.getFeeType() != ThemeInfoBean.FEETYPE_FREE
				//						&& ThemePurchaseManager.getCustomerLevel(mContext) == ThemeConstants.CUSTOMER_LEVEL0) {
				//					ThemePurchaseManager.getInstance(mContext).savePaidThemePkg(
				//							infoBean.getPackageName());
				//				}
				if (infoBean.getFeaturedId() != 0) {
					if (infoBean.isInAppPay()) {
						mPurchaseManager.handleInAppClick(infoBean, (Activity) mContext,
								itemThemeView.getmPosition());
					} else {
						mPurchaseManager.handleNormalFeaturedClickEvent(mContext, infoBean,
								itemThemeView.getmPosition());
					}
					PreferencesManager pm = new PreferencesManager(mContext,
							IPreferencesIds.FEATUREDTHEME_CONFIG, Context.MODE_PRIVATE);
					boolean bool = pm.getBoolean(IPreferencesIds.HAD_SHOW_VIP_TIP, false);
					if (!bool
							&& infoBean.getFeeType() != ThemeInfoBean.FEETYPE_FREE
							&& ThemePurchaseManager.getCustomerLevel(mContext) == ThemeConstants.CUSTOMER_LEVEL0) {
						ThemePurchaseManager.getInstance(mContext).savePaidThemePkg(
								infoBean.getPackageName());
					}
				} else {
					// 应用
					String type = infoBean.getThemeType();
					if (null != type && type.equals(ThemeInfoBean.THEMETYPE_GETJAR)
							&& AppUtils.isAppExist(mContext, infoBean.getPackageName())) {
						Intent intent = new Intent();
						int level = ThemePurchaseManager.getCustomerLevel(mContext);
						intent = mContext.getPackageManager().getLaunchIntentForPackage(
								infoBean.getPackageName());
						if (level != ThemeConstants.CUSTOMER_LEVEL0) {

							intent.putExtra("viplevel", level);
						}
						mContext.startActivity(intent);
					} else {
						if (infoBean.getBeanType() == ThemeConstants.LOCKER_INSTALLED_THEME_ID) { //锁屏主题
							String curPackageName = infoBean.getPackageName();
							if (curPackageName != null) {
								String oldPackageName = ThemeManager.getInstance(mContext)
										.getCurLockerTheme();
								new GoLockerThemeManager(mContext).changeLockTheme(curPackageName);
								ArrayList<ThemeInfoBean> beanList = updateInstalledLockerList(
										curPackageName, oldPackageName);
								setThemeDatas(beanList, mBannerBean);
							}
						} else {
							applyTheme(infoBean);
						}
					}
					StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_LOCAL_THEME_APPLY);
				}
			}

		};

		mItemImageClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ItemThemeView itemThemeView = (ItemThemeView) v.getTag();
				ThemeInfoBean infoBean = itemThemeView.getThemeData();
				int staticsType = 0;
				if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
					staticsType = GuiThemeStatistics.THEME_LAUNCHER_TYPE;

				} else {
					staticsType = GuiThemeStatistics.THEME_LOCKER_TYPE;

				}
				if (infoBean.getFeaturedId() != 0) {
					gotoFeaturedThemeDetailPage(infoBean);
					StatisticsData
							.countThemeTabData(StatisticsData.THEME_TAB_ID_CHOICENESS_THEME_DETAIL);
					int srcType = 0;
					if (infoBean.isInAppPay()) {
						srcType = 1;
					}
					int tabId = 0;
					if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
						tabId = ThemeConstants.STATICS_ID_FEATURED;
					} else if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
						tabId = ThemeConstants.STATICS_ID_HOT;
					} else if (infoBean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID) {
						tabId = infoBean.getSortId();
					} else if (infoBean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
						tabId = ThemeConstants.STATICS_ID_LOCKER;
					}
					GuiThemeStatistics.getInstance(mContext).saveUserDetailClick(mContext,
							infoBean.getPackageName(), itemThemeView.getmPosition(), staticsType,
							String.valueOf(srcType), String.valueOf(tabId));
				} else {
					mPurchaseManager.handleNormalFeaturedClickEvent(mContext, infoBean,
							itemThemeView.getmPosition());
				}
			}
		};

	}

	/**
	 * 应用主题
	 */
	private void applyTheme(ThemeInfoBean mInfoBean) {
		if (mInfoBean == null) {
			return;
		}
		String pkgName = mInfoBean.getPackageName();
		if (pkgName.equals(ThemeManager.getInstance(mContext).getCurThemePackage())) {
			Toast.makeText(mContext, R.string.theme_already_using, Toast.LENGTH_SHORT).show();
			return;
		}
		if (!mInfoBean.isNewTheme()) {
			// 不是大主题
			Intent intentGoLauncher = new Intent();
			intentGoLauncher.setClass(mContext, GoLauncher.class);
			mContext.startActivity(intentGoLauncher);
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, MyThemeReceiver.CHANGE_THEME);
			intent.putExtra(MyThemeReceiver.PKGNAME_STRING, pkgName);
			mContext.sendBroadcast(intent);
			ThemeManageActivity.exit();
		} else {
			// 大主题
			initNewThemeResource();
			showDialog(pkgName, mInfoBean);
		}
	}

	private AlertDialog mDialog;
	private String mGolauncherText;
	private String mGowidgetText;
	private String mGolockText;

	/**
	 * 初始化大主题需要的一些资源
	 * */
	private void initNewThemeResource() {

		mGolauncherText = mContext.getResources().getString(R.string.new_theme_golauncher);
		mGowidgetText = mContext.getResources().getString(R.string.new_theme_gowidget);
		mGolockText = mContext.getResources().getString(R.string.new_theme_golock);

	}

	private void showDialog(final String pkgName, final ThemeInfoBean mInfoBean) {

		LayoutInflater factory = LayoutInflater.from(mContext);
		View view = factory.inflate(R.layout.theme_detail_alertdialog, null);

		final MyAdapter myAdapter = new MyAdapter(mContext, mInfoBean);
		myAdapter.filterNotExistTheme();

		ListView myListView = (ListView) view.findViewById(R.id.Theme_detail_alertdialog_list);
		myListView.setAdapter(myAdapter);
		myListView.setCacheColorHint(Color.TRANSPARENT);
		myListView.setDivider(null);

		Button button = (Button) view.findViewById(R.id.theme_detail_alertdialog_sure);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub
				Context context = mContext;
				if (mInfoBean != null && context != null) {
					// 存在桌面主题且被选中
					if (mInfoBean.ismExistGolauncher()
							&& myAdapter.getmCheckBoxState().get(mGolauncherText)) {
						Intent it = new Intent();
						it.setClass(context, GoLauncher.class);
						context.startActivity(it);

						Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
						intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING,
								MyThemeReceiver.CHANGE_THEME);
						intent.putExtra(MyThemeReceiver.PKGNAME_STRING, pkgName);
						context.sendBroadcast(intent);
					}
					// 存在widget主题且被选中
					if (mInfoBean.getGoWidgetPkgName() != null
							&& myAdapter.getmCheckBoxState().get(mGowidgetText)) {
						Intent it = new Intent(ICustomAction.ACTION_CHANGE_WIDGETS_THEME);
						it.putExtra(ICustomAction.WIDGET_THEME_KEY, pkgName);
						context.sendBroadcast(it);
					}
					// 存在GO锁屏主题且被选中
					if (mInfoBean.ismExistGolock()
							&& myAdapter.getmCheckBoxState().get(mGolockText)) {
						if (AppUtils.isGoLockerExist(context)) {
							try {
								String newThemePkgName = mInfoBean.getPackageName();
								if (newThemePkgName != null) {
									Intent it = new Intent(
											ICustomAction.ACTION_SEND_TO_GOLOCK_FOR_THEME_DETAIL);
									it.putExtra(ThemeDetailView.NEW_THEME_KEY, newThemePkgName);
									context.sendBroadcast(it);
								}
							} catch (Exception e) {
							}
						} else {

						}
					}
				}
				dimissDialog();
			}
		});

		mDialog = new AlertDialog.Builder(mContext).create();
		mDialog.show();

		WindowManager.LayoutParams layoutParams = mDialog.getWindow().getAttributes();
		layoutParams.width = android.view.ViewGroup.LayoutParams.FILL_PARENT;
		layoutParams.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

		mDialog.getWindow().setGravity(Gravity.CENTER);
		mDialog.getWindow().setAttributes(layoutParams);

		Window window = mDialog.getWindow();
		window.setBackgroundDrawableResource(R.drawable.theme_detail_menu_bg);
		window.setContentView(view);

	}

	private void dimissDialog() {
		mDialog.dismiss();
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-9-13]
	 */
	class MyAdapter extends BaseAdapter {
		private ArrayList<String> mNewThemeTips = null;
		private HashMap<String, Boolean> mCheckBoxState = null;
		private LayoutInflater mInflater;
		ThemeInfoBean mInfoBean = null;

		public MyAdapter(Context context, ThemeInfoBean infoBean) {

			this.mInflater = LayoutInflater.from(context);
			mInfoBean = infoBean;
			mNewThemeTips = new ArrayList<String>();
			mCheckBoxState = new HashMap<String, Boolean>();
			mNewThemeTips.add(mGolauncherText);
			mNewThemeTips.add(mGowidgetText);
			mNewThemeTips.add(mGolockText);

			for (int i = 0; i < mNewThemeTips.size(); i++) {
				// 默认checkbox状态为选中
				mCheckBoxState.put(mNewThemeTips.get(i), true);
			}
		}

		public HashMap<String, Boolean> getmCheckBoxState() {
			return mCheckBoxState;
		}

		public void filterNotExistTheme() {
			if (mInfoBean != null) {
				if (!mInfoBean.ismExistGolauncher()) {
					mNewThemeTips.remove(mGolauncherText);
					mInfoBean.getNewThemeInfo().getNewThemePkg()
							.remove(ThemeDetailView.GOLAUNCHER_ACTION);
				}
				if (!mInfoBean.ismExistGolock()) {
					mNewThemeTips.remove(mGolockText);
					mInfoBean.getNewThemeInfo().getNewThemePkg()
							.remove(ThemeDetailView.GOLOCK_ACTION);
				}
				if (mInfoBean.getGoWidgetPkgName() == null) {
					mNewThemeTips.remove(mGowidgetText);
					mInfoBean.getNewThemeInfo().getNewThemePkg()
							.remove(ThemeDetailView.GOWIDGET_ACTION);
				}
			}
		}

		@Override
		public int getCount() {
			return mNewThemeTips.size();
		}

		@Override
		public Object getItem(int position) {
			return mNewThemeTips.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = null;
			CheckBox checkBox = null;
			Button downloadButton = null;
			if (convertView != null) {
				textView = (TextView) convertView.findViewById(R.id.new_theme_tip);
				checkBox = (CheckBox) convertView.findViewById(R.id.new_theme_checkbox);
				downloadButton = (Button) convertView.findViewById(R.id.new_theme_download_button);
			} else {
				convertView = mInflater.inflate(R.layout.new_theme_tips_item, null);
				textView = (TextView) convertView.findViewById(R.id.new_theme_tip);
				checkBox = (CheckBox) convertView.findViewById(R.id.new_theme_checkbox);
				downloadButton = (Button) convertView.findViewById(R.id.new_theme_download_button);
			}
			textView.setText(mNewThemeTips.get(position));
			DeskSettingConstants.setTextViewTypeFace(textView);
			DeskSettingConstants.setTextViewTypeFace(downloadButton);
			final int pos = position;
			// final String newPkg =
			// mInfoBean.getNewThemeInfo().getNewThemePkg().get(position);
			final String newPkg = getNewPkg(position);
			if (newPkg != null && !newPkg.trim().equals("")) {
				Intent intent = new Intent(newPkg);
				if (!AppUtils.isAppExist(mContext, intent)) {
					checkBox.setVisibility(View.GONE);
					downloadButton.setVisibility(View.VISIBLE);
				} else {
					checkBox.setVisibility(View.VISIBLE);
					downloadButton.setVisibility(View.GONE);
				}
			}

			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					mCheckBoxState.put(mNewThemeTips.get(pos), isChecked);
				}
			});

			downloadButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (newPkg != null && !newPkg.trim().equals("")) {
						if (newPkg.trim().equals(ThemeDetailView.GOWIDGET_ACTION)) {
							Intent toGoWidget = new Intent(ICustomAction.ACTION_GOTO_GOWIDGET_FRAME);
							mContext.sendBroadcast(toGoWidget);
							// 退出主题预览
							Intent goLauncherIntent = new Intent(mContext, GoLauncher.class);
							mContext.startActivity(goLauncherIntent);
							ThemeDetailActivity.exit();
						} else if (newPkg.trim().equals(ThemeDetailView.GOLOCK_ACTION)) {
							AppsDetail.gotoDetailDirectly(mContext,
									AppsDetail.START_TYPE_APPRECOMMENDED, newPkg);
							//							GoStoreOperatorUtil.gotoStoreDetailDirectly(getContext(), newPkg);
						}
					}
					dimissDialog();
				}
			});

			return convertView;
		}

		private String getNewPkg(int position) {
			String newPkg = null;
			try {
				newPkg = mInfoBean.getNewThemeInfo().getNewThemePkg().get(position);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return newPkg;
		}

	}

	private ThemeInfoBean geThemeInfoBean(String pkgName) {
		if (mThemeDataArrays == null || pkgName == null) {
			return null;
		}
		ArrayList<ThemeInfoBean[]> list = (ArrayList<ThemeInfoBean[]>) mThemeDataArrays.clone();
		for (int i = 0; i < list.size(); i++) {
			ThemeInfoBean[] themeInfoBeans = list.get(i);
			for (int j = 0; j < themeInfoBeans.length; j++) {
				ThemeInfoBean infoBean = themeInfoBeans[j];
				if (infoBean != null && infoBean.getPackageName() != null
						&& infoBean.getPackageName().equals(pkgName)) {
					list.clear();
					return infoBean;
				}
			}
		}
		list.clear();
		return null;
	}

	private void gotoFeaturedThemeDetailPage(ThemeInfoBean infoBean) {
		Intent intent = new Intent(mContext, ThemeDetailActivity.class);
		intent.putExtra(ThemeConstants.DETAIL_MODEL_EXTRA_KEY,
				ThemeConstants.DETAIL_MODEL_FEATURED_EXTRA_VALUE);
		intent.putExtra(ThemeConstants.PACKAGE_NAME_EXTRA_KEY, infoBean.getPackageName());
		intent.putExtra(ThemeConstants.DETAIL_ID_EXTRA_KEY, infoBean.getFeaturedId());
		intent.putExtra(ThemeConstants.TITLE_EXTRA_KEY, infoBean.getThemeName());
		mContext.startActivity(intent);
	}

	private void initDownloadReceiver() {
		mDownloadReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (ICustomAction.ACTION_UPDATE_DOWNLOAD_PERCENT.equals(action)) {
					Bundle data = intent.getExtras();
					if (data != null) {
						long appId = data.getInt(AppsThemeDetailActivity.DOWNLOADING_APP_ID);
						int percent = data.getInt(AppsThemeDetailActivity.PERSENT_KEY);
						if (isDownLoadTheme(appId) && mDoloadingMap != null) {
							mDoloadingMap.put(appId, percent);
							notifyDataSetChanged();
						}
					}
				} else if (ICustomAction.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
					Bundle data = intent.getExtras();
					if (data != null) {
						long appId = data.getInt(AppsThemeDetailActivity.DOWNLOADING_APP_ID);
						if (mDoloadingMap != null && mDoloadingMap.containsKey(appId)) {
							mDoloadingMap.remove(appId);
							notifyDataSetChanged();
							ThemeInfoBean bean = getDownloadThemeInfo(appId);
							if (bean != null && bean.isInAppPay()) {
								Intent it = new Intent(ICustomAction.ACTION_NEW_THEME_INSTALLED);
								it.setData(Uri.parse("package://"));
								context.sendBroadcast(it);
							}
						}
					}
				} else if (ICustomAction.ACTION_UPDATE_DOWNLOAD_STOP.equals(action)
						|| ICustomAction.ACTION_UPDATE_DOWNLOAD_FAILED.equals(action)
						|| ICustomAction.ACTION_DOWNLOAD_DESTROY.equals(action)) {
					Bundle data = intent.getExtras();
					if (data != null) {
						long appId = data.getInt(AppsThemeDetailActivity.DOWNLOADING_APP_ID);
						if (mDoloadingMap != null) {
							mDoloadingMap.remove(appId);
							notifyDataSetChanged();
						}
					}
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_DOWNLOAD_COMPLETE);
		intentFilter.addAction(ICustomAction.ACTION_UPDATE_DOWNLOAD_PERCENT);
		intentFilter.addAction(ICustomAction.ACTION_UPDATE_DOWNLOAD_STOP);
		intentFilter.addAction(ICustomAction.ACTION_UPDATE_DOWNLOAD_FAILED);
		intentFilter.addAction(ICustomAction.ACTION_DOWNLOAD_DESTROY);
		mContext.registerReceiver(mDownloadReceiver, intentFilter);
	}

	private boolean isShowProgressBar(long appId) {
		if (null != mDoloadingMap) {
			return mDoloadingMap.containsKey(appId);
		} else {
			return false;
		}
	}

	public void recyle() {
		mBannerImageMap.clear();
		mContext.unregisterReceiver(mDownloadReceiver);
		mPurchaseManager.unRegisterObserver(this);
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, Object object2) {
		// TODO Auto-generated method stub
		switch (msgId) {
			case IDiyMsgIds.THEME_INAPP_PAID_FINISHED :
				Message msg = Message.obtain();
				msg.what = msgId;
				msg.arg1 = param;
				msg.obj = object;
				mHandler.sendMessage(msg);
				break;
			case ThemeImageManager.EVENT_NETWORK_ICON_URL_CHANGE :
			case ThemeImageManager.EVENT_NETWORK_ICON_CHANGE :
			case ThemeImageManager.EVENT_LOCAL_ICON_EXIT :
				msg = Message.obtain();
				msg.what = msgId;
				if (object != null) {
					if (object instanceof Bitmap) {
						mBannerImageMap.put((String) object2, (Bitmap) object);
					} else if (object instanceof BitmapDrawable) {
						mBannerImageMap
								.put((String) object2, ((BitmapDrawable) object).getBitmap());
					}
				}
				mHandler.sendMessage(msg);
				break;
			default :
				break;
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);
			switch (msg.what) {
				case IDiyMsgIds.THEME_INAPP_PAID_FINISHED :
					if (ThemeAppInBillingManager.PURCHASE_STATE_PURCHASED == msg.arg1) {
						ThemeInfoBean bean = geThemeInfoBean((String) msg.obj);
						mPurchaseManager.startDownload(bean);
						if (bean != null) {
							GuiThemeStatistics.getInstance(mContext).onAppInstalled(
									bean.getPackageName());
						}
					}
					removeMessages(IDiyMsgIds.THEME_INAPP_PAID_FINISHED);
				case ThemeImageManager.EVENT_NETWORK_ICON_CHANGE :
				case ThemeImageManager.EVENT_LOCAL_ICON_EXIT :
				case ThemeImageManager.EVENT_NETWORK_ICON_URL_CHANGE :
					notifyDataSetChanged();
					break;

				default :
					break;
			}

		}
	};

	private void updateProgressBar(ProgressBar progressBar, long id) {
		if (progressBar == null || mDoloadingMap == null) {
			return;
		}
		int per = mDoloadingMap.get(id);
		progressBar.setProgress(per);
	}

	/**
	 * 桌面本地主题点击事件
	 * */
	protected void deskInstalledClickEvent(String packageName) {
		Intent intent = new Intent(mContext, ThemeDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(ThemeConstants.PACKAGE_NAME_EXTRA_KEY, packageName);
		mContext.startActivity(intent);

	}

	/**
	 * <br>功能简述:检查banner是否显示
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private boolean isShowBanner() {
		boolean bRet = false;
		if ((mContext instanceof ThemeManageActivity) && mIsLauncherFeature && mBannerBean != null
				&& mBannerBean.mElements != null && mBannerBean.mElements.size() > 0) {
			if (mBannerBean.mType != mThemeType) {
				return false;
			}
			BannerElement element = mBannerBean.mElements.get(0);
			if (element.mSDate != null && element.mEDate != null) {
				if (element.mPkgs != null) {
					for (String pkg : element.mPkgs) {
						if (AppUtils.isAppExist(mContext, pkg)) {
							return false;
						}

					}
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date();
				String today = sdf.format(date);
				if (today.compareTo(element.mSDate) >= 0 && today.compareTo(element.mEDate) <= 0) {
					bRet = true;
				}
			}
		}
		return bRet;
	}

	private boolean isShowGoStore(int position) {
		if (mThemeDataArrays == null || mIsSpecTheme) {
			return false;
		}
		int itemCount = mThemeDataArrays.size();
		if (position == getCount() - 1
				&& (mIsOverScreen || mSpaceCalculator.calculateIsCover(itemCount, isShowBanner()) || (getCount() == position))) {
			return true;
		}
		return false;
	}

	private boolean isDownLoadTheme(long downloadId) {
		if (mThemeDataArrays != null) {
			for (ThemeInfoBean[] infoArray : mThemeDataArrays) {
				for (int i = 0; i < infoArray.length; i++) {
					ThemeInfoBean bean = infoArray[i];
					if (bean.getFeaturedId() == downloadId) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private ThemeInfoBean getDownloadThemeInfo(long downloadId) {
		if (mThemeDataArrays != null) {
			for (ThemeInfoBean[] infoArray : mThemeDataArrays) {
				for (int i = 0; i < infoArray.length; i++) {
					ThemeInfoBean bean = infoArray[i];
					if (bean.getFeaturedId() == downloadId) {
						return bean;
					}
				}
			}
		}

		return null;
	}
}
