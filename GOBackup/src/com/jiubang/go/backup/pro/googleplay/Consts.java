/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.go.backup.pro.googleplay;
//CHECKSTYLE:OFF
/**
 * This class holds global constants that are used throughout the application to
 * support in-app billing.
 */
public class Consts {
	// The response codes for a request, defined by Android Market.
	// android market用于对我们的请求的回应，不能改变
	public enum ResponseCode {
		RESULT_OK, // 此回应指出当前请求已经成功的发送至Android
					// Market服务器了。如果这个代码在我们发出CHECK_BILLING_SUPPORTED请求时被回传，就表示了该猴急支持的Android
					// Market的iap机制。
		RESULT_USER_CANCELED, // 此回应指出使用者在付费页面下按了返回键而非购买商品
		RESULT_SERVICE_UNAVAILABLE, // 此回应指出网络连接失效
		RESULT_BILLING_UNAVAILABLE, // 此回应指出手机不支持iap机制，主因是您指定的API_VERSION无法被Android
									// Market识别，或者使用者没有资格使用iap机制(举例，使用者住在禁止使用iap机制的国家，也有可能用户正在使用的google
									// 账号不支持支付功能)
		RESULT_ITEM_UNAVAILABLE, // 此回应指出Android
									// Market无法找到此请求的应用程式内产品列表的商品。这可能是因為您在发送REQUEST_PURCHAS请求时，产品ID拚错了，或者是在应用程式产品列表上的商品尚未被发布。
		RESULT_DEVELOPER_ERROR, // 此回应指出您的APP试图发送iap请求，但是APP的AnddroidManifest.xml裡却没有加入com.android.vending.BILLING权限。也可能是因為应用程式没有正确的被签署，或者您发送了一个非正确格式的请求，像是忘了传Bundle的key值或者是使用了一个无法被识别的请求类型。
		RESULT_ERROR; // 此回应指出Android
						// Market传来了一个无法预期的错误。举例来说，这个错误是出自您居然想要购买您自己在贩售的商品，而这个商品却没有被Google
						// Checkout允许贩售。

		// Converts from an ordinal value to the ResponseCode
		public static ResponseCode valueOf(int index) {
			ResponseCode[] values = ResponseCode.values();
			if (index < 0 || index >= values.length) {
				return RESULT_ERROR;
			}
			return values[index];
		}
	}

	// The possible states of an in-app purchase, as defined by Android Market.
	// android market用于返回应用内付费应用的购买状态，不能改变
	public enum PurchaseState {
		// Responses to requestPurchase or restoreTransactions.
		PURCHASED, // User was charged for the order.购买成功
		CANCELED, // The charge failed on the server.购买失败（用户取消或者出现异常）
		REFUNDED; // User received a refund for the order.用户进行退款操作

		// Converts from an ordinal value to the PurchaseState
		public static PurchaseState valueOf(int index) {
			PurchaseState[] values = PurchaseState.values();
			if (index < 0 || index >= values.length) {
				return CANCELED;
			}
			return values[index];
		}
	}

	// 这些是您在发送sendBillingRequest()请求时的类型值，不可被改变
	public static final String CHECK_BILLING_SUPPORTED = "CHECK_BILLING_SUPPORTED";// 这个请求是拿来做Android
																					// Market是否支持iap机制的验证用的。通常在您的APP第1次启动时，会发出这个请求。如果您想要启动或取消特定的使用介面的功能(这些功能是提供给付费功能用的)时，这个请求是很有用的。
	public static final String REQUEST_PURCHASE = "REQUEST_PURCHASE";// 这个请求是iap机制的基础，会发送购买讯息至Android
																		// Market。当使用者想要购买应用程式内商品时，您就可以发送此请求。Android
																		// Market会呈现付费使用介面给使用者，接著会处理金融交易。
	public static final String GET_PURCHASE_INFORMATION = "GET_PURCHASE_INFORMATION";// 这个请求会去接收购买状态遭改变的详细资讯。当购买请求付费成功或当使用者在付费途中取消了交易，都会发生购买状态遭改变这个事件。当然，这也可能是使用者请求了购买商品的退费请求。Android市集一旦遇到商品状态遭改变时，就会通知您的APP，因此，这个请求只要在Android市集要求您接收交易信息时再发出即可。
	public static final String CONFIRM_NOTIFICATIONS = "CONFIRM_NOTIFICATIONS";// 这个请求目的是向Android
																				// Market承认您已经收到购买状态遭改变的详细资讯。因此，这个讯息也可以说是向Android市集再次确认您之前发送过GET_PURCHASE_INFORMATION出去，您已经接收到了购买资讯的通知了
	public static final String RESTORE_TRANSACTIONS = "RESTORE_TRANSACTIONS";// 这个请求接收了使用者被管理的购买商品的交易状态。发送此请求的时机，应该只会在当您需要接收使用者的交易状态，由其是您的APP被再次安装或者在手机上第1次安装时。

