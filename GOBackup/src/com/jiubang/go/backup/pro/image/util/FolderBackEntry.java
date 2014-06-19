package com.jiubang.go.backup.pro.image.util;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * @author jiangpeihe
 *文件夹entry
 */
public class FolderBackEntry extends GroupBackupEntry {
	private Context mContext = null;
	private String mName = null;
	private EntryType mType = null;
	private boolean mIsSelected;

	public FolderBackEntry(Context context, String name, EntryType type) {
		super(context, name, type);
		mContext = context;
		mName = name;
		mType = type;
	}
	public FolderBackEntry(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		for (BaseBackupEntry entry : getEntryList()) {
			if (entry.isSelected()) {
				((OneImageBackupEntry) entry).backup(ctx, data, listener);
			}
		}
		return true;
	}
	@Override
	public String getDescription() {
		return mName == null ? null : mName.substring(mName.lastIndexOf(File.separator) + 1);
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.dic);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		Drawable icon = context.getResources().getDrawable(R.drawable.dic);
		return icon;
	}

}
