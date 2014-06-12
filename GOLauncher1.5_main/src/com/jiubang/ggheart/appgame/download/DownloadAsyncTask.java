/*
 * 文 件 名:  DownloadAsyncTask.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-22
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.os.AsyncTask;
import android.util.Log;

import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-22]
 */
public class DownloadAsyncTask extends AsyncTask<Object, DownloadTask, Integer> {

	/**
	 * 时间步，用于轮询
	 */
	private static final int TIME_STEP = 2000;
	/**
	 * 下载任务
	 */
	private DownloadTask mDownloadTask = null;
	/**
	 * 最大分段线程数
	 */
	private static final int MAX_THREAD_NUM = 3;
	/**
	 * 5M是分段的最小块
	 */
	private static final long MIN_BLOCK_SIZE = 5242880;
	/**
	 * 当前所有下载线程数据与状态
	 */
	private DownloadResult mDownloadResult = null;
	/**
	 * 下载管理类
	 */
	private DownloadManager mDownloadManager = null;
	/**
	 * 下载线程队列
	 */
	private ArrayList<DownloadThread> mThreadList = new ArrayList<DownloadThread>();
	
	public DownloadAsyncTask(DownloadTask task, DownloadManager downloadManager) {
		super();
		mDownloadTask = task;
		mDownloadManager = downloadManager;
		mDownloadResult = new DownloadResult(mDownloadManager, mDownloadTask);
	}

	@Override
	protected Integer doInBackground(Object... params) {
		int result = DownloadResult.DOWNLOAD_RUNNING;
		if (mDownloadTask != null && mDownloadTask.getState() != DownloadTask.STATE_STOP
				&& mDownloadTask.getState() != DownloadTask.STATE_DELETE) {
			// 设置状态
			mDownloadTask.setState(DownloadTask.STATE_START);
			// 任务状态改变之后，保存下载任务的信息
			mDownloadManager.saveNotCompleteTask();
			// 通知下载开始
			mDownloadTask.notifyListener(IDownloadListenerConstance.METHOD_ON_START_DOWNLOAD_ID);
			result = startDownload();
			switch (result) {
			// 根据返回结果进行回调
				case DownloadResult.DOWNLOAD_COMPLETE : {
					// 改变任务状态和保存下载信息
					if (mDownloadTask != null) {
						mDownloadTask.setState(DownloadTask.STATE_FINISH);
						// 任务状态改变之后，保存下载任务的信息
						mDownloadManager.saveNotCompleteTask();
					}
				}
					break;
				case DownloadResult.DOWNLOAD_FAIL : {
					handleState(DownloadTask.STATE_FAIL,
							IDownloadListenerConstance.METHOD_ON_DOWNLOAD_FAIL_ID);
				}
					break;
				case DownloadResult.DOWNLOAD_STOP : {
					handleState(DownloadTask.STATE_STOP,
							IDownloadListenerConstance.METHOD_ON_DOWNLOAD_STOP_ID);
					break;
				}
				case DownloadResult.DOWNLOAD_DELETE : {
					handleState(DownloadTask.STATE_DELETE,
							IDownloadListenerConstance.METHOD_ON_DOWNLOAD_DELETE_ID);
					break;
				}
				default :
					break;
			}
		} else if (mDownloadTask != null && mDownloadTask.getState() == DownloadTask.STATE_STOP) {
			result = DownloadResult.DOWNLOAD_STOP;
		} else if (mDownloadTask != null && mDownloadTask.getState() == DownloadTask.STATE_DELETE) {
			result = DownloadResult.DOWNLOAD_DELETE;
		} else {
			result = DownloadResult.DOWNLOAD_FAIL;
		}
		return result;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		switch (result) {
		// 根据返回结果进行回调
			case DownloadResult.DOWNLOAD_COMPLETE : {
				mDownloadManager.removeDownloadTaskById(mDownloadTask.getId(), true);
				mDownloadTask
						.notifyListener(IDownloadListenerConstance.METHOD_ON_DOWNLOAD_COMPLETE_ID);
			}
				break;
			case DownloadResult.DOWNLOAD_FAIL : {
				mDownloadManager.notifyListener(
						IDownloadManagerListener.METHOD_ON_FAIL_DOWNLOADTASK_ID, mDownloadTask);
				break;
			}
			case DownloadResult.DOWNLOAD_DELETE : {
				mDownloadTask.destory();
				break;
			}
			default :
				break;
		}
	}

