package com.jiubang.ggheart.billing;

/**
 * 
 * <br>类描述: 付费状态的接口
 * <br>功能详细描述:
 * 
 * @author  zhoujun
 * @date  [2012-9-17]
 */
public interface IPurchaseStateListener {

	/**
	 * 付费成功
	 */
	public static final int PURCHASE_STATE_PURCHASED = 1;

	/**
	 * 付费取消或失败
	 */
	public static final int PURCHASE_STATE_CANCELED = 2;
	
	/**
	 * 付费结果
	 * @param purchaseState  付费状态
	 * @param packageName 付费主题的包名
	 */
	public void purchaseState(int purchaseState, String packageName);
}
