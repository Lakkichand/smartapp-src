package com.jiubang.ggheart.billing;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.net.CryptTool;
import com.jiubang.ggheart.billing.base.Consts;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 应用内付费的管理类
 * 
 * @author zhoujun
 * 
 */
public class ThemeAppInBillingManager {
	/**
	 * 付费成功
	 */
	public static final int PURCHASE_STATE_PURCHASED = 1;

	/**
	 * 付费取消
	 */
	public static final int PURCHASE_STATE_CANCELED = 2;

	private Context mContext;
	private static ThemeAppInBillingManager sInstance;
	private HashMap<String, ThemePurchaseInfo> mListenerMap;

	//	public static final String ACTION_PURCHASE_STATE_RESULT = "go.launcherex.purchase.state.RESPONSE";
	//	public static final String ACTION_PURCHASE_SUPPORTED_RESULT = "go.launcherex.purchase.supported.RESPONSE";

	public static final String EXTRA_FOR_ITEMID = "itemId";
	public static final String EXTRA_FOR_STATE = "state";
	public static final String EXTRA_FOR_PACKAGENAME = "packageName";
	public static final String EXTRA_FOR_SUPPORTED = "supported";

	/**
	 * 统计数据加密密钥
	 */
	private static final String STATISTICS_DATA_ENCRYPT_KEY = "lvsiqiaoil611230";

	/**
	 * 统计数据使用的编码
	 */
	private static final String STATISTICS_DATA_CODE = "UTF-8";

	/**
	 * 统计数据各字段分隔符
	 */
	private static final String STATISTICS_DATA_SEPARATE_STRING = "||";

	/**
	 * 字段值无效用-1表示
	 */
	private static final int STATISTICS_DATA_DEFAULT_VALUE = -1;

	/**
	 * 谷歌市场应用内付费
	 */
	private static final int STATISTICS_DATA_PURCHASE_TYPE = 5;

	/**
	 * 统计数据，未付费
	 */
	private static final int STATISTICS_DATA_NO_PAIED = 0;

	/**
	 * 统计数据，付费成功
	 */
	private static final int STATISTICS_DATA_HAS_PAIED = 1;

	/**
	 * 统计数据，上传的url
	 */
	private static final String STATISTICS_DATA_URL = "http://gostore.3g.cn/gostore/webcontent/function/GetjarPrice.jsp";
	//	private static final String STATISTICS_DATA_URL = "http://192.168.215.121:8080/gostore/webcontent/function/GetjarPrice.jsp";

	private ThemeAppInBillingManager(Context context) {
		mContext = context;
		mListenerMap = new HashMap<String, ThemePurchaseInfo>();
		registerPurchaseReceiver();
	}

