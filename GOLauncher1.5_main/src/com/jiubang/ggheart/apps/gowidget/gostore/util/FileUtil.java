package com.jiubang.ggheart.apps.gowidget.gostore.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;

import com.jiubang.ggheart.launcher.LauncherEnv;

public class FileUtil {
	/**
	 * 将图片数据存入sd卡文件中
	 * 
	 * @author huyong
	 * @param iconByte
	 *            待存入的文件路径
	 * @param iconType
	 *            图片类型，传入null，则默认以png为后缀
	 * @return 存入后的文件路径，存入失败，则返回null
	 */
	public static String saveIconToSDFile(final byte[] iconByte, String iconType) {
		Random random = new Random();
		int randomId = random.nextInt();
		String string2 = String.valueOf(randomId);
		String string3 = iconType;
		if (string3 == null) {
			string3 = ".png";
		}
		String pathString = LauncherEnv.Path.GOSTORE_ICON_PATH + string2 + string3;
		boolean result = saveByteToSDFile(iconByte, pathString);
		if (result) {
			return pathString;
		} else {
			return null;
		}
	}

	/**
	 * 保存位图到通用图片库中
	 * 
	 * @author huyong
	 * @param bitmap
	 *            ：位图资源
	 * @param fileName
	 *            ：待保存文件名
	 * @param iconFormat
	 *            ：图片格式
	 * @return true for 保存成功，false for 保存失败。
	 */
	public static boolean saveBitmapToCommonIconSDFile(final Bitmap bitmap, final String fileName,
			CompressFormat iconFormat) {
		String filePathName = LauncherEnv.Path.GOSTORE_ICON_PATH;
		filePathName += fileName;
		return saveBitmapToSDFile(bitmap, filePathName, iconFormat);

	}

	/**
	 * 保存位图到sd卡目录下
	 * 
	 * @author huyong
	 * @param bitmap
	 *            ：位图资源
	 * @param filePathName
	 *            ：待保存的文件完整路径名
	 * @param iconFormat
	 *            ：图片格式
	 * @return true for 保存成功，false for 保存失败。
	 */
	public static boolean saveBitmapToSDFile(final Bitmap bitmap, final String filePathName,
			CompressFormat iconFormat) {
		boolean result = false;
		if (bitmap == null || bitmap.isRecycled()) {
			return result;
		}
		try {
			createNewFile(filePathName, false);
			OutputStream outputStream = new FileOutputStream(filePathName);
			result = bitmap.compress(iconFormat, 100, outputStream);
			outputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 
	 * @author huyong
	 * @param byteData
	 * @param fileName
	 * @return
	 */
	public static boolean saveByteToCommonIconSDFile(final byte[] byteData, final String fileName) {
		String filePathName = LauncherEnv.Path.GOSTORE_ICON_PATH;
		filePathName += fileName;
		return saveByteToSDFile(byteData, filePathName);
	}

	/**
	 * 保存数据到指定文件
	 * 
	 * @author huyong
	 * @param byteData
	 * @param filePathName
	 * @return true for save successful, false for save failed.
	 */
	public static boolean saveByteToSDFile(final byte[] byteData, final String filePathName) {
		boolean result = false;
		try {
			File newFile = createNewFile(filePathName, false);
			FileOutputStream fileOutputStream = new FileOutputStream(newFile);
			// BufferedOutputStream outputStream = new
			// BufferedOutputStream(fileOutputStream);
			fileOutputStream.write(byteData);
			fileOutputStream.flush();
			fileOutputStream.close();
			result = true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}

	public static byte[] getByteFromSDFile(final String filePathName) {
		byte[] bs = null;
		try {
			File newFile = new File(filePathName);
			FileInputStream fileInputStream = new FileInputStream(newFile);
			DataInputStream dataInputStream = new DataInputStream(fileInputStream);
			BufferedInputStream inPutStream = new BufferedInputStream(dataInputStream);
			bs = new byte[(int) newFile.length()];
			inPutStream.read(bs);
			fileInputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return bs;
	}

	/**
	 * 
	 * @author huyong
	 * @param path
	 *            ：文件路径
	 * @param append
	 *            ：若存在是否插入原文件
	 * @return
	 */
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
	 * sd卡是否可读写
	 * 
	 * @author huyong
	 * @return
	 */
	public static boolean isSDCardAvaiable() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	/**
	 * 指定路径文件是否存在
	 * 
	 * @author huyong
	 * @param filePath
	 * @return
	 */
	public static boolean isFileExist(String filePath) {
		boolean result = false;
		try {
			File file = new File(filePath);
			result = file.exists();
			file = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result;
	}

	/**
	 * 如果filePath表示的是一个文件，则删除文件。如果filePath表示的是一个目录，则删除目录及目录下的子目录和文件
	 * 
	 * @author xiedezhi
	 * @param filePath
	 *            文件路径
	 */
	public static void delFile(String filePath) {
		File file = new File(filePath);
		if (file != null && file.exists()) {// 文件是否存在
			if (file.isFile()) {// 如果是文件
				file.delete();
			} else if (file.isDirectory()) {// 如果是目录
				File[] subFiles = file.listFiles();
				if (subFiles.length == 0) {
					file.delete();
				} else {
					for (int i = 0; i < subFiles.length; i++) {
						File subFile = subFiles[i];
						if (subFile.isDirectory()) {
							delFile(subFile.getAbsolutePath());// 递归调用del方法删除子目录和子文件
						}
						subFile.delete();
					}
				}
			}
		}
	}

}
