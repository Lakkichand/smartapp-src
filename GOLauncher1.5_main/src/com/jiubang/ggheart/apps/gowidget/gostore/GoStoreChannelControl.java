package com.jiubang.ggheart.apps.gowidget.gostore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.ref.SoftReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.MainDataHttpOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ChannelBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ChannelBean.ChannelInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ChannelCheckBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 渠道控制 单例
 * 
 * @author wangzhuobin
 * 
 */
public class GoStoreChannelControl {

	// XML文件标签名
	private static final String TIMESTAMP = "timestamp";
	private static final String ORIGINAL_UID = "originalUID";
	private static final String CHANNELINFO = "channelInfo";

	// XML文件属性名
	private static final String NAME = "name";
	private static final String URL = "url";
	private static final String ICON_PATH = "icon_path";

	// 缓存文件保存路径
	private static final String CHANNEL_CACHE_PATH = Environment.getExternalStorageDirectory()
			+ "/GoStore/channel_cache.xml";
	public final static String COMMON_ICON_PATH = Environment.getExternalStorageDirectory()
			+ "/GoStore/icon/";

	// 渠道验证成功，发送广播的Action
	//	public static final String ACTION_CHANNEL_CHECK_NAME_SUCCESS = "com.jiubang.ggheart.apps.gowidget.gostore.views.GoStoreChannelControl.ACTION_CHANNEL_CHECK_NAME_SUCCESS";

	public static final String GOSTORE_DEFAULT_CHANNEL = "999"; // GO精品默认渠道号

	private static final String GOSTORE_CHANNEL_CHECK_FILENAME = "gostore_channel_check"; // GO精品渠道包验证结果存储文件名称
	private static final String GOSTORE_CHANNEL_CHECK_ID = "gostore_channel_check_id"; // GO精品渠道包验证返回结果渠道ID的键值
	private static final String GOSTORE_CHANNEL_CHECK_NAME = "gostore_channel_check_name"; // GO精品渠道包验证返回结果渠道名称的键值
	private static final String ORIGINAL_UID_KEY = "originalUID"; // 原始渠道号的键值
	private static final String ORIGINAL_VERSIONCODE_KEY = "originalVersionCode"; // 原始VersionCode的键值
	private static final String ORIGINAL_LANGUAGE_KEY = "original_language_key"; // 原始语言的键值
	private static final String GOSTORE_CHANNEL_CHECK_ICON_PATH = "gostore_channel_check_icon_path"; // GO精品渠道包验证返回的渠道图标保存路径的键值
	public static final String CHANNEL_NAME_KEY = "channelName"; // 渠道名称的键值

	// 要显示的数据BEAN集合
	private ArrayList<DisplayChannelInfoBean> mDisplayChannelInfoBeans = null;

	// 控制是否进行网络请求的标志位
	private boolean mCanGetChannelInfoFromNet = true;

	private Handler mHandler = null;

	private Context mContext = null;

	private static GoStoreChannelControl sSelf = null;

	// 是否被回收的标志
	private static boolean sIsRecycle = false;

	public synchronized static GoStoreChannelControl getChannelControlInstance(Context context) {
		if (sSelf == null || sIsRecycle) {
			if (context != null) {
				sSelf = new GoStoreChannelControl(context);
				sIsRecycle = false;
			}
		}
		return sSelf;
	}

	private GoStoreChannelControl(Context context) {

		mContext = context;
		// 初始化Handler
		initHandler(context);
	}

