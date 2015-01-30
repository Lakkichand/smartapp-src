package com.zhidian.wifibox.controller;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.AdvertisementBean;
import com.zhidian.wifibox.data.AutoUpdateBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.MemoryBean;
import com.zhidian.wifibox.data.PopupCommend;
import com.zhidian.wifibox.data.XDataDownload;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.util.Setting;

/**
 * 主界面控制器，负责一些进入时耗时的操作
 * 
 * @author xiedezhi
 * 
 */
public class MainController extends TACommand {
	/**
	 * 初次进入注册用户信息
	 */
	public static final String REGISTER_USERINFO = "MAINCONTROLLER_REGISTER_USERINFO";
	/**
	 * 检查更新
	 */
	public static final String CHECK_FOR_UPDATE = "MAINCONTROLLER_CHECK_FOR_UPDATE";
	/**
	 * 检查手机内存
	 */
	public static final String CHECK_MEMORY = "MAINCONTROLLER_CHECK_MEMORY";
	/**
	 * 如果图标文件夹超过100M，清理掉最早创建的图标
	 */
	public static final String CLEAN_ICON = "MAINCONTROLLER_CLEAN_ICON";
	/**
	 * 获取活动弹窗推荐
	 */
	public static final String POPUP_RECOMEND = "MAINCONTROLLER_POPUP_RECOMEND";
	/**
	 * 门店广告
	 */
	public static final String MIBAO_ADVERTISEMENT = "MAINCONTROLLER_MIBAO_ADVERTISEMENT";
	/**
	 * 获取地理位置，保存到preference
	 */
	public static final String MIBAO_GETLOCATION = "MAINCONTROLLER_MIBAO_GETLOCATION";

	private Handler mHandler = new Handler(Looper.getMainLooper());

