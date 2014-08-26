package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

public class NewsListApi extends AbstractApi {
	
	@Override
	protected String getPath() {
		return "gamebox/news/list";
	}
	@Override
	public HttpMethod getHttpMethod() {
		return HttpMethod.GET;
	}

}