	/**
	 * 初始化Handler的方法
	 * 
	 * @param context
	 */
	private void initHandler(final Context context) {
		// TODO Auto-generated method stub
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				// 数据成功返回，更新到内存集合中
				if (msg.obj != null && msg.obj instanceof ChannelBean) {
					ChannelBean channelBean = (ChannelBean) msg.obj;
					// 更新到内存集合中
					loadDataToDisplayBean(context, channelBean);
					// 回收网络请求数据
					channelBean.recycle();
				}
			}

		};
	}

	/**
	 * 外部获取下载渠道信息的方法
	 * 
	 * @param context
	 * @return
	 */
	public ArrayList<DisplayChannelInfoBean> getDownloadChannelInfo() {

		ArrayList<DisplayChannelInfoBean> resultArrayList = null;
		if (mContext != null) {

			// 如果内存没有渠道 信息，则到SD卡中取
			if (mDisplayChannelInfoBeans == null || mDisplayChannelInfoBeans.size() == 0) {
				if (GoStorePhoneStateUtil.isSDCardAccess()) {
					mDisplayChannelInfoBeans = getDownloadChannelInfoFromSDCard(mContext);
				}
			}

			resultArrayList = mDisplayChannelInfoBeans;

			// 如果允许进行网络请求，就异步进行网络请求下载渠道信息
			if (mCanGetChannelInfoFromNet) {

				// 关闭网络请求标志
				mCanGetChannelInfoFromNet = false;
				// 异步从网络中获取下载渠道信息
				getDownloadChannelInfoFromNet(mContext);
			}
		}
		return resultArrayList;
	}

	/**
	 * 从SD卡中获取下载渠道信息的方法
	 * 
	 * @param context
	 * @return
	 */
	private synchronized ArrayList<DisplayChannelInfoBean> getDownloadChannelInfoFromSDCard(
			Context context) {

		ArrayList<DisplayChannelInfoBean> resultArrayList = null;

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(CHANNEL_CACHE_PATH);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (inputStream != null) {
			try {
				XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
				if (parser != null) {

					resultArrayList = new ArrayList<GoStoreChannelControl.DisplayChannelInfoBean>();

					parser.setInput(inputStream, null);

					String tagName = null;
					int eventType = 0;
					DisplayChannelInfoBean displayChannelInfoBean = null;
					String originalUID = null;
					String nowLocalUID = GoStorePhoneStateUtil.getGoStoreUid(mContext);
					String iconPath = null;
					while (parser.next() != XmlPullParser.END_DOCUMENT) {
						tagName = parser.getName();
						eventType = parser.getEventType();
						if (tagName.equals(ORIGINAL_UID) && eventType == XmlPullParser.START_TAG) {
							originalUID = parser.getAttributeValue(null, ORIGINAL_UID);
						} else if (tagName.equals(CHANNELINFO)
								&& eventType == XmlPullParser.START_TAG) {
							if (originalUID == null || "".equals(originalUID.trim())
									|| !originalUID.equals(nowLocalUID)) {
								break;
							}
							displayChannelInfoBean = new DisplayChannelInfoBean();
							displayChannelInfoBean.mChannelName = parser.getAttributeValue(null,
									NAME);
							displayChannelInfoBean.mChannelUrl = parser
									.getAttributeValue(null, URL);
							iconPath = parser.getAttributeValue(null, ICON_PATH);
							displayChannelInfoBean.mIconPath = iconPath;
							if (iconPath != null && !"".equals(iconPath.trim())) {
								displayChannelInfoBean.mChannelIcon = new SoftReference<Bitmap>(
										BitmapFactory.decodeFile(COMMON_ICON_PATH + iconPath));
							}
							if (displayChannelInfoBean.mChannelIcon == null) {
								if (context != null) {
									BitmapDrawable bitmapDrawable = (BitmapDrawable) context
											.getResources().getDrawable(R.drawable.icon);
									displayChannelInfoBean.mChannelIcon = new SoftReference<Bitmap>(
											bitmapDrawable.getBitmap());
								}
							}
							resultArrayList.add(displayChannelInfoBean);
						}
					}
				}
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					inputStream = null;
				}
			}
		}

		return resultArrayList;
	}

	/**
	 * 异步从网络中获取下载渠道信息的方法
	 * 
	 * @param context
	 */
	public void getDownloadChannelInfoFromNet(final Context context) {

		if (context != null && GoStorePhoneStateUtil.isNetWorkAvailable(context)) {
			byte[] postData = ThemeHttp.getPostData(context, null, GoStorePublicDefine.FUNID_MORE);

			THttpRequest request;
			try {
				request = new THttpRequest(GoStorePublicDefine.URL_HOST3, postData,
						new IConnectListener() {

							@Override
							public void onStart(THttpRequest request) {

							}

							@SuppressWarnings("unchecked")
							@Override
							public void onFinish(THttpRequest request, IResponse response) {
								// TODO Auto-generated method stub
								if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
									ArrayList<BaseBean> baseBeans = (ArrayList<BaseBean>) response
											.getResponse();
									if (baseBeans != null && baseBeans.size() > 0) {
										if (baseBeans.get(0) instanceof ChannelBean) {
											ChannelBean channelBean = (ChannelBean) baseBeans
													.get(0);
											if (channelBean != null) {
												// 如果有SD卡
												if (GoStorePhoneStateUtil.isSDCardAccess()) {
													// 把下载渠道信息缓存到SD卡中
													writeChannelInfosToSDCard(channelBean);
												}
												// 同步到UI线程更新内存数据集合
												if (mHandler != null) {
													Message message = new Message();
													message.obj = channelBean;
													mHandler.sendMessage(message);
												}
											}
										}
									}
								}
								// 重新打开网络请求标志
								mCanGetChannelInfoFromNet = true;
							}

							@Override
							public void onException(THttpRequest request, int reason) {
								StatisticsData.saveHttpExceptionDate(mContext, request, reason);
								// 重新打开网络请求标志
								mCanGetChannelInfoFromNet = true;

							}
						});
				// 设置POST请求头
				request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);

				MainDataHttpOperator operator = new MainDataHttpOperator();
				request.setOperator(operator);

				SimpleHttpAdapter.getInstance(context).addTask(request);

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 把网络请求数据更新到内存集合的方法
	 * 
	 * @param context
	 * @param channelBean
	 */
	private void loadDataToDisplayBean(Context context, ChannelBean channelBean) {
		if (channelBean != null && channelBean.mChannelInfoArrayList != null
				&& channelBean.mChannelInfoArrayList.size() > 0) {

			if (mDisplayChannelInfoBeans == null) {
				mDisplayChannelInfoBeans = new ArrayList<GoStoreChannelControl.DisplayChannelInfoBean>();
			} else {
				// 清除原来内存集合中的数据
				for (DisplayChannelInfoBean displayChannelInfoBean : mDisplayChannelInfoBeans) {
					displayChannelInfoBean.recycle();
				}
				mDisplayChannelInfoBeans.clear();
			}

			DisplayChannelInfoBean displayChannelInfoBean = null;
			for (ChannelInfo channelInfo : channelBean.mChannelInfoArrayList) {

				displayChannelInfoBean = new DisplayChannelInfoBean();
				displayChannelInfoBean.mChannelName = channelInfo.mChannelName;
				displayChannelInfoBean.mChannelUrl = channelInfo.mChannelUrl;
				displayChannelInfoBean.mIconPath = "";
				if (channelInfo.mIconData != null && channelInfo.mIconData.length > 0) {
					if (channelInfo.mChannelName != null) {
						displayChannelInfoBean.mIconPath = String.valueOf(channelInfo.mChannelName
								.hashCode());
					}
					// 如果下发渠道数据有图片
					displayChannelInfoBean.mChannelIcon = new SoftReference<Bitmap>(
							BitmapFactory.decodeByteArray(channelInfo.mIconData, 0,
									channelInfo.mIconData.length));
				} else {
					if (context != null) {
						// 如果没有图片，则使用默认图片
						BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources()
								.getDrawable(R.drawable.icon);
						displayChannelInfoBean.mChannelIcon = new SoftReference<Bitmap>(
								bitmapDrawable.getBitmap());
					}
				}
				mDisplayChannelInfoBeans.add(displayChannelInfoBean);
			}
		}
	}

	/**
	 * 把下载渠道信息写到SD卡的方法
	 * 
	 * @param channelBean
	 *            渠道信息数据BEAN
	 * @return
	 */
	private synchronized boolean writeChannelInfosToSDCard(ChannelBean channelBean) {

		boolean result = false;

		if (channelBean != null && channelBean.mChannelInfoArrayList != null
				&& channelBean.mChannelInfoArrayList.size() > 0) {

			StringWriter writer = new StringWriter();
			OutputStream outputStream = null;
			OutputStreamWriter outputStreamWriter = null;
			try {
				XmlSerializer xmlSerializer = XmlPullParserFactory.newInstance().newSerializer();
				xmlSerializer.setOutput(writer);

				xmlSerializer.startDocument("UTF-8", true);

				// 保存时间戳
				xmlSerializer.startTag("", TIMESTAMP);
				long timeStamp = channelBean.mTimeStamp;
				xmlSerializer.attribute("", TIMESTAMP, String.valueOf(timeStamp));
				xmlSerializer.endTag("", TIMESTAMP);

				// 保存验证时所使用的渠道号
				xmlSerializer.startTag("", ORIGINAL_UID);
				xmlSerializer.attribute("", ORIGINAL_UID,
						GoStorePhoneStateUtil.getGoStoreUid(mContext));
				xmlSerializer.endTag("", ORIGINAL_UID);

				ArrayList<ChannelInfo> channelInfosArrayList = channelBean.mChannelInfoArrayList;

				String channelName = null;
				String channelUrl = null;
				String iconPath = null;
				for (ChannelInfo channelInfo : channelInfosArrayList) {

					channelName = channelInfo.mChannelName;
					channelUrl = channelInfo.mChannelUrl;

					// 渠道名和URL都不能为空，否则不缓存
					if (channelName != null && !"".equals(channelName.trim()) && channelUrl != null
							&& !"".equals(channelUrl.trim())) {

						channelName = channelName.trim();
						channelUrl = channelUrl.trim();

						xmlSerializer.startTag("", CHANNELINFO);
						xmlSerializer.attribute("", NAME, channelName);
						xmlSerializer.attribute("", URL, channelUrl);

						// 如果下发数据没有图标，那么图标路径为空
						iconPath = "";
						if (channelInfo.mIconData != null && channelInfo.mIconData.length > 0) {
							iconPath = String.valueOf(channelName.hashCode());
							FileUtil.saveByteToCommonIconSDFile(channelInfo.mIconData, iconPath);
						}

						xmlSerializer.attribute("", ICON_PATH, iconPath);
						xmlSerializer.endTag("", CHANNELINFO);
					}
				}

				xmlSerializer.endDocument();

				File file = FileUtil.createNewFile(CHANNEL_CACHE_PATH, false);
				outputStream = new FileOutputStream(file);
				outputStreamWriter = new OutputStreamWriter(outputStream);
				outputStreamWriter.write(writer.toString());

				result = true;

			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (outputStreamWriter != null) {
						outputStreamWriter.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 渠道包验证的方法
	 */
	public void checkChannelAvailable() {
		if (mContext != null) {
			byte[] postData = ThemeHttp.getPostData(mContext, null,
					GoStorePublicDefine.FUNID_CHANNEL_CHECK);

			THttpRequest request;
			try {
				request = new THttpRequest(GoStorePublicDefine.URL_HOST3, postData,
						new IConnectListener() {

							@Override
							public void onStart(THttpRequest request) {

							}

							@SuppressWarnings("unchecked")
							@Override
							public void onFinish(THttpRequest request, IResponse response) {
								// TODO Auto-generated method stub
								if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
									ArrayList<BaseBean> baseBeans = (ArrayList<BaseBean>) response
											.getResponse();
									if (baseBeans != null && baseBeans.size() > 0) {
										if (baseBeans.get(0) instanceof ChannelCheckBean) {
											ChannelCheckBean channelCheckBean = (ChannelCheckBean) baseBeans
													.get(0);
											if (mContext != null && channelCheckBean != null) {
												PreferencesManager sharedPreferences = new PreferencesManager(
														mContext, GOSTORE_CHANNEL_CHECK_FILENAME,
														Context.MODE_PRIVATE);
												sharedPreferences.putString(
														GOSTORE_CHANNEL_CHECK_ID,
														channelCheckBean.mChannelId);
												sharedPreferences.putString(
														GOSTORE_CHANNEL_CHECK_NAME,
														channelCheckBean.mChannelName);
												sharedPreferences.putString(ORIGINAL_UID_KEY,
														GoStorePhoneStateUtil
																.getGoStoreUid(mContext));
												sharedPreferences.putString(ORIGINAL_LANGUAGE_KEY,
														Locale.getDefault().getLanguage());
												sharedPreferences.putInt(ORIGINAL_VERSIONCODE_KEY,
														getThisAppVerCode(mContext));
												if (channelCheckBean.mImgData != null
														&& channelCheckBean.mImgData.length > 0) {
													// 如果有图片数据下发
													if (GoStorePhoneStateUtil.isSDCardAccess()) {
														// 如果SD卡可读
														try {
															String iconSavePath = String
																	.valueOf(channelCheckBean
																			.hashCode())
																	+ String.valueOf(System
																			.currentTimeMillis());
															Bitmap bitmap = BitmapFactory
																	.decodeByteArray(
																			channelCheckBean.mImgData,
																			0,
																			channelCheckBean.mImgData.length);
															int widthAndHeight = GoStoreDisplayUtil
																	.scalePxToMachine(mContext, 50);
															bitmap = Bitmap.createScaledBitmap(
																	bitmap, widthAndHeight,
																	widthAndHeight, true);
															// 保存图片到sd卡中
															if (bitmap != null
																	&& FileUtil
																			.saveBitmapToCommonIconSDFile(
																					bitmap,
																					iconSavePath,
																					CompressFormat.PNG)) {
																sharedPreferences
																		.putString(
																				GOSTORE_CHANNEL_CHECK_ICON_PATH,
																				iconSavePath);
																if (!bitmap.isRecycled()) {
																	bitmap.recycle();
																	bitmap = null;
																}
															}
														} catch (Exception e) {
															// TODO: handle
															// exception
															// OOM捕获
														}
													}
												} else {
													// 如果没有图片数据下发
													// 把原来的路径设置为空
													sharedPreferences.putString(
															GOSTORE_CHANNEL_CHECK_ICON_PATH, null);
												}
												sharedPreferences.commit();
												// 清理数据
												channelCheckBean.recycle();
												// 发送广播,让收到广播的UI进行更新
												sendChannelCheckNameBrocast(mContext);
											}
										}
									}
								}
							}

							@Override
							public void onException(THttpRequest request, int reason) {
								StatisticsData.saveHttpExceptionDate(mContext, request, reason);
							}
						});
				// 设置POST请求头
				request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);

				MainDataHttpOperator operator = new MainDataHttpOperator();
				request.setOperator(operator);

				SimpleHttpAdapter.getInstance(mContext).addTask(request);

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取渠道图标的方法
	 * 
	 * @param context
	 * @return
	 */
	public static Drawable getChannelIconDrawable(Context context) {
		BitmapDrawable drawable = null;
		String originChannelId = null;
		String nowLoadChannelId = null;
		String iconSavePath = null;
		int originVersionCode = -1;
		if (context != null) {
			// 先到SharedPreferences里面取
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTORE_CHANNEL_CHECK_FILENAME, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				originChannelId = sharedPreferences.getString(ORIGINAL_UID_KEY, null);
				nowLoadChannelId = GoStorePhoneStateUtil.getGoStoreUid(context);
				originVersionCode = sharedPreferences.getInt(ORIGINAL_VERSIONCODE_KEY, -1);
				iconSavePath = sharedPreferences.getString(GOSTORE_CHANNEL_CHECK_ICON_PATH, null);
				if (originChannelId != null && originChannelId.equals(nowLoadChannelId)) {
					// 如果已经进行过验证，且没有进行不同渠道包的覆盖安装
					int nowVersionCode = getThisAppVerCode(context);
					if (nowVersionCode > originVersionCode) {
						// 如果现在的渠道包的版本更加新
						// 则直接使用现在渠道包里面配置的文本
						iconSavePath = null;
					} else {
						// 如果还是使用渠道包原来的版本
					}
				} else {
					// 如果没有进行过渠道验证，则直接在配置文件中取
					// 或者进行了渠道验证，但又进行了不同渠道包的覆盖安装,则直接在配置文件中取
					iconSavePath = null;
				}
				// 如果经过上面判断后，路径不为空，则从路径中取图片
				if (iconSavePath != null && !"".equals(iconSavePath.trim())) {
					drawable = (BitmapDrawable) Drawable
							.createFromPath(LauncherEnv.Path.GOSTORE_ICON_PATH + iconSavePath);
					if (drawable != null) {
						drawable.setTargetDensity(context.getResources().getDisplayMetrics());
					}
				}
			}
		}
		return drawable;
	}

	/**
	 * 获取渠道名称的方法
	 * 
	 * @param context
	 * @return
	 */
	public static String getChannelCheckName(Context context) {
		String channelId = null;
		String channelName = null;
		String originChannelId = null;
		String nowLoadChannelId = null;
		String originLanguage = null;
		int originVersionCode = -1;
		if (context != null) {
			// 先到SharedPreferences里面取
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					GOSTORE_CHANNEL_CHECK_FILENAME, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				channelName = sharedPreferences.getString(GOSTORE_CHANNEL_CHECK_NAME, null);
				channelId = sharedPreferences.getString(GOSTORE_CHANNEL_CHECK_ID, null);
				originChannelId = sharedPreferences.getString(ORIGINAL_UID_KEY, null);
				nowLoadChannelId = GoStorePhoneStateUtil.getGoStoreUid(context);
				originVersionCode = sharedPreferences.getInt(ORIGINAL_VERSIONCODE_KEY, -1);
				originLanguage = sharedPreferences.getString(ORIGINAL_LANGUAGE_KEY, null);
				if (originChannelId != null && originChannelId.equals(nowLoadChannelId)) {
					// 如果已经进行过验证，且没有进行不同渠道包的覆盖安装
					int nowVersionCode = getThisAppVerCode(context);
					if (nowVersionCode > originVersionCode) {
						// 如果现在的渠道包的版本更加新
						// 则直接使用现在渠道包里面配置的文本
						channelName = context.getResources().getString(
								R.string.themestore_channel_name);
					} else {
						// 如果还是使用渠道包原来的版本
						if (channelName == null
								|| "".equals(channelName.trim())
								|| (originLanguage != null && !originLanguage.equals(Locale
										.getDefault().getLanguage()))) {
							// 如果SharedPreferences里面取到的名称是空的
							// 或者验证时的语言跟现在客户端的语言不一样
							if (channelId != null) {
								// 如果SharedPreferences里面的ID不为空
								if (!channelId.equals(nowLoadChannelId)) {
									// 如果本地ID与返回ID不相同，表明本地的渠道已经失效，取默认的渠道名称
									channelName = context.getResources().getString(
											R.string.themestore_default_channel_name);
								} else {
									// 如果相同，则取配置文件的渠道名称
									channelName = context.getResources().getString(
											R.string.themestore_channel_name);
								}
							} else {
								// 如果SharedPreferences里面的ID也是空的话，取配置文件的渠道名称
								channelName = context.getResources().getString(
										R.string.themestore_channel_name);
							}
						}
					}
				} else {
					// 如果没有进行过渠道验证，则直接在配置文件中取
					// 或者进行了渠道验证，但又进行了不同渠道包的覆盖安装,则直接在配置文件中取
					channelName = context.getResources()
							.getString(R.string.themestore_channel_name);
					// 同时清掉之前的缓存数据
					sharedPreferences.clear();
					sharedPreferences.commit();
				}
				// 如果上述取到的名称还是为空，则最后取默认的渠道名称
				if (channelName == null || "".equals(channelName.trim())) {
					channelName = context.getResources().getString(
							R.string.themestore_default_channel_name);
				}
			}
		}
		return channelName;
	}

	/**
	 * 发送渠道名称验证成功的广播
	 * 
	 * @param context
	 */
	private void sendChannelCheckNameBrocast(Context context) {
		if (context != null) {
			Intent intent = new Intent();
			intent.setAction(ICustomAction.ACTION_CHANNEL_CHECK_NAME_SUCCESS);
			intent.putExtra(CHANNEL_NAME_KEY, getChannelCheckName(context));
			context.sendBroadcast(intent);
		}
	}

	/**
	 * 获取本应用versionCode的方法
	 * 
	 * @param context
	 * @return
	 */
	public static int getThisAppVerCode(Context context) {
		int result = -1;
		if (context != null) {
			PackageManager manager = context.getPackageManager();
			if (manager != null) {
				try {
					PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
					if (info != null) {
						result = info.versionCode; // 版本号
					}
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public synchronized void recycle() {
		if (mDisplayChannelInfoBeans != null) {
			for (DisplayChannelInfoBean displayChannelInfoBean : mDisplayChannelInfoBeans) {
				if (displayChannelInfoBean != null) {
					displayChannelInfoBean.recycle();
				}
			}
			mDisplayChannelInfoBeans.clear();
			mDisplayChannelInfoBeans = null;
		}
		mHandler = null;
		mContext = null;
		sIsRecycle = true;
	}

	/**
	 * 用于显示的数据BEAN
	 */
	public static class DisplayChannelInfoBean {
		public String mChannelName = null; // 渠道名
		public String mChannelUrl = null; // 渠道URL
		public String mIconPath = null; // 渠道图片路径
		public SoftReference<Bitmap> mChannelIcon = null; // 渠道图片

		public void recycle() {
			mChannelName = null;
			mChannelUrl = null;
			mIconPath = null;
			mChannelIcon = null;
		}
	}
}
