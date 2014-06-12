package com.jiubang.ggheart.apps.desks.Preferences;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Pattern;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.launcher.cropimage.CropImageActivity;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemCheckBoxView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemListView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingVisualBackgroundTabView;
import com.jiubang.ggheart.apps.desks.appfunc.handler.FuncAppDataHandler;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.golauncherwallpaper.ChooseWallpaper;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * @author YeJijiong
 * @version 创建时间：2012-10-15 上午11:06:36 功能表个性化设置界面Activity
 */
public class FunAppUISettingVisualActivity extends DeskSettingBaseActivity {
	public static final int DIALOG_ID_INIT_LIST = 1; // 正在加载主题弹出框

	// 设置数据
	private FunAppSetting mFunAppSetting; // 功能表设置信息

	private String[] mAllThemePackage; // 所有主题包名
	private String[] mAllThemeName; // 所有主题名称

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

	// views
	private ImageView mAppdrawerWallpaperPic; // 功能表壁纸图片
	private ImageView mAppdrawerBaseLine; // 功能表壁纸图片下的分隔横线
	private DeskSettingItemListView mAppdrawerListView; // 功能表背景类型显示(只有“透明背景”时才显示此view)
	private DeskSettingItemCheckBoxView mAppdrawerWallpaperBlur; // 功能表背景是否模糊
	private DeskSettingItemListView mAppdrawerSelectCard; // 功能选项卡和底座背景

