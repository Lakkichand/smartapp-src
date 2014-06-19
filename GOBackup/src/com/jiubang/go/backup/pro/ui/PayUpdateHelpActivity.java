package com.jiubang.go.backup.pro.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.Item;
import com.google.analytics.tracking.android.Tracker;
import com.google.analytics.tracking.android.Transaction;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.BaseActivity;
import com.jiubang.go.backup.pro.GoBackupApplication;
import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.googleplay.BillingService;
import com.jiubang.go.backup.pro.googleplay.BillingService.RequestPurchase;
import com.jiubang.go.backup.pro.googleplay.BillingService.RestoreTransactions;
import com.jiubang.go.backup.pro.googleplay.Consts;
import com.jiubang.go.backup.pro.googleplay.Consts.PurchaseState;
import com.jiubang.go.backup.pro.googleplay.Consts.ResponseCode;
import com.jiubang.go.backup.pro.googleplay.ResponseHandler;
import com.jiubang.go.backup.pro.product.manage.DungeonsPurchaseObserver;
import com.jiubang.go.backup.pro.product.manage.IGooglePayListener;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;
import com.jiubang.go.backup.pro.statistics.StatisticsTool;
import com.jiubang.go.backup.pro.track.ga.TrackerLog;

/**
 * @author ReyZhang 应用内付费
 */
public class PayUpdateHelpActivity extends BaseActivity implements IGooglePayListener {
	public static final String EXTRA_IS_PAID = "extra_is_paid";
	public static final String EXTRA_PURCHASE_REQUEST_SOURCE = "extra_purchase_request_source";

	// 不能连接
	private static final int DIALOG_CANNOT_CONNECT_ID = 0x3001;
	// 不支持应用内付费
	private static final int DIALOG_BILLING_NOT_SUPPORTED_ID = 0x3002;
	// 错误返回
	private static final int DIALOG_RESPONSE_CODE_ERROR_ID = 0x3003;
	// 不能绑定android市场
	private static final int DIALOG_CANNOT_BIND_TO_SERVICE = 0x3004;
	// 购买成功提示
	private static final int DIALOG_PURCHASE_SUCCESS = 0x4001;
	// 退款成功提示
	private static final int DIALOG_REFUNDED_SUCCESS = 0x4002;

	// 购买提示
	private static final int DIALOG_PURCHASE_TIP = 03005;

	// Google购买观察者
	private DungeonsPurchaseObserver mDungeonsPurchaseObserver;
	// Google付费Service
	private BillingService mBillingService;
	// log标签
	private String mTAG = "GoBackup_DungeonsPurchaseObserver";
	// 产品信息
	private ProductPayInfo mGoBackupProduct;

	private Button mPayUpdateButton;
	private ListView mListView;
	private BaseAdapter mListAdapter;

	private Dialog mDialog;

	private ProgressDialog mRestroeDialog;

	// 请求标记
	private boolean mIsRequest = false;

	private final String mPURCHASEDBNAME = "purchase.db";

	private final int mDelayTime = 15000;

	private boolean mHasSendPurchaseRequest = false;

	private int mPurchaseRequestSource;

	private Tracker mTracker;
	private byte[] mLock = new byte[0];

	private static HashMap<Integer, Integer> sProFeatureMap = new HashMap<Integer, Integer>();
	private StatisticsDataManager mSdm;

	// 收费项的标志
	public static final int LIST_POS_CLOUD_BACKUP = 0;
	public static final int LIST_POS_UNLIMITED_BACKUP_SIZE = 1;
	public static final int LIST_POS_MORE_BACKUP_CONTENT = 2;
	public static final int LIST_POS_BACKUP_APP_DATA_ONLY = 3;
	public static final int LIST_POS_EDIT_BACKUP = 4;
	public static final int LIST_POS_FREEZE_APP = 5;

