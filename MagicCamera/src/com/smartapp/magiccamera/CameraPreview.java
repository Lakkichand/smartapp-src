/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smartapp.magiccamera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.slidinglayer.SlidingLayer;
import com.smartapp.funmirror.util.FileUtil;
import com.smartapp.funmirror.util.MeshUtil;
import com.smartapp.funmirror.wrap.BulgeWarp;
import com.smartapp.funmirror.wrap.DentWarp;
import com.smartapp.funmirror.wrap.NoWarp;
import com.smartapp.funmirror.wrap.SquareWarp;
import com.smartapp.funmirror.wrap.SwirlWarp;
import com.smartapp.funmirror.wrap.TallWarp;
import com.smartapp.funmirror.wrap.WarpStrategy;
import com.smartapp.funmirror.wrap.Warper;
import com.smartapp.funmirror.wrap.WideWarp;
import com.smartapp.magiccamera.view.PhotoView;
// Need the following import to get access to the app resources, since this
// class is in a sub-package.

// ----------------------------------------------------------------------

@SuppressLint("NewApi")
public class CameraPreview extends Activity {
	private Preview mPreview;
	Camera mCamera;
	int numberOfCameras;
	int cameraCurrentlyLocked;

	// The first rear facing camera
	int defaultCameraId;

	private PhotoView mPhotoView;

	private ByteArrayOutputStream mBOS;
	private Rect mRect;

	private float[] mOrigArray = null;

	private float[] mVertsArray = null;

	private Warper mWrapper = new Warper();

	private WarpStrategy mWarpStrategy = null;

	private int rotationInDegrees = 180;

	private Matrix mM;

	private ImageView mTakePic;

	private ImageView mSwitchCam;

	private Button mOpen;
	private Button mClose;
	private SlidingLayer mSlidingLayer;

