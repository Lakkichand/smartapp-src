package com.jiubang.ggheart.components.facebook;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.gau.utils.cache.utils.CacheFileUtils;
import com.gau.utils.cache.utils.ZipFilesUtils;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.INetRecord;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingBackupActivity;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask;
import com.jiubang.ggheart.apps.desks.backup.ImportDatabaseTask;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-12-21]
 */
public class FacebookBackupUtil {

	public static final int HANDLER_DOWNLOAD_SUCCESS = 1; // 下载成功
	public static final int HANDLER_DOWNLOAD_FAIL = 2; // 下载失败
	public static final int HANDLER_DOWNLOAD_LOCALISNEWEST = 3; // 下载请求时,本地FB备份已是最新
	public static final int HANDLER_UPLOAD_SUCCESS = 4; // 上传成功
	public static final int HANDLER_UPLOAD_FAIL = 5; // 上传失败


	

	public static void backupFacebookDB(DeskSettingBackupActivity activity) {
		if (activity != null && !activity.isFinishing()) {
			//FB备份
			com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask task = new com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask();
			task.setActivity(activity);
			task.setListner(activity);
			task.setType(com.jiubang.ggheart.apps.desks.backup.ExportDatabaseTask.TYPE_FACEBOOK);
			task.execute();
		}
	}

