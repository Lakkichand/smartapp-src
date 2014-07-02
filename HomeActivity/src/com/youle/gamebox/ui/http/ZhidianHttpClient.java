package com.youle.gamebox.ui.http;

import com.ta.TASyncHttpClient;
import com.ta.util.http.AsyncHttpClient;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 14-4-21.
 */
public class ZhidianHttpClient {
    public static final String TAG="ZhidianHttpClient" ;
    public static void request(AbstractApi api,JsonHttpListener listener){
        if(validateUrl(api.getUrl())) {
            AsyncHttpClient taSyncHttpClient = new AsyncHttpClient();
            listener.setAsyncHttpClient(taSyncHttpClient);
            LOGUtil.e(TAG, api.getHttpMethod() + ":" + api.getUrl() + "?" + api.getParams());
            if (api.getHttpMethod() == HttpMethod.POST) {
                taSyncHttpClient.post(api.getUrl(), api.getParams(), listener);
            } else {
                taSyncHttpClient.get(api.getUrl(), api.getParams(), listener);
            }
        }else{
            LOGUtil.i(TAG,"Not a url "+api.getUrl());
        }
    }

    private static boolean validateUrl(String url){
        synchronized (url) {
            if (url.startsWith("http://")) {
                return true;
            } else {
                return false;
            }
        }
    }
}
