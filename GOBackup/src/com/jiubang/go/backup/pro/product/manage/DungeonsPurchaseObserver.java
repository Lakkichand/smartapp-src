package com.jiubang.go.backup.pro.product.manage;

import android.content.Context;
import com.jiubang.go.backup.pro.googleplay.BillingService.RequestPurchase;
import com.jiubang.go.backup.pro.googleplay.BillingService.RestoreTransactions;
import com.jiubang.go.backup.pro.googleplay.Consts.PurchaseState;
import com.jiubang.go.backup.pro.googleplay.Consts.ResponseCode;
import com.jiubang.go.backup.pro.googleplay.PurchaseObserver;

/**
 * 应用内购买观察者
 * 
 * @author ReyZhang
 */
public class DungeonsPurchaseObserver extends PurchaseObserver {

	private IGooglePayListener mListener;

	public DungeonsPurchaseObserver(Context context, IGooglePayListener listener) {
		super(context);
		mListener = listener;
	}

	@Override
	public void onBillingSupported(boolean supported) {
		mListener.onIBillingSupported(supported);
	}

	@Override
	public void onPurchaseStateChange(PurchaseState purchaseState, String productId, int quantity,
			long purchaseTime, String developerPayload) {
		mListener.onIPurchaseStateChange(purchaseState, productId, quantity, purchaseTime,
				developerPayload);
	}

	@Override
	public void onRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode) {
		mListener.onIRequestPurchaseResponse(request, responseCode);
	}

	@Override
	public void onRestoreTransactionsResponse(RestoreTransactions request, ResponseCode responseCode) {
		mListener.onIRestoreTransactionsResponse(request, responseCode);
	}

}
