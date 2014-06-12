package com.jiubang.ggheart.data.theme.bean;

import java.util.HashMap;

import com.jiubang.ggheart.data.theme.ThemeManager;

/**
 * 功能表主题Bean基类
 * @author yangguanxiang
 *
 */
public abstract class AppFuncBaseThemeBean extends ThemeBean {
	public final static String ALLAPPS_TAB_NAME = "AllApps";
	public final static String RECENTAPPS_TAB_NAME = "RecentApps";
	public final static String PROCESS_TAB_NAME = "Process";

	public AbsWallpaperBean mWallpaperBean;
	public AbsFoldericonBean mFoldericonBean;
	public AbsFolderBean mFolderBean;
	public AbsAllTabsBean mAllTabsBean;
	public HashMap<String, AbsTabIconBean> mTabIconBeanMap;
	public AbsTabBean mTabBean;
	public AbsTabTitleBean mTabTitleBean;
	public AbsHomeBean mHomeBean;
	public AbsAllAppMenuBean mAllAppMenuBean;
	public AbsAllAppDockBean mAllAppDockBean;
	public AbsRecentDockBean mRecentDockBean;
	public AbsRuningDockBean mRuningDockBean;

	public AbsMoveToDeskBean mMoveToDeskBean;
	public AbsClearHistoryBean mClearHistoryBean;
	public AbsCloseRunningBean mCloseRunningBean;
	public AbsIndicatorBean mIndicatorBean;
	public AbsAppIconBean mAppIconBean;
	public AbsAppSettingBean mAppSettingBean;

	public AbsSwitchMenuBean mSwitchMenuBean;
	//	public CommonEditActionBarBean mCommonEditActionBarBean;
	public AbsSwitchButtonBean mSwitchButtonBean;

	public AppFuncBaseThemeBean() {
		this(ThemeManager.DEFAULT_THEME_PACKAGE);
	}

	public AppFuncBaseThemeBean(String pkgName) {
		super(pkgName);
	}

	/**
	 * 对应XML的Wallpaper的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsWallpaperBean {
		/**
		 * 墙纸
		 */
		public String mImagePath;
		/**
		 * 墙纸背景颜色及透明度
		 */
		public int mBackgroudColor;

