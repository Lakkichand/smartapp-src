package com.jiubang.ggheart.appgame.appcenter.help;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.jiubang.ggheart.appgame.base.data.AppGameNetLogControll;
import com.jiubang.ggheart.appgame.base.data.ClassificationExceptionRecord;

/**
 * 应用推荐模块，IO工具类
 * 
 * @author zhoujun
 * 
 */
public class RecommAppFileUtil {

	public static String unzipData(InputStream inStream) {
		try {
			byte[] old_bytes = toByteArray(inStream);
			byte[] new_bytes = ungzip(old_bytes);
			return new String(new_bytes, "utf-8");
		} catch (Exception ex) {
			ex.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(ex);
		}
		return null;
	}

	/**
	 * 解压缩并且统计下载速度
	 * 
	 * @param inStream
	 * @return
	 */
	public static String unzipDataAndLog(InputStream inStream) {
		try {
			long time = System.currentTimeMillis();
			byte[] old_bytes = toByteArray(inStream);
			// 统计下载速度 old_bytes/time2
			long time2 = System.currentTimeMillis() - time;
			if (time2 > 0) {
				String speed = String.valueOf(old_bytes.length / time2);
				AppGameNetLogControll.getInstance().setDownloadSpeed(
						AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, speed);
			}
			byte[] new_bytes = ungzip(old_bytes);
			return new String(new_bytes, "utf-8");
		} catch (Exception ex) {
			ex.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(ex);
			// 记录异常信息，同时保存网络信息
			AppGameNetLogControll.getInstance().setExceptionCode(
					AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, ex);
		}
		return null;
	}

	public static String readFileToString(String filePath) {
		if (filePath == null || "".equals(filePath)) {
			return null;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			return readToString(inputStream, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * 
	 * @param inputStream
	 * @param encoding
	 * @return
	 */
	public static String readToString(InputStream inputStream, String encoding) {

		InputStreamReader in = null;
		try {
			StringWriter sw = new StringWriter();
			in = new InputStreamReader(inputStream, encoding);
			copy(in, sw);
			return sw.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return null;
	}

	private static int copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[1024 * 4];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public static int copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024 * 4];
		int count = 0;
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	public static byte[] ungzip(byte[] bs) throws Exception {
		GZIPInputStream gzin = null;
		ByteArrayInputStream bin = null;
		try {
			bin = new ByteArrayInputStream(bs);
			gzin = new GZIPInputStream(bin);
			return toByteArray(gzin);
		} catch (Exception e) {
			throw e;
		} finally {
			if (bin != null) {
				bin.close();
			}
		}
	}

	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	/**
	 * 从SD卡读取Bitmap
	 * 
	 * @author xiedezhi
	 */
	public static Bitmap getBitmapFromSDCard(String filepath) {
		if (filepath == null) {
			return null;
		}
		File file = new File(filepath);
		if ((!file.exists()) || (!file.isFile())) {
			return null;
		}
		try {
			Bitmap ret = BitmapFactory.decodeFile(filepath);
			return ret;
		} catch (OutOfMemoryError er) {// 预防内存溢出
			er.printStackTrace();
			System.gc();
			try {
				Bitmap ret = BitmapFactory.decodeFile(filepath);
				return ret;
			} catch (OutOfMemoryError ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}

}
