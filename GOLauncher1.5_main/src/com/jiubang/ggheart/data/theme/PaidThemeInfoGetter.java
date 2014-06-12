package com.jiubang.ggheart.data.theme;

import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.gau.utils.cache.encrypt.CryptTool;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.INetRecord;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.gohandbook.StringOperator;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 *获取四合一付费主题相关信息类
 * @author liulixia
 *
 */
public class PaidThemeInfoGetter {
	//正式地址
	private static final String PAID_THEME_INFO_URL = "http://gostore.3g.cn/gostore/webcontent/function/AllGetjarPrice.jsp?appuid=1&channel=";
	//测试地址：
//    private static final String PAID_THEME_INFO_URL = "http://61.145.124.70:8081/gostore/webcontent/function/AllGetjarPrice.jsp?appuid=1&channel=";
    //文件保存路径
    private static final String FILE_PATH = LauncherEnv.Path.PAID_THEME_INFO_PATH + "themeinfo.txt";
    //字符串加密密钥
    public final static String ENCRYPT_KEY = "PAIDTHEMEINFO130122";
    
    private final static boolean DEBUG = false;
    private final static String TAG = "llx";
    
    private final static int AUTO_GET_DATA_DELAYED = 1 * 10 * 1000;
    
	/**
	 * 服务器请求获取付费主题相关信息
	 */
	public static void getPaidThemesInfo(final Context context) {
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			public void run() {
				String uid = GoStorePhoneStateUtil.getUid(context);
				String lang = String.format("%s_%s",
						Locale.getDefault().getLanguage().toLowerCase(),
						getCountry(context));
				if (DEBUG) {
					Log.i(TAG, "uid = " + uid + ",lang = " + lang);
				}
				final String url = PAID_THEME_INFO_URL + uid + "&lang=" + lang;
				if (DEBUG) {
					Log.i(TAG, "url = " + url);
				}
				try {
					THttpRequest request = new THttpRequest(url, null,
							new IConnectListener() {
						@Override
						public void onStart(THttpRequest arg0) {
							//Log.e("lch", "onStart");
						}
						
						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							//解析请求数据
							if (response != null && response.getResponse() != null
									&& (response.getResponse() instanceof String)) {
								final String responseContent = response.getResponse().toString();
								getResponseWriteToSD(responseContent);
							}
						}
						
						@Override
						public void onException(THttpRequest arg0, int arg1) {
						}
					});
					
					request.setOperator(new StringOperator()); //设置返回数据类型-字符串
					
					//设置报错提示
					request.setNetRecord(new INetRecord() {
						
						@Override
						public void onTransFinish(THttpRequest arg0, Object arg1, Object arg2) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onStartConnect(THttpRequest arg0, Object arg1, Object arg2) {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onException(Exception e, Object arg1, Object arg2) {
							//e.printStackTrace(); //打印出HTTP请求真实的错误信息
						}
						
						@Override
						public void onConnectSuccess(THttpRequest arg0, Object arg1, Object arg2) {
							// TODO Auto-generated method stub
							
						}
					});
					SimpleHttpAdapter httpAdapter = SimpleHttpAdapter.getInstance(context);
					httpAdapter.addTask(request);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, AUTO_GET_DATA_DELAYED);
	}
	
	private static void getResponseWriteToSD(final String responseContent) {
		if (DEBUG) {
			Log.i(TAG, "responseContent = " + responseContent);
		}
		if (FileUtil.isSDCardAvaiable()) {
			new Thread() {
				public void run() {
					String encryptString = CryptTool.encrypt(responseContent, ENCRYPT_KEY);
					FileUtil.saveByteToSDFile(encryptString.getBytes(), FILE_PATH);
				}
			}.start();
		}
	}
	
	
	/**
	 * 获取SIM卡所在的国家
	 * 
	 * @param context
	 * @return 当前手机sim卡所在的国家，如果没有sim卡，取本地语言代表的国家
	 */
	private static  String getCountry(Context context) {
		String ret = null;
		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso();
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}

		if (ret == null || ret.equals("")) {
			ret = Locale.getDefault().getCountry().toLowerCase();
		}
		if (DEBUG) {
			Log.d(TAG, "local :" + ret);
		}
		return null == ret ? "error" : ret;
	}
}
