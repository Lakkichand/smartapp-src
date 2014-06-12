/*
 * 文 件 名:  AppKitActivity.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-12-3
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.recommend;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp.BoutiqueAppInfo;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.component.CommonProgress;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataDownload;
import com.jiubang.ggheart.appgame.base.downloadmanager.AppsDownloadActivity;
import com.jiubang.ggheart.appgame.base.utils.AppDownloadListener;
import com.jiubang.ggheart.appgame.base.utils.AppGameConfigUtils;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-12-3]
 */
public class AppKitsActivity extends Activity implements OnClickListener, OnScrollListener {
	/**
	 * 选择项的状态：勾选、未勾选、下载中
	 */
	public final static int STATE_UNSELECT = 0;

	public final static int STATE_SELECT = 1;

	public final static int STATE_DOWNLOADING = 2;

	private Context mContext = null;
	/**
	 * 入口值
	 */
	private int mEntrance = -1;
	/**
	 * 从桌面快捷方式进入
	 */
	public static final int ENTRANCE_ID_SHORTCUTS = 1001;
	/**
	 * 从应用中心/游戏中心进入
	 */
	public static final int ENTRANCE_ID_CENTER = 1002;

	public static final String ENTRANCE_KEY = "AppKitsActivity_Entrance_Key";

	// 返回箭头按钮
	private ImageButton mBackBtn = null;
	// 下载管理按钮
	private ImageButton mDownloadMgrBtn = null;
	// 一键安装按钮
	private Button mApplyBtn = null;
	// 底部跳转游戏/应用中心按钮
	private Button mGoToCenterBtn = null;

	private PinnedHeaderListView mListView = null;

	private NetworkTipsTool mNetworkTip = null;

	private IDownloadService mDownloadController = null;

	private AppkitsController mController = null;

	private AppKitsAdapter mAdapter = null;
	/**
	 * 是否已经绑定下载服务的标记
	 */
	private boolean mHasBindService = false;

	private BroadcastReceiver mDownloadReceiver = null;

	private BroadcastReceiver mInstalledReceiver = null;
	/**
	 * 
	 */
	private ArrayList<AppkitsBean> mResultList = new ArrayList<AppkitsBean>();
	/**
	 * 网络错误提示的VIEW
	 */
	private RelativeLayout mTipView = null;
	/**
	 * 安装完所有的数据之后显示的view，用作mTipView的子VIEW
	 */
	private RelativeLayout mNoDataView = null;
	/**
	 * 加载下一页的进度条
	 */
	private CommonProgress mCommonProgress = null;
	/**
	 * CommonProgress的布局参数
	 */
	private FrameLayout.LayoutParams mCommonProgressLP = null;
	
