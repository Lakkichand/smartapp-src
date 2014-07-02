package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 2014/5/27.
 */
public class PCenterMyGameOptionView extends LinearLayout implements View.OnClickListener {
    @InjectView(R.id.pc_mygame_option_gzone)
    LinearLayout mPcMygameOptionGzone;
    @InjectView(R.id.pc_mygame_option_radius)
    LinearLayout mPcMygameOptionRadius;
    @InjectView(R.id.pc_mygame_option_packs)
    LinearLayout mPcMygameOptionPacks;
    @InjectView(R.id.pc_mygame_option_forum)
    LinearLayout mPcMygameOptionForum;
    @InjectView(R.id.pc_mygame_option_uninstall)
    LinearLayout mPcMygameOptionUninstall;
    @InjectView(R.id.pc_mygame_option_linear)
    LinearLayout mPcMygameOptionLinear;
    Context mcContext;

    public PCenterMyGameOptionView(Context context) {
        super(context);
        mcContext = context;
        initView(context);
    }

    public PCenterMyGameOptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public PCenterMyGameOptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context){
       LayoutInflater.from(context).inflate(R.layout.pcenter_layout_mygame_option, this);
       ButterKnife.inject(this);
        mPcMygameOptionGzone.setOnClickListener(this);
        mPcMygameOptionPacks.setOnClickListener(this);
        mPcMygameOptionRadius.setOnClickListener(this);
        mPcMygameOptionForum.setOnClickListener(this);
        mPcMygameOptionUninstall.setOnClickListener(this);
    }

    // visable onclick
    public void setOnclick(boolean onclick){
        if(!onclick)mPcMygameOptionLinear.setBackgroundColor(Color.BLUE);
        mPcMygameOptionGzone.setClickable(onclick);
        mPcMygameOptionGzone.setClickable(onclick);
        mPcMygameOptionPacks.setClickable(onclick);
        mPcMygameOptionRadius.setClickable(onclick);
        mPcMygameOptionForum.setClickable(onclick);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(mcContext,"onclicl",Toast.LENGTH_SHORT).show();
        if(view == mPcMygameOptionGzone){

        }else if(view == mPcMygameOptionPacks){

        }else if(view == mPcMygameOptionRadius){

        }else if(view == mPcMygameOptionForum){

        }else if(view == mPcMygameOptionUninstall){

        }

    }
}
