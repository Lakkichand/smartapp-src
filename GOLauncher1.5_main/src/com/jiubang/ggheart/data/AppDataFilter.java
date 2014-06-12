package com.jiubang.ggheart.data;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.go.util.Utilities;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.AppDataThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
/**
 * 
 *
 */
public class AppDataFilter extends BroadCaster {

	public final static int EVENT_THEME_CHANGED = 0;

	// 以应用程序component为key，资源名称为value
	private AppDataThemeBean mAppDataThemeBean = null;
	private BitmapDrawable mIconback; // 底图
	// private Bitmap mIconbackCopy; //底图拷贝
	private BitmapDrawable mIconupon; // 罩子
	private BitmapDrawable mIconmask; // mask蒙版
	private Context mContext;

	public AppDataFilter(Context context) {
		mContext = context;
		mAppDataThemeBean = (AppDataThemeBean) ThemeManager.getInstance(mContext).getThemeBean(
				ThemeBean.THEMEBEAN_TYPE_APPDATA);
	}

	public AppDataFilter(Context context, AppDataThemeBean bean) {
		mContext = context;
		mAppDataThemeBean = bean;
	}

	public void resetData() {
		mAppDataThemeBean = (AppDataThemeBean) ThemeManager.getInstance(mContext).getThemeBean(
				ThemeBean.THEMEBEAN_TYPE_APPDATA);
		mIconback = null;
		mIconupon = null;
	}

	public void resetData(AppDataThemeBean bean) {
		mAppDataThemeBean = bean;
		mIconback = null;
		mIconupon = null;
	}

	public boolean needResetData(String packageStr) {
		if (null == packageStr && null == mAppDataThemeBean) {
			return false;
		}

		if (packageStr != null && mAppDataThemeBean != null) {
			final String pkgName = mAppDataThemeBean.getPackageName();
			if (pkgName != null && packageStr.equals(pkgName)) {
				return false;
			}
		}

		return true;
	}

	public boolean isNeedReplace(String componentName) {
		boolean result = false;
		if (mAppDataThemeBean == null) {
			return result;
		}
		ConcurrentHashMap<String, String> filterMap = mAppDataThemeBean.getFilterAppsMap();
		if (filterMap == null || filterMap.size() <= 0) {
			return result;
		}
		result = filterMap.containsKey(componentName);
		return result;
	}

	public String getDrawableName(String comopnentName) {
		if (mAppDataThemeBean == null) {
			return null;
		}
		ConcurrentHashMap<String, String> filterMap = mAppDataThemeBean.getFilterAppsMap();
		String drawableName = filterMap.get(comopnentName);
		return drawableName;
	}

	/**
	 * 获取罩子图
	 * 
	 * @author huyong
	 * @return
	 */
	public BitmapDrawable getIconupon(boolean showBase) { // 当不显示底座时，罩子也不显示 -by
															// Yugi
		if (!showBase) {
			return mIconupon = null;
		}
		if (mAppDataThemeBean != null) {
			// TODO:根据业务规则来获取随机图
			ArrayList<String> iconuponList = mAppDataThemeBean.getIconuponNameList();
			if (iconuponList != null) {
				int size = iconuponList.size();
				if (size <= 0) {
					return null;
				}
				Random rand = new Random();
				int index = rand.nextInt(size);
				String iconuponName = iconuponList.get(index);
				final String pkgName = mAppDataThemeBean.getPackageName();
				if (null != pkgName) {
					mIconupon = (BitmapDrawable) ImageExplorer.getInstance(mContext).getDrawable(
							pkgName, iconuponName);
				} else {
					mIconupon = (BitmapDrawable) ImageExplorer.getInstance(mContext).getDrawable(
							iconuponName);
				}
			}
		}
		return mIconupon;
	}
	
