package com.smartapp.autostartmanager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

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
	 * 计算下载次数
	 */
	public static String convertDownloadTimes(long times) {
		if (times < 10000) {
			return times + "次下载";
		} else if (times >= 10000 && times < 100000000) {
			return ((int) (times / 10000.0 + 0.5)) + "万次下载";
		} else {
			return ((int) (times / 100000000.0 + 0.5)) + "亿次下载";
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

}
