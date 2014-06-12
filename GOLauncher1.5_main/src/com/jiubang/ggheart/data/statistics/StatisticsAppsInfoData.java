package com.jiubang.ggheart.data.statistics;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.database.Cursor;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.net.util.HttpUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DataProvider;
import com.jiubang.ggheart.data.tables.StatisticsAppDataTable;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 类描述:统计用户软件列表及软件点击次数管理类。
 * 功能详细描述:
 * 
 * @author  huyong
 * @date  [2012-9-12]
 */
//CHECKSTYLE:OFF
public class StatisticsAppsInfoData {

	public final static int ERROR_CODE_NO_SUCH_TABLE = 1;
	public final static int ERROR_CODE_NO_SUCH_COLUMN = 2;
	public final static int ERROR_CODE_EXIST_SUCH_COLUMN = 3;

	/**
	 * 
	 * 类描述:软件列表统计数据bean
	 * 功能详细描述:
	 * 
	 * @author  huyong
	 * @date  [2012-9-12]
	 */
	final static class Data {
		public String mPkgName; // 包名
		public int mClickCount = 0; // 点击次数
		public int mVerCode = 0; // 版本号
		public String mVerName = "1.0"; // 版本名称
	}

	public static String getAllAppsInfo(Context context, boolean isNeedClickCnt) {
		// long curTime = SystemClock.currentThreadTimeMillis();
		final String comma = ",";
		final String sharp = "#";
		StringBuffer sb = new StringBuffer();
		// 扫描出当前内存中所有程序
		List<Data> datas = scanApps(context, isNeedClickCnt);
		if (datas == null) {
			datas = new ArrayList<Data>();
		}
		// Log.i("statics", "datas.size1 =  " + datas.size());
		// 通过数据库中内容进行修正处理一下 ，内存中有，而数据库中没有则不理，内存中没有，而数据库中有则补充添加
		if (isNeedClickCnt) {
			//若当前需要统计点击次数，则需要从DB中进行更新获取，否则，不需要进行更新获取。
			updateDatasFromDB(context, datas);
		}
		// Log.i("statics", "datas.size2 =  " + datas.size());

		PackageManager pm = context.getPackageManager();
		for (Data data : datas) {
			// 包名
			sb.append(data.mPkgName);
			sb.append(comma);
			// 点击数
			sb.append(data.mClickCount);
			sb.append(comma);
			// 版本号
			sb.append(data.mVerCode);
			sb.append(comma);
			// 版本名称
			sb.append(data.mVerName);
			sb.append(comma);

			// TODO:增加是否内置标志
			boolean isSystemApp = false;
			try {
				ApplicationInfo applicationInfo = pm.getApplicationInfo(data.mPkgName, 0);
				isSystemApp = ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
						|| ((applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				Log.i("statisAppsInfo", "getAllAppsInfo has exception " + e.getMessage());
			}
			int isSystem = isSystemApp ? 1 : 0;
			sb.append(isSystem);

			sb.append(comma);
			// 非200的渠道，要添加apk签名
			if (!"200".equals(GoStorePhoneStateUtil.getUid(context))) {
				// 对于2.2机型以下，不取签名信息
				if (Build.VERSION.SDK_INT >= 8) {
					sb.append(getAppSignature(pm, data.mPkgName));
				}
			}

			// 上传apk size
			sb.append(comma);
			sb.append(getApkSize(pm, data.mPkgName));

			sb.append(sharp);
		}

		// long nowTime = SystemClock.currentThreadTimeMillis();
		// Log.i("statics", "time =  " + (nowTime - curTime));
		return sb.toString();
	}

	/**
	 * 查找出列表中与指定包名相同的data项
	 * 
	 * @author huyong
	 * @param datas
	 * @param pkgName
	 * @return
	 */
	private static Data findDataInMemory(final List<Data> datas, final String pkgName) {
		if (datas == null || datas.size() <= 0 || pkgName == null) {
			return null;
		}

		Data result = null;
		for (Data data : datas) {
			if (data.mPkgName.equals(pkgName)) {
				result = data;
				break;
			}
		}
		return result;
	}

	/**
	 * 将当前内存中的数据与DB中的数据进行比对，若DB有，内存没有，则将其添加到内存数据中；若DB有，内存也有，则将DB中数据更新到内存中
	 * 
	 * @author huyong
	 * @param context
	 * @param datas
	 *            ：当前内存中的数据
	 */
	private static void updateDatasFromDB(Context context, List<Data> datas) {
		Data data = null;
		Cursor cursor = null;
		String tmpStr = null;
		try {
			cursor = DataProvider.getInstance(context).getStatisticsAllDataInfos();
			if (null != cursor && cursor.moveToFirst()) {
				int pkgIndex = cursor.getColumnIndex(StatisticsAppDataTable.PKGNAME);
				int clickIndex = cursor.getColumnIndex(StatisticsAppDataTable.CLICKCNT);
				int vercodeIndex = cursor.getColumnIndex(StatisticsAppDataTable.VERCODE);
				int vernameIndex = cursor.getColumnIndex(StatisticsAppDataTable.VERNAME);

				do {
					// 包名
					tmpStr = cursor.getString(pkgIndex);
					// 假图标不进行统计
					if (LauncherEnv.GO_STORE_PACKAGE_NAME.equals(tmpStr)
							|| LauncherEnv.GO_THEME_PACKAGE_NAME.equals(tmpStr)
							|| LauncherEnv.GO_WIDGET_PACKAGE_NAME.equals(tmpStr)) {
						continue;
					}
					// 内存中没有，则新增一个。
					data = findDataInMemory(datas, tmpStr);
					if (data == null) {
						data = new Data();
						data.mPkgName = tmpStr;
						datas.add(data);
					}
					// 点击数
					data.mClickCount = cursor.getInt(clickIndex);
					if (data.mVerCode == 0) {
						// 版本号
						data.mVerCode = cursor.getInt(vercodeIndex);
					}
					if (data.mVerName == null || data.mVerName.equals("1.0")) {
						// 版本名称
						data.mVerName = cursor.getString(vernameIndex);
					}

				} while (cursor.moveToNext());
			}

		} catch (Exception e) {
			// TODO: handle exception
			 Log.i("AppInfo", "updateDatasFromDB has exception = " + e.getMessage());
		} finally {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

	/**
	 * 保存应用程序信息,增加点击次数
	 * 
	 * @author huyong
	 * @param pkgName
	 */
	public static void addAppInfoClickedCount(final Intent intent, Context context) {
		synchronized (DataProvider.DB_LOCK) {
			if (intent == null || intent.getComponent() == null) {
				return;
			}
			String pkgName = intent.getComponent().getPackageName();
			if (pkgName == null) {
				return;
			}

			// 自增DB中数据
			DataProvider dataProvider = DataProvider.getInstance(context);

			// 若数据库中不存在，则需首先插入一条，否则，直接添加点击数即可。
			int isExist = dataProvider.isExistStatisticsDataInfo(pkgName);
			if (isExist != ERROR_CODE_EXIST_SUCH_COLUMN) {
				// 数据库中不存在该app信息
				Data data = new Data();
				getDataByPkgName(data, context, pkgName);
				if (isExist == ERROR_CODE_NO_SUCH_TABLE) {
					// 重置数据，即清理掉数据表
					dataProvider.resetStatisticsAllDataInfos();
				}
				// 插入到数据库
				insertDataToDB(dataProvider, data);
			}

			dataProvider.addClickCntStatisticsAllDataInfos(pkgName);
		}
	}

	/**
	 * 重置所有统计数据
	 * 
	 * @author huyong
	 * @param context
	 */
	public static void resetStatisticsAllDataInfos(Context context) {
		synchronized (DataProvider.DB_LOCK) {
			DataProvider dataProvider = DataProvider.getInstance(context);
			// 重置数据
			dataProvider.resetStatisticsAllDataInfos();
			
			// 重新保存当前所有值
			insertAllInfoApps(context);
		}
	}

	/**
	 * 保存当前可扫描出所有app信息
	 * 
	 * @author huyong
	 * @param context
	 */
	private static void insertAllInfoApps(Context context) {
		// 重新扫描出当前最新
		List<Data> datas = scanApps(context, true);

		DataProvider dataProvider = DataProvider.getInstance(context);
		dataProvider.beginTransaction();

		try {
			for (Data data : datas) {
				insertDataToDB(dataProvider, data);
			}

			dataProvider.setTransactionSuccessful();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			dataProvider.endTransaction();
		}

	}

	private static void insertDataToDB(final DataProvider dataProvider, final Data data) {
		ContentValues values = new ContentValues();
		values.put(StatisticsAppDataTable.PKGNAME, data.mPkgName);
		values.put(StatisticsAppDataTable.CLICKCNT, data.mClickCount);
		values.put(StatisticsAppDataTable.VERCODE, data.mVerCode);
		values.put(StatisticsAppDataTable.VERNAME, data.mVerName);

		dataProvider.insertStatisticsAllDataInfos(values);
	}

	private static List<Data> scanApps(Context context, boolean isNeedAddSpecialApp) {
//		List<ResolveInfo> infos = getLauncherApps(context);
		List<ApplicationInfo> apps = getAllApps(context);
		int size = apps.size();
		List<Data> datas = new ArrayList<Data>(size);
		Data data = null;
		String pkgName = null;
		Set<String> tmpSet = new HashSet<String>(size);
		for (ApplicationInfo info : apps) {
			try {
				if((info.flags & ApplicationInfo.FLAG_SYSTEM)==0)
				{
					pkgName = info.packageName.toString();
//					pkgName = info.activityInfo.packageName.toString();
					if (tmpSet.contains(pkgName)) {
						// 已存在，则不重复统计
						continue;
					} else {
						tmpSet.add(pkgName);
					}
					data = new Data();
					getDataByPkgName(data, context, pkgName);
					datas.add(data);
				}

				// 如果这个过程过错，这个数据应该弃掉

			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		if (isNeedAddSpecialApp) {
			addSpecialApps(context, datas);
		}
		
		return datas;
	}
	
	/**
	 * 功能简述:增加特别的图标，纯业务需要。
	 * 功能详细描述:增加对应用中心、游戏中心、装机必备、玩机必备的统计。
	 * 注意:
	 * @param datas
	 */
	private static void addSpecialApps(Context context, List<Data> datas) {
		//应用中心
		ComponentName com = new ComponentName(LauncherEnv.RECOMMAND_CENTER_PACKAGE_NAME,
				ICustomAction.ACTION_FUNC_SHOW_RECOMMENDCENTER);
		addSpecialApp(context, datas, com);
		
		//游戏中心
		ComponentName gameComp = new ComponentName(LauncherEnv.GAME_CENTER_PACKAGE_NAME,
				ICustomAction.ACTION_FUNC_SHOW_GAMECENTER);
		addSpecialApp(context, datas, gameComp);
		
		//TODO:
		//装机必备
		//玩机必备
		
	}
	
	private static void addSpecialApp(Context context, List<Data> datas, ComponentName com) {
		AppDataEngine appDataEngine = AppDataEngine.getInstance(context); 
		if (appDataEngine != null && appDataEngine.isAppExist(com)) {
			Data data = new Data();
			data.mPkgName = com.getPackageName();
			datas.add(data);
			Log.i("statics", "addSpecialApp() pkg = " + data.mPkgName);
		}
	}

	private static void getDataByPkgName(Data data, final Context context, String pkgName) {
		if (data == null || context == null || pkgName == null) {
			return;
		}
		data.mPkgName = pkgName;
		data.mVerCode = AppUtils.getVersionCodeByPkgName(context, pkgName);
		data.mVerName = AppUtils.getVersionNameByPkgName(context, pkgName);
	}
	
	private static List<ApplicationInfo> getAllApps(Context context) {
		List<ApplicationInfo> apps = null;
		PackageManager packageMgr = context.getPackageManager();
		try {
			apps = packageMgr.getInstalledApplications(0);
		} catch (Exception e) {
			Log.i("AppInfo", "getLauncherApps has exception = " + e.getMessage());
		}
		if (apps == null) {
			apps = new ArrayList<ApplicationInfo>();
		}
		packageMgr = null;
		return apps;
	}

	private static List<ResolveInfo> getLauncherApps(Context context) {
		List<ResolveInfo> infos = null;
		PackageManager packageMgr = context.getPackageManager();
		Intent intent = new Intent(ICustomAction.ACTION_MAIN);
		// update by zhoujun，现在需要显示主题等图标
		intent.addCategory("android.intent.category.LAUNCHER");
		// update by zhoujun end 2010-05-02
		try {
			infos = packageMgr.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
		} catch (Exception e) {
			Log.i("AppInfo", "getLauncherApps has exception = " + e.getMessage());
		}
		if (infos == null) {
			infos = new ArrayList<ResolveInfo>();
		}
		packageMgr = null;
		return infos;
	}

	/**
	 * 根据包名，获取程序签名
	 * 
	 * @param context
	 * @param packname
	 * @return
	 */
	public static String getAppSignature(PackageManager pm, String packname) {
		InputStream input = null;
		String publicKeyStr = null;
		try {
			PackageInfo packinfo = pm.getPackageInfo(packname, PackageManager.GET_SIGNATURES);
			Signature[] signatures = packinfo.signatures;
			byte[] cert = signatures[0].toByteArray();
			input = new ByteArrayInputStream(cert);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate c = (X509Certificate) cf.generateCertificate(input);
			publicKeyStr = c.getPublicKey().toString();

			// 使用DSA Public Key签名
			int str = publicKeyStr.indexOf("DSA Public Key");
			if (str > -1) {
				int index = publicKeyStr.indexOf("y:");
				publicKeyStr = publicKeyStr.substring(index + "y:".length() + 1);
			} else {
				//4.1以下 使用 RSA Public Key签名
				String strIndex = "modulus: ";
				String secordStrIndex = "\n";

				//4.1以上 使用OpenSSLRSAPublicKey签名
				if (Build.VERSION.SDK_INT >= 16) {
					strIndex = "modulus=";
					secordStrIndex = ",";
				}

				int index1 = publicKeyStr.indexOf(strIndex);
				int index3 = publicKeyStr.indexOf(secordStrIndex, index1);
				publicKeyStr = publicKeyStr.substring(index1 + strIndex.length() + 1, index3);
			}
			String baseValue = Base64.encodeToString(c.getPublicKey().getEncoded(), Base64.NO_WRAP);
			String key = HttpUtil.mD5generator(baseValue);
			return key;
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		} catch (CertificateException e) {
			// e.printStackTrace();
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return null;
	}

	private static long getApkSize(PackageManager pm, String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return 0;
		}
		try {
			PackageInfo packinfo = pm.getPackageInfo(packageName, 0);
			if (packinfo != null) {
				File file = new File(packinfo.applicationInfo.publicSourceDir);
				if (file.exists()) {
					return file.length();
				}
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			Log.i("AppsInfo", "getApkSize has exception " + e.getMessage());
		}
		return 0;
	}

	/**
	 * 获得应用点击次数
	 * 
	 * @author huyong
	 * @param pkgName
	 */
	public static int getAppClickedCount(final Intent intent, Context context) {
		if (intent == null || intent.getComponent() == null) {
			return 0;
		}
		String pkgName = intent.getComponent().getPackageName();
		if (pkgName == null) {
			return 0;
		}

		// 自增DB中数据
		DataProvider dataProvider = DataProvider.getInstance(context);

		// 若数据库中不存在，则需首先插入一条，否则，直接添加点击数即可。
		int isExist = dataProvider.isExistStatisticsDataInfo(pkgName);
		if (isExist != ERROR_CODE_EXIST_SUCH_COLUMN) {
			if (isExist == ERROR_CODE_NO_SUCH_TABLE) {
				// 重置数据，即清理掉数据表
				dataProvider.resetStatisticsAllDataInfos();
			}
			return 0;
		}

		return dataProvider.getClickCntStatisticsAllDataInfos(pkgName);
	}
}
