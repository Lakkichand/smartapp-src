/*
 * 文 件 名:  ServiceCallbackDownload.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-10-22
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * <br>类描述:下载的服务随用随关的需求，用于桌面下载
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-10-22]
 */
public class ServiceCallbackDownload {

	public static void callbackDownload(final Context context,
			final ServiceCallbackRunnable runnable) {
		ServiceConnection connection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				IDownloadService mDownloadController = IDownloadService.Stub.asInterface(service);
				runnable.setDownloadController(mDownloadController);
				runnable.run();
				context.unbindService(this);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {

			}
		};
		try {
			context.startService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE));
			context.bindService(new Intent(ICustomAction.ACTION_DOWNLOAD_SERVICE), connection,
					Context.BIND_AUTO_CREATE);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * <br>类描述:桌面下载直接bindService之后，在ServiceConnect的回调Runnable
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-10-24]
	 */
	public static abstract class ServiceCallbackRunnable implements Runnable {
		/**
		 * 下载控制接口
		 */
		protected IDownloadService mDownloadController = null;
		/** {@inheritDoc} */

		public void setDownloadController(IDownloadService downloadController) {
			mDownloadController = downloadController;
		}

		// 下载文件保存目录
		public final static String DOWNLOAD_DIRECTORY_PATH = Environment
				.getExternalStorageDirectory() + "/GoStore/download/";
		public long downloadFileDirectly(Context context, String fileName, String downloadUrl,
				long id, String packageName, String customDownloadFileName, int iconType,
				String iconInfo) {
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
			long taskId = -1;
			if (!sdCardExist) {
				Toast.makeText(context, R.string.gostore_no_sdcard, Toast.LENGTH_SHORT).show();
				return taskId;
			}
			if (context == null || fileName == null || "".equals(fileName.trim())
					|| downloadUrl == null || "".equals(downloadUrl.trim())) {
				return taskId;
			}
			fileName = fileName.trim();
			downloadUrl = downloadUrl.trim();
			String saveFilePath = null;
			if (customDownloadFileName != null && customDownloadFileName.trim().length() > 0) {
				saveFilePath = customDownloadFileName;
			} else {
				saveFilePath = DOWNLOAD_DIRECTORY_PATH + fileName + System.currentTimeMillis()
						+ ".apk";
			}
			DownloadTask task = new DownloadTask(id, downloadUrl, fileName, saveFilePath,
					packageName, iconType, iconInfo);
			try {
				if (mDownloadController != null) {
					taskId = mDownloadController.addDownloadTask(task);
					if (taskId != -1) {
						// 添加默认的下载监听器
						mDownloadController.addDownloadTaskListenerByName(taskId,
								DefaultDownloadListener.class.getName());
						mDownloadController.startDownload(taskId);
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			return taskId;
		}
	}
}
