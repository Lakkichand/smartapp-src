package com.zhidian.wifibox.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * 文件工具类
 * 
 * @author xiedezhi
 * 
 */
public class FileUtil {

	public static final String WIFIBOX_DIR = Environment
			.getExternalStorageDirectory() + "/MIBAO/";
	public static final String WIFIBOX_IMAGE = WIFIBOX_DIR + "image/";
	public static final String WIFIBOX_DOWN = WIFIBOX_DIR + "download/";
	public static final String WIFIBOX_CALE = WIFIBOX_DIR + "wifiboxCale";

	public static File createNewFile(String path, boolean append) {
		File newFile = new File(path);
		if (!append) {
			if (newFile.exists()) {
				newFile.delete();
			}
		}
		if (!newFile.exists()) {
			try {
				File parent = newFile.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				newFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return newFile;
	}

	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return dir.length();
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file); // 如果遇到目录则通过递归调用继续统计
			}
		}
		return dirSize;
	}

	/**
	 * 保存数据到指定文件
	 */
	public static boolean saveByteToFile(final byte[] byteData,
			final String filePathName) {
		boolean result = false;
		try {
			File newFile = createNewFile(filePathName, false);
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			fileOutputStream.write(byteData);
			fileOutputStream.flush();
			fileOutputStream.close();
			result = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 读取文件数据
	 */
	public static byte[] getByteFromFile(final String filePathName) {
		byte[] bs = null;
		try {
			File newFile = new File(filePathName);
			FileInputStream fileInputStream = new FileInputStream(newFile);
			DataInputStream dataInputStream = new DataInputStream(
					fileInputStream);
			BufferedInputStream inPutStream = new BufferedInputStream(
					dataInputStream);
			bs = new byte[(int) newFile.length()];
			inPutStream.read(bs);
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bs;
	}

	// 获取图片路径

	public static String getImagePath() {
		creaFile();
		return WIFIBOX_IMAGE;
	}

	// 从url中提取图片名

	public static String getDownIconPath(String url) {
		if ("".equals(url)) {
			return "";
		}
		String imageName = url.substring(url.lastIndexOf("/") + 1);
		imageName = imageName.substring(0, imageName.lastIndexOf("."));
		return imageName;
	}

	public static void creaFile() {
		try {
			File file = new File(WIFIBOX_DIR);
			if (!file.exists()) {
				file.mkdir();
			}
			File file2 = new File(WIFIBOX_IMAGE);
			if (!file2.exists()) {
				file2.mkdir();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 保存位图到sd卡目录下
	 * 
	 * @param bitmap
	 *            ：位图资源
	 * @param filePathName
	 *            ：待保存的文件完整路径名
	 * @param iconFormat
	 *            ：图片格式
	 * @return true for 保存成功，false for 保存失败。
	 */
	public static boolean saveBitmapToSDFile(final Bitmap bitmap,
			final String filePathName, CompressFormat iconFormat) {
		boolean result = false;
		try {
			createNewFile(filePathName, false);
			OutputStream outputStream = new FileOutputStream(filePathName);
			result = bitmap.compress(iconFormat, 100, outputStream);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * 獲取本地圖片
	 * 
	 * @param iamgeName
	 * @return
	 */
	public static Bitmap getSDBitmap(String iamgeName) {
		File imageFile = new File(FileUtil.getImagePath(), iamgeName);
		Bitmap bitmap = null;
		if (imageFile.exists()) {
			try {
				bitmap = BitmapFactory.decodeStream(new FileInputStream(
						imageFile));
				if (bitmap != null) {
					// Log.d(TAG, "imagepATH:找到了");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();

			}
		}
		return bitmap;
	}

	/**
	 * sd卡是否可读写
	 */
	public static boolean isSDCardAvaiable() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * 创建数据库名称
	 * 
	 * @return
	 */
	public static String getDbName() {
		String dbName = "";
		if (isSDCardAvaiable() && getSDCardFreeSize() >= 100) {
			dbName = WIFIBOX_DIR + "wifibox.db"; // 数据库名
		} else {
			dbName = "wifibox.db";
		}
		Log.e("", "dbName = " + dbName);
		return dbName;
	}

	/**
	 * 返回SD卡空闲大小
	 */
	public static long getSDCardFreeSize() {
		try {
			// 取得SD卡文件路径
			File path = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(path.getPath());
			// 获取单个数据块的大小(Byte)
			long blockSize = sf.getBlockSize();
			// 空闲的数据块的数量
			long freeBlocks = sf.getAvailableBlocks();
			// 返回SD卡空闲大小
			long ret = (long) ((freeBlocks * blockSize) / 1024.0 / 1024.0 + 0.5); // 单位MB
			Log.e("", "freesize = " + ret);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.e("", "freesize = 0");
		return 0;
	}

	/**
	 * 指定路径文件是否存在
	 */
	public static boolean isFileExist(String filePath) {
		boolean result = false;
		try {
			File file = new File(filePath);
			result = file.exists();
			file = null;
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 转换文件大小
	 * 
	 * @param size
	 *            单位KB
	 */
	public static String convertFileSize(long size) {
		if (size > 1073741824) {
			return (new BigDecimal(size / 1073741824.0)).setScale(2,
					BigDecimal.ROUND_HALF_UP).doubleValue()
					+ "TB";
		} else if (size > 1048576) {
			return (new BigDecimal(size / 1048576.0)).setScale(2,
					BigDecimal.ROUND_HALF_UP).doubleValue()
					+ "GB";
		} else if (size > 1024) {
			return (new BigDecimal(size / 1024.0)).setScale(2,
					BigDecimal.ROUND_HALF_UP).doubleValue()
					+ "MB";
		} else {
			return size + "KB";
		}
	}
	
	/**
	 * 根据byte大小转换单位为MB/G/KB
	 * @param bytes
	 * @return
	 */
	public static String bytes2kb(long bytes) {  
        BigDecimal filesize = new BigDecimal(bytes);  
        BigDecimal megabyte = new BigDecimal(1024 * 1024);  
        float returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)  
                .floatValue();  
        if (returnValue > 1 && returnValue < 1024)  
            return (returnValue + "MB");
        BigDecimal gigabyte = new BigDecimal(1024 * 1024 * 1024);
        returnValue = filesize.divide(gigabyte, 2, BigDecimal.ROUND_UP).floatValue();
        if (returnValue > 1) {
        	return (returnValue + "G");
        }
        BigDecimal kilobyte = new BigDecimal(1024);  
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)  
                .floatValue();  
        return (returnValue + "KB");  
    }

	/**
	 * 计算下载次数
	 */
	public static String convertDownloadTimes(long times) {
		if (times < 10000) {
			return times + "次";
		} else if (times >= 10000 && times < 100000000) {
			return ((int) (times / 10000.0 + 0.5)) + "万次";
		} else {
			return ((int) (times / 100000000.0 + 0.5)) + "亿次";
		}
	}

	/**
	 * 转换时间长度
	 */
	public static String convertTime(int second) {
		if (second > 3600) {
			return (new BigDecimal(second / 3600.0)).setScale(1,
					BigDecimal.ROUND_HALF_UP).doubleValue()
					+ "小时";
		} else if (second > 60) {
			return (new BigDecimal(second / 60.0)).setScale(0,
					BigDecimal.ROUND_HALF_UP).doubleValue()
					+ "分钟";
		} else {
			return second + "秒";
		}
	}
	
	/**
	 * 把int类型的毫秒数转换成时间格式
	 * @param milliscond
	 * @return
	 */
	public static String milliscond2Time(int milliscond) {
		Date date = new Date();
	     SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
	     sdf.setTimeZone(TimeZone.getTimeZone("GMT+0"));
	     date.setTime(milliscond);
	     return sdf.format(date);
	}
	
	/**
	 * 格式化时间，将其变成00:00的形式
	 */
	public static String formatTime(int time) {
		int secondSum = time / 1000;
		int minute = secondSum / 60;
		int second = secondSum % 60;

		String result = "";
		if (minute < 10)
			result = "0";
		result = result + minute + ":";
		if (second < 10)
			result = result + "0";
		result = result + second;

		return result;
	}

	public static void copyFile(String srcStr, String decStr) {
		// 前提
		File srcFile = new File(srcStr);
		if (!srcFile.exists()) {
			return;
		}
		File decFile = new File(decStr);
		if (!decFile.exists()) {
			File parent = decFile.getParentFile();
			parent.mkdirs();

			try {
				decFile.createNewFile();

			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(srcFile);
			output = new FileOutputStream(decFile);
			byte[] data = new byte[4 * 1024]; // 4k
			while (true) {
				int len = input.read(data);
				if (len <= 0) {
					break;
				}
				output.write(data);
			}
		} catch (Exception e) {
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (Exception e2) {
				}
			}
			if (null != output) {
				try {
					output.close();
				} catch (Exception e2) {
				}
			}
		}
	}

	/**
	 * 按行读取字符串
	 */
	public static List<String> readLine(String src) {
		try {
			if (src == null) {
				return null;
			}
			BufferedReader rd = new BufferedReader(new StringReader(src));
			String str = null;
			List<String> ret = new ArrayList<String>();
			while ((str = rd.readLine()) != null) {
				if (!TextUtils.isEmpty(str)) {
					ret.add(str);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	public static boolean deleteFile(String sPath) {
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false。
	 */
	public static boolean DeleteFolder(String sPath) {
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回 false
			return false;
		} else {
			// 判断是否为文件
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}
	
	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}
	
	/** 通知系统扫描SDCard，及时更新媒体库 */
	public static void scanSdCard(Context mContext) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
					Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
		} else {
			mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
					Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
		}
		
		// 4.4之后的系统无法通过广播更新，所以使用下面这个方法扫描更新
		MediaScannerConnection.scanFile(mContext, new String[] { 
				Environment.getExternalStorageDirectory().getAbsolutePath() }, null, null);
	}
	
	public static String getFileExtension(File file)
	{
		return getFileExtension(file.getName());
	}
	
	/**
	 * Gets extension of the file name excluding the . character
	 */
	public static String getFileExtension(String fileName)
	{
		if (fileName.contains("."))
			return fileName.substring(fileName.lastIndexOf('.')+1);
		else 
			return "";
	}
	
	/**
	 * 获取文件格式
	 */
	public static String getFileMimeType(File file)
	{
		String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(file));
		if (type == null) return "*/*";
		return type;
	}

	/**
	 * @Title: getExtSDCardPaths
	 * @Description: to obtain storage paths, the first path is theoretically
	 *               the returned value of
	 *               Environment.getExternalStorageDirectory(), namely the
	 *               primary external storage. It can be the storage of internal
	 *               device, or that of external sdcard. If paths.size() >1,
	 *               basically, the current device contains two type of storage:
	 *               one is the storage of the device itself, one is that of
	 *               external sdcard. Additionally, the paths is directory.
	 * @return List<String>
	 * @throws IOException
	 */
	public static List<String> getExtSDCardPaths() {
		List<String> paths = new ArrayList<String>();
		String extFileStatus = Environment.getExternalStorageState();
		File extFile = Environment.getExternalStorageDirectory();
		if (extFileStatus.equals(Environment.MEDIA_MOUNTED) && extFile.exists()
				&& extFile.isDirectory() && extFile.canWrite()) {
			paths.add(extFile.getAbsolutePath());
		}
		Process process = null;
		try {
			process = Runtime.getRuntime().exec("mount");
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			int mountPathIndex = 1;
			while ((line = br.readLine()) != null) {
				// format of sdcard file system: vfat/fuse
				if ((!line.contains("fat") && !line.contains("fuse") && !line
						.contains("storage"))
						|| line.contains("secure")
						|| line.contains("asec")
						|| line.contains("firmware")
						|| line.contains("shell")
						|| line.contains("obb")
						|| line.contains("legacy") || line.contains("data")) {
					continue;
				}
				String[] parts = line.split(" ");
				int length = parts.length;
				if (mountPathIndex >= length) {
					continue;
				}
				String mountPath = parts[mountPathIndex].trim();
				if (!mountPath.contains("/") || mountPath.contains("data")
						|| mountPath.contains("Data")) {
					continue;
				}
				File mountRoot = new File(mountPath);
				if (!mountRoot.exists() || !mountRoot.isDirectory()
						|| !mountRoot.canWrite()) {
					continue;
				}
				boolean equalsToPrimarySD = mountPath.equals(extFile
						.getAbsolutePath());
				if (equalsToPrimarySD) {
					continue;
				}
				paths.add(mountPath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				process.destroy();
			} catch (Exception e) {
			}
		}
		return paths;
	}

}
