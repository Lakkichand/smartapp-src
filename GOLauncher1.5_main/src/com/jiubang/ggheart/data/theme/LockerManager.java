package com.jiubang.ggheart.data.theme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 锁屏精选主题管理
 */
public class LockerManager {

	public static final String XMLFILE = LauncherEnv.Path.GOTHEMES_PATH + "paidlockers.xml";
	// 图片保存路径
	public final static String ICONPATH = LauncherEnv.Path.GOTHEMES_PATH + "lockericon/";
	public final static String GOCONTACT_THEME_ACTION = "com.jiubang.goscreenlock.theme";
	public final static String THEME_CATEGORY = "android.intent.category.INFO";
	public static final String GOLOCKER_PACKAGE = "com.jiubang.goscreenlock";
	public final static int DELETE_THEME = 500;
	public final static String THEME_PREVIEW_FOLDER = "preview";
	// 收费主题的限制显示个数
	// private final static int LIMIT_COUNT_OF_TOLL_THEME = 6;

	public static final String DEFAULT_THEME_PACKAGE = "default";
	public static Context sContext = null;
	public static int sSellModeCount = 0;
	private static LockerManager sInstance = null;

	private HashMap<String, Integer> mFullScreenSpTheme = null;
	private GoLockerThemeManager mLockerThemeManager;

	private LockerManager() {
		mLockerThemeManager = new GoLockerThemeManager(sContext);
		mFullScreenSpTheme = new HashMap<String, Integer>();
		mFullScreenSpTheme.put("com.jiubang.goscreenlock.theme.dark", Integer.valueOf(204));
		mFullScreenSpTheme.put("com.jiubang.goscreenlock.theme.icecream", Integer.valueOf(205));
		mFullScreenSpTheme.put("com.jiubang.goscreenlock.theme.fishpool", Integer.valueOf(203));
		mFullScreenSpTheme.put("com.jiubang.goscreenlock.theme.colorbox", Integer.valueOf(201));
	};

	/**
	 * 获取单例对象
	 * 
	 * @param ct
	 * @return
	 */
	static synchronized public LockerManager getInstance(Context ct) {
		if (sInstance != null) {
			return sInstance;
		}
		sContext = ct;
		sInstance = new LockerManager();
		return sInstance;
	}

	/**
	 * 更新单例对象
	 * 
	 * @param ct
	 * @return
	 */
	static synchronized public LockerManager resetInstance(Context ct) {
		sInstance = new LockerManager();
		return sInstance;
	}

	/**
	 * 获取锁屏精选主题
	 * 
	 * @return
	 */
	public ArrayList<ThemeInfoBean> getFeaturedThemeInfoBeans(BroadCasterObserver observer) {

		return new OnlineThemeGetter(sContext).getFeaturedThemeInfoBeans(getInstallThemeInfoBean(),
				ThemeConstants.LOCKER_FEATURED_THEME_ID, false, observer);

	}

	// /**
	// * 排序精选主题
	// *
	// * @author huyong
	// * @param arrayList
	// * @param recommendCount
	// * @return
	// */
	// private ArrayList<ThemeInfoBean> filterRecommendFeaturedTheme(
	// ArrayList<ThemeInfoBean> arrayList, int recommendCount) {
	// ArrayList<ThemeInfoBean> result = null;
	// if (arrayList == null || arrayList.size() <= 0) {
	// return result;
	// }
	//
	// result = new ArrayList<ThemeInfoBean>();
	// // 前面固定的个数
	// int firstCount = recommendCount;
	// if (arrayList.size() < recommendCount) {
	// firstCount = arrayList.size();
	// }
	//
	// for (int i = 0; i < firstCount; i++) {
	// result.add(arrayList.get(i));
	// }
	// // 获取待显示的收费主题
	// ArrayList<Integer> toShowPaidThemeIndexList = getRandomInts(
	// arrayList.size() - recommendCount, recommendCount,
	// arrayList.size());
	//
	// if (toShowPaidThemeIndexList != null) {
	// for (int i = 0; i < toShowPaidThemeIndexList.size(); i++) {
	// int index = toShowPaidThemeIndexList.get(i);
	// result.add(arrayList.get(index));
	// }
	// }
	//
	// return result;
	// }

	/**
	 * 产生半闭包随机数的集合的方法
	 * 
	 * @param count
	 *            随机数的数目
	 * @param min
	 *            随机数最小值
	 * @param max
	 *            随机数最大值（不含）
	 * @return 返回随机数[min - max)区间内的不重复整数
	 */
	private ArrayList<Integer> getRandomInts(int count, int min, int max) {
		if (count < 0 || min > max) {
			return null;
		}
		if (count > (max - min)) {
			count = max - min;
		}
		ArrayList<Integer> result = new ArrayList<Integer>();
		Random rand = new Random();
		for (int i = 0, randomNumber = 0; i < count;) {
			randomNumber = min + rand.nextInt(max - min);
			if (!result.contains(randomNumber)) {
				// LogUtil.i("LockerManager", "random = " + randomNumber);
				result.add(randomNumber);
				i++;
			}
		}
		return result;
	}

	public ArrayList<ThemeInfoBean> getInstallThemeInfoBean() {
		ArrayList<ThemeInfoBean> beans = new ArrayList<ThemeInfoBean>();
		// 已安装
		Map<CharSequence, CharSequence> mInstalledThemePackage = mLockerThemeManager
				.queryInstalledTheme();
		ThemeInfoBean bean = null;
		Iterator<CharSequence> iterator = mInstalledThemePackage.keySet().iterator();
		for (; iterator.hasNext();) {
			CharSequence packageName = iterator.next();
			bean = new ThemeInfoBean();
			bean.setPackageName(packageName.toString());
			beans.add(bean);
		}
		return beans;
	}
	
	
	public boolean isZipTheme(Context context, String packageName) {
		return mLockerThemeManager.isZipTheme(context, packageName);
	}
	
	public String getZipThemeFileName(String packageName) {
		return mLockerThemeManager.getZipThemeFileName(packageName);
	}
	public static String getLockerUid(Context context) {
		String result = "200";
		if (context != null) {
			ContentResolver contentResolver = context.getContentResolver();
			if (contentResolver != null) {
				String uri = AppUtils.getCurLockerPkgName(context);
				StringBuffer sb = new StringBuffer();
				sb.append("content://");
				sb.append(uri);
				sb.append("/theme");
				Uri golockerUri = Uri.parse(sb.toString());
				Cursor cursor = null;
				try {
					cursor = contentResolver.query(golockerUri, null, null, null, null);
					if (cursor != null && cursor.moveToFirst()) {
						int index = cursor.getColumnIndex("uid");
						result = cursor.getString(index);
					}
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					if (cursor != null) {
						cursor.close();
					}
				}
			}
		}
		return result;
	}
}