		public AbsWallpaperBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 对应XML的Foldericon的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsFoldericonBean {

		public String mPackageName;
		/**
		 * 文件夹缩略图底图
		 */
		public String mFolderIconBottomPath;
		/**
		 * 文件夹缩略图顶罩(打开)
		 */
		public String mFolderIconTopOpenPath;
		/**
		 * 文件夹缩略图顶罩(关闭)
		 */
		public String mFolderIconTopClosedPath;

		public AbsFoldericonBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 对应XML的Folder的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsFolderBean {
		/**
		 * 文件夹展开时的背景图
		 */
		public String mFolderBgPath;

		/**
		 * 文件夹展开时的编辑框背景图
		 */
		public String mFolderEditBgPath;
		/**
		 * 文件夹编辑框文字颜色
		 */
		public int mFolderEditTextColor;

		/**
		 * 文件夹收起按钮
		 */
		// public String mFolderUpButtonPath;
		/**
		 * 文件夹收起按钮选中
		 */
		// public String mFolderUpButtonSelectedPath;
		/**
		 * 文件夹展开时的背景图绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mFolderBgDrawingWay;
		/**
		 * 文件夹内图标与文件夹名称分割线开关
		 * <p>
		 * 0：关闭 1：打开
		 * </p>
		 */
		public byte mFolderLineEnabled;
		/**
		 * 文件夹打开时功能表覆盖的一层颜色
		 */
		public int mFolderOpenBgColor;
		/**
		 * 文件夹背景图需要下拉的高度
		 */
		public int mImageBottomH;

		/**
		 * 添加按钮
		 */
		public String mFolderAddButton;

		/**
		 * 添加按钮高亮
		 */
		public String mFolderAddButtonLight;
		/**
		 * 排序按钮
		 */
		public String mFolderSortButton;

		/**
		 * 排序按钮高亮
		 */
		public String mFolderSortButtonLight;

		public AbsFolderBean() {
			init();
		}
		protected abstract void init();
	}

	/**
	 * 对应XML的AllTabs的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsAllTabsBean {
		/**
		 * 整个Tab栏背景最底层图片(竖屏)
		 */
		public String mAllTabsBgBottomVerPath;
		/**
		 * 整个Tab栏背景最底层图片(横屏)
		 */
		public String mAllTabsBgBottomHorPath;
		/**
		 * 整个Tab栏背景最底层图片的绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mAllTabsBgDrawingWay;

		public AbsAllTabsBean() {
			init();
		}
		protected abstract void init();
	}

	/**
	 * 对应XML的Tab的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsTabBean {
		/**
		 * 单个Tab背景最底层图片(竖屏)
		 */
		public String mTabBgBottomVerPath;
		/**
		 * 单个Tab背景最底层图片(横屏)
		 */
		public String mTabBgBottomHorPath;
		/**
		 * 单个Tab背景最底层图片的绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mTabBgDrawingWay;
		/**
		 * 单个Tab选中时的底层图片(竖屏)
		 */
		public String mTabSelectedBottomVerPath;
		/**
		 * 单个Tab选中时的底层图片(横屏)
		 */
		public String mTabSelectedBottomHorPath;
		/**
		 * 单个Tab选中时的图片绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mTabSelectedDrawingWay;

		/**
		 * 单个Tab点击和聚焦时的底层图片(竖屏)
		 */
		public String mTabFocusedBottomVerPath;
		/**
		 * 单个Tab点击和聚焦时的底层图片(横屏)
		 */
		public String mTabFocusedBottomHorPath;
		/**
		 * 单个Tab点击和聚焦时的图片绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mTabFocusedDrawingWay;
		/**
		 * Tab栏分割线开关
		 * <p>
		 * 0：关闭 1：打开
		 * </p>
		 */
		public int mTabCutLineEnabled;
		/**
		 * Tab屏幕自适应开关
		 * <p>
		 * 0：关闭 1：打开
		 * </p>
		 */
		public int mTabOrientationEnabled;

		public AbsTabBean() {
			init();
		}
		protected abstract void init();
	}

	/**
	 * 对应XML的TabIcon的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsTabIconBean {
		/**
		 * TabIcon的名字
		 */
		public String name;
		/**
		 * TabIcon默认时图标
		 */
		public String mTabIconUnSelected;
		/**
		 * TabIcon选中和聚焦时图标
		 */
		public String mTabIconSelected;
		/**
		 * 当前Tab的TabIcon图标
		 */
		public String mTabIconCurrent;

		public AbsTabIconBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 对应XML的TabTitle的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsTabTitleBean {
		/**
		 * Tab未选中时的字体颜色
		 */
		public int mTabTitleColorUnSelected;
		/**
		 * Tab选中时的字体颜色
		 */
		public int mTabTitleColorSelected;
		/**
		 * Tab字体与底部的间距(竖屏)
		 */
		public int mTabTitleGapVer;
		/**
		 * Tab字体与底部的间距(横屏)
		 */
		public int mTabTitleGapHor;

		public AbsTabTitleBean() {
			init();
		}
		protected abstract void init();
	}

	/**
	 * 对应XML的Home的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsHomeBean {
		/**
		 * TabIcon未选中时图标
		 */
		public String mHomeUnSelected;
		/**
		 * TabIcon选中时图标
		 */
		public String mHomeSelected;
		/**
		 * Home键背景图片(竖屏)
		 */
		public String mHomeBgVerPath;
		/**
		 * Home键背景图片(横屏)
		 */
		public String mHomeBgHorPath;
		/**
		 * Home键背景图片绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mHomeBgDrawingWay;
		/**
		 * Home键背景填充颜色
		 */
		public int mHomeBgColor;
		/**
		 * 自定义tab栏背景主题包名
		 */
		public String mTabHomeBgPackage;
		/**
		 * home栏竖直分隔线(竖屏)
		 */
		public String mHomeDeliverLineV;

