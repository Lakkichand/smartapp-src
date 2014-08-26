package com.youle.gamebox.ui.util;

import org.apache.http.cookie.Cookie;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class CookiesUtil {
	private static CookiesUtil instance = null;
	private static Context mContext = null;
	private String tag = "CookiesUtil";

	public static CookiesUtil getInstance(Context context) {
		if (instance == null) {
			instance = new CookiesUtil();
		}
		mContext = context;
		return instance;
	}

	public void mySynCookies(Cookie sessionCookie, Context context, String url) {
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		if (sessionCookie != null) {
			cookieManager.removeSessionCookie();
			String cookieString = sessionCookie.getName() + "="
					+ sessionCookie.getValue() + "; domain="
					+ sessionCookie.getDomain();
			cookieManager.setCookie(url, cookieString);
			CookieSyncManager.getInstance().sync();
		}
	}

	/**
	 * 同步cookie
	 * 
	 * @param url
	 */
	public void synCookies(String url, String cookies) {
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		// CookieManager.setAcceptFileSchemeCookies(true);
		// cookieManager.removeSessionCookie();//移除
		// cookieManager.removeAllCookie();
		// cookieManager.removeExpiredCookie();
		// String cookieString = cookies.getName() + "=" + cookies.getValue() +
		// "; domain=" + cookies.getDomain();
		// cookieManager.setCookie(url4Load, cookieString);
		cookieManager.setCookie(url, cookies);// cookies是在HttpClient中获得的cookie
		CookieSyncManager.getInstance().sync();

	}

	public void setCookies(String url, String sessionId,String domain) {
		// sid=3bc74d52-f92e-421b-a877-efd7cb8d7a53;

		String cookies = "sid=" + sessionId + ";domain="+domain+";path=/";
		LOGUtil.e("test", "cookies : " + cookies);
		// String cookies = "sid=" +
		// sessionId+";JSESSIONID=abcRVmp3tQdF_fvzf4N8t; ";
		synCookies(url, cookies);
	}

	// 清空cookie
	public void removeCookie() {
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();// 移除
	}

	/**
	 * 得到cookie
	 * 
	 * @param url
	 * @return
	 */
	public String getCookie(String url) {
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		return cookieManager.getCookie(url);

	}
}
