package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 应用游戏中心，精品推荐页的banner图展示视图
 * 
 * @author xiedezhi
 * 
 */
public class FeatureHeaderView extends LinearLayout {

	private LayoutInflater mInflater = null;

	/**
	 * banner图的点击事件
	 */
	private OnClickListener mItemClickListener = null;
	/**
	 * banner视图列表
	 */
	private List<ImageSwitcher> mItems = new ArrayList<ImageSwitcher>();

	private Drawable mDefaultBanner = null;

	public FeatureHeaderView(Context context) {
		super(context);
		init();
	}

	public FeatureHeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		this.setOrientation(LinearLayout.VERTICAL);
		mInflater = LayoutInflater.from(getContext());
	}

	private LinearLayout.LayoutParams getItemLayoutParams() {
		LinearLayout.LayoutParams ret = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT, RecommAppsUtils.dip2px(
						getContext(), 80.0f));
		return ret;
	}

	/**
	 * 设置banner图的点击事件
	 */
	public void setItemClickListener(OnClickListener listener) {
		mItemClickListener = listener;
	}

	/**
	 * 根据URL地址设置banner图
	 */
	private void setBanner(final ImageSwitcher switcher, String filepath, String filename, String url) {
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		Bitmap bm = AsyncImageManager.getInstance().loadImage(filepath, filename, url, true, true,
				null, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap bm, String url) {
						ImageView imageView = (ImageView) switcher.getNextView();
						imageView.setBackgroundDrawable(null);
						switcher.setImageDrawable(new BitmapDrawable(bm));
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null) {
			imageView.setBackgroundDrawable(null);
			imageView.setImageBitmap(bm);
		} else {
			setDefaultBanner(imageView);
		}
	}

	/**
	 * 根据数据列表初始化view
	 */
	public void fillUp(List<BoutiqueApp> list) {
		this.removeAllViews();
		mItems.clear();
		if (list == null || list.size() <= 0) {
			return;
		}
		for (BoutiqueApp app : list) {
			// 初始化图片保存路径和文件名
			String icon = app.pic;
			if (icon == null || icon.equals("")) {
				continue;
			}
			String fileName = String.valueOf(icon.hashCode());
			app.picLocalPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
			app.picLocalFileName = fileName;
		}
		// 每个view的tag都设为对应的app
		for (int i = 0; i < list.size() && i + 1 < list.size(); i += 2) {
			View items = mInflater.inflate(R.layout.apps_mgr_feature_iphonestyle_singlecell, null);

			BoutiqueApp app1 = list.get(i);
			ImageSwitcher item1 = (ImageSwitcher) items.findViewById(R.id.iphonestyle_singlecell_imageswitcher1);
			item1.setTag(app1);
			item1.setOnClickListener(mItemClickListener);
			mItems.add(item1);
			//加载图标
			setBanner(item1, app1.picLocalPath, app1.picLocalFileName, app1.pic);

			BoutiqueApp app2 = list.get(i + 1);
			ImageSwitcher item2 = (ImageSwitcher) items.findViewById(R.id.iphonestyle_singlecell_imageswitcher2);
			item2.setTag(app2);
			item2.setOnClickListener(mItemClickListener);
			mItems.add(item2);
			// 加载图标
			setBanner(item2, app2.picLocalPath, app2.picLocalFileName, app2.pic);
			LinearLayout.LayoutParams params = getItemLayoutParams();
			if (i == 0) {
				// 第一组
				params.topMargin = RecommAppsUtils.dip2px(getContext(), 4f);
			} else {
				params.topMargin = RecommAppsUtils.dip2px(getContext(), 4f);
			}
			if (i + 2 >= list.size() || i + 3 >= list.size()) {
				// 最后一组
				params.bottomMargin = RecommAppsUtils.dip2px(getContext(), 4f);
			} else {
				params.bottomMargin = 0;
			}
			this.addView(items, params);
		}
		//加一根分割线
		View listline = new View(getContext());
		listline.setBackgroundResource(R.drawable.listline);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT, RecommAppsUtils.dip2px(
						getContext(), 0.5f));
		this.addView(listline, lp);
	}

	/**
	 * 设置默认的banner图
	 */
	private void setDefaultBanner(ImageView item) {
		if (item == null) {
			return;
		}
		if (mDefaultBanner == null) {
			// 应用中心国内的某些渠道要修改名称，修改为 安卓应用市场（中文）/GO Market（英文），所以要进行渠道控制，默认图片里面有名字，要跟着换
			// Add by wangzhuobin 2012.10.31
			int id = R.drawable.appcenter_default_banner;
			mDefaultBanner = getResources().getDrawable(id);
		}
		item.setImageBitmap(null);
		item.setBackgroundDrawable(mDefaultBanner);
	}

	/**
	 * 获取元素个数
	 */
	public int getItemCount() {
		if (mItems == null) {
			return 0;
		}
		return mItems.size();
	}

}
