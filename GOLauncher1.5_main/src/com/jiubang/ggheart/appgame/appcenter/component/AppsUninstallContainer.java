package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.root.Commander;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager.DataLoadCompletedListenter;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager.SortedAppInfo;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.components.DeskProgressDialog;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 
 * <br>
 * 类描述: 批量卸载页面 <br>
 * 功能详细描述:
 * 
 * @author zhoujun
 * @date [2012-10-18]
 */
public class AppsUninstallContainer extends LinearLayout {
	private Context mContext = null;

	private MyAppsView mPhoneListView = null;

	private TextView mTitleText = null; // title文本
	// private ImageView mTitleBackImg = null; // 返回按钮
	private ImageView mSelectedBtn = null; // 选择按钮
	private Button mUninstallBtn = null; // 卸载按钮
	private Button mCancelBtn = null; // 取消按钮

	private String mTitleStr = ""; // title文本内容

	// private Handler mHandler = null; // 用于处理点击title返回事件
	// 选择状态
	public static final int SELECTED_STATE_NONE = 0; // 没选中
	public static final int SELECTED_STATE_PART = 1; // 选择部分
	public static final int SELECTED_STATE_ALL = 2; // 全选
	// 状态
	private int mSelectState = SELECTED_STATE_NONE;
	// 是否以ROOT卸载
	public boolean mIsRoot = false;
	// Loading等待界面
	private ProgressDialog mProgressDialog = null;
	private MyAppsDataManager mAppDataManager;

