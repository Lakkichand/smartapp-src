/*
 * 文 件 名:  DownloadDeleteView.java
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.text.Html;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * <br>类描述:下载管理的批量删除界面
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-9-4]
 */
public class DownloadDeleteView extends RelativeLayout
		implements
			OnClickListener,
			OnScrollListener,
			OnItemClickListener {

	private Handler mHandler = null;

	// 返回按钮
	//	private ImageView mBackBtn = null;

	// 勾选全部按钮
	private ImageView mSelectAllBtn = null;

	// 顶部显示勾选几项的文本
	private TextView mTitleTextView = null;

	// 批量删除按钮
	private Button mDeleteBtn = null;

	// 取消按钮
	private Button mCancelBtn = null;

	private PinnedHeaderListView mListView = null;

	// 下载控制
	private IDownloadService mDownloadController = null;

	// 无下载项提示
	private NetworkTipsTool mNetworkTipTool = null;

	//正在下载的任务列表
	private ArrayList<DownloadTask> mDownloadingList = new ArrayList<DownloadTask>();

	//已经下载完成的任务列表
	private ArrayList<DownloadTask> mDownloadedList = new ArrayList<DownloadTask>();

	//已经下载并且安装的任务列表
	private ArrayList<DownloadTask> mInstalledList = new ArrayList<DownloadTask>();

	private DownloadDeleteViewAdapter mAdapter = null;

	public DownloadDeleteView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public DownloadDeleteView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DownloadDeleteView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		initDownloadController();
		mSelectAllBtn = (ImageView) findViewById(R.id.mass_delete_imageView_select);
		mSelectAllBtn.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.mass_delete_title_textView);
		mDeleteBtn = (Button) findViewById(R.id.mass_delete_btn);
		mDeleteBtn.setOnClickListener(this);
		mCancelBtn = (Button) findViewById(R.id.mass_delete_cancel_btn);
		mCancelBtn.setOnClickListener(this);
		mListView = (PinnedHeaderListView) findViewById(R.id.mass_delete_listview);
		updateData();
		updateTipView();
		mAdapter = new DownloadDeleteViewAdapter(getContext(), mDownloadingList, mDownloadedList,
				mInstalledList);
		mListView.setAdapter(mAdapter);
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		initListHeaderView();
		changeTip();
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
	}

	private void initDownloadController() {
		mDownloadController = ((AppsDownloadActivity) getContext()).getDownloadController();
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
						if (AppUtils.isAppExist(getContext(), task.getDownloadApkPkgName())) {
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
			mNetworkTipTool.showRetryErrorTip(null, NetworkTipsTool.TYPE_NO_DOWNLOAD_ITEM);
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
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		updateData();
		mAdapter.updateList(mDownloadingList, mDownloadedList, mInstalledList);
		changeTip();
	}

	@Override
	public void onClick(View v) {
		//		if (v == mBackBtn || v == mCancelBtn) {
		//			if (mHandler != null) {
		//				mHandler.sendEmptyMessage(AppsDownloadActivity.MSG_REMOVE_DELETE_VIEW);
		//				return;
		//			}
		//		}
		if (v == mCancelBtn) {
			if (mHandler != null) {
				mHandler.sendEmptyMessage(AppsDownloadActivity.MSG_REMOVE_DELETE_VIEW);
				return;
			}
		} else if (v == mSelectAllBtn) {
			boolean isSelectAll = true;
			if (mAdapter != null) {
				HashMap<Long, Boolean> map = mAdapter.getSelectHashMap();
				for (Boolean flag : map.values()) {
					if (!flag) {
						isSelectAll = false;
						break;
					}
				}
				if (isSelectAll) {
					Iterator<Long> it = map.keySet().iterator();
					while (it.hasNext()) {
						map.put((Long) it.next(), false);
					}
					mSelectAllBtn.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
				} else {
					Iterator<Long> it = map.keySet().iterator();
					while (it.hasNext()) {
						map.put((Long) it.next(), true);
					}
					mSelectAllBtn.setBackgroundResource(R.drawable.apps_uninstall_selected);
				}
				mAdapter.notifyDataSetChanged();
				changeTip();
			}
		} else if (v == mDeleteBtn) {
			if (mAdapter != null && mAdapter.getSelectCount() == 0) {
				Toast.makeText(getContext(),
						getContext().getString(R.string.download_manager_no_selected_item), 1000)
						.show();
			} else {
				View view = createDialog();
				mDialog = null;
				mDialog = new Dialog(getContext(), R.style.AppGameSettingDialog);
				mDialog.setContentView(view);
				mDialog.show();
			}
		}
	}

	private boolean mIsSelected = true;
	private Dialog mDialog = null;
	private View createDialog() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.appgame_downloadmanager_delete_dialog, null);
		RelativeLayout relativeLayout = (RelativeLayout) view
				.findViewById(R.id.appgame_download_manager_delete_select);
		TextView textview = (TextView) view.findViewById(R.id.appgame_download_manager_tip);

		String leftTip = getContext().getString(R.string.download_manager_dialog_tips_left);
		String rightTip = getContext().getString(R.string.download_manager_dialog_tips_right);
		Spanned str = Html.fromHtml("<font color=#202020>" + leftTip + "</font>"
				+ "<font color=#FF0000>" + mAdapter.getSelectCount() + "</font>"
				+ "<font color=#202020>" + rightTip + "</font>");
		textview.setText(str);
		final ImageView img = (ImageView) view
				.findViewById(R.id.appgame_download_manager_delete_checkbox);
		relativeLayout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsSelected) {
					img.setImageResource(R.drawable.apps_uninstall_not_selected);
					mIsSelected = false;
				} else {
					img.setImageResource(R.drawable.apps_uninstall_selected);
					mIsSelected = true;
				}
			}
		});
		Button button = (Button) view.findViewById(R.id.appgame_download_delete_dialog_ok);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapter != null && mAdapter.getSelectHashMap() != null) {
					ArrayList<Long> mRemoveIdList = new ArrayList<Long>();
					ArrayList<Long> mDeleteIdList = new ArrayList<Long>();
					HashMap<Long, Boolean> map = mAdapter.getSelectHashMap();
					if (mDownloadController == null) {
						return;
					}
					try {
						Iterator it = map.entrySet().iterator();
						while (it.hasNext()) {
							Entry entry = (Entry) it.next();
							if ((Boolean) entry.getValue()) {
								// 将任务按照“正在下载”和“下载完成”分成两大类
								if (mDownloadController.getDownloadTaskById((Long) entry.getKey()) != null) {
									mRemoveIdList.add((Long) entry.getKey());
								} else {
									mDeleteIdList.add((Long) entry.getKey());
								}
								it.remove();
							}
						}
						// 删除“正在下载”的任务
						if (mRemoveIdList != null && mRemoveIdList.size() > 0) {

							mDownloadController.removeDownloadTasksById(mRemoveIdList);
							if (mIsSelected) {
								for (Long id : mRemoveIdList) {
									for (DownloadTask task : mDownloadingList) {
										if (id == task.getId()) {
											FileUtil.deleteFile(task.getSaveFilePath() + ".tmp");
										}
									}
								}
							}
						}
						//删除“下载完成”的任务
						if (mDeleteIdList != null && mDeleteIdList.size() > 0) {
							for (Long id : mDeleteIdList) {
								mDownloadController.removeDownloadCompleteItem(id);
							}
							if (mIsSelected) {
								for (Long id : mDeleteIdList) {
									for (DownloadTask task : mDownloadedList) {
										if (id == task.getId()) {
											FileUtil.deleteFile(task.getSaveFilePath());
										}
									}
									for (DownloadTask task : mInstalledList) {
										if (id == task.getId()) {
											FileUtil.deleteFile(task.getSaveFilePath());
										}
									}
								}
							}
						}

						// 统计代码
						if (mIsSelected) {
							StatisticsData.countStatData(getContext(),
									StatisticsData.KEY_DELETE_FILE);
						} else {
							StatisticsData.countStatData(getContext(),
									StatisticsData.KEY_NO_DELETE_FILE);
						}

						mHandler.sendEmptyMessage(AppsDownloadActivity.MSG_UPDATE_LIST);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				mDialog.dismiss();
			}
		});
		Button cancelBtn = (Button) view.findViewById(R.id.appgame_download_delete_dialog_cancel);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mDialog.dismiss();
			}
		});
		return view;
	}
	/**
	 * <br>功能简述:更新提示
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void changeTip() {
		if (mAdapter == null || (mAdapter != null && mAdapter.getSelectCount() == 0)) {
			mDeleteBtn.setBackgroundResource(R.drawable.yjzi_btn_disable);
			mDeleteBtn.setTextColor(getContext().getResources().getColor(
					R.color.appgame_download_btn_black));
			mDeleteBtn.setOnClickListener(null);
			mTitleTextView.setText(R.string.select_group_applications);
			// 可勾选项数为0，所以全选按钮为没有勾选的状态
			mSelectAllBtn.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
		} else {
			mDeleteBtn.setBackgroundResource(R.drawable.yzjz_white_button);
			mDeleteBtn.setTextColor(getContext().getResources().getColor(
					R.color.downloadmanager_text_red));
			mDeleteBtn.setOnClickListener(this);
			int count = mAdapter.getSelectCount();
			String result = String.format(getContext().getString(R.string.selected_apps_result),
					count);
			mTitleTextView.setText(result);
			if (mAdapter.getSelectHashMap().size() == count && count != 0) {
				mSelectAllBtn.setBackgroundResource(R.drawable.apps_uninstall_selected);
			} else {
				mSelectAllBtn.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position >= 0 && position < mAdapter.getCount()) {
			DownloadTask task = (DownloadTask) mAdapter.getItem(position);
			if (task == null) {
				return;
			}
			long taskId = task.getId();
			HashMap<Long, Boolean> map = mAdapter.getSelectHashMap();
			boolean isSelected = false;
			if (map != null && map.containsKey(taskId)) {
				isSelected = map.get(taskId);
			} else {
				return;
			}
			map.put(taskId, !isSelected);
			mAdapter.notifyDataSetChanged();
			changeTip();
		}
	}
}
