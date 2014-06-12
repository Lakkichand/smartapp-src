package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageBaseBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 类描述: 弹出框消息内容入口类
 * 功能详细描述:
 * @date  [2012-9-28]
 */
public class MessageDialogContentActivity extends Activity implements OnClickListener {

	private MessageManager mManager;
	public final static String MODE_WEB = "webview";
	public final static int MSG_REMOVE_WEBLOADING_BAR = 1;
	private String mMsgId = null;
	private MessageCenterWebView mMsgCenterWebView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		String msgUrl = null;
		if (bundle != null) {
			mMsgId = bundle.getString("msgid");
			msgUrl = bundle.getString("msgurl");
		}
		if (mMsgId == null || msgUrl == null || msgUrl.equals("")) {
			finish();
			return;
		}
		
		setContentView(R.layout.message_center_webview);
		mManager = MessageManager.getMessageManager(GOLauncherApp.getContext());
		mMsgCenterWebView = (MessageCenterWebView) findViewById(R.id.webviewlayout);
		mMsgCenterWebView.initWebView(this);
		setUp();
	}

	private void setUp() {
		MessageHeadBean msgHeadBean = mManager.getMessageHeadBean(mMsgId);

		String str = MessageCenterActivity.compareDate(msgHeadBean.mMsgTimeStamp,
				this.getApplicationContext());
		mMsgCenterWebView.setTitleAndStamp(msgHeadBean.mTitle, str);
		mManager.markAsReaded(msgHeadBean);
		
		mMsgCenterWebView.setOriginalUrl(msgHeadBean.mUrl);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//		if (mManager != null) {
		//			mManager.setShowing(false);
		//		}
		mManager = null;
		
		if (mMsgCenterWebView != null) {
			mMsgCenterWebView.onDestory();
			mMsgCenterWebView = null;
		}
	}

	@Override
	public void onClick(View v) {
		
	}

	

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
				default :
					break;
			}
		}

	};
	
	public void sendRunnable(Runnable runnable) {
		 mHandler.post(runnable);
	}

	public void startDownLoad(String url) {
		//        final DownloadManager downloadManager = DownloadManager.getInstance(getApplicationContext());
		IDownloadService downloadController = GOLauncherApp.getApplication()
				.getDownloadController();

		final String msgId = mMsgId;

		String[] urlContent = url.split(MessageBaseBean.URL_SPLIT);
		if (urlContent.length < 2) {
			return ;
		}
		String[] nameContent = urlContent[1].split(MessageBaseBean.URL_SPLIT_NAME);
		String pkgName = nameContent[0];
		String appName = nameContent[1];              //显示在通知栏上面的下载app name

		DownloadTask downloadTask = new DownloadTask(Long.valueOf(msgId), url, appName, 0, 0,
				LauncherEnv.Path.MESSAGECENTER_PATH + msgId + ".apk", pkgName);
		//        // 设置标识一次下载的ID，各次下载均不相同
		//        downloadTask.setDownloadId(Long.valueOf(msgId));
		//        downloadTask.addDownloadListener(new MessageCenterDownloadListener(getApplicationContext()));
		//        downloadManager.startDownload(downloadTask);
		try {
			if (downloadController != null) {
				long taskId = downloadController.addDownloadTask(downloadTask);
				if (taskId != -1) {
					downloadController.addDownloadTaskListener(taskId,
							new MessageCenterDownloadListener(getApplicationContext()));
					downloadController.startDownload(taskId);
				}
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (mMsgCenterWebView != null) {
			if (mMsgCenterWebView.onKeyDown(keyCode, event)) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
