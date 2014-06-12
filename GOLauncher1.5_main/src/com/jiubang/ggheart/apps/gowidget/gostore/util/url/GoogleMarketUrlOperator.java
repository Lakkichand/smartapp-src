package com.jiubang.ggheart.apps.gowidget.gostore.util.url;

import java.util.HashMap;

import android.content.Context;

import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 */
public class GoogleMarketUrlOperator implements IUrlOperator {

	private IUrlOperator mUrlOperator = null;

	private static GoogleMarketUrlOperator smSelf = null;

	private GoogleMarketUrlOperator() {
	}

	public synchronized static GoogleMarketUrlOperator getInstance() {
		if (null == smSelf) {
			smSelf = new GoogleMarketUrlOperator();
		}
		return smSelf;
	}

	@Override
	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap) {
		// TODO Auto-generated method stub
		boolean result = false;
		if (context != null && urlHashMap != null && urlHashMap.size() > 0) {
			String googleMarketUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_GOOGLE_MARKET);
			String webMarketUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_WEB_GOOGLE_MARKET);
			if (googleMarketUrl != null && !"".equals(googleMarketUrl.trim())
					&& GoStoreAppInforUtil.isExistGoogleMarket(context)) {
				// 去掉GA
				// googleMarketUrl = googleMarketUrl.trim()
				// + LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
				if (!GoStoreOperatorUtil.gotoMarket(context, googleMarketUrl)) {
					// 如果跳转电子市场失败
					if (mUrlOperator != null) {
						result = mUrlOperator.handleUrl(context, urlHashMap);
					}
				} else {
					result = true;
				}
			} else if (webMarketUrl != null && !"".equals(webMarketUrl.trim())) {
				// 跳转web版地址
				// 去掉GA
				// webMarketUrl = webMarketUrl +
				// LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK;
				GoStoreOperatorUtil.gotoBrowser(context, webMarketUrl);
				result = true;
			} else {
				// 如果没有Google电子市场的地址或者用户没有安装电子市场
				if (mUrlOperator != null) {
					result = mUrlOperator.handleUrl(context, urlHashMap);
				}
			}
		}
		return result;
	}

	public IUrlOperator getUrlOperator() {
		return mUrlOperator;
	}

	public void setUrlOperator(IUrlOperator urlOperator) {
		this.mUrlOperator = urlOperator;
	}

	@Override
	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName) {
		return false;
	}

	@Override
	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap,
			Class<? extends IAidlDownloadListener.Stub>[] listenerClazzArray,
			String customDownloadFileName, int iconType, String iconUrlInfo, int module) {
		return false;
	}

}
