package com.jiubang.go.backup.pro.image.util;

import java.util.List;

import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;

/**
 * @author jiangpeihe
 *照片恢复entry
 */
/**
 * @author jiangpeihe
 *照片文件夹entry
 */
public class ImageRestoreEntry extends GroupRestoreEntry {
	private Context mContext = null;
	private EntryType mType = EntryType.TYPE_USER_IMAGE;
	private boolean mIsSelected;
	private String mRecordDir;
	private BackupDBHelper mBackupDBHelper;
	public ImageRestoreEntry(Context context, boolean ret) {
		super(context);
		mContext = context;
	}
	public ImageRestoreEntry(Context context, BackupDBHelper dbHelper, String recordRootDir) {
		super(context);
		mContext = context;
		mRecordDir = recordRootDir;
		mBackupDBHelper = dbHelper;
		List<String> foldeNameList = ImageOperater.getImageFolderPathListFromBackupTable(dbHelper);
		addFolderEntries(foldeNameList);
	}

	public void addFolderEntries(List<String> foldeNameList) {
		List<ImageBean> allImageList = ImageOperater.queryAllImageinfo(mBackupDBHelper);
		for (String foderName : foldeNameList) {
			FolderRestoreEntry entry = new FolderRestoreEntry(mContext, foderName, mType);
			List<ImageBean> imageInfo = ImageOperater.getImageMapByParentFileName(foderName,
					allImageList).get(foderName);
			for (ImageBean image : imageInfo) {
				BaseRestoreEntry imageEntry = new OneImageRestoreEntry(mContext, image, mRecordDir);
				if (imageEntry != null) {
					entry.addEntry(imageEntry);
				}
			}
			addEntry(entry);
		}
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		boolean ret = true;
		for (BaseRestoreEntry entry : getEntryList()) {
			ret = entry.restore(context, data, listener);
		}
		return ret;
	}

	@Override
	public int getCount() {
		int count = 0;
		for (BaseRestoreEntry entry : getEntryList()) {
			count += entry == null ? 0 : ((FolderRestoreEntry) entry).getCount();
		}
		return count;
	}

	@Override
	public int getSelectedCount() {
		int selectCount = 0;
		for (BaseRestoreEntry entry : getEntryList()) {
			selectCount += ((FolderRestoreEntry) entry).getSelectedCount();
		}
		return selectCount;
	}

	@Override
	public long getSpaceUsage() {
		long spaceUsedSize = 0;
		for (BaseRestoreEntry entry : getEntryList()) {
			spaceUsedSize += entry.getSpaceUsage();
		}
		return spaceUsedSize;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_USER_IMAGE;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.image) : "";
	}

	@Override
	public boolean isSelected() {
		boolean isSelected = true;
		for (BaseRestoreEntry entry : getEntryList()) {
			if (!entry.isSelected()) {
				isSelected = false;
			}
		}
		return isSelected;
	}

	@Override
	public void setSelected(boolean selected) {
		mIsSelected = selected;
		for (BaseRestoreEntry entry : getEntryList()) {
			entry.setSelected(mIsSelected);
		}
	}

}
