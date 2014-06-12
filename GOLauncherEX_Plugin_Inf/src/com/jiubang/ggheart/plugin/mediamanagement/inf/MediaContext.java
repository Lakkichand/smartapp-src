package com.jiubang.ggheart.plugin.mediamanagement.inf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

/**
 * 
 * <br>类描述: 多媒体插件上下文
 * <br>功能详细描述: Context的包装类，重载getClassLoader方法
 * 
 * @author  yangguanxiang
 * @date  [2012-11-23]
 */
public class MediaContext extends Context {
	private Context mContext;
	private ClassLoader mLoader;
	public MediaContext(Context parent, ClassLoader classLoader) {
		mContext = parent;
		mLoader = classLoader;
	}

	public Context getParent() {
		return mContext;
	}

	@Override
	public AssetManager getAssets() {
		return mContext.getAssets();
	}

	@Override
	public Resources getResources() {
		return mContext.getResources();
	}

	@Override
	public PackageManager getPackageManager() {

		return mContext.getPackageManager();
	}

	@Override
	public ContentResolver getContentResolver() {

		return mContext.getContentResolver();
	}

	@Override
	public Looper getMainLooper() {
		return mContext.getMainLooper();
	}

	@Override
	public Context getApplicationContext() {
		return mContext.getApplicationContext();
	}

	@Override
	public void setTheme(int resid) {
		mContext.setTheme(resid);
	}

	@Override
	public Theme getTheme() {
		return mContext.getTheme();
	}

	@Override
	public ClassLoader getClassLoader() {
		return mLoader;
	}

	@Override
	public String getPackageName() {
		return mContext.getPackageName();
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		return mContext.getApplicationInfo();
	}

	@Override
	public String getPackageResourcePath() {
		return mContext.getPackageResourcePath();
	}

