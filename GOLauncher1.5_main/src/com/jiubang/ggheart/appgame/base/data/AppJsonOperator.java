/**
 * 
 */
package com.jiubang.ggheart.appgame.base.data;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONObject;

import com.gau.utils.net.operator.IHttpOperator;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.BasicResponse;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;

/**
 * @author liguoliang
 * 
 */
public class AppJsonOperator implements IHttpOperator {

	@Override
	public IResponse operateHttpResponse(THttpRequest request, HttpResponse response)
			throws IllegalStateException, IOException {
		HttpEntity entity = response.getEntity();
		InputStream is = entity.getContent();
		JSONObject json = DownloadUtil.parseMsgListStreamData(is, true);
		BasicResponse iResponse = new BasicResponse(IResponse.RESPONSE_TYPE_JSONOBJECT, json);
		return iResponse;
	}
}
