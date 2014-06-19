package com.jiubang.go.backup.pro;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.Item;
import com.google.analytics.tracking.android.Transaction;
import com.jiubang.go.backup.pro.errorreport.CrashReport;
import com.jiubang.go.backup.pro.googleplay.BillingService;
import com.jiubang.go.backup.pro.googleplay.BillingService.RequestPurchase;
import com.jiubang.go.backup.pro.googleplay.BillingService.RestoreTransactions;
import com.jiubang.go.backup.pro.googleplay.Consts;
import com.jiubang.go.backup.pro.googleplay.Consts.PurchaseState;
import com.jiubang.go.backup.pro.googleplay.Consts.ResponseCode;
import com.jiubang.go.backup.pro.googleplay.ResponseHandler;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.product.manage.Base64;
import com.jiubang.go.backup.pro.product.manage.DungeonsPurchaseObserver;
import com.jiubang.go.backup.pro.product.manage.EncryptArithmetic;
import com.jiubang.go.backup.pro.product.manage.IGooglePayListener;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsTool;
import com.jiubang.go.backup.pro.track.ga.TrackerLog;
import com.jiubang.go.backup.pro.util.LogUtil;
import com.jiubang.go.backup.pro.util.Util;

/**
 * GO备份 Application实例，目前主要执行备份记录扫描，及收费状态校验及其他初始化工作
 *
 * @author maiyongshen
 */
public class GoBackupApplication extends Application implements IGooglePayListener {
	// log标签
	private static final String TAG = "GoBackup_DungeonsPurchaseObserver";
	// 储存标记
	public static final String DB_INITIALIZED = "db_initialized";
	// 产品信息
	private ProductPayInfo mGoBackupProduct;
	// PREFS_NAME
	public static final String PREFS_NAME = "GooglePayPrefs";

	private static ExecutorService sThreadPool = Executors.newFixedThreadPool(5);
	private final String mProductId = "com.jiubang.go.backup.prokey_pay";

	@Override
	public void onCreate() {
		super.onCreate();
		//		long t = System.currentTimeMillis();
		// 初始化备份所需要的一些环境与信息
		initEnviroment(this);

		// 初始化报告反馈
		CrashReport crashReport = new CrashReport();
		crashReport.start(getApplicationContext());

		setupBillingService();
		//		queryAPPlicationLicensing();
		BackupManager.getInstance().initPackagesInfo(this);

		// 上传统计日志
		uploadStatisticsData(this);

		// 谷歌分析
		GAServiceManager.getInstance().dispatch();

		//		t = System.currentTimeMillis() - t;
		//		LogUtil.d("GoBackupApplication onCreate time = " + t);
	}

