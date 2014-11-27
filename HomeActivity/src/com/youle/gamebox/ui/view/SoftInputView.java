package com.youle.gamebox.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 14-7-15.
 */
public class SoftInputView extends RelativeLayout {
    private OnSoftInputShowListener listener;
    public  boolean isShowFace = false ;
    private int softH = 0 ;
    public void setListener(OnSoftInputShowListener listener) {
        this.listener = listener;
    }

    public interface OnSoftInputShowListener {
        public void onSoftInputShow();

        public void onSoftInputDissmiss();
    }

    public SoftInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (oldw > 0) {
            if (h > oldh) {
                if(h-oldh==softH) {
                    if (listener != null&&!isShowFace) {
                        listener.onSoftInputDissmiss();
                    }
                }
            } else {
                if(softH==0) {
                    softH = oldh - h;
                }
                if(oldh-h == softH) {
                    if (listener != null) {
                        listener.onSoftInputShow();
                    }
                }
            }
        }
    }
}
