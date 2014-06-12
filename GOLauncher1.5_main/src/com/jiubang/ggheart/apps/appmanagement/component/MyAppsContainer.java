package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.jiubang.core.framework.ICleanable;

public class MyAppsContainer extends LinearLayout implements ICleanable {

	private final static String SIZE_INFOR_FORMAT_STRING = "%1$s/%2$s";
	private final static int PERCENTAGE_OF_50 = 50; // 50%
	private final static int PERCENTAGE_OF_80 = 80; // 80%

	private Context mContext = null;
	private LinearLayout mProgressLinearLayout; // 进度条
	private MyAppsView mPhoneListView = null;
	// private AppsUninstallView mSDCardListView = null;
	private LinearLayout mSDCardLayout = null; // sdcard布局
	private TextView mInternalSize = null; // 手机内存 可用/总量
	private TextView mSDCardSize = null; // sdcard内存 可用/总量
	private ProgressBar mInternalBarGreen = null; // 手机内存显示进度条(绿色)
	private ProgressBar mInternalBarOrange = null; // 手机内存显示进度条（橙色）
	private ProgressBar mInternalBarRed = null; // 手机内存显示进度条（红色）
	private ProgressBar mSDCardBarGreen = null; // SD卡内存显示进度条（绿色）
	private ProgressBar mSDCardBarOrange = null; // SD卡内存显示进度条（橙色）
	private ProgressBar mSDCardBarRed = null; // SD卡内存显示进度条（红色）
	private TextView mNoSdcardInfo = null; // sdcard不可用时提示信息

