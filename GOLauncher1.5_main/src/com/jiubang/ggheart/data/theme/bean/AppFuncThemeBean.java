package com.jiubang.ggheart.data.theme.bean;

import java.util.HashMap;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.data.info.AppSettingDefault;
import com.jiubang.ggheart.data.theme.ThemeManager;

/**
 * 功能表主题的Bean
 * 
 * @author wenjiaming
 * 
 */
public class AppFuncThemeBean extends AppFuncBaseThemeBean {

	public AppFuncThemeBean() {
		this(ThemeManager.DEFAULT_THEME_PACKAGE);
	}

	public AppFuncThemeBean(String pkgName) {
		super(pkgName);
		mBeanType = THEMEBEAN_TYPE_FUNCAPP;
		mWallpaperBean = new WallpaperBean();
		mFoldericonBean = new FoldericonBean();
		mFolderBean = new FolderBean();
		mAllTabsBean = new AllTabsBean();
		mTabBean = new TabBean();
		mTabIconBeanMap = new HashMap<String, AbsTabIconBean>();
		mTabTitleBean = new TabTitleBean();
		mHomeBean = new HomeBean();
		mMoveToDeskBean = new MoveToDeskBean();
		mClearHistoryBean = new ClearHistoryBean();
		mCloseRunningBean = new CloseRunningBean();
		mIndicatorBean = new IndicatorBean();
		mAppIconBean = new AppIconBean();
		mAllAppMenuBean = new AllAppMenuBean();
		mAppSettingBean = new AppSettingBean();
		mAllAppDockBean = new AllAppDockBean();
		mRecentDockBean = new RecentDockBean();
		mRuningDockBean = new RuningDockBean();
		mSwitchMenuBean = new SwitchMenuBean();
		//		mCommonEditActionBarBean = new CommonEditActionBarBean();
		mSwitchButtonBean = new SwitchButtonBean();
		// 所有功能Tab
		TabIconBean tabIconBean = new TabIconBean();
		tabIconBean.name = ALLAPPS_TAB_NAME;
		tabIconBean.mTabIconUnSelected = Integer.toString(R.drawable.appfunc_allapp_dark);
		tabIconBean.mTabIconSelected = Integer.toString(R.drawable.appfunc_allapp_light);
		tabIconBean.mTabIconCurrent = AppFuncConstants.NONE;
		mTabIconBeanMap.put(tabIconBean.name, tabIconBean);

		// 最近打开Tab
		tabIconBean = new TabIconBean();
		tabIconBean.name = RECENTAPPS_TAB_NAME;
		tabIconBean.mTabIconUnSelected = Integer.toString(R.drawable.appfunc_recentapp_dark);
		tabIconBean.mTabIconSelected = Integer.toString(R.drawable.appfunc_recentapp_light);
		tabIconBean.mTabIconCurrent = AppFuncConstants.NONE;
		mTabIconBeanMap.put(tabIconBean.name, tabIconBean);

		// 正在运行Tab
		tabIconBean = new TabIconBean();
		tabIconBean.name = PROCESS_TAB_NAME;
		tabIconBean.mTabIconUnSelected = Integer.toString(R.drawable.appfunc_process_dark);
		tabIconBean.mTabIconSelected = Integer.toString(R.drawable.appfunc_process_light);
		tabIconBean.mTabIconCurrent = AppFuncConstants.NONE;
		mTabIconBeanMap.put(tabIconBean.name, tabIconBean);
	}

	/**
	 * 壁纸bean
	 * @author yangguanxiang
	 *
	 */
	public class WallpaperBean extends AbsWallpaperBean {

