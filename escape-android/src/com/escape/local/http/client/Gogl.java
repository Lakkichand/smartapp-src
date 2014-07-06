package com.escape.local.http.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpClientConnection;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.escape.message.PackageRequest;
import com.escape.message.PackageResponse;
import com.smartapp.escapeandroid.nativeutil.NativeUtil;

public class Gogl {
    // TODO 每个方法的exception是在本地处理还是抛出？
    private HttpParams mParams;
    // private HttpParams mDirectParams;
    private HttpProcessor mHttpproc;
    private HttpRequestExecutor mHttpexecutor;
    private HttpContext mClientContext;// 这个是作为client的context，要跟作为服务器的context区分开
    // private HttpContext mDirectClientContext;
    private HttpHost mHost;
    // 每一个Gogl对象都维护一个GAEConn长连接,GAEConn维护着一个连接到GAE的socket连接
    private DefaultHttpClientConnection mGAEConn;
    private ConnectionReuseStrategy mConnStrategy;
    private SSLSocketFactory mFactory;

	private String mAppId = NativeUtil.getRandomAppId();

    public Gogl() {
	init();
    }

    private void init() {
	// mDirectParams = new SyncBasicHttpParams();
	mParams = new BasicHttpParams();
	HttpProtocolParams.setVersion(mParams, HttpVersion.HTTP_1_1);
	HttpProtocolParams.setContentCharset(mParams, "UTF-8");
	HttpProtocolParams.setUserAgent(mParams, "HttpComponents/1.1");
	// HttpProtocolParams.setUseExpectContinue(mParams, true);

	mHttpproc = new ImmutableHttpProcessor(new HttpRequestInterceptor[] {
		// Required protocol interceptors
		new RequestContent(), new RequestTargetHost(),
		// Recommended protocol interceptors
		new RequestConnControl(), new RequestUserAgent(), new RequestExpectContinue() });

	mHttpexecutor = new HttpRequestExecutor();
	mClientContext = new BasicHttpContext(null);
	// mDirectClientContext = new BasicHttpContext(null);
//	 mHost = new HttpHost("sx20100708.appspot.com");
	mHost = new HttpHost(mAppId + ".appspot.com");
	// mHost = new HttpHost("localhost", 8888);
	mGAEConn = new DefaultHttpClientConnection();
	mConnStrategy = new DefaultConnectionReuseStrategy();
	mFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

	mClientContext.setAttribute(ExecutionContext.HTTP_CONNECTION, mGAEConn);
	mClientContext.setAttribute(ExecutionContext.HTTP_TARGET_HOST, mHost);

	// mDirectClientContext.setAttribute(ExecutionContext.HTTP_CONNECTION,
	// mGAEConn);
    }

    private void setError(HttpResponse response, String message) {
	response.setStatusCode(500);
	response.addHeader(HttpHeaders.CONNECTION, HTTP.CONN_CLOSE);
	response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=iso-8859-1");
	response.addHeader("Server", "HttpComponents/1.1");
	message = "<p>" + message + "</p>";
	response.addHeader(HttpHeaders.CONTENT_LENGTH, message.getBytes().length + "");
	try {
	    response.setEntity(new StringEntity(message, "UTF-8"));
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	    return;
	}
    }