		/**
		 * home栏竖直分隔线(竖屏)
		 */
		public String mHomeDeliverLineH;

		public AbsHomeBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 对应XML的AllAppDock的Tag
	 * 
	 */
	public abstract class AbsAllAppDockBean {

		/**
		 * 我的应用为选中
		 */
		public String mHomeMyApp;
		/**
		 * 我的应用选中
		 */
		public String mHomeMyAppLight;
		/**
		 * home栏搜索键未选中时的背景图
		 */
		public String mHomeSearch;
		/**
		 * home栏搜索键选中时的背景图
		 */
		public String mHomeSearchSelected;
		/**
		 * home栏菜单按钮未选中背景图
		 */
		public String mHomeMenu;
		/**
		 * home栏菜单按钮选中背景图
		 */
		public String mHomeMenuSelected;

		/**
		 * home栏切换按钮选中背景图
		 */
		public String mSwitcher;

		/**
		 * home栏切换按钮选中背景图
		 */
		public String mSwitcherSelected;

		/**
		 * home栏返回按钮选中背景图
		 */
		public String mGoBack;

		/**
		 * home栏返回按钮选中背景图
		 */
		public String mGoBackSelected;

		public AbsAllAppDockBean() {
			init();
		}
		protected abstract void init();
	}

	/**
	 * 对应XML的AllAppMenu的Tag
	 * 
	 */
	public abstract class AbsAllAppMenuBean {

		/**
		 * 背景(竖屏)
		 */
		public String mMenuBgV;
		/**
		 * 背景(横屏)
		 */
		public String mMenuBgH;
		/**
		 * 分割线（竖屏）
		 */
		public String mMenuDividerV;
		/**
		 * 分割线（横屏）
		 */
		public String mMenuDividerH;
		/**
		 * 字体颜色
		 */
		public int mMenuTextColor;
		/**
		 * 菜单项被选中后的背景图
		 */
		public String mMenuItemSelected;

		public AbsAllAppMenuBean() {
			init();
		}
		protected abstract void init();
	}

	/**
	 * 对应XML的RecentDock的Tag
	 * 
	 */
	public abstract class AbsRecentDockBean {
		/**
		 * home栏最近打开清除按钮未选中背景图
		 */
		public String mHomeRecentClear;
		/**
		 * home栏最近打开清除按钮选中背景图
		 */
		public String mHomeRecentClearSelected;
		/**
		 * home栏最近打开没有数据时的背景图
		 */
		public String mHomeRecentNoDataBg;
		/**
		 * home栏最近打开没有数据时文字颜色
		 */
		public int mHomeRecentNoDataTextColor;

		public AbsRecentDockBean() {
			init();
		}
		protected abstract void init();
	}

