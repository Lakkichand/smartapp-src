package com.jiubang.ggheart.apps.gowidget.taskmanager;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.gau.go.launcherex.R;
import com.go.launcher.taskmanager.TaskMgrControler;
import com.go.util.graphics.DrawUtils;
import com.go.util.log.LogUnit;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.gowidget.GoWidgetAdapter;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class TaskRunning42Widget extends LinearLayout
		implements
			BroadCasterObserver,
			View.OnLongClickListener,
			OnGestureListener,
			View.OnTouchListener,
			View.OnClickListener {
	private final int TIMER_DURATION = 1000;

	private final int TIME_TO_START = 600;

	private final int TIMER_REFRESH_DURATION = TIMER_DURATION * 60;

	private final int SIZE = 12;

	private final String GO_TASKMANAGER_APP = "com.gau.go.launcherex.gowidget.taskmanager";
	/** 内容父容器 */
	private LinearLayout mBgView = null;
	/** 内存进度 */
	private ImageView mImage = null;
	/** 关闭按钮 */
	private ImageButton mKillButton = null;
	/** 刷新按钮 */
	private ImageButton mRefreshButton = null;
	/** 显示内存状况的文本 */
	private DeskTextView mText = null;

	private Context mContext = null;
	/** 总的内存 */
	private long totalMemory;
	/** 内存状态父容器 */
	private LinearLayout menoryLayout = null;

	/** 提示语 */
	private TextView textView = null;
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
	 * 显示图标区域
	 */
	private FrameLayout mIconsLayout;
	/**
	 * 循环定时器
	 */
	private CountDownTimer mCycleTimer;
	/**
	 * 用于计算定时刷新
	 */
	private long mRealtime;

	private Handler mHandler;

	/**
	 * 显示图标的视图
	 */
	private ViewFlipper mViewFlipper;
	/** 用于UI显示的列表 */
	private ArrayList<ArrayList<FunTaskItemInfo>> list;

	private GestureDetector mGestureDetector;
	/** ViewFlipper的总共屏幕数 */
	private int mTotalScreens;
	/** ViewFlipper的当前屏幕 */
	private int mCurScreen;
	/** 帧动画 */
	private AnimationDrawable mAnimationDrawable;

	private CountDownTimer mSingleKillTimer;

	private int mColor;

	private boolean mDestroy = false;
	/** 接收屏幕状态广播 */
	// private ScreenStateReceiver screenStateReceiver = null;
	/** 接收锁定状态广播 */
	private ShowLockListReceiver showLockListReceiver = null;

	private LayoutInflater inflater = null;

	/** 进度曹长度 */
	private Drawable progressDrawable1 = null, progressDrawable2 = null, progressDrawable3 = null,
			progressDrawable4 = null, progressDrawable5 = null;

	// 是否提示手机优化内存数量
	private boolean mIsToast = false;
	// 记录上一次可用内存用于计算
	private int mAgoAvilableSize;

	public TaskRunning42Widget(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public TaskRunning42Widget(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	private void init() {
		this.inflater = LayoutInflater.from(mContext);
		mResources = getGoWidgetResources(GoWidgetAdapter.TASK_MANAGER);
		if (mResources == null) {
			throw new RuntimeException("no resource found for task widget 4x2 !!");
		}

		AppCore.getInstance().getTaskMgrControler().registerObserver(this);
		mGestureDetector = new GestureDetector(mContext, this);
		initHandle();
		registerBroadcast();
	}

	/**
	 * 注册广播
	 */
	private void registerBroadcast() {
		/*
		 * screenStateReceiver = new ScreenStateReceiver();
		 * 
		 * IntentFilter intentFilter = new IntentFilter();
		 * //intentFilter.addAction(Constant.SCREEN_OFF);
		 * intentFilter.addAction(Constant.SCREEN_ON);
		 * mContext.registerReceiver(screenStateReceiver, intentFilter);
		 * 
		 * Intent intent = new Intent(); intent.setAction("taskmanager");
		 * mContext.sendBroadcast(intent);
		 */

		showLockListReceiver = new ShowLockListReceiver();

		IntentFilter intentFilter2 = new IntentFilter();
		intentFilter2.addAction(ICustomAction.ACTION_RESPONDISSHOWLOCK);
		intentFilter2.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter2.addAction(ICustomAction.ACTION_RESPOND_UPDATE_FROMAPP);
		mContext.registerReceiver(showLockListReceiver, intentFilter2);

		Intent intent2 = new Intent();
		intent2.setAction(ICustomAction.ACTION_REQUESTISSHOWLOCK);
		mContext.sendBroadcast(intent2);

	}

	/**
	 * 广播接收
	 * 
	 * @author linshaowu
	 * 
	 */
	// private class ScreenStateReceiver extends BroadcastReceiver
	// {
	// @Override
	// public void onReceive(Context context, Intent intent)
	// {
	// String action = intent.getAction();
	// // 屏幕关闭/开启
	// if (Constant.SCREEN_ON.equals(action))
	// {
	// if (isCloseApp())
	// {
	// updateAppsList();
	// try
	// {
	// Thread.sleep(2000);
	// } catch (InterruptedException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// AppCore.getInstance().getTaskMgrControler().terminateAll();// 杀掉非锁定程序
	// update();
	// }
	//
	// }
	// }
	// }

	private class ShowLockListReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ICustomAction.ACTION_RESPONDISSHOWLOCK.equals(action)) {
				updateAppsList();
				updateViewFlipper(false);
				// 开屏自动更新
			} else if (Intent.ACTION_SCREEN_ON.equals(action)
					|| ICustomAction.ACTION_RESPOND_UPDATE_FROMAPP.equals(action)) {
				mIsToast = false;
				update();
			}
		}
	}

	/**
	 * 是否已关掉自动关闭功能 :true是 false:否
	 * 
	 * @return
	 */
	private boolean isCloseApp() {
		try {
			Uri CONTENT_URI = Uri.parse(Constant.QUERYLOCKSTATE);
			String columns[] = new String[] { "isAutoClose", "isLcoklist", };
			ContentResolver cr = mContext.getContentResolver();
			Cursor cur = cr.query(CONTENT_URI, columns, null, null, null);

			if (cur.moveToFirst()) {
				Log.i("isAutoClose", cur.getString(cur.getColumnIndex("isAutoClose")));
				return "-1".equals(cur.getString(cur.getColumnIndex("isAutoClose"))) ? false : true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
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

	/**
	 * 主题切换入口
	 * 
	 * @param bundle
	 * @throws NameNotFoundException
	 */
	public boolean onApplyTheme(Bundle bundle) throws NameNotFoundException {
		String packageName = bundle.getString(Constant.GOWIDGET_THEME);
		int type = bundle.getInt(Constant.GOWIDGET_TYPE);
		int themeId = bundle.getInt(Constant.GOWIDGET_THEMEID);
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
		// 开始解析
		TaskManagerThemeAnalysis taskManagerThemeAnalysis = new TaskManagerThemeAnalysis(
				Constant.STYLE42, themeId);
		Analysis.parser(taskManagerThemeAnalysis, inputStream);

		TaskManagerThemeBean taskManagerThemeBean = taskManagerThemeAnalysis.taskManagerThemeBean;
		Resources resTheme = getContext().getPackageManager().getResourcesForApplication(
				packageName);
		try {
			// 全局布局背景
			mBgView.setBackgroundDrawable(getThemeDrawable(resTheme, taskManagerThemeBean.widgetBg,
					packageName));
			// 程序图标背景
			mIconsLayout.setBackgroundDrawable(getThemeDrawable(resTheme,
					taskManagerThemeBean.icoLayoutBg, packageName));
			// kill按钮
			mKillButton.setBackgroundDrawable(getThemeDrawable(resTheme,
					taskManagerThemeBean.killBtn, packageName));
			// 刷新按钮
			mRefreshButton.setBackgroundDrawable(getThemeDrawable(resTheme,
					taskManagerThemeBean.refreshBtn, packageName));
			// 内存布局背景
			menoryLayout.setBackgroundDrawable(getThemeDrawable(resTheme,
					taskManagerThemeBean.bottomLayoutBg, packageName));

			if (taskManagerThemeBean.fontColor != null) {
				// 内存文字key
				textView.setTextColor(Color.parseColor(taskManagerThemeBean.fontColor));
				// 内存文字values
				mText.setTextColor(Color.parseColor(taskManagerThemeBean.fontColor));
			} else {
				// 内存文字key
				textView.setTextColor(Color.parseColor("#000000"));
				// 内存文字values
				mText.setTextColor(Color.parseColor("#000000"));
			}
			// 进度长度
			progressDrawable1 = getThemeDrawable(resTheme, taskManagerThemeBean.porcess1,
					packageName);
			// 进度长度
			progressDrawable2 = getThemeDrawable(resTheme, taskManagerThemeBean.porcess2,
					packageName);
			// 进度长度3
			progressDrawable3 = getThemeDrawable(resTheme, taskManagerThemeBean.porcess3,
					packageName);

			progressDrawable4 = getThemeDrawable(resTheme, taskManagerThemeBean.porcess4,
					packageName);

			progressDrawable5 = getThemeDrawable(resTheme, taskManagerThemeBean.porcess5,
					packageName);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		}
		return true;
	}

	/**
	 * 获取图片资源
	 * 
	 * @param resources
	 * @param drawableName
	 * @param packageName
	 * @return
	 */
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

	/**
	 * 初始化widget并生成资源图片
	 */
	private void initWidget() {
		// 全局布局容器
		mBgView = (LinearLayout) findViewById(R.id.layout_gobal);
		mColor = getResources().getColor(R.color.delete_color_filter);

		GonClickListener gonClickListener = new GonClickListener();
		// 程序图标父容器
		mIconsLayout = (FrameLayout) findViewById(R.id.icons_layout);
		// 内存进度图标
		mImage = (ImageView) findViewById(R.id.progress_bar);
		mImage.setOnClickListener(gonClickListener);
		mImage.setOnLongClickListener(this);
		// 杀死进程按钮
		mKillButton = (ImageButton) findViewById(R.id.kill);
		mKillButton.setOnClickListener(gonClickListener);
		mKillButton.setOnLongClickListener(this);
		// 刷新进程按钮
		mRefreshButton = (ImageButton) findViewById(R.id.refresh);
		mRefreshButton.setOnClickListener(gonClickListener);
		mRefreshButton.setOnLongClickListener(this);
		// 加载显示图标区域
		mViewFlipper = (ViewFlipper) findViewById(R.id.icon_flipper);
		mViewFlipper.setOnTouchListener(this);
		// 内存大小文本
		mText = (DeskTextView) findViewById(R.id.text);
		mText.setTextColor(Color.BLACK);
		textView = (TextView) findViewById(R.id.arm_txt);
		textView.setTextColor(Color.BLACK);
		menoryLayout = (LinearLayout) findViewById(R.id.arm_layout);
		// 父容器ID
		int parentResid = doResid("task_manager42_bg");
		// 程序图标容器ID
		int iconResid = doResid("task_manager42_top_bg");
		// 杀掉进程ID
		int kilResid = doResid("kill_selector");
		// 刷新进程ID
		int refreshResid = doResid("re_selector");
		// 内存状态父容器ID
		int menoryResid = doResid("task_manager42_bottom_bg");

		int menoryTxtResid = mResources.getIdentifier("ram_status_text", "string",
				GoWidgetAdapter.TASK_MANAGER);
		try {
			// TODO 设置资源图片
			if (parentResid != 0) {
				Drawable drawable = mResources.getDrawable(parentResid);
				mBgView.setBackgroundDrawable(drawable);
			}
			if (iconResid != 0) {
				mIconsLayout.setBackgroundDrawable(mResources.getDrawable(iconResid));

			}
			if (kilResid != 0) {
				mKillButton.setBackgroundDrawable(mResources.getDrawable(kilResid));
			}
			if (refreshResid != 0) {
				mRefreshButton.setBackgroundDrawable(mResources.getDrawable(refreshResid));
			}
			if (menoryResid != 0) {
				menoryLayout.setBackgroundDrawable(mResources.getDrawable(menoryResid));
			}
			if (menoryTxtResid != 0) {
				textView.setText(mResources.getString(menoryTxtResid));
			}

			int identifier = mResources.getIdentifier("porcess1", "drawable",
					GoWidgetAdapter.TASK_MANAGER);
			if (identifier != 0) {
				try {
					progressDrawable1 = mResources.getDrawable(identifier);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}

			identifier = mResources.getIdentifier("porcess2", "drawable",
					GoWidgetAdapter.TASK_MANAGER);
			if (identifier != 0) {
				try {
					progressDrawable2 = mResources.getDrawable(identifier);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}

			identifier = mResources.getIdentifier("porcess3", "drawable",
					GoWidgetAdapter.TASK_MANAGER);
			if (identifier != 0) {
				try {
					progressDrawable3 = mResources.getDrawable(identifier);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}

			identifier = mResources.getIdentifier("porcess4", "drawable",
					GoWidgetAdapter.TASK_MANAGER);
			if (identifier != 0) {
				try {
					progressDrawable4 = mResources.getDrawable(identifier);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}

			identifier = mResources.getIdentifier("porcess5", "drawable",
					GoWidgetAdapter.TASK_MANAGER);
			if (identifier != 0) {
				try {
					progressDrawable5 = mResources.getDrawable(identifier);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
				}
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		}
	}

	/**
	 * 生成图片ID
	 * 
	 * @param imageName
	 * @return
	 */
	private int doResid(String imageName) {
		return mResources.getIdentifier(imageName, "drawable", GoWidgetAdapter.TASK_MANAGER);
	}

	@SuppressWarnings("unused")
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		updateAppsList();
		if (mResources != null) {
			// mBgView =(LinearLayout)findViewById(R.id.layout_gobal);
			// mColor = getResources().getColor(R.color.delete_color_filter);
			/*
			 * //竖屏 if(GoLauncher.getOrientation() ==
			 * Configuration.ORIENTATION_PORTRAIT) { mBgView.removeAllViews();
			 * View view =
			 * inflater.inflate(R.layout.task_running_4_2_content_vertical,
			 * mBgView); initWidget(view, VERTICAL); } else {//横屏
			 * mBgView.removeAllViews(); View view =
			 * inflater.inflate(R.layout.task_running_4_2_content_horizontal,
			 * mBgView); initWidget(view, HORIZONTAL); }
			 */

			// mBgView.removeAllViews();
			// View view =
			// inflater.inflate(R.layout.task_running_4_2_content_vertical,
			// mBgView);
			initWidget();

			updateText();
			updateViewFlipper(true);
			// initCycleTimerBean();

		} else {
			removeAllViews();
			AppCore.getInstance().getTaskMgrControler().unRegisterObserver(this);
		}
	}

	private class GonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// String tempText = mText.getText().toString();
			// if(tempText != null && !"".equals(tempText) &&
			// tempText.contains("/")){
			// try {
			// mAgoAvilableSize = Integer.parseInt(tempText.substring(0,
			// tempText.indexOf("/")));
			// } catch (NumberFormatException e) {
			// e.printStackTrace();
			// }
			// }
			int id = v.getId();
			switch (id) {
				case R.id.kill :
					mIsToast = false;

					List<View> views = mIconsLayout.getTouchables();
					int size = 0;
					if (views != null) {
						size = views.size();
						for (int i = 0; i < size; i++) {
							// if(!views.get(i).getClass().toString().contains("TaskRunningImageView")){
							// continue;
							// }
							View view = views.get(i);
							if (view instanceof TaskRunningImageView) {

								TaskRunningImageView childView = (TaskRunningImageView) view;

								if (childView.mInfo != null && !childView.mInfo.isInWhiteList()) {

									try {
										childView.setImageDrawable(null);
										childView.setBackgroundResource(R.anim.kill_app);
										mAnimationDrawable = (AnimationDrawable) childView
												.getBackground();
										mAnimationDrawable.start();
									} catch (OutOfMemoryError e) {
										e.printStackTrace();
										OutOfMemoryHandler.handle();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						} // for循环 结束
					}
					// 通知任务管理器APP更新
					Intent intent1 = new Intent();
					intent1.setAction(ICustomAction.ACTION_REQUEST_UPDATE_TOAPP);
					mContext.sendBroadcast(intent1);

					AppCore.getInstance().getTaskMgrControler().terminateAll(mProgressesList);
					break;
				case R.id.refresh :
					mIsToast = false;

					// 通知任务管理器APP更新
					Intent intent2 = new Intent();
					intent2.setAction(ICustomAction.ACTION_REQUEST_UPDATE_TOAPP);
					mContext.sendBroadcast(intent2);

					update();
					// initCycleTimerBean();
					break;
				case R.id.progress_bar :
					Intent intent = new Intent();
					intent = mContext.getPackageManager().getLaunchIntentForPackage(
							GO_TASKMANAGER_APP);
					mContext.startActivity(intent);
					break;
				default :
					break;
			}
		}

	}

	/**
	 * 更新显示正在运行程序图标的视图
	 * 
	 * @param isInit
	 *            是否第一次初始化视图
	 */
	private synchronized void updateViewFlipper(boolean isInit) {
		int orientation = GoLauncher.getOrientation();
		// DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		int size = list.size();
		for (int i = 0; i < size; i++) {
			ArrayList<FunTaskItemInfo> infoList = list.get(i);
			LinearLayout linearLayout = (LinearLayout) mViewFlipper.getChildAt(i);
			if (linearLayout == null) {
				linearLayout = new LinearLayout(mContext, null);
				linearLayout.setPadding(DrawUtils.dip2px(2), DrawUtils.dip2px(2),
						DrawUtils.dip2px(2), DrawUtils.dip2px(2));
				mViewFlipper.addView(linearLayout);
			}

			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				linearLayout.setOrientation(LinearLayout.VERTICAL);
				View view = linearLayout.getChildAt(0);
				if (view != null && !(view instanceof LinearLayout)) {
					removeInfoObserver(linearLayout);
					linearLayout.removeAllViews();
					view = null;
				}
				LinearLayout topLayout = (LinearLayout) view;
				if (topLayout == null) {
					topLayout = new LinearLayout(mContext, null);
					topLayout.setOrientation(LinearLayout.HORIZONTAL);
					topLayout.setGravity(Gravity.CENTER);
					topLayout.setPadding(0, DrawUtils.dip2px(4), 0, DrawUtils.dip2px(5));
					linearLayout.addView(topLayout, new LinearLayout.LayoutParams(
							android.view.ViewGroup.LayoutParams.FILL_PARENT, 0, 1));
				}
				removeInfoObserver(topLayout);
				topLayout.removeAllViews();
				LinearLayout bottomLayout = (LinearLayout) linearLayout.getChildAt(1);
				if (bottomLayout == null) {
					bottomLayout = new LinearLayout(mContext, null);
					bottomLayout.setOrientation(LinearLayout.HORIZONTAL);
					bottomLayout.setGravity(Gravity.CENTER);
					bottomLayout.setPadding(0, 0, 0, DrawUtils.dip2px(2));
					linearLayout.addView(bottomLayout, new LinearLayout.LayoutParams(
							android.view.ViewGroup.LayoutParams.FILL_PARENT, 0, 1));
				}
				removeInfoObserver(bottomLayout);
				bottomLayout.removeAllViews();
				int infoSize = infoList.size();
				for (int j = 0; j < SIZE; j++) {
					TaskRunningImageView image = new TaskRunningImageView(mContext, null);
					if (j < infoSize) {
						image.setOnClickListener(this);
						image.setOnTouchListener(this);
						FunTaskItemInfo info = infoList.get(j);
						image.setAppInfo(info);
						image.setScaleType(ScaleType.FIT_CENTER);
						Bitmap bitmap = info.getAppItemInfo().mIcon.getBitmap();
						image.setImageBitmap(bitmap);
					}
					if (j < 6) {
						topLayout.addView(image,
								new LinearLayout.LayoutParams(0, DrawUtils.dip2px(35), 1));
					} else {
						bottomLayout.addView(image,
								new LinearLayout.LayoutParams(0, DrawUtils.dip2px(35), 1));
					}
				}
			} else {
				linearLayout.setOrientation(LinearLayout.HORIZONTAL);
				removeInfoObserver(linearLayout);
				linearLayout.removeAllViews();
				int infoSize = infoList.size();
				for (int j = 0; j < SIZE; j++) {
					TaskRunningImageView image = new TaskRunningImageView(mContext, null);
					if (j < infoSize) {
						image.setOnTouchListener(this);
						image.setOnClickListener(this);
						FunTaskItemInfo info = infoList.get(j);
						image.setAppInfo(info);
						image.setScaleType(ScaleType.FIT_CENTER);
						Bitmap bitmap = info.getAppItemInfo().mIcon.getBitmap();
						image.setImageBitmap(bitmap);
					}
					linearLayout.addView(image, new LinearLayout.LayoutParams(0,
							android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1));
				}
			}
		}

		mTotalScreens = mViewFlipper.getChildCount();
		if (size < mTotalScreens) {
			mViewFlipper.setInAnimation(mContext, R.anim.push_top_in);
			mViewFlipper.setOutAnimation(mContext, R.anim.push_bottom_out);
			mViewFlipper.removeViews(size, mTotalScreens - size);
			mTotalScreens = mViewFlipper.getChildCount();
		}
		if (!isInit) {
			if (mCurScreen > mTotalScreens - 1) {
				mCurScreen = mTotalScreens - 1;
			}
		} else {
			mCurScreen = 0;
		}
	}

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
		updateViewFlipper(false);
	}

	/**
	 * 刷新内存状态的指示器和文本
	 */
	private synchronized void updateText() {
		long availMemory = AppCore.getInstance().getTaskMgrControler().retriveAvailableMemory() / 1024;
		// 更新文本
		if (totalMemory == 0) {
			totalMemory = AppCore.getInstance().getTaskMgrControler().retriveTotalMemory() / 1024;
			if (totalMemory != 0) {
				if (mText != null) {
					mText.setText(availMemory + "/" + totalMemory + "M");
				}
			} else {
				if (mText != null) {
					mText.setText(availMemory + "M");
				}
			}
		} else {
			if (mText != null) {
				mText.setText(availMemory + "/" + totalMemory + "M");
			}
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
			mIsToast = false;
		}

		// 更新图片
		if (mImage == null) {
			return;
		}
		if (totalMemory == 0) {
			mImage.setImageDrawable(progressDrawable1);
		} else {
			try {

				float temp = availMemory / (totalMemory + 0.0f);
				if (temp <= 0.2f) {
					mImage.setImageDrawable(progressDrawable5);
				} else if (temp > 0.2f && temp <= 0.4f) {
					mImage.setImageDrawable(progressDrawable4);
				} else if (temp > 0.4f && temp <= 0.6f) {
					mImage.setImageDrawable(progressDrawable3);
				} else if (temp > 0.6f && temp <= 0.8f) {
					mImage.setImageDrawable(progressDrawable2);
				} else {
					mImage.setImageDrawable(progressDrawable1);
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
			}
		}
	}

	@Override
	public void onBCChange(int msgId, int param, Object object,
			@SuppressWarnings("rawtypes") List objects) {
		switch (msgId) {
			case TaskMgrControler.TERMINATE_ALL : {
				// 关闭所有进程
				if (mCycleTimer != null) {
					mCycleTimer.cancel();
				}

				// 计时器启动
				initTimerBean();
			}
				break;
			case TaskMgrControler.TERMINATE_SINGLE : {
				// 关闭单个进程
				// update();
				// 重新初始化循环定时器
				// initCycleTimerBean();
			}
				break;
			case TaskMgrControler.ADDWHITEITEM :
			case TaskMgrControler.ADDWHITEITEMS :
			case TaskMgrControler.DELETEWHITEITEM : {
				// 增加或者減少锁定程序时，重新获取列表
				updateAppsList();
				updateViewFlipper(false);
			}
				break;
			default :
				break;
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
		mTimer = new CountDownTimer(TIME_TO_START, TIME_TO_START) {

			@Override
			public void onTick(long millisUntilFinished) {
				// update();
			}

			@Override
			public void onFinish() {
				update();
				// initCycleTimerBean();
			}
		};
		mTimer.start();
	}

	private void initHandle() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case AppItemInfo.INCONCHANGE :
						Object[] obj = (Object[]) msg.obj;
						TaskRunningImageView view = (TaskRunningImageView) obj[0];
						BitmapDrawable drawable = (BitmapDrawable) obj[1];
						try {
							view.setImageBitmap(drawable.getBitmap());
							view.setScaleType(ScaleType.FIT_CENTER);
						} catch (OutOfMemoryError e) {
							e.printStackTrace();
							view.setVisibility(View.GONE);
							OutOfMemoryHandler.handle();
						}
						break;
					default :
						break;
				}
			};
		};
	}

	@Override
	public boolean onLongClick(View v) {
		// 判断当前是否锁屏
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(getContext());
			return true;
		}

		if (v.equals(mKillButton) || v.equals(mRefreshButton)) {
			LogUnit.i("onLongClick");
			performLongClick();
		}
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		LogUnit.i("onTouch " + event.getAction());
		boolean onTouchEvent = mGestureDetector.onTouchEvent(event);
		if (v instanceof ViewFlipper) {
			onTouchEvent = true;
		} else if (v instanceof TaskRunningImageView) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				((TaskRunningImageView) v).setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
			} else if (event.getAction() == MotionEvent.ACTION_UP
					|| event.getAction() == MotionEvent.ACTION_CANCEL) {
				((TaskRunningImageView) v).setColorFilter(0x00000000, PorterDuff.Mode.SRC_ATOP);
			}
		}
		return onTouchEvent;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// 判断当前是否锁屏
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(getContext());
			return;
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		LogUnit.i("onFling : " + velocityY);
		boolean isHandle = false;
		if (mViewFlipper.getChildCount() > 0) {
			if (velocityY < -200 && mCurScreen < mTotalScreens - 1) {
				mCurScreen++;
				mViewFlipper.setInAnimation(mContext, R.anim.push_bottom_in);
				mViewFlipper.setOutAnimation(mContext, R.anim.push_top_out);
				mViewFlipper.showNext();
				isHandle = true;
			} else if (velocityY > 200 && mCurScreen > 0) {
				mCurScreen--;
				mViewFlipper.setOutAnimation(mContext, R.anim.push_bottom_out);
				mViewFlipper.setInAnimation(mContext, R.anim.push_top_in);
				mViewFlipper.showPrevious();
				isHandle = true;
			}
		}
		return isHandle;
	}

	@Override
	public void onClick(View v) {
		TaskRunningImageView view = (TaskRunningImageView) v;
		if (view.mInfo != null && !view.mInfo.isInWhiteList()) {

			view.setImageDrawable(null);
			view.setBackgroundResource(R.anim.kill_app);
			mAnimationDrawable = (AnimationDrawable) view.getBackground();
			mAnimationDrawable.start();
			AppCore.getInstance().getTaskMgrControler().terminateApp(view.mInfo.getPid());
			if (mSingleKillTimer != null) {
				mSingleKillTimer.cancel();
			}
			mSingleKillTimer = new CountDownTimer(TIME_TO_START, TIMER_DURATION) {

				@Override
				public void onTick(long millisUntilFinished) {

				}

				@Override
				public void onFinish() {

					mIsToast = false;
					// 关闭单个进程
					update();
					// 重新初始化循环定时器
					// initCycleTimerBean();
				}

			};
			mSingleKillTimer.start();
		}
	}

	public void onRemove(int widgetId) {
		onDestroy();
		mDestroy = true;
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

	public void onDestroy() {
		mContext.unregisterReceiver(showLockListReceiver);
		AppCore.getInstance().getTaskMgrControler().unRegisterObserver(this);
		mBgView.setBackgroundDrawable(null);
		mBgView = null;
		mImage.setImageDrawable(null);
		mImage = null;
		mKillButton.setImageDrawable(null);
		mKillButton = null;
		mRefreshButton.setImageDrawable(null);
		mRefreshButton = null;
		if (mProgressesList != null) {
			mProgressesList.clear();
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mCycleTimer != null) {
			mCycleTimer.cancel();
			mCycleTimer = null;
		}
		if (mSingleKillTimer != null) {
			mSingleKillTimer.cancel();
			mSingleKillTimer = null;
		}
		mIconsLayout.setBackgroundDrawable(null);
		mIconsLayout.removeAllViews();
		mViewFlipper.setBackgroundDrawable(null);
		removeInfoObserver(mViewFlipper);
		mViewFlipper.removeAllViews();
		if (mAnimationDrawable != null) {
			mAnimationDrawable.stop();
			mAnimationDrawable = null;
		}
		removeAllViews();
		if (list != null) {
			for (ArrayList<FunTaskItemInfo> itemInfo : list) {
				if (itemInfo != null) {
					itemInfo.clear();
				}
			}
			list.clear();
		}
	}

	/**
	 * 释放所有Info的注册Observer对象
	 * 
	 * @param view
	 *            ViewGroup
	 */
	private void removeInfoObserver(ViewGroup view) {
		int childCount = view.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = view.getChildAt(i);
			if (child != null && child instanceof TaskRunningImageView) {

				TaskRunningImageView image = (TaskRunningImageView) child;
				image.setOnTouchListener(null);
				image.setOnClickListener(null);
				if (image.getDrawable() != null) {
					image.getDrawable().setCallback(null);
				}
				image.setImageDrawable(null);
				if (image.mInfo != null) {
					image.mInfo.unRegisterObserver(image);
				}
				image.setAppInfo(null);
			} else {
				if (child != null && child instanceof ViewGroup) {
					removeInfoObserver((ViewGroup) child);
				}
			}
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == 1000) {
				try {
					TaskRunningImageView taskRunningImageView = (TaskRunningImageView) msg.obj;
					FunTaskItemInfo info = taskRunningImageView.getFunTaskItemInfo();
					Intent intent = info.getAppItemInfo().mIntent;
					// 还原该程序图标颜色
					taskRunningImageView.setColorFilter(0x00000000, PorterDuff.Mode.SRC_ATOP);
					if (intent != null) {
						mContext.startActivity(intent);
					}
					msg.obj = null;
					// msg.recycle();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (msg.what == 2000) {
				try {
					TaskRunningImageView view = (TaskRunningImageView) msg.obj;
					if (view.mInfo != null && !view.mInfo.isInWhiteList()) {

						view.setImageDrawable(null);
						view.setBackgroundResource(R.anim.kill_app);
						mAnimationDrawable = (AnimationDrawable) view.getBackground();
						mAnimationDrawable.start();
						AppCore.getInstance().getTaskMgrControler()
								.terminateApp(view.mInfo.getPid());
						if (mSingleKillTimer != null) {
							mSingleKillTimer.cancel();
						}
						mSingleKillTimer = new CountDownTimer(TIME_TO_START, TIMER_DURATION) {

							@Override
							public void onTick(long millisUntilFinished) {

							}

							@Override
							public void onFinish() {
								// 关闭单个进程
								mIsToast = false;
								update();
								// 重新初始化循环定时器
								// initCycleTimerBean();
							}

						};
						mSingleKillTimer.start();
					}
				} catch (OutOfMemoryError e) {
					OutOfMemoryHandler.handle();

				}
			}
		}
	};

	// //////////////////////////////////////////////////////////////////////////
	private class TaskRunningImageView extends LinearLayout
			implements
				OnTouchListener,
				OnClickListener,
				BroadCasterObserver {

		private FunTaskItemInfo mInfo;

		private Context context = null;

		private ImageView IconIV = null;

		private ImageView lockIV = null;

		public TaskRunningImageView(Context context) {
			super(context);
			this.context = context;
			init();
		}

		public TaskRunningImageView(Context context, AttributeSet attrs) {
			super(context, attrs);
			this.context = context;
			init();
		}

		private void init() {
			setClickable(true);
			RelativeLayout relativeLayout = new RelativeLayout(context);
			addView(relativeLayout);
			relativeLayout.setClickable(true);
			setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			IconIV = new ImageView(context);
			IconIV.setClickable(true);
			IconIV.setLongClickable(true);

			relativeLayout.addView(IconIV, new LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

			lockIV = new ImageView(context);
			lockIV.setImageResource(R.drawable.minus2);
			lockIV.setVisibility(View.GONE);

			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			relativeLayout.addView(lockIV, layoutParams);
			IconIV.setOnLongClickListener(new AppOnLongClick());
			IconIV.setOnClickListener(this);
			IconIV.setOnTouchListener(this);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void onBCChange(int msgId, int param, Object object, List objects) {
			switch (msgId) {
				case AppItemInfo.INCONCHANGE : {
					Message message = Message.obtain();
					message.what = AppItemInfo.INCONCHANGE;
					message.obj = new Object[] { this, object };
					mHandler.sendMessage(message);
				}
					break;
				default :
					break;
			}
		}

		public void setAppInfo(FunTaskItemInfo info) {
			if (mInfo != null && info != null && info.equals(mInfo)) {
				return;
			}
			if (mInfo != null) {
				mInfo.getAppItemInfo().unRegisterObserver(this);
			}
			mInfo = info;
			if (info != null && info.getAppItemInfo() != null) {
				info.getAppItemInfo().registerObserver(this);
			}
			isShowLock();
		}

		private void isShowLock() {
			if (mInfo != null) {
				if (mInfo.isInWhiteList()) {
					lockIV.setVisibility(View.VISIBLE);
				} else {
					lockIV.setVisibility(View.GONE);
				}
			}
		}

		/**
		 * 程序长按事件
		 * 
		 * @author linshaowu
		 * 
		 */
		private class AppOnLongClick implements OnLongClickListener {

			@Override
			public boolean onLongClick(View v) {
				// TODO 以消息传出去，不参与逻辑处理

				TaskRunningImageView view = getImageView();
				if (view.getFunTaskItemInfo() != null
						&& view.getFunTaskItemInfo().getAppItemInfo() != null
						&& view.getFunTaskItemInfo().getAppItemInfo().mIntent != null) {

					Message msg = Message.obtain();
					msg.obj = getImageView();
					msg.what = 1000;
					handler.sendMessage(msg);
				}

				return true;
			}
		}

		/**
		 * 返回 自身view
		 * 
		 * @return
		 */
		public TaskRunningImageView getImageView() {
			return this;
		}

		public FunTaskItemInfo getFunTaskItemInfo() {
			return this.mInfo;
		}

		public void setScaleType(ScaleType scaleType) {
			IconIV.setScaleType(scaleType);
		}

		public void setImageBitmap(Bitmap bitmap) {
			IconIV.setImageBitmap(bitmap);
		}

		public void setColorFilter(int color, PorterDuff.Mode mode) {
			IconIV.setColorFilter(color, mode);
		}

		public void setImageDrawable(Drawable drawable) {
			IconIV.setImageDrawable(drawable);
		}

		public Drawable getDrawable() {
			return IconIV.getDrawable();
		}

		@Override
		public Drawable getBackground() {
			return IconIV.getBackground();
		}

		@Override
		public void setBackgroundResource(int resid) {
			// TODO Auto-generated method stub
			IconIV.setBackgroundResource(resid);
		}

		@Override
		public void setOnLongClickListener(OnLongClickListener l) {
			// TODO Auto-generated method stub
			IconIV.setOnLongClickListener(l);
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			LogUnit.i("onTouch " + event.getAction());
			boolean onTouchEvent = mGestureDetector.onTouchEvent(event);
			if (v instanceof ViewFlipper) {
				onTouchEvent = true;
			} else if (v instanceof ImageView) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					((ImageView) v).setColorFilter(mColor, PorterDuff.Mode.SRC_ATOP);
				} else if (event.getAction() == MotionEvent.ACTION_UP
						|| event.getAction() == MotionEvent.ACTION_CANCEL) {
					((ImageView) v).setColorFilter(0x00000000, PorterDuff.Mode.SRC_ATOP);
				}
			}
			return onTouchEvent;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (getFunTaskItemInfo() != null && getFunTaskItemInfo().getAppItemInfo() != null
					&& getFunTaskItemInfo().getAppItemInfo().mIntent != null) {
				// if(mInfo != null && mInfo.isInWhiteList())
				// {
				// return;
				// }
				if (!getFunTaskItemInfo().isInWhiteList()) {
					lockIV.setVisibility(View.GONE);
					Message msg = Message.obtain();
					msg.obj = getImageView();
					msg.what = 2000;
					handler.sendMessage(msg);
				}
			}
		}
	}

	public void onStart(Bundle bundle) {
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
		// e.printStackTrace();
		// }
	}
}
