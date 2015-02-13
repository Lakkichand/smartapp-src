package com.zhidian.wifibox.util;

import org.json.JSONObject;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.controller.MainController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.XBoxIdController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.XDataDownload;

/**
 * 盒子编号管理者，单例模式
 * 
 * 如果assets文件里面放的是默认boxid，则需要读取当前连接的盒子boxid替换掉
 * 
 * 第一个读取的boxid当做是这个包的默认boxid
 * 
 * @author xiedezhi
 * 
 */
public class BoxIdManager {
	/**
	 * 默认的boxid
	 */
	private static final String DEFAULT_BOXID = "BBBBBBBBBBBB";
	// TODO 更改默认boxid

	private Setting mSetting = new Setting(TAApplication.getApplication());
	/**
	 * 保存在SharedPreferences的KEY
	 */
	private static final String WIFI_BOX_KEY = "WIFI_BOX_KEY";
	/**
	 * 保存在本地代替assets/boxId的文件路径
	 */
	private static final String ASSETS_BOXID_FILEPATH = "/.XIANGWIFI/.assets/BOXID";

	/**
	 * 单实例
	 */
	private volatile static BoxIdManager sInstance = null;

	/**
	 * 获取实例对象
	 */
	public static BoxIdManager getInstance() {
		if (sInstance == null) {
			synchronized (BoxIdManager.class) {
				if (sInstance == null) {
					sInstance = new BoxIdManager();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 把当前需要使用的boxId保存起来
	 */
	public void saveBoxId(String boxId) {
		if (boxId == null) {
			return;
		}
		boxId = boxId.trim();
		mSetting.putString(WIFI_BOX_KEY, boxId);
	}

	/**
	 * 获取boxId
	 */
	public String getBoxId() {
		String ret = mSetting.getString(WIFI_BOX_KEY);
		if (TextUtils.isEmpty(ret)) {
			ret = readAssetsBoxId();
		}
		if (TextUtils.isEmpty(ret)) {
			ret = DEFAULT_BOXID;
		}
		ret = ret.trim();
		saveBoxId(ret);
		return ret;
	}

	/**
	 * 如果assets/boxId是默认的编号，则连上第一个盒子时，用该盒子的编号替换assets/boxId
	 */
	public void saveAssetsBoxId(String boxId) {
		if (boxId == null) {
			return;
		}
		boxId = boxId.trim();
		FileUtil.saveByteToFile(boxId.getBytes(), PathConstant.SDCARD
				+ ASSETS_BOXID_FILEPATH);
		FileUtil.saveByteToFile(boxId.getBytes(), PathConstant.CACHE
				+ ASSETS_BOXID_FILEPATH);
	}

	/**
	 * 读取保存在本地的boxId，如果是空，则返回assets/boxId
	 */
	public String readAssetsBoxId() {
		String internalBoxid = AppUtils.readAssetsFile(
				TAApplication.getApplication(), "boxId");
		// 优先读取assets/boxid
		if (!isDefaultBoxId(internalBoxid)) {
			saveAssetsBoxId(internalBoxid);
			return internalBoxid;
		}
		byte[] bytes = FileUtil.getByteFromFile(PathConstant.CACHE
				+ ASSETS_BOXID_FILEPATH);
		if (bytes == null || bytes.length <= 0) {
			bytes = FileUtil.getByteFromFile(PathConstant.SDCARD
					+ ASSETS_BOXID_FILEPATH);
		}
		if (bytes != null && bytes.length > 0
				&& (!TextUtils.isEmpty((new String(bytes)).trim()))) {
			saveAssetsBoxId(new String(bytes));
			String ret = new String(bytes);
			return ret;
		}
		saveAssetsBoxId(internalBoxid);
		return internalBoxid;
	}

	/**
	 * 校验市场安装量boxid
	 */
	public void checkMarketInstallBoxId() {
		boolean b = mSetting
				.getBoolean(Setting.HAS_VERIFY_MARKETINSTALL, false);
		if (!b) {
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			if (BoxIdManager.getInstance().isDefaultBoxId(
					BoxIdManager.getInstance().readAssetsBoxId())) {
				// 如果还没有读到盒子id，则不校验
				return;
			}
			RequestParams params = new RequestParams();
			params.put("uuId", InfoUtil.getUUID(TAApplication.getApplication()));
			params.put("boxNum", BoxIdManager.getInstance().readAssetsBoxId());
			CDataDownloader.getPostData(CDataDownloader.getVerifyBoxIdUrl(),
					params, new AsyncHttpResponseHandler() {

						@Override
						public void onSuccess(String content) {
							try {
								JSONObject json = new JSONObject(content);
								int statusCode = json.optInt("statusCode", -1);
								// 成功
								if (statusCode == 0) {
									mSetting.putBoolean(
											Setting.HAS_VERIFY_MARKETINSTALL,
											true);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
		}
	}

	/**
	 * 检查保存在本地文件的boxId是否默认boxId，如果是，则读取盒子的boxId来替换掉自己的boxId
	 * 
	 * @return 是默认boxId则返回true，否则返回false
	 */
	public boolean checkAssetsBoxId() {
		String boxid = readAssetsBoxId();
		if (DEFAULT_BOXID.equals(boxid)) {
			// 更新boxId
			if (ModeManager.checkRapidly()) {
				crawlBoxId();
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 从网络读取盒子boxId
	 */
	private void crawlBoxId() {
		TAApplication.getApplication().doCommand(
				TAApplication.getApplication().getString(
						R.string.xboxidcontroller),
				new TARequest(XBoxIdController.GAIN_BOXID, XDataDownload
						.getXBoxIdUrl()), new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						if (response.getData() == null) {
							return;
						}
						String boxId = (String) response.getData();
						boxId = boxId.trim();
						if (boxId.toLowerCase().contains("<html>")) {
							return;
						}
						if (!TextUtils.isEmpty(boxId.trim())) {
							if (isDefaultBoxId(readAssetsBoxId())) {
								saveAssetsBoxId(boxId.trim());
							}
						}
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {
					}
				}, true, false);
	}

	/**
	 * 是否默认的boxid
	 */
	public boolean isDefaultBoxId(String boxid) {
		return DEFAULT_BOXID.equals(boxid);
	}

}
