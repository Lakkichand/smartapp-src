package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.gau.utils.cache.encrypt.CryptTool;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.go.util.log.LogUnit;
import com.go.util.window.WindowControl;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageBaseBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageContentBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.desks.net.NetOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 类描述: MessageHttp类
 * 功能详细描述: 主要处理一些网络请求和解析JSon数据的操作
 * @date  [2012-9-28]
 */
public class MessageHttp {

	public static final String PVERSION = "1.3"; // 请求协议版本号
	private Context mContext;
	private PraseListener mListener;
	private static final int POST_TYPE_GET_LIST = 1; // 获取消息列表
	private static final int POST_TYPE_GET_MSG = 2; // 获取一个消息内容
	private static final int POST_TYPE_GET_URL = 3; //获取桌面后台链接

	public static final int STATUS_IDLE = 0;
	public static final int STATUS_POSTING = 1;
	public static final int STATUS_GETING_MSG = 2;
	public static final int STATUS_GETING_URL = 3;
	private HttpPost mHttpPost;
	private int mStatus;

	private static final int REQUEST_TIMEOUT = 7 * 1000; //设置请求超时7秒钟  
	private static final int RESPONSE_TIMEOUT = 7 * 1000;  //设置等待数据超时时间7秒钟  
	private static String sCHANNAEL = null;
	private static String sURL = ConstValue.HOSTURL_BASE;
	private byte[] mLock = new byte[0];

	public MessageHttp(Context context) {
		mContext = context;
	}

