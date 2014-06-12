/*
 * 文 件 名:  RecommAppDownloadListenter1.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.apps.appmanagement.download;

import java.io.File;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IAidlDownloadListener;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-8-23]
 */
public class RecommAppDownloadListenter extends IAidlDownloadListener.Stub {

	private Context mContext;

	private int mRestarCount;

	private Handler mHandler;

	public RecommAppDownloadListenter(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	@Override
	public void onStart(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWait(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpdate(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onComplete(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (task != null) {
			String filePath = task.getSaveFilePath();
			File saveFile = new File(filePath);
			if (saveFile.exists() && saveFile.isFile()) {
				if (filePath.endsWith(".tmp")) {
					filePath = filePath.replace(".tmp", ".png");
					Log.e("recommdownload ", " onDownloadComplete something is wrong ");
					if (saveFile.renameTo(new File(filePath))) {

					}
				}
				if (mHandler != null) {
					Message message = mHandler.obtainMessage(1);
					mHandler.sendMessage(message);
				}
			}
		}
	}

	@Override
	public void onFail(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub
		if (mContext != null && task != null) {
			++mRestarCount;
			if (mRestarCount <= 5) {
				if (Machine.isNetworkOK(mContext)) {
					IDownloadService downloadController = GOLauncherApp.getApplication()
							.getDownloadController();
					downloadController.restartDownloadById(task.getId());
				}
			} else {
				handleDownloadFail(task);
			}
		}
	}

	@Override
	public void onReset(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStop(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancel(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionSuccess(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void onException(DownloadTask task) throws RemoteException {
		// TODO Auto-generated method stub

	}

	private void handleDownloadFail(DownloadTask downloadTask) {
		int taskId = Long.valueOf(downloadTask.getId()).intValue();
		String filePath = downloadTask.getSaveFilePath();
		if (filePath != null && filePath.length() > 0) {
			File saveFile = new File(filePath);
			if (saveFile.exists()) {
				saveFile.delete();
			}
		}
		IDownloadService mDownloadController = GOLauncherApp.getApplication()
				.getDownloadController();
		try {
			if (mDownloadController != null) {
				mDownloadController.removeDownloadTaskById(taskId);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