		@Override
		protected void init() {
			mBackgroudColor = AppFuncConstants.DEFAULT_BG_COLOR;
			mImagePath = "";
		}

	}
	/**
	 * 对应XML的Foldericon的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class FoldericonBean extends AbsFoldericonBean {

		@Override
		protected void init() {
			mPackageName = ThemeManager.DEFAULT_THEME_PACKAGE;
			mFolderIconBottomPath = Integer.toString(R.drawable.appfunc_folder);
			mFolderIconTopOpenPath = Integer.toString(R.drawable.appfunc_folder_open_top);
			mFolderIconTopClosedPath = Integer.toString(R.drawable.appfunc_folder_top);
		}

	}

	/**
	 * 对应XML的Folder的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class FolderBean extends AbsFolderBean {
		@Override
		protected void init() {
			mFolderBgPath = Integer.toString(R.drawable.appfunc_folder_frame);
			mFolderEditBgPath = Integer.toString(R.drawable.appfunc_rename);
			mFolderEditTextColor = 0xFFFFFFFF;
			// mFolderUpButtonPath = Integer.toString(R.drawable.appfunc_up);
			// mFolderUpButtonSelectedPath = Integer
			// .toString(R.drawable.appfunc_up_light);
			mFolderBgDrawingWay = (byte) 1;
			mFolderLineEnabled = (byte) 0;
			mFolderOpenBgColor = 0x50000000;
			// TODO:这个值要dimen规范化
			mImageBottomH = DrawUtils.dip2px(3);
			mFolderAddButton = Integer.toString(R.drawable.appfunc_up);
			mFolderAddButtonLight = Integer.toString(R.drawable.appfunc_up_light);
			mFolderSortButton = Integer.toString(R.drawable.sort);
			mFolderSortButtonLight = Integer.toString(R.drawable.sort_light);
		}
	}

	/**
	 * 对应XML的AllTabs的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class AllTabsBean extends AbsAllTabsBean {

		@Override
		protected void init() {
			mAllTabsBgBottomVerPath = String.valueOf(R.drawable.tab_bg);
			// mAllTabsBgBottomHorPath = String.valueOf(R.drawable.tab_bg);
			mAllTabsBgDrawingWay = (byte) 1;
		}
	}

	/**
	 * 对应XML的Tab的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class TabBean extends AbsTabBean {

		@Override
		protected void init() {
			mTabBgBottomVerPath = "";
			mTabBgBottomHorPath = "";
			mTabBgDrawingWay = (byte) 1;
			mTabSelectedBottomVerPath = "";
			mTabSelectedBottomHorPath = "";
			mTabSelectedDrawingWay = (byte) 1;
			mTabFocusedBottomVerPath = String.valueOf(R.drawable.tab_bg_focused);
			mTabFocusedBottomHorPath = "";
			mTabFocusedDrawingWay = (byte) 1;
			mTabCutLineEnabled = (byte) 0;
			mTabOrientationEnabled = 1;
		}
	}

	/**
	 * 对应XML的TabIcon的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class TabIconBean extends AbsTabIconBean {

		@Override
		protected void init() {

		}

	}

	/**
	 * 对应XML的TabTitle的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class TabTitleBean extends AbsTabTitleBean {

		@Override
		protected void init() {
			mTabTitleColorSelected = 0xFFFFFFFF;
			mTabTitleColorUnSelected = 0xFFBFBFBF;
			mTabTitleGapVer = 7;
			mTabTitleGapHor = 46;
		}
	}

	/**
	 * 对应XML的Home的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class HomeBean extends AbsHomeBean {

		@Override
		protected void init() {
			mHomeSelected = Integer.toString(R.drawable.appfunc_home_light);
			mHomeUnSelected = Integer.toString(R.drawable.appfunc_home);
			mHomeBgVerPath = Integer.toString(R.drawable.shorcut_slaver);
			mHomeBgHorPath = "";
			mHomeBgDrawingWay = (byte) 1;
			mHomeBgColor = 1;
			mTabHomeBgPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
			mHomeDeliverLineV = Integer.toString(R.drawable.appfunc_icon_line);
			mHomeDeliverLineH = Integer.toString(R.drawable.appfunc_icon_line_h);
		}
	}

	/**
	 * 对应XML的AllAppDock的Tag
	 * 
	 */
	public class AllAppDockBean extends AbsAllAppDockBean {

		@Override
		protected void init() {
			mHomeMenu = Integer.toString(R.drawable.appfunc_more);
			mHomeMenuSelected = Integer.toString(R.drawable.appfunc_more_light);
			mSwitcher = Integer.toString(R.drawable.appfunc_switch_button_app);
			mSwitcherSelected = Integer.toString(R.drawable.appfunc_switch_button_app_light);
			mGoBack = Integer.toString(R.drawable.appfunc_switch_button_back);
			mGoBackSelected = Integer.toString(R.drawable.appfunc_switch_button_back_light);
		}
	}

