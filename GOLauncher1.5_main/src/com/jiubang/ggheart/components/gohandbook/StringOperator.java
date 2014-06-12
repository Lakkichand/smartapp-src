package com.jiubang.ggheart.components.gohandbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import com.gau.utils.net.operator.IHttpOperator;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.BasicResponse;
import com.gau.utils.net.response.IResponse;

/**
 * 
 * <br>类描述: 解析NET请求返回的数据转换成字符串
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class StringOperator implements IHttpOperator {
	private final static int BYTE_SIZE = 1024;

	@Override
	public IResponse operateHttpResponse(THttpRequest request, HttpResponse response)
			throws IllegalStateException, IOException {
		String content = null;
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		ByteArrayOutputStream baopt = new ByteArrayOutputStream();
		byte[] buff = new byte[BYTE_SIZE];
		int len = -1;

		while ((len = is.read(buff)) != -1) {
			baopt.write(buff, 0, len);
		}
		content = new String(baopt.toString());
		BasicResponse ret = new BasicResponse(IResponse.RESPONSE_TYPE_STRING, content);
		return ret;
	}

}
