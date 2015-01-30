package com.zhidian.wifibox.view.dialog;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.download.DownloadService;
import com.zhidian.wifibox.download.IDownloadInterface;

/**
 * 切换极速模式WIFI的提示框
 * 
 * @author xiedezhi
 * 
 */
public class XNetDialog extends Dialog {

	public static XNetDialog sDialog;

	static {
		sDialog = new XNetDialog(TAApplication.getApplication());
		sDialog.getWindow().setType(
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	}

	public XNetDialog(Context context) {
		super(context, R.style.Dialog);
		setContentView(R.layout.switch_mode_dialog);
		TextView title = (TextView) findViewById(R.id.dialog_title);
		title.setText("网络提示：");
		TextView msg = (TextView) findViewById(R.id.dialog_msg);
		msg.setText("您已成功连接享WIFI，是否重新访问网络？");
		TextView yes = (TextView) findViewById(R.id.dialog_update);
		yes.setText("是");
		yes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
				if (!DownloadService.sIsRunning) {
					restart();
					return;
				}
				// 暂停所有下载任务
				Intent xi = new Intent(IDownloadInterface.PAUSE_ALL_TASK_ACTION);
				TAApplication.getApplication().sendBroadcast(xi);
				BroadcastReceiver receiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent i) {
						try {
							TAApplication.getApplication().unregisterReceiver(
									this);
						} catch (Exception e) {
							e.printStackTrace();
						}
						restart();
					}
				};
				IntentFilter filter = new IntentFilter();
				filter.addAction(IDownloadInterface.PAUSE_ALL_TASK_ACTION_COMPLETE);
				TAApplication.getApplication().registerReceiver(receiver,
						filter);
			}
		});
		TextView no = (TextView) findViewById(R.id.dialog_cancel);
		no.setText("否");
		no.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}

	/**
	 * 重启应用
	 */
	private void restart() {
		// 重启安卓装机大师
		Intent intent = new Intent(TAApplication.getApplication(),
				MainActivity.class);
		PendingIntent restartIntent = PendingIntent.getActivity(
				TAApplication.getApplication(), 0, intent,
				Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		// 退出程序
		AlarmManager mgr = (AlarmManager) TAApplication.getApplication()
				.getSystemService(Context.ALARM_SERVICE);
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
				restartIntent); // 1秒钟后重启应用
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(1);
	}
}
