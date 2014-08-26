package com.zhidian.wifibox.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.CacheCleanAdapter;
import com.zhidian.wifibox.controller.CacheCleanerController;
import com.zhidian.wifibox.data.CacheDataBean;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.view.PageProgressBitmapDrawable;

/**
 * 缓存清理
 * 
 * @author xiedezhi
 * 
 */
public class CacheCleanerActivity extends Activity {

	private TextView tvTotalge, tvSizeTotal;
	private ListView mListView;
	private CacheCleanAdapter mAdapter;
	private TextView mAllCache;
	private Button mAKey;
	private View mContent;
	private View mProgressLayout;
	private ImageView mProgress;
	private TextView mProgressText;
	private LinearLayout nocontentLayout; // 没有内容
	private FrameLayout havecontentLayout; // 有内容

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
			Toast.makeText(CacheCleanerActivity.this, "请点击\"清除缓存\"进行清理",
					Toast.LENGTH_SHORT).show();
			AppUtils.showInstalledAppDetails(CacheCleanerActivity.this,
					mNeedCheckBean.mInfo.packageName);
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cache_clean);
		TextView tvTitle = (TextView) findViewById(R.id.header_title_text);
		tvTitle.setText("缓存清理");
		ImageView btnBack = (ImageView) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mAllCache = (TextView) findViewById(R.id.cache_clean_all);
		tvTotalge = (TextView) findViewById(R.id.total);
		tvSizeTotal = (TextView) findViewById(R.id.size);
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
				// 一键清理
				TAApplication.getApplication().doCommand(
						getString(R.string.cachecleanercontroller),
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
		// 获取应用缓存列表
		TAApplication.getApplication().doCommand(
				getString(R.string.cachecleanercontroller),
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
						tvTotalge.setText(apps.size() + "");
						tvSizeTotal.setText(FileUtil
								.convertFileSize((long) (totalSize / 1024.0f + 0.5f)));
						// mTotal.setText("共"
						// + apps.size()
						// + "个缓存垃圾，清理可节省空间"
						// + FileUtil
						// .convertFileSize((long) (totalSize / 1024.0f +
						// 0.5f)));
						mAllCache.setText("全部缓存:"
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("缓存清理");
			MobclickAgent.onResume(this);
		}
		if (mNeedCheckBean != null) {
			final CacheDataBean bean = mNeedCheckBean;
			mNeedCheckBean = null;
			// 获取单个应用缓存
			TAApplication.getApplication()
					.doCommand(
							getString(R.string.cachecleanercontroller),
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
										String toast = "清理缓存";
										if (bean.mCache >= 1024.0) {
											toast += FileUtil
													.convertFileSize((long) (bean.mCache / 1024.0f + 0.5f));
										} else {
											toast += bean.mCache + "B";
										}
										Toast.makeText(getApplicationContext(),
												toast, Toast.LENGTH_SHORT)
												.show();
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
										tvTotalge.setText(list.size() + "");
										tvSizeTotal.setText(FileUtil
												.convertFileSize((long) (totalSize / 1024.0f + 0.5f)));
										mAllCache.setText("全部缓存:"
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
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("缓存清理");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
