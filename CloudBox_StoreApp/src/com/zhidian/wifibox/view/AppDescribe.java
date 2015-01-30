package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.ImagePagerActivity;
import com.zhidian.wifibox.adapter.AppInfoPictureAdapter;
import com.zhidian.wifibox.adapter.DefaultAdapter;
import com.zhidian.wifibox.adapter.RelatedRecommendAdapter;
import com.zhidian.wifibox.controller.DetailController;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DetailDataBean;
import com.zhidian.wifibox.data.DetailDataBean.RelatedRecommendBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.listener.AppdetailListener;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.HtmlRegexpUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 应用描述
 * 
 * @author zhaoyl
 * 
 */
public class AppDescribe implements OnClickListener {

	private Context mContext;
	private GridView gridView; // 应用图片
	private AppInfoPictureAdapter adapter;
	private List<String> shoImages; // 图片url
	private List<String> thumbImages; // 缩略图片url
	private HorizontalScrollView mScrollView;
	private int mWidthPixels;
	private int cWidth = DrawUtil.dip2px(TAApplication.getApplication(), 112); // 列宽
	private int hSpacing = DrawUtil.dip2px(TAApplication.getApplication(), 5f); // 水平间距
	private String url; // 请求链接
	private String correlationUrl; // 相关推荐url

	private ImageView ivImage; // 应用logo
	private TextView tvAppName; // 应用名称
	private TextView tvAppSize; // 应用大小
	private TextView tvVersion2; // 版本号;
	private TextView tvWarn; // 提醒
	private TextView tvDownloadTime; // 下载次数
	private TextView tvLanguage; // 语言
	private TextView tvUpdateDate; // 更新时间
	private TextView tvIntro; // 简介
	private GridView ReferralsGridView;// 相关推荐
	private RelatedRecommendAdapter reAdapter;
	private TAApplication application;
	private LinearLayout allmoreLayout; // all
	private LinearLayout AboutRecommendLayout; // 相关推荐View
	private ImageView ivOpen; // 展开图片
	private TextView tvOpenText; // 展开Text
	private AppdetailListener listener;
	private LinearLayout home_liear_pro; // 加载数据进度条
	private RelativeLayout home_liear_connent;// 加载成功后原界面要显示的内容
	private BgPageView bgPageView;

	// private InfoUtil infoUtil;

	public AppDescribe(Context context, View view, Long appId,
			AppDataBean appdataBean, TAApplication application,
			AppdetailListener listener) {
		mContext = context;
		this.application = application;
		this.listener = listener;
		url = CDataDownloader.getDetailUrl(appId);
		correlationUrl = CDataDownloader.getCorrelationlUrl(appId);
		initUI(view, appdataBean);

		// 是否有缓存数据
		DetailDataBean bean = TabDataManager.getInstance()
				.getDataAppDetail(url);
		if (bean != null) {
			showToUI(bean);
			if (listener != null) {
				listener.onShow();
			}
		} else {
			initData(appdataBean);
			getAppData();
		}

		List<RelatedRecommendBean> relatedList = TabDataManager.getInstance()
				.getDataAppRelated(correlationUrl);
		if (relatedList != null) {
			initRelatedApp(relatedList);
		} else {
			getCorrelationData();
		}

	}

	/**
	 * 初始化UI
	 * 
	 * @param appdataBean
	 */
	private void initData(AppDataBean appdataBean) {
		if (appdataBean != null) {
			showInitUI(appdataBean);
		} else {
			// bgPageView.showProgress();// 显示加载进度条
		}

	}