    private boolean rangefetch(PackageRequest packReq, PackageResponse packResp, HttpContext context) {
	HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
	System.err.println("range fetch start>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//		Log.e("Test", "url = " + request.getRequestLine().getUri());
//		Log.e("Test", "url = " + packReq.getUri());
//		Log.e("Test", "request = " + request.toString());
//		Log.e("Test", "request = " + packReq.toString());
	int[] m = packResp.parseContentRange();
	if (m == null) {
	    System.err.println("rangefetch end 1");
	    return false;
	}
	System.err.println("ContentRange = " + packResp.getHeaders(HttpHeaders.CONTENT_RANGE)[0].getValue());
	System.err.println("m[0] = " + m[0] + "  m[1] = " + m[1] + "  m[2] = " + m[2]);
	int start = m[0];
	int end = m[2];
	/*
	 * if (packReq.containsHeader(HttpHeaders.RANGE) &&
	 * (!packReq.isAddedRangeHeader())) {// range头是浏览器设的
	 */
//	if (request.containsHeader(HttpHeaders.RANGE)) {
//	    int[] req_range = packReq.parseRange();
//	    if (req_range != null) {
//		if (req_range[0] == -1) {
//		    if (req_range[1] != -1) { // bytes=-xxxx 表示最后xxxx个字节
//			if (m[1] - m[0] + 1 == req_range[1] && m[1] + 1 == m[2]) {
//			    System.err.println("rangefetch end 2");
//			    return false;
//			}
//			if (m[2] >= req_range[1]) {
//			    start = m[2] - req_range[1];// m[2]-req_range[1]至m[2]就是最后req_range[1]个字节
//			}
//		    }
//		} else {
//		    start = req_range[0];
//		    if (req_range[1] != -1) { // bytes xxxx-xxxx x1到x2字节
//			if (m[0] == req_range[0] && m[1] == req_range[1]) {
//			    System.err.println("rangefetch end 3");
//			    return false;
//			}
//			if (end > req_range[1]) {
//			    end = req_range[1];
//			}
//		    }
//		}
//	    }
//	    packResp.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + m[2]);
//	} else if (start == 0) {// 这种情况表明range头是在服务器那里加上去的或在adjustRequest的时候加上去的
	    packResp.setResponseCode(200);
	    packResp.removeHeaders(HttpHeaders.CONTENT_RANGE);
	    packResp.setMsg("OK");
//	}
	System.err.println("start = " + start + "   end = " + end);
	packResp.setHeader(HttpHeaders.CONTENT_LENGTH, (end - start) + "");
	System.err.println("CONTENT_LENGTH = " + packResp.getHeaders(HttpHeaders.CONTENT_LENGTH)[0].getValue());
	long partSize = GoglHelper.RANGE_SIZE;

	HttpServerConnection conn = (HttpServerConnection) context.getAttribute(ExecutionContext.HTTP_CONNECTION);
	HttpResponse response = (HttpResponse) context.getAttribute(ExecutionContext.HTTP_RESPONSE);
	packResp.toHttpResponse(response);
	start = m[1] + 1;
	try {
			Log.e("Test", "###$$$ " + response.getStatusLine().toString());
			for (Header header : response.getAllHeaders()) {
				Log.e("Test",
						"###$$$ " + header.getName() + " : "
								+ header.getValue());
			}
	    conn.sendResponseHeader(response);
	    Log.e("Test", "###$$$  conn.sendResponseHeader(response)");
	    HttpEntity entity = response.getEntity();
	    if (entity != null) {
		System.err.println("origin partsize = " + GoglHelper.RANGE_SIZE);
		partSize = entity.getContentLength();
		System.err.println("fixed partsize = " + partSize);
		conn.sendResponseEntity(response);
		Log.e("Test", "###$$$  conn.sendResponseEntity(response)");
	    }
	    conn.flush();
	} catch (HttpException e) {
	    // TODO 这里有exception怎么办？其他地方的exception处理恰当吗？
	    e.printStackTrace();
	    System.err.println("rangefetch end 4");
	    return true;
	} catch (IOException e) {
	    // TODO 这里有exception怎么办？其他地方的exception处理恰当吗？
	    e.printStackTrace();
	    System.err.println("rangefetch end 5");
	    return true;
	}
	int failed = 0;
	while (start < end) {
			Log.e("Test", "rangefetch loop url = " + packReq.getUri());
	    System.err.println("start = " + start + "  end = " + end);
	    if (failed > 8) {
		System.err.println("rangefetch end 6");
		return true;
	    }
	    long tail = start + partSize - 1;
	    if (tail > end - 1) {
		tail = end - 1;
	    }
	    packReq.setHeader(HttpHeaders.RANGE, "bytes=" + start + "-" + tail);
	    System.err.println(HttpHeaders.RANGE + ":" + packReq.getFirstHeader(HttpHeaders.RANGE).getValue());
	    PackageResponse packResp_ = fetch(packReq);
	    if (packResp_ == null || packResp_.getResponseCode() >= 400 || packResp_.isError()) {
		failed++;
		if (packResp_ == null) {
		    System.out.println("packResp_ == null");
		} else if (packResp_.getResponseCode() >= 400) {
		    System.out.println("packResp_.getResponseCode() = " + packResp_.getResponseCode());
		} else if (packResp_.isError()) {
		    System.out.println("packResp_.isError()");
		}
//		try {
//		    Thread.sleep(1000);
//		} catch (InterruptedException e) {
//		    e.printStackTrace();
//		}
		continue;
	    }
	    if (packResp_.getResponseCode() == 302 && packResp_.containsHeader(HttpHeaders.LOCATION)) {
		System.err.println("getResponseCode() == 302  location = " + packResp_.getFirstHeader(HttpHeaders.LOCATION).getValue());
		packReq.setUri(packResp_.getFirstHeader(HttpHeaders.LOCATION).getValue());
		continue;
	    }
	    int[] tm = packResp_.parseContentRange();
	    if (tm == null || tm[0] != start) {
		System.err.println("tm == null || tm[0] != start");
		failed++;
		continue;
	    }
	    System.err.println(HttpHeaders.CONTENT_RANGE + ":" + packResp_.getFirstHeader(HttpHeaders.CONTENT_RANGE).getValue());
	    start = tm[1] + 1;
	    failed = 0;
	    try {
		byte[] content = packResp_.getEnity();
		if (content != null) {
		    response.setEntity(new ByteArrayEntity(content));
		    conn.sendResponseEntity(response);
		    conn.flush();
		}
	    } catch (HttpException e) {
		// TODO 这里有exception怎么办？其他地方的exception处理恰当吗？
		e.printStackTrace();
		System.err.println("rangefetch end 9");
		return true;
	    } catch (IOException e) {
		// TODO 这里有exception怎么办？其他地方的exception处理恰当吗？
		e.printStackTrace();
		System.err.println("rangefetch end 10");
		return true;
	    }
	}
	System.err.println("rangefetch end normally");
	return true;
    }

    private PackageResponse _fetch(PackageRequest packReq, String fetchhost, String fetchserver) {
	if (!mGAEConn.isOpen()) {
	    Socket socket;
	    try {
		socket = GoglHelper.getAnSSLSocket(mFactory);
//		 socket = GoglHelper.getASocket();
		mGAEConn.bind(socket, mParams);
	    } catch (IOException e) {
		e.printStackTrace();
		return null;
	    }
	}
	byte[] packet = packReq.packaged();
	if (packet == null) {
	    return null;
	}
	BasicHttpEntityEnclosingRequest gRequest = new BasicHttpEntityEnclosingRequest("POST", "/fetch");
	gRequest.setEntity(new ByteArrayEntity(packet));
	gRequest.setParams(mParams);
	gRequest.setHeader(HttpHeaders.CONNECTION, HTTP.CONN_CLOSE);

	HttpResponse gResponse = null;
	try {
	    mHttpexecutor.preProcess(gRequest, mHttpproc, mClientContext);
	     System.err.println("$$$Gogl request$$$ " + gRequest.toString()
	     + "\n");
	    long t1 = System.currentTimeMillis();
	    gResponse = mHttpexecutor.execute(gRequest, mGAEConn, mClientContext);
	    long t2 = System.currentTimeMillis();
	    System.err.println("从服务器拿数据的时间:<<" + ">>"
	    + (t2 - t1) + "ms");
	    gResponse.setParams(mParams);
	    long x1 = System.currentTimeMillis();
	    mHttpexecutor.postProcess(gResponse, mHttpproc, mClientContext);
	    long x2 = System.currentTimeMillis();
	    System.out.println("postProcess 耗时" + (x2 - x1) + "ms");

	    HttpEntity entity = gResponse.getEntity();
	    byte[] result = EntityUtils.toByteArray(entity);
	    long t3 = System.currentTimeMillis();
	    System.out.println("gResponse.getEntity耗时" + (t3 - t2) + "ms");
	    
	    if (!mConnStrategy.keepAlive(gResponse, mClientContext) || !mGAEConn.isOpen() || !mGAEConn.isStale()) {
		try {
		    mGAEConn.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	    // byte[] content = MessageHelper.decompress(result);
	    byte[] content = result;
	    PackageResponse ret = PackageResponse.getInstance(content);
	    if (ret != null) {
		// System.err.println(ret.getMsg());
	    }
	    long t4 = System.currentTimeMillis();
	    System.out.println("PackageResponse.getInstance耗时" + (t4 - t3) + "ms");
	    return ret;
	} catch (HttpException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (Throwable e) {
	    e.printStackTrace();
	}
	return null;
    }

    private PackageResponse fetch(PackageRequest packReq) {
	String fetchhost = mAppId + ".appspot.com";
	String fetchserver = "http://" + fetchhost + "/fetch";
	return _fetch(packReq, fetchhost, fetchserver);
    }

    public void excute(HttpRequest request, HttpResponse response, HttpContext context) {
	PackageRequest packReq = PackageRequest.getInstance(request);
	if (packReq == null) {
	    setError(response, "PackageRequest.getInstance = null");
	    return;
	}
	GoglHelper.adjustRequest(packReq, context);
	System.err.println(packReq);
	PackageResponse packResp = fetch(packReq);
	if (packResp == null) {
	    setError(response, "PackageResponse = null");
	    return;
	}
	System.out.println("<<URL = " + packReq.getUri() + " >>" + packResp);
	if (packResp.isError()) {
	    // System.out
	    // .println("<<URL = " + packReq.getUri() + " >>" + packResp);
	    setError(response, "Server ERROR : " + packResp.getMsg());
	    return;
	}
	if (packResp.getResponseCode() == 206 && packReq.getMethod().equalsIgnoreCase("get")) {
	    // System.out
	    // .println("<<URL = " + packReq.getUri() + " >>" + packResp);
	    if (rangefetch(packReq, packResp, context)) { // TODO
							  // rResponse已经在rangefetch中消耗了，这里要怎么做？
		System.err.println("<<<<<<<<<<<<<<<<<<<<<<rangefetch end !");
		response.removeHeaders(GoglHelper.RESPONSE_USED_UP);
		response.setHeader(GoglHelper.RESPONSE_USED_UP, "true");
		return;
	    }
	}
	byte[] content = packResp.getEnity();
	if (content != null) {
	    packResp.setHeader(HttpHeaders.CONTENT_LENGTH, content.length + "");
	} else {
	    packResp.setHeader(HttpHeaders.CONTENT_LENGTH, 0 + "");
	}
	// System.out.println("<<URL = " + packReq.getUri() + " >>" + packResp);
	packResp.toHttpResponse(response);
    }

    public void close() {
	// TODO 这里为什么没有被调用
	if (mGAEConn != null && mGAEConn.isOpen()) {
	    try {
		mGAEConn.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }
}
