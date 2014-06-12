package com.jiubang.ggheart.apps.desks.Preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogTypeId;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingMultiInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarItemInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemListView;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskEditText;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述:桌面设置公共类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-12]
 */
public class DeskSettingConstants {
	/**
	 * 屏幕切换速度 - 默认速度
	 */
	public static final int SCREEN_CHANGE_SPEED_DEFAULT = 60;

	/**
	 * 屏幕切换速度 - 快速速度
	 */
	public static final int SCREEN_CHANGE_SPEED_QUICK = 75;

	/**
	 * 屏幕切换速度 - 平缓速度
	 */
	public static final int SCREEN_CHANGE_SPEED_SMOOTH = 45;

	/**
	 * 屏幕切换速度- 默认类型
	 */
	public static final int SCREEN_CHANGE_SPEED_TYPE_DEFAULT = 1;
	/**
	 * 屏幕切换速度- 快速类型
	 */
	public static final int SCREEN_CHANGE_SPEED_TYPE_QUICK = 2;
	/**
	 * 屏幕切换速度- 平缓类型
	 */
	public static final int SCREEN_CHANGE_SPEED_TYPE_SMOOTH = 3;
	/**
	 * 屏幕切换速度- 自定义类型
	 */
	public static final int SCREEN_CHANGE_SPEED_TYPE_CUSTOM = 4;

	/**
	 * 屏幕切换速度 - 速度最小值
	 */
	public static final int SCREEN_CHANGE_SPEED_MIN = 0;

	/**
	 * 屏幕切换速度 - 速度最大值
	 */
	public static final int SCREEN_CHANGE_SPEED_MAX = 100;

	/**
	 * 屏幕切换速度 - 弹力最小值
	 */
	public static final int SCREEN_CHANGE_SPEED_ELASTIC_MIN = 0;

	/**
	 * 屏幕切换速度 - 弹力最大值
	 */
	public static final int SCREEN_CHANGE_SPEED_ELASTIC_MAX = 100;

	/**
	 * 自定义行列数最大值
	 */
	public static final int ROWS_COLS_MAX_SIZE = 10;

	/**
	 * 自定义行列数最小值
	 */
	public static final int ROWS_COLS_MIN_SIZE = 3;

	public final static String BGSETTINGTAG = "ReadFromSource"; // 标记背景图来源：主题包或文件夹

	/**
	 * <br>功能简述:boolean转int值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param isCheck
	 * @return
	 */
	public static int boolean2Int(boolean isCheck) {
		if (isCheck) {
			return FunAppSetting.ON;
		} else {
			return FunAppSetting.OFF;
		}
	}