	static {
		sProFeatureMap.put(StatisticsKey.PURCHASE_FROM_BACKUP_APP_DATA_ONLY,
				LIST_POS_BACKUP_APP_DATA_ONLY);
		sProFeatureMap.put(StatisticsKey.PURCHASE_FROM_BACKUP_SIZE_LIMIT,
				LIST_POS_UNLIMITED_BACKUP_SIZE);
		sProFeatureMap.put(StatisticsKey.PURCHASE_FROM_BACKUP_SYSTEM_SETTING,
				LIST_POS_MORE_BACKUP_CONTENT);
		sProFeatureMap.put(StatisticsKey.PURCHASE_FROM_CLOUD_BACKUP, LIST_POS_CLOUD_BACKUP);
		sProFeatureMap.put(StatisticsKey.PURCHASE_FROM_EDIT_BACKUP, LIST_POS_EDIT_BACKUP);
		sProFeatureMap.put(StatisticsKey.PURCHASE_FROM_FREEZE_APP, LIST_POS_FREEZE_APP);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent() != null) {
			mPurchaseRequestSource = getIntent().getIntExtra(EXTRA_PURCHASE_REQUEST_SOURCE,
					StatisticsKey.PURCHASE_FROM_INVALID_VALUE);
		}

		mSdm = StatisticsDataManager.getInstance();
		// 设置付费入口
		mSdm.setPurchaseSource(this, mPurchaseRequestSource);
		// 每次进入收费页，都要更新，总数都加1
		mSdm.setEnterPurchaseHelpActivityCount(PayUpdateHelpActivity.this, 1);
		// 一进入付费页，马上上传一次统计
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mLock) {
					StatisticsTool.uploadPayActionStatisticsInfo(PayUpdateHelpActivity.this);
				}
			}
		}).start();
		initViews();
		// 创建观察者..
		mDungeonsPurchaseObserver = new DungeonsPurchaseObserver(this, this);
		// 注册观察者
		ResponseHandler.register(mDungeonsPurchaseObserver);
		// billingService 用于与googlepay交互的service
		mBillingService = new BillingService();
		mBillingService.setContext(this);
		mGoBackupProduct = ProductManager.getProductPayInfo(getApplicationContext(),
				ProductPayInfo.PRODUCT_ID);

	}

	private void initViews() {
		setContentView(R.layout.pay_update_help_view);
		View returnButton = findViewById(R.id.return_btn);
		returnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mListView = (ListView) findViewById(R.id.pay_update_help_listview);
		TextView title = (TextView) findViewById(R.id.title);

		View footerBar = findViewById(R.id.footer_bar);
		boolean isPaid = getIntent() != null
				? getIntent().getBooleanExtra(EXTRA_IS_PAID, false)
				: false;
		if (isPaid) {
			title.setText(R.string.msg_is_advanced);
			footerBar.setVisibility(View.GONE);
		} else {
			title.setText(R.string.pay_update_title);
			footerBar.setVisibility(View.VISIBLE);
			mPayUpdateButton = (Button) findViewById(R.id.update_btn);
			mPayUpdateButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 只要点击购买按钮，马上上传一次统计
					new Thread(new Runnable() {
						@Override
						public void run() {
							synchronized (mLock) {
								// 每次点击付费按钮，点击总数都加1
								mSdm.setClickPurchaseButtonCount(PayUpdateHelpActivity.this, 1);
								mSdm.setPurchaseMethodClickCount(PayUpdateHelpActivity.this,
										StatisticsKey.PURCHASE_METHOD_CLICK_COUNT_ONE);
								mSdm.setPurchaseMethod(PayUpdateHelpActivity.this,
										StatisticsKey.PURCHASE_BY_BILLING_IN_STORE);
								StatisticsTool
										.uploadPayActionStatisticsInfo(PayUpdateHelpActivity.this);
							}
						}
					}).start();
					onCheckBillingSupported();
					// Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri
					// .parse("http://market.android.com/details?id="
					// + "com.jiubang.gobackupprokey"));
					// startActivity(marketIntent);
				}
			});
		}

		List<PayItemContent> payItemList = getPayteItemList();
		mListAdapter = new PayUpdateHelpAdapter(PayUpdateHelpActivity.this, payItemList);
		mListView.setAdapter(mListAdapter);
		final Integer selectedPosition = sProFeatureMap.get(mPurchaseRequestSource);
		if (selectedPosition != null) {
			((PayUpdateHelpAdapter) mListAdapter).setSelection(selectedPosition);
			mListView.setSelection(selectedPosition);
		}
	}

	private List<PayItemContent> getPayteItemList() {
		List<PayItemContent> payItemList = new ArrayList<PayItemContent>();

		PayItemContent content1 = new PayItemContent();
		content1.mDrawableId = R.drawable.big_icon_cloud;
		content1.mItemTitle = getString(R.string.pay_title_1);
		content1.mItemTitleTips = getString(R.string.pay_detail_1);
		payItemList.add(content1);

		PayItemContent content2 = new PayItemContent();
		content2.mDrawableId = R.drawable.big_icon_endless;
		content2.mItemTitle = getString(R.string.pay_title_2);
		content2.mItemTitleTips = getString(R.string.pay_detail_2);
		payItemList.add(content2);

		PayItemContent content3 = new PayItemContent();
		content3.mDrawableId = R.drawable.big_icon_more;
		content3.mItemTitle = getString(R.string.pay_title_3);
		content3.mItemTitleTips = getString(R.string.pay_detail_3);
		payItemList.add(content3);

		PayItemContent content4 = new PayItemContent();
		content4.mDrawableId = R.drawable.big_icon_data;
		content4.mItemTitle = getString(R.string.pay_title_4);
		content4.mItemTitleTips = getString(R.string.pay_detail_4);
		payItemList.add(content4);

		PayItemContent content5 = new PayItemContent();
		content5.mDrawableId = R.drawable.big_icon_edit;
		content5.mItemTitle = getString(R.string.pay_title_5);
		content5.mItemTitleTips = getString(R.string.pay_detail_5);
		payItemList.add(content5);

		PayItemContent content6 = new PayItemContent();
		content6.mDrawableId = R.drawable.pro_feature_freeze_app;
		content6.mItemTitle = getString(R.string.pay_title_6);
		content6.mItemTitleTips = getString(R.string.pay_detail_6);
		payItemList.add(content6);

		return payItemList;

	}

	@Override
	public void onIPurchaseStateChange(PurchaseState purchaseState, String productId, int quantity,
			long purchaseTime, String developerPayload) {
		if (Consts.DEBUG) {
			Log.i(mTAG, "onPurchaseStateChange() itemId: " + productId + " " + purchaseState);
		}
		if (purchaseState == PurchaseState.PURCHASED) {
			if (productId.equals(ProductPayInfo.PRODUCT_ID)) {
				mGoBackupProduct.setAlreadyPaid(true);
				Message.obtain(mHandler, MSG_SHOW_DIALOG, DIALOG_PURCHASE_SUCCESS, -1)
						.sendToTarget();
				mIsRequest = false;
			}

			if (mHasSendPurchaseRequest) {
				TrackerLog.i("PayUpdateHelpActivity onIPurchaseStateChange PURCHASED");
				Transaction trans = new Transaction.Builder(productId, 0).setAffiliation(
						"In-App Store").build();
				trans.addItem(new Item.Builder(productId, "GOBackupPro", 4990000, quantity).build());
				mTracker.trackTransaction(trans);
				GAServiceManager.getInstance().dispatch();
				// 用户购买成功，记录时间
				StatisticsDataManager.getInstance().setPurchaseTime(this, purchaseTime);
				StatisticsDataManager.getInstance().setPurchaseMethod(this,
						StatisticsKey.PURCHASE_BY_BILLING_IN_STORE);
				mHasSendPurchaseRequest = false;
				// StatisticsDataManager.getInstance().setPurchaseSource(this,
				// mPurchaseRequestSource);

				// 用户付费成功，马上上传一次统计
				new Thread(new Runnable() {
					@Override
					public void run() {
						StatisticsTool.uploadPayActionStatisticsInfo(PayUpdateHelpActivity.this);
					}
				}).start();

			}
		} else if (purchaseState == PurchaseState.CANCELED) {
			mGoBackupProduct.setAlreadyPaid(false);
		} else if (purchaseState == PurchaseState.REFUNDED) {
			mGoBackupProduct.setAlreadyPaid(false);
			Message.obtain(mHandler, MSG_SHOW_DIALOG, DIALOG_REFUNDED_SUCCESS, -1).sendToTarget();
		}
		SharedPreferences prefs = this.getSharedPreferences(GoBackupApplication.PREFS_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = prefs.edit();
		edit.putBoolean(GoBackupApplication.DB_INITIALIZED, true);
		edit.commit();
	}

	@Override
	public void onIRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
		if (Consts.DEBUG) {
			Log.d(mTAG, request.mProductId + ": " + responseCode);
		}
		if (responseCode == ResponseCode.RESULT_OK) {
			if (Consts.DEBUG) {
				Log.i(mTAG, "得到异步回传的消息，购买请求已经发出");
			}
			mHasSendPurchaseRequest = true;
		} else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
			if (Consts.DEBUG) {
				Log.i(mTAG, "得到异步回传的消息，用户取消了交易");
			}
		} else {
			if (Consts.DEBUG) {
				Log.i(mTAG, "得到异步回传的消息，responseCode为：" + responseCode);
			}
		}
	}

	@Override
	public void onIRestoreTransactionsResponse(RestoreTransactions request,
			ResponseCode responseCode) {
		if (responseCode == ResponseCode.RESULT_OK) {
			if (Consts.DEBUG) {
				Log.d(mTAG, "completed RestoreTransactions request");
			}
			SharedPreferences prefs = this.getSharedPreferences(GoBackupApplication.PREFS_NAME,
					Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putBoolean(GoBackupApplication.DB_INITIALIZED, true);
			edit.commit();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					dismissRestoreTransactionDialog();
				}
			});
		} else {
			if (Consts.DEBUG) {
				Message.obtain(mHandler, MSG_SHOW_DIALOG, DIALOG_RESPONSE_CODE_ERROR_ID, -1)
						.sendToTarget();
				Log.d(mTAG, "RestoreTransactions error: " + responseCode);
			}
		}
		mIsRequest = false;
	}

	@Override
	public void onIBillingSupported(boolean supported) {
		if (Consts.DEBUG) {
			Log.i(mTAG, "supported: " + supported);
		}
		if (supported) {
			// 判断是否是有取过购买凭证

			File purchaseDBFile = getApplication().getDatabasePath(mPURCHASEDBNAME);
			if (purchaseDBFile.exists()) {
				purchaseDBFile.delete();
			}
			// if (!restoreDatabase()) {
			// // 如果没有取过购买凭证，则发起购买验证
			// mBillingService.restoreTransactions();
			// // Log.v("restoreTransaction", "onRestoreTransaction");
			// return;
			// }
			try {
				mBillingService.requestPurchase(ProductPayInfo.PRODUCT_ID, null);
				mIsRequest = true;
			} catch (Exception e) {
				Message.obtain(mHandler, MSG_SHOW_DIALOG, DIALOG_BILLING_NOT_SUPPORTED_ID, -1)
						.sendToTarget();
			}
			// Log.v("requestPurchase", "onRequestPurchase");

		} else {
			Message.obtain(mHandler, MSG_SHOW_DIALOG, DIALOG_BILLING_NOT_SUPPORTED_ID, -1)
					.sendToTarget();
		}
	}

	private Dialog createDialog(int titleId, int messageId) {
		String helpUrl = replaceLanguageAndRegion(getString(R.string.help_url));
		if (Consts.DEBUG) {
			Log.i(mTAG, helpUrl);
		}
		final Uri helpUri = Uri.parse(helpUrl);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		switch (titleId) {
			case R.string.cannot_connect_title :
			case R.string.billing_not_supported_title :
			case R.string.response_code_fail_title :
			case R.string.purchase_tip_title :
			case R.string.cannot_bind_to_service_title :
				builder.setTitle(titleId)
						.setIcon(android.R.drawable.stat_sys_warning)
						.setMessage(messageId)
						.setCancelable(true)
						.setPositiveButton(android.R.string.ok, null)
						.setNegativeButton(R.string.learn_more,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Intent intent = new Intent(Intent.ACTION_VIEW, helpUri);
										startActivity(intent);
									}
								});
				break;
			case R.string.purchase_success_title :
			case R.string.refunded_success_title :
				builder.setTitle(titleId).setMessage(messageId).setCancelable(true)
						.setNegativeButton(R.string.summit, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								setResult(RESULT_OK);
								finish();
							}
						});
				break;
		}

		return builder.create();
	}

	private String replaceLanguageAndRegion(String str) {
		// Substitute language and or region if present in string
		if (str.contains("%lang%") || str.contains("%region%")) {
			Locale locale = Locale.getDefault();
			str = str.replace("%lang%", locale.getLanguage().toLowerCase());
			str = str.replace("%region%", locale.getCountry().toLowerCase());
		}
		return str;
	}

	private void onCheckBillingSupported() {
		if (!mBillingService.checkBillingSupported()) {
			mDialog = createDialog(DIALOG_CANNOT_BIND_TO_SERVICE);
			mDialog.show();
		}
	}

	private boolean applyForChargeFeature() {
		if (mGoBackupProduct.isAlreadyPaid()) {
			return true;
		}
		return false;
	}

	private boolean restoreDatabase() {
		PreferenceManager pm = PreferenceManager.getInstance();
		boolean initialized = pm.getBoolean(getApplicationContext(),
				GoBackupApplication.DB_INITIALIZED, false);
		return initialized;
	}

	protected Dialog createDialog(int id) {
		switch (id) {
			case DIALOG_CANNOT_CONNECT_ID :
				return createDialog(R.string.cannot_connect_title, R.string.cannot_connect_message);

			case DIALOG_BILLING_NOT_SUPPORTED_ID :
				return createDialog(R.string.billing_not_supported_title,
						R.string.billing_not_supported_message);

			case DIALOG_RESPONSE_CODE_ERROR_ID :
				return createDialog(R.string.response_code_fail_title,
						R.string.response_code_fail_message);

			case DIALOG_CANNOT_BIND_TO_SERVICE :
				return createDialog(R.string.cannot_bind_to_service_title,
						R.string.cannot_bind_to_service_message);

			case DIALOG_PURCHASE_TIP :
				return createDialog(R.string.purchase_tip_title, R.string.purchase_tip_message);

			case DIALOG_PURCHASE_SUCCESS :
				return createDialog(R.string.purchase_success_title,
						R.string.purchase_success_message);

			case DIALOG_REFUNDED_SUCCESS :
				return createDialog(R.string.refunded_success_title,
						R.string.refunded_success_message);

			default :
				return null;
		}
	}

	private void showCustomDialog(int id) {
		showDialog(createDialog(id));
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mBillingService != null) {
			if (mIsRequest) {
				File purchaseDBFile = getApplication().getDatabasePath(mPURCHASEDBNAME);
				if (purchaseDBFile.exists()) {
					purchaseDBFile.delete();
				}
				mRestroeDialog = BaseActivity.createSpinnerProgressDialog(this, false);
				mRestroeDialog.setTitle(getString(R.string.pay_update_title));
				mRestroeDialog.setMessage(getString(R.string.pay_validate));
				mRestroeDialog.show();
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						dismissRestoreTransactionDialog();
						Toast.makeText(getApplicationContext(),
								getString(R.string.fail_market_connect), Toast.LENGTH_LONG).show();
					}
				}, mDelayTime);
				try {
					mBillingService.restoreTransactions();
				} catch (Exception e) {
					Message.obtain(mHandler, MSG_SHOW_DIALOG, DIALOG_BILLING_NOT_SUPPORTED_ID, -1)
							.sendToTarget();
				}
			}
		}
	}

	private void dismissRestoreTransactionDialog() {
		if (mRestroeDialog != null && mRestroeDialog.isShowing()) {
			mRestroeDialog.dismiss();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		EasyTracker.getInstance().activityStart(this);
		mTracker = EasyTracker.getTracker();
	}

	@Override
	protected void onStop() {
		super.onStop();

		EasyTracker.getInstance().activityStop(this);
	}

	private static final int MSG_SHOW_DIALOG = 0X1001;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SHOW_DIALOG :
					showCustomDialog(msg.arg1);
					break;
				default :
					break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		if (mRestroeDialog != null && mRestroeDialog.isShowing()) {
			mRestroeDialog.dismiss();
		}
	}

}
