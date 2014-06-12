package com.jiubang.ggheart.apps.appmanagement.component;

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
import com.jiubang.ggheart.components.DeskProgressDialog;

public class AppsUninstallContainer extends LinearLayout {
	private Context mContext = null;

	private MyAppsView mPhoneListView = null;

	private TextView mTitleText = null; // title文本
	private ImageView mTitleBackImg = null; // 返回按钮
	private ImageView mSelectedBtn = null; // 选择按钮
	private Button mUninstallBtn = null; // 卸载按钮
	private Button mCancelBtn = null; // 取消按钮

	private String mTitleStr = ""; // title文本内容

	private Handler mHandler = null; // 用于处理点击title返回事件
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

	public AppsUninstallContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
	}

	public AppsUninstallContainer(Context context) {
		super(context);

		mContext = context;

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		initView();
	}

	private void initView() {
		// title back
		mTitleBackImg = (ImageView) this.findViewById(R.id.apps_uninstall_title_imageView_back);
		mTitleBackImg.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 关闭批量卸载VIEW前，要将已选中的数据状态设置为未选中
				mPhoneListView.SetAllAppsSelectState(false);
				Back();
			}
		});

		// title内容
		mTitleText = (TextView) this.findViewById(R.id.apps_uninstall_title_textView);
		mTitleStr = mContext.getString(R.string.selected_apps_result);

		// 选择按钮
		mSelectedBtn = (ImageView) this.findViewById(R.id.apps_uninstall_title_imageView_select);
		SetChooseBtnState(SELECTED_STATE_NONE);
		mSelectedBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectBtnClicked();
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
				UninstallSure();
			}
		});

		// 取消
		mCancelBtn = (Button) this.findViewById(R.id.apps_uninstall_cancel);
		mCancelBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 取消前，要将已选中的数据状态设置为未选中
				mPhoneListView.SetAllAppsSelectState(false);
				Back();
			}
		});

		// 显示进度条，加载数据
		execMonitorTask();
	}

	/**
	 * 设置选择按钮样式
	 * 
	 * @param statecode
	 */
	private void SetChooseBtnState(int statecode) {
		switch (statecode) {
			case SELECTED_STATE_NONE :
				mSelectedBtn.setImageResource(R.drawable.apps_uninstall_not_selected);
				mSelectState = SELECTED_STATE_NONE;
				break;
			case SELECTED_STATE_PART :
				mSelectedBtn.setImageResource(R.drawable.apps_uninstall_selected_part);
				mSelectState = SELECTED_STATE_PART;
				break;
			case SELECTED_STATE_ALL :
				mSelectedBtn.setImageResource(R.drawable.apps_uninstall_selected);
				mSelectState = SELECTED_STATE_ALL;
				break;
			default :
				break;
		}
	}

	/**
	 * 返回
	 * 
	 * @author zhaojunjie
	 */
	public void Back() {
		Message msg = new Message();
		msg.what = AppsManagementActivity.BACK_TO_MAINVIEW;
		mHandler.sendMessage(msg);
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
				case SELECTED_STATE_NONE :
					// 钩选框
					SetChooseBtnState(SELECTED_STATE_NONE);
					// 标题
					mTitleText.setText(mContext.getString(R.string.select_group_applications));
					// 确认按钮
					mUninstallBtn.setTextColor(Color.parseColor("#7B7B7B"));
					mUninstallBtn.setEnabled(false);
					break;
				case SELECTED_STATE_PART :
					SetChooseBtnState(SELECTED_STATE_PART);
					// 改变标题
					String result = String.format(mTitleStr, msg.arg1);
					mTitleText.setText(result);
					mUninstallBtn.setTextColor(Color.parseColor("#FF0000"));
					mUninstallBtn.setEnabled(true);
					break;
				case SELECTED_STATE_ALL :
					SetChooseBtnState(SELECTED_STATE_ALL);
					// 改变标题
					String resultall = String.format(mTitleStr, msg.arg1);
					mTitleText.setText(resultall);
					mUninstallBtn.setTextColor(Color.parseColor("#FF0000"));
					mUninstallBtn.setEnabled(true);
					break;
				default :
					break;
			}
		}
	};

	/**
	 * 点击选择按钮
	 * 
	 * @author zhaojunjie
	 */
	private void SelectBtnClicked() {
		switch (mSelectState) {
			case SELECTED_STATE_NONE :
				mPhoneListView.SetAllAppsSelectState(true);
				break;
			case SELECTED_STATE_PART :
				mPhoneListView.SetAllAppsSelectState(true);
				break;
			case SELECTED_STATE_ALL :
				mPhoneListView.SetAllAppsSelectState(false);
				break;
			default :
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
	private void UninstallSure() {
		new AlertDialog.Builder(mContext)
				.setTitle(mContext.getString(R.string.appsuninstall_btn))
				.setMessage(mContext.getString(R.string.appsuninstall_sure))
				.setPositiveButton(mContext.getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								// 确定按钮事件
								UninstallApps();
							}
						})
				.setNegativeButton(mContext.getString(R.string.cancle),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						}).show();
	}

	/**
	 * 批量卸载应用
	 */
	private void UninstallApps() {
		if (mIsRoot) {
			showProgressDialog();
			execUninstallMonitorTask();
		} else {
			ArrayList<String> appsPkgNames = mPhoneListView.getSelectApps();
			for (String pkgname : appsPkgNames) {
				((AppsManagementActivity) mContext).addUninstallApp(pkgname);
			}
			updateList(null);
			mTitleText.setText(mContext.getString(R.string.select_group_applications));
		}
	}

	/**
	 * 初始化控件属性
	 */
	private void initViewAttribute() {
		mTitleText.setText(mContext.getString(R.string.select_group_applications));
		SetChooseBtnState(SELECTED_STATE_NONE);
		mUninstallBtn.setTextColor(Color.parseColor("#7B7B7B"));
		mUninstallBtn.setEnabled(false);
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
		execMonitorTask();
	}

	private int changeToPercentage(long useSize, long allSize) {
		if (allSize > 0) {
			return (int) (useSize * 100 / allSize);
		} else {
			return 0;
		}
	}

	public void setHandler(Handler handler) {
		mHandler = handler;
		mPhoneListView.setHandler(handler);
	}

	public void cleanup() {
		if (mMonitorTask != null) {
			mMonitorTask.cancel(true);
			mMonitorTask = null;
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

	// -------------------以下是异步任务代码---------------------------------
	private InitMonitorTask mMonitorTask;

	private void execMonitorTask() {

		if (mPhoneListView != null) {
			mPhoneListView.showHeaderView();
		}
		// 加载数据
		if (mMonitorTask != null && mMonitorTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}

		mMonitorTask = new InitMonitorTask();
		mMonitorTask.execute();
	}

	/**
	 * 异步任务，加载应用程序
	 */
	private class InitMonitorTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			if (mPhoneListView != null) {
				mPhoneListView.updateList(null);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (mPhoneListView != null) {
				mPhoneListView.refreshView();
			}

		}

		@Override
		protected void onCancelled() {
			if (mPhoneListView != null) {
				mPhoneListView.cleanup();
			}
			super.onCancelled();
		}
	}

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
						// AppsManagementActivity.sendHandler(this,
						// IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						// IDiyMsgIds.APPS_MANAGEMENT_UNINSTALL_APP, 0, pkgname,
						// null);
						((AppsManagementActivity) mContext).addUninstallApp(pkgname);
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
				Toast.makeText(mContext, mContext.getString(R.string.appsuninstall_finish),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
	}
}
