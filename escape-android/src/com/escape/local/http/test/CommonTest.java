package com.escape.local.http.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CommonTest {

	public static final int BUFFER = 1024;

	public static void main(String[] args) throws Exception {
		//
		// String json =
		// "asdfasjdflasjdfoaiwjefaishdfkjasdfvkjxncvkjxzcnvwerwui0e923447359374957349867634fklvn x,nmdvnmc dkvsHS UYAQQWUQWTQUW3541254125!#^^&%$$%^&BGFYFYDYFHGV";
		//
		// byte[] pack = compress(json.getBytes());
		//
		// byte[] content = decompress(pack);

//		byte[] pack = null;
//		JSONObject json = new JSONObject();
//		json.put("fuck", "fuck");
//		json.put("pack", pack);
//		System.out.println(json.toString());
//		System.out.println(json.getString("pack"));
	}

	/**
	 * 数据压缩
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static byte[] compress(byte[] data) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// 压缩
		compress(bais, baos);

		byte[] output = baos.toByteArray();

		baos.flush();
		baos.close();

		bais.close();

		return output;
	}

	/**
	 * 数据压缩
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 * @throws Exception
	 */
	public static void compress(InputStream is, OutputStream os)
			throws IOException {

		GZIPOutputStream gos = new GZIPOutputStream(os);

		int count;
		byte data[] = new byte[BUFFER];
		while ((count = is.read(data, 0, BUFFER)) != -1) {
			gos.write(data, 0, count);
		}

		gos.finish();

		gos.flush();
		gos.close();
	}

	/**
	 * 数据解压缩
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static byte[] decompress(byte[] data) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// 解压缩

		decompress(bais, baos);

		data = baos.toByteArray();

		baos.flush();
		baos.close();

		bais.close();

		return data;
	}

	/**
	 * 数据解压缩
	 * 
	 * @param is
	 * @param os
	 * @throws IOException
	 * @throws Exception
	 */
	public static void decompress(InputStream is, OutputStream os)
			throws IOException {

		GZIPInputStream gis = new GZIPInputStream(is);

		int count;
		byte data[] = new byte[BUFFER];
		while ((count = gis.read(data, 0, BUFFER)) != -1) {
			os.write(data, 0, count);
		}

		gis.close();
	}
}
