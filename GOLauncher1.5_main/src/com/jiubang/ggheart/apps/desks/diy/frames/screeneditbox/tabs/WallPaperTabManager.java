package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.SortUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.help.AppsManagementConstants;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.WallpaperItemInfo;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.WallpaperSubInfo;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.zip.ZipResources;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 壁纸数据读取(负责数据读取，解析，封装)
 */
public class WallPaperTabManager {
	public final static String GOWALLPAPER = "go_wallpaper";
	public final static String MULTIPLEWALLPAPER = "multiple_wallpaper";
	public final static String GALLERYWALLPAPER = "gallery_wallpaper";
	public final static String LIVEWALLPAPER = "live_wallpaper";
	public final static String OHTERWALLPAPER = "other_wallpaper";
	// 图库壁纸
	private final static String GALLERY_ACTIVITY_NAME = "com.android.launcher2.WallpaperChooser";
	private final static String GALLERY_PACKAGE = "com.android.launcher";
	// 动态壁纸
	private final static String LIVE_ACTIVITY_NAME = "com.android.wallpaper.livepicker.LiveWallpaperActivity";
	private final static String LIVE_PACKAGE = "com.android.wallpaper.livepicker";

	// 多屏多壁纸相关参数
	public final static String MULTIPLEWALLPAPER_ACTIVITY_NAME = "com.go.multiplewallpaper.MultipleWallpaperSettingActivity";
	
	private Context mContext;
	private WallpaperTab mWallpaperTab;

	public static String sIMAGERESOURCE = "image_resource";
	public static String sTEXTNAME = "text_name";
	private ArrayList<WallpaperSubInfo> mThumbs; // 缩略图
	private ArrayList<WallpaperSubInfo> mImages; // 墙纸应用大图

	public WallPaperTabManager(Context context, WallpaperTab mWallpaperTab) {
		super();
		this.mContext = context;
		this.mWallpaperTab = mWallpaperTab;
	}