	/**
	 * 对应XML的AllAppMenu的Tag
	 * 
	 */
	public class AllAppMenuBean extends AbsAllAppMenuBean {

		@Override
		protected void init() {
			mMenuBgV = Integer.toString(R.drawable.allapp_menu_bg_vertical);
			mMenuBgH = Integer.toString(R.drawable.allapp_menu_bg_horizontal);
			mMenuDividerV = Integer.toString(R.drawable.allfunc_allapp_menu_line);
			mMenuDividerH = Integer.toString(R.drawable.allfunc_allapp_menu_line);
			mMenuTextColor = 0xff000000;
			mMenuItemSelected = Integer.toString(R.drawable.menu_item_background);
		}
	}

	/**
	 * 对应XML的RecentDock的Tag
	 * 
	 */
	public class RecentDockBean extends AbsRecentDockBean {

		@Override
		protected void init() {
			mHomeRecentClear = Integer.toString(R.drawable.recent_app_clear);
			mHomeRecentClearSelected = Integer.toString(R.drawable.recent_clear_light);
			mHomeRecentNoDataBg = Integer.toString(R.drawable.appfunc_recent_no_data_bg);
			mHomeRecentNoDataTextColor = 0x7fffffff;
		}
	}

	/**
	 * 对应XML的RuningDock的Tag
	 * 
	 */
	public class RuningDockBean extends AbsRuningDockBean {

		@Override
		protected void init() {
			mHomeMemoryBg = Integer.toString(R.drawable.promanage_memory_bg);
			mHomeMemoryProcessLow = Integer.toString(R.drawable.promanage_memory_green);
			mHomeMemoryProcessMiddle = Integer.toString(R.drawable.promanage_memory_orange);
			mHomeMemoryProcessHigh = Integer.toString(R.drawable.promanage_memory_red);
			mHomeCleanNormal = Integer.toString(R.drawable.promanage_clear_normal);
			mHomeCleanLight = Integer.toString(R.drawable.promanage_clear_light);
			mHomeLockListNormal = Integer.toString(R.drawable.promanage_lock_list_normal);
			mHomeLockListLight = Integer.toString(R.drawable.promanage_lock_list_light);
			mHomeRunningInfoImg = Integer.toString(R.drawable.promanage_info_running);
			mHomeRunningLockImg = Integer.toString(R.drawable.promanage_lock_running);
			mHomeEditDockBgV = Integer.toString(R.drawable.promanage_edit_dock_v);
			mHomeEditDockBgH = Integer.toString(R.drawable.promanage_edit_dock_h);
			mHomeEditDockTouchBgV = Integer.toString(R.drawable.promanage_edit_dock_touch_v);
			mHomeEditDockTouchBgH = Integer.toString(R.drawable.promanage_edit_dock_touch_h);
			mHomeRunningUnLockImg = Integer.toString(R.drawable.promanage_unlock_running);
			mHomeLineImgV = Integer.toString(R.drawable.promanage_line_v);
			mHomeLineImgH = Integer.toString(R.drawable.promanage_line_h);
			mHomeTextColor = 0xFFFFFFFF;
		}
	}

