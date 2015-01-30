package com.zhidian.wifibox.view;

import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.AppUninstallActivity;
import com.zhidian.wifibox.activity.AppUpdateActivity;
import com.zhidian.wifibox.activity.CleanMasterActivity;
import com.zhidian.wifibox.activity.DownloadManagerActivity;
import com.zhidian.wifibox.controller.ManagerController;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.file.fragment.FileManagerActivity;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.view.ScanView.ProgressCallBack;

/**
 * 管理界面
 * 
 * @author xiedezhi
 * 
 */
public class ManagerContainer extends LinearLayout implements IContainer {
	private PageDataBean mBean;
	/**
	 * 是否正在计算分数
	 */
	private volatile boolean mCalculating = false;

	private ScanView mScanView;

	public ManagerContainer(Context context) {
		super(context);
	}

	public ManagerContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		findViewById(R.id.download).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 下载管理
				Intent intent = new Intent(getContext(),
						DownloadManagerActivity.class);
				getContext().startActivity(intent);
			}
		});
		findViewById(R.id.deliver).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 文件管理
				Intent intent = new Intent();
				intent.setClass(getContext(), FileManagerActivity.class);
				getContext().startActivity(intent);
			}
		});
		findViewById(R.id.appuninstall).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 软件卸载
						Intent intent = new Intent();
						intent.setClass(getContext(),
								AppUninstallActivity.class);
						getContext().startActivity(intent);
					}
				});
		findViewById(R.id.popularity).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
			}
		});
		findViewById(R.id.appupdate).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 安装包管理
				Intent intent = new Intent();
				intent.setClass(getContext(), AppUpdateActivity.class);
				getContext().startActivity(intent);
			}
		});
		findViewById(R.id.clearup).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 手机清理
				Intent intent = new Intent();
				intent.setClass(getContext(), CleanMasterActivity.class);
				getContext().startActivity(intent);
			}
		});

		findViewById(R.id.clean_up).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 手机清理
				Intent intent = new Intent();
				intent.setClass(getContext(), CleanMasterActivity.class);
				getContext().startActivity(intent);
			}
		});

		// 更新个数
		Setting setting = new Setting(getContext());
		TextView updateCount = (TextView) findViewById(R.id.appupdate_text_hint);
		int count = setting.getInt(Setting.UPDATE_COUNT);
		if (count > 0) {
			updateCount.setVisibility(View.VISIBLE);
			updateCount.setText(count + "");
		} else {
			updateCount.setVisibility(View.GONE);
		}
	}

	/**
	 * 正在下载的任务数
	 */
	private void updateDownloadingCount() {
		Map<String, DownloadTask> map = DownloadTaskRecorder.getInstance()
				.getDownloadTaskList();
		if (map == null || map.isEmpty()) {
			findViewById(R.id.download_text_hint).setVisibility(View.INVISIBLE);
			return;
		}
		int ret = 0;
		for (String key : map.keySet()) {
			String apkPath = DownloadUtil.getCApkFileFromUrl(map.get(key).url);
			String packName = map.get(key).packName;
			if (FileUtil.isFileExist(apkPath)
					|| (InstallingValidator.getInstance().isAppExist(
							TAApplication.getApplication(), packName)
							&& map.get(key).state != DownloadTask.DOWNLOADING
							&& map.get(key).state != DownloadTask.WAITING && map
							.get(key).state != DownloadTask.PAUSING)) {
			} else {
				ret++;
			}
		}
		if (ret <= 0) {
			findViewById(R.id.download_text_hint).setVisibility(View.INVISIBLE);
		} else {
			findViewById(R.id.download_text_hint).setVisibility(View.VISIBLE);
			TextView downloadCount = (TextView) findViewById(R.id.download_text_hint);
			downloadCount.setText(ret + "");
		}
	}

	@Override
	public void onAppAction(String packName) {
	}

	@Override
	public String getDataUrl() {
		return PageDataBean.MANAGER_URL;
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		updateDownloadingCount();
	}

	@Override
	public void updateContent(PageDataBean bean) {
		mBean = bean;
		calculateScore(0);
		updateDownloadingCount();
	}

	@Override
	public void onResume() {
		// 更新个数
		Setting setting = new Setting(getContext());
		TextView updateCount = (TextView) findViewById(R.id.appupdate_text_hint);
		int count = setting.getInt(Setting.UPDATE_COUNT);
		if (count > 0) {
			updateCount.setVisibility(View.VISIBLE);
			updateCount.setText(count + "");
		} else {
			updateCount.setVisibility(View.GONE);
		}
		updateDownloadingCount();
		// 重新计算分数
		int point = setting.getInt(Setting.METER_LAST_POINT);
		calculateScore(point);
	}

	@Override
	public void beginPage() {
		if (mBean == null) {
			return;
		}
		StatService.trackBeginPage(getContext(), "" + mBean.mStatisticsTitle);
	}

	@Override
	public void endPage() {
		if (mBean == null) {
			return;
		}
		StatService.trackEndPage(getContext(), "" + mBean.mStatisticsTitle);
	}

	/**
	 * 计算分数
	 */
	private void calculateScore(int minScore) {
		if (mCalculating) {
			return;
		}
		final Setting setting = new Setting(getContext());
		boolean calculate = setting.getBoolean(Setting.METER_NEED_CALCULATE,
				false);
		int point = setting.getInt(Setting.METER_LAST_POINT);
		if (calculate || point <= 0) {
			// 获取当前咪表的值
			RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
			board.removeView(mScanView);
			if (mScanView != null) {
				mScanView.setCallBack(null);
			}
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			mScanView = new ScanView(getContext());
			mScanView.setCallBack(new ProgressCallBack() {

				@Override
				public void progressUpdate(int progress) {
					setScore(progress);
				}
			});
			board.addView(mScanView, lp);
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.managercontroller),
					new TARequest(ManagerController.CALCULATE, minScore),
					new TAIResponseListener() {

						@Override
						public void onStart() {
							mCalculating = true;
						}

						@Override
						public void onSuccess(TAResponse response) {
							final int rprogress = (Integer) response.getData();
							setting.putInt(Setting.METER_LAST_POINT, rprogress);
							if (mScanView != null) {
								mScanView.setCallBack(new ProgressCallBack() {

									@Override
									public void progressUpdate(int progress) {
										setScore(progress);
										if (Math.abs(rprogress - progress) <= 5) {
											if (mScanView != null) {
												mScanView.setCallBack(null);
											}
											setScore(rprogress);
											RelativeLayout board = (RelativeLayout) findViewById(R.id.board);
											board.removeView(mScanView);
											mCalculating = false;
										}
									}
								});
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
			setting.putBoolean(Setting.METER_NEED_CALCULATE, false);
		} else {
			setScore(point);
		}
	}

	/**
	 * 更新当前分数
	 */
	private void setScore(int progress) {
		TextView score = (TextView) findViewById(R.id.score);
		score.setText("" + progress);
		TextView unit = (TextView) findViewById(R.id.unit);
		if (progress <= 64) {
			findViewById(R.id.board).setBackgroundColor(0xFFd45856);
			unit.setTextColor(0xFFFFd3d3);
		} else if (progress <= 84) {
			findViewById(R.id.board).setBackgroundColor(0xFFeb9837);
			unit.setTextColor(0xFFf4e2ac);
		} else {
			findViewById(R.id.board).setBackgroundColor(0xFF32b27c);
			unit.setTextColor(0xFFc3fee5);
		}
	}
}
