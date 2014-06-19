package com.jiubang.go.backup.pro.image.util;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.CheckBox;
import android.widget.ListView;

import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
/**
 * @author jiangpeihe
 *照片备份展示的adapter
 */
public class ImageBackupAdapter extends CommonImageAdapter {
	List<BaseBackupEntry> mEntryList;
	public ImageBackupAdapter(Context context, BaseEntry entry, CheckBox checkBox, ListView listview,
			Bitmap bitmap) {
		super(context, entry, checkBox, listview, bitmap);
		if (entry instanceof FolderBackEntry) {
			mEntryList = ((FolderBackEntry) entry).getEntryList();
		}
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
		return ImageOperater.getScaleBitmap(imagePath);

	}

	@Override
	public ImageBean getImage(int position) {
		return ((OneImageBackupEntry) getItem(position)).getImage();
	}

	@Override
	public BaseEntry getEntry(int posititon) {
		return (BaseEntry) getItem(posititon);
	}

}
