package com.smartapp.autostartmanager;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

	private AdView adView;
	private View mContentLayout;
	private View mProgressLayout;
	private ImageView mProgress;
	private TextView mProgressText;

	private ListView mListView;
	private MainAdapter mAdapter;

	public static final int MSG_DISABLE = 11001;
	public static final int MSG_ENABLE = 11002;
	private Handler mHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(final android.os.Message msg) {
			switch (msg.what) {
			case MSG_DISABLE: {
				final DataBean bean = (DataBean) msg.obj;
				// 如果是系统应用，提示用户有风险
				if (bean.mIsSysApp) {
					AlertDialog.Builder builder = new Builder(MainActivity.this);
					builder.setMessage(getText(R.string.wmsg));
					builder.setTitle(getText(R.string.wtitle));
					builder.setPositiveButton(getText(R.string.wyes),
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									disableApp(bean);
								}
							});
					builder.setNegativeButton(getText(R.string.wno),
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					builder.create().show();
				} else {
					disableApp(bean);
				}
				// TODO 在2.3和4.X机器上测试
				break;
			}
			case MSG_ENABLE: {
				final ProgressDialog dialog = ProgressDialog
						.show(MainActivity.this, null,
								getString(R.string.processing));
				dialog.setCancelable(false);
				final Object obj = msg.obj;
				// 启用组件
				TAApplication.getApplication().doCommand("maincontroller",
						new TARequest(MainController.ENABLE_APP, msg.obj),
						new TAIResponseListener() {

							@Override
							public void onStart() {
							}

							@Override
							public void onSuccess(TAResponse response) {
								dialog.dismiss();
								Boolean ret = (Boolean) response.getData();
								if (!ret) {
									Toast.makeText(MainActivity.this,
											R.string.operafail,
											Toast.LENGTH_SHORT).show();
									return;
								}
								// 更新列表
								DataBean b = (DataBean) obj;
								b.mIsForbid = false;
								mAdapter.updateSelf();
							}

							@Override
							public void onRuning(TAResponse response) {
							}

							@Override
							public void onFailure(TAResponse response) {
								dialog.dismiss();
							}

							@Override
							public void onFinish() {
							}
						}, true, false);
				break;
			}
			default:
				break;
			}
		};
	};
	/**
	 * activity 是否结束
	 */
	private boolean mIsFinish = false;
	/**
	 * 定时更新cpu使用率
	 */
	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			mHandler.removeCallbacks(mRunnable);
			// 获取运行应用列表
			TAApplication.getApplication().doCommand("maincontroller",
					new TARequest(MainController.CPU_RATE, null),
					new TAIResponseListener() {

						@Override
						public void onStart() {
						}

						@Override
						public void onSuccess(TAResponse response) {
							Map<String, Float> retRate = (Map<String, Float>) response
									.getData();
							// 更新CPU使用率
							mAdapter.updateCPU(retRate);
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

	private void disableApp(final DataBean bean) {
		final ProgressDialog dialog = ProgressDialog.show(MainActivity.this,
				null, getString(R.string.processing));
		dialog.setCancelable(false);
		// 禁用组件
		TAApplication.getApplication().doCommand("maincontroller",
				new TARequest(MainController.DISABLE_APP, bean),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						dialog.dismiss();
						Boolean ret = (Boolean) response.getData();
						if (!ret) {
							Toast.makeText(MainActivity.this,
									R.string.operafail, Toast.LENGTH_SHORT)
									.show();
							return;
						}
						// 更新列表
						bean.mIsForbid = true;
						bean.mMemory = 0;
						mAdapter.updateSelf();
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFailure(TAResponse response) {
						dialog.dismiss();
					}

					@Override
					public void onFinish() {
					}
				}, true, false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mIsFinish = false;
		// 创建adView。
		adView = new AdView(this);
		adView.setAdUnitId("ca-app-pub-6335053266754945/7051389713");
		adView.setAdSize(AdSize.BANNER);
		FrameLayout layout = (FrameLayout) findViewById(R.id.adcontianer);
		// 在其中添加adView。
		layout.addView(adView);
		// 启动一般性请求。
		AdRequest adRequest = new AdRequest.Builder().build();
		// 在adView中加载广告请求。
		adView.loadAd(adRequest);

		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new MainAdapter(mHandler);
		mListView.setAdapter(mAdapter);

		mContentLayout = findViewById(R.id.content);
		mProgressLayout = findViewById(R.id.progress_layout);
		mProgress = (ImageView) findViewById(R.id.progress);
		mProgressText = (TextView) findViewById(R.id.progress_text);

		mContentLayout.setVisibility(View.GONE);
		mProgressLayout.setVisibility(View.VISIBLE);
		Drawable drawable = new PageProgressBitmapDrawable(getResources(),
				DrawUtil.sPageProgressBitmap, 0);
		mProgress.setImageDrawable(drawable);

		// 获取运行应用列表
		TAApplication.getApplication().doCommand("maincontroller",
				new TARequest(MainController.SCAN_COMMAND, null),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						mProgressLayout.setVisibility(View.GONE);
						mContentLayout.setVisibility(View.VISIBLE);
						List<DataBean> retList = (List<DataBean>) response
								.getData();
						mAdapter.update(retList);
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
							if (progress < 0) {
								progress = 0;
							}
							if (progress > 100) {
								progress = 100;
							}
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

	}

	@Override
	protected void onResume() {
		adView.resume();
		super.onResume();
	}

	@Override
	public void onPause() {
		adView.pause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		mIsFinish = true;
		adView.destroy();
		super.onDestroy();
	}
}
