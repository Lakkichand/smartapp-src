package com.jiubang.go.backup.pro.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.jiubang.go.backup.pro.model.BackupManager;

/**
 * 应用程序信息loader
 * 
 * @author wencan
 */
public class AppExtraInfoLoader {
	private List<BaseEntry> mAllAppEntrys;
	private Context mContext;

	public static final int LOAD_LABEL = 0x0001;
	public static final int LOAD_SIZE = LOAD_LABEL << 1;
	public static final int LOAD_ICON = LOAD_SIZE << 1;

	public AppExtraInfoLoader(Context context, List<BaseEntry> appEntrys) {
		mContext = context;
		mAllAppEntrys = Collections.synchronizedList(new ArrayList<BaseEntry>());

		if (appEntrys != null && appEntrys.size() > 0) {
			Iterator<BaseEntry> iterator = appEntrys.iterator();
			if (iterator == null) {
				return;
			}

			while (iterator.hasNext()) {
				try {
					BaseEntry entry = iterator.next();
					mAllAppEntrys.add(entry);
				} catch (Exception e) {
					break;
				}
			}

		}
	}

	public void load(int loadType) {
		long dt = System.currentTimeMillis();

		boolean label = (loadType & LOAD_LABEL) > 0;
		boolean icon = (loadType & LOAD_ICON) > 0;
		boolean size = (loadType & LOAD_SIZE) > 0;

		int threadCount = 0;
		if (label) {
			threadCount++;
		}
		if (icon) {
			threadCount++;
		}
		if (size) {
			threadCount++;
		}

		CountDownLatch latch = new CountDownLatch(threadCount);
		ExecutorService exec = Executors.newFixedThreadPool(threadCount);
		if (label) {
			exec.submit(new LabelLoader(mContext, latch));
		}
		if (icon) {
			exec.submit(new IconLoader(mContext, latch));
		}
		if (size) {
			exec.submit(new SizeLoader(mContext, latch));
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Log.d("GOBackup", "AppLoader : load finish");

		exec.shutdown();
		mAllAppEntrys.clear();
		Log.d("GOBackup", "load = " + (System.currentTimeMillis() - dt));
	}

	/**
	 * LabelLoader
	 * 
	 * @author wencan
	 */
	private class LabelLoader implements Runnable {
		private CountDownLatch mLatch;
		private Context mContext;

		public LabelLoader(Context context, CountDownLatch latch) {
			this.mLatch = latch;
			this.mContext = context;
		}

		@Override
		public void run() {
			loadLabel();
			mLatch.countDown();
			Log.d("GOBackup", "AppLoader : LabelLoader finish");
		}

		private void loadLabel() {
			Iterator<BaseEntry> iterator = mAllAppEntrys.iterator();
			if (iterator == null) {
				return;
			}

			final PackageManager pm = mContext.getPackageManager();
			final BackupManager bm = BackupManager.getInstance();
			while (iterator.hasNext()) {
				BaseEntry entry = null;
				try {
					entry = iterator.next();
					if (entry == null) {
						continue;
					}
					if (entry instanceof AppEntry) {
						AppInfo appInfo = ((AppEntry) entry).getAppInfo();
						if (appInfo == null || appInfo.isApplicationNameValid()) {
							continue;
						}
//						appEntry.getAppInfo().loadLabel(pm);
						bm.getApplicationName(mContext, appInfo.packageName);
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	/**
	 * IconLoader
	 * 
	 * @author wencan
	 */
	private class IconLoader implements Runnable {
		private CountDownLatch mLatch;
		private Context mContext;

		public IconLoader(Context context, CountDownLatch latch) {
			this.mLatch = latch;
			this.mContext = context;
		}

		@Override
		public void run() {
			loadIcon();
			mLatch.countDown();
			Log.d("GOBackup", "AppLoader : IconLoader finish");
		}

		private void loadIcon() {
			Iterator<BaseEntry> iterator = mAllAppEntrys.iterator();
			if (iterator == null) {
				return;
			}
			while (iterator.hasNext()) {
				BaseEntry entry = null;
				try {
					entry = iterator.next();
					if (entry == null) {
						continue;
					}

					if (entry instanceof AppBackupEntry) {
						AppBackupEntry appEntry = (AppBackupEntry) entry;
						if (appEntry.hasIconInited()) {
							continue;
						}
						appEntry.loadIcon(mContext);
					} else if (entry instanceof AppRestoreEntry) {
						// AppRestoreEntry的label是从数据库初始化
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	/**
	 * SizeLoader
	 * 
	 * @author wencan
	 */
	private class SizeLoader implements Runnable {

		private CountDownLatch mLatch;
		private Context mContext;

		public SizeLoader(Context context, CountDownLatch latch) {
			this.mLatch = latch;
			this.mContext = context;
		}

		@Override
		public void run() {
			Iterator<BaseEntry> iterator = mAllAppEntrys.iterator();
			if (iterator == null) {
				return;
			}

			while (iterator.hasNext()) {
				try {
					BaseEntry entry = iterator.next();
					if (entry == null) {
						continue;
					}

					if (entry instanceof AppBackupEntry) {
						AppBackupEntry appEntry = (AppBackupEntry) entry;
						if (!appEntry.hasInitSizeFinish()) {
							appEntry.getSpaceUsage();
						}
					}
				} catch (Exception e) {
					break;
				}
			}
			mLatch.countDown();
		}
	}
}