	// 告知您正在使用的iap机制服务的Android Market版本号。正确值为1
	public static final int API_VERSION = 1;

	/**
	 * This is the action we use to bind to the MarketBillingService.
	 * 这个action用来绑定到MarketBillingService，不能自己定义
	 */
	public static final String MARKET_BILLING_SERVICE_ACTION = "com.android.vending.billing.MarketBillingService.BIND";

	// Intent actions that we send from the BillingReceiver to the
	// BillingService. Defined by this application.
	// 这些Action是用于从BillingReceiver发送到BillingService，我们可以自己定义
	public static final String ACTION_CONFIRM_NOTIFICATION = "com.example.dungeons.CONFIRM_NOTIFICATION";
	public static final String ACTION_GET_PURCHASE_INFORMATION = "com.example.dungeons.GET_PURCHASE_INFORMATION";
	public static final String ACTION_RESTORE_TRANSACTIONS = "com.example.dungeons.RESTORE_TRANSACTIONS";

	// Intent actions that we receive in the BillingReceiver from Market.
	// These are defined by Market and cannot be changed.
	// 这些Action是从Android Market发送到BillingReceiver中的，不能改变定义
	public static final String ACTION_IN_APP_NOTIFY = "com.android.vending.billing.IN_APP_NOTIFY";// 这个回应指出购买状态遭到改变了，也就是说购买成功了，也或者取消、甚至退费了。这个回应包含了一至多个讯息notificationIDs。每一个讯息notificationID都与特定的Android
																									// Market讯息相对应，每个讯息也包含了关於一或多个交易的资讯。当您的APP接收到了IN_APP_NOTIFY这个广播意图后，您就要发送含这个讯息notificationID的GET_PURCHASE_INFORMATION请求出去，接收详细的交易讯息。
	public static final String ACTION_RESPONSE_CODE = "com.android.vending.billing.RESPONSE_CODE";// 这个广播意图包含了从Android
																									// Market而来的回应码，是在您发送了iap金流请求后传来的。回应码可以表示金流请求是否成功的传至Android
																									// Market。这个意图不是用来报告任何购买状态遭改变(像是退费或购买资讯)。范例程式裡将这个广播意图指派到一个叫ACTION_RESPONSE_CODE的实体。
	public static final String ACTION_PURCHASE_STATE_CHANGED = "com.android.vending.billing.PURCHASE_STATE_CHANGED";// 这个广播意图夹带了一或多个交易的详细资讯。
																													// 交易的详细资讯是由JSON字串传来的，此外，这个JSON不仅被签署过，还顺便传来一个数位签章(未加密的状态)。為了帮助您确认您iap机制讯息的安全，您的APP可以验証这个JSON字串传来的数位签章。

	// These are the names of the extras that are passed in an intent from
	// Market to this application and cannot be changed.
	// Market和我们的程序交互数据时，在Intent中所带的数据的key，不能改变定义
	public static final String NOTIFICATION_ID = "notification_id";
	/** 被签署过的JSON字符串 */
	public static final String INAPP_SIGNED_DATA = "inapp_signed_data";
	/** 未加密的数字签名 */
	public static final String INAPP_SIGNATURE = "inapp_signature";
	public static final String INAPP_REQUEST_ID = "request_id";
	public static final String INAPP_RESPONSE_CODE = "response_code";

	// These are the names of the fields in the request bundle.
	// 我们向Market提起请求时所用的Bundle的key，不能改变定义
	public static final String BILLING_REQUEST_METHOD = "BILLING_REQUEST";
	public static final String BILLING_REQUEST_API_VERSION = "API_VERSION";
	public static final String BILLING_REQUEST_PACKAGE_NAME = "PACKAGE_NAME";
	public static final String BILLING_REQUEST_ITEM_ID = "ITEM_ID";
	public static final String BILLING_REQUEST_DEVELOPER_PAYLOAD = "DEVELOPER_PAYLOAD";
	public static final String BILLING_REQUEST_NOTIFY_IDS = "NOTIFY_IDS";
	public static final String BILLING_REQUEST_NONCE = "NONCE";

	public static final String BILLING_RESPONSE_RESPONSE_CODE = "RESPONSE_CODE";
	public static final String BILLING_RESPONSE_PURCHASE_INTENT = "PURCHASE_INTENT";
	public static final String BILLING_RESPONSE_REQUEST_ID = "REQUEST_ID";
	public static long BILLING_RESPONSE_INVALID_REQUEST_ID = -1;

	public static final boolean DEBUG = false;
}
