package com.go.root;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import android.util.Log;

import com.go.root.Executer.Result;

/**
 * root工具类
 * 
 * @author luopeihuan
 * 
 *         <ol>
 *         主要接口：
 *         <li> {@link #isRootAvailable()}判断手机是否已经root</li>
 *         <li> {@link #isAccessGiven()}判断当前应用是否已经获取root权限</li>
 *         <li> {@link #sendShell(String[], int, int)} 、
 *         {@link #sendShell(String[], int, Result, int)} 和
 *         {@link #sendShell(String[], int, Result, boolean, int)} 通过su执行相应的命令</li>
 *         </ol>
 */
public class RootUtils {
	public static final boolean DEBUG = false;
	private static final String TAG = "RootUtils";

	protected static boolean sAccessGiven = false;
	protected static String[] sSpace;
	protected static String sBusyboxVersion;
	protected static Set<String> sPath;
	protected static int sTimeout = 10000;
	public static List<String> sLastFoundBinaryPaths = new ArrayList<String>();

	public static int sShellDelay = 0;

	public static void log(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void log(String tag, String msg) {
		if (DEBUG) {
			Log.d(tag, msg);
		}
	}

	public static void log(String tag, String msg, Throwable th) {
		if (DEBUG) {
			Log.d(tag, msg, th);
		}
	}

	/**
	 * @return <code>true</code> if su was found.
	 */
	public static boolean isRootAvailable() {
//		return SnapShotManager.isRoot();
		return findBinary("su");
	}

	/**
	 * This will return the environment variable $PATH
	 * 
	 * @return <code>Set<String></code> A Set of Strings representing the
	 *         environment variable $PATH
	 * @throws Exception
	 *             if we cannot return the $PATH variable
	 */
	public static Set<String> getPath() throws Exception {
		if (sPath != null) {
			return sPath;
		} else {
			if (returnPath()) {
				return sPath;
			} else {
				throw new Exception();
			}
		}
	}

	protected static boolean returnPath() throws TimeoutException {
		File tmpDir = new File("/data/local/tmp");
		try {
			if (!tmpDir.exists()) {
				sendShell(new String[] { "mkdir /data/local/tmp" }, 0, sTimeout);
			}

			sPath = new HashSet<String>();
			// Try to read from the file.
			LineNumberReader lnr = null;
			sendShell(new String[] { "dd if=/init.rc of=/data/local/tmp/init.rc",
					"chmod 0777 /data/local/tmp/init.rc" }, 0, sTimeout);

			lnr = new LineNumberReader(new FileReader("/data/local/tmp/init.rc"));
			String line;
			while ((line = lnr.readLine()) != null) {
				log(line);
				if (line.contains("export PATH")) {
					int tmp = line.indexOf("/");
					sPath = new HashSet<String>(Arrays.asList(line.substring(tmp).split(":")));
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			Log.e(TAG, "returnPath Error: ", e);
			return false;
		}
	}

	public static boolean findBinary(String binaryName) {

		boolean found = false;
		sLastFoundBinaryPaths.clear();

		try {
			for (String paths : getPath()) {
				File file = new File(paths + "/" + binaryName);
				if (file.exists()) {
					log(binaryName + " was found here: " + paths);
					sLastFoundBinaryPaths.add(paths);
					found = true;
				} else {
					log(binaryName + " was NOT found here: " + paths);
				}
			}
		} catch (TimeoutException ex) {
			Log.i(TAG, "findBinary " + binaryName + " error", ex);
		} catch (Exception e) {
			Log.i(TAG, binaryName
					+ " was not found, more information MAY be available with Debugging on.", e);
		}

		if (!found) {
			log("Trying second method");
			log("Checking for " + binaryName);
			String[] places = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/",
					"/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/" };
			for (String where : places) {
				File file = new File(where + binaryName);
				if (file.exists()) {
					log(binaryName + " was found here: " + where);
					sLastFoundBinaryPaths.add(where);
					found = true;
				} else {
					log(binaryName + " was NOT found here: " + where);
				}
			}
		}

		return found;
	}

	/**
	 * Sends several shell command as su, unless useRoot is set to false
	 * 
	 * @param commands
	 *            array of commands to send to the shell
	 * @param sleepTime
	 *            time to sleep between each command, delay.
	 * @param timeout
	 *            How long to wait before throwing TimeoutException, sometimes
	 *            when running root commands on certain devices or roms ANR's
	 *            may occur because a process never returns or readline never
	 *            returns. This allows you to protect your application from
	 *            throwing an ANR. if you pass -1, then the default timeout is 5
	 *            minutes.
	 * @return a LinkedList containing each line that was returned by the shell
	 *         after executing or while trying to execute the given commands.
	 *         You must iterate over this list, it does not allow random access,
	 *         so no specifying an index of an item you want, not like you're
	 *         going to know that anyways.
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public static List<String> sendShell(String[] commands, int sleepTime, int timeout)
			throws IOException, RootToolsException, TimeoutException {
		return sendShell(commands, sleepTime, null, timeout);
	}

	/**
	 * Sends several shell command as su (attempts to)
	 * 
	 * @param commands
	 *            array of commands to send to the shell
	 * @param sleepTime
	 *            time to sleep between each command, delay.
	 * @param result
	 *            injected result object that implements the Result class
	 * @param timeout
	 *            How long to wait before throwing TimeoutException, sometimes
	 *            when running root commands on certain devices or roms ANR's
	 *            may occur because a process never returns or readline never
	 *            returns. This allows you to protect your application from
	 *            throwing an ANR. if you pass -1, then the default timeout is 5
	 *            minutes.
	 * @return a <code>LinkedList</code> containing each line that was returned
	 *         by the shell after executing or while trying to execute the given
	 *         commands. You must iterate over this list, it does not allow
	 *         random access, so no specifying an index of an item you want, not
	 *         like you're going to know that anyways.
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TimeoutException
	 */
	public static List<String> sendShell(String[] commands, int sleepTime, Result result,
			int timeout) throws IOException, RootToolsException, TimeoutException {
		return sendShell(commands, sleepTime, result, false, timeout);
	}

	public static List<String> sendShell(String[] commands, int sleepTime, Result result,
			boolean useRoot, int timeout) throws IOException, RootToolsException, TimeoutException {
		return new Executer().sendShell(commands, sleepTime, result, useRoot, timeout);
	}

	/**
	 * @return <code>true</code> if your app has been given root access.
	 * @throws TimeoutException
	 *             if this operation times out. (cannot determine if access is
	 *             given)
	 */
	public static boolean isAccessGiven() {
		try {
			sShellDelay = 500;
			log("Checking for Root access");
			sAccessGiven = false;
			sendShell(new String[] { "id" }, 0, sTimeout);

			if (sAccessGiven) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			sShellDelay = 0;
		}
	}
}
