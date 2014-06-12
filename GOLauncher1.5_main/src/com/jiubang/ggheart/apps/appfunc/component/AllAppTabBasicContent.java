package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogSingleChoice;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncExceptionHandler;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.AllAppAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncAdapter;
import com.jiubang.ggheart.apps.desks.appfunc.model.DataSetObserver;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 所有程序Tab对应的基本风格内容控件,在ui上表现为tab栏切换到所有程序时tab栏（不包括）下面包括所有图标和底部操作栏的整个部分
 * 
 * @author tanshu
 * 
 */
public class AllAppTabBasicContent extends AppFuncTabBasicContent
		implements
			IComponentEventListener {

	private Thread mThread = null;

	private AppFuncUtils mUtils;

	public AllAppTabBasicContent(Activity activity, int tickCount, int x, int y, int width,
			int height, int gridId) {
		super(activity, tickCount, x, y, width, height, gridId);
		// XBaseGrid.layout中会做这一步，这里没必要做了
		// mAdapter.loadApp();
		// 监听adapter,停止进度条
		mUtils = AppFuncUtils.getInstance(activity);

		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onInvalidated() {
				super.onInvalidated();
				dismissProgressDialog();
			}

			@Override
			public void onNotify(MessageID msgId, Object obj1, Object obj2) {
				if (msgId == AppFuncConstants.MessageID.ALL_SORTSETTING) {
					beginSortIcon(((Integer) obj1).intValue());

					Log.i("pl", "notify value is " + ((Integer) obj1).intValue());
				}
			}
		});
		// 内部事件监听者
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.ALLAPPS_TABCONTENT,
				this);
		// 注册桌面锁屏事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOCKCHANGES,
				this);

		// 为所有程序网格组件注册图标定位事件监听
		DeliverMsgManager.getInstance().registerMsgHandler(AppFuncConstants.ALLAPPS_GRID, mGrid);

		// mHomeComponent.setChildEventListener(this);
		mHomeComponent
				.setDockContent(new AppFuncHomeIcon(mContext, tickCount, x, y, width, height));
		mHomeComponent.setEditStateDockContent(new AppMoveToDesk(activity, tickCount, x, y, width,
				height));

		// 添加XGrid
		mGrid.setGridId(gridId);
		mGrid.setIsFolderEnable(true);
		mGrid.setZorder(1);
		mGrid.setEventListener(this);
		mGrid.setDragable(!GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen);

		addComponent(mGrid);
		addComponent(mHomeComponent);

	}

	private void beginSortIcon(final int itemCaused) {
		showProgressDialog();

		GOLauncherApp.getSettingControler().getFunAppSetting().setSortType(itemCaused, false);

		// 通知桌面文件夹排序方式改变
		// GoLauncher.sendHandler(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.EVENT_UPDATE_ALL_FOLDER_PREVIEW, -1, null, null);
		Log.i("pl", "itemCaused is" + itemCaused);
		// 启动一个新线程去调用后台
		mThread = new Thread(ThreadName.FUNC_SORT) {
			@Override
			public void run() {
				try {
					switch (itemCaused) {
						case FunAppSetting.SORTTYPE_LETTER :
							AppFuncFrame.getFunControler().sortByLetterAndSave("ASC");
							break;
						case FunAppSetting.SORTTYPE_TIMENEAR :
							AppFuncFrame.getFunControler().sortByTimeAndSave("DESC");
							break;
						case FunAppSetting.SORTTYPE_TIMEREMOTE :
							AppFuncFrame.getFunControler().sortByTimeAndSave("ASC");
							break;
						case FunAppSetting.SORTTYPE_FREQUENCY :
							AppFuncFrame.getFunControler().sortByFrequencyAndSave("DESC");
							break;
						default :
							break;
					}
				} catch (DatabaseException e) {
					AppFuncExceptionHandler.handle(e);
					dismissProgressDialog();
				}
			}
		};
		mThread.start();
	}

	// @Override
	// protected void drawCurrentFrame(Canvas canvas) {
	//
	//
	// if (mHomeComponent!=null) {
	// mHomeComponent.paintCurrentFrame(canvas, mHomeComponent.mX,
	// mHomeComponent.mY);
	// }
	// if (mGridContainer!=null) {
	// mGridContainer.paintCurrentFrame(canvas, mGridContainer.mX,
	// mGridContainer.mY);
	// }
	// }

	public void showSelectSort() {
		int selectedItem = GOLauncherApp.getSettingControler().getFunAppSetting().getSortType();

		try {
			DialogSingleChoice mDialog = new DialogSingleChoice(mActivity);
			mDialog.show();
			mDialog.setTitle(R.string.dlg_sortChangeTitle);
			final CharSequence[] items = mActivity.getResources().getTextArray(
					R.array.select_sort_style);
			mDialog.setItemData(items, selectedItem, true);
			mDialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {
					beginSortIcon(item);
				}
			});
		} catch (Exception e) {
			try {
				DeskToast.makeText(mContext, R.string.alerDialog_error, Toast.LENGTH_SHORT).show();
			} catch (OutOfMemoryError error) {
				OutOfMemoryHandler.handle();
			}
		}
	}

	private void showProgressDialog() {
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
				AppFuncConstants.PROGRESSBAR_SHOW, null);
	}

	public synchronized void dismissProgressDialog() {
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
				AppFuncConstants.PROGRESSBAR_HIDE, null);
	}

	@Override
	public void notify(int key, Object obj) {

		switch (key) {
			case AppFuncConstants.EXITEDITMODEL : {
				mGrid.setDragStatus(false);
				break;
			}
			case AppFuncConstants.THEME_CHANGE : {
				mHomeComponent.resetResource();
				break;
			}
			case AppFuncConstants.LOADTHEMERES : {
				mHomeComponent.loadResource();
				break;
			}
			case AppFuncConstants.LOCKCHANGES : {
				mGrid.setDragable(!GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen);
				break;
			}
			case AppFuncConstants.RESETHOMEMOVETODESK : {
				mHomeComponent.resetMotion();
				break;
			}
		}
	}

	@Override
	protected AppFuncAdapter initCurrentAdapter() {
		// 是否显示名称
		boolean showName = (AppFuncFrame.getDataHandler().getShowName() < FunAppSetting.APPNAMEVISIABLEYES)
				? false
				: true;
		return new AllAppAdapter(mActivity, showName);
	}

	/**
	 * 重新布局
	 */
	@Override
	public void layout(int left, int top, int right, int bottom) {

		setPosition(left, top, right, bottom);
		layoutGrid(left, top, right, bottom);
		super.layout(left, top, right, bottom);
	}

	/**
	 * 默认的键盘处理事件
	 */
	@Override
	public boolean onKey(KeyEvent event) {
		if (mGrid != null) {
			return mGrid.onKey(event);
		}

		return false;
	}

	@Override
	public boolean onEventFired(XComponent component, byte eventType, Object event, int arg,
			Object object) {

		if (component == mHomeComponent) {
			// 退出功能表时停止编辑模式动画
			if (mGrid != null) {
				mGrid.setDragStatus(false);
			}
		}

		if (component == mGrid) {
			if (eventType == EventType.FOCUSEVENTPASS) {
				passFocus();
			}
		}

		return true;
	}

	protected void setGridSize(int left, int top, int right, int bottom) {
		int bottomHeight = 0;
		if (AppFuncFrame.getDataHandler().isShowActionBar()) {
			bottomHeight = mUtils.getDimensionPixelSize(sBottomHeight_id);
		}
		if (mUtils.isVertical()) {
			mGrid.setSize(right - left, bottom - top - bottomHeight);
		} else {
			mGrid.setSize(right - left - bottomHeight, bottom - top);
		}
	}

	/**
	 * XGrid布局
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
		if (metrics.widthPixels > metrics.heightPixels) {
			mGrid.setOrientation(XBaseGrid.HORIZONTAL);
		} else {
			mGrid.setOrientation(XBaseGrid.VERTICAL);
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
		setGridSize(left, top, right, bottom);
		mGrid.updateLayoutParams();
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
	}

	public int getHomeComponetSize() {
		if (mUtils.isVertical()) {
			return mHomeComponent.getHeight();
		} else {
			return mHomeComponent.getWidth();
		}
	}

	@Override
	protected void onShow() {
		super.onShow();
	}
}
