package com.jiubang.go.backup.pro.data;

import java.io.File;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.util.Util;

/**
 * AccountBackupEntry
 * 
 * @author WenCan
 */
public class AccountBackupEntry extends BaseBackupEntry {
	public static final String ACCOUNT_FILE_NAME = "account.encrypt";

	@Override
	public boolean backup(Context ctx, Object data, IAsyncTaskListener listener) {
		if (ctx == null || data == null || listener == null) {
			return false;
		}
		if (!(data instanceof BackupArgs)) {
			return false;
		}

		listener.onStart(null, null);
		boolean ret = false;
		ret = backupAccountInternal(ctx, (BackupArgs) data);
		BackupState state = ret ? BackupState.BACKUP_SUCCESSFUL : BackupState.BACKUP_ERROR_OCCURRED;
		setState(state);
		listener.onEnd(ret, this, null);
		return true;
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

	private boolean backupAccountInternal(Context context, BackupArgs args) {
		final String rootDir = Util.ensureFileSeparator(args.mBackupPath);

		String accountDbPath = null;
		String accountDbName = null;
		// LinuxShell ls = new LinuxShell();
		// accountDbPath =
		// ls.backupAccount(context.getFilesDir().getAbsolutePath(), rootDir);
		accountDbPath = backupAccountWithRootProcess(context, context.getFilesDir()
				.getAbsolutePath(), rootDir);
		if (accountDbPath == null) {
			// 备份失败
			return false;
		}

		Log.d("GOBackup", "AccountBackupEntry : accountDbPath = " + accountDbPath);
		accountDbName = accountDbPath.substring(accountDbPath.lastIndexOf(File.separator) + 1);
		Log.d("GOBackup", "AccountBackupEntry : accountDbName = " + accountDbName);
		File tempAccountFile = new File(rootDir, accountDbName);
		if (!tempAccountFile.exists()) {
			return false;
		}

		boolean ret = false;
		// 加密
		File accountFile = new File(rootDir, ACCOUNT_FILE_NAME);
		if (Util.encryFile(tempAccountFile, accountFile, Constant.getPassword())) {
			ret = true;
		}

		/*
		 * if(ret){
		 * args.mConfig.put(BackupPropertiesConfig.P_BACKUP_ACCOUNT_PATH,
		 * accountDbPath); }
		 */
		if (tempAccountFile != null && tempAccountFile.exists()) {
			tempAccountFile.delete();
		}
		return ret;
	}

	private String backupAccountWithRootProcess(Context ctx, String internalDataDir, String desc) {
		String accountDbPath = null;
		RootShell rp = null;
		try {
			rp = RootShell.startShell();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		String command = "";
		command += internalDataDir + "backup bc ";
		command += "-b ";
		command += internalDataDir + "busybox" + " ";
		command += desc;
		String result = new RootShell.Command(command).execute(rp);
		if (result != null && result.contains("GO_SUCCESS")) {
			String[] allResults = result.split("\n");
			if (allResults != null) {
				for (String str : allResults) {
					if (str.startsWith("ACCOUNT_DB_PATH=")) {
						accountDbPath = str.substring(16);
						Log.d("GOBackup", "backupAccount : accountDbPath = " + accountDbPath);
					}
				}
			}
		}
		return accountDbPath;
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
