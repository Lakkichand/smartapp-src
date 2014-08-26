package com.zhidian.wifibox.view;

import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.APKManageActivity;
import com.zhidian.wifibox.activity.AppManageActivity;
import com.zhidian.wifibox.activity.CleanupActivity;
import com.zhidian.wifibox.activity.DownloadManagerActivity;
import com.zhidian.wifibox.controller.ManagerController;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.Setting;

/**
 * 管理界面
 * 
 * @author xiedezhi
 * 
 */
public class ManagerContainer extends LinearLayout implements IContainer {

	private ImageView mMeter;
	private Button mAKey;
	private TextView mProgress;
	private View mMeterFrame;
	private ImageView AnimaImg;
	/**
	 * 是否正在一键清理
	 */
	private boolean mCleaning = false;

	public ManagerContainer(Context context) {
		super(context);
	}

	public ManagerContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		AnimaImg = (ImageView) findViewById(R.id.anima_image);
		AnimaImg.setVisibility(View.GONE);
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
				// TODO 应用互传
			}
		});
		findViewById(R.id.appmanager).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 软件管理
				Intent intent = new Intent();
				intent.setClass(getContext(), AppManageActivity.class);
				getContext().startActivity(intent);
			}
		});
		findViewById(R.id.popularity).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 周边人气
			}
		});
		findViewById(R.id.apkmanager).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 安装包管理
				Intent intent = new Intent();
				intent.setClass(getContext(), APKManageActivity.class);
				getContext().startActivity(intent);
			}
		});
		findViewById(R.id.clearup).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 手机清理
				Intent intent = new Intent(getContext(), CleanupActivity.class);
				getContext().startActivity(intent);
			}
		});

		mMeter = (ImageView) findViewById(R.id.meter);
		mAKey = (Button) findViewById(R.id.akey);
		mAKey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCleaning) {
					return;
				}
				// 一键清理
				mCleaning = true;
				int current = 0;

				// setAnimaAlpha();

				try {
					current = Integer.valueOf(mProgress.getText().toString());
				} catch (Exception e) {
					e.printStackTrace();
				}

				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.managercontroller),
						new TARequest(ManagerController.CLEAN, current),
						new TAIResponseListener() {

							@Override
							public void onStart() {
							}

							@Override
							public void onSuccess(TAResponse response) {
								AnimaImg.setVisibility(View.VISIBLE);
								int progress = (Integer) response.getData();
								mProgress.setText(progress + "");
								mCleaning = false;

								gotoAnimation(progress);
								gotoBackground(progress);
							}

							@Override
							public void onRuning(TAResponse response) {
								AnimaImg.clearAnimation();
								AnimaImg.setVisibility(View.GONE);
								int progress = (Integer) response.getData();
								gotoBackground(progress);
								mProgress.setText(progress + "");
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
		mProgress = (TextView) findViewById(R.id.progress_text);
		Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(),
				"fonts/desktop_speedup_font.ttf");
		mProgress.setTypeface(typeFace);
		mMeter.setImageBitmap(DrawUtil.sMeterBitmap);
		mMeterFrame = findViewById(R.id.meter_frame);

		InfoUtil util = new InfoUtil(getContext());
		int width = util.getWidth();
		int height = (int) (width * 1.0 / 480.0 * 369.0 + 0.5);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width,
				height);
		mMeter.setLayoutParams(lp);

		LinearLayout.LayoutParams lpx = new LinearLayout.LayoutParams(width,
				height);
		mMeterFrame.setLayoutParams(lpx);

		// 更新个数
		Setting setting = new Setting(getContext());
		TextView updateCount = (TextView) findViewById(R.id.appmanager_text_hint);
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

	/**
	 * 一键清理
	 */
	public void oneKeyCleanup() {
		if (mAKey != null) {
			mAKey.performClick();
		}
	}

	/**
	 * 火箭移动动画
	 * 
	 * @param progress
	 */
	private void gotoAnimation(final int progress) {
		if (progress >= 0 && progress <= 50) {
			AnimaImg.setImageResource(R.drawable.rocket_icon_tail);
		} else if (progress > 50 && progress <= 75) {
			AnimaImg.setImageResource(R.drawable.car_icon_tail);
		} else if (progress > 75) {
			AnimaImg.setImageResource(R.drawable.bike_icon_tail);
		}

		Animation animation = AnimationUtils.loadAnimation(getContext(),
				R.anim.set_right_to_left);
		animation.setInterpolator(new AccelerateDecelerateInterpolator());
		animation.setDuration(1000);
		animation.setFillEnabled(true);
		animation.setFillAfter(true);
		animation.setZAdjustment(Animation.ZORDER_TOP);
		AnimaImg.startAnimation(animation);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (progress >= 0 && progress <= 50) {
					AnimaImg.setImageResource(R.drawable.rocket_icon);
				} else if (progress > 50 && progress <= 75) {
					AnimaImg.setImageResource(R.drawable.car_icon);
				} else if (progress > 75) {
					AnimaImg.setImageResource(R.drawable.bike_icon);
				}
			}
		});

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
		Setting setting = new Setting(getContext());
		long last = setting.getLong(Setting.METER_UPDATE_TIME);
		long now = System.currentTimeMillis();
		if (now - last <= 2500) {
			ActivityManager acm = (ActivityManager) TAApplication
					.getApplication()
					.getSystemService(Context.ACTIVITY_SERVICE);
			MemoryInfo outInfo = new MemoryInfo();
			acm.getMemoryInfo(outInfo);
			long avi = (long) (outInfo.availMem / 1024.0 / 1024.0 + 0.5);
			long total = (long) (InfoUtil.getTotalRAM() / 1024.0 + 0.5);
			long used = total - avi;
			int progress = (int) (used * 1.0 / total * 100 + 0.5);
			gotoBackground(progress);
			mProgress.setText(progress + "");
			AnimaImg.clearAnimation();
			AnimaImg.setVisibility(View.GONE);
			return;
		}
		setting.putLong(Setting.METER_UPDATE_TIME, now);
		// 获取当前咪表的值
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.managercontroller),
				new TARequest(ManagerController.CALCULATE, null),
				new TAIResponseListener() {

					@Override
					public void onStart() {
					}

					@Override
					public void onSuccess(TAResponse response) {
						if (mCleaning) {
							return;
						}
						AnimaImg.setVisibility(View.VISIBLE);
						int progress = (Integer) response.getData();

						mProgress.setText(progress + "");

						gotoAnimation(progress);
						gotoBackground(progress);
					}

					@Override
					public void onRuning(TAResponse response) {
						if (mCleaning) {
							return;
						}
						AnimaImg.clearAnimation();
						AnimaImg.setVisibility(View.GONE);
						int progress = (Integer) response.getData();
						gotoBackground(progress);
						mProgress.setText(progress + "");
					}

					@Override
					public void onFailure(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}
				}, true, false);
		updateDownloadingCount();
	}

	/**
	 * 根据数值改变背景颜色
	 */
	protected void gotoBackground(int progress) {
		if (progress >= 0 && progress <= 50) {
			Drawable meter = new MetersDrawable(getResources(),
					DrawUtil.sMeterBitmap, progress);
			mMeter.setImageDrawable(meter);
		} else if (progress > 50 && progress <= 75) {
			Drawable meter = new MetersDrawable(getResources(),
					DrawUtil.oMeterBitmap, progress);
			mMeter.setImageDrawable(meter);
		} else if (progress > 75) {
			Drawable meter = new MetersDrawable(getResources(),
					DrawUtil.rMeterBitmap, progress);
			mMeter.setImageDrawable(meter);
		}
	}

	@Override
	public void onResume() {
		// 更新个数
		Setting setting = new Setting(getContext());
		TextView updateCount = (TextView) findViewById(R.id.appmanager_text_hint);
		int count = setting.getInt(Setting.UPDATE_COUNT);
		if (count > 0) {
			updateCount.setVisibility(View.VISIBLE);
			updateCount.setText(count + "");
		} else {
			updateCount.setVisibility(View.GONE);
		}
		updateDownloadingCount();
	}

}
