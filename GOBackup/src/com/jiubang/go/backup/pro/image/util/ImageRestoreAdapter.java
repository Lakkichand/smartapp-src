package com.jiubang.go.backup.pro.image.util;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.ListView;

import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;

/**
 * @author jiangpeihe
 *照片恢复展示的adpter
 */
public class ImageRestoreAdapter extends CommonImageAdapter {
	List<BaseRestoreEntry> mEntryList;
	private BackupDBHelper mBackupDBHelper = null;

	public ImageRestoreAdapter(Context context, BaseEntry entry, CheckBox checkBox,
			ListView listView, BackupDBHelper backupDBHelper, Bitmap bitmap) {
		super(context, entry, checkBox, listView, bitmap);
		if (entry instanceof FolderRestoreEntry) {
			mEntryList = ((FolderRestoreEntry) entry).getEntryList();
		}
		mBackupDBHelper = backupDBHelper;
	}

	@Override
	public int getCount() {
		return mEntryList != null ? mEntryList.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return mEntryList.get(position);
	}

	@Override
	public Bitmap getThumbnailBitmap(String imagePath) {
		return ImageOperater.getThumbnailBitmap(mBackupDBHelper, imagePath);

	}

	@Override
	public ImageBean getImage(int position) {
		return ((OneImageRestoreEntry) getItem(position)).getImage();
	}

	@Override
	public BaseEntry getEntry(int posititon) {
		return (BaseEntry) getItem(posititon);
	}

}