	public static void restoreFacebookDB(final DeskSettingBackupActivity activity) {
		FacebookBackupUtil.restoreFBData(activity, new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case FacebookBackupUtil.HANDLER_DOWNLOAD_SUCCESS :
						Toast.makeText(activity, R.string.facebook_taost_downloadsuccess,
								Toast.LENGTH_SHORT).show();
						//下载成功
						showRestoreDBDialog(activity);
						break;
						
					case FacebookBackupUtil.HANDLER_DOWNLOAD_LOCALISNEWEST :
						showRestoreDBDialog(activity);
						break;

					case FacebookBackupUtil.HANDLER_DOWNLOAD_FAIL :
						//下载失败
						Toast.makeText(activity, R.string.facebook_taost_downloadfail,
								Toast.LENGTH_SHORT).show();
						if (activity != null && !activity.isFinishing()) {
							activity.updateFacebookView();
						}
						break;

					default :
						break;
				}
			}
		});
	}

	private static boolean backupFiles() {
		try {
			ArrayList<File> backupFiles = new ArrayList<File>();

			String id = GoFacebookUtil.getUserInfo().getId();
			String fbpath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_FACEBOOK_DIR + "/"
					+ id;
			File file = new File(fbpath);
			if (file.exists()) {
				backupFiles.add(file);
			}

			//生成本地打包文件,如果有旧文件存在,则会先删除旧文件
			String fileName = id + "_localzip";
			String filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FACEBOOK_DIR + fileName;
			boolean created = FileUtil.createFile(filePath, true);
			if (created) {
				File zipFile = new File(filePath);
				ZipFilesUtils.zipFiles(backupFiles, zipFile);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();

			GoFacebookUtil.log("zipfile fail");
		}
		return false;
	}

	public static void backupFBData(final Activity activity, final Handler handler) {
		boolean backuped = backupFiles();
		if (!backuped) {
			GoFacebookUtil.log("backup fail");
			return;
		}

		String url = getUrl(activity, "1"); //获取URL地址
		try {
			ByteArrayOutputStream byopt = new ByteArrayOutputStream();
			DataOutputStream daopt = new DataOutputStream(byopt);

			String fileName = GoFacebookUtil.getUserInfo().getId() + "_localzip";
			String filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.FACEBOOK_DIR + fileName;
			int filelength = (int) FileUtil.getFileSize(filePath);
			daopt.writeInt(filelength); // 文件长度
			byte[] fileDate = CacheFileUtils.readFileToByte(filePath);
			daopt.write(fileDate); // 文件流

			byopt.close();
			daopt.close();

			THttpRequest request = new THttpRequest(url, byopt.toByteArray(),
					new IConnectListener() {
						@Override
						public void onStart(THttpRequest arg0) {
							//Log.e("lch", "onStart");
						}

						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							if (response != null) {
								final int type = response.getResponseType();
								switch (type) {
									case FacebookResponse.RESPONSE_TYPE_NORMAL :
										handler.sendEmptyMessage(FacebookBackupUtil.HANDLER_UPLOAD_SUCCESS);
										break;

									case FacebookResponse.RESPONSE_TYPE_NODATA :
										handler.sendEmptyMessage(FacebookBackupUtil.HANDLER_UPLOAD_FAIL);
										break;

									default :
										break;
								}
							}
						}

						@Override
						public void onException(THttpRequest arg0, int arg1) {
						}
					});

			request.setOperator(new BackupStringOperator(activity)); //设置返回数据类型-字符串
			request.addHeader("Content-Type", "application/octet-stream; charset=UTF-8"); // 加入流content-Type说明,不然正式服务器解释不了

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
			SimpleHttpAdapter httpAdapter = SimpleHttpAdapter.getInstance(activity);
			httpAdapter.addTask(request);
		} catch (Exception e) {
			//Log.e("lch", "e:" + e.toString());
			e.printStackTrace();
			//toast
			Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	public static void restoreFBData(final Activity activity, final Handler handler) {
		String url = getUrl(activity, "2"); //获取URL地址
		try {
			THttpRequest request = new THttpRequest(url, null, new IConnectListener() {
				@Override
				public void onStart(THttpRequest arg0) {
				}

				@Override
				public void onFinish(THttpRequest request, IResponse response) {
					if (activity != null && !activity.isFinishing() && response != null) {
						final int type = response.getResponseType();
						switch (type) {
							case FacebookResponse.RESPONSE_TYPE_NORMAL :
								//下载成功
								handler.sendEmptyMessage(HANDLER_DOWNLOAD_SUCCESS);
								break;

							case FacebookResponse.RESPONSE_TYPE_LOCALISNEWEST :
								//本地FB备份文件已是最新
								handler.sendEmptyMessage(HANDLER_DOWNLOAD_LOCALISNEWEST);
								break;

							case FacebookResponse.RESPONSE_TYPE_NODATA :
								//下载失败
								handler.sendEmptyMessage(HANDLER_DOWNLOAD_FAIL);
								break;

							default :
								break;
						}
					}
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
				}
			});

			request.setOperator(new RestoreStringOperator()); //设置返回数据类型-字符串

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
			SimpleHttpAdapter httpAdapter = SimpleHttpAdapter.getInstance(activity);
			httpAdapter.addTask(request);
		} catch (Exception e) {
			e.printStackTrace();
			//toast
			Toast.makeText(activity, "ERROR", Toast.LENGTH_SHORT).show();
			return;
		}
	}

	private static String getUrl(Context context, String funid) {
		StringBuffer buffer = new StringBuffer(GoFacebookUtil.SERVER); // 正式服务器
		buffer.append("funid=" + funid);

		String imei = Statistics.getVirtualIMEI(context);
		String vps = getVps(context, imei);
		buffer.append("&vps=" + vps);
		String uid = GoStorePhoneStateUtil.getUid(context);
		buffer.append("&channel=" + uid);
		String fbid = GoFacebookUtil.getUserInfo().getId();
		buffer.append("&facebookid=" + fbid);

		if ("2".equals(funid)) {
			PreferencesManager sp = new PreferencesManager(GoLauncher.getContext(),
					IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
			long time = -1;
			String id = GoFacebookUtil.getUserInfo().getId();
			String path = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_FACEBOOK_DIR + "/"
					+ id + "_download";
			File file = new File(path);
			if (file != null && file.exists()) {
				time = sp.getLong(GoFacebookUtil.getUserInfo().getId(), -1);
			}
			buffer.append("&updatetime=" + time);
		}
		GoFacebookUtil.log("URL=" + buffer);

		return buffer.toString();
	}
	
	/**
	 * 应用中心/游戏中心根据vps定义规范，获取本机vps信息
	 * 
	 * @param context
	 * @param imei
	 * @return vps字符串
	 */
	public static String getVps(Context context, String imei) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		StringBuilder vpsStringBuilder = new StringBuilder(64);
		vpsStringBuilder.append("2#");
		vpsStringBuilder.append("Android#");
		vpsStringBuilder.append(Build.MODEL + "#");
		vpsStringBuilder.append(imei + "#");
		vpsStringBuilder.append("#"); // 产品id不上传
		vpsStringBuilder.append(width + "_" + height + "#"); // 分辨率

		// update by zhoujun,添加三个字段：#sdk#imsi#是否安装电子市场(0:未安装 1：已安装)
		vpsStringBuilder.append("#"); // 版本号不上传
		vpsStringBuilder.append(Build.VERSION.SDK_INT + "#");
		vpsStringBuilder.append("#"); // imsi码不上传
		int isExistGoogleMarket = GoStoreAppInforUtil.isExistGoogleMarket(context) ? 1 : 0;
		vpsStringBuilder.append(isExistGoogleMarket + "#");

		//是否有SD卡
		String hasSdCard = "0";
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			hasSdCard = "1";
		}
		//lang和region两个字段不用再上传，转空串，所以补两个#
		vpsStringBuilder.append(hasSdCard + "#");
		vpsStringBuilder.append(getLanguage() + "#");
		vpsStringBuilder.append(getRegion(context));
		
		
		String vps = vpsStringBuilder.toString();
		try {
			vps = URLEncoder.encode(vps, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return vps;
	}
	
	private static String getLanguage() {
		Locale locale = null;
		if (DeskResourcesConfiguration.getInstance() != null) {
			locale = DeskResourcesConfiguration.getInstance().getmLocale();
		}
		//如果获取桌面的local为空。则获取系统的local
		if (locale == null) {
			locale = Locale.getDefault();
		}
		
		return locale.getLanguage().toLowerCase();
	}
	
	private static String getRegion(Context context) {
		String ret = null;
		Locale locale = null;
		if (DeskResourcesConfiguration.getInstance() != null) {
			locale = DeskResourcesConfiguration.getInstance().getmLocale();
		}
		//如果获取桌面的local为空。则获取系统的local
		if (locale == null) {
			locale = Locale.getDefault();
		}
		try {

			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			// SIM卡状态
			boolean simCardUnable = telManager.getSimState() != TelephonyManager.SIM_STATE_READY;
			String simOperator = telManager.getSimOperator();
			// 如果SIM卡正常可用
			if (!(simCardUnable || TextUtils.isEmpty(simOperator))) {
				ret = telManager.getSimCountryIso();
			}

		} catch (Throwable e) {
			// e.printStackTrace();
		}
		if (ret == null || ret.equals("")) {
			ret = Locale.getDefault().getCountry().toLowerCase();
		}
		return ret;
	}

	private static void showRestoreDBDialog(final DeskSettingBackupActivity activity) {
		if (activity != null && !activity.isFinishing()) {
			DialogConfirm mNormalDialog = new DialogConfirm(activity);
			mNormalDialog.show();
			mNormalDialog.setTitle(activity.getString(R.string.attention_title));
			mNormalDialog.setMessage(activity
					.getString(R.string.facebook_restore_db_dialog_summary));
			mNormalDialog.setPositiveButton(null, new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (activity != null && !activity.isFinishing()) {
						ImportDatabaseTask task = new ImportDatabaseTask();
						task.setActivity(activity);
						task.setType(ExportDatabaseTask.TYPE_FACEBOOK);
						task.setListner(activity);
						task.execute();
					}
				}
			});
			
			mNormalDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				
				@Override
				public void onDismiss(DialogInterface dialog) {
					if (activity != null && !activity.isFinishing()) {
						activity.updateFacebookView();
					}
				}
			});
		}
	}
}
