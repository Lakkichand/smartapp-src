package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 2014/6/3.
 */
public class HeadOnclickView extends LinearLayout{
    @InjectView(R.id.headonclick_layout_text)
    TextView mHeadonclickLayoutText;
    @InjectView(R.id.headonclick_layout_falg)
    TextView mHeadonclickLayoutFalg;
    @InjectView(R.id.headonclick_layout_view)
    View mHeadonclickLayoutView;
    @InjectView(R.id.headonclick_layout_onclick)
    LinearLayout mHeadonclickLayoutOnclick;

    public HeadOnclickView(Context context) {
        super(context);
        init(context);
    }

    public HeadOnclickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public HeadOnclickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.headonclick_layout,this);
        ButterKnife.inject(this);
        setVisiableView(View.INVISIBLE);
        setVisiableFalg(View.INVISIBLE);

    }

    public void setVisiableView(int falg){
        mHeadonclickLayoutView.setVisibility(falg);
    }
    public void setVisiableFalg(int falg){
        mHeadonclickLayoutFalg.setVisibility(falg);
    }
    public void setFalgText(String text){
        mHeadonclickLayoutFalg.setVisibility(View.VISIBLE);
        mHeadonclickLayoutFalg.setText(text);
    }
    public void setTitleText(String text){
        mHeadonclickLayoutText.setVisibility(View.VISIBLE);
        mHeadonclickLayoutText.setText(text);
    }
}
