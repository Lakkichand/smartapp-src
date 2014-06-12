package com.jiubang.ggheart.apps.gowidget.gostore.util;

import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.jiubang.ggheart.apps.gowidget.gostore.bean.BaseItemBean;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ListElementBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ListElementBean.Element;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.ListElementBean.SoftwareElement;

public class GoStoreDisplayUtil {
	/**
	 * 对Bitmap进行匹配机型的缩放，若不能缩放，则直接返回原图.
	 * 
	 * @param context
	 * @param sourceBitmap
	 *            源Bitmap
	 * @return
	 */
	public static Bitmap scaleBitmapToMachine(Context context, Bitmap sourceBitmap) {
		Bitmap desBitmap = sourceBitmap;
		if (context != null && sourceBitmap != null) {
			int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
			if (densityDpi != GoStorePublicDefine.STANDARD_DENSITYDPI) {
				float scale = densityDpi / GoStorePublicDefine.STANDARD_DENSITYDPI;
				try {
					desBitmap = Bitmap.createScaledBitmap(sourceBitmap,
							(int) (sourceBitmap.getWidth() * scale),
							(int) (sourceBitmap.getHeight() * scale), true);
				} catch (Error e) {
					// TODO: handle exception
					// Log.i("ThemeStoreUtil",
					// "scaleBitmapToMachine throw exception= " +
					// e.getMessage());
				}
			}
		}
		return desBitmap;
	}

	/**
	 * 对Bitmap进行匹配手机屏幕的缩放，若不能缩放，则直接返回原图.
	 * 
	 * @param context
	 * @param sourceBitmap
	 *            源Bitmap
	 * @return
	 */
	public static Bitmap scaleBitmapToScreen(Context context, Bitmap sourceBitmap) {
		Bitmap desBitmap = sourceBitmap;
		if (context != null && sourceBitmap != null) {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			wMgr.getDefaultDisplay().getMetrics(dm);
			int width = dm.widthPixels;
			int height = dm.heightPixels;
			if (sourceBitmap.getWidth() != width || sourceBitmap.getHeight() != height) {
				try {
					desBitmap = Bitmap.createScaledBitmap(sourceBitmap, width, height, true);
				} catch (Error e) {
					// TODO: handle exception
					// Log.i("ThemeStoreUtil",
					// "scaleBitmapToMachine throw exception= " +
					// e.getMessage());
				}
			}
		}
		return desBitmap;
	}

	/**
	 * 对像素进行匹配机型的缩放
	 * 
	 * @param context
	 * @param sourcePx
	 * @return
	 */
	public static int scalePxToMachine(Context context, int sourcePx) {
		int result = sourcePx;
		if (context != null) {
			float scale = 1.0f;
			int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
			scale = densityDpi / GoStorePublicDefine.STANDARD_DENSITYDPI;
			result = (int) (sourcePx * scale);
		}
		return result;
	}

	/**
	 * 把Bitmap进行指定大小的缩放
	 * 
	 * @param sourceBitmap
	 *            源Bitmap
	 * @param width
	 *            目标宽度
	 * @param height
	 *            目标高度
	 * @return
	 */
	public static Bitmap scaleBitmapToSize(Bitmap sourceBitmap, int width, int height) {
		Bitmap desBitmap = Bitmap.createScaledBitmap(sourceBitmap, width, height, true);
		return desBitmap;
	}

