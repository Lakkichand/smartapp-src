package fq.router2.adservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONArray;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class CloudUtil {

	public static final String sLegalTag = "###sLegalTag:EasyVPN###";

	private static final String[] sGoogleIpList = { "74.125.235.147",
			"203.208.46.171", "74.125.31.100", "173.194.72.101",
			"74.125.235.198", "74.125.235.197", "74.125.235.194",
			"74.125.235.201", "74.125.31.132", "203.208.46.131",
			"203.208.46.132", "203.208.46.133", "203.208.46.134",
			"203.208.46.135", "203.208.46.136", "203.208.46.137",
			"203.208.46.138" };

	private static final String[] sGoogleHostList = { "mail.google.com",
			"play.google.com", "www.google.com", "maps.google.com" };

	private static final String[] sApplicationIPs = { "fqroutercloud1",
			"fqroutercloud2", "fqroutercloud3" };

	private static byte[] getAppspotRequest(String applicationID, String path) {
		StringBuffer sb = new StringBuffer("GET /" + path + " HTTP/1.1\r\n");
		sb.append("Host:" + applicationID + ".appspot.com\r\n");
		sb.append("Connection:Close\r\n\r\n");
		return sb.toString().getBytes();
	}

	private static String getResponse(InputStream socketIn) throws Exception {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int len = -1;
		while ((len = socketIn.read(buff)) != -1) {
			buffer.write(buff, 0, len);
		}
		String result = new String(buffer.toByteArray(), "UTF-8");
		return result;
	}

	private static SSLSocket initSSLSocket(SSLSocketFactory factory,
			String host, int connectTimeOut, int soTimeOut)
			throws UnknownHostException, IOException {
		SSLSocket socket = (SSLSocket) factory.createSocket();
		socket.connect(new InetSocketAddress(host, 443), connectTimeOut);
		String[] sup = socket.getSupportedCipherSuites();
		socket.setEnabledCipherSuites(sup);
		socket.setSoTimeout(soTimeOut);
		return socket;
	}

	private static String[] getIpListByHost(String host) {
		if (host == null) {
			return null;
		}
		try {
			java.net.InetAddress[] x = java.net.InetAddress.getAllByName(host);
			if (x != null && x.length > 0) {
				String[] ret = new String[x.length];
				for (int i = 0; i < x.length; i++) {
					ret[i] = x[i].getHostAddress();
				}
				return ret;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public static String getResponseBody(String response) {
		try {
			if (response == null) {
				return null;
			}
			int startIndex = response.indexOf(sLegalTag);
			if (startIndex == -1) {
				return null;
			}
			return response.substring(startIndex + sLegalTag.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONArray getCloudAdInfo() {
		int index = (int) (System.currentTimeMillis() % 3);
		String appID = sApplicationIPs[index];
		List<String> list = new ArrayList<String>();
		for (String host : sGoogleHostList) {
			String[] ret = getIpListByHost(host);
			if (ret != null && ret.length > 0) {
				for (String r : ret) {
					list.add(r);
				}
			}
		}
		for (String ip : sGoogleIpList) {
			list.add(ip);
		}
		byte[] request = getAppspotRequest(appID, "fqroutercloud");
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory
				.getDefault();
		for (String ip : list) {
			Socket socket = null;
			InputStream in = null;
			OutputStream out = null;
			try {
				socket = initSSLSocket(factory, ip, 2000, 2000);
				OutputStream socketOut = socket.getOutputStream();
				socketOut.write(request);
				String result = getResponse(socket.getInputStream());
				String body = getResponseBody(result);
				Log.e("", "body = " + body);
				JSONArray array = new JSONArray(body);
				if (array != null && array.length() > 0) {
					return array;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (socket != null) {
						socket.close();
					}
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	// 通过包名检测系统中是否安装某个应用程序
	public static boolean checkApkExist(Context context, String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return false;
		}
		try {
			context.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

}
