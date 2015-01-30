package com.zhidian.wifibox.controller;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.ta.util.http.RequestParams;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.DataParser;
import com.zhidian.wifibox.data.ErrorDesc;
import com.zhidian.wifibox.data.SpkInstallBean;
import com.zhidian.wifibox.db.dao.SpkFirstDao;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.util.StatisticsUtil;

/**
 * 插件部分 市场spk安装控制器 当第一次安装进入应用时调用
 * 
 * @author zhaoyl
 * 
 */
public class MarketInstallController extends TACommand {

	private static final String TAG = MarketInstallController.class
			.getSimpleName();
	/**
	 * 上传数据
	 */
	public static final String INSTALL_MARKET_SQL = "install_market_sql";
	/**
	 * 保存数据
	 */
	public static final String APP_INSTALL_DATA = "app_install_data";
	private Setting setting;

	@Override
	protected void executeCommand() {
		// 处理请求，这里是子线程操作.
		setting = new Setting(TAApplication.getApplication());
		String installTime = setting.getString(Setting.INSTALL_TIME);
		TARequest request = getRequest();
		String command = (String) request.getTag();
		final SpkInstallBean bean = new SpkInstallBean();
		if (command.equals(INSTALL_MARKET_SQL)) {
			if (!StatisticsUtil.verifyPhoneTable()) {
				// 如果没有上传phone表，先上传
				return;
			}
			SpkFirstDao dao = new SpkFirstDao(TAApplication.getApplication());
			List<SpkInstallBean> list = dao.getSpkData();
			if (list != null && list.size() > 0) {
				for (int i = 0; i < list.size(); i++) {
					SpkInstallBean ben = list.get(i);
					Log.e(TAG, "数据库有数据状态");
					upload(ben, dao);
				}
			} else {
				// 只在当是原来数据库不是保存在sd卡以下的旧版本升级到新版本，且第一次的安装 数据没有上传成功时调用。
				Log.e(TAG, "数据库无数据状态");
				SpkInstallBean ben2 = new SpkInstallBean();
				ben2.boxNum = InfoUtil.getBoxId(TAApplication.getApplication());
				ben2.uuId = InfoUtil.getUUID(TAApplication.getApplication());// 这里保存的UUID已废弃。
				ben2.installTime = installTime;
				ben2.installPackageName = AppUtils
						.getAllAppsString(TAApplication.getApplication());
				upload(ben2, dao);
			}

		} else if (command.equals(APP_INSTALL_DATA)) {
			// 先保存数据到数据库中
			bean.boxNum = InfoUtil.getBoxId(TAApplication.getApplication());
			bean.uuId = InfoUtil.getUUID(TAApplication.getApplication());// 这里保存的UUID已废弃。
			bean.installTime = installTime;
			bean.installPackageName = AppUtils.getAllAppsString(TAApplication
					.getApplication());

			// 先保存到数据库
			SpkFirstDao dao = new SpkFirstDao(TAApplication.getApplication());
			dao.saveSpkInstallInfo(bean);
		}
	}

	private void upload(final SpkInstallBean ben, final SpkFirstDao dao) {
		RequestParams params2 = new RequestParams();
		try {

			JSONObject json = new JSONObject();
			json.put("boxNum", ben.boxNum);// 盒子编号
			json.put("uuId", InfoUtil.getUUID(TAApplication.getApplication()));// 这个UUID现取，而不是取数据库中的。
			json.put("installTime", ben.installTime);// 安装时间
			json.put("installPackageName", ben.installPackageName);// 已安装包

			params2.put("json", json.toString());

		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		CDataDownloader.getPostData2(CDataDownloader.getpPlugInstall(),
				params2, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String content) {
						try {
							JSONObject json = new JSONObject(content);
							int statusCode = json.optInt("statusCode", -1);

							// 成功
							if (statusCode == 0) {// 0表示成功
								Log.e("MarketInstallController",
										"(数据库)上传第一次安装数据成功" + ben.installTime);
								dao.deleteOneData(ben.installTime);// 删除数据库中的数据
								setting.putBoolean(Setting.INSTALL_STATUS, true);// 改变状态，以后都不再发送数据
							} else if (statusCode == 1) {// 有误

								JSONArray array = json
										.optJSONArray("errorDesc");
								if (array != null) {
									for (int i = 0; i < array.length(); i++) {
										String str = array.getString(i);
										ErrorDesc error = DataParser
												.parseErrorDesc(str);// 解析错误信息
										Log.i(TAG, "错误参数名："
												+ error.parameterName + "错误类型"
												+ error.errorType);
									}
								}

								dao.deleteOneData(ben.installTime);// 删除数据库中的数据
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						MarketInstallController.this
								.sendSuccessMessage(content);
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onFailure(Throwable error) {
						// 失败
						Log.d(TAG, error.getMessage());
					}

					@Override
					public void onFinish() {
					}
				});
	}

}
