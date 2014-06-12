package com.jiubang.ggheart.data.theme;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * Go锁屏主题的解析
 * 
 * @author dingzijian
 * 
 */
public class GoLockerThemeManager {

	//	private static String ACTION_SEND_TO_GOLOCK = "com.gau.go.launcherex_action_send_to_golock";

	private final static String NEW_THEME_KEY = "newtheme";

	private final static String THEME_PREVIEW_FOLDER = "preview";

	private Context mContext = null;

	public static final int THEME_ICON_WIDTH = 100; // 主题小图标宽度
	public static final int THEME_ICON_HEIGHT = 100; // 主题小图标高度

	public static final String ZIP_THEME_PATH = LauncherEnv.Path.GOLOCKER_ZIP_HEMES_PATH;
	public static final String ZIP_POSTFIX = "go";
	private ZipResources mZipResources;
	public GoLockerThemeManager(Context mContext) {
		this.mContext = mContext;
		mZipResources = new ZipResources();
	}

	/**
	 * 查询已安装的GO锁屏主题
	 * 
	 * @return 以主题包名为key,主题名称为value的map
	 * 
	 * */
	public Map<CharSequence, CharSequence> queryInstalledTheme() {
		Map<CharSequence, CharSequence> themePackage = new HashMap<CharSequence, CharSequence>();
		//		if (!AppUtils.isAppExist(mContext, LauncherEnv.GO_LOCK_PACKAGE_NAME)) {
		//			return themePackage;
		//		}
		PackageManager mPackageManager = mContext.getPackageManager();
		Intent searchIntent = new Intent(ICustomAction.ACTION_GOLOCK_THEME);
		searchIntent.addCategory(Intent.CATEGORY_INFO);
		List<ResolveInfo> themes = mPackageManager.queryIntentActivities(searchIntent, 0);
		if (!themes.isEmpty()) {
			for (ResolveInfo info : themes) {
				CharSequence themeName;
				themeName = getThemeName(mContext, info.activityInfo.packageName, "theme_name");
				if (themeName == null) { // 如果从锁屏包中没拿到名字的保护处理
					themeName = info.activityInfo.loadLabel(mPackageManager);
					if (themeName.length() > 4) {
						themeName = themeName.subSequence(4, themeName.length() - 2);
					} else {
						themeName = themeName.subSequence(0, themeName.length());
					}
				}
				// 修改map结构以包名为键替代以主题名称为键，是因为有可能存在名称相同的主题 modify by yangbing
				// 2012-07-02
				// themePackage.put(themeName, info.activityInfo.packageName);
				themePackage.put(info.activityInfo.packageName, themeName);
			}
		}
		Map<CharSequence, CharSequence> zipThemes = scanZipThemes();
		if (zipThemes != null && !zipThemes.isEmpty()) {
			themePackage.putAll(zipThemes);
		}
		// 对查询结果进行排序
		List<Map.Entry<CharSequence, CharSequence>> mappingList = null;
		mappingList = new ArrayList<Map.Entry<CharSequence, CharSequence>>(themePackage.entrySet());
		Collections.sort(mappingList, new Comparator<Map.Entry<CharSequence, CharSequence>>() {
			@Override
			public int compare(Map.Entry<CharSequence, CharSequence> mapping1,
					Map.Entry<CharSequence, CharSequence> mapping2) {
				Comparator<Object> cmp = Collator.getInstance(java.util.Locale.CHINA);
				return cmp.compare(mapping1.getKey(), mapping2.getKey());
			}
		});
		return themePackage;
	}

