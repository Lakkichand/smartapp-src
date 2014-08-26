package com.youle.gamebox.ui.api.special;

import com.ta.util.http.RequestParams;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 2014/6/3.
 */
public class SpecialAdetailApi extends AbstractApi {
    @NoteParam
    String specialId;

    @Override
    protected String getPath() {
        return "/gamebox/special/" + specialId;
    }

    public void setSpecialId(String specialId) {
        this.specialId = specialId;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET ;
    }
}
