package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.go.util.Utilities;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.ImageUtil;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogBase;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.DockItemControler;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-11-14]
 */
public class DockAddIconAppView extends LinearLayout
		implements
			OnClickListener,
			OnAddIconAppCheckListner {
	
	private Context mContext;
	private Handler mHandler;
	private Object mMutex;
	private OnAddIconClickListner mListner;
	
	private LinearLayout mContentLayout;	//内容布局
	private LinearLayout mTitleLayout;	//标题布局
	private ImageView mBackBtn;	//返回按钮
	private TextView mTitle;	//标题
	
	private DockAddIconCheckViewGroup mMultiCheckViewGroup;	//显示内容控件
	
	private GoProgressBar mGoProgressBar;	//等待框
	
	private ArrayList<Object> mAllList = new ArrayList<Object>(); // 列表的所有元素
	
	private static final int INIT_FINISH = 1;	//完成数据加载

	private Thread mInitThread;	//初始化线程
		
	private int mType;	//加载数据类型
	
	public DockAddIconAppView(Context context) {
		super(context);
	}
	
	public DockAddIconAppView(Context context, int type) {
		super(context);
		mContext = context;
		mType = type;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.dock_add_icon_app_view, this);
		mContentLayout = (LinearLayout) view.findViewById(R.id.contentLayout);
		initLayoutHeight();
		
		mTitleLayout = (LinearLayout) view.findViewById(R.id.title_layout);
		mTitleLayout.setOnClickListener(this);
		
		mBackBtn = (ImageView) view.findViewById(R.id.back_btn);
		mBackBtn.setOnClickListener(this);
		mTitle = (TextView) view.findViewById(R.id.title);
		mGoProgressBar = (GoProgressBar) view.findViewById(R.id.modify_progress);
		mMultiCheckViewGroup = (DockAddIconCheckViewGroup) view.findViewById(R.id.multi_check_viewgroup);
		mMultiCheckViewGroup.setOnAddIconAppCheckListner(this);
		mMutex = new Object();
		initHandler();
		initData();
		
		
	}
	
	public void initLayoutHeight() {
		//更换总的布局高宽
		if (mContentLayout != null) {
			android.view.ViewGroup.LayoutParams layoutParams = mContentLayout.getLayoutParams();
			if (mType == DockAddIconFrame.TYPE_ADD_ICON_DEFAULT) {
				layoutParams.height = (int) getResources().getDimension(R.dimen.dock_add_view_view_height_default);
			} else {
				layoutParams.height = (int) getResources().getDimension(R.dimen.dock_add_view_view_height);
			}
			if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
				DialogBase.setDialogWidth(mContentLayout, mContext);
			} else {
				layoutParams.width = (int) getResources().getDimension(R.dimen.folder_edit_view_width);
			}
		}
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case INIT_FINISH : {
						mMultiCheckViewGroup.setContentList(mAllList);	//设置显示内容
						dismissProgressDialog(); // 取消加载框
						break;
					}
				}
			}
		};
	}

	/**
	 * <br>功能简述:初始化数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initData() {
		showProgressDialog(); // 显示提示框
		initListData();
	}

	/**
	 * <br>功能简述:初始化显示数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initListData() {
		mInitThread = new Thread(ThreadName.INIT_MODIFY_FOLDER_APP_LIST) {
			@Override
			public void run() {
				synchronized (mMutex) {
					if (mAllList == null) {
						mAllList = new ArrayList<Object>();
					}
					// 先清空
					mAllList.clear();
					switch (mType) {
						case DockAddIconFrame.TYPE_ADD_ICON_APP :
							initAppData();
							break;
						case DockAddIconFrame.TYPE_ADD_ICON_GOSHORTCUT :
							initGoShortCutData();
							break;
							
						case DockAddIconFrame.TYPE_ADD_ICON_DEFAULT :
							initDockDefaultIconData();
							break;
							
						default :
							break;
					}
					mHandler.sendEmptyMessage(INIT_FINISH);
				}
			}

		};
		mInitThread.start();
		mInitThread = null;
	}

	/**
	 * <br>功能简述:设置标题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param resid
	 */
	public void setTitle(int resid) {
		if (mTitle != null) {
			mTitle.setText(resid);
		}
	}

	/**
	 * <br>功能简述:显示等待框
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void showProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.INVISIBLE) {
			mGoProgressBar.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * <br>功能简述:关闭等待框
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void dismissProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.VISIBLE) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	

	private void cleanHandlerMsg() {
		if (mHandler != null) {
			mHandler.removeMessages(INIT_FINISH);
		}
	}

	@Override
	public void onItemCheck(View view, int position) {
		Object object = mAllList.get(position);
		if (mListner != null) {
			mListner.onIconsClick(DockAddIconFrame.TYPE_ADD_ICON_APP, view, position, object);
		}
	}
	
	/**
	 * <br>功能简述:设置点击ITEM监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param listener
	 */
	public void setOnAddIconClickListener(OnAddIconClickListner listener) {
		mListner = listener;
	}

	@Override
	public void onClick(View v) {
		if (v == mBackBtn || v == mTitleLayout) {
			if (mListner != null) {
				mListner.onBackBtnClick(mType);
			}
		}
	}
	
	/**
	 * <br>功能简述: 初始化应用程序数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initAppData() {
		final AppDataEngine engine = GOLauncherApp.getAppDataEngine();
		ArrayList<AppItemInfo> list2 = engine.getCompletedAppItemInfosExceptHide();
		if (list2.size() > 0) {
			try {
				SortUtils.sort(list2, "getTitle", null, null, null);
			} catch (IllegalArgumentException e) {
				// 可能因为用户手机Java运行时环境的问题出错
				e.printStackTrace();
			}
			
			for (AppItemInfo info : list2) {
				if (info.mIntent != null && info.mIntent.getComponent() != null) {
//					ShortCutInfo shortCutInfo = new ShortCutInfo();
//					shortCutInfo.mItemType = IItemType.ITEM_TYPE_APPLICATION;
//					shortCutInfo.mTitle = info.mTitle;
//					shortCutInfo.mIntent = info.mIntent;
//					shortCutInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
//					shortCutInfo.mIcon = info.mIcon;
					mAllList.add(info);
				}
			}
		}
	}
	
	
	/**
	 * <br>功能简述: 初始化Go快捷方式数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initGoShortCutData() {
		String[] intentActions = new String[] {
				ICustomAction.ACTION_SHOW_MAIN_SCREEN,
				ICustomAction.ACTION_SHOW_MAIN_OR_PREVIEW,
				ICustomAction.ACTION_SHOW_PREVIEW,	//屏幕预览
				ICustomAction.ACTION_SHOW_FUNCMENU_FOR_LAUNCHER_ACITON,	//显示功能标
				ICustomAction.ACTION_SHOW_EXPEND_BAR, 
				ICustomAction.ACTION_SHOW_HIDE_STATUSBAR,
				ICustomAction.ACTION_SHOW_DOCK, 
				ICustomAction.ACTION_ENABLE_SCREEN_GUARD,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE,
				ICustomAction.ACTION_FUNC_SPECIAL_APP_GOTHEME,
				ICustomAction.ACTION_SHOW_PREFERENCES, 
				ICustomAction.ACTION_SHOW_MENU,
				ICustomAction.ACTION_SHOW_DIYGESTURE, 
				ICustomAction.ACTION_SHOW_PHOTO,
				ICustomAction.ACTION_SHOW_MUSIC, 
				ICustomAction.ACTION_SHOW_VIDEO };

		int[] titles = new int[] {
				R.string.customname_mainscreen,
				R.string.customname_mainscreen_or_preview,
				R.string.customname_preview,
				R.string.customname_Appdrawer,
				R.string.customname_notification,
				R.string.customname_status_bar,
				R.string.goshortcut_showdockbar,
				R.string.goshortcut_lockscreen,
				R.string.customname_gostore,
				R.string.customname_themeSetting,
				R.string.customname_preferences,
				R.string.customname_mainmenu, 
				R.string.customname_diygesture,
				R.string.customname_photo, 
				R.string.customname_music,
				R.string.customname_video
		};
		
		int[] drawableIds = new int[] { R.drawable.go_shortcut_mainscreen,
				R.drawable.go_shortcut_main_or_preview, R.drawable.go_shortcut_preview,
				R.drawable.go_shortcut_appdrawer, R.drawable.go_shortcut_notification,
				R.drawable.go_shortcut_statusbar, R.drawable.go_shortcut_hide_dock,
				R.drawable.go_shortcut_lockscreen, R.drawable.go_shortcut_store,
				R.drawable.go_shortcut_themes, R.drawable.go_shortcut_preferences,
				R.drawable.go_shortcut_menu, R.drawable.go_shortcut_diygesture,
				R.drawable.go_shortcut_photo, R.drawable.go_shortcut_music,
				R.drawable.go_shortcut_video };
		
		final int size = intentActions.length;
		final String goComponentName = "com.gau.launcher.action";
		ShortCutInfo shortCutInfo = null;
		Intent intent = null;
		ComponentName cmpName = null;
		for (int i = 0; i < size; i++) {
			shortCutInfo = new ShortCutInfo();
			intent = new Intent(intentActions[i]);
			cmpName = new ComponentName(goComponentName, intentActions[i]);
			intent.setComponent(cmpName);
			shortCutInfo.mIntent = intent;
			shortCutInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
			shortCutInfo.mTitle = mContext.getText(titles[i]);
			shortCutInfo.mIcon = Utilities.createIconThumbnail(getIcons(drawableIds[i]), mContext);
			shortCutInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
			mAllList.add(shortCutInfo);
		}
	}

	/**
	 * <br>功能简述:通过drawableId拿推荐图标图片
	 * <br>功能详细描述:可以过滤某些图标进行download tag标签合成图片（tag图片共享一张，减少图片资源） <br>
	 * @param drawableId
	 * @return　经过合成规则处理后的图片
	 */
	private Drawable getIcons(int drawableId) {
		Drawable tag = mContext.getResources().getDrawable(drawableId);
		Drawable drawable = mContext.getResources().getDrawable(R.drawable.screenedit_icon_bg);
		try {
			int width = drawable.getIntrinsicWidth();
			int height = drawable.getIntrinsicHeight();
			Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			Canvas cv = new Canvas(bmp);
			ImageUtil.drawImage(cv, drawable, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			ImageUtil.drawImage(cv, tag, ImageUtil.STRETCHMODE, 0, 0, width, height, null);
			BitmapDrawable bmd = new BitmapDrawable(bmp);
			bmd.setTargetDensity(mContext.getResources().getDisplayMetrics());
			drawable = bmd;
		} catch (Throwable e) {
			// 出错则不进行download Tag合成图
		}

		return drawable;
	}
	
	/**
	 * <br>功能简述:初始化DOCK默认数据
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initDockDefaultIconData() {
		Drawable[] drawables = getDockDefaultIcon();
		
		int[] name = new int[] {
				R.string.customname_dial,
				R.string.customname_contacts,
				R.string.customname_Appdrawer,
				R.string.customname_sms,
				R.string.customname_browser };
		String[] res = new String[] { 
				"shortcut_0_0_phone", 
				"shortcut_0_1_contacts",
				"shortcut_0_2_funclist", 
				"shortcut_0_3_sms", 
				"shortcut_0_4_browser" };
		Intent[] intent = new Intent[] { 
				AppIdentifier.createSelfDialIntent(mContext),
				AppIdentifier.createSelfContactIntent(mContext),
				new Intent(ICustomAction.ACTION_SHOW_FUNCMENU),
				AppIdentifier.createSelfMessageIntent(),
				AppIdentifier.createSelfBrowseIntent(mContext.getPackageManager()) };

		long time = System.currentTimeMillis();
		
		ShortCutInfo shortCutInfo = null;
		for (int i = 0; i < name.length; i++) {
			shortCutInfo = new ShortCutInfo();
			shortCutInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;
			shortCutInfo.mTitle = mContext.getText(name[i]);
			shortCutInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
			shortCutInfo.mIntent = intent[i];
			shortCutInfo.mIcon = drawables[i];
			shortCutInfo.mFeatureIconPath = res[i];
			shortCutInfo.mInScreenId = time + i;
			shortCutInfo.mFeatureIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
			mAllList.add(shortCutInfo);
		}
	}
	
	/**
	 * <br>功能简述:获取DOCK默认图标5个图片
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private Drawable[] getDockDefaultIcon() {
		Drawable[] drawables = new Drawable[DockUtil.ICON_COUNT_IN_A_ROW];
		String pkg = GoSettingControler.getInstance(mContext).getShortCutSettingInfo().mStyle;
		ImageExplorer imageExplorer = ImageExplorer.getInstance(mContext);
		int[] ids = new int[] { R.drawable.shortcut_0_0_phone, R.drawable.shortcut_0_1_contacts,
				R.drawable.shortcut_0_2_funclist, R.drawable.shortcut_0_3_sms,
				R.drawable.shortcut_0_4_browser };
		int drawableBound = mContext.getResources().getDimensionPixelSize(R.dimen.screen_icon_size);
		for (int i = 0; i < DockUtil.ICON_COUNT_IN_A_ROW; i++) {
			DeskThemeBean.SystemDefualtItem dockThemeItem = DockItemControler.getSystemDefualtItem(
					pkg, i);
			Drawable drawable = null;
			if (null != dockThemeItem && null != dockThemeItem.mIcon
					&& null != dockThemeItem.mIcon.mResName) {
				// 主题安装包
				drawable = imageExplorer.getDrawable(pkg, dockThemeItem.mIcon.mResName);
			} else {
				// 风格安装包
				drawable = DockItemControler.getStylePkgDrawable(mContext, pkg, i);
			}

			try {
				if (drawable == null) {
					drawable = mContext.getResources().getDrawable(ids[i]);
				}
				if (drawable != null) {
					//有些主题的图片制件不规范，大小不一
					int width = drawable.getIntrinsicWidth();
					float scale = drawableBound * 1.0f / width;
					drawable = scale == 1 ? drawable : BitmapUtility.zoomDrawable(drawable, scale,
							scale, mContext.getResources());
				}
			} catch (OutOfMemoryError e) {
			}
			drawables[i] = drawable;
		}
		return drawables;
		
	}
	
	/**
	 * <br>功能简述:横竖屏切换事件
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onConfigurationChanged() {
		//更换总的布局高宽
		initLayoutHeight();
		if (mMultiCheckViewGroup != null) {
			mMultiCheckViewGroup.onConfigurationChanged();
		}
	}
	
	/**
	 * <br>功能简述:注销时释放资源
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onDestroy() {
		synchronized (mMutex) {
			dismissProgressDialog();
			cleanHandlerMsg();
			mListner = null;
			mInitThread = null;
			if (mAllList != null) {
				mAllList.clear();
				mAllList = null;
			}
			try {
				if (mMultiCheckViewGroup != null) {
					mMultiCheckViewGroup.recyle();
					mMultiCheckViewGroup = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
