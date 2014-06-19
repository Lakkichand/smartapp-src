package com.jiubang.go.backup.pro.data;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 壁纸备份项
 * 
 * @author wencan
 */
public class WallpaperBackupEntry extends BaseBackupEntry {

	public static final String WALLPAPER_FILE_NAME = "wallpaper.pg";

	private Context mContext;

	public WallpaperBackupEntry(Context context) {
		mContext = context;
	}

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof BackupArgs)) {
			return false;
		}
		setState(BackupState.BACKUPING);
		BackupArgs args = (BackupArgs) data;
		listener.onStart(null, null);
		boolean ret = backupInternal(ctx, args);
		if (ret) {
			ret = updateBackupDb(args.mDbHelper);
		}
		BackupState state = ret ? BackupState.BACKUP_SUCCESSFUL : BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);
		listener.onEnd(ret, this, getWallpaperBackupFiles(args.mBackupPath));
		return true;
	}

	private boolean backupInternal(Context context, BackupArgs args) {
		return backupWallpaper(context, args);
	}

	private boolean backupWallpaper(Context context, BackupArgs args) {
		WallpaperManager wm = WallpaperManager.getInstance(context);

		Drawable drawable = wm.getDrawable();
		if (!(drawable instanceof BitmapDrawable)) {
			return false;
		}
		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

		File destFile = new File(args.mBackupPath, WALLPAPER_FILE_NAME);
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
			try {
				bitmap.compress(CompressFormat.PNG, 100, bos);
			} finally {
				bos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (Util.getAndroidSystemVersion() >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				wm.forgetLoadedWallpaper();
			}
		}
		return true;
	}

	private boolean updateBackupDb(BackupDBHelper dbHelper) {
		if (dbHelper == null) {
			return false;
		}

		ContentValues cv = new ContentValues();
		cv.put(DataTable.MIME_TYPE, MimetypeTable.MIMETYPE_VALUE_WALLPAPER);
		cv.put(DataTable.DATA1, WALLPAPER_FILE_NAME);
		cv.put(DataTable.DATA14, new Date().getTime());
		return dbHelper.reflashDatatable(cv);
		// if (dbHelper.update(DataTable.TABLE_NAME, cv, where, null) == 0) {
		// return dbHelper.insert(DataTable.TABLE_NAME, cv);
		// }
		// return true;
	}

	private String[] getWallpaperBackupFiles(String dir) {
		if (dir == null) {
			return null;
		}

		return new String[] { dir + WALLPAPER_FILE_NAME };
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
}