	/**
	 * 对应XML的MoveToDesk的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class MoveToDeskBean extends AbsMoveToDeskBean {

		@Override
		protected void init() {
			mMoveToDeskBgBottomVerPath = Integer.toString(R.drawable.appfunc_movetodesk);
			mMoveToDeskBgBottomHorPath = "";
			mMoveToDeskBgDrawingWay = (byte) 1;
			mMoveToDeskBgColor = 0xFFFFFFFF;
		}
	}

	/**
	 * 对应XML的ClearHistory的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class ClearHistoryBean extends AbsClearHistoryBean {

		@Override
		protected void init() {
			mClearHistoryBottomSelectedVerPath = Integer.toString(R.drawable.button_v2);
			mClearHistoryBottomUnselectedVerPath = Integer.toString(R.drawable.button_v);
			mClearHistoryBottomSelectedHorPath = Integer.toString(R.drawable.button_h2);
			mClearHistoryBottomUnselectedHorPath = Integer.toString(R.drawable.button_h);
			mClearHistorySelectedDrawingWay = (byte) 1;
			mClearHistoryUnselectedDrawingWay = (byte) 1;
			mClearHistoryTextColor = 0xFFFFFFFF;
		}
	}

	/**
	 * 对应XML的CloseRunning的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class CloseRunningBean extends AbsCloseRunningBean {

		@Override
		protected void init() {
			mCloseRunningBottomSelectVerPath = Integer.toString(R.drawable.button_v2);
			mCloseRunningBottomUnselectVerPath = Integer.toString(R.drawable.button_v);
			mCloseRunningBottomSelectHorPath = Integer.toString(R.drawable.button_h2);
			mCloseRunningBottomUnselectHorPath = Integer.toString(R.drawable.button_h);
			mCloseRunningSelectDrawingWay = (byte) 1;
			mCloseRunningUnselectDrawingWay = (byte) 1;
			mCloseRunningTextColor = 0xFFFFFFFF;
		}
	}

	/**
	 * 滚动条指示器
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class IndicatorBean extends AbsIndicatorBean {

		@Override
		protected void init() {
			indicatorCurrentHor = Integer.toString(R.drawable.appfunc_lightbar);
			indicatorHor = Integer.toString(R.drawable.appfunc_normalbar);
		}
	}

	/**
	 * 应用程序
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class AppIconBean extends AbsAppIconBean {

		@Override
		protected void init() {
			mTextColor = AppFuncConstants.ICON_TEXT_COLOR;
			mIconBgColor = AppFuncConstants.ICON_BG_FOCUSED;
			mDeletApp = Integer.toString(R.drawable.kill);
			mDeletHighlightApp = Integer.toString(R.drawable.kill_light);
			mNewApp = Integer.toString(R.drawable.new_install_app);
			mUpdateIcon = Integer.toString(R.drawable.appfunc_app_update);
			mLockApp = Integer.toString(R.drawable.promanage_lock_icon);
			mKillApp = Integer.toString(R.drawable.promanage_close_normal);
			mKillAppLight = Integer.toString(R.drawable.promanage_close_light);
			// mEditFolder = Integer.toString(R.drawable.eidt_folder);
			// mEditHighlightFolder =
			// Integer.toString(R.drawable.eidt_folder_light);
		}
	}

	/**
	 * 跟主题相关的配置
	 * 
	 * @author wenjiaming
	 * 
	 */
	public class AppSettingBean extends AbsAppSettingBean {

		@Override
		protected void init() {
			mGridFormat = AppSettingDefault.LINECOLUMNNUM;
		}
	}

	public void initTabHomeBean() {

		mAllTabsBean = new AllTabsBean();
		mTabBean = new TabBean();
		mTabIconBeanMap = new HashMap<String, AbsTabIconBean>();
		mTabTitleBean = new TabTitleBean();
		mHomeBean = new HomeBean();
		mMoveToDeskBean = new MoveToDeskBean();

		// 所有功能Tab
		TabIconBean tabIconBean = new TabIconBean();
		tabIconBean.name = ALLAPPS_TAB_NAME;
		tabIconBean.mTabIconUnSelected = Integer.toString(R.drawable.appfunc_allapp_dark);
		tabIconBean.mTabIconSelected = Integer.toString(R.drawable.appfunc_allapp_light);
		tabIconBean.mTabIconCurrent = AppFuncConstants.NONE;
		mTabIconBeanMap.put(tabIconBean.name, tabIconBean);

		// 最近打开Tab
		tabIconBean = new TabIconBean();
		tabIconBean.name = RECENTAPPS_TAB_NAME;
		tabIconBean.mTabIconUnSelected = Integer.toString(R.drawable.appfunc_recentapp_dark);
		tabIconBean.mTabIconSelected = Integer.toString(R.drawable.appfunc_recentapp_light);
		tabIconBean.mTabIconCurrent = AppFuncConstants.NONE;
		mTabIconBeanMap.put(tabIconBean.name, tabIconBean);

		// 正在运行Tab
		tabIconBean = new TabIconBean();
		tabIconBean.name = PROCESS_TAB_NAME;
		tabIconBean.mTabIconUnSelected = Integer.toString(R.drawable.appfunc_process_dark);
		tabIconBean.mTabIconSelected = Integer.toString(R.drawable.appfunc_process_light);
		tabIconBean.mTabIconCurrent = AppFuncConstants.NONE;
		mTabIconBeanMap.put(tabIconBean.name, tabIconBean);
	}