	private void handleState(int state, int methodId) {
		if (mDownloadTask != null) {
			mDownloadTask.setState(state);
			// 任务状态改变之后，保存下载任务的信息
			mDownloadManager.saveNotCompleteTask();
			if (state == DownloadTask.STATE_FAIL) {
				int count = mDownloadTask.getRestartCount();
				if (count > 0) {
					count--;
					mDownloadManager.removeTaskIdFromDownloading(mDownloadTask.getId(), false);
					mDownloadTask.notifyListener(methodId);
					mDownloadTask.setRestartCount(count);
					mDownloadManager.restartDownload(mDownloadTask.getId());
				} else {
					mDownloadManager.removeTaskIdFromDownloading(mDownloadTask.getId(), true);
					// 加入到失败队列中，网络重连接之后自动下载
					mDownloadManager.getFailTaskIdList().add(mDownloadTask.getId());
					mDownloadTask.notifyListener(methodId);
				}
			} else {
				mDownloadManager.removeTaskIdFromDownloading(mDownloadTask.getId(), true);
				mDownloadTask.notifyListener(methodId);
			}
		}
	}

	private int startDownload() {

		if (mDownloadTask != null) {
			if (mDownloadTask.getState() == DownloadTask.STATE_STOP) {
				// 如果停止
				mDownloadResult.setResult(DownloadResult.DOWNLOAD_STOP);
			} else if (mDownloadTask.getState() == DownloadTask.STATE_DELETE) {
				// 如果删除
				mDownloadResult.setResult(DownloadResult.DOWNLOAD_DELETE);
			} else {
				try {
					// 在文件保存后加入.tmp后缀
					String tempFilePath = mDownloadTask.getSaveFilePath()
							+ DownloadManager.sDOWNLOADING_FORMAT;
					// 查看下载保存文件是否存在
					File saveFile = new File(tempFilePath);
					if (!saveFile.exists()) {
						// 如果不存在，就创建
						File parentFile = saveFile.getParentFile();
						if (!parentFile.exists()) {
							parentFile.mkdirs();
						}
						saveFile.createNewFile();
						// 如果文件不存在，但是下载数大于0
						if (mDownloadTask.getAlreadyDownloadSize() > 0) {
							// 重置下载任务数据
							mDownloadTask.resetDownloadTask();
							// 进行通知
							mDownloadTask
									.notifyListener(IDownloadListenerConstance.METHOD_ON_RESET_DOWNLOAD_TASK_ID);
						}
					}
					// 真正开始下载
					downloadFile(mDownloadTask.getDownloadUrl(), tempFilePath);
					if (mDownloadResult.getResult() == DownloadResult.DOWNLOAD_COMPLETE) {
						// 下载完成后，去掉3.tmp后缀
						saveFile.renameTo(new File(mDownloadTask.getSaveFilePath()));
						if (mDownloadTask.getIsApkFile()) {
							String command = "chmod 666 " + mDownloadTask.getSaveFilePath();
							Runtime runtime = Runtime.getRuntime();
							runtime.exec(command);
						}
					} else if (mDownloadResult.getResult() == DownloadResult.DOWNLOAD_FAIL) {
						// 下载失败，将所有线程失败的结果通知下载监听器
						ConcurrentHashMap<String, ThreadResultBean> map = mDownloadResult.getResultMap();
						Iterator<Entry<String, ThreadResultBean>> iter = map.entrySet().iterator();
						while (iter.hasNext()) {
							ThreadResultBean threadResultBean = iter.next().getValue();
							Exception e = threadResultBean.getException();
							if (e != null) {
								mDownloadTask.addException(e);
							}
						}
						mDownloadTask
								.notifyListener(IDownloadListenerConstance.METHOD_ON_EXCEPTION);
					}
				} catch (Exception e) {
					mDownloadResult.setResult(DownloadResult.DOWNLOAD_FAIL);
					mDownloadTask.addException(e);
					mDownloadTask.notifyListener(IDownloadListenerConstance.METHOD_ON_EXCEPTION);
				}
			}
		} else {
			// 下载任务为NULL，下载失败
			mDownloadResult.setResult(DownloadResult.DOWNLOAD_FAIL);
		}
		return mDownloadResult.getResult();
	}

