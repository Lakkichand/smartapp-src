package com.youle.gamebox.ui.util;

import android.util.Log;
import com.youle.gamebox.ui.BuildConfig;

/**
 * Created by Administrator on 14-4-22.
 */
public class LOGUtil {
    public static void v(String tag,Object o){
        if(BuildConfig.DEBUG){
            Log.v(tag,o+"")  ;
        }
    }
    public static void d(String tag,Object o){
        if(BuildConfig.DEBUG){
            Log.d(tag,o+"")  ;
        }
    }
    public static void i(String tag,Object o){
        if(BuildConfig.DEBUG){
            Log.i(tag,o+"")  ;
        }
    }
    public static void w(String tag,Object o){
        if(BuildConfig.DEBUG){
            Log.w(tag,o+"")  ;
        }
    }
    public static void e(String tag,Object o){
        if(BuildConfig.DEBUG){
            Log.e(tag,o+"")  ;
        }
    }
}
