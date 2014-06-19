package com.jiubang.go.backup.pro.model;

import java.io.File;

import android.content.Context;

import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 */
public class Constant {
	public final static String GOBACKUP_ROOT_DIR = "GOBackup";
	public final static String BACKUP_RES_ROOT_DIR = "AllBackup";
	public final static String DROPBOX_ROOT_DIR = "GOBackup";
	public final static String DROPBOX_CONTENT_DIR = "content";
	public final static String GOBACKUP_CACHE_DIR = "cache";
	public final static String BACKUP_BINARY_FILE_BACKUP = "backup"; // root权限备份应用的二进制文件
	public final static String BACKUP_BINARY_FILE_BUSYBOX = "busybox"; // busybox
	// public static String BACKUP_BINARY_FILE_SOCKET_CLIENT = "NativeMain";

	// busybox以及backup二进制可执行文件的版本，用于判断是否需要替换files目录的相对应的文件
	// 覆盖安装程序时，不会主动的去替换files目录下的相对应的文件，需要手动判断版本号以及更新
	public static final int BUSYBOX_BACKUP_FILE_CURRENT_VERSION = 22;

	private static final String GOBACKUP_PASSWORD = "GO_BACKUP";

	public static String getPassword() {
		return GOBACKUP_PASSWORD;
	}

	/*
	 * public static String buildDropboxContentDir(){ return File.separator +
	 * DROPBOX_ROOT_DIR + File.separator + DROPBOX_CONTENT_DIR + File.separator;
	 * }
	 */

	public static String buildNetworkBackupCacheDir(Context context) {
		return Util.ensureFileSeparator(Util.getSdRootPathOnPreference(context)) + GOBACKUP_ROOT_DIR + File.separator + GOBACKUP_CACHE_DIR
				+ File.separator;
	}
}
