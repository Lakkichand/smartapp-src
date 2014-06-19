package com.jiubang.go.backup.pro.image.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * @author jiangpeihe
 *一张照片备份
 */
public class OneImageRestoreEntry extends BaseRestoreEntry {
	private Context mContext = null;
	private ImageBean mImage;
	private IAsyncTaskListener mListener = null;
	private boolean mIsCancel = false;

	public OneImageRestoreEntry(Context context, ImageBean image, String recordDir) {
		super();
		mContext = context;
		mImage = image;
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		boolean result = true;
		if (context == null || data == null || listener == null) {
			result = false;
			return result;
		}
		if (!(data instanceof RestoreArgs)) {
			return false;
		}
		setState(RestoreState.RESTORING);
		mListener = listener;
		mListener.onStart(OneImageRestoreEntry.this, null);
		result = new ImageRestore().restoreImage(mContext, mImage);
		finish(result);
		return true;
	}

	@Override
	public void stopRestore() {
		mIsCancel = true;
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

	private void finish(boolean result) {
		if (mIsCancel) {
			setState(RestoreState.RESTORE_CANCELED);
		} else {
			RestoreState state = result
					? RestoreState.RESTORE_SUCCESSFUL
					: RestoreState.RESTORE_ERROR_OCCURRED;
			setState(state);
		}

		if (mListener != null) {
			if (mIsCancel) {
				result = false;
			}
			mListener.onEnd(result, OneImageRestoreEntry.this, null);
		}
		mIsCancel = false;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_image);
	}

}
