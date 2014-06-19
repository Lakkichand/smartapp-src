package com.jiubang.go.backup.pro.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * @author GoBackup Dev Team
 */
public class LinuxShell {
	private Process mProgress;
	private DataOutputStream mDos;
	private DataInputStream mDis;
	private DataInputStream mErrorDis;
	private static final int M2048 = 2048;
	private byte[] mBuffer = new byte[M2048];
	private static final int M14 = 14;
	private static final int M10 = 10;
	private static final int M16 = 16;

	public LinuxShell() {
	}

	private void exit() {
		try {
			if (mDis != null) {
				mDis.close();
				mDis = null;
			}
			if (mErrorDis != null) {
				mErrorDis.close();
				mErrorDis = null;
			}
			if (mDos != null) {
				mDos.close();
				mDos = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mProgress != null) {
			// mProgress.destroy();
			mProgress = null;
		}
	}

	public boolean getRootAuthorityAndEnsureValid(Context context) {
		if (context == null) {
			return false;
		}

		boolean ret = false;
		int rc = 0;
		String appFileDir = Util.ensureFileSeparator(context.getFilesDir().getAbsolutePath());
		try {
			mProgress = Runtime.getRuntime().exec("su \n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			mDos.writeBytes("echo result=$? \n");

			String command = "";
			command += appFileDir;
			command += "backup cs";
			command += "\n";
			mDos.writeBytes(command);

			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			if ((rc = mErrorDis.available()) > 0) {
				// 在弹出的授权界面中，如果用户拒绝授权，会返回Permission denied错误
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					if (error.contains("Permission denied")) {
						ret = false;
					}
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					// 如果用户授权，则返回的结果中包含result=0
					String result = new String(mBuffer, 0, rc);
					if (result.contains("result=0") && result.contains("SU_NORMAL")) {
						ret = true;
					} else {
						ret = false;
					}
				}
			}

		} catch (IOException e) {
			// 如果用户手机没有root，会返回io错误
			e.printStackTrace();
			ret = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			ret = false;
		} finally {
			exit();
		}
		return ret;
	}

	public boolean checkSupportSu(String internalDataDir) {
		if (internalDataDir == null) {
			return false;
		}

		internalDataDir = Util.ensureFileSeparator(internalDataDir);

		boolean ret = false;
		int rc = 0;
		try {
			mProgress = Runtime.getRuntime().exec("su \n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			String command = "";
			command += internalDataDir;
			command += "backup cs ";
			command += "\n";

			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			if ((rc = mErrorDis.available()) > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					// Log.d("GOBackup", "LinuxShell : checkSupportSu error = "
					// + error);
					if (error.contains("Permission denied")) {
						ret = false;
					}
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					// 如果用户授权，则返回的结果中包含result=0
					String result = new String(mBuffer, 0, rc);
					// Log.d("GOBackup", "LinuxShell : checkSupportSu result = "
					// + result);
					if (result.contains("SU_NORMAL")) {
						ret = true;
					} else {
						ret = false;
					}
				}
			}

		} catch (IOException e) {
			// 如果用户手机没有root，会返回io错误
			e.printStackTrace();
			ret = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			ret = false;
		} finally {
			exit();
		}
		return ret;
	}

	/*
	 * public String[] querySystemPath(){ boolean ret = true; int rc = 0;
	 * String[] paths = null; try { mProgress =
	 * Runtime.getRuntime().exec("sh \n"); mDos = new
	 * DataOutputStream(mProgress.getOutputStream()); mDis = new
	 * DataInputStream(mProgress.getInputStream()); mErrorDis = new
	 * DataInputStream(mProgress.getErrorStream());
	 * mDos.writeBytes("echo $PATH \n"); mDos.writeBytes("exit \n");
	 * mDos.flush(); mProgress.waitFor(); if((rc = mErrorDis.available()) > 0){
	 * //在弹出的授权界面中，如果用户拒绝授权，会返回Permission denied错误 rc = mErrorDis.read(mBuffer,
	 * 0, mBuffer.length); if(rc > 0){ String error = new String(mBuffer, 0,
	 * rc); if(!error.equals("\n")){ ret = false; } } } rc = mDis.available();
	 * if(rc > 0){ rc = mDis.read(mBuffer, 0, mBuffer.length); if(rc != -1){
	 * //正常返回，分析结果 String result = null; if(mBuffer[rc-1] == '\n'){ result = new
	 * String(mBuffer, 0, rc -1); }else{ result = new String(mBuffer, 0, rc); }
	 * paths = result.split(":"); } } } catch (IOException e) {
	 * //如果用户手机没有root，会返回io错误 e.printStackTrace(); ret = false; } catch
	 * (InterruptedException e) { e.printStackTrace(); ret = false; } finally {
	 * exit(); } return paths; }
	 */

	/**
	 * 修改文件的属性 如果修改应用本身目录的文件是不需要root权限，如果修改非本应用的文件，需要root权限
	 * 如果修改成功，标准输出和错误输出都没有数据输出，如果错误，错误输出会有输出，根据这个判断是否执行成功
	 * 
	 * @param mod
	 * @param fileNames
	 * @param needRoot
	 * @return
	 */
	public boolean chmod(String mod, String[] fileNames, boolean needRoot) {
		boolean ret = true;
		int rc = 0;
		try {
			String command = "chmod " + mod + " ";
			for (String fileName : fileNames) {
				command += fileName + " ";
			}
			command += "\n";

			if (needRoot) {
				mProgress = new ProcessBuilder("su").start();
			} else {
				mProgress = new ProcessBuilder("sh").start();
			}
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			rc = mErrorDis.available();
			if (rc > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					if (error.contains("Failure") || error.contains("Permission denied")
							|| error.contains("No such file or directory")) {
						ret = false;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			exit();
		}
		return ret;
	}

	/**
	 * 备份 apk和数据
	 * 
	 * @param internalDataFilePath
	 *            GOBackup内部data目录路径
	 * @param apkFilePath
	 *            apk文件路径
	 * @param dataFilePath
	 *            数据文件路径
	 * @param descPath
	 *            目的文件路径
	 * @param packageName
	 *            待备份apk包名
	 * @return
	 */
	public boolean backupApp(String internalDataFilePath, String apkFilePath, String dataFilePath,
			String descPath, String packageName, boolean needBackupData) {
		boolean ret = true;
		if (internalDataFilePath == null) {
			return false;
		}
		if (!internalDataFilePath.endsWith(File.separator)) {
			internalDataFilePath += File.separator;
		}

		int rc = 0;
		if (needBackupData && dataFilePath == null) {
			return false;
		}

		try {
			mProgress = Runtime.getRuntime().exec("su root \n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			String command = "";
			command += internalDataFilePath + "backup ba ";
			if (needBackupData) {
				command += "-d " + dataFilePath + " ";
			}
			command += internalDataFilePath + "busybox ";
			command += apkFilePath + " ";
			command += descPath + " ";
			command += packageName + "\n";
			Log.d("GOBackup", "command = " + command);
			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			rc = mErrorDis.available();
			if (rc > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					// Log.d("GOBackup", "backup : error = " + error);
					if (error.contains("backup failure") || error.contains("Permission denied")) {
						ret = false;
					}
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					String result = new String(mBuffer, 0, rc);
					// Log.d("GOBackup", "backup : result = " + result);
					if (result.contains("backup success")) {
						ret = true;
					} else {
						ret = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			exit();
		}
		return ret;
	}

	/**
	 * 恢复应用以及数据
	 * 
	 * @param internalDataFilePath
	 *            备份的内部files文件路径
	 * @param apkFileFullPath
	 *            apk文件路径 如果为空，则不恢复apk
	 * @param dataFileFullPath
	 *            数据文件路径，如果为空，则不恢复数据
	 * @param dataDirPath
	 *            备份的内部data路径，如果为空，则不恢复数据
	 * @return
	 */
	public boolean restoreApp(String internalDataFilePath, String apkFileFullPath,
			String dataFileFullPath, String dataDirPath, boolean isPrivateApp) {
		boolean ret = true;
		int rc = 0;
		if (internalDataFilePath == null/* || apkFileFullPath == null */) {
			return false;
		}
		if (!internalDataFilePath.endsWith(File.separator)) {
			internalDataFilePath += File.separator;
		}
		try {
			mProgress = Runtime.getRuntime().exec("su -\n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			boolean sdk14 = Build.VERSION.SDK_INT >= M14 ? true : false;
			String command = "";
			command += internalDataFilePath + "backup ra ";
			if (apkFileFullPath != null) {
				command += "-a " + apkFileFullPath + " ";
			}
			if (sdk14) {
				command += "-D LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH") + " ";
			}
			if (dataFileFullPath != null && dataDirPath != null) {
				command += "-i " + dataDirPath + " -d " + dataFileFullPath + " ";
			}
			if (isPrivateApp) {
				command += "-l ";
			}
			command += internalDataFilePath + "busybox";
			command += " \n";
			Log.d("GOBackup", "LinuxShell : command = " + command);
			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			rc = mErrorDis.available();
			if (rc > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					// Log.d("GOBackup", "LinuxShell : error = " + error);
					if (error.contains("restore failure") || error.contains("Permission denied")) {
						ret = false;
					}
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					String result = new String(mBuffer, 0, rc);
					// Log.d("GOBackup", "LinuxShell : result = " + result);
					if (result.contains("restore success")) {
						ret = true;
					} else {
						ret = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			exit();
		}
		return ret;
	}

	/**
	 * 检查是否支持内置busybox（待优化）
	 * 
	 * @return
	 */
	public boolean checkSupportInternalBusybox(String internalBusyBoxFilePath) {
		boolean ret = false;
		int rc = 0;
		if (internalBusyBoxFilePath == null) {
			return false;
		}
		try {
			mProgress = Runtime.getRuntime().exec("sh \n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			internalBusyBoxFilePath = Util.ensureFileSeparator(internalBusyBoxFilePath);
			String command = internalBusyBoxFilePath + "busybox \n";
			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			if ((rc = mErrorDis.available()) > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					// Log.d("GOBackup",
					// "checkSupportInternalBusybox : error = " + error);
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					String result = new String(mBuffer, 0, rc);
					if (result.contains("Usage: busybox")) {
						ret = true;
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			ret = false;
		} finally {
			exit();
		}
		return ret;
	}

	/**
	 * 通过执行tar命令，判断是否支持备份恢复数据
	 * 
	 * @param ctx
	 * @return
	 */
	/*
	 * public boolean checkSupportStandardTarCmd(Context ctx){ boolean ret =
	 * false; int rc = 0; String testTempDesPath =
	 * Util.getApplicationDataDirPath(ctx, ctx.getPackageName());
	 * testTempDesPath = Util.EnsureFileSeparator(testTempDesPath);
	 * testTempDesPath += "test.tar.gz"; String testTempSrcPath =
	 * ctx.getFilesDir().getAbsolutePath(); try { mProgress =
	 * Runtime.getRuntime().exec("sh \n"); mDos = new
	 * DataOutputStream(mProgress.getOutputStream()); mDis = new
	 * DataInputStream(mProgress.getInputStream()); mErrorDis = new
	 * DataInputStream(mProgress.getErrorStream()); String command = "tar -zcf "
	 * + testTempDesPath + " " + testTempSrcPath + " -C / --exclude lib \n"; //
	 * Log.d("GOBackup", "checkSupportStandardTarCmd : command = " + command);
	 * mDos.writeBytes(command);
	 * mDos.writeBytes("echo checkSupportStandardTarCmd result = $? \n");
	 * mDos.writeBytes("exit \n"); mDos.flush(); mProgress.waitFor(); if((rc =
	 * mErrorDis.available()) > 0){ rc = mErrorDis.read(mBuffer, 0,
	 * mBuffer.length); if(rc > 0){ String error = new String(mBuffer, 0, rc);
	 * // Log.d("GOBackup", "checkSupportStandardTarCmd : error = " + error);
	 * if(error.contains("Permission denied")){ ret = false; } } } rc =
	 * mDis.available(); if(rc > 0){ rc = mDis.read(mBuffer, 0, mBuffer.length);
	 * if(rc != -1){ String result = new String(mBuffer, 0, rc); //
	 * Log.d("GOBackup", "checkSupportStandardTarCmd : result = " + result);
	 * if(result.contains("checkSupportStandardTarCmd result = 0")){ ret = true;
	 * } } } } catch (IOException e) { e.printStackTrace(); ret = false; } catch
	 * (InterruptedException e) { e.printStackTrace(); ret = false; } finally {
	 * File tempFile = new File(testTempDesPath); if(tempFile.exists()){
	 * tempFile.delete(); } exit(); } return ret; }
	 */

	public String backupWifi(String internalDataDir, String descDir) {
		int rc = 0;
		if (internalDataDir == null || descDir == null) {
			return null;
		}
		String wifiPath = null;

		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		try {
			mProgress = Runtime.getRuntime().exec("su \n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			String command = "";
			command += internalDataDir + "backup bw ";
			command += "-b ";
			command += internalDataDir + "busybox" + " ";
			command += descDir;
			command += "\n";
			Log.d("GOBackup", "LinuxShell : command = " + command);
			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			if ((rc = mErrorDis.available()) > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					Log.d("GOBackup", "backupWifi : error = " + error);
					if (error.contains("wfii backup error")) {
						wifiPath = null;
					}
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					String result = new String(mBuffer, 0, rc);
					Log.d("GOBackup", "backupWifi : result = " + result);
					if (result.contains("wifi backup success")) {
						String[] allResults = result.split("\n");
						if (allResults != null) {
							for (String str : allResults) {
								if (str.startsWith("WIFI_PATH=")) {
									wifiPath = str.substring(M10);
									Log.d("GOBackup", "backwifi : wifiPath = " + wifiPath);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			exit();
		}
		return wifiPath;
	}

	public boolean restoreWifi(String internalDataDir, String src, String desc) {
		if (internalDataDir == null || src == null || desc == null) {
			return false;
		}

		int rc = 0;
		boolean ret = false;
		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		try {
			mProgress = Runtime.getRuntime().exec("su \n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			String command = "";
			command += internalDataDir + "backup bw ";
			command += "-r " + desc + " ";
			command += internalDataDir + "busybox" + " ";
			command += src;
			command += "\n";
			Log.d("GOBackup", "LinuxShell : restoreWifi : command = " + command);
			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			if ((rc = mErrorDis.available()) > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					Log.d("GOBackup", "restoreWifi : error = " + error);
					if (error.contains("wifi restore error")) {
						ret = false;
					}
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					String result = new String(mBuffer, 0, rc);
					Log.d("GOBackup", "restoreWifi : result = " + result);
					if (result.contains("wifi restore success")) {
						ret = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			ret = false;
		} finally {
			exit();
		}
		return ret;
	}

	public String backupAccount(String internalDataDir, String desc) {
		if (internalDataDir == null || desc == null) {
			return null;
		}

		int rc = 0;
		String accountDbPath = null;
		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		try {
			mProgress = Runtime.getRuntime().exec("su \n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			String command = "";
			command += internalDataDir + "backup bc ";
			command += "-b ";
			command += internalDataDir + "busybox" + " ";
			command += desc;
			command += "\n";
			Log.d("GOBackup", "LinuxShell : backupAccount : command = " + command);
			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			if ((rc = mErrorDis.available()) > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					Log.d("GOBackup", "backupAccount : error = " + error);
					if (error.contains("backup account error")) {
						accountDbPath = null;
					}
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					String result = new String(mBuffer, 0, rc);
					Log.d("GOBackup", "backupAccount : result = " + result);
					if (result.contains("backup account success")) {
						String[] allResults = result.split("\n");
						if (allResults != null) {
							for (String str : allResults) {
								if (str.startsWith("ACCOUNT_DB_PATH=")) {
									accountDbPath = str.substring(M16);
									Log.d("GOBackup", "backupAccount : accountDbPath = "
											+ accountDbPath);
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			accountDbPath = null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			accountDbPath = null;
		} finally {
			exit();
		}
		return accountDbPath;
	}

	public boolean restoreAccount(String internalDataDir, String src, String desc) {
		if (internalDataDir == null || src == null || desc == null) {
			return false;
		}

		int rc = 0;
		boolean ret = false;
		internalDataDir = Util.ensureFileSeparator(internalDataDir);
		try {
			mProgress = Runtime.getRuntime().exec("su \n");
			mDos = new DataOutputStream(mProgress.getOutputStream());
			mDis = new DataInputStream(mProgress.getInputStream());
			mErrorDis = new DataInputStream(mProgress.getErrorStream());

			String command = "";
			command += internalDataDir + "backup bc ";
			command += "-r " + desc + " ";
			command += internalDataDir + "busybox" + " ";
			command += src;
			command += "\n";
			Log.d("GOBackup", "LinuxShell : restoreAccount : command = " + command);
			mDos.writeBytes(command);
			mDos.writeBytes("exit \n");
			mDos.flush();
			mProgress.waitFor();

			if ((rc = mErrorDis.available()) > 0) {
				rc = mErrorDis.read(mBuffer, 0, mBuffer.length);
				if (rc > 0) {
					String error = new String(mBuffer, 0, rc);
					Log.d("GOBackup", "restoreAccount : error = " + error);
					if (error.contains("restore account error")) {
						ret = false;
					}
				}
			}

			rc = mDis.available();
			if (rc > 0) {
				rc = mDis.read(mBuffer, 0, mBuffer.length);
				if (rc != -1) {
					String result = new String(mBuffer, 0, rc);
					Log.d("GOBackup", "restoreAccount : result = " + result);
					if (result.contains("restore account success")) {
						ret = true;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			ret = false;
		} finally {
			exit();
		}
		return ret;
	}
}
