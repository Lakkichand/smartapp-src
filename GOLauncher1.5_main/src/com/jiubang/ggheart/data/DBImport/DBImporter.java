/*
 * 文 件 名:  DBImporter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-9-11
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.data.DBImport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Environment;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.INotificationId;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.data.DataProvider;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author rongjinsong
 * @date [2012-9-11]
 */
public class DBImporter {
	public static void importDB(Context context, String srcPkg, String dstPkg) {
		DataProvider.getInstance(context).close();
		String srcFile = Environment.getDataDirectory() + "/data/" + srcPkg
				+ "/databases/androidheart.db";
		String dstFile = Environment.getDataDirectory() + "/data/" + dstPkg
				+ "/databases/androidheart.db";
		File srcDB = new File(srcFile);
		if (srcDB.exists()) {
			deleteDbFile(dstFile);
			File destFolder = new File(Environment.getDataDirectory() + "/data/" + dstPkg
					+ "/databases/");
			destFolder.mkdirs();
			try {
				copyOutPutFile(new File(srcFile), new File(dstFile), 0);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void copyOutPutFile(File src, File dst, int encryptbyte) throws IOException {
		if (!src.exists()) {
			return;
		}
		FileInputStream srcStream = new FileInputStream(src);
		FileOutputStream dstStream = new FileOutputStream(dst);
		FileChannel inChannel = srcStream.getChannel();
		FileChannel outChannel = dstStream.getChannel();
		if (encryptbyte < 0) {
			encryptbyte = 0;
		}
		try {
			inChannel.transferTo(inChannel.size() - encryptbyte, inChannel.size(), outChannel);
			outChannel.transferFrom(inChannel, outChannel.size(), inChannel.size());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {

			srcStream.close();
			dstStream.close();
		}
	}

	private static File deleteDbFile(String path) {
		File dbFile = new File(path);
		FileUtil.deleteFile(path);
		deleteDbWalFiles(path);
		return dbFile;
	}

	private static void deleteDbWalFiles(String path) {
		String tmpFilePath = path + "-shm";
		FileUtil.deleteFile(tmpFilePath);
		tmpFilePath = path + "-wal";
		FileUtil.deleteFile(tmpFilePath);
	}

	/**
	 * 在通知栏提示
	 * 
	 * @param mContext
	 */
	public static void sendNotify(Context context) {
		PackageManager mPackageManager = context.getPackageManager();
		Intent intent = new Intent("com.gau.go.launcherex.MAIN");
		List<ResolveInfo> infos = mPackageManager.queryIntentActivities(intent, 0);
		Iterator iterator = infos.iterator();
		while (iterator.hasNext()) {
			ResolveInfo info = (ResolveInfo) iterator.next();
			if (info.activityInfo.packageName.equals(context.getPackageName())) {
				iterator.remove();
				continue;
			}
			File file = new File("/data/data/" + info.activityInfo.packageName + "/shared_prefs/"
					+ IPreferencesIds.DB_PROVIDER_SUPPORT + ".xml");
			if (!file.exists()) {
				iterator.remove();
				continue;
			}
			try {
				Context ctx = context.createPackageContext(info.activityInfo.packageName,
						Context.CONTEXT_IGNORE_SECURITY);
				SharedPreferences sharedPreferences = ctx.getSharedPreferences(
						IPreferencesIds.DB_PROVIDER_SUPPORT, Context.CONTEXT_IGNORE_SECURITY);
				if (!sharedPreferences.getBoolean(IPreferencesIds.IMPORT_SUPPORT, false)) {
					iterator.remove();
					continue;
				}
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (infos.size() > 0) {
			String title = context.getString(R.string.desk_migrate);
			String text = context.getString(R.string.overwrite_launcher_notify);
			intent = new Intent(GoLauncher.getContext(), LauncherSelectorActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			AppUtils.sendNotification(GoLauncher.getContext(), intent, R.drawable.sta_notify_news,
					title, title, text, INotificationId.MIGRATE_TIP);
		}
		// Log.e(null,
		// "colin *-------------------------------first  "+infos.size());
	}
}