	private PreferencesManager mPreferencesManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.fun_app_ui_setting_visual_view);
		// 启动异步扫描主题包
		GetAllThemesTask task = new GetAllThemesTask();
		task.execute();
		load();

		GoSettingControler controler = GOLauncherApp.getSettingControler();
		mFunAppSetting = controler.getFunAppSetting();
		findView();
		initSaveFile();
		Display display = getWindowManager().getDefaultDisplay();
		mFileWidth = display.getWidth();
		mFileHeight = display.getHeight();
		// 处理go桌面主题-设置透明背景
		mPreferencesManager = new PreferencesManager(this,
				IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW,
				Context.MODE_WORLD_WRITEABLE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		removeDialog(DIALOG_ID_INIT_LIST);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		mAppdrawerListView.dismissDialog();
		mAppdrawerSelectCard.dismissDialog();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog dialog = null;
		if (id == DIALOG_ID_INIT_LIST) {
			dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.icon_style_loading_message));
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					finish();
				}
			});
		}
		return dialog;
	}

	/**
	 * <br>
	 * 功能简述:获取桌面壁纸图片 <br>
	 * 功能详细描述:
	 */
	public Drawable getDefaultWallpaper() {
		// 1:动态壁纸
		WallpaperInfo wallpaperInfo = null;
		boolean hasException = false;
		try {
			WallpaperManager wm = WallpaperManager.getInstance(this);
			Method getWallpaperInfo = wm.getClass().getMethod(
					"getWallpaperInfo");
			wallpaperInfo = (WallpaperInfo) getWallpaperInfo.invoke(wm);
		} catch (Throwable e) {
			hasException = true;
		}
		final boolean isLiveWallpaper = !hasException && wallpaperInfo != null ? true
				: false;
		if (isLiveWallpaper) {
			Drawable drawable = wallpaperInfo
					.loadThumbnail(getPackageManager());
			return drawable;
		}

		// 2:静态壁纸
		if (getWallpaper() != null) {
			Drawable drawable = getWallpaper();
			return drawable;
		}
		return null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (null == data) {
			// 回滚回之前的值
			if (IRequestCodeIds.REQUEST_OPERATION_SELECT_BACKGROUND == requestCode
					|| DeskSettingVisualBackgroundTabView.DRAWERBG_CLIP_CODE == requestCode) {
				restoreAppdrawerBgType();
			}
			return;
		}

		switch (requestCode) {
		case IRequestCodeIds.REQUEST_OPERATION_SELECT_BACKGROUND: {
			try {
				// 获取数据
				Uri imageUri = data.getData();
				// 不裁剪
				if (null == imageUri) {
					Bundle bundle = data.getExtras();
					if (null != bundle) {
						String pn = bundle
								.getString(ChooseWallpaper.BACGROUND_IMG_RESPKGNAME);
						if (pn.endsWith(ChooseWallpaper.TRANSPARENT_BG)) {
							mPreferencesManager
									.putBoolean(
											IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_APPDRAWER,
											true);
							mPreferencesManager.commit();
							saveAppdrawerPic(FunAppSetting.BG_NON,
									FunAppSetting.DEFAULTBGPATH);
							// 显示
							showAppdrawerPic(false);
							// 更新
							updateAppdrawerWallpaper();

						} else {
							mPreferencesManager
									.putBoolean(
											IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_APPDRAWER,
											false);
							mPreferencesManager.commit();
							String name = bundle
									.getString(ChooseWallpaper.BACGROUND_IMG_RESNAME);
							URI uri = new URI(
									DeskSettingVisualBackgroundTabView.BGSETTINGTAG,
									pn, name);

							// 成功更换功能表背景主题图片
							saveAppdrawerPic(FunAppSetting.BG_GO_THEME,
									uri.toString());
							// 显示
							showAppdrawerPic(true);
							// 更新
							updateAppdrawerWallpaper();
						}

					}
				} else {
					// 调用照相机的裁剪
					clipAppdrawerPic(imageUri);
				}
			} catch (Exception e) {
				String textString = this.getString(R.string.NotFindCROP);
				DeskToast.makeText(this, textString, Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}

		}
			break;

		case DeskSettingVisualBackgroundTabView.DRAWERBG_CLIP_CODE: {
			// 成功更换功能表自定义背景
			// 通知功能表背景改变
			saveAppdrawerPic(FunAppSetting.BG_CUSTOM);
			// 显示
			showAppdrawerPic(true);
			// 更新
			updateAppdrawerWallpaper();
		}
			break;

		default:
			break;
		}
	}

	protected void findView() {
		mAppdrawerWallpaperPic = (ImageView) findViewById(R.id.fun_app_appdrawwallpaperpic);
		mAppdrawerWallpaperPic.setOnClickListener(this);
		mAppdrawerListView = (DeskSettingItemListView) findViewById(R.id.fun_app_appdrawer_bg_base_view);
		mAppdrawerListView.setOnValueChangeListener(this);
		mAppdrawerListView.setOnListClickListener(this);
		mAppdrawerBaseLine = (ImageView) findViewById(R.id.fun_app_appdrawerwallpaperpicbase);
		mAppdrawerBaseLine.setOnClickListener(this);

		mAppdrawerWallpaperBlur = (DeskSettingItemCheckBoxView) findViewById(R.id.fun_app_appdrawer_blur);
		mAppdrawerWallpaperBlur.setOnValueChangeListener(this);
		mAppdrawerSelectCard = (DeskSettingItemListView) findViewById(R.id.fun_app_appdrawer_selectcard);
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
		String curThemePkg = ThemeManager.getInstance(this)
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
	public void onClick(View v) {
		if (v == mAppdrawerWallpaperPic || v == mAppdrawerBaseLine) {
			mAppdrawerListView.performClick();
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

		if (baseView == mAppdrawerListView) // 功能表
		{
			// 保存设置
			DeskSettingConstants.updateSingleChoiceListView(mAppdrawerListView,
					(String) newValue);
			mAppdrawerListView.setSummaryEnabled(false);
			int iValue = Integer.parseInt(value);

			if (iValue == FUN_BG_NON) {
				mPreferencesManager
						.putBoolean(
								IPreferencesIds.DESK_SETTING_VISUAL_BACKGROUND_TAB_VIEW_APPDRAWER,
								false);
				mPreferencesManager.commit();
				mFunAppSetting.setBgSetting(iValue);
				mFunAppSetting
						.setBackgroundPicPath(FunAppSetting.DEFAULTBGPATH);
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
		}
		return bRet;
	}

	private void gotoGoThemeBg(String chooser, int retCode) {
		Intent intent = new Intent(this, ChooseWallpaper.class);
		intent.putExtra(ChooseWallpaper.CHOOSERTYPE, chooser);
		try {
			(this).startActivityForResult(intent, retCode);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void gotoFunSystemPreView() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		try {
			Intent chooser = Intent.createChooser(intent, null);
			(this).startActivityForResult(chooser,
					IRequestCodeIds.REQUEST_OPERATION_SELECT_BACKGROUND);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
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
			Intent intent = new Intent(this, CropImageActivity.class);
			intent.setData(imageUri);
			intent.putExtra("outputX", mFileWidth);
			intent.putExtra("outputY", mFileHeight);
			intent.putExtra("aspectX", mFileWidth);
			intent.putExtra("aspectY", mFileHeight);
			intent.putExtra("scale", true);
			intent.putExtra("noFaceDetection", true);
			Uri uri = Uri.parse("file://" + mSaveFile);
			intent.putExtra("output", uri);
			startActivityForResult(intent, DRAWERBG_CLIP_CODE);
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
	 * 功能简述:更新功能表壁纸显示图片 <br>
	 * 功能详细描述: <br>
	 * 注意:更新时机：1:第一次进入此界面；2:选择了其他图片
	 */
	public void updateAppdrawerWallpaper() {
		int setting = mFunAppSetting.getBgSetting();
		Drawable drawable = null;
		Bitmap bm = null;
		if (setting == FunAppSetting.BG_DEFAULT) {
			drawable = getDefaultWallpaper();
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
						bm = DeskSettingConstants.decodeResource(this,
								packageName, resId, DrawUtils.dip2px(80));
					} else {
						Resources resources = getPackageManager()
								.getResourcesForApplication(packageName);
						if (null != resources) {
							int identifier = resources.getIdentifier(idStr,
									"drawable", packageName);
							if (identifier != 0) {
								bm = DeskSettingConstants.decodeResource(this,
										packageName, identifier,
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
		mAppdrawerListView.dismissDialog();
		mAppdrawerSelectCard.dismissDialog();
	}

	/**
	 * 
	 * <br>
	 * 类描述:扫描获取所有主题包 <br>
	 * 功能详细描述:
	 * 
	 * @author ruxueqin
	 * @date [2012-9-24]
	 */
	class GetAllThemesTask extends AsyncTask<Void, Void, String> {
		String[] mAllThemePackage;
		String[] mAllThemeName;

		@Override
		protected void onPreExecute() {
			// 显示扫描等待提示框
			showDialog(DeskSettingVisualActivity.DIALOG_ID_INIT_LIST);
		}

		@Override
		protected String doInBackground(Void... params) {
			// 扫描全部主题
			ArrayList<ThemeInfoBean> themeInfos = GOLauncherApp
					.getThemeManager().getAllThemeInfosWithoutDefaultTheme();
			int themeSize = 0;
			if (themeInfos != null) {
				themeSize = themeInfos.size();
			}

			// entries初始化
			mAllThemePackage = new String[themeSize];
			mAllThemeName = new String[themeSize];

			for (int i = 0; i < themeSize; i++) {
				mAllThemePackage[i] = themeInfos.get(i).getPackageName();
				mAllThemeName[i] = themeInfos.get(i).getThemeName();
			}

			if (null != themeInfos) {
				themeInfos.clear();
				themeInfos = null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// 1:取消扫描提示框
			removeDialog(DeskSettingVisualActivity.DIALOG_ID_INIT_LIST);

			// 2:设置各tab主题内容
			setThemesData(mAllThemePackage, mAllThemeName);
		}
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
}