	public AppsUninstallContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mAppDataManager = MyAppsDataManager.getInstance(context);
	}

	public AppsUninstallContainer(Context context) {
		super(context);

		mContext = context;
		mAppDataManager = MyAppsDataManager.getInstance(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initView();
	}

	private void initView() {
		// title back
		// mTitleBackImg = (ImageView)
		// this.findViewById(R.id.apps_uninstall_title_imageView_back);
		// mTitleBackImg.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // 关闭批量卸载VIEW前，要将已选中的数据状态设置为未选中
		// mPhoneListView.setAllAppsSelectState(false);
		// back();
		// }
		// });

		// title内容
		mTitleText = (TextView) this
				.findViewById(R.id.apps_uninstall_title_textView);
		mTitleStr = mContext.getString(R.string.selected_apps_result);

		// 选择按钮
		mSelectedBtn = (ImageView) this
				.findViewById(R.id.apps_uninstall_title_imageView_select);
		setChooseBtnState(SELECTED_STATE_NONE);
		mSelectedBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectBtnClicked();
			}
		});

		// 列表
		mPhoneListView = (MyAppsView) this.findViewById(R.id.phoneapps_list);
		mPhoneListView.setAppListState(MyAppsView.APP_IN_PHONE);
		mPhoneListView.setViewtype(MyAppsView.VIEW_TYPE_APPS_UNINSTALL);
		mPhoneListView.setContainerHandler(mMyHandler);
		mPhoneListView.initView();
		mPhoneListView.setSaveEnabled(false);

		// 卸载按钮
		mUninstallBtn = (Button) this.findViewById(R.id.apps_uninstall_btn);
		mUninstallBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StatisticsData.countStatData(mContext,
						StatisticsData.KEY_BATCH_UNINSTALL);
				uninstallSure();
			}
		});

		// 取消
		mCancelBtn = (Button) this.findViewById(R.id.apps_uninstall_cancel);
		mCancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 取消前，要将已选中的数据状态设置为未选中
				mPhoneListView.setAllAppsSelectState(false);
				back();
			}
		});

		SortedAppInfo sortedAppInfo = mAppDataManager.getData(mDataListenter);

		if (sortedAppInfo == null) {
			// 如果没有数据，显示进度条
		} else {
			mPhoneListView.showHeaderView();
			mPhoneListView.setData(sortedAppInfo);
		}

		initViewAttribute();
	}

	/**
	 * 设置选择按钮样式
	 * 
	 * @param statecode
	 */
	private void setChooseBtnState(int statecode) {
		switch (statecode) {
		case SELECTED_STATE_NONE:
			mSelectedBtn
					.setImageResource(R.drawable.apps_uninstall_not_selected);
			mSelectState = SELECTED_STATE_NONE;
			break;
		case SELECTED_STATE_PART:
			mSelectedBtn
					.setImageResource(R.drawable.apps_uninstall_selected_part);
			mSelectState = SELECTED_STATE_PART;
			break;
		case SELECTED_STATE_ALL:
			mSelectedBtn.setImageResource(R.drawable.apps_uninstall_selected);
			mSelectState = SELECTED_STATE_ALL;
			break;
		default:
			break;
		}
	}

	/**
	 * 返回
	 * 
	 * @author zhaojunjie
	 */
	public void back() {
		AppsManagementActivity.sendHandler(this,
				IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.REMOVE_UNINSTALL_APP_VIEW, 0, null, null);
	}

	/**
	 * 响应全选按钮状态被改变
	 * 
	 * @author zhaojunjie
	 */
	private Handler mMyHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SELECTED_STATE_NONE:
				// 钩选框
				setChooseBtnState(SELECTED_STATE_NONE);
				// 标题
				if (mContext != null) {
					mTitleText.setText(mContext
							.getString(R.string.select_group_applications));
				}
				// 确认按钮
				mUninstallBtn.setTextColor(Color.parseColor("#7B7B7B"));
				mUninstallBtn.setEnabled(false);

				mUninstallBtn
						.setBackgroundResource(R.drawable.yjzi_btn_disable);
				// mUninstallBtn.setTextColor(mContext.getResources().getColor(
				// R.color.appgame_download_btn_black));
				// mUninstallBtn.setEnabled(false);
				break;
			case SELECTED_STATE_PART:
				setChooseBtnState(SELECTED_STATE_PART);
				// 改变标题
				String result = String.format(mTitleStr, msg.arg1);
				mTitleText.setText(result);
				mUninstallBtn.setTextColor(Color.parseColor("#FF0000"));
				mUninstallBtn
						.setBackgroundResource(R.drawable.yzjz_white_button);
				// mUninstallBtn.setTextColor(mContext.getResources().getColor(android.R.color.white));
				mUninstallBtn.setEnabled(true);
				break;
			case SELECTED_STATE_ALL:
				setChooseBtnState(SELECTED_STATE_ALL);
				// 改变标题
				String resultall = String.format(mTitleStr, msg.arg1);
				mTitleText.setText(resultall);
				mUninstallBtn.setTextColor(Color.parseColor("#FF0000"));
				mUninstallBtn.setEnabled(true);

				mUninstallBtn
						.setBackgroundResource(R.drawable.yzjz_white_button);
				// mUninstallBtn.setTextColor(mContext.getResources().getColor(android.R.color.white));
				// mUninstallBtn.setEnabled(true);
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 点击选择按钮
	 * 
	 * @author zhaojunjie
	 */
	private void selectBtnClicked() {
		switch (mSelectState) {
		case SELECTED_STATE_NONE:
			mPhoneListView.setAllAppsSelectState(true);
			break;
		case SELECTED_STATE_PART:
			mPhoneListView.setAllAppsSelectState(true);
			break;
		case SELECTED_STATE_ALL:
			mPhoneListView.setAllAppsSelectState(false);
			break;
		default:
			break;
		}
	}

	/**
	 * 是否以ROOT卸载
	 * 
	 * @param b
	 */
	public void setIsRootUninstall(boolean b) {
		mIsRoot = b;
	}

	/**
	 * 卸载确认
	 */
	private void uninstallSure() {
		new AlertDialog.Builder(mContext)
				.setTitle(mContext.getString(R.string.appsuninstall_btn))
				.setMessage(mContext.getString(R.string.appsuninstall_sure))
				.setPositiveButton(mContext.getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// 确定按钮事件
								uninstallApps();
							}
						})
				.setNegativeButton(mContext.getString(R.string.cancle),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).show();
	}

	/**
	 * 批量卸载应用
	 */
	private void uninstallApps() {
		if (mIsRoot) {
			showProgressDialog();
			execUninstallMonitorTask();
		} else {
			Toast.makeText(mContext,
					mContext.getString(R.string.appsuninstall_no_root_message),
					Toast.LENGTH_SHORT).show();
			ArrayList<String> appsPkgNames = mPhoneListView.getSelectApps();
			// 取消所有选择状态
			mPhoneListView.setAllAppsSelectState(false);
			for (String pkgname : appsPkgNames) {
				AppsManagementActivity.sendHandler(this,
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.APPS_MANAGEMENT_UNINSTALL_APP, 0, pkgname,
						null);
			}
			updateList(null);
			mTitleText.setText(mContext
					.getString(R.string.select_group_applications));
		}
	}

	/**
	 * 初始化控件属性
	 */
	private void initViewAttribute() {
		mTitleText.setText(mContext
				.getString(R.string.select_group_applications));
		setChooseBtnState(SELECTED_STATE_NONE);
		mUninstallBtn.setTextColor(Color.parseColor("#7B7B7B"));
		mUninstallBtn.setEnabled(false);
		mUninstallBtn.setBackgroundResource(R.drawable.yjzi_btn_disable);
		// mUninstallBtn.setTextColor(mContext.getResources().getColor(
		// R.color.appgame_download_btn_black));
		// mUninstallBtn.setEnabled(false);
	}

	/**
	 * 等待界面
	 */
	private void showProgressDialog() {
		mProgressDialog = DeskProgressDialog.show(mContext, null,
				mContext.getString(R.string.appsuninstall_uninstalling), true);
	}

	/**
	 * 关闭等待界面
	 */
	private void dismissProgressDialog() {
		if (mProgressDialog != null) {
			try {
				((DeskProgressDialog) mProgressDialog).selfDestruct();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mProgressDialog = null;
		}
	}

	public void updateList(List<String> appSizeList) {
		// mPhoneListView.updateList(appSizeList);
		initViewAttribute();
		// 显示进度条，加载数据
		// execMonitorTask();
	}

	// private int changeToPercentage(long useSize, long allSize) {
	// if (allSize > 0) {
	// return (int) (useSize * 100 / allSize);
	// } else {
	// return 0;
	// }
	// }

	// public void setHandler(Handler handler) {
	// mHandler = handler;
	// // mPhoneListView.setHandler(handler);
	// }

	public void cleanup() {
		if (mAppDataManager != null) {
			mAppDataManager.removeDataLoadComletedListenter(mDataListenter);
			mDataListenter = null;
		}

		if (mContext != null) {
			mContext = null;
		}

		if (mPhoneListView != null) {
			mPhoneListView.cleanup();
			mPhoneListView.setAdapter(null);
			mPhoneListView = null;
		}

	}

	private DataLoadCompletedListenter mDataListenter = new DataLoadCompletedListenter() {

		@Override
		public void loadCompleted(SortedAppInfo sortedAppInfo) {
			mPhoneListView.showHeaderView();
			mPhoneListView.setData(sortedAppInfo);
		}

	};

	// 异步卸载
	private UninstallMonitorTask mUninstallMonitorTask;

	/**
	 * 异步任务，卸载应用程序
	 */
	private void execUninstallMonitorTask() {

		if (mPhoneListView != null) {
			mPhoneListView.showHeaderView();
		}
		// 加载数据
		if (mUninstallMonitorTask != null
				&& mUninstallMonitorTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}

		mUninstallMonitorTask = new UninstallMonitorTask();
		mUninstallMonitorTask.execute();
	}

	/**
	 * 异步任务，卸载应用程序
	 */
	private class UninstallMonitorTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (mPhoneListView != null) {
				ArrayList<String> appsPkgNames = mPhoneListView.getSelectApps();
				for (String pkgname : appsPkgNames) {
					boolean success = Commander.getInstance().exec(
							new String[] { "pm uninstall " + pkgname });
					if (!success) {
						// root执行失败时，调用系统的卸载界面
						AppsManagementActivity.sendHandler(this,
								IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
								IDiyMsgIds.APPS_MANAGEMENT_UNINSTALL_APP, 0,
								pkgname, null);
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mPhoneListView != null) {
				dismissProgressDialog();

				updateList(null);
				Toast.makeText(mContext,
						mContext.getString(R.string.appsuninstall_finish),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}

}