	private void setupBillingService() {
		// Google pay应用内付费
		DungeonsPurchaseObserver observer = new DungeonsPurchaseObserver(GoBackupApplication.this,
				GoBackupApplication.this);
		// 注册观察者
		ResponseHandler.register(observer);

		postRunnable(new Runnable() {
			@Override
			public void run() {
				// billingService 用于与googlepay交互的service
				try {
					BillingService billingService = new BillingService();
					billingService.setContext(GoBackupApplication.this);
					mGoBackupProduct = ProductManager.getProductPayInfo(getApplicationContext(),
							ProductPayInfo.PRODUCT_ID);
					billingService.restoreTransactions();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void queryAPPlicationLicensing() {
		String deviceId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
		if (checkIsPiad(deviceId)) {
			ProductPayInfo.sIsPaidUserByKey = true;
			return;
		}
		Intent checkGooglicensingIntent = new Intent();
		checkGooglicensingIntent.setAction("com.jiubang.intent.action.MARKET_LICENSE_REQUEST");
		sendBroadcast(checkGooglicensingIntent);
	}

	private boolean checkIsPiad(String deviceId) {
		byte[] nameData = null;
		try {
			nameData = mProductId.getBytes(ProductPayInfo.DEFALUTENCODING);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		String mEncryptProductId = Base64.encodeToString(nameData, Base64.DEFAULT)
				.replace("\n", "").replace("=", "");
		String state = Environment.getExternalStorageState();
		String mFilePath = null;
		String mFileCatalog = null;
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			mFileCatalog = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ ProductPayInfo.PROBATION_RECORD_CATALOG;
			mFilePath = mFileCatalog + File.separator + mEncryptProductId;
		} else {
			Context keyContext = null;
			try {
				keyContext = createPackageContext("com.jiubang.gobackupprokey",
						Context.CONTEXT_IGNORE_SECURITY);
				mFilePath = keyContext.getFilesDir() + File.separator + mEncryptProductId;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		File payinfoFile = new File(mFilePath);
		if (payinfoFile.exists()) {
			byte[] payContentData = Util.readDataFromFile(mFilePath);
			byte[] retData = null;
			try {
				retData = EncryptArithmetic.tripleDesDencrypt(payContentData);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			String payInfoFromFile = new String(retData);
			String phoneDevie = deviceId + "HasPaid";
			if (phoneDevie.equals(payInfoFromFile)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onIPurchaseStateChange(PurchaseState purchaseState, String productId, int quantity,
			long purchaseTime, String developerPayload) {
		if (Consts.DEBUG) {
			Log.i(TAG, "onPurchaseStateChange() itemId: " + productId + " " + purchaseState);
		}
		if (purchaseState == PurchaseState.PURCHASED) {
			if (productId.equals(ProductPayInfo.PRODUCT_ID)) {
				mGoBackupProduct.setAlreadyPaid(true);
			}
			// GA统计
			TrackerLog.i("PayUpdateHelpActivity onIPurchaseStateChange PURCHASED");
			Transaction trans = new Transaction.Builder(productId, 0)
					.setAffiliation("In-App Store").build();
			trans.addItem(new Item.Builder(productId, "GOBackupPro", 4990000, quantity).build());
			EasyTracker.getInstance().setContext(getApplicationContext());
			EasyTracker.getTracker().trackTransaction(trans);
		} else if (purchaseState == PurchaseState.CANCELED) {
			mGoBackupProduct.setAlreadyPaid(false);
		} else if (purchaseState == PurchaseState.REFUNDED) {
			mGoBackupProduct.setAlreadyPaid(false);
		}
	}

	@Override
	public void onIRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
		if (Consts.DEBUG) {
			Log.d(TAG, request.mProductId + ": " + responseCode);
		}
		if (responseCode == ResponseCode.RESULT_OK) {
			if (Consts.DEBUG) {
				Log.i(TAG, "得到异步回传的消息，购买请求已经发出");
			}
		} else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
			if (Consts.DEBUG) {
				Log.i(TAG, "得到异步回传的消息，用户取消了交易");
			}
		} else {
			if (Consts.DEBUG) {
				Log.i(TAG, "得到异步回传的消息，responseCode为：" + responseCode);
			}
		}
	}

	@Override
	public void onIRestoreTransactionsResponse(RestoreTransactions request,
			ResponseCode responseCode) {
		if (responseCode == ResponseCode.RESULT_OK) {
			if (Consts.DEBUG) {
				Log.d(TAG, "completed RestoreTransactions request");
			}
			PreferenceManager pm = PreferenceManager.getInstance();
			pm.putBoolean(getApplicationContext(), GoBackupApplication.DB_INITIALIZED, true);
		} else {
			if (Consts.DEBUG) {
				// showDialog(DIALOG_RESPONSE_CODE_ERROR_ID);
				Log.d(TAG, "RestoreTransactions error: " + responseCode);
			}
		}
	}

	@Override
	public void onIBillingSupported(boolean supported) {
		LogUtil.d("support billing");
	}

	public static void postRunnable(Runnable runnable) {
		if (runnable == null) {
			return;
		}
		sThreadPool.execute(runnable);
	}

	private static void uploadStatisticsData(Context context) {
		StatisticsDataManager sdm = StatisticsDataManager.getInstance();
		long now = System.currentTimeMillis();
		long lastUploadTime = sdm.getLastUploadTime(context);
		if (lastUploadTime <= 0 || Math.abs(now - lastUploadTime) >= sdm.getUploadInterval()) {
			StatisticsTool.uploadStatisticsData(context, false);
		}
	}
	/**
	 * <br>功能简述:初始化备份所需要的一些环境与信息的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 */
	private static void initEnviroment(Context context) {
		// 创建备份根目录文件夹
		// 先检查一下之前是否已经初始化过，已经有记录了
		String rootDir = Util.getSdRootPathOnPreference(context);
		if (rootDir == null) {
			//	如果之前没有记录，就要进行初始化
			//			long t1 = System.currentTimeMillis();
			//	找出可用的SD卡根目录并进行保存
			rootDir = Util.getDefalutValidSdPath(context);
			PreferenceManager.getInstance().putString(context,
					PreferenceManager.KEY_BACKUP_SD_PATH, rootDir);
			//			t1 = System.currentTimeMillis() - t1;
			//			LogUtil.d("init sd path time = " + t1);
		}

		// 拷贝用于root权限的应用备份恢复需要的文件
		final PreferenceManager pm = PreferenceManager.getInstance();
		boolean hasCopyBinaryFile = pm.getBoolean(context,
				PreferenceManager.KEY_HAS_COPY_BIANRY_FILE, false);
		boolean needUpdateBinaryFile = pm.getInt(context,
				PreferenceManager.KEY_BINARY_FILE_VERSION, 0) != Constant.BUSYBOX_BACKUP_FILE_CURRENT_VERSION;
		if (!hasCopyBinaryFile || needUpdateBinaryFile) {
			//			long t2 = System.currentTimeMillis();
			boolean ret = Util.copyBackupBinaryFileToSystemDirectory(context);
			pm.putBoolean(context, PreferenceManager.KEY_HAS_COPY_BIANRY_FILE, ret);
			pm.putInt(context, PreferenceManager.KEY_BINARY_FILE_VERSION,
					Constant.BUSYBOX_BACKUP_FILE_CURRENT_VERSION);
			//			t2 = System.currentTimeMillis() - t2;
			//			LogUtil.d("copy binary file time = " + t2);
		}
	}
}