	/**
	 * 对应XML的RuningDock的Tag
	 * 
	 */
	public abstract class AbsRuningDockBean {
		/**
		 * home栏正在运行内存条背景图
		 */
		public String mHomeMemoryBg;
		/**
		 * home栏正在运行低内存进度条图片
		 */
		public String mHomeMemoryProcessLow;
		/**
		 * home栏正在运行中内存进度条图片
		 */
		public String mHomeMemoryProcessMiddle;
		/**
		 * home栏正在运行高内存进度条图片
		 */
		public String mHomeMemoryProcessHigh;
		/**
		 * home栏正在运行清除按钮为选中背景图
		 */
		public String mHomeCleanNormal;
		/**
		 * home栏正在运行清除按钮选中背景图
		 */
		public String mHomeCleanLight;
		/**
		 * home栏正在运行锁定所有按钮未选中背景图
		 */
		public String mHomeLockListNormal;
		/**
		 * home栏正在运行锁定所有按钮选中背景图
		 */
		public String mHomeLockListLight;
		/**
		 * home栏正在运行信息图标
		 */
		public String mHomeRunningInfoImg;
		/**
		 * home栏正在运行锁定单个程序图标
		 */
		public String mHomeRunningLockImg;
		/**
		 * home栏正在运行编辑条背景图片(竖屏)
		 */
		public String mHomeEditDockBgV;
		/**
		 * home栏正在运行编辑条背景图片（横屏）
		 */
		public String mHomeEditDockBgH;
		/**
		 * home栏正在运行编辑条被触摸时背景图片(竖屏)
		 */
		public String mHomeEditDockTouchBgV;
		/**
		 * home栏正在运行编辑条被触摸时背景图片（横屏）
		 */
		public String mHomeEditDockTouchBgH;

		/**
		 * home栏正在运行解锁单个程序图标
		 */
		public String mHomeRunningUnLockImg;
		/**
		 * home栏分割线（竖屏）
		 */
		public String mHomeLineImgV;
		/**
		 * home栏分割线（横屏）
		 */
		public String mHomeLineImgH;
		/**
		 * home栏内存条文字显示颜色
		 */
		public int mHomeTextColor;

		public AbsRuningDockBean() {
			init();
		}
		protected abstract void init();
	}

	/**
	 * 对应XML的MoveToDesk的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsMoveToDeskBean {
		/**
		 * 移动到桌面按钮背景底层图片(竖屏)
		 */
		public String mMoveToDeskBgBottomVerPath;
		/**
		 * 移动到桌面按钮背景底层图片(横屏)
		 */
		public String mMoveToDeskBgBottomHorPath;
		/**
		 * 移动到桌面背景图片绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mMoveToDeskBgDrawingWay;
		/**
		 * 移动到桌面字体颜色
		 */
		public int mMoveToDeskBgColor;

