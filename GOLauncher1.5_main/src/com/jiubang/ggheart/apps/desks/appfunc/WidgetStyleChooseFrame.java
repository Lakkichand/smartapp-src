package com.jiubang.ggheart.apps.desks.appfunc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.mars.MImage;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.themescan.DotProgressBar;
import com.jiubang.ggheart.apps.gowidget.GoWidgetConstant;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetParser;
import com.jiubang.ggheart.apps.gowidget.WidgetParseInfo;
import com.jiubang.ggheart.apps.gowidget.widgetThemeChoose.IWidgetChooseFrame;
import com.jiubang.ggheart.components.DeskAlertDialog;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.PreviewThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.data.theme.parser.IParser;
import com.jiubang.ggheart.data.theme.parser.ParseWidgetTheme;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

public class WidgetStyleChooseFrame extends AbstractFrame
		implements
			android.view.View.OnClickListener,
			IWidgetChooseFrame {

	// private static DeskAlertDialog deskAlertDialog = null;
	private static boolean mDeskAlertDlgCanShow = true;
	private static final String KEYWORD = "gowidget";
	private static final String WIDGET_PACKAGE_PREFIX = "com.gau.go.launcherex.gowidget.";

	private ImageView mImgDownloadView;
	/*
	 * view的缓冲器
	 */
	private RecycleView mRecycleView;

	private DotProgressBar mProgressBar = null;

	private RelativeLayout mLayout;
	private LinearLayout mContenView;
	private WidgetStyleChooseView mChooseView;
	private DeskTextView mTitle;
	private Context mContext;

	private GoWidgetProviderInfo mAppInfo;

	private boolean mNeedToDownload;
	private boolean mNeedConfig;
	private String mConfigActivity;

	private DeskTextView mToDeskButton;

	// 底下的button
	private LinearLayout mAddToDesktop = null;

	// widget的样式列表数据
	private ArrayList<WidgetParseInfo> mWidgetDatas;

	private boolean mBFirstLoad = false;

	private LayoutInflater mInflater;

	public WidgetStyleChooseFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);

		mLayout = new RelativeLayout(activity);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		mLayout.setLayoutParams(rlp);
		mLayout.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// 判断当前是否锁屏
				if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
					LockScreenHandler.showLockScreenNotification(mActivity);
					return true;
				}

				return true;
			}
		});

		mWidgetDatas = new ArrayList<WidgetParseInfo>();

		mNeedToDownload = false;
		mNeedConfig = false;
		mConfigActivity = "";

		mContext = activity;
		mRecycleView = new RecycleView();

		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		creatView();

	}

	@Override
	public boolean isOpaque() {
		return true;
	}

	@Override
	public void onAdd() {
		super.onAdd();
		// 注册按键事件
		mFrameManager.registKey(this);
		mBFirstLoad = true;
	}

	@Override
	public void onRemove() {
		super.onRemove();
		// 反注册按键事件
		mFrameManager.unRegistKey(this);
		removeAndLeave();
	}

	@Override
	public void onResume() {
		super.onResume();
		// TODO: 重新取一次数据，重新画一次！
		// LogUnit.i("testTheme", "ThemeManager.handleAppChange()");
		if (!mBFirstLoad) {
			// mScanView.refreshDataAndView();
			// mContenView.refreshDataAndView();
			// Data();
		}
		mBFirstLoad = false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		switch (msgId) {
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
				Log.i("DiyThemeScanFrame", "orientaiton is " + param);

				// if (mContenView != null) {
				// // mContenView.changeOrientation(param);
				// }
				creatView();
				parseData();

				break;
			}

			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				// 移除自己
				GoLauncher.postMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
				break;
			}
			case IDiyMsgIds.WIDGETCHOOSE_PROGRAM : {

				if (object != null) {
					mAppInfo = (GoWidgetProviderInfo) object;
					parseData();
				}

				break;
			}

			default :
				break;
		}
		return false;
	}

	@Override
	public View getContentView() {
		// return mContenView;
		return mLayout;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			// 移除自己
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
					getId(), null, null);

			return true;
		}

		// key事件交给view处理
		if (mContenView != null && mContenView.onKeyUp(keyCode, event)) {
			return true;
		}

		return true;
	}

	private void initView() {
		MImage lightImg = new MImage(mContenView.getResources(), R.drawable.lightbar);
		MImage normalImg = new MImage(mContenView.getResources(), R.drawable.normalbar);

		// mProgressBar.setTotalNum(mChooseView.getCount());
		mProgressBar.setImage(normalImg, lightImg);

	}

	private void refreshIndicator() {
		mProgressBar.setTotalNum(mWidgetDatas.size());
	}

	private void removeAndLeave() {
		// 作清理工作
		recycle();
		// 要求回收一下
		OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
	}

	/**
	 * 回收资源
	 */
	protected void recycle() {
		if (mProgressBar != null) {
			mProgressBar.recycle();
		}

		if (mRecycleView != null) {
			mRecycleView.clear();
		}

		if (mWidgetDatas != null) {
			mWidgetDatas.clear();
		}
	}

	@Override
	public void onClick(View v) {
		if (mAddToDesktop == v) {
			if (!mNeedToDownload) {
				final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
				int gowidgetId = widgetManager.allocateWidgetId();
				int currentIndex = mChooseView.getCurrentScreen();

				// 越界保护
				if (currentIndex > (mWidgetDatas.size() - 1)) {
					currentIndex = mWidgetDatas.size() - 1;
				}
				if (currentIndex < 0) {
					return;
				}

				WidgetParseInfo info = mWidgetDatas.get(currentIndex);
				final AppWidgetProviderInfo provider = mAppInfo.mProvider;
				String widgetPackage = provider.provider.getPackageName();

				provider.minHeight = DrawUtils.dip2px(info.minHeight);
				provider.minWidth = DrawUtils.dip2px(info.minWidth);

				Bundle bundle = new Bundle();
				bundle.putInt(GoWidgetConstant.GOWIDGET_ID, gowidgetId);
				bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, info.type);
				bundle.putString(GoWidgetConstant.GOWIDGET_LAYOUT, info.layoutID);
				bundle.putParcelable(GoWidgetConstant.GOWIDGET_PROVIDER, provider);
				bundle.putString(GoWidgetConstant.GOWIDGET_THEME, info.themePackage);
				bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, info.themeType);

				// 内置类型
				int prototype = GoWidgetBaseInfo.PROTOTYPE_NORMAL;
				if (mAppInfo.mInnerWidgetInfo != null) {
					prototype = mAppInfo.mInnerWidgetInfo.mPrototype;
				} else {
					// 尝试找出其他内置的widget，如任务管理器widget
					InnerWidgetInfo innerWidgetInfo = widgetManager
							.getInnerWidgetInfo(widgetPackage);
					if (innerWidgetInfo != null) {
						prototype = innerWidgetInfo.mPrototype;
						// 更新包名为实际inflate xml的包名
						// widgetPackage = innerWidgetInfo.mInflatePkg;
						// ComponentName temp = new ComponentName(widgetPackage,
						// provider.provider.getClassName());
						// provider.provider = temp;
					}
				}

				bundle.putInt(GoWidgetConstant.GOWIDGET_PROTOTYPE, prototype);
				bundle.putBoolean(GoWidgetConstant.GOWIDGET_ADD_TO_SCREEN, true);

				if (!info.longkeyConfigActivty.equals("")) {
					ComponentName temp = new ComponentName(widgetPackage, info.longkeyConfigActivty);
					provider.configure = temp;
				}

				if (mNeedConfig) {
					if (mConfigActivity.equals("") && !info.configActivty.equals("")) {
						startConfigActivity(bundle, widgetPackage, info.configActivty);

					} else if (!mConfigActivity.equals("")) {
						startConfigActivity(bundle, widgetPackage, mConfigActivity);
					} else {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.ADD_GO_WIDGET, gowidgetId, bundle, null);
					}
				} else {
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.ADD_GO_WIDGET, gowidgetId, bundle, null);
				}

				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
				bundle = null;
			} else {
				gotoMarketForAPK(LauncherEnv.Market.BY_KEYWORD + KEYWORD);
			}
			return;
		}

		// ImageView view = mChooseView.getDownloadView();

		if (mImgDownloadView != null && v.equals(mImgDownloadView)) {
			String uriString = LauncherEnv.Market.BY_KEYWORD + KEYWORD;

			gotoMarketForAPK(uriString);
			// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
			// IDiyMsgIds.REMOVE_FRAME, getId(), null, null);
		}
	}

	private void creatView() {
		// if(mChooseView != null){
		// mChooseView.removeAllViews();
		// }

		mLayout.removeAllViews();
		LayoutInflater layoutInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mContenView = (LinearLayout) layoutInflater.inflate(R.layout.widgetchoosestyle, null);

		mProgressBar = (DotProgressBar) mContenView.findViewById(R.id.indicate);

		mChooseView = (WidgetStyleChooseView) mContenView.findViewById(R.id.choosecontentview);
		mChooseView.setIndicator(mProgressBar);
		mChooseView.setWidgetStyleChooseFrame(this);

		mTitle = (DeskTextView) mContenView.findViewById(R.id.widgettitle);

		mAddToDesktop = (LinearLayout) mContenView.findViewById(R.id.widgetchoosebutton);
		mToDeskButton = (DeskTextView) mContenView.findViewById(R.id.widgetchoosedesktoptext);

		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		mLayout.addView(mContenView, rlp);

		if (mAddToDesktop != null) {
			mAddToDesktop.setOnClickListener(this);
		}

		initView();
	}

	private void parseData() {

		if (mAppInfo == null) {
			return;
		}

		mWidgetDatas.clear();
		int count = 0;
		String packageName = mAppInfo.mProvider.provider.getPackageName();
		if (packageName.equals("")) // 没有安装GoWidget
		{
			mTitle.setText(mAppInfo.mProvider.label);
			WidgetParseInfo item = new WidgetParseInfo();
			item.resouceId = R.drawable.widget_downoad;
			item.resouces = mChooseView.getResources();
			item.title = mContext.getString(R.string.widget_choose_defaulstyle);
			mWidgetDatas.add(item);
			mToDeskButton.setText(mContext.getString(R.string.widget_choose_download));
			mNeedToDownload = true;

			mProgressBar.setIsShow(false);
			// mChooseView基本设置
			mChooseView.removeAllViews();
			count = mWidgetDatas.size();
			mChooseView.resetScroll(count);
			mChooseView.mShowView.add(0);
			addDownloadView();
		}
		// 内置的GOWidget
		else if (mAppInfo.mInnerWidgetInfo != null) {
			mTitle.setText(mAppInfo.mProvider.label);
			ArrayList<WidgetParseInfo> styleList = InnerWidgetParser.getWidgetParseInfos(mActivity,
					mAppInfo.mInnerWidgetInfo);
			if (styleList != null && styleList.size() > 0) {
				mWidgetDatas.addAll(styleList);
			}
			styleList = null;

			// 如果当前主题包有widget主题，则取主题包里面的预览图
			// 解析预览图
			try {
				replaceThemePreview(mAppInfo.mInnerWidgetInfo.mThemeConfig);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			if (mWidgetDatas.size() == 1) {
				mProgressBar.setIsShow(false);
			}
			refreshData();
		} else {
			try {
				Resources resources = mContext.getPackageManager().getResourcesForApplication(
						packageName);

				int resId = resources.getIdentifier(GoWidgetConstant.WIDGET_TITLE, "string",
						packageName);
				String title = resources.getString(resId);
				mTitle.setText(title);

				// 获取图片
				int drawableList = resources.getIdentifier(GoWidgetConstant.PREVIEW_LIST, "array",
						packageName);
				if (drawableList > 0) {
					final String[] extras = resources.getStringArray(drawableList);
					for (String extra : extras) {
						int res = resources.getIdentifier(extra, "drawable", packageName);
						if (res != 0) {
							WidgetParseInfo item = new WidgetParseInfo();
							item.resouceId = res;
							item.resouces = resources;

							item.themePackage = null;
							mWidgetDatas.add(item);
						}
					}
				}

				// 获取图片文字
				int titilList = resources.getIdentifier(GoWidgetConstant.STYLE_NAME_LIST, "array",
						packageName);
				if (titilList > 0) {
					final String[] titles = resources.getStringArray(titilList);
					count = 0;
					for (String titl : titles) {
						int res = resources.getIdentifier(titl, "string", packageName);
						if (res != 0) {
							WidgetParseInfo item = mWidgetDatas.get(count);
							item.title = resources.getString(res);
							count++;
						}
					}
				}

				// 获取类型
				int typeList = resources.getIdentifier(GoWidgetConstant.TYPE_LIST, "array",
						packageName);
				if (typeList > 0) {
					final int[] typeLists = resources.getIntArray(typeList);
					count = 0;
					for (int types : typeLists) {

						WidgetParseInfo item = mWidgetDatas.get(count);
						item.type = types;
						item.styleType = String.valueOf(types);
						count++;
					}
				}

				// 获取行数
				int rowList = resources.getIdentifier(GoWidgetConstant.ROW_LIST, "array",
						packageName);
				if (rowList > 0) {
					final int[] rowLists = resources.getIntArray(rowList);
					count = 0;
					for (int row : rowLists) {

						WidgetParseInfo item = mWidgetDatas.get(count);
						item.mRow = row;
						count++;
					}
				}

				// 获取列数
				int colList = resources.getIdentifier(GoWidgetConstant.COL_LIST, "array",
						packageName);
				if (colList > 0) {
					final int[] colListS = resources.getIntArray(colList);
					count = 0;
					for (int col : colListS) {

						WidgetParseInfo item = mWidgetDatas.get(count);
						item.mCol = col;
						count++;
					}
				}

				// 获取layout id
				int layoutIDList = resources.getIdentifier(GoWidgetConstant.LAYOUT_LIST, "array",
						packageName);
				if (layoutIDList > 0) {
					final String[] layouIds = resources.getStringArray(layoutIDList);
					count = 0;
					for (String id : layouIds) {

						WidgetParseInfo item = mWidgetDatas.get(count);
						item.layoutID = id;
						count++;
					}
				}

				// 获取竖屏最小宽度
				int minWidthVer = resources.getIdentifier(GoWidgetConstant.MIN_WIDTH, "array",
						packageName);
				if (minWidthVer > 0) {
					final int[] widthIds = resources.getIntArray(minWidthVer);
					count = 0;
					for (int w : widthIds) {

						WidgetParseInfo item = mWidgetDatas.get(count);
						item.minWidth = w;
						count++;
					}
				}

				// 获取竖屏最小高度
				int minHeightVer = resources.getIdentifier(GoWidgetConstant.MIN_HEIGHT, "array",
						packageName);
				if (minHeightVer > 0) {
					final int[] widthIds = resources.getIntArray(minHeightVer);
					count = 0;
					for (int h : widthIds) {

						WidgetParseInfo item = mWidgetDatas.get(count);
						item.minHeight = h;
						count++;
					}
				}

				// 获取layout id
				int configActivityList = resources.getIdentifier(GoWidgetConstant.CONFIG_LIST,
						"array", packageName);
				if (configActivityList > 0) {
					mNeedConfig = true;

					final String[] layouIds = resources.getStringArray(configActivityList);
					count = 0;
					for (String id : layouIds) {

						WidgetParseInfo item = mWidgetDatas.get(count);
						item.configActivty = id;
						count++;
					}
				} else {

					// 是否有统一的设置界面
					int resConfigId = resources.getIdentifier("configname", "string", packageName);
					if (resConfigId > 0) {
						mConfigActivity = resources.getString(resConfigId);
						if (mConfigActivity.equals("")) {
							mNeedConfig = false;
						} else {
							mNeedConfig = true;
						}
					}
				}

				int longkeyconfigActivityList = resources.getIdentifier(
						GoWidgetConstant.SETTING_LIST, "array", packageName);
				if (longkeyconfigActivityList > 0) {

					final String[] layouIds = resources.getStringArray(longkeyconfigActivityList);
					count = 0;
					for (String id : layouIds) {

						WidgetParseInfo item = mWidgetDatas.get(count);
						item.longkeyConfigActivty = id;
						count++;
					}
				}

				// 如果当前主题包有widget主题，则取主题包里面的预览图
				// 解析预览图
				String widgetPackage = mAppInfo.mProvider.provider.getPackageName();
				if (widgetPackage.length() > WIDGET_PACKAGE_PREFIX.length()) {
					widgetPackage = packageName.substring(WIDGET_PACKAGE_PREFIX.length());
				}
				String themeFileName = "widget_" + widgetPackage + ".xml";
				replaceThemePreview(themeFileName);

				if (mWidgetDatas.size() == 1) {
					mProgressBar.setIsShow(false);
				}

				refreshData();

			} catch (NameNotFoundException e) {
				e.printStackTrace();

				// 退出widget选择界面
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
			}
		}

		refreshIndicator();
	}

	// 更新预览图
	private void replaceThemePreview(String fileName) throws NameNotFoundException {
		XmlPullParser xmlPullParser = null;
		IParser parser = null;
		ThemeBean themeBean = null;
		String themePackage = GOLauncherApp.getThemeManager().getCurThemePackage();

		if (!themePackage.equals(LauncherEnv.PACKAGE_NAME)) {
			Log.i("WidgetStyleChooseFrame", "begin parserTheme " + fileName);

			InputStream inputStream = XmlParserFactory.createInputStream(mContext, themePackage,
					fileName);
			if (null == inputStream) {
				return;
			}

			xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
			themeBean = new PreviewThemeBean();
			if (xmlPullParser != null) {
				parser = new ParseWidgetTheme();
				parser.parseXml(xmlPullParser, themeBean);
				if (themeBean != null) {
					themeBean.setPackageName(themePackage);
				}
				parser = null;
			}

			// 关闭inputStream
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Resources resTheme = mContext.getPackageManager().getResourcesForApplication(
					themePackage);

			ArrayList<String> widgetStyleList = ((PreviewThemeBean) themeBean).getWidgetStyleList();
			ArrayList<String> widgetPreviewList = ((PreviewThemeBean) themeBean)
					.getWidgetPreviewList();
			ArrayList<String> widgetThemeTypeList = ((PreviewThemeBean) themeBean)
					.getWidgetThemeTypeList();

			ArrayList<WidgetParseInfo> newWidgetDatas = new ArrayList<WidgetParseInfo>();

			int themeType = -1;
			Boolean isReplaced = false;

			// 对所有样式进行遍历
			for (int index = 0; index < mWidgetDatas.size(); index++) {
				isReplaced = false;

				WidgetParseInfo widgetItem = mWidgetDatas.get(index);
				String style = widgetItem.styleType;
				if (null == style) {
					continue;
				}

				// 主题包内可能有某个样式的多套皮肤，也有可能没有
				for (int i = 0; i < widgetStyleList.size(); i++) {
					if (style.equals(widgetStyleList.get(i))) {
						int res = resTheme.getIdentifier(widgetPreviewList.get(i), "drawable",
								themePackage);

						try {
							if (i >= 0 && i < widgetThemeTypeList.size()) {
								themeType = Integer.parseInt(widgetThemeTypeList.get(i));
							} else {
								themeType = -1;
							}
						} catch (NumberFormatException e) {
							// TODO: handle exception
							themeType = -1;
						}

						WidgetParseInfo newWidgetInfo = (WidgetParseInfo) widgetItem.clone();
						newWidgetInfo.resouceId = res;
						newWidgetInfo.resouces = resTheme;

						newWidgetInfo.themePackage = themePackage;
						newWidgetInfo.themeType = themeType;

						newWidgetDatas.add(newWidgetInfo);

						isReplaced = true;
					}
				}

				if (!isReplaced) {
					WidgetParseInfo newWidgetInfo = (WidgetParseInfo) widgetItem.clone();
					newWidgetDatas.add(newWidgetInfo);
				}
			}

			mWidgetDatas.clear();
			mWidgetDatas = null;
			mWidgetDatas = newWidgetDatas;
		}
	}

	private void gotoMarketForAPK(String uriString) {
		if (mContext != null) {
			final String path = mAppInfo.mDownloadUrl;
			// 如果是中国用户
			if (Machine.isCnUser()) {
				String title = mContext.getString(R.string.downDialog_title);
				String linkArray[] = { uriString, path };
				showTip((Activity) mContext, title, linkArray);
				linkArray = null;
			} else {
				AppUtils.gotoBrowserIfFailtoMarket(mContext, uriString, path);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
			}
		}
	}

	public void startConfigActivity(Bundle bundle, String pkgName, String configure) {
		try {
			Log.i("widget", "config activity: " + mConfigActivity);
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClassName(pkgName, configure);
			intent.putExtras(bundle);
			mContext.startActivity(intent);
		} catch (Exception e) {
			// 退出widget选择界面
			// sendMessage(IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.REMOVE_FRAME,
			// getId(), null, null);
			Log.i("widgetChooser", "startConfigActivity error: " + pkgName + "." + configure);
		}
	}

	@Override
	public void removeView(int position) {

		if (position < 0 || mChooseView.mShowView.size() < 0) {
			return;
		}

		int index = mChooseView.mShowView.indexOf(position);

		if (index == -1) {
			return;
		}

		View view = mChooseView.getChildAt(index);

		if (view != null) {
			mChooseView.removeViewInLayout(view);
			mChooseView.mShowView.remove(index);
			mRecycleView.addView(view);
		}
	}

	public void refreshData() {
		// 样式总数
		int count = mWidgetDatas.size();

		mChooseView.removeAllViews();
		mChooseView.resetScroll(count);
		mRecycleView.clear();
		// mInfoCache.clear()
		if (count > 0) {
			addView(0);
		}
	}

	// widget不在，增加下载view
	public void addDownloadView() {

		WidgetParseInfo info = mWidgetDatas.get(0);

		FrameLayout viewFrame = (FrameLayout) mInflater.inflate(R.layout.widgetchoosesubview, null);

		mImgDownloadView = (ImageView) viewFrame.findViewById(R.id.widgetsubviewimage);
		if (mImgDownloadView != null) {
			mImgDownloadView.setImageDrawable(info.resouces.getDrawable(info.resouceId));
			mImgDownloadView.setClickable(true);
			mImgDownloadView.setFocusable(true);
			mImgDownloadView.setOnClickListener(this);
		}

		DeskTextView texView = (DeskTextView) viewFrame.findViewById(R.id.widgetstyletitle);
		texView.setText(info.title);

		DeskTextView downLoad = (DeskTextView) viewFrame.findViewById(R.id.download);
		downLoad.setText(R.string.widget_choose_downloadnow);

		DeskTextView downLoadInfo = (DeskTextView) viewFrame.findViewById(R.id.downlaodinfo);
		downLoadInfo.setText(R.string.widget_choose_downloadinfo);

		GridView gridview = (GridView) viewFrame.findViewById(R.id.widgetgridview);

		ImageAdapter adpter = new ImageAdapter(mContext);
		adpter.changeResouce(info.mRow, info.mCol);
		gridview.setAdapter(adpter);

		mChooseView.addView(viewFrame);
	}

	public void showTip(final Activity activity, final String title, final String[] linkArray) {

		if (mDeskAlertDlgCanShow) {
			String content = null;// 对话框内容
			String positiveBtnText = null;// 左边按钮文字
			String negativeBtnText = null;// 右边按钮文字
			// 是否为中国用户
			final boolean isCN = Machine.isCnUser(activity);
			if (isCN) {
				content = activity.getString(R.string.downDialog_downForWhere);
				positiveBtnText = activity.getString(R.string.downDialog_downForMarket);
				negativeBtnText = activity.getString(R.string.downDialog_downForGoLauncher);
			} else {
				content = activity.getString(R.string.downDialog_downForWhere);
				positiveBtnText = activity.getString(R.string.ok);
				negativeBtnText = activity.getString(R.string.cancle);
			}

			final DeskAlertDialog deskAlertDialog = new DeskAlertDialog(activity);
			deskAlertDialog.setTitle(title);
			deskAlertDialog.setMessage(content);
			deskAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveBtnText,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (null == linkArray) {
								return;
							}

							// 如果有電子市場
							if (AppUtils.isMarketExist(mActivity)) {
								AppUtils.gotoMarket(mActivity, linkArray[0]);
							} else {
								AppUtils.gotoBrowser(mActivity, linkArray[1]);
							}

							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
						}
					});
			deskAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeBtnText,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							if (isCN) {
								AppUtils.gotoBrowser(mActivity, linkArray[1]);
							}
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IFrameworkMsgId.REMOVE_FRAME, getId(), null, null);
						}
					});
			deskAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					mDeskAlertDlgCanShow = true;
					deskAlertDialog.selfDestruct();
				}
			});

			mDeskAlertDlgCanShow = false;
			deskAlertDialog.show();
		}

	}

	// widget存在，增加样式view
	@Override
	public void addView(int position) {

		int count = mWidgetDatas.size();

		if (position < 0 || position >= count) {
			return;
		}

		mChooseView.mShowView.add(position);

		View view = mRecycleView.getView();

		WidgetParseInfo widgetInfo = mWidgetDatas.get(position);

		ViewHolder viewHolder = (ViewHolder) view.getTag();
		viewHolder.position = position;

		if (widgetInfo.title != null) {
			viewHolder.name.setText(widgetInfo.title);
		} else {
			viewHolder.name.setText("");
		}

		try {
			viewHolder.imagePreview.setImageDrawable(widgetInfo.resouces
					.getDrawable(widgetInfo.resouceId));
		} catch (NotFoundException e) {
			// 没取到对应的预览图
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
		}
		ImageAdapter adpter = new ImageAdapter(mContext);
		adpter.changeResouce(widgetInfo.mRow, widgetInfo.mCol);
		viewHolder.gridView.setAdapter(adpter);

		mChooseView.addView(view);
	}

	/*
	 * view 的缓冲器,用来产生或是回收view
	 */
	class RecycleView {
		private ArrayList<View> mScrapViews;

		public RecycleView() {
			mScrapViews = new ArrayList<View>();
		}

		public void addView(View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			holder.imagePreview.setImageBitmap(null);
			holder.name.setText("");
			this.mScrapViews.add(view);
		}

		public View getView() {
			int i = mScrapViews.size();
			int j;

			if (i > 0) {
				j = i - 1;
				// Log.i(TAG_Wiget, "return a exit view");
				return mScrapViews.remove(j);
			}

			View viewFrame;

			viewFrame = mInflater.inflate(R.layout.widgetchoosesubview, null, false);

			ViewHolder holder = new ViewHolder();

			holder.imagePreview = (ImageView) viewFrame.findViewById(R.id.widgetsubviewimage);

			holder.name = (DeskTextView) viewFrame.findViewById(R.id.widgetstyletitle);

			holder.gridView = (GridView) viewFrame.findViewById(R.id.widgetgridview);

			viewFrame.setTag(holder);

			return viewFrame;
		}

		public void clear() {
			if (mScrapViews != null) {
				mScrapViews.clear();
			}
		}
	}

	/*
	 * viewholder 用来显示并传递数据的view
	 */
	class ViewHolder {
		/*
		 * view在列表的位置
		 */
		int position;

		/*
		 * 预览图片
		 */
		ImageView imagePreview;

		/*
		 * 样式的名字
		 */
		DeskTextView name;

		/*
		 * 样式的正方形显示
		 */
		GridView gridView;

		ViewHolder() {
		}
	}

	// 处理预览界面体现样式的正方形
	private class ImageAdapter extends BaseAdapter {

		private Context mContext;

		static final int GREEN = R.drawable.widgetchoosestyle_green;
		static final int GRAY = R.drawable.widgetchoosestyle_gray;
		static final int COL = 4;

		private Integer[] mThumbIds = { GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY, GRAY,
				GRAY, GRAY, GRAY, GRAY, GRAY, GRAY };

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public void changeResouce(int row, int col) {

			if (row > COL || col > COL) {

				return;
			}

			for (int i = 0; i < mThumbIds.length; i++) {
				if (i % COL < col) {

					if (i >= COL * row) {
						break;
					}
					mThumbIds[i] = GREEN;
				}
			}
		}

		@Override
		public int getCount() {
			return mThumbIds.length;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;

			if (convertView == null) {
				imageView = new ImageView(mContext);
				imageView.setLayoutParams(new GridView.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
				// 设置ImageView宽高
				// imageView.setAdjustViewBounds(false);
				// imageView.setScaleType(ImageView.ScaleType.FIT_XY);

				// imageView.setPadding(8, 8, 8, 8);
			} else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageResource(mThumbIds[position]);
			return imageView;
		}
	}

	@Override
	public void updateCurrentView(int newScreen, int oldScreen) {
		// TODO Auto-generated method stub

	}

}
