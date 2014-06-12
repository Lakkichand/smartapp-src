package com.jiubang.ggheart.apps.desks.ggmenu;

import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;

/**
 * GGMenu的相关配置数据
 * 
 * @author ouyongqiang
 * 
 */
public class GGMenuData {
	// 菜单参数------------------------------------------------------------------------------
	/**
	 * 菜单项的最大列数
	 */
	public static final int GGMENU_MAX_COLOUMNS = 4;

	/**
	 * 菜单项的最大行数
	 */
	public static final int GGMENU_MAX_ROWS = 2;

	/**
	 * 菜单项更多按钮的文字
	 */
	public static final int GGMENU_MORE_ITEM_STRING = R.string.more_menu_item;

	/**
	 * 菜单项返回按钮的文字
	 */
	public static final int GGMENU_PRE_ITEM_STRING = R.string.pre_menu_item;

	/**
	 * 空白项的图片
	 */
	public static final int GGMENU_NULL_IMAGE = R.drawable.menu_null;

	// ID
	/**
	 * screen menu ids 常用tab:添加、屏幕编辑、壁纸、GO精品、主题、桌面设置、特效、系统设置
	 * 周边tab:GO锁屏、GO小部件、通讯统计、语言 更多tab:更新、分享、评分、意见反馈、重新启动、通知栏、锁定编辑
	 */
	public static final int GLMENU_ID_ADD = 100;
	public static final int GLMENU_ID_WALLPAPER = 101;
	public static final int GLMENU_ID_THEME = 102;
	public static final int GLMENU_ID_GOSTORE = 103;
	public static final int GLMENU_ID_SCREENEDIT = 104;
	public static final int GLMENU_ID_PREFERENCE = 105;
	public static final int GLMENU_ID_SYSSETTING = 106;
	public static final int GLMENU_ID_EFFECT = 107;
	public static final int GLMENU_ID_GOLOCKER = 108;
	public static final int GLMENU_ID_GOWIDGET = 109;
	public static final int GLMENU_ID_NOTIFICATION = 110;
	public static final int GLMENU_ID_LANGUAGE = 111;
	public static final int GLMENU_ID_UPDATE = 112;
	public static final int GLMENU_ID_SHARE = 113;
	public static final int GLMENU_ID_RATE = 114;
	public static final int GLMENU_ID_FEEDBACK = 115;
	public static final int GLMENU_ID_RESTART = 116;
	public static final int GLMENU_ID_NOTIFICATIONBAR = 117;
	public static final int GLMENU_ID_LOCKEDIT = 118;
	public static final int GLMENU_ID_UNLOCKEDIT = 119;
	public static final int GLMENU_ID_MESSAGE = 120;
	public static final int GLMENU_ID_GOBACKUP = 121;
	public static final int GLMENU_ID_GOHDLAUNCHER = 122;
	public static final int GLMENU_ID_APPCENTER = 130;
	public static final int GLMENU_ID_GAMEZONE = 131;
	public static final int GLMENU_ID_ONE_X_GUIDE = 123; // ONE X/S型号特别版本引导
	public static final int GLMENU_ID_GOHANDBOOK = 124; // GO手册
	public static final int GLMENU_ID_SNAPSHOT = 125;
	public static final int GLMENU_ID_MEDIA_MANAGEMENT_PLUGIN = 126;
	public static final int GLMENU_ID_FACEBOOK_LIKE_US = 127;
	
	// app drawer
	public static final int GGMENU_ID_SORT = 200;
	public static final int GGMENU_ID_NEW_FOLDER = 201;
	public static final int GGMENU_ID_HIDE_APP = 202;
	public static final int GGMENU_ID_APPDRAWER_SETTING = 203;
	public static final int GGMENU_ID_APPDRAWER_LOCK = 300;
	public static final int GGMENU_ID_APPMGR_APP_CENTER = 204; // 应用分发平台：应用中心
	public static final int GGMENU_ID_APPMGR_GAME_CENTER = 205; // 应用分发平台：游戏中心
	/**
	 * @author 
	 * 
	 */
	public static class TabData {
		/**
		 * tab名称
		 */
		private String mName;

		/**
		 * 菜单项id数组
		 */
		private int[] mIds;

		/**
		 * 菜单项名字数组
		 */
		private int[] mTexts;

		/**
		 * 菜单项图片数组
		 */
		private Drawable[] mDrawables;

		public TabData(String name, int[] ids, int[] textids, Drawable[] drawables) {
			mName = name;
			mIds = ids;
			mTexts = textids;
			mDrawables = drawables;
		}

		public String getName() {
			return mName;
		}

		public int[] getIds() {
			return mIds;
		}

		public int[] getTextids() {
			return mTexts;
		}

		public Drawable[] getDrawables() {
			return mDrawables;
		}
	}
}
