package com.youle.gamebox.ui.api;

import com.ta.util.http.RequestParams;

/**
 * Created by Administrator on 2014/5/26.
 */
public class TestRequestApi extends AbstractApi{

    @Override
    protected String getPath() {
        return "";
    }

    @Override
    public String getUrl() {
        return "http://192.168.0.165:8081/gamebox/home/spread/ordinary?yy=24";
    }

}

