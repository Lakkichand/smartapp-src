/*
 * 文 件 名:  ThemePurchaseManager.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-8-21
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.gau.utils.cache.CacheManager;
import com.gau.utils.cache.encrypt.CryptTool;
import com.gau.utils.cache.impl.FileCacheImpl;
import com.gau.utils.cache.utils.CacheUtil;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster;
import com.jiubang.ggheart.billing.IPurchaseStateListener;
import com.jiubang.ggheart.billing.PurchaseStateManager;
import com.jiubang.ggheart.billing.PurchaseSupportedManager;
import com.jiubang.ggheart.billing.ThemeAppInBillingManager;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.broadcastReceiver.MyThemeReceiver;
import com.jiubang.ggheart.data.theme.zip.ZipResources;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author rongjinsong
 * @date [2012-8-21]
 */
public class ThemePurchaseManager extends BroadCaster implements IPurchaseStateListener {

	private ThemeAppInBillingManager mInBillingManager;
	private Context mContext;
	private static ThemePurchaseManager sSelf;
	private ArrayList<String> mTestPaidPks;
	private static boolean sDeBug = ThemeManager.SDEBUG;
	private CacheManager mCacheManager;
	private ThemePurchaseManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mInBillingManager = ThemeAppInBillingManager.getInstance(context);
		mCacheManager = new CacheManager(new FileCacheImpl(LauncherEnv.Path.GOTHEMES_PATH));
		if (sDeBug) {
			mTestPaidPks = new ArrayList<String>();
		}
	}

	public synchronized static ThemePurchaseManager getInstance(Context context) {
		if (sSelf == null) {
			sSelf = new ThemePurchaseManager(context);
		}
		return sSelf;
	}

	private synchronized static void clearSelf() {
		sSelf = null;
	}

	/**
	 * 根据主题包名，检查付费状态
	 * 
	 * @param packageName
	 * @return 返回主题付费后，下载的zip文件名称
	 */
	public static String queryPurchaseState(Context context, String packageName) {
		//		if (sDeBug) {
		//			if (mTestPaidPks.contains(packageName)) {
		//				return packageName;
		//			}
		//			return null;
		//		}
		return PurchaseStateManager.query(context, packageName);
		//		return mInBillingManager.checkBillingState(packageName);
	}

	public void requestPurchase(Activity activity, ThemeInfoBean bean, int position) {
		int staticsType;
		if (bean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
				|| bean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			staticsType = GuiThemeStatistics.THEME_LAUNCHER_TYPE;

		} else {
			staticsType = GuiThemeStatistics.THEME_LOCKER_TYPE;
		}
		int tabId = 0;
		if (bean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			tabId = ThemeConstants.STATICS_ID_FEATURED;
		} else if (bean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			tabId = ThemeConstants.STATICS_ID_HOT;
		} else if (bean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID) {
			tabId = bean.getSortId();
		} else {
			tabId = ThemeConstants.STATICS_ID_LOCKER;
		}
		if (activity instanceof ThemeDetailActivity) {
			GuiThemeStatistics.getInstance(mContext).saveUserDetailGet(activity,
					bean.getPackageName(), position, staticsType, "1", String.valueOf(tabId));
		} else {
			GuiThemeStatistics.getInstance(mContext).saveUserTouch(mContext, bean.getPackageName(),
					position, staticsType, "1", String.valueOf(tabId));
		}
		//		if (sDeBug) {
		//			purchaseState(1, bean.getPayId());
		//			mTestPaidPks.add(bean.getPayId());
		//		} else {
		mInBillingManager.requestPurchase(bean.getPackageName(), bean.getPayId(), this);
		//		}
	}

	/**
	 * <br>
	 * 功能简述:付费选择：getjar或内付费 <br>
	 * 功能详细描述: <br>
	 * 注意:如果不支持内付费则只显示getjar
	 * 
	 * @param activity
	 * @param infoBean
	 * @param purchaseStateListener
	 */
	public void selectPayType(final Activity activity, final ThemeInfoBean infoBean,
			final int position) {

		if (!Machine.isCnUser(activity) && PurchaseSupportedManager.checkBillingSupported(activity)) {
			requestPurchase(activity, infoBean, position);
		} else {
			handleNormalFeaturedClickEvent(activity, infoBean, position);
		}
	}

	/**
	 * <br>
	 * 功能简述:主题包是否已下载 <br>
	 * 功能详细描述:如果文件存在但未付费也算未下载 <br>
	 * 注意:
	 * 
	 * @param themeName
	 * @return
	 */
	public boolean hasDownloaded(String themeName, String packageName) {
		File file = new File(LauncherEnv.Path.GOT_ZIP_HEMES_PATH + themeName
				+ ZipResources.ZIP_POSTFIX);
		if (file.exists() && queryPurchaseState(mContext, packageName) != null) {
			return true;
		}
		return false;
	}

	/**
	 * <br>
	 * 功能简述:下载内付费的主题包 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param infoBean
	 */
	public void startDownload(ThemeInfoBean infoBean) {
		if (infoBean == null) {
			return;
		}
		String url = infoBean.getDownLoadUrl();
		if (url == null || url.equals("")) {
			return;
		}
		if (!android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			DeskToast.makeText(mContext, R.string.import_export_sdcard_unmounted,
					Toast.LENGTH_SHORT).show();
			return;
		}
		String name = infoBean.getThemeName();
		if (name == null || name.equals("")) {
			name = infoBean.getPackageName();
		}
		String downLoadUrl = infoBean.getDownLoadUrl() + "&imei="
				+ GoStorePhoneStateUtil.getVirtualIMEI(mContext);
		//		IDownloadService mController = GOLauncherApp.getApplication().getDownloadController();
		//		try {
		//			DownloadTask task = mController.getDownloadTaskById(infoBean.getFeaturedId());
		//			if (task != null && task.getDownloadUrl() != null
		//					&& task.getDownloadUrl().equals(downLoadUrl)) {
		//				int state = task.getState();
		//				if(state == DownloadTask.STATE_STOP){
		//					IDownloadService downloadService = GOLauncherApp.getApplication().getDownloadController();
		//					downloadService.addDownloadTask(task);
		//					downloadService.startDownload(task.getId());
		//					return;
		//				}else if(state == DownloadTask.STATE_DOWNLOADING){
		//					Toast.makeText(mContext, R.string.themestore_downloading, 600).show();
		//				return;
		//				}
		//			}
		//		} catch (RemoteException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}

		String path = null;
		if (infoBean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
			path = LauncherEnv.Path.GOLOCKER_ZIP_HEMES_PATH;
		} else {
			path = LauncherEnv.Path.GOT_ZIP_HEMES_PATH;
		}
		GoStoreOperatorUtil.downloadFileDirectly(GOLauncherApp.getContext(), name, path,
				infoBean.getDownLoadUrl(), infoBean.getFeaturedId(), name
						+ ZipResources.ZIP_POSTFIX, false, AppsDetail.START_TYPE_DOWNLOAD_GO);
	}

	/**
	 * <br>
	 * 功能简述:处理推荐主题点击事件 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param bean
	 */
	protected void handleNormalFeaturedClickEvent(Context context, ThemeInfoBean bean, int position) {
		HashMap<Integer, String> urlMap = bean.getUrlMap();
		int staticsType;
		if (bean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
				|| bean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			staticsType = GuiThemeStatistics.THEME_LAUNCHER_TYPE;

		} else {
			staticsType = GuiThemeStatistics.THEME_LOCKER_TYPE;

		}
		int tabId = 0;
		if (bean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			tabId = ThemeConstants.STATICS_ID_FEATURED;
		} else if (bean.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
			tabId = ThemeConstants.STATICS_ID_HOT;
		} else if (bean.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID) {
			tabId = bean.getSortId();
		} else {
			tabId = ThemeConstants.STATICS_ID_LOCKER;
		}
		if (context instanceof ThemeDetailActivity) {
			GuiThemeStatistics.getInstance(mContext).saveUserDetailGet(context,
					bean.getPackageName(), position, staticsType, "0", String.valueOf(tabId));
		} else {
			GuiThemeStatistics.getInstance(mContext).saveUserTouch(context, bean.getPackageName(),
					position, staticsType, "0", String.valueOf(tabId));
		}
		if (urlMap == null) {
			if (bean.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
				deskFeaturedClickEvent(bean.getPackageName());
			} else if (bean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
				lockerFeaturedClickEvent(bean.getPackageName());
			}
			return;
		}
		String url = urlMap.get(ThemeInfoBean.URL_KEY_GOSTORE);
		if (null != url) {
			AppsDetail.gotoDetailDirectly(mContext, AppsDetail.START_TYPE_APPRECOMMENDED,
					bean.getFeaturedId(), bean.getPackageName());
			//			Intent intent = new Intent();
			//			intent.setClass(mContext, ItemDetailActivity.class);
			//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			//			intent.putExtra(GoStorePublicDefine.ITEM_ID_KEY, String.valueOf(bean.getFeaturedId()));
			//			intent.putExtra(GoStorePublicDefine.ITEM_PKG_NAME, bean.getPackageName());
			//			mContext.startActivity(intent);
			return;
		}
		url = urlMap.get(ThemeInfoBean.URL_KEY_FTP);
		if (null != url) {
			GoStoreOperatorUtil.downloadFileDirectly(mContext, bean.getThemeName(), url,
					bean.getFeaturedId(), bean.getPackageName(), null, DownloadTask.ICON_TYPE_ID,
					bean.getFirstPreViewDrawableName());
			return;
		}
		url = urlMap.get(ThemeInfoBean.URL_KEY_GOOGLEMARKET);
		if (url != null) {
			gtotoMarketByurl(url);
			return;
		}
		url = urlMap.get(ThemeInfoBean.URL_KEY_WEB_GOOGLEMARKET);
		if (url != null) {
			GoStoreOperatorUtil.gotoBrowser(mContext, url);
			return;
		}
		url = urlMap.get(ThemeInfoBean.URL_KEY_OTHER);
		if (url != null) {
			GoStoreOperatorUtil.gotoBrowser(mContext, url);
			return;
		}
	}

	/**
	 * 跳转到电子市场
	 * 
	 * @param pkgName
	 */
	private void gotoMarket(String pkgName) {
		if (AppUtils.isMarketExist(mContext)) {
			// 跳转到market
			AppUtils.gotoMarket(mContext, LauncherEnv.Market.APP_DETAIL + pkgName
					+ LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK);
		} else {
			// 跳转到网页版market
			GoStoreOperatorUtil.gotoBrowser(mContext, LauncherEnv.Market.BROWSER_APP_DETAIL
					+ pkgName + LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK);
		}
	}

	private void gtotoMarketByurl(String url) {
		if (AppUtils.isMarketExist(mContext)) {
			// 跳转到market
			AppUtils.gotoMarket(mContext, url);
		} else {
			// 跳转到网页版market
			GoStoreOperatorUtil.gotoBrowser(mContext, url);
		}
	}

	@Override
	public void purchaseState(int purchaseState, String packageName) {
		// TODO Auto-generated method stub
		broadCast(IDiyMsgIds.THEME_INAPP_PAID_FINISHED, purchaseState, packageName, null);
	}

	public void handleInAppClick(ThemeInfoBean infoBean, Activity activity, int position) {
		if (infoBean.getPrice() != null && infoBean.getPrice().endsWith("0.0")) {
			startDownload(infoBean);
		} else if (queryPurchaseState(mContext, infoBean.getPackageName()) != null) {
			if (hasDownloaded(infoBean.getThemeName(), infoBean.getPackageName())) {
				applyTheme(activity, infoBean.getPackageName());
			} else {
				new PurchaseStateManager(mContext).save(infoBean.getPackageName(),
						LauncherEnv.Path.GOT_ZIP_HEMES_PATH);
				startDownload(infoBean);
			}
		} else {
			requestPurchase(activity, infoBean, position);
		}
	}

	public void destory() {
		clearSelf();
		clearAllObserver();
		if (mInBillingManager != null) {
			mInBillingManager.destory();
		}
	}

	public void applyTheme(Activity activity, String packageName) {
		Intent intentGoLauncher = new Intent();
		intentGoLauncher.setClass(activity, GoLauncher.class);
		activity.startActivity(intentGoLauncher);
		Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
		intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, MyThemeReceiver.CHANGE_THEME);
		intent.putExtra(MyThemeReceiver.PKGNAME_STRING, packageName);
		activity.sendBroadcast(intent);
		ThemeDetailActivity.exit();
	}

	/**
	 * 默认推荐主题跳转使用 桌面精选主题点击事件
	 * */
	protected void deskFeaturedClickEvent(String packageName) {

		if ("200".equals(GoStorePhoneStateUtil.getUid(mContext)) || !Machine.isCnUser(mContext)) { // 国外
			gotoMarket(packageName);

		} else { // 国内
			gotoGostore(packageName);
		}
	}

	/**
	 * 跳转到GO精品
	 * 
	 * @param pkgName
	 */
	private void gotoGostore(String pkgName) {
		AppsDetail.gotoDetailDirectly(mContext, AppsDetail.START_TYPE_APPRECOMMENDED, pkgName);
		//		Intent it = new Intent(mContext, ItemDetailActivity.class);
		//		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//		it.putExtra(ThemeConstants.PACKAGE_NAME_EXTRA_KEY, pkgName);
		//		try {
		//			mContext.startActivity(it);
		//		} catch (ActivityNotFoundException e) {
		//			e.printStackTrace();
		//		}
	}

	/**
	 * 默认锁屏主题 锁屏精选主题点击事件 包名第一个字母 s代表收费 n代表免费
	 * */
	protected void lockerFeaturedClickEvent(String packageName) {

		// 真实包名
		String pkgName = packageName.substring(1);
		if (packageName.startsWith("n")) { // 免费
			if ("200".equals(GoStorePhoneStateUtil.getUid(mContext)) || !Machine.isCnUser(mContext)) { // 国外
				gotoMarket(packageName);

			} else { // 国内
				gotoGostore(packageName);
			}
		} else { // 收费
			gotoMarket(packageName);
		}

	}
	public void payForVip(int level) {
		String payId = null;
		// 生成一个加密的付费包名,查询是根据这个来查
		String id = Machine.getAndroidId();
		String payName = null;
		if (level == ThemeConstants.CUSTOMER_LEVEL1) {
			payName = CryptTool.encrypt(IPreferencesIds.THEME_CUSTOMER_LEVEL_1, id);
			payId = ThemeConstants.VIP_LEVE1_PAY_ID;
			if (sDeBug) {
				payId = "android.test.purchased";
			}
		} else if (level == ThemeConstants.CUSTOMER_LEVEL2) {
			payName = CryptTool.encrypt(IPreferencesIds.THEME_CUSTOMER_LEVEL_2, id);
			payId = ThemeConstants.VIP_LEVE2_PAY_ID;
			if (sDeBug) {
				payId = "android.test.purchased";
			}
		} else if (level == ThemeConstants.CUSTOMER_LEVEL1_UPGRADE) {
			payName = CryptTool.encrypt(IPreferencesIds.THEME_CUSTOMER_LEVEL_2, id);
			payId = ThemeConstants.VIP_LEVE1_UPGRADE_PAY_ID;
			if (sDeBug) {
				payId = "android.test.purchased";
			}
		}
		mInBillingManager.requestPurchase(payName, payId, this);
	}

	public static int getCustomerLevel(Context context) {
		String id = Machine.getAndroidId();
		String level = CryptTool.encrypt(IPreferencesIds.THEME_CUSTOMER_LEVEL_2, id);
		if (queryPurchaseState(context, level) != null) {
			return ThemeConstants.CUSTOMER_LEVEL2;
		}
		level = CryptTool.encrypt(IPreferencesIds.THEME_CUSTOMER_LEVEL_1, id);
		if (queryPurchaseState(context, level) != null) {
			return ThemeConstants.CUSTOMER_LEVEL1;
		}
		return ThemeConstants.CUSTOMER_LEVEL0;
	}

	public static void clearPaidThemePkgList() {
		FileUtil.deleteFile(LauncherEnv.Path.GOTHEMES_PATH + ThemeConstants.CLICK_THEME_PKGNAME);
	}

	public void savePaidThemePkg(String pkg) {
		try {
			JSONObject obj = getPaidThemePkg();
			ArrayList<String> list = parsePaidInfo(obj);
			if (list != null && list.contains(pkg)) {
				return;
			}
			JSONArray jsonArray = null;

			if (obj != null) {
				jsonArray = obj.getJSONArray("pkgs");
			} else {
				obj = new JSONObject();
			}

			if (jsonArray == null) {
				jsonArray = new JSONArray();
				obj.put("pkgs", jsonArray);
			}

			JSONObject json = new JSONObject();
			json.put("pkg", pkg);

			jsonArray.put(json);
			mCacheManager.saveCache(ThemeConstants.CLICK_THEME_PKGNAME, obj.toString().getBytes());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private JSONObject getPaidThemePkg() {
		byte[] cacheData = mCacheManager.loadCache(ThemeConstants.CLICK_THEME_PKGNAME);
		if (cacheData == null) {
			return null;
		}
		JSONObject obj = CacheUtil.byteArrayToJson(cacheData);
		return obj;
	}
	public ArrayList<String> getPaidPkgs() {
		return parsePaidInfo(getPaidThemePkg());
	}

	private ArrayList<String> parsePaidInfo(JSONObject paidJson) {
		if (paidJson != null) {
			try {
				JSONArray jsonArray = paidJson.getJSONArray("pkgs");
				if (jsonArray != null && jsonArray.length() > 0) {
					int length = jsonArray.length();
					ArrayList<String> paidInfoList = new ArrayList<String>(length);
					for (int i = 0; i < length; i++) {
						JSONObject json = jsonArray.getJSONObject(i);
						String pkg = json.optString("pkg", "");
						if (!pkg.equals("")) {
							paidInfoList.add(pkg);
						}
					}
					return paidInfoList;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