	/**
	 * 把Bitmap进行指定大小的缩放，若不能缩放，则直接返回原图。
	 * 
	 * @param sourceBitmap
	 *            源Bitmap
	 * @param width
	 *            目标宽度
	 * @param height
	 *            目标高度
	 * @return
	 */
	public static Bitmap scaleBitmapToDisplay(Context context, Bitmap sourceBitmap,
			int displayWidth, int displayHeight) {
		Bitmap desBitmap = sourceBitmap;
		if (context != null && sourceBitmap != null && displayWidth > 0 && displayHeight > 0) {
			if (sourceBitmap.getWidth() == displayWidth
					&& sourceBitmap.getHeight() == displayHeight) {
				// 目标尺寸与现有尺寸相同，则直接返回。
				return desBitmap;
			}
			float originalWidth = sourceBitmap.getWidth();
			float originalHeight = sourceBitmap.getHeight();
			float scale = 1.0f;
			if (originalWidth > 0.0 && originalHeight > 0.0) {
				float scaleWidth = displayWidth / originalWidth;
				float scaleHeight = displayHeight / originalHeight;
				scale = scaleWidth > scaleHeight ? scaleWidth : scaleHeight;
			}
			try {
				// 等比缩放
				Bitmap bitmap = Bitmap.createScaledBitmap(sourceBitmap,
						(int) (sourceBitmap.getWidth() * scale),
						(int) (sourceBitmap.getHeight() * scale), true);
				if (bitmap != null) {
					int bmpWidth = bitmap.getWidth();
					int bmpHeight = bitmap.getHeight();
					if (bmpWidth == displayWidth && bmpHeight == displayHeight) {
						desBitmap = bitmap;
					} else {
						// 显示缩放
						int startWidth = (bitmap.getWidth() - displayWidth) / 2;
						int startHeight = (bitmap.getHeight() - displayHeight) / 2;
						startWidth = startWidth < 0 ? 0 : startWidth;
						startHeight = startHeight < 0 ? 0 : startHeight;
						desBitmap = Bitmap.createBitmap(bitmap, startWidth, startHeight,
								displayWidth, displayHeight);
						if (!bitmap.equals(desBitmap)) {
							String hashCode = Integer.valueOf(bitmap.hashCode()).toString();
							// 回收中间的缩放bitmap
							bitmap.recycle();
						}
					}
					bitmap = null;
				}

			} catch (Throwable e) {
				// TODO: handle exception
				// Log.i("GoStore",
				// "ThemeStoreUtil.scaleBitmapToDisplay throw error for " +
				// e.getMessage());
			}
		}
		return desBitmap;
	}

