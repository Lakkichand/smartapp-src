package com.youle.gamebox.ui.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Administrator on 2014/6/13.
 */
public class TOASTUtil {
    public static void showLONG(Context context,int stringId){
        String string = context.getString(stringId);
        Toast.makeText(context,string,Toast.LENGTH_LONG).show();
    }
    public static void showSHORT(Context context,int stringId){
        String string = context.getString(stringId);
        Toast.makeText(context,string,Toast.LENGTH_SHORT).show();
    }
    public static void showSHORT(Context context,String text){
        Toast.makeText(context,text,Toast.LENGTH_SHORT).show();
    }
}
