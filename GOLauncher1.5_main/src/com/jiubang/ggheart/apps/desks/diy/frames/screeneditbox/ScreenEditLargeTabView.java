package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.gau.go.launcherex.R;
import com.go.util.file.FileUtil;
import com.go.util.graphics.BitmapUtility;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.BaseTab;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs.WidgetSubTab;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
/**
 * 
 * <br>类描述:负责应用程序，文件夹，Go快捷方式和Go小部件等较大tab的展示
 * <br>功能详细描述:完成布局，设置当前tab给container，并完成标题栏和指示器的操作
 * 
 * @date  [2012-9-10]
 */
public class ScreenEditLargeTabView extends LinearLayout
		implements
			OnClickListener,
			TabIndicatorUpdateListner,
			TabActionListener {

	private ScreenEditBoxContainerForApps mContainerForApps;
	private ScreenEditBoxContainerForGoWidgets mContainerForGoWidgets;

	private LinearLayout mBackTab; // 二级页面的返回菜单(应用程序添加，文件夹添加，Go快捷方式添加)
	private LinearLayout mBackTabForWidget; // 二级页面的返回菜单(Go小部件添加)

	private DesktopIndicator mIndicator; // 指示器
	private RelativeLayout mIndicatorLayout; // 指示器layout
	private OnRespondTouch mOnRespondTouch;	// touch响应
	private String mCurTabTag; // 当前tab

	private ScreenEditLayout mEditLayout;

	public void setEditLayout(ScreenEditLayout mEditLayout) {
		this.mEditLayout = mEditLayout;
	}

	private DataEdngine mDataEdngine; // 用于管理tab数据

	public DataEdngine getDataEdngine() {
		return mDataEdngine;
	}

	public void setDataEdngine(DataEdngine mDataEdngine) {
		this.mDataEdngine = mDataEdngine;
	}

	private Context mContext;

	private int mHorizontalpading;
	private int mViewWidth;
	private int mItemsCount; // 当前屏最多图标数
	private TextView mBackTabText;

	public ScreenEditLargeTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//		mDataEdngine = new DataEdngine(context);
		mContext = context;
		mHorizontalpading = (int) context.getResources().getDimension(
				R.dimen.screen_edit_view_horizontal_space);
		mViewWidth = (int) mContext.getResources().getDimension(R.dimen.screen_edit_view_width);
		// 当前屏最多图标数
		mItemsCount = (GoLauncher.getDisplayWidth() - mHorizontalpading)
				/ (mViewWidth + mHorizontalpading);
		int rightSpace = GoLauncher.getDisplayWidth() - mHorizontalpading - mItemsCount
				* (mViewWidth + mHorizontalpading);
		if (rightSpace >= mViewWidth) {
			++mItemsCount;
		}

	}

	public void setmOnRespondTouch(OnRespondTouch mOnRespondTouch) {
		this.mOnRespondTouch = mOnRespondTouch;
	}

	public ScreenEditBoxContainerForApps getContainer() {
		return mContainerForApps;
	}
	public ScreenEditBoxContainerForGoWidgets getGowidgetContainer() {
		return mContainerForGoWidgets;
	}
	@Override
	public void onClick(View v) {
		onTabClick(mCurTabTag);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		// 计算 mContainerForApps 与 mContainerForGoWidgets的高度
		int mAppHeight = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.APPSCALE
				- getContext().getResources().getDimension(
						R.dimen.screen_edit_indicator_height) - getContext()
				.getResources().getDimension(
						R.dimen.screen_edit_tabtitle_height));
		FrameLayout.LayoutParams appParms = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT, mAppHeight);
		mContainerForApps = (ScreenEditBoxContainerForApps) findViewById(R.id.container_apps);
		mContainerForApps.setLayoutParams(appParms);
		mContainerForApps.setIndicatorUpdateListner(this);
		mContainerForGoWidgets = (ScreenEditBoxContainerForGoWidgets) findViewById(R.id.container_gowidgets);
		//Add by xiangliang 低分辨率屏幕动态计算
		if (DrawUtils.sHeightPixels < 800) {
			int mGowidgetHeight = (int) (DrawUtils.sHeightPixels * ScreenEditLayout.GOWIDGETSCALE
					- getContext().getResources().getDimension(
							R.dimen.screen_edit_indicator_height) - getContext()
					.getResources().getDimension(
							R.dimen.screen_edit_tabtitle_height));
			FrameLayout.LayoutParams gowidgetParms = new FrameLayout.LayoutParams(
					LayoutParams.FILL_PARENT, mGowidgetHeight);
			mContainerForGoWidgets.setLayoutParams(gowidgetParms);
		}
		mContainerForGoWidgets.setIndicatorUpdateListner(this);

		mIndicator = (DesktopIndicator) findViewById(R.id.edit_indicator_large);
		mIndicator.setDefaultDotsIndicatorImage(R.drawable.screen_edit_indicator_cur,
				R.drawable.screen_edit_indicator_other);
		mIndicator.setIndicatorListner(this);

		// 不允许指示器空白处的touch事件向下传递
		mIndicatorLayout = (RelativeLayout) findViewById(R.id.indicator_layout_large);
		mIndicatorLayout.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		iniBackTab();
	}
	// 设置当前tab
	@Override
	public void setCurrentTab(String tabs) {
		if (mDataEdngine == null) {
			return;
		}
		BaseTab tab = mDataEdngine.getTab(tabs);
		if (tab == null) {
			return;
		}
		if (tab.mTabActionListener == null) {
			tab.setTabActionListener(this);
		}
		mCurTabTag = tab.getTag();
		// 应用程序
		if (mEditLayout.getContainerMode() == 1) {
			mContainerForGoWidgets.removeAllViews();
			mContainerForGoWidgets.setVisibility(GONE);

			if (mContainerForApps != null) {
				mContainerForApps.setVisibility(VISIBLE);
				mContainerForApps.setCurrentTab(tab);
				mContainerForApps.requestLayout();
				mIndicator.setCurrent(0);
				mIndicator.setTotal(mContainerForApps.getPageCount());
			}
		} // 小部件
		else if (mEditLayout.getContainerMode() == 2) {  
			mContainerForApps.removeAllViews();
			mContainerForApps.setVisibility(GONE);

			if (mContainerForGoWidgets != null) {
				mContainerForGoWidgets.setVisibility(VISIBLE);
				mContainerForGoWidgets.setCurrentLargeTab(tab);
				mContainerForGoWidgets.requestLayout();
				mIndicator.setCurrent(0);
				mIndicator.setTotal(mContainerForGoWidgets.getPageCount());
			}
		}
	}

	public void setCurrentBackTab(BaseTab tab) {
		if (tab == null) {
			return;
		}
		mCurTabTag = tab.getTag();
		if (mCurTabTag.equals(BaseTab.TAB_APP) || mCurTabTag.equals(BaseTab.TAB_THEME)
				|| mCurTabTag.equals(BaseTab.TAB_GOWIDGET)) {
			mEditLayout.setContainerMode(0);
		}

		if (mEditLayout.getContainerMode() == 1) {
			if (mContainerForApps != null) {
				mContainerForApps.setVisibility(VISIBLE);
				mContainerForApps.setCurrentTab(tab);
				mContainerForApps.requestLayout();
				mIndicator.setCurrent(0);
				mIndicator.setTotal(mContainerForApps.getPageCount());
			}
		}
		if (mEditLayout.getContainerMode() == 2) {
			mContainerForApps.removeAllViews();
			mContainerForApps.setVisibility(GONE);

			if (mContainerForGoWidgets != null) {
				mContainerForGoWidgets.setVisibility(VISIBLE);
				mContainerForGoWidgets.setCurrentLargeTab(tab);
				mContainerForGoWidgets.requestLayout();
				mIndicator.setCurrent(0);
				mIndicator.setTotal(mContainerForGoWidgets.getPageCount());
			}
		}

	}

	@Override
	public void updateIndicator(int num, int current) {
		if (num >= 0 && current >= 0 && current < num) {
			mIndicator.setTotal(num);
			mIndicator.setCurrent(current);
		}
	}

	@Override
	public void clickIndicatorItem(int index) {
		if (mEditLayout.getContainerMode() == 1) {
			if (mContainerForApps != null) {
				mContainerForApps.snapToScreen(index, false, -1);
			}
		} else if (mEditLayout.getContainerMode() == 2) {
			if (mContainerForGoWidgets != null) {
				mContainerForGoWidgets.snapToScreen(index, false, -1);
			}
		}
	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100) {
			if (mEditLayout.getContainerMode() == ScreenEditLayout.APPSLAYOUT) {
				if (mContainerForApps != null) {
					mContainerForApps.getScreenScroller().setScrollPercent(percent);
				}
			} else if (mEditLayout.getContainerMode() == ScreenEditLayout.GOWIDGETLAYOUT) {
				if (mContainerForGoWidgets != null) {
					mContainerForGoWidgets.getScreenScroller().setScrollPercent(percent);
				}
			}

		}
	}

	@Override
	public void onScrollChanged(int offset) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.OFFSET, offset);
		if (mIndicator != null) {
			mIndicator.updateIndicator(DesktopIndicator.UPDATE_SLIDER_INDICATOR, dataBundle);
		}
	}

	@Override
	public void onScreenChanged(int newScreen) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(DesktopIndicator.CURRENT, newScreen);
		if (mIndicator != null) {
			mIndicator.updateIndicator(DesktopIndicator.UPDATE_DOTS_INDICATOR, dataBundle);
		}
	}

	public String getCurTabTag() {
		return mCurTabTag;
	}
	
	public void selfDestruct() {
		if (mOnRespondTouch != null) {
			mOnRespondTouch = null;
		}
		if (mContainerForApps != null) {
			mContainerForApps.unInitAppFont();
			mContainerForApps.unInitImage();
			mContainerForApps.removeAllViews();
			mContainerForApps = null;
		}
		if (mContainerForGoWidgets != null) {
			mContainerForGoWidgets.unInitWidgetFont();
			mContainerForGoWidgets.unInitImage();
			mContainerForGoWidgets.removeAllViews();
			mContainerForGoWidgets = null;
		}
		if (mIndicator != null) {
			mIndicator = null;
		}
		if (mDataEdngine != null) {
			mDataEdngine.clearData();
			mDataEdngine = null;
		}
		if (mBackTabText != null && mBackTabText instanceof DeskTextView) {
			((DeskTextView) mBackTabText).selfDestruct();
			mBackTabText = null;
		}
		// 添加界面退出，cancel异步加载线程和删除缓存
		DrawableLoadTasker.getInstance().cancel(true);
		DrawableLoadTasker.clear();
		DrawableCacheManager.getInstance().clearCache();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mOnRespondTouch != null) {
			return !mOnRespondTouch.isRespondTouch();
		} else {
			return super.onInterceptTouchEvent(event);
		}
	}

	/**
	 * 初始化二级页面上半部分的返回条
	 */
	private void iniBackTab() {
		mBackTab = (LinearLayout) findViewById(R.id.tabs_back_large);
		mBackTabForWidget = (LinearLayout) findViewById(R.id.tabs_back_gowidget);
		final ImageView back = (ImageView) mBackTab.findViewById(R.id.tabs_back_img_large);
		mBackTab.setBackgroundResource(R.drawable.screen_edit_tab_selector);
		mBackTab.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					back.setBackgroundResource(R.drawable.screen_edit_tab_top_back_light);
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (!mBackTab.isPressed()) {
						back.setBackgroundResource(R.drawable.screen_edit_tab_top_back);
					}
				}
				return false;
			}
		});
		mBackTab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					back.setBackgroundResource(R.drawable.screen_edit_tab_top_back);
					// 离开文件夹新建页面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.LEAVE_NEW_FOLDER_STATE, -1, null, null);

					final int tabLevel = getCurTabLevel(getCurTabTag());
					if (tabLevel == BaseTab.TAB_LEVEL_3) {
						ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
								.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
						if (screenEditBoxFrame != null) {
							ScreenEditLayout mLayView = (ScreenEditLayout) screenEditBoxFrame
									.getContentView();
							if (!mLayView.mIsAnimation) {
								// 如果还在动画过程中就不触发
								mLayView.changesizeForNormal();
							}
						}
					}
				} catch (Exception e) {
					//异常情况下，安全退出添加页面
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREENEDIT_CLOSE_SCREEN_EDIT_LAYOUT, 1, null, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
							IDiyMsgIds.SCREENEDIT_SHOW_ANIMATION_OUT, 0, null, null);
				}
			}
		});
	}

	/**
	 * 根据tab名称，刷新二级页面上半部分的显示
	 */
	public void refreshBackTab(final String tabname) {
		/*
		 * //清空 应用程序列表数据 if(mDataEdngine.getTab(BaseTab.TAB_ADDAPPS)!=null){
		 * ((AddAppTab)mDataEdngine.getTab(BaseTab.TAB_ADDAPPS)).clearData();
		 * mDataEdngine.removeData(BaseTab.TAB_ADDAPPS); }//清空 文件夹列表数据 else
		 * if(mDataEdngine.getTab(BaseTab.TAB_ADDFOLDER)!=null){
		 * ((AddFolderTab)mDataEdngine
		 * .getTab(BaseTab.TAB_ADDFOLDER)).clearData();
		 * mDataEdngine.removeData(BaseTab.TAB_ADDFOLDER); }
		 */
		mBackTabForWidget.setVisibility(GONE);
		// 设置返回Tab为可见
		mBackTab.setVisibility(View.VISIBLE);
		ImageView icon = (ImageView) mBackTab.findViewById(R.id.tabs_back_icon_large);
		mBackTabText = (TextView)  mBackTab.findViewById(R.id.tabs_back_text_large);
		if (tabname.equals(BaseTab.TAB_GOWIDGET)) {

			mBackTabText.setText(mContext.getString(R.string.tab_add_widget));
			icon.setBackgroundResource(R.drawable.tab_add_widget_icon);
		} else if (tabname.equals(BaseTab.TAB_THEME)) {
			mBackTabText.setText(mContext.getString(R.string.tab_add_visual_theme));
			icon.setBackgroundResource(R.drawable.change_theme_4_def3);
		} else if (tabname.equals(BaseTab.TAB_LOCKER)) {
			mBackTabText.setText(mContext.getString(R.string.tab_add_visual_locker));
			icon.setBackgroundResource(R.drawable.screen_edit_golocker);
		} else if (tabname.equals(BaseTab.TAB_GOWALLPAPER)) {
			mBackTabText.setText(mContext.getString(R.string.go_wallpaper));
			icon.setBackgroundResource(R.drawable.gowallpaper_logo);

		} else if (tabname.equals(BaseTab.TAB_ADDAPPS)) {
			// mDataEdngine.removeData(BaseTab.TAB_ADDAPPS);
			mBackTabText.setText(mContext.getString(R.string.tab_add_app));
			icon.setBackgroundResource(R.drawable.gesture_application);
		} else if (tabname.equals(BaseTab.TAB_ADDFOLDER)) {
			// mDataEdngine.removeData(BaseTab.TAB_ADDFOLDER);
			mBackTabText.setText(mContext.getString(R.string.tab_add_app_folder));
			icon.setBackgroundResource(R.drawable.tab_add_folder_icon);
		} else if (tabname.equals(BaseTab.TAB_ADDGOSHORTCUT)) {
			mBackTabText.setText(mContext.getString(R.string.dialog_name_go_shortcut));
			icon.setBackgroundResource(R.drawable.screen_edit_go_shortcut);
		} else if (tabname.equals(BaseTab.TAB_ADDGOWIDGET)) {
			// 获取名称
			ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
					.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
			String s = screenEditBoxFrame.getInfo().mProvider.label;
			mBackTabText.setText(s);
			icon.setBackgroundDrawable(getWidgetIcon(screenEditBoxFrame.getInfo()));

			final ImageView infoImageView = (ImageView) mBackTabForWidget
					.findViewById(R.id.tabs_gowidget_info);
			ImageView skinImageView = (ImageView) mBackTabForWidget
					.findViewById(R.id.tabs_gowidget_skin);
			mBackTabForWidget.setVisibility(VISIBLE);
			skinImageView.setVisibility(View.INVISIBLE);
			infoImageView.setBackgroundResource(R.drawable.screenedit_widget_info_select);
			infoImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mContainerForGoWidgets == null) {
						return;
					}
					String tag = (String) mBackTabForWidget.getTag();
					if (tag == null) {
						tag = "info";
					}
					if (tag.equals("info")) {
						// 跳转到最后一页
						int max = mContainerForGoWidgets.getChildCount();
						mCurrent = mContainerForGoWidgets.getmScroller().getCurrentScreen();
						mContainerForGoWidgets.snapToScreen(max - 1, false, -1);
						mBackTabForWidget.setTag("pic");
						infoImageView
								.setBackgroundResource(R.drawable.screenedit_widget_page_select);
					} else if (tag.equals("pic")) {
						if (mCurrent == mContainerForGoWidgets.getChildCount()) {
							mContainerForGoWidgets.snapToScreen(mCurrent - 1, false, -1);
						} else {
							mContainerForGoWidgets.snapToScreen(mCurrent, false, -1);
						}
						mBackTabForWidget.setTag("info");
						infoImageView
								.setBackgroundResource(R.drawable.screenedit_widget_info_select);
					}
				}
			});
			skinImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 切换皮肤
					ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
							.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
					if (screenEditBoxFrame.getInfo() != null) {
						if (mDataEdngine == null) {
							return;
						}
						BaseTab tab = mDataEdngine.getTab(mCurTabTag);
						((WidgetSubTab) tab).showSkinsSelecte(v);
					}
				}
			});

		}

	}

	// gowidget预览 滑动时 ，更换左上角按钮状态（Info图标）
	public void changeGowidgetToInfoPage() {
		mBackTabForWidget.setTag("pic");
		mBackTabForWidget.findViewById(R.id.tabs_gowidget_info).setBackgroundResource(
				R.drawable.screenedit_widget_page_select);
	}

	// gowidget预览 滑动时 ，更换左上角按钮状态(预览图标)
	public void changeGowidgetToPicPage() {
		mBackTabForWidget.setTag("info");
		mBackTabForWidget.findViewById(R.id.tabs_gowidget_info).setBackgroundResource(
				R.drawable.screenedit_widget_info_select);
	}

	int mCurrent = 0; // widget预览时 记录当前屏

	@Override
	public void onTabClick(String tag) {
		setCurrentTab(tag);

	}
	
	public void setTag(String tag) {
		if (mDataEdngine == null) {
			return;
		}
		setCurrentTab(tag);
	}

	/**
	 * 刷新指定tab的内容
	 * 
	 * @param tag
	 */
	public void updateTab(String tag) {
		if (mDataEdngine == null) {
			return;
		}
		mDataEdngine.updateTab(tag);
	}

	public int getCurTabLevel(String tabTag) {
		return mDataEdngine.getTabLevel(tabTag);
	}
	// 刷新tab数据
	@Override
	public void onRefreshTab(String tabName, int index) {
		if (tabName != null && mCurTabTag.equals(tabName)) {
			setCurrentTab(tabName);
			// 计算第几页
			if (mItemsCount != 0) {
				getContainer().getScreenScroller().setCurrentScreen(index / mItemsCount);
			}
		}
	}

	@Override
	public void onRefreshTopBack(String tabName) {
		if (tabName != null) {
			refreshBackTab(tabName);
		}
	}

	public void changeSizeForApps() {

	}

	private final static float DEFAULT_DENSITY = 1.5F; // 480x800下的density

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
}