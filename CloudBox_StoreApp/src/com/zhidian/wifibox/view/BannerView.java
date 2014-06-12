package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppDetailActivity;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.adapter.AdvAdapter;
import com.zhidian.wifibox.controller.BannerController;
import com.zhidian.wifibox.data.BannerDataBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 幻灯片组件，逻辑处理通过BannerController完成
 * 
 * @author xiedezhi
 * 
 */
public class BannerView extends RelativeLayout implements OnGestureListener {

	private AutoScrollViewPager adViewPager = null;// 幻灯片滑动组件
	private ViewGroup viewGroup = null; // 点
	private LinearLayout.LayoutParams warp_warp;
	private List<View> imgeViews = new ArrayList<View>(); // 要显示的广告图List
	private ImageView[] pointViews = null; // 要显示的 点
	private AdvAdapter advAdapter;
	/**
	 * 手势滑动处理类
	 */
	private GestureDetector mDetector = new GestureDetector(
			TAApplication.getApplication(), this);

	public BannerView(Context context) {
		super(context);
	}

	public BannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 初始化 View
	 */
	@Override
	protected void onFinishInflate() {
		adViewPager = (AutoScrollViewPager) findViewById(R.id.adv_roll_viewpager);
		viewGroup = (ViewGroup) findViewById(R.id.adv_roll_viewGroup);

		warp_warp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		warp_warp.setMargins(2, 0, 2, 0);

		imgeViews = new ArrayList<View>();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}

	/**
	 * controller数据回调接口
	 */
	private TAIResponseListener mRListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			PageDataBean bean = (PageDataBean) response.getData();
			if (bean.mStatuscode == 0) {
				// 先缓存列表
				TabDataManager.getInstance().cachePageData(bean);
				// 更新列表
				initViewPager(bean.mBannerList);
			}
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onRuning(TAResponse response) {
		}

		@Override
		public void onFinish() {
		}

		@Override
		public void onFailure(TAResponse response) {
		}
	};

	/**
	 * 初始化
	 * 
	 * @param mark
	 *            用于读取数据的mark值
	 * @param loadLocalDataFirst
	 *            先加载本地数据
	 */
	public void init(String mark, boolean loadLocalDataFirst) {
		// 只有缓存没有数据，loadLocalDataFirst才有效
		String url = CDataDownloader.getBannerUrl(mark);
		PageDataBean bean = TabDataManager.getInstance().getPageData(url);
		if (bean == null) {
			String[] obj = { url, url, loadLocalDataFirst + "" };
			TARequest request = new TARequest(BannerController.LOAD_DATA, obj);
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.bannercontroller), request,
					mRListener, true, false);
		} else {
			// 展示数据
			initViewPager(bean.mBannerList);
		}
	}

	/*************************
	 * 展示幻灯片
	 * 
	 * @param mBannerList
	 *************************/
	private void initViewPager(List<BannerDataBean> mBannerList) {
		imgeViews.clear();
		for (int i = 0; i < mBannerList.size(); i++) {
			BannerDataBean bean = mBannerList.get(i);
			String url = bean.imgUrl;
			final ImageView imageView = new ImageView(getContext());
			imageView.setScaleType(ScaleType.FIT_XY);
			imageView.setTag(url);
			imageView.setTag(R.string.app, bean);
			// 下载，显示图片
			Bitmap bm = AsyncImageManager.getInstance().loadImage(
					PathConstant.ICON_ROOT_PATH, url.hashCode() + "", url,
					true, false, new AsyncImageLoadedCallBack() {

						@Override
						public void imageLoaded(Bitmap imageBitmap,
								String imgUrl) {
							if (imageView.getTag().equals(imgUrl)) {
								imageView.setImageBitmap(imageBitmap);
							}
						}
					});

			if (bm != null) {
				imageView.setImageBitmap(bm);
			} else {
				// // 默认
				imageView.setImageBitmap(DrawUtil.sDefaultBanner);
			}

			imgeViews.add(imageView);
		}

		advAdapter = new AdvAdapter(getContext(), imgeViews);
		adViewPager.setAdapter(advAdapter);
		adViewPager.setInterval(3000);
		adViewPager.setScrollDurationFactor(1.5);
		adViewPager
				.setSlideBorderMode(AutoScrollViewPager.SLIDE_BORDER_MODE_TO_PARENT);
		adViewPager.startAutoScroll();
		adViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				for (int i = 0; i < imgeViews.size(); i++) {
					if (i == arg0) {
						pointViews[i]
								.setBackgroundResource(R.drawable.banner_dian_focus);
					} else {
						pointViews[i]
								.setBackgroundResource(R.drawable.banner_dian_blur);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

		pointViews = new ImageView[imgeViews.size()]; // 根据广告个数，初始化点的个数
		viewGroup.removeAllViews();
		ImageView dianImage;
		for (int i = 0; i < imgeViews.size(); i++) {
			dianImage = new ImageView(getContext());
			dianImage.setLayoutParams(warp_warp);
			pointViews[i] = dianImage;
			if (i == 0) {
				pointViews[i]
						.setBackgroundResource(R.drawable.banner_dian_focus);
			} else {
				pointViews[i]
						.setBackgroundResource(R.drawable.banner_dian_blur);
			}
			viewGroup.addView(pointViews[i]);
		}
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		View v = imgeViews.get(adViewPager.getCurrentItem());
		// 点击事件
		BannerDataBean bean = (BannerDataBean) v.getTag(R.string.app);
		if (bean.type == 1) {
			// 网页
			try {
				Uri uri = Uri.parse(bean.target);
				getContext().startActivity(new Intent(Intent.ACTION_VIEW, uri));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (bean.type == 2) {
			// 应用
			try {
				long id = Long.valueOf(bean.target);
				Intent intent = new Intent(getContext(),
						AppDetailActivity.class);
				intent.putExtra("appId", id);
				getContext().startActivity(intent);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (bean.type == 3) {
			// 专题
			try {
				long id = Long.valueOf(bean.target);
				// 跳转到专题内容
				List<Object> list = new ArrayList<Object>();
				list.add(bean);
				// 通知TabManageView跳转下一层级，把TopicDataBean带过去
				MainActivity.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
						CDataDownloader.getTopicContentUrl(id, 1), list);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

}
