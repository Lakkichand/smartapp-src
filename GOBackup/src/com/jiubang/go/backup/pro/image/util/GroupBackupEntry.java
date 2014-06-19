package com.jiubang.go.backup.pro.image.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * @author jiangpeihe
 *组照片entry
 */
public class GroupBackupEntry extends BaseBackupEntry {

	private List<BaseBackupEntry> mEntryList = new ArrayList<BaseBackupEntry>();
	private Context mContext = null;
	private String mName = null;
	private EntryType mType = null;
	private boolean mIsSelected;
	private OnSelectedChangeListener mOnSelectedChangeListener;

	public GroupBackupEntry(Context context, String name, EntryType type) {
		mContext = context;
		mName = name;
		mType = type;
	}
	public GroupBackupEntry(Context context) {
		mContext = context;
	}
	public void addEntry(BaseBackupEntry entry) {
		mEntryList.add(entry);
	}

	public int getCount() {
		return mEntryList.size();
	}

	public int getSelectedCount() {
		int selectCount = 0;
		for (BaseBackupEntry entry : mEntryList) {
			if (entry.isSelected()) {
				selectCount++;
			}
		}
		return selectCount;
	}

	public String getName() {
		return null;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		boolean ret = true;
		for (BaseBackupEntry entry : getEntryList()) {
			ret = entry.backup(ctx, data, listener);
		}
		return ret;
	}

	@Override
	public int getId() {
		return 0;
	}
	@Override
	public EntryType getType() {
		return mType;
	}
	@Override
	public long getSpaceUsage() {
		long spaceUsedSize = 0;
		for (BaseBackupEntry entry : mEntryList) {
			spaceUsedSize += entry.getSpaceUsage();
		}
		return spaceUsedSize;
	}
	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	@Override
	public boolean isSelected() {
		boolean isSelected = true;
		for (BaseBackupEntry entry : mEntryList) {
			if (!entry.isSelected()) {
				isSelected = false;
			}
		}
		return isSelected;
	}

	@Override
	public void setSelected(boolean selected) {
		for (BaseBackupEntry entry : mEntryList) {
			entry.setSelected(selected);
		}
	}

	@Override
	public void setOnSelectedChangeListener(OnSelectedChangeListener l) {
		for (BaseBackupEntry entry : mEntryList) {
			entry.setOnSelectedChangeListener(l);
		}
	}

	public List<BaseBackupEntry> getEntryList() {
		return mEntryList;
	}
	@Override
	public String getDescription() {
		return null;
	}
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return null;
	}
	
}
