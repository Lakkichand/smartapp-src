package com.gau.go.launcherex.theme.cover.ui.action;

import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
/**
 * 
 * @author jiangxuwen
 *
 */
public abstract class ShakeAction extends BaseAction {
	public int mMaxSpeed;
	public ShakeAction(Drivenable drivenable, int actionIndex,
			int animatingBitmapType, boolean isBitmapSymmetric, int shakeSpeed) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric);
		// TODO Auto-generated constructor stub
		mMaxSpeed = shakeSpeed;
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
	}
	
	public abstract void handShake(float speed);
	
	@Override
	public boolean doAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		// TODO Auto-generated method stub
		
	}

}
