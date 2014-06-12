package com.jiubang.ggheart.apps.desks.Preferences.view;

import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.launcher.colorpicker.ColorPickerDialog;
import com.go.util.ConvertUtils;
import com.go.util.SortUtils;
import com.go.util.Utilities;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogTypeId;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSeekBarItemInfo;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.dock.StyleBaseInfo;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DockItemControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.ScreenStyleConfigInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeConfig;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.AppDataThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskFolderThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.parser.TagSet;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>
 * 类描述:个性化设置tab 图标 <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-9-13]
 */
public class DeskSettingVisualIconTabView extends DeskSettingVisualAbsTabView {
	private ColorPickerDialog mColorPickerDialog; // 颜色选择器
	private int mPressColor; // 点击颜色
	private int mFocusColor; // 聚焦颜色
	private boolean mCustomBg; // 是否自定义颜色
	private boolean mIsSave;

	// 设置信息
	private DesktopSettingInfo mDesktopInfo; // 桌面设置
	private ScreenStyleConfigInfo mScreenStyleInfo; // 屏幕风格设置
	private ShortCutSettingInfo mShortcutInfo; // 快捷条设置

	boolean mThemeHasGGmenuRes = false; // 所有主题内，是否包含主题菜单

	// 主题包信息
	String[] mAllThemePackage;
	String[] mAllThemeName;

	// 图标大小类型
	public static final int LARGE_ICON_SIZE = 0x1; // 大图标
	public static final int DEFAULT_ICON_SIZE = 0x2; // 小图标
	public static final int DIY_ICON_SIZE = 0x3; // 自定义图标

	private int mOriginalSize; // 进入此界面时图标的大小，用于判断退出时图标大小是否有改变

	// 图标显示排版
	// 大图标大小
	private final int mLargeIconSize = (int) getResources().getDimension(
			R.dimen.screen_icon_large_size);
	// 小图标大小
	private final int mSmallIconSize = (int) getResources().getDimension(
			R.dimen.screen_icon_size);
	private int mIconSizeMax; // 图标最大值
	private int mIconSizeMin; // 图标最小值
	private boolean mFlag = true; // 用于判断图标大小是否符合存储条件
	private String mIconSizeLoadChoice = null; // 用于判断图标大小是否符合桌面重启条件

	// views
	// 图标图片显示区
	private RelativeLayout mShowIconsFrame; // 图标框
	private ImageView mLargeLine; // 大图标标记线
	private ImageView mSmallLine; // 小图标标记线
	private TextView mMaxIcon; // 最大值标记text
	private TextView mLargeIcon; // 大图标标记text
	private TextView mSmallIcon; // 小图标标记text
	private ImageView mDeskIcon; // 图标
	private ImageView mDockIcon; // dock图标
	private ImageView mFolderIcon; // 文件夹图标
	private TextView mDeskText; // 图标Text
	private TextView mDockText; // dock图标Text
	private TextView mFolderText; // 文件夹图标Text

	// 普通点击view区
	private DeskSettingItemListView mIconSize; // 图标大小
	private DeskSettingItemBaseView mHighLight; // 图标高亮显示
	private DeskSettingItemCheckBoxView mIconBase; // 显示图标底图
	private DeskSettingItemListView mThemeStyleIcon; // 主题图标风格
	private DeskSettingItemListView mDockStyleIcon; // 快捷条风格
	private DeskSettingItemListView mFolderStyleIcon; // 文件夹风格
	private DeskSettingItemListView mMenuStyleIcon; // 菜单风格

	public DeskSettingVisualIconTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mIconSizeMin = (int) (getResources().getDimensionPixelSize(
				R.dimen.screen_icon_size) / 1.5f);
		// 将最大值由配置里的固定值改为根据机器的屏幕宽度动态计算
		final int screenWidth = DrawUtils.sWidthPixels < DrawUtils.sHeightPixels ? DrawUtils.sWidthPixels
				: DrawUtils.sHeightPixels;
		mIconSizeMax = screenWidth > 240 ? screenWidth / 4 : screenWidth / 3;

