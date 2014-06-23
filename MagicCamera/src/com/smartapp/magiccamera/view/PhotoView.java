package com.smartapp.magiccamera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class PhotoView extends View {

	private Bitmap mBitmap;

	private float[] mVerts;

	public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PhotoView(Context context) {
		super(context);
	}

	public void refresh(Bitmap bitmap, float[] verts) {
		mBitmap = bitmap;
		mVerts = verts;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap == null || mVerts == null) {
			return;
		}
		canvas.drawBitmapMesh(this.mBitmap, 63, 63, this.mVerts, 0, null, 0,
				null);
	}
}
