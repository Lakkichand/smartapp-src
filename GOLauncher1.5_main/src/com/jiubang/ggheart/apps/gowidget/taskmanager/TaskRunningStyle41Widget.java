package com.jiubang.ggheart.apps.gowidget.taskmanager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.launcher.taskmanager.TaskMgrControler;
import com.go.util.graphics.DrawUtils;
import com.go.util.log.LogUnit;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.gowidget.GoWidgetAdapter;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class TaskRunningStyle41Widget extends LinearLayout
		implements
			BroadCasterObserver,
			View.OnLongClickListener,
			AnimationListener {
	private final int TIMER_DURATION = 1000;

	private final int TIME_TO_START = 600;

	private final int TIMER_REFRESH_DURATION = TIMER_DURATION * 60;

	private final int SIZE = 10;

	private final String GO_TASKMANAGER_APP = "com.gau.go.launcherex.gowidget.taskmanager";

	private LinearLayout widgetLayout = null;

	private LinearLayout barLayout = null;

	private TextView availTV = null;

	private TextView totalTV = null;

	// private ImageView mShowLink = null;

	private ImageButton mKillButton = null;

	private ImageButton mRefreshButton = null;

	private TextView availBgIV = null;

	private Context mContext;

	private long totalMemory;

	private Drawable smallDrawable = null;

	private Drawable middleDrawable = null;

	private Drawable heightDrawable = null;

	private String packName = GoWidgetAdapter.TASK_MANAGER;

	/**
	 * 正在运行程序列表
	 */
	private ArrayList<FunTaskItemInfo> mProgressesList;
	/**
	 * 杀死所有程序，定时刷新器
	 */
	private CountDownTimer mTimer;

	private Resources mResources;

	/**
	 * 循环定时器
	 */
	private CountDownTimer mCycleTimer;
	/**
	 * 用于计算定时刷新
	 */
	private long mRealtime;

	/**
	 * 用于判断动画是否完成
	 */
	private boolean mAnimationIsFinish;

	// 进度条加减动画
	private Animation myAnimation_Scale_Reduce;
	private Animation myAnimation_Scale_Add;

	/**
	 * 用于UI显示的列表
	 */
	private ArrayList<ArrayList<FunTaskItemInfo>> list;

	/** 接收锁定状态广播 */
	private ShowLockListReceiver showLockListReceiver = null;
	/** 动作过滤 */
	private IntentFilter intentFilter2 = null;

	private Handler mHandler;
	private static final int MSG_INIT = 0;
	private static final int MSG_UPDATE = 1;
	private static final int ADD_ANIMATION = 2;

	private boolean mIsFirstLayout;

	private String mTotleFontColor = "#FFFFFF";

	private String mAvailFontColor = "#FFFFFF";

	// 是否提示手机优化内存数量
	private boolean mIsToast = false;
	// 进度条是否做动画
	private boolean mIsMove = false;
	// 记录上一次可用内存用于计算
	private int mAgoAvilableSize;

	// 动画的时间
	private static final int MILLSINFUTRRE = 500;

	public TaskRunningStyle41Widget(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public TaskRunningStyle41Widget(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	private void init() {
		mResources = getGoWidgetResources(GoWidgetAdapter.TASK_MANAGER);
		if (mResources == null) {
			throw new RuntimeException("no resource found for task widget 4x1 !!");
		}
		AppCore.getInstance().getTaskMgrControler().registerObserver(this);
		registerBroadcast();
	}

	/**
	 * 注册广播
	 */
	private void registerBroadcast() {

		showLockListReceiver = new ShowLockListReceiver();
		intentFilter2 = new IntentFilter();
		intentFilter2.addAction(ICustomAction.ACTION_RESPONDISSHOWLOCK);
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
			if (ICustomAction.ACTION_RESPONDISSHOWLOCK.equals(action)) {
				updateAppsList();
				// 开屏自动更新
			} else if (Intent.ACTION_SCREEN_ON.equals(action)
					|| ICustomAction.ACTION_RESPOND_UPDATE_FROMAPP.equals(action)) {
				mIsToast = false;
				update();
			}
		}
	}

	/**
	 * 获取Apk包的资源类
	 * 
	 * @param themePackage
	 * @return
	 */
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

	public boolean onApplyTheme(Bundle bundle) throws NameNotFoundException {
		String packageName = bundle.getString(Constant.GOWIDGET_THEME);
		int type = bundle.getInt(Constant.GOWIDGET_TYPE);
		int themeId = bundle.getInt(Constant.GOWIDGET_THEMEID);
		packName = packageName;
		if (packageName.equals(GoWidgetAdapter.TASK_MANAGER)) {
			initElement();
			return true;
		}
		InputStream inputStream = XmlParserFactory.createInputStream(getContext(), packageName,
				Constant.WIDGETTHEMEFILENAME);
		// 主题文件不存在，直接返回
		if (null == inputStream) {
			return false;
		}
		// 开始解析
		TaskManagerThemeAnalysis taskManagerThemeAnalysis = new TaskManagerThemeAnalysis(
				Constant.STYLENEW41, themeId);
		Analysis.parser(taskManagerThemeAnalysis, inputStream);

		TaskManagerThemeBean taskManagerThemeBean = taskManagerThemeAnalysis.taskManagerThemeBean;
		Resources resTheme = getContext().getPackageManager().getResourcesForApplication(
				packageName);
		try {
			widgetLayout.setBackgroundDrawable(getThemeDrawable(resTheme,
					taskManagerThemeBean.widgetBg, packageName));
			barLayout.setBackgroundDrawable(getThemeDrawable(resTheme,
					taskManagerThemeBean.icoLayoutBg, packageName));
			mKillButton.setBackgroundDrawable(getThemeDrawable(resTheme,
					taskManagerThemeBean.killBtn, packageName));

			mRefreshButton.setBackgroundDrawable(getThemeDrawable(resTheme,
					taskManagerThemeBean.refreshBtn, packageName));
			mTotleFontColor = taskManagerThemeBean.fontColor;
			mAvailFontColor = taskManagerThemeBean.fontColor;
			availTV.setTextColor(Color.parseColor(mTotleFontColor));
			totalTV.setTextColor(Color.parseColor(mAvailFontColor));
			// mShowLink.setImageDrawable(getThemeDrawable(resTheme,
			// taskManagerThemeBean.showLink, packageName));
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
			OutOfMemoryHandler.handle();
			return false;
		}

		return true;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		widgetLayout = (LinearLayout) findViewById(R.id.layout_gobal);
		barLayout = (LinearLayout) findViewById(R.id.layout_bar_bg);
		barLayout.setOnClickListener(viewClickListener);
		barLayout.setOnLongClickListener(this);

		availTV = (TextView) findViewById(R.id.avail_txt);
		totalTV = (TextView) findViewById(R.id.total_txt);
		availBgIV = (TextView) findViewById(R.id.avail_img);
		// mShowLink = (ImageView) findViewById(R.id.link_img);

		mKillButton = (ImageButton) findViewById(R.id.task_manager_clean);
		mKillButton.setOnClickListener(viewClickListener);
		mKillButton.setOnLongClickListener(this);

		mRefreshButton = (ImageButton) findViewById(R.id.task_manager_refresh);
		mRefreshButton.setOnClickListener(viewClickListener);
		mRefreshButton.setOnLongClickListener(this);
		initElement();
		mIsFirstLayout = true;
		initHandle();
	}

	private void initHandle() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case MSG_INIT :
						updateText();
						initElement();
						break;
					case MSG_UPDATE :
						updateText();
						break;
					case ADD_ANIMATION :
						addProgressAnimation();
						break;
					default :
						break;
				}
			}
		};
	}

	private void initElement() {
		updateAppsList();
		if (mResources == null) {
			return;
		}
		// 设置全局背景
		int identifier = mResources.getIdentifier("task_manager_style_4_1_widget_bg", "drawable",
				packName);
		if (identifier != 0) {
			try {
				widgetLayout.setBackgroundDrawable(mResources.getDrawable(identifier));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}
		}

		// 进度栏背景
		identifier = mResources.getIdentifier("task_manager_style_4_1_bar_bg_focus", "drawable",
				packName);
		if (identifier != 0) {
			try {
				barLayout.setBackgroundDrawable(mResources.getDrawable(identifier));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}
		}
		// 刷新进程
		identifier = mResources.getIdentifier("task_manager_style_4_1_refresh_selector",
				"drawable", packName);
		if (identifier != 0) {
			try {
				mRefreshButton.setBackgroundDrawable(mResources.getDrawable(identifier));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}
		}
		// 关闭所有进程
		identifier = mResources.getIdentifier("task_manager_style_4_1_clean_selector", "drawable",
				packName);
		if (identifier != 0) {
			try {
				mKillButton.setBackgroundDrawable(mResources.getDrawable(identifier));
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}
		}

		identifier = mResources.getIdentifier("task_manager_style_4_1_bar_samll", "drawable",
				packName);
		if (identifier != 0) {
			try {
				smallDrawable = mResources.getDrawable(identifier);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}

		}
		identifier = mResources.getIdentifier("task_manager_style_4_1_bar_middle", "drawable",
				packName);
		if (identifier != 0) {
			try {
				middleDrawable = mResources.getDrawable(identifier);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}

		}
		identifier = mResources.getIdentifier("task_manager_style_4_1_bar_height", "drawable",
				packName);
		if (identifier != 0) {
			try {
				heightDrawable = mResources.getDrawable(identifier);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}

		}
		// 点击指示图标
		// identifier = mResources.getIdentifier("task_manager_style_4_1_link",
		// "drawable", packName);
		// if (identifier != 0) {
		// try {
		// mShowLink.setImageDrawable(mResources.getDrawable(identifier));
		// } catch (OutOfMemoryError e) {
		// e.printStackTrace();
		// OutOfMemoryHandler.handle();
		// }
		//
		// }

		if (packName.equals(GoWidgetAdapter.TASK_MANAGER)) {

			availTV.setTextColor(Color.WHITE);
			totalTV.setTextColor(Color.BLACK);
		} else {
			availTV.setTextColor(Color.parseColor(mTotleFontColor));
			totalTV.setTextColor(Color.parseColor(mAvailFontColor));
		}
	}

	public void onStart(Bundle bundle) {
		// updateText();
		// initCycleTimerBean();
		update();
		// initElement();
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

	private OnClickListener viewClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String tempText = availTV.getText().toString();
			if (tempText != null && !"".equals(tempText) && tempText.contains("M")) {
				try {
					mAgoAvilableSize = Integer
							.parseInt(tempText.substring(0, tempText.indexOf("M")));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			int id = v.getId();
			if (id == R.id.task_manager_clean) {

				// 判断是否已经结束动画
				if (!mAnimationIsFinish) {
					return;
				}

				mIsToast = true;
				mIsMove = true;

				// 通知任务管理器APP更新
				Intent intent = new Intent();
				intent.setAction(ICustomAction.ACTION_REQUEST_UPDATE_TOAPP);
				mContext.sendBroadcast(intent);

				AppCore.getInstance().getTaskMgrControler().terminateAll(mProgressesList);
			} else if (id == R.id.task_manager_refresh) {

				// 判断是否已经结束动画
				if (!mAnimationIsFinish) {
					return;
				}

				mIsToast = false;
				mIsMove = true;

				// 通知任务管理器APP更新
				Intent intent = new Intent();
				intent.setAction(ICustomAction.ACTION_REQUEST_UPDATE_TOAPP);
				mContext.sendBroadcast(intent);

				update();
				// initCycleTimerBean();

				// 点击进入任务管理器app程序
			} else if (id == R.id.layout_bar_bg) {
				Intent intent = new Intent();
				intent = mContext.getPackageManager().getLaunchIntentForPackage(GO_TASKMANAGER_APP);
				mContext.startActivity(intent);
			}
		}
	};

	/**
	 * 初始化一个循环检查定时器(1分钟检查一次后台正在运行列表的变化)
	 */
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

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (mIsFirstLayout && changed) {
			mIsFirstLayout = false;
			mHandler.sendEmptyMessageDelayed(MSG_INIT, 1000);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mIsFirstLayout = true;
	}

	/**
	 * 获取过滤了白名单的列表
	 * 
	 * @param originArrayList
	 * @return
	 */
	private ArrayList<FunTaskItemInfo> getFilteredList(ArrayList<FunTaskItemInfo> originArrayList) {
		if (null == originArrayList) {
			return null;
		}

		Iterator<FunTaskItemInfo> appIterator = originArrayList.iterator();

		// 设置不显示忽略列表内的程序
		if (FunAppSetting.NEGLECTAPPS == GOLauncherApp.getSettingControler().getFunAppSetting()
				.getShowNeglectApp()) {
			while (appIterator.hasNext()) {
				FunTaskItemInfo aInfo = appIterator.next();
				if (aInfo.isInWhiteList()) {
					appIterator.remove();
				}
			}
		}
		Collections.sort(originArrayList, new Comparator<FunTaskItemInfo>() {
			@Override
			public int compare(FunTaskItemInfo object1, FunTaskItemInfo object2) {
				if (object2.isInWhiteList() && !object1.isInWhiteList()) {
					return 1;
				} else if (!object2.isInWhiteList() && object1.isInWhiteList()) {
					return -1;
				}
				return 0;
			}
		});
		return originArrayList;
	}

	private synchronized void update() {

		updateText();
		updateAppsList();
	}

	private int formLengt = 0;
	private int toLengt = 0;

	// // 动画定时器
	// CountDownTimer reduceCountDownTimer = null;// 减
	// CountDownTimer addCountDownTimer = null;// 加

	// /**
	// * 刷新内存状态的指示器和文本
	// */
	// private synchronized void updateText() {
	//
	// /**
	// * 标志动画正在做
	// */
	// mAnimationIsFinish = false;
	//
	// if (reduceCountDownTimer != null || addCountDownTimer != null) {
	// return;
	// }
	//
	// long availMemory = AppCore.getInstance().getTaskMgrControler()
	// .retriveAvailableMemory() / 1024;
	// // 更新文本
	// if (totalMemory == 0) {
	// totalMemory = AppCore.getInstance().getTaskMgrControler()
	// .retriveTotalMemory() / 1024;
	//
	// }
	// final long usedMemory = totalMemory - availMemory;
	// totalTV.setText(totalMemory + "M");
	// availTV.setText(usedMemory + "M");
	// int barLenght = barLayout.getWidth();
	// if (barLenght == 0) {
	// mHandler.sendEmptyMessageDelayed(MSG_UPDATE, 1000);
	// return;
	// }
	// int tempL = barLenght;
	// try {
	// final float temp = usedMemory / (totalMemory + 0.0f);
	// // if (temp > 0.8f) {
	// // totalTV.setVisibility(View.GONE);
	// // } else {
	// // totalTV.setVisibility(View.VISIBLE);
	// // }
	// formLengt = availBgIV.getWidth();
	// toLengt = (int) (tempL * temp);
	// if (formLengt == 0) {
	// if (temp <= 0.7f) {
	// availBgIV.setBackgroundDrawable(smallDrawable);
	//
	// } else if (temp > 0.7f && temp <= 0.9f) {
	// availBgIV.setBackgroundDrawable(middleDrawable);
	// } else {
	// availBgIV.setBackgroundDrawable(heightDrawable);
	// }
	// availBgIV.setWidth(toLengt);
	// } else {
	// availBgIV.setWidth(formLengt);
	// long millisInFuture = (formLengt - DrawUtils.dip2px(20))
	// / DrawUtils.dip2px(5) * 30;
	// int tempText = 0;
	// if(usedMemory > 0){
	// tempText = (int) (usedMemory / ((formLengt - DrawUtils
	// .dip2px(20)) / DrawUtils.dip2px(5)));
	// }else{
	// tempText = (int)((formLengt - DrawUtils
	// .dip2px(20)) / DrawUtils.dip2px(5));
	// }
	//
	// final int numSize = tempText;
	//
	// reduceCountDownTimer = new CountDownTimer(millisInFuture, 30) {
	// int i = 1;
	//
	// @Override
	// public void onTick(long millisUntilFinished) {
	//
	// i++;
	// formLengt = formLengt - DrawUtils.dip2px(5);
	// availBgIV.post(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// try {
	// Drawable drawable = availBgIV
	// .getBackground();
	// if (drawable != null) {
	// Rect r = drawable.getBounds();
	// drawable.setBounds(r.left, r.top,
	// r.left + formLengt, r.bottom);
	// availBgIV.invalidate();
	// }
	// // availTV.setText((usedMemory - numSize * i)
	// // + "M");
	//
	// availBgIV.setWidth(formLengt);
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// }
	//
	// }
	// });
	// }
	//
	// @Override
	// public void onFinish() {
	//
	// long availMemory = AppCore.getInstance()
	// .getTaskMgrControler().retriveAvailableMemory() / 1024;
	// // 更新文本
	// if (totalMemory == 0) {
	// totalMemory = AppCore.getInstance()
	// .getTaskMgrControler().retriveTotalMemory() / 1024;
	//
	// }
	// final long usedMemory2 = totalMemory - availMemory;
	// // totalTV.setText(totalMemory + "M");
	// // availTV.setText(usedMemory + "M");
	// int barLenght = barLayout.getWidth();
	// int tempL = barLenght;
	// final float temp = usedMemory2 / (totalMemory + 0.0f);
	// formLengt = availBgIV.getWidth();
	// toLengt = (int) (tempL * temp);
	//
	// if (temp <= 0.7f) {
	// availBgIV.setBackgroundDrawable(smallDrawable);
	//
	// } else if (temp > 0.7f && temp <= 0.9f) {
	// availBgIV.setBackgroundDrawable(middleDrawable);
	// } else {
	// availBgIV.setBackgroundDrawable(heightDrawable);
	// }
	//
	// reduceCountDownTimer = null;
	// // long millisInFuture = (toLengt -
	// // DrawUtils.dip2px(50))
	// // / DrawUtils.dip2px(5) * 30;
	// long millisInFuture = (toLengt - DrawUtils.dip2px(20))
	// / DrawUtils.dip2px(5) * 30;
	// int tempText = 0;
	// if(usedMemory2 > 0){
	// tempText = (int) (usedMemory2 / ((toLengt) / DrawUtils
	// .dip2px(5)));
	// }else{
	// tempText = (int)(toLengt / DrawUtils.dip2px(5));
	// }
	//
	// final int numSize = tempText;
	//
	// addCountDownTimer = new CountDownTimer(millisInFuture,
	// 30) {
	// int i = 2;
	//
	// @Override
	// public void onTick(long millisUntilFinished) {
	// i++;
	// formLengt = formLengt + DrawUtils.dip2px(5);
	// availBgIV.post(new Runnable() {
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	// try {
	// if (formLengt < toLengt) {
	// // availTV.setText((numSize * i)
	// // + "M");
	// availBgIV.setWidth(formLengt);
	// Drawable drawable = availBgIV
	// .getBackground();
	// if (drawable != null) {
	// Rect r = drawable
	// .getBounds();
	// drawable.setBounds(r.left,
	// r.top,
	// r.left + formLengt,
	// r.bottom);
	// availBgIV.invalidate();
	// }
	// } else {
	// // availTV.setText(usedMemory2
	// // + "M");
	// }
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// }
	// }
	// });
	// }
	//
	// @Override
	// public void onFinish() {
	// //标志动画已经完成
	// mAnimationIsFinish = true;
	//
	// availTV.setText(usedMemory2+ "M");
	// availBgIV.setWidth(toLengt);
	// if(mIsToast && mAgoAvilableSize >= usedMemory2){
	// long saveMemorySize = mAgoAvilableSize - usedMemory2;
	// if(saveMemorySize <= 5){
	// Toast toast = Toast.makeText(getContext(),
	// getResources().getString(R.string.task_killer_toast_best),
	// Toast.LENGTH_SHORT);
	// toast.show();
	// }else{
	// try {
	// String toastStr =
	// String.format(getResources().getString(R.string.task_killer_toast),
	// saveMemorySize);
	// Toast toast = Toast.makeText(getContext(), toastStr, Toast.LENGTH_SHORT);
	// toast.show();
	// } catch (IllegalFormatException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// addCountDownTimer = null;
	// }
	// };
	// addCountDownTimer.start();
	// }
	// };
	// reduceCountDownTimer.start();
	// }
	// } catch (OutOfMemoryError e) {
	// e.printStackTrace();
	// OutOfMemoryHandler.handle();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * 刷新内存状态的指示器和文本
	 */
	private synchronized void updateText() {
		/**
		 * 标志动画正在做
		 */
		mAnimationIsFinish = false;

		long availMemory = AppCore.getInstance().getTaskMgrControler().retriveAvailableMemory() / 1024;
		// 更新文本
		if (totalMemory == 0) {
			totalMemory = AppCore.getInstance().getTaskMgrControler().retriveTotalMemory() / 1024;

		}
		final long usedMemory = totalMemory - availMemory;
		totalTV.setText(totalMemory + "M");
		availTV.setText(usedMemory + "M");
		int barLenght = barLayout.getWidth();
		if (barLenght == 0) {
			mHandler.sendEmptyMessageDelayed(MSG_UPDATE, 1000);
			return;
		}
		int tempL = barLenght;
		try {
			final float temp = usedMemory / (totalMemory + 0.0f);
			// if (temp > 0.8f) {
			// totalTV.setVisibility(View.GONE);
			// } else {
			// totalTV.setVisibility(View.VISIBLE);
			// }
			formLengt = availBgIV.getWidth();
			toLengt = (int) (tempL * temp);

			// 如果做动画
			if (mIsMove) {
				if (formLengt == 0) {
					if (temp <= 0.7f) {
						availBgIV.setBackgroundDrawable(smallDrawable);

					} else if (temp > 0.7f && temp <= 0.9f) {
						availBgIV.setBackgroundDrawable(middleDrawable);
					} else {
						availBgIV.setBackgroundDrawable(heightDrawable);
					}
					availBgIV.setWidth(toLengt);
				} else {
					availBgIV.clearAnimation();
					availBgIV.setWidth(formLengt);
					myAnimation_Scale_Reduce = new ScaleReduceAnimation(formLengt,
							DrawUtils.dip2px(20), availBgIV, availTV, usedMemory);
					myAnimation_Scale_Reduce.setDuration(500);
					myAnimation_Scale_Reduce.setInterpolator(new LinearInterpolator());
					myAnimation_Scale_Reduce.setFillAfter(true);
					myAnimation_Scale_Reduce.setAnimationListener(TaskRunningStyle41Widget.this);

					availBgIV.setAnimation(myAnimation_Scale_Reduce);

					// new Thread(new Runnable() {
					//
					// @Override
					// public void run() {
					// try {
					// Thread.sleep(1000);
					// } catch (InterruptedException e) {
					// e.printStackTrace();
					// }
					// }
					// }).start();

				}
			} else {
				if (temp <= 0.7f) {
					availBgIV.setBackgroundDrawable(smallDrawable);

				} else if (temp > 0.7f && temp <= 0.9f) {
					availBgIV.setBackgroundDrawable(middleDrawable);
				} else {
					availBgIV.setBackgroundDrawable(heightDrawable);
				}
				availBgIV.setWidth(toLengt);

				// 标志动画已经完成
				mAnimationIsFinish = true;
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	/**
	 * 更新正在运行的程序列表
	 */
	private synchronized void updateAppsList() {
		// 先清空以前的
		if (mProgressesList != null) {
			mProgressesList.clear();
		}
		mProgressesList = getFilteredList(AppCore.getInstance().getTaskMgrControler()
				.getProgresses());
		// 组织适合UI的队列
		if (list == null) {
			list = new ArrayList<ArrayList<FunTaskItemInfo>>();
		} else {
			for (ArrayList<FunTaskItemInfo> itemInfo : list) {
				if (itemInfo != null) {
					itemInfo.clear();
				}
			}
		}
		list.clear();
		int size = mProgressesList.size();
		ArrayList<FunTaskItemInfo> tempList = null;
		for (int i = 0; i < size; i++) {
			if (i % SIZE == 0) {
				tempList = new ArrayList<FunTaskItemInfo>();
				list.add(tempList);
			}
			tempList.add(mProgressesList.get(i));
		}
	}

	/**
	 * 杀死所有程序后，单次立刻刷新后台正在运行的程序列表
	 */
	private void initTimerBean() {
		if (mTimer != null) {
			mTimer.cancel();
		}
		if (mCycleTimer != null) {
			mCycleTimer.cancel();
		}
		// 计时器启动
		mTimer = new CountDownTimer(TIMER_DURATION + TIME_TO_START, TIMER_DURATION) {

			@Override
			public void onTick(long millisUntilFinished) {
				update();
			}

			@Override
			public void onFinish() {
				// update();
				// initCycleTimerBean();
			}
		};
		mTimer.start();
	}

	public void onRemove(int widgetId) {
		onDestroy();
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

	public void onDestroy() {
		AppCore.getInstance().getTaskMgrControler().unRegisterObserver(this);
		mContext.unregisterReceiver(showLockListReceiver);
		showLockListReceiver = null;
		// mKillButton.setImageDrawable(null);
		// mKillButton = null;
		// mRefreshButton.setImageDrawable(null);
		// mRefreshButton = null;
		if (mProgressesList != null) {
			mProgressesList.clear();
		}
		if (mTimer != null) {
			mTimer.cancel();
			// mTimer = null;
		}
		if (mCycleTimer != null) {
			mCycleTimer.cancel();
			// mCycleTimer = null;
		}
		// removeAllViews();
		if (list != null) {
			for (ArrayList<FunTaskItemInfo> itemInfo : list) {
				if (itemInfo != null) {
					itemInfo.clear();
				}
			}
			list.clear();
		}

		if (myAnimation_Scale_Reduce != null) {
			myAnimation_Scale_Reduce = null;
		}
		if (myAnimation_Scale_Add != null) {
			myAnimation_Scale_Add = null;
		}

		System.gc();

		try {
			this.finalize();
		} catch (Throwable e) {
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case TaskMgrControler.TERMINATE_ALL :
				// 关闭所有进程
				if (mCycleTimer != null) {
					mCycleTimer.cancel();
				}
				// 计时器启动
				initTimerBean();
				break;
			case TaskMgrControler.TERMINATE_SINGLE :
				// 关闭单个进程
				// update();
				// 重新初始化循环定时器
				// initCycleTimerBean();
				break;
			case TaskMgrControler.ADDWHITEITEM :
			case TaskMgrControler.ADDWHITEITEMS :
			case TaskMgrControler.DELETEWHITEITEM :
				// 增加或者減少锁定程序时，重新获取列表
				updateAppsList();
				break;
			default :
				break;
		}

	}

	/**
	 * 从最低点拉伸动画
	 */
	private void addProgressAnimation() {
		try {
			// 再次更新一次数据，重新计算偏移量
			long availMemory = AppCore.getInstance().getTaskMgrControler().retriveAvailableMemory() / 1024;
			// 更新文本
			if (totalMemory == 0) {
				totalMemory = AppCore.getInstance().getTaskMgrControler().retriveTotalMemory() / 1024;

			}
			final long usedMemory2 = totalMemory - availMemory;
			// totalTV.setText(totalMemory + "M");
			// availTV.setText(usedMemory + "M");
			int barLenght = barLayout.getWidth();
			int tempL = barLenght;
			final float temp = usedMemory2 / (totalMemory + 0.0f);
			// formLengt = availBgIV.getWidth();
			toLengt = (int) (tempL * temp);

			if (temp <= 0.7f) {
				availBgIV.setBackgroundDrawable(smallDrawable);

			} else if (temp > 0.7f && temp <= 0.9f) {
				availBgIV.setBackgroundDrawable(middleDrawable);
			} else {
				availBgIV.setBackgroundDrawable(heightDrawable);
			}

			// 启动增加的动画
			myAnimation_Scale_Add = new ScaleAddAnimation(DrawUtils.dip2px(20), toLengt, availBgIV,
					availTV, usedMemory2);
			myAnimation_Scale_Add.setDuration(500);
			myAnimation_Scale_Add.setInterpolator(new LinearInterpolator());
			myAnimation_Scale_Add.setFillAfter(true);
			myAnimation_Scale_Add.setAnimationListener(TaskRunningStyle41Widget.this);

			availBgIV.setAnimation(myAnimation_Scale_Add);

			if (mIsToast && mAgoAvilableSize >= usedMemory2) {
				long saveMemorySize = mAgoAvilableSize - usedMemory2;
				if (saveMemorySize <= 5) {
					Toast toast = Toast.makeText(getContext(),
							getResources().getString(R.string.task_killer_toast_best),
							Toast.LENGTH_SHORT);
					toast.show();
				} else {
					try {
						String toastStr = String.format(
								getResources().getString(R.string.task_killer_toast),
								saveMemorySize);
						Toast toast = Toast.makeText(getContext(), toastStr, Toast.LENGTH_SHORT);
						toast.show();
					} catch (IllegalFormatException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onLongClick(View v) {
		performLongClick();
		return true;
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// 缩短的动画完成
		if (animation == myAnimation_Scale_Reduce) {
			// myAnimation_Scale_Reduce.reset();
			// myAnimation_Scale_Reduce = null;
			// availBgIV.setAnimation(null);

			mHandler.sendEmptyMessage(ADD_ANIMATION);

			// 拉伸动画完成
		} else if (animation == myAnimation_Scale_Add) {
			// myAnimation_Scale_Add.reset();
			// if(myAnimation_Scale_Reduce != null){
			// try {
			// myAnimation_Scale_Reduce.cancel();
			// } catch (NoSuchMethodError e) {
			// e.printStackTrace();
			// availBgIV.clearAnimation();
			// }
			// }
			// if(myAnimation_Scale_Add != null){
			// try {
			// myAnimation_Scale_Add.cancel();
			// } catch (NoSuchMethodError e) {
			// e.printStackTrace();
			// availBgIV.clearAnimation();
			// }
			// }
			availBgIV.clearAnimation();
			availBgIV.setAnimation(null);

			// 标志动画已经完成
			mAnimationIsFinish = true;

			mIsMove = false;
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
	}

}
