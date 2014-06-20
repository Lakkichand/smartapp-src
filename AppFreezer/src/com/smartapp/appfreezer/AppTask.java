package com.smartapp.appfreezer;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 冻结和解冻应用的task
 * 
 * @author xiedezhi
 * 
 */
public class AppTask extends AsyncTask<Object, Integer, Void> {

	private Context mContext;
	/**
	 * 需要冻结/解冻的应用
	 */
	private List<ListDataBean> mList;
	/**
	 * 冻结或者解冻
	 */
	private int mAction;
	/**
	 * 成功的个数
	 */
	private int mSuccessfulTaskCount;
	/**
	 * 进度对话框
	 */
	private AlertDialog mProgressDialog;
	/**
	 * 进度条
	 */
	private ProgressBar mProgressBar;
	/**
	 * 进度信息
	 */
	private TextView mMessageView;
	/**
	 * 进度百分比
	 */
	private TextView mProgressPercent;
	/**
	 * 用于向MainActivity发送消息
	 */
	private Handler mHandler;

	public AppTask(Context context, int action, Handler handler) {
		mContext = context;
		mAction = action;
		mSuccessfulTaskCount = 0;
		mHandler = handler;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		Log.e("TEST", "onPreExecute");
	}

	@Override
	protected Void doInBackground(Object... params) {
		Log.e("TEST", "doInBackground");
		mList = (List<ListDataBean>) params[0];
		if (AppFreezer.isCollectionEmpty(mList)) {
			return null;
		}
		final int taskCount = mList.size();
		for (int i = 0; i < taskCount; i++) {
			if (isCancelled()) {
				break;
			}
			publishProgress(i);
			final String packageName = mList.get(i).mInfo.packageName;
			boolean result = mAction == MainActivity.ACTION_FREEZE_APPS ? AppFreezer
					.disablePackage(mContext, packageName) : AppFreezer
					.enablePackage(mContext, packageName);
			if (result) {
				mSuccessfulTaskCount++;
			}
		}
		onPostProcess();
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		final int index = values[0];
		final int max = mList.size();
		if (mProgressDialog == null) {
			createProgressDialog(max);
		}
		if (!mProgressDialog.isShowing()) {
			mProgressDialog.show();
			mProgressDialog.getWindow().setLayout(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
		}
		if (!AppFreezer.isCollectionEmpty(mList)) {
			mMessageView.setText(mList.get(index).mAppName);
			mProgressPercent.setText(String.format("%1d/%2d", index, max));
			mProgressBar.setProgress(index);
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
	}

	/**
	 * 创建进度对话框
	 */
	private void createProgressDialog(int maxValue) {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		View view = inflater.inflate(R.layout.freeze_app_progress, null);
		mProgressDialog = new AlertDialog.Builder(mContext)
				.setTitle(
						mAction == MainActivity.ACTION_FREEZE_APPS ? R.string.freezing_apps
								: R.string.unfreezing_apps)
				.setPositiveButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								cancel(false);
							}
						}).setCancelable(false).setView(view).create();

		mProgressBar = (ProgressBar) view.findViewById(R.id.progress);
		mProgressBar.setMax(maxValue);
		mMessageView = (TextView) view.findViewById(R.id.title);
		mProgressPercent = (TextView) view.findViewById(R.id.progress_number);
	}

	private void dismissDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
	}

	private void showResultToast() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(
						mContext,
						mContext.getString(mAction == MainActivity.ACTION_FREEZE_APPS ? R.string.freeze_app_result
								: R.string.unfreeze_app_result)
								+ " "
								+ mSuccessfulTaskCount
								+ " "
								+ mContext.getString(R.string.apps),
						Toast.LENGTH_LONG).show();
			}
		});
	}

	/**
	 * 刷新列表
	 */
	private void refreshAppList() {
		mHandler.sendEmptyMessage(MainActivity.MSG_REFRESH_LIST);
	}

	/**
	 * 完成
	 */
	private void onPostProcess() {
		showResultToast();
		dismissDialog();
		refreshAppList();
	}

}
