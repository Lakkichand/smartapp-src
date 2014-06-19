package com.jiubang.go.backup.pro.image.util;

import java.util.List;

import android.content.Context;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;

/**
 * @author jiangpeihe
 *照片entry
 */
public class ImageBackupEntry extends GroupBackupEntry {
	private Context mContext = null;
	private EntryType mType = EntryType.TYPE_USER_IMAGE;
	private boolean mIsSelected;
	public final static String IMAGE_DIR_NAME = "Images";
	
	public ImageBackupEntry(Context context) {
		super(context);
		mContext = context;
		List<String> foldeNameList = ImageOperater.getImageFolderPathListFromLocalDB(mContext);
		addFolderEntries(foldeNameList);

	}
	
	public ImageBackupEntry(Context context, boolean ret) {
		super(context);
		mContext = context;
	}
	
	public void addFolderEntries(List<String> foldeNameList) {
		List<ImageBean> allImageList = ImageOperater.getImageList(mContext);
		for (String foderName : foldeNameList) {
			FolderBackEntry entry = new FolderBackEntry(mContext, foderName, mType);
			List<ImageBean> imageInfo = ImageOperater.getImageMapByParentFileName(foderName,
					allImageList).get(foderName);
			for (ImageBean image : imageInfo) {
				BaseBackupEntry imageEntry = new OneImageBackupEntry(mContext, image);
				if (imageEntry != null) {
					entry.addEntry(imageEntry);
				}
			}
			addEntry(entry);
		}
	}

	@Override
	public int getCount() {
		int count = 0;
		for (BaseBackupEntry entry : getEntryList()) {
			count += entry == null ? 0 : ((FolderBackEntry) entry).getCount();
		}
		return count;
	}

	@Override
	public int getSelectedCount() {
		int selectCount = 0;
		for (BaseBackupEntry entry : getEntryList()) {
			selectCount += ((FolderBackEntry) entry).getSelectedCount();
		}
		return selectCount;
	}

	@Override
	public long getSpaceUsage() {
		long spaceUsedSize = 0;
		for (BaseBackupEntry entry : getEntryList()) {
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
		for (BaseBackupEntry entry : getEntryList()) {
			if (!entry.isSelected()) {
				isSelected = false;
			}
		}
		return isSelected;
	}

	@Override
	public void setSelected(boolean selected) {
		mIsSelected = selected;
		for (BaseBackupEntry entry : getEntryList()) {
			entry.setSelected(mIsSelected);
		}
	}

}
