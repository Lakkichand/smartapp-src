package com.jiubang.go.backup.pro.statistics;

import java.security.MessageDigest;

/**
 * 加解密工具类
 * 
 * @author yuxiaowei
 */
public class CryptTool {
	public static byte[] xor(byte[] bytes, byte key) {

		byte[] result = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			result[i] = (byte) ((bytes[i]) ^ key);
		}
		return result;
	}

	/**
	 * 循环异或，一般用于加密
	 * 
	 * @param bytes
	 * @param key
	 * @return
	 */
	public static byte[] xor(byte[] bytes, byte[] key) {
		byte[] result = bytes;
		for (int i = 0; i < key.length; i++) {
			result = xor(result, key[i]);
		}
		return result;
	}

	/**
	 * 加密 ：使用异或加密后采用base64编码返回字符串
	 * 
	 * @param src
	 * @param key
	 * @return
	 */
	public static String encrypt(String src, String key) {
		try {
			byte[] b = xor(src.getBytes("utf-8"), key.getBytes("utf-8"));
			// BASE64Encoder encoder = new BASE64Encoder();
			// return encoder.encode(b);
			return Base64.encodeBytes(b);
			// return new String(b,"utf-8");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解密 ：使用异或解密后采用utf8编码返回字符串
	 * 
	 * @param src
	 * @param key
	 * @return
	 */
	public static String decrypt(String src, String key) {
		try {
			// BASE64Decoder decoder = new BASE64Decoder();
			// byte[] b=xor(decoder.decodeBuffer(src), key.getBytes("utf-8"));
			byte[] b = xor(Base64.decode(src), key.getBytes("utf-8"));
			return new String(b, "utf-8");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * public static void main(String[] args) { //
	 * tempTda=CWYJZghmC2YJAglmCWYLZgtmCQIJZglmCmYLZgk= // rd=92424 //
	 * info=71614f6a7f2390e1b933762a59b9f12c // 36d40a4c228dcbec1cd2383dfcb6fd7d
	 * String s="1_1_0_3_1;1_1_3_3_1;1_1_2_3_1"; String key="92424"; String
	 * t=encrypt(s, key); System.out.println("加密后:"+t); //
	 * System.out.println(EscapeUtil.escapeUrl(t, "utf-8")); //
	 * t="CWYJZghmC2YJAglmCWYLZgtmCQIJZglmCmYLZgk="; //
	 * System.out.println("info="
	 * +MD5.MD5generator("CWYJZghmC2YJAglmCWYLZgtmCQIJZglmCmYLZgk="+"92424"));
	 * t=decrypt(t, key); System.out.println("解密后:"+t); }
	 */

	/**
	 * 产生MD5加密串
	 * 
	 * @author huyong
	 * @param plainText
	 * @param charset
	 * @return
	 */
	private static String to32BitString(String plainText, String charset) {
		try {
			final int m0xff = 0xFF;
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			if (charset != null) {
				md.update(plainText.getBytes(charset));
			} else {
				md.update(plainText.getBytes());
			}
			byte[] byteArray = md.digest();
			StringBuffer md5Buf = new StringBuffer();
			for (int i = 0; i < byteArray.length; i++) {
				if (Integer.toHexString(m0xff & byteArray[i]).length() == 1) {
					md5Buf.append("0").append(Integer.toHexString(m0xff & byteArray[i]));
				} else {
					md5Buf.append(Integer.toHexString(m0xff & byteArray[i]));
				}
			}
			return md5Buf.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 产生32位md5加密字符串
	 * 
	 * @param s
	 *            待加密的字符串
	 * @return md5加密处理后的字符串
	 */
	public final static String mD5generator(String s) {
		// String charset =System.getProperties()
		return to32BitString(s, null);
	}
}
