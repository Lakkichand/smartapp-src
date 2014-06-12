/*
 * 文 件 名:  DownloadControllerView.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-9-4
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.downloadmanager;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
/**
 * <br>类描述:下载管理的控制界面
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-9-4]
 */
public class DownloadControllView extends RelativeLayout
		implements
			OnClickListener,
			OnScrollListener,
			OnItemLongClickListener {
	/**
	 * “点一点，发现惊喜”读取数据的分类id
	 */
	private static final int ONECLICK_TYPEID = 101;
	
	// 下载列表
	private PinnedHeaderListView mListView;

	// 返回箭头按钮
	private ImageButton mBackBtn;

	// 显示批量删除按钮
	private ImageButton mDeleteViewBtn;

	// 下载控制器
	private IDownloadService mDownloadController = null;

	//回调的handler
	private Handler mHandler = null;

	// 无下载项提示
	private NetworkTipsTool mNetworkTipTool = null;

	//正在下载的任务列表
	private ArrayList<DownloadTask> mDownloadingList = new ArrayList<DownloadTask>();

	//已经下载完成的任务列表
	private ArrayList<DownloadTask> mDownloadedList = new ArrayList<DownloadTask>();

	//已经下载并且安装的任务列表
	private ArrayList<DownloadTask> mInstalledList = new ArrayList<DownloadTask>();

	private DownloadControllViewAdapter mAdapter = null;

	public DownloadControllView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DownloadControllView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DownloadControllView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		initDownloadController();
		mListView = (PinnedHeaderListView) findViewById(R.id.appgame_download_controll_listView);
		mListView.setOnScrollListener(this);
		mListView.setOnItemLongClickListener(this);
		mBackBtn = (ImageButton) findViewById(R.id.appgame_download_controll_back);
		mBackBtn.setOnClickListener(this);
		mDeleteViewBtn = (ImageButton) findViewById(R.id.appgame_download_delete_view);
		mDeleteViewBtn.setOnClickListener(this);
		//先获取数据
		updateData();
		//再更新view
		updateTipView();
		//初始化adapter
		mAdapter = new DownloadControllViewAdapter(getContext(), mDownloadingList, mDownloadedList,
				mInstalledList);
		mAdapter.setDownloadController(mDownloadController);
		mListView.setAdapter(mAdapter);
		initListHeaderView();
	}

	private void initListHeaderView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.recomm_appsmanagement_list_head, mListView, false);
		TextView tv = (TextView) view.findViewById(R.id.nametext);
		//对显示的文字做margin的设置
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		int padding = getContext().getResources().getDimensionPixelSize(
				R.dimen.download_manager_text_padding);
		tv.setPadding(padding * 3, padding, 0, padding);
		tv.setLayoutParams(lp);
