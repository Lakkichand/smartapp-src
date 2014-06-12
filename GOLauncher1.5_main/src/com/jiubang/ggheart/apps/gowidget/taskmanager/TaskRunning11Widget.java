package com.jiubang.ggheart.apps.gowidget.taskmanager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.IllegalFormatException;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.launcher.taskmanager.TaskMgrControler;
import com.go.util.log.LogUnit;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.gowidget.GoWidgetAdapter;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class TaskRunning11Widget extends FrameLayout
		implements
			BroadCasterObserver,
			View.OnLongClickListener {

	private final int TIMER_DURATION = 1000;

	private final int TIMER_REFRESH_DURATION = TIMER_DURATION * 60;

	private PointerView mPointerView;

	private ImageView mKillButton;
	/** 显示内存状况的文本 */
	private DeskTextView mText;

	private Context mContext;

	private long mTotalMemory;

	private ImageView mWrapView;
	private ImageView mBgView;

	private CountDownTimer mTimer;

	private Resources mResources;

	private CountDownTimer mCycleTimer;

	private long mRealtime;

	private boolean isStartClick;

	// 是否提示手机优化内存数量
	private boolean mIsToast = false;
	// 记录上一次可用内存用于计算
	private int mAgoAvilableSize;

	/** 接收锁定状态广播 */
	private ShowLockListReceiver showLockListReceiver = null;
	/** 动作过滤 */
	private IntentFilter intentFilter2 = null;

	public TaskRunning11Widget(Context context) {
		super(context, null);
	}

	public TaskRunning11Widget(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public TaskRunning11Widget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}

	private void init() {
		mResources = getGoWidgetResources(GoWidgetAdapter.TASK_MANAGER);
		if (mResources == null) {
			throw new RuntimeException("no resource found for task widget 1x1 !!");
		}

		AppCore.getInstance().getTaskMgrControler().registerObserver(this);
		registerBroadcast();
		isStartClick = false;
		mTotalMemory = 0;
	}

	private Resources getGoWidgetResources(String themePackage) {
		if (themePackage == null) {
			return null;
		}
		Resources resources = null;
		try {
			resources = mContext.getPackageManager().getResourcesForApplication(themePackage);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return resources;
	}

	public void onStart(Bundle bundle) {
		if (mPointerView != null) {
			mPointerView.updateAnagel(1.1f);
			mTimer = new CountDownTimer(500, 500) {

				@Override
				public void onTick(long millisUntilFinished) {

				}

				@Override
				public void onFinish() {
					mIsToast = false;
					update();
				}
			};
			mTimer.start();
		}
		isStartClick = true;
		// initCycleTimerBean();
		// try {
		// int widget_id = bundle.getInt("gowidget_Id");
		// Intent intent = new
		// Intent("com.gau.go.launcherex.gowidget.taskmanager.StatisticsService");
		// Bundle bundle2 = new Bundle();
		// bundle2.putInt("action", 0);
		// bundle2.putInt("widgetid", widget_id);
		// intent.putExtras(bundle2);
		// getContext().startService(intent);
		// } catch (Exception e) {
		// // TODO: handle exception
		// e.printStackTrace();
		// }

	}

	public void onDelete(int widgetId) {
		// try {
		// Intent intent = new
		// Intent("com.gau.go.launcherex.gowidget.taskmanager.StatisticsService");
		// Bundle bundle2 = new Bundle();
		// bundle2.putInt("action", 1);
		// bundle2.putInt("widgetid", widgetId);
		// intent.putExtras(bundle2);
		// getContext().startService(intent);
		// } catch (Exception e) {
		// // TODO: handle exception
		// e.printStackTrace();
		// }
	}

	/**
	 * 主题更换入口
	 * 
	 * @param packageName
	 * @param type
	 * @throws NameNotFoundException
	 */
	public boolean onApplyTheme(Bundle bundle) throws NameNotFoundException {

		String packageName = bundle.getString(Constant.GOWIDGET_THEME);
		int type = bundle.getInt(Constant.GOWIDGET_TYPE);
		int themeId = bundle.getInt(Constant.GOWIDGET_THEMEID);

		// 如果该包是widget当前包的话：
		if (packageName.equals(GoWidgetAdapter.TASK_MANAGER)) {
			initWidget();
			return true;
		}

		InputStream inputStream = XmlParserFactory.createInputStream(getContext(), packageName,
				Constant.WIDGETTHEMEFILENAME);
		// 主题文件不存在，直接返回
		if (null == inputStream) {
			return false;
		}
		// //开始解析
		TaskManagerThemeAnalysis taskManagerThemeAnalysis = new TaskManagerThemeAnalysis(
				Constant.STYLE11, themeId);
		try {
			Analysis.parser(taskManagerThemeAnalysis, inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		TaskManagerThemeBean taskManagerThemeBean = taskManagerThemeAnalysis.taskManagerThemeBean;
		Resources resTheme = getContext().getPackageManager().getResourcesForApplication(
				packageName);
		try {

			mBgView = (ImageView) findViewById(R.id.bgid);
			mBgView.setImageDrawable(getThemeDrawable(resTheme, taskManagerThemeBean.widgetBg,
					packageName));

			mPointerView = (PointerView) findViewById(R.id.movepoint);
			mPointerView.setImageDrawable(getThemeDrawable(resTheme, taskManagerThemeBean.point,
					packageName));

			mWrapView = (ImageView) findViewById(R.id.wrapimage);
			mWrapView.setImageDrawable(getThemeDrawable(resTheme, taskManagerThemeBean.wrap,
					packageName));

			mKillButton = (ImageView) findViewById(R.id.applybutton);
			mKillButton.setImageDrawable(getThemeDrawable(resTheme, taskManagerThemeBean.killBtn,
					packageName));

			mText = (DeskTextView) findViewById(R.id.text);
			mText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			if (taskManagerThemeBean.fontColor != null) {
				mText.setTextColor(Color.parseColor(taskManagerThemeBean.fontColor));
			} else {
				mText.setTextColor(Color.parseColor("#B312ff00"));
			}

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		}
		return true;
	}

	public Drawable getThemeDrawable(Resources resources, String drawableName, String packageName) {
		Drawable drawable = null;
		// 如果名字为"none"，不获取资源，返回空
		if (resources != null && !drawableName.equals(Constant.NONE)) {
			int identifier = resources.getIdentifier(drawableName, Constant.DRAWABLE, packageName);
			if (identifier != 0) {
				drawable = resources.getDrawable(identifier);
			}
		}
		return drawable;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		// mResources = getGoWidgetResources(GoWidgetAdapter.TASK_MANAGER);
		if (mResources != null) {
			initWidget();

		} else {
			removeAllViews();
			AppCore.getInstance().getTaskMgrControler().unRegisterObserver(this);
		}
	}

	private void initWidget() {
		int identifier;
		mBgView = (ImageView) findViewById(R.id.bgid);

		// 设置显示图标区域的背景
		identifier = mResources.getIdentifier("task11background", "drawable",
				GoWidgetAdapter.TASK_MANAGER);
		if (identifier != 0) {
			try {
				mBgView.setImageDrawable(mResources.getDrawable(identifier));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}

		}

		mText = (DeskTextView) findViewById(R.id.text);
		mText.setTextColor(Color.parseColor("#B312ff00"));

		// 设置显示指针图片
		mPointerView = (PointerView) findViewById(R.id.movepoint);
		identifier = mResources
				.getIdentifier("movepoint", "drawable", GoWidgetAdapter.TASK_MANAGER);
		if (identifier != 0) {
			try {
				mPointerView.setImageDrawable(mResources.getDrawable(identifier));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}

			mPointerView.updateCentral();
			// mPointerView.updateAnagel(0);
			updateMemory();
		}

		// 设置罩子图片
		mWrapView = (ImageView) findViewById(R.id.wrapimage);
		identifier = mResources.getIdentifier("wrap", "drawable", GoWidgetAdapter.TASK_MANAGER);
		if (identifier != 0) {
			try {
				mWrapView.setImageDrawable(mResources.getDrawable(identifier));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}

		}
		// 加载杀死所有程序按钮的资源
		mKillButton = (ImageView) findViewById(R.id.applybutton);
		mKillButton.setOnLongClickListener(this);
		identifier = mResources.getIdentifier("task11kill_selector", "drawable",
				GoWidgetAdapter.TASK_MANAGER);
		if (identifier != 0) {

			try {
				mKillButton.setImageDrawable(mResources.getDrawable(identifier));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}
		}
		mKillButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPointerView != null && mPointerView.isMoving()) {
					return;
				}

				// 通知任务管理器APP更新
				Intent intent = new Intent();
				intent.setAction(ICustomAction.ACTION_REQUEST_UPDATE_TOAPP);
				mContext.sendBroadcast(intent);

				String tempText = mText.getText().toString();
				if (tempText != null && !"".equals(tempText) && tempText.contains("M")) {
					try {
						mAgoAvilableSize = Integer.parseInt(tempText.trim().substring(0,
								tempText.indexOf("M")));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}

				isStartClick = true;
				mIsToast = true;

				// mPointerView.starAnimation(toAngel);
				// if (mPointerView != null)
				// {
				// mPointerView.updateAnagel(-20);
				// }

				AppCore.getInstance().getTaskMgrControler().terminateAll();
				// update();
			}
		});
	}

	private void updateMemory() {
		long availMemory = AppCore.getInstance().getTaskMgrControler().retriveAvailableMemory() / 1024;
		// 更新文本
		if (mTotalMemory == 0) {
			mTotalMemory = AppCore.getInstance().getTaskMgrControler().retriveTotalMemory() / 1024;

		}
		if (mTotalMemory != 0) {
			availMemory = mTotalMemory - availMemory;
		}
		mText.setText(availMemory + "M");
	}

	// 桌面反调用
	public void onRemove(int widgetId) {
		// 释放图片资源
		mKillButton.setImageDrawable(null);
		mKillButton = null;
		mPointerView.setImageDrawable(null);
		mPointerView = null;
		mWrapView.setImageDrawable(null);
		mWrapView = null;
		mBgView.setImageDrawable(null);
		mBgView = null;
		if (mTimer != null) {
			mTimer.cancel();
		}
		if (mCycleTimer != null) {
			mCycleTimer.cancel();
		}

		removeAllViews();
		AppCore.getInstance().getTaskMgrControler().unRegisterObserver(this);
		mContext.unregisterReceiver(showLockListReceiver);

		return;
	}

	private void initCycleTimerBean() {
		if (mCycleTimer != null) {
			mCycleTimer.cancel();
			mRealtime = 0;
		}
		// 计时器启动
		mCycleTimer = new CountDownTimer(Long.MAX_VALUE, TIMER_REFRESH_DURATION) {

			@Override
			public void onTick(long millisUntilFinished) {
				if (mRealtime == 0) {
					mRealtime = millisUntilFinished;

					if (isStartClick) {
						isStartClick = false;

					} else {
						update();
					}
				}
				long countdownInterval = mRealtime - millisUntilFinished;
				LogUnit.i("countdownInterval : " + countdownInterval);
				if (countdownInterval > TIMER_REFRESH_DURATION) {
					mRealtime = millisUntilFinished;
					update();
				}
			}

			@Override
			public void onFinish() {
			}
		};
		mCycleTimer.start();
	}

	/**
	 * 刷新内存状态的指示器和文本
	 */
	private void update() {
		long availMemory = AppCore.getInstance().getTaskMgrControler().retriveAvailableMemory() / 1024;
		// 更新文本

		if (mTotalMemory == 0) {
			mTotalMemory = AppCore.getInstance().getTaskMgrControler().retriveTotalMemory() / 1024;
		}
		mText.setText(availMemory + "M");

		long usedMemory = mTotalMemory - availMemory;

		// 弹出提示
		if (mIsToast && availMemory >= mAgoAvilableSize) {
			long saveMemorySize = availMemory - mAgoAvilableSize;
			if (saveMemorySize <= 5) {
				Toast toast = Toast.makeText(getContext(),
						getResources().getString(R.string.task_killer_toast_best),
						Toast.LENGTH_SHORT);
				toast.show();
			} else {
				try {
					String toastStr = String.format(
							getResources().getString(R.string.task_killer_toast), saveMemorySize);
					Toast toast = Toast.makeText(getContext(), toastStr, Toast.LENGTH_SHORT);
					toast.show();
				} catch (IllegalFormatException e) {
					e.printStackTrace();
				}
			}
		}

		// Log.i("widget", "mTotalMemory: " + mTotalMemory);
		// 更新图片
		if (mTotalMemory == 0) {
			// mPointerView.starAnimation(toAngel);
			if (mPointerView != null) {
				mPointerView.updateAnagel(0);
			}

		} else {
			if (mPointerView != null) {
				float temp = usedMemory / (mTotalMemory + 0.0f);
				mPointerView.updateAnagel(temp * 100);
			}
		}
	}

	/**
	 * 刷新内存状态的指示器和文本
	 */
	private void updateBack() {
		long availMemory = AppCore.getInstance().getTaskMgrControler().retriveAvailableMemory() / 1024;
		// 更新文本

		if (mTotalMemory == 0) {
			mTotalMemory = AppCore.getInstance().getTaskMgrControler().retriveTotalMemory() / 1024;
		}
		mText.setText(availMemory + "M");

		long usedMemory = mTotalMemory - availMemory;

		if (mTotalMemory != 0 && mPointerView != null) {
			float temp = usedMemory / (mTotalMemory + 0.0f);
			mPointerView.updateAnagel(temp * 100);
		}

		// 弹出提示
		if (mIsToast && availMemory >= mAgoAvilableSize) {
			long saveMemorySize = availMemory - mAgoAvilableSize;
			if (saveMemorySize <= 5) {
				Toast toast = Toast.makeText(getContext(),
						getResources().getString(R.string.task_killer_toast_best),
						Toast.LENGTH_SHORT);
				toast.show();
			} else {
				try {
					String toastStr = String.format(
							getResources().getString(R.string.task_killer_toast), saveMemorySize);
					Toast toast = Toast.makeText(getContext(), toastStr, Toast.LENGTH_SHORT);
					toast.show();
				} catch (IllegalFormatException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 注册广播
	 */
	private void registerBroadcast() {

		showLockListReceiver = new ShowLockListReceiver();
		intentFilter2 = new IntentFilter();
		intentFilter2.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter2.addAction(ICustomAction.ACTION_RESPOND_UPDATE_FROMAPP);
		mContext.registerReceiver(showLockListReceiver, intentFilter2);
		Intent intent2 = new Intent();
		intent2.setAction(ICustomAction.ACTION_REQUESTISSHOWLOCK);
		mContext.sendBroadcast(intent2);
	}

	private class ShowLockListReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// 开屏自动更新
			if (Intent.ACTION_SCREEN_ON.equals(action)
					|| ICustomAction.ACTION_RESPOND_UPDATE_FROMAPP.equals(action)) {
				mIsToast = false;
				update();
			}
		}
	}

	@Override
	public void onBCChange(int msgId, int param, Object object,
			@SuppressWarnings("rawtypes") List objects) {
		switch (msgId) {
			case TaskMgrControler.TERMINATE_ALL : {
				// 关闭所有进程
				// updateAppsList();
				// 计时器启动

				if (mCycleTimer != null) {
					mCycleTimer.cancel();
				}
				// 计时器启动

				initTimerBean();
			}
				break;
			default :
				break;
		}

	}

	private void initTimerBean() {

		if (mTimer != null) {
			mTimer.cancel();
		}
		if (mCycleTimer != null) {
			mCycleTimer.cancel();
		}

		// 做动画
		// mPointerView.starAnimation(toAngel);
		int duration = 500;
		if (mPointerView != null) {
			duration = mPointerView.backToZeroAngel();
		}

		// 计时器启动
		mTimer = new CountDownTimer(duration, duration) {

			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				updateBack();
				mIsToast = false;
				// initCycleTimerBean();
			}
		};
		mTimer.start();
	}

	@Override
	public boolean onLongClick(View v) {
		// 判断当前是否锁屏
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(getContext());
			return true;
		}

		if (v.equals(mKillButton)) {
			performLongClick();
		}
		return true;
	}

	public String readFileFromLocal(Context context, String filePath) {

		AssetManager assets = context.getAssets();
		InputStream is = null;
		try {
			is = assets.open(filePath);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuffer xml = new StringBuffer();
		try {
			isr = new InputStreamReader(is, "UTF-8");
			char[] b = new char[4096];
			for (int n; (n = isr.read(b)) != -1;) {
				xml.append(new String(b, 0, n));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return xml.toString();
	}

}
