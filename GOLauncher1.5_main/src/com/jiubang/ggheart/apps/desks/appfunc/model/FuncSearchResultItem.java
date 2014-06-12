package com.jiubang.ggheart.apps.desks.appfunc.model;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.go.util.file.media.FileInfo;
import com.jb.util.pySearch.SearchResultItem;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-10-17]
 */
public class FuncSearchResultItem {

	/**
	 * 本地类型
	 */
	public static final byte ITEM_TYPE_LOCAL_APPS = 0;
	/**
	 * 网络类型
	 */
	public static final byte ITEM_TYPE_APP_CENTER_APPS = 1;
	/**
	 * 结果的数量类型
	 */
	public static final byte ITEM_TYPE_RESULT_HEADER = 2;
	/**
	 * 图片类型
	 */
	public static final byte ITEM_TYPE_LOCAL_IMAGE = 3;
	/**
	 * 音频类型
	 */
	public static final byte ITEM_TYPE_LOCAL_AUDIO = 4;
	/**
	 * 视频类型
	 */
	public static final byte ITEM_TYPE_LOCAL_VIDEO = 5;
	/**
	 * 网络搜索记录
	 */
	public static final byte ITEM_TYPE_WEB_HISTORY = 6;
	/**
	 * 本地搜索记录(程序)
	 */
	public static final byte ITEM_TYPE_LOCAL_HISTORY_APPS = 7;
	/**
	 * 网络搜索记录(媒体文件)
	 */
	public static final byte ITEM_TYPE_LOCAL_HISTORY_MEDIA = 8;
	/**
	 *  热词项
	 */
	public static final byte ITEM_TYPE_WEB_KEY_WORDS = 9;
	
	public static final byte ITEM_TYPE_SEARCH_WEB = 10;
	/**
	 * 本地资源类型才需要这个字段；
	 */
	public FileInfo fileInfo;
	/**
	 * 游戏应用中心搜索返回bean
	 */
	public BoutiqueApp appInfo;
	/**
	 * 应用程序标题
	 */
	public String mTitle;
	/**
	 * 应用程序图标
	 */
	public Drawable mIcon;
	
	public Intent mIntent;
	/**
	 * 搜索数据类型
	 */
	public byte mType = -1;
	/**
	 * 匹配关键字在标题中的索引值
	 */
	public int mMatchIndex = -1;
	/**
	 * 匹配的字符个数
	 */
	public int mMatchWords = 0;
	/**
	 * 是否当前类型的最后一项。
	 */
	public boolean mIsLastItem;
	/**
	 * 排行状况 1：新增2：上升 3：持平 4：下降
	 */
	public int state;
	/**
	 * 排行状况图标
	 */
	public String sicon;
	
	public BoutiqueApp recApp;
	/**
	 * 根据搜索结果设置标题
	 * 
	 * @param title
	 * @param item
	 */
	public void setTitle(String title, SearchResultItem item) {
		if (item != null) {
			mMatchIndex = item.mMatchPos;
			mMatchWords = item.mMatchWords;
		}
		mTitle = title;
	}

	/**
	 * 获取搜索关键字在结果标题中的索引位置，以便按相关性进行排序
	 * 
	 * @author huyong
	 * @return
	 */
	public int getMatchIndex() {
		return mMatchIndex;
	}

}
