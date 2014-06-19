package com.jiubang.go.backup.pro.image.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;

import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.net.sync.NetRestoreEngine;

/**
 * @author jiangpeihe
 *照片备份
 */
public class ImageRestore {
	private Context mContext = null;
	private ImageBean mImage;
	public String mImageDir = ImageBackupEntry.IMAGE_DIR_NAME;

	public boolean restoreImage(Context context, ImageBean image) {
		boolean result = true;
		if (context == null || image == null) {
			result = false;
			return result;
		}
		mContext = context;
		mImage = image;
		result = startImageRestore(mContext);
		return result;
	}

	public boolean startImageRestore(Context context) {
		// 恢复
		boolean result = false;
		//备份的图片路径名，注意不能用mImage.mImagePath，因为在tasktable中不错这网上照片的路径
		String imageParentPath = mImage.mImageParentFilePath;
		String imagePath = NetRestoreEngine.sSdCarkPath
				+ imageParentPath.substring(imageParentPath.indexOf(File.separator) + 1)
				+ File.separator + mImage.mImageDisplayName;
		File imageFile = new File(imagePath);
		String cacheImagePath = imagePath.replace(NetRestoreEngine.sSdCarkPath,
				Constant.buildNetworkBackupCacheDir(mContext) + mImageDir + File.separator);
		File cacheImageFile = new File(cacheImagePath);
		if (!imageFile.exists()) {
			//不存在照片
			imageFile = createFile(imagePath);
			if (cacheImageFile.exists()) {
				result = copyImageFile(cacheImageFile, imageFile);
			} else {
				result = false;
			}
		} else if (imageFile.length() != mImage.mImageSize) {
			//同名，但是不是统一张照片
			imageFile = createFile(NetRestoreEngine.sSdCarkPath
					+ imageParentPath.substring(imageParentPath.indexOf(File.separator) + 1)
					+ File.separator + mImage.mImageDisplayName.replace(".", "(1)."));
			if (cacheImageFile.exists()) {
				result = copyImageFile(cacheImageFile, imageFile);
			} else {
				result = false;
			}
		} else {
			//			全相同的照片
			result = true;
		}
		return result;
	}

	// 创建文件
	private File createFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			return file;
		}
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	//	复制照片
	public boolean copyImageFile(File source, File destination) {
		boolean ret = true;
		BufferedInputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(source));
			out = new BufferedOutputStream(new FileOutputStream(destination));
			byte[] buf = new byte[1024];
			int len;
			try {
				len = in.read(buf);
				while (len != -1) {
					out.write(buf, 0, len);
					len = in.read(buf);
				}
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
				ret = false;
			} finally {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;

	}
}