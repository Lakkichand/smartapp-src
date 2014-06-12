package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;  
import android.util.AttributeSet;  
import android.widget.TextView;  

  /**
   * zhujian
   * 该textview将一直获得焦点，用于跑马灯效果
   */

public class ScrollForeverTextView extends TextView {  
  
    public ScrollForeverTextView(Context context) {  
        super(context);  
        // TODO Auto-generated constructor stub  
    }  
  
    public ScrollForeverTextView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public ScrollForeverTextView(Context context, AttributeSet attrs,  
            int defStyle) {  
        super(context, attrs, defStyle);  
    }  
  
    @Override  
    public boolean isFocused() {  
        return true;  
    } 
}