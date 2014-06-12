package com.go.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;

import com.jiubang.ggheart.launcher.ICustomAction;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Log;
/**
 * 
 * @author 
 *
 */
// TODO:改名
public class ConvertUtils {

	public final static String FORM_DECIMAL_WITH_TWO = "####.##"; // 格式：保留小数点后两位
	public final static String FORM_DECIMAL_WITH_ONE = "####.#"; // 格式：保留小数点后一位
	public final static String FORM_WITHOUT_DECIMAL = "####"; // 格式：不保留小数点

	static public boolean int2boolean(int intValue) {
		if (intValue == 0) {
			return false;
		}
		return true;
	}

	static public int boolean2int(boolean booleanValue) {
		if (booleanValue) {
			return 1;
		}
		return 0;
	}

	public static String intentToString(final Intent intent) {
		if (intent == null) {
			return null;
		}
		String returnStr = null;
		if (intent.getAction() == null || intent.getAction().equals("")) {
			intent.setAction(Intent.ACTION_VIEW);
		}
		try {
			returnStr = intent.toUri(0);
		} catch (Exception e) {
			Log.i("ConvertUtils", "has exception " + e.getMessage());
		} catch (Error e) {
			Log.i("ConvertUtils", "has error " + e.getMessage());
		}
		return returnStr;
	}