	public Map<CharSequence, CharSequence> scanZipThemes() {
		Map<CharSequence, CharSequence> maps = null;
		File dir = new File(ZIP_THEME_PATH);
		if (dir.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				private Pattern mPattern = Pattern.compile(ZIP_POSTFIX);

				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					String nameString = new File(filename).getName();
					String postfix = nameString.substring(nameString.lastIndexOf(".") + 1);
					return mPattern.matcher(postfix).matches();
				}
			};
			String[] zipThemeNames = dir.list(filter);
			if (zipThemeNames != null && zipThemeNames.length > 0) {
				maps = new HashMap<CharSequence, CharSequence>();
				for (int i = 0; i < zipThemeNames.length; i++) {
					String fileName = zipThemeNames[i];
					String packageName = mZipResources.getThemePkgFromReflect(ZIP_THEME_PATH
							+ fileName);
					if (packageName == null) {
						continue;
					}
					Resources res = mZipResources.getThemeResourcesFromReflect(mContext,
							packageName);
					if (res == null) {
						continue;
					}
					int id = res.getIdentifier("theme_name", "string", packageName);

					String themeName = null;
					if (id != 0) {
						themeName = res.getString(id);
						if (themeName == null) { // 如果从锁屏包中没拿到名字的保护处理
							themeName = packageName;
						}
						maps.put(packageName, themeName);
					}
				}
			}
		}
		return maps;
	}

	/**
	 * 获取指定GO锁屏的主题名
	 */
	public String getThemeName(Context context, String pkgname, String resName) {
		if (pkgname == null) {
			return null;
		}
		try {
			Resources resources = context.getPackageManager().getResourcesForApplication(pkgname);
			if (resources == null) {
				return null;
			}
			int resource_id = resources.getIdentifier(resName, "string", pkgname);
			if (resource_id != 0) {
				return resources.getString(resource_id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// 切换Go锁屏的主题
	public boolean changeLockTheme(CharSequence newThemePkgName) {
		try {
			if (newThemePkgName != null) {
				Intent intent_GL = new Intent(ICustomAction.ACTION_SEND_TO_GOLOCK_FOR_GOLOCKER);
				intent_GL.putExtra(NEW_THEME_KEY, newThemePkgName);
				mContext.sendBroadcast(intent_GL);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.GO_LOCKER_PRECHANGE, -1, null, null);
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.GO_LOCKER_CHANGED, -1, null, null);
					}
				}, 800);
			}
		} catch (Exception e) {
		}
		return false;

	}

	private Bitmap getPreViewImage(Resources res, String aName, String pkgName) {
		Bitmap bmp = null;
		InputStream is = null;
		if (res == null) {
			return bmp;
		}
		try {
			is = res.getAssets().open(THEME_PREVIEW_FOLDER + "/" + aName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null == is) {
			try {
				is = res.getAssets().open(aName);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (is == null) {
			try {
				int id = mContext.getResources().getIdentifier(aName, "raw", pkgName);
				is = res.openRawResource(id);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		bmp = BitmapFactory.decodeStream(is, null, null);
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				Log.i("ThemeManager", "IOException for close inputSteam");
			}
		}
		return bmp;
	}

	public BitmapDrawable getPreView(String packageName) {
		Bitmap bmp = null;
		try {
			Resources res = null;
			if (AppUtils.isAppExist(mContext, packageName)) {
				Context ct = mContext.createPackageContext(packageName,
						Context.CONTEXT_IGNORE_SECURITY);
				res = ct.getResources();
			} else if (isZipTheme(mContext, packageName)) {
				res = getZipRes(packageName);
			}
			bmp = getPreViewImage(res, "thumb.jpg", packageName);
			// if(bmp == null)
			// { // 删除了之前的文件夹，所以直接用这个图片路径（图片名称）就可以了
			// String name = "" + aSkinDataItem.getIconPath();
			// bmp = GetPreViewImage(ct,name);
			// }
		} catch (Exception e) {
		}
		if (bmp != null) {
			return new BitmapDrawable(bmp);
		}
		return null;
	}

	private Resources getZipRes(String packageName) {
		return mZipResources.getThemeResourcesFromReflect(mContext, packageName);
	}

	// 从添加界面获取锁屏预览图
	public BitmapDrawable getPreViewForScreenEdit(String packageName) {
		Bitmap bmp = null;
		try {
			Resources res = null;
			if (AppUtils.isAppExist(mContext, packageName)) {
				Context ct = mContext.createPackageContext(packageName,
						Context.CONTEXT_IGNORE_SECURITY);
				res = ct.getResources();

			} else if (mZipResources.isZipThemeExist(packageName)) {
				res = mZipResources.getThemeResourcesFromReflect(mContext, packageName);
			}

			bmp = getPreViewImage(res, "thumb.jpg", packageName);
			if (bmp == null) {
				Drawable d = mContext.getResources()
						.getDrawable(R.drawable.ic_launcher_application);
				return (BitmapDrawable) d;
			}

		} catch (Exception e) {
		}
		return new BitmapDrawable(bmp);
	}

	/**
	 * 根据默认主题包，获取随机主题预览图，如果获取为null，则代表当前go锁屏没有随机锁屏功能
	 * 
	 * @param packageName
	 * @return
	 */
	public BitmapDrawable getRandomPreView(String packageName) {
		Bitmap bmp = null;
		try {
			Resources res = null;
			if (AppUtils.isAppExist(mContext, packageName)) {
				Context ct = mContext.createPackageContext(packageName,
						Context.CONTEXT_IGNORE_SECURITY);
				res = ct.getResources();

			} else if (mZipResources.isZipThemeExist(packageName)) {
				res = mZipResources.getThemeResourcesFromReflect(mContext, packageName);
			}
			bmp = getPreViewImage(res, "random_thumb.jpg", packageName);
			if (bmp == null) {
				return null;
			}
		} catch (Exception e) {
		}
		return new BitmapDrawable(bmp);
	}

	public BitmapDrawable cutThemeIcon(String packageName, Resources res) {
		Bitmap bitmap2 = BitmapFactory.decodeResource(res, R.drawable.go_store);

		int corpWith = bitmap2.getWidth();
		int corpHeight = bitmap2.getHeight();

		BitmapDrawable ret = getPreView(packageName);

		// 缩放
		int width = ret.getIntrinsicWidth();
		int height = ret.getIntrinsicHeight();
		if (width == corpWith && height == corpHeight) {
			return ret;
		}
		float wScale = (float) corpWith / (float) width;
		float hScale = (float) corpHeight / (float) height;
		float scale = wScale > hScale ? wScale : hScale;
		try {
			BitmapDrawable drawable = BitmapUtility.zoomDrawable(ret, scale, scale, res);
			ret = drawable;
		} catch (Exception e) {
			return ret;
		}

		// 截取
		width = ret.getIntrinsicWidth();
		height = ret.getIntrinsicHeight();
		if (width > corpWith || height > corpHeight) {
			try {
				BitmapDrawable drawable = BitmapUtility
						.clipDrawable(ret, corpWith, corpHeight, res);
				ret = drawable;
			} catch (Exception e) {
				return ret;
			}
		}
		return ret;
	}

	public boolean isZipTheme(Context context, String packageName) {
		boolean bRet = false;
		if (!AppUtils.isAppExist(context, packageName)) {
			if (mZipResources.isZipThemeExist(packageName)) {
				bRet = true;
			}
		}
		return bRet;
	}

	public String getZipThemeFileName(String pkgName) {
		return mZipResources.getZipThemeName(pkgName);
	}

	/**
	 * 
	 * <br>类描述:zip反射工具类
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-11-1]
	 */
	public class ZipResources {

		private HashMap<String, ZipThemeInfo> mZipThemeCache;
		public ZipResources() {
			mZipThemeCache = new HashMap<String, GoLockerThemeManager.ZipThemeInfo>();
		}
		/**
		 * 查看sd卡主题是否存在
		 * 
		 * @param packageName
		 * @return
		 */
		public boolean isZipThemeExist(String packageName) {
			boolean ret = false;
			if (getZipThemeName(packageName) != null) {
				ret = true;
			}
			return ret;
		}

		/**
		 * <br>功能简述:返回package对应的go主题包文件名
		 * <br>功能详细描述:
		 * <br>注意:
		 * @param packageName
		 * @return
		 */
		public String getZipThemeName(String packageName) {
			ZipThemeInfo info = null;
			if (mZipThemeCache.containsKey(packageName)) {
				info = mZipThemeCache.get(packageName);
				if (info.mFileName != null) {
					return info.mFileName;
				}
			}
			String[] fileList = getSdThemeFileList();
			if (fileList == null) {
				return null;
			}
			for (int i = 0; i < fileList.length; i++) {
				String apkPath = ZIP_THEME_PATH + fileList[i];
				String pkg = getThemePkgFromReflect(apkPath);
				if (pkg != null && pkg.equals(packageName)) {
					if (info == null) {
						info = new ZipThemeInfo();
						mZipThemeCache.put(packageName, info);
					}
					info.mFileName = fileList[i];
					info.mPackagename = packageName;
					mZipThemeCache.put(packageName, info);
					return fileList[i];
				}
			}
			return null;
		}

		/**
			 * 通过反射获取内置主题的Resources
			 * @param context
			 * @param apkPath
			 * @return
			 * @author rongjinsong
			 */
		public Resources getThemeResourcesFromReflect(Context context, String packageName) {
			if (null == packageName || packageName.length() < 0) {
				return context.getResources();
			}
			ZipThemeInfo info = null;
			if (mZipThemeCache.containsKey(packageName)) {
				info = mZipThemeCache.get(packageName);
				if (info.mResources != null) {
					return info.mResources;
				}
			} else {
				info = new ZipThemeInfo();
				mZipThemeCache.put(packageName, info);
			}
			String[] zipThemeNames = getSdThemeFileList();
			if (zipThemeNames != null) {
				for (int i = 0; i < zipThemeNames.length; i++) {
					String apkPath = ZIP_THEME_PATH + zipThemeNames[i];
					String pkg = getThemePkgFromReflect(apkPath);
					if (pkg != null && pkg.equals(packageName)) {
						String path_assetmanager = "android.content.res.AssetManager";
						try {
							// apk包的文件路径
							// 这是一个Package 申明器, 是隐蔽的
							// 构造函数的参数只有一个, apk文件的路径
							Class[] typeArgs = new Class[1];
							typeArgs[0] = String.class;
							Object[] valueArgs = new Object[1];

							Class assetMagCls = Class.forName(path_assetmanager);
							Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
							Object assetMag = assetMagCt.newInstance((Object[]) null);
							typeArgs = new Class[1];
							typeArgs[0] = String.class;
							Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
									"addAssetPath", typeArgs);
							valueArgs = new Object[1];
							valueArgs[0] = apkPath;
							assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
							Resources res = context.getResources();
							typeArgs = new Class[3];
							typeArgs[0] = assetMag.getClass();
							typeArgs[1] = res.getDisplayMetrics().getClass();
							typeArgs[2] = res.getConfiguration().getClass();
							Constructor resCt = Resources.class.getConstructor(typeArgs);
							valueArgs = new Object[3];
							valueArgs[0] = assetMag;
							valueArgs[1] = res.getDisplayMetrics();
							valueArgs[2] = res.getConfiguration();
							info.mResources = (Resources) resCt.newInstance(valueArgs);
							return info.mResources;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			return context.getResources();
		}

		/**
		 * 采用反射获取内置主题的包名
		 * @param fileName 
		 * @return
		 * @author rongjinsong
		 */
		public String getThemePkgFromReflect(String apkPath) {
			if (null == apkPath || apkPath.length() < 0) {
				return null;
			}

			Iterator iterator = mZipThemeCache.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Entry) iterator.next();
				String key = (String) entry.getKey();
				ZipThemeInfo info = (ZipThemeInfo) entry.getValue();
				if (info.mFileName != null && apkPath.endsWith(info.mFileName)) {
					return key;
				}
			}

			String path_packageparser = "android.content.pm.PackageParser";
			try {
				// apk包的文件路径
				// 这是一个Package 申明器, 是隐蔽的
				// 构造函数的参数只有一个, apk文件的路径
				Class[] typeArgs = new Class[1];
				typeArgs[0] = String.class;
				Object[] valueArgs = new Object[1];
				Class pkgParserCls = Class.forName(path_packageparser);
				Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
				valueArgs[0] = apkPath;
				Object pkgParser = pkgParserCt.newInstance(valueArgs);
				// 这个是与显示有关的, 里面涉及到一些像素显示等等
				DisplayMetrics metrics = new DisplayMetrics();
				metrics.setToDefaults();

				typeArgs = new Class[4];
				typeArgs[0] = File.class;
				typeArgs[1] = String.class;
				typeArgs[2] = DisplayMetrics.class;
				typeArgs[3] = Integer.TYPE;
				Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",
						typeArgs);
				valueArgs = new Object[4];
				valueArgs[0] = new File(apkPath);
				valueArgs[1] = apkPath;
				valueArgs[2] = metrics;
				valueArgs[3] = 0;
				Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
				// 应用法度信息包, 这个公开的, 不过有些函数, 变量没公开
				Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
				ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
				// uid 输出为"-1",原因是未安装,体系未分派其Uid。
				ZipThemeInfo zipinfo = new ZipThemeInfo();
				zipinfo.mFileName = apkPath.substring(apkPath.lastIndexOf("/") + 1);
				zipinfo.mPackagename = info.packageName;
				mZipThemeCache.put(info.packageName, zipinfo);
				return info.packageName;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return LauncherEnv.PACKAGE_NAME;

		}
		/**
		 * <br>功能简述:扫描所有sd卡内go主题
		 * <br>功能详细描述:
		 * <br>注意:
		 * @return
		 */
		public String[] getSdThemeFileList() {
			String[] zipThemeNames = null;
			File dir = new File(ZIP_THEME_PATH);
			if (dir.exists()) {
				FilenameFilter filter = new FilenameFilter() {
					private Pattern mPattern = Pattern.compile(ZIP_POSTFIX);

					@Override
					public boolean accept(File dir, String filename) {
						// TODO Auto-generated method stub
						String nameString = new File(filename).getName();
						String postfix = nameString.substring(nameString.lastIndexOf(".") + 1);
						return mPattern.matcher(postfix).matches();
					}
				};
				zipThemeNames = dir.list(filter);
			}
			return zipThemeNames;
		}

		/**
		 * <br>功能简述:删除主题
		 * <br>功能详细描述:
		 * <br>注意:
		 * @param packageName
		 */
		public void deleteTheme(String packageName) {
			String fileName = getZipThemeName(packageName);
			File file = new File(ZIP_THEME_PATH + fileName);
			if (file.exists()) {
				file.delete();
			}
		}
	}
	/**
	 * 
	 * <br>类描述:zip主题信息
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2013-1-16]
	 */
	public static class ZipThemeInfo {
		public String mFileName;
		public Resources mResources;
		public String mPackagename;
	}

}
