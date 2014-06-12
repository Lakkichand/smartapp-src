package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.DrawableCacheManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.IDrawableLoader;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditTabView;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.gowidget.GoWidgetFinder;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreChannelControl;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.adrecommend.AdElement;
import com.jiubang.ggheart.data.theme.adrecommend.AdHttpAdapter;
import com.jiubang.ggheart.data.theme.adrecommend.AdHttpAdapter.AdResponseData;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.parser.ThemeInfoParser;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:Widget一级界面
 */
public class WidgetTab extends BaseTab implements IDrawableLoader {

	private static final List<String> INSTALLED_PACKAGES = new ArrayList<String>();
	static {
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.TASK_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.SWITCH_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.CALENDAR_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.CLOCK_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.NOTE_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.CONTACT_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.SINA_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.TENCNT_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.EMAIL_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.FACEBOOK_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.TWITTER_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.RECOMMAND_GOSMS_PACKAGE);
		INSTALLED_PACKAGES.add(LauncherEnv.Plugin.BOOKMARK_PACKAGE);
	}
	public static final int INSTALL_VIEW = 0;
	public static final int UNINSTALL_VIEW = 1;

	public static final int NOTINSTALLED = 0; // 未安装
	public static final int INSTALLED = 1; // 已安装
	public static final int CO_VERSION = 2; // 合作版本

	public static final float DEFAULT_DENSITY = 240f;
	public static final String TASK_MANAGER = "com.gau.go.launcherex.gowidget.taskmanager";

	private static final String LANGUAGE = "language";

	public ArrayList<ThemeInfoBean> mThemeInfos;

	private ArrayList<GoWidgetProviderInfo> mGoWidgetInfoList; // 所有widget
	private GoWidgetFinder mFinder;
	private HashMap<String, GoWidgetProviderInfo> mProviderMap;
	private boolean mIsDeleteView = false;

	private ArrayList<WidgetInfo> mItems;

	private final static String THIRD_WIDGET_PACKAGE = "THIRD_WIDGET_PACKAGE";
	private int mInstall = 1; // 已安装与未安装的标志位    初始值改为1，因为添加了GO手册入口
	private Context mContext;
	private String mChannelName = null;
	private static final long CLICK_TIME = 700;
	private long mLastTime; // 上次的点击时间

	private static final String WIDGET_WEIBO = "com.gau.go.launcherex.gowidget.weibowidget";
	private static final String WIDGET_GOTORE = "com.gau.go.launcherex.gowidget.gostore";

	private GoProgressBar mGoProgressBar;
	private DrawableCacheManager mCacheManager;
	private int mClickPosition;
	public WidgetTab(Context context, String tag, int level) {
		super(context, tag, level);
		mContext = context;
		mLastTime = System.currentTimeMillis();
		mChannelName = GoStoreChannelControl.getChannelCheckName(mContext);
		mMutex = new Object();
		mIsNeedAsyncLoadData = true;
		mCacheManager = DrawableCacheManager.getInstance();
		initListByLoading();
	}

	@Override
	public ArrayList<Object> getDtataList() {
		return null;
	}

	@Override
	public int getItemCount() {
		if (mGoWidgetInfoList != null) {
			return mGoWidgetInfoList.size();
		}
		return 0;
	}
	@Override
	public View getView(int position) {
		View view = null;
		view = mInflater.inflate(R.layout.screen_edit_item, null);
		TextView text = (TextView) view.findViewById(R.id.title);
		if (position > mGoWidgetInfoList.size() || position < 0) {
			return null;
		} else if (position == 0) { //GO 手册
			String goTitle = mContext.getResources().getString(R.string.go_handbook_title);
			text.setText(goTitle);
			//生成GO手册 快捷方式
			ShortCutInfo itemInfo = new ShortCutInfo();
			itemInfo.mIntent = new Intent(ICustomAction.ACTION_SHOW_GO_HANDBOOK);
			itemInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
			itemInfo.mTitle = goTitle;
			itemInfo.mIcon = mContext.getResources().getDrawable(R.drawable.go_handbook_icon);
			view.setTag(itemInfo);
		} else {
			GoWidgetProviderInfo info = mGoWidgetInfoList.get(position);
			text.setText(info.mProvider.label);
			view.setTag(position);
			
//			if (mEditBox != null) {
//				if (mEditBox.getWidgetName() != null
//						&& info.mProvider.provider.getPackageName().equals(
//								mEditBox.getWidgetName())) {
//					mEditBox.setWidgetName(null); // 清空gowidget快捷方式跳转控制变量
//					
//					index = position;
//					try {
//						GoLauncher.sendMessage(this,
//								IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
//								IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET, 0, info,
//								null);
//					} catch (Exception ex) {
//					}
//				}
//			}
		}
		return view;
	}
	// 异步load图
	@Override
	public Drawable loadDrawable(int position, Object arg) {
		Drawable drawable = null;
		try {
			//GO手册
			if (position == 0) {
				drawable = mContext.getResources().getDrawable(R.drawable.go_handbook_widget_icon);
				return drawable;
			}
			GoWidgetProviderInfo info = mGoWidgetInfoList.get(position);
			// 根据包名和lable作为键值，防止重复
			drawable = mCacheManager.getDrawableFromCache(DrawableCacheManager.CACHE_WIDGETTAB
					+ info.mProvider.provider.getPackageName() + info.mProvider.label + position);
			if (drawable != null) {
				return drawable;
			}
			final String pkgName = info.mProvider.provider.getPackageName();
			if (info.mInnerWidgetInfo != null
					&& info.mInnerWidgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
				info.mProvider.label = mChannelName;
				if (info.mInnerWidgetInfo != null) {
					info.mInnerWidgetInfo.mTitle = info.mProvider.label;
				}
			}
			if (pkgName.equals("")) {
				if (info.mProvider.icon > 0) {
					drawable = mContext.getResources().getDrawable(info.mProvider.icon);
				} else if (info.mIconPath != null && info.mIconPath.length() > 0) {
					BitmapDrawable imgDrawable = null;
					if (FileUtil.isFileExist(info.mIconPath)) {
						Bitmap bitmap = BitmapFactory.decodeFile(info.mIconPath);
						final Resources resources = mContext.getResources();
						DisplayMetrics displayMetrics = resources.getDisplayMetrics();
						float density = displayMetrics.densityDpi;
						float scale = density / DEFAULT_DENSITY;
						bitmap = Bitmap.createScaledBitmap(bitmap,
								(int) (bitmap.getWidth() * scale),
								(int) (bitmap.getHeight() * scale), false);
						imgDrawable = new BitmapDrawable(resources, bitmap);
						imgDrawable.setTargetDensity(displayMetrics);
					}
					if (imgDrawable == null) {
						imgDrawable = (BitmapDrawable) ImageExplorer.getInstance(mContext)
								.getDrawable(ThemeManager.DEFAULT_THEME_PACKAGE, info.mIconPath);
					}
					drawable = imgDrawable;
				}
			} else {
				Resources resources = mFinder.getGoWidgetResources(pkgName);
				if (resources != null) {
					drawable = resources.getDrawable(info.mProvider.icon);
				}
			}
			// widget本身就有椭圆的框框，所以不需要勾图
			drawable = getFitIcon(drawable, false);
			mCacheManager.saveToCache(DrawableCacheManager.CACHE_WIDGETTAB
					+ info.mProvider.provider.getPackageName() + info.mProvider.label + position,
					drawable);

		} catch (Exception e) {
			// TODO: handle exception
		}
		return drawable;
	}
	// 加载完成的操作
	@Override
	public void displayResult(View view, Drawable drawable) {
		if (drawable == null) {
			return;
		}
		ImageView image = (ImageView) view;
		View parent = (View) view.getParent();
		int position = 0;
		if (parent != null) {
			parent = (View) parent.getParent();
			if (parent != null) {
				if (parent.getTag() instanceof ShortCutInfo) {
					position = 0;
				} else {

					position = (Integer) parent.getTag();
				}
			}
		}

		//如果是GO手册
		if (position == 0) {
			Drawable d = mContext.getResources().getDrawable(R.drawable.go_handbook_widget_icon);
			image.setImageDrawable(getFitIcon(d, false));
		} else if (position >= mInstall) {
			image.setImageDrawable(drawable);
			ColorMatrix colorMatrix = new ColorMatrix();
			colorMatrix.setSaturation(0f);
			image.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
		} else {
			image.setImageDrawable(drawable);
			GoWidgetProviderInfo info = mGoWidgetInfoList.get(position);
			final String pkgName = info.mProvider.provider.getPackageName();
			int versionCode = getVersionCodeByPkgName(mContext, pkgName);
			int newVersionCode = info.mVersionCode;
			if (newVersionCode > versionCode) {
				if (parent != null) {
					ImageView update = (ImageView) parent.findViewById(R.id.screen_edit_update);
					update.setVisibility(View.VISIBLE);
					update.setImageResource(R.drawable.appfunc_app_update);
					update.setOnClickListener(this);
					update.setTag(position);
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		long curTime = System.currentTimeMillis();
		if (curTime - mLastTime < CLICK_TIME) {
			return;
		}
		mLastTime = curTime;
		//如果是GO手册  则飞入屏幕进行添加
		if (v.getTag() instanceof ShortCutInfo) {
			if (!checkScreenVacant(1, 1) || !AddGoShortCutTab.resetTag(v)) {
				return;
			}
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_AUTO_FLY,
					DragFrame.TYPE_ADD_APP_DRAG, v, null);
			return;
		} else {
			mClickPosition = Integer.parseInt("" + v.getTag());

			GoWidgetProviderInfo info = mGoWidgetInfoList.get(getClickPosition());
			if (v instanceof ImageView) {
				String pkgName = info.mProvider.provider.getPackageName();
				gotoMarketForAPK(pkgName);
				return;
			}
			{
				if (getClickPosition() < mInstall && info != null) { // 加保护
					// 判断进行预览
					try {
						// SCREEN_EDIT_ADD_GOWIDGET
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
								IDiyMsgIds.SCREEN_EDIT_ADD_GOWIDGET, 0, info, null);
					} catch (Exception ex) {
						String pkgName = info.mGoWidgetPkgName;
						gotoMarketForAPK(pkgName);
					}
				} else {
					// 未安装的去goStore下载
					if (info != null) {
						String pkgName = info.mGoWidgetPkgName;
						// gotoMarketForAPK(pkgName);
						// add by chenguanyu 2012.7.10
						String title = info.mProvider.label;
						String content = mContext.getString(R.string.fav_app);
						String[] linkArray = new String[] { pkgName, info.mDownloadUrl };
						CheckApplication.downloadAppFromMarketFTPGostore(mContext, content,
								linkArray, LauncherEnv.GOLAUNCHER_GOOGLE_REFERRAL_LINK, title,
								System.currentTimeMillis(), Machine.isCnUser(mContext),
								CheckApplication.FROM_SCREENEDIT);
					}
				}
			}
		}
	}

	@Override
	public void clearData() {
		super.clearData();
		if (mItems != null) {
			mItems.clear();
			mItems = null;
		}
		if (mGoProgressBar != null) {
			mGoProgressBar = null;
		}
	}
	/**
	 * 
	 * <br>类描述:封装widget信息的bean
	 */
	public class WidgetInfo {
		public String mTitle;
		public Drawable mPreDrawable;
		public String mPackageName;

		public WidgetInfo(String packageName) {
			mPackageName = packageName;
		}
	}
	// 初始化widget列表
	private void initList() {
		mInstall = 1; //加入go手册  初始值改为 1
		mItems = new ArrayList<WidgetInfo>();
		mGoWidgetInfoList = new ArrayList<GoWidgetProviderInfo>();
		initData();
		getWidgetList();
	}

	private void initData() {
		if (mThemeInfos != null) {
			mThemeInfos.clear();
			mThemeInfos = null;
		}
		mThemeInfos = new ArrayList<ThemeInfoBean>();

		addTollTheme(mThemeInfos);

	}
	// 获取widget列表信息
	public void getWidgetList() {

		mGoWidgetInfoList.clear();

		mFinder = new GoWidgetFinder(mContext);
		mFinder.scanAllInstalledGoWidget();
		mProviderMap = mFinder.getGoWidgetInfosMap();

		Set<Entry<String, GoWidgetProviderInfo>> entryset = mProviderMap.entrySet();
		GoWidgetProviderInfo providerInfo = null;
		int size = 0;
		if (mThemeInfos != null) {
			size = mThemeInfos.size();
		}

		// 已安装 的
		// 判断是否可以更新 参照 GoWidgetAdapter
		ThemeInfoBean bean = null;
		if (mThemeInfos != null && mThemeInfos.size() > 0) {
			bean = mThemeInfos.get(0);
			if (bean != null && bean.getVersionCode() == null) {
				for (Entry<String, GoWidgetProviderInfo> entry : entryset) {
					mGoWidgetInfoList.add(entry.getValue());
					++mInstall; // 处理卸载后 状态没有变化
				}
			} else {
				String pkgName, beanPkgName;
				for (Entry<String, GoWidgetProviderInfo> entry : entryset) {
					pkgName = entry.getValue().mProvider.provider.getPackageName();
					for (int i = 0; i < size; i++) {
						bean = mThemeInfos.get(i);
						beanPkgName = bean.getPackageName();
						String versionCode = bean.getVersionCode();
						String versionName = bean.getVersionName();
						if (pkgName.equals(beanPkgName) && versionCode != null) {
							entry.getValue().mVersionCode = Integer.parseInt(versionCode);
							entry.getValue().mVersionName = versionName;
							break;
						}
					}
					++mInstall;
					mGoWidgetInfoList.add(entry.getValue());
				}
			}
		}
		// 排序
		sortGoWidgetInfoList(mGoWidgetInfoList);

		// 获取系统内置的
		final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
		final ArrayList<InnerWidgetInfo> innerWidgets = widgetManager.getInnerWidgetList();
		if (innerWidgets != null) {
			int count = innerWidgets.size();
			for (int i = count - 1; i >= 0; i--) {
				final InnerWidgetInfo innerWidgetInfo = innerWidgets.get(i);
				if (innerWidgetInfo.mBuildin == InnerWidgetInfo.BUILDIN_ALL) {
					// update by zhoujun 353渠道，不需要应用游戏中心的widget
					if (Statistics.APPGAME_WIDGET_PACKAGE_NAME
							.equals(innerWidgetInfo.mStatisticPackage)) {
						final ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
						if (channelConfig != null
								&& (!channelConfig.isNeedAppCenter() || !channelConfig
										.isNeedGameCenter())) {
							continue;
						}
					}
					// update by zhoujun 2012-08-11 end
					providerInfo = new GoWidgetProviderInfo(innerWidgetInfo.mWidgetPkg, "");
					providerInfo.mProvider.label = innerWidgetInfo.mTitle;
					providerInfo.mProvider.icon = innerWidgetInfo.mIconId;
					providerInfo.mInnerWidgetInfo = innerWidgetInfo;
					if (mIsDeleteView) {
						mGoWidgetInfoList.add(providerInfo);
					} else {
						mGoWidgetInfoList.add(providerInfo);
					}
					++mInstall;
				}
			}
		}

		// 未安装
		ThemeInfoBean infoBean = null;
		for (int i = 0; i < size; i++) {
			infoBean = mThemeInfos.get(i);
			providerInfo = mProviderMap.get(infoBean.getPackageName());
			if (providerInfo == null) {
				GoWidgetProviderInfo info = new GoWidgetProviderInfo("", "");
				info.mProvider.label = infoBean.getThemeName();
				if (infoBean.getPackageName() != null) {
					info.mGoWidgetPkgName = infoBean.getPackageName();
				}
				if (infoBean.getPreViewDrawableNames() != null
						&& infoBean.getPreViewDrawableNames().size() > 0) {
					info.mIconPath = infoBean.getPreViewDrawableNames().get(0);
				}
				if (infoBean.getThemeInfo() != null) {
					info.mDownloadUrl = infoBean.getThemeInfo().trim();
				}
				mGoWidgetInfoList.add(info);
			}
			providerInfo = null;
		}

	}

	/**
	 * 排序已安装列表： Go手册、天气、任务管理器、开关、日历、时钟、便笺、联系人、新浪微博、腾讯微博、 邮箱、FB
	 * Widget、TwiWidget、GO短信、书签、其他已安装应用、应用中心、store
	 * 
	 * @param installedInfos
	 */
	private void sortGoWidgetInfoList(ArrayList<GoWidgetProviderInfo> installedInfos) {
		HashMap<String, GoWidgetProviderInfo> mapInfos = new HashMap<String, GoWidgetProviderInfo>();
		List<GoWidgetProviderInfo> infos = new ArrayList<GoWidgetProviderInfo>();
		for (GoWidgetProviderInfo info : installedInfos) {
			if (info.mProvider.provider != null) {
				if (INSTALLED_PACKAGES.contains(info.mProvider.provider.getPackageName())) {
					infos.add(info);
				}
				mapInfos.put(info.mProvider.provider.getPackageName(), info);
			}
		}
		installedInfos.removeAll(infos);
		infos.clear();

		// 加入 GO手册
		GoWidgetProviderInfo go = new GoWidgetProviderInfo();
		infos.add(go);

		GoWidgetProviderInfo info = null;
		info = mapInfos.get(LauncherEnv.Plugin.RECOMMAND_GOWEATHEREX_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.TASK_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.SWITCH_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.CALENDAR_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.CLOCK_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.NOTE_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.CONTACT_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.SINA_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.TENCNT_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.EMAIL_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.FACEBOOK_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.TWITTER_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.RECOMMAND_GOSMS_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.BOOKMARK_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.APP_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		info = mapInfos.get(LauncherEnv.Plugin.STORE_PACKAGE);
		if (info != null) {
			infos.add(info);
		}
		installedInfos.addAll(0, infos);
	}
	// 加载收费主题
	public void addTollTheme(ArrayList<ThemeInfoBean> arrayList) {

		ArrayList<ThemeInfoBean> paidThemeInfoBeans = getPaidThemeInfoBeans();
		if (paidThemeInfoBeans == null || paidThemeInfoBeans.size() <= 0) {
			return;
		}

		int size = paidThemeInfoBeans.size();
		for (int i = size - 1; i >= 0; --i) {
			arrayList.add(0, paidThemeInfoBeans.get(i));
		}

		paidThemeInfoBeans.clear();
		paidThemeInfoBeans = null;

	}
	// 获取付费主题列表
	private ArrayList<ThemeInfoBean> getPaidThemeInfoBeans() {

		ArrayList<ThemeInfoBean> paidThemeInfoBeans = new ArrayList<ThemeInfoBean>();
		final String xmlFile = LauncherEnv.Path.GOTHEMES_PATH + "gowidget.xml";
		final String iconPath = LauncherEnv.Path.GOTHEMES_PATH + "icon/";

		final StringBuffer curVersionBuf = new StringBuffer();
		final StringBuffer recommendThemesBuf = new StringBuffer();
		// 判断语言
		if (xmlFile != null) {
			InputStream inputStream = null;
			try {
				String language = null;
				XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
				inputStream = new FileInputStream(xmlFile);
				parser.setInput(inputStream, null);
				while (parser.next() != XmlPullParser.END_DOCUMENT) {
					if (parser.getName() != null && parser.getName().equals(LANGUAGE)) {
						language = parser.getAttributeValue("", LANGUAGE);
						break;
					}
				}
				String tmpLaunguage = Locale.getDefault().getLanguage()
						+ Locale.getDefault().getCountry();
				if (language == null || !language.equals(tmpLaunguage)) {
					FileUtil.deleteFile(xmlFile);
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					try {
						inputStream.close();
						inputStream = null;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		// 解析xml
		new ThemeInfoParser().parseLauncherThemeXml(mContext, xmlFile, curVersionBuf,
				recommendThemesBuf, paidThemeInfoBeans, ThemeConstants.LAUNCHER_FEATURED_THEME_ID);
		boolean isHasSinaWeibo = false;
		for (ThemeInfoBean bean : paidThemeInfoBeans) {
			String packageName = bean.getPackageName();
			if (WIDGET_WEIBO.equals(packageName)) {
				isHasSinaWeibo = true;
				break;
			}
		}
		/*
		 * if(!isHasSinaWeibo && Machine.isCnUser(mContext)){ ThemeInfoBean bean
		 * = new ThemeInfoBean("新浪微博", WIDGET_WEIBO);
		 * bean.addDrawableName("sina_weibo");
		 * bean.setThemeInfo(WIDGET_WEIBO_URL); paidThemeInfoBeans.add(bean); }
		 */
		/**
		 * 
		 * <br>类描述:网络状态观察者
		 */
		class ConnectListener implements IConnectListener {
			private String mNewVersion;
			private int mRecommendCount;

			public ConnectListener() {
			}

			public ConnectListener(String version, int recommendCount) {
				mNewVersion = version;
				mRecommendCount = recommendCount;
			}

			@Override
			public void onStart(THttpRequest request) {
			}

			@Override
			public void onFinish(THttpRequest request, IResponse response) {
				if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
					AdResponseData resData = (AdResponseData) response.getResponse();

					ArrayList<AdElement> adList = resData.mAdList;
					if (adList != null && adList.size() > 0
							&& adList.get(0).mAdName.equals("version")) {
						mNewVersion = adList.get(0).mAdText;
						if (mNewVersion == null || mNewVersion.trim() == null
								|| mNewVersion.trim().length() <= 0) {
							mNewVersion = "0";
						}
						mRecommendCount = adList.get(0).mMaxDisplayCount;
						String curVersion = null;
						if (curVersionBuf != null && curVersionBuf.toString() != null
								&& curVersionBuf.toString().trim() != null
								&& curVersionBuf.toString().trim().length() > 0) {
							curVersion = curVersionBuf.toString().trim();

						}
						if (curVersion == null
								|| Integer.valueOf(mNewVersion) > (Integer.valueOf(curVersion))) {
							AdHttpAdapter adHttpAdapter = new AdHttpAdapter(mContext,
									new ConnectListener(mNewVersion, mRecommendCount));
							String verString = mContext.getResources().getString(
									R.string.curVersion);
							String fm = Statistics.getUid(mContext);
							String pid = String.valueOf("1013");
							adHttpAdapter.getAdData(null, null, verString, null, pid, 10, null, fm,
									Statistics.getVirtualIMEI(mContext));
						}
					} else {
						boolean result = saveAdElementAsPaidBeanToSDCard(adList, mNewVersion,
								mRecommendCount);
					}
				}
			}

			@Override
			public void onException(THttpRequest request, int reason) {
				StatisticsData.saveHttpExceptionDate(mContext, request, reason);
			}

			private boolean saveAdElementAsPaidBeanToSDCard(ArrayList<AdElement> adList,
					String version, int recommendCount) {
				boolean result = false;
				if (adList == null || adList.size() <= 0) {
					return result;
				}
				int count = adList.size();
				ArrayList<ThemeInfoBean> beansList = new ArrayList<ThemeInfoBean>(count);
				String pkgName = null;
				ThemeInfoBean paidBean = null;
				AdElement adElement = null;
				for (int i = 0; i < count; i++) {
					adElement = adList.get(i);
					pkgName = adElement.mAdOptData;
					String imgName = "gowidget" + String.valueOf(i);
					paidBean = new ThemeInfoBean();
					paidBean.setPackageName(pkgName);
					paidBean.setThemeName(adElement.mAdName);
					paidBean.setThemeInfo(adElement.mAdText);
					String versionCode = String.valueOf(adElement.mDelay);
					paidBean.setVersionCode(versionCode);
					paidBean.setVersionName(adElement.mAppID); // 暂用作版本名称的描述
					FileUtil.saveBitmapToSDFile(adElement.mIcon, iconPath + imgName,
							CompressFormat.PNG);
					paidBean.addDrawableName(imgName);

					beansList.add(paidBean);
				}
				result = new ThemeInfoParser().writeGoThemeToXml(version, recommendCount,
						beansList, xmlFile);
				return result;
			}
		}

		int recomendThemesCount = 0;
		if (recommendThemesBuf != null && recommendThemesBuf.toString() != null
				&& recommendThemesBuf.toString().trim() != null
				&& recommendThemesBuf.toString().trim().length() > 0) {
			try {
				recomendThemesCount = Integer.valueOf(recommendThemesBuf.toString().trim());
			} catch (Exception e) {
				//				Log.i("ThemeManager", "Integer.valueOf has exception");
			}
		}
		AdHttpAdapter adHttpAdapter = new AdHttpAdapter(mContext, new ConnectListener());
		final String pid = String.valueOf("1012");
		int pageCount = 10;
		String fm = Statistics.getUid(mContext);
		String curVerString = mContext.getResources().getString(R.string.curVersion);
		adHttpAdapter.getAdData(null, null, curVerString, null, pid, pageCount, null, fm,
				Statistics.getVirtualIMEI(mContext));

		return paidThemeInfoBeans;
	}

	@Override
	public void resetData() {
		initList();
	}

	/**
	 * 下载GoWidget
	 * 
	 * @param uriString
	 *            电子市场地址
	 * @param item
	 */
	private void gotoMarketForAPK(String pkgName) {
		// 直接跳转到GO Store的该插件的详情界面 再选择下载
		if (pkgName != null) {
			AppsDetail.gotoDetailDirectly(mContext, AppsDetail.START_TYPE_APPRECOMMENDED, pkgName);
//			GoStoreOperatorUtil.gotoStoreDetailDirectly(mContext, pkgName);
			GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_NO_WIDGET,
					mContext);
		}
	}

	/**
	 * 获取指定包的版本号
	 * 
	 * @author huyong
	 * @param context
	 * @param pkgName
	 */
	private int getVersionCodeByPkgName(Context context, String pkgName) {
		int versionCode = 0;
		if (pkgName != null) {
			PackageManager pkgManager = context.getPackageManager();
			try {
				PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
				versionCode = pkgInfo.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return versionCode;
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName) {
		super.handleAppChanged(msgId, pkgName);

		if (pkgName != null) {
			if (pkgName.startsWith(ICustomAction.MAIN_GOWIDGET_PACKAGE)
					|| pkgName.equals(TASK_MANAGER) || pkgName.equals(WIDGET_GOTORE)
					|| pkgName.equals(WIDGET_WEIBO) || pkgName.equals(LauncherEnv.GOSMS_PACKAGE)) {
				// 内置、GOWidget
				initList();
				if (mTabActionListener != null) {
					mTabActionListener.onRefreshTab(BaseTab.TAB_GOWIDGET, 0);
				}
			}

		}

	}

	private Object mMutex;
	private final static int LIST_INIT_OK = 1000;

	private void initListByLoading() {
		// 显示提示框
		showProgressDialog();
		new Thread("widget") {
			@Override
			public void run() {
				// 初始化数据
				synchronized (mMutex) {
					initList();
					// 对外通知
					Message msg = new Message();
					msg.what = LIST_INIT_OK;
					mHandler.sendMessage(msg);
				}
			};
		}.start();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LIST_INIT_OK :
					// 刷新
					if (mTabActionListener != null) {
						mTabActionListener.onRefreshTab(BaseTab.TAB_GOWIDGET, 0);
					}
					dismissProgressDialog();
					break;

				default :
					break;
			}
		};
	};
	// 显示进度条
	private void showProgressDialog() {
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditTabView mLayOutView = screenEditBoxFrame.getTabView();
			mGoProgressBar = (GoProgressBar) mLayOutView.findViewById(R.id.edit_tab_progress);
			if (mGoProgressBar != null) {
				mGoProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}
	// 取消进度条
	private void dismissProgressDialog() {
		if (mGoProgressBar != null) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getClickPosition() {
		return mClickPosition;
	}

	public void setClickPosition(int position) {
		mClickPosition = position;
	}
}