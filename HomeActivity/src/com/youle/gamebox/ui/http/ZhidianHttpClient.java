package com.youle.gamebox.ui.http;

import com.loopj.android.http.AsyncHttpClient;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.util.LOGUtil;

import java.io.IOException;

/**
 * Created by Administrator on 14-4-21.
 */
public class ZhidianHttpClient {
    public static final String TAG="ZhidianHttpClient" ;
    public static void request(AbstractApi api,JsonHttpListener listener) {
        if(validateUrl(api.getUrl())) {
            AsyncHttpClient taSyncHttpClient = new AsyncHttpClient();
            listener.setAsyncHttpClient(taSyncHttpClient);
            LOGUtil.e(TAG, api.getHttpMethod() + ":" + api.getUrl() + "?" + api.getParams());
            if (api.getHttpMethod() == HttpMethod.POST) {
//                try {
//                    taSyncHttpClient.post(null,api.getUrl(),api.getParams().getEntity(listener),api.getContentType(),listener);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                taSyncHttpClient.post(api.getUrl(),api.getParams(),listener);
//                taSyncHttpClient.post(api.getUrl(),api.getParams(),listener);
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