		super.onFinishInflate();
	}

	public void setInfos(DesktopSettingInfo desktopSettingInfo,
			ShortCutSettingInfo shortCutSettingInfo,
			ScreenStyleConfigInfo screenStyleConfigInfo) {
		mDesktopInfo = desktopSettingInfo;
		mShortcutInfo = shortCutSettingInfo;
		mScreenStyleInfo = screenStyleConfigInfo;
	}

	@Override
	protected void findView() {
		mShowIconsFrame = (RelativeLayout) findViewById(R.id.frame);
		mLargeLine = (ImageView) findViewById(R.id.large_line);
		mSmallLine = (ImageView) findViewById(R.id.small_line);
		mMaxIcon = (TextView) findViewById(R.id.max_text);
		mLargeIcon = (TextView) findViewById(R.id.large_text);
		mSmallIcon = (TextView) findViewById(R.id.small_text);
		initIconShowFrame();

		mDeskIcon = (ImageView) findViewById(R.id.deskicon);
		mDockIcon = (ImageView) findViewById(R.id.dockicon);
		mFolderIcon = (ImageView) findViewById(R.id.foldericon);

		mDeskText = (TextView) findViewById(R.id.desktext);
		mDockText = (TextView) findViewById(R.id.docktext);
		mFolderText = (TextView) findViewById(R.id.foldertext);

		mIconSize = (DeskSettingItemListView) findViewById(R.id.icon_size);
		mIconSize.setOnValueChangeListener(this);
		mIconSize.setOnListClickListener(this);
		mOriginalSize = Utilities.getIconSize(getContext());
		updateIconsPosition(GoLauncher.getIconSizeStyle(), mOriginalSize);
		initCustomIconSizeSeeBar();

		mHighLight = (DeskSettingItemBaseView) findViewById(R.id.icon_highlight);
		mHighLight.setOnClickListener(this);
		mIconBase = (DeskSettingItemCheckBoxView) findViewById(R.id.icon_base);
		mIconBase.setOnValueChangeListener(this);

		mThemeStyleIcon = (DeskSettingItemListView) findViewById(R.id.theme_icon);
		mThemeStyleIcon.setOnValueChangeListener(this);
		mDockStyleIcon = (DeskSettingItemListView) findViewById(R.id.dock_icon);
		mDockStyleIcon.setOnValueChangeListener(this);
		mFolderStyleIcon = (DeskSettingItemListView) findViewById(R.id.folder_icon);
		mFolderStyleIcon.setOnValueChangeListener(this);
		mMenuStyleIcon = (DeskSettingItemListView) findViewById(R.id.menu_icon);
		mMenuStyleIcon.setOnValueChangeListener(this);
	}

	/**
	 * <br>
	 * 功能简述:初始化自定义图标大小调节条 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initCustomIconSizeSeeBar() {
		DeskSettingSeekBarItemInfo sizeSeekBarItemInfo = new DeskSettingSeekBarItemInfo();
		sizeSeekBarItemInfo.setTitle(getResources().getString(
				R.string.icon_size_setting_seekbar_text)); // 设置标题
		sizeSeekBarItemInfo.setMinValue(mIconSizeMin);
		sizeSeekBarItemInfo.setMaxValue(mIconSizeMax);
		sizeSeekBarItemInfo.setSelectValue(mOriginalSize);

		ArrayList<DeskSettingSeekBarItemInfo> seekBarItemInfos = new ArrayList<DeskSettingSeekBarItemInfo>();
		seekBarItemInfos.add(sizeSeekBarItemInfo);

		// seekBarInfo
		DeskSettingSeekBarInfo seekBarInfo = new DeskSettingSeekBarInfo();
		seekBarInfo.setSeekBarItemInfos(seekBarItemInfos);
		seekBarInfo.setTitle(getResources().getString(
				R.string.icon_size_setting_dialog_title));

		DeskSettingInfo customDeskSettingInfo = new DeskSettingInfo();
		customDeskSettingInfo.setSeekBarInfo(seekBarInfo);
		customDeskSettingInfo.setType(DialogTypeId.TYPE_DESK_SETTING_SEEKBAR); // 设置seekbar类型

		mIconSize.getDeskSettingInfo().setSecondInfo(customDeskSettingInfo);
		mIconSize.getDeskSettingInfo()
				.setCustomPosition(
						mIconSize.getDeskSettingInfo().getSingleInfo()
								.getEntries().length - 1); // 设置自定义的位置
	}

	/**
	 * <br>
	 * 功能简述:初始化图标大小框框的显示排版参数 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initIconShowFrame() {
		// 1:框框
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mShowIconsFrame
				.getLayoutParams();
		lp.width = getResources().getDimensionPixelSize(
				R.dimen.desk_setting_visual_tab_icon_frame_width);
		lp.height = mIconSizeMax;

		// 2:大图标标记线
		lp = (RelativeLayout.LayoutParams) mLargeLine.getLayoutParams();
		lp.bottomMargin = mLargeIconSize;

		// 3:小图标标记线
		lp = (RelativeLayout.LayoutParams) mSmallLine.getLayoutParams();
		lp.bottomMargin = mSmallIconSize;

		// 4:最大值标记text
		mMaxIcon.setText(mMaxIcon.getText() + "(" + mIconSizeMax + ")");

		// 5:大图标标记text
		lp = (RelativeLayout.LayoutParams) mLargeIcon.getLayoutParams();
		lp.topMargin = mIconSizeMax - mLargeIconSize - DrawUtils.dip2px(4);
		mLargeIcon.setText(mLargeIcon.getText() + "(" + mLargeIconSize + ")");

		// 6:小图标标记text
		lp = (RelativeLayout.LayoutParams) mSmallIcon.getLayoutParams();
		lp.topMargin = mIconSizeMax - mSmallIconSize - DrawUtils.dip2px(4);
		mSmallIcon.setText(mSmallIcon.getText() + "(" + mSmallIconSize + ")");
	}

	/**
	 * <br>
	 * 功能简述:更新图标显示区size <br>
	 * 功能详细描述: <br>
	 * 注意:调用时机：1:图标大小发生改变;2:横竖屏切换
	 * 
	 * @param type
	 *            　图标大小类型
	 * @param screenIconSize
	 *            　图标大小
	 */
	private void updateIconsPosition(int type, int screenIconSize) {
		// 1:图标框框显示
		int frameWidth = getResources().getDimensionPixelSize(
				R.dimen.desk_setting_visual_tab_icon_frame_width);
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mShowIconsFrame
				.getLayoutParams();
		lp.width = frameWidth;

		// 2:图标显示
		int screenWidth = screenIconSize;
		int dockWidth = 0;
		if (type == DEFAULT_ICON_SIZE || type == DIY_ICON_SIZE) {
			dockWidth = screenIconSize * 60 / 72;
		} else {
			dockWidth = screenIconSize * 72 / 84;
		}

		int folderWidth = screenWidth;
		int sum = screenWidth + dockWidth + folderWidth;
		int textWidth = getResources().getDimensionPixelSize(
				R.dimen.desk_setting_visual_tab_icon_text_width);
		if (sum <= frameWidth) {
			// 3等分
			// deskicon
			int centerX = frameWidth / 6;
			int l = centerX - screenWidth / 2;
			lp = (RelativeLayout.LayoutParams) mDeskIcon.getLayoutParams();
			lp.leftMargin = l;
			lp.width = screenWidth;
			lp.height = screenWidth;
			// desktext
			lp = (RelativeLayout.LayoutParams) mDeskText.getLayoutParams();
			lp.leftMargin = l + screenWidth / 2 - textWidth / 2;

			// dockicon
			centerX = frameWidth / 2;
			l = centerX - dockWidth / 2;
			lp = (RelativeLayout.LayoutParams) mDockIcon.getLayoutParams();
			lp.leftMargin = l;
			lp.width = dockWidth;
			lp.height = dockWidth;

			// docktext
			lp = (RelativeLayout.LayoutParams) mDockText.getLayoutParams();
			lp.leftMargin = l + dockWidth / 2 - textWidth / 2;

			// foldericon
			centerX = (int) (frameWidth * (5.0f / 6));
			l = centerX - screenWidth / 2;
			lp = (RelativeLayout.LayoutParams) mFolderIcon.getLayoutParams();
			lp.leftMargin = l;
			lp.width = folderWidth;
			lp.height = folderWidth;

			// foldertext
			lp = (RelativeLayout.LayoutParams) mFolderText.getLayoutParams();
			lp.leftMargin = l + folderWidth / 2 - textWidth / 2;
		} else {
			// 从头逐个排版
			// deskicon
			int l = 0;
			lp = (RelativeLayout.LayoutParams) mDeskIcon.getLayoutParams();
			lp.leftMargin = l;
			lp.width = screenWidth;
			lp.height = screenWidth;

			// desktext
			lp = (RelativeLayout.LayoutParams) mDeskText.getLayoutParams();
			lp.leftMargin = screenWidth / 2 - textWidth / 2;

			// dockicon
			l += screenWidth;
			lp = (RelativeLayout.LayoutParams) mDockIcon.getLayoutParams();
			lp.leftMargin = l;
			lp.width = dockWidth;
			lp.height = dockWidth;

			// docktext
			lp = (RelativeLayout.LayoutParams) mDockText.getLayoutParams();
			lp.leftMargin = l + dockWidth / 2 - textWidth / 2;

			// foldericon
			l += dockWidth;
			lp = (RelativeLayout.LayoutParams) mFolderIcon.getLayoutParams();
			lp.leftMargin = l;
			lp.width = folderWidth;
			lp.height = folderWidth;

			// foldertext
			lp = (RelativeLayout.LayoutParams) mFolderText.getLayoutParams();
			lp.leftMargin = l + folderWidth / 2 - textWidth / 2;
		}
		mDeskIcon.requestLayout();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		// 图标太大，最大值框装不下，可能最后一个图标会被割一部分
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mDeskIcon
				.getLayoutParams();
		mDeskIcon.layout(mDeskIcon.getLeft(), mDeskIcon.getTop(),
				mDeskIcon.getLeft() + lp.width, mDeskIcon.getBottom());

		lp = (RelativeLayout.LayoutParams) mDockIcon.getLayoutParams();
		mDockIcon.layout(mDockIcon.getLeft(), mDockIcon.getTop(),
				mDockIcon.getLeft() + lp.width, mDockIcon.getBottom());

		lp = (RelativeLayout.LayoutParams) mFolderIcon.getLayoutParams();
		mFolderIcon.layout(mFolderIcon.getLeft(), mFolderIcon.getTop(),
				mFolderIcon.getLeft() + lp.width, mFolderIcon.getBottom());
	}

	@Override
	public void changeOrientation() {
		// mIconSize.dismissDialog();
		// mThemeStyleIcon.dismissDialog();
		// mDockStyleIcon.dismissDialog();
		// mFolderStyleIcon.dismissDialog();
		// mMenuStyleIcon.dismissDialog();
		dismissColorPicker();
		updateIconsPosition(GoLauncher.getIconSizeStyle(), mDeskIcon.getWidth());
	}

	@Override
	public void load() {
		if (null != mDesktopInfo) {

			String value = null;
			int iconStyle = mDesktopInfo.getIconSizeStyle();

			mIconSizeLoadChoice = Integer.valueOf(iconStyle).toString();

			if (iconStyle != 2) {
				// 自定义或推荐
				mFlag = false;
			} else {
				// 默认
				mFlag = true;
			}

			value = Integer.valueOf(iconStyle).toString();
			mIconSize.getDeskSettingInfo().getSingleInfo()
					.setSelectValue(value);
			DeskSettingConstants.updateSingleChoiceListView(mIconSize, value);

			value = mScreenStyleInfo.getIconStyle();
			DeskSettingConstants.updateSingleChoiceListView(mThemeStyleIcon,
					value);
			loadDeskIcon(value);

			value = mScreenStyleInfo.getFolderStyle();
			mFolderStyleIcon.getDeskSettingInfo().getSingleInfo()
					.setSelectValue(value);
			DeskSettingConstants.updateSingleChoiceListView(mFolderStyleIcon,
					value);
			loadFolderIcon(value);

			mIconBase.setIsCheck(mDesktopInfo.mShowIconBase);

			value = mThemeHasGGmenuRes ? mScreenStyleInfo.getGGmenuStyle()
					: ThemeManager.DEFAULT_THEME_PACKAGE;
			mMenuStyleIcon.getDeskSettingInfo().getSingleInfo()
					.setSelectValue(value);
			DeskSettingConstants.updateSingleChoiceListView(mMenuStyleIcon,
					value);

			mCustomBg = mDesktopInfo.mCustomAppBg;
			mPressColor = mDesktopInfo.mPressColor;
			mFocusColor = mDesktopInfo.mFocusColor;
		}

		if (null != mShortcutInfo) {
			DeskSettingConstants.updateSingleChoiceListView(mDockStyleIcon,
					mShortcutInfo.mStyle);
			String pkg = mShortcutInfo.mStyle;
			loadDockIcon(pkg);
		}
	}

	/**
	 * <br>
	 * 功能简述:加载指定主题包拨号图标样式 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param themePkg
	 *            　指定主题包名
	 */
	private void loadDeskIcon(String themePkg) {
		Drawable drawable = null;
		AppDataEngine engine = AppDataEngine.getInstance(getContext());
		if (engine != null) {
			AppDataThemeBean bean = engine.createAppDataThemeBean(themePkg);
			if (bean != null) {
				ConcurrentHashMap<String, String> hm = bean.getFilterAppsMap();
				// 初始化主题包默认的识别为拨号程序的component name
				String[] dialComponents = new String[] {
						"ComponentInfo{com.android.contacts/com.android.contacts.DialtactsActivity}",
						"ComponentInfo{com.android.htcdialer/com.android.htcdialer.Dialer}",
						"ComponentInfo{com.sec.android.app.dialertab/com.sec.android.app.dialertab.DialerTabActivity}",
						"ComponentInfo{com.sonyericsson.android.socialphonebook/com.sonyericsson.android.socialphonebook.DialerEntryActivity}",
						"ComponentInfo{com.android.htccontacts/com.android.htccontacts.DialerTabActivity}",
						"ComponentInfo{com.android.contacts/com.android.contacts.activities.DialtactsActivity}" };
				ImageExplorer explorer = ImageExplorer
						.getInstance(getContext());
				for (String component : dialComponents) {
					String resName = hm.get(component);
					if (resName != null) {
						drawable = explorer.getDrawable(themePkg, resName);
						if (drawable != null) {
							break;
						}
					}
				}

			}
		}
		drawable = drawable != null ? drawable : getResources().getDrawable(
				R.drawable.phone_4_def3);
		mDeskIcon.setImageDrawable(drawable);
	}

	/**
	 * <br>
	 * 功能简述:加载指定主题包dock功能表图标样式 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param themePkg
	 *            　指定主题包名
	 */
	private void loadDockIcon(String themePkg) {
		DeskThemeBean.SystemDefualtItem dockThemeItem = DockItemControler
				.getSystemDefualtItem(themePkg, 2);

		Drawable drawable = null;
		if (null != dockThemeItem && null != dockThemeItem.mIcon
				&& null != dockThemeItem.mIcon.mResName) {
			// 主题安装包
			drawable = ImageExplorer.getInstance(getContext()).getDrawable(
					themePkg, dockThemeItem.mIcon.mResName);
		} else {
			// 风格安装包
			drawable = DockItemControler.getStylePkgDrawable(getContext(),
					themePkg, 2);
		}

		drawable = drawable != null ? drawable : getResources().getDrawable(
				R.drawable.shortcut_0_2_funclist);
		mDockIcon.setImageDrawable(drawable);
	}

	/**
	 * <br>
	 * 功能简述:加载指定主题包文件夹底图图片样式 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param themePkg
	 *            　指定主题包名
	 */
	private void loadFolderIcon(String themePkg) {
		ThemeManager themeManager = GOLauncherApp.getThemeManager();
		DeskFolderThemeBean themeBean = null;
		Drawable drawable = null;
		try {
			// 如果桌面在后台被杀，Appcore = null,会解析不了themeBean
			themeBean = themeManager.parserDeskFolderTheme(themePkg);
		} catch (Exception e) {
		}

		if (themeBean != null && themeBean.mFolderStyle != null) {
			if (themeBean.mFolderStyle.mBackground != null) {
				drawable = ImageExplorer.getInstance(getContext()).getDrawable(
						themePkg, themeBean.mFolderStyle.mBackground.mResName);
			}
		}
		drawable = drawable != null ? drawable : getResources().getDrawable(
				R.drawable.folder_back);

		mFolderIcon.setImageDrawable(drawable);
	}

	@Override
	public void save() {
		if (null != mScreenStyleInfo && null != AppCore.getInstance()) {
			String style = mScreenStyleInfo.getIconStyle();
			if (null == style
					|| !style.equals(mThemeStyleIcon.getDeskSettingInfo()
							.getSingleInfo().getSelectValue())) {
				mScreenStyleInfo.setIconStyle(mThemeStyleIcon
						.getDeskSettingInfo().getSingleInfo().getSelectValue());
				GoLauncher.sendBroadcastHandler(this,
						IDiyMsgIds.REFRESH_SCREENICON_THEME, -1, null, null);
			}

			style = mScreenStyleInfo.getFolderStyle();
			if (null == style
					|| !style.equals(mFolderStyleIcon.getDeskSettingInfo()
							.getSingleInfo().getSelectValue())) {
				mScreenStyleInfo.setFolderStyle(mFolderStyleIcon
						.getDeskSettingInfo().getSingleInfo().getSelectValue());
				GoLauncher.sendBroadcastHandler(this,
						IDiyMsgIds.REFRESH_FOLDER_THEME, -1, null, null);
			}

			style = mScreenStyleInfo.getGGmenuStyle();
			if (null == style
					|| !style.equals(mMenuStyleIcon.getDeskSettingInfo()
							.getSingleInfo().getSelectValue())) {
				mScreenStyleInfo.setGGmenuStyle(mMenuStyleIcon
						.getDeskSettingInfo().getSingleInfo().getSelectValue());
				GoLauncher.sendBroadcastHandler(this,
						IDiyMsgIds.REFRESH_GGMENU_THEME, -1, null, null);
			}
		}

		// if (mDesktopInfo != null) {
		// boolean bChanged = false;
		// if (mDesktopInfo.mCustomAppBg != mCustomBg) {
		// mDesktopInfo.mCustomAppBg = mCustomBg;
		// mDesktopInfo.setReload(true);
		// bChanged = true;
		// }
		// if (mDesktopInfo.mPressColor != mPressColor) {
		// mDesktopInfo.mPressColor = mPressColor;
		// mDesktopInfo.setReload(true);
		// bChanged = true;
		// }
		// if (mDesktopInfo.mFocusColor != mFocusColor) {
		// mDesktopInfo.mFocusColor = mFocusColor;
		// mDesktopInfo.setReload(true);
		// bChanged = true;
		// }
		//
		//
		// if (bChanged) {
		// GOLauncherApp.getSettingControler().updateDesktopSettingInfo(
		// mDesktopInfo);
		// }
		// }

		// checkIconSizeChange();

		if (null != mDesktopInfo && null != AppCore.getInstance()) {

			String stylevalue = mDockStyleIcon.getDeskSettingInfo()
					.getSingleInfo().getSelectValue();
			if (null != stylevalue && null != mShortcutInfo
					&& !mShortcutInfo.mStyle.equals(stylevalue)) {
				mShortcutInfo.mStyle = stylevalue;
				GOLauncherApp.getSettingControler()
						.updateCurThemeShortCutSettingStyle(stylevalue);
				GOLauncherApp.getSettingControler()
						.updateCurThemeShortCutSettingBgSwitch(true);
				GOLauncherApp.getSettingControler()
						.updateCurThemeShortCutSettingCustomBgSwitch(true);
				GOLauncherApp.getSettingControler().updateShortCutCustomBg(
						false);
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.DOCK_SETTING_CHANGED, -1, null, null);

			}

		}
		if (mDesktopInfo != null
				&& mDesktopInfo.mShowIconBase != mIconBase.getIsCheck()) {
			mDesktopInfo.mShowIconBase = mIconBase.getIsCheck();
			GOLauncherApp.getSettingControler().updateDesktopSettingInfo(
					mDesktopInfo, false);
			GoLauncher.sendBroadcastHandler(this,
					IDiyMsgIds.EVENT_SHOW_OR_HIDE_ICON_BASE,
					ConvertUtils.boolean2int(mDesktopInfo.mShowIconBase), null,
					null);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == mHighLight) {
			if (mColorPickerDialog != null && mColorPickerDialog.isShowing()) {
				return;
			}
			mPressColor = mDesktopInfo.mPressColor;
			mColorPickerDialog = new ColorPickerDialog(getContext(),
					mHighlightsColorListener, mCustomBg, mPressColor,
					ColorPickerDialog.ICON, ColorPickerDialog.NOXY,
					ColorPickerDialog.NOXY);
			mColorPickerDialog.show();
		}
	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView baseView, Object value) {
		if (baseView == mIconBase) {
			mIconBase.setIsCheck((Boolean) value);
		} else if (baseView == mThemeStyleIcon) {
			DeskSettingConstants.updateSingleChoiceListView(mThemeStyleIcon,
					(String) value);
			loadDeskIcon((String) value);
		} else if (baseView == mDockStyleIcon) {
			DeskSettingConstants.updateSingleChoiceListView(mDockStyleIcon,
					(String) value);
			loadDockIcon((String) value);
		} else if (baseView == mFolderStyleIcon) {
			DeskSettingConstants.updateSingleChoiceListView(mFolderStyleIcon,
					(String) value);
			loadFolderIcon((String) value);
		} else if (baseView == mMenuStyleIcon) {
			DeskSettingConstants.updateSingleChoiceListView(mMenuStyleIcon,
					(String) value);
		} else if (baseView == mIconSize) {
			int iconSize = 0;
			DeskSettingSingleInfo singleInfo = mIconSize.getDeskSettingInfo()
					.getSingleInfo();
			if (value instanceof String) {
				DeskSettingConstants.updateSingleChoiceListView(mIconSize,
						(String) value);

				iconSize = mDesktopInfo.getIconRealSize(Integer
						.valueOf((String) value));

				DeskSettingSeekBarItemInfo seekBarItemInfo = mIconSize
						.getDeskSettingInfo().getSecondInfo().getSeekBarInfo()
						.getSeekBarItemInfos().get(0);
				seekBarItemInfo.setSelectValue(iconSize);

			} else if (value instanceof String[]) {
				DeskSettingConstants.updateSingleChoiceListView(mIconSize,
						mIconSize.getDeskSettingInfo().getSingleInfo()
								.getEntryValues()[2].toString());

				String valueStr = ((String[]) value)[0];
				iconSize = Integer.valueOf(valueStr);
				mDesktopInfo.mIconSize = iconSize;

			}

			// 修改显示图标大小
			int type = Integer.valueOf(singleInfo.getSelectValue());
			updateIconsPosition(type, iconSize);

			checkIconSizeChange();

		}

		return true;
	}

	ColorPickerDialog.OnColorChangedListener mHighlightsColorListener = new ColorPickerDialog.OnColorChangedListener() {
		@Override
		public void colorChanged(int color) {
			mPressColor = color;
			mFocusColor = color;
		}

		@Override
		public void useCustom(boolean custom) {
			mCustomBg = custom;
		}

		@Override
		public void colorIsSave(boolean isSave) {
			// TODO Auto-generated method stub
			mIsSave = isSave;
			if (mIsSave) {
				if (mDesktopInfo != null) {
					if (mDesktopInfo.mCustomAppBg != mCustomBg) {
						mDesktopInfo.mCustomAppBg = mCustomBg;
						mDesktopInfo.setReload(true);
					}
					if (mDesktopInfo.mPressColor != mPressColor) {
						mDesktopInfo.mPressColor = mPressColor;
						mDesktopInfo.setReload(true);
					}
					if (mDesktopInfo.mFocusColor != mFocusColor) {
						mDesktopInfo.mFocusColor = mFocusColor;
						mDesktopInfo.setReload(true);
					}
					GOLauncherApp.getSettingControler()
							.updateDesktopSettingInfo(mDesktopInfo);
				}
			}
		}
	};

	private void dismissColorPicker() {
		if (mColorPickerDialog != null && mColorPickerDialog.isShowing()) {
			float x = mColorPickerDialog.getmPointX();
			float y = mColorPickerDialog.getmPointY();
			mColorPickerDialog.dismiss();
			mColorPickerDialog = null;
			mColorPickerDialog = new ColorPickerDialog(getContext(),
					mHighlightsColorListener, mCustomBg, mPressColor,
					ColorPickerDialog.ICON, x, y);
			mColorPickerDialog.show();
		}
	}

	/**
	 * <br>
	 * 功能简述:主题图标内容初始化 <br>
	 * 功能详细描述: <br>
	 * 注意:在所有主题扫描完成后调用
	 */
	private void initDeskIconList() {
		final int size = mAllThemeName.length;
		int curThemeIndex = 0;
		String curThemePkg = ThemeManager.getInstance(getContext())
				.getCurThemePackage(); // 当前主题

		String[] values = new String[size + 1];
		values[0] = LauncherEnv.PACKAGE_NAME;

		if (curThemePkg.equals(values[0])) {
			curThemeIndex = 0;
		}

		for (int i = 0; i < size; i++) {

			values[i + 1] = mAllThemePackage[i];
			if (curThemePkg.equals(values[i + 1])) {
				curThemeIndex = i + 1;
			}
		}

		mThemeStyleIcon.getDeskSettingInfo().getSingleInfo()
				.setEntryValues(values);
		String[] entries = new String[size + 1];
		entries[0] = getResources().getString(R.string.defaultstyle);

		if (0 == curThemeIndex) {
			entries[0] = entries[0] + "("
					+ getResources().getString(R.string.current) + ")";
		}

		for (int i = 0; i < size; i++) {
			entries[i + 1] = mAllThemeName[i];
			if (i + 1 == curThemeIndex) {
				entries[i + 1] = entries[i + 1] + "("
						+ getResources().getString(R.string.current) + ")";
			}
		}

		mThemeStyleIcon.getDeskSettingInfo().getSingleInfo()
				.setEntries(entries);
	}

	/**
	 * <br>
	 * 功能简述:快捷条风格内容初始化 <br>
	 * 功能详细描述: <br>
	 * 注意:在所有主题扫描完成后调用
	 */
	private void initDockStyleRefEntry() {
		// 风格安装包扫描
		ArrayList<StyleBaseInfo> styleInfos = GOLauncherApp
				.getDockStyleIconManager().getAllStyleBaseInfos();
		int themeSize = mAllThemePackage.length;
		int styleSize = 0;
		if (styleInfos != null) {
			styleSize = styleInfos.size();
		}

		// entries初始化
		String[] entries = new String[themeSize + styleSize + 1];
		entries[0] = getResources().getString(R.string.defaultstyle);
		String curThemePkg = ThemeManager.getInstance(getContext())
				.getCurThemePackage();

		if (curThemePkg.equals(LauncherEnv.PACKAGE_NAME)) {
			entries[0] = entries[0] + "("
					+ getResources().getString(R.string.current) + ")";
		}

		for (int i = 0; i < themeSize; i++) {
			entries[i + 1] = mAllThemeName[i];
			if (curThemePkg.equals(mAllThemePackage[i])) {
				entries[i + 1] = entries[i + 1] + "("
						+ getResources().getString(R.string.current) + ")";
			}
		}

		for (int i = 0; i < styleSize; i++) {
			entries[i + themeSize + 1] = styleInfos.get(i).mStyleName;
		}

		mDockStyleIcon.getDeskSettingInfo().getSingleInfo().setEntries(entries);
		// values初始化
		String[] values = new String[themeSize + styleSize + 1];
		values[0] = DockUtil.DOCK_DEFAULT_STYLE_STRING;

		for (int i = 0; i < themeSize; i++) {
			values[i + 1] = mAllThemePackage[i];
		}

		for (int i = 0; i < styleSize; i++) {
			values[i + themeSize + 1] = styleInfos.get(i).mPkgName;
		}

		mDockStyleIcon.getDeskSettingInfo().getSingleInfo()
				.setEntryValues(values);

		if (null != styleInfos) {
			styleInfos.clear();
			styleInfos = null;
		}
	}

	/**
	 * <br>
	 * 功能简述:文件夹风格内容初始化 <br>
	 * 功能详细描述: <br>
	 * 注意:在所有主题扫描完成后调用
	 */
	private void initFolderStyleRefEntry() {
		String curThemePkg = ThemeManager.getInstance(getContext())
				.getCurThemePackage();
		int count = mAllThemePackage.length;
		// 添加默认的主题包名
		ArrayList<String> arrEntries = new ArrayList<String>();
		ArrayList<String> arrValues = new ArrayList<String>();

		arrEntries.add(getResources().getString(R.string.defaultstyle));
		arrValues.add(ThemeManager.DEFAULT_THEME_PACKAGE);

		for (int i = 0; i < count; i++) {
			String packageName = mAllThemePackage[i];
			String themeName = mAllThemeName[i];
			// 解析桌面中相关主题信息
			arrEntries.add(themeName);
			arrValues.add(packageName);
		}

		if (arrEntries.size() != arrValues.size()) {
			Log.e("IconStyle", "arrEntries.size() != arrValues.size()");
		}
		String[] entries = new String[arrEntries.size()];
		String[] values = new String[arrEntries.size()];
		for (int i = 0; i < arrEntries.size(); i++) {
			entries[i] = arrEntries.get(i);
			values[i] = arrValues.get(i);
		}
		if (curThemePkg.equals(LauncherEnv.PACKAGE_NAME)) {
			entries[0] = entries[0] + "("
					+ getResources().getString(R.string.current) + ")";
		} else {
			for (int i = 1; i < entries.length; i++) {
				if (curThemePkg.equals(values[i])) {
					entries[i] = entries[i] + "("
							+ getResources().getString(R.string.current) + ")";
					break;
				}
			}
		}
		mFolderStyleIcon.getDeskSettingInfo().getSingleInfo()
				.setEntries(entries);
		mFolderStyleIcon.getDeskSettingInfo().getSingleInfo()
				.setEntryValues(values);
		arrEntries.clear();
		arrValues.clear();
		arrValues = null;
		arrEntries = null;
	}

	/**
	 * 排序比较器，中英文按a~z进行混排
	 */
	private Comparator<ThemeInfoBean> mComparator = new Comparator<ThemeInfoBean>() {
		@Override
		public int compare(ThemeInfoBean object1, ThemeInfoBean object2) {
			int result = 0;
			String str1 = object1.getThemeName();
			String str2 = object2.getThemeName();
			str1 = SortUtils.changeChineseToSpell(getContext(), str1);
			str2 = SortUtils.changeChineseToSpell(getContext(), str2);
			Collator collator = null;
			if (Build.VERSION.SDK_INT < 16) {
				collator = Collator.getInstance(Locale.CHINESE);
			} else {
				collator = Collator.getInstance(Locale.ENGLISH);
			}

			if (collator == null) {
				collator = Collator.getInstance(Locale.getDefault());
			}
			result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
			return result;
		}
	};

	/**
	 * <br>
	 * 功能简述:菜单风格内容初始化 <br>
	 * 功能详细描述: <br>
	 * 注意:在所有主题扫描完成后调用
	 */
	private void initGGmenuStyleList() {
		ArrayList<ThemeInfoBean> entriesArr = new ArrayList<ThemeInfoBean>();
		String curThemePkg = ThemeManager.getInstance(getContext())
				.getCurThemePackage();
		ArrayList<ThemeInfoBean> themeInfoBeans = GOLauncherApp
				.getThemeManager().getAllThemeInfosWithoutDefaultTheme();
		Collections.sort(themeInfoBeans, mComparator);

		for (int i = 0; i < themeInfoBeans.size(); i++) {
			ThemeInfoBean themeInfoBean = themeInfoBeans.get(i);
			InputStream inputStream = null;
			XmlPullParser xmlPullParser = null;
			inputStream = ThemeManager.getInstance(getContext())
					.createParserInputStream(themeInfoBean.getPackageName(),
							ThemeConfig.DESKTHEMEFILENAME);
			if (inputStream != null) {
				xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
			} else {
				xmlPullParser = XmlParserFactory.createXmlParser(getContext(),
						ThemeConfig.DESKTHEMEFILENAME,
						themeInfoBean.getPackageName());
			}
			if (xmlPullParser == null) {
				continue;
			}
			try {
				int eventType = xmlPullParser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG) {
						// 标签名
						String tagName = xmlPullParser.getName();
						if (tagName.equals(TagSet.GlMenu)) {
							String identify = xmlPullParser
									.getAttributeValue(
											null,
											com.jiubang.ggheart.data.theme.parser.AttributeSet.IDENTITY);
							if (null != identify && identify.equals("desk")) {
								entriesArr.add(themeInfoBean);
								break;
							}
						}
					}
					eventType = xmlPullParser.next();
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		String[] entries = new String[entriesArr.size() + 1];
		String[] values = new String[entriesArr.size() + 1];
		entries[0] = getResources().getString(R.string.defaultstyle);
		values[0] = ThemeManager.DEFAULT_THEME_PACKAGE;

		if (curThemePkg.equals(LauncherEnv.PACKAGE_NAME)) {
			entries[0] = entries[0] + "("
					+ getResources().getString(R.string.current) + ")";
		}

		for (int i = 1; i < entries.length; i++) {
			entries[i] = entriesArr.get(i - 1).getThemeName();
			values[i] = entriesArr.get(i - 1).getPackageName();
			if (curThemePkg.equals(values[i])) {
				entries[i] = entries[i] + "("
						+ getResources().getString(R.string.current) + ")";
			}
		}

		entriesArr.clear();
		entriesArr = null;
		mMenuStyleIcon.getDeskSettingInfo().getSingleInfo().setEntries(entries);
		mMenuStyleIcon.getDeskSettingInfo().getSingleInfo()
				.setEntryValues(values);
		String ggmenuPackage = mScreenStyleInfo.getGGmenuStyle();
		for (int i = 0; i < values.length; i++) {
			if (values[i].equals(ggmenuPackage)) {
				mThemeHasGGmenuRes = true;
				break;
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:检测图标大小是否有变化，是否通知桌面重启 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void checkIconSizeChange() {
		final String value = mIconSize.getDeskSettingInfo().getSingleInfo()
				.getSelectValue();
		int index = value != null ? Integer.parseInt(value) : DEFAULT_ICON_SIZE;

		if (mFlag) {
			if (mDesktopInfo.getIconSizeStyle() != index
					|| (index == DIY_ICON_SIZE)) {
				if (Machine.isTablet(getContext()) && index == LARGE_ICON_SIZE) {
					Toast.makeText(
							getContext(),
							"Sorry,the option  of large icon style in tablet is invalid.",
							Toast.LENGTH_LONG).show();
				}
				mDesktopInfo.setIconSizeStyle(index);
				if (index != DIY_ICON_SIZE) {
					mDesktopInfo.mIconSize = 0;
				}
				GOLauncherApp.getSettingControler().updateDesktopSettingInfo(
						mDesktopInfo, false);
				if (null != GoLauncher.getContext()) {
					GoLauncher.getContext().setNeedReStart();
				}
			} else {

				if (null != GoLauncher.getContext()) {
					GoLauncher.getContext().setNotNeedReStart();
				}
			}
		} else {
			if (mDesktopInfo.getIconSizeStyle() != index
					|| (index == DIY_ICON_SIZE && mDesktopInfo.mIconSize != mOriginalSize)) {
				if (Machine.isTablet(getContext()) && index == LARGE_ICON_SIZE) {
					Toast.makeText(
							getContext(),
							"Sorry,the option  of large icon style in tablet is invalid.",
							Toast.LENGTH_LONG).show();
				}
				mDesktopInfo.setIconSizeStyle(index);
				if (index != DIY_ICON_SIZE) {
					mDesktopInfo.mIconSize = 0;
				}
				GOLauncherApp.getSettingControler().updateDesktopSettingInfo(
						mDesktopInfo, false);
				if (null != GoLauncher.getContext()) {
					GoLauncher.getContext().setNeedReStart();
				}
			} else {

				if (null != GoLauncher.getContext()) {
					GoLauncher.getContext().setNotNeedReStart();
				}
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:设置所有主题数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param themePkgs
	 * @param themeNames
	 */
	public void setThemesData(String[] themePkgs, String[] themeNames) {
		mAllThemePackage = themePkgs;
		mAllThemeName = themeNames;

		initDeskIconList();
		initDockStyleRefEntry();
		initFolderStyleRefEntry();
		initGGmenuStyleList();
		load();
	}
}
