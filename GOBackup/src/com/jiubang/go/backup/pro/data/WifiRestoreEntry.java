package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.model.BackupDBHelper.DataTable;
import com.jiubang.go.backup.pro.model.BackupDBHelper.MimetypeTable;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.Util;

/**
 * wifi恢复项
 * 
 * @author wencan
 */
public class WifiRestoreEntry extends BaseRestoreEntry {
	private final Context mContext;
	private String mRecordDir;
	private String mWifiPath;

	public WifiRestoreEntry(Context context, String recordDir, String wifiPath) {
		mContext = context;
		mRecordDir = recordDir;
		mWifiPath = wifiPath;

		if (getOrginalFile(mRecordDir) != null && RootShell.isRootValid()) {
			setRestorableState(RestorableState.DATA_RESTORABLE);
		}
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
		listener.onStart(WifiRestoreEntry.this, null);
		if (getRestorableState() != RestorableState.DATA_RESTORABLE) {
			listener.onEnd(false, WifiRestoreEntry.this, null);
			return false;
		}

		boolean ret = false;
		ret = restoreWifiInternal(context, (RestoreArgs) data);
		RestoreState state = ret
				? RestoreState.RESTORE_SUCCESSFUL
				: RestoreState.RESTORE_ERROR_OCCURRED;
		setState(state);
		listener.onEnd(ret, WifiRestoreEntry.this, null);
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
		return EntryType.TYPE_SYSTEM_WIFI;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return mContext != null ? mContext.getString(R.string.wifi_access_points) : "";
	}

	public static File getOrginalFile(String recordDir) {
		if (recordDir == null) {
			return null;
		}
		File wifiFile = new File(recordDir, WifiBackupEntry.WIFI_BACKUP_NAME);
		if (wifiFile.exists()) {
			return wifiFile;
		}
		return null;
	}

	private boolean restoreWifiInternal(Context ctx, RestoreArgs args) {
		if (mWifiPath == null) {
			return false;
		}

		File wifiFile = new File(args.mRestorePath, WifiBackupEntry.WIFI_BACKUP_NAME);
		if (!wifiFile.exists()) {
			return false;
		}

		// 解密
		File tempWifiFile = new File(args.mRestorePath, "wifi.temp");
		if (!Util.decryptFile(wifiFile, tempWifiFile, Constant.getPassword())) {
			if (tempWifiFile != null && tempWifiFile.exists()) {
				tempWifiFile.delete();
			}
			return false;
		}

		// 恢复
		boolean ret = false;
		// LinuxShell ls = new LinuxShell();
		// ret = ls.restoreWifi(ctx.getFilesDir().getAbsolutePath(),
		// tempWifiFile.getAbsolutePath(), wifiPath);
		ret = restoreWifiWithRootProcess(ctx, ctx.getFilesDir().getAbsolutePath(),
				tempWifiFile.getAbsolutePath(), mWifiPath);

		// 刷新wifi
		if (ret) {
			reflashWifi(ctx);
		}

		if (tempWifiFile != null && tempWifiFile.exists()) {
			tempWifiFile.delete();
		}

		return ret;
	}

	private boolean restoreWifiWithRootProcess(Context context, String internalDataDir, String src,
			String desc) {
		boolean ret = false;
		RootShell rootShell = null;
		try {
			rootShell = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		String command = "";
		command += internalDataDir + "backup bw ";
		command += "-r " + desc + " ";
		command += internalDataDir + "busybox" + " ";
		command += src;
		String result = new RootShell.Command(command).execute(rootShell);
		if (result != null && result.contains("GO_SUCCESS")) {
			ret = true;
		}
		return ret;
	}

	private static boolean reflashWifi(Context ctx) {
		WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		if (wm.isWifiEnabled()) {
			wm.setWifiEnabled(false);
			wm.setWifiEnabled(true);
		}
		return true;
	}

	@Override
	public boolean loadIcon(Context context) {
		boolean ret = false;
		if (context != null) {
			mInitingIcon = true;
			Drawable icon = context.getResources().getDrawable(R.drawable.icon_wifi);
			if (icon != null) {
				setIcon(icon);
				ret = true;
			}
			mInitingIcon = false;
		}
		return ret;
	}

	@Override
	public boolean isNeedRootAuthority() {
		return true;
	}

	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return context.getResources().getDrawable(R.drawable.icon_wifi);
	}

	@Override
	public void deleteEntryInformationFromRecord(BackupDBHelper backupDBHelper, BaseEntry entry,
			String recordRootPah) {
		backupDBHelper.delete(DataTable.TABLE_NAME, DataTable.MIME_TYPE + "="
				+ MimetypeTable.MIMETYPE_VALUE_WIFI, null);
		File wifiFile = new File(recordRootPah, WifiBackupEntry.WIFI_BACKUP_NAME);
		if (wifiFile.exists()) {
			wifiFile.delete();
		}
	}
}
