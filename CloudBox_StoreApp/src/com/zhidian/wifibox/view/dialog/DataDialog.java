package com.zhidian.wifibox.view.dialog;

import java.io.File;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.util.TALogger;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.FileHttpResponseHandler;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;

/**
 * 加载对话框
 * 
 * @author zhaoyl
 * 
 */
public class DataDialog extends Dialog {

	private MainActivity mActivity;
	private boolean isMust;
	private TextView mNewVersion, mUpdateMessage, appSize;
	private Button updateButton, cancelButton;
	private String url;

	private NotificationManager mNotificationManager;
	private Notification notification;

	private DataDialog(Context context) {
		super(context, R.style.Dialog);
		setContentView(R.layout.dialog_update_message);
		mNewVersion = (TextView) findViewById(R.id.new_version);
		appSize = (TextView) findViewById(R.id.app_size_tv);
		mUpdateMessage = (TextView) findViewById(R.id.new_function_tv);
		updateButton = (Button) findViewById(R.id.dialog_update);
		cancelButton = (Button) findViewById(R.id.dialog_cancel);
		updateButton
				.setOnClickListener(new android.view.View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
						// 添加下载任务
						AsyncHttpClient syncHttpClient = TAApplication
								.getApplication().getAsyncHttpClient();
						final FileHttpResponseHandler fHandler = new FileHttpResponseHandler(
								DownloadUtil.getCApkFileFromUrl(url)) {

							@Override
							public void onProgress(long totalSize,
									long currentSize, long speed, long time, long aveSpeed) {
								super.onProgress(totalSize, currentSize, speed,
										time, aveSpeed);
								int percent = 0;
								try {
									percent = (int) ((currentSize / 1024 * 100) / (totalSize / 1024));
								} catch (Exception e) {
									e.printStackTrace();
								}
								TALogger.e(mActivity, totalSize
										+ "------------  " + percent + "%"
										+ "  --------" + speed);
								// 设置通知的icon
								notification.icon = R.drawable.icon;
								// 设置通知的时间
								notification.when = System.currentTimeMillis();
								notification.tickerText = "开始升级";
								notification.contentView = new RemoteViews(
										mActivity.getPackageName(),
										R.layout.notification);
								notification.contentView.setTextViewText(
										R.id.down_tv, "正在下载");
								notification.contentView.setTextViewText(
										R.id.app_name,
										"升级"
												+ mActivity
														.getString(R.string.app_name));
								notification.contentView.setTextViewText(
										R.id.percent_tv, percent + "%");
								notification.contentView.setProgressBar(
										R.id.pb, 100, percent, false);
								Bitmap bm = ((BitmapDrawable) mActivity
										.getResources().getDrawable(
												R.drawable.icon)).getBitmap();
								notification.contentView.setImageViewBitmap(
										R.id.myicon, bm);
								Intent intent = new Intent(mActivity,
										MainActivity.class);
								PendingIntent pendingIntent = PendingIntent
										.getActivity(
												mActivity,
												0,
												intent,
												PendingIntent.FLAG_CANCEL_CURRENT);
								notification.contentIntent = pendingIntent;
								notification.flags |= Notification.FLAG_AUTO_CANCEL;
								// 4.发送通知
								mNotificationManager.notify(100223,
										notification);
							}

							@Override
							public void onSuccess(byte[] binaryData) {
								super.onSuccess(binaryData);
								mNotificationManager.cancel(100223);
								File file = new File(DownloadUtil
										.getCApkFileFromUrl(url));
								if (!FileUtil.isSDCardAvaiable()) {
									try {
										DownloadUtil.chmod(file.getParentFile()
												.getParentFile());
										DownloadUtil.chmod(file.getParentFile());
										DownloadUtil.chmod(file);
									} catch (Throwable e) {
										e.printStackTrace();
									}
								}
								Intent intent = new Intent();
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.setAction(android.content.Intent.ACTION_VIEW);
								intent.setDataAndType(Uri.fromFile(file),
										"application/vnd.android.package-archive");
								mActivity.startActivity(intent);
								mActivity.finish();
								TALogger.e(mActivity, file.getAbsolutePath());
							}

							@Override
							public void onFailure(Throwable error,
									byte[] binaryData) {
								super.onFailure(error, binaryData);
								mNotificationManager.cancel(100223);
								if (error != null
										&& error.getMessage().contains("404")) {
									Toast.makeText(mActivity, "更新失败，文件找不到了",
											Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(mActivity, "更新失败，请检查网络",
											Toast.LENGTH_SHORT).show();
								}
								if (isMust) {
									mActivity.finish();
								}
							}

						};

						syncHttpClient.download(url, fHandler);
					}
				});
		cancelButton
				.setOnClickListener(new android.view.View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
						if (isMust) {
							mActivity.finish();
						}
					}
				});
	}

	/**
	 * 
	 * @param context
	 * @param version
	 *            新版本号
	 * @param size
	 *            应用大小
	 * @param message
	 *            新功能描述
	 */
	public DataDialog(MainActivity activity, String version, String size,
			String message, String url, boolean isMust) {
		this(activity);
		mNotificationManager = (NotificationManager) activity
				.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notification = new Notification();
		mActivity = activity;
		mNewVersion.setText(version);
		appSize.setText(size);
		mUpdateMessage.setText(message);
		this.url = url;
		this.isMust = isMust;
		if (isMust) {
			cancelButton.setText(R.string.update_exit);
			setCancelable(false);
		} else {
			cancelButton.setText(R.string.update_later);
			setCancelable(true);
		}
	}
}
