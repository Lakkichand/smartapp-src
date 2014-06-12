package com.jiubang.ggheart.appgame.base.utils;

import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataDownload;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataParser;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataParser.LocalJSON;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;


/**
 * 应用市场公共处理util
 * @author zhouxuewen
 *
 */
public class GoMarketOperatorUtil {
	
	private final static int AUTO_GET_DATA_DELAYED = 2 * 60 * 1000;

	/**
	 * 后台自动加载数据至缓存
	 */
	public static void autoGetData(final Context context) {
		//　获取应用市场缓存是否存在
		String key = ClassificationDataDownload.buildClassificationKey(
				ClassificationDataBean.TOP_TYPEID, 1);
		boolean isCacheExist = AppCacheManager.getInstance().isCacheExist(key);

		// 自动加载要满足用户是ＷＩＦＩ状态及没有缓存两人个条件
		if (GoStorePhoneStateUtil.isWifiEnable(context) && !isCacheExist) {
			new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String url = ClassificationDataDownload.getUrl(context);
					//TODO:XIEDEZHI 这里读取本地数据会不会很慢
					List<LocalJSON> typeIdList = ClassificationDataParser.getLocalSubTypeidList(0);
					int[] typeIdArray = new int[typeIdList.size()];
					for (int i = 0; i < typeIdList.size(); i++) {
						typeIdArray[i] = typeIdList.get(i).mTypeId;
					}
					final int[] typeIds = typeIdArray;
					final int pageId = 1;
					final JSONObject postdata = ClassificationDataDownload.getPostJson(context, typeIds,
							MainViewGroup.ACCESS_FOR_SHORTCUT, pageId, 0);
					THttpRequest request = null;
					try {
						request = new THttpRequest(url, postdata.toString().getBytes(),
								new IConnectListener() {

									@Override
									public void onStart(THttpRequest request) {
									}

									@Override
									public void onFinish(THttpRequest request,
											IResponse response) {
										if (response != null
												&& response.getResponse() != null
												&& (response.getResponse() instanceof JSONObject)) {
											try {
												JSONObject json = (JSONObject) response
														.getResponse();
												List<ClassificationDataBean> beans = ClassificationDataDownload
														.getClassificationData(json, postdata,
																context, typeIds,
																pageId, 1, true);
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}

									@Override
									public void onException(THttpRequest request,
											int reason) {
									}
								});
					} catch (Exception e) {
						Log.i("GoMarketOperatorUtil", "autoGetData has exception = " + e.getMessage());
						return;
					}
					if (request != null) {
						// 设置备选url
						try {
							request.addAlternateUrl(ClassificationDataDownload.getAlternativeUrl(context));
						} catch (Exception e) {
							e.printStackTrace();
						}
						// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
						request.setRequestPriority(Thread.MAX_PRIORITY);
						request.setOperator(new AppJsonOperator());
						AppHttpAdapter httpAdapter = AppHttpAdapter
								.getInstance(context);
						httpAdapter.addTask(request, true);
					}
				
				}
			}, AUTO_GET_DATA_DELAYED);
		}
	}

}
