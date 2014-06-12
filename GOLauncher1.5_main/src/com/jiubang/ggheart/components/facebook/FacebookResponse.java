package com.jiubang.ggheart.components.facebook;

import com.gau.utils.net.response.IResponse;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-12-26]
 */
public class FacebookResponse implements IResponse {
	
	public static final int RESPONSE_TYPE_NORMAL =  1000;
	public static final int RESPONSE_TYPE_NODATA =  1001;
	public static final int RESPONSE_TYPE_LOCALISNEWEST = 1002;
	public static final int RESPONSE_TYPE_OG_SUCCESS = 1003;
	public static final int RESPONSE_TYPE_OG_FAILED = 1004;

	
	private int mType = RESPONSE_TYPE_NORMAL;
	private String mOpenGraphUrl; 
	
	public void setResponseType(int type) {
		mType = type;
	}

	@Override
	public Object getResponse() {
		return null;
	}

	@Override
	public int getResponseType() {
		return mType;
	}
	
	public String getOGUrl() {
		return mOpenGraphUrl;
	}
	
	public void setOGUrl(String url) {
		mOpenGraphUrl = url;
	}
}
