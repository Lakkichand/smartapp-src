package com.jiubang.ggheart.plugin.mediamanagement;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.go.util.AppUtils;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.BasePluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaManager;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaMessageManager;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaUIManager;
import com.jiubang.ggheart.plugin.mediamanagement.inf.ISwitchMenuControler;
import com.jiubang.ggheart.plugin.mediamanagement.inf.MediaContext;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-16]
 */
public class MediaPluginFactory extends BasePluginFactory {
	private static final String ADMIN_NAME = "com.jiubang.ggheart.plugin.mediamanagement.MediaManagementAdmin";
	private static int sMediaPluginExist = -1;
	private static IMediaManager sMediaManager;
	private static IMediaUIManager sMediaUIManager;
	private static ISwitchMenuControler sSwitchMenuControler;

	private static MediaContext getMediaContext(Context remoteContext, ClassLoader dexLoader) {
		MediaContext mediaContext = null;
		if (remoteContext != null && dexLoader != null) {
			mediaContext = new MediaContext(remoteContext, dexLoader);
		}
		return mediaContext;
	}

	public static void buildSwitchMenuControler(Activity activity, View rootView) {
		if (sSwitchMenuControler == null) {
			sSwitchMenuControler = new SwitchMenuControler(activity, rootView);
		}
	}

	@SuppressWarnings("rawtypes")
	public static void buildMediaPlugin(Activity activity) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Context ctx = getRemoteContext(activity, PackageName.MEDIA_PLUGIN);
		ClassLoader loader = createDexClassLoader(activity, ctx, PackageName.MEDIA_PLUGIN, null);
		Class clazz = getPluginAdminClass(activity, ADMIN_NAME, loader);
//		try {
			Constructor constructor = clazz.getConstructor(MediaContext.class, Activity.class,
					IMediaMessageManager.class);
			Object pluginMain = constructor.newInstance(getMediaContext(ctx, loader), activity,
					new MediaMessageManager());
			Method method = clazz.getMethod("getMediaManager");
			sMediaManager = (IMediaManager) method.invoke(pluginMain);
			method = clazz.getMethod("getMediaUIManager");
			sMediaUIManager = (IMediaUIManager) method.invoke(pluginMain);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static IMediaManager getMediaManager() {
		return sMediaManager;
	}

	public static IMediaUIManager getMediaUIManager() {
		return sMediaUIManager;
	}

	public static ISwitchMenuControler getSwitchMenuControler() {
		return sSwitchMenuControler;
	}

	public static boolean isMediaPluginExist(Context context) {
		if (sMediaPluginExist == -1) {
			sMediaPluginExist = AppUtils.isAppExist(context, PackageName.MEDIA_PLUGIN) ? 1 : 0;
		}
		return sMediaPluginExist == 1 ? true : false;
	}

	/**
	 * 设置资源管理插件是否存在
	 * @param isExist
	 */
	public static void setMediaPluginExist(int isExist) {
		sMediaPluginExist = isExist;
	}

	public static float sMediaPluginHavePlayBarVersion = 1.2f;
	private static int sMediaPluginHavePlayingBar = -1;
	/**
	 * 检测插件包当前版本是否拥有了正在播放条
	 * @return
	 */
	public static boolean isMediaPluginHavePlayingBar() {
		if (sMediaPluginHavePlayingBar == -1) {
			String versionName = AppUtils.getVersionNameByPkgName(GOLauncherApp.getContext(),
					PackageName.MEDIA_PLUGIN);
			float mediaPluginCurrentVersion = AppUtils.changeVersionNameToFloat(versionName);
			if (mediaPluginCurrentVersion >= sMediaPluginHavePlayBarVersion) {
				sMediaPluginHavePlayingBar = 1;
			} else {
				sMediaPluginHavePlayingBar = 0;
			}
		}
		return sMediaPluginHavePlayingBar == 1 ? true : false;
	}
}
