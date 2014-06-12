package com.gau.go.launcherex.theme.cover.ui.action;

import java.util.ArrayList;
import java.util.Random;
import com.gau.go.launcherex.theme.cover.DrawUtils;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * 
 * @author maxiaojun
 * 
 */
public class FallObjectAction extends ShakeAction {
	private Bitmap[] mActionBitmaps;
	private Drivenable mDrivenable;
	private float mStartX;
	private float mStartY;
	private long mCurrTime;
	private int mBitmapIndex;
	private ArrayList<FallObject> mObjectList;
	private Random mRandom = new Random();
	private boolean mIsDraw;
	private int mScreenHeight;

	public FallObjectAction(Drivenable drivenable, Bitmap[] actionBitmaps,
			int actionIndex, int animatingBitmapType,
			boolean isBitmapSymmetric, int shakeSpeed) {
		super(drivenable, actionIndex, animatingBitmapType, isBitmapSymmetric,
				shakeSpeed);
		// TODO Auto-generated constructor stub
		mActionBitmaps = actionBitmaps;
		mDrivenable = drivenable;
		mObjectList = new ArrayList<FallObject>();
		mIsDraw = false;
		mScreenHeight = DrawUtils.getScreenViewHeight();
		init();
	}

	private void init() {
		mBitmapIndex = 0;
		mStartX = mDrivenable.mX + 100;
		mStartY = mDrivenable.mY + 50;
		mObjectList.clear();
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
		mBitmapIndex = 0;
		mActionBitmaps = null;
		mObjectList.clear();
	}

	@Override
	public void onDraw(Camera camera, Matrix matrix, Canvas canvas, Paint paint) {
		super.onDraw(camera, matrix, canvas, paint);
		if (!mIsDraw) {
			return;
		}
		canvas.save();
		int size = mObjectList.size();
		for (int i = 0; i < size; i++) {
			FallObject object = mObjectList.get(i);
			if (object.mIsAlive) {
				canvas.drawBitmap(object.mBitmap, object.mX, object.mY, paint);
			}
		}
		canvas.restore();
	}

	@Override
	public boolean doAction() {
		// TODO Auto-generated method stub
		long time = System.currentTimeMillis();
		if (mBitmapIndex < mActionBitmaps.length && time - mCurrTime > 300) {
			float x = mRandom.nextBoolean() ? mStartX + mRandom.nextInt(18) + 8
					: mStartX - (mRandom.nextInt(18) + 8);
			FallObject object = new FallObject(mActionBitmaps[mBitmapIndex], x,
					mStartY);
			mObjectList.add(mBitmapIndex, object);
			mBitmapIndex++;
		}

		int size = mObjectList.size();
		for (int i = 0; i < size; i++) {
			FallObject object = mObjectList.get(i);
			mStartY += 10;
			object.mY += 10;
			if (object.mY > mScreenHeight) {
				object.mIsAlive = false;
			}
		}

		return true;
	}

	/**
	 * 
	 * @author maxiaojun
	 * 
	 */
	class FallObject {
		private boolean mIsAlive;
		private float mX;
		private float mY;
		private Bitmap mBitmap;

		public FallObject(Bitmap bitmap, float x, float y) {
			mIsAlive = true;
			mX = x;
			mY = y;
			mBitmap = bitmap;
		}
	}

	@Override
	public Bitmap getBimap(int deccelerate) {
		// TODO Auto-generated method stub
		return mDrivenable.mBitmap;
	}

	@Override
	public void onResume(Bitmap[] actionBitmaps, Bitmap shadow,
			Bitmap[] action1Bitmaps, Bitmap[] action2Bitmaps,
			Bitmap action1Shadow, Bitmap action2Shadow,
			Bitmap defaultAction2Bitmap) {
		// TODO Auto-generated method stub
		mActionBitmaps = actionBitmaps;
	}

	
	@Override
	public void handShake(float speed) {
		// TODO Auto-generated method stub
//		if (speed > mMaxSpeed) {
			mIsDraw = true;
			init();
//		}
	}

}