	private void downloadFile(String downloadUrl, String filePath) throws Exception {
		HttpURLConnection httpConnection = null;
		// 下载所需要的线程数
		int threadNum = 0;
		try {
			// 先取得下载文件的总长
			httpConnection = openConnection(downloadUrl);
			if (httpConnection == null) {
				throw new ConnectException();
			}
			// 如果返回码不是200或者206，则抛出异常
			int responseCode = httpConnection.getResponseCode();
			AsrFilter asrFilter = new AsrFilter();
			// 判断类似于CMCC类型的需要验证的wifi网络
			if (!String.valueOf(responseCode).startsWith("2")
					|| asrFilter.isAsrResponse(httpConnection.getContentType())) {
				// AppGameNetInfoLog.getInstance().setExceptionCode(e);
				throw new Exception(httpConnection.getURL().getHost());
			}
			// 连接成功
			mDownloadTask.notifyListener(IDownloadListenerConstance.METHOD_ON_CONNECTION_SUCCESS);
			//建立连接成功之后，在task记录连接网络的网络类型是wifi还是其它类型的网络，用于下载失败重连
			boolean isInNewwork = GoStorePhoneStateUtil.isNetWorkAvailable(GOLauncherApp
					.getContext());
			boolean isInWifi = GoStorePhoneStateUtil.isWifiEnable(GOLauncherApp.getContext());
			if (isInNewwork && !isInWifi) {
				mDownloadTask.setDownloadNetWorkType(DownloadTask.NETWORK_TYPE_OTHER);
			} else {
				mDownloadTask.setDownloadNetWorkType(DownloadTask.NETWORK_TYPE_WIFI);
			}
			// 设置下载文件总长
			String content_length = httpConnection.getHeaderField("content-range");
			String acc = httpConnection.getHeaderField("Accept-Ranges");
			long totalSize = 0;
			if (content_length != null) {
				totalSize = Long.valueOf(content_length.substring(content_length.indexOf("/") + 1,
						content_length.length()));
			} else {
				totalSize = httpConnection.getContentLength();
			}
			mDownloadResult.setTotalSize(totalSize);
			if (httpConnection != null) {
				httpConnection.disconnect();
				httpConnection = null;
			}
			// 不支持多线程断点 
			if (responseCode == 200) {
				// 不支持多线程断点的下载任务，不能进行下载失败重试，否则将会不停地从0开始下载
				Log.i("liuxinyang","single thread");
				mDownloadTask.setRestartCount(0);
				runSingleThread(downloadUrl, filePath, totalSize);
			} else if (responseCode == 206) {
				Log.i("liuxinyang","Multi thread");
				// 重置下载重试次数为3
				mDownloadTask.setRestartCount(3);
				// 支持多线程断点
				runMultiThread(downloadUrl, filePath, totalSize);
			}
			long time = System.currentTimeMillis();
			while (mDownloadTask.getState() != DownloadTask.STATE_STOP
					&& mDownloadTask.getState() != DownloadTask.STATE_DELETE
					&& mDownloadResult.getResult() == DownloadResult.DOWNLOAD_RUNNING) {
				// 每隔time_step的时间就更新一次数据
				if (System.currentTimeMillis() - time > TIME_STEP) {
					if (mDownloadTask.getState() != DownloadTask.STATE_STOP
							&& mDownloadTask.getState() != DownloadTask.STATE_DELETE) {
						mDownloadTask.setState(DownloadTask.STATE_DOWNLOADING);
					}
					mDownloadTask.setAlreadyDownloadSize(mDownloadResult.getAlreadyDownloadSize());
					mDownloadTask.setAlreadyDownloadPercent(mDownloadResult
							.getAlreadyDownloadPercent());
					mDownloadTask
							.notifyListener(IDownloadListenerConstance.METHOD_ON_PROGRESS_UPDATE_ID);
					time = System.currentTimeMillis();
				}
			}
			// 跳出while循环，第一种情况是任务手动暂停或者删除
			// 第二种情况，是任务下载失败
			// 下载的结果保存在DownloadResult中
			if (mDownloadTask.getState() == DownloadTask.STATE_STOP
					|| mDownloadTask.getState() == DownloadTask.STATE_DELETE) {
				// 停止所有正在下载的线程
				for (int i = 0; i < mThreadList.size(); i++) {
					mThreadList.get(i).setStop(true);
				}
				// 由于线程的异步运行，而DownloadResult根据所有的线程运行结果得到最终结果
				// 所以直接设置DownloadResult的结果
				if (mDownloadTask.getState() == DownloadTask.STATE_STOP) {
					mDownloadResult.setResult(DownloadResult.DOWNLOAD_STOP);
				} else if (mDownloadTask.getState() == DownloadTask.STATE_DELETE) {
					mDownloadResult.setResult(DownloadResult.DOWNLOAD_DELETE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();   
            PrintWriter pw = new PrintWriter(sw, true);   
            e.printStackTrace(pw);
			throw e;
		} finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
		}
	}
	/**
	 * <br>功能简述:建立连接
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param downloadUrl
	 * @return
	 * @throws Exception
	 */
	private HttpURLConnection openConnection(String downloadUrl) {
		HttpURLConnection httpConnection = null;
		try {
			URL url = new URL(downloadUrl);
			if (ThemeHttp.isCWWAPConnect(GOLauncherApp.getContext())
					&& GoStorePhoneStateUtil.getNetWorkType(GOLauncherApp.getContext()) != GoStorePhoneStateUtil.NETTYPE_UNICOM) {
				Proxy proxy = null;
				if (GoStorePhoneStateUtil.getNetWorkType(GOLauncherApp.getContext()) == GoStorePhoneStateUtil.NETTYPE_TELECOM) {
					String proxyHost = android.net.Proxy.getDefaultHost();
					int port = android.net.Proxy.getDefaultPort();
					proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost,
							port));
				} else {
					// 联想的3gwap经测试不需设置代理
					String host = ThemeHttp.getProxyHost(GOLauncherApp.getContext());
					int port = ThemeHttp.getProxyPort(GOLauncherApp.getContext());
					proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(host, port));
				}
				httpConnection = (HttpURLConnection) url.openConnection(proxy);
				httpConnection.setConnectTimeout(60000);
				httpConnection.setReadTimeout(60000);
				httpConnection.setChunkedStreamingMode(4096); // 设置chunked传输块大小
				proxy = null;
			} else {
				httpConnection = (HttpURLConnection) url.openConnection();
				httpConnection.setConnectTimeout(45000);
				httpConnection.setReadTimeout(45000);
			}
			// 断点续传
			// 设置 User-Agent
			httpConnection.setRequestProperty("User-Agent", "NetFox");
			String range = "bytes=" + "-1";
			httpConnection.setRequestProperty("Range", range);
			httpConnection.setDoInput(true);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();   
            PrintWriter pw = new PrintWriter(sw, true);   
		} catch (IOException e) {
			e.printStackTrace();
			StringWriter sw = new StringWriter();   
            PrintWriter pw = new PrintWriter(sw, true);   
		}
		return httpConnection;
	}
	
	private void runSingleThread(String downloadUrl, String filePath, Long totalSize) {
		mDownloadTask.setTotalSize(totalSize);
		mDownloadTask.setThreadNum(1);
		DownloadSingleThread thread = new DownloadSingleThread(mDownloadResult, downloadUrl, filePath);
		thread.setTag("" + mDownloadTask.getId() + 0);
		thread.start();
		// 任务状态改变之后，保存下载任务的信息
		mDownloadManager.saveNotCompleteTask();
	}
	
	private void runMultiThread(String downloadUrl, String filePath, Long totalSize) {
		// 先判断下载任务属于断点续传，断点续传的线程信息已经存在于downloadTask中
		int threadNum = mDownloadTask.getThreadNum();
		if (threadNum > 0 && mDownloadTask.getThreadDataMap().size() == threadNum) {
			long alreadyDownloadSize = totalSize;
			Iterator<Entry<String, DownloadTask.ThreadData>> iter = mDownloadTask
					.getThreadDataMap().entrySet().iterator();
			// 重新创建线程下载
			while (iter.hasNext()) {
				Entry<String, DownloadTask.ThreadData> entry = iter.next();
				DownloadTask.ThreadData data = entry.getValue();
				DownloadThread thread = new DownloadThread(mDownloadResult, downloadUrl, filePath,
						data.mStartPos, data.mEndPos);
				thread.setTag(data.mTag);
				// 计算已经下载的字节数
				alreadyDownloadSize = alreadyDownloadSize - (data.mEndPos - data.mStartPos);
				mThreadList.add(thread);
				thread.start();
			}
			mDownloadResult.updateAlreadyDownloadSize(alreadyDownloadSize);
		} else {
			// 假如task保存的信息不对称，则相当于重新下载
			// 根据文件总长计算需要使用的线程数
			threadNum = (int) (totalSize / MIN_BLOCK_SIZE);
			// 实际每个线程下载的数据块大小
			long blockSize = 0;
			// 文件总长分成N块之后，余数的大小
			long moreSize = 0;
			if (threadNum >= MAX_THREAD_NUM) {
				threadNum = MAX_THREAD_NUM;
				blockSize = totalSize / MAX_THREAD_NUM;
				moreSize = totalSize % blockSize;
			} else {
				blockSize = MIN_BLOCK_SIZE;
				moreSize = totalSize % MIN_BLOCK_SIZE;
			}
			if (moreSize > 0 && threadNum < MAX_THREAD_NUM) {
				threadNum += 1;
			}
			mDownloadTask.setTotalSize(totalSize);
			mDownloadTask.setThreadNum(threadNum);
			// 线程分段下载
			for (int i = 0; i < threadNum; i++) {
				DownloadThread thread = null;
				long start = 0;
				long end = 0;
				if (i == threadNum - 1) {
					if ((i + 1) * blockSize > totalSize) {
						thread = new DownloadThread(mDownloadResult, downloadUrl, filePath, i
								* blockSize, i * blockSize + moreSize);
						start = i * blockSize;
						end = i * blockSize + moreSize;
					} else {
						thread = new DownloadThread(mDownloadResult, downloadUrl, filePath, i
								* blockSize, (i + 1) * blockSize + moreSize);
						start = i * blockSize;
						end = (i + 1) * blockSize + moreSize;
					}
				} else {
					thread = new DownloadThread(mDownloadResult, downloadUrl, filePath, i
							* blockSize, (i + 1) * blockSize);
					start = i * blockSize;
					end = (i + 1) * blockSize;
				}
				thread.setTag("" + mDownloadTask.getId() + i);
				mThreadList.add(thread);
				thread.start();
			}
		}
		// 任务状态改变之后，保存下载任务的信息
		mDownloadManager.saveNotCompleteTask();
	}
	
	
}