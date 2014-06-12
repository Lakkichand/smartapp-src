package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.file.FileUtil;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.DrawableCacheManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.IDrawableLoader;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditLargeTabView;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditLayout;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditSkinMenu;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.GoWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.AbsWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.GoWidgetConstant;
import com.jiubang.ggheart.apps.gowidget.GoWidgetManager;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetInfo;
import com.jiubang.ggheart.apps.gowidget.InnerWidgetParser;
import com.jiubang.ggheart.apps.gowidget.WidgetParseInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreChannelControl;
import com.jiubang.ggheart.apps.gowidget.widgetThemeChoose.WidgetThemeChooseFrame;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.GoWidgetBaseInfo;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.PreviewSpecficThemeBean;
import com.jiubang.ggheart.data.theme.bean.ThemeBean;
import com.jiubang.ggheart.data.theme.parser.ParseSpecficWidgetTheme;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * widget 二级页面
 */
public class WidgetSubTab extends BaseTab implements IDrawableLoader {

	private ArrayList<View> mItems;
	private static final long CLICK_TIME = 700;
	private static final float GOWIDGETIMAGESCALE = 0.56f; // widget预览图片相对添加界面的高宽比例
	private static final float GOWIDGETTITLEHEIGHT = 0.15f; // widget标题预览相对于添加界面的高度比例，宽度与图片相同
	private long mLastTime; // 上次的点击时间
	private int mShowListInitMsg; // list数据初始化状态
	private Integer mPosition = 0;	
	private DrawableCacheManager mCacheManager; // Drawable缓存管理单例

	public WidgetSubTab(Context context, String tag, int level) {
		super(context, tag, level);
		mCacheManager = DrawableCacheManager.getInstance();	
		mLastTime = System.currentTimeMillis(); 
		mIsNeedAsyncLoadData = true;
		initListByLoading(); // 异步数据加载
	}

	@Override
	public ArrayList<Object> getDtataList() {
		return null;
	}

	@Override
	public int getItemCount() {
		if (mItems != null) {
			return mItems.size();
		}
		return 0;
	}

	@Override
	public View getView(int position) {
		View view = mItems.get(position);
		if (view.getTag() != null) {
			if (view.getTag() instanceof AbsWidgetInfo) {
				AbsWidgetInfo info = (AbsWidgetInfo) view.getTag();
				info.mAddIndex = position;
			}
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		long curTime = System.currentTimeMillis();
		if (curTime - mLastTime < CLICK_TIME) {
			return;
		}
		mLastTime = curTime;
		if (v instanceof TextView && v.getTag() != null && v.getTag() instanceof Integer) {
			if (null != mSkinMenu) {
				mSkinMenu.dismiss();
			}
			int position = (Integer) v.getTag();
			if (mPosition == position) {
				return;
			}
			mPosition = position;
			if (mItems != null) {
				mItems.clear();
				mItems = null;
			}
			mItems = (ArrayList<View>) selectSkin(mPosition);
			// 刷新
			if (mTabActionListener != null) {
				mTabActionListener.onRefreshTab(BaseTab.TAB_ADDGOWIDGET, 0);
			}
		} else {
			// 添加至桌面
			AbsWidgetInfo info = null;
			if (null != v.getTag() && v.getTag() instanceof AbsWidgetInfo) {
				info = (AbsWidgetInfo) v.getTag();
			}
			if (null == info || !checkScreenVacant(info.mCol, info.mRow)) {
				return;
			}
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_AUTO_FLY, DragFrame.TYPE_ADD_WIDGET_DRAG ,
					v, null);
		}

		super.onClick(v);
	}

	@Override
	public boolean onLongClick(View v) {
		// AbsWidgetInfo info = null ;
		// if(null != v.getTag() &&v.getTag() instanceof AbsWidgetInfo){
		// info = (AbsWidgetInfo) v.getTag();
		// }
		// if(null == info){
		// return false;
		// }
		//
		// ArrayList<Object> list = new ArrayList<Object>();
		// list.add(Workspace.getLayoutScale());
		// list.add(0);
		// list.add(0);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.DRAG_START, DragFrame.TYPE_ADD_WIDGET_DRAG, v, list);

		return false;
	}

	@Override
	public void clearData() {
		if (mItems != null) {
			mItems.clear();
			mItems = null;
		}
		if (null != mSkinMenu) {
			mSkinMenu.cleanup();
		}
		if (mGoProgressBar != null) {
			mGoProgressBar = null;
		}
		// 清空其余资源
		cleanup();
		super.clearData();
	}

	@Override
	public void resetData() {
		// TODO Auto-generated method stub

	}

	private final static int LIST_INIT_OK = 1000;

	private final static int LIST_DEFAULT_OK = 2000;
	private final static int LIST_DEFAULT_FAIL = 3000;
	private GoProgressBar mGoProgressBar;

	private void initListByLoading() {
		showProgressDialog();
		new Thread(ThreadName.SCREEN_EDIT_THEMETAB) {
			@Override
			public void run() {
				// 初始化数据
				synchronized (this) {
					ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
							.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
					if (screenEditBoxFrame.getInfo() != null) {
						mItems = (ArrayList<View>) getDefaultWidgetPic(screenEditBoxFrame.getInfo());
					}
				}
			};
		}.start();
	}

