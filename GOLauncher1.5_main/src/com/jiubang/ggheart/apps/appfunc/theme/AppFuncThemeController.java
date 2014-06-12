package com.jiubang.ggheart.apps.appfunc.theme;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeConfig;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.AppFuncThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.data.theme.parser.FuncThemeParser;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-13]
 */
public class AppFuncThemeController {
	private static final Pattern PATTERN = Pattern.compile("\\d+");
	private AppFuncThemeBean mAppFuncThemeBean;
	private Context mContext;
	private ThemeManager mThemeManager;
	private ImageExplorer mImageExplorer;
	private volatile boolean mIsChangeTheme;

	/**
	 * 是否使用主题
	 */
	private boolean mIsHasTheme;
	
	private static AppFuncThemeController sInstance;
	
	public static AppFuncThemeController getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new AppFuncThemeController(context);
		}
		return sInstance;
	}

	private AppFuncThemeController(Context context) {
		this.mContext = context;
		mThemeManager = GOLauncherApp.getThemeManager();
		mImageExplorer = AppCore.getInstance().getImageExplorer();
		initThemeData();
		mIsChangeTheme = true;
	}

	/**
	 * 是否为默认主题
	 * 
	 * @return
	 */
	public boolean isDefaultTheme() {
		if (mThemeManager != null && mThemeManager.getCurThemeInfoBean() != null) {
			String curThemeName = mThemeManager.getCurThemeInfoBean().getPackageName();
			if (ThemeManager.isAsDefaultThemeToDo(curThemeName)) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * 主题内容初始化或者重置时，重新获取最新主题的数据
	 */
	private void initThemeData() {
		if (isUsedTheme()) {
			mIsHasTheme = true;
			mAppFuncThemeBean = (AppFuncThemeBean) mThemeManager
					.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
			// 保护代码
			if (mAppFuncThemeBean == null) {
				mAppFuncThemeBean = new AppFuncThemeBean();
			}

		} else {
			mIsHasTheme = false;
			mAppFuncThemeBean = new AppFuncThemeBean();
		}
		praserTabHomeTheme(true);
		praserIndicatorTheme();
		praserFolderTheme();

	}

	private void praserIndicatorTheme() {

		String themePackage = GOLauncherApp.getSettingControler().getFunAppSetting()
				.getIndicatorSetting();
		if (themePackage.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
			mAppFuncThemeBean.mIndicatorBean = mAppFuncThemeBean.new IndicatorBean();
		} else {
			if (!AppUtils.isAppExist(mContext, themePackage)) {
				themePackage = GOLauncherApp.getThemeManager().getCurThemePackage();
			}
			InputStream inputStream = null;
			XmlPullParser xmlPullParser = null;
			AppFuncThemeBean themeBean = null;
			FuncThemeParser parser = null;
			ThemeManager thememanager = ThemeManager.getInstance(GoLauncher.getContext()
					.getApplication());
			inputStream = thememanager.createParserInputStream(themePackage,
					ThemeConfig.APPFUNCTHEMEFILENAME);
			if (inputStream != null) {
				xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
			} else {
				xmlPullParser = XmlParserFactory.createXmlParser(mContext,
						ThemeConfig.APPFUNCTHEMEFILENAME, themePackage);
			}

			if (xmlPullParser != null) {
				themeBean = new AppFuncThemeBean(themePackage);
				parser = new FuncThemeParser();
				parser.parseIndicatorXml(xmlPullParser, themeBean);
				mAppFuncThemeBean.mIndicatorBean = themeBean.mIndicatorBean;
			}
			// 关闭inputStream
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("ThemeManager", "IOException for close inputSteam");
				}
			}
		}

	}

	private void praserTabHomeTheme(boolean bFromeInit) {

		String themePackage = GOLauncherApp.getSettingControler().getFunAppSetting()
				.getTabHomeBgSetting();
		if (ThemeManager.isAsDefaultThemeToDo(themePackage)) {
			mAppFuncThemeBean.initTabHomeBean();
		} else {
			if (!ThemeManager.isInstalledTheme(mContext, themePackage)) {
				themePackage = GOLauncherApp.getThemeManager().getCurThemePackage();
			}
			InputStream inputStream = null;
			XmlPullParser xmlPullParser = null;
			AppFuncThemeBean themeBean = null;
			FuncThemeParser parser = null;
			ThemeManager thememanager = ThemeManager.getInstance(GoLauncher.getContext()
					.getApplication());
			inputStream = thememanager.createParserInputStream(themePackage,
					ThemeConfig.APPFUNCTHEMEFILENAME);
			if (inputStream != null) {
				xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
			} else {
				xmlPullParser = XmlParserFactory.createXmlParser(mContext,
						ThemeConfig.APPFUNCTHEMEFILENAME, themePackage);
			}
			if (xmlPullParser != null) {
				themeBean = new AppFuncThemeBean(themePackage);
				parser = new FuncThemeParser();
				parser.parseTabHomeXml(xmlPullParser, themeBean);
				mAppFuncThemeBean.mAllTabsBean = themeBean.mAllTabsBean;
				mAppFuncThemeBean.mHomeBean = themeBean.mHomeBean;
				mAppFuncThemeBean.mHomeBean.mTabHomeBgPackage = themePackage;
				mAppFuncThemeBean.mMoveToDeskBean = themeBean.mMoveToDeskBean;
				mAppFuncThemeBean.mTabBean = themeBean.mTabBean;
				mAppFuncThemeBean.mTabTitleBean = themeBean.mTabTitleBean;
				mAppFuncThemeBean.mTabIconBeanMap = themeBean.mTabIconBeanMap;

			}
			// 关闭inputStream
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("ThemeManager", "IOException for close inputSteam");
				}
			}
		}
	}

	private boolean praserFolderTheme() {

		String themePackage = GOLauncherApp.getSettingControler().getScreenStyleSettingInfo()
				.getFolderStyle();
		if (mAppFuncThemeBean.mFoldericonBean.mPackageName != null
				&& mAppFuncThemeBean.mFoldericonBean.mPackageName.equals(themePackage)) {
			return false;
		}
		mAppFuncThemeBean.initFolderThemeBean();
		if (!themePackage.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
			if (!ThemeManager.isInstalledTheme(mContext, themePackage)) {
				themePackage = GOLauncherApp.getThemeManager().getCurThemePackage();
			}
			InputStream inputStream = null;
			XmlPullParser xmlPullParser = null;
			AppFuncThemeBean themeBean = null;
			FuncThemeParser parser = null;
			inputStream = ThemeManager.getInstance(GoLauncher.getContext().getApplication())
					.createParserInputStream(themePackage, ThemeConfig.APPFUNCTHEMEFILENAME);
			if (inputStream != null) {
				xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
			} else {
				xmlPullParser = XmlParserFactory.createXmlParser(mContext,
						ThemeConfig.APPFUNCTHEMEFILENAME, themePackage);
			}
			if (xmlPullParser != null) {
				themeBean = new AppFuncThemeBean(themePackage);
				parser = new FuncThemeParser();
				parser.parseFolderXml(xmlPullParser, themeBean);
				mAppFuncThemeBean.mFolderBean = themeBean.mFolderBean;
				mAppFuncThemeBean.mFoldericonBean = themeBean.mFoldericonBean;
			}
			// 关闭inputStream
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i("ThemeManager", "IOException for close inputSteam");
				}
			}
		}
		mAppFuncThemeBean.mFoldericonBean.mPackageName = themePackage;
		return true;
	}

	/**
	 * 如果是桌面文件夹的图片就不需做recycle，所以不添加
	 * 
	 * @param drawableName
	 * @return
	 */
	public Drawable getDrawable(String drawableName) {
		return getDrawable(drawableName, true);
	}

	/**
	 * 如果是桌面文件夹的图片就不需做recycle，所以不添加
	 * 
	 * @param drawableName
	 * @return
	 */
	public Drawable getDrawable(String drawableName, String packageName) {
		if (null == packageName) {
			return getDrawable(drawableName, true);
		}
		return getDrawable(drawableName, false, packageName);

	}

	/**
	 * 通过AppFuncThemeBean的字符串键值获取像对应的资源
	 * 
	 * @param drawableName
	 * @return
	 */
	public Drawable getDrawable(String drawableName, String packageName, boolean addToHashMap) {
		if (null == packageName) {
			return getDrawable(drawableName, addToHashMap);
		} else {
			return getDrawable(drawableName, addToHashMap, packageName);
		}
	}

	/**
	 * 通过AppFuncThemeBean的字符串键值获取像对应的资源
	 * 
	 * @param drawableName
	 * @param addToHashMap
	 *            是否添加到缓存的hashMap里面
	 * @return Drawable
	 */
	public Drawable getDrawable(String drawableName, boolean addToHashMap) {
		Drawable drawable = null;
		if (drawableName.equals(AppFuncConstants.NONE)) {
			return drawable;
		}

		AppFuncUtils funcUtil = AppFuncUtils.getInstance(mContext);
		if (drawableName.trim().compareTo("") != 0) {
			try {
				if (mIsHasTheme) {
					// 先从功能表主题资源管理器取图片
					boolean matches = false;
					try {
						matches = PATTERN.matcher(drawableName).matches();
					} catch (PatternSyntaxException e) {
						// e.printStackTrace();
						Log.i("XViewFrame", "match pattern error, drawableName =" + drawableName);
					}

					if (!matches) {
						int resourceId = mImageExplorer.getResourceId(drawableName);
						if (resourceId > 0) {
							drawable = funcUtil.getDrawableFromPicManager(resourceId);
							// 如果没有，再从图片资源管理器取
							if (drawable == null) {
								drawable = mImageExplorer.getDrawable(drawableName);
								if (drawable != null && addToHashMap) {
									// 加入功能表主题资源管理器
									funcUtil.addToPicManager(resourceId, drawable);
								}
							}
						}
					} else {
						// 特殊处理删除图标和文件夹编辑图标
						int drawableId = Integer.valueOf(drawableName).intValue();
						// if (drawableId == R.drawable.kill
						// || drawableId == R.drawable.kill_light
						// ||drawableId == R.drawable.eidt_folder
						// ||drawableId == R.drawable.eidt_folder_light) {
						// // 主题未提供，直接从主程序包里面取
						// drawable =
						// funcUtil.getDrawableFromMainPkg(drawableId);
						// } else {
						// drawable = funcUtil.getDrawable(drawableId,
						// addToHashMap);
						// }

						// 主题未提供，直接从主程序包里面取
						drawable = funcUtil.getDrawableFromMainPkg(drawableId);
					}
				} else {
					drawable = funcUtil.getDrawable(Integer.valueOf(drawableName).intValue(),
							addToHashMap);
				}
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
			} catch (NumberFormatException e) {
				Log.i("XViewFrame", "AppFuncThemeController getDrawable error, drawableName ="
						+ drawableName);
			}
		}
		return drawable;
	}

	/**
	 * 通过AppFuncThemeBean的字符串键值获取像对应的资源
	 * 
	 * @param drawableName
	 * @param addToHashMap
	 *            是否添加到缓存的hashMap里面
	 * @return Drawable
	 */
	public Drawable getDrawable(String drawableName, boolean addToHashMap, String packageName) {
		Drawable drawable = null;
		if (drawableName.equals(AppFuncConstants.NONE)) {
			return drawable;
		}

		AppFuncUtils funcUtil = AppFuncUtils.getInstance(mContext);
		if (drawableName.trim().compareTo("") != 0) {
			try {
				if (!packageName.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
					// 先从功能表主题资源管理器取图片
					boolean matches = false;
					try {
						matches = PATTERN.matcher(drawableName).matches();
					} catch (PatternSyntaxException e) {
						// e.printStackTrace();
						Log.i("XViewFrame", "match pattern error, drawableName =" + drawableName);
					}

					if (!matches) {
						int resourceId = mImageExplorer.getResourceId(drawableName, packageName);
						if (resourceId > 0) {
							// 如果没有，再从图片资源管理器取
							if (drawable == null) {
								drawable = mImageExplorer.getDrawable(packageName, drawableName);
							}
						}
					} else {
						// 特殊处理删除图标和文件夹编辑图标
						int drawableId = Integer.valueOf(drawableName).intValue();
						// if (drawableId == R.drawable.kill
						// || drawableId == R.drawable.kill_light
						// ||drawableId == R.drawable.eidt_folder
						// ||drawableId == R.drawable.eidt_folder_light) {
						// // 主题未提供，直接从主程序包里面取
						// drawable =
						// funcUtil.getDrawableFromMainPkg(drawableId);
						// } else {
						// drawable = funcUtil.getDrawable(drawableId,
						// addToHashMap);
						// }
						// 主题未提供，直接从主程序包里面取
						drawable = funcUtil.getDrawableFromMainPkg(drawableId);
					}
				} else {
					if (PATTERN.matcher(drawableName).matches()) {

						drawable = funcUtil.getDrawableFromMainPkg(Integer.valueOf(drawableName)
								.intValue());
					} else {
						int resourceId = mImageExplorer.getResourceId(drawableName, packageName);
						if (resourceId > 0) {
							// 如果没有，再从图片资源管理器取
							if (drawable == null) {
								drawable = mImageExplorer.getDrawable(packageName, drawableName);
							}
						}
					}
				}
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
			} catch (NumberFormatException e) {
				Log.i("XViewFrame", "AppFuncThemeController getDrawable error, drawableName ="
						+ drawableName);
			}
		}
		return drawable;
	}

	/**
	 * 获取主题的数据结构Bean
	 * 
	 * @return AppFuncThemeBean
	 */
	public AppFuncThemeBean getThemeBean() {
		return mAppFuncThemeBean;
	}

	/**
	 * 判断是否使用主题
	 * 
	 * @return true for 使用其他主题，false for 默认主题
	 */
	private boolean isUsedTheme() {
		return mThemeManager.isUsedTheme();
	}

	public void setChangeTheme(boolean mIsChangeTheme) {
		this.mIsChangeTheme = mIsChangeTheme;
	}

	public void handleMessage(int msgId, int param, Object object,
			@SuppressWarnings("rawtypes") List objects) {
		switch (msgId) {
			case IDiyMsgIds.EVENT_THEME_CHANGED : {
				mIsChangeTheme = true;
				// 通知释放资源
				preLoadResources();
				// 清空资源
				AppFuncUtils.getInstance(mContext).clearResources();
				ImageExplorer.getInstance(mContext).clearData();
				// 重新加载资源
				initThemeData();
				// 通知注册此消息的组件重新获取主题资源
				DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.LOADTHEMERES, null);
				mIsChangeTheme = false;
				Log.d("XViewFrame", "Received EVENT_THEME_CHANGED");
			}
				break;
			case IDiyMsgIds.APPDRAWER_TAB_HOME_THEME_CHANGE :
				// 清空资源
				// AppFuncUtils.getInstance((Activity)
				// mContext).clearResources();
				// 通知释放资源
				// preLoadResources();
				praserTabHomeTheme(false);
				DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.RELOADTABHOMETHEMERES,
						null);
				DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.LOADTHEMERES, null);
				break;
			case IDiyMsgIds.APPDRAWER_INDICATOR_THEME_CHANGE :
				// 清空资源
				// AppFuncUtils.getInstance((Activity)
				// mContext).clearResources();
				praserIndicatorTheme();
				DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.LOADTHEMERES, null);
				break;
			case IDiyMsgIds.APPDRAWER_FOLDER_THEME_CHANGE :
				// 清空资源
				// AppFuncUtils.getInstance((Activity)
				// mContext).clearResources();
				if (praserFolderTheme()) {
					ApplicationIcon.sIsReloaded = false;
					DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.RELOAD_FOLDER_THEMES,
							null);
				}
				break;
			default :
				break;
		}
	}

	/**
	 * 预处理图片
	 */
	private void preLoadResources() {
		// 通知组件释放资源
		DeliverMsgManager.getInstance().sendMsg(AppFuncConstants.THEME_CHANGE, null);
	}
}