	public static Intent stringToIntent(final String intentStr) {
		if (intentStr == null) {
			return null;
		}
		try {
			Intent intent = Intent.parseUri(intentStr, 0);
			return intent;
		} catch (URISyntaxException e) {
			// TODO LOG 创建Intent失败
			e.printStackTrace();
			return null;
		} catch (NumberFormatException e) {
			// TODO LOG 创建Intent失败
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			// TODO LOG 创建Intent失败
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 系统应用比较，比较ComponentName
	 * 
	 * @param srcIntent
	 * @param desIntent
	 * @return
	 */
	// old interface
	public static boolean intentCompare(Intent srcIntent, Intent desIntent) {
		if (null == srcIntent || null == desIntent) {
			return false;
		}

		ComponentName srcComponentName = srcIntent.getComponent();
		ComponentName desComponentName = desIntent.getComponent();
		
		String src = srcIntent.getAction();
		String des = desIntent.getAction();
		// ADT-7842 增加对go手册的处理
		if (src != null && des != null
				&& src.equals(ICustomAction.ACTION_SHOW_GO_HANDBOOK)
				&& des.equals(ICustomAction.ACTION_SHOW_GO_HANDBOOK)) {
			return true;
		}
		
		if (null == srcComponentName || null == desComponentName) {
			return false;
		}

		return srcComponentName.equals(desComponentName);
	}

	/**
	 * 系统快捷方式比较，没有ComponentName
	 * 
	 * @param srcIntent
	 * @param desIntent
	 * @return
	 */
	public static boolean shortcutIntentCompare(Intent srcIntent, Intent desIntent) {
		if (null == srcIntent || null == desIntent) {
			return false;
		}

		String srcIntentString = intentToString(srcIntent);
		String desIntentString = intentToString(desIntent);
		if (null == srcIntentString || null == desIntentString
		/* || srcIntentString.length() != desIntentString.length() */)// launchFlags
																		// 可能导致长度
		{
			return false;
		}

		// 每个分号的找，避免顺序错乱导致不匹配的问题
		String separateStr = ";";

		int start = 0;
		int end = srcIntentString.indexOf(separateStr, start);
		while (end != -1) {
			String subStr = srcIntentString.substring(start, end);

			if (!subStr.contains("launchFlags") && !desIntentString.contains(subStr))// launchFlags
																						// 不参与比较
			{
				return false;
			}

			start = end + 1;
			end = srcIntentString.indexOf(separateStr, start);
		}

		String subStr = srcIntentString.substring(start, srcIntentString.length());
		if (!desIntentString.contains(subStr)) {
			return false;
		}

		return true;
	}

	/**
	 * 自己的应用比较，没有ComponentName
	 * 
	 * @param srcIntent
	 * @param desIntent
	 * @return
	 */
	// old interface
	public static boolean selfIntentCompare(Intent srcIntent, Intent desIntent) {
		if (null == srcIntent || null == desIntent) {
			return false;
		}

		String srcStr = intentToString(srcIntent);
		String desStr = intentToString(desIntent);
		if (null == srcStr || null == desStr) {
			return false;
		}

		// -3是去掉“end"后的匹配
		int length = desStr.contains("end") ? (desStr.length() - 3) : desStr.length();

		return srcStr.regionMatches(0, desStr, 0, length);
	}

	/**
	 * <br>功能简述:把intent转String,比较String是否equals，以此判断intent相等
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param srcIntent
	 * @param desIntent
	 * @return
	 */
	public static boolean intentToStringCompare(Intent srcIntent, Intent desIntent) {
		if (null == srcIntent || null == desIntent) {
			return false;
		}

		try {
			String srcStr = srcIntent.toUri(0);
			String desStr = desIntent.toUri(0);
			if (null == srcStr || null == desStr) {
				return false;
			}

			return srcStr.equals(desStr);
		} catch (Exception e) {
			// 不处理
		}
		return false;
	}

	/**
	 * 判断两个Intent是否属于一个应用 resolve by dengdazhong 修复：ADT-6899
	 * 添加一键锁屏快捷方式到桌面，卸载GO锁屏时没有清除该快捷方式
	 * 移除桌面图标时除了判断intent是否一致外，还要判断他们是否属于同一个程序，因为有可能是快捷方式，被卸载程序的快捷方式也需要移除
	 * 
	 * @param src
	 * @param des
	 * @return
	 */
	public static boolean isIntentsBelongSameApp(Intent src, Intent des) {
		if (null == src || null == des) {
			return false;
		}
		ComponentName srcComponentName = src.getComponent();
		ComponentName desComponentName = des.getComponent();
		if (null == srcComponentName || null == desComponentName) {
			return false;
		}
		String desPkgName = desComponentName.getPackageName();
		String srcPkgName = srcComponentName.getPackageName();
		if (desPkgName != null && srcPkgName != null) {
			return srcPkgName.equals(desPkgName);
		} else {
			return false;
		}
	}

	public static String uriToString(final Uri uri) {
		if (null == uri) {
			return null;
		}
		return uri.toString();
	}

	public static Uri stringToUri(String uriStr) {
		if (null == uriStr) {
			return null;
		}
		return Uri.parse(uriStr);
	}

	public static BitmapDrawable createBitmap(Context contexts, byte[] data) {
		if (null == data || data.length <= 0) {
			return null;
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		Bitmap bitmap = BitmapFactory.decodeStream(bis);
		return new BitmapDrawable(contexts.getResources(), bitmap);
	}

	public static void saveBitmapToValues(ContentValues values, String key,
			BitmapDrawable bitmapDrawable) {
		if (bitmapDrawable != null) {
			Bitmap bitmap = bitmapDrawable.getBitmap();
			if (bitmap == null) {
				return;
			}
			int size = bitmap.getWidth() * bitmap.getHeight() * 4;
			ByteArrayOutputStream out = new ByteArrayOutputStream(size);
			try {
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				values.put(key, out.toByteArray());
			} catch (IOException e) {
				Log.w("Error", "Could not write icon");
			}
		}
	}

	/**
	 * 返回大小缩写样式
	 * 
	 * @param size
	 * @param form
	 *            转换的格式，如：“####.##”保留小数点后两位
	 * @return
	 */
	public static String convertSizeToString(long size, String form) {
		String sizeStr = String.valueOf(size) + "B";
		if (size > 1024L) {
			size = size / 1024L;
			if (size < 1024L) {
				sizeStr = size + "KB";
			} else {
				float sizeMB = size / 1024F;
				if (sizeMB < 1024F) {
					DecimalFormat format = new DecimalFormat(form);
					sizeStr = format.format(sizeMB) + "MB";
				} else {
					float sizeGB = sizeMB / 1024F;
					form = FORM_DECIMAL_WITH_ONE;
					DecimalFormat format = new DecimalFormat(form);
					sizeStr = format.format(sizeGB) + "GB";
				}

			}
		}
		return sizeStr;
	}

	/**
	 * 返回转换后容量大小
	 * 
	 * @param size
	 * @return 整型
	 */
	public static int convertSize(long size) {
		if (size > 1024L) {
			size = size / 1024L;
			if (size < 1024L) {
				// 直接返回
			} else {
				size = size / 1024L;
				if (size < 1024F) {
					// 直接返回
				} else {
					size = size / 1024L;
				}
			}
		}
		return (int) size;
	}

	/**
	 * 返回转换后容量大小
	 * 
	 * @param size
	 * @return 整型
	 */
	public static int convertSizeToKB(long size) {
		if (size > 1024L) {
			size = size / 1024L;
		}
		return (int) size;
	}

	/**
	 * 持续时间转换成HH:mm格式字符串
	 * 
	 * @param duration
	 *            （毫秒数）
	 * @return
	 */
	public static String convertDuration(long duration) {
		// return String.valueOf(duration / 60000 + ":" + (duration / 1000) %
		// 60);

		int ss = 1000;
		int mi = ss * 60;
		int hh = mi * 60;

		long hour = duration / hh;
		long minute = (duration - hour * hh) / mi;
		long second = (duration - hour * hh - minute * mi) / ss;

		String hourStr = String.valueOf(hour);
		if (hour < 10) {
			hourStr = "0" + hourStr;
		}
		String minStr = String.valueOf(minute);
		if (minute < 10) {
			minStr = "0" + minStr;
		}
		String secStr = String.valueOf(second);
		if (second < 10) {
			secStr = "0" + secStr;
		}

		if (hour == 0) {
			return minStr + ":" + secStr;
		} else {
			return hourStr + ":" + minStr + ":" + secStr;
		}
	}
	
	/**
	 * 旋转角度转化（0-360）
	 * @return
	 */
	public static float angleConvert(float oldAngle) {
		float newAngle = oldAngle;
		newAngle = newAngle < 0 ? (newAngle + 360) : newAngle;
		newAngle = newAngle > 360 ? (newAngle - 360) : newAngle;
		return newAngle;
	}
}