	public void initFolderThemeBean() {
		if (mFoldericonBean == null) {
			mFoldericonBean = new FoldericonBean();
		} else {
			mFoldericonBean.mFolderIconBottomPath = Integer.toString(R.drawable.appfunc_folder);
			mFoldericonBean.mFolderIconTopOpenPath = Integer
					.toString(R.drawable.appfunc_folder_open_top);
			mFoldericonBean.mFolderIconTopClosedPath = Integer
					.toString(R.drawable.appfunc_folder_top);
		}

		if (mFolderBean == null) {
			mFolderBean = new FolderBean();
		} else {
			mFolderBean.mFolderBgPath = Integer.toString(R.drawable.appfunc_folder_frame);
			mFolderBean.mFolderEditBgPath = Integer.toString(R.drawable.appfunc_rename);
			mFolderBean.mFolderEditTextColor = 0xFFFFFFFF;
			mFolderBean.mFolderAddButton = Integer.toString(R.drawable.appfunc_up);
			mFolderBean.mFolderAddButtonLight = Integer.toString(R.drawable.appfunc_up_light);
			mFolderBean.mFolderBgDrawingWay = (byte) 1;
			mFolderBean.mFolderLineEnabled = (byte) 0;
			mFolderBean.mFolderOpenBgColor = 0x50000000;
			mFolderBean.mImageBottomH = 34;
		}
	}

	/**
	 * 
	 * 主题Bean
	 */
	public class SwitchMenuBean extends AbsSwitchMenuBean {

		@Override
		protected void init() {
			mPackageName = ThemeManager.DEFAULT_THEME_PACKAGE;
			mMenuBgV = Integer.toString(R.drawable.appfunc_switch_menu_bg);
			mMenuBgH = Integer.toString(R.drawable.appfunc_switch_menu_bg_h);
			mMenuDividerV = Integer.toString(R.drawable.allfunc_allapp_menu_line);
			mMenuDividerH = Integer.toString(R.drawable.allfunc_allapp_menu_line);
			mMenuTextColor = 0xff000000;
			mMenuItemSelected = Integer.toString(R.drawable.transparent);

			mItemLabelAppSelectors = new String[] {
					Integer.toString(R.drawable.switch_menu_image_selector),
					Integer.toString(R.drawable.switch_menu_audio_selector),
					Integer.toString(R.drawable.switch_menu_video_selector),
					Integer.toString(R.drawable.switch_menu_search_selector) };

			mItemLabelImageSelectors = new String[] {
					Integer.toString(R.drawable.switch_menu_app_selector),
					Integer.toString(R.drawable.switch_menu_audio_selector),
					Integer.toString(R.drawable.switch_menu_video_selector),
					Integer.toString(R.drawable.switch_menu_search_selector) };

			mItemLabelAudioSelectors = new String[] {
					Integer.toString(R.drawable.switch_menu_app_selector),
					Integer.toString(R.drawable.switch_menu_image_selector),
					Integer.toString(R.drawable.switch_menu_video_selector),
					Integer.toString(R.drawable.switch_menu_search_selector) };
			mItemLabelVedioSelectors = new String[] {
					Integer.toString(R.drawable.switch_menu_app_selector),
					Integer.toString(R.drawable.switch_menu_image_selector),
					Integer.toString(R.drawable.switch_menu_audio_selector),
					Integer.toString(R.drawable.switch_menu_search_selector) };
			mItemLabelSearchSelectors = new String[] {
					Integer.toString(R.drawable.switch_menu_app_selector),
					Integer.toString(R.drawable.switch_menu_image_selector),
					Integer.toString(R.drawable.switch_menu_audio_selector),
					Integer.toString(R.drawable.switch_menu_video_selector) };
		}
	}

