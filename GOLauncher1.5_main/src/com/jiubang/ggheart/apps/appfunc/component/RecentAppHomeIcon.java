package com.jiubang.ggheart.apps.appfunc.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.common.component.CommonActionBar;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncImageButton.OnClickListener;
import com.jiubang.ggheart.apps.appfunc.controler.AppDrawerControler;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogConfirm;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
/**
 * 
 * <br>类描述:功能表最近打开底部操作栏
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-26]
 */
public class RecentAppHomeIcon extends CommonActionBar implements OnClickListener {
	private static final int HOME_WIDTH_ID = R.dimen.appfunc_home_width;
	private static final int HOME_HEIGTH_ID = R.dimen.appfunc_home_heigth;
	private AppFuncImageButton mCleanupButton;
	// private FunAppSetting mFunAppSetting;
	private DialogConfirm mDialog = null;

	public RecentAppHomeIcon(Context context, int tickCount, int x, int y, int width, int height) {
		super(context, tickCount, x, y, width, height, TYPE_SIMPLE, HIDE_TYPE_BOTTOM);
		int buttonWidth = mUtils.getDimensionPixelSize(HOME_WIDTH_ID);
		int buttonHeight = mUtils.getDimensionPixelSize(HOME_HEIGTH_ID);
		mCleanupButton = new AppFuncImageButton(mActivity, tickCount, 0, 0, buttonWidth,
				buttonHeight);
		mCleanupButton.setClickListener(this);
		addComponent(mCleanupButton, LAYOUT_GRAVITY_RIGHT);
		showDivider(true);
		// mFunAppSetting =
		// GOLauncherApp.getSettingControler().getFunAppSetting();
	}

	public void initResource(String packageName) {
		super.loadResource(packageName);
		Drawable cleanNormal = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeCleanNormal, packageName);
		Drawable cleanLight = mThemeCtrl.getDrawable(
				mThemeCtrl.getThemeBean().mRuningDockBean.mHomeCleanLight, packageName);
		mCleanupButton.setIcon(cleanNormal);
		mCleanupButton.setIconPressed(cleanLight);
	}

	@Override
	public void loadResource(String packageName) {
		initResource(packageName);
	}

	@Override
	public void onClick(XComponent view) {
		if (view == mCleanupButton) {
			clearHistory();
		}
	}

	private void clearHistory() {
		mDialog = new DialogConfirm(mActivity);
		mDialog.show();
		mDialog.setTitle(R.string.dlg_promanageTitle);
		mDialog.setMessage(R.string.dlg_recentContent);
		mDialog.setPositiveButton(null, new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				AppCore.getInstance().getRecentAppControler().removeRecentAppItems();
				AppDrawerControler.getInstance(mActivity).removeAllRecentAppItems();
			}
		});
	}

	@Override
	protected synchronized void onHide() {
		if (mDialog != null) {
			if (mDialog.isShowing()) {
				mDialog.dismiss();
			}
			mDialog = null;
		}
		super.onHide();
	}

	//	@Override
	//	protected void onShow() {
	//		if (mCleanupButton != null) {
	//			addComponent(mCleanupButton, LAYOUT_GRAVITY_RIGHT);
	//		}
	//		super.onShow();
	//	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		super.drawCurrentFrame(canvas);
		if (indexOfComponent(mCleanupButton) > -1) {
			if (GoLauncher.isPortait()) {
				ImageUtil.drawImage(canvas, mDividerV, ImageUtil.CENTERMODE, mCleanupButton.mX
						- mDividerSpace, mPaddingTop, mCleanupButton.mX, mHeight - mPaddingBottom,
						mDividerPaint);
			} else {
				ImageUtil.drawImage(canvas, mDividerH, ImageUtil.CENTERMODE, mPaddingLeft,
						mCleanupButton.mY + mCleanupButton.getHeight(), mWidth - mPaddingRight,
						mCleanupButton.mY + mCleanupButton.getHeight() + mDividerSpace,
						mDividerPaint);
			}
		}
	}
}
