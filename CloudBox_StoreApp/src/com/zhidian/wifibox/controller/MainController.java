package com.zhidian.wifibox.controller;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.AutoUpdateBean;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.MemoryBean;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.InfoUtil;

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

	@SuppressLint("NewApi")
	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(REGISTER_USERINFO)) {
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
			CDataDownloader.getPostData(CDataDownloader.getRegisterUrl(),
					params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								// 成功
								if (statusCode == 0) {
									SharedPreferences prefs = PreferenceManager
											.getDefaultSharedPreferences(TAApplication
													.getApplication());
									Editor editor = prefs.edit();
									editor.putBoolean("hasRegisterUserInfo",
											true);
									editor.commit();
									String uuid = json.optString("result", "");
									if (!TextUtils.isEmpty(uuid)) {
										InfoUtil.saveUUID(uuid.getBytes());
									}
								}
							} catch (Exception e) {
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onFailure(Throwable error) {
							Log.d("MainController", error.getMessage());
						}

						@Override
						public void onFinish() {
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
