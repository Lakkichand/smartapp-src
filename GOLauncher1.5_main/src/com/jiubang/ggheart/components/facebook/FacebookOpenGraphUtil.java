package com.jiubang.ggheart.components.facebook;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.INetRecord;
import com.gau.utils.net.operator.IHttpOperator;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.statistics.Statistics;
/**
 * 
 * @author xiangliang
 *
 */
public class FacebookOpenGraphUtil {
	public static final int FACEBOOK_OG_SUCCEESS = 0X01;
	
	public static void requestFacebookOG(final Context context, String pkgName, String themeName, final Handler handler) {

		String url = null;
//		try {
//			url = new String(getUrl(context, pkgName, themeName).getBytes("UTF-8"));
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		url = getUrl(context, pkgName, themeName);
		try {
			THttpRequest request = new THttpRequest(url, null,
					new IConnectListener() {

						@Override
						public void onStart(THttpRequest arg0) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							if (response != null) {
								int type = response.getResponseType();
								switch (type) {
								case FacebookResponse.RESPONSE_TYPE_OG_SUCCESS:
									String url = ((FacebookResponse) response).getOGUrl();
									Message msg = Message.obtain();
									msg.obj = url;
									msg.what = FACEBOOK_OG_SUCCEESS;
									handler.sendMessage(msg);
									break;
								case FacebookResponse.RESPONSE_TYPE_OG_FAILED:
									
									break;
								default:
									break;
								}
							}

						}

						@Override
						public void onException(THttpRequest arg0, int arg1) {

						}
					});
			request.setOperator(new FacebookOGOperator());
			request.addHeader("Content-Type", "application/octet-stream; charset=UTF-8"); // 加入流content-Type说明,不然正式服务器解释不了
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
				public void onException(Exception arg0, Object arg1, Object arg2) {
					// TODO Auto-generated method stub
					
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
	private static String getUrl(Context context, String pkgName, String themeName) {
		StringBuffer buffer = new StringBuffer(GoFacebookUtil.SERVER); // 正式服务器
		buffer.append("funid=" + "3");
		String imei = Statistics.getVirtualIMEI(context);
		String vps = FacebookBackupUtil.getVps(context, imei);
		buffer.append("&vps=" + vps);
		String uid = GoStorePhoneStateUtil.getUid(context);
		buffer.append("&channel=" + uid);
		String fbid = GoFacebookUtil.getUserInfo().getId();
		if (fbid == null) {
			fbid = "-1";
		}
		buffer.append("&facebookid=" + fbid);
		buffer.append("&pkgname=" + pkgName);
		if (themeName != null) {
			try {
			themeName =	themeName.replace(" ", "");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		buffer.append("&title=" + themeName);
		
		return buffer.toString();
	}
	/**
	 * 
	 * @author xiangliang
	 *
	 */
	static class FacebookOGOperator implements IHttpOperator {

		@Override
		public IResponse operateHttpResponse(THttpRequest request,
				HttpResponse response) throws IllegalStateException,
				IOException {
			FacebookResponse facebookResponse = new FacebookResponse();
			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			DataInput dataInput = new DataInputStream(is);
			int funid = dataInput.readInt();
			int length = dataInput.readInt();
			if (length == 0 || funid != 3) {
				facebookResponse
						.setResponseType(FacebookResponse.RESPONSE_TYPE_OG_FAILED);
				return facebookResponse;
			}
			String url = dataInput.readUTF();
			facebookResponse.setOGUrl(url);
			facebookResponse.setResponseType(FacebookResponse.RESPONSE_TYPE_OG_SUCCESS);
			return facebookResponse;
		}

	}
}
