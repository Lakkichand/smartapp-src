package com.jiubang.go.backup.pro.product.manage;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Log;

/**
 * 加密算法
 * 
 * @author GoBackup Dev Team
 */
public class EncryptArithmetic {
	private static final String LOG_TAG = "GoBackup_EncryptArithmetic"; // 日志标识

	private static final String MD5KEY = "MD5";
	private static final String DEFAULTENCODING = "utf-8";
	private static final String TRIPLE_KEY = "guangzhou_huizhiwccpcomm";
	private static final String TRIPLE_ALGORITHM = "DESede";

	private static final int M4 = 4;
	private static final int M3 = 3;
	private static final int M17 = 17;
	private static final int M49 = 49;
	private static final int M0XFF = 0xff;
	private static final int M16 = 16;

	/**
	 * 异或加密算法：明文加密
	 * 
	 * @param sourceString
	 * @param key
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String mXOREncrypt(String sourceString, String key)
			throws UnsupportedEncodingException {
		byte[] keyByteArray = key.getBytes(DEFAULTENCODING);

		byte[] strByteArray = sourceString.getBytes(DEFAULTENCODING);

		for (int i = 0; i < strByteArray.length; i++) {
			for (int j = 0; j < keyByteArray.length; j++) {
				strByteArray[i] = (byte) (strByteArray[i] ^ keyByteArray[j]);
			}
		}

		String result = new String(strByteArray);

		return result;
	}

	/**
	 * 偏移加密算法：明文加密
	 * 
	 * @param sourceString
	 * @param key
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String offsetEncrypt(String sourceString, String registerCode)
			throws UnsupportedEncodingException {
		int[] totalValue = new int[M4];
		byte[] codeByteArray = registerCode.getBytes(DEFAULTENCODING);

		int j = 0;
		for (int i = 0; i < codeByteArray.length; i++) {
			if (i % M4 == M3) {
				totalValue[j] += codeByteArray[i];
				totalValue[j] = totalValue[j] % M17 + M49;
				j++;
			} else {
				totalValue[j] += codeByteArray[i];
			}
		}

		byte[] strByteArray = sourceString.getBytes(DEFAULTENCODING);

		for (int i = 0; i < strByteArray.length; i++) {
			strByteArray[i] = (byte) (strByteArray[i] + totalValue[i]);
		}

		String result = new String(strByteArray);

		return result;
	}

	/**
	 * 偏移解密算法
	 * 
	 * @param sourceString
	 * @param key
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String offsetDencrypt(String sourceString, String registerCode)
			throws UnsupportedEncodingException {

		int[] totalValue = new int[M4];
		byte[] codeByteArray = registerCode.getBytes(DEFAULTENCODING);

		int j = 0;
		for (int i = 0; i < codeByteArray.length; i++) {
			if (i % M4 == M3) {
				totalValue[j] += codeByteArray[i];
				totalValue[j] = totalValue[j] % M17 + M49;
				j++;
			} else {
				totalValue[j] += codeByteArray[i];
			}
		}

		byte[] strByteArray = sourceString.getBytes(DEFAULTENCODING);

		for (int i = 0; i < strByteArray.length; i++) {
			strByteArray[i] = (byte) (strByteArray[i] - totalValue[i]);
		}

		String result = new String(strByteArray);

		return result;
	}

	/**
	 * 异或加密算法：密文解密
	 * 
	 * @param ciphertext
	 * @param key
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String mXORDencrypt(String ciphertext, String key)
			throws UnsupportedEncodingException {
		String result = mXOREncrypt(ciphertext, key);
		byte[] b = result.getBytes(DEFAULTENCODING);

		result = new String(b);

		return result;
	}

	/**
	 * MD5加密
	 * 
	 * @param input
	 * @return
	 */
	public static byte[] md5Encrypt(byte[] input) {
		try {
			MessageDigest md5 = MessageDigest.getInstance(MD5KEY);
			return md5.digest(input);
		} catch (Exception e) {
			Log.d(LOG_TAG, LOG_TAG + ":" + e.getMessage());
			return null;
		}
	}

	public static String md5EncryptSerialCode(byte[] input) {
		try {
			final int m8 = 8;
			final int m24 = 24;
			MessageDigest md5 = MessageDigest.getInstance(MD5KEY);
			byte[] temp = md5.digest(input);
			String s = toHexString(temp);
			s = s.replace("-", "");
			s = s.substring(m8, m24);
			return s;
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * MD5加密
	 * 
	 * @param input
	 * @return
	 */
	public static String md5EncryptAll(byte[] input) {
		byte[] encryData = md5Encrypt(input);
		// String rs=toHexString(encryData);
		// return rs;
		String s = new String(encryData);
		return s;

	}

	public static String toHexString(byte[] b) {

		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String tmp = Integer.toString(b[i] & M0XFF, M16);
			if (tmp.length() == 1) {
				tmp = "0" + tmp;
			}
			if (tmp.length() == 0) {
				tmp = "00";
			}
			ret = ret + tmp;
		}
		return ret;
	}

	/***
	 * 3DES加密
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static byte[] tripleDesEncrypt(byte[] input) throws Exception {
		byte[] encryptData = null;
		byte[] key = TRIPLE_KEY.getBytes("ASCII");
		SecretKey desKey = new SecretKeySpec(key, TRIPLE_ALGORITHM);
		Cipher c1 = Cipher.getInstance(TRIPLE_ALGORITHM);
		c1.init(Cipher.ENCRYPT_MODE, desKey);
		encryptData = c1.doFinal(input);
		return encryptData;
	}

	/**
	 * 3DES解密
	 * 
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static byte[] tripleDesDencrypt(byte[] input) throws Exception {
		byte[] key = TRIPLE_KEY.getBytes("ASCII");
		SecretKey desKey = new SecretKeySpec(key, TRIPLE_ALGORITHM);
		Cipher c1 = Cipher.getInstance(TRIPLE_ALGORITHM);
		c1.init(Cipher.DECRYPT_MODE, desKey);
		byte[] dencryptData = c1.doFinal(input);
		return dencryptData;
	}

	public static final byte[] DES_KEY_PUBLIC = { 1, 3, 5, 7, 2, 4, 6, 8 };

	/**
	 * 用DES方法加密输入的字节 bytKey需为8字节长，是加密的密码 bytKey参数无效
	 * 
	 * @throws Exception
	 */
	public static String encryptByDES(byte[] input) {
		byte[] data = encryptByDES(input, DES_KEY_PUBLIC);
		String ret = "";
		for (int i = 0; i < data.length; i++) {
			String tmp = Integer.toString(data[i] & M0XFF, M16);
			if (tmp.length() == 1) {
				tmp = "0" + tmp;
			}
			if (tmp.length() == 0) {
				tmp = "00";
			}
			ret = ret + tmp;
		}
		// logger.info("encrypted:" + ret);
		return ret;
	}

	/**
	 * 用DES方法加密输入的字节 bytKey需为8字节长，是加密的密码 bytKey参数无效
	 * 
	 * @throws Exception
	 */
	public static byte[] encryptByDES(byte[] bytP, byte[] bytKey) {
		try {
			DESKeySpec desKS = new DESKeySpec(DES_KEY_PUBLIC);
			SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
			SecretKey sk = skf.generateSecret(desKS);
			Cipher cip = Cipher.getInstance("DES");
			cip.init(Cipher.ENCRYPT_MODE, sk);
			return cip.doFinal(bytP);
		} catch (Exception e) {
			return null;
		}
	}
}
