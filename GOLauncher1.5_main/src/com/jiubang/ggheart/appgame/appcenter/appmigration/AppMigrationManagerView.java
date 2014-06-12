/*
 * 文 件 名:  AppMigrationView.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-16
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.appcenter.appmigration;

import java.io.File;
import java.lang.reflect.Field;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.SortUtils;
import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.AppGameTabsBar;
import com.jiubang.ggheart.appgame.base.component.AppGameTabsBar.TabObserver;
import com.jiubang.ggheart.appgame.base.component.ContainerBuiler;
import com.jiubang.ggheart.appgame.base.component.IContainer;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-16]
 */
public class AppMigrationManagerView extends LinearLayout
		implements
			ScreenScrollerListener,
			IContainer {

	private final int mMSG_INIT_VIEW = 1001;

	private final int mMsg_INIT_LOADING = 1002;

	private final int mMsg_UPDATE_VIEW = 1003;
	
	private LinearLayout mProgressLinearLayout = null; // 进度条

	private LayoutInflater mInflater = null;
	
	/**
	 * 检索手机内的应用程序的线程
	 */
	private MyThread mThread = null;
	/**
	 * 包管理器
	 */
	private PackageManager mPm = null;
	/**
	 * 标题栏，在UI2.0中标题栏只用于二级和以下的tab，顶级tab的标题栏用GridTitleBar
	 */
	protected RelativeLayout mTitleBar = null;
	/**
	 * tab头
	 */
	protected AppGameTabsBar mAppGameTabsBar;
	/**
	 * 子tab栏视图容器
	 */
	protected ScrollerViewGroup mScrollerViewGroup = null;
	/**
	 * 当前选中的页面位置
	 */
	private int mCurrentIndex = 0;
	/**
	 * APP迁移存储位置之后的包名
	 */
	private ArrayList<String> mPkgs = new ArrayList<String>();
	/**
	 * 该层级标题
	 */
	protected TextView mTitleText = null;
	/**
	 * 返回按钮
	 */
	protected ImageView mBackButton = null;
	/**
	 * 
	 */
	protected OnClickListener mOnBackClickListener = null;
	/**
	 * Tab头的分页title
	 */
	private int[] mTitleResources = new int[] { R.string.appgame_migration_tab_phone,
			R.string.appgame_migration_tab_sdcard, R.string.appgame_migration_tab_no_move };
	/**
	 * sd卡的app
	 */
	private ArrayList<AppMigrationBean> mSdApp = new ArrayList<AppMigrationBean>();
	/**
	 * 手机内存的app
	 */
	private ArrayList<AppMigrationBean> mInternalStorageApp = new ArrayList<AppMigrationBean>();
	/**
	 * 系统的app
	 */
	private ArrayList<AppMigrationBean> mSystemApp = new ArrayList<AppMigrationBean>();
	/**
	 * 移动程序的存储位置的广播接收器
	 */
	private BroadcastReceiver mReceiver = null;
	/**
	 * 排序比较器，中英文按a~z进行混排
	 */
	private Comparator<AppMigrationBean> mComparator = new Comparator<AppMigrationBean>() {
		@Override
		public int compare(AppMigrationBean object1, AppMigrationBean object2) {
			int result = 0;
			String str1 = object1.getName();
			String str2 = object2.getName();
			str1 = SortUtils.changeChineseToSpell(getContext(), str1);
			str2 = SortUtils.changeChineseToSpell(getContext(), str2);
			Collator collator = null;
			if (Build.VERSION.SDK_INT < 16) {
				collator = Collator.getInstance(Locale.CHINESE);
			} else {
				collator = Collator.getInstance(Locale.ENGLISH);
			}

			if (collator == null) {
				collator = Collator.getInstance(Locale.getDefault());
			}
			result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
			return result;
		}
	};

	public AppMigrationManagerView(Context context) {
		super(context);
		mPm = context.getPackageManager();
		initView();
		int sdkVersion = Build.VERSION.SDK_INT;
		if (sdkVersion > 7) {
			registerStorage();
		}
	}

	public AppMigrationManagerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPm = context.getPackageManager();
		initView();
		int sdkVersion = Build.VERSION.SDK_INT;
		if (sdkVersion > 7) {
			registerStorage();
		}
	}

	private void initView() {
		mInflater = LayoutInflater.from(getContext());
		this.setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(getResources().getColor(R.color.center_background));
		initTitle();
		//初始化tab的三个分页
		initSwitchBar();
		mAppGameTabsBar.setVisibility(View.GONE);
		// scrollerview初始化
		initScrollGroup();
		mScrollerViewGroup.setVisibility(View.GONE);
		// 产生loading界面，检索手机内的程序
		mHandler.sendEmptyMessage(mMsg_INIT_LOADING);
		// 检索手机内的程序
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		mThread = new MyThread();
		mThread.start();
	}

	/**
	 * <br>功能简述:初始化loading界面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initProgressView() {
		// 加入一个进度条用于显示“正在检索手机程序”
		if (mProgressLinearLayout != null) {
			removeView(mProgressLinearLayout);
			mProgressLinearLayout = null;
		}
		mProgressLinearLayout = (LinearLayout) mInflater.inflate(R.layout.appgame_btmprogress,
				null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		params.gravity = Gravity.CENTER;
		mProgressLinearLayout.setLayoutParams(params);
		addView(mProgressLinearLayout);
	}

	/**
	 * <br>功能简述:初始化顶部title
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initTitle() {
		if (mInflater == null) {
			mInflater = LayoutInflater.from(getContext());
		}
		if (mTitleBar != null) {
			removeView(mTitleBar);
			mTitleBar = null;
		}
		mTitleBar = (RelativeLayout) mInflater.inflate(R.layout.appgame_management_toptitle_layout,
				null);
		mTitleText = (TextView) mTitleBar.findViewById(R.id.apps_management_title_text);
		mTitleText.setText(getContext().getString(R.string.appgame_migration_title));
		mBackButton = (ImageView) mTitleBar.findViewById(R.id.apps_management_title_back_iamge);
		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeSelf();
			}
		});
		mTitleBar.setBackgroundResource(R.drawable.appgame_titlebar_bg);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				DrawUtils.dip2px(50.67f));
		params.weight = 0;
		addView(mTitleBar, params);
	}

	/**
	 * <br>功能简述:初始化tab的可切换的view
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initSwitchBar() {
		//初始化tab的三个分页，分别是SD卡，手机内存 和 不可移动
		if (mAppGameTabsBar != null) {
			removeView(mAppGameTabsBar);
			mAppGameTabsBar = null;
		}
		mAppGameTabsBar = new AppGameTabsBar(getContext(), new TabObserver() {
			@Override
			public void handleChangeTab(int tabIndex) {
				// 点击tab 要切换scrollerViewGroup
				AsyncImageManager.getInstance().restore();
				mScrollerViewGroup.gotoViewByIndexImmediately(tabIndex);
			}
		});
		mAppGameTabsBar.setBackgroundResource(R.drawable.appgame_subtab_bg);
		ArrayList<String> titleList = new ArrayList<String>();
		for (int i = 0; i < mTitleResources.length; i++) {
			titleList.add(getContext().getString(mTitleResources[i]));
		}
		mAppGameTabsBar.initTabsBar(titleList);
		addView(mAppGameTabsBar);
	}

	/**
	 * <br>功能简述:初始化scroll page 
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initScrollGroup() {
		if (mScrollerViewGroup != null) {
			removeView(mScrollerViewGroup);
			mScrollerViewGroup = null;
		}
		// scrollerview初始化
		mScrollerViewGroup = new ScrollerViewGroup(getContext(), this);
		mScrollerViewGroup.setIsNeedGap(true);
		mScrollerViewGroup.setGapColor(getContext().getResources().getColor(
				R.color.app_game_page_gap_color));
		mScrollerViewGroup.setBackgroundColor(getResources().getColor(R.color.center_background));
		addView(mScrollerViewGroup, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
	}

	/**
	 * 
	 * <br>类描述:检索手机内的APP
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-11-22]
	 */
	private class MyThread extends Thread {
		@Override
		public void run() { 
			super.run();
			List<ApplicationInfo> allList = mPm.getInstalledApplications(0);
			List<ApplicationInfo> systemList = new ArrayList<ApplicationInfo>();
			Iterator<ApplicationInfo> iter = allList.iterator();
			while (iter.hasNext()) {
				ApplicationInfo info = iter.next();
				if ((info.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
					iter.remove();
					systemList.add(info);
				}
			}
			ArrayList<AppMigrationBean> sdApp = new ArrayList<AppMigrationBean>();
			ArrayList<AppMigrationBean> internalStorageApp = new ArrayList<AppMigrationBean>();
			ArrayList<AppMigrationBean> systemApp = new ArrayList<AppMigrationBean>();
			// 得到 “SD卡” 和 “手机内存”的程序列表
			for (ApplicationInfo info : allList) {
				AppMigrationBean bean = new AppMigrationBean();
				bean.setName(info.loadLabel(mPm).toString());
				bean.setPackageName(info.packageName);
				// 判断程序占用空间
				File file = new File(info.publicSourceDir);
				if (file.exists()) {
					bean.setSize(ConvertUtils.convertSizeToString(file.length(),
							ConvertUtils.FORM_DECIMAL_WITH_TWO));
				}
				// 根据不同的类型，分别加入到不同的列表中
				try {
					PackageInfo packageinfo = mPm.getPackageInfo(info.packageName, 0);
					Field field = packageinfo.getClass().getField("installLocation");
					int i = 0;
					i = (Integer) field.get(packageinfo);
					//可移动的应用
					if (i == 0) {
						if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
							bean.setType(AppMigrationBean.sTYPE_SD);
							sdApp.add(bean);
						} else {
							bean.setType(AppMigrationBean.sTYPE_INTERNAL_STORAGE);
							internalStorageApp.add(bean);
						}
					} else {
						bean.setType(AppMigrationBean.sTYPE_SYSTEM);
						systemApp.add(bean);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// 得到“不可移动”的系统程序列表
			for (ApplicationInfo info : systemList) {
				AppMigrationBean bean = new AppMigrationBean();
				bean.setName(info.loadLabel(mPm).toString());
				bean.setPackageName(info.packageName);
				File file = new File(info.publicSourceDir);
				if (file.exists()) {
					bean.setSize(ConvertUtils.convertSizeToString(file.length(),
							ConvertUtils.FORM_DECIMAL_WITH_TWO));
				}
				bean.setType(AppMigrationBean.sTYPE_SYSTEM);
				systemApp.add(bean);
			}
			allList = null;
			systemList = null;
			// 发送消息，通知界面继续初始化
			HashMap<String, ArrayList<AppMigrationBean>> map = new HashMap<String, ArrayList<AppMigrationBean>>();
			map.put("sd", sdApp);
			map.put("internal", internalStorageApp);
			map.put("system", systemApp);
			Message msg = new Message();
			msg.what = mMSG_INIT_VIEW;
			msg.obj = map;
			mHandler.sendMessage(msg);
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case mMsg_INIT_LOADING :
					if (mScrollerViewGroup != null) {
						mScrollerViewGroup.removeAllViews();
					}
					// 初始化loading界面
					initProgressView();
					break;
				case mMSG_INIT_VIEW :
					// 先清空之前的数据
					mSystemApp.clear();
					mSdApp.clear();
					mInternalStorageApp.clear();
					HashMap<String, ArrayList<AppMigrationBean>> map = (HashMap<String, ArrayList<AppMigrationBean>>) msg.obj;
					mSystemApp = map.get("system");
					mSdApp = map.get("sd");
					mInternalStorageApp = map.get("internal");
					Collections.sort(mSdApp, mComparator);
					Collections.sort(mInternalStorageApp, mComparator);
					Collections.sort(mSystemApp, mComparator);
					// 移除进度显示
					if (mProgressLinearLayout != null) {
						removeView(mProgressLinearLayout);
						mProgressLinearLayout = null;
					}
					mAppGameTabsBar.setVisibility(View.VISIBLE);
					mScrollerViewGroup.setVisibility(View.VISIBLE);
					// scrollGroup的三个分页
					for (int i = 0; i < mTitleResources.length; i++) {
						AppMigrationContainer container = (AppMigrationContainer) mInflater
								.inflate(R.layout.app_migration_container, null);
						mScrollerViewGroup.addView(container, new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.FILL_PARENT,
								LinearLayout.LayoutParams.FILL_PARENT));
						// 手机内存
						if (mTitleResources[i] == R.string.appgame_migration_tab_phone) {
							ArrayList<AppMigrationBean> internalStorageApp = sortByGroup(mInternalStorageApp);
							container.setData(internalStorageApp,
									AppMigrationBean.sTYPE_INTERNAL_STORAGE);
						} else if (mTitleResources[i] == R.string.appgame_migration_tab_sdcard) {
							// SD卡
							ArrayList<AppMigrationBean> sdApp = sortByGroup(mSdApp);
							container.setData(sdApp, AppMigrationBean.sTYPE_SD);
						} else if (mTitleResources[i] == R.string.appgame_migration_tab_no_move) {
							// 系统程序(不可移动)
							ArrayList<AppMigrationBean> systemApp = sortByGroup(mSystemApp);
							container.setData(systemApp, AppMigrationBean.sTYPE_SYSTEM);
						}
					}
					mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
					break;
				case mMsg_UPDATE_VIEW:
					if (mPkgs.size() > 0) {
						for (int i = 0; i < mPkgs.size(); i++) {
							int index = isExistByPackageName(mSdApp, mPkgs.get(i));
							if (index != -1) {
								AppMigrationBean bean = mSdApp.remove(index);
								bean.setType(AppMigrationBean.sTYPE_INTERNAL_STORAGE);
								mInternalStorageApp.add(bean);
							} else {
								index = isExistByPackageName(mInternalStorageApp, mPkgs.get(i));
								if (index != -1) {
									AppMigrationBean bean = mInternalStorageApp.remove(index);
									bean.setType(AppMigrationBean.sTYPE_SD);
									mSdApp.add(bean);
								}
							}
						}
						mPkgs.clear();
						// 对列表重新排序
						Collections.sort(mSdApp, mComparator);
						Collections.sort(mInternalStorageApp, mComparator);
						// 重新按A～Z分组
						ArrayList<AppMigrationBean> internalStorageApp = sortByGroup(mInternalStorageApp);
						ArrayList<AppMigrationBean> sdApp = sortByGroup(mSdApp);
						// 更新“手机”和“SD”界面的adapter的数据源
						for (int i = 0; i < mScrollerViewGroup.getChildCount(); i++) {
							AppMigrationContainer container = (AppMigrationContainer) mScrollerViewGroup
									.getChildAt(i);
							if (container.getShowType() == AppMigrationBean.sTYPE_SD) {
								container.updateAdapterList(sdApp);
							} else if (container.getShowType() == AppMigrationBean.sTYPE_INTERNAL_STORAGE) {
								container.updateAdapterList(internalStorageApp);
							}
						}
					}
					break;
			}
		};
	};

	public void setOnBackClickListener(OnClickListener listener) {
		mOnBackClickListener = listener;
	}

	private void removeSelf() {
		if (mOnBackClickListener != null) {
			mOnBackClickListener.onClick(mBackButton);
		}
	}

	/**
	 * <br>功能简述:对扫描出来的应用按A～Z进行分组,传递的参数不会改变，返回一个全新的list
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param list
	 * @return
	 */
	private ArrayList<AppMigrationBean> sortByGroup(final ArrayList<AppMigrationBean> list) {
		ArrayList<AppMigrationBean> tempList = (ArrayList<AppMigrationBean>) list.clone();
		for (int i = 0; i < list.size(); i++) {
			char firstLetter = '#';
			if (list.get(i).getName() == null || list.get(i).getName().equals("")) {
				firstLetter = '#';
			} else {
				firstLetter = SortUtils.changeChineseToSpell(getContext(), list.get(i).getName())
						.toUpperCase().charAt(0);
			}
			if (firstLetter < 'A' || firstLetter > 'Z') {
				firstLetter = '#';
			}
			int index = isExistByName(tempList, String.valueOf(firstLetter));
			if (index != -1) {
				continue;
			} else {
				index = isExistByName(tempList, list.get(i).getName());
				AppMigrationBean bean = new AppMigrationBean();
				bean.setName(String.valueOf(firstLetter));
				bean.setType(AppMigrationBean.sTYPE_GROUP);
				tempList.add(index, bean);
			}
		}
		return tempList;
	}

	/**
	 * <br>功能简述:用APP名字查询这个APP是否存在于列表中
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param list
	 * @param groupName
	 * @return
	 */
	private int isExistByName(ArrayList<AppMigrationBean> list, String groupName) {
		for (int i = 0; i < list.size(); i++) {
			AppMigrationBean bean = list.get(i);
			if (bean.getName().equals(groupName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * <br>功能简述:用APP的包名查询这个APP是否存在于列表中
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param list
	 * @param groupName
	 * @return
	 */
	private int isExistByPackageName(ArrayList<AppMigrationBean> list, String PackageName) {
		for (int i = 0; i < list.size(); i++) {
			AppMigrationBean bean = list.get(i);
			if (bean.getPackageName().equals(PackageName)) {
				return i;
			}
		}
		return -1;
	}

	private void registerStorage() {
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE)) {
						String[] pkgs = intent
								.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
						for (int i = 0; i < pkgs.length; i++) {
							mPkgs.add(pkgs[i]);
						}
						mHandler.sendEmptyMessage(mMsg_UPDATE_VIEW);
					}
				}
			};
		}
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		getContext().registerReceiver(mReceiver, intentFilter);
	}

	/**
	 * 更新当前屏幕选中页面的位置
	 */
	public synchronized void setCurrentIndex(int index) {
		mCurrentIndex = index;
	}

	/**
	 * 获取当前屏幕选中位置
	 */
	public synchronized int getCurrentIndex() {
		return mCurrentIndex;
	}

	@Override
	public ScreenScroller getScreenScroller() {
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {

	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		AsyncImageManager.getInstance().restore();
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		mAppGameTabsBar.setButtonSelected(newScreen, true);
	}

	@Override
	public void onScrollFinish(int currentScreen) {

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			removeSelf();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			removeSelf();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void cleanup() {
		if (mReceiver != null) {
			getContext().unregisterReceiver(mReceiver);
		}
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.removeAllViews();
		}
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
	}

	@Override
	public void sdCardTurnOff() {
		mHandler.sendEmptyMessage(mMsg_INIT_LOADING);
		if (mThread != null) {
			
			mThread.interrupt();
			mThread = null;
		}
		mThread = new MyThread();
		mThread.start();
	}

	@Override
	public void sdCardTurnOn() {
		mHandler.sendEmptyMessage(mMsg_INIT_LOADING);
		if (mThread != null) {
			mThread.interrupt();
			mThread = null;
		}
		mThread = new MyThread();
		mThread.start();
	}

	@Override
	public void onActiveChange(boolean isActive) {

	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		return false;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		return false;
	}

	@Override
	public void onResume() {
		
	}

	@Override
	public void onStop() {

	}

	@Override
	public void onAppAction(String packName, int appAction) {
		// TODO Auto-generated method stub
		if (appAction == MainViewGroup.FLAG_INSTALL || appAction == MainViewGroup.FLAG_UNINSTALL
				|| appAction == MainViewGroup.FLAG_UPDATE) {
			mHandler.sendEmptyMessage(mMsg_INIT_LOADING);
			if (mThread != null) {
				mThread.interrupt();
				mThread = null;
			}
			mThread = new MyThread();
			mThread.start();
		}
	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {

	}

	@Override
	public void initEntrance(int access) {

	}

	@Override
	public int getTypeId() {
		return 0;
	}

	@Override
	public void onFinishAllUpdateContent() {

	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {

	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {

	}

	@Override
	public void onTrafficSavingModeChange() {

	}

	@Override
	public void setUpdateData(Object value, int state) {

	}

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers) {
		// do nothing
		
	}

	@Override
	public void removeContainers() {
		// do nothing
		
	}

	@Override
	public List<IContainer> getSubContainers() {
		// do nothing
		return null;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		// do nothing
		
	}

	@Override
	public void prevLoading() {
		//do nothing
	}

	@Override
	public void prevLoadFinish() {
		//do nothing
	}
	
	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}
}
