package com.escape.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MessageHelper {

	public static byte[] compress(byte[] data) {
		if (data == null) {
			return null;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			compress(bais, baos);
			byte[] output = baos.toByteArray();
			return output;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				baos.flush();
				baos.close();
				bais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static void compress(InputStream is, OutputStream os)
			throws IOException {
		GZIPOutputStream gos = new GZIPOutputStream(os);
		int count;
		byte data[] = new byte[1024];
		while ((count = is.read(data, 0, 1024)) != -1) {
			gos.write(data, 0, count);
		}
		gos.finish();
		gos.flush();
		gos.close();
	}

	public static byte[] decompress(byte[] data) {
		if (data == null) {
			return null;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			decompress(bais, baos);
			data = baos.toByteArray();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(new String(data));
		} finally {
			try {
				baos.flush();
				baos.close();
				bais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static void decompress(InputStream is, OutputStream os)
			throws IOException {
		GZIPInputStream gis = new GZIPInputStream(is);
		int count;
		byte data[] = new byte[1024];
		while ((count = gis.read(data, 0, 1024)) != -1) {
			os.write(data, 0, count);
		}
		gis.close();
	}

}
