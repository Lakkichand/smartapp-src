package com.youle.gamebox.ui.view;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 2014/5/13.
 */
public class GameTitleBarView extends BaseTitleBarView {
    public final  static int _leftBut = 0 ;
    public final  static int _rightBut = 0 ;
    TitlebaOnlickListener titlebaOnlickListener = null;
    public GameTitleBarView(Context context) {
        super(context);
        initView();
    }

    public GameTitleBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public GameTitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private void initView(){
        setTitleBarLeftView(null);
        setTitleBarMiddleView(null,"");
        setTitleBarRightView(null);

    }

    @Override
    protected void left_ButBack() {
        if(titlebaOnlickListener!=null){
            titlebaOnlickListener.onclickBack(_leftBut);
        }
        LOGUtil.d("junjun", "back 统一处理");
        FragmentManager supportFragmentManager = ((BaseActivity) mContext).getSupportFragmentManager();
        if(supportFragmentManager.getBackStackEntryCount()>1){
            supportFragmentManager.popBackStack();
        }else{
            ((BaseActivity) mContext).finish();
        }

    }

    @Override
    protected void right_ButBack() {
        if(titlebaOnlickListener!=null){
            titlebaOnlickListener.onclickBack(_rightBut);
        }else{
            LOGUtil.d("junjun","seearch 统一处理");
        }
    }



    public void setTitlebaOnlickListener(TitlebaOnlickListener titlebaOnlickListener ){
        this.titlebaOnlickListener = titlebaOnlickListener;
    }
    public interface TitlebaOnlickListener{
        void onclickBack(int onclickFalg);
    }

}
