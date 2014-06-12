package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.KeyEvent;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.DataSetObserver;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.ProManageAdapter;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 功能表正在运行Tab对应内容
 * @author yangguanxiang
 *
 */
public class ProManageTabBasicContent extends AppFuncTabBasicContent
		implements
			IComponentEventListener {

	private int mFocuseIndex;
	private boolean isAdapterChanged = false; // adapter数据是否变化
	private ProManageHomeIcon proHome;
//	private ProManageEditDock proHomeEdit; // TODO 这个变量等做正在运行优化时请删除，不再需要了，把他移到AppFunTabComponent中去
	private AlertDialog mDialog;

	public ProManageTabBasicContent(Activity activity, int tickCount, int x, int y, int width,
			int height, int gridId) {
		super(activity, tickCount, x, y, width, height, gridId);
		mFocuseIndex = -1;
		// 添加组件
		mGrid.setEventListener(this);
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.PROCESS_GRID, mGrid);
		addComponent(mGrid);
		// 监听adapter是否有数据变化
		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onInvalidated() {
				isAdapterChanged = true;

			}
		});
		proHome = new ProManageHomeIcon(mContext, tickCount, x, y, width, height);
		proHome.setAdapter((ProManageAdapter) mAdapter);
		mHomeComponent.setDockContent(proHome);
//		proHomeEdit = new ProManageEditDock(activity, tickCount, x, y, width, height);
//		if (mAdapter instanceof ProManageAdapter) {
//			((ProManageAdapter) mAdapter).setProManageEdithome(proHomeEdit);
//		}
//		mHomeComponent.setEditStateDockContent(proHomeEdit);
		addComponent(mHomeComponent);
	}

	@Override
	public void notify(int key, Object obj) {
		if (key == AppFuncConstants.THEME_CHANGE) {
			mHomeComponent.resetResource();
		} else if (key == AppFuncConstants.LOADTHEMERES) {
			mHomeComponent.loadResource();
		}
	}

	@Override
	protected AppFuncAdapter initCurrentAdapter() {
		// 是否显示名称
		boolean showName = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
				? false
				: true;
		return new ProManageAdapter(mActivity, showName);
	}

	@Override
	public void layout(int left, int top, int right, int bottom) {
		setPosition(left, top, right, bottom);
		layoutGrid(left, top, right, bottom);
		super.layout(left, top, right, bottom);
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
		// int bottomHeight = mUtils.getStandardSize(sBottomHeight);
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

	@Override
	public void tabChangeUpdate() {
		if (proHome != null) {
			proHome.refreshMemory(true);
			// proHome.startAddMotion();
		}

	};

	/**
	 * 默认的处理是否可以聚焦
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
	 * 键盘处理事件
	 */
	@Override
	public boolean onKey(KeyEvent event) {
		boolean isVertical = AppFuncUtils.getInstance(mActivity).isVertical();
		// if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
		// return true;
		// }
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
					closePrograms();
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

	public void release() {
		// 释放
		((ProManageAdapter) mAdapter).release();
	}

	/**
	 * 焦点返回处理
	 */
	@Override
	public void passFocus() {
		mFocuseIndex = 0;
	}

	/**
	 * 关闭管理程序
	 */
	private void closePrograms() {

		AlertDialog.Builder builder = new DeskBuilder(mActivity);
		builder.setTitle(mActivity.getString(R.string.dlg_promanageTitle));
		builder.setMessage(mActivity.getString(R.string.dlg_promanageContent));
		builder.setPositiveButton(mActivity.getString(R.string.sure),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						((ProManageAdapter) mAdapter).teminateCurApps();
					}
				});

		builder.setNegativeButton(mActivity.getString(R.string.cancel), null);
		try {
			mDialog = builder.show();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			OutOfMemoryHandler.handle();
		}

	}

	public void swtichHomeToDesk(boolean needAnimate) {
		if (mHomeComponent != null) {
			mHomeComponent.showEditDockContent(needAnimate);
		}
	}

	public void swtichDeskToHome(boolean needAnimate) {
		if (mHomeComponent != null) {
			mHomeComponent.showDockContent(needAnimate);
		}
		if (isAdapterChanged && proHome != null) {
			proHome.refreshMemory(false);
			isAdapterChanged = false;
		}

	}

	@Override
	protected void onHide() {
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			mDialog = null;
		}
		super.onHide();
	}

	@Override
	protected void checkAndChangeHomeComponentStatus() {
		mHomeComponent.showDockContent(false);
	}
}
