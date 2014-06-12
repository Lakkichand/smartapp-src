package com.zhidian.wifibox.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.TrashScanAdapter;
import com.zhidian.wifibox.controller.TrashScanController;
import com.zhidian.wifibox.data.TransScanDataBean;
import com.zhidian.wifibox.util.FileUtil;

/**
 * 残留文件清理
 * 
 * @author xiedezhi
 * 
 */
public class TrashScanActivity extends Activity {
	/**
	 * 扫描UI
	 */
	private View mScanning;
	/**
	 * 当前正在扫描的路径
	 */
	private TextView mPath;
	/**
	 * 扫描进度
	 */
	private ProgressBar mProgress;
	/**
	 * 扫描结果
	 */
	private TextView mResult;

	private ListView mListView;

	private TrashScanAdapter mAdapter;

	private TextView mTotal;
	/**
	 * 一键清理
	 */
	private Button mAKey;

	private View mContent;
	//private View mNoContent;
	
	private LinearLayout nocontentLayout; // 没有内容
	private FrameLayout havecontentLayout; // 有内容

	public static final int MSG_CLEAN = 1001;
	public static final int MSG_UPDATE = 1002;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_CLEAN) {
				List<TransScanDataBean> ret = mAdapter.getData();
				List<TransScanDataBean> list = new ArrayList<TransScanDataBean>();
				int index = msg.arg1;
				TransScanDataBean bean = ret.get(index);
				for (int i = 0; i < ret.size(); i++) {
					if (i != index) {
						list.add(ret.get(i));
					}
				}
				mAdapter.update(list);
				updateResult();
				updateAKey();
				for (String path : bean.paths) {
					File file = new File(path);
					file.delete();
				}
			} else if (msg.what == MSG_UPDATE) {
				updateAKey();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trashscan);

		TextView tvTitle = (TextView) findViewById(R.id.header_title_text);
		tvTitle.setText("残留文件");
		findViewById(R.id.header_title_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
		mScanning = findViewById(R.id.scanning);
		mPath = (TextView) findViewById(R.id.path);
		mProgress = (ProgressBar) findViewById(R.id.progress);
		mResult = (TextView) findViewById(R.id.result);
		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new TrashScanAdapter(TrashScanActivity.this, mHandler);
		mListView.setAdapter(mAdapter);
		mTotal = (TextView) findViewById(R.id.total);
		mAKey = (Button) findViewById(R.id.a_key);
		mContent = findViewById(R.id.content);
		//mNoContent = findViewById(R.id.notrash);
		
		nocontentLayout = (LinearLayout) findViewById(R.id.no_content);
		//havecontentLayout = (FrameLayout) findViewById(R.id.have_content);

		mContent.setVisibility(View.VISIBLE);
		nocontentLayout.setVisibility(View.GONE);

		mScanning.setVisibility(View.VISIBLE);
		mResult.setVisibility(View.GONE);

		mTotal.setText("正在扫描...");
		mAKey.setVisibility(View.INVISIBLE);

		mAKey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 一键清理
				List<TransScanDataBean> ret = mAdapter.getData();
				List<TransScanDataBean> list = new ArrayList<TransScanDataBean>();
				List<TransScanDataBean> select = new ArrayList<TransScanDataBean>();
				for (TransScanDataBean bean : ret) {
					select.add(bean);
				}
				mAdapter.update(list);
				updateResult();
				for (TransScanDataBean bean : select) {
					for (String path : bean.paths) {
						File file = new File(path);
						file.delete();
					}
				}
			}
		});

		// 获取应用缓存列表
		TAApplication.getApplication().doCommand(
				getString(R.string.trashscancontroller),
				new TARequest(TrashScanController.SCAN_TRASH, null),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						Object obj = response.getData();
						if (obj == null) {
							updateResult();
							return;
						}
						if (obj instanceof List<?>) {
							try {
								List<TransScanDataBean> ret = (List<TransScanDataBean>) obj;
								mAdapter.update(ret);
								updateResult();
								updateAKey();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						mAdapter.finish();
					}

					@Override
					public void onRuning(TAResponse response) {
						Object obj = response.getData();
						if (obj instanceof Integer) {
							int process = (Integer) obj;
							mProgress.setProgress(process);
						} else if (obj instanceof String) {
							// 展示正在扫描的路径
							String path = (String) obj;
							mPath.setText(path);
						} else if (obj instanceof List<?>) {
							try {
								List<TransScanDataBean> ret = (List<TransScanDataBean>) obj;
								mAdapter.update(ret);
							} catch (Exception e) {
								e.printStackTrace();
							}
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

	/**
	 * 更新选中的残留文件信息
	 */
	private void updateAKey() {
		List<TransScanDataBean> ret = mAdapter.getData();
		long size = 0;
		boolean select = false;
		for (TransScanDataBean bean : ret) {
			if (bean.isSelect) {
				select = true;
				size += bean.size;
			}
		}
		if (select) {
			mTotal.setText("残留文件："
					+ FileUtil.convertFileSize((long) (size / 1024.0)));
			mAKey.setVisibility(View.VISIBLE);
			mAKey.setEnabled(true);
		} else {
			mTotal.setText("请选择您要清理的内容");
			mAKey.setEnabled(false);
			mAKey.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 更新残留文件总信息
	 */
	private void updateResult() {
		List<TransScanDataBean> ret = mAdapter.getData();
		if (ret == null || ret.size() <= 0) {
			// 提示无残留垃圾
			mContent.setVisibility(View.GONE);
			nocontentLayout.setVisibility(View.VISIBLE);
			return;
		}
		mScanning.setVisibility(View.GONE);
		mResult.setVisibility(View.VISIBLE);
		long size = 0;
		for (TransScanDataBean bean : ret) {
			size += bean.size;
		}
		mResult.setText("共" + ret.size() + "项残留垃圾，清理可节省存储空间"
				+ FileUtil.convertFileSize((long) (size / 1024.0)));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("残留文件清理");
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("残留文件清理");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
