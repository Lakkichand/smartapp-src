package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.RecentAppAdapter;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 最近打开Tab对应的基本风格内容控件
 * 
 * @author tanshu
 * 
 */
public class RecentAppTabBasicContent extends AppFuncTabBasicContent
		implements
			IComponentEventListener {
	/**
	 * "清空历史记录"按钮
	 */
	private int mFocuseIndex;

	private int mNoDataBGW = 0;
	private int mNoDataBGH = 0;
	private Rect mNoDataBGR = new Rect();
	/**
	 * 没有数据时的背景图
	 */
	private Drawable mNoDataBg = null;

	private String mTips = null;
	private float mTipsX = 0;
	private float mTipsY = 0;
	private Dialog mDialog;
	private Paint mPaint = null;
	private int mTextColor;

	public RecentAppTabBasicContent(Activity activity, int tickCount, int x, int y, int width,
			int height, int gridId) {
		super(activity, tickCount, x, y, width, height, gridId);
		mGrid.setEventListener(this);
		addComponent(mGrid);
		mHomeComponent.setDockContent(new RecentAppHomeIcon(activity, tickCount, x, y, width,
				height));
		// addComponent(mHomeComponent);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		loadThemeResource();
		mFocuseIndex = -1;
		mPaint.setColor(mTextColor);
		mPaint.setTextSize(AppFuncUtils.getInstance(mActivity).getScaledSize(24));
	}

	private void loadThemeResource() {
		String curPackageName = ThemeManager.getInstance(mActivity).getCurThemePackage();
		String packageName = null;
		if (!curPackageName.equals(GOLauncherApp.getSettingControler().getFunAppSetting()
				.getTabHomeBgSetting())) {
			packageName = GOLauncherApp.getSettingControler().getFunAppSetting()
					.getTabHomeBgSetting();
		}
		if (!AppUtils.isAppExist(mContext, packageName)) {
			packageName = GOLauncherApp.getThemeManager().getCurThemePackage();
		}
		mNoDataBg = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRecentDockBean.mHomeRecentNoDataBg, packageName);
		mTextColor = mThemeCtrl.getThemeBean().mRecentDockBean.mHomeRecentNoDataTextColor;
		mPaint.setColor(mTextColor);
	}

	@Override
	public void notify(int key, Object obj) {
		if (key == AppFuncConstants.THEME_CHANGE) {
			mHomeComponent.resetResource();
			mNoDataBg = null;
		} else if (key == AppFuncConstants.LOADTHEMERES) {
			mHomeComponent.loadResource();
			// mNoDataBg =
			// mContext.getResources().getDrawable(R.drawable.appfunc_recent_no_data_bg);
			loadThemeResource();
			if (mNoDataBg != null) {
				mNoDataBGW = mNoDataBg.getIntrinsicWidth();
				mNoDataBGH = mNoDataBg.getIntrinsicHeight();
			}
			mTips = mContext.getResources().getString(R.string.appfunc_no_recent_data);
		}
	}

	@Override
	protected AppFuncAdapter initCurrentAdapter() {
		// 是否显示名称
		boolean showName = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
				? false
				: true;
		return new RecentAppAdapter(mActivity, showName);
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		setPosition(left, top, right, bottom);

		mNoDataBGR.set((mWidth - mNoDataBGW) / 2, AppFuncUtils.getInstance(mActivity)
				.getStandardSize(70), (mWidth - mNoDataBGW) / 2 + mNoDataBGW, AppFuncUtils
				.getInstance(mActivity).getStandardSize(70) + mNoDataBGH);
		if (mNoDataBg != null) {
			mNoDataBg.setBounds(mNoDataBGR);
		}

		if (mTips != null) {
			float textW = mPaint.measureText(mTips);
			mTipsX = (mWidth - textW) / 2;
			mTipsY = mNoDataBGR.bottom + AppFuncUtils.getInstance(mActivity).getStandardSize(44);
		}
		layoutGrid(left, top, right, bottom);
		RecentAppAdapter adapter = (RecentAppAdapter) mAdapter;
		adapter.setMaxCount(mGrid.getRowNums() * mGrid.getColunmNums());
		adapter.loadApp();
		if (adapter.getCount() == 0) {
			removeComponent(mHomeComponent);
		} else {
			addComponent(mHomeComponent);
		}
		super.layout(left, top, right, bottom);
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mAdapter.getCount() == 0) {
			if (mNoDataBg != null) {
				mNoDataBg.draw(canvas);
			}
			if (mTips != null) {
				canvas.drawText(mTips, mTipsX, mTipsY, mPaint);
			}
		} else {
			super.drawCurrentFrame(canvas);
		}
	}

	@Override
	public boolean onEventFired(XComponent component, byte eventType, Object event, int arg,
			Object object) {

		if (component == mGrid) {
			if (eventType == EventType.FOCUSEVENTPASS) {
				passFocus();
			}
			return true;
		}

		return false;
	}

	/**
	 * 布局宫格
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	@Override
	protected void layoutGrid(int left, int top, int right, int bottom) {
		setGridParameters();
		DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();
		int padding = (int) (AppFuncConstants.SCROLL_SIZE * metrics.density);
		// int bottomHeight =
		// AppFuncUtils.getInstance(mActivity).getStandardSize(sBottomHeight);
		int bottomHeight = 0;
		if (AppFuncFrame.getDataHandler().isShowActionBar()) {
			bottomHeight = mUtils.getDimensionPixelSize(sBottomHeight_id);
		}
		if (metrics.widthPixels > metrics.heightPixels) {
			mGrid.setOrientation(XBaseGrid.HORIZONTAL);
			mGrid.setSize(right - left - bottomHeight, bottom - top);
		} else {
			mGrid.setOrientation(XBaseGrid.VERTICAL);
			mGrid.setSize(right - left, bottom - top - bottomHeight);
		}
		GoSettingControler goSettingControler = GOLauncherApp.getSettingControler();
		ScreenSettingInfo screenInfo = goSettingControler.getScreenSettingInfo();
		String indicatorPos = screenInfo.mIndicatorPosition;
		if (indicatorPos.equals(ScreenIndicator.INDICRATOR_ON_TOP)) {
			mGrid.setPaddingTop(padding * 2);
			if (mUtils.isVertical()) {
				mGrid.setPaddingBottom(padding);
			} else {
				mGrid.setPaddingBottom(0);
			}
		} else {
			mGrid.setPaddingTop(0);
			mGrid.setPaddingBottom(padding * 2);
		}
		mGrid.setPaddingLeft(0);
		mGrid.setPaddingRight(0);
		mGrid.updateLayoutParams();
	}

	/**
	 * 是否可以聚焦
	 */
	@Override
	public boolean requestFocused() {

		if (getAdapter().getCount() > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 焦点返回处理
	 */
	@Override
	public void passFocus() {
		mFocuseIndex = 0;

	}

	/**
	 * 聚焦组件
	 */
	@Override
	public void setFocused(boolean on) {
		if (on) {
			mFocuseIndex = 0;

		} else {
			mFocuseIndex = -1;

		}
	}

	/**
	 * 清除历史记录
	 */

	private void clearHistory() {
		AlertDialog.Builder builder = new DeskBuilder(mActivity);
		builder.setTitle(mActivity.getString(R.string.dlg_promanageTitle));
		builder.setMessage(mActivity.getString(R.string.dlg_recentContent));
		builder.setPositiveButton(mActivity.getString(R.string.ok),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
//						AppCore.getInstance().getRecentAppControler().removeRecentAppItems();
						AppDrawerControler.getInstance(mActivity).removeAllRecentAppItems();
					}
				});
		builder.setNegativeButton(mActivity.getString(R.string.cancel), null);
		mDialog = builder.show();
		mDialog.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_HOME) {
					mDialog.dismiss();
				}
				return false;
			}
		});
	}

	@Override
	public boolean onKey(KeyEvent event) {
		boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();

		if (mFocuseIndex == 1 || mFocuseIndex == -1) {
			if (mGrid != null) {
				return mGrid.onKey(event);
			}
		}

		if (mFocuseIndex == 0) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (isVertical) {
						if (getAdapter().getCount() > 0) {

							mFocuseIndex++;
							mGrid.setFocused(true);
						}
					}
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
					if (isVertical) {

						mFocuseIndex = -1;
						AppFuncHandler.getInstance().setTabHasFocus();
					}
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
					if (!isVertical) {

						mFocuseIndex = -1;
						AppFuncHandler.getInstance().setTabHasFocus();
					}
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
					if (!isVertical) {
						if (getAdapter().getCount() > 0) {

							mFocuseIndex++;
							mGrid.setFocused(true);
						}
					}
				}
			} else if (event.getAction() == KeyEvent.ACTION_UP) {
				// Enter键只能收到UP消息
				if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
						|| (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
					clearHistory();
				} else if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
					if (ApplicationIcon.sIsStartApp == false) {
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
								AppFuncConstants.EXITAPPFUNCFRAME, null);
					}
				}
			}
			return true;
		}
		ApplicationIcon.sIsStartApp = false;
		return false;
	}

	public void dimissDialog() {
		if (null != mDialog && mDialog.isShowing()) {
			mDialog.dismiss();
		}
	}

	@Override
	protected synchronized void onHide() {
		dimissDialog();
		super.onHide();
	}
}
