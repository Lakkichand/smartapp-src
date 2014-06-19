package com.jiubang.go.backup.pro.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 壁纸恢复项
 * 
 * @author wencan
 */
public class WallpaperRestoreEntry extends BaseRestoreEntry {

	private Context mContext;

	public WallpaperRestoreEntry(Context context, String recordDir) {
		mContext = context;

		if (recordDir != null && hasWallpaperBackupFile(recordDir)) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
	}

	public static boolean hasWallpaperBackupFile(String dir) {
		if (dir == null) {
			return false;
		}

		dir = Util.ensureFileSeparator(dir);
		return new File(dir, WallpaperBackupEntry.WALLPAPER_FILE_NAME).exists();
	}

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		if (context == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof RestoreArgs)) {
			return false;
		}
		setState(RestoreState.RESTORING);
		listener.onStart(null, null);
		boolean ret = false;
		ret = restoreWallpaper(context, (RestoreArgs) data);
		RestoreState state = ret
				? RestoreState.RESTORE_SUCCESSFUL
				: RestoreState.RESTORE_ERROR_OCCURRED;
		setState(state);
		listener.onEnd(ret, WallpaperRestoreEntry.this, null);
		return true;
	}

	@Override
	public void stopRestore() {
	}

	@Override
	public int getId() {
		return 0;
	}

	@Override
	public EntryType getType() {
		return EntryType.TYPE_SYSTEM_WALLPAPER;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.wallpaper) : "";
	}

	@Override
	public boolean isNeedRootAuthority() {
		return false;
	}

	private boolean restoreWallpaper(Context context, RestoreArgs args) {
		File file = new File(args.mRestorePath, WallpaperBackupEntry.WALLPAPER_FILE_NAME);
		if (!file.exists()) {
			return false;
		}

		try {
			FileInputStream fis = new FileInputStream(file);
			try {
				WallpaperManager.getInstance(context).setStream(fis);
			} finally {
				fis.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;

		//		Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
		//		if (bm == null) {
		//			return false;
		//		}
		//		boolean ret = true;
		//		WallpaperManager wm = WallpaperManager.getInstance(context);
		//		try {
		//			wm.setBitmap(bm);
		//		} catch (Exception e) {
		//			e.printStackTrace();
		//			ret = false;
		//		}
		//		return ret;
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_wallpaper);
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
		return context.getResources().getDrawable(R.drawable.icon_wallpaper);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_WALLPAPER, null);
		File wallpaperFile = new File(recordRootPah, WallpaperBackupEntry.WALLPAPER_FILE_NAME);
		if (wallpaperFile.exists()) {
			wallpaperFile.delete();
		}
	}
}
