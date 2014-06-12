/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;

import com.gau.go.launcherex.R;
import com.go.launcher.cropimage.CropImageActivity;
import com.go.util.device.Machine;
import com.go.util.window.WindowControl;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * @author ruxueqin 自定义图标工具类
 */
public class CustomIconUtil {
	public static final String DST_FOLDER_PATH = LauncherEnv.Path.SDCARD
			+ LauncherEnv.Path.SPECIAL_ICON_PATH;

	/**
	 * 定义是什么操作请求裁剪
	 */
	public static final int SCREEN_ICON = 1;
	public static final int DOCK_ICON = 2;
	public static final int DOCK_BG = 3;

	/**
	 * DOCK得到自定义图标文件路径
	 * 
	 * @return 文件路径
	 */
	public static String getFilePathInDock() {
		// String string1 = "/sdcard/ma/dock_";
		String string1 = LauncherEnv.Path.SDCARD + LauncherEnv.Path.SPECIAL_ICON_PATH
				+ LauncherEnv.Path.DOCK_FOLDER + "/";
		// dock3.0注释
		String string2 = String.valueOf(System.currentTimeMillis());
		// end dock3.0
		String string3 = ".png";
		String pathString = string1 + string2 + string3;
		return pathString;
	}

	/**
	 * SCREENFRAME得到自定义图标文件路径
	 * 
	 * @return 文件路径
	 */
	public static String getFilePathInScreenFrame() {
		// String string1 = "/sdcard/ma/screen_";
		String path = LauncherEnv.Path.SDCARD + LauncherEnv.Path.SPECIAL_ICON_PATH
				+ LauncherEnv.Path.SCREEN_FOLDER + "/";
		long current = System.currentTimeMillis();
		String png = String.valueOf(current) + ".png";
		String pathString = path + png;
		return pathString;
	}

	public static String getCropFilePath(String cropImageAction) {
		if (cropImageAction != null) {
			Uri uri = Uri.parse(cropImageAction);
			return uri.getPath();
		}
		return null;
	}

	/**
	 * 裁剪图片
	 * 
	 * @param intent
	 *            相机传入数据
	 * @param frameid
	 *            来自哪一个层请求
	 * @return
	 */
	public static Intent getCropImageIntent(Context context, Intent intent, int frameid) {
		if (context == null || intent == null) {
			return null;
		}

		// 获取数据
		Uri srcUri = intent.getData();
		Uri dstUri = null;

		int size = (int) context.getResources().getDimension(android.R.dimen.app_icon_size);
		if (Machine.isLephone()) {
			size = Machine.LEPHONE_ICON_SIZE;
		}
		int width = 0;
		int height = size;
		float proportionWH = 0;
		switch (frameid) {
			case SCREEN_ICON :
				// 检测目标文件夹
				// newFolder(DST_FOLDER_PATH + LocalPath.SCREEN_FOLDER);
				final String path = getFilePathInScreenFrame();
				dstUri = Uri.parse("file://" + path);
				width = size;
				proportionWH = 1;
				break;

			case DOCK_ICON :
				// 检测目标文件夹
				// newFolder(DST_FOLDER_PATH + LocalPath.DOCK_FOLDER);

				String dockPath = getFilePathInDock();
				dstUri = Uri.parse("file://" + dockPath);
				// width = DockConstant.getIconSize();
				width = size;
				height = width;
				proportionWH = 1;
				break;

			case DOCK_BG :
				try {
					String saveFileString = DockLogicControler.getDockBgSaveFilePath(GOLauncherApp
							.getThemeManager().getCurThemePackage());
					dstUri = Uri.parse(saveFileString);
				} catch (Exception e) {
					// 防空指针
					e.printStackTrace();
				}
				width = getDockBgWidth(context);
				// final int bgId = GoLauncher.isLargeIcon() ?
				// R.dimen.dock_bg_large_height : R.dimen.dock_bg_height;
				/**
				 * 如果是按大图标的高度截图，那么转回小图标时，由于没有限制Dock高度，会超出很大范围，所以统一截图小模式 TODO
				 * 如果用户不满意，后期可以改为保存两张不同尺寸的图片
				 */
				height = context.getResources().getDimensionPixelSize(R.dimen.dock_bg_height);
				proportionWH = (float) width / (float) height;
				break;

			default :
				break;
		}

		Intent cropIntent = new Intent(context, CropImageActivity.class);
		cropIntent.setData(srcUri); // 源图片的Uri
		cropIntent.putExtra("output", dstUri); // 编辑后图片输出路径的Uri
		cropIntent.putExtra("outputFormat", "PNG"); // 编辑后图片的保存格式，默认是75%质量的jpg
		cropIntent.putExtra("scale", true); // 支持缩放，默认为true
		cropIntent.putExtra("aspectX", (int) proportionWH); // 裁剪框的纵横比
		cropIntent.putExtra("aspectY", 1); // x:y
		cropIntent.putExtra("outputX", width); // 保存的图片的宽度
		cropIntent.putExtra("outputY", height); // 保存的图片的高度，最好满足裁剪框的纵横比

		cropIntent.putExtra("arrowHorizontal", R.drawable.camera_crop_width); // 水平方向的箭头的资源id，非必需
		cropIntent.putExtra("arrowVertical", R.drawable.camera_crop_height); // 垂直方向的箭头的资源id，非必需
		return cropIntent;
	}

	private static int getDockBgWidth(Context context) {
		int width = 0;
		Rect rect = WindowControl.getDisplayRect(context);
		width = (WindowControl.isPortrait(context)) ? rect.right : rect.bottom;
		return width;
	}
}
