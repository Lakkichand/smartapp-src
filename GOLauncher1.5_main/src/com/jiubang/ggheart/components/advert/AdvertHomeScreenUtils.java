package com.jiubang.ggheart.components.advert;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.jiubang.ggheart.components.gohandbook.SharedPreferencesUtil;
import com.jiubang.ggheart.data.info.FavoriteInfo;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ScreenAppWidgetInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;

/**
 * 
 * <br>类描述:首屏广告图标工具类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2013-1-28]
 */
public class AdvertHomeScreenUtils {
	public  static final String MARK_UNDER_LINE = "_";
	public  static final String MARK_DIVIDE = ";";
	
	/**
	 * <br>功能简述:获取首屏当前所有图标信息字符串
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param object
	 * @return
	 */
	public static String getIconInfoString(Object object) {
		if (object == null || !(object instanceof ItemInfo)) {
			return null;
		}
		
		String baseInfoString = getItemInfoString((ItemInfo) object);
		String otherInfoString = "";
		
		if (object instanceof ScreenAppWidgetInfo) {
			otherInfoString = getScreenAppWidgetInfoString((ScreenAppWidgetInfo) object);
		}
		
		else if (object instanceof FavoriteInfo) {
			otherInfoString = getFavoriteInfoString((FavoriteInfo) object);
		}
		
		else if (object instanceof ShortCutInfo) {
			otherInfoString = getShortCutInfoString((ShortCutInfo) object);
		}
		
		else if (object instanceof UserFolderInfo) {
			otherInfoString = getUserFolderInfoString((UserFolderInfo) object);
		}
		
		String infoString = baseInfoString + otherInfoString;
		return infoString;
	}
	
	/**
	 * <br>功能简述:获取widget对应的信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 * @return
	 */
	public static String getScreenAppWidgetInfoString(ScreenAppWidgetInfo info) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(info.mAppWidgetId).append(MARK_DIVIDE);
		return buffer.toString();
	}

	/**
	 * <br>功能简述:获取推荐widget对应的信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 * @return
	 */
	public static String getFavoriteInfoString(FavoriteInfo info) {
		StringBuffer buffer = new StringBuffer();
//		buffer.append(info.mInvalidArea).append(MARK_DIVIDE); //不能用这个。第一次获取是""重启后获取的是null
		buffer.append(MARK_DIVIDE);
		return buffer.toString();
	}

	/**
	 * <br>功能简述:获取app应用的信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 * @return
	 */
	public static String getShortCutInfoString(ShortCutInfo info) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(info.mFeatureIconType).append(MARK_UNDER_LINE); //图片类型。主要判断是否改变图片
		buffer.append(info.mFeatureTitle).append(MARK_UNDER_LINE);
		buffer.append(info.mTitle).append(MARK_UNDER_LINE);
		buffer.append(info.mIntent).append(MARK_DIVIDE);
		return buffer.toString();
	}
	
	
	/**
	 * <br>功能简述:获取文件夹的图标信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 * @return
	 */
	public static String getUserFolderInfoString(UserFolderInfo info) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(info.mFeatureIconType).append(MARK_UNDER_LINE); //图片类型。主要判断是否改变图片
		buffer.append(info.mFeatureTitle).append(MARK_UNDER_LINE);
		buffer.append(info.mTitle).append(MARK_DIVIDE);
		
		return buffer.toString();
	}
	
	/**
	 * <br>功能简述:获取最所有图标最基本的信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 * @return
	 */
	public static String getItemInfoString(ItemInfo info) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(info.mInScreenId).append(MARK_UNDER_LINE);
		buffer.append(info.mScreenIndex).append(MARK_UNDER_LINE);
		buffer.append(info.mItemType).append(MARK_UNDER_LINE);
		buffer.append(info.mCellX).append(MARK_UNDER_LINE);
		buffer.append(info.mCellY).append(MARK_UNDER_LINE);
		buffer.append(info.mSpanX).append(MARK_UNDER_LINE);
		buffer.append(info.mSpanY).append(MARK_UNDER_LINE);
		return buffer.toString();
	}
	
	/**
	 * <br>功能简述:保存首屏图标所有信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param cacheString
	 */
	public static void saveHomeScreenCache(Context context, String cacheString) {
		if (context == null || TextUtils.isEmpty(cacheString)) {
			return;
		}
		
		SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(context);
		preferencesUtil.saveString(AdvertConstants.ADVERT_HOME_SCREEN_CACHE, cacheString);
	}
	
	/**
	 * <br>功能简述:获取首屏图标所有信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param cacheString
	 */
	public static String getHomeScreenCache(Context context) {
		SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(context);
		String cacheString = preferencesUtil.getString(AdvertConstants.ADVERT_HOME_SCREEN_CACHE, "");
		return cacheString;
	}
}