	/**
	 * 预览回调
	 */
	private PreviewCallback mPreviewCallback = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Size size = camera.getParameters().getPreviewSize();
			int w = size.width;
			int h = size.height;
			YuvImage image = new YuvImage(data, ImageFormat.NV21, w, h, null);
			if (mBOS == null) {
				mBOS = new ByteArrayOutputStream();
			}
			mBOS.reset();
			if (mRect == null) {
				mRect = new Rect(0, 0, w, h);
			}
			if (!image.compressToJpeg(mRect, 90, mBOS)) {
				return;
			}
			byte[] tmp = mBOS.toByteArray();
			Bitmap bmp = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
			if (mM == null) {
				mM = new Matrix();
			}
			mM.reset();
			mM.postRotate(rotationInDegrees);
			// 水平翻转
			mM.postScale(-1, 1);
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), mM, true);
			bmp = Bitmap.createScaledBitmap(bmp, mPhotoView.getWidth(),
					mPhotoView.getHeight(), true);
			if (mOrigArray == null) {
				mOrigArray = MeshUtil.getOrigArray(bmp);
			}
			if (mVertsArray == null) {
				mVertsArray = new float[mOrigArray.length];
				mWrapper.applyWarp(mOrigArray, mVertsArray, 4096,
						bmp.getWidth(), bmp.getHeight(), bmp.getWidth() / 2,
						bmp.getHeight() / 2, mWarpStrategy);
			}
			mPhotoView.refresh(bmp, mVertsArray);
		}
	};

	private LinearLayout mNoWarp;
	private ImageView mNoWarpOK;
	private LinearLayout mBulgeWarp;
	private ImageView mBulgeWarpOK;
	private LinearLayout mDentWarp;
	private ImageView mDentWarpOK;
	private LinearLayout mSquareWarp;
	private ImageView mSquareWarpOK;
	private LinearLayout mSwirlWarp;
	private ImageView mSwirlWarpOK;
	private LinearLayout mTallWarp;
	private ImageView mTallWarpOK;
	private LinearLayout mWideWarp;
	private ImageView mWideWarpOK;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Create a RelativeLayout container that will hold a SurfaceView,
		// and set it as the content of our activity.
		setContentView(R.layout.camera_preview);

		mPreview = (Preview) findViewById(R.id.priview);
		mPhotoView = (PhotoView) findViewById(R.id.image);
		// mImage = (ImageView) findViewById(R.id.imageview);

		// 判断SDK大于9
		if (android.os.Build.VERSION.SDK_INT >= 9) {
			// Find the total number of cameras available
			numberOfCameras = Camera.getNumberOfCameras();

			// Find the ID of the default camera
			CameraInfo cameraInfo = new CameraInfo();
			for (int i = 0; i < numberOfCameras; i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
					defaultCameraId = i;
				}
			}
		} else {
			numberOfCameras = 1;
			defaultCameraId = 1;
		}

		mTakePic = (ImageView) findViewById(R.id.takepic);
		mSwitchCam = (ImageView) findViewById(R.id.switch_x);

		mTakePic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 照相
				mCamera.autoFocus(new AutoFocusCallback() {

					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						Bitmap bmp = Bitmap.createBitmap(mPhotoView.getWidth(),
								mPhotoView.getHeight(), Config.ARGB_8888);
						Canvas canvas = new Canvas(bmp);
						mPhotoView.draw(canvas);
						String fileName = "/magiccamera/image/xtmp.jpg";
						if (FileUtil.isSDCardAvaiable()) {
							fileName = Environment
									.getExternalStorageDirectory()
									.getAbsolutePath()
									+ fileName;
						} else {
							fileName = getCacheDir().getAbsolutePath()
									+ fileName;
						}
						boolean b = FileUtil.saveBitmapToSDFile(bmp, fileName,
								CompressFormat.JPEG);
						if (b) {
							Intent intent = new Intent(CameraPreview.this,
									ShowActivity.class);
							intent.putExtra(ShowActivity.IMAGE_KEY, fileName);
							startActivity(intent);
						}
					}
				});
			}
		});

		if (numberOfCameras <= 1) {
			mSwitchCam.setVisibility(View.GONE);
		} else {
			mSwitchCam.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mCamera != null) {
						mCamera.stopPreview();
						mPreview.setCamera(null);
						mCamera.setPreviewCallback(null);
						mCamera.release();
						mCamera = null;
					}

					if (rotationInDegrees == 180) {
						rotationInDegrees = 0;
					} else {
						rotationInDegrees = 180;
					}

					// Acquire the next camera and request Preview to
					// reconfigure
					// parameters.
					mCamera = Camera.open((cameraCurrentlyLocked + 1)
							% numberOfCameras);
					mCamera.setPreviewCallback(mPreviewCallback);
					cameraCurrentlyLocked = (cameraCurrentlyLocked + 1)
							% numberOfCameras;
					mPreview.switchCamera(mCamera);

					// Start the preview
					mCamera.startPreview();
				}
			});
		}

		mSlidingLayer = (SlidingLayer) findViewById(R.id.slidingLayer1);
		mOpen = (Button) findViewById(R.id.buttonOpen);
		mClose = (Button) findViewById(R.id.buttonClose);

		mOpen.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mSlidingLayer.isOpened()) {
					mSlidingLayer.openLayer(true);
				}
			}
		});

		mClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSlidingLayer.isOpened()) {
					mSlidingLayer.closeLayer(true);
				}
			}
		});

		mNoWarp = (LinearLayout) findViewById(R.id.nowarp);
		mNoWarp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences preference = getPreferences(MODE_PRIVATE);
				Editor editor = preference.edit();
				editor.putString(Warper.WRAP_KEY, Warper.NOWARP);
				editor.commit();
				initWarp();
			}
		});
		mNoWarpOK = (ImageView) findViewById(R.id.nowarpok);
		mBulgeWarp = (LinearLayout) findViewById(R.id.bulgewarp);
		mBulgeWarp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences preference = getPreferences(MODE_PRIVATE);
				Editor editor = preference.edit();
				editor.putString(Warper.WRAP_KEY, Warper.BULGEWARP);
				editor.commit();
				initWarp();
			}
		});
		mBulgeWarpOK = (ImageView) findViewById(R.id.bulgewarpok);
		mDentWarp = (LinearLayout) findViewById(R.id.dentwarp);
		mDentWarp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences preference = getPreferences(MODE_PRIVATE);
				Editor editor = preference.edit();
				editor.putString(Warper.WRAP_KEY, Warper.DENTWARP);
				editor.commit();
				initWarp();
			}
		});
		mDentWarpOK = (ImageView) findViewById(R.id.dentwarpok);
		mSquareWarp = (LinearLayout) findViewById(R.id.squarewarp);
		mSquareWarp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences preference = getPreferences(MODE_PRIVATE);
				Editor editor = preference.edit();
				editor.putString(Warper.WRAP_KEY, Warper.SQUAREWARP);
				editor.commit();
				initWarp();
			}
		});
		mSquareWarpOK = (ImageView) findViewById(R.id.squarewarpok);
		mSwirlWarp = (LinearLayout) findViewById(R.id.swirlwarp);
		mSwirlWarp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences preference = getPreferences(MODE_PRIVATE);
				Editor editor = preference.edit();
				editor.putString(Warper.WRAP_KEY, Warper.SWIRLWARP);
				editor.commit();
				initWarp();
			}
		});
		mSwirlWarpOK = (ImageView) findViewById(R.id.swirlwarpok);
		mTallWarp = (LinearLayout) findViewById(R.id.tallwarp);
		mTallWarp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences preference = getPreferences(MODE_PRIVATE);
				Editor editor = preference.edit();
				editor.putString(Warper.WRAP_KEY, Warper.TALLWARP);
				editor.commit();
				initWarp();
			}
		});
		mTallWarpOK = (ImageView) findViewById(R.id.tallwarpok);
		mWideWarp = (LinearLayout) findViewById(R.id.widewarp);
		mWideWarp.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SharedPreferences preference = getPreferences(MODE_PRIVATE);
				Editor editor = preference.edit();
				editor.putString(Warper.WRAP_KEY, Warper.WIDEWARP);
				editor.commit();
				initWarp();
			}
		});
		mWideWarpOK = (ImageView) findViewById(R.id.widewarpok);

		// 初始化warper
		initWarp();
	}

	/**
	 * 根据保存的preferences设置视图和warper
	 */
	private void initWarp() {
		mNoWarpOK.setVisibility(View.INVISIBLE);
		mBulgeWarpOK.setVisibility(View.INVISIBLE);
		mDentWarpOK.setVisibility(View.INVISIBLE);
		mSquareWarpOK.setVisibility(View.INVISIBLE);
		mSwirlWarpOK.setVisibility(View.INVISIBLE);
		mTallWarpOK.setVisibility(View.INVISIBLE);
		mWideWarpOK.setVisibility(View.INVISIBLE);

		mVertsArray = null;

		SharedPreferences preference = getPreferences(MODE_PRIVATE);
		String v = preference.getString(Warper.WRAP_KEY, "");
		if (v.equals(Warper.NOWARP)) {
			mNoWarpOK.setVisibility(View.VISIBLE);
			mWarpStrategy = new NoWarp();
		} else if (v.equals(Warper.BULGEWARP)) {
			mBulgeWarpOK.setVisibility(View.VISIBLE);
			mWarpStrategy = new BulgeWarp();
		} else if (v.equals(Warper.DENTWARP)) {
			mDentWarpOK.setVisibility(View.VISIBLE);
			mWarpStrategy = new DentWarp();
		} else if (v.equals(Warper.SQUAREWARP)) {
			mSquareWarpOK.setVisibility(View.VISIBLE);
			mWarpStrategy = new SquareWarp();
		} else if (v.equals(Warper.SWIRLWARP)) {
			mSwirlWarpOK.setVisibility(View.VISIBLE);
			mWarpStrategy = new SwirlWarp();
		} else if (v.equals(Warper.TALLWARP)) {
			mTallWarpOK.setVisibility(View.VISIBLE);
			mWarpStrategy = new TallWarp();
		} else if (v.equals(Warper.WIDEWARP)) {
			mWideWarpOK.setVisibility(View.VISIBLE);
			mWarpStrategy = new WideWarp();
		} else {
			v = Warper.DENTWARP;
			Editor editor = preference.edit();
			editor.putString(Warper.WRAP_KEY, v);
			editor.commit();
			mDentWarpOK.setVisibility(View.VISIBLE);
			mWarpStrategy = new DentWarp();
		}

		if (mSlidingLayer.isOpened()) {
			mSlidingLayer.closeLayer(true);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mSlidingLayer.isOpened()) {
				mSlidingLayer.closeLayer(true);
				return true;
			}
		default:
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();
		mCamera.setPreviewCallback(mPreviewCallback);
		cameraCurrentlyLocked = defaultCameraId;
		rotationInDegrees = 180;
		mPreview.setCamera(mCamera);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Because the Camera object is a shared resource, it's very
		// important to release it when the activity is paused.
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 杀进程
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}

