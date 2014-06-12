package com.jiubang.ggheart.billing;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.jiubang.ggheart.billing.base.BillingService.RequestPurchase;
import com.jiubang.ggheart.billing.base.BillingService.RestoreTransactions;
import com.jiubang.ggheart.billing.base.Consts;
import com.jiubang.ggheart.billing.base.Consts.PurchaseState;
import com.jiubang.ggheart.billing.base.Consts.ResponseCode;
import com.jiubang.ggheart.billing.base.PurchaseObserver;
import com.jiubang.ggheart.data.theme.zip.ZipResources;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 应用内付费的管理类
 * 
 * @author zhoujun
 * 
 */
public class AppInBillingManager {

	private static AppInBillingManager sInstance;
	private AppInBillingImplement mAppInBillingImpl;
	private ThemePurchaseObserver mPurchaseObserver;
	private Activity mActivity;
	/**
	 * 之所有用itemid去关联packagename，
	 *  是以为在付费结束后，是用packagename作为key，来保存付费信息
	 */
	private Map<String, String> mZipPackageNameMap = null;

	/**
	 * 保存付费状态的类
	 */
	private PurchaseStateManager mStateManager;

	private boolean mCurrBillingSupported;

	private String DATA_INITIALIZED = "data_initialized";
	private AppInBillingManager(Activity activity) {
		if (Consts.DEBUG) {
			Log.e(Consts.TAG, "AppInBillingManager create is running");
		}
		mActivity = activity;
		mAppInBillingImpl = new AppInBillingImplement(activity);
		mPurchaseObserver = new ThemePurchaseObserver(activity, new Handler());
		mAppInBillingImpl.registerObserver(mPurchaseObserver);
		mZipPackageNameMap = new HashMap<String, String>();
		mStateManager = new PurchaseStateManager(activity);
		checkBillingSupported();
	}

	public static synchronized AppInBillingManager createInstance(Activity activity) {
		if (sInstance == null) {
			sInstance = new AppInBillingManager(activity);
		}
		return sInstance;
	}

	public static AppInBillingManager getInstance() {
		return sInstance;
	}

	/**
	 * 检查所在区域 是否支持应用内支付
	 * 
	 * @return true:支持付费； false：不支持
	 */
	public void checkBillingSupported() {
		if (Consts.DEBUG) {
			Log.d(Consts.TAG, "AppInBillingManager checkBillingSupported running:");
		}
		if (mAppInBillingImpl != null) {
			mCurrBillingSupported = PurchaseSupportedManager.checkBillingSupported(mActivity);
			mAppInBillingImpl.checkBillingSupported(Consts.ITEM_TYPE_INAPP);

		}
	}

	/**
	 * 请求付费,调用前请确保已经调用过initPurchaseObserver()
	 * @param packageName 付费主题的包名
	 * @param productId  付费主题的产品id
	 * @param purchaseStateListener 付费状态的监听器
	 */
	public void requestPurchase(final String productId, String packageName) {

		if (productId == null || mPurchaseObserver == null) {
			return;
		}

		if (mAppInBillingImpl != null) {
			if (mZipPackageNameMap != null) {
				//如果一个付费对应多个包名，那么这里就有问题了........
				mZipPackageNameMap.put(productId, packageName);
			}
			mAppInBillingImpl.requestPurchase(productId, Consts.ITEM_TYPE_INAPP, "");
		}
	}

	/**
	 * 退出时，清空资源
	 */
	public void destory() {
		if (Consts.DEBUG) {
			Log.d(Consts.TAG, "AppInBillingManager destory is running ");
		}
		if (mAppInBillingImpl != null && mPurchaseObserver != null) {
			mAppInBillingImpl.unregisterObserver(mPurchaseObserver);
			mAppInBillingImpl.destory();
			mPurchaseObserver = null;
		}
		if (mZipPackageNameMap != null) {
			mZipPackageNameMap.clear();
		}
		clear();
	}

	private static void clear() {
		sInstance = null;
	}

	/**
	* A {@link PurchaseObserver} is used to get callbacks when Android Market sends
	* messages to this application so that we can update the UI.
	*/
	private class ThemePurchaseObserver extends PurchaseObserver {
		public ThemePurchaseObserver(Activity activity, Handler handler) {
			super(activity, handler);
		}

		@Override
		public void onBillingSupported(boolean supported, String type) {
			if (Consts.DEBUG) {
				Log.d(Consts.TAG, "checkBillingSupported supported:" + supported);
			}
			if (supported){
				restoreTransactions();
			}
			//避免重复修改是否支持付费的值
			if (mCurrBillingSupported != supported) {
				PurchaseSupportedManager.saveSupported(mActivity, supported);
				mCurrBillingSupported = supported;
			}

			//首次检查完改区域是否支持内购后，就关闭服务
			if (mZipPackageNameMap != null && mZipPackageNameMap.size() == 0) {
				if (Consts.DEBUG) {
					Log.e(Consts.TAG, "mZipPackageNameMap size is 0 and destory");
				}
				destory();
			}
		}