	/**
	 * <br>功能简述:int转boolean值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param isCheck
	 * @return
	 */
	public static boolean int2Boolan(int value) {
		if (value == FunAppSetting.ON) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <br>功能简述:获取屏幕速度选择的项
	 * <br>功能详细描述:
	 * <br>注意:可以优化。直接在setting里面处理
	 * @param speed
	 * @param elastic
	 * @return
	 */
	public static int getScreenChangeSpeedType(int speed, int elastic) {
		int value = SCREEN_CHANGE_SPEED_TYPE_CUSTOM;
		if (SCREEN_CHANGE_SPEED_DEFAULT == speed && SCREEN_CHANGE_SPEED_ELASTIC_MIN == elastic) {
			value = SCREEN_CHANGE_SPEED_TYPE_DEFAULT;
		} else if (SCREEN_CHANGE_SPEED_QUICK == speed && SCREEN_CHANGE_SPEED_ELASTIC_MIN == elastic) {
			value = SCREEN_CHANGE_SPEED_TYPE_QUICK;
		} else if (SCREEN_CHANGE_SPEED_SMOOTH == speed
				&& SCREEN_CHANGE_SPEED_ELASTIC_MIN == elastic) {
			value = SCREEN_CHANGE_SPEED_TYPE_SMOOTH;
		}
		return value;
	}

	/**
	 * <br>功能简述:获取屏幕切换的速度大小
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param curValue
	 * @return
	 */
	public static int getScreenChangeSpeedSize(int curValue) {
		int value = 0;
		if (curValue == SCREEN_CHANGE_SPEED_TYPE_DEFAULT) {
			value = SCREEN_CHANGE_SPEED_DEFAULT;
		} else if (curValue == SCREEN_CHANGE_SPEED_TYPE_QUICK) {
			value = SCREEN_CHANGE_SPEED_QUICK;
		} else if (curValue == SCREEN_CHANGE_SPEED_TYPE_SMOOTH) {
			value = SCREEN_CHANGE_SPEED_SMOOTH;
		}
		return value;
	}

	/**
	 * <br>功能简述:获取屏幕切换速度普通的弹力值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static int getScreenChangeSpeedNormalElastic() {
		return SCREEN_CHANGE_SPEED_ELASTIC_MIN;
	}

	/**
	 * <br>功能简述:	添加seekBar到自定义视图中
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param firstSeekBarItemInfo	第一个seekBar
	 * @param secondSeekBarItemInfo 第二个seekBar
	 * @param valueArrayId	值的列表
	 * @param deskSettingItemListView	View
	 */
	public static void setSecondInfoOfSeekBar(Context context,
			ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList, int valueArrayId,
			DeskSettingItemListView deskSettingItemListView) {
		CharSequence[] entries = context.getResources().getTextArray(valueArrayId); //获取多选显示的内容
		int entriesSize = entries.length;

		//seekBarInfo
		DeskSettingSeekBarInfo seekBarInfo = new DeskSettingSeekBarInfo();
		seekBarInfo.setSeekBarItemInfos(seekBarItemInfoList);
		seekBarInfo.setTitle(entries[entriesSize - 1].toString()); //设置标题

		DeskSettingInfo parentDeskSettingInfo = deskSettingItemListView.getDeskSettingInfo();
		DeskSettingInfo customDeskSettingInfo = new DeskSettingInfo();
		customDeskSettingInfo.setSeekBarInfo(seekBarInfo);
		customDeskSettingInfo.setType(DialogTypeId.TYPE_DESK_SETTING_SEEKBAR); //设置seekbar类型
		customDeskSettingInfo.setParentInfo(parentDeskSettingInfo); //设置父类对象为一级菜单

		parentDeskSettingInfo.setSecondInfo(customDeskSettingInfo);
		parentDeskSettingInfo.setCustomPosition(entriesSize - 1); //设置自定义的位置
	}

	/**
	 * <br>功能简述:获取功能表滚屏模式的类型
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param direction	方向
	 * @param loop	是否循环滚动
	 * @return
	 */
	public static int getFunAppScrollType(int direction, int loop) {
		int value = -1;
		switch (direction) {
			case FunAppSetting.SCREEN_SCROLL_HORIZONTAL :
				value = FunAppSetting.SCREEN_SCROLL_HORIZONTAL;
				break;
			case FunAppSetting.SCREEN_SCROLL_VERTICAL :
				value = FunAppSetting.SCREEN_SCROLL_VERTICAL;
				break;	
			default :
				break;
		}
		
		
//		//竖向滚屏
//		int value = FunAppSetting.SCREEN_SCROLL_VERTICAL;
//
//		//竖向循环滚屏
////		if (direction == FunAppSetting.SCREEN_SCROLL_VERTICAL && loop == 1) {
////			value = FunAppSetting.SCREEN_SCROLL_VERTICAL_LOOP;
////		}
//
//		//横向滚屏
//		 if (direction == FunAppSetting.SCREEN_SCROLL_HORIZONTAL) {
//			value = FunAppSetting.SCREEN_SCROLL_HORIZONTAL;
//		}
//
//		//横线循环滚屏
//		else if (direction == FunAppSetting.SCREEN_SCROLL_HORIZONTAL && loop == 1) {
//			value = FunAppSetting.SCREEN_SCROLL_HORIZONTAL_LOOP;
//		}

		return value;
	}

	/**
	 * <br>功能简述:判断是否小屏幕机器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static boolean checkIsSmallScreen() {
		int screenWidth = GoLauncher.getScreenWidth();
		int screenHeight = GoLauncher.getScreenHeight();
		int smallerBound = screenWidth < screenHeight ? screenWidth : screenHeight;
		if (smallerBound <= 240) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * <br>功能简述:整形数组转成字符串数组
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intValue
	 * @return
	 */
	public static String[] intArray2StringArray(int[] value) {
		int valueSize = value.length;
		String[] stringValue = new String[valueSize];
		for (int i = 0; i < valueSize; i++) {
			stringValue[i] = String.valueOf(value[i]);
		}
		return stringValue;
	}

	/**
	 * <br>功能简述:字符串数组转成整形数组
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param intValue
	 * @return
	 */
	public static int[] stringArray2IntArray(String[] value) {
		int valueSize = value.length;
		int[] intValue = new int[valueSize];
		for (int i = 0; i < valueSize; i++) {
			intValue[i] = Integer.parseInt(value[i]);
		}
		return intValue;
	}

	/**
	 * <br>功能简述:获取2级菜多选选择得值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 * @return
	 */
	public static int[] getSecondInfoMultiSelectValue(DeskSettingItemListView view) {
		int[] curSecondValueInt = null;
		if (view.getDeskSettingInfo() != null && view.getDeskSettingInfo().getSecondInfo() != null
				&& view.getDeskSettingInfo().getSecondInfo().getMultiInInfo() != null) {
			//获取2级菜单已勾选的值
			String[] curSecondValueString = view.getDeskSettingInfo().getSecondInfo()
					.getMultiInInfo().getSelectValues();
			if (curSecondValueString != null) {
				curSecondValueInt = stringArray2IntArray(curSecondValueString);
			}
		}
		return curSecondValueInt;
	}

	/**
	 * <br>功能简述:获取2级菜SeekBar队列
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 * @return
	 */
	public static ArrayList<DeskSettingSeekBarItemInfo> getSecondSeekBarItemInfo(
			DeskSettingItemListView view) {
		ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfoList = null;
		if (view != null && view.getDeskSettingInfo() != null
				&& view.getDeskSettingInfo().getSecondInfo() != null
				&& view.getDeskSettingInfo().getSecondInfo().getSeekBarInfo() != null) {

			//自定义类型-调节条队列数据
			seekBarItemInfoList = view.getDeskSettingInfo().getSecondInfo().getSeekBarInfo()
					.getSeekBarItemInfos();
		}
		return seekBarItemInfoList;
	}

	/**
	 * <br>功能简述:功能表横向滚屏特效
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public static void setSecondInfoMulti(int entrisId, int entryValuesId, int cutSize,
			int[] selectValue, int customPosition, DeskSettingItemListView view, Context context) {
		DeskSettingInfo customDeskSettingInfo = new DeskSettingInfo();
		customDeskSettingInfo.setType(DialogTypeId.TYPE_DESK_SETTING_MULTICHOICE); //设置多选类型

		CharSequence[] entries = getSecondEffectsTextArray(entrisId, cutSize, context); //获取多选显示的内容
		CharSequence[] entryValues = getSecondEffectsTextArray(entryValuesId, cutSize, context); //获取多选显示内容的值
		String[] selectValues = DeskSettingConstants.intArray2StringArray(selectValue); //已勾选的值

		DeskSettingInfo parentInfo = view.getDeskSettingInfo();

		DeskSettingMultiInfo multiInfo = new DeskSettingMultiInfo(); //创建多选对象
		multiInfo.setEntries(entries);
		multiInfo.setEntryValues(entryValues);
		multiInfo.setTitle(entries[customPosition].toString()); //设置多选标题
		multiInfo.setSelectValues(selectValues);

		customDeskSettingInfo.setMultiInInfo(multiInfo);
		customDeskSettingInfo.setParentInfo(parentInfo);

		parentInfo.setSecondInfo(customDeskSettingInfo); //设置自定菜单
		parentInfo.setCustomPosition(customPosition); //设置自定义的位置
	}

	public static CharSequence[] getSecondEffectsTextArray(int resId, int cutSize, Context context) {
		CharSequence[] textArray = null;
		CharSequence[] textArrayRes = context.getResources().getTextArray(resId);
		int size = textArrayRes.length;
		if (size > cutSize) {
			textArray = new CharSequence[textArrayRes.length - cutSize]; //-3是为了去掉头部三个默认、随机、自定义
			for (int i = 0; i < textArray.length; i++) {
				textArray[i] = textArrayRes[i + cutSize];
			}
		}
		return textArray;

	}

	public static CharSequence[] getSecondEffectsTextArray(int resId, int[] contentIndex,
			Context context) {
		CharSequence[] textArray = null;
		CharSequence[] textArrayRes = context.getResources().getTextArray(resId);
		try {
			if (contentIndex != null) {
				textArray = new CharSequence[contentIndex.length];
				for (int i = 0; i < contentIndex.length; i++) {
					textArray[i] = textArrayRes[contentIndex[i]];
				}
			}
		} catch (IndexOutOfBoundsException e) {
		}

		return textArray;

	}

	/**
	 * <br>功能简述:设置父类单选框选择得值
	 * <br>功能详细描述:把单选框选择的临时值设置到真实值上去，主要是因为选择自定义选项，需要关闭父类的单选对话框。所以需要用这个方法
	 * <br>注意:要先判断是否存在父类引用。所以需要在设置子类时同事把父类设置上
	 * @param deskSettingInfo
	 */
	public static void setparentInfoSelectValueTemp(DeskSettingInfo deskSettingInfo) {
		DeskSettingInfo parentInfo = deskSettingInfo.getParentInfo();
		if (parentInfo != null) {
			if (parentInfo.getType() == DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE
					|| parentInfo.getType() == DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_WITH_CHECKBOX) {
				DeskSettingSingleInfo singleInfo = parentInfo.getSingleInfo();
				singleInfo.setSelectValue(singleInfo.getSelectValueTemp()); //把单选临时的值设置到真正的选择值里面
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:通过drawableId拿推荐图标图片 <br>
	 * 功能详细描述:可以过滤某些图标进行download tag标签合成图片（tag图片共享一张，减少图片资源） <br>
	 * @param drawableId
	 * @return　经过合成规则处理后的图片
	 */
	public static Drawable getGoEffectsIcons(Context context, int drawableId) {
		Drawable tag = context.getResources().getDrawable(drawableId);

		Drawable drawable = context.getResources().getDrawable(R.drawable.screenedit_icon_bg);
		try {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas cv = new Canvas(bmp);
			ImageUtil.drawImage(cv, drawable, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			ImageUtil.drawImage(cv, tag, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			BitmapDrawable bmd = new BitmapDrawable(bmp);
			bmd.setTargetDensity(context.getResources().getDisplayMetrics());
			drawable = bmd;
		} catch (Throwable e) {
			// 出错则不进行download Tag合成图
		}

		return drawable;
	}

	/**
	 * <br>功能简述:更新一个单选DeskSettingItemListView的值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 * @param value
	 */
	public static void updateSingleChoiceListView(DeskSettingItemListView view, String value) {
		if (view == null || value == null) {
			return;
		}
		int type = view.getDeskSettingInfo().getType();
		if (type == DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE
				|| type == DialogTypeId.TYPE_DESK_SETTING_SINGLECHOICE_FONT) {
			DeskSettingSingleInfo singleInfo = view.getDeskSettingInfo().getSingleInfo();
			singleInfo.setSelectValue(value);
			view.setSummaryText(singleInfo.getEntry());
		}
	}

	/**
	 * <br>功能简述:以指定高度解析指定主题图片
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param pkg
	 * @param resId
	 * @param height
	 * @return
	 */
	public static Bitmap decodeResource(Context context, String pkg, int resId, int height) {
		Bitmap bm = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Resources resources = context.getPackageManager().getResourcesForApplication(pkg);
			if (null != resources) {
				BitmapFactory.decodeResource(resources, resId, options);
			}

			// Calculate inSampleSize  
			options.inSampleSize = calculateInSampleSize(options, 1000, height);

			// Decode bitmap with inSampleSize set  
			options.inJustDecodeBounds = false;
			try {
				bm = BitmapFactory.decodeResource(resources, resId, options);
			} catch (OutOfMemoryError e) {
			} catch (Exception e) {
			}

		} catch (OutOfMemoryError e) {
		} catch (Exception e) {
		}
		return bm;
	}

	/**
	 * <br>功能简述:以指定高度解析指定sd卡图片
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param pkg
	 * @param resId
	 * @param height
	 * @return
	 */
	public static Bitmap decodeFile(String path, int height) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);

		// Calculate inSampleSize  
		options.inSampleSize = calculateInSampleSize(options, 1000, height);

		// Decode bitmap with inSampleSize set  
		options.inJustDecodeBounds = false;
		Bitmap bm = null;
		try {
			bm = BitmapFactory.decodeFile(path, options);
		} catch (OutOfMemoryError e) {
		} catch (Exception e) {
		}
		return bm;
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth,
			int reqHeight) {
		// 图像原始高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	/**
	 * <br>功能简述:根据按钮图片的大小调整调节条的宽度。不然会有空白位置显示。
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param seekBar
	 */
	public static void setSeekBarPadding(SeekBar seekBar, Context context) {
		Drawable thum = context.getResources().getDrawable(
				R.drawable.desk_setting_dialog_seekbar_select);
		int size = (int) (thum.getIntrinsicWidth() / 2);
		seekBar.setThumbOffset(size); //设置按钮的位置。系统默认是8px
		seekBar.setPadding(size, 0, size, 0);
	}

	/**
	 * 
	 * @author huyong
	 * @param src：源文件
	 * @param dst：目标文件
	 * @param encryptbyte：加密字节长度，不需加密，则传入0
	 * @throws IOException
	 */
	public static void copyOutPutFile(File src, File dst, int encryptbyte) throws IOException {
		FileInputStream srcStream = new FileInputStream(src);
		FileOutputStream dstStream = new FileOutputStream(dst);
		FileChannel inChannel = srcStream.getChannel();
		FileChannel outChannel = dstStream.getChannel();
		if (encryptbyte < 0) {
			encryptbyte = 0;
		}
		try {
			inChannel.transferTo(inChannel.size() - encryptbyte, inChannel.size(), outChannel);
			outChannel.transferFrom(inChannel, outChannel.size(), inChannel.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			srcStream.close();
			dstStream.close();
		}
	}

	/**
	 * 
	 * @author huyong
	 * @param src：源文件
	 * @param dst：目标文件
	 * @param encryptbyte：加密字节数，若不需要加密，直接传入0
	 * @throws IOException
	 */
	public static void copyInputFile(File src, File dst, int encryptbyte) throws IOException {
		FileInputStream srcStream = new FileInputStream(src);
		FileOutputStream dstStream = new FileOutputStream(dst);
		FileChannel inChannel = srcStream.getChannel();
		FileChannel outChannel = dstStream.getChannel();

		if (encryptbyte < 0) {
			encryptbyte = 0;
		}
		try {
			inChannel.transferTo(encryptbyte, inChannel.size(), outChannel);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			srcStream.close();
			dstStream.close();
		}
	}

	/**
	 * <br>功能简述:设置单选列表的选择值和更新summary
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param valueString
	 * @param itemListView
	 */
	public static void setSingleInfoValueAndSummary(int value, DeskSettingItemListView itemListView) {
		DeskSettingSingleInfo singleInfo = itemListView.getDeskSettingInfo().getSingleInfo();
		if (singleInfo != null) {
			singleInfo.setSelectValue(String.valueOf(value));
			itemListView.updateSumarryText();
		}
	}
	
	/**
	 * <br>功能简述:遍历所有控件，把DeskView和DeskButton反注册
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public static void selfDestruct(View view) {
		if (null == view) {
			return;
		}

		if (view instanceof DeskTextView) {
			((DeskTextView) view).selfDestruct();
		}
		else if (view instanceof DeskButton) {
			((DeskButton) view).selfDestruct();
		}
		else if (view instanceof DeskEditText) {
			((DeskEditText) view).selfDestruct();
		}
		else if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			int count = group.getChildCount();
			for (int i = 0; i < count; i++) {
				selfDestruct(group.getChildAt(i));
			}
		}
	}

	/**
	 * <br>功能简述:修改文字为桌面字体样式
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param textView
	 */
	public static void setTextViewTypeFace(TextView textView) {
		//获取字体样式
		if (textView == null) {
			return;
		}
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (controler != null) {
			FontBean fontBean = controler.getUsedFontBean();
			if (fontBean != null) {
				textView.setTypeface(fontBean.mFontTypeface, fontBean.mFontStyle);	
			}
		}
	}

}