	@Override
	public String getPackageCodePath() {
		return mContext.getPackageCodePath();
	}

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		return mContext.getSharedPreferences(name, mode);
	}

	@Override
	public FileInputStream openFileInput(String name) throws FileNotFoundException {
		return mContext.openFileInput(name);
	}

	@Override
	public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
		return mContext.openFileOutput(name, mode);
	}

	@Override
	public boolean deleteFile(String name) {
		return mContext.deleteFile(name);
	}

	@Override
	public File getFileStreamPath(String name) {
		return mContext.getFileStreamPath(name);
	}

	@Override
	public File getFilesDir() {
		return mContext.getFilesDir();
	}

	@Override
	public File getExternalFilesDir(String type) {
		return mContext.getExternalFilesDir(type);
	}

	@Override
	public File getCacheDir() {
		return mContext.getCacheDir();
	}

	@Override
	public File getExternalCacheDir() {
		return mContext.getExternalCacheDir();
	}

	@Override
	public String[] fileList() {
		return mContext.fileList();
	}

	@Override
	public File getDir(String name, int mode) {
		return mContext.getDir(name, mode);
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode, CursorFactory factory) {
		return mContext.openOrCreateDatabase(name, mode, factory);
	}

	@Override
	public boolean deleteDatabase(String name) {
		return mContext.deleteDatabase(name);
	}

	@Override
	public File getDatabasePath(String name) {
		return mContext.getDatabasePath(name);
	}

	@Override
	public String[] databaseList() {
		return mContext.databaseList();
	}

	@Override
	public Drawable getWallpaper() {
		return mContext.getWallpaper();
	}

	@Override
	public Drawable peekWallpaper() {
		return mContext.peekWallpaper();
	}

	@Override
	public int getWallpaperDesiredMinimumWidth() {
		return mContext.getWallpaperDesiredMinimumWidth();
	}

	@Override
	public int getWallpaperDesiredMinimumHeight() {
		return mContext.getWallpaperDesiredMinimumHeight();
	}

	@Override
	public void setWallpaper(Bitmap bitmap) throws IOException {
		mContext.setWallpaper(bitmap);
	}

	@Override
	public void setWallpaper(InputStream data) throws IOException {
		mContext.setWallpaper(data);
	}

	@Override
	public void clearWallpaper() throws IOException {
		mContext.clearWallpaper();
	}

	@Override
	public void startActivity(Intent intent) {
		mContext.startActivity(intent);
	}

	@Override
	public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask,
			int flagsValues, int extraFlags) throws SendIntentException {
		mContext.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
	}

	@Override
	public void sendBroadcast(Intent intent) {
		mContext.sendBroadcast(intent);
	}

	@Override
	public void sendBroadcast(Intent intent, String receiverPermission) {
		mContext.sendBroadcast(intent, receiverPermission);
	}

	@Override
	public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
		mContext.sendOrderedBroadcast(intent, receiverPermission);
	}

	@Override
	public void sendOrderedBroadcast(Intent intent, String receiverPermission,
			BroadcastReceiver resultReceiver, Handler scheduler, int initialCode,
			String initialData, Bundle initialExtras) {
		mContext.sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler,
				initialCode, initialData, initialExtras);
	}

	@Override
	public void sendStickyBroadcast(Intent intent) {
		mContext.sendStickyBroadcast(intent);
	}

	@Override
	public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver,
			Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
		mContext.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler, initialCode,
				initialData, initialExtras);
	}

	@Override
	public void removeStickyBroadcast(Intent intent) {
		mContext.removeStickyBroadcast(intent);
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		return mContext.registerReceiver(receiver, filter);
	}

	@Override
	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
			String broadcastPermission, Handler scheduler) {
		return mContext.registerReceiver(receiver, filter, broadcastPermission, scheduler);
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {
		mContext.unregisterReceiver(receiver);
	}

	@Override
	public ComponentName startService(Intent service) {
		return mContext.startService(service);
	}

	@Override
	public boolean stopService(Intent service) {
		return mContext.stopService(service);
	}

	@Override
	public boolean bindService(Intent service, ServiceConnection conn, int flags) {
		return mContext.bindService(service, conn, flags);
	}

	@Override
	public void unbindService(ServiceConnection conn) {
		mContext.unbindService(conn);
	}

	@Override
	public boolean startInstrumentation(ComponentName className, String profileFile,
			Bundle arguments) {
		return mContext.startInstrumentation(className, profileFile, arguments);
	}

	@Override
	public Object getSystemService(String name) {
		return mContext.getSystemService(name);
	}

	@Override
	public int checkPermission(String permission, int pid, int uid) {
		return mContext.checkPermission(permission, pid, uid);
	}

	@Override
	public int checkCallingPermission(String permission) {
		return mContext.checkCallingPermission(permission);
	}

	@Override
	public int checkCallingOrSelfPermission(String permission) {
		return mContext.checkCallingOrSelfPermission(permission);
	}

	@Override
	public void enforcePermission(String permission, int pid, int uid, String message) {
		mContext.enforcePermission(permission, pid, uid, message);
	}

	@Override
	public void enforceCallingPermission(String permission, String message) {
		mContext.enforceCallingPermission(permission, message);
	}

	@Override
	public void enforceCallingOrSelfPermission(String permission, String message) {
		mContext.enforceCallingOrSelfPermission(permission, message);
	}

	@Override
	public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
		mContext.grantUriPermission(toPackage, uri, modeFlags);
	}

	@Override
	public void revokeUriPermission(Uri uri, int modeFlags) {
		mContext.revokeUriPermission(uri, modeFlags);
	}

	@Override
	public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
		return mContext.checkUriPermission(uri, pid, uid, modeFlags);
	}

	@Override
	public int checkCallingUriPermission(Uri uri, int modeFlags) {
		return mContext.checkCallingUriPermission(uri, modeFlags);
	}

	@Override
	public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
		return mContext.checkCallingOrSelfUriPermission(uri, modeFlags);
	}

	@Override
	public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid,
			int uid, int modeFlags) {
		return mContext.checkUriPermission(uri, readPermission, writePermission, pid, uid,
				modeFlags);
	}

	@Override
	public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
		mContext.enforceUriPermission(uri, pid, uid, modeFlags, message);
	}

	@Override
	public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
		mContext.enforceCallingUriPermission(uri, modeFlags, message);
	}

	@Override
	public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
		mContext.enforceCallingOrSelfUriPermission(uri, modeFlags, message);
	}

	@Override
	public void enforceUriPermission(Uri uri, String readPermission, String writePermission,
			int pid, int uid, int modeFlags, String message) {
		mContext.enforceUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags,
				message);
	}

	@Override
	public Context createPackageContext(String packageName, int flags) throws NameNotFoundException {
		return mContext.createPackageContext(packageName, flags);
	}

}
