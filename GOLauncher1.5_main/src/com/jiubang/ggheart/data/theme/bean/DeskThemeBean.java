package com.jiubang.ggheart.data.theme.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;

import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.ggmenu.GlMenuTabView;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 主题桌面
 * 
 * @author liyuehui
 * 
 */
public class DeskThemeBean extends ThemeBean {

	/*
	 * 版本信息
	 */
	public float mDeskVersion;
	public String mDeskVersionName;

	/**
	 * 壁纸
	 */
	public WallpaperBean mWallpaper;
	/**
	 * 默认是否滚动壁纸
	 */
	public boolean mIsScollWallpaper;
	/**
	 * 通用样式
	 */
	public CommonStylesBean mCommonStyles;
	/**
	 * 屏幕
	 */
	public ScreenBean mScreen;
	/**
	 * 指示器
	 */
	public IndicatorBean mIndicator;
	/**
	 * 预览
	 */
	public PreviewBean mPreview;
	/**
	 * Dock条
	 */
	public DockBean mDock;
	/**
	 * 操作手势
	 */
	public OperationSettingBean mOperationSetting;
	/**
	 * 高级设置
	 */
	public AdvancedSettingBean mAdvancedSetting;

	public MenuBean mDeskMenuBean;
	public MenuBean mAppDrawerMenuBean;
	public MenuBean mProgramMenuBean;

	public PreferenceAppearanceBean mPreferenceAppearanceBean;

	public DeskThemeBean(String pkgName) {
		super(pkgName);
		mBeanType = THEMEBEAN_TYPE_DESK;
		mWallpaper = new WallpaperBean();
		mCommonStyles = new CommonStylesBean();
		mScreen = new ScreenBean();
		mIndicator = new IndicatorBean();
		mPreview = new PreviewBean();
		mDock = new DockBean();
		mOperationSetting = new OperationSettingBean();
		mAdvancedSetting = new AdvancedSettingBean();

		initDefaultTheme();
	}

	public void initDefaultTheme() {
		mWallpaper.mResName = "";
		mScreen.initDefaultTheme();
		mCommonStyles.initDefaultTheme();
		mIndicator.initDefaultTheme();
		mPreview.initDefaultTheme();
		mDock.initDefaultTheme();
	}

	/**
	 * 图片层
	 * 
	 * @author liyuehui
	 */
	public class WallpaperBean {
		public String mResName;
		public Fill mWallpaperFill;
		public int mColor;
		public int mBorder;
		public int mBorderColor;
		public BorderLine mBorderLine;
		public Margins mMargins;

		public Margins mPortMargins;
		public Margins mLandMargins;

		public String mIdentity;

		public WallpaperBean() {
			mColor = 0;
			mBorder = 0;
			mBorderColor = 0;
			mBorderLine = BorderLine.None;
			mMargins = new Margins();
			mWallpaperFill = Fill.None;

			mPortMargins = new Margins();
			mLandMargins = new Margins();
		}
	}

	public WallpaperBean createWallpaperBean() {
		return new WallpaperBean();
	}

	/**
	 * 节点（对界面没什么实质性作用）
	 * 
	 * @author liyuehui
	 * 
	 */
	public class Element extends ThemeBean {
		public String mSource;
	}

	/**
	 * 屏幕样式
	 * 
	 * @author liyuehui
	 * 
	 */
	public class ScreenBean extends Element {
		/**
		 * 屏幕个数
		 */
		// public int mScreenCount;
		/**
		 * 屏幕图标样式
		 */
		public ScreenIconStyle mIconStyle;
		/**
		 * 屏幕文件夹样式
		 */
		public FolderStyle mFolderStyle;
		public Light mLight;
		public Font mFont;
		public TrashStyle mTrashStyle;

		public ScreenBean() {
			mIconStyle = new ScreenIconStyle();
			mFolderStyle = new FolderStyle();
			mLight = new Light();
			mFont = new Font();
			mTrashStyle = new TrashStyle();

		}

