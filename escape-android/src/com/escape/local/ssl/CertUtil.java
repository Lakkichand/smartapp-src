package com.escape.local.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;

import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

import android.util.Log;

import com.escape.local.http.main.ProxyApplication;

public class CertUtil {

	public static final String mCertPath = ProxyApplication.sCacheDir + "/";
	public static final String mCACertFileName = "CA/ECA.crt";
	public static final String mCAKeyFileName = "CA/ECA.key";
	public static X509Certificate mCACert = null;
	public static PrivateKey mCAPriKey = null;

	static {
		addBCProvider();
		if (!checkCA()) {
			Log.e("Test", "检查CA失败");
			// TODO 退出程序
		} else {
			Log.e("Test", "检查CA成功");
		}
	}

	private static void addBCProvider() {
		if (java.security.Security.getProvider("BC") == null) {
			java.security.Security
					.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		}
	}

	private static boolean saveCertificate(String file, X509Certificate cert) {
		try {
			FileOutputStream os = new FileOutputStream(file);
			Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
			wr.write("-----BEGIN CERTIFICATE-----\n");
			wr.write(new String(Base64.encode(cert.getEncoded()), "UTF-8"));
			wr.write("\n-----END CERTIFICATE-----\n");
			wr.flush();
			wr.close();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static X509Certificate loadCertificate(String file) {
		try {
			FileInputStream is = new FileInputStream(file);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
			is.close();
			return cert;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static boolean savePrivateKey(String file, PrivateKey key) {
		try {
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
					key.getEncoded());
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(pkcs8EncodedKeySpec.getEncoded());
			fos.flush();
			fos.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static PrivateKey loadPrivateKey(String file) {
		try {
			File filePrivateKey = new File(file);
			FileInputStream fis = new FileInputStream(filePrivateKey);
			byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
			fis.read(encodedPrivateKey);
			fis.close();

			KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
					encodedPrivateKey);
			PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
			return privateKey;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator caKeyPairGen = KeyPairGenerator.getInstance("RSA",
					"BC");
			caKeyPairGen.initialize(1024, new SecureRandom());
			KeyPair keypair = caKeyPairGen.genKeyPair();
			return keypair;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static X509Certificate createCACert(PublicKey pubKey,
			PrivateKey privKey) {
		try {
			X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
			String issuer = "CN=Escape CA, OU=Escape Root, O=Escape, L=Cernet, ST=Internet, C=CN";
			String subject = issuer;
			v3CertGen.setSerialNumber(BigInteger.valueOf(System
					.currentTimeMillis() * (117)));
			v3CertGen.setIssuerDN(new X509Principal(issuer));
			long thisTime = System.currentTimeMillis();
			v3CertGen.setNotBefore(new Date(thisTime - 24l * 60 * 60 * 1000));
			v3CertGen.setNotAfter(new Date(thisTime + 31l * 365 * 24 * 60 * 60
					* 1000));
			v3CertGen.setSubjectDN(new X509Principal(subject));
			v3CertGen.setPublicKey(pubKey);
			v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
			v3CertGen.addExtension(X509Extensions.BasicConstraints, true,
					new BasicConstraints(true));
			v3CertGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
					new SubjectKeyIdentifierStructure(pubKey));
			X509Certificate cert = v3CertGen.generateX509Certificate(privKey);
			cert.checkValidity();
			cert.verify(pubKey);
			return cert;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static X509Certificate createClientCert(PublicKey pubKey,
			PrivateKey caPrivKey, PublicKey caPubKey, String host) {
		try {
			X509V3CertificateGenerator v3CertGen = new X509V3CertificateGenerator();
			String issuer = "CN=Escape CA, OU=Escape Root, O=Escape, L=Cernet, ST=Internet, C=CN";
			Hashtable<DERObjectIdentifier, String> attrs = new Hashtable<DERObjectIdentifier, String>();
			Vector<DERObjectIdentifier> order = new Vector<DERObjectIdentifier>();

			attrs.put(X509Principal.C, "CN");
			attrs.put(X509Principal.ST, "Internet");
			attrs.put(X509Principal.L, "Cernet");
			attrs.put(X509Principal.O, host);
			attrs.put(X509Principal.OU, "Escape Branch");
			attrs.put(X509Principal.CN, host);
			order.addElement(X509Principal.C);
			order.addElement(X509Principal.ST);
			order.addElement(X509Principal.L);
			order.addElement(X509Principal.O);
			order.addElement(X509Principal.OU);
			order.addElement(X509Principal.CN);
			v3CertGen.reset();
			v3CertGen.setSerialNumber(BigInteger.valueOf(System
					.currentTimeMillis() * 103));
			v3CertGen.setIssuerDN(new X509Principal(issuer));
			v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 24l
					* 60 * 60 * 1000));
			v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + 30l
					* 365 * 24 * 60 * 60 * 1000));
			v3CertGen.setSubjectDN(new X509Principal(order, attrs));
			v3CertGen.setPublicKey(pubKey);
			v3CertGen.setSignatureAlgorithm("SHA1WithRSAEncryption");
			X509Certificate cert = v3CertGen.generateX509Certificate(caPrivKey);
			cert.checkValidity();
			cert.verify(caPubKey);
			return cert;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static CertAndKey getCertificateAndKey(String host) {
		if (mCACert == null || mCAPriKey == null) {
			return null;
		}

		String certFileName = mCertPath + host + ".crt";
		File certFile = new File(certFileName);
		String keyFileName = mCertPath + host + ".key";
		File keyFile = new File(keyFileName);

		if (certFile.exists() && keyFile.exists()) {
			X509Certificate cert = loadCertificate(certFileName);
			PrivateKey key = loadPrivateKey(keyFileName);
			if (cert != null && key != null) {
				CertAndKey ck = new CertAndKey();
				ck.mCert = cert;
				ck.mKey = key;
				// System.out.println("client cert and key exists");
				return ck;
			}
		}
		KeyPair keyPair = generateKeyPair();
		if (keyPair == null) {
			return null;
		}
		X509Certificate cert = createClientCert(keyPair.getPublic(), mCAPriKey,
				mCACert.getPublicKey(), host);
		if (cert == null) {
			return null;
		}

		// 要确保成功保存证书和私钥
		boolean ret = savePrivateKey(keyFileName, keyPair.getPrivate());
		if (!ret) {
			return null;
		}
		ret = saveCertificate(certFileName, cert);
		if (!ret) {
			return null;
		}
		CertAndKey ck = new CertAndKey();
		ck.mCert = cert;
		ck.mKey = keyPair.getPrivate();
		// System.out.println("create client cert and key success");
		return ck;
	}

	public static boolean installCA() {
		// TODO 安装根证书
		return false;
	}

	public static boolean checkCA() {
		// TODO 程序启动时检查CA，如果返回false,程序退出
		String certFileName = mCertPath + mCACertFileName;
		File certFile = new File(certFileName);
		String keyFileName = mCertPath + mCAKeyFileName;
		File keyFile = new File(keyFileName);

		if (certFile.exists() && keyFile.exists()) {
			X509Certificate cert = loadCertificate(certFileName);
			PrivateKey key = loadPrivateKey(keyFileName);
			if (cert != null && key != null) {
				mCACert = cert;
				mCAPriKey = key;
				return true;
			}
		}

		KeyPair keyPair = generateKeyPair();
		if (keyPair == null) {
			return false;
		}
		X509Certificate cert = createCACert(keyPair.getPublic(),
				keyPair.getPrivate());
		if (cert == null) {
			return false;
		}
		mCACert = cert;
		mCAPriKey = keyPair.getPrivate();
		// 要确保成功保存证书和私钥
		boolean ret = savePrivateKey(mCertPath + mCAKeyFileName,
				keyPair.getPrivate());
		if (!ret) {
			return false;
		}
		ret = saveCertificate(mCertPath + mCACertFileName, cert);
		if (!ret) {
			return false;
		}
		return true;
	}

	public static SSLSocketFactory createSSLSocketFactory(String host) {
		try {
			SSLContext sslcontext = SSLContext.getInstance("SSL");
			X509KeyManager[] iKeyManager = new X509KeyManager[] { new IX509KeyManager(
					host) };
			sslcontext.init(iKeyManager, null, null);
			SSLSocketFactory sslSocketFactory = sslcontext.getSocketFactory();
			return sslSocketFactory;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class CertAndKey {
		public X509Certificate mCert = null;
		public PrivateKey mKey = null;
	}

}