	/**
	 * 注册监听付费成功的广播
	 */
	private void registerPurchaseReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ICustomAction.ACTION_PURCHASE_STATE_RESULT);
		intentFilter.addAction(ICustomAction.ACTION_PURCHASE_SUPPORTED_RESULT);
		mContext.registerReceiver(mPurchaseStateReceiver, intentFilter);
	}

	private void unregisterPurchaseReceiver() {
		if (mContext != null) {
			try {
				mContext.unregisterReceiver(mPurchaseStateReceiver);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private BroadcastReceiver mPurchaseStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();
				if (ICustomAction.ACTION_PURCHASE_STATE_RESULT.equals(action)) {
					String itemId = intent.getStringExtra(EXTRA_FOR_ITEMID);
					int state = intent.getIntExtra(EXTRA_FOR_STATE, 0);
					if (Consts.DEBUG) {
						Log.e(Consts.TAG, "mPurchaseStateReceiver itemId:" + itemId + ",state:"
								+ state);
					}
					onStateChange(itemId, state);
				}
				//				else if (ACTION_PURCHASE_SUPPORTED_RESULT.equals(action)) {
				//					mSupported = intent.getBooleanExtra(EXTRA_FOR_SUPPORTED, false);
				//				}

			}
		}
	};

	public synchronized static ThemeAppInBillingManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ThemeAppInBillingManager(context);
		}
		return sInstance;
	}

	/**
	 * 请求付费
	 * @param packageName 付费主题的包名
	 * @param productId  付费主题的产品id
	 * @param purchaseStateListener 付费状态的监听器
	 */
	public void requestPurchase(String packageName, String productId,
			IPurchaseStateListener purchaseStateListener) {
				productId = "android.test.purchased";
		if (packageName == null || productId == null) {
			return;
		}
		//		productId = "android.test.purchased";
		ThemePurchaseInfo themeInfo = new ThemePurchaseInfo(packageName, purchaseStateListener);
		mListenerMap.put(productId, themeInfo);
		purchaseByBroadcast(productId, packageName);
		sendBillingStatisticsData(packageName, STATISTICS_DATA_PURCHASE_TYPE,
				STATISTICS_DATA_NO_PAIED);

	}

	private void sendBillingStatisticsData(final String packageName, final int paidType,
			final int paidSucessed) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String data = createData(packageName, paidType, paidSucessed);
					if (data != null) {
						String statisticsData = getStatisticsData(data);
						DownloadUtil.sendDataByPost(STATISTICS_DATA_URL, statisticsData);
					}
				} catch (Exception e) {

				}

			}
		}).start();
	}

	private String createData(String packageName, int paidType, int paidSucessed) {
		if (mContext != null && packageName != null) {
			StringBuffer stringBuffer = new StringBuffer();
			String imei = Statistics.getVirtualIMEI(mContext);
			String country = RecommAppsUtils.local(mContext);
			stringBuffer.append(imei).append(STATISTICS_DATA_SEPARATE_STRING).append(country)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(packageName)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(paidType)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(paidSucessed)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(STATISTICS_DATA_DEFAULT_VALUE)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(STATISTICS_DATA_DEFAULT_VALUE)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(STATISTICS_DATA_DEFAULT_VALUE)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(STATISTICS_DATA_DEFAULT_VALUE)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(STATISTICS_DATA_DEFAULT_VALUE)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(STATISTICS_DATA_DEFAULT_VALUE)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(STATISTICS_DATA_DEFAULT_VALUE);
			return stringBuffer.toString();
		}
		return null;
	}

	/**
	 * 对统计数据进行加密
	 * @param statistics
	 * @return
	 */
	private String getStatisticsData(String statistics) {
		byte[] statisticsByte = null;
		//对所有的统计数据进行加密
		if (statistics != null) {
			statistics = CryptTool.encrypt(statistics, STATISTICS_DATA_ENCRYPT_KEY);
			try {
				statisticsByte = statistics.getBytes(STATISTICS_DATA_CODE);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		String result = null;
		try {
			result = new String(statisticsByte, STATISTICS_DATA_CODE);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 发送付费请求的广播
	 * @param itemId
	 */
	private void purchaseByBroadcast(String itemId, String packageName) {
		if (mContext != null) {
			Intent intent = new Intent();
			intent.setAction(ICustomAction.ACTION_THEME_PARCHASE);
			intent.putExtra(AppInBillingRequestReceiver.EXTRA_FOR_ITEMID, itemId);
			intent.putExtra(AppInBillingRequestReceiver.EXTRA_FOR_PACKAGENAME, packageName);
			mContext.sendBroadcast(intent);
		}
	}

	/**
	 * <br>功能简述: 退出主题或gostore时，关闭付费的服务，减少占用的内存
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void stopPurchaseService() {
		if (mContext != null) {
			Intent intent = new Intent();
			intent.setAction(ICustomAction.ACTION_THEME_STOP_SERVICE);
			mContext.sendBroadcast(intent);
		}
	}

	/**
	 * 退出时，清空资源
	 */
	public void destory() {
		stopPurchaseService();
		unregisterPurchaseReceiver();

		if (mListenerMap != null) {
			mListenerMap.clear();
			mListenerMap = null;
		}
	}

//	/**
//	 * 付费状态的监听器
//	 * 
//	 * @author zhoujun
//	 * 
//	 */
//	public interface PurchaseStateListener {
//		/**
//		 * 付费结果
//		 * @param purchaseState  付费状态
//		 * @param packageName 付费主题的包名
//		 */
//		public void purchaseState(int purchaseState, String packageName);
//	}

	/**
	 * 
	 * <br>类描述:  付费相关类。负责关联 付费id，包名和回调方法。
	 * <br>功能详细描述:
	 * 
	 * @author  zhoujun
	 * @date  [2012-9-10]
	 */
	private class ThemePurchaseInfo {
		private String mPackageName;
		private IPurchaseStateListener mListener;

		public ThemePurchaseInfo(String packageName, IPurchaseStateListener listener) {
			mPackageName = packageName;
			mListener = listener;
		}

		public String getmPackageName() {
			return mPackageName;
		}

		public IPurchaseStateListener getmListener() {
			return mListener;
		}
	}

	public void onStateChange(String itemId, int state) {
		if (Consts.DEBUG) {
			Log.d(Consts.TAG, "onStateChange itemId:" + itemId + ",state:" + state);
		}
		if (mListenerMap != null) {
			ThemePurchaseInfo themeInfo = mListenerMap.get(itemId);
			if (themeInfo != null) {
				IPurchaseStateListener listener = themeInfo.getmListener();
				String packageName = themeInfo.getmPackageName();
				if (listener != null) {
					listener.purchaseState(state, packageName);
					if (state == PURCHASE_STATE_PURCHASED) {
						sendBillingStatisticsData(packageName, STATISTICS_DATA_PURCHASE_TYPE,
								STATISTICS_DATA_HAS_PAIED);
					}
				}
				mListenerMap.remove(itemId);
			} else {
				if (Consts.DEBUG) {
					Log.e(Consts.TAG, "ThemePurchaseInfo is null,thread is killed");
				}
			}

		}
	}
}
