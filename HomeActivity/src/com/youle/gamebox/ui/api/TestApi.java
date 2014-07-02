package com.youle.gamebox.ui.api;

/**
 * Created by Administrator on 14-4-23.
 */
public class TestApi extends AbstractApi {
    private int id ;
    private String name ;
    @Override
    protected String getPath() {
        return "http://www.baidu.com";
    }
}
