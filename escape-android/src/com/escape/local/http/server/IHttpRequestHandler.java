package com.escape.local.http.server;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import com.escape.local.http.client.Gogl;

public class IHttpRequestHandler implements HttpRequestHandler {

	@Override
	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {
//		if (request.getRequestLine().getUri().contains("wandoujia")
//				|| request.getRequestLine().getUri().contains(".baidu.com")
//				|| request.getRequestLine().getUri().contains("360buy.com")
//				|| request.getRequestLine().getUri().contains(".3g.cn")
//				|| request.getRequestLine().getUri()
//						.contains("market.hiapk.com")
//				|| request.getRequestLine().getUri().contains("3g.163.com")
//				|| request.getRequestLine().getUri()
//						.contains("push.dopool.com")
//				|| request.getRequestLine().getUri()
//						.contains("glu-apac.s3.amazonaws.com")) {
//			return;
//		}
		new Gogl().excute(request, response, context);
		// TODO 这里是不是要调用close()?
	}

}
