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

package com.go.launcher.cropimage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImageActivity extends MonitoredActivity {
	private static final String TAG = "CropImageActivity";

	// These are various options can be specified in the intent.
	private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG; // only
																				// used
																				// with
																				// mSaveUri
	private Uri mSaveUri = null;
	private boolean mSetWallpaper = false;
	private int mAspectX, mAspectY;
	// private boolean mDoFaceDetection = true;
	private boolean mCircleCrop = false;
	private final Handler mHandler = new Handler();

	// These options specifiy the output image size and whether we should
	// scale the output to fit it (or just crop it).
	private int mOutputX, mOutputY;
	private boolean mScale;
	private boolean mScaleUp = true;

	boolean mWaitingToPick; // Whether we are wait the user to pick a face.
	boolean mSaving; // Whether the "save" button is already clicked.

	private CropImageView mImageView;
	private ContentResolver mContentResolver;

	private Bitmap mBitmap;
	HighlightView mCrop;
	private boolean mHasApha = true;

	// private IImageList mAllImages;
	// private IImage mImage;

	private Drawable mResizeDrawableWidth;
	private Drawable mResizeDrawableHeight;
	private Drawable mResizeDrawableDiagonal;

	private boolean mNeedCrop = true;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		mContentResolver = getContentResolver();

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// MenuHelper.showStorageToast(this);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (extras != null) {
			// if (extras.getString("circleCrop") != null) {
			// mCircleCrop = true;
			// mAspectX = 1;
			// mAspectY = 1;
			// }
			mSaveUri = (Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT);
			if (mSaveUri != null) {
				String outputFormatString = extras.getString("outputFormat");
				if (outputFormatString != null) {
					mOutputFormat = Bitmap.CompressFormat.valueOf(outputFormatString);
				}
			} else {
				mSetWallpaper = extras.getBoolean("setWallpaper");
			}
			mBitmap = (Bitmap) extras.getParcelable("data");
			mAspectX = extras.getInt("aspectX");
			mAspectY = extras.getInt("aspectY");
			mOutputX = extras.getInt("outputX");
			mOutputY = extras.getInt("outputY");
			mScale = extras.getBoolean("scale", false);
			mScaleUp = extras.getBoolean("scaleUpIfNeeded", false);
			// mDoFaceDetection = extras.containsKey("noFaceDetection")
			// ? !extras.getBoolean("noFaceDetection")
			// : true;

			android.content.res.Resources resources = getResources();
			int id = extras.getInt("arrowHorizontal", -1);
			if (id != -1) {
				mResizeDrawableWidth = resources.getDrawable(id);
			}
			id = extras.getInt("arrowVertical", -1);
			if (id != -1) {
				mResizeDrawableHeight = resources.getDrawable(id);
			}
			// id = extras.getInt("arrowDiagonal", -1);
			// if(id != -1){
			// mResizeDrawableDiagonal = resources.getDrawable(id);
			// }
		}

		if (mBitmap == null) {
			Uri target = intent.getData();
			try {
				decodeBitmapStreamSafe(target);
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			// TODO
			// if (mImage != null) {
			// // Don't read in really large bitmaps. Use the (big) thumbnail
			// // instead.
			// // TODO when saving the resulting bitmap use the
			// // decode/crop/encode api so we don't lose any resolution.
			// mBitmap = mImage.thumbBitmap(IImage.ROTATE_AS_NEEDED);
			// }
		}

		if (mBitmap == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		// Make UI fullscreen.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.cropimage);

		mImageView = (CropImageView) findViewById(R.id.image);
		findViewById(R.id.discard).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		findViewById(R.id.crop).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSaveClicked();
			}
		});

		final Button btn = (Button) findViewById(R.id.no_crop);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// if (mNeedCrop)
				// {
				// mImageView.setImageBitmapResetBase(mBitmap, true);
				// mImageView.remove(mCrop);
				// mCrop = null;
				//
				// btn.setText(R.string.crop_text);
				// }
				// else
				// {
				// makeDefault();
				// mImageView.requestLayout();
				// mCrop = mImageView.mHighlightViews.get(0);
				// mCrop.setFocus(true);
				//
				// btn.setText(R.string.no_crop_text);
				// }
				// mNeedCrop = !mNeedCrop;
				mNeedCrop = false;
				onSaveClicked();
			}
		});

		// startFaceDetection();
		mImageView.setImageBitmapResetBase(mBitmap, true);
		makeDefault();
		mCrop = mImageView.mHighlightViews.get(0);
		mCrop.setFocus(true);
	}

	private boolean decodeBitmapStreamSafe(Uri target) {
		String path = uriToFilePath(target, this);
        int degree = 0;
        if (path != null) {
            degree = readBitmapDegree(path);
        }
		
		InputStream is = null;

		boolean bool = true;
		int scale = 1;
		Options opt = new Options();
		while (bool) {
			try {
				is = mContentResolver.openInputStream(target);
				opt.inSampleSize = scale;
				mBitmap = null;
				mBitmap = BitmapFactory.decodeStream(is, null, opt);
				mBitmap = postRotateToBitmap(degree, mBitmap);
				mHasApha = mBitmap.hasAlpha();
				bool = false;

				return true;
			} catch (OutOfMemoryError e) {
				// 如果解碼大圖片，出现爆内存，则每次缩放一半
				OutOfMemoryHandler.handle();
				scale *= 2;
				if (scale > (1 << 10)) {
					// 防止异常死循环
					return false;
				}
			} catch (Throwable e) {
				// TODO: handle Throwable
				bool = false;

				return false;
			}
		}
		return false;
	}

	// private void startFaceDetection() {
	// if (isFinishing()) {
	// return;
	// }
	//
	// mImageView.setImageBitmapResetBase(mBitmap, true);

	// Util.startBackgroundJob(this, null,
	// getResources().getString(R.string.runningFaceDetection),
	// new Runnable() {
	// public void run() {
	// final CountDownLatch latch = new CountDownLatch(1);
	// final Bitmap b = (mImage != null)
	// ? mImage.fullSizeBitmap(IImage.UNCONSTRAINED,
	// 1024 * 1024)
	// : mBitmap;
	// mHandler.post(new Runnable() {
	// public void run() {
	// if (b != mBitmap && b != null) {
	// mImageView.setImageBitmapResetBase(b, true);
	// mBitmap.recycle();
	// mBitmap = b;
	// }
	// if (mImageView.getScale() == 1F) {
	// mImageView.center(true, true);
	// }
	// latch.countDown();
	// }
	// });
	// try {
	// latch.await();
	// } catch (InterruptedException e) {
	// throw new RuntimeException(e);
	// }
	// mRunFaceDetection.run();
	// }
	// }, mHandler);
	// }

	private Bitmap createNoCropBitmap() {
		Bitmap bitmap = mBitmap;
		try {
			if (null != bitmap) {
				int bmpW = bitmap.getWidth();
				int bmpH = bitmap.getHeight();
				float wScale = (float) mOutputX / (float) bmpW;
				float hScale = (float) mOutputY / (float) bmpH;
				float scale = wScale < hScale ? wScale : hScale;
				boolean needScale = scale < 1;
				boolean needYFit = false;
				if (scale < 1) {
					needYFit = (int) (bmpH * scale) < mOutputY - 1; // 由于缩放误差1个单位
				} else {
					needYFit = bmpH < mOutputY;
				}
				boolean needXFit = false; // for dock
				if (scale < 1) {
					needXFit = (int) (bmpW * scale) < mOutputX - 1; // 由于缩放误差1个单位
				} else {
					needXFit = bmpW < mOutputX;
				}
				if (needScale) {
					Bitmap tempbitmap = BitmapUtility.createScaledBitmap(bitmap,
							(int) (bmpW * scale), (int) (bmpH * scale));
					bitmap.recycle();
					bitmap = tempbitmap;
				}
				if (needYFit || needXFit) {
					Bitmap tempbitmap = BitmapUtility.createBitmap(bitmap, mOutputX, mOutputY);
					bitmap.recycle();
					bitmap = tempbitmap;
				}
			}
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
			bitmap = null;
		} catch (Exception e) {
			e.printStackTrace();
			bitmap = null;
		}
		return bitmap;
	}

	private void onSaveClicked() {
		if (!mNeedCrop) {
			Bitmap nocropBitmap = createNoCropBitmap();
			mImageView.clear();
			if (nocropBitmap != mBitmap) {
				mBitmap.recycle();
				mBitmap = null;
			}
			if (null != nocropBitmap) {
				// Return the cropped image directly or save it to the specified
				// URI.
				Bundle myExtras = getIntent().getExtras();
				if (myExtras != null
						&& (myExtras.getParcelable("data") != null || myExtras
								.getBoolean("return-data"))) {
					Bundle extras = new Bundle();
					extras.putParcelable("data", nocropBitmap);
					setResult(RESULT_OK, (new Intent()).setAction(ICustomAction.ACTION_INLINE_DATA).putExtras(extras));
					finish();
				} else {
					final Bitmap b = nocropBitmap;
					final int msdId = mSetWallpaper ? R.string.wallpaper : R.string.savingImage;
					Util.startBackgroundJob(this, null, getResources().getString(msdId),
							new Runnable() {
								@Override
								public void run() {
									saveOutput(b);
								}
							}, mHandler);
				}
			} else {
				setResult(RESULT_CANCELED);
				finish();
			}
			return;
		}
		// TODO this code needs to change to use the decode/crop/encode single
		// step api so that we don't require that the whole (possibly large)
		// bitmap doesn't have to be read into memory
		if (mCrop == null) {
			return;
		}

		if (mSaving) {
			return;
		}
		mSaving = true;

		Bitmap croppedImage = null;
		Bitmap.Config bmpConfig = mHasApha ? Bitmap.Config.ARGB_8888 : Bitmap.Config.ARGB_8888;

		// If the output is required to a specific size, create an new image
		// with the cropped image in the center and the extra space filled.
		if (mOutputX != 0 && mOutputY != 0 && !mScale) {
			// Don't scale the image but instead fill it so it's the
			// required dimension
			try {
				croppedImage = Bitmap.createBitmap(mOutputX, mOutputY, bmpConfig);
			} catch (OutOfMemoryError e) {
				// 创建失败
				e.printStackTrace();
				OutOfMemoryHandler.handle();
				Toast.makeText(this, getString(R.string.err_out_of_memory), Toast.LENGTH_SHORT);
				// Release bitmap memory as soon as possible
				mImageView.clear();
				mBitmap.recycle();
				mBitmap = null;

				setResult(RESULT_CANCELED);
				finish();
				return;
			}

			Canvas canvas = new Canvas(croppedImage);
			Rect srcRect = mCrop.getCropRect();
			Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);

			int dx = (srcRect.width() - dstRect.width()) / 2;
			int dy = (srcRect.height() - dstRect.height()) / 2;

			// If the srcRect is too big, use the center part of it.
			srcRect.inset(Math.max(0, dx), Math.max(0, dy));

			// If the dstRect is too big, use the center part of it.
			dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

			// Draw the cropped bitmap in the center
			canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

			// Release bitmap memory as soon as possible
			mImageView.clear();
			mBitmap.recycle();
		} else {
			Rect r = mCrop.getCropRect();

			// int width = r.width();
			// int height = r.height();

			// If we are circle cropping, we want alpha channel, which is the
			// third param here.
			try {
				croppedImage = Bitmap.createBitmap(mOutputX, mOutputY, bmpConfig);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				OutOfMemoryHandler.handle();
				// 创建失败
				// Toast.makeText(this, getString(R.string.err_out_of_memory),
				// Toast.LENGTH_SHORT).show();
				// Release bitmap memory as soon as possible
				mImageView.clear();
				mBitmap.recycle();

				setResult(RESULT_CANCELED);
				finish();
				return;
			}

			Canvas canvas = new Canvas(croppedImage);
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
					| Paint.FILTER_BITMAP_FLAG)); // 在有缩放的情况下需要提高绘图质量
			Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);
			canvas.drawBitmap(mBitmap, r, dstRect, null);

			// Release bitmap memory as soon as possible
			mImageView.clear();
			mBitmap.recycle();

			// if (mCircleCrop) {
			// // OK, so what's all this about?
			// // Bitmaps are inherently rectangular but we want to return
			// // something that's basically a circle. So we fill in the
			// // area around the circle with alpha. Note the all important
			// // PortDuff.Mode.CLEAR.
			// Canvas c = new Canvas(croppedImage);
			// Path p = new Path();
			// p.addCircle(width / 2F, height / 2F, width / 2F,
			// Path.Direction.CW);
			// c.clipPath(p, Region.Op.DIFFERENCE);
			// c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
			// }

			// If the required dimension is specified, scale the image.
			// if (mOutputX != 0 && mOutputY != 0 && mScale) {
			// croppedImage = Util.transform(new Matrix(), croppedImage,
			// mOutputX, mOutputY, mScaleUp, Util.RECYCLE_INPUT);
			// }
		}

		mImageView.setImageBitmapResetBase(croppedImage, true);
		mImageView.center(true, true);
		mImageView.mHighlightViews.clear();

		// Return the cropped image directly or save it to the specified URI.
		Bundle myExtras = getIntent().getExtras();
		if (myExtras != null
				&& (myExtras.getParcelable("data") != null || myExtras.getBoolean("return-data"))) {
			Bundle extras = new Bundle();
			extras.putParcelable("data", croppedImage);
			setResult(RESULT_OK, (new Intent()).setAction(ICustomAction.ACTION_INLINE_DATA).putExtras(extras));
			finish();
		} else {
			final Bitmap b = croppedImage;
			final int msdId = mSetWallpaper ? R.string.wallpaper : R.string.savingImage;
			Util.startBackgroundJob(this, null, getResources().getString(msdId), new Runnable() {
				@Override
				public void run() {
					saveOutput(b);
				}
			}, mHandler);
		}
	}

	/**
	 * 确保文件存在，不存在则创建一个空文件
	 * 
	 * @param uri
	 */
	private void makeSureFileExist(Uri uri) {
		String path = uri.getPath();
		if (path != null) {
			File file = new File(path);
			if (!file.exists()) {
				try {
					File parent = file.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					file.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void saveOutput(Bitmap croppedImage) {
		if (mSaveUri != null) {
			makeSureFileExist(mSaveUri);
			OutputStream outputStream = null;
			try {
				outputStream = mContentResolver.openOutputStream(mSaveUri);
				if (outputStream != null) {
					croppedImage.compress(mOutputFormat, 75, outputStream);
				}
			} catch (IOException ex) {
				// TODO: report error to caller
				Log.e(TAG, "Cannot open file: " + mSaveUri, ex);
			} catch (IllegalStateException e) {
				Log.e(TAG, "IllegalStateException Error");
			} finally {
				Util.closeSilently(outputStream);
			}

			Bundle extras = new Bundle();
			// 将保存的路径再次返回给调用者
			setResult(RESULT_OK, new Intent(mSaveUri.toString()).putExtras(extras));
		} else if (mSetWallpaper) {
			try {
				WallpaperManager.getInstance(this).setBitmap(croppedImage);
				setResult(RESULT_OK);
			} catch (IOException e) {
				Log.e(TAG, "Failed to set wallpaper.", e);
				setResult(RESULT_CANCELED);
			}
		}

		final Bitmap b = croppedImage;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				mImageView.clear();
				b.recycle();
			}
		});

		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (null != mImageView) {
			mImageView.clear();
		}
		if (null != mBitmap) {
			if (!mBitmap.isRecycled()) {
				mBitmap.recycle();
			}
			mBitmap = null;
		}
	}

	// @Override
	// protected void onDestroy() {
	// if (mAllImages != null) {
	// mAllImages.close();
	// }
	// super.onDestroy();
	// }

	// Runnable mRunFaceDetection = new Runnable() {
	// @SuppressWarnings("hiding")
	// float mScale = 1F;
	// Matrix mImageMatrix;
	// FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
	// int mNumFaces
	//
	// // For each face, we create a HightlightView for it.
	// private void handleFace(FaceDetector.Face f) {
	// PointF midPoint = new PointF();
	//
	// int r = ((int) (f.eyesDistance() * mScale)) * 2;
	// f.getMidPoint(midPoint);
	// midPoint.x *= mScale;
	// midPoint.y *= mScale;
	//
	// int midX = (int) midPoint.x;
	// int midY = (int) midPoint.y;
	//
	// HighlightView hv = new HighlightView(mImageView);
	//
	// int width = mBitmap.getWidth();
	// int height = mBitmap.getHeight();
	//
	// Rect imageRect = new Rect(0, 0, width, height);
	//
	// RectF faceRect = new RectF(midX, midY, midX, midY);
	// faceRect.inset(-r, -r);
	// if (faceRect.left < 0) {
	// faceRect.inset(-faceRect.left, -faceRect.left);
	// }
	//
	// if (faceRect.top < 0) {
	// faceRect.inset(-faceRect.top, -faceRect.top);
	// }
	//
	// if (faceRect.right > imageRect.right) {
	// faceRect.inset(faceRect.right - imageRect.right,
	// faceRect.right - imageRect.right);
	// }
	//
	// if (faceRect.bottom > imageRect.bottom) {
	// faceRect.inset(faceRect.bottom - imageRect.bottom,
	// faceRect.bottom - imageRect.bottom);
	// }
	//
	// hv.setup(mImageMatrix, imageRect, faceRect, mCircleCrop,
	// mAspectX != 0 && mAspectY != 0);
	//
	// mImageView.add(hv);
	// }
	//
	// // Create a default HightlightView if we found no face in the picture.
	// private void makeDefault() {
	// HighlightView hv = new HighlightView(mImageView);
	//
	// int width = mBitmap.getWidth();
	// int height = mBitmap.getHeight();
	//
	// Rect imageRect = new Rect(0, 0, width, height);
	//
	// // make the default size about 4/5 of the width or height
	// int cropWidth = Math.min(width, height) * 4 / 5;
	// int cropHeight = cropWidth;
	//
	// if (mAspectX != 0 && mAspectY != 0) {
	// if (mAspectX > mAspectY) {
	// cropHeight = cropWidth * mAspectY / mAspectX;
	// } else {
	// cropWidth = cropHeight * mAspectX / mAspectY;
	// }
	// }
	//
	// int x = (width - cropWidth) / 2;
	// int y = (height - cropHeight) / 2;
	//
	// RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
	// hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop,
	// mAspectX != 0 && mAspectY != 0);
	// mImageView.add(hv);
	// }
	//
	// // Scale the image down for faster face detection.
	// private Bitmap prepareBitmap() {
	// if (mBitmap == null) {
	// return null;
	// }
	//
	// // 256 pixels wide is enough.
	// if (mBitmap.getWidth() > 256) {
	// mScale = 256.0F / mBitmap.getWidth();
	// }
	// Matrix matrix = new Matrix();
	// matrix.setScale(mScale, mScale);
	// Bitmap faceBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap
	// .getWidth(), mBitmap.getHeight(), matrix, true);
	// return faceBitmap;
	// }
	//
	// public void run() {
	// mImageMatrix = mImageView.getImageMatrix();
	// Bitmap faceBitmap = prepareBitmap();
	//
	// mScale = 1.0F / mScale;
	// if (faceBitmap != null && mDoFaceDetection) {
	// FaceDetector detector = new FaceDetector(faceBitmap.getWidth(),
	// faceBitmap.getHeight(), mFaces.length);
	// mNumFaces = detector.findFaces(faceBitmap, mFaces);
	// }
	//
	// if (faceBitmap != null && faceBitmap != mBitmap) {
	// faceBitmap.recycle();
	// }
	//
	// mHandler.post(new Runnable() {
	// public void run() {
	// mWaitingToPick = mNumFaces > 1;
	// if (mNumFaces > 0) {
	// for (int i = 0; i < mNumFaces; i++) {
	// handleFace(mFaces[i]);
	// }
	// } else {
	// makeDefault();
	// }
	// mImageView.invalidate();
	// if (mImageView.mHighlightViews.size() == 1) {
	// mCrop = mImageView.mHighlightViews.get(0);
	// mCrop.setFocus(true);
	// }
	//
	// if (mNumFaces > 1) {
	// Toast t = Toast.makeText(CropImageActivity.this,
	// R.string.multiface_crop_help,
	// Toast.LENGTH_SHORT);
	// t.show();
	// }
	// }
	// });
	// }
	// };

	private void makeDefault() {
		HighlightView hv = new HighlightView(mImageView);

		int width = 0;
		int height = 0;
		if (mBitmap != null) {
			width = mBitmap.getWidth();
			height = mBitmap.getHeight();
		}

		Rect imageRect = new Rect(0, 0, width, height);

		// make the default size about 4/5 of the width or height
		int cropWidth = Math.min(width, height) * 4 / 5;
		int cropHeight = cropWidth;

		if (mAspectX != 0 && mAspectY != 0) {
			if (mAspectX > mAspectY) {
				cropHeight = cropWidth * mAspectY / mAspectX;
			} else {
				cropWidth = cropHeight * mAspectX / mAspectY;
			}
		}

		int x = (width - cropWidth) / 2;
		int y = (height - cropHeight) / 2;

		RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
		hv.setup(null, imageRect, cropRect, mCircleCrop, mAspectX != 0 && mAspectY != 0);
		mImageView.add(hv);
		hv.setResizeDrawable(mResizeDrawableWidth, mResizeDrawableHeight, mResizeDrawableDiagonal);
	}
	
	/**
     * <br>注意:可能为null
     * @param uri
     * @param activity
     * @return
     */
    public String uriToFilePath(Uri uri, Context context) {
        String path  = null;
        if (context == null || uri == null) {
            return path;
        }
        String[] proj = { MediaStore.Images.Media.DATA };
        try {
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                path = cursor.getString(index);
            }
        } catch (Exception e) {
            Log.i(TAG, "Exception" + e);
        }
        return path;
    }
    
    /**
     * 根据角度现转位图
     * @param degree
     * @param bitmap
     * @return
     */
    public Bitmap postRotateToBitmap(int degree, Bitmap bitmap) {
        if (degree != 0 && bitmap != null) {
            try {
                Matrix matrix = new Matrix();
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                matrix.postRotate(degree, width >> 1, height >> 1);
                Bitmap des = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
                return des;
            } catch (OutOfMemoryError e) {
                Log.i(TAG, "OutOfMemoryError" + e);
                return bitmap;
            } catch (Exception e) {
                Log.i(TAG, "Exception" + e);
                return bitmap;
            }
        }
        return bitmap;
    }
    
    /**
     * 读取Bitmap角度
     * @param filePath
     * @return
     */
    private int readBitmapDegree(String filePath) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90 :
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180 :
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270 :
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            Log.i(TAG, "IOException" + e);
        }
        return degree;
    }
}