	/**
	 * 查询所有壁纸
	 */
	public List<WallpaperItemInfo> findAll() {
		List<WallpaperItemInfo> list = new ArrayList<WallpaperItemInfo>();
		List<WallpaperItemInfo> appInfoList = new ArrayList<WallpaperItemInfo>();
		PackageManager pm = mContext.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_SET_WALLPAPER, null);
		// 通过查询，获得所有ResolveInfo对象.
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
				PackageManager.MATCH_DEFAULT_ONLY);

		// 调用系统排序 ， 根据name排序
		// 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
		if (list != null) {
			list.clear();
			for (ResolveInfo reInfo : resolveInfos) {
				list.add(getWallpaperItemInfo(reInfo, pm));
			}
			// 进行排序
			String sortMethod = "getTitle";
			String order = "ASC";
			SortUtils.sort(list, sortMethod, null, null, order);
			// 把GO桌面放第一位（如果设置了动态壁纸则顺延至第二位）
			for (WallpaperItemInfo appInfo : list) {
				if (LauncherEnv.PACKAGE_NAME.equals(appInfo.getPkgName())) {
					appInfoList.add(0, appInfo);
				} else {
					appInfoList.add(appInfo);
				}
			}
			// 添加动态壁纸的设置项
			ResolveInfo settingInfo = null;
			WallpaperManager wm = (WallpaperManager) mContext
					.getSystemService(Context.WALLPAPER_SERVICE);
			android.app.WallpaperInfo wi = wm.getWallpaperInfo();
			if (wi != null && wi.getSettingsActivity() != null 
					&& !wi.getPackageName().equals(LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
				LabeledIntent li = new LabeledIntent(mContext.getPackageName(),
						R.string.configure_wallpaper, 0);
				li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
				settingInfo = pm.resolveActivity(li, 0);
				// 添加动态壁纸设置放在第1个位置,索引为0
				if (settingInfo != null) {
					WallpaperItemInfo appInfo = getWallpaperItemInfo(settingInfo, pm);
					if (appInfo != null) {
						// 桌面添加了动态壁纸属性，此处需要屏蔽桌面的动态壁纸属性
						if (!LauncherEnv.PACKAGE_NAME.equals(appInfo.getPkgName())) {
							appInfoList.add(0, getWallpaperItemInfo(settingInfo, pm));
						} 
					}
					
				} else {
					String activityName = wi.getSettingsActivity(); // 获得该应用程序的启动Activity的name
					String pkgName = wi.getPackageName(); // 获得应用程序的包名
					String appLabel = mContext.getResources().getString(
							R.string.configure_wallpaper);
					Drawable icon = mContext.getResources().getDrawable(
							R.drawable.live_wallpaper_logo);
					// 为应用程序的启动Activity 准备Intent
					Intent launchIntent = new Intent();
					launchIntent.setComponent(new ComponentName(pkgName, activityName));
					launchIntent.setAction(Intent.ACTION_SET_WALLPAPER);

					WallpaperItemInfo appInfo = new WallpaperItemInfo();
					appInfo.setAppLabel(appLabel);
					appInfo.setPkgName(pkgName);
					// 确定动态壁纸设置项的icon是已经匹配的，所以不进行fitIcon处理
					appInfo.setAppIcon(icon);
					appInfo.setIntent(launchIntent);
					appInfoList.add(0, appInfo);
				}
			}

			// 加入裁剪模式 ，排在第一
			addCutModeItem(appInfoList);

			// 加入多屏多壁纸 ，排在裁剪模式后面，即排在第二
			addMultipleWallpaperItem(appInfoList);

			addDownLoadItem(appInfoList);
		}
		return appInfoList;

	}

	/**
	 * 功能简述:增加“获取更多”图标，点击后跳转至GO精品的壁纸分类。 功能详细描述: 注意:排在最后
	 * 
	 * @param appInfoList
	 */
	private void addDownLoadItem(List<WallpaperItemInfo> appInfoList) {
		String appLabel = mContext.getString(R.string.themestore_mainlistview_btmbutton);
		WallpaperItemInfo appInfo = new WallpaperItemInfo();
		Intent launchIntent = new Intent();
		launchIntent.setClass(mContext, AppsManagementActivity.class);
		Bundle bundle = new Bundle();
//		if (GoStorePhoneStateUtil.is200ChannelUid(mContext)) {
//			bundle.putString("sort", String.valueOf(SortsBean.SORT_LIVEWALLPAPER)); // 动态壁纸分类
//		} else {
//			bundle.putString("sort", String.valueOf(SortsBean.SORT_WALLPAPER)); // 壁纸分类
//		}
		bundle.putInt(AppsManagementConstants.APPS_MANAGEMENT_ENTRANCE_KEY,
				MainViewGroup.ACCESS_FOR_APPCENTER_WALLPAPER);
		bundle.putBoolean(AppsManagementConstants.APPS_MANAGEMENT_SHOW_FRONTCOVER, false);
		StatisticsData.countStatData(mContext, StatisticsData.ENTRY_KEY_WALLPAPER);
//		GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_MOREWALLPAPER,
//				mContext);
		AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext, AppRecommendedStatisticsUtil.ENTRY_TYPE_WALLPAPER);
		launchIntent.putExtras(bundle);
		appInfo.setAppLabel(appLabel);
		appInfo.setAppIcon(mContext.getResources().getDrawable(R.drawable.gostore_4_def3));
		// appInfo.setPkgName(mContext.getPackageName()); // 跟Go桌面壁纸tab包名冲突，暂关闭
		appInfo.setIntent(launchIntent);
		appInfoList.add(appInfo);
	}

	/**
	 * 添加多屏多壁纸
	 * 
	 * @param appInfoList
	 * 
	 * @author chenbingdong
	 */
	private void addMultipleWallpaperItem(List<WallpaperItemInfo> appInfoList) {
		WallpaperItemInfo appInfo = new WallpaperItemInfo();
		String appLabel = mContext.getString(R.string.go_multiple_wallpaper_title);
		appInfo.setAppLabel(appLabel);
		appInfo.setPkgName(LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME);
		Intent launchIntent = null;
		launchIntent = new Intent();
		launchIntent.setComponent(new ComponentName(
				LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME,
				MULTIPLEWALLPAPER_ACTIVITY_NAME));
		Drawable drawable = mContext.getResources().getDrawable(
				R.drawable.multiple_wallpaper_icon);
		// 判断多屏多壁纸应用是否存在
		if (!AppUtils.isAppExist(mContext,
				LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
			// 应用不存在，图标变灰处理
			drawable = BitmapUtility.getNeutralDrawable(drawable);
		} else {
			// 应用存在，图标恢复彩色处理
			drawable = BitmapUtility.getOriginalDrawable(drawable);
		}
		appInfo.setAppIcon(drawable);

		appInfo.setIntent(launchIntent);

		// 排在裁剪模式后面，即排在第二
		appInfoList.add(1, appInfo);

	}

	// 裁剪壁纸的方法
	private void addCutModeItem(List<WallpaperItemInfo> appInfoList) {
		WallpaperItemInfo tempInfo = new WallpaperItemInfo();
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		ScreenSettingInfo mScreenInfo = controler.getScreenSettingInfo();
		BitmapDrawable drawable = null;
		if (mScreenInfo.mWallpaperScroll) {
			drawable = (BitmapDrawable) mContext.getResources().getDrawable(
					R.drawable.wallpaper_default);
		} else {
			drawable = (BitmapDrawable) mContext.getResources().getDrawable(
					R.drawable.wallpaper_vertical);
		}
		tempInfo.setAppIcon(drawable);
		tempInfo.setAppLabel(mContext.getResources().getString(
				R.string.tab_add_wallpaper_displaymode));
		appInfoList.add(0, tempInfo);
	}

	/**
	 * 将ResolveInfo转化为WallpaperItemInfo
	 * 
	 * @param reInfo
	 * @param pm
	 * @return
	 */
	private WallpaperItemInfo getWallpaperItemInfo(ResolveInfo reInfo, PackageManager pm) {
		String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
		String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
		String appLabel = (String) reInfo.loadLabel(pm); // 获得应用程序的Label
		Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
		// 为应用程序的启动Activity 准备Intent
		Intent launchIntent = new Intent();
		launchIntent.setComponent(new ComponentName(pkgName, activityName));
		launchIntent.setAction(Intent.ACTION_SET_WALLPAPER);

		WallpaperItemInfo appInfo = new WallpaperItemInfo();
		appInfo.setAppLabel(appLabel);
		appInfo.setPkgName(pkgName);
		appInfo.setAppIcon(fitIcon(icon, pkgName, activityName));
		appInfo.setIntent(launchIntent);
		return appInfo;
	}

	/**
	 * 对一些特殊的壁纸做图标处理
	 * 
	 * @param srcIcon
	 * @param packageName
	 * @return
	 */
	private Drawable fitIcon(Drawable srcIcon, String packageName, String activityName) {

		if (LauncherEnv.PACKAGE_NAME.equals(packageName)) {
			Drawable icon = mContext.getResources().getDrawable(R.drawable.gowallpaper_logo);
			return icon;
		} else if (packageName.equals(GALLERY_PACKAGE)
				&& activityName.equals(GALLERY_ACTIVITY_NAME)) {
			return mContext.getResources().getDrawable(R.drawable.gallery_4_def3);
		} else if (packageName.equals(LIVE_PACKAGE) && activityName.equals(LIVE_ACTIVITY_NAME)) {
			return mContext.getResources().getDrawable(R.drawable.live_wallpaper_logo);
		}

		else {
			Drawable fitIcon = mWallpaperTab.getFitIcon(srcIcon, true);
			if (fitIcon != null) {
				return fitIcon;
			}
		}

		return srcIcon;
	}

	/**
	 * 加载功能表背景图片
	 */
	public Map loadDrawables(String drawablesResName) {
		Map<String, List<WallpaperSubInfo>> map = new HashMap<String, List<WallpaperSubInfo>>();
		mThumbs = new ArrayList<WallpaperSubInfo>();
		mImages = new ArrayList<WallpaperSubInfo>();
		if (drawablesResName == null) {
			return null;
		}
		// 将本程序对应的默认主题也添加进去
		if (mContext != null) {
			addDrawables(mContext.getResources(), mContext.getPackageName(), drawablesResName, null);

			// 查找第三方主题包，并将主题包中的墙纸提取出来
			Intent intent = new Intent(ICustomAction.ACTION_MAIN_THEME_PACKAGE);
			intent.addCategory(ThemeManager.THEME_CATEGORY);
			PackageManager pm = mContext.getPackageManager();
			List<ResolveInfo> themes = pm.queryIntentActivities(intent, 0);
			int size = themes.size();
			String themePackage = null;
			Resources resources = null;
			CharSequence lable; // 主题程序名称
			for (int i = 0; i < size; i++) {
				themePackage = themes.get(i).activityInfo.packageName.toString();
				lable = themes.get(i).activityInfo.loadLabel(pm);
				try {
					resources = pm.getResourcesForApplication(themePackage);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				addDrawables(resources, themePackage, drawablesResName, lable);
			}

			//update by zhoujun 解析zip主题
			if (Machine.isSDCardExist() && mContext != null) {
				//有sdcard
				ConcurrentHashMap<String, ThemeInfoBean> zipHashMap = ThemeManager.getInstance(
						mContext).scanAllZipThemes();
				if (zipHashMap != null) {
					Iterator<String> pakcageIterator = zipHashMap.keySet().iterator();
					if (pakcageIterator != null) {
						String packageName = null;
						ThemeInfoBean themeInfoBean = null;
						while (pakcageIterator.hasNext()) {
							packageName = pakcageIterator.next();
							themeInfoBean = zipHashMap.get(packageName);
							themeInfoBean = ThemeManager.getInstance(mContext).getThemeInfo(
									themeInfoBean.getPackageName(), themeInfoBean);
							resources = ZipResources.getThemeResourcesFromReflect(mContext,
									packageName);
							//非空保护,themeInfoBean异常为空时,则不加载该张壁纸
							if (themeInfoBean != null) {
								addDrawables(resources, packageName,
										drawablesResName,
										themeInfoBean.getThemeName());
							}
						}
					}
				}
			}
			// update by zhoujun 2012-08-23 end 
		}
		map.put("mThumbs", mThumbs);
		map.put("mImages", mImages);
		return map;
	}

	private void addDrawables(Resources resources, String packageName, String resName,
			CharSequence lable) {
		if (resources == null || packageName == null) {
			return;
		}
		// add by jiangchao 20120703 特殊处理：安装UI3.01包后，GO桌面壁纸存在两个UI3.0主题壁纸
		if (packageName.equals("com.gau.go.launcherex.theme.defaultthemethree")) {
			return;
		}
		try {
			int drawableList = resources.getIdentifier(resName, "array", packageName);
			if (drawableList <= 0) {
				return;
			}
			final String[] extras = resources.getStringArray(drawableList);
			for (String extra : extras) {
				int res = resources.getIdentifier(extra, "drawable", packageName);
				if (res != 0) {
					final int thumbRes = resources.getIdentifier(extra + "_thumb", "drawable",
							packageName);
					if (thumbRes != 0) {
						WallpaperSubInfo thumbItem = new WallpaperSubInfo();
						// thumbItem.mImageResName = extra;
						thumbItem.setImageResId(thumbRes);
						thumbItem.setPackageName(packageName);
						thumbItem.setResource(resources);
						thumbItem.setType(1);
						// 设置图片名称
						if (lable == null) {
							thumbItem.setImageResName(mContext.getResources().getString(
									R.string.theme_title)); // 默认主题
						} else {
							thumbItem.setImageResName(lable.toString());
						}
						mThumbs.add(thumbItem);

						WallpaperSubInfo imageItem = new WallpaperSubInfo();
						// imageItem.mImageResName = extra;
						imageItem.setImageResId(res);
						imageItem.setPackageName(packageName);
						imageItem.setResource(resources);
						imageItem.setType(1);
						mImages.add(imageItem);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clear() {
		if (mThumbs != null) {
			mThumbs.clear();
			mThumbs = null;
		}
		if (mImages != null) {
			mImages.clear();
			mImages = null;
		}
	}

}