	private static void compoundNameValuePairs(Context context, JSONObject data, int auto) {
		if (context != null) {
			if (data == null) {
				data = new JSONObject();
			}
			// vps
			String imei = GoStorePhoneStateUtil.getVirtualIMEI(context);
			try {
				data.put("vps", HttpUtil.getVps(context, imei));
				data.put("launcherid", imei);
				// channel
				sCHANNAEL = GoStorePhoneStateUtil.getUid(context);
				if (ConstValue.DEBUG && FileUtil.isFileExist(LauncherEnv.Path.MESSAGECENTER_PATH + "properties.txt")) {
					String info = EncodingUtils.getString(FileUtil.
							getByteFromSDFile(LauncherEnv.Path.MESSAGECENTER_PATH + "properties.txt"), "UTF-8");
					if (info != null && info.split("#").length == 2) {
						String[] properties = info.split("#");
						sCHANNAEL = properties[0];
						sURL = properties[1];
					} 
				}
				
				if (ConstValue.DEBUG) {
					LogUnit.diyInfo(ConstValue.MSG_TAG, "uid = " + sCHANNAEL);
					LogUnit.diyInfo(ConstValue.MSG_TAG, "url = " + sURL);
					LogUnit.diyInfo(ConstValue.MSG_TAG, "屏幕密度： " + WindowControl.getDensity(context));
				}
				data.put("channel", sCHANNAEL);
				// lang 带上区域信息，如zh_cn,en_us
				Locale locale = Locale.getDefault();
				String language = String.format("%s_%s", locale.getLanguage().toLowerCase(), locale
						.getCountry().toLowerCase());
				data.put("lang", language);
				data.put("local", locale.getCountry().toLowerCase());
				// pversion 协议版本号
				data.put("pversion", PVERSION);
				// isfee
				String isFee = "1";
				if (GoStorePhoneStateUtil.isCnUser(context) || !AppUtils.isMarketExist(context)) {
					// 若是中国用户，或是未安装电子市场，则均为不能收费用户。
					isFee = "0";
				}
				data.put("isfee", isFee);
				String curVersion = context.getString(R.string.curVersion);
				int index = curVersion.indexOf("beta");

				if (index > 0) {
					curVersion = curVersion.substring(0, index);
				} else if ((index = curVersion.indexOf("Beta")) > 0) {
					curVersion = curVersion.substring(0, index);
				}
				data.put("cversion", curVersion);

				PackageManager pm = context.getPackageManager();
				PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
				data.put("vcode", info.versionCode);
				data.put("Isauto", auto);
				//TODO 产品id
//				data.put("pid", "d6addc867c35e17ece2d286d892e9890");
				data.put("pid", null);
				data.put("spappcenter", 1);
				data.put("density", WindowControl.getDensity(context));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public synchronized void postUpdateRequest(int auto) {
		if (mStatus != STATUS_IDLE) {
			return;
		}
		if (null != mContext) {
			mStatus = STATUS_POSTING;
			JSONObject phead = getPhead(mContext, null, auto);
			JSONObject postdataJsonObject = getPostJson(mContext, phead, POST_TYPE_GET_LIST, null);
			String url = HttpUtil.getUrl(ConstValue.URL_GET_MSG_LIST, sURL);
			mHttpPost = new HttpPost(url);
			// 绑定到请求 Entry
			StringEntity se;
			JSONObject msgList = null;
			MessageListBean msgsBean = null;
			try {
				se = new StringEntity(postdataJsonObject.toString());
				mHttpPost.setEntity(se);
				//设置请求时间的TIME_OUT 和回应的时间的Time_OUT
				BasicHttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams, RESPONSE_TIMEOUT);
				HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(mHttpPost);

				msgList = parseMsgListStreamData(httpResponse.getEntity().getContent());
				msgsBean = new MessageListBean();

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} finally {
				if (mHttpPost != null) {
					mHttpPost = null;
				}
			}
			if (parseMsgList(msgList, msgsBean) && mListener != null) {
				mListener.listParseFinish(true, msgsBean);
			} else {
				mListener.listParseFinish(false, null);
			}
		}
		mStatus = STATUS_IDLE;
	}
	
	public synchronized void postGetUrlRequest() {
		if (mStatus != STATUS_IDLE) {
			return;
		}
		if (null != mContext) {
			mStatus = STATUS_GETING_URL;
			JSONObject head = getPhead(mContext, null, 0);
			JSONObject postdataJson = getPostJson(mContext, head, POST_TYPE_GET_URL, null);
			if (ConstValue.DEBUG) {
				Log.i(ConstValue.MSG_TAG, postdataJson.toString());
			}
			String url = HttpUtil.getUrl(ConstValue.URL_GET_URL, sURL);
			if (ConstValue.DEBUG) {
				LogUnit.diyInfo(ConstValue.MSG_TAG, "postGetUrlRequest url = " + url);
			}
			mHttpPost = new HttpPost(url);
			StringEntity se;
			JSONObject msg = null;
			JSONObject sUrls = null;
			try {
				se = new StringEntity(postdataJson.toString());
				mHttpPost.setEntity(se);
				//设置请求时间的TIME_OUT 和回应的时间的Time_OUT
				BasicHttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams, RESPONSE_TIMEOUT);
				HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(mHttpPost);
				
				msg = parseMsgContentStreamData(httpResponse.getEntity().getContent());
				if (msg != null && msg.has("surls")) {
					sUrls = msg.getJSONObject("surls");
					if (sUrls != null) {
						if (sUrls.has("2")) {
							String getUrl = sUrls.getString("2");
							if (getUrl != null && !getUrl.equals("")) {
								NetOperator.connectToNet(GOLauncherApp.getContext(), getUrl);
							}
						}
						if (sUrls.has("3")) {
							String getFacebookInfo = sUrls.getString("3");
							saveFacebookInfo(getFacebookInfo);
						}
					}
				}
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} finally {
				if (mHttpPost != null) {
					mHttpPost = null;
				}
			}
		}
		mStatus = STATUS_IDLE;
	}

	// public void postUpdateRequest(){
	// if( mStatus != STATUS_IDLE )
	// {
	// return;
	// }
	// if(null != mContext){
	// JSONObject obj = new JSONObject();
	//
	// byte[] postData = getPostData(mContext, obj,POST_TYPE_GET_LIST,null);
	// THttpRequest request;
	// try {
	// String url = HttpUtil.getUrl(ConstValue.URL_GET_MSG_LIST);
	// request = new THttpRequest(url, postData, new IConnectListener() {
	//
	// @Override
	// public void onStart(THttpRequest request) {
	// // TODO Auto-generated method stub
	// mStatus = STATUS_POSTING;
	// }
	//
	// @Override
	// public void onFinish(THttpRequest request, IResponse response) {
	// // TODO Auto-generated method stub
	// if(response.getResponseType() == IResponse.RESPONSE_TYPE_BYTEARRAY){
	// JSONObject jsonObject = (JSONObject) response.getResponse();
	// MessageListBean msgsBean = new MessageListBean();
	// if(praseMsgList(jsonObject,msgsBean) && mListener != null)
	// {
	// mListener.listPraseFinish(true,msgsBean);
	// }
	// }
	// mStatus = STATUS_IDLE;
	// }
	//
	// @Override
	// public void onException(THttpRequest request, int reason) {
	// // TODO Auto-generated method stub
	// if(mListener != null)
	// {
	// mListener.listPraseFinish(false,null);
	// }
	// mStatus = STATUS_IDLE;
	//
	// }
	// });
	// MessageListStreamHttpOperator operator = new
	// MessageListStreamHttpOperator();
	// request.setOperator(operator);
	// SimpleHttpAdapter.getInstance(mContext).addTask(request);
	// } catch (IllegalArgumentException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (URISyntaxException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }

	// /**
	// *
	// * @param context
	// * @param nameValuePairs
	// * @param msgId 消息ID
	// * @return
	// */
	public static byte[] getPostData(Context context, JSONObject phead, int type, String msgId) {
		if (phead == null) {
			phead = new JSONObject();
		}
		compoundNameValuePairs(context, phead, 0);
		byte[] postData = null;
		try {
			JSONObject request = new JSONObject();
			request.put("phead", phead);
			if (type == POST_TYPE_GET_MSG) {
				request.put("id", msgId);
			} else {
				long timeStamp = HttpUtil.getLastUpdateMsgTime(context);
				request.put("lts", timeStamp);
			}
			postData = request.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return postData;
	}

	/**
	 * <br>功能简述:头信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param phead
	 * @param auto
	 * @return
	 */
	public static JSONObject getPhead(Context context, JSONObject phead, int auto) {
		if (phead == null) {
			phead = new JSONObject();
		}
		compoundNameValuePairs(context, phead, auto);
		return phead;
	}

	public static JSONObject getPostJson(Context context, JSONObject phead, int type, String msgId) {
		JSONObject request = new JSONObject();
		// byte[] postData = null;
		try {
			request.put("phead", phead);
			if (type == POST_TYPE_GET_MSG) {
				request.put("id", msgId);
			} else if (type == POST_TYPE_GET_URL) {
				request.put("types", "2#3");
			} else {
				long timeStamp = HttpUtil.getLastUpdateMsgTime(context);
				request.put("lts", timeStamp);
			}
			if (ConstValue.DEBUG) {
//				Log.i(ConstValue.MSG_TAG, "request = " + request.toString());
				LogUnit.diyInfo(ConstValue.MSG_TAG, "requestToServer : " + request.toString());
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return request;
	}

	private boolean parseMsgList(JSONObject obj, MessageListBean bean) {
		boolean ret = false;
		if (obj == null) {
			return ret;
		}
		
		try {
			long lts = obj.getLong(MessageListBean.TAG_LTS);
			JSONArray array = obj.getJSONArray(MessageListBean.TAG_MSGS);
			HttpUtil.saveLastUpdateMsgTime(mContext, lts);
			bean.praseMsgsHead(array);			
			ret = true;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	public synchronized void postGetMsgContentRequest(String id) {
		if (mStatus != STATUS_IDLE) {
			return;
		}
		mStatus = STATUS_GETING_MSG;
		if (null != mContext) {
			JSONObject head = getPhead(mContext, null, 0);
			JSONObject postdataJson = getPostJson(mContext, head, POST_TYPE_GET_MSG, id);
			if (ConstValue.DEBUG) {
				Log.i(ConstValue.MSG_TAG, postdataJson.toString());
			}
			String url = HttpUtil.getUrl(ConstValue.URL_GET_MSG_CONTENT, sURL);
			mHttpPost = new HttpPost(url);
			StringEntity se;
			JSONObject msg = null;
//			MessageContentBean msgContent = null;
			String contentUrl = "";
			try {
				se = new StringEntity(postdataJson.toString());
				mHttpPost.setEntity(se);
				//设置请求时间的TIME_OUT 和回应的时间的Time_OUT
				BasicHttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams, RESPONSE_TIMEOUT);
				HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(mHttpPost);

				msg = parseMsgContentStreamData(httpResponse.getEntity().getContent());
//				msgContent = new MessageContentBean();
				contentUrl = msg.getString("msgurl");

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (mHttpPost != null) {
					mHttpPost = null;
				}
			}
//			if (parseMsg(msg, msgContent) && mListener != null) {
//				mListener.msgParseFinish(true, msgContent);
//			} else {
//				mListener.msgParseFinish(false, null);
//			}
//			if (mListener != null && contentUrl != null && !contentUrl.equals("")) {
//				mListener.msgParseFinish(true, contentUrl);
//			} else {
//				mListener.msgParseFinish(false, null);
//			}
		}
		mStatus = STATUS_IDLE;
	}

	private boolean parseMsg(JSONObject obj, MessageContentBean msgContent) {
		boolean ret = false;
		if (obj != null) {
			try {
				msgContent.mId = obj.getString(MessageBaseBean.TAG_MSG_ID);
				msgContent.mType = obj.getInt(MessageBaseBean.TAG_MSG_TYPE);
				msgContent.mTitle = obj.getString(MessageBaseBean.TAG_MSG_TITLE);
				msgContent.mMsgTimeStamp = obj.getString(MessageBaseBean.TAG_MSG_TIME);
				msgContent.praseWidget(obj.getJSONArray(MessageContentBean.sTAG_MSGWIDGETS));
				ret = true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;

	}

	public void setPraseListener(PraseListener listener) {
		mListener = listener;
	}

	public int getStatus() {
		return mStatus;
	}

	private JSONObject parseMsgListStreamData(final InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String jsonString;
		try {
			jsonString = reader.readLine();
			if (jsonString != null) {
				if (ConstValue.DEBUG) {
//					Log.i(ConstValue.MSG_TAG, "list:" + jsonString);
					LogUnit.diyInfo(ConstValue.MSG_TAG, "responseFromServer : " + jsonString);
				}
				JSONObject jsonObject = new JSONObject(jsonString);
				JSONObject result = jsonObject.getJSONObject(MessageListBean.TAG_RESULT);
				int status = result.getInt(MessageListBean.TAG_STATUS);
				if (status == ConstValue.STATTUS_OK) {
					String apkSignatures = "";
					String apkNames = "";
					if (result.has(MessageListBean.TAG_APKSIGNNATURES)) {
						apkSignatures = result.getString(MessageListBean.TAG_APKSIGNNATURES);
					}
					if (result.has(MessageListBean.TAG_APKNAMES)) {
						apkNames = result.getString(MessageListBean.TAG_APKNAMES);
					}
					writeToSDCard(apkSignatures + "#" + apkNames);
					return jsonObject;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}
	
	/**
	 * 启动异步线程将内容写进sd卡
	 * @param content
	 */
	private void writeToSDCard(final String content) {
		if (FileUtil.isSDCardAvaiable()) {
			try {
				new Thread() {
					public void run() {
						String encryptString = CryptTool.encrypt(content, ConstValue.ENCRYPT_KEY);
						if (FileUtil.isFileExist(LauncherEnv.Path.MESSAGECENTER_PATH + "filterinfo.txt")) {
							String info = EncodingUtils.getString(FileUtil.
									getByteFromSDFile(LauncherEnv.Path.MESSAGECENTER_PATH + "filterinfo.txt"), "UTF-8");
							if (!info.equals(encryptString)) {
								FileUtil.saveByteToSDFile(encryptString.getBytes(),
										LauncherEnv.Path.MESSAGECENTER_PATH + "filterinfo.txt");
							}
						} else {
							FileUtil.saveByteToSDFile(encryptString.getBytes(),
									LauncherEnv.Path.MESSAGECENTER_PATH + "filterinfo.txt");
						}
					}
				}.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public JSONObject parseMsgContentStreamData(InputStream in) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String jsonString;
		try {
			jsonString = reader.readLine();
			if (jsonString != null) {
				if (ConstValue.DEBUG) {
					Log.i(ConstValue.MSG_TAG, "msg:" + jsonString);
				}
				JSONObject jsonObject = new JSONObject(jsonString);
				JSONObject result = jsonObject.getJSONObject(MessageListBean.TAG_RESULT);
				int status = result.getInt(MessageListBean.TAG_STATUS);
				if (status == ConstValue.STATTUS_OK) {
					return jsonObject;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}

	public void abortPost() {
		if (mHttpPost != null && !mHttpPost.isAborted()) {
			mHttpPost.abort();
			mHttpPost = null;
		}
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param url
	 * @param dir
	 * @param fileName
	 */
	public static boolean downloadZipRes(final String url, final String path, final String fileName) {
		boolean bRet = false;
		if (url == null || url.equals("")) {
			return bRet;
		}
		HttpURLConnection conn = null;
		InputStream is = null;
		FileOutputStream out = null;
		try {
			URL url_im = new URL(url);
			conn = (HttpURLConnection) url_im.openConnection();
			conn.connect();
			is = conn.getInputStream();
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdir();
			}
			File zipFile = new File(path + fileName);
			if (!zipFile.exists()) {
				zipFile.createNewFile();
				out = new FileOutputStream(zipFile);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = is.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
				out.flush();
			}
			bRet = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if (ConstValue.DEBUG) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (conn != null) {
				conn.disconnect();
			}
		}
		return bRet;
	}

	public synchronized boolean updateThemeNotifyStatisticsData(int type, long uuid, boolean isShow) {
		boolean bRet = false;
		if (!Machine.isNetworkOK(GOLauncherApp.getContext())) {
			return bRet;
		}
		if (null != mContext) {
			try {

				JSONObject phead = getPhead(mContext, null, 0);
				JSONObject data = new JSONObject();
				data.put("phead", phead);
				String config = null;
				String showConfig;
				if (type == ThemeConstants.STATICS_ID_FEATURED_NOTIFY) {
					config = IPreferencesIds.SHAREDPREFERENCES_MSG_THEME_NOTIFY_STATICS_DATA;
					showConfig = IPreferencesIds.SHAREDPREFERENCES_MSG_THEME_NOTIFY_SHOW_STATICS_DATA;
				} else {
					config = IPreferencesIds.SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_STATICS_DATA;
					showConfig = IPreferencesIds.SHAREDPREFERENCES_MSG_LOCKER_THEME_NOTIFY_SHOW_STATICS_DATA;
				}
				PreferencesManager manager = new PreferencesManager(mContext, config,
						Context.MODE_PRIVATE);
				int pushCnt = manager.getInt(IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES, 0);
				int msgClickCnt = manager.getInt(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES,
						0);
				int showCnt = 0;
				if (isShow) {
					PreferencesManager pm = new PreferencesManager(mContext, showConfig,
							Context.MODE_PRIVATE);
					showCnt = pm.getInt(showConfig, 0);
				}

				String rst = String.valueOf(System.currentTimeMillis()) + "#" + type + "#"
						+ pushCnt + "#" + showCnt + "#" + MessageBaseBean.VIEWTYPE_STATUS_BAR + "#"
						+ msgClickCnt + "#" + uuid + "#" + 0 + "#" + 0;
				data.put("rst", rst);
				String url = HttpUtil.getUrl(ConstValue.URLPOST_MSG_STATICDATA, sURL);
				HttpPost post = new HttpPost(url);
				// 绑定到请求 Entry
				StringEntity se;
				se = new StringEntity(data.toString());
				post.setEntity(se);
				//设置请求时间的TIME_OUT 和回应的时间的Time_OUT
				BasicHttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams, RESPONSE_TIMEOUT);
				HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(post);
				InputStream in = httpResponse.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String jsonString = reader.readLine();
				JSONObject jsonResult = new JSONObject(jsonString);
				String ret = jsonResult.getString("isok");
				int ok = Integer.valueOf(ret);
				if (ok == ConstValue.STATTUS_OK) {
					bRet = true;
					manager.clear();
					if (isShow) {
						new PreferencesManager(mContext, showConfig, Context.MODE_PRIVATE).clear();
					}
				}

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return bRet;
	}


	public synchronized boolean updateStatisticsData(Vector<MessageHeadBean> beans, int entrance, long uuid, String staticticsType, String clickItemName) {
		boolean bRet = false;
		if (!Machine.isNetworkOK(GOLauncherApp.getContext())) {
			return bRet;
		}
		if (null != mContext && beans != null && beans.size() > 0) {
			try {
				JSONObject phead = getPhead(mContext, null, 0);
				JSONObject data = new JSONObject();
				data.put("phead", phead);
				
				StringBuilder sb = new StringBuilder();
				String rst = "";
				for (int i = 0; i < beans.size(); i++) {
					int pushCnt = 0;
					int showCnt = 0;
					int msgClickCnt = 0;
					int btnCnt = 0;
					int closeBtnClickCnt = 0;
					String btnName = "";
					MessageHeadBean bean = beans.get(i);					
					if (staticticsType != null) {
						if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES)) {
							pushCnt = getStaticItemData(bean.mId,
									IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES);
						} else if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES)) {
							showCnt = getStaticItemData(bean.mId,
									IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES);
						} else if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES)) {
							msgClickCnt = getStaticItemData(bean.mId,
									IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES);
						} else if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_BUTTON_CLICK_TIMES)) {
							Map.Entry entry = getStaticItemButtonclickCount(bean.mId, clickItemName);
							
							if (entry != null) {
								String key = (String) entry.getKey();
								if (key != null) {
									int index = key.lastIndexOf("#");
									if (index > 0) {
										btnName = key.substring(index + 1);
										btnCnt = (Integer) entry.getValue();
									}
								}
							}
						} else if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_COVER_FRAME_CLOSE_BUTTON_CLICK_TIMES)) { //罩子层关闭按钮点击次数
							closeBtnClickCnt = getStaticItemData(bean.mId,
									IPreferencesIds.SHAREDPREFERENCES_MSG_COVER_FRAME_CLOSE_BUTTON_CLICK_TIMES);
						}
					}
					if (i > 0) {
						sb.append("&");
					}
					rst = String.valueOf(System.currentTimeMillis()) + "#" + bean.mId + "#"
							+ pushCnt + "#" + showCnt + "#" + entrance + "#" + msgClickCnt + "#" + uuid
							+ "#" + btnName + "#" + btnCnt + "#" + closeBtnClickCnt;
					sb.append(rst);
				}

				data.put("rst", sb.toString());
				if (ConstValue.DEBUG) {
					LogUnit.diyInfo(ConstValue.MSG_TAG, "updateStatisticsData : " + sb.toString());
				}
				String url = HttpUtil.getUrl(ConstValue.URLPOST_MSG_STATICDATA, sURL);
				HttpPost post = new HttpPost(url);
				// 绑定到请求 Entry
				StringEntity se;
				se = new StringEntity(data.toString());
				post.setEntity(se);
				//设置请求时间的TIME_OUT 和回应的时间的Time_OUT
				BasicHttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams, RESPONSE_TIMEOUT);
				HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(post);
				InputStream in = httpResponse.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String jsonString = reader.readLine();
				JSONObject jsonResult = new JSONObject(jsonString);
				String ret = jsonResult.getString("isok");
				int ok = Integer.valueOf(ret);
				if (ok == ConstValue.STATTUS_OK) {
					bRet = true;
				}

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
		if (bRet) {
			for (int i = 0; i < beans.size(); i++) {
				MessageHeadBean bean = beans.get(i);
				if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES)) {
					removeItemData(bean.mId, IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES);
				} else if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES)) {
					removeItemData(bean.mId, IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES);
				} else if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES)) {
					removeItemData(bean.mId, IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES);
				} else if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_BUTTON_CLICK_TIMES)) {
					removeItemData(bean.mId + "#" + clickItemName, IPreferencesIds.SHAREDPREFERENCES_MSG_BUTTON_CLICK_TIMES);
				} else if (staticticsType.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_COVER_FRAME_CLOSE_BUTTON_CLICK_TIMES)) {
					removeItemData(bean.mId, IPreferencesIds.SHAREDPREFERENCES_MSG_COVER_FRAME_CLOSE_BUTTON_CLICK_TIMES);
				}
			}
		}
		return bRet;
	}
	
	/**
	 * 功能描述：上传错误统计
	 * @return
	 */
	public synchronized boolean updateErrorStatisticsData(Vector<MessageHeadBean> beans, int errorType, int errorReason, long uuid) {
		boolean bRet = false;
		if (!Machine.isNetworkOK(GOLauncherApp.getContext())) {
			return bRet;
		}
		if (null != mContext && beans != null && beans.size() > 0) {
			try {
				JSONObject phead = getPhead(mContext, null, 0);
				JSONObject data = new JSONObject();
				data.put("phead", phead);
				
				StringBuilder sb = new StringBuilder();
				String est = "";
				for (int i = 0; i < beans.size(); i++) {
					MessageHeadBean bean = beans.get(i);
					
					if (i > 0) {
						sb.append("&");
					}
					est = String.valueOf(System.currentTimeMillis()) + "#" + bean.mId + "#"
							+ errorType + "#" + errorReason + "#" + uuid;
					sb.append(est);
				}

				data.put("est", sb.toString());
				if (ConstValue.DEBUG) {
					LogUnit.diyInfo(ConstValue.MSG_TAG, "updateFilterStatisticsData : " + sb.toString());
				}
				String url = HttpUtil.getUrl(ConstValue.URLPOST_MSG_STATICDATA, sURL);
				HttpPost post = new HttpPost(url);
				// 绑定到请求 Entry
				StringEntity se;
				se = new StringEntity(data.toString());
				post.setEntity(se);
				//设置请求时间的TIME_OUT 和回应的时间的Time_OUT
				BasicHttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
				HttpConnectionParams.setSoTimeout(httpParams, RESPONSE_TIMEOUT);
				HttpResponse httpResponse = new DefaultHttpClient(httpParams).execute(post);
				InputStream in = httpResponse.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String jsonString = reader.readLine();
				JSONObject jsonResult = new JSONObject(jsonString);
				String ret = jsonResult.getString("isok");
				int ok = Integer.valueOf(ret);
				if (ok == ConstValue.STATTUS_OK) {
					bRet = true;
				}

			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
		return bRet;
	}
	
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id
	 */
	public void saveShowStatisticsData(String id) {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.SHAREDPREFERENCES_MSG_STATISTICSDATA, Context.MODE_PRIVATE);
		synchronized (mLock) {
			int count = manager.getInt(IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES + "#" + id, 0);
			manager.putInt(IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES + "#" + id, count + 1);
			manager.commit();
		}
	}
	
	/**
	 * <br>功能简述:保存facebook开关标志
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id
	 */
	public void saveFacebookInfo(String id) {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
		synchronized (mLock) {
			if (id != null && id.equals("1")) {
				manager.putBoolean(IPreferencesIds.FACEBOOK_MESSAGE_CENTER_SWITCH, true);
			} else {
				manager.putBoolean(IPreferencesIds.FACEBOOK_MESSAGE_CENTER_SWITCH, false);
			}
			manager.commit();
		}
	}

	/**
	 * <br>功能简述:保存消息中心中点击和展示次数的统计
	 * <br>功能详细描述:
	 * <br>注意:用于点击和展示次数
	 * @param id 消息ID
	 * @param preferenceId
	 */

	public void saveStaticItemData(String id, String preferenceId) {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.SHAREDPREFERENCES_MSG_STATISTICSDATA, Context.MODE_PRIVATE);
		synchronized (mLock) {
			int count = manager.getInt(preferenceId + "#" + id, 0);
			manager.putInt(preferenceId + "#" + id, count + 1);
			manager.commit();
		}
	}

	/**
	 * <br>功能简述:获取消息中心中点击和展示次数的统计
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id
	 * @param preferenceId
	 * @return
	 */
	public int getStaticItemData(String id, String preferenceId) {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.SHAREDPREFERENCES_MSG_STATISTICSDATA, Context.MODE_PRIVATE);
		return manager.getInt(preferenceId + "#" + id, 0);
	}
	
	private void removeItemData(String id, String preferenceId) {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.SHAREDPREFERENCES_MSG_STATISTICSDATA, Context.MODE_PRIVATE);
		synchronized (mLock) {
			manager.remove(preferenceId + "#" + id);
		}
	}

	/**
	 * <br>功能简述:获取消息中心中点击和展示次数的统计
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id 消息ID
	 * @param preferenceId
	 * @return
	 */
	public void saveStaticItemButtonClick(String id, String name) {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.SHAREDPREFERENCES_MSG_STATISTICSDATA, Context.MODE_PRIVATE);
		synchronized (mLock) {
			int count = manager.getInt(IPreferencesIds.SHAREDPREFERENCES_MSG_BUTTON_CLICK_TIMES + "#"
					+ id + "#" + name, 0);
			manager.putInt(IPreferencesIds.SHAREDPREFERENCES_MSG_BUTTON_CLICK_TIMES + "#" + id + "#"
					+ name, count + 1);
			manager.commit();
		}
	}

	public Map.Entry getStaticItemButtonclickCount(String id, String itemName) {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.SHAREDPREFERENCES_MSG_STATISTICSDATA, Context.MODE_PRIVATE);
		Map<String, ?> data = manager.getAll();
		Iterator iterator = data.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Entry) iterator.next();
			String key = (String) entry.getKey();
			if (key.equals(IPreferencesIds.SHAREDPREFERENCES_MSG_BUTTON_CLICK_TIMES + "#" + id + "#" + itemName)) {
				return entry;
			}
		}
		return null;
	}

	/**
	 * <br>功能简述:清楚一条消息的统计
	 * <br>功能详细描述:
	 * <br>注意
	 */
	public void clearStaticData() {
		PreferencesManager manager = new PreferencesManager(mContext,
				IPreferencesIds.SHAREDPREFERENCES_MSG_STATISTICSDATA, Context.MODE_PRIVATE);
		manager.clear();
	}
}
