package com.jiubang.ggheart.apps.desks.ggmenu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.ggmenu.GGMenuData.TabData;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 菜单，目前只支持两页的显示
 * 
 * @author ouyongqiang
 * 
 */
public class GGMenu
		implements
			OnKeyListener,
			OnItemClickListener,
			ScrollerAddViewListener,
			ICleanable {

	/**
	 * 程序上下文
	 */
	private Context mContext;

	/**
	 * 弹出窗口
	 */
	private PopupWindow mPopupWindow;

	/**
	 * 菜单所显示在的View
	 */
	private View mParent;

	public static int sTextColor;

	// 点击监听
	private OnMenuItemSelectedListener mMenuItemSelectedListener;

	private GlMenuTabView mTabView;
	private GlMenuGridViewsContainer mContainer;

	public GlMenuGridViewsContainer getContainer() {
		return mContainer;
	}

	private Drawable mBackgroundDrawable;
	private Drawable mItemBackgroundDrawable;
	private Drawable mItemLineDrawable;

	private TabData[] mTabs = null;
	private int mColNum;
	private int mMenulayout;
	private int mMenuitemlayout;

	public GGMenu(Context context, View parent, int ggMenuLayout, int textColor,
			int selecttabColor, int unselecttabColor, Drawable background, Drawable itembackground,
			Drawable itemline, Drawable unselecttabline, Drawable selecttabline, Drawable newMsg) {
		// mContext = context;
		// modify by huyong 2011-12-15 Debug for M9手机内存泄露
		mContext = GOLauncherApp.getContext();
		// modify by huyong 2011-12-15 Debug for M9手机内存泄露 end

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// mContainer外包一层窗口是因为如果mContainer是popupwindow根视图，
		// 使用screenscroller不会调用computeScroll()
		mTabView = (GlMenuTabView) inflater.inflate(R.layout.glmenu, null);
		mContainer = (GlMenuGridViewsContainer) mTabView.findViewById(R.id.container);
		mPopupWindow = new PopupWindow(mTabView,
				context.getResources().getDisplayMetrics().widthPixels, 0, true);
		mTabView.setmPopupWindow(mPopupWindow);
		mTabView.setmTabFontColor(selecttabColor, unselecttabColor);

		mPopupWindow.setAnimationStyle(R.style.AnimationInputMethod);
		mPopupWindow.setOutsideTouchable(true);
		mParent = parent;

		sTextColor = textColor;
		if (null != background) {
			mTabView.setBackgroundDrawable(background);
		}

		mTabView.setmTabLineDrawable(selecttabline, unselecttabline);
		mBackgroundDrawable = background;
		mItemBackgroundDrawable = itembackground;
		mItemLineDrawable = itemline;
		
	}

	public void setMenuData(TabData[] tabs, int colNum, int menulayout, int menuitemlayout,
			int position) {
		if (null == tabs) {
			return;
		}

		mTabs = tabs;
		mColNum = colNum;
		mMenulayout = menulayout;
		mMenuitemlayout = menuitemlayout;

		int length = tabs.length;
		for (int i = 0; i < length; i++) {
			if (i != position) {
				mContainer.addView(new LinearLayout(mContext));
			} else {
				GGMenuGridView gridView = getGGMenuGridView(position);
				mContainer.addView(gridView);
			}
		}
		mContainer.setTabCount(length);
		mContainer.setmGridViews(length);
		mContainer.setAddViewListener(this);
		mContainer.gotoTab(position);
		setSelectTab(position);
	}

	@Override
	public void addView(int position) {
		if (mContainer == null || position >= mContainer.getChildCount()) {
			return;
		}

		//当滑动菜单的时候判断是否已经添加GGMenuGridView
		View view = mContainer.getChildAt(position);
		if (view != null && view instanceof LinearLayout) {
			if (((LinearLayout) view).getChildCount() == 0) {
				GGMenuGridView gridView = getGGMenuGridView(position);
				((LinearLayout) view).addView(gridView);
			}
		}
	}

	public GGMenuGridView getGGMenuGridView(int position) {
		if (position >= mTabs.length) {
			return null;
		}
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TabData tab = mTabs[position];
		GGMenuGridView gridView = (GGMenuGridView) inflater.inflate(mMenulayout, null);

		gridView.setOnItemClickListener(this);
		gridView.setOnKeyListener(this);
		int l = gridView.getPaddingLeft();
		int t = gridView.getPaddingTop();
		int r = gridView.getPaddingRight();
		int b = gridView.getPaddingBottom();
		boolean needPadding = false;
		if (null != mItemBackgroundDrawable) {
			gridView.setSelector(mItemBackgroundDrawable);
			needPadding = true;
		}
		if (needPadding) {
			gridView.setPadding(l, t, r, b);
		}
		gridView.setNumColumns(mColNum);
		GGMenuApdater adapter = new GGMenuApdater(mContext, tab.getTextids(), tab.getDrawables(),
				tab.getIds(), mMenuitemlayout);
		gridView.setAdapter(adapter);
		// 设置分割线
		gridView.setDivLineDrawable(mItemLineDrawable);
		return gridView;
	}

	/**
	 * 设置选中tabID
	 * 
	 * @param tabid
	 */
	public void setSelectTab(int tabid) {
		mTabView.changeTabState(tabid);
	}

	/**
	 * 菜单是否正在显示
	 * 
	 * @return boolean 正在显示为true， 否则为false
	 */
	public boolean isShowing() {
		if (null != mPopupWindow) {
			return mPopupWindow.isShowing();
		} else {
			return false;
		}
	}

	/**
	 * 显示弹出菜单
	 */
	public void show() {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		// 判断是否要高亮锁定屏幕
		boolean needShowMenu = sharedPreferences.getBoolean(
				IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GGMENU, false);
		if (needShowMenu) {
			Drawable drawable = mContext.getResources().getDrawable(
					R.drawable.menuitem_deskunlock_light);
			String name = mContext.getString(R.string.menuitem_lockdesktop_unlock);
			updateItem(GGMenuData.GLMENU_ID_UNLOCKEDIT, GGMenuData.GLMENU_ID_UNLOCKEDIT, drawable,
					name);
		}

		mPopupWindow.showAtLocation(mParent, Gravity.BOTTOM | Gravity.LEFT, 0, 0);
		//用户行为统计--打开菜单
		StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_OPEN_MENU,
				StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
	}

	public void show(boolean vibrate) {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		boolean needShowMenu = sharedPreferences.getBoolean(
				IPreferencesIds.SHOULD_SHOW_SCREEN_LOCK_GGMENU, false);
		if (needShowMenu) {
			Drawable drawable = mContext.getResources().getDrawable(
					R.drawable.menuitem_deskunlock_light);
			String name = mContext.getString(R.string.menuitem_lockdesktop_unlock);
			updateItem(GGMenuData.GLMENU_ID_UNLOCKEDIT, GGMenuData.GLMENU_ID_UNLOCKEDIT, drawable,
					name);
		}
		mPopupWindow.showAtLocation(mParent, Gravity.BOTTOM | Gravity.LEFT, 0, 0);
		//用户行为统计--打开菜单
		StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_OPEN_MENU,
				StatisticsData.USER_ACTION_DEFAULT, IPreferencesIds.DESK_ACTION_DATA);
	}

	public void show(int page, boolean vibrate) {
		mPopupWindow.showAtLocation(mParent, Gravity.BOTTOM | Gravity.LEFT, 0, 0);
	}

	/**
	 * 关闭菜单
	 */
	public void dismiss() {
		GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
				IDiyMsgIds.APPDRAWER_REMOVE_GGMENU_GUIDE, -1, null, null);
		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
		}
	}

	/**
	 * 返回菜单项的数目
	 * 
	 * @return 菜单项的数目
	 */
	public int getCount() {
		// menu2.0
		return 0;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			//bug ADT-2632 Menu弹出状态时，音量键无法调节
			//add by dengdazhong
			//			if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			//				dismiss();
			//			}
			return false;
		}
		if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_ENTER
				|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			// NOTE:
			// KeyEvent.KEYCODE_ENTER 是键盘enter键
			// KeyEvent.KEYCODE_DPAD_CENTER　是感应球确定键
			if (isShowing()) {
				dismiss();
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isShowing()) {
				if (GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
						IDiyMsgIds.APPDRAWER_IS_GGMENU_GUIDE_SHOWING, -1, null, null)) {
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IDiyMsgIds.APPDRAWER_REMOVE_GGMENU_GUIDE, -1, null, null);
					return true;
				}
				dismiss();
				return true;
			}
		}
		return false;
	}

	public void updateItem(int oldId, int newid, Drawable drawable, String name) {
		GGMenuGridView[] gridviews = mContainer.getmGridViews();
		if (null != gridviews) {
			for (GGMenuGridView ggMenuGridView : gridviews) {
				GGMenuApdater adapter = (GGMenuApdater) ggMenuGridView.getAdapter();
				if (adapter.updateItem(oldId, newid, drawable, name)) {
					break;
				}
			}
		}
	}

	/**
	 * @return the mMenuItemSelectedListener
	 */
	public OnMenuItemSelectedListener getmMenuItemSelectedListener() {
		return mMenuItemSelectedListener;
	}

	/**
	 * @param mMenuItemSelectedListener
	 *            the mMenuItemSelectedListener to set
	 */
	public void setmMenuItemSelectedListener(OnMenuItemSelectedListener mMenuItemSelectedListener) {
		this.mMenuItemSelectedListener = mMenuItemSelectedListener;
	}

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		if (position >= 0) {
			int menuid = (int) parent.getAdapter().getItemId(position);
			StatisticsData.countMenuData(mContext, menuid);
			mMenuItemSelectedListener.onMenuItemSelected(menuid);
			mPopupWindow.dismiss();
		}
	}

	/**
	 * @param mDismissListener
	 *            the mDismissListener to set
	 */
	public void setmDismissListener(OnDismissListener mDismissListener) {
		if (null != mPopupWindow) {
			mPopupWindow.setOnDismissListener(mDismissListener);
		}
	}

	@Override
	public void cleanup() {
		mContext = null;

		mPopupWindow = null;

		mParent = null;

		// 点击监听
		mMenuItemSelectedListener = null;

		if (null != mTabView) {
			mTabView.cleanup();
			mTabView = null;
		}
		mContainer = null;
		mBackgroundDrawable = null;
		mItemBackgroundDrawable = null;
		mItemLineDrawable = null;
		mTabs = null;
	}
}