	/**
	 * 功能简述:更新皮肤按钮的可见性状态
	 * 
	 */
	private void updateSkinImageView() {
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		ImageView skinImageView = (ImageView) screenEditBoxFrame.getLargeTabView().findViewById(
				R.id.tabs_gowidget_skin);
		if (mDatasDetail == null) {
			return;
		}
		if (mDatasDetail.size() > 1) {
			skinImageView.setVisibility(View.VISIBLE);
		} else {
			skinImageView.setVisibility(View.INVISIBLE);
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LIST_INIT_OK :
					if (null == mDatasDetail) {
						dismissProgressDialog();
						return;
					}
					initmOtherStylesInfos(mDatasDetail.get(0));
					for (int i = 0; i < mDatasDetail.get(0).size(); i++) {

						addSubView(mDatasDetail.get(0).get(i));
					}
					// 加入info页
					initInfoPage();
					// 刷新
					if (mTabActionListener != null) {
						mTabActionListener.onRefreshTab(BaseTab.TAB_ADDGOWIDGET, 0);
					}
					mShowListInitMsg = LIST_INIT_OK;
					updateSkinImageView();
					dismissProgressDialog();
					break;

				case LIST_DEFAULT_OK :
					if (null == mWidgetDatasScan) {
						dismissProgressDialog();
						return;
					}
					initDefaultWidgetPicViews();
					// 加入info页
					initInfoPage();
					// 刷新
					if (mTabActionListener != null) {
						mTabActionListener.onRefreshTab(BaseTab.TAB_ADDGOWIDGET, 0);
					}
					mShowListInitMsg = LIST_DEFAULT_OK;
					updateSkinImageView();
					dismissProgressDialog();
					break;

				case LIST_DEFAULT_FAIL :
					dismissProgressDialog();
					break;
				default :
					break;
			}
		};
	};

	private void showProgressDialog() {
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditLargeTabView mLayOutView = screenEditBoxFrame.getLargeTabView();
			mGoProgressBar = (GoProgressBar) mLayOutView.findViewById(R.id.edit_tab_progress);
			if (mGoProgressBar != null) {
				mGoProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void dismissProgressDialog() {
		if (mGoProgressBar != null) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 皮肤弹出菜单组件
	 */
	private ScreenEditSkinMenu mSkinMenu;

	/**
	 * 显示皮肤选择
	 */
	public void showSkinsSelecte(View v) {
		// if (null == mDatas || mDatas.size() <= 1 || !mScanSkinFinished)
		// return;
		if (null == mDatasDetail) {
			return;
		}
		if (null == mSkinMenu) {
			int size = mDatasDetail.size();
			String[] teStrings = new java.lang.String[size];
			for (int i = 0; i < size; i++) {
				try {
					teStrings[i] = mDatasDetail.get(i).get(0).title;
				} catch (Exception e) {
				}
			}
			mSkinMenu = new ScreenEditSkinMenu(mContext, teStrings);
			int mBtnsLayoutHeight = v.getHeight();
			mSkinMenu.setParrentHeight(mBtnsLayoutHeight);
			mSkinMenu.setmItemClickListener(this);
		}

		mSkinMenu.show(v);
	}

	GoWidgetProviderInfo mGoWidgetProviderInfo;
	// widget默认风格的样式列表数据
	private ArrayList<WidgetParseInfo> mWidgetDatasScan;

	// widget其他风格的样式数据
	private LinkedList<GoWidgetInfo> mOtherStylesInfosScan;
	// 是否需要带设置的
	private boolean mNeedConfig;
	private List<View> mItemViews;

	private String mConfigActivity = "";	// 带有设置的widget的activity名
	private String mCurrentSkinPkg;
	// 处理单个widget部分
	private int[] mRowLists = null; // 几行
	private int[] mColLists = null; // 几列
	private int[] mStyleTypeList = null; // widget样式数目	
	private final static float DEFAULT_DENSITY = 1.5F; // 480x800下的density
	// 扫描出来有几套风格皮肤数据
	private LinkedList<GoWidgetInfo> mWidgetDatasDetail;

	// 这个gowidget对应的每套风格皮肤的所有数据
	private ConcurrentHashMap<Integer, LinkedList<GoWidgetInfo>> mDatasDetail;
	// 初始化widget view的所有信息
	public List<View> getDefaultWidgetPic(GoWidgetProviderInfo info) {
		mItemViews = new ArrayList<View>();
		mGoWidgetProviderInfo = info;
		mWidgetDatasScan = new ArrayList<WidgetParseInfo>();
		try {
			// 1,默认
			parseData();
			// selectDefaultSkin();
			// 2,开始处理不同皮肤的
			initWidgetArray(info);
			getAllGoWidgetInfos(info);
			// 3,异步扫描
			mDatasDetail = new ConcurrentHashMap<Integer, LinkedList<GoWidgetInfo>>();
			scanSkins();
		} catch (Exception e) {
			Message msg = new Message();
			msg.what = LIST_DEFAULT_FAIL;
			mHandler.sendMessage(msg);
		}
		return mItemViews;
	}
    // 扫描主题的第一种方式
	private void parseData() {

		if (null == mGoWidgetProviderInfo) {
			return;
		}

		mWidgetDatasScan.clear();
		int count = 0;
		String packageName = mGoWidgetProviderInfo.mProvider.provider.getPackageName();
		if (packageName.equals("")) // 没有安装GoWidget
		{
			WidgetParseInfo item = new WidgetParseInfo();
			item.resouceId = R.drawable.widget_downoad;
			item.resouces = mContext.getResources();
			item.title = mContext.getString(R.string.widget_choose_defaulstyle);
			mWidgetDatasScan.add(item);
		}
		// 内置的GOWidget
		else if (mGoWidgetProviderInfo.mInnerWidgetInfo != null) {
			ArrayList<WidgetParseInfo> styleList = InnerWidgetParser.getWidgetParseInfos(mContext,
					mGoWidgetProviderInfo.mInnerWidgetInfo);
			if (styleList != null && styleList.size() > 0) {
				mWidgetDatasScan.addAll(styleList);
			}
			styleList = null;

			// 如果当前主题包有widget主题，则取主题包里面的预览图
			// 解析预览图
			// try {
			// replaceThemePreview(mGoWidgetProviderInfo.mInnerWidgetInfo.mThemeConfig);
			// } catch (NameNotFoundException e) {
			// }
		} else {
			try {
				Resources resources = mContext.getPackageManager().getResourcesForApplication(
						packageName);

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
							mWidgetDatasScan.add(item);
						}
					}
				}

				// 获取标题
				int titilList = resources.getIdentifier(GoWidgetConstant.STYLE_NAME_LIST, "array",
						packageName);
				if (titilList > 0) {
					final String[] titles = resources.getStringArray(titilList);
					count = 0;
					for (String titl : titles) {
						int res = resources.getIdentifier(titl, "string", packageName);
						if (res != 0) {
							WidgetParseInfo item = mWidgetDatasScan.get(count);
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

						WidgetParseInfo item = mWidgetDatasScan.get(count);
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

						WidgetParseInfo item = mWidgetDatasScan.get(count);
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

						WidgetParseInfo item = mWidgetDatasScan.get(count);
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

						WidgetParseInfo item = mWidgetDatasScan.get(count);
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

						WidgetParseInfo item = mWidgetDatasScan.get(count);
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

						WidgetParseInfo item = mWidgetDatasScan.get(count);
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

						WidgetParseInfo item = mWidgetDatasScan.get(count);
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

						WidgetParseInfo item = mWidgetDatasScan.get(count);
						item.longkeyConfigActivty = id;
						count++;
					}
				}

				// 如果当前主题包有widget主题，则取主题包里面的预览图
				// 解析预览图
				// String widgetPackage =
				// mGoWidgetProviderInfo.mProvider.provider.getPackageName();
				// if (widgetPackage.length() > WIDGET_PACKAGE_PREFIX.length())
				// {
				// widgetPackage =
				// packageName.substring(WIDGET_PACKAGE_PREFIX.length());
				// }
				// String themeFileName = "widget_" + widgetPackage + ".xml";
				// replaceThemePreview(themeFileName);
			} catch (NameNotFoundException e) {
				// 退出widget选择界面
				// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				// IDiyMsgIds.REMOVE_FRAME, IDiyFrameIds.GOWIDGET_MANAGER_FRAME,
				// null, null);
			} catch (Exception e) {
				Toast.makeText(mContext, "Parse default style data error.", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	public void selectDefaultSkin() {
		mCurrentSkinPkg = mGoWidgetProviderInfo.mProvider.provider.getPackageName();
		// removeAllViews();
		initDefaultWidgetPicViews();
		// updateScrollerAndIndicatorData();
		mIsDefaultStyle = true;
	}
	// 初始化默认风格
	public void initDefaultWidgetPicViews() {
		if (null == mWidgetDatasScan || mWidgetDatasScan.isEmpty()) {
			return;
		}

		for (int i = 0; i < mWidgetDatasScan.size(); i++) {
			WidgetParseInfo info = mWidgetDatasScan.get(i);
			addSubView(info);
		}

	}

	public List<View> selectSkin(int position) {
		mShowListInitMsg = LIST_INIT_OK;
		if (position == 0) {
			// 设置成默认
			mIsDefaultStyle = true;
			for (int i = 0; i < mWidgetDatasScan.size(); i++) {
				WidgetParseInfo info = mWidgetDatasScan.get(i);
				addSubView(info);
			}

		} else {
			// mShowListInitMsg = LIST_DEFAULT_OK;
			try {
				initOtherWidgetPicViews(mDatasDetail.get(position));
			} catch (Exception e) {
			}
		}
		// 加上info页
		initInfoPage();
		return mItemViews;
	}

	public void initOtherWidgetPicViews(LinkedList<GoWidgetInfo> linkedList) {
		if (null == linkedList || linkedList.isEmpty()) {
			return;
		}
		mOtherStylesInfosScan = linkedList;
		int size = linkedList.size();
		for (int i = 0; i < size; i++) {
			GoWidgetInfo info = linkedList.get(i);
			addSubView(info);
			mCurrentSkinPkg = info.packageName;
		}
		mIsDefaultStyle = false;

	}
	// 设置widget展示view
	private void addSubView(WidgetParseInfo info) {
		if (null == info) {
			return;
		}
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout viewFrame = (LinearLayout) inflater.inflate(
				R.layout.screen_edit_gowidget_choosepre_subview, null);
		// Add by xiangliang  对分辨率小于800的屏幕进行根据比例适配widget展示高度
		if (DrawUtils.sHeightPixels < 800) {
			ImageView image = (ImageView) viewFrame.findViewById(R.id.screenedit_upperhalf);
			LinearLayout titleLinear = (LinearLayout) viewFrame.findViewById(R.id.downhalf);
			LinearLayout.LayoutParams imageparms = (LinearLayout.LayoutParams) image
					.getLayoutParams();
			imageparms.height = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE * GOWIDGETIMAGESCALE);
			imageparms.width = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE * GOWIDGETIMAGESCALE);
			image.setLayoutParams(imageparms);
			LinearLayout.LayoutParams titleparms = (LinearLayout.LayoutParams) titleLinear
					.getLayoutParams();
			titleparms.height = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE * GOWIDGETTITLEHEIGHT);
			titleparms.width = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE * GOWIDGETIMAGESCALE);
			titleLinear.setLayoutParams(titleparms);
		}
		GridView gridview = (GridView) viewFrame.findViewById(R.id.widgetgridview);
		ImageAdapter adpter = new ImageAdapter(mContext);
		adpter.changeResouce(info.mRow, info.mCol);
		gridview.setAdapter(adpter);
		gridview.setEnabled(false);
		DeskTextView textView = (DeskTextView) viewFrame.findViewById(R.id.widgetstyletitle);
		textView.setText(info.title);
		viewFrame.setTag(info);
		mItemViews.add(viewFrame);
	}
	// 设置widget展示view
	private void addSubView(GoWidgetInfo info) {
		if (null == info) {
			return;
		}
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout viewFrame = (LinearLayout) inflater.inflate(
				R.layout.screen_edit_gowidget_choosepre_subview, null);
		// Add by xiangliang  对分辨率小于800的屏幕进行根据比例适配widget展示高度
		if (DrawUtils.sHeightPixels < 800) {
			//			 viewFrame = ( LinearLayout ) inflater.inflate(
			//						R.layout.screen_edit_gowidget_choosepre_subview_small, null);		
			ImageView image = (ImageView) viewFrame.findViewById(R.id.screenedit_upperhalf);
			LinearLayout titleLinear = (LinearLayout) viewFrame.findViewById(R.id.downhalf);
			LinearLayout.LayoutParams imageparms = (LinearLayout.LayoutParams) image
					.getLayoutParams();
			imageparms.height = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE * GOWIDGETIMAGESCALE);
			imageparms.width = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE * GOWIDGETIMAGESCALE);
			image.setLayoutParams(imageparms);
			LinearLayout.LayoutParams titleparms = (LinearLayout.LayoutParams) titleLinear
					.getLayoutParams();
			titleparms.height = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE * GOWIDGETTITLEHEIGHT);
			titleparms.width = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE * GOWIDGETIMAGESCALE);
			titleLinear.setLayoutParams(titleparms);
		}

		GridView gridview = (GridView) viewFrame.findViewById(R.id.widgetgridview);
		ImageAdapter adpter = new ImageAdapter(mContext);
		adpter.changeResouce(info.mRow, info.mCol);
		gridview.setAdapter(adpter);
		gridview.setEnabled(false);

		DeskTextView textView = (DeskTextView) viewFrame.findViewById(R.id.widgetstyletitle);
		String lastTitle = String.format(info.title + "(%dx%d)", info.mCol, info.mRow);
		textView.setText(lastTitle);
		viewFrame.setTag(info);
		mItemViews.add(viewFrame);
	}
	// 异步加载图片
	@Override
	public Drawable loadDrawable(int position, Object arg) {
		Drawable drawable = null;
		try {
			if (mItems != null && position >= 0 && position <= mItems.size() - 2) {
				GoWidgetInfo widgetInfo = null;
				Resources resources = null;
				if (mShowListInitMsg == LIST_INIT_OK) {
					widgetInfo = mDatasDetail.get(Integer.valueOf(mPosition)).get(position);
					if (widgetInfo == null) {
						return null;
					}
					drawable = mCacheManager
							.getDrawableFromCache(DrawableCacheManager.CACHE_WIDGETSUBTAB
									+ widgetInfo.packageName + widgetInfo.resouceId);
					if (drawable != null) {
						return drawable;
					}
					try {
						resources = widgetInfo.resouces;
						if (resources != null) {
							drawable = resources.getDrawable(widgetInfo.resouceId);
							mCacheManager.saveToCache(DrawableCacheManager.CACHE_WIDGETSUBTAB
									+ widgetInfo.packageName + widgetInfo.resouceId, drawable);
							return drawable;
						}
					} catch (OutOfMemoryError e) {
						OutOfMemoryHandler.handle();
					}
				} else {
					WidgetParseInfo parseInfo = null;
					parseInfo = mWidgetDatasScan.get(position);
					if (parseInfo == null) {
						return null;
					}
					resources = parseInfo.resouces;
					drawable = mCacheManager
							.getDrawableFromCache(DrawableCacheManager.CACHE_WIDGETSUBTAB
									+ parseInfo.themePackage + parseInfo.resouceId);
					if (drawable != null) {
						return drawable;
					}
					try {
						if (resources != null) {
							drawable = resources.getDrawable(parseInfo.resouceId);
							if (drawable != null) {
								BitmapUtility.zoomDrawable(mContext, drawable, drawable
										.getIntrinsicWidth(), (int) mContext.getResources()
										.getDimension(R.dimen.screen_edit_gowidget_preimage_heght));
								mCacheManager.saveToCache(DrawableCacheManager.CACHE_WIDGETSUBTAB
										+ parseInfo.themePackage + parseInfo.resouceId, drawable);
								return drawable;
							}
						}
					} catch (OutOfMemoryError e) {
						OutOfMemoryHandler.handle();
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return drawable;
	}
	// 加载完成的显示
	@Override
	public void displayResult(View view, Drawable drawable) {
		if (drawable != null) {
			ImageView upperHalfIV = (ImageView) view;
			upperHalfIV.setImageDrawable(drawable);
		}
	}


	/**
	 * 
	 * <br>功能详细描述:	处理预览界面体现样式的正方形
	 *
	 */
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
				imageView.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT,
						LayoutParams.WRAP_CONTENT));
			} else {
				imageView = (ImageView) convertView;
			}
			imageView.setImageResource(mThumbIds[position]);
			return imageView;
		}
	}

	// 第二步    初始化widget数据列表 
	private void initWidgetArray(GoWidgetProviderInfo info) {
		try {
			// if (null == mDetailScan.getmInfoBean() ||
			// !(mDetailScan.getmInfoBean() instanceof GoWidgetProviderInfo))
			// return;
			Resources widgetResources = null;
			if (null != info.mInnerWidgetInfo
					&& info.mInnerWidgetInfo.mBuildin == InnerWidgetInfo.BUILDIN_ALL) {
				// gostore
				mRowLists = mContext.getResources().getIntArray(info.mInnerWidgetInfo.mRowList);
				mColLists = mContext.getResources().getIntArray(info.mInnerWidgetInfo.mColumnList);
				mStyleTypeList = mContext.getResources().getIntArray(
						info.mInnerWidgetInfo.mTypeList);
				return;
			}
			String widgetPkg = null;

			widgetPkg = info.mProvider.provider.getPackageName();
			widgetResources = mContext.getPackageManager().getResourcesForApplication(widgetPkg);
			// 获取行数
			int rowList = widgetResources.getIdentifier(GoWidgetConstant.ROW_LIST, "array",
					widgetPkg);
			if (rowList > 0) {
				mRowLists = widgetResources.getIntArray(rowList);
			}

			// 获取列数
			int colList = widgetResources.getIdentifier(GoWidgetConstant.COL_LIST, "array",
					widgetPkg);
			if (colList > 0) {
				mColLists = widgetResources.getIntArray(colList);
			}

			// 获取当前typeId对应widget的行列数
			// 如果typeId不存在，<0，则返回
			int styleTypeId = widgetResources.getIdentifier(GoWidgetConstant.TYPE_LIST, "array",
					widgetPkg);
			if (styleTypeId > 0) {
				mStyleTypeList = widgetResources.getIntArray(styleTypeId);
			}
		} catch (Exception e) {
			//			Log.i("WidgetSubTab", "initWidgetArray() has exception " + e.getMessage());
		}
	}

	// 扫描所有主题包
	private List<ResolveInfo> getAllWidgetThemesInfo() {
		// 桌面主题包
		PackageManager pm = mContext.getPackageManager();
		// widget主题包
		Intent intent = new Intent(ICustomAction.ACTION_WIDGET_THEME_PACKAGE);
		intent.addCategory(WidgetThemeChooseFrame.THEME_CATEGORY);

		List<ResolveInfo> widgetThemes = pm.queryIntentActivities(intent, 0);
		return widgetThemes;
	}
    // 扫描widget的风格数据
	private void getAllGoWidgetInfos(GoWidgetProviderInfo info) {

		List<ResolveInfo> themes = getAllWidgetThemesInfo();
		mWidgetDatasDetail = new LinkedList<GoWidgetInfo>();

		ThemeBean themeBean = null;
		String appPackageName = null;
		String themeFileName = null;
		String loadingThemeName = mContext.getString(R.string.loading);
		String widgetPkg = info.mProvider.provider.getPackageName();

		int size = themes.size();
		for (int i = 0; i <= size; i++) {
			if (i == 0) {
				// 加上widget默认的主题
				appPackageName = widgetPkg;
			} else {
				appPackageName = themes.get(i - 1).activityInfo.packageName.toString();
			}

			try {
				themeFileName = doParseData(widgetPkg, info);
			} catch (Exception e) {
			}

			// 增强短信配置文件判断
			if (widgetPkg.equals(LauncherEnv.GOSMS_PACKAGE)) {
				if (!appPackageName.equals(LauncherEnv.GOSMS_PACKAGE)) {
					themeFileName = "widget_smswidget.xml";
				} else {
					themeFileName = "widget_gosms.xml";
				}
			}

			InputStream inputStream = XmlParserFactory.createInputStream(mContext, appPackageName,
					themeFileName);

			// for 任务管理器EX add by chenguanyu 2012.6.29
			if (inputStream == null
					&& widgetPkg.equals(LauncherEnv.Plugin.RECOMMAND_GOTASKMANAGER_PACKAGE)) {
				themeFileName = "widget_taskmanager.xml";
				inputStream = XmlParserFactory.createInputStream(mContext, appPackageName,
						themeFileName);
			}

			if (null == inputStream) {
				//				Log.i("GoWidgetManagerFrame", "no file:" + themeFileName + " in package:"
				//						+ appPackageName);
				continue;
			}

			GoWidgetInfo widgetItem = new GoWidgetInfo();
			widgetItem.title = loadingThemeName;

			themeBean = new PreviewSpecficThemeBean();
			if (themeBean != null) {
				themeBean.setPackageName(appPackageName);

				((PreviewSpecficThemeBean) themeBean).setInputStream(inputStream);

				widgetItem.packageName = appPackageName;
				widgetItem.themeBean = (PreviewSpecficThemeBean) themeBean;
				mWidgetDatasDetail.add(widgetItem);
			}
		}
	}
	// 从widget包解析数据
	private String doParseData(String packageName, GoWidgetProviderInfo info)
			throws NameNotFoundException {
		String widgetName = "";
		final int prefixLength = WidgetThemeChooseFrame.WIDGET_PACKAGE_PREFIX.length();
		if (ThemeManager.DEFAULT_THEME_PACKAGE.equals(packageName)) {
			// update by zhoujun
			// 应用游戏中心的widget和gostorewidget的packageName是一样的，这里需要进一步判断，否则会将应用中心widget的mTitle改为“GO
			// 精品”
			if (info.mInnerWidgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_GOSTORE) {
				// GOSTORE widget 标题需要根据渠道号显示
				info.mInnerWidgetInfo.mTitle = GoStoreChannelControl.getChannelCheckName(mContext);
			}
			// update by zhoujun 2012-08-13 end
			String fileName = info.mInnerWidgetInfo.mThemeConfig;
			return fileName;
		}
		if (packageName.length() > prefixLength) {
			widgetName = packageName.substring(prefixLength);
		}
		return "widget_" + widgetName + ".xml";
	}

	// 第三步 扫描皮肤
	private void scanSkins() {
		try {
			int count = mWidgetDatasDetail.size();
			for (int i = 0; i < count; i++) {
				GoWidgetInfo info = mWidgetDatasDetail.get(i);
				parserThemeInfo(info);
			}
			// mScanSkinFinished = true;
		} catch (Exception e) {
			// 这里是异步扫描，可能在扫描过程中用户点击返回，这里可能出现空指针
		}
		if (null == mWidgetDatasDetail || mWidgetDatasDetail.isEmpty()) {
			return;
		}
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		ImageView skinImageView = (ImageView) screenEditBoxFrame.getLargeTabView().findViewById(
				R.id.tabs_gowidget_skin);
		if (null != mWidgetDatasDetail && mWidgetDatasDetail.size() > 1) {
			// 根据是否有皮肤设置是否可点击
			skinImageView.setClickable(true);
		} else {
			skinImageView.setClickable(false);
		}
		if (null != mDatasDetail && !mDatasDetail.isEmpty()) {
			/**
			 * 说明：这是gowidget主包内有>1套风格的处理，例如clock
			 */
			int oldDefaultStyleCount = getDefaultSkinStyleCount();
			int newDefaultStyleCount = mDatasDetail.get(0).size();

			Message msg = new Message();
			if (oldDefaultStyleCount != newDefaultStyleCount) {
				msg.what = LIST_INIT_OK;
			} else {
				msg.what = LIST_DEFAULT_OK;
				// initDefaultWidgetPicViews();
			}
			mHandler.sendMessage(msg);
		}
	}
	// 默认样式
	private boolean mIsDefaultStyle = true;

	// 初始化其他风格
	public void initmOtherStylesInfos(LinkedList<GoWidgetInfo> linkedList) {
		mOtherStylesInfosScan = linkedList;
		mIsDefaultStyle = false;
	}

	/**
	 * 获取未经异步扫描前，gowidget主包内有几张预览图，用于与异步扫描完后 对比数量是否有变化
	 * 
	 * @return
	 */
	public int getDefaultSkinStyleCount() {
		return (null != mWidgetDatasScan) ? mWidgetDatasScan.size() : 0;
	}

	/**
	 * 解析各个主题包中widget预览信息
	 * 
	 * @author penglong
	 * @param themePackage
	 *            主题包名 styleString 当前widget的style themeBean 需要填充的theme信息
	 */
	private void parserThemeInfo(GoWidgetInfo widgetItem) {
		if (null == widgetItem || null == widgetItem.themeBean || null == widgetItem.packageName) {
			return;
		}

		PreviewSpecficThemeBean themeBean = widgetItem.themeBean;
		String themePackage = widgetItem.packageName;

		InputStream inputStream = themeBean.getInputStream();

		// 扫描主题包内有几个主题
		XmlPullParser xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
		ParseSpecficWidgetTheme parser = new ParseSpecficWidgetTheme();
		parser.parseXml(xmlPullParser, themeBean);
		parser = null;

		// 关闭inputStream
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
		}

		Resources resTheme = null;
		try {
			resTheme = mContext.getPackageManager().getResourcesForApplication(themePackage);
		} catch (NameNotFoundException e) {
		}

		if (null == resTheme) {
			return;
		}

		ArrayList<String> widgetPreview = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_PREVIEW);
		ArrayList<String> widgetTitle = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_TITLE);
		ArrayList<String> widgetThemeType = themeBean
				.getWidgetAttrib(ParseSpecficWidgetTheme.WIDGET_THEME_TYPE);
		ArrayList<Integer> themePositionList = themeBean.getThemePositionList();
		ArrayList<Integer> styleids = themeBean.getmStyleIdsList();

		GoWidgetInfo newGoWidgetInfo = null;
		if (null == themePositionList || 0 == themePositionList.size() || null == styleids
				|| styleids.isEmpty() || themePositionList.size() != styleids.size()) {
			return;
		} else {
			LinkedList<GoWidgetInfo> widgetInfos = new LinkedList<GoWidgetInfo>();
			for (int i = 0; i < themePositionList.size(); i++) {
				newGoWidgetInfo = new GoWidgetInfo();
				newGoWidgetInfo.packageName = widgetItem.packageName;

				try {
					newGoWidgetInfo.themeId = Integer.parseInt(widgetThemeType.get(i));
				} catch (Exception e) {
					newGoWidgetInfo.themeId = -1;
				}

				if (widgetTitle != null && i < widgetTitle.size()) {
					int resId = resTheme.getIdentifier(widgetTitle.get(i), "string", themePackage);
					if (resId != 0) {
						newGoWidgetInfo.title = resTheme.getString(resId);
					}
				}

				if (widgetPreview != null && i < widgetPreview.size()) {
					int res = resTheme
							.getIdentifier(widgetPreview.get(i), "drawable", themePackage);
					if (res != 0) {
						newGoWidgetInfo.resouceId = res;
						newGoWidgetInfo.resouces = resTheme;
					}
				}

				// 几行几列
				int typePosition = -1;
				int lengh = (null == mStyleTypeList) ? 0 : mStyleTypeList.length;
				for (int j = 0; j < lengh; j++) {
					if (styleids.get(i) == mStyleTypeList[j]) {
						newGoWidgetInfo.styleId = styleids.get(i);
						typePosition = j;
						break;
					}
				}
				// 没找到对应的typeId;typePosition是指当前type在widget包里的位置
				if (-1 == typePosition) {
					continue;
				}
				try {
					newGoWidgetInfo.mCol = mColLists[typePosition];
					newGoWidgetInfo.mRow = mRowLists[typePosition];
				} catch (Exception e) {
					continue;
				}

				widgetInfos.add(newGoWidgetInfo);
			}
			if (!widgetInfos.isEmpty()) {
				mDatasDetail.put(mDatasDetail.size(), widgetInfos);
			}
		}
		return;
	}

	// 添加gowidget至桌面

	public void addGoWidget(int index) {
		final GoWidgetManager widgetManager = AppCore.getInstance().getGoWidgetManager();
		int gowidgetId = widgetManager.allocateWidgetId();
		// int currentIndex = getCurrentScreen() - 1;
		int currentIndex = index;
		WidgetParseInfo info = null;
		GoWidgetInfo goWidgetInfo = null;
		if (!mIsDefaultStyle && null != mOtherStylesInfosScan
				&& currentIndex < mOtherStylesInfosScan.size()) {
			// 不是默认风格，要通过styleID查找对应的info
			goWidgetInfo = mOtherStylesInfosScan.get(currentIndex);
			int styleid = goWidgetInfo.styleId;
			int datacount = mWidgetDatasScan.size();
			for (int i = 0; i < datacount; i++) {
				WidgetParseInfo widgetParseInfo = mWidgetDatasScan.get(i);
				if (widgetParseInfo.styleType != null
						&& (Integer.valueOf(widgetParseInfo.styleType) == styleid)) {
					info = widgetParseInfo;
					break;
				}
			}
		} else if (null != mWidgetDatasScan && currentIndex < mWidgetDatasScan.size()) {
			// info = mWidgetDatas.get(0);
			info = mWidgetDatasScan.get(currentIndex);
		}

		if (null == info) {
			return;
		}
		final AppWidgetProviderInfo provider = mGoWidgetProviderInfo.mProvider;
		String widgetPackage = provider.provider.getPackageName();

		provider.minHeight = DrawUtils.dip2px(info.minHeight);
		provider.minWidth = DrawUtils.dip2px(info.minWidth);

		Bundle bundle = new Bundle();
		bundle.putInt(GoWidgetConstant.GOWIDGET_ID, gowidgetId);
		bundle.putInt(GoWidgetConstant.GOWIDGET_TYPE, info.type);
		bundle.putString(GoWidgetConstant.GOWIDGET_LAYOUT, info.layoutID);
		bundle.putParcelable(GoWidgetConstant.GOWIDGET_PROVIDER, provider);
		String themePackage = mIsDefaultStyle ? info.themePackage : goWidgetInfo.packageName;
		bundle.putString(GoWidgetConstant.GOWIDGET_THEME, themePackage);
		int themeid = mIsDefaultStyle ? info.themeType : goWidgetInfo.themeId;
		bundle.putInt(GoWidgetConstant.GOWIDGET_THEMEID, themeid);

		// 内置类型
		int prototype = GoWidgetBaseInfo.PROTOTYPE_NORMAL;
		if (mGoWidgetProviderInfo.mInnerWidgetInfo != null) {
			prototype = mGoWidgetProviderInfo.mInnerWidgetInfo.mPrototype;
		} else {
			// 尝试找出其他内置的widget，如任务管理器widget
			InnerWidgetInfo innerWidgetInfo = widgetManager.getInnerWidgetInfo(widgetPackage);
			if (innerWidgetInfo != null) {
				prototype = innerWidgetInfo.mPrototype;
				// // 更新包名为实际inflate xml的包名
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
				//添加到当前桌面
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.ADD_GO_WIDGET,
						gowidgetId, bundle, null);
			}

		} else {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.GET_ENOUGHSPACELIST,
					0, bundle, null);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.ADD_GO_WIDGET,
					gowidgetId, bundle, null);
		}
		//延迟刷新
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.WIDGET_DELAY_REFRESH,
				2000, 0, null);
		bundle = null;
	}

	// 启动设置界面
	public void startConfigActivity(Bundle bundle, String pkgName, String configure) {
		try {
			//			Log.i("widget", "config activity: " + mConfigActivity);
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClassName(pkgName, configure);
			intent.putExtras(bundle);
			mContext.startActivity(intent);
		} catch (Exception e) {
			// 退出widget选择界面
			// sendMessage(IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.REMOVE_FRAME,
			// getId(), null, null);
			//			Log.i("widgetChooser", "startConfigActivity error: " + pkgName + "." + configure);
		}
	}

	// 清空数据
	public void cleanup() {
		if (null != mWidgetDatasDetail) {
			mWidgetDatasDetail.clear();
			mWidgetDatasDetail = null;
		}

		if (null != mDatasDetail) {
			int size = mDatasDetail.size();
			for (int i = 0; i < size; i++) {
				LinkedList<GoWidgetInfo> infos = mDatasDetail.get(i);
				if (null != infos) {
					infos.clear();
					infos = null;
				}
			}
			mDatasDetail.clear();
			mDatasDetail = null;
		}
		// scan
		if (mWidgetDatasScan != null) {
			mWidgetDatasScan.clear();
			mWidgetDatasScan = null;
		}
		if (null != mOtherStylesInfosScan) {
			mOtherStylesInfosScan.clear();
			mOtherStylesInfosScan = null;
		}
		 unInitInfoFont();
	}
	public void unInitInfoFont() {
		if (mInfoText != null && mInfoText instanceof DeskTextView) {
			mInfoText.selfDestruct();
			mInfoText = null;
		}
		if (mUninstallButton != null && mUninstallButton instanceof DeskButton) {
			((DeskButton) mUninstallButton).selfDestruct();
			mUninstallButton = null;
		}
		if (mFbButton != null && mFbButton instanceof DeskButton) {
			((DeskButton) mFbButton).selfDestruct();
			mFbButton = null;
		}
	}
	// 获取widget名称
	String mWidgetName;

	// 获取图标名称
	private Drawable getWidgetIcon(GoWidgetProviderInfo info) {
		Drawable drawable = null;
		final String pkgName = info.mProvider.provider.getPackageName();
		if (pkgName.equals("")) {
			if (info.mProvider.icon > 0) {
				drawable = mContext.getResources().getDrawable(info.mProvider.icon);
			} else if (info.mIconPath != null && info.mIconPath.length() > 0) {
				BitmapDrawable imgDrawable = null;
				if (FileUtil.isFileExist(info.mIconPath)) {
					try {
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
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (imgDrawable == null) {
					imgDrawable = (BitmapDrawable) ImageExplorer.getInstance(mContext).getDrawable(
							ThemeManager.DEFAULT_THEME_PACKAGE, info.mIconPath);
				}
				drawable = imgDrawable;
			}
		} else {
			Resources resources;
			try {
				resources = mContext.getPackageManager().getResourcesForApplication(pkgName);
				if (resources != null) {
					drawable = resources.getDrawable(info.mProvider.icon);
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (drawable != null) {
			int size = (int) mContext.getResources().getDimension(R.dimen.gowidget_title_icon_size);
			return BitmapUtility.zoomDrawable(mContext, drawable, size, size);
		}
		return drawable;
	}

	private View mInfoView;
	private DeskTextView mInfoText;	
	private Button mFbButton;
	private Button mUninstallButton;
	// 初始化info页面
	public void initInfoPage() {
		if (null == mGoWidgetProviderInfo) {
			return;
		}

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// Add by xiangliang 小于800分辨率手机才有另一种布局
		if (DrawUtils.sHeightPixels < 800) {
			mInfoView = inflater.inflate(R.layout.screenedit_gowidget_info_page_small, null);
		} else {
			mInfoView = inflater.inflate(R.layout.screenedit_gowidget_info_page, null);
		
		}
		// 文字
		String nameString = mContext.getResources().getString(R.string.gowidget_info_name);
		String versionString = mContext.getResources().getString(R.string.gowidget_info_version);
		String designerString = mContext.getResources().getString(R.string.gowidget_info_designer);
		// update by zhoujun 应用游戏中心，预览详情界面，反馈地址是 golauncher.goforandroid.com
		int resourceId = R.string.gowidget_info_fb;
		if (mGoWidgetProviderInfo.mInnerWidgetInfo != null
				&& mGoWidgetProviderInfo.mInnerWidgetInfo.mPrototype == GoWidgetBaseInfo.PROTOTYPE_APPGAME) {
			resourceId = R.string.gowidget_appgame_info_fb;
		}
		String fbString = mContext.getResources().getString(resourceId);
		// update by zhoujun 2012-08-13 end
		String blogString = mContext.getResources().getString(R.string.gowidget_info_blog);
		mInfoText = (DeskTextView) mInfoView.findViewById(R.id.text1);
		mInfoText.setText(nameString + mGoWidgetProviderInfo.mProvider.label);

		String pkgString = mGoWidgetProviderInfo.mProvider.provider.getPackageName();
		PackageManager pm = mContext.getPackageManager();
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = pm.getPackageInfo(pkgString, 0);
			versionString = versionString + pkgInfo.versionName;
		} catch (Exception e) {
		}
		mInfoText = (DeskTextView)  mInfoView.findViewById(R.id.text2);
		mInfoText.setText(versionString);

		mInfoText = (DeskTextView)  mInfoView.findViewById(R.id.text3);
		mInfoText.setText(designerString);

		mInfoText = (DeskTextView)  mInfoView.findViewById(R.id.text4);
		mInfoText.setText(fbString);

		mInfoText = (DeskTextView)  mInfoView.findViewById(R.id.text5);
		mInfoText.setText(blogString);

		/*
		 * //更新 String newestVersionString =
		 * getContext().getResources().getString(R.string.newest_version);
		 * textView = (TextView)mInfoView.findViewById(R.id.newestVersion);
		 * 
		 * 
		 * Button updateButton =
		 * (Button)mInfoView.findViewById(R.id.updateVersionBtn);
		 * updateButton.setOnClickListener(new OnClickListener(){
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub if(mOnUpdateLinstener!=null){
		 * mOnUpdateLinstener.onUpdateLinstener(); } }
		 * 
		 * }); boolean isNeedUpdate =false; if(pkgInfo!=null){
		 * if(mGoWidgetProviderInfo.mVersionName!=null){ newestVersionString =
		 * newestVersionString + mGoWidgetProviderInfo.mVersionName; }else{
		 * newestVersionString = newestVersionString + pkgInfo.versionName; }
		 * 
		 * int versionCode = pkgInfo.versionCode; int newVersionCode =
		 * mGoWidgetProviderInfo.mVersionCode; if(newVersionCode>versionCode){
		 * isNeedUpdate = true; } } textView.setText(newestVersionString);
		 * RelativeLayout updateLayout =
		 * (RelativeLayout)mInfoView.findViewById(R.id.updateVersion);
		 * if(isNeedUpdate){ updateLayout.setVisibility(View.VISIBLE); }else{
		 * updateLayout.setVisibility(View.GONE); }
		 */
		// 反馈、卸载

		mFbButton = (Button) mInfoView.findViewById(R.id.fb);
		mUninstallButton = (Button) mInfoView.findViewById(R.id.uninstall);
		// mFbButton.setOnClickListener(this);

		mFbButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String widgetName = null;
				if (null != mGoWidgetProviderInfo && null != mGoWidgetProviderInfo.mProvider) {
					widgetName = mGoWidgetProviderInfo.mProvider.label;
				}
				sendMail(widgetName);
			}
		});
		// update by zhoujun 新增应用游戏中心的widget
		if (null != mGoWidgetProviderInfo.mInnerWidgetInfo
				&& (Statistics.GOSTORE_WIDGET_PACKAGE_NAME
						.equals(mGoWidgetProviderInfo.mInnerWidgetInfo.mStatisticPackage) || Statistics.APPGAME_WIDGET_PACKAGE_NAME
						.equals(mGoWidgetProviderInfo.mInnerWidgetInfo.mStatisticPackage))) {
			// go精品和应用游戏中心widget没有“卸载组件”
			mUninstallButton.setVisibility(View.INVISIBLE);
		} else {
			mUninstallButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					deleteGoWidget(mGoWidgetProviderInfo.mProvider.provider.getPackageName());
				}
			});
		}
		// update by zhoujun 2012-08-11 end
		mItemViews.add(mInfoView);
	}

	/**
	 * 意见反馈
	 * 
	 * @param context
	 * @param file
	 * @param body
	 */
	private void sendMail(String widgetName) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		String[] receiver = new String[] { "golauncher@goforandroid.com" };

		String subject = mContext.getResources().getString(R.string.gowidget_info_fb_subject);
		subject = widgetName + " - " + subject;

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		emailIntent.setType("plain/text");
		mContext.startActivity(emailIntent);
	}

	/**
	 * 卸载widget
	 * 
	 * @param themePkg
	 * @param curThemePkgName
	 */
	private void deleteGoWidget(String themePkg) {
		if (ICustomAction.PKG_GOWIDGET_SWITCH.equals(themePkg)) {
			PackageManager pm = mContext.getPackageManager();
			PackageInfo pkgInfo = null;
			int versionCode = 0;
			try {
				pkgInfo = pm.getPackageInfo(themePkg, 0);
				versionCode = pkgInfo.versionCode;
				// 卸载开关，广播通知开关关闭相关安全权限
				Intent intent = new Intent(ICustomAction.ACTION_ON_OFF_UNINSTALL_BROADCAST);
				mContext.sendBroadcast(intent);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.GOWIDGET_UNINSTALL_GOWIDGET_SWITCH, -1, null, null);
				if (versionCode < 10) {
					// 版本過低，可能卸载失败，提示更新
					Toast.makeText(mContext,
							R.string.uninstall_switch_version_too_old_to_uninstall,
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
			}
			return;
		}
		Uri packageURI = Uri.parse("package:" + themePkg);
		Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
		Activity activity = (Activity) mContext;
		activity.startActivityForResult(uninstallIntent, IRequestCodeIds.REQUEST_GOWIDGET_UNINSTALL);
	}

}