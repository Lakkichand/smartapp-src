package com.jiubang.ggheart.billing;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.billing.base.BillingService;
import com.jiubang.ggheart.billing.base.Consts;
import com.jiubang.ggheart.billing.base.PurchaseObserver;
import com.jiubang.ggheart.billing.base.ResponseHandler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 完成应用内付费的功能
 * 
 * @author zhoujun
 * 
 */
public class AppInBillingImplement {
	private BillingService mBillingService;
	private Context mContext;

	public AppInBillingImplement(Context context) {
		mBillingService = new BillingService();
		mContext = context;
		mBillingService.setContext(context);
	}

	public void registerObserver(PurchaseObserver observer) {
		ResponseHandler.register(observer);
	}

	public void unregisterObserver(PurchaseObserver observer) {
		ResponseHandler.unregister(observer);
	}

	/**
	 * 检查是否支持应用内付费
	 * 
	 * @param itemType
	 *            付费类型
	 * @return
	 */
	public void checkBillingSupported(String itemType) {
		mBillingService.checkBillingSupported(itemType);
	}

	/**
	 * 请求付费
	 * 
	 * @param productId
	 * @param itemType
	 * @param developerPayload
	 */
	public void requestPurchase(String productId, String itemType, String developerPayload) {
		boolean status = mBillingService.requestPurchase(productId, itemType, developerPayload);
		if (!status) {
			Toast.makeText(
					GOLauncherApp.getApplication(),
					GOLauncherApp.getApplication().getString(
							R.string.appinbilling_start_google_play), Toast.LENGTH_SHORT).show();
		}
	}

	public void restoreTransactions() {
		mBillingService.restoreTransactions();
	}

	public void destory() {
		if (Consts.DEBUG) {
			Log.d(Consts.TAG, "AppInBillingImplement destory is running ");
		}
		if (mBillingService != null) {
			mBillingService.unbind();
		}
		if (mContext != null) {
			Intent intent = new Intent();
			intent.setClass(mContext, BillingService.class);
			mContext.stopService(intent);
		}
	}
}