	/**
	 * 检测屏幕是否有足够宽度显示
	 * 
	 * @param context
	 * @param requestWidth
	 *            显示需要的宽度
	 * @return 如果屏幕有足够宽度返回TRUE，否则返回FALSE
	 */
	public static boolean checkEnoughWidthToDisplayMainSort(Context context, int requestWidth) {
		boolean result = true;
		if (context != null) {
			WindowManager windowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			if (windowManager != null) {
				DisplayMetrics displayMetrics = new DisplayMetrics();
				windowManager.getDefaultDisplay().getMetrics(displayMetrics);
				int displayHeight = displayMetrics.widthPixels;
				if (displayHeight < requestWidth) {
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * 检测给定宽度是否与屏幕显示宽度一样
	 * 
	 * @param context
	 * @param width
	 *            给定宽度
	 * @return
	 */
	public static boolean isEqualDisplayWidth(Context context, int width) {
		boolean result = false;
		if (context != null) {
			WindowManager windowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			if (windowManager != null) {
				DisplayMetrics displayMetrics = new DisplayMetrics();
				windowManager.getDefaultDisplay().getMetrics(displayMetrics);
				int displayWidth = displayMetrics.widthPixels;
				if (displayWidth == width) {
					result = true;
				}
			}
		}
		return result;
	}

	/**
	 * 是否隐藏首页底部更多按钮的判断方法
	 * 
	 * @return TRUE,隐藏 FALSE,显示
	 */
	public static boolean isHideMainViewBottom() {
		boolean result = false;
		Locale locale = Locale.getDefault();
		// 简体中文
		Locale locale_zh_CN = Locale.CHINA;
		if (locale.getCountry().equals(locale_zh_CN.getCountry())
				&& locale.getLanguage().equals(locale_zh_CN.getLanguage())) {
			result = true;
		}
		return result;
	}

	/**
	 * 获取屏幕宽高比的方法，默认值为0.6f
	 * 
	 * @param context
	 * @return 屏幕宽高比
	 */
	public static float getDisplayWidthAndHeightScale(Context context) {
		float scale = 0.6f;
		if (context != null) {
			// 获取屏幕高度和宽度
			WindowManager windowManager = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			if (windowManager != null) {
				DisplayMetrics displayMetrics = new DisplayMetrics();
				windowManager.getDefaultDisplay().getMetrics(displayMetrics);
				float displayWidth = displayMetrics.widthPixels;
				float displayHeight = displayMetrics.heightPixels;
				// 计算屏幕宽高比
				scale = displayWidth / displayHeight;
			}
		}
		return scale;
	}

	/**
	 * 是否需要显示（只有本地已安装且不需更新时，才不需显示）
	 * 
	 * @author huyong
	 * @param context
	 * @param pkgName
	 * @param verCode
	 * @return
	 */
	public static boolean isNeedToShow(Context context, String pkgName, int verCode) {
		boolean result = true;
		if (context == null || pkgName == null) {
			return result;
		}

		if (GoStoreAppInforUtil.isApplicationExsit(context, pkgName)) {
			// 只有本地已安装，且不可更新，才不需显示
			result = GoStoreAppInforUtil.isNewToAlreadyInstall(context, pkgName, verCode);
		}

		return result;
	}

	/**
	 * 根据指定规则，过滤数据。
	 * 
	 * @author huyong
	 * @param baseItemBeans
	 */
	public static void filterElementData(Context context, ArrayList<Element> elements) {
		if (context == null || elements == null || elements.size() == 0) {
			return;
		}
		Element elementBean = null;
		SoftwareElement softwareElement = null;
		String pkgName = null;
		int verCode = 0;
		final int size = elements.size();
		ArrayList<Element> tmpElements = new ArrayList<Element>(size);
		for (int i = 0; i < size; ++i) {
			elementBean = elements.get(i);
			if (elementBean.mElementType == ListElementBean.ELEMENTTYPE_SOFTWARE) {
				// 只过滤软件类型的数据
				softwareElement = (SoftwareElement) elementBean;
				pkgName = softwareElement.mPkgName;
				verCode = softwareElement.mVersionCode;
				if (!GoStoreDisplayUtil.isNeedToShow(context, pkgName, verCode)) {
					tmpElements.add(elementBean);
				}
			}
		}
		if (tmpElements.size() == size) {
			// 若发现全部被过滤，则反而不过滤
			return;
		}

		for (Element element : tmpElements) {
			// Log.i("filter", "filter = " +
			// ((SoftwareElement)element).mPkgName);
			elements.remove(element);
		}
	}

	/**
	 * 根据指定规则，过滤数据。
	 * 
	 * @author huyong
	 * @param baseItemBeans
	 */
	public static void filterDataView(Context context, ArrayList<BaseItemBean> baseItemBeans) {
		if (context == null || baseItemBeans == null || baseItemBeans.size() == 0) {
			return;
		}
		BaseItemBean itemBean = null;
		String pkgName = null;
		final int size = baseItemBeans.size();
		ArrayList<BaseItemBean> tmpItems = new ArrayList<BaseItemBean>(size);
		for (int i = 0; i < size; ++i) {
			itemBean = baseItemBeans.get(i);
			pkgName = itemBean.getPkgName();
			int verCode = itemBean.getVerCode();
			if (!GoStoreDisplayUtil.isNeedToShow(context, pkgName, verCode)) {
				tmpItems.add(itemBean);
			}
		}
		int tmpSize = tmpItems.size();
		if (tmpSize == 0 || tmpSize == size) {
			// 若发现全部被过滤，则反而不过滤
			return;
		}
		for (BaseItemBean item : tmpItems) {
			// Log.i("filter", "filter = " + item.getPkgName());
			baseItemBeans.remove(item);
		}
	}
}