	/**
	 * 装机必备的分类ID
	 */
	private int mTypeId = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.appkits_layout);
	    //根据SIM卡国家，设置语言信息
		AppGameConfigUtils.updateResourcesLocaleBySim(this, getResources());
		
		registerDownloadBroadCast();
		registerInstallBroadCast();
		mContext = this;
		// 得到入口值和类型值
		Intent intent = getIntent();
		mEntrance = intent.getIntExtra(ENTRANCE_KEY, ENTRANCE_ID_CENTER);
		mController = new AppkitsController(this, mModeChangeListener);
		initView();
		
		// 显示进度条
		showCommonProgress();
		new Thread() {
			public void run() {
				preLoading();
				mHandler.obtainMessage(MSG_RELOAD_DATA).sendToTarget();
			};
		}.start();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// 先启动下载服务
		GOLauncherApp.getContext().startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
		// 再bind服务
		if (!mHasBindService) {
			mHasBindService = GOLauncherApp.getContext().bindService(
					new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), mConnenction,
					Context.BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		//解除绑定下载服务
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			mHasBindService = false;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mController.destory();
		mResultList.clear();
		mAdapter = null;
		unregisterReceiver(mDownloadReceiver);
		unregisterReceiver(mInstalledReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdapter != null) {
			mHandler.sendEmptyMessage(MSG_DATA_REFRESH);
		}
	}

	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mDownloadController = IDownloadService.Stub.asInterface(service);
			try {
				// 添加保持DownloadService运行的Activity的className
				// 假如activity在Task的TopProject,那么DownloadService就不会被停止
				if (mDownloadController != null) {
					mDownloadController.addRunningActivityClassName(AppKitsActivity.class.getName());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mDownloadController = null;
		}
	};

	private void initView() {
		// 按钮初始化
		mApplyBtn = (Button) findViewById(R.id.apply);
		mApplyBtn.setOnClickListener(this);
		mBackBtn = (ImageButton) findViewById(R.id.yjzj_back);
		mBackBtn.setOnClickListener(this);
		mGoToCenterBtn = (Button) findViewById(R.id.gotocenter);
		mGoToCenterBtn.setOnClickListener(this);
		mDownloadMgrBtn = (ImageButton) findViewById(R.id.download_manager);
		mDownloadMgrBtn.setOnClickListener(this);
		// 其余组件初始化
		mListView = (PinnedHeaderListView) findViewById(R.id.recommend_listview);
		mListView.setOnScrollListener(this);
		mTipView = (RelativeLayout) findViewById(R.id.progress_view);
		mNetworkTip = new NetworkTipsTool(mTipView);
		ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
		// 从快捷方式入口进入一键装机界面，底部显示两个按钮
		if (mEntrance == ENTRANCE_ID_SHORTCUTS) {
			boolean isNeedAppGameEntrance = false;
			if (channelConfig != null) {
				channelConfig.roadConfig();
				isNeedAppGameEntrance = channelConfig.isNeedAppCenter();
			}
			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.gotocenterLayout);
			if (isNeedAppGameEntrance) {
				// 底部按钮可见
				linearLayout.setVisibility(View.VISIBLE);
			} else {
				linearLayout.setVisibility(View.GONE);
			}
		}
		// 根据一键装机or一键玩机设置字串
		TextView title = (TextView) findViewById(R.id.textViewTitle);
		title.setText(R.string.recommended_yjzj);
		
		mGoToCenterBtn.setText(R.string.appcenter_title);
		//3.18需求，从桌面图标进入一键装机玩机不显示返回箭头
		if (mEntrance == ENTRANCE_ID_SHORTCUTS) {
			mBackBtn.setVisibility(View.GONE);
			title.setPadding((int) this.getResources().getDimension(R.dimen.appkits_title_padding),
					0, 0, 0);
		}
	}
	
	/**
	 * 展示浮在列表底部的进度条
	 */
	private void showCommonProgress() {
		if (mCommonProgress == null) {
			LayoutInflater inflater = LayoutInflater.from(this);
			mCommonProgress = (CommonProgress) inflater.inflate(R.layout.appgame_common_progress,
					null);
			mCommonProgressLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					DrawUtils.dip2px(40), Gravity.BOTTOM);
			mCommonProgressLP.bottomMargin = DrawUtils.dip2px(56.0f);
			addContentView(mCommonProgress, mCommonProgressLP);
		}
		mCommonProgress.setVisibility(View.VISIBLE);
		mCommonProgress.startAnimation(AppGameDrawUtils.getInstance().mCommonProgressAnimation);
	}

	/**
	 * 移除浮在列表底部的进度条
	 */
	private void removeCommonProgress() {
		if (mCommonProgress != null) {
			mCommonProgress.setVisibility(View.GONE);
		}
	}
	
	/**
	 * <br>功能简述:当一键装机的所有数据都安装之后，显示这个VIEW
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initAllInstallView() {
		if (mNoDataView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			mNoDataView = (RelativeLayout) inflater.inflate(R.layout.themestore_nodata_tip_full,
					null);
			TextView textView1 = (TextView) mNoDataView.findViewById(R.id.noDataTextViewFullOne);
			textView1.setText(this.getString(R.string.recommended_install_all_app));
			TextView textView2 = (TextView) mNoDataView.findViewById(R.id.noDataTextViewFullTwo);
			textView2.setText("");
		}
		mNoDataView.setGravity(Gravity.CENTER);
		if (mTipView != null) {
			mTipView.removeAllViews();
			mTipView.addView(mNoDataView, new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
		}
		mNoDataView.setGravity(Gravity.CENTER);
		if (mTipView != null) {
			mTipView.addView(mNoDataView, new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
		}
	}

	/**
	 * <br>功能简述:分组信息的初始化
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initListHeaderView() {
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.recomm_appsmanagement_list_head, mListView, false);
		TextView tv = (TextView) view.findViewById(R.id.nametext);
		tv.setBackgroundResource(R.drawable.list_head_bg);
		ImageView img = (ImageView) view.findViewById(R.id.divider);
		img.setBackgroundResource(R.drawable.listview_divider);
		//对显示的文字做margin的设置
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		int padding = this.getResources().getDimensionPixelSize(
				R.dimen.download_manager_text_padding);
		tv.setPadding(padding * 2, padding, 0, padding);
		tv.setLayoutParams(lp);
		mListView.setPinnedHeaderView(view);
	}

	/**
	 * <br>功能简述:进入一键装机之后，先进行预加载
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void preLoading() {
		ClassificationDataBean bean = ClassificationDataDownload.getLocalData(301, 1, null);
		if (bean == null || bean.categoriesList == null) {
			return;
		}
		mTypeId = bean.typeId;
		ArrayList<AppkitsBean> resultList = new ArrayList<AppkitsBean>();
		for (CategoriesDataBean b : bean.categoriesList) {
			ClassificationDataBean temp = ClassificationDataDownload
					.getLocalData(b.typeId, 1, null);
			if (temp != null) {
				AppkitsBean titleBean = new AppkitsBean();
				titleBean.mTitle = temp.typename;
				resultList.add(titleBean);
				// 分组头的4个需要显示的应用
				AppkitsBean appBean = new AppkitsBean();
				if (temp.featureList == null) {
					resultList.remove(resultList.size() - 1);
					continue;
				}
				appBean.mAppInfoList = (ArrayList<BoutiqueApp>) temp.featureList;
				resultList.add(appBean);
			}
		}
		if (resultList != null && resultList.size() > 0) {
			mResultList = resultList;
			mHandler.obtainMessage(MSG_SHOW_VIEW).sendToTarget();
		}
	}
	
	/**
	 * <br>功能简述:通过网络使用controller获取数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void loadData() {
		Bundle bundle = new Bundle();
		bundle.putInt("typeId", 301);
		bundle.putInt("itp", 2);
		bundle.putInt("pageId", 1);
		bundle.putInt("startIndex", 1);
		mController.sendRequest(AppkitsController.ACTION_NEXT_PAGE, bundle);
		// 显示进度条
		showCommonProgress();
	}

	/**
	 * <br>功能简述:从网络数据结果中，筛选小于等于4个应用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param list
	 * @return
	 */
	private ArrayList<AppkitsBean> filterAppKitsBean(final ArrayList<AppkitsBean> list) {
		if (list == null || list.size() <= 0) {
			return null;
		}
		ArrayList<AppkitsBean> resultList = new ArrayList<AppkitsBean>();
		for (int i = 0; i < list.size(); i++) {
			AppkitsBean bean = list.get(i);
			if (bean.mTitle != null && !bean.mTitle.equals("")) {
				// 分组的信息
				resultList.add(bean);
			} else if (bean.mAppInfoList != null && bean.mAppInfoList.size() > 0) {
				// 应用信息
				AppkitsBean appBean = new AppkitsBean();
				int count = 0;
				for (BoutiqueApp app : bean.mAppInfoList) {
					if (!AppUtils.isAppExist(mContext, app.info.packname)) {
						appBean.mAppInfoList.add(app);
						count++;
					}
					// 只要求显示4个
					if (count == 4) {
						break;
					}
				}
				// 分组的应用信息个数为零，丢弃整个分组
				if (appBean.mAppInfoList == null || appBean.mAppInfoList.size() <= 0) {
					// 丢弃
					resultList.remove(resultList.size() - 1);
				} else {
					resultList.add(appBean);
				}
			}
		}
		return resultList;
	}

	/**
	 * <br>功能简述:刷新底部的一键安装的个数
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void setApplyButtonStyle() {
		if (mAdapter == null) {
			return;
		}
		String text = null;
		text = getString(R.string.recommended_install);
		int count = 0;
		HashMap<Long, Integer> hashMap = mAdapter.getSelectHashMap();
		Iterator<Entry<Long, Integer>> iter = hashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Long, Integer> entry = iter.next();
			int state = entry.getValue();
			if (state == STATE_SELECT) {
				count++;
			}
		}
		if (count > 0) {
			text = text + "(" + count + ")";
			mApplyBtn.setBackgroundResource(R.drawable.yjzj_selector);
			mApplyBtn.setTextColor(this.getResources().getColor(android.R.color.white));
			mApplyBtn.setEnabled(true);
		} else {
			mApplyBtn.setBackgroundResource(R.drawable.yjzi_btn_disable);
			mApplyBtn
					.setTextColor(this.getResources().getColor(R.color.appgame_download_btn_black));
			mApplyBtn.setEnabled(false);
		}
		mApplyBtn.setText(text);
	}

	/**
	 * controller的回调方法 
	 */
	private IModeChangeListener mModeChangeListener = new IModeChangeListener() {
		@Override
		public void onModleChanged(int action, int state, Object value) {
			if (action == AppkitsController.ACTION_NEXT_PAGE_DATA
					&& state == AppkitsController.STATE_RESPONSE_OK) {
				ArrayList<AppkitsBean> list = (ArrayList<AppkitsBean>) value;
				if (list == null || list.size() <= 0) {
					// 假如也没有预加载的数据 
					if (mResultList == null || mResultList.size() <= 0) {
						mHandler.obtainMessage(MSG_NO_DATA).sendToTarget();
					}
				} else {
					mResultList = list;
					// 统计需要，记录“装机必备”的分类ID
					mTypeId = mController.getTypeId();
					mHandler.obtainMessage(MSG_SHOW_VIEW).sendToTarget();
				}
			} else if (action == AppkitsController.ACTION_NEXT_PAGE_DATA
					&& state == AppkitsController.STATE_RESPONSE_ERR) {
				mHandler.obtainMessage(MSG_NO_DATA).sendToTarget();
			}
		}
	};

	private void registerDownloadBroadCast() {
		mDownloadReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (ICustomAction.ACTION_APP_DOWNLOAD.equals(intent.getAction())
						&& mAdapter != null) {
					DownloadTask downloadTask = intent
							.getParcelableExtra(AppDownloadListener.UPDATE_DOWNLOAD_INFO);
					notifyDownloadState(downloadTask);
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ICustomAction.ACTION_APP_DOWNLOAD);
		registerReceiver(mDownloadReceiver, filter);
	}

	private void registerInstallBroadCast() {
		// 注册安装广播接收器
		// 收到广播，把列表的一项从数据源移除
		mInstalledReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {
					String packageName = intent.getData().getSchemeSpecificPart();
					if (packageName != null) {
						mHandler.sendEmptyMessage(MSG_DATA_REFRESH);
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		registerReceiver(mInstalledReceiver, filter);
	}

	private void notifyDownloadState(DownloadTask downloadTask) {
		if (downloadTask == null || mAdapter == null) {
			return;
		}
		int firstIndex = mListView.getFirstVisiblePosition();
		int lastIndex = mListView.getLastVisiblePosition();
		boolean ret = false;
		int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			AppkitsBean bean = (AppkitsBean) mAdapter.getItem(i);
			if (bean.mTitle != null && !bean.mTitle.equals("")) {
				continue;
			}
			for (BoutiqueApp app : bean.mAppInfoList) {
				if (app == null || app.info == null || app.info.appid == null) {
					continue;
				}
				if (app.info.appid.equals(downloadTask.getId() + "")) {
					app.downloadState.state = downloadTask.getState();
					app.downloadState.alreadyDownloadPercent = downloadTask
							.getAlreadyDownloadPercent();
					// 因为页面有一个headerview，所以位置要加上1
					if (i >= firstIndex && i <= lastIndex) {
						ret = true;
						mAdapter.notifyDataSetChanged();
					}
					break;
				}
			}
		}
	}

	private void startDownload() {
		if (mAdapter == null || mAdapter.getSelectHashMap() == null) {
			return;
		}
		ArrayList<Long> ids = mAdapter.getSelectIds();
		HashMap<Long, Integer> hashMap = mAdapter.getSelectHashMap();
		for (Long id : ids) {
			// 从adapter里取出数据
			int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				AppkitsBean bean = (AppkitsBean) mAdapter.getItem(i);
				if (bean.mTitle != null && !bean.mTitle.equals("")) {
					continue;
				}
				for (BoutiqueApp app : bean.mAppInfoList) {
					if (app == null || app.info == null || app.info.appid == null) {
						continue;
					}
					// ID匹配的话，就进行下载
					BoutiqueAppInfo info = app.info;
					if (info != null && info.appid.equals(id + "")) {
						if (checkDownloadInfo(info.packname)) {
							hashMap.put(id, STATE_UNSELECT);
							Toast.makeText(this, info.name + this.getString(R.string.has_download),
									1000).show();
							break;
						}
						// 改变勾选状态
						hashMap.put(id, STATE_DOWNLOADING);
						// 统计下载点击量
						AppRecommendedStatisticsUtil.getInstance().saveDownloadClick(this,
									info.packname, Integer.valueOf(info.appid),
									String.valueOf(mTypeId), 1);
						// 下载保存的文件路径
						// 开始下载工作
						String filePath = GoStoreOperatorUtil.DOWNLOAD_DIRECTORY_PATH
								+ info.packname + "_" + info.version + ".apk";
						File apk = new File(filePath);
						if (apk.exists()) {
							apk.delete();
						}
						try {
							if (mDownloadController != null
									&& mDownloadController.getDownloadTaskById(id) == null) {
								long taskId = -1;
								DownloadTask task = new DownloadTask(id, info.downloadurl,
										info.name, filePath, info.packname,
										DownloadTask.ICON_TYPE_URL, info.icon,
										AppsDetail.START_TYPE_APPRECOMMENDED);
								taskId = mDownloadController.addDownloadTask(task);
								if (taskId != -1) {
									mDownloadController.addDownloadTaskListenerByName(taskId,
											AppDownloadListener.class.getName());
									mDownloadController.startDownload(taskId);
								}
							}
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		mHandler.sendEmptyMessage(MSG_LISTVIEW_REFRESH);
	}

	/**
	 * 用包名判断此次下载是否已经在下载完成队列 true , 存在于下载队列 false ， 不存在于下载队列
	 * 
	 * @param pkn
	 */
	private boolean checkDownloadInfo(String pkn) {
		try {
			ArrayList<DownloadTask> downloadCompleteList = (ArrayList<DownloadTask>) mDownloadController
					.getDownloadCompleteList();
			for (DownloadTask task : downloadCompleteList) {
				if (task.getDownloadApkPkgName().equals(pkn)) {
					return true;
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}
	@Override
	public void onClick(View v) {
		if (v == mGoToCenterBtn) {
			AppManagementStatisticsUtil.getInstance().saveCurrentEnter(this,
					AppManagementStatisticsUtil.ENTRY_TYPE_DESK);
			AppsManagementActivity.startAppCenter(this, MainViewGroup.ACCESS_FOR_RECOMMENDLIST,
					false);
			this.finish();
		} else if (v == mDownloadMgrBtn) {
			// 进入下载管理
			Intent intent = new Intent(this, AppsDownloadActivity.class);
			this.startActivity(intent);
		} else if (v == mBackBtn) {
			// 返回上一层
			ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
			if (null == channelConfig) {
				return;
			}
			AppManagementStatisticsUtil.getInstance().saveCurrentEnter(this,
					AppManagementStatisticsUtil.ENTRY_TYPE_DESK);
			AppsManagementActivity.startAppCenter(this, MainViewGroup.ACCESS_FOR_RECOMMENDLIST,
					false);
		} else if (v == mApplyBtn) {
			// 一键安装
			if (mAdapter != null) {
				if (!FileUtil.isSDCardAvaiable()) {
					Toast.makeText(this, this.getString(R.string.import_export_sdcard_unmounted),
							Toast.LENGTH_SHORT).show();
				} else {
					mApplyBtn.setBackgroundResource(R.drawable.yjzi_btn_disable);
					mApplyBtn.setEnabled(false);
					// 开始下载操作
					startDownload();
				}
			} else {
				Toast.makeText(this, this.getString(R.string.themestore_download_fail), Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	private final static int MSG_NO_DATA = 1001;

	private final static int MSG_SHOW_VIEW = 1002;

	private final static int MSG_DATA_REFRESH = 1003;

	private final static int MSG_LISTVIEW_REFRESH = 1004;
	
	private final static int MSG_INSTALL_ALL_DATA = 1005;

	private final static int MSG_RELOAD_DATA = 1006;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_NO_DATA :
					mListView.setVisibility(View.GONE);
					removeCommonProgress();
					mNetworkTip.showRetryErrorTip(new OnClickListener() {
						@Override
						public void onClick(View v) {
							loadData();
							mNetworkTip.showNothing();
						}
					}, true);
					break;
				case MSG_INSTALL_ALL_DATA:
					mListView.setVisibility(View.GONE);
					removeCommonProgress();
					initAllInstallView();
				case MSG_SHOW_VIEW :
					// 数据获取完，需要上传分类ID为统计
					if (mEntrance == ENTRANCE_ID_SHORTCUTS) {
						AppManagementStatisticsUtil.getInstance();
						AppManagementStatisticsUtil.saveTabClickData(mContext, mTypeId, null);
					}
					//　显示数据
					this.sendEmptyMessage(MSG_DATA_REFRESH);
					break;
				case MSG_DATA_REFRESH :
					// 过滤已安装的App，筛选四个组成一个分类
					ArrayList<AppkitsBean> list = filterAppKitsBean(mResultList);
					if (list == null || list.size() <= 0) {
						if (mResultList == null || mResultList.size() == 0) {
							this.sendEmptyMessage(MSG_NO_DATA);
						} else {
							this.sendEmptyMessage(MSG_INSTALL_ALL_DATA);
						}
						return;
					}
					mNetworkTip.showNothing();
					removeCommonProgress();
					mListView.setVisibility(View.VISIBLE);
					// 获取当前正在下载的所有任务
					ArrayList<DownloadTask> downloadTaskList = null; 
					if (mDownloadController != null) {
						try {
							downloadTaskList = (ArrayList<DownloadTask>) mDownloadController.getDownloadingTaskSortByTime();
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (mAdapter == null) {
						mAdapter = new AppKitsAdapter(mContext, mNotify, list, downloadTaskList);
						mListView.setAdapter(mAdapter);
						initListHeaderView();
					} else {
						mAdapter.updateList(list, downloadTaskList);
						mAdapter.notifyDataSetChanged();
					}
					break;
				case MSG_LISTVIEW_REFRESH :
					// 刷新listview
					if (mAdapter != null) {
						setApplyButtonStyle();
						mAdapter.notifyDataSetChanged();
					}
					break;
				case MSG_RELOAD_DATA:
					loadData();
					break;
				default :
					break;
			}
		};
	};

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		// AndroidDevice.hideInputMethod(ContactListActivity.this);
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}
	/**
	 * apdater勾选数据变化之后，view需要回调，进行变化
	 */
	private IAppKitsActivityNotify mNotify = new IAppKitsActivityNotify() {
		@Override
		public void notifyView() {
			setApplyButtonStyle();
		}
	};
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			// 按返回键时，判断一键装机玩机是否从桌面快捷方式进入
			// 因为一键装机属于另一个进程，假如从桌面快捷方式进入，返回时则返回到桌面进程，所以需要杀死进程
			// 假如从应用中心进入，则不需要杀死进程
			finish();
			if (mEntrance == ENTRANCE_ID_SHORTCUTS) {
				android.os.Process.killProcess(Process.myPid());
			} 
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
}