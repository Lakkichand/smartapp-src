package com.zhidian.wifibox.view.dialog;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;

/**
 * 删除下载任务提示框
 * 
 * @author xiedezhi
 * 
 */
public class DownloadDeleteDialog extends Dialog {

	private TextView mSelect;
	private Button mCancle;
	private Button mConfirm;
	private DownloadTask mTask;

	public DownloadDeleteDialog(Context context, DownloadTask task) {
		super(context, R.style.Dialog);
		setContentView(R.layout.download_delete_dialog);
		getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mTask = task;
		mSelect = (TextView) findViewById(R.id.select);
		mCancle = (Button) findViewById(R.id.delethit_dialog_cancle);
		mConfirm = (Button) findViewById(R.id.delethit_dialog_goon);
		mSelect.setCompoundDrawablesWithIntrinsicBounds(context.getResources()
				.getDrawable(R.drawable.cleanmaster_select), null, null, null);
		mSelect.setTag(true);
		mSelect.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Boolean b = (Boolean) v.getTag();
				if (b) {
					mSelect.setCompoundDrawablesWithIntrinsicBounds(
							v.getContext()
									.getResources()
									.getDrawable(
											R.drawable.cleanmaster_noselect),
							null, null, null);
				} else {
					mSelect.setCompoundDrawablesWithIntrinsicBounds(
							v.getContext().getResources()
									.getDrawable(R.drawable.cleanmaster_select),
							null, null, null);
				}
				mSelect.setTag(!b);
			}
		});
		mCancle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		mConfirm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Boolean b = (Boolean) mSelect.getTag();
				if (!b && FileUtil.isSDCardAvaiable()) {
					// 保留文件
					final File file = new File(DownloadUtil
							.getCApkFileFromUrl(mTask.url));
					final File file_ = new File(DownloadUtil
							.getCApkFileFromUrl(mTask.url) + ".tmpx");
					final File tFile = new File(DownloadUtil
							.getCTempApkFileFromUrl(mTask.url));
					final File tFile_ = new File(DownloadUtil
							.getCTempApkFileFromUrl(mTask.url) + ".tmpx");
					file_.delete();
					tFile_.delete();
					if (file.exists()) {
						FileUtil.copyFile(file.getAbsolutePath(),
								file_.getAbsolutePath());
					}
					if (tFile.exists()) {
						FileUtil.copyFile(tFile.getAbsolutePath(),
								tFile_.getAbsolutePath());
					}
					v.postDelayed(new Runnable() {

						@Override
						public void run() {
							file.delete();
							tFile.delete();
							if (file_.exists()) {
								FileUtil.copyFile(file_.getAbsolutePath(),
										file.getAbsolutePath());
								file_.delete();
							}
							if (tFile_.exists()) {
								FileUtil.copyFile(tFile_.getAbsolutePath(),
										tFile.getAbsolutePath());
								tFile_.delete();
							}
						}
					}, 100);
				}
				Intent intent = new Intent(
						IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
				intent.putExtra("command",
						IDownloadInterface.REQUEST_COMMAND_DELETE);
				intent.putExtra("url", mTask.url);
				TAApplication.getApplication().sendBroadcast(intent);
				dismiss();
			}
		});
		setCancelable(true);
	}

}
