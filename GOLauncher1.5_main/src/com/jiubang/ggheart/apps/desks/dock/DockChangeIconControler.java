/**
 * 
 */
package com.jiubang.ggheart.apps.desks.dock;

import android.content.Context;

import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.DockBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * @author ruxueqin
 * 
 */
public class DockChangeIconControler {
	private Context mContext;

	private static DockChangeIconControler sControler;

	private DockBean mDockBean;

	private String mThemePkg;

	/**
	 * 
	 */
	public DockChangeIconControler(Context context) {
		mContext = context;
	}

	public static DockChangeIconControler getInstance(Context context) {
		if (null == sControler) {
			sControler = new DockChangeIconControler(context);
		}
		return sControler;
	}

	public DockBean getDockBean(String themePkg) {
		if (null == themePkg) {
			return null;
		}

		if (!themePkg.equals(mThemePkg)
				|| (null != mDockBean && null != mDockBean.mSymtemDefualt && mDockBean.mSymtemDefualt
						.isEmpty())) {
			mDockBean = GOLauncherApp.getThemeManager().getDockBeanFromTheme(themePkg);
			mThemePkg = themePkg;
		}
		return mDockBean;
	}
}