// ----------------------------------------------------------------------

/**
 * A simple wrapper around a Camera and a SurfaceView that renders a centered
 * preview of the Camera to the surface. We need to center the SurfaceView
 * because not all devices have cameras that support preview sizes at the same
 * aspect ratio as the device's display.
 */
@SuppressLint("NewApi")
class Preview extends ViewGroup implements SurfaceHolder.Callback {

	private final String TAG = "Preview";

	SurfaceView mSurfaceView;
	SurfaceHolder mHolder;
	Size mPreviewSize;
	List<Size> mSupportedPreviewSizes;
	Camera mCamera;

	Preview(Context context) {
		super(context);
		init(context);
	}

	public Preview(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public Preview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mSurfaceView = new SurfaceView(context);
		addView(mSurfaceView);

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setCamera(Camera camera) {
		mCamera = camera;
		if (mCamera != null) {
			mSupportedPreviewSizes = mCamera.getParameters()
					.getSupportedPreviewSizes();
			requestLayout();
		}
	}

	public void switchCamera(Camera camera) {
		setCamera(camera);
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();

		camera.setParameters(parameters);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// We purposely disregard child measurements because act as a
		// wrapper to a SurfaceView that centers the camera preview instead
		// of stretching it.
		final int width = resolveSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		setMeasuredDimension(width, height);

		if (mSupportedPreviewSizes != null) {
			mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width,
					height);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed && getChildCount() > 0) {
			final View child = getChildAt(0);

			final int width = r - l;
			final int height = b - t;

			int previewWidth = width;
			int previewHeight = height;
			if (mPreviewSize != null) {
				previewWidth = mPreviewSize.width;
				previewHeight = mPreviewSize.height;
			}

			// Center the child SurfaceView within the parent.
			if (width * previewHeight > height * previewWidth) {
				final int scaledChildWidth = previewWidth * height
						/ previewHeight;
				child.layout((width - scaledChildWidth) / 2, 0,
						(width + scaledChildWidth) / 2, height);
			} else {
				final int scaledChildHeight = previewHeight * width
						/ previewWidth;
				child.layout(0, (height - scaledChildHeight) / 2, width,
						(height + scaledChildHeight) / 2);
			}
		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		try {
			if (mCamera != null) {
				mCamera.setPreviewDisplay(holder);
			}
		} catch (IOException exception) {
			Log.e(TAG, "IOException caused by setPreviewDisplay()", exception);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		if (mCamera != null) {
			mCamera.stopPreview();
		}
	}

	/**
	 * 获取预览大小
	 */
	private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		// 寻找最贴近view大小一半的预览
		double area = w * h / 3;
		Size ret = null;
		double dif = Double.MAX_VALUE;
		for (Size size : sizes) {
			int xw = size.width;
			int xh = size.height;
			double xdif = Math.abs(xw * xh - area);
			if (xdif < dif) {
				dif = xdif;
				ret = size;
			}
		}
		Log.e("Test", "getOptimalPreviewSize w = " + w + "  h = " + h
				+ "  ret.width = " + ret.width + "  ret.height = " + ret.height);
		return ret;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		if (mCamera == null) {
			return;
		}
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
		requestLayout();

		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

}