	public MyAppsContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
	}

	public MyAppsContainer(Context context) {
		super(context);

		mContext = context;

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		initView();
	}

	private void initView() {
		mPhoneListView = (MyAppsView) this.findViewById(R.id.phone_list);
		mPhoneListView.setAppListState(MyAppsView.APP_IN_PHONE);
		mPhoneListView.setViewtype(MyAppsView.VIEW_TYPE_APPS);
		mPhoneListView.initView();
		mPhoneListView.setSaveEnabled(false);
		// mSDCardListView =
		// (MyAppsView)this.findViewById(R.id.sdcard_list);
		// mSDCardListView.setAppListState(MyAppsView.APP_IN_SDCARD);
		// mSDCardListView.init();
		mProgressLinearLayout = (LinearLayout) this.findViewById(R.id.app_list_progress);
		initMemoryState();

		// 显示进度条，加载数据
		execMonitorTask();
	}

	// 初始化底部内存信息
	private void initMemoryState() {
		mSDCardLayout = (LinearLayout) this.findViewById(R.id.sdcard_memory);
		// if (!Machine.isSDCardExist()) {
		// // 如果没有sdcard，则不显示该进度条
		// mSDCardLayout.setVisibility(GONE);
		// }

		mInternalSize = (TextView) this.findViewById(R.id.internal_size);
		mSDCardSize = (TextView) this.findViewById(R.id.sdcard_size);

		mInternalBarGreen = (ProgressBar) this.findViewById(R.id.internal_storage_green);
		mInternalBarOrange = (ProgressBar) this.findViewById(R.id.internal_storage_orange);
		mInternalBarRed = (ProgressBar) this.findViewById(R.id.internal_storage_red);
		mSDCardBarGreen = (ProgressBar) this.findViewById(R.id.sdcard_storage_green);
		mSDCardBarOrange = (ProgressBar) this.findViewById(R.id.sdcard_storage_orange);
		mSDCardBarRed = (ProgressBar) this.findViewById(R.id.sdcard_storage_red);
		mNoSdcardInfo = (TextView) this.findViewById(R.id.no_sdcard_info);

		loadMemoryData();
	}

	// 读取手机内存和sd卡内存信息的方法
	private void loadMemoryData() {
		loadInternalData();
		showSDCardInfo();
	}

	private void showSDCardInfo() {
		if (Machine.isSDCardExist()) {
			loadSDCardData();

			if (mNoSdcardInfo.getVisibility() == View.VISIBLE) {
				mNoSdcardInfo.setVisibility(View.GONE);
			}

			if (mSDCardSize.getVisibility() == View.GONE) {
				mSDCardSize.setVisibility(View.VISIBLE);
			}
		} else {
			if (mSDCardLayout.getVisibility() == View.VISIBLE) {
				// mSDCardLayout.setVisibility(View.GONE);

				mSDCardBarGreen.setVisibility(GONE);
				mSDCardBarOrange.setVisibility(GONE);
				mSDCardBarRed.setVisibility(GONE);
				mSDCardSize.setVisibility(GONE);

				if (mNoSdcardInfo.getVisibility() == View.GONE) {
					mNoSdcardInfo.setVisibility(View.VISIBLE);
				}

				mNoSdcardInfo.setText(mContext.getResources().getString(
						R.string.apps_management_sdcard_no_exist));
			}
		}
	}

	private void loadInternalData() {
		long internAll = Machine.getTotalInternalMemorySize();
		long internUsed = internAll - Machine.getAvailableInternalMemorySize();

		// 手机已用内存
		String internAvalSize = ConvertUtils.convertSizeToString(internUsed,
				ConvertUtils.FORM_WITHOUT_DECIMAL);
		// 手机总内存
		String internAllSize = ConvertUtils.convertSizeToString(internAll,
				ConvertUtils.FORM_WITHOUT_DECIMAL);
		String phoneSizeFormat = String.format(SIZE_INFOR_FORMAT_STRING, internAvalSize,
				internAllSize);
		mInternalSize.setText(phoneSizeFormat);

		int internalPercentage = changeToPercentage(internUsed, internAll);
		if (internalPercentage <= PERCENTAGE_OF_50) {
			// 小于50%，进度条为绿色
			mInternalBarGreen.setVisibility(VISIBLE);
			mInternalBarOrange.setVisibility(GONE);
			mInternalBarRed.setVisibility(GONE);
			mInternalBarGreen.setProgress(internalPercentage);
		} else if (internalPercentage <= PERCENTAGE_OF_80) {
			// 大于50%，小于80%，进度条为橙色
			mInternalBarOrange.setVisibility(VISIBLE);
			mInternalBarGreen.setVisibility(GONE);
			mInternalBarRed.setVisibility(GONE);
			mInternalBarOrange.setProgress(internalPercentage);
		} else {
			// 大于80%，进度条为红色
			mInternalBarRed.setVisibility(VISIBLE);
			mInternalBarGreen.setVisibility(GONE);
			mInternalBarOrange.setVisibility(GONE);
			mInternalBarRed.setProgress(internalPercentage);
		}
	}

	private void loadSDCardData() {
		long sdCardAll = Machine.getTotalExternalMemorySize();
		long sdCardUsed = sdCardAll - Machine.getAvailableExternalMemorySize();

		// sdcard已用内存
		String sdcardAvalSize = ConvertUtils.convertSizeToString(sdCardUsed,
				ConvertUtils.FORM_WITHOUT_DECIMAL);
		// sdcard总内存
		String sdcardAllSize = ConvertUtils.convertSizeToString(sdCardAll,
				ConvertUtils.FORM_WITHOUT_DECIMAL);
		String sdSizeFormat = String
				.format(SIZE_INFOR_FORMAT_STRING, sdcardAvalSize, sdcardAllSize);
		mSDCardSize.setText(sdSizeFormat);

		int sdCardPercentage = changeToPercentage(sdCardUsed, sdCardAll);

		if (sdCardPercentage <= PERCENTAGE_OF_50) {
			// //小于50%，进度条为绿色
			mSDCardBarGreen.setVisibility(VISIBLE);
			mSDCardBarOrange.setVisibility(GONE);
			mSDCardBarRed.setVisibility(GONE);
			mSDCardBarGreen.setProgress(sdCardPercentage);
		} else if (sdCardPercentage <= PERCENTAGE_OF_80) {
			// 大于50%，小于80%，进度条为橙色
			mSDCardBarOrange.setVisibility(VISIBLE);
			mSDCardBarGreen.setVisibility(GONE);
			mSDCardBarRed.setVisibility(GONE);
			mSDCardBarOrange.setProgress(sdCardPercentage);
		} else {
			// 大于80%，进度条为红色
			mSDCardBarRed.setVisibility(VISIBLE);
			mSDCardBarGreen.setVisibility(GONE);
			mSDCardBarOrange.setVisibility(GONE);
			mSDCardBarRed.setProgress(sdCardPercentage);
		}
	}

	// /**
	// * 刷新列表内容
	// */
	// public void updateList() {
	// mPhoneListView.updateList();
	// loadMemoryData();
	// }

	public void updateList(List<String> appSizeList) {
		// mPhoneListView.updateList(appSizeList);
		// 显示进度条，加载数据
		execMonitorTask();
		loadMemoryData();
	}

	private int changeToPercentage(long useSize, long allSize) {
		if (allSize > 0) {
			return (int) (useSize * 100 / allSize);
		} else {
			return 0;
		}
	}

	public void setHandler(Handler handler) {
		mPhoneListView.setHandler(handler);
	}

	@Override
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

	private InitMonitorTask mMonitorTask;

	private void execMonitorTask() {

		if (mPhoneListView != null) {
			mPhoneListView.showHeaderView();
		}
		// 加载数据
		if (mMonitorTask != null && mMonitorTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		if (mProgressLinearLayout.getVisibility() == View.GONE) {
			mProgressLinearLayout.setVisibility(View.VISIBLE);
		}

		if (mPhoneListView != null) {
			mPhoneListView.setVisibility(View.GONE);
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
				mPhoneListView.setVisibility(View.VISIBLE);
				mPhoneListView.refreshView();
			}
			mProgressLinearLayout.setVisibility(View.GONE);

		}

		@Override
		protected void onCancelled() {
			if (mPhoneListView != null) {
				mPhoneListView.cleanup();
			}
			super.onCancelled();
		}
	}
}
