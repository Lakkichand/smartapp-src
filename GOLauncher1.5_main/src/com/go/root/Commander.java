package com.go.root;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * root命令执行类 在同一个进程里只需要请求一次root权限，避免多次弹出权限请求框干扰用户
 * 
 * @author luopeihuan
 * 
 */
public class Commander {
	private static Commander sInstance = null;

	public static Commander getInstance() {
		if (sInstance == null) {
			sInstance = new Commander();
		}
		return sInstance;
	}

	private Commander() {
	}

	private Process mProcess;
	private DataOutputStream mOut;
	private DataInputStream mIn;
	private boolean mHasPermission = false;

	/**
	 * 关闭进程，在不需要使用时请关闭
	 */
	public void close() {
		try {
			if (mOut != null) {
				mOut.writeBytes("exit\n");
				mOut.flush();
				mOut.close();
				mOut = null;
			}

			if (mIn != null) {
				mIn.close();
				mIn = null;
			}
			if (mProcess != null) {
				mProcess.destroy();
			}
		} catch (Exception e) {
		}
		mHasPermission = false;
	}

	/**
	 * 请求root权限，在相同的进程中仅需要请求一次。
	 * 
	 * @return
	 */
	public boolean requireRootAccess() {
		try {
			if (mProcess == null) {
				mProcess = Runtime.getRuntime().exec("su");
				mOut = new DataOutputStream(mProcess.getOutputStream());
				mIn = new DataInputStream(mProcess.getInputStream());
				mHasPermission = false;
			}

			if (mOut == null || mIn == null) {
				return false;
			}

			String line = null;
			if (!mHasPermission) {
				mOut.writeBytes("id\n");
				mOut.flush();

				line = mIn.readLine();
				if (line == null || !line.toLowerCase().contains("uid=0")) {
					close();
					return false;
				}
				mHasPermission = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return mHasPermission;
	}

	/**
	 * 执行命令
	 * 
	 * @param cmds
	 * @return
	 */
	public boolean exec(String[] cmds) {
		boolean hasAccess = requireRootAccess();
		if (hasAccess) {
			try {
				// 执行命令
				for (String single : cmds) {
					// Log.i("luoph", "cmd: " + single);
					mOut.writeBytes(single + "\n");
					mOut.flush();
					// line = mIn.readLine();
					// Log.i("luoph", "exec output: " + line);
				}
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}
