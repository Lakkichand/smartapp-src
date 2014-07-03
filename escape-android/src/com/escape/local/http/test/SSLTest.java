package com.escape.local.http.test;

import java.io.File;
import java.security.KeyPair;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import com.escape.local.ssl.CertUtil;
import com.escape.local.ssl.CertUtil.CertAndKey;

public class SSLTest {

	public static void main(String[] args) throws CertificateEncodingException {
		System.out.println(CertUtil.checkCA());
		CertAndKey ck = CertUtil.getCertificateAndKey("www.baidu.com");
		System.out.println(ck.mCert.toString() + "    " + ck.mKey.toString());
		// File file = new File(CertUtil.mCertPath + CertUtil.mCACertFileNae);
		// System.out.println(file.exists());
		// CertUtil.addBCProvider();
		//
		// KeyPair CAKeyPair = CertUtil.generateKeyPair();
		// if (CAKeyPair == null) {
		// return;
		// }
		// boolean ret = CertUtil.savePrivateKey(CertUtil.mCertPath
		// + CertUtil.mCAKeyFileName, CAKeyPair.getPrivate());
		// if (!ret) {
		// System.out.println("保存私钥失败");
		// }
		// // PrivateKey _priKey = CertUtil
		// // .loadPrivateKey("/home/escape/桌面/EscapeCA/ECA.key");
		// // System.out.println(_priKey.equals(CAKeyPair.getPrivate()));
		// X509Certificate CACert = CertUtil.createCACert(CAKeyPair.getPublic(),
		// CAKeyPair.getPrivate());
		// if (CACert == null) {
		// return;
		// }
		// System.out
		// .println("BasicConstraints = " + CACert.getBasicConstraints());
		// ret = CertUtil.saveCertificate(CertUtil.mCertPath
		// + CertUtil.mCACertFileName, CACert);
		// if (!ret) {
		// System.out.println("保存证书失败");
		// }
		// X509Certificate _ca = CertUtil
		// .loadCertificate("/home/escape/桌面/EscapeCA/ECA.crt");
		// System.out.println(CACert.equals(_ca));
		//
		// KeyPair clientKeyPair = CertUtil.generateKeyPair();
		// if (clientKeyPair == null) {
		// return;
		// }
		// ret = CertUtil.savePrivateKey("/home/escape/桌面/EscapeCA/EClient.key",
		// clientKeyPair.getPrivate());
		// if (!ret) {
		// System.out.println("保存私钥失败");
		// }
		// X509Certificate clientCertificate = CertUtil.createClientCert(
		// clientKeyPair.getPublic(), CAKeyPair.getPrivate(),
		// CAKeyPair.getPublic(), "EClient");
		// ret =
		// CertUtil.saveCertificate("/home/escape/桌面/EscapeCA/EClient.crt",
		// clientCertificate);
		// X509Certificate _client = CertUtil
		// .loadCertificate("/home/escape/桌面/EscapeCA/EClient.crt");
		// System.out.println("@#@" + _client.equals(clientCertificate));
	}
}