		public AbsMoveToDeskBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 对应XML的ClearHistory的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsClearHistoryBean {
		/**
		 * 最近打开清除历史记录按钮选择底层图片(竖屏)
		 */
		public String mClearHistoryBottomSelectedVerPath;
		/**
		 * 最近打开清除历史记录按钮未选择底层图片(竖屏)
		 */
		public String mClearHistoryBottomUnselectedVerPath;
		/**
		 * 最近打开清除历史记录按钮选择底层图片(横屏)
		 */
		public String mClearHistoryBottomSelectedHorPath;
		/**
		 * 最近打开清除历史记录按钮未选择底层图片(横屏)
		 */
		public String mClearHistoryBottomUnselectedHorPath;
		/**
		 * 移动到桌面背景图片选择绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mClearHistorySelectedDrawingWay;
		/**
		 * 移动到桌面背景图片未选择绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mClearHistoryUnselectedDrawingWay;
		/**
		 * 最近打开清除历史记录按钮填充颜色
		 */
		public int mClearHistoryTextColor;

		public AbsClearHistoryBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 对应XML的CloseRunning的Tag
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsCloseRunningBean {
		/**
		 * 正在运行关闭所有程序选择底层图片(竖屏)路径
		 */
		public String mCloseRunningBottomSelectVerPath;
		/**
		 * 正在运行关闭所有程序未选择底层图片(竖屏)路径
		 */
		public String mCloseRunningBottomUnselectVerPath;
		/**
		 * 正在运行关闭所有程序选择底层图片(横屏)路径
		 */
		public String mCloseRunningBottomSelectHorPath;
		/**
		 * 正在运行关闭所有程序未选择底层图片(横屏)路径
		 */
		public String mCloseRunningBottomUnselectHorPath;
		/**
		 * 正在运行关闭所有程序选择底层图片绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mCloseRunningSelectDrawingWay;
		/**
		 * 正在运行关闭所有程序未选择底层图片绘制方式
		 * <p>
		 * 0：平铺 1：拉伸 2：居中
		 * </p>
		 */
		public byte mCloseRunningUnselectDrawingWay;
		/**
		 * 正在运行关闭所有程序填充颜色
		 */
		public int mCloseRunningTextColor;

		public AbsCloseRunningBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 滚动条指示器
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsIndicatorBean {
		/**
		 * 横向指示器当前屏图片路径
		 */
		public String indicatorCurrentHor;
		/**
		 * 横向指示器图片路径
		 */
		public String indicatorHor;

		public AbsIndicatorBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 应用程序
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsAppIconBean {
		/**
		 * 应用程序文本名称的颜色
		 */
		public int mTextColor;
		/**
		 * 应用程序聚焦时的背景颜色
		 */
		public int mIconBgColor;
		/**
		 * 应用程序卸载图标
		 */
		public String mDeletApp;
		/**
		 * 应用程序卸载高亮图标
		 */
		public String mDeletHighlightApp;
		/**
		 * 文件夹编辑图标
		 */
		public String mEditFolder;
		/**
		 * 文件夹编辑高亮图标
		 */
		public String mEditHighlightFolder;

		/**
		 * 新安装程序
		 */
		public String mNewApp;
		/**
		 * 可更新
		 */
		public String mUpdateIcon;
		/**
		 * 锁定
		 */
		public String mLockApp;

		/**
		 * 停止
		 */
		public String mKillApp;
		/**
		 * 停止高亮
		 */
		public String mKillAppLight;

		public AbsAppIconBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 跟主题相关的配置
	 * 
	 * @author wenjiaming
	 * 
	 */
	public abstract class AbsAppSettingBean {
		/**
		 * 行列数
		 */
		public int mGridFormat;

		public AbsAppSettingBean() {
			init();
		}

		protected abstract void init();
	}

	/**
	 * 
	 * 主题Bean
	 */
	public abstract class AbsSwitchMenuBean {
		public String mPackageName;
		/**
		 * 背景(竖屏)
		 */
		public String mMenuBgV;
		/**
		 * 背景(横屏)
		 */
		public String mMenuBgH;
		/**
		 * 分割线（竖屏）
		 */
		public String mMenuDividerV;
		/**
		 * 分割线（横屏）
		 */
		public String mMenuDividerH;
		/**
		 * 字体颜色
		 */
		public int mMenuTextColor;
		/**
		 * 菜单项被选中后的背景图
		 */
		public String mMenuItemSelected;
		public String[] mItemLabelAppSelectors;
		public String[] mItemLabelImageSelectors;
		public String[] mItemLabelAudioSelectors;
		public String[] mItemLabelVedioSelectors;
		public String[] mItemLabelSearchSelectors;

		public AbsSwitchMenuBean() {
			init();
		}

		protected abstract void init();
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
	public abstract class AbsSwitchButtonBean {
		/**
		 * 图片浏览器图标
		 */
		public String mGalleryIcon;
		/**
		 * 图片浏览器图标高亮
		 */
		public String mGalleryLightIcon;
		/**
		 * 音乐浏览器图标
		 */
		public String mMusicIcon;
		/**
		 * 音乐浏览器图标高亮
		 */
		public String mMusicLightIcon;
		/**
		 * 视频浏览器图标
		 */
		public String mVideoIcon;
		/**
		 * 视频浏览器图标高亮
		 */
		public String mVideoLightIcon;
		/**
		 * 应用浏览器图标
		 */
		public String mAppIcon;
		/**
		 * 应用浏览器图标高亮
		 */
		public String mAppIconLight;
		/**
		 * 功能表搜索图标
		 */
		public String mSearchIcon;
		/**
		 * 功能表搜索图标高亮
		 */
		public String mSearchIconLight;

		public AbsSwitchButtonBean() {
			init();
		}

		protected abstract void init();
	}
}