//		int paddingLeft = getContext().getResources().getDimensionPixelSize(
//				R.dimen.appcenter_list_item_padding);
//		mListView.setPaddingLeft(paddingLeft);
		mListView.setPinnedHeaderView(view);
	}

	private void initDownloadController() {
		mDownloadController = ((AppsDownloadActivity) getContext()).getDownloadController();
	}

	/**
	 * 点击“点一点，发现惊喜"的点击事件处理
	 */
	private OnClickListener mNetworkTipBtnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			AppsManagementActivity.startTopic(getContext(), ONECLICK_TYPEID, false);
		}
	};

	private void updateTipView() {
		if (mNetworkTipTool == null) {
			LinearLayout tipview = (LinearLayout) findViewById(R.id.apps_download_tips);
			mNetworkTipTool = new NetworkTipsTool(tipview);
		}
		if (mDownloadingList != null && mDownloadingList.size() <= 0 && mDownloadedList != null
				&& mDownloadedList.size() <= 0 && mInstalledList != null
				&& mInstalledList.size() <= 0) {
			mListView.setVisibility(View.INVISIBLE);
			//			mNetworkTipTool.showErrorTip(NetworkTipsTool.TYPE_NO_DOWNLOAD_ITEM);
			mNetworkTipTool.showRetryErrorTip(mNetworkTipBtnListener,
					NetworkTipsTool.TYPE_NO_DOWNLOAD_ITEM);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mNetworkTipTool.showNothing();
			mNetworkTipTool.removeProgress();
		}
	}

	private void updateData() {
		if (mDownloadController != null) {
			try {
				mDownloadingList.clear();
				mDownloadedList.clear();
				// "正在下载“的任务
				mDownloadingList = (ArrayList<DownloadTask>) mDownloadController
						.getDownloadingTaskSortByTime();
				// 已经下载完成的任务
				mDownloadedList = (ArrayList<DownloadTask>) mDownloadController
						.getDownloadCompleteList();
				// 已经下载完成且安装的下载任务
				mInstalledList.clear();
				Iterator<DownloadTask> it = mDownloadedList.iterator();
				ArrayList<String> installedTaskList = (ArrayList<String>) mDownloadController
						.getInstalledTaskList();
				while (it.hasNext()) {
					DownloadTask task = (DownloadTask) it.next();
					// 先判断apk包是否还在ＳＤ卡
					if (FileUtil.isFileExist(task.getSaveFilePath())) {
						// 假如程序已经安装在设备上
						// 判断已下载的程序包的版本是不是高于设备
						// 高于设备则显示安装，不属于“已安装”列表
						if (isAppExist(task.getDownloadApkPkgName())) {
							if (installedTaskList.contains(task.getDownloadApkPkgName())) {
								mInstalledList.add(task);
								it.remove();
							}
						}
					} else {
						it.remove();
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 通过包名查询程序是否已经安装
	 * 
	 * @param packageName
	 * @return true安装 ，false没有安装
	 */
	private boolean isAppExist(String packageName) {
		boolean bRet = AppUtils.isAppExist(getContext(), packageName);
		return bRet;
	}

	@Override
	public void onClick(View v) {
		if (v == mBackBtn) {
			mHandler.sendEmptyMessage(AppsDownloadActivity.MSG_FINISH_ACTIVITY);
			return;
		} else if (v == mDeleteViewBtn) {
			mHandler.sendEmptyMessage(AppsDownloadActivity.MSG_SHOW_DELETE_VIEW);
			return;
		}
	}

	public void notifyTask(DownloadTask task) {
		int firstIndex = mListView.getFirstVisiblePosition();
		int lastIndex = mListView.getLastVisiblePosition();
		switch (task.getState()) {
			case DownloadTask.STATE_WAIT :
			case DownloadTask.STATE_START :
			case DownloadTask.STATE_DOWNLOADING :
			case DownloadTask.STATE_FAIL :
			case DownloadTask.STATE_STOP :
			case DownloadTask.STATE_RESTART : {
				int count = mAdapter.getCount();
				for (int i = 0; i < count; i++) {
					DownloadTask dt = (DownloadTask) mAdapter.getItem(i);
					if (dt.getId() == task.getId()) {
						dt.setAlreadyDownloadPercent(task.getAlreadyDownloadPercent());
						dt.setAlreadyDownloadSize(task.getAlreadyDownloadSize());
						dt.setTotalSize(task.getTotalSize());
						dt.setState(task.getState());
						if (i >= firstIndex && i <= lastIndex) {
							mAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
				break;
			case DownloadTask.STATE_DELETE :
			case DownloadTask.STATE_FINISH : {
				updateList();
			}
				break;
			default :
				break;
		}
	}

	/**
	 * <br>功能简述:更新adapter的数据源
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void updateList() {
		updateData();
		updateTipView();
		mAdapter.updateList(mDownloadingList, mDownloadedList, mInstalledList);
	}

	/**
	 * <br>功能简述:设置回调的handler
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// mContactLogic.onScrollStateChanged(view, scrollState);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		// AndroidDevice.hideInputMethod(ContactListActivity.this);
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (mHandler != null) {
			mHandler.sendEmptyMessage(AppsDownloadActivity.MSG_SHOW_DELETE_VIEW);
		}
		return true;
	}
}