	/**
	 * 
	 * 底部编辑操作栏Bean
	 */
	//	public class CommonEditActionBarBean {
	//		public String mSelectAll;
	//		public String mSelectAllPressd;
	//
	//		public String mDeselectAll;
	//		public String mDeselectAllPressd;
	//
	//		public String mDelete;
	//		public String mDeletePressd;
	//
	//		public String mShare;
	//		public String mSharePressd;
	//
	//		public String mDone;
	//		public String mDonePressd;
	//
	//		public String mRemove;
	//		public String mRemovePressd;
	//
	//		public String mAdd;
	//		public String mAddPressd;
	//
	//		public String mHeart;
	//		public String mHeartPressd;
	//
	//		public String mAddto;
	//		public String mAddtoPressd;
	//
	//		public String mRename;
	//		public String mRenamePressd;
	//
	//		public CommonEditActionBarBean() {
	//			mSelectAll = Integer.toString(R.drawable.appfunc_mediamanagement_button_selectall);
	//			mSelectAllPressd = Integer
	//					.toString(R.drawable.appfunc_mediamanagement_button_selectall_light);
	//
	//			mDeselectAll = Integer.toString(R.drawable.appfunc_mediamanagement_button_deselect);
	//			mDeselectAllPressd = Integer
	//					.toString(R.drawable.appfunc_mediamanagement_button_deselect_light);
	//
	//			mDelete = Integer.toString(R.drawable.appfunc_mediamanagement_button_delete);
	//			mDeletePressd = Integer
	//					.toString(R.drawable.appfunc_mediamanagement_button_delete_light);
	//
	//			mShare = Integer.toString(R.drawable.appfunc_mediamanagement_button_share);
	//			mSharePressd = Integer.toString(R.drawable.appfunc_mediamanagement_button_share_light);
	//
	//			mDone = Integer.toString(R.drawable.appfunc_mediamanagement_button_done);
	//			mDonePressd = Integer.toString(R.drawable.appfunc_mediamanagement_button_done_light);
	//
	//			mRemove = Integer.toString(R.drawable.appfunc_mediamanagement_button_remove);
	//			mRemovePressd = Integer
	//					.toString(R.drawable.appfunc_mediamanagement_button_remove_light);
	//			mAdd = Integer.toString(R.drawable.appfunc_mediamanagement_button_add);
	//			mAddPressd = Integer.toString(R.drawable.appfunc_mediamanagement_button_add_light);
	//			mAddto = Integer.toString(R.drawable.appfunc_mediamanagement_button_addto);
	//			mAddtoPressd = Integer.toString(R.drawable.appfunc_mediamanagement_button_addto_light);
	//			mRename = Integer.toString(R.drawable.appfunc_mediamanagement_button_rename);
	//			mRenamePressd = Integer
	//					.toString(R.drawable.appfunc_mediamanagement_button_rename_light);
	//			mHeart = Integer.toString(R.drawable.appfunc_mediamanagement_button_heart);
	//			mHeartPressd = Integer.toString(R.drawable.appfunc_mediamanagement_button_heart_light);
	//
	//		}
	//	}

	/**
	 * 
	 * 选项卡按钮Bean
	 */
	public class SwitchButtonBean extends AbsSwitchButtonBean {

		@Override
		protected void init() {
			mGalleryIcon = Integer.toString(R.drawable.appfunc_switch_button_gallery);
			mGalleryLightIcon = Integer.toString(R.drawable.appfunc_switch_button_gallery_light);
			mMusicIcon = Integer.toString(R.drawable.appfunc_switch_button_music);
			mMusicLightIcon = Integer.toString(R.drawable.appfunc_switch_button_music_light);
			mVideoIcon = Integer.toString(R.drawable.appfunc_switch_button_video);
			mVideoLightIcon = Integer.toString(R.drawable.appfunc_switch_button_video_light);
			mAppIcon = Integer.toString(R.drawable.appfunc_switch_button_app);
			mAppIconLight = Integer.toString(R.drawable.appfunc_switch_button_app_light);
			mSearchIcon = Integer.toString(R.drawable.appfunc_switch_button_search);
			mSearchIconLight = Integer.toString(R.drawable.appfunc_switch_button_search_light);
		}
	}
}
