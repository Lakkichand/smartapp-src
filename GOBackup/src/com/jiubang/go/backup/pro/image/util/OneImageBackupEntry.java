package com.jiubang.go.backup.pro.image.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * @author jiangpeihe
 *一张照片entry
 */
public class OneImageBackupEntry extends BaseBackupEntry {
	private Context mContext = null;
	private BackupArgs mBackupArgs;
	private IAsyncTaskListener mListener;
	private ImageBean mImage;
	private String mImageSdpath = null;
	public OneImageBackupEntry(Context context, ImageBean image, String imageSdpath) {
		super();
		mContext = context;
		mImage = image;
		mImageSdpath = imageSdpath;
	}

	public OneImageBackupEntry(Context context, ImageBean image) {
		super();
		mContext = context;
		mImage = image;
	}

	private String[] getImageBackupFiles() {
		return new String[] { mImage.mImagePath };

	}

	public boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}
		//在数据库中记录已备份图片的路径
		
		Bitmap scaleBitmap = null;
		
		ContentValues cv = new ContentValues();

		try {
			String imagePath = mImage.mImagePath;
			if (imagePath == null) {
				return false;
			}
			File imageFile = new File(imagePath);
			if (!imageFile.exists()) {
				return false;
			}
			
			if (TextUtils.isEmpty(mImageSdpath)) {
				return false;
			}

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imagePath, opts);
			mImage.mImageShape = opts.outWidth + " X " + opts.outHeight;
			cv.put(DataTable.DATA6, mImage.mImageShape);
			
			scaleBitmap = ImageOperater.getScaleBitmap(imagePath);
			if (scaleBitmap != null) {
				int thumbialWidth = scaleBitmap.getWidth();
				int thumbailHeight = scaleBitmap.getHeight();
				String thumbailShape = thumbialWidth + " X " + thumbailHeight;
				cv.put(DataTable.DATA8, thumbailShape);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
					scaleBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
				} finally {
					scaleBitmap.recycle();
				}
				try {
					final byte[] imageData = baos.toByteArray();
					if (imageData != null && imageData.length > 0) {
						cv.put(DataTable.DATA3, imageData);
						cv.put(DataTable.DATA4, imageData.length);
					}
				} finally {
					try {
						baos.close();
					} catch (IOException e) { }
				}
			}

			String imageRelativePath = imagePath.replace(mImageSdpath, File.separator);
			String imageRelativeFilePath = mImage.mImageParentFilePath.replace(mImageSdpath,
					File.separator);
			
			cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_IMAGE);
			cv.put(DataTable.DATA1, mImage.mImageDisplayName);
			cv.put(DataTable.DATA2, mImage.mImageSize);
			cv.put(DataTable.DATA5, imageRelativeFilePath);
			cv.put(DataTable.DATA7, imageRelativePath);
			
			dbHelper.reflashDatatable(cv);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof BackupArgs)) {
			return false;
		}
		mBackupArgs = (BackupArgs) data;
		mListener = listener;
		setState(BackupState.BACKUPING);
		mListener.onStart(null, null);
		boolean ret = updateBackupDb(mBackupArgs.mDbHelper);
		if (mListener != null) {
			mListener.onEnd(ret, this, getImageBackupFiles());
		}
		return true;
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_IMAGE;
	}

	@Override
	public long getSpaceUsage() {
		return mImage == null ? 0 : mImage.mImageSize;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mImage.mImageDisplayName : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	public ImageBean getImage() {
		return mImage;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_image);
	}

}
