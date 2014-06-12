package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageBaseBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 类描述:  消息中心内容入口类
 * 功能详细描述:
 * @date  [2012-9-28]
 */
public class MessageContentActivity extends Activity
		implements
			OnClickListener,
			OnCancelListener,
			OnKeyListener {

	private MessageManager mManager;
	private final static int DIALOG_WAIT = 1;
//	private final static int GET_MSG_OK = 0x03;
//	private final static int GET_MSG_ERRO = 0x04;
	private final static int MSG_DOWNLOAD_CALLBACK = 0x05;
	private final static int MSG_DOWNLOAD_UPDATE_PROGRESS = 0x06;
	private final static int MSG_DOWNLOAD_FINISH = 0x07;
	private final static int MSG_DOWNLOAD_UPDATE_UI = 0X08;

	/**
	 * 接收DownloadManager返回的数据，更新UI
	 * 当用户退出消息中心而且消除通知栏时 ，用于接收DownManager返回的的下载task列表
	 */
	private BroadcastReceiver mDownloadReceiver = null;
	/**
	 * 监听程序安装，如果在当前界面有程序安装，则需要刷新一下底部的按钮的显示的文字
	 */
	private BroadcastReceiver mpackageAddReceiver = null;

	private ProgressBar mProgressBar = null;
	private TextView mTextProgress = null;
	private View mBtnDownload = null;
	private View mBtnCancel = null;

	private String mMsgId = null; //当前消息内容标识的ID

	private View mDownloadLayout = null;
	private IDownloadService mDownloadController;

	private ArrayList<DownloadTask> mDownloadTaskList = null;    //获取IDownloadService中的所有的下载列表
	private DownloadTask mCurrentDownloadTask = null;
	private final int mTextProgressID = 2;   //mTextProgress“正在下载”的标识ID ，用于让mProgressBar的代码布局找到的相对位置。
	private MessageCenterDownloadListener mMsgDownloadListener;
	
	private MessageCenterWebView mMsgCenterWebView = null;
	
	/**
	 * 是否已经绑定下载服务的标记
	 */
	private boolean mHasBindService = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.message_content);
		
		Bundle bundle = getIntent().getExtras();
		String id = null;
		String msgUrl = null;
		int fromWhere = -1;
		if (bundle != null) {
			id = bundle.getString("msgid");
			msgUrl = bundle.getString("msgurl");
			mMsgId = id;
			fromWhere = bundle.getInt("where", -1);
		}
		if (id == null || msgUrl == null) {
			finish();
			return;
		}