		@Override
		public void onPurchaseStateChange(PurchaseState purchaseState, String itemId, int quantity,
				long purchaseTime, String developerPayload) {
			if (Consts.DEBUG) {
				Log.i(Consts.TAG, "onPurchaseStateChange() itemId: " + itemId + " " + purchaseState);
			}
			int state = ThemeAppInBillingManager.PURCHASE_STATE_CANCELED;
			if (purchaseState == PurchaseState.PURCHASED) {
				state = ThemeAppInBillingManager.PURCHASE_STATE_PURCHASED;
			}
			sendPurchaseResponse(itemId, state);
			//			else if (purchaseState == PurchaseState.CANCELED) {
			//
			//			} else {
			//				Log.d("zj", "onPurchaseStateChange : REFUNDED");
			//				state = ThemeAppInBillingManager.PURCHASE_STATE_PURCHASED;
			//			}

		}

		@Override
		public void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
			if (Consts.DEBUG) {
				Log.d(Consts.TAG, request.mProductId + ": " + responseCode);
			}
			if (responseCode == ResponseCode.RESULT_OK) {
				if (Consts.DEBUG) {
					Log.i(Consts.TAG, "purchase was successfully sent to server");
				}
				return;
			} else if (responseCode == ResponseCode.RESULT_USER_CANCELED) {
				if (Consts.DEBUG) {
					Log.i(Consts.TAG, "user canceled purchase");
				}
			} else {
				if (Consts.DEBUG) {
					Log.i(Consts.TAG, "purchase failed");
				}
			}
			//			sendPurchaseResponse(request.mProductId, ThemeAppInBillingManager.PURCHASE_STATE_PURCHASED);
			//			String itemId = request.mProductId;
			//			onStateChange(itemId, AppInBillingManager.PURCHASE_STATE_CANCELED);
		}

		@Override
		public void onRestoreTransactionsResponse(RestoreTransactions request,
				ResponseCode responseCode) {
			if (responseCode == ResponseCode.RESULT_OK) {
				if (Consts.DEBUG) {
					Log.d(Consts.TAG, "completed RestoreTransactions request");
				}
				// Update the shared preferences so that we don't perform
				// a RestoreTransactions again.
				//				saveInitializeState();
			} else {
				if (Consts.DEBUG) {
					Log.d(Consts.TAG, "RestoreTransactions error: " + responseCode);
				}
			}
		}
	}

	private void sendPurchaseResponse(String itemId, int state) {
		if (Consts.DEBUG) {
			Log.d(Consts.TAG, "sendPurchaseResponse itemId:" + itemId + ",state:" + state);
		}
		if (mZipPackageNameMap != null) {
			String packageName = mZipPackageNameMap.get(itemId);
			if (state == ThemeAppInBillingManager.PURCHASE_STATE_PURCHASED) {
				savePurchaseState(packageName);
			}
			//			else {
			//				Toast.makeText(GOLauncherApp.getApplication(), "付费失败", Toast.LENGTH_SHORT).show();
			//			}
			mZipPackageNameMap.remove(itemId);
		}
		Intent intent = new Intent();
		intent.setAction(ICustomAction.ACTION_PURCHASE_STATE_RESULT);
		intent.putExtra(ThemeAppInBillingManager.EXTRA_FOR_ITEMID, itemId);
		intent.putExtra(ThemeAppInBillingManager.EXTRA_FOR_STATE, state);
		//这里好像不需要返回
		//		intent.putExtra(ThemeAppInBillingManager.EXTRA_FOR_PACKAGENAME, packageName);
		if (mActivity == null) {
			GOLauncherApp.getApplication().sendBroadcast(intent);
		} else {
			mActivity.sendBroadcast(intent);
		}
	}

	private void savePurchaseState(String packageName) {
		if (mStateManager != null) {
			mStateManager.save(packageName, packageName + ZipResources.ZIP_POSTFIX);
		}
	}
	//	/**
	//	 * 请求加载付费记录
	//	 */
	private void restoreTransactions() {
		//			if (mActivity != null) {
		//				SharedPreferences sharedPreferences = mActivity.getSharedPreferences("appinbilling",
		//						Context.MODE_PRIVATE);
		//				boolean initialized = sharedPreferences.getBoolean(DATA_INITIALIZED, false);
		//				if (!initialized) {
		if (mAppInBillingImpl != null) {
			mAppInBillingImpl.restoreTransactions();
		}
		//				}
		//			}

	}
	//
	//	/**
	//	 * 保存付费记录
	//	 */
	//	private void saveInitializeState() {
	//		if (mActivity != null) {
	//			SharedPreferences sharedPreferences = mActivity.getSharedPreferences("appinbilling",
	//					Context.MODE_PRIVATE);
	//			SharedPreferences.Editor edit = sharedPreferences.edit();
	//			edit.putBoolean(DATA_INITIALIZED, true);
	//			edit.commit();
	//		}
	//	}
	//	private void sendResponseSupportedBroadcast(boolean supported) {
	//		Intent intent = new Intent();
	//		intent.setAction(ThemeAppInBillingManager.ACTION_PURCHASE_SUPPORTED_RESULT);
	//		intent.putExtra(ThemeAppInBillingManager.EXTRA_FOR_SUPPORTED, supported);
	//		if (mActivity == null) {
	//			GOLauncherApp.getApplication().sendBroadcast(intent);
	//		} else {
	//			mActivity.sendBroadcast(intent);
	//		}
	//	}
}
