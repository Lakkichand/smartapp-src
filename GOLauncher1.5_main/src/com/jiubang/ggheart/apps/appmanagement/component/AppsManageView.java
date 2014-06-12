package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.HashSet;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.appmanagement.download.ApplicationDownloadListener;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BaseView;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class AppsManageView extends BaseView implements ScreenScrollerListener, IMessageHandler {

	public static final int MY_APPS_VIEW_ID = 0;
	public static final int APPS_UPDATE_VIEW_ID = 1;
	public static final int APPS_RECOMMEND_VIEW_ID = 2;

	// 点击统计栏跳转到应用管理的标识
	public static final int APPS_START_TYPE = 1;

	private LayoutInflater mInflater;
	// private ThemeTitle mThemeTitle = null;
	private LinearLayout mTopTitleLinear;
	private AppsUpdateViewContainer mUpdateListViewContainer = null; // 更新列表
	private MyAppsContainer mMyAppsListContainer = null; // 卸载列表
	private RecommendedAppsContainer mRecommAppContainer; // 推荐列表

	private ScrollerViewGroup mScrollerViewGroup = null; // 可手势滑动的LinearLayout
	private LinearLayout mTabLayout = null; // tab布局
	private int mCurrentTab = 0; // 记录当前显示是哪个tab，用于返回时显示
	private TextView mMyAppsTab = null;
	private AppsUpdateTab mAppsUpdateTab = null;
	private TextView mRecommAppTab = null;
	private HashSet<TextView> mTabSet = new HashSet<TextView>();
	private OnClickListener mClickListener = null;
	private int mEntranceId = -1; // 入口id
	private Handler mHandler = null; // 用于处理点击title返回gostore事件
	private ImageView mOperatorButton;
	private ImageView mDelallButton; // 批量卸载按钮
	private int mOrderType;
	private PreferencesManager mPreferences;

	// private Context mContext = null;

	// go桌面发送到Go任务管理器的广播
	// private static final String APPMANAGEMENT_ACTION_APP_SIZE =
	// "com.gau.go.launcherex.gowidget.taskmanager.appmanagement.action.APP_SIZE";
	// private static final String APPMANAGEMENT_RECEIVER_APP_SIZE =
	// "com.gau.go.launcherex.appmanagement.action.APP_SIZE";
	// 启动GO任务管理的service
	// public static final String APPMANAGEMENT_ACTION_START_TASKMANAGER =
	// "com.gau.go.launcherex.gowidget.taskmanager.start_service_for_golauncher";

	public AppsManageView(Context context, int entranceId, int showViewId) {
		super(context);
		// mContext = context;
		mInflater = LayoutInflater.from(context);
		GoLauncher.registMsgHandler(this);
		mEntranceId = entranceId;
		mPreferences = new PreferencesManager(getContext(), IPreferencesIds.APPS_ORDER_TYPE,
				Context.MODE_PRIVATE);
		// 初始化界面
		initView();
		mScrollerViewGroup.gotoViewByIndex(showViewId);
		registerSDCardListener(context);
		registerDownloadReceiver(context);
		// context.registerReceiver(appSizeListener, new IntentFilter(
		// APPMANAGEMENT_RECEIVER_APP_SIZE));
	}

	/**
	 * 初始化界面的方法
	 */
	private void initView() {
		setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		setOrientation(LinearLayout.VERTICAL);
		// setBackgroundResource(android.R.color.white);
		setBackgroundColor(Color.parseColor("#faf9f9"));
		String title = getContext().getString(R.string.softmanager_titile);
		// 初始化标题啊
		initTitle(title);
		// 初始化tab
		initTab();
		mScrollerViewGroup = new ScrollerViewGroup(getContext(), this);
		// 初始化列表
		initListView();
		addView(mScrollerViewGroup);
	}

	/**
	 * 初始化标题的方法
	 */
	private void initTitle(String titleStr) {
		// title部分得到上面的标题条
		// mThemeTitle = (ThemeTitle) mInflater.inflate(
		// R.layout.themestore_toptitle, null);
		mTopTitleLinear = (LinearLayout) mInflater.inflate(
				R.layout.apps_management_toptitle_layout, null);
		mTopTitleLinear.setBackgroundResource(R.drawable.themestore_detail_topbar_bg);

		TextView titleText = (TextView) mTopTitleLinear
				.findViewById(R.id.apps_management_title_text);
		titleText.setText(titleStr);

		ImageView backButton = (ImageView) mTopTitleLinear
				.findViewById(R.id.apps_management_title_back_iamge);

		if (mEntranceId != IDiyFrameIds.GO_STORE_FRAME) {
			// 如果不是由gostore进入，则不显示返回箭头，并且没有点击事件
			backButton.setVisibility(GONE);
		} else {
			backButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO 结束应用管理Activity，启动gostore Activity
					Message msg = new Message();
					msg.what = AppsManagementActivity.BACK_TO_GOSTORE;
					mHandler.sendMessage(msg);
				}
			});
		}

		mOperatorButton = (ImageView) mTopTitleLinear
				.findViewById(R.id.apps_management_title_sort_button);
		mOperatorButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentTab == MY_APPS_VIEW_ID) {
					// 切换集中排序方式
					// showDialog();
					showSelectSort();
				} else if (mCurrentTab == APPS_UPDATE_VIEW_ID) {
					// 忽略更新
					Message message = mHandler
							.obtainMessage(AppsManagementActivity.NO_UPDATE_BUTTON_CLICK);
					message.arg1 = AppsManagementActivity.MESSAGE_SHOW_NO_UPDATE_VIEW;
					mHandler.sendMessage(message);
				} else {
					// 应用推荐模块，操作按钮要隐藏
					// if (v.getVisibility() == View.VISIBLE) {
					// v.setVisibility(View.GONE);
					// }
					// Log.d("AppsManageView", " this is not should running ");
				}
			}
		});
		setOperatorImage();

		// 批量删除按钮
		mDelallButton = (ImageView) mTopTitleLinear
				.findViewById(R.id.apps_management_title_delall_button);
		mDelallButton.setImageResource(R.drawable.download_uninstall);
		mDelallButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Message msg = new Message();
				msg.what = AppsManagementActivity.GO_TO_APPS_UNINSTALL;
				mHandler.sendMessage(msg);
			}
		});

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mTopTitleLinear, params);
	}

	private void initTab() {
		mClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v != null && v instanceof TextView) {
					mScrollerViewGroup.gotoViewByIndex((Integer) v.getTag());
					// onScreenChanged 也会调用这个方法；所以注释了.
					// changeCurrentTab((TextView) v);
				}
			}
		};

		mTabLayout = (LinearLayout) mInflater.inflate(R.layout.appsmanagement_tab, null);
		mMyAppsTab = (TextView) mTabLayout.findViewById(R.id.my_apps);
		mMyAppsTab.setOnClickListener(mClickListener);
		mMyAppsTab.setTag(MY_APPS_VIEW_ID);
		mAppsUpdateTab = (AppsUpdateTab) mTabLayout.findViewById(R.id.update_apps);
		mAppsUpdateTab.setOnClickListener(mClickListener);
		mAppsUpdateTab.setTag(APPS_UPDATE_VIEW_ID);
		mRecommAppTab = (TextView) mTabLayout.findViewById(R.id.recomm_apps);
		mRecommAppTab.setOnClickListener(mClickListener);
		mRecommAppTab.setTag(APPS_RECOMMEND_VIEW_ID);
		addView(mTabLayout);
		mTabSet.add(mMyAppsTab);
		mTabSet.add(mAppsUpdateTab);
		mTabSet.add(mRecommAppTab);

		changeCurrentTab(mMyAppsTab);
	}

	/**
	 * 初始化列表的方法
	 */
	private void initListView() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mMyAppsListContainer = (MyAppsContainer) mInflater.inflate(
				R.layout.apps_management_myapps_layout, null);
		mScrollerViewGroup.addView(mMyAppsListContainer, params);

		if (mUpdateListViewContainer == null) {
			mUpdateListViewContainer = (AppsUpdateViewContainer) mInflater.inflate(
					R.layout.appsmanagement_update_list_container, null);
		}
		mScrollerViewGroup.addView(mUpdateListViewContainer, params);
		mRecommAppContainer = (RecommendedAppsContainer) mInflater.inflate(
				R.layout.apps_management_recomm_layout, null);
		mScrollerViewGroup.addView(mRecommAppContainer, params);
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
	}

	@Override
	public boolean hasNoData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void recycle() {
		super.recycle();
		GoLauncher.unRegistMsgHandler(this);

		if (mMyAppsListContainer != null) {
			mMyAppsListContainer.cleanup();
			mMyAppsListContainer = null;
		}
		if (mUpdateListViewContainer != null) {
			mUpdateListViewContainer.cleanup();
			mUpdateListViewContainer = null;
		}
		if (mRecommAppContainer != null) {
			mRecommAppContainer.cleanup();
			mRecommAppContainer = null;
		}
		if (mScrollerViewGroup != null) {
			mScrollerViewGroup.removeAllViews();
			mScrollerViewGroup.destory();
			mScrollerViewGroup = null;
		}
		// if (mAppSizeList != null) {
		// mAppSizeList.clear();
		// }
	}

	@Override
	public void updateUI() {
		super.updateUI();
		if (mCurrentTab == MY_APPS_VIEW_ID) {
			updateMyAppsList();
		} else if (mCurrentTab == APPS_UPDATE_VIEW_ID) {
			mUpdateListViewContainer.requestData();
		}
	}

	// -----------------滚动器事件-----------------------//
	@Override
	public ScreenScroller getScreenScroller() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingIntercepted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFlingStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub
		if (newScreen == MY_APPS_VIEW_ID) {
			changeCurrentTab(mMyAppsTab);
		} else if (newScreen == APPS_UPDATE_VIEW_ID) {
			changeCurrentTab(mAppsUpdateTab);
		} else {
			changeCurrentTab(mRecommAppTab);
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		// TODO Auto-generated method stub

	}

	private void changeCurrentTab(TextView currentTab) {
		if (mOperatorButton != null) {
			mCurrentTab = (Integer) currentTab.getTag();
			if (mCurrentTab == APPS_RECOMMEND_VIEW_ID) {
				mOperatorButton.setVisibility(View.GONE);
				mDelallButton.setVisibility(View.GONE);
			} else {
				if (mCurrentTab == APPS_UPDATE_VIEW_ID) {
					mOperatorButton.setImageResource(R.drawable.apps_manage_no_update_button);
					mDelallButton.setVisibility(View.GONE);
				} else {
					setOperatorImage();
					mDelallButton.setVisibility(View.VISIBLE);
				}
				if (mOperatorButton.getVisibility() == View.GONE) {
					mOperatorButton.setVisibility(View.VISIBLE);
				}
			}
		}

		for (TextView tab : mTabSet) {
			if (tab == currentTab) {
				tab.setBackgroundResource(R.drawable.appsmanagement_tab_current_selector);
			} else {
				tab.setBackgroundResource(R.drawable.appsmanagement_tab_default_selector);
			}
		}
	}

	public MyAppsContainer getMyAppsContainer() {
		return mMyAppsListContainer;
	}

	public AppsUpdateViewContainer getUpdateAppsContainer() {
		return mUpdateListViewContainer;
	}

	public AppsUpdateTab getAppsUpdateTab() {
		return mAppsUpdateTab;
	}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId, final int param,
			final Object object, final List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPS_MANAGEMENT_UPDATE_COUNT :
				mAppsUpdateTab.setUpdateCount(param);
				break;
		// case IDiyMsgIds.APPS_MANAGEMENT_QUERY_APP_SIZE:
		// Log.d("handleMessage", "sendBrocastReceicerForAppSize----");
		// this.sendBrocastReceicerForAppSize();
		}
		return false;
	}

	@Override
	public int getId() {
		return IDiyFrameIds.APPS_MANAGEMENT_MAIN_FRAME;
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
		mMyAppsListContainer.setHandler(mHandler);
	}

	// 注册监听sdcard状态
	// 监听类
	private final BroadcastReceiver mSdcardListener = new BroadcastReceiver() {
		Boolean mTempStatus = false;

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			Log.d("TAG", "sdcard action:::::" + action);
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)
			// || Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
			// || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)
			) {

				// SD卡成功挂载
				if (mUpdateListViewContainer != null) {
					mUpdateListViewContainer.showUpdateView();
				}
				if (mRecommAppContainer != null) {
					mRecommAppContainer.showUpdateView();
				}
				updateMyAppsList();
				mTempStatus = false;
			} else if (Intent.ACTION_MEDIA_REMOVED.equals(action)
					|| Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
					|| Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
				// SD卡挂载失败
				if (mTempStatus == false) {

					if (mUpdateListViewContainer != null) {
						mUpdateListViewContainer.showExceptionView();
					}
					if (mRecommAppContainer != null) {
						mRecommAppContainer.showExceptionView(false);
					}
					updateMyAppsList();

					mTempStatus = true;
				}
			}
		}
	};

	// 注册监听
	private void registerSDCardListener(Context context) {
		IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		intentFilter.addDataScheme("file");
		context.registerReceiver(mSdcardListener, intentFilter);
	}

	public void unregisterReceiver(Context context) {
		context.unregisterReceiver(mSdcardListener);
		context.unregisterReceiver(mDownloadReceiver);
		// 取消go任务管理的监听
		// context.unregisterReceiver(appSizeListener);
		Log.d("AppaManageView", "unregisterReceiver");
	}

	// public void sendBrocastReceicerForAppSize() {

	// Intent intent = new Intent(APPMANAGEMENT_ACTION_APP_SIZE);
	// mContext.sendBroadcast(intent);
	// Log.d("sendBrocastReceicerForAppSize",
	// "sendBrocastReceicerForAppSize is running ---");
	// }

	// private List<String> mAppSizeList = null;
	// private final BroadcastReceiver appSizeListener = new BroadcastReceiver()
	// {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// if (intent != null) {
	// List<String> appSizeList = intent
	// .getStringArrayListExtra("cache_message_list");
	// if (appSizeList != null && !appSizeList.isEmpty()) {
	// mAppSizeList = appSizeList;
	// Log.e("BroadcastReceiver", "appSizeList size:"
	// + mAppSizeList.size());
	// refreshMyAppsList();
	// }
	// }
	// }
	// };

	/**
	 * 重新加载应用程序信息
	 */
	public void updateMyAppsList() {
		if (mMyAppsListContainer != null) {
			// mMyAppsListContainer.updateList(mAppSizeList);
			mMyAppsListContainer.updateList(null);
		}
	}

	/**
	 * 安装，更新和卸载时，刷新数据
	 * 
	 * @param packageName
	 * @param isInstall
	 */
	public void refreshData(String packageName, boolean isInstall) {
		if (packageName == null || "".equals(packageName)) {
			return;
		}
		if (mRecommAppContainer != null) {
			mRecommAppContainer.refreshData(packageName, isInstall);
		}
		if (mUpdateListViewContainer != null) {
			mUpdateListViewContainer.updateList(packageName, isInstall);
		}
		updateMyAppsList();
	}

	/**
	 * 收到任务管理的数据后，刷新应用程序大小
	 */
	// public void refreshMyAppsList() {
	// if (mMyAppsListContainer != null) {
	// mMyAppsListContainer.refreshData(mAppSizeList);
	// }
	// }
	public void showSelectSort() {
		AlertDialog.Builder builder = new DeskBuilder(getContext());
		mOrderType = mPreferences.getInt("orderType", 0);

		builder.setTitle(getContext().getString(R.string.apps_sort_dialog_title));
		builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
			}
		});

		CharSequence[] data = getContext().getResources().getTextArray(
				R.array.apps_sort_dialog_style);
		if (null != data && data.length > 0) {
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getContext(),
					R.layout.desk_select_dialog_singlechoice, R.id.radio_textview, data);
			builder.setAdapter(adapter, null);
		}

		builder.setSingleChoiceItems(R.array.select_sort_style, mOrderType,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						int currType = item;

						dialog.dismiss();

						mPreferences.putInt("orderType", currType);
						mPreferences.commit();
						if (mOrderType != currType) {
							updateMyAppsList();
							mOrderType = currType;
							setOperatorImage();
						}
					}
				});
		try {
			builder.show();
		} catch (Exception e) {
			try {
				DeskToast.makeText(getContext(), R.string.alerDialog_error, Toast.LENGTH_SHORT)
						.show();
			} catch (OutOfMemoryError error) {
				OutOfMemoryHandler.handle();
			}
		}
	}

	private void setOperatorImage() {
		if (mOperatorButton != null) {
			mOrderType = mPreferences.getInt("orderType", 0);
			int resourceId = R.drawable.apps_sort_by_time_bg;
			switch (mOrderType) {
				case 0 :
					resourceId = R.drawable.apps_sort_by_location_bg;
					break;
				case 1 :
					resourceId = R.drawable.apps_sort_by_time_bg;
					break;
				case 2 :
					resourceId = R.drawable.apps_sort_by_name_bg;
					break;
				case 3 :
					resourceId = R.drawable.apps_sort_by_size_bg;
					break;
				default :
					break;
			}
			mOperatorButton.setImageResource(resourceId);
		}
	}

	/**
	 * 注册下载状态的事件
	 */
	private void registerDownloadReceiver(Context context) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_APP_DOWNLOAD_FOR_APPS);
		context.registerReceiver(mDownloadReceiver, intentFilter);
	}

	/**
	 * 接收下载进度的广播
	 */
	private BroadcastReceiver mDownloadReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int downloadState = intent.getIntExtra(
					ApplicationDownloadListener.ACTION_APP_DOWNLOAD_STATE, 0);
			DownloadTask downloadTask = intent
					.getParcelableExtra(ApplicationDownloadListener.ACTION_APP_DOWNLOAD_TASK);
			boolean refreshUpdateAppState = true;
			boolean refreshRecommAppState = true;
			if (downloadTask.getState() == DownloadTask.STATE_DOWNLOADING) {
				if (mCurrentTab == APPS_UPDATE_VIEW_ID) {
					refreshRecommAppState = false;
				} else if (mCurrentTab == APPS_RECOMMEND_VIEW_ID) {
					refreshUpdateAppState = false;
				} else {
					//如果在我的应用界面，就不需要更新下载进度
					refreshUpdateAppState = false;
					refreshRecommAppState = false;
				}
			}
			if (refreshUpdateAppState) {
				if (mUpdateListViewContainer != null) {
					mUpdateListViewContainer.updateDownloadState(downloadState, downloadTask);
				}
			}

			if (refreshRecommAppState) {
				if (mRecommAppContainer != null) {
					mRecommAppContainer.updateDownloadState(downloadState, downloadTask);
				}
			}

		}
	};
	// private void updateAppList() {
	// if (mMyAppsListContainer != null) {
	// mMyAppsListContainer.updateList(null);
	// }
	//
	// }
}
