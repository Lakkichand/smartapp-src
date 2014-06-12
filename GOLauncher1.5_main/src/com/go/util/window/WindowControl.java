package com.go.util.window;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.components.DeskToast;

/**
 * Android窗口控制类,外部通过单例形式进行调用
 * 
 * @author yuankai
 * @version 1.0
 */
public class WindowControl {
	public static final int WALLPAPER_SCREENS_SPAN = 2;

	// /**
	// * 设置是否显示标题
	// *
	// * @param hasTitle
	// * 是否有标题
	// */
	// public static void setHasTitle(Activity activity, boolean hasTitle)
	// {
	// if (hasTitle)
	// {
	// activity.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	// }
	// else
	// {
	// activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
	// }
	// }

	/**
	 * 获取是否全屏
	 * 
	 * @return 是否全屏
	 */
	public static boolean getIsFullScreen(Activity activity) {
		boolean ret = false;
		try {
			WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
			ret = (attrs.flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 设置是否全屏
	 * 
	 * @param activity
	 *            上下文
	 * @param isFullScreen
	 *            是否全屏
	 */
	public static void setIsFullScreen(Activity activity, boolean isFullScreen) {
		try {
			if (isFullScreen) {
				// go full screen
				WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
				attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
				activity.getWindow().setAttributes(attrs);
				activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			} else {
				// go non-full screen
				WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
				attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
				activity.getWindow().setAttributes(attrs);
				activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 伸展通知栏
	 * 
	 * @throws Exception
	 *             invoke方法报的异常
	 */
	public static void expendNotification(Activity activity) throws Exception {
		String methodName = "expand";
		 if (Build.VERSION.SDK_INT >= 17) {
			 //Android4.2接口发生变化
			 methodName = "expandNotificationsPanel";
		 }
		 
		Object service = activity.getSystemService("statusbar");
		if (service != null) {
			Method expand = service.getClass().getMethod(methodName);
			expand.invoke(service);
		}
	}

	/**
	 * 收起通知栏
	 * 
	 * @throws Exception
	 *             invoke方法报的异常
	 * @author yangbing
	 */
	public static void collapseNotification(Context context) throws Exception {
		String methodName = "collapse";
		 if (Build.VERSION.SDK_INT >= 17) {
			 //Android4.2接口发生变化
			 methodName = "collapsePanels";
		 }
		
		Object obj = context.getSystemService("statusbar");
		if (obj != null) {
			Method expand = obj.getClass().getMethod(methodName);
			expand.invoke(obj);
		}
	}

	/**
	 * 设置壁纸
	 * 
	 * @param context
	 *            context
	 * @param resources
	 *            如果是主题包，必须用主题包的resource
	 * @param resId
	 *            图片资源id
	 */
	public static void setWallpaper(Context context, Resources resources, int resId) {
		OutOfMemoryHandler.handle();

		if (context == null || resources == null || resId < 0) {
			return;
		}

		boolean bSetOk = false;

		WallpaperManager wpm = null;
		Drawable drb = null;
		BitmapDrawable bdrb = null;
		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			// 获取宽高
			// 竖屏状态
			int screenW = 0;
			int screenH = 0;
			if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				screenW = resources.getDisplayMetrics().widthPixels;
				screenH = resources.getDisplayMetrics().heightPixels;
			} else {
				screenW = resources.getDisplayMetrics().heightPixels;
				screenH = resources.getDisplayMetrics().widthPixels;
			}

			wpm = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
			drb = resources.getDrawable(resId);
			{
				// 图片处理
				// 对Lephone的特殊处理
				if (Machine.isLephone()) {
					bdrb = prepareWallpaper((BitmapDrawable) drb, screenH, screenH, resources);
				} else {
					bdrb = prepareWallpaper((BitmapDrawable) drb, screenW * WALLPAPER_SCREENS_SPAN,
							screenH, resources);
				}
				out = new ByteArrayOutputStream();
				boolean b = bdrb.getBitmap().compress(CompressFormat.JPEG, 100, out);
				if (b) {
					in = new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
					out.close();
					out = null;
					wpm.setStream(in);
					in.close();
					in = null;
					bSetOk = true;
				} else {
					in = resources.openRawResource(resId);
					wpm.setStream(in);
					in.close();
					in = null;
					bSetOk = true;
				}
			}
		} catch (OutOfMemoryError e) {
			// 内存爆掉，不进行图片处理
			OutOfMemoryHandler.handle();

			try {
				if (wpm != null) {
					in = resources.openRawResource(resId);
					wpm.setStream(in);
					in.close();
					in = null;
				}
				bSetOk = true;
			} catch (Throwable e2) {
				Log.i(LogConstants.HEART_TAG, "fail to re-change wallpaper " + e2);
			}
		} catch (IOException e) {
			Log.i(LogConstants.HEART_TAG, "fail to change wallpaper " + e);
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "fail to change wallpaper " + e);
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				in = null;
			}
			if (null != out) {
				try {
					out.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

		if (!bSetOk) {
			try {
				DeskToast.makeText(context, context.getString(R.string.set_wallpaper_error),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public static BitmapDrawable prepareWallpaper(BitmapDrawable bmpDrawable, int w, int h,
			Resources res) {
		BitmapDrawable ret = bmpDrawable;

		// 缩放
		int width = ret.getIntrinsicWidth();
		int height = ret.getIntrinsicHeight();
		if (width == w && height == h) {
			return ret;
		}
		float wScale = (float) w / (float) width;
		float hScale = (float) h / (float) height;
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
		if (width > w || height > h) {
			try {
				BitmapDrawable drawable = BitmapUtility.clipDrawable(ret, w, h, res);
				ret = drawable;
			} catch (Exception e) {
				return ret;
			}
		}

		return ret;
	}

	/**
	 * 获取屏幕显示区域
	 * 
	 * @param context
	 * @return 屏幕显示矩形区域
	 */
	public static Rect getDisplayRect(Context context) {
		final DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return new Rect(0, 0, dm.widthPixels, dm.heightPixels);
	}

	/**
	 * 获取density
	 * 
	 * @param context
	 * @return
	 */
	public static float getDensity(Context context) {
		return context.getResources().getDisplayMetrics().density;
	}

	/**
	 * 是否竖屏
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isPortrait(Context context) {
		int orientation = context.getResources().getConfiguration().orientation;
		return orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	/**
	 * 获取状态栏高度
	 * 
	 * @param activity
	 * @return 全屏时返回0，非全屏返状态栏实际高度
	 */
	public static int getStatusbarHeight(Activity activity) {
		boolean isFullScreen = getIsFullScreen(activity);
		if (!isFullScreen) {
			return StatusBarHandler.getStatusbarHeight();
		}
		return 0;
	}
}
