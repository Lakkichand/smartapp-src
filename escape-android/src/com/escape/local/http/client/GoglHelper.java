package com.escape.local.http.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.escape.message.PackageRequest;
import com.smartapp.escapeandroid.nativeutil.NativeUtil;

public class GoglHelper {
    public static final int RANGE_SIZE = 1024 * 1024;

    public static final String RESPONSE_USED_UP = "response.useup";
    public static final String HTTP_SSL_HOST = "http.ssl.host";
    public static final String HTTP_SSL_PORT = "http.ssl.port";
    public static final String IS_CONNECT_REQUEST = "is.connect.request";

    private static final String[] SKIP_HEADERS = { "host", "vary", "via", "x-forwarded-for", "proxy-authorization", "proxy-connection", "upgrade", "keep-alive" };
    private static final String[] AUTO_RANGE_HOSTS = { ".+\\.youtube\\.com", ".+\\.googlevideo\\.com", "av\\.vimeo\\.com", ".+\\.mediafire\\.com", ".+\\.filesonic\\.com",
	    ".+\\.filesonic\\.jp", "smile-.+\\.nicovideo\\.jp", "video\\..+\\.fbcdn\\.net" };
    private static final String[] AUTO_RANGE_TAIL = { ".7z", ".zip", ".rar", ".bz2", ".tar", ".wmv", ".avi", ".flv", ".hlv", ".mp4",".apk" };

    public static String getRequestHost(PackageRequest request, HttpContext context) {
	String host = null;
	Header[] headers = request.getHeaders(HttpHeaders.HOST);
	if (headers != null && headers.length > 0) {
	    return headers[0].getValue();
	}
	host = (String) context.getAttribute(HTTP_SSL_HOST);
	if (host == null) {
	    try {
		URL url = new URL(request.getUri());
		host = url.getHost();
	    } catch (MalformedURLException e) {
		e.printStackTrace();
	    }
	}
	return host;
    }

    public static void adjustRequest(PackageRequest packReq, HttpContext context) {
	if(packReq == null){
	    return;
	}
	String host = getRequestHost(packReq, context);
	if (packReq.getUri().startsWith("/")) {
	    Boolean isHttpsRequest = (Boolean) context.getAttribute(GoglHelper.IS_CONNECT_REQUEST);
	    if (isHttpsRequest == null) {
		isHttpsRequest = false;
	    }
	    String sheme = isHttpsRequest ? "https://" : "http://";
	    packReq.setUri(sheme + host + packReq.getUri());
	}
	for (String header : SKIP_HEADERS) {
	    packReq.removeHeaders(header);
	}
	packReq.setHeader(HttpHeaders.CONNECTION, HTTP.CONN_CLOSE);
	if (packReq.containsHeader(HttpHeaders.RANGE)) {
	    // TODO 这里如果请求的长度范围超过了最大长度怎么办？好像服务器有处理
//		int[] range = packReq.parseRange();
//		if(range[0] != -1 && range[1] == -1){
//			packReq.setHeader(HttpHeaders.RANGE, "bytes=" + range[0] + "-" + (range[0] + RANGE_SIZE));
//				System.err.println("调整 Range : "
//						+ packReq.getHeaders(HttpHeaders.RANGE)[0].getValue());
//		}
	    return;
	}
	// try {
	// URL url = new URL(packReq.getUri());
	// String path = url.getPath();
	// for (String suffix : AUTO_RANGE_TAIL) {
	// if (path.endsWith(suffix)) {
	// packReq.addHeader(HttpHeaders.RANGE, "bytes=0" + "-"
	// + RANGE_SIZE);//TODO 这里不一定从0开始吧?
	// System.out
	// .println("adjustRequest match AUTO_RANGE_TAIL url = "
	// + packReq.getUri());
	// packReq.performAddRangeHeader();
	// return;
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// return;
	// }
//	for (String regex : AUTO_RANGE_HOSTS) {
//	    if (host.matches(regex)) {
		try {
		    URL url = new URL(packReq.getUri());
		    String host_ = url.getHost();
		    if(host_.contains("googlevideo")){
		    	packReq.addHeader(HttpHeaders.RANGE, "bytes=0" + "-" + RANGE_SIZE);
		    	packReq.performAddRangeHeader();
				return;
		    }
		    String path = url.getPath();
		    for (String suffix : AUTO_RANGE_TAIL) {
			if (path.endsWith(suffix)) {
			    packReq.addHeader(HttpHeaders.RANGE, "bytes=0" + "-" + RANGE_SIZE);
			    Log.e("Test", "adjustRequest match AUTO_RANGE_TAIL url = " + packReq.getUri());
			    packReq.performAddRangeHeader();
			    return;
			}
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		    return;
		}
		// packReq.addHeader(HttpHeaders.RANGE, "bytes=0" + "-"
		// + RANGE_SIZE);
		// packReq.performAddRangeHeader();
		return;
	    }
//	}
//    }

    private static HandshakeCompletedListener mHandshakeCompletedListener = new HandshakeCompletedListener() {

	@Override
	public void handshakeCompleted(HandshakeCompletedEvent event) {
	    // TODO 尽量减少SSL握手的次数
	    System.err.println("\n|||||||加密套件：" + event.getCipherSuite() + " 会话：" + event.getSession() + " 对方：" + event.getSession().getPeerHost() + "|||||||\n");
	}
    };

    protected static SSLSocket getAnSSLSocket(SSLSocketFactory factory) throws IOException {
	// TODO set timeout,look at the http server's timeout
	SSLSocket socket = (SSLSocket) factory.createSocket();
//	boolean success = false;
//	for (int i = 131; i <= 138; i++) {
//	    String addr = "203.208.46." + i;
//	    try {
//		socket.connect(new InetSocketAddress(addr, 443));
//		success = true;
//	    } catch (Exception e) {
//		e.printStackTrace();
//	    }
//	    if (success) {
//		break;
//	    }
//	}
//	if (!success) {
	    socket.connect(new InetSocketAddress(NativeUtil.getEngineIp(), 443));
//	}
	// socket.addHandshakeCompletedListener(mHandshakeCompletedListener);
	String[] sup = socket.getSupportedCipherSuites();
	socket.setEnabledCipherSuites(sup);
	socket.setSoTimeout(60000);
	return socket;
    }

    public static SSLSocket getWrapSocket(Socket socket, SSLSocketFactory factory) throws IOException {
	SSLSocket wrapSocket = (SSLSocket) factory.createSocket(socket, socket.getInetAddress().getHostAddress(), socket.getPort(), false);
	wrapSocket.setUseClientMode(false);
	String[] sup = wrapSocket.getSupportedCipherSuites();
	wrapSocket.setEnabledCipherSuites(sup);
	// wrapSocket.addHandshakeCompletedListener(mHandshakeCompletedListener);
	return wrapSocket;
    }

    protected static Socket getASocket() throws UnknownHostException, IOException {
	Socket socket = new Socket("203.208.46.137", 80);
	// Socket socket = new Socket("localhost", 8888);
	return socket;
    }
}