		public void initDefaultTheme() {
			mIconStyle.mCellHeightLand = 80;
			mIconStyle.mCellWidthLand = 74;
			// 竖屏
			mIconStyle.mCellHeightPort = 80;
			mIconStyle.mCellWidthPort = 100;

			mTrashStyle.initDefaultTheme();
		}
	}

	public ScreenBean createScreenBean() {
		return new ScreenBean();
	}

	/**
	 * 垃圾桶样式
	 * 
	 * @author liyuehui
	 * 
	 */
	public class TrashStyle {
		/**
		 * 垃圾桶删除时前景色
		 */
		public int mIconForeColor = 0xA5FF0000;
		/**
		 * 垃圾桶删除和不删除的样式
		 */
		public TrashLayer mTrashingLayer;
		public TrashLayer mTrashedLayer;

		public TrashStyle() {
		}

		public void initDefaultTheme() {
		}
	}

	public TrashStyle createTrashStyle() {
		return new TrashStyle();
	}

	/**
	 * 垃圾桶层
	 * 
	 * @author liyuehui
	 * 
	 */
	public class TrashLayer extends ShowItemLayer {
		public String mResImage;
		public boolean mTrashing;
	}

	public TrashLayer createTrashLayer() {
		return new TrashLayer();
	}

	/**
	 * 焦点发光样式
	 * 
	 * @author liyuehui
	 * 
	 */
	public class Light {
		/**
		 * 发光颜色
		 */
		public int mColor;
		/**
		 * 发光图片资源名称
		 */
		public String mResImage;

		public Light() {
			mColor = Color.YELLOW;
		}
	}

