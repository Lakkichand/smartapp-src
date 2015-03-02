package com.zhidian.checkapk;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.trinea.android.common.util.PackageUtils;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.ta.util.download.DownloadManager;
import com.zhidian.bean.APKBean;
import com.zhidian.bean.DownloadBean;
import com.zhidian.bean.InstallBean;
import com.zhidian.controller.CheckFailApkController;
import com.zhidian.controller.GainAPKController;
import com.zhidian.util.APKUtil;
import com.zhidian.util.CAPathConstant;
import com.zhidian.util.DownloadUrl;
import com.zhidian.util.DownloadUtil;
import com.zhidian.util.FileUtil;
import com.zhidian.wifibox.dao.InstallApkDao;
import com.zhidian.wifibox.download.DownloadService;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.root.RootShell;
import com.zhidian.wifibox.view.dialog.LoadingDialog;

public class MainActivity extends Activity implements OnClickListener {

	static {
		System.loadLibrary("zip");
		System.loadLibrary("readres");
	}

	ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

	public native void readFromAssets(AssetManager ass, String filename);

	public native String readFromAssetsLibzip(String ass, String filename);

	private Context mContext;
	private ApkListAdapter adapter;
	private ListView listView;
	private Button btnBegin;// 开始安装
	private Button btnCheck;// 查看日志
	private Button btnGoonDown;// 继续下载
	private Button btnGainAddress;// 获取下载地址
	private static final int GET_ROOT = 100;
	private List<APKBean> blist;
	private TextView tvNowName; // 正在安装的apk
	private TextView tvNowDownloading; // 正在下载中的apk
	private TextView tvNowStatus; // 安装状态
	private TextView tvTotal; // 要下载安装的总个数
	private boolean canInstall = true;
	private Object Objt = new Object();
	private String url;
	private LoadingDialog loadingDialog;// 加载对话框
	private List<DownloadBean> uList;// 下载地址
	private EditText etBegin;// Number开始
	private EditText etEnd;// Number结束
	private int nowNumber;// 现在正在验证的Number
	private TextView tvNowNumber;// 现在正在验证的Number
	private String begin;
	private String end;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		initregisterReceiver();
		initUI();
		FileUtil.delFile(CAPathConstant.C_APK_ROOTPATH);
	}

	private void initregisterReceiver() {
		// 注册下载广播事件
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_BROADCAST_ACTION);
		registerReceiver(mDownloadListener, intentFilter);

		// 启动下载服务
		Intent intent = new Intent(MainActivity.this, DownloadService.class);
		startService(intent);
	}

	/**
	 * 初始化UI
	 */
	private void initUI() {
		btnBegin = (Button) findViewById(R.id.begin);
		btnBegin.setOnClickListener(this);
		btnCheck = (Button) findViewById(R.id.check);
		btnCheck.setOnClickListener(this);
		btnGainAddress = (Button) findViewById(R.id.gain_address);
		btnGainAddress.setOnClickListener(this);
		btnGoonDown = (Button) findViewById(R.id.goon_download);
		btnGoonDown.setOnClickListener(this);

		tvNowName = (TextView) findViewById(R.id.now_install);
		tvNowDownloading = (TextView) findViewById(R.id.now_downloading);
		tvNowStatus = (TextView) findViewById(R.id.now_status);
		tvTotal = (TextView) findViewById(R.id.apk_size);
		tvNowNumber = (TextView) findViewById(R.id.nownumber);
		etBegin = (EditText) findViewById(R.id.begin_number);
		etEnd = (EditText) findViewById(R.id.end_number);

		listView = (ListView) findViewById(R.id.listview);
		adapter = new ApkListAdapter(mContext);
		listView.setAdapter(adapter);

		loadingDialog = new LoadingDialog(mContext);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.begin:// 开始下载apk
			gotoPut();
			break;

		case R.id.check:// 查看日志
			gotoCheck();
			break;

		case R.id.gain_address:// 获取下载地址
			begin = etBegin.getText().toString().trim();
			end = etEnd.getText().toString().trim();
			if (TextUtils.isEmpty(begin) || TextUtils.isEmpty(end)) {
				Toast.makeText(mContext, "number不能为空！", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			nowNumber = Integer.parseInt(begin);
			getAddressData();
			break;

		case R.id.goon_download:// 继续下载
			goonDownloadApk();
			break;

		default:
			break;
		}
	}

	/**
	 * 继续下载
	 */
	private void goonDownloadApk() {
		Intent intent = new Intent(IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
		intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_CONTINUE);
		intent.putExtra("url", url);
		TAApplication.getApplication().sendBroadcast(intent);
	}

	/**
	 * 获取下载地址
	 */
	private void getAddressData() {

		showDialog("正在获取number为： " + nowNumber + "，的下载地址...");

		uList = new ArrayList<DownloadBean>();
		String url = DownloadUrl.DOWNLOADAPK + "?number=" + nowNumber;
		TAApplication.getApplication().doCommand(
				getString(R.string.gainapkcontroller),
				new TARequest(GainAPKController.GAIN_DATA, url),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						uList = (List<DownloadBean>) response.getData();
						closeDialog();
						if (uList.size() <= 0) {
							nowNumber = nowNumber + 1;
							int endnow = Integer.parseInt(end);
							if (nowNumber <= endnow) {
								getAddressData();
							}
						} else {
							tvNowNumber.setText("正在验证number=" + nowNumber);
							tvTotal.setText(uList.size() + "");

							int beginInt = Integer.parseInt(begin);
							if (nowNumber > beginInt) {
								// 开始新的下载
								gotoPut();
							}
						}

					}

					@Override
					public void onStart() {

					}

					@Override
					public void onRuning(TAResponse response) {

					}

					@Override
					public void onFinish() {

					}

					@Override
					public void onFailure(TAResponse response) {
						closeDialog();

					}
				}, true, false);

	}

	/**
	 * 开始下载
	 */
	private void gotoPut() {
		new Thread("gotoPut") {
			public void run() {
				if (uList != null) {
					for (int i = 0; i < uList.size(); i++) {
						String urlStr = uList.get(i).downUrl;
						String boxNum = uList.get(i).boxNum;
						String code = uList.get(i).code;
						int versionCode = uList.get(i).versionCode;
						int rank = i + 1;

						String curl = urlStr.replaceAll(
								"CloudBox_StoreApp.apk", "config.dat");
						String config = DownloadUtil.sendGet(curl);
						Log.e("", "config = " + config);
						Intent intent = new Intent(
								IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
						intent.putExtra("command",
								IDownloadInterface.REQUEST_COMMAND_ADD);
						intent.putExtra("boxNum", boxNum);
						intent.putExtra("code", code);
						intent.putExtra("versionCode", versionCode);
						intent.putExtra("url", urlStr);
						intent.putExtra("rank", rank);
						intent.putExtra("config", config);
						TAApplication.getApplication().sendBroadcast(intent);

						Log.e("准备下载：", "rank：" + rank + "  boxNum:" + boxNum
								+ "  versionCode:" + versionCode + "  config："
								+ config + "  --" + urlStr);
					}
				}
			};
		}.start();
	}

	/**
	 * 查看日志
	 */
	private void gotoCheck() {
		Intent intent = new Intent();
		intent.setClass(mContext, RecordActivity.class);
		startActivity(intent);
	}
	
	@Override
	public void onBackPressed() {
	}

	/**
	 * 开始安装
	 * 
	 * @param filePath
	 * @param b
	 */
	private void gotoBgein(DownloadBean b) {
		tvNowStatus.setText("安装状态：" + "正在获取ROOT权限");
		boolean isHave = RootShell.isRootValid();
		Message msg = new Message();
		msg.what = GET_ROOT;
		msg.arg1 = isHave ? 1 : 0;
		msg.obj = b;
		mHandler.sendMessage(msg);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_ROOT:
				if (msg.arg1 == 1) {
					// 有ROOT权限
					tvNowStatus.setText("安装状态：" + "获取ROOT权限成功");
					DownloadBean b = (DownloadBean) msg.obj;
					silentInstall(b);
				} else {

					tvNowStatus.setText("安装状态：" + "获取ROOT权限失败");
					Toast.makeText(mContext, "您的手机没有ROOT权限", Toast.LENGTH_SHORT)
							.show();
				}
				break;

			default:
				break;
			}
		};
	};

	/**
	 * 静默安装
	 * 
	 * @param b
	 */
	@SuppressLint("SimpleDateFormat")
	private synchronized void silentInstall(final DownloadBean b) {
		fixedThreadPool.execute(new Runnable() {

			@Override
			public void run() {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						tvNowName.setText("正在安装第" + b.rank + "个：" + b.filePath);
						tvNowStatus.setText("安装状态：" + "正在安装...");
					}
				});

				/**
				 * 获取apk的版本号
				 */
				final String versionCode = String.valueOf(APKUtil
						.getVersionCode(mContext, b.filePath));

				/**
				 * 获取apk中的boxNum
				 */
				final String boxNum = readFromAssetsLibzip(b.filePath,
						"assets/boxId");

				/**
				 * 安装的返回码
				 */
				final int status = PackageUtils.installSilent(mContext,
						b.filePath);

				Log.e("状态码：", status + "");
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						InstallApkDao dao = new InstallApkDao(mContext);
						InstallBean bean = new InstallBean();
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
						String nowTime = formatter.format(curDate);

						bean.boxNum = b.boxNum;
						bean.versionCode = String.valueOf(b.versionCode);
						Log.e("", "bean.versionCode = " + bean.versionCode);
						bean.installTime = nowTime;
						bean.status = "0";
						bean.msg = "成功";
						bean.code = b.code;
						bean.downloadUrl = b.downUrl;

						switch (status) {
						case PackageUtils.INSTALL_SUCCEEDED:
							// 安装成功
							if (!bean.boxNum.equals(boxNum)) {
								bean.msg = "boxNum不同";
								bean.status = "1";
							}
							if (!bean.boxNum.equals(b.config)) {
								bean.msg = "config不同";
								bean.status = "1";
							}
							Log.e("", "bean.versionCode:" + bean.versionCode
									+ "---b.versionCode:" + b.versionCode);
							if (!bean.versionCode.equals(versionCode)) {
								bean.msg = "versionCode不同";
								bean.status = "1";
							}

							tvNowStatus.setText("安装状态：" + "安装成功");
							// showToast("安装成功");
							break;
						case PackageUtils.INSTALL_FAILED_ALREADY_EXISTS:
							// 安装失败，应用已经存在
							bean.msg = "安装失败,失败原因代码：" + status;
							bean.status = "1";
							tvNowStatus.setText("安装状态：" + "安装失败，应用已经存在");
							// showToast("安装失败，应用已经存在");
							break;
						case PackageUtils.INSTALL_FAILED_INVALID_APK:
							// 安装失败，安装包无效
							bean.msg = "安装失败,失败原因代码：" + status;
							bean.status = "1";
							tvNowStatus.setText("安装状态：" + "安装失败，安装包无效");
							// showToast("安装失败，安装包无效");
							break;
						case PackageUtils.INSTALL_FAILED_INSUFFICIENT_STORAGE:
							// 安装失败，没有足够的存储空间
							bean.msg = "安装失败,失败原因代码：" + status;
							bean.status = "1";
							tvNowStatus.setText("安装状态：" + "安装失败，没有足够的存储空间");
							// showToast("安装失败，没有足够的存储空间");
							break;
						default:
							bean.msg = "安装失败,失败原因代码：" + status;
							bean.status = "1";
							tvNowStatus.setText("安装状态：" + "安装失败，未知原因");
							break;
						}

						dao.saveInfo(bean);

						/**
						 * 上传验证结果到服务端
						 */
						UnloadCheckResult(bean);

						// 删除掉已验证的apk包
						FileUtil.deleteSingle(b.filePath);

					}

				});

			}
		});
	}

	/**
	 * 上传验证结果到服务端
	 * 
	 * @param bean
	 */
	private void UnloadCheckResult(InstallBean bean) {

		TAApplication.getApplication().doCommand(
				getString(R.string.checkfailapkcontroller),
				new TARequest(CheckFailApkController.SIGN_CHECK, bean),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {

					}

					@Override
					public void onStart() {

					}

					@Override
					public void onRuning(TAResponse response) {

					}

					@Override
					public void onFinish() {

					}

					@Override
					public void onFailure(TAResponse response) {

					}
				}, true, false);

	}

	private void showToast(String cotent) {
		Toast.makeText(mContext, cotent, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 下载广播接收器
	 */
	private final BroadcastReceiver mDownloadListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadTask task = intent.getParcelableExtra("task");

			// 通知container
			int p = task.alreadyDownloadPercent;
			int rank = task.rank;

			if (task.state == DownloadTask.COMPLETE) {// 下载完成
				showToast("下载完成");
				String filePath = DownloadUtil.getCApkFileFromUrl(task.url);
				DownloadBean b = new DownloadBean();
				b.boxNum = task.boxNum;
				b.code = task.code;
				b.versionCode = task.versionCode;
				b.filePath = filePath;
				b.downUrl = task.url;
				b.rank = rank;
				b.config = task.config;
				gotoBgein(b);

				int allDownloadSize = DownloadManager.getDownloadManager()
						.getQueuehandlerCount();// 所有任务
				int nowDownloadSize = DownloadManager.getDownloadManager()
						.getDownloadinghandlerCount();// 正在下载的任务
				int stopDownloadSize = DownloadManager.getDownloadManager()
						.getPausinghandlerCount();// 暂停的任务

				TextView info = (TextView) findViewById(R.id.info);
				info.setText("等待中：" + allDownloadSize + "正在下载："
						+ nowDownloadSize + "暂停：" + stopDownloadSize);

				if (allDownloadSize == 0 && nowDownloadSize == 0
						&& stopDownloadSize == 0) {
					nowNumber = nowNumber + 1;
					int endnow = Integer.parseInt(end);
					if (nowNumber <= endnow) {
						// 获取新的下载地址
						getAddressData();
					}
				}
			} else if (task.state == DownloadTask.PAUSING) {// 已暂停
				showToast("已暂停");
				url = task.url;
				btnGoonDown.setVisibility(View.VISIBLE);
			} else if (task.state == DownloadTask.DOWNLOADING) {
				tvNowDownloading.setText("正在下载第 " + rank + "个：" + p + "%");
				btnGoonDown.setVisibility(View.GONE);
			} else if (task.state == DownloadTask.FAIL) {// 下载失败
				showToast("下载失败");
			}
		}
	};

	/**
	 * 显示对话框
	 * 
	 * @param text
	 */
	private void showDialog(String text) {
		if (loadingDialog != null) {
			loadingDialog.setMessage(text);
			loadingDialog.show();
		}
	}

	/**
	 * 取消对话框
	 */
	private void closeDialog() {
		if (loadingDialog != null) {
			loadingDialog.cancel();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mDownloadListener);
	}

}
