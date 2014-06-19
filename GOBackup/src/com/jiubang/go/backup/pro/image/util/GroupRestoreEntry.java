package com.jiubang.go.backup.pro.image.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * @author jiangpeihe
 *文件夹entry
 */
public class GroupRestoreEntry extends BaseRestoreEntry {

	private List<BaseRestoreEntry> mEntryList = new ArrayList<BaseRestoreEntry>();
	private Context mContext = null;
	private String mName = null;
	private EntryType mType = null;
	private boolean mIsSelected;
	private OnSelectedChangeListener mOnSelectedChangeListener;

	public GroupRestoreEntry(Context context, String name, EntryType type) {
		mContext = context;
		mName = name;
		mType = type;
	}
	public GroupRestoreEntry(Context context) {
		mContext = context;
	}

	public void addEntry(BaseRestoreEntry entry) {
		mEntryList.add(entry);
	}

	public int getCount() {
		return mEntryList.size();
	}

	public int getSelectedCount() {
		int selectCount = 0;
		for (BaseRestoreEntry entry : mEntryList) {
			if (entry.isSelected()) {
				selectCount++;
			}
		}
		return selectCount;
	}

	@Override
	public long getSpaceUsage() {
		long spaceUsedSize = 0;
		for (BaseRestoreEntry entry : mEntryList) {
			spaceUsedSize += entry.getSpaceUsage();
		}
		return spaceUsedSize;
	}

	@Override
	public boolean isSelected() {
		boolean isSelected = true;
		for (BaseRestoreEntry entry : mEntryList) {
			if (!entry.isSelected()) {
				isSelected = false;
			}
		}
		return isSelected;
	}

	@Override
	public void setSelected(boolean selected) {
		for (BaseRestoreEntry entry : mEntryList) {
			entry.setSelected(selected);
		}
	}

	@Override
	public void setOnSelectedChangeListener(OnSelectedChangeListener l) {
		for (BaseRestoreEntry entry : mEntryList) {
			entry.setOnSelectedChangeListener(l);
		}
	}

	public List<BaseRestoreEntry> getEntryList() {
		return mEntryList;
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		return false;
	}

	@Override
	public void stopRestore() {
		for (BaseRestoreEntry entry : mEntryList) {
			entry.stopRestore();
		}
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
	public String getDescription() {
		return null;
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isRestorable() {
		return true;
	}

}
