package com.gau.go.launcherex.theme.cover.ui.action;

import android.graphics.Bitmap;

/**
 * 
 * <br>类描述:空的action
 * <br>功能详细描述:为了填充无action
 * 
 * @author  guoyiqing
 * @date  [2012-11-10]
 */
public class EmptyAction extends BaseAction {

	public EmptyAction(Drivenable drivenable, int actionIndex, int animatingBitmapType, boolean isBitmapSymmetric) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
	}

	@Override
	public boolean doAction() {
		return false;	
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow, Bitmap[] action1Bitmaps,
			Bitmap[] action2Bitmaps, Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		
	}

	@Override
	public void cleanUp() {
	}

}