	@SuppressLint("NewApi")
	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(REGISTER_USERINFO)) {
			Setting setting = new Setting(TAApplication.getApplication());
			RequestParams params = new RequestParams();
			params.put("uuid", InfoUtil.getUUID(TAApplication.getApplication()));
			params.put("imei", InfoUtil.getIMEI(TAApplication.getApplication()));
			params.put("imsi", InfoUtil.getIMSI(TAApplication.getApplication()));
			params.put("model", InfoUtil.getModel());
			params.put("version", InfoUtil.getVersion());
			params.put("simOperatorName",
					InfoUtil.getSimOperatorName(TAApplication.getApplication()));
			params.put("manufacturer", InfoUtil.getManuFacturer());
			params.put("mac",
					InfoUtil.getLocalMacAddress(TAApplication.getApplication()));
			params.put("networkCountryIso",
					InfoUtil.getISO(TAApplication.getApplication()));
			params.put("installTime", setting.getString(Setting.INSTALL_TIME));
			CDataDownloader.getPostData(CDataDownloader.getRegisterUrl(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								// 成功
								if (statusCode == 0) {
									Log.e("上传phone信息", "成功");
									SharedPreferences prefs = PreferenceManager
											.getDefaultSharedPreferences(TAApplication
													.getApplication());
									Editor editor = prefs.edit();
									editor.putBoolean(
											Setting.HASREGISTERUSERINFO, true);
									editor.commit();
									String uuid = json.optString("result", "");
									// Log.e("", "服务器下发的UUID：" + uuid);
									if (!TextUtils.isEmpty(uuid.trim())) {
										InfoUtil.saveUUID(uuid.getBytes());
									}
								}
							} catch (Exception e) {
							}
							MainController.this.sendSuccessMessage(content);
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							// Log.d("MainController", error.getMessage());
							MainController.this.sendFailureMessage(error);
						}

						@Override
						public void onFinish() {
							MainController.this.sendFinishMessage();
						}
					});
		} else if (command.equals(CHECK_FOR_UPDATE)) {
			// 检查更新
			String url = CDataDownloader.getAutoUpdateUrl(InfoUtil
					.getVersionName(TAApplication.getApplication()), AppUtils
					.readAssetsFile(TAApplication.getApplication(), "boxId"));
			CDataDownloader.getData(url, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(String content) {
					AutoUpdateBean bean = DataParser.parseAutoUpdate(content);
					MainController.this.sendSuccessMessage(bean);
				}

				@Override
				public void onStart() {
				}

				@Override
				public void onFailure(Throwable error) {
					MainController.this.sendFailureMessage(error.getMessage());
				}

				@Override
				public void onFinish() {
				}
			});
		} else if (command.equals(CHECK_MEMORY)) {
			// 检查内存
			MemoryBean bean = new MemoryBean();
			File path = Environment.getDataDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			long totalBlocks = stat.getBlockCount();
			bean.setMemorySize(availableBlocks * blockSize);
			bean.setMemoryAvail(totalBlocks * blockSize);

			// 检查SD卡内存
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				if (android.os.Build.VERSION.SDK_INT <= 8) {
					// 取得sdcard文件路径
					File sdPath = Environment.getExternalStorageDirectory();

					StatFs statfs = new StatFs(sdPath.getPath());
					// 获取block的SIZE
					long sdBlocSize = statfs.getBlockSize();
					// 获取BLOCK数量
					long sdTotalBlocks = statfs.getBlockCount();
					// 己使用的Block的数量
					long availBlocks = statfs.getAvailableBlocks();

					long totalSize = sdTotalBlocks * sdBlocSize;
					long availSize = availBlocks * sdBlocSize;

					bean.setTotalSdMemory(totalSize);
					bean.setAvailSdMemory(availSize);
				} else {
					StorageManager storageManager = (StorageManager) TAApplication
							.getApplication().getSystemService(
									Context.STORAGE_SERVICE);
					long totalSize = 0;
					long availSize = 0;
					try {
						Class<?>[] paramClasses = {};
						Method getVolumePathsMethod = StorageManager.class
								.getMethod("getVolumePaths", paramClasses);
						getVolumePathsMethod.setAccessible(true);
						Object[] params = {};
						Object invoke = getVolumePathsMethod.invoke(
								storageManager, params);
						for (int i = 0; i < ((String[]) invoke).length; i++) {
							StatFs sdstat = getStatFs(((String[]) invoke)[i]);
							totalSize += calculateTotalSizeInMB(sdstat);
							availSize += calculateSizeInMB(sdstat);
						}
						bean.setTotalSdMemory(totalSize);
						bean.setAvailSdMemory(availSize);
					} catch (NoSuchMethodException e1) {
						e1.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			}
			MainController.this.sendSuccessMessage(bean);
		} else if (command.equals(CLEAN_ICON)) {
			File dir = new File(PathConstant.ICON_ROOT_PATH);
			if (!dir.exists() || !dir.isDirectory()) {
				return;
			}
			long size = FileUtil.getDirSize(dir);
			if (size > 100L * 1024L * 1024L) {
				long needToClean = size - 100L * 1024L * 1024L;
				File[] imgs = dir.listFiles();
				if (imgs == null) {
					return;
				}
				Arrays.sort(imgs, new Comparator<File>() {

					@Override
					public int compare(File lhs, File rhs) {
						if (lhs.lastModified() > rhs.lastModified()) {
							return 1;
						}
						if (lhs.lastModified() == rhs.lastModified()) {
							return 0;
						}
						if (lhs.lastModified() < rhs.lastModified()) {
							return -1;
						}
						return 0;
					}
				});
				for (File img : imgs) {
					if (needToClean > 0) {
						needToClean -= img.length();
						img.delete();
					} else {
						break;
					}
				}
			}
		} else if (command.equals(POPUP_RECOMEND)) {
			CDataDownloader.getData(CDataDownloader.getPopupWindowsUrl(),
					new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(String content) {
							super.onSuccess(content);
							PopupCommend poc = DataParser
									.parsePopupCommendData(content);
							MainController.this.sendSuccessMessage(poc);
						}

						@Override
						public void onFailure(Throwable error) {
							super.onFailure(error);
							MainController.this.sendFailureMessage(error);
						}
					});
		} else if (command.equals(MIBAO_ADVERTISEMENT)) {
			// 先获取boxid
			CDataDownloader.getData(XDataDownload.getXBoxIdUrl(),
					new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(String content) {
							if (TextUtils.isEmpty(content)) {
								return;
							}
							if (TextUtils.isEmpty(content.trim())) {
								return;
							}
							if (content.contains("<html>")) {
								return;
							}
							content = content.trim();
							// 再获取门店广告数据
							CDataDownloader.getData(CDataDownloader
									.getAdvertisementUrl(content),
									new AsyncHttpResponseHandler() {

										@Override
										public void onSuccess(String content) {
											AdvertisementBean bean = DataParser
													.parseAdvertisementData(content);
											if (bean == null) {
												return;
											}
											// 保存时间
											Setting setting = new Setting(
													TAApplication
															.getApplication());
											setting.putLong(
													Setting.SHOW_ADVERTISEMENT_TIME,
													System.currentTimeMillis());
											showNotification(bean);
										}
									});
						}
					});
		} else if (command.equals(MIBAO_GETLOCATION)) {
			Log.e("", "MIBAO_GETLOCATION");
			Setting setting = new Setting(TAApplication.getApplication());
			setting.putLong(
					Setting.MIBAO_LOCATION_TIME_PREFIX
							+ InfoUtil.getBoxId(TAApplication.getApplication()),
					System.currentTimeMillis());
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					try {
						final LocationClient locationClient = new LocationClient(
								TAApplication.getApplication());
						LocationClientOption option = new LocationClientOption();
						option.setCoorType("gcj02");
						option.setOpenGps(true); // 打开gps
						option.setAddrType("all");
						option.setScanSpan(10000);
						option.setProdName(TAApplication.getApplication()
								.getString(R.string.app_name)); // 设置产品线名称
						option.setIsNeedAddress(true);
						option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
						locationClient.setLocOption(option);
						final BDLocationListener listener = new BDLocationListener() {

							@Override
							public void onReceiveLocation(BDLocation loc) {
								try {
									if (loc == null) {
										return;
									}
									Log.e("",
											"latitude = " + loc.getLatitude()
													+ "  longitude = "
													+ loc.getLongitude());
									Log.e("", "address = " + loc.getAddrStr());
									Log.e("", "loctype = " + loc.getLocType());
									Log.e("", "radius = " + loc.getRadius());
									LocationManager locationManager = (LocationManager) TAApplication
											.getApplication().getSystemService(
													Context.LOCATION_SERVICE);
									boolean gps = locationManager
											.isProviderEnabled(LocationManager.GPS_PROVIDER);
									Log.e("", "gps = " + gps);
									WifiManager wifi_service = (WifiManager) TAApplication
											.getApplication().getSystemService(
													Context.WIFI_SERVICE);
									WifiInfo wifiInfo = wifi_service
											.getConnectionInfo();
									Log.e("", "rssi = " + wifiInfo.getRssi());
									String boxid = InfoUtil
											.getBoxId(TAApplication
													.getApplication());
									// 上传位置信息
									RequestParams params = new RequestParams();
									params.put("boxNum", boxid);
									SimpleDateFormat formatter = new SimpleDateFormat(
											"yyyy-MM-dd HH:mm:ss");
									String nowTime = formatter.format(new Date(
											System.currentTimeMillis()));
									params.put("getTime", nowTime);
									params.put("longitude",
											"" + loc.getLongitude());
									params.put("latitude",
											"" + loc.getLatitude());
									params.put("location", loc.getAddrStr());
									params.put("locationType", loc.getLocType()
											+ "");
									params.put("gpsStatus", gps ? "1" : "0");
									params.put("radius", loc.getRadius() + "");
									params.put("rssi", wifiInfo.getRssi() + "");
									params.put(
											"versionType",
											AppUtils.getVersionCode(
													TAApplication
															.getApplication(),
													TAApplication
															.getApplication()
															.getPackageName())
													+ "");
									Log.e("", params.toString());
									CDataDownloader.getPostData(
											CDataDownloader.getLocationUrl(),
											params, null);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						};
						locationClient.registerLocationListener(listener);
						locationClient.start();// 将开启与获取位置分开，就可以尽量的在后面的使用中获取到位置
						locationClient.requestLocation();
						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								try {
									locationClient
											.unRegisterLocationListener(listener);
									locationClient.stop();
								} catch (Throwable e) {
									e.printStackTrace();
								}
							}
						}, 30 * 1000);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	/**
	 * 展示门店广告
	 */
	private void showNotification(AdvertisementBean bean) {
		try {
			NotificationManager notificationManager = (NotificationManager) TAApplication
					.getApplication().getSystemService(
							android.content.Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification();
			notification.icon = R.drawable.ad_notify;
			notification.when = System.currentTimeMillis();
			notification.tickerText = bean.title;
			notification.contentView = new RemoteViews(TAApplication
					.getApplication().getPackageName(),
					R.layout.notification_ad);
			notification.contentView.setTextViewText(R.id.decription_state_tv,
					bean.title);
			notification.contentView.setTextViewText(R.id.apps_state_tv,
					bean.content);
			Bitmap bm = ((BitmapDrawable) TAApplication.getApplication()
					.getResources().getDrawable(R.drawable.ad_notify_icon))
					.getBitmap();
			notification.contentView.setImageViewBitmap(R.id.myicon, bm);
			Intent intent = new Intent("android.intent.action.VIEW",
					Uri.parse(bean.httpUrl));
			PendingIntent pendingIntent = PendingIntent.getActivity(
					TAApplication.getApplication(), 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			notification.contentIntent = pendingIntent;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			// 发送通知
			notificationManager.notify(22140, notification);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private StatFs getStatFs(String path) {
		try {
			return new StatFs(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private long calculateSizeInMB(StatFs stat) {
		if (stat != null) {
			return stat.getAvailableBlocks() * 1L * stat.getBlockSize();
		}
		return 0;
	}

	private long calculateTotalSizeInMB(StatFs stat) {
		if (stat != null) {
			return stat.getBlockCount() * 1L * stat.getBlockSize();
		}
		return 0;
	}

}
