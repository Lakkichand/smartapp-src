package com.zhidian.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.os.Environment;

public class FileUtil {

	public static final String ZHIDIAN_DIR = Environment
			.getExternalStorageDirectory() + "/ZhidianApk";

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

	/**
	 * 创建文件
	 * 
	 * @param path
	 * @param append
	 * @return
	 */
	public static boolean createNewFile(String path) {
		boolean isExists = false;
		File newFile = new File(path);
		if (isSDCardAvaiable()) {
			if (!newFile.exists()) {
				try {
					newFile.mkdirs();
					isExists = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return isExists;
	}

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
	 */
	public static boolean isSDCardAvaiable() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public static List<String> getFileList(String path) {
		File f = new File(path);
		String[] str = f.list();
		List<String> list = java.util.Arrays.asList(str);
		return list;

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
	 * 删除单个文件
	 * 
	 * @param file
	 */
	public static void deleteSingle(String path) {

		try {
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 删除文件夹及下的所有文件
	 * 
	 * @param file
	 */
	public static void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}

			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}
}
