package com.ta.util.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectTimeoutException;

import android.os.Message;
import android.util.Log;

import com.ta.exception.FileAlreadyExistException;
import com.ta.util.TALogger;
import com.zhidian.wifibox.util.TimeTool;

public class FileHttpResponseHandler extends AsyncHttpResponseHandler {
	public final static int TIME_OUT = 30000;
	private final static int BUFFER_SIZE = 1024 * 8;

	private static final String TAG = "FileHttpResponseHandler";
	private static final String TEMP_SUFFIX = ".download";
	private File file;
	private File tempFile;
	private File baseDirFile;
	private RandomAccessFile outputStream;
	private long downloadSize;
	private long previousFileSize;
	private long totalSize;
	private long networkSpeed;
	private long previousTime;
	private long totalTime;
	private boolean interrupt = false;
	private boolean timerInterrupt = false;
	private String url;
	private Timer timer = new Timer();
	private static final int TIMERSLEEPTIME = 1000;
	private long mTSpeed1 = -1;
	private long mTSpeed2 = -1;
	private long mTSpeed3 = -1;

	/**
	 * 上次发送进度时的已下载大小
	 */
	private long mLastSize;
	/**
	 * 上次发送速度时的时间
	 */
	private long mLastTime;

	public FileHttpResponseHandler(String url, String rootFile, String fileName) {

		super();
		this.url = url;
		this.baseDirFile = new File(rootFile);
		this.file = new File(rootFile, fileName);
		this.tempFile = new File(rootFile, fileName + TEMP_SUFFIX);
		init();
	}

	public FileHttpResponseHandler(String rootFile, String fileName) {
		super();
		this.baseDirFile = new File(rootFile);
		this.file = new File(rootFile, fileName);
		this.tempFile = new File(rootFile, fileName + TEMP_SUFFIX);
		init();
	}

	public FileHttpResponseHandler(String filePath) {
		super();
		this.file = new File(filePath);
		this.baseDirFile = new File(this.file.getParent());
		this.tempFile = new File(filePath + TEMP_SUFFIX);
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		if (!this.baseDirFile.exists()) {
			this.baseDirFile.mkdirs();
		}
	}

	private void startTimer() {
		mLastSize = previousFileSize;
		mLastTime = System.currentTimeMillis();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (!timerInterrupt) {
					long currentSize = getDownloadSize();
					long currentTime = System.currentTimeMillis();
					long speed = (long) (((currentSize - mLastSize) * 1.0 / (currentTime - mLastTime)) / 1.024 + 0.5);
					mTSpeed1 = mTSpeed2;
					mTSpeed2 = mTSpeed3;
					mTSpeed3 = speed;
					long aSpeed = 0;
					if (mTSpeed1 == -1 && mTSpeed2 == -1) {
						aSpeed = mTSpeed3;
					} else if (mTSpeed1 == -1) {
						aSpeed = (long) ((mTSpeed3 + mTSpeed2) / 2.0 + 0.5);
					} else {
						aSpeed = (long) ((mTSpeed3 + mTSpeed2 + mTSpeed1) / 3.0 + 0.5);
					}
					sendProgressMessage(totalSize, currentSize, speed,
							TimeTool.getStandardTime(), aSpeed);
					mLastSize = currentSize;
					mLastTime = currentTime;
					try {
						Thread.sleep(TIMERSLEEPTIME);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}, 1000, 1000);
	}

	private void stopTimer() {
		timerInterrupt = true;
	}

	public File getFile() {
		return file;
	}

	public String getUrl() {
		return url;
	}

	private class ProgressReportingRandomAccessFile extends RandomAccessFile {
		private int progress = 0;

		public ProgressReportingRandomAccessFile(File file, String mode)
				throws FileNotFoundException {
			super(file, mode);

		}

		@Override
		public void write(byte[] buffer, int offset, int count)
				throws IOException {

			super.write(buffer, offset, count);
			progress += count;
			totalTime = System.currentTimeMillis() - previousTime;
			downloadSize = progress + previousFileSize;
			if (totalTime > 0) {
				networkSpeed = (long) ((progress / totalTime) / 1.024);
			}

		}
	}

	public boolean isInterrupt() {

		return interrupt;
	}

	public void setInterrupt(boolean interrupt) {
		this.interrupt = interrupt;
	}

	public long getDownloadSize() {

		return downloadSize;
	}

	public long getTotalSize() {

		return totalSize;
	}

	public double getDownloadSpeed() {

		return this.networkSpeed;
	}

	public void setPreviousFileSize(long previousFileSize) {
		this.previousFileSize = previousFileSize;
	}

	public long getTotalTime() {

		return this.totalTime;
	}

	public void onSuccess(byte[] binaryData) {
		onSuccess(new String(binaryData));
	}

	public void onSuccess(int statusCode, byte[] binaryData) {
		onSuccess(binaryData);
	}

	public void onFailure(Throwable error, byte[] binaryData) {
		onFailure(error);
	}

