package com.youle.gamebox.ui.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.*;
import butterknife.ButterKnife;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 2014/5/14.
 */
public abstract class BaseDialogView extends Dialog{
    protected  Context mContext;
    protected BaseDialogView(Context context) {
        super(context, R.style.style_basedialog);
        mContext = context;
        showDialog(context);
    }


    public void showDialog(Context context) {
        View view = LayoutInflater.from(context).inflate(getDialogView(), null);
        ButterKnife.inject(this,view);
        this.setContentView(view);
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER_VERTICAL);
        lp.alpha = 1.0f; // 透明度
        WindowManager m = ((Activity) context).getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        lp.height = (int) (d.getHeight()); // 高度设置为屏幕的0.9
        lp.width = (int) (d.getWidth()); // 宽度设置为屏幕的0.65
        lp.y=0;
        dialogWindow.setAttributes(lp);
        this.setCancelable(true);
        this.show();
        getDialogView(view);
    }

    public abstract int getDialogView();
    public abstract void getDialogView(View view);
    public interface DialogOnListener{
        void onclick(int click);
    }
}

