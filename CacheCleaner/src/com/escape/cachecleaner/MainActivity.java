package com.escape.cachecleaner;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;

public class MainActivity extends Activity {

	private TextView mTotal;
	private ListView mListView;
	private CacheCleanAdapter mAdapter;
	private TextView mAllCache;
	private Button mAKey;
	private View mContent;
	private View mProgressLayout;
	private ImageView mProgress;
	private TextView mProgressText;
	private LinearLayout nocontentLayout;
	private FrameLayout havecontentLayout;

	private FrameLayout mADFrame;
	private AdView adView;

	/**
	 * 界面在onresume时需要检查是否已清理缓存的应用
	 */
	private CacheDataBean mNeedCheckBean;
	/**
	 * 用于清理单个应用缓存
	 */
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			mNeedCheckBean = (CacheDataBean) msg.obj;
			Toast.makeText(MainActivity.this, R.string.clickclear,
					Toast.LENGTH_SHORT).show();
			AppUtils.showInstalledAppDetails(MainActivity.this,
					mNeedCheckBean.mInfo.packageName);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ImageButton btnBack = (ImageButton) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mAllCache = (TextView) findViewById(R.id.cache_clean_all);
		mTotal = (TextView) findViewById(R.id.total);
		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new CacheCleanAdapter(mHandler);
		mListView.setAdapter(mAdapter);
		mAKey = (Button) findViewById(R.id.akey);
		mContent = findViewById(R.id.content);
		mProgressLayout = findViewById(R.id.progress_layout);
		mProgress = (ImageView) findViewById(R.id.progress);
		mProgressText = (TextView) findViewById(R.id.progress_text);

		nocontentLayout = (LinearLayout) findViewById(R.id.no_content);
		havecontentLayout = (FrameLayout) findViewById(R.id.have_content);

		mContent.setVisibility(View.GONE);
		mProgressLayout.setVisibility(View.VISIBLE);
		Drawable drawable = new PageProgressBitmapDrawable(getResources(),
				DrawUtil.sPageProgressBitmap, 0);
		mProgress.setImageDrawable(drawable);

		mAKey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TAApplication.getApplication().doCommand(
						"cachecleanercontroller",
						new TARequest(CacheCleanerController.A_KEY_CLEAN_UP,
								mAdapter.getTotalCacheSize()),
						new TAIResponseListener() {

							@Override
							public void onStart() {
							}

							@Override
							public void onSuccess(TAResponse response) {
								nocontentLayout.setVisibility(View.VISIBLE);
								havecontentLayout.setVisibility(View.GONE);
							}

							@Override
							public void onRuning(TAResponse response) {
							}

							@Override
							public void onFailure(TAResponse response) {
							}

							@Override
							public void onFinish() {
							}
						}, true, false);
			}
		});
		Log.e("", "doCommand " + CacheCleanerController.CACHE_APP);
		// 获取应用缓存列表
		TAApplication.getApplication().doCommand("cachecleanercontroller",
				new TARequest(CacheCleanerController.CACHE_APP, null),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						mProgressLayout.setVisibility(View.GONE);
						mContent.setVisibility(View.VISIBLE);
						List<CacheDataBean> apps = (List<CacheDataBean>) response
								.getData();
						long totalSize = 0;
						for (CacheDataBean bean : apps) {
							totalSize += bean.mCache;
						}
						// 展示列表
						mAdapter.update(apps);
						mTotal.setText(getString(R.string.tip1)
								+ apps.size()
								+ getString(R.string.tip2)
								+ FileUtil
										.convertFileSize((long) (totalSize / 1024.0f + 0.5f)));
						mAllCache.setText(getString(R.string.allcache)
								+ FileUtil
										.convertFileSize((long) (totalSize / 1024.0f + 0.5f)));
						if (mAdapter.getTotalCacheSize() <= 0) {
							nocontentLayout.setVisibility(View.VISIBLE);
							havecontentLayout.setVisibility(View.GONE);
						}
					}

					@Override
					public void onRuning(TAResponse response) {
						try {
							int[] array = (int[]) response.getData();
							int progress = (int) (array[0] * 1.0 / array[1]
									* 100.0 + 0.5);
							Drawable drawable = new PageProgressBitmapDrawable(
									getResources(),
									DrawUtil.sPageProgressBitmap, progress);
							mProgress.setImageDrawable(drawable);
							mProgressText.setText(progress + "%");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}
				}, true, false);
		mADFrame = (FrameLayout) findViewById(R.id.adframe);
		// 创建adView。
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-6335053266754945/2845822912");
		adView.setAdSize(AdSize.BANNER);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		mADFrame.addView(adView, lp);
		// 启动一般性请求。
		AdRequest adRequest = new AdRequest.Builder().build();
		// 在adView中加载广告请求。
		adView.loadAd(adRequest);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mNeedCheckBean != null) {
			final CacheDataBean bean = mNeedCheckBean;
			mNeedCheckBean = null;
			// 获取单个应用缓存
			TAApplication.getApplication()
					.doCommand(
							"cachecleanercontroller",
							new TARequest(
									CacheCleanerController.CHECK_SINGLE_APP,
									bean), new TAIResponseListener() {

								@Override
								public void onStart() {
								}

								@Override
								public void onSuccess(TAResponse response) {
									Long cachesize = (Long) response.getData();
									if (cachesize <= 0) {
										// 移除
										List<CacheDataBean> list = new ArrayList<CacheDataBean>();
										List<CacheDataBean> src = mAdapter
												.getData();
										for (CacheDataBean bean_ : src) {
											if (!bean_.mInfo.packageName
													.equals(bean.mInfo.packageName)) {
												list.add(bean_);
											}
										}
										mAdapter.update(list);
										long totalSize = 0;
										for (CacheDataBean bean : list) {
											totalSize += bean.mCache;
										}
										mTotal.setText(getString(R.string.tip1)
												+ list.size()
												+ getString(R.string.tip2)
												+ FileUtil
														.convertFileSize((long) (totalSize / 1024.0f + 0.5f)));
										mAllCache.setText(getString(R.string.allcache)
												+ FileUtil
														.convertFileSize((long) (totalSize / 1024.0f + 0.5f)));
										if (mAdapter.getTotalCacheSize() <= 0) {
											nocontentLayout
													.setVisibility(View.VISIBLE);
											havecontentLayout
													.setVisibility(View.GONE);
										}
									}
								}

								@Override
								public void onRuning(TAResponse response) {
								}

								@Override
								public void onFailure(TAResponse response) {
								}

								@Override
								public void onFinish() {
								}
							}, true, false);
		}
		adView.resume();
	}

	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		adView.destroy();
		super.onDestroy();
	}

}
