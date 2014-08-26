package com.youle.gamebox.ui.http;

import org.apache.http.Header;

/**
 * Created by Administrator on 14-5-9.
 */
public interface IUIWhithNetListener {
    public void onLoadStart() ;

    public void onSuccess(String content) ;

    public void onFailure(Throwable error) ;

}
