package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.view.View;

import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;

/**
 * 主题展示，将应用主题的样式代码集中到这个类中
 * 
 * @author luopeihuan
 * 
 */

public class ScreenThemeSpreader {
	private final WeakReference<Workspace> mWorkspace;
	private final WeakReference<DesktopIndicator> mIndicator;
	private final WeakReference<DeskThemeControler> mThemeControler;
	private final WeakReference<Activity> mActivity;
	private ScreenThemeHandler mHandler;

	public ScreenThemeSpreader(Activity activity, DeskThemeControler controler,
			Workspace workspace, DesktopIndicator indicator) {
		mActivity = new WeakReference<Activity>(activity);
		mWorkspace = new WeakReference<Workspace>(workspace);
		mIndicator = new WeakReference<DesktopIndicator>(indicator);
		mThemeControler = new WeakReference<DeskThemeControler>(controler);
	}

	public void applyTheme() {
		DeskThemeControler controler = mThemeControler.get();
		if (controler == null) {
			return;
		}

		// 指示器主题更改
		applyIndicatorTheme();

		DeskThemeBean themeBean = controler.getDeskThemeBean();
		if (themeBean != null && controler.isUesdTheme()) {
			applyScreenTheme(themeBean.mScreen);
		} else {
			applyScreenTheme(null);
		}
	}

	public void cancel() {
		if (mHandler != null) {
			mHandler.stop();
			mHandler = null;
		}
	}

	private void applyScreenTheme(DeskThemeBean.ScreenBean screenBean) {
		Workspace workspace = mWorkspace.get();
		Activity activity = mActivity.get();
		if (workspace == null || activity == null) {
			return;
		}

		// TODO 暂时不修改布局参数
		/*
		 * if(screenBean != null && screenBean.mIconStyle != null) {
		 * if(CellLayout.mPortrait) {
		 * CellLayout.setCellWidth(screenBean.mIconStyle.mCellWidthPort);
		 * CellLayout.setCellHeight(screenBean.mIconStyle.mCellHeightPort); }
		 * else { CellLayout.setCellWidth(screenBean.mIconStyle.mCellWidthLand);
		 * CellLayout.setCellHeight(screenBean.mIconStyle.mCellHeightLand); } }
		 * else { if(CellLayout.mPortrait) { final int width =
		 * activity.getResources
		 * ().getDimensionPixelSize(R.dimen.cell_width_port);
		 * CellLayout.setCellWidth(width); final int height =
		 * activity.getResources
		 * ().getDimensionPixelSize(R.dimen.cell_height_port);
		 * CellLayout.setCellHeight(height); } else { final int width =
		 * activity.
		 * getResources().getDimensionPixelSize(R.dimen.cell_width_land);
		 * CellLayout.setCellWidth(width); final int height =
		 * activity.getResources
		 * ().getDimensionPixelSize(R.dimen.cell_height_land);
		 * CellLayout.setCellHeight(height); } }
		 */

		ArrayList<BubbleTextView> iconList = new ArrayList<BubbleTextView>();
		final int count = workspace.getChildCount();
		for (int i = 0; i < count; i++) {
			CellLayout layout = (CellLayout) workspace.getChildAt(i);
			if (layout == null) {
				continue;
			}
			final int childCount = layout.getChildCount();
			for (int j = 0; j < childCount; j++) {
				View child = layout.getChildAt(j);
				if (child != null && child instanceof BubbleTextView) {
					iconList.add((BubbleTextView) child);
				}
			}
		}

		if (!iconList.isEmpty()) {
			cancel();
			mHandler = new ScreenThemeHandler(iconList);
			mHandler.start();
		}
		workspace.requestLayout();
	}

	private static void changeTheme(ArrayList<BubbleTextView> arrayList) {
		int count = Math.min(ScreenThemeHandler.ITEMS_COUNT, arrayList.size());
		while (count-- > 0) {
			final BubbleTextView icon = arrayList.remove(0);
			icon.init();
		}
	}

	private void applyIndicatorTheme() {
		DesktopIndicator indicator = mIndicator.get();
		if (indicator != null) {
			indicator.applyTheme();
			indicator.requestLayout();
		}
	}

	private static class ScreenThemeHandler extends Handler implements MessageQueue.IdleHandler {
		private ArrayList<BubbleTextView> mIconList;
		private boolean mStop = false;
		static final int ITEMS_COUNT = 4;

		ScreenThemeHandler(ArrayList<BubbleTextView> iconArrayList) {
			mIconList = iconArrayList;
		}

		@Override
		public boolean queueIdle() {
			if (mStop) {
				return false;
			}
			if (mIconList != null && !mIconList.isEmpty()) {
				changeTheme(mIconList);
				return true;
			}
			return false;
		}

		private void start() {
			if (mStop) {
				return;
			}

			// Ask for notification when message queue becomes idle
			final MessageQueue messageQueue = Looper.myQueue();
			messageQueue.addIdleHandler(this);
		}

		private void stop() {
			mStop = true;
			final MessageQueue messageQueue = Looper.myQueue();
			messageQueue.removeIdleHandler(this);

			if (mIconList != null) {
				mIconList.clear();
				mIconList = null;
			}
		}
	}
}
