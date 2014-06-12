/*
 * 文 件 名:  DownloadSingleThread.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2013-1-15
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import android.util.Log;

import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * <br>类描述:单线程下载，适用于不支持断点续传的环境，每次下载的位置都将从0开始,保证下载数据的正确
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2013-1-15]
 */
public class DownloadSingleThread extends Thread {

	private String mTag = "";
	/**
	 * 下载文件的起始位置
	 */
	private long mStartPosition = 0;
	/**
	 * 下载文件的终止位置
	 */
	private long mEndPosition = 0;
	/**
	 * 需要存在的文件路径
	 */
	private String mFilePath = null;
	/**
	 * 下载文件的url地址
	 */
	private String mDownloadUrl = null;
	/**
	 * 外部指示线程是否需要停止下载
	 */
	private boolean mIsStop = false;
	/**
	 * 下载的状态,初始为正在运行
	 */
	private int mState = 0;
	/**
	 * 下载结果回调
	 */
	private DownloadResult mDownloadResult = null;

	private long mTime = 0;

	private final static long TIME_STEP = 2000;

	public DownloadSingleThread(DownloadResult downloadResult, String downloadUrl, String filePath) {
		mDownloadResult = downloadResult;
		mFilePath = filePath;
		mDownloadUrl = downloadUrl;
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param state
	 */
	public void setStop(boolean isStop) {
		mIsStop = isStop;
	}

	public void setTag(String tag) {
		mTag = tag;
	}

	public String getTag() {
		return mTag;
	}

	@Override
	public void run() {
		super.run();
		RandomAccessFile randomFile = null;
		HttpURLConnection httpConnection = null;
		InputStream inputStream = null;
		try {
			File file = new File(mFilePath);
			// 文件不存在,创建一个新的文件
			if (!file.exists()) {
				file.createNewFile();
			}
			// 创建随机读写文件,从0开始 
			randomFile = new RandomAccessFile(file, "rw");
			randomFile.seek(0);
			httpConnection = openConnection(mDownloadUrl);
			if (httpConnection == null) {
				mState = ThreadResultBean.DOWNLOAD_THREAD_NOT_FINISH;
				// connection为空，返回没有完成
				mDownloadResult.notifyDownloadResult(mTag,
						createThreadResultBean(mState, mStartPosition, mEndPosition, null));
				return;
			}
			// 如果返回码不是200或者206，则抛出异常
			int responseCode = httpConnection.getResponseCode();
			AsrFilter asrFilter = new AsrFilter();
			// 判断类似于CMCC类型的需要验证的wifi网络
			if (!String.valueOf(responseCode).startsWith("2")
					|| asrFilter.isAsrResponse(httpConnection.getContentType())) {
				Exception e = new Exception(httpConnection.getURL().getHost());
				mState = ThreadResultBean.DOWNLOAD_THREAD_ERROR;
				mDownloadResult.notifyDownloadResult(mTag,
						createThreadResultBean(mState, mStartPosition, mEndPosition, e));
				return;
			}
			// 因为不支持断点，所以要从0开始下载
			mStartPosition = 0;
			mEndPosition = httpConnection.getContentLength();
			// 开始下载，需要通知外部
			mState = ThreadResultBean.DOWNLOAD_THREAD_RUNNING;
			mDownloadResult.notifyDownloadResult(mTag,
					createThreadResultBean(mState, mStartPosition, mEndPosition, null));
			inputStream = httpConnection.getInputStream();
			byte[] buffer = new byte[4096];
			int hasRead = 0;
			while (!mIsStop && mStartPosition < mEndPosition
					&& (hasRead = inputStream.read(buffer)) > 0) {
				// 向文件写入数据
				randomFile.write(buffer, 0, hasRead);
				// 累计该线程下载的总大小
				mStartPosition += hasRead;
				mDownloadResult.updateAlreadyDownloadSize(hasRead);
				if (System.currentTimeMillis() - mTime > TIME_STEP) {
					mTime = System.currentTimeMillis();
					mState = ThreadResultBean.DOWNLOAD_THREAD_RUNNING;
					// 开始下载，需要通知外部
					mDownloadResult.notifyDownloadResult(mTag,
							createThreadResultBean(mState, mStartPosition, mEndPosition, null));
				}
			}
			if (mStartPosition >= mEndPosition) {
				mState = ThreadResultBean.DOWNLOAD_THREAD_FINISH;
				// 通知下载完成
				mDownloadResult.notifyDownloadResult(mTag,
						createThreadResultBean(mState, mStartPosition, mEndPosition, null));
				Log.i("liuxinyang", "download complete");
				return;
			} else {
				mState = ThreadResultBean.DOWNLOAD_THREAD_NOT_FINISH;
				// 通知下载未完成
				mDownloadResult.notifyDownloadResult(mTag,
						createThreadResultBean(mState, mStartPosition, mEndPosition, null));
				return;
			}
		} catch (Exception e) {
			// 向外反馈这个错误
			mState = ThreadResultBean.DOWNLOAD_THREAD_ERROR;
			mDownloadResult.notifyDownloadResult(mTag,
					createThreadResultBean(mState, mStartPosition, mEndPosition, e));
			StringWriter sw = new StringWriter();   
            PrintWriter pw = new PrintWriter(sw, true);   
            e.printStackTrace(pw);
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (randomFile != null) {
					randomFile.close();
				}
				if (httpConnection != null) {
					httpConnection.disconnect();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	// TODO:下载wangzhuobin 有两个地方用到相同的方法，是否可以抽到辅助工具类里面
	/**
	 * <br>功能简述:建立连接
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param downloadUrl
	 * @return
	 * @throws Exception
	 */
	private HttpURLConnection openConnection(String downloadUrl) throws Exception {
		HttpURLConnection httpConnection = null;
		URL url = new URL(downloadUrl);
		if (GoStorePhoneStateUtil.getNetWorkType(GOLauncherApp.getContext()) != GoStorePhoneStateUtil.NETTYPE_UNICOM
				&& ThemeHttp.isCWWAPConnect(GOLauncherApp.getContext())) {
			Proxy proxy = null;
			if (GoStorePhoneStateUtil.getNetWorkType(GOLauncherApp.getContext()) == GoStorePhoneStateUtil.NETTYPE_TELECOM) {
				String proxyHost = android.net.Proxy.getDefaultHost();
				int port = android.net.Proxy.getDefaultPort();
				proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
			} else {
				// 联想的3gwap经测试不需设置代理
				String host = ThemeHttp.getProxyHost(GOLauncherApp.getContext());
				int port = ThemeHttp.getProxyPort(GOLauncherApp.getContext());
				proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(host, port));
			}
			httpConnection = (HttpURLConnection) url.openConnection(proxy);
			httpConnection.setConnectTimeout(60000);
			httpConnection.setReadTimeout(60000);
			// 设置chunked传输块大小
			httpConnection.setChunkedStreamingMode(4096);
			proxy = null;
		} else {
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setConnectTimeout(45000);
			httpConnection.setReadTimeout(45000);
		}
		// 断点续传设置 User-Agent
		httpConnection.setRequestProperty("User-Agent", "NetFox");
		// 设置获取数据的范围
		httpConnection.setDoInput(true);
		return httpConnection;
	}

	/**
	 * <br>功能简述:取当前线程的下载起始位置。起始位置会随着下载数据的变化而增加，最终等于endPosition
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public long getStartPosition() {
		return mStartPosition;
	}

	/**
	 * <br>功能简述:取当前线程的下载终止位置
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public long getEndPosition() {
		return mEndPosition;
	}

	private ThreadResultBean createThreadResultBean(int state, long start, long end, Exception e) {
		ThreadResultBean bean = new ThreadResultBean();
		bean.setState(mState);
		bean.setStartPosition(start);
		bean.setEndPosition(end);
		bean.setException(e);
		return bean;
	}

}
