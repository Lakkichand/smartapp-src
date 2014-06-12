package com.zhidian.wifibox.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 崩溃处理服务
 * 
 * @author xiedezhi
 * 
 */
public class CrashHandleService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String path = intent.getStringExtra("logpath");
		if (path != null) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					TAApplication.getApplication());
			dialog.setTitle("\"" + getResources().getString(R.string.app_name)
					+ "\"出问题了");
			dialog.setIcon(android.R.drawable.ic_dialog_info);
			dialog.setMessage("将问题发送给\"安卓装机大师\"团队，我们将更好地优化产品和服务");
			dialog.setNegativeButton("退出",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			dialog.setPositiveButton("发送",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							RequestParams params = new RequestParams();
							params.put("uuid",
									InfoUtil.getUUID(CrashHandleService.this));
							params.put("imei",
									InfoUtil.getIMEI(CrashHandleService.this));
							params.put("mac",
									InfoUtil.getBoxId(CrashHandleService.this));
							params.put("model", android.os.Build.MODEL);
							params.put("manufacturer",
									android.os.Build.MANUFACTURER);
							params.put("osVersion",
									android.os.Build.VERSION.SDK);
							params.put("marketVersion", InfoUtil
									.getVersionName(CrashHandleService.this));
							DateFormat formatter = new SimpleDateFormat(
									"yyyy年MM月dd日 HH时mm分ss秒 E");
							String time = formatter.format(new Date());
							params.put("inserttime", time);
							params.put("content",
									new String(FileUtil.getByteFromFile(path)));
							CDataDownloader.getPostData(
									CDataDownloader.getExceptionUrl(), params,
									new AsyncHttpResponseHandler() {
										@Override
										public void onSuccess(String content) {
											try {
												JSONObject json = new JSONObject(
														content);
												if (json.optInt("statusCode",
														-1) == 0) {
													Toast.makeText(
															CrashHandleService.this,
															"发送成功",
															Toast.LENGTH_SHORT)
															.show();
												} else {
													Toast.makeText(
															CrashHandleService.this,
															"发送失败",
															Toast.LENGTH_SHORT)
															.show();
												}
											} catch (JSONException e) {
												e.printStackTrace();
												Toast.makeText(
														CrashHandleService.this,
														"发送失败",
														Toast.LENGTH_SHORT)
														.show();
											}
										}

										@Override
										public void onStart() {
										}

										@Override
										public void onFailure(Throwable error) {
											Toast.makeText(
													CrashHandleService.this,
													"发送失败", Toast.LENGTH_SHORT)
													.show();
										}

										@Override
										public void onFinish() {
										}

									});
							Toast.makeText(CrashHandleService.this, "正在发送...",
									Toast.LENGTH_SHORT).show();
						}
					});
			AlertDialog mDialog = dialog.create();
			mDialog.getWindow().setType(
					WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);// 设定为系统级警告，关键
			mDialog.show();
		}
		return START_NOT_STICKY;
	}
}