	/**
	 * 填充部分UI数据
	 */
	private void showInitUI(AppDataBean db) {
		tvAppName.setText(db.name);
		int size = db.size;
		tvWarn.setVisibility(View.GONE);
		tvDownloadTime.setText(FileUtil.convertDownloadTimes(db.downloads));
		tvAppSize.setText(FileUtil.convertFileSize(size));
		tvVersion2.setText(db.version);
		if (listener != null) {
			listener.getAppData(db.name);
		}

		ivImage.setTag(db.iconUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, db.iconUrl.hashCode() + "",
				db.iconUrl, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (ivImage.getTag().equals(imgUrl)) {
							ivImage.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			ivImage.setImageBitmap(bm);
		} else {
			// 默认
		}
	}

	/**********************
	 * 初始化
	 **********************/
	private void initUI(View view, AppDataBean appdataBean) {
		gridView = (GridView) view
				.findViewById(R.id.detail_describe_mGridView_pic);
		mScrollView = (HorizontalScrollView) view
				.findViewById(R.id.detail_describe_mScrollView);
		ivImage = (ImageView) view.findViewById(R.id.detail_describe_image);
		tvAppName = (TextView) view.findViewById(R.id.detail_describe_name);
		tvAppSize = (TextView) view.findViewById(R.id.detail_describe_size);
		tvVersion2 = (TextView) view
				.findViewById(R.id.detail_describe_version_2);
		tvWarn = (TextView) view.findViewById(R.id.detail_describe_warn);
		tvDownloadTime = (TextView) view
				.findViewById(R.id.detail_describe_download);
		tvLanguage = (TextView) view
				.findViewById(R.id.detail_describe_language);
		tvUpdateDate = (TextView) view.findViewById(R.id.detail_describe_time);
		tvIntro = (TextView) view.findViewById(R.id.detail_describe_intro);
		allmoreLayout = (LinearLayout) view
				.findViewById(R.id.detal_describe_all_more);
		allmoreLayout.setOnClickListener(this);
		AboutRecommendLayout = (LinearLayout) view
				.findViewById(R.id.describe_about);
		ivOpen = (ImageView) view.findViewById(R.id.detal_describe_more);
		tvOpenText = (TextView) view
				.findViewById(R.id.detal_describe_more_text);

		home_liear_pro = (LinearLayout) view.findViewById(R.id.home_liear_pro);
		home_liear_connent = (RelativeLayout) view
				.findViewById(R.id.home_liear_connent);
		bgPageView = new BgPageView(mContext, home_liear_pro,
				home_liear_connent);

		ReferralsGridView = (GridView) view
				.findViewById(R.id.detail_describe_mGridView_recommend);

		// if (appdataBean != null) {
		// Toast.makeText(mContext, "有", Toast.LENGTH_SHORT).show();
		mScrollView.setHorizontalScrollBarEnabled(false);// 隐藏滚动条
		DefaultAdapter appadapter = new DefaultAdapter(mContext);
		gridView.setAdapter(appadapter);
		LayoutParams params = new LayoutParams(3 * (cWidth + hSpacing)
				- hSpacing, LayoutParams.WRAP_CONTENT);
		gridView.setLayoutParams(params);
		gridView.setColumnWidth(cWidth);
		gridView.setHorizontalSpacing(hSpacing);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setNumColumns(3);
		// }

	}

	/**********************
	 * 展示图片列表
	 **********************/
	private void initView() {

		mScrollView.setHorizontalScrollBarEnabled(false);// 隐藏滚动条
		adapter = new AppInfoPictureAdapter(mContext, thumbImages, application);
		gridView.setAdapter(adapter);
		LayoutParams params = new LayoutParams(adapter.getCount()
				* (cWidth + hSpacing) - hSpacing, LayoutParams.WRAP_CONTENT);
		gridView.setLayoutParams(params);
		gridView.setColumnWidth(cWidth);
		gridView.setHorizontalSpacing(hSpacing);
		gridView.setStretchMode(GridView.NO_STRETCH);
		gridView.setNumColumns(adapter.getCount());
		Log.i("列宽：", "" + cWidth);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				int x = (int) (view.getLeft() + view.getWidth() / 2.0 - mWidthPixels / 2.0);
				mScrollView.smoothScrollTo(x, 0);
				int selectItem = position;
				Bundle b = new Bundle();
				b.putInt(ImagePagerActivity.POSTION, selectItem);
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < shoImages.size(); i++) {
					list.add(shoImages.get(i));

				}
				b.putStringArrayList(ImagePagerActivity.PARAMS, list);
				// Intent intent = new Intent(mContext,
				// PictureViewerActivity.class);
				Intent intent = new Intent(mContext, ImagePagerActivity.class);
				intent.putExtra(ImagePagerActivity.BUNLDER, b);
				mContext.startActivity(intent);
				// ((Activity)
				// mContext).overridePendingTransition(R.anim.my_scale_action,
				// R.anim.my_alpha_action);
				((Activity) mContext).overridePendingTransition(
						android.R.anim.fade_in, android.R.anim.fade_out);

			}
		});
	}

	/**********************
	 * 从网络获取应用详情数据
	 **********************/
	private void getAppData() {

		TAApplication.getApplication().doCommand(
				TAApplication.getApplication().getString(
						R.string.detailviewcontroller),
				new TARequest(DetailController.GAIN_NETWORK, url),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						// bgPageView.showContent();
						if (listener != null) {
							listener.onShow();
						}
						DetailDataBean db = (DetailDataBean) response.getData();
						showToUI(db);

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
						// String string = (String) response.getData();
						if (listener != null) {
							listener.onCancle();
						}
						// Log.i("AppDescribe", string);
						bgPageView
								.showLoadException(new onCallBackOnClickListener() {

									@Override
									public void onClick() {
										bgPageView.showContent();// 显示原来的内容
										getAppData();
										getCorrelationData();
									}
								});
					}
				}, true, false);
	}

	/**********************
	 * 从网络获取相关推荐应用数据
	 **********************/
	private void getCorrelationData() {
		TAApplication.getApplication()
				.doCommand(
						TAApplication.getApplication().getString(
								R.string.detailviewcontroller),
						new TARequest(DetailController.GAIN_CORRELATION,
								correlationUrl), new TAIResponseListener() {

							@SuppressWarnings("unchecked")
							@Override
							public void onSuccess(TAResponse response) {
								List<RelatedRecommendBean> relatedList = (List<RelatedRecommendBean>) response
										.getData();
								initRelatedApp(relatedList);

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
						}, true, false);
	}

	/********************
	 * 展示数据到UI
	 ********************/
	private String strIntro = "";

	@SuppressWarnings("unused")
	protected void showToUI(DetailDataBean db) {

		if (db == null || db.packageName == null || db.description == null) {
			return;
		}

		shoImages = db.screenshotUrls;
		thumbImages = db.thumbUrls;
		List<RelatedRecommendBean> relatedApps = db.relatedApps;
		int size = db.size;
		tvAppName.setText(db.name);

		if (listener != null) {
			listener.getData(db);
		}

		tvAppSize.setText(FileUtil.convertFileSize(size));
		tvVersion2.setText(db.version);
		tvWarn.setVisibility(View.GONE);

		strIntro = db.description;
		String regexpHtml = HtmlRegexpUtil.filterHtml(strIntro);
		if (regexpHtml.length() > 70) {
			String i = regexpHtml.substring(0, 69) + "......";
			tvIntro.setText(Html.fromHtml(i));
			allmoreLayout.setVisibility(View.VISIBLE);
		} else {
			tvIntro.setText(Html.fromHtml(HtmlRegexpUtil
					.filterimgHtml(strIntro)));
			allmoreLayout.setVisibility(View.GONE);
		}

		tvDownloadTime.setText(FileUtil.convertDownloadTimes(db.downloads));
		tvLanguage.setText(db.language);
		tvUpdateDate.setText(db.updateTime);

		ivImage.setTag(db.iconUrl);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, db.iconUrl.hashCode() + "",
				db.iconUrl, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (ivImage.getTag().equals(imgUrl)) {
							ivImage.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			ivImage.setImageBitmap(bm);
		} else {
			// TODO 默认
		}
		initView();
		// initRelatedApp(relatedApps);

	}

	/***********************
	 * 相关推荐
	 ***********************/
	private void initRelatedApp(List<RelatedRecommendBean> relatedApps) {

		if (relatedApps != null && relatedApps.size() > 0) {
			reAdapter = new RelatedRecommendAdapter(mContext, relatedApps,
					application);
			ReferralsGridView.setAdapter(reAdapter);
			AboutRecommendLayout.setVisibility(View.VISIBLE);
		} else {
			AboutRecommendLayout.setVisibility(View.GONE);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.detal_describe_all_more:
			getMore();
			break;

		default:
			break;
		}

	}

	boolean textFlag = false;

	private void getMore() {
		if (textFlag) {// 收缩
			ivOpen.setImageResource(R.drawable.more_down);
			tvOpenText.setText(R.string.open);
			String regexpHtml = HtmlRegexpUtil.filterHtml(strIntro);
			if (regexpHtml.length() > 70) {
				String i = regexpHtml.substring(0, 69) + "......";
				tvIntro.setText(Html.fromHtml(i));
			} else {
				tvIntro.setText(Html.fromHtml(HtmlRegexpUtil
						.filterimgHtml(strIntro)));
			}
			textFlag = false;
		} else {// 展开
			try {
				tvIntro.setText(Html.fromHtml(HtmlRegexpUtil
						.filterimgHtml(strIntro)));
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				tvIntro.setText(HtmlRegexpUtil.filterimgHtml(strIntro));
			}

			ivOpen.setImageResource(R.drawable.more_up);
			tvOpenText.setText(R.string.fold);

			textFlag = true;
		}
	}

}
