package com.jiubang.ggheart.apps.gowidget.gostore.util.url;

import java.util.HashMap;

import android.content.Context;

import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 */
public class OtherUrlOperator implements IUrlOperator {

	private IUrlOperator mUrlOperator = null;

	private static OtherUrlOperator smSelf = null;

	private OtherUrlOperator() {
	}

	public synchronized static OtherUrlOperator getInstance() {
		if (null == smSelf) {
			smSelf = new OtherUrlOperator();
		}
		return smSelf;
	}

	@Override
	public boolean handleUrl(Context context, HashMap<Integer, String> urlHashMap) {
		// TODO Auto-generated method stub
		boolean result = false;
		if (context != null && urlHashMap != null && urlHashMap.size() > 0) {
			String otherUrl = urlHashMap.get(GoStorePublicDefine.URL_TYPE_OTHER_ADDRESS);
			if (otherUrl != null && !"".equals(otherUrl.trim())) {
				// 如果有其它地址
				// 目前是直接跳转浏览器，以后可能会有各种处理
				GoStoreOperatorUtil.gotoBrowser(context, otherUrl);
				result = true;
			} else {
				// 如果没有其它地址
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
