package com.jiubang.go.backup.pro.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * @author GoBackup Dev Team
 */
public class EncryptDecrypt {
	// 密钥
	// private SecretKey key = null;
	// 用key产生Cipher
	// private Cipher cipher = null;
	// Cipher实例名称
	private final String mINSTANCENAME = "DES";

	private SecretKey genKey(String password) {
		SecretKey key = null;
		try {
			// DES算法要求有一个可信任的随机数源
			SecureRandom sr = new SecureRandom();
			// 从原始密匙数据创建DESKeySpec对象
			DESKeySpec dks = new DESKeySpec(password.getBytes());
			// 创建一个密匙工厂，然后用它把DESKeySpec转换成
			// 一个SecretKey对象
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(mINSTANCENAME);
			key = keyFactory.generateSecret(dks);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			key = null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			key = null;
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
			key = null;
		}
		return key;
	}

	private Cipher createCipher(boolean encrypt, String password) {
		Cipher cipher = null;
		SecretKey key = null;
		try {
			key = genKey(password);
			// 设置算法,应该与加密时的设置一样
			cipher = Cipher.getInstance(mINSTANCENAME);
			if (encrypt) {
				cipher.init(Cipher.ENCRYPT_MODE, key);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, key);
			}
		} catch (Exception e) {
			e.printStackTrace();
			cipher = null;
		}
		return cipher;
	}

	public boolean encrypt(String sourceFileName, String targetFileName, String password) {
		if (sourceFileName == null || targetFileName == null || password == null) {
			return false;
		}

		Cipher cipher = createCipher(true, password);
		if (cipher == null) {
			return false;
		}

		boolean ret = true;
		File file = new File(sourceFileName);
		BufferedOutputStream out = null;
		CipherInputStream in = null;
		try {
			// 输出流,请注意文件名称的获取
			out = new BufferedOutputStream(new FileOutputStream(targetFileName));
			// 输入流
			in = new CipherInputStream(new BufferedInputStream(new FileInputStream(file)), cipher);
			int thebyte = 0;
			while ((thebyte = in.read()) != -1) {
				out.write(thebyte);
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}

	public boolean encrypt(File sourceFile, File targetFile, String password) {
		if (sourceFile == null || targetFile == null || password == null) {
			return false;
		}

		Cipher cipher = createCipher(true, password);
		if (cipher == null) {
			return false;
		}

		boolean ret = true;
		BufferedOutputStream out = null;
		CipherInputStream in = null;
		try {
			// 输出流,请注意文件名称的获取
			out = new BufferedOutputStream(new FileOutputStream(targetFile));
			// 输入流
			in = new CipherInputStream(new BufferedInputStream(new FileInputStream(sourceFile)),
					cipher);
			int thebyte = 0;
			while ((thebyte = in.read()) != -1) {
				out.write(thebyte);
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}

	public boolean decrypt(String sourceFileName, String targetFileName, String password) {
		if (sourceFileName == null || targetFileName == null || password == null) {
			return false;
		}

		Cipher cipher = createCipher(false, password);
		if (cipher == null) {
			return false;
		}

		boolean ret = true;
		File file = new File(sourceFileName);
		BufferedOutputStream out = null;
		CipherInputStream in = null;
		try {
			// 输出流,请注意文件名称的获取
			out = new BufferedOutputStream(new FileOutputStream(targetFileName));
			// 输入流
			in = new CipherInputStream(new BufferedInputStream(new FileInputStream(file)), cipher);
			int thebyte = 0;
			while ((thebyte = in.read()) != -1) {
				out.write(thebyte);
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}

	public boolean decrypt(File sourceFile, File targetFile, String password) {
		if (sourceFile == null || targetFile == null || password == null) {
			return false;
		}

		Cipher cipher = createCipher(false, password);
		if (cipher == null) {
			return false;
		}

		boolean ret = true;
		BufferedOutputStream out = null;
		CipherInputStream in = null;
		try {
			// 输出流,请注意文件名称的获取
			out = new BufferedOutputStream(new FileOutputStream(targetFile));
			// 输入流
			in = new CipherInputStream(new BufferedInputStream(new FileInputStream(sourceFile)),
					cipher);
			int thebyte = 0;
			while ((thebyte = in.read()) != -1) {
				out.write(thebyte);
			}
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
			ret = false;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
			}
		}
		return ret;
	}
}