	/**
	 * 获取蒙版图
	 * 
	 * @author jiangchao
	 * @return
	 */
	public BitmapDrawable getIconmask(boolean showBase) {
		if (!showBase) {
			return mIconmask = null;
		}
		if (mAppDataThemeBean != null) {
			// TODO:根据业务规则来获取随机图
			ArrayList<String> iconmaskList = mAppDataThemeBean.getmIconmaskNameList();
			if (iconmaskList != null) {
				int size = iconmaskList.size();
				if (size <= 0) {
					return null;
				}
				Random rand = new Random();
				int index = rand.nextInt(size);
				String iconmaskName = iconmaskList.get(index);
				final String pkgName = mAppDataThemeBean.getPackageName();
				if (null != pkgName) {
					mIconmask = (BitmapDrawable) ImageExplorer.getInstance(mContext).getDrawable(
							pkgName, iconmaskName);
				} else {
					mIconmask = (BitmapDrawable) ImageExplorer.getInstance(mContext).getDrawable(
							iconmaskName);
				}
			}
		}
		return mIconmask;
	}
	
	/**
	 * 获取底图
	 * 
	 * @author huyong
	 * @return
	 */
	public BitmapDrawable getIconback(boolean showBase) {
		if (!showBase) {
			return mIconback = null;
		}
		if (mAppDataThemeBean != null) {
			// TODO:根据业务规则来获取随机图
			ArrayList<String> iconbackList = mAppDataThemeBean.getIconbackNameList();
			if (iconbackList != null) {
				int size = iconbackList.size();
				if (size <= 0) {
					return null;
				}
				Random rand = new Random();
				int index = rand.nextInt(size);
				String iconbackName = iconbackList.get(index);
				final String packageName = mAppDataThemeBean.getPackageName();
				if (null != packageName) {
					mIconback = (BitmapDrawable) ImageExplorer.getInstance(mContext).getDrawable(
							packageName, iconbackName);
				} else {
					mIconback = (BitmapDrawable) ImageExplorer.getInstance(mContext).getDrawable(
							iconbackName);
				}
			}
		}
		return mIconback;
	}

	/**
	 * 获取图片缩放比率
	 * 
	 * @author huyong
	 * @return
	 */
	public float getIconScaleFactor() {
		if (mAppDataThemeBean != null) {
			return mAppDataThemeBean.getScaleFactor();
		} else {
			return AppDataThemeBean.DEFALUT_SCALE_FACTOR;
		}
	}

	/*
	 * public Bitmap getIconbackCopy() { if (mIconbackCopy != null) { return
	 * mIconbackCopy; } BitmapDrawable iconback = getIconback(); if (iconback !=
	 * null) { mIconbackCopy =
	 * iconback.getBitmap().copy(Bitmap.Config.ARGB_8888, true); } return
	 * mIconbackCopy; }
	 */

	/**
	 * 通过intent，获取主题资源包中对应程序的资源图
	 * 
	 * @author huyong
	 * @param itemIntent
	 * @return
	 */
	public BitmapDrawable getThemeDrawable(final Intent itemIntent) {
		if (null == mAppDataThemeBean) {
			return null;
		}
		if (itemIntent == null || itemIntent.getComponent() == null) {
			return null;
		}
		String comopnentName = itemIntent.getComponent().toString();
		String drawableName = getDrawableName(comopnentName);
		Drawable tmpDrawable = null;
		final String pkgName = mAppDataThemeBean.getPackageName();
		if (null != pkgName) {
			tmpDrawable = ImageExplorer.getInstance(mContext).getDrawable(pkgName, drawableName);
		} else {
			tmpDrawable = ImageExplorer.getInstance(mContext).getDrawable(drawableName);
		}

		BitmapDrawable bmpDrawable = Utilities.createBitmapDrawableFromDrawable(tmpDrawable,
				mContext);
		if (bmpDrawable != null && bmpDrawable.getBitmap() != null) {
			final Bitmap bitmap = Utilities
					.createBitmapThumbnail(bmpDrawable.getBitmap(), mContext);
			bmpDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
		}
		return bmpDrawable;
	}

	public String getDataThemeBeanPackage() {
		if (null != mAppDataThemeBean) {
			return mAppDataThemeBean.getPackageName();
		}
		return null;

	}
}