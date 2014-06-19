package com.jiubang.go.backup.pro.product.manage;

import com.jiubang.go.backup.pro.googleplay.BillingService.RequestPurchase;
import com.jiubang.go.backup.pro.googleplay.BillingService.RestoreTransactions;
import com.jiubang.go.backup.pro.googleplay.Consts.PurchaseState;
import com.jiubang.go.backup.pro.googleplay.Consts.ResponseCode;

/**
 * 应用内付费监听器
 * 
 * @author ReyZhang
 */
public interface IGooglePayListener {

	public void onIPurchaseStateChange(PurchaseState purchaseState, String productId, int quantity,
			long purchaseTime, String developerPayload);

	public void onIRequestPurchaseResponse(RequestPurchase request, ResponseCode responseCode);

	public void onIRestoreTransactionsResponse(RestoreTransactions request,
			ResponseCode responseCode);

	public void onIBillingSupported(boolean supported);
}
