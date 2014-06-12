package com.jiubang.ggheart.apps.desks.Preferences.view;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.launcher.cropimage.CropImageActivity;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.WallpaperDensityUtil;
import com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.DockLogicControler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenFrame;
import com.jiubang.ggheart.apps.desks.golauncherwallpaper.ChooseWallpaper;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>
 * 类描述:个性化设置-背景tab view <br>
 * 功能详细描述:
 * 
 * @author ruxueqin
 * @date [2012-9-25]
 */
public class DeskSettingVisualBackgroundTabView extends
		DeskSettingVisualAbsTabView {

	// 设置数据
	private FunAppSetting mFunAppSetting; // 功能表设置信息
	private ShortCutSettingInfo mShortcutInfo; // 快捷条设置信息
	private ScreenSettingInfo mScreenInfo; // 屏幕设置信息

	private String[] mAllThemePackage; // 所有主题包名
	private String[] mAllThemeName; // 所有主题名称

	// dock背景类型
	public static final int DOCK_BG_NON = 0; // 无背景
	public static final int DOCK_BG_GO_THEME = 1; // 主题背景
	public static final int DOCK_BG_CUSTOM = 2; // 自定义背景

	private int mDockBgvalue; // dock条当前背景值
	private boolean mOldWallpaperScrollV; // 标记进入此界面时，壁纸是否可滚动

	// 功能表背景
	private static final int FUN_BG_NON = 2; // 透明背景
	private static final int FUN_BG_THEME = 4; // 主题背景
	private static final int FUN_BG_CUSTOM = 5; // 自定义背景

	public final static String BGSETTINGTAG = "ReadFromSource"; // 标记背景图来源：主题包或文件夹

	// 功能表背景保存图片
	private String mSaveFile; // 功能表背景图片保存路径
	private int mFileHeight; // 图片裁剪高度
	private int mFileWidth; // 图片裁剪宽度

	public static final int DRAWERBG_CLIP_CODE = 1001; // 功能表背景图片裁剪requestCode

	private String mAppdrawerBgSettingValue; // 用于保存功能表背景改变之前的值,用于选择图片不成功时回滚
	private String mDockBgSettingValue; // 用于保存dock背景改变之前的值,用于选择图片不成功时回滚

	// views
	private DeskSettingTitleView mWallpaperTitle; // 壁纸项标题
	private ImageView mWallpaperPic; // 壁纸
	// private DeskSettingItemListView mWallpaperListView = null; // 壁纸背景类型显示
	private ImageView mWallpaperBaseLine; // 壁纸底下的分隔横线
	private DeskSettingItemCheckBoxView mWallpaperScroable; // 壁纸滚动
	private DeskSettingTitleView mDockBgTitle; // dock背景项标题
	private ImageView mDockPic; // dock背景图片
	private ImageView mDockBaseLine; // dock背景图片下的分隔横线
	private DeskSettingItemListView mDockListView; // dock背景类型显示(只有“无背景”时才显示此view)
	private DeskSettingTitleView mAppdrawerBgTitle; // 功能表背景项标题
	private ImageView mAppdrawerWallpaperPic; // 功能表壁纸图片
	private ImageView mAppdrawerBaseLine; // 功能表壁纸图片下的分隔横线
	private DeskSettingItemListView mAppdrawerListView; // 功能表背景类型显示(只有“透明背景”时才显示此view)
	private DeskSettingItemCheckBoxView mAppdrawerWallpaperBlur; // 功能表背景是否模糊
	private DeskSettingItemListView mAppdrawerSelectCard; // 功能选项卡和底座背景

	public long mLastClickTime; // 最后一次点击时间
	public static final long CLICK_TIME = 400; // 每次点击间隔时间

	private PreferencesManager mPreferencesManager = null;

	public DeskSettingVisualBackgroundTabView(Context context,
			AttributeSet attrs) {
		super(context, attrs);
		initSaveFile();
		Display display = ((Activity) getContext()).getWindowManager()
				.getDefaultDisplay();
		mFileWidth = display.getWidth();
		mFileHeight = display.getHeight();
		// 处理go桌面主题-设置透明背景
		mPreferencesManager = new PreferencesManager(context,
				IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW,
				Context.MODE_WORLD_WRITEABLE);
	}

	@Override
	protected void findView() {
		mWallpaperTitle = (DeskSettingTitleView) findViewById(R.id.wallpapertitle);
		mWallpaperTitle.setOnClickListener(this);
		mWallpaperPic = (ImageView) findViewById(R.id.wallpaperpic);
		mWallpaperPic.setOnClickListener(this);
		// mWallpaperListView = (DeskSettingItemListView)
		// findViewById(R.id.wallpaper_bg_base_view);
		// mWallpaperListView.setOnValueChangeListener(this);
		// mWallpaperListView.setOnListClickListener(this);

		mWallpaperScroable = (DeskSettingItemCheckBoxView) findViewById(R.id.wallpaper_scrollable);
		mWallpaperScroable.setOnValueChangeListener(this);
		mDockBaseLine = (ImageView) findViewById(R.id.dockpicbase);
		mDockBaseLine.setOnClickListener(this);

		mDockBgTitle = (DeskSettingTitleView) findViewById(R.id.dockbgtitle);
		mDockBgTitle.setOnClickListener(this);
		mDockPic = (ImageView) findViewById(R.id.dockpic);
		mDockPic.setOnClickListener(this);
		mDockListView = (DeskSettingItemListView) findViewById(R.id.dock_bg_base_view);
		mDockListView.setOnValueChangeListener(this);
		mDockListView.setOnListClickListener(this);
		mWallpaperBaseLine = (ImageView) findViewById(R.id.wallpaperpicbase);
		mWallpaperBaseLine.setOnClickListener(this);

		mAppdrawerBgTitle = (DeskSettingTitleView) findViewById(R.id.appdrawerbgtitle);
		mAppdrawerBgTitle.setOnClickListener(this);
		mAppdrawerWallpaperPic = (ImageView) findViewById(R.id.appdrawwallpaperpic);
		mAppdrawerWallpaperPic.setOnClickListener(this);
		mAppdrawerListView = (DeskSettingItemListView) findViewById(R.id.appdrawer_bg_base_view);
		mAppdrawerListView.setOnValueChangeListener(this);
		mAppdrawerListView.setOnListClickListener(this);
		mAppdrawerBaseLine = (ImageView) findViewById(R.id.appdrawerwallpaperpicbase);
		mAppdrawerBaseLine.setOnClickListener(this);

		mAppdrawerWallpaperBlur = (DeskSettingItemCheckBoxView) findViewById(R.id.appdrawer_blur);
		mAppdrawerWallpaperBlur.setOnValueChangeListener(this);
		mAppdrawerSelectCard = (DeskSettingItemListView) findViewById(R.id.appdrawer_selectcard);
		mAppdrawerSelectCard.setOnValueChangeListener(this);
	}

	/**
	 * <br>
	 * 功能简述:初始化功能表背景选择项信息 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initAppDrawTabBgList() {

		int size = mAllThemeName.length;
		int curThemeIndex = 0;
		String curThemePkg = ThemeManager.getInstance(getContext())
				.getCurThemePackage(); // 当前主题

		String[] values = new String[size + 1];
		values[0] = LauncherEnv.PACKAGE_NAME;

		if (curThemePkg.equals(values[0])) {
			curThemeIndex = 0;
		}

		for (int i = 0; i < mAllThemePackage.length; i++) {

			values[i + 1] = mAllThemePackage[i];
			if (curThemePkg.equals(values[i + 1])) {
				curThemeIndex = i + 1;
			}
		}

		DeskSettingSingleInfo info = mAppdrawerSelectCard.getDeskSettingInfo()
				.getSingleInfo();

		info.setEntryValues(values);
		String[] entries = new String[size + 1];
		entries[0] = getResources().getString(R.string.defaultstyle);

		if (0 == curThemeIndex) {
			entries[0] = entries[0] + "("
					+ getResources().getString(R.string.current) + ")";
		}

		for (int i = 0; i < mAllThemeName.length; i++) {
			entries[i + 1] = mAllThemeName[i];
			if (i + 1 == curThemeIndex) {
				entries[i + 1] = entries[i + 1] + "("
						+ getResources().getString(R.string.current) + ")";
			}
		}

		info.setEntries(entries);
	}

	@Override
	public void load() {
		if (null != mShortcutInfo) {
			mDockBgvalue = getDockBgType();

			DeskSettingConstants.updateSingleChoiceListView(mDockListView,
					Integer.toString(mDockBgvalue));
			mDockListView.setSummaryEnabled(false);
			boolean showAppdrawerPic = mDockBgvalue != DOCK_BG_NON;
			showDockPic(showAppdrawerPic);
			if (showAppdrawerPic) {
				updateDockWallpaper();
			}
		}

		// 屏幕
		if (null != mScreenInfo) {
			mOldWallpaperScrollV = mScreenInfo.mWallpaperScroll;
			mWallpaperScroable.setIsCheck(mOldWallpaperScrollV);
			int summaryId = mOldWallpaperScrollV ? R.string.normal_wallpaper
					: R.string.haploid_wallpaper;
			mWallpaperScroable.setSummaryText(summaryId);
		}
	}

	@Override
	public void save() {
		// 1:dock背景
		int newstylevalue;
		if (null == mDockListView) {
			return;
		} else if (mDockListView.getDeskSettingInfo().getSingleInfo()
				.getSelectValue() == null)// 获得pref失败，设置为现在的值
		{
			newstylevalue = getDockBgType();
		} else {
			newstylevalue = Integer.parseInt(mDockListView.getDeskSettingInfo()
					.getSingleInfo().getSelectValue());
		}
		if (newstylevalue == DOCK_BG_NON) {
			GOLauncherApp.getSettingControler()
					.updateCurThemeShortCutSettingBgSwitch(false);
		} else if (newstylevalue == DOCK_BG_CUSTOM) {
			GOLauncherApp.getSettingControler()
					.updateCurThemeShortCutSettingBgSwitch(true);
			GOLauncherApp.getSettingControler()
					.updateCurThemeShortCutSettingCustomBgSwitch(true);
			GOLauncherApp.getSettingControler().updateShortCutCustomBg(true);
		} else if (newstylevalue == DOCK_BG_GO_THEME) {
			boolean isTransparent = false;
			if (mPreferencesManager != null) {
				isTransparent = mPreferencesManager
						.getBoolean(
								IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_DOCK,
								false);
			}
			if (isTransparent) {
				GOLauncherApp.getSettingControler()
						.updateCurThemeShortCutSettingBgSwitch(false);
			} else {
				GOLauncherApp.getSettingControler()
						.updateCurThemeShortCutSettingBgSwitch(true);
				GOLauncherApp.getSettingControler()
						.updateCurThemeShortCutSettingCustomBgSwitch(true);
				GOLauncherApp.getSettingControler().updateShortCutCustomBg(
						false);
			}

		}
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
				IDiyMsgIds.DOCK_SETTING_CHANGED, -1, null, null);

		// 2:壁纸滚动性
		if (null != mScreenInfo) {
			if (mScreenInfo.mWallpaperScroll != mOldWallpaperScrollV) {
				GOLauncherApp.getSettingControler().updateScreenSettingInfo(
						mScreenInfo);
				WallpaperDensityUtil
						.setWallpaperDimension((Activity) getContext());
			}

		}
	}

	@Override
	public void onClick(View v) {
		if (v == mWallpaperTitle || v == mWallpaperPic
				|| v == mWallpaperBaseLine) {
			// 处理快速点击的BUG
			long curTime = System.currentTimeMillis();
			if (curTime - mLastClickTime < CLICK_TIME) {
				return;
			}
			mLastClickTime = curTime;

			DialogSingleChoice mDialog = new DialogSingleChoice(getContext());
			mDialog.show();
			mDialog.setTitle(R.string.dlg_wallPaperTitle);
			final CharSequence[] items = getContext().getResources()
					.getTextArray(R.array.wallpaper_bg_type_entris);
			mDialog.setItemData(items, -1, false);
			mDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {

					if (item == 0) {
						gotoGoThemeBg(ChooseWallpaper.TYPE_WALLPAPERCHOOSER, -1);
					} else {
						final Intent pickWallpaper = new Intent(
								Intent.ACTION_SET_WALLPAPER);
						Bundle bundle = new Bundle();
						bundle.putString(ChooseWallpaper.CHOOSERTYPE,
								ChooseWallpaper.TYPE_WALLPAPERCHOOSER);
						pickWallpaper.putExtras(bundle);

						Intent chooser = Intent.createChooser(
								pickWallpaper,
								getResources().getText(
										R.string.chooser_wallpaper));

						WallpaperManager wm = (WallpaperManager) getContext()
								.getSystemService(Context.WALLPAPER_SERVICE);
						WallpaperInfo wi = wm.getWallpaperInfo();

						if (wi != null && wi.getSettingsActivity() != null) {
							LabeledIntent li = new LabeledIntent(getContext()
									.getPackageName(),
									R.string.configure_wallpaper, 0);
							li.setClassName(wi.getPackageName(),
									wi.getSettingsActivity());
							if (null != getContext().getPackageManager()
									.resolveActivity(li, 0)) {
								// 检查是否存在该activity
								chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
										new Intent[] { li });
							}
						}

						try {
							getContext().startActivity(chooser);
							WallpaperControler.sIsWallpaperSetting = true;

						} catch (ActivityNotFoundException e) {
							String textString = getResources().getString(
									R.string.no_app_handle);
							DeskToast.makeText(getContext(), textString,
									Toast.LENGTH_LONG).show();
						}

					}

				}
			});
		} else if (v == mDockBgTitle || v == mDockPic || v == mDockBaseLine) {
			mDockListView.performClick();
		} else if (v == mAppdrawerBgTitle || v == mAppdrawerWallpaperPic
				|| v == mAppdrawerBaseLine) {
			mAppdrawerListView.performClick();
		} else if (v == mDockListView) {
			mDockBgSettingValue = mDockListView.getDeskSettingInfo()
					.getSingleInfo().getSelectValue();
		} else if (v == mAppdrawerListView) {
			mAppdrawerBgSettingValue = mAppdrawerListView.getDeskSettingInfo()
					.getSingleInfo().getSelectValue();
		}
	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView baseView,
			Object newValue) {
		boolean bRet = true;
		String value = newValue.toString();

		if (baseView == mDockListView) {
			int stylevalue = Integer.valueOf((String) newValue);

			DeskSettingConstants.updateSingleChoiceListView(mDockListView,
					(String) newValue);
			mDockListView.setSummaryEnabled(false);
			if (stylevalue == DOCK_BG_NON) {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
						IDiyMsgIds.CLEAR_BG, -1, null, null);
				GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
						IDiyMsgIds.CLEAR_BG, -1, null, null);
				mPreferencesManager
						.putBoolean(
								IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_DOCK,
								false);
				mPreferencesManager.commit();
				showDockPic(false);
			} else if (stylevalue == DOCK_BG_GO_THEME) {
				gotoGoThemeBg(ChooseWallpaper.TYPE_DOCK_BACKGROUNDCHOOSER,
						IRequestCodeIds.DOCK_GO_THEME_BG);
			} else if (stylevalue == DOCK_BG_CUSTOM) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				try {
					((Activity) getContext()).startActivityForResult(intent,
							IRequestCodeIds.DOCK_CROP_CUSTOM_BG);
				} catch (Exception e) {
					e.printStackTrace();
					DeskToast.makeText(getContext(),
							R.string.activity_not_found, Toast.LENGTH_SHORT)
							.show();
				}
			}
		} else if (baseView == mAppdrawerListView) // 功能表
		{
			// 保存设置
			DeskSettingConstants.updateSingleChoiceListView(mAppdrawerListView,
					(String) newValue);
			mAppdrawerListView.setSummaryEnabled(false);
			int iValue = Integer.parseInt(value);

			if (iValue == FUN_BG_NON) {
				mFunAppSetting.setBgSetting(iValue);
				mFunAppSetting
						.setBackgroundPicPath(FunAppSetting.DEFAULTBGPATH);
				mPreferencesManager
						.putBoolean(
								IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_APPDRAWER,
								false);
				mPreferencesManager.commit();
				showAppdrawerPic(false);
			} else if (iValue == FUN_BG_THEME) {
				gotoGoThemeBg(ChooseWallpaper.TYPE_BACKGROUNDCHOOSER,
						IRequestCodeIds.REQUEST_OPERATION_SELECT_BACKGROUND);
			} else if (iValue == FUN_BG_CUSTOM) {
				gotoFunSystemPreView();
			}
			bRet = true;

		} else if (baseView == mAppdrawerWallpaperBlur) {

			if ((Boolean) newValue) {
				mFunAppSetting.setBlurBackground(1);
			} else {
				mFunAppSetting.setBlurBackground(0);
			}
		} else if (baseView == mAppdrawerSelectCard) {
			mFunAppSetting.setTabHomeBgSetting(value);
			DeskSettingConstants.updateSingleChoiceListView(
					mAppdrawerSelectCard, value);
		} else if (baseView == mWallpaperScroable) {
			mScreenInfo.mWallpaperScroll = (Boolean) newValue;
			mWallpaperScroable.setIsCheck((Boolean) newValue);
			int summaryId = ((Boolean) newValue) ? R.string.normal_wallpaper
					: R.string.haploid_wallpaper;
			mWallpaperScroable.setSummaryText(summaryId);
		}
		return bRet;
	}

	/**
	 * <br>
	 * 功能简述:获取dock背景类型 <br>
	 * 功能详细描述: <br>
	 * 注意:TODO:这里可以优化，直接在数据库里存类型值
	 * 
	 * @return
	 */
	private int getDockBgType() {
		if (!mShortcutInfo.mBgPicSwitch) {
			return DOCK_BG_NON;
		} else if (mShortcutInfo.mBgiscustompic) {
			return DOCK_BG_CUSTOM;
		} else {
			return DOCK_BG_GO_THEME;
		}
	}

	private void gotoGoThemeBg(String chooser, int retCode) {
		Intent intent = new Intent(getContext(), ChooseWallpaper.class);
		intent.putExtra(ChooseWallpaper.CHOOSERTYPE, chooser);
		try {
			((Activity) getContext()).startActivityForResult(intent, retCode);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void gotoFunSystemPreView() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		try {
			Intent chooser = Intent.createChooser(intent, null);
			((Activity) getContext()).startActivityForResult(chooser,
					IRequestCodeIds.REQUEST_OPERATION_SELECT_BACKGROUND);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>
	 * 功能简述:显示快捷条背景类型：1:无背景；2:显示图片 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param show
	 *            false:无背景：文字框; true:显示图片：
	 */
	public void showDockPic(boolean show) {
		if (show) {
			mDockListView.setVisibility(View.GONE);
			mDockPic.setVisibility(View.VISIBLE);
		} else {
			boolean isTransparent = false;
			if (mPreferencesManager != null) {
				isTransparent = mPreferencesManager
						.getBoolean(
								IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_DOCK,
								false);
			}
			if (isTransparent) {
				mDockListView.setVisibility(View.VISIBLE);
				DeskSettingConstants.updateSingleChoiceListView(mDockListView,
						mDockListView.getDeskSettingInfo().getSingleInfo()
								.getEntryValues()[1].toString());
				mDockListView.setSummaryEnabled(false);
				mDockListView.setTitleText(R.string.desk_setting_transparent);
				mDockPic.setVisibility(View.GONE);
			} else {
				mDockListView.setVisibility(View.VISIBLE);
				DeskSettingConstants.updateSingleChoiceListView(mDockListView,
						mDockListView.getDeskSettingInfo().getSingleInfo()
								.getEntryValues()[0].toString());
				mDockListView.setSummaryEnabled(false);
				mDockListView.setTitleText(mDockListView.getDeskSettingInfo()
						.getSingleInfo().getEntry());
				mDockPic.setVisibility(View.GONE);
			}

		}
	}

	/**
	 * <br>
	 * 功能简述:更新快捷条壁纸显示图片 <br>
	 * 功能详细描述: <br>
	 * 注意:更新时机：1:第一次进入此界面；2:选择了其他图片
	 */
	public void updateDockWallpaper() {
		// 设置图片
		Drawable drawable = DockLogicControler.getDockBgDrawable();
		mDockPic.setImageDrawable(drawable);
	}

	/**
	 * <br>
	 * 功能简述:回滚dock背景类型值 <br>
	 * 功能详细描述: <br>
	 * 注意:在设置不成功时回滚，例如进入了主题选择图片界面，却不选择图片退出
	 */
	public void restoreDockBgType() {
		DeskSettingConstants.updateSingleChoiceListView(mDockListView,
				mDockBgSettingValue);
		mDockListView.setSummaryEnabled(false);
	}

	/**
	 * <br>
	 * 功能简述:回滚功能表背景类型值 <br>
	 * 功能详细描述: <br>
	 * 注意:在设置不成功时回滚，例如进入了主题选择图片界面，却不选择图片退出
	 */
	public void restoreAppdrawerBgType() {
		DeskSettingConstants.updateSingleChoiceListView(mAppdrawerListView,
				mAppdrawerBgSettingValue);
		mAppdrawerListView.setSummaryEnabled(false);
	}

	/**
	 * <br>
	 * 功能简述:显示功能表背景类型：1:透明背景；2:显示图片 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param show
	 *            false:透明背景：文字框; true:显示图片：
	 */
	public void showAppdrawerPic(boolean show) {
		if (show) {
			mAppdrawerListView.setVisibility(View.GONE);
			mAppdrawerWallpaperPic.setVisibility(View.VISIBLE);
		} else {

			boolean isTransparent = false;
			if (mPreferencesManager != null) {
				isTransparent = mPreferencesManager
						.getBoolean(
								IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_APPDRAWER,
								false);
			}

			if (isTransparent) {
				mAppdrawerListView.setVisibility(View.VISIBLE);
				DeskSettingConstants.updateSingleChoiceListView(
						mAppdrawerListView, mAppdrawerListView
								.getDeskSettingInfo().getSingleInfo()
								.getEntryValues()[1].toString());
				mAppdrawerListView.setSummaryEnabled(false);
				mAppdrawerListView
						.setTitleText(R.string.desk_setting_transparent);
				mAppdrawerWallpaperPic.setVisibility(View.GONE);
			} else {
				mAppdrawerListView.setVisibility(View.VISIBLE);
				DeskSettingConstants.updateSingleChoiceListView(
						mAppdrawerListView, mAppdrawerListView
								.getDeskSettingInfo().getSingleInfo()
								.getEntryValues()[0].toString());
				mAppdrawerListView.setSummaryEnabled(false);
				mAppdrawerListView.setTitleText(mAppdrawerListView
						.getDeskSettingInfo().getSingleInfo().getEntry());
				mAppdrawerWallpaperPic.setVisibility(View.GONE);
			}

		}
	}

	/**
	 * <br>
	 * 功能简述:裁剪功能表背景图片 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param imageUri
	 */
	public void clipAppdrawerPic(Uri imageUri) {
		if (mSaveFile != null) {
			Intent intent = new Intent(getContext(), CropImageActivity.class);
			intent.setData(imageUri);
			intent.putExtra("outputX", mFileWidth);
			intent.putExtra("outputY", mFileHeight);
			intent.putExtra("aspectX", mFileWidth);
			intent.putExtra("aspectY", mFileHeight);
			intent.putExtra("scale", true);
			intent.putExtra("noFaceDetection", true);
			Uri uri = Uri.parse("file://" + mSaveFile);
			intent.putExtra("output", uri);
			((Activity) getContext()).startActivityForResult(intent,
					DRAWERBG_CLIP_CODE);
		}
	}

	public void saveAppdrawerPic(int setting) {
		if (mSaveFile != null) {
			saveAppdrawerPic(setting, mSaveFile);
		}
	}

	/**
	 * <br>
	 * 功能简述:保存功能表背景图片 <br>
	 * 功能详细描述: <br>
	 * 注意:path 背景路径(可能是其他主题包下的图片或者sd卡上的图片)
	 * 
	 * @param setting
	 *            背景类型
	 * @param path
	 *            背景路径
	 */
	public void saveAppdrawerPic(int setting, String path) {
		mFunAppSetting.setBgSetting(setting);
		mFunAppSetting.setBackgroundPicPath(path);
	}

	/**
	 * <br>
	 * 功能简述:初始化功能表背景图片保存路径 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void initSaveFile() {
		// 主题相关
		ThemeManager themeManager = GOLauncherApp.getThemeManager();
		ThemeBean themeBean = themeManager
				.getThemeBean(ThemeBean.THEMEBEAN_TYPE_FUNCAPP);
		// 主题包的路径
		String themeName = null;
		if (null == themeBean) {
			// 默认主题
			themeName = ThemeManager.DEFAULT_THEME_PACKAGE;
		} else {
			// 其他主题
			themeName = themeBean.getPackageName();
		}

		mSaveFile = LauncherEnv.Path.SDCARD + LauncherEnv.Path.THEME_PATH
				+ themeName + LauncherEnv.Path.FUNC_FOLDER + "/"
				+ LauncherEnv.Path.APPDRAWER_BG;
	}

	/**
	 * <br>
	 * 功能简述:更新桌面壁纸显示图片 <br>
	 * 功能详细描述: <br>
	 * 注意:更新时机：onResume()
	 */
	public void updateWallpaper() {
		// 1:动态壁纸
		WallpaperInfo wallpaperInfo = null;
		boolean hasException = false;
		try {
			WallpaperManager wm = WallpaperManager.getInstance(getContext());
			Method getWallpaperInfo = wm.getClass().getMethod(
					"getWallpaperInfo");
			wallpaperInfo = (WallpaperInfo) getWallpaperInfo.invoke(wm);
		} catch (Throwable e) {
			hasException = true;
		}
		final boolean isLiveWallpaper = !hasException && wallpaperInfo != null ? true
				: false;
		Drawable drawable = null;
		if (isLiveWallpaper) {
			drawable = wallpaperInfo.loadThumbnail(getContext()
					.getPackageManager());
			mWallpaperPic.setImageDrawable(drawable);
		} else {
			// 2:静态壁纸
			drawable = getContext().getWallpaper();
			if (drawable == null) {
				ScreenFrame sc = (ScreenFrame) GoLauncher
						.getFrame(IDiyFrameIds.SCREEN_FRAME);
				drawable = sc.mWorkspace.getBackground();
			}
			mWallpaperPic.setImageDrawable(drawable);

		}
		// 更新mWallpaperPic显示宽度
		if (drawable != null) {
			int showHeight = getResources()
					.getDimensionPixelSize(
							R.dimen.desk_setting_visual_tab_background_wallpaperpic_height);
			int showWidth = getPicShowWidth(drawable, showHeight);
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mWallpaperPic
					.getLayoutParams();
			lp.width = showWidth;
		}
	}

	/**
	 * <br>
	 * 功能简述:更新功能表壁纸显示图片 <br>
	 * 功能详细描述: <br>
	 * 注意:更新时机：1:第一次进入此界面；2:选择了其他图片
	 */
	public void updateAppdrawerWallpaper() {
		int setting = mFunAppSetting.getBgSetting();
		Drawable drawable = null;
		Bitmap bm = null;
		if (setting == FunAppSetting.BG_DEFAULT) {
			drawable = mWallpaperPic.getDrawable();
		} else if (setting == FunAppSetting.BG_GO_THEME) {
			String path = mFunAppSetting.getBackgroundPicPath();

			try {
				URI uri = new URI(path);
				String sc = uri.getScheme();
				if (null != sc && sc.equals(DeskSettingConstants.BGSETTINGTAG)) { // 主题包中的背景图片
					String packageName = uri.getRawSchemeSpecificPart();
					String idStr = uri.getFragment();
					boolean matches = Pattern.matches(
							FuncAppDataHandler.PATTERN, idStr);
					if (matches) {
						// 全数字
						int resId = Integer.valueOf(idStr);
						bm = DeskSettingConstants.decodeResource(getContext(),
								packageName, resId, DrawUtils.dip2px(80));
					} else {
						Resources resources = getContext().getPackageManager()
								.getResourcesForApplication(packageName);
						if (null != resources) {
							int identifier = resources.getIdentifier(idStr,
									"drawable", packageName);
							if (identifier != 0) {
								bm = DeskSettingConstants.decodeResource(
										getContext(), packageName, identifier,
										DrawUtils.dip2px(80));
							}
						}
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			drawable = new BitmapDrawable(bm);
		} else if (setting == FunAppSetting.BG_CUSTOM) {
			String pathCustom = mFunAppSetting.getBackgroundPicPath();
			bm = DeskSettingConstants.decodeFile(pathCustom,
					DrawUtils.dip2px(80));
			drawable = new BitmapDrawable(bm);
		}
		mAppdrawerWallpaperPic.setImageDrawable(drawable);
		// 更新mAppdrawerWallpaperPic显示宽度
		if (drawable != null) {
			int showHeight = getResources()
					.getDimensionPixelSize(
							R.dimen.desk_setting_visual_tab_background_wallpaperpic_height);
			int showWidth = getPicShowWidth(drawable, showHeight);
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mAppdrawerWallpaperPic
					.getLayoutParams();
			lp.width = showWidth;
		}
	}

	@Override
	public void onResume() {
		updateWallpaper();
		int setting = mFunAppSetting.getBgSetting();
		if (setting == FunAppSetting.BG_DEFAULT) {
			Drawable drawable = mWallpaperPic.getDrawable();
			mAppdrawerWallpaperPic.setImageDrawable(drawable);
			// 更新mAppdrawerWallpaperPic显示宽度
			if (drawable != null) {
				int showHeight = getResources()
						.getDimensionPixelSize(
								R.dimen.desk_setting_visual_tab_background_wallpaperpic_height);
				int showWidth = getPicShowWidth(drawable, showHeight);
				LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mAppdrawerWallpaperPic
						.getLayoutParams();
				lp.width = showWidth;
			}
		}
	}

	/**
	 * <br>
	 * 功能简述:外部设置主题包数据 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @param themePkgs
	 * @param themeNames
	 */
	public void setThemesData(String[] themePkgs, String[] themeNames) {
		mAllThemePackage = themePkgs;
		mAllThemeName = themeNames;
		initAppDrawTabBgList();

		appdrawerSelectCardList();
	}

	/**
	 * <br>
	 * 功能简述:更新主题相关view状态 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private void appdrawerSelectCardList() {
		if (null != mFunAppSetting) {
			int bgSetting = mFunAppSetting.getBgSetting();

			// 将默认主题背景和go主题背景合并为一项
			if (bgSetting == 3) {
				bgSetting = 4;
			}
			mAppdrawerWallpaperBlur.setIsCheck(mFunAppSetting
					.getBlurBackground() == 1);

			DeskSettingConstants.updateSingleChoiceListView(mAppdrawerListView,
					Integer.toString(bgSetting));
			mAppdrawerListView.setSummaryEnabled(false);
			int noneValue = Integer.valueOf(mAppdrawerListView
					.getDeskSettingInfo().getSingleInfo().getEntryValues()[0]
					.toString());

			boolean showAppdrawerPic = bgSetting != noneValue;
			showAppdrawerPic(showAppdrawerPic);
			if (showAppdrawerPic) {
				updateAppdrawerWallpaper();
			}

			DeskSettingConstants.updateSingleChoiceListView(
					mAppdrawerSelectCard, mFunAppSetting.getTabHomeBgSetting());
		}
	}

	public void changeOrientation() {
		// mDockListView.dismissDialog();
		// mAppdrawerListView.dismissDialog();
		// mAppdrawerSelectCard.dismissDialog();
	}

	private int getPicShowWidth(Drawable drawable, int height) {
		int width = 0;
		if (drawable != null) {
			int drawableWidth = drawable.getIntrinsicWidth();
			int drawableHeight = drawable.getIntrinsicHeight();
			float scale = height * 1.0f / drawableHeight;
			width = (int) (drawableWidth * scale);
		}

		return width;
	}

	public void setInfos(ShortCutSettingInfo shortCutInfo,
			FunAppSetting funAppSetting, ScreenSettingInfo screenSettingInfo) {
		mShortcutInfo = shortCutInfo;
		mFunAppSetting = funAppSetting;
		mScreenInfo = screenSettingInfo;
	}
}