	public Light createLight() {
		return new Light();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class Font {
		public String mAppearence;
		public int mColor;
		public int mBGColor;
		public int mSize;

		public Font() {
			mColor = Color.WHITE;
			mBGColor = Color.BLACK;
			mSize = 20;
		}
	}

	public Font createFont() {
		return new Font();
	}

	/**
	 * 屏幕图标样式
	 * 
	 * @author liyuehui
	 */
	public class ScreenIconStyle extends Layer {
		/**
		 * 发光模式
		 */
		public ShowlightMode mShowlightMode;
		/**
		 * 竖屏
		 */
		public int mCellWidthPort;
		/**
		 * 横屏
		 */
		public int mCellWidthLand;
		/**
		 * 竖屏
		 */
		public int mCellHeightPort;
		/**
		 * 横屏
		 */
		public int mCellHeightLand;
		public Valign mTextValign;
		public Halign mTextLalign;
		public Margins mTextMargins;
		public WallpaperBean mTextBackgroud;
		public WallpaperBean mIconBackgroud; // 图标背景

		public ScreenIconStyle() {
			mShowlightMode = ShowlightMode.None;
			// TODO:横竖屏位置信息
			mCellWidthPort = 0;
			mCellWidthLand = 0;
			mCellHeightPort = 0;
			mCellHeightLand = 0;
			mTextValign = Valign.Top;
			mTextLalign = Halign.Center;
			mTextMargins = new Margins();
			mTextBackgroud = new WallpaperBean();
			mIconBackgroud = new WallpaperBean();
			mIconBackgroud.mColor = 0xB2191919;
		}
	}

	public ScreenIconStyle createScreenIconStyle() {
		return new ScreenIconStyle();
	}

	/**
	 * 桌面文件夹样式
	 * 
	 * @author liyuehui
	 * 
	 */
	public class FolderStyle extends ScreenIconStyle {
		public WallpaperBean mBackground;
		public WallpaperBean mOpendFolder;
		public WallpaperBean mClosedFolder;
		public String mPackageName;

		public FolderStyle() {
			mBackground = new WallpaperBean();
			mOpendFolder = new WallpaperBean();
			mClosedFolder = new WallpaperBean();
			mPackageName = LauncherEnv.PACKAGE_NAME;
		}
	}

	public FolderStyle createFolderStyle() {
		return new FolderStyle();
	}

	/**
	 * 指示器样式
	 * 
	 * @author liyuehui
	 * 
	 */
	public class IndicatorBean extends Element {
		/**
		 * 指示器模式
		 */
		public IndicatorShowMode mIndicatorShowMode;
		public int mWhenScreenCount = 5;
		public IndicatorItem mSlide;
		public IndicatorItem mDots;

		public IndicatorBean() {
			mIndicatorShowMode = IndicatorShowMode.None;
		}

		public void initDefaultTheme() {
			// //点状
			// mIndicatorShowMode=IndicatorShowMode.None;
			// IndicatorItem item=new IndicatorItem();
			// item.mSelectedBitmap.mResName="lightbar";
			// item.mUnSelectedBitmap.mResName="normalbar";
			// mIndicatorItems.add(item);
			//
			// //条形
			// mIndicatorShowMode=IndicatorShowMode.None;
			// IndicatorItem item1=new IndicatorItem();
			// item1.mSelectedBitmap.mResName="scrollv";
			// item1.mSelectedBitmap.mWallpaperFill=Fill.Nine;
			// mIndicatorItems.add(item1);
		}
	}

	public IndicatorBean createIndicatorBean() {
		return new IndicatorBean();
	}

	/**
	 * 指示器项样式
	 * 
	 * @author liyuehui
	 * 
	 */
	public class IndicatorItem extends Layer {
		public WallpaperBean mSelectedBitmap;
		public WallpaperBean mUnSelectedBitmap;

		public IndicatorItem() {
			mSelectedBitmap = new WallpaperBean();
			mUnSelectedBitmap = new WallpaperBean();
		}
	}

	public IndicatorItem createIndicatorItem() {
		return new IndicatorItem();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class PreviewBean extends Element {
		public int mLineItemCount;

		public Card mCurrScreen;
		public Card mScreen;
		public Card mAddScreen;
		public Card mFucosScreen;
		public Card mFocusAddScreen;
		public Card mDeleteScreen;

		public WallpaperBean mHome;
		public WallpaperBean mNotHome;
		public WallpaperBean mColsed;
		public WallpaperBean mColsing;

		public PreviewBean() {
			mLineItemCount = 3;
			mCurrScreen = new Card();
			mScreen = new Card();
			mAddScreen = new Card();
			mFucosScreen = new Card();
			mFocusAddScreen = new Card();
			mDeleteScreen = new Card();

			mHome = new WallpaperBean();
			mNotHome = new WallpaperBean();
			mColsed = new WallpaperBean();
			mColsing = new WallpaperBean();
		}

		public void initDefaultTheme() {
			// CurrScreen.InitDefaultTheme();
			// CurrScreen.mBackgroundPort.mResName="preview_border_light";
			// CurrScreen.mBackgroundLand.mResName="preview_border_light_land";
			// Screen.InitDefaultTheme();
			// Screen.mBackgroundPort.mResName="preview_border";
			// Screen.mBackgroundLand.mResName="preview_border_land";
			// AddItem.InitDefaultTheme();
			// Screen.mBackgroundPort.mResName="preview_addscreen";
			// Screen.mBackgroundLand.mResName="preview_addscreen_land";
			//
			// HomeItem.mResName="preview_home_btn_light";
			// NoneHomeItem.mResName="preview_home_btn";
			// Colsed.mResName="preview_del_btn";
			// Colsing.mResName="preview_del_btn_light";
			/*
			 * AddScreenLandResName ="preview_addscreen_land_selector";
			 * ColsedResName ="preview_del_btn_selector"; HomeResName
			 * ="preview_home_btn_selector";
			 */
		}
	}

	public PreviewBean createPreviewBean() {
		return new PreviewBean();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class CardItem extends Layer {
		public WallpaperBean mBackground;
		public WallpaperBean mFore;

		public String mIdentity;

		public CardItem() {
			mBackground = new WallpaperBean();
			mFore = new WallpaperBean();
		}
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class Card {
		public CardItem mItem;

		public String mIdentity;

		public Card() {
			mItem = new CardItem();
		}
	}

	public CardItem createCardItem() {
		return new CardItem();
	}

	public Card creaCard() {
		return new Card();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class OperationSettingBean extends Element {
		public HashMap<String, Boolean> mSettings;
		public List<Response> mResponse;

		public OperationSettingBean() {
			mSettings = new HashMap<String, Boolean>();
			mResponse = new ArrayList<Response>();
		}
	}

	public OperationSettingBean createOperationSettingBean() {
		return new OperationSettingBean();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class Response {
		public String mKey;
		public Intent mGestureIntent;

		public Response(String key) {
			mKey = key;
		}

		public Response(String key, Intent intent) {
			this(key);
			mGestureIntent = intent;
		}

		public Response(String key, String intent) {
			this(key);
			mGestureIntent = new Intent(intent);
		}
	}

	public Response createResponse(String key) {
		return new Response(key);
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class AdvancedSettingBean extends Element {
		public HashMap<String, Boolean> mSettings;

		public AdvancedSettingBean() {
			mSettings = new HashMap<String, Boolean>();
		}
	}

	public AdvancedSettingBean createAdvancedSettingBean() {
		return new AdvancedSettingBean();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class DockBean extends Element {
		public int mColor;
		public int mWidth;
		public int mHeight;
		public int mLineItemCount;
		public DockSettingBean mDockSetting;
		public List<NotifyItem> mNotifys;

		public List<Layer> mIconStyle;
		public List<SystemDefualtItem> mSymtemDefualt;

		public SystemDefualtItem mNoApplicationIcon;
		public SystemDefualtItem mNullIcon;
		public List<ThemeDefualtItem> mThemeDefualt;
		public NotifyStyle mNotifyStyle;

		public DockBean() {
			mWidth = DrawUtils.dip2px(64);
			mHeight = DrawUtils.dip2px(64);
			mLineItemCount = 5;
			mDockSetting = new DockSettingBean();
			mNotifys = new ArrayList<NotifyItem>();
			mIconStyle = new ArrayList<DeskThemeBean.Layer>();
			mSymtemDefualt = new ArrayList<DeskThemeBean.SystemDefualtItem>();
			mNoApplicationIcon = new SystemDefualtItem();
			mNullIcon = new SystemDefualtItem();
			mThemeDefualt = new ArrayList<DeskThemeBean.ThemeDefualtItem>();
			mNotifyStyle = new NotifyStyle();
		}

		public void setWidth(int width) {
			mWidth = width;
			mWidth = DrawUtils.dip2px(mWidth);
		}

		public void setHeight(int height) {
			mHeight = height;
			mHeight = DrawUtils.dip2px(mHeight);
		}

		public void initDefaultTheme() {

		}
	}

	public DockBean createDockBean() {
		return new DockBean();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class NotifyStyle {
		public WallpaperBean mTipImage;
		public Valign mValign;
		public Halign mHalign;
		public Valign mTextValign;
		public Halign mTextHalign;
		public Margins mMargins;

		public NotifyStyle() {
			mTipImage = new WallpaperBean();
			mValign = Valign.Top;
			mHalign = Halign.Right;
			mTextValign = Valign.Top;
			mTextHalign = Halign.Right;
			mMargins = new Margins();
		}
	}

	public NotifyStyle createNotifyStyle() {
		return new NotifyStyle();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class SystemDefualtItem {
		public WallpaperBean mIcon;
		public Intent mGestureIntent;

		public SystemDefualtItem() {
			mIcon = new WallpaperBean();
		}

		public SystemDefualtItem(String intent) {
			this();
			mGestureIntent = new Intent(intent);
		}

		public void setGestureIntent(String intent) {
			mGestureIntent = new Intent(intent);
		}

		public int mIndex;
	}

	public SystemDefualtItem createSystemDefualt() {
		return new SystemDefualtItem();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class ThemeDefualtItem extends SystemDefualtItem {
		public String mIntent;

		public ThemeDefualtItem() {
			super();
		}

		public ThemeDefualtItem(String intent) {
			this();
			mIntent = intent;
		}
	}

	public ThemeDefualtItem createThemeDefualt() {
		return new ThemeDefualtItem();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class NotifyItem {
		public NotifyTypes mNotifyType;
		public String mIntentKeyWord;
		public boolean mOpen;

	}

	public NotifyItem createNotifyItem() {
		return new NotifyItem();
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public class DockSettingBean {
		public int mRowCount;
		public boolean mIsBackground;
		public String mBackground;
		public Fill mBackgroundFill;

		public DockSettingBean() {
			mRowCount = 3;
			mBackgroundFill = Fill.None;

			// 默认主题默认背景
			mIsBackground = true;
			mBackground = "dock";
		}
	}

	public DockSettingBean createDockSettingBean() {
		return new DockSettingBean();
	}

	/**
	 * 程序图标层
	 * 
	 * @author liyuehui
	 * 
	 */
	public class Layer {
		public int mWidth;
		public int mHeight;
		public Valign mValign;
		public Halign mHalign;
		public Margins mMargins;

		public boolean mIsApplicationIcon;

		public Layer() {
			mIsApplicationIcon = true;
			mMargins = new Margins();
			mValign = Valign.Mid;
			mHalign = Halign.Center;
		}

		public void setWidth(int width) {
			mWidth = width;
			mWidth = DrawUtils.dip2px(mWidth);
		}

		public void setHeight(int height) {
			mHeight = height;
			mHeight = DrawUtils.dip2px(mHeight);
		}
	}

	public Layer createLayer() {
		return new Layer();
	}

	/**
	 * 图标以外的层
	 * 
	 * @author liyuehui
	 */
	public class ShowItemLayer extends Layer {
		public WallpaperBean mBackImage;
		public WallpaperBean mForeImage;

		public ShowItemLayer() {
			mIsApplicationIcon = false;
			mBackImage = new WallpaperBean();
			mForeImage = new WallpaperBean();
		}
	}

	public ShowItemLayer createShowItemLayer() {
		return new ShowItemLayer();
	}

	/**
	 * 共用图标样式
	 * 
	 * @author liyuehui
	 */
	public class CommonStylesBean extends ThemeBean {
		public IconStyle mIconStyle;

		public CommonStylesBean() {
			mIconStyle = new IconStyle();
		}

		public void initDefaultTheme() {
			mIconStyle.mIconItems.clear();
		}
	}

	public CommonStylesBean createCommonStylesBean() {
		return new CommonStylesBean();
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-11-19]
	 */
	public class IconStyle {
		public List<Layer> mIconItems;

		public IconStyle() {
			mIconItems = new ArrayList<DeskThemeBean.Layer>();
		}
	}

	public IconStyle createIconStyle() {
		return new IconStyle();
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-11-19]
	 */
	public class Margins {
		public int mLeft = 0;
		public int mTop = 0;
		public int mRight = 0;
		public int mBotton = 0;

		public Margins(String pMargins) {
			if (null == pMargins || pMargins.length() < 4) {
				return;
			}
			pMargins = pMargins.substring(1, pMargins.length() - 1);
			String[] margin = pMargins.split(",");
			if (margin.length >= 1) {
				mLeft = Integer.parseInt(margin[0]);
				mLeft = DrawUtils.dip2px(mLeft);
			}
			if (margin.length >= 2) {
				mTop = Integer.parseInt(margin[1]);
				mTop = DrawUtils.dip2px(mTop);
			}
			if (margin.length >= 3) {
				mRight = Integer.parseInt(margin[2]);
				mRight = DrawUtils.dip2px(mRight);
			}
			if (margin.length >= 4) {
				mBotton = Integer.parseInt(margin[3]);
				mBotton = DrawUtils.dip2px(mBotton);
			}
		}

		public Margins() {
			mLeft = 0;
			mTop = 0;
			mRight = 0;
			mBotton = 0;
		}
	}

	public Margins createMargins(String pMargins) {
		return new Margins(pMargins);
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-11-19]
	 */
	public class MenuBean {
		public String mIdentity;
		public int mRows;
		public int mColumns;
		public int mTextColor;
		public WallpaperBean mBackground;
		public WallpaperBean mItemBackground;
		public MenuItemBean mMoreItem;
		public MenuItemBean mBackItem;
		public ArrayList<MenuItemBean> mItems = new ArrayList<DeskThemeBean.MenuItemBean>();

		// menu2.0新增变量
		public WallpaperBean mItemLineBean;
		public WallpaperBean mUnselectTabLineBean;
		public WallpaperBean mSelectTabLineBean;

		// 消息中心
		public WallpaperBean mNewMessageNotify;

		// tab字体颜色
		public int mTabSelectFontColor;
		public int mTabUnselectFontColor;
		public String mPackageName;
		// highlight text color
		public int mHighLightTextColor;

		public MenuBean() {
			mPackageName = ThemeManager.DEFAULT_THEME_PACKAGE;
			mTabUnselectFontColor = GlMenuTabView.DEFALUTUNSELECTTABCOLOR;
			mTabSelectFontColor = GlMenuTabView.DEFALUTSELECTTABCOLOR;
			mHighLightTextColor = 0xff7ca500;
			mTextColor = 0x00000000;
		}
	}

	public MenuBean createMenuBean() {
		return new MenuBean();
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-11-19]
	 */
	public class MenuItemBean {
		public String mIdentity;
		public int mId;
		public String mName;
		public WallpaperBean mImage;
		public WallpaperBean mHighColorImage;

		public MenuItemBean() {

		}
	}

	public MenuItemBean createMenuItemBean() {
		return new MenuItemBean();
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  rongjinsong
	 * @date  [2012-11-19]
	 */
	public class PreferenceAppearanceBean {
		// Appearance
		public int mTitleColor;
		public int mTitleStyle;
		public WallpaperBean mSeparateLine;
		public WallpaperBean mScroll;
		public WallpaperBean mBackground;
		// Item
		public int mItemTitleColor;
		public int mItemSummaryColor;
		public WallpaperBean mItemBackground;
		// Category
		public int mCategoryColor;
		public WallpaperBean mCategoryBackground;

		public PreferenceAppearanceBean() {

		}
	}

	public PreferenceAppearanceBean createPreferenceAppearanceBean() {
		return new PreferenceAppearanceBean();
	}

	/**
	 * 填充模式
	 * 
	 * @author liyuehui
	 */
	public enum Fill {
		None, Center, Tensile, Tile, Nine
	}

	/**
	 * 发光模式
	 * 
	 * @author liyuehui
	 * 
	 */
	public enum ShowlightMode {
		None, AndroidSytem, Light
	}

	/**
	 * 边框样式
	 * 
	 * @author liyuehui
	 * 
	 */
	public enum BorderLine {
		None,
		/**
		 * 实线
		 */
		Solid,
		/**
		 * 虚线
		 */
		Dotted
	}

	/**
	 * 指示器类型
	 * 
	 * @author liyuehui
	 * 
	 */
	public enum IndicatorShowMode {
		/**
		 * 默认
		 */
		None,
		/**
		 * 点状
		 */
		Point,
		/**
		 * 条状
		 */
		Line
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-19]
 */
	public enum NotifyTypes {
		None, SMS, CALL, GMAIL
	}

	/**
	 * 垂直对齐
	 * 
	 * @author liyuehui
	 * 
	 */
	public enum Valign {
		None, Top, Mid, Botton
	}

	/**
	 * 水平对齐
	 * 
	 * @author liyuehui
	 * 
	 */
	public enum Halign {
		None, Left, Center, Right
	}
}