//		mDownloadController = GOLauncherApp.getApplication().getDownloadController();
		mDownloadTaskList = new ArrayList<DownloadTask>();
		mManager = MessageManager.getMessageManager(GOLauncherApp.getContext());
		
		mMsgCenterWebView = (MessageCenterWebView) findViewById(R.id.webviewlayout);
		mMsgCenterWebView.initWebView(this);
		MessageHeadBean msgHeadBean = mManager.getMessageHeadBean(mMsgId);
		if (msgHeadBean == null) {
			finish();
			return;
		}
		String str = MessageCenterActivity.compareDate(msgHeadBean.mMsgTimeStamp,
				this.getApplicationContext());
		mMsgCenterWebView.setTitleAndStamp(msgHeadBean.mTitle, str);
		
		if (fromWhere == MessageBaseBean.VIEWTYPE_STATUS_BAR) {
			Bitmap bitmap = msgHeadBean.mBitmap;
			if (bitmap != null && !bitmap.isRecycled()) {
				msgHeadBean.mBitmap.recycle();
				msgHeadBean.mBitmap = null;
			}
			mManager.setCurrentMsgId(msgHeadBean.mId);
			mManager.markAsReaded(msgHeadBean);
			mManager.setEntrance(MessageBaseBean.VIEWTYPE_STATUS_BAR);
			mManager.saveClickStatisticsData(msgHeadBean.mId);
			Vector<MessageHeadBean> statisticsMsgs = new Vector<MessageHeadBean>();
			statisticsMsgs.add(msgHeadBean);
			mManager.updateStatisticsData(statisticsMsgs, fromWhere, 0, IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES, null);
		}
		
		//设置消息标题
		TextView title = (TextView) findViewById(R.id.topbar_title);
		title.setText(msgHeadBean.mTitle);
		title.setSingleLine(true);
		title.setEllipsize(TruncateAt.MARQUEE);
		title.setMarqueeRepeatLimit(-1);
		title.setFocusableInTouchMode(true);

		findViewByIds();
		setProgressBarUI();

		setdownloadBtnListener();
		getDownloadListByIDownloadService();
		initDownloadReceiver();
		packageAddReceiver();
		
		mMsgCenterWebView.setOriginalUrl(msgUrl);
		
		// 先启动下载服务
		GOLauncherApp.getContext().startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
		// 再bind服务
		if (!mHasBindService) {
			mHasBindService = GOLauncherApp.getContext().bindService(
					new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), mConnenction,
					Context.BIND_AUTO_CREATE);
		}
	}

	private void findViewByIds() {
		mDownloadLayout = findViewById(R.id.messagecenter_download_layout);
		mBtnDownload = findViewById(R.id.download_control);
		mBtnCancel = findViewById(R.id.download_cancle);
		mTextProgress = (TextView) findViewById(R.id.messagecenter_download_percent);
		mProgressBar = (ProgressBar) findViewById(R.id.messagecenter_download_progress);
		mTextProgress.setId(mTextProgressID);    //mTextProgress“正在下载”的标识ID ，用于让mProgressBar的代码布局找到的相对位置。
		LinearLayout backbtnArea = (LinearLayout) findViewById(R.id.back_btnArea);
		backbtnArea.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}

		});
	}
	
	public void sendRunnable(Runnable runnable) {
		 mHandler.post(runnable);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (mMsgCenterWebView != null) {
			mMsgCenterWebView.onDestory();
		}

		if (mDownloadReceiver != null) {
			unregisterReceiver(mDownloadReceiver);

		}
		if (mpackageAddReceiver != null) {
			unregisterReceiver(mpackageAddReceiver);
		}
		
		// 解除下载服务的绑定
		if (mHasBindService) {
			GOLauncherApp.getContext().unbindService(mConnenction);
			GOLauncherApp.getApplication().setDownloadController(null);
			mHasBindService = false;
		}
		mManager = null;
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		Dialog dialog = null;
		if (id == DIALOG_WAIT) {
			dialog = ProgressDialog.show(this, "", getString(R.string.msgcenter_dialog_wait_msg),
					true);
			dialog.setOnCancelListener(this);
			dialog.setOnKeyListener(this);
		}
		return dialog;
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
			removeDialog(DIALOG_WAIT);
			if (mManager != null) {
				mManager.abortPost();
			}
			return true;
		}
		return false;
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
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		if (mManager != null) {
			mManager.abortPost();
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
				case MSG_DOWNLOAD_CALLBACK :
					if (msg.obj != null) {
						notifyDownloadState((DownloadTask) msg.obj);
					}
					break;
				case MSG_DOWNLOAD_UPDATE_PROGRESS :
					updateProgressBar(msg.arg1);
					break;
				case MSG_DOWNLOAD_FINISH :
					downLoadFinish();
					break;
				case MSG_DOWNLOAD_UPDATE_UI :
					if (mCurrentDownloadTask != null) {
						showDownloadUI();
						updateProgressBar(mCurrentDownloadTask.getAlreadyDownloadPercent());
					}
					break;
				default :
					break;
			}
		}

	};

	public void startDownLoad(String url) {
//		final String msgId = mManager.getMsgId();
		final String msgId = mMsgId;
		String[] urlContent = url.split(MessageBaseBean.URL_SPLIT);
		if (urlContent.length != 2) {
			return;
		}
		String[] nameContent = urlContent[1].split(MessageBaseBean.URL_SPLIT_NAME);
		if (nameContent.length != 2) {
			return;
		}
		String pkgName = nameContent[0];
		String appName = nameContent[1];	          //显示在通知栏上面的下载app name

		if (mMsgDownloadListener == null) {
			mMsgDownloadListener = new MessageCenterDownloadListener(getApplicationContext());
		}
		if (mCurrentDownloadTask == null) {
			mCurrentDownloadTask = new DownloadTask(Long.valueOf(msgId), url, appName, 0, 0,
					LauncherEnv.Path.MESSAGECENTER_PATH + msgId + ".apk", pkgName);
		}

		try {
			if (mDownloadController != null) {
				long taskId = mDownloadController.addDownloadTask(mCurrentDownloadTask);
				if (taskId != -1) {
					mDownloadController.addDownloadTaskListener(taskId, mMsgDownloadListener);
					mDownloadController.startDownload(taskId);
				}
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showDownloadUI();
	}
	
	/**
	 * 下载服务的控制接口Connector
	 */
	private ServiceConnection mConnenction = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			IDownloadService mController = IDownloadService.Stub.asInterface(service);
			// 设置整个进程通用的下载控制接口
			GOLauncherApp.getApplication().setDownloadController(mController);
			mDownloadController = mController;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i("", "Theme onServiceDisconnected");
			GOLauncherApp.getApplication().setDownloadController(null);
		}
	};

	private boolean isShowProgressBar(long appId) {
//		MessageContentBean beans = mManager.getMsg();
		String msId = mManager.getMsgId();
		if (msId != null) {
			long id = Long.valueOf(msId);
//			ArrayList<MessageWidgetBean> btns = beans.getButtonWidgets();
//			for (int i = 0; i < btns.size(); i++) {
//				MessageWidgetBean btn = btns.get(i);
//				if ((btn.mActtype == MessageWidgetBean.ACTTYPE_LINK || btn.mActtype == MessageWidgetBean.ACTTYPE_DOWNLOAD)
//						&& btn.mActvaule != null && id == appId) {
//					return true;
//				}
//			}
			if (id == appId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 功能简述:  更新下载进度
	 * @param percent
	 */
	private void updateProgressBar(int percent) {
		if (mProgressBar == null) {
			mProgressBar = (ProgressBar) findViewById(R.id.messagecenter_download_progress);
		}
		if (mTextProgress == null) {
			mTextProgress = (TextView) findViewById(R.id.messagecenter_download_percent);
		}
		mProgressBar.setProgress(percent);
		mTextProgress.setText(this.getString(R.string.message_download) + " " + percent + "%");
	}

	protected void downLoadFinish() {
		mProgressBar.setProgress(0);
		mTextProgress.setText(this.getString(R.string.message_download) + " ");

		if (mDownloadLayout.getVisibility() != View.GONE) {
			mDownloadLayout.setVisibility(View.GONE);
		}
		
		//当下载完程序之后，需要刷新一下下载按钮的文本显示
		if (mMsgCenterWebView != null) {
			mMsgCenterWebView.loadUrl("javascript:hasDownloadButton()");
		}
	}

	private void notifyDownloadState(final DownloadTask downloadTask) {
		if (downloadTask == null) {
			return;
		}
		long appId = downloadTask.getId();
		int status = downloadTask.getState();
		
		switch (status) {
			case DownloadTask.STATE_DOWNLOADING :
				if (isShowProgressBar(appId)) {
					showDownloadUI();
					updateProgressBar(downloadTask.getAlreadyDownloadPercent());
				}
				break;
			case DownloadTask.STATE_STOP :
				break;
			case DownloadTask.STATE_FINISH :
				downLoadFinish();
				break;
			default :
				break;
		}
	}

	/**
	 * 获取下载列表
	 */
	private void getDownloadListByIDownloadService() {

		try {
			if (mDownloadController != null) {
				Map<Long, DownloadTask> map = mDownloadController.getDownloadConcurrentHashMap();
				for (DownloadTask task : map.values()) {
					if (task != null) {
						mDownloadTaskList.add(task);
					}
				}
				setmDownloadTasks(mDownloadTaskList);
				Message msg = Message.obtain();
				msg.what = MSG_DOWNLOAD_UPDATE_UI;
				mHandler.sendMessage(msg);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 功能简述:  设置当前下载的Task的对象
	 * 功能详细描述: 主要是用户暂停下载后，重新从menu进入消息中心，点击进入消息内容中
	 * 				仍然显示上次下载进度
	 * 注意:
	 * @param downloadTaskList
	 */
	private void setmDownloadTasks(ArrayList<DownloadTask> downloadTaskList) {
		for (int i = 0; i < downloadTaskList.size(); i++) {
			if (downloadTaskList.get(i).getId() == Integer.valueOf(mMsgId)) {
				mCurrentDownloadTask = downloadTaskList.get(i);
			}
		}
	}

	/**
	 * 功能简述: 并更新下载显示的UI和获取下载任务列表并在再次进入显示上次内容
	 * 功能详细描述:
	 * 注意:
	 */
	private void initDownloadReceiver() {
		mDownloadReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (ICustomAction.ACTION_MESSAGECENTER_DOWNLOAD.equals(action)) {
					if (mHandler != null) {
						mCurrentDownloadTask = intent
								.getParcelableExtra(MessageCenterDownloadListener.UPDATE_DOWNLOAD_INFO);
						Message msg = Message.obtain();
						msg.what = MSG_DOWNLOAD_CALLBACK;
						msg.obj = mCurrentDownloadTask;
						mHandler.sendMessage(msg);
					}
				}

			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_MESSAGECENTER_DOWNLOAD);
		this.registerReceiver(mDownloadReceiver, intentFilter);
	}

	private void setdownloadBtnListener() {
		mBtnDownload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCurrentDownloadTask.getState() == DownloadTask.STATE_STOP) {
					// 继续下载并显示暂停的图标
					try {
						if (mDownloadController != null) {
							PreferencesManager sp = new PreferencesManager(getApplicationContext(), IPreferencesIds.DOWNLOAD_MANAGER_TASK_STATE,
									Context.MODE_PRIVATE);
							sp.remove(String.valueOf(mCurrentDownloadTask.getId()));
							mDownloadController.restartDownloadById(mCurrentDownloadTask.getId());
							mBtnDownload
									.setBackgroundResource(R.drawable.downloadmanager_pause_selector);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}

				} else {
					//暂停下载并显示继续的图标,
					try {
						if (mDownloadController != null) {
							PreferencesManager sp = new PreferencesManager(getApplicationContext(), IPreferencesIds.DOWNLOAD_MANAGER_TASK_STATE,
									Context.MODE_PRIVATE);
							sp.putString(String.valueOf(mCurrentDownloadTask.getId()), String.valueOf(DownloadTask.TASK_STATE_NORMAL));
							sp.commit();
							mDownloadController.stopDownloadById(mCurrentDownloadTask.getId());
							mBtnDownload.setBackgroundResource(R.drawable.msg_download_selector);
						}
					} catch (RemoteException e) {
						e.printStackTrace();
					}

				}

			}
		});
		mBtnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//取消下载，恢复到之前未按    
				try {
					if (mDownloadController != null) {
						mDownloadController.removeDownloadTaskById(mCurrentDownloadTask.getId());
						mBtnDownload
								.setBackgroundResource(R.drawable.downloadmanager_pause_selector);
						downLoadFinish();
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void showDownloadUI() {
		if (mCurrentDownloadTask != null) {
			if (mDownloadLayout.getVisibility() != View.VISIBLE) {
				mDownloadLayout.setVisibility(View.VISIBLE);
			}
			if (mCurrentDownloadTask.getState() == DownloadTask.STATE_STOP) {
				mBtnDownload.setBackgroundResource(R.drawable.msg_download_selector);
			} else {
				mBtnDownload.setBackgroundResource(R.drawable.downloadmanager_pause_selector);
			}
		}
	}

	/**
	 * 功能简述:   横竖屏的时候改变progressBar的长度，适应不同分辨率的手机
	 * 功能详细描述:
	 * 注意:
	 */
	private void setProgressBarUI() {
		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		int screenWidth = wm.getDefaultDisplay().getWidth();
		int imageWidth = this.getResources().getDrawable(R.drawable.detail_download_pause)
				.getIntrinsicWidth();

		//设置progressBar的长度
		int progressBarLen = (int) ((screenWidth - imageWidth * 2) * 0.85);
		RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(progressBarLen,
				LayoutParams.WRAP_CONTENT);
		param.addRule(RelativeLayout.BELOW, mTextProgressID);
		mProgressBar.setLayoutParams(param);

	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
		setProgressBarUI();
	}

	/**
	 * 功能简述:监听安装 ，如果此时用户安装程序了，要刷新界面
	 */
	private void packageAddReceiver() {
		mpackageAddReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					if (mMsgCenterWebView != null) {
						mMsgCenterWebView.loadUrl("javascript:hasDownloadButton()");
					}
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		this.registerReceiver(mpackageAddReceiver, intentFilter);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
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
