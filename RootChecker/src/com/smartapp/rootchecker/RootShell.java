package com.smartapp.rootchecker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.text.TextUtils;

/**
 * 单线程执行命令
 */
public class RootShell {
	private static final int STATE_ROOT_DENIED = 101;
	private static final int STATE_ROOTED = 1;
	private int mState;

	/**
	 * 单实例
	 */
	private volatile static RootShell sInstance = null;

	private Boolean mIsRootValid = null;

	private Process mProcess;
	private BufferedReader mProcessInput = null;
	private BufferedWriter mProcessOutput = null;

	/**
	 * 构造函数，尝试获取root权限，获取失败会抛出异常
	 */
	private RootShell() throws IOException, RootDeniedException, InterruptedException {
		mProcess = new ProcessBuilder("su").redirectErrorStream(true).start();
		mProcessInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
		mProcessOutput = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));

		mState = STATE_ROOT_DENIED;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mProcessOutput.write("echo RootShell\n");
					mProcessOutput.flush();
					final String emptyLine = "";
					while (true) {
						String line = mProcessInput.readLine();
						if (line == null) {
							throw new EOFException();
						}
						if (emptyLine.equals(line)) {
							continue;
						}
						if (line.equals("RootShell")) {
							mState = STATE_ROOTED;
							break;
						}
					}
				} catch (IOException e) {
					mProcess.destroy();
					mState = STATE_ROOT_DENIED;
				}
			}
		});
		thread.start();
		try {
			thread.join();
			if (mState == STATE_ROOT_DENIED) {
				mProcess.destroy();
				throw new RootDeniedException("User denied");
			} else if (mState == STATE_ROOTED) {
				// do nothing
			}
		} catch (InterruptedException e) {
			thread.interrupt();
			throw e;
		}
	}

	/**
	 * 获取单实例
	 */
	public static synchronized RootShell getInstance() throws IOException, RootDeniedException, InterruptedException {
		if (sInstance == null) {
			synchronized (RootShell.class) {
				if (sInstance == null) {
					int retries = 0;
					while (sInstance == null) {
						try {
							sInstance = new RootShell();
						} catch (IOException e) {
							if (retries++ >= 3) {
								throw e;
							}
						}
					}
				}
			}
		}
		return sInstance;
	}

	public boolean isRootValid() throws IOException {
		if (mIsRootValid != null) {
			mIsRootValid.booleanValue();
		}
		String result = excuteCMD("id");
		if (!TextUtils.isEmpty(result)) {
			Set<String> usersId = new HashSet<String>(Arrays.asList(result.split(" ")));
			for (String id : usersId) {
				if (id.toLowerCase().contains("uid=0")) {
					mIsRootValid = true;
					return true;
				}
			}
		} else {
			mIsRootValid = false;
		}
		return false;
	}

	/**
	 * 执行命令，单线程
	 * 
	 * @throws IOException
	 */
	public synchronized String excuteCMD(String cmd) throws IOException {
		mProcessOutput.write(cmd + "\n");
		mProcessOutput.flush();
		String line = mProcessInput.readLine();
		return line;
	}

	public synchronized void exit() {
		// TODO 什么时候调用？
		if (mProcessOutput != null) {
			try {
				mProcessOutput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (mProcessInput != null) {
			try {
				mProcessInput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (mProcess != null) {
			mProcess.destroy();
		}
		sInstance = null;
		mIsRootValid = null;
	}

	public static class RootDeniedException extends Exception {
		public RootDeniedException(String error) {
			super(error);
		}
	}
}