package com.zhidian.wifibox.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.ProcessManagerAdapter;
import com.zhidian.wifibox.controller.ProcessManagerController;
import com.zhidian.wifibox.data.ProcessDataBean;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.view.PageProgressBitmapDrawable;

/**
 * 进程管理
 * 
 * @author xiedezhi
 * 
 */
public class ProcessManagerActivity extends Activity {
	/**
	 * 百分比
	 */
	private TextView mPercentage;
	/**
	 * 已用内存
	 */
	private TextView mUsed;
	/**
	 * 总内存
	 */
	private TextView mTotal;
	/**
	 * 进度条
	 */
	private ProgressBar mProgressBar;
	/**
	 * listview
	 */
	private PinnedHeaderListView mListView;
	/**
	 * adapter
	 */
	private ProcessManagerAdapter mAdapter;

	private View mContent;
	private View mProgressLayout;
	private ImageView mProgress;
	private TextView mProgressText;
	/**
	 * activity 是否结束
	 */
	private boolean mIsFinish = false;

	private Handler mHandler = new Handler(Looper.getMainLooper());
	/**
	 * 定时更新cpu使用率
	 */
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			mHandler.removeCallbacks(mRunnable);
			// 获取运行应用列表
			TAApplication.getApplication().doCommand(
					getString(R.string.processmanagercontroller),
					new TARequest(ProcessManagerController.CPU_RATE, null),
					new TAIResponseListener() {

						@Override
						public void onStart() {
						}

						@Override
						public void onSuccess(TAResponse response) {
							Map<String, Float> retRate = (Map<String, Float>) response
									.getData();
							List<ProcessDataBean> list = mAdapter.getDataList();
							for (String pkg : retRate.keySet()) {
								for (ProcessDataBean bean : list) {
									if (bean.mInfo.packageName.equals(pkg)) {
										float rate = retRate.get(pkg);
										rate = ((int) (rate * 10.0f + 0.5f)) / 10.0f;
										bean.mCpuRate = "CPU:" + rate + "%";
										break;
									}
								}
							}
							// 展示列表
							List<ProcessDataBean> userList = new ArrayList<ProcessDataBean>();
							List<ProcessDataBean> sysList = new ArrayList<ProcessDataBean>();
							for (ProcessDataBean bean : list) {
								if (bean.mIsSysApp) {
									sysList.add(bean);
								} else {
									userList.add(bean);
								}
							}
							mAdapter.update(userList, sysList);
							mHandler.removeCallbacks(mRunnable);
							if (!mIsFinish) {
								mHandler.postDelayed(mRunnable, 1500);
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
	};

	/**
	 * 更新内存使用情况
	 */
	private void updateMemory() {
		// 获取总内存和可用内存
		ActivityManager acm = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo outInfo = new MemoryInfo();
		acm.getMemoryInfo(outInfo);
		long avi = (long) (outInfo.availMem / 1024.0 / 1024.0 + 0.5);
		long total = (long) (InfoUtil.getTotalRAM() / 1024.0 + 0.5);
		long used = total - avi;
		int progress = ((int) (used * 1.0 / total * 100 + 0.5));
		mPercentage.setText(progress + "%");
		mUsed.setText(used + "M");
		mTotal.setText("/" + total + "M");
		mProgressBar.setProgress(progress);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.process_manager);
		findViewById(R.id.header_title_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
		TextView title = (TextView) findViewById(R.id.header_title_text);
		title.setText("进程管理");
		mPercentage = (TextView) findViewById(R.id.percentage);
		mUsed = (TextView) findViewById(R.id.used);
		mTotal = (TextView) findViewById(R.id.total);
		mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
		mListView = (PinnedHeaderListView) findViewById(R.id.listview);
		mAdapter = new ProcessManagerAdapter();
		mListView.setAdapter(mAdapter);

		mContent = findViewById(R.id.content);
		mProgressLayout = findViewById(R.id.progress_layout);
		mProgress = (ImageView) findViewById(R.id.progress);
		mProgressText = (TextView) findViewById(R.id.progress_text);

		mContent.setVisibility(View.GONE);
		mProgressLayout.setVisibility(View.VISIBLE);
		Drawable drawable = new PageProgressBitmapDrawable(getResources(),
				DrawUtil.sPageProgressBitmap, 0);
		mProgress.setImageDrawable(drawable);

		// 获取总内存和可用内存
		updateMemory();
		// 获取运行应用列表
		TAApplication.getApplication().doCommand(
				getString(R.string.processmanagercontroller),
				new TARequest(ProcessManagerController.RUNNING_APP, null),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						mProgressLayout.setVisibility(View.GONE);
						mContent.setVisibility(View.VISIBLE);
						List<ProcessDataBean> retList = (List<ProcessDataBean>) response
								.getData();
						if (retList == null) {
							retList = new ArrayList<ProcessDataBean>();
						}
						// 展示列表
						List<ProcessDataBean> userList = new ArrayList<ProcessDataBean>();
						List<ProcessDataBean> sysList = new ArrayList<ProcessDataBean>();
						for (ProcessDataBean bean : retList) {
							if (bean.mIsSysApp) {
								sysList.add(bean);
							} else {
								userList.add(bean);
							}
						}
						mAdapter.update(userList, sysList);
						mHandler.removeCallbacks(mRunnable);
						if (!mIsFinish) {
							mHandler.post(mRunnable);
						}
					}

					@Override
					public void onRuning(TAResponse response) {
						Object obj = response.getData();
						if (obj != null && obj instanceof Integer) {
							int progress = (Integer) obj;
							Drawable drawable = new PageProgressBitmapDrawable(
									getResources(),
									DrawUtil.sPageProgressBitmap, progress);
							mProgress.setImageDrawable(drawable);
							mProgressText.setText(progress + "%");
						}
					}

					@Override
					public void onFailure(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}
				}, true, false);
		findViewById(R.id.akey).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 一键清理
				TAApplication.getApplication().doCommand(
						getString(R.string.processmanagercontroller),
						new TARequest(ProcessManagerController.A_KEY_CLEAN_UP,
								null), new TAIResponseListener() {
							final ProgressDialog dialog = ProgressDialog.show(
									ProcessManagerActivity.this, null,
									"正在结束...", false, false);

							@Override
							public void onStart() {
							}

							@Override
							public void onSuccess(TAResponse response) {
								List<ProcessDataBean> retList = (List<ProcessDataBean>) response
										.getData();
								if (retList == null) {
									retList = new ArrayList<ProcessDataBean>();
								}
								// 展示列表
								List<ProcessDataBean> userList = new ArrayList<ProcessDataBean>();
								List<ProcessDataBean> sysList = new ArrayList<ProcessDataBean>();
								for (ProcessDataBean bean : retList) {
									if (bean.mIsSysApp) {
										sysList.add(bean);
									} else {
										userList.add(bean);
									}
								}
								mAdapter.update(userList, sysList);
								mHandler.removeCallbacks(mRunnable);
								if (!mIsFinish) {
									mHandler.post(mRunnable);
								}
								updateMemory();
								dialog.dismiss();
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("进程管理");
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("进程管理");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		mIsFinish = true;
		super.onDestroy();
		mHandler.removeCallbacks(mRunnable);
	}

}
