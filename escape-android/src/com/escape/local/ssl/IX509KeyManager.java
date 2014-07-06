package com.escape.local.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

import com.escape.local.ssl.CertUtil.CertAndKey;

public class IX509KeyManager implements X509KeyManager {

	private String mEntryname;
	private X509Certificate mCACert;
	private X509Certificate mClientCert;
	private PrivateKey mClientKey;

	IX509KeyManager(String host) {
		if (host == null) {
			throw new IllegalArgumentException("arguments can not be null.");
		}
		this.mEntryname = host;
		this.mCACert = CertUtil.mCACert;
		CertAndKey ck = CertUtil.getCertificateAndKey(host);
		if (ck != null) {
			mClientCert = ck.mCert;
			mClientKey = ck.mKey;
		}
	}

	@Override
	public String[] getClientAliases(String keyType, Principal[] prncpls) {
//		System.err.println("getClientAliases keyType = " + keyType);
		return (new String[] { "" });
	}

	@Override
	public String chooseClientAlias(String[] keyType, Principal[] prncpls,
			Socket socket) {
//		System.err.println("chooseClientAlias");
		return "";
	}

	@Override
	public String[] getServerAliases(String keyType, Principal[] prncpls) {
//		System.err.println("getServerAliases keyType = " + keyType);
		if (keyType.equalsIgnoreCase("RSA")) {
			return (new String[] { mEntryname });
		}
		return null;
	}

	@Override
	public String chooseServerAlias(String keyType, Principal[] prncpls,
			Socket socket) {
//		System.err.println("chooseServerAlias keyType = " + keyType);
		if (keyType.equalsIgnoreCase("RSA")) {
			return mEntryname;
		}
		return null;
	}

	@Override
	public X509Certificate[] getCertificateChain(String alias) {
//		System.err.println("getCertificateChain alias = " + alias);
		if (alias.equals(mEntryname)) {
			X509Certificate x509certificates[] = new X509Certificate[2];
			x509certificates[0] = mClientCert;
			x509certificates[1] = mCACert;
			return x509certificates;
		}
		return null;
	}

	@Override
	public PrivateKey getPrivateKey(String alias) {
//		System.err.println("getPrivateKey alias = " + alias);
		if (alias.equals(mEntryname)) {
			return this.mClientKey;
		}
		return null;
	}

}