	protected void sendSuccessMessage(int statusCode, byte[] responseBody) {
		long currentSize = getDownloadSize();
		long currentTime = System.currentTimeMillis();
		long speed = (long) (((currentSize - mLastSize) * 1.0 / (currentTime - mLastTime)) / 1.024 + 0.5);
		// long aveSpeed;//平均速度，用于在app上显示
		sendProgressMessage(totalSize, currentSize, speed, currentTime,
				networkSpeed);
		mLastSize = currentSize;
		mLastTime = currentTime;
		sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[] { statusCode,
				responseBody }));
	}

	@Override
	protected void sendFailureMessage(Throwable e, byte[] responseBody) {
		sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[] { e,
				responseBody }));
	}

	protected void sendProgressMessage(long totalSize, long currentSize,
			long speed, long time, long aveSpeed) {
		sendMessage(obtainMessage(PROGRESS_MESSAGE, new Object[] { totalSize,
				currentSize, speed, time, aveSpeed }));
	}

	protected void handleSuccessMessage(int statusCode, byte[] responseBody) {
		onSuccess(statusCode, responseBody);
	}

	protected void handleFailureMessage(Throwable e, byte[] responseBody) {
		onFailure(e, responseBody);
	}

	@Override
	protected void handleMessage(Message msg) {
		Object[] response;
		switch (msg.what) {
		case SUCCESS_MESSAGE:
			response = (Object[]) msg.obj;
			handleSuccessMessage(((Integer) response[0]).intValue(),
					(byte[]) response[1]);
			break;
		default:
			super.handleMessage(msg);
			break;
		}
	}

	@Override
	protected void sendResponseMessage(HttpResponse response) {
		Throwable error = null;
		byte[] responseBody = null;
		long result = -1;
		int statusCode = 0;
		// previousTime = System.currentTimeMillis();
		try {
			statusCode = response.getStatusLine().getStatusCode();
			Log.e("", "statusCode = " + statusCode);
			if (statusCode >= 400 && statusCode <= 499) {
				sendFailureMessage(new Exception("找不到文件  statusCode = "
						+ statusCode), "找不到文件  statusCode = " + statusCode);
				if (tempFile != null) {
					tempFile.delete();
				}
				if (file != null) {
					file.delete();
				}
				return;
			}
			if (statusCode >= 500 && statusCode <= 599) {
				sendFailureMessage(new Exception("服务器内部错误  statusCode = "
						+ statusCode), "服务器内部错误  statusCode = " + statusCode);
				if (tempFile != null) {
					tempFile.delete();
				}
				if (file != null) {
					file.delete();
				}
				return;
			}
			long contentLenght = response.getEntity().getContentLength();
			// -1的解决方式ContentLength 在手机访问的时候出现了问题，返回为-1
			if (contentLenght == -1) {
				contentLenght = response.getEntity().getContent().available();
			}
			totalSize = contentLenght + previousFileSize;

			TALogger.v(TAG, "totalSize: " + totalSize);

			if (file.exists() && totalSize == file.length()) {
				TALogger.v(TAG,
						"Output file already exists. Skipping download.");
				throw new FileAlreadyExistException(
						"Output file already exists. Skipping download.");
			} else if (tempFile.exists()) {
				// response.addHeader("Range", "bytes=" + tempFile.length()
				// +
				// "-");
				TALogger.v(TAG, "yahooo: "
						+ response.getEntity().getContentLength());
				previousFileSize = tempFile.length();

				TALogger.v(TAG, "File is not complete, download now.");
				TALogger.v(TAG, "File length:" + tempFile.length()
						+ " totalSize:" + totalSize);

			}
			outputStream = new ProgressReportingRandomAccessFile(tempFile, "rw");
			InputStream input = response.getEntity().getContent();
			startTimer();
			int bytesCopied = copy(input, outputStream);
			if ((previousFileSize + bytesCopied) != totalSize
					&& totalSize != -1 && !interrupt) {
				if (tempFile != null) {
					tempFile.delete();
				}
				if (file != null) {
					file.delete();
				}
				throw new IOException("Download incomplete: " + bytesCopied
						+ " != " + totalSize);
			}
			TALogger.v(TAG, "Download completed successfully.");
			result = bytesCopied;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			sendFailureMessage(e, responseBody);
			error = e;
		} catch (FileAlreadyExistException e) {
			// TODO Auto-generated catch block
			// sendSuccessMessage(statusCode, e.getMessage().getBytes());
			error = e;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			error = e;
			// sendFailureMessage(e, responseBody);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// sendFailureMessage(e, responseBody);
			error = e;
		}
		// 停止打印
		stopTimer();
		// 保证timer被关闭
		try {
			Thread.sleep(TIMERSLEEPTIME);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result == -1 || interrupt || error != null) {
			if (error != null) {
				Log.v(TAG, "Download failed." + error.getMessage());
				if (error instanceof FileAlreadyExistException) {
					sendSuccessMessage(statusCode, "下载成功！".getBytes());

				} else {
					sendFailureMessage(error, responseBody);
				}
			}
			return;
		}
		tempFile.renameTo(file);
		sendSuccessMessage(statusCode, "下载成功！".getBytes());
	}

	public int copy(InputStream input, RandomAccessFile out) throws IOException {

		if (input == null || out == null) {
			return -1;
		}

		byte[] buffer = new byte[BUFFER_SIZE];

		BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
		TALogger.v(TAG, "length" + out.length());
		int count = 0, n = 0;
		long errorBlockTimePreviousTime = -1, expireTime = 0;
		try {
			out.seek(out.length());
			previousTime = System.currentTimeMillis();
			while (!interrupt) {
				n = in.read(buffer, 0, BUFFER_SIZE);
				if (n == -1) {
					break;
				}
				out.write(buffer, 0, n);
				count += n;
				if (networkSpeed == 0) {
					if (errorBlockTimePreviousTime > 0) {
						expireTime = System.currentTimeMillis()
								- errorBlockTimePreviousTime;
						if (expireTime > TIME_OUT) {
							throw new ConnectTimeoutException(
									"connection time out.");
						}
					} else {
						errorBlockTimePreviousTime = System.currentTimeMillis();
					}
				} else {
					expireTime = 0;
					errorBlockTimePreviousTime = -1;
				}
			}
		} finally {

			try {
				out.close();
				// 无法关闭 inputstram
				// input.close();
				// in.close();
			} catch (IOException e) {
				// TODO: handle exception
			}
		}
		return count;

	}

	public File getTempFile() {
		// TODO Auto-generated method stub
		return tempFile;
	}

}