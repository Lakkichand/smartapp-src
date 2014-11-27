package com.youle.gamebox.ui.util;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-6-24.
 */
public class UIUtil {
    public static LinearLayout.LayoutParams getLineLayoutParam(Context context){
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,10);
        p.setMargins(0,context.getResources().getDimensionPixelOffset(R.dimen.game_detail_provider),0,0);
        return  p;
    }
    public static void toast(Context context,String content ){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show();
    }
    public static void toast(Context context,int content ){
        Toast.makeText(context,content,Toast.LENGTH_SHORT).show();
    }
}
