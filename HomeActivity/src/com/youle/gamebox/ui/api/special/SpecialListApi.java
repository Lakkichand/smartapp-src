package com.youle.gamebox.ui.api.special;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 2014/6/3.
 */
public class SpecialListApi extends AbstractApi{
    @Override
    protected String getPath() {
        return "/gamebox/special";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET ;
    }
}
