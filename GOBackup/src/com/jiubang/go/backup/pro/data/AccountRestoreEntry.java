package com.jiubang.go.backup.pro.data;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.Util;

/**
 * AccountRestoreEntry
 * 
 * @author WenCan
 */
public class AccountRestoreEntry extends BaseRestoreEntry {

	@Override
	public boolean restore(Context context, Object data, IAsyncTaskListener listener) {
		if (context == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof RestoreArgs)) {
			return false;
		}

		listener.onStart(null, null);
		boolean ret = restoreAccountInternal(context, (RestoreArgs) data);
		RestoreState state = ret
				? RestoreState.RESTORE_SUCCESSFUL
				: RestoreState.RESTORE_ERROR_OCCURRED;
		setState(state);
		listener.onEnd(ret, null, null);
		return false;
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
		return EntryType.TYPE_SYSTEM_ACCOUNT;
	}

	@Override
	public long getSpaceUsage() {
		return 0;
	}

	@Override
	public String getDescription() {
		return "Account";
	}

	private boolean restoreAccountInternal(Context context, RestoreArgs args) {
		/*
		 * final String rootDir = Util.EnsureFileSeparator(args.mRestorePath);
		 * final BackupPropertiesConfig bpc = args.mConfig; String accountDbPath
		 * =
		 * String.valueOf(bpc.get(BackupPropertiesConfig.P_BACKUP_ACCOUNT_PATH)
		 * ); //TODO加上rom版本的判断 File accountFile = new File(rootDir,
		 * AccountBackupEntry.ACCOUNT_FILE_NAME); if(!accountFile.exists()){
		 * //备份文件不存在 return false; } //解密 File tempAccountFile = new
		 * File(rootDir, "account.temp"); if(!Util.DecryptFile(accountFile,
		 * tempAccountFile, Constant.getPassword())){ //解密失败 if(tempAccountFile
		 * != null && tempAccountFile.exists()){ tempAccountFile.delete();
		 * return false; } } boolean ret = false; // LinuxShell ls = new
		 * LinuxShell(); // ret =
		 * ls.restoreAccount(context.getFilesDir().getAbsolutePath(),
		 * tempAccountFile.getAbsolutePath(), accountDbPath); ret =
		 * restoreAccountWithRootProcess(context,
		 * context.getFilesDir().getAbsolutePath(),
		 * tempAccountFile.getAbsolutePath(), accountDbPath); if(tempAccountFile
		 * != null && tempAccountFile.exists()){ tempAccountFile.delete(); }
		 * return ret;
		 */
		return false;
	}

	private boolean restoreAccountWithRootProcess(Context context, String internalDataDir,
			String src, String desc) {
		boolean ret = false;
		RootShell rp = null;
		try {
			rp = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		String command = "";
		command += internalDataDir + "backup bc ";
		command += "-r " + desc + " ";
		command += internalDataDir + "busybox" + " ";
		command += src;
		String result = new RootShell.Command(command).execute(rp);
		if (result != null && result.contains("GO_SUCCESS")) {
			ret = true;
		}
		return ret;
	}

	@Override
	public boolean isNeedRootAuthority() {
		return true;
	}
	
	@Override
	public Drawable getIcon(Context context, OnDrawableLoadedListener listener) {
		return null;
	}
}
