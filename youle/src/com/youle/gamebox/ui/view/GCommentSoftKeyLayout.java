package com.youle.gamebox.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 14-7-30.
 */
public class GCommentSoftKeyLayout extends LinearLayout {
    ISoftKeybordListener softKeybordListener ;

    public void setSoftKeybordListener(ISoftKeybordListener softKeybordListener) {
        this.softKeybordListener = softKeybordListener;
    }

    public interface ISoftKeybordListener {
        public void showScro(boolean show);
    }


    public GCommentSoftKeyLayout(Context context) {
        super(context);
    }

    public GCommentSoftKeyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldh == 0) {
        }else {
              if(h-oldh>0){//键盘消失
                 if(softKeybordListener!=null){
                     softKeybordListener.showScro(false);
                 }
              }else {
                  if(softKeybordListener!=null){
                      softKeybordListener.showScro(true);
                  }
              }
        }
    }
}
