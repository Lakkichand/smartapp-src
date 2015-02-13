package com.zhidian.wifibox.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;

/**
 * 离线模式，用户下载普版应用，提示用户跳到超速模式
 * 
 * @author xiedezhi
 * 
 */
public class OfflineDownloadDialog extends Dialog {

	public OfflineDownloadDialog(Context context, final DownloadTask task) {
		super(context, R.style.Dialog);
		setContentView(R.layout.switch_mode_dialog);
		TextView title = (TextView) findViewById(R.id.dialog_title);
		title.setText("下载提示");
		TextView msg = (TextView) findViewById(R.id.dialog_msg);
		msg.setText("无线路由享WIFI没有连接外网，去体验离线超速下载吧 ");
		TextView yes = (TextView) findViewById(R.id.dialog_update);
		yes.setText("确定");
		yes.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
				TAApplication.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
						IDiyMsgIds.SHOW_SPEEDING_DOWNLOAD, -1, null, null);
				// 删除任务
				Intent intent = new Intent(
						IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
				intent.putExtra("command",
						IDownloadInterface.REQUEST_COMMAND_DELETE);
				intent.putExtra("url", task.url);
				TAApplication.getApplication().sendBroadcast(intent);
			}
		});
		TextView no = (TextView) findViewById(R.id.dialog_cancel);
		no.setText("取消");
		no.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
				// 删除任务
				Intent intent = new Intent(
						IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
				intent.putExtra("command",
						IDownloadInterface.REQUEST_COMMAND_DELETE);
				intent.putExtra("url", task.url);
				TAApplication.getApplication().sendBroadcast(intent);
			}
		});
	}

}
