package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.util.LOGUtil;

import java.util.List;

/**
 * Created by Administrator on 2014/5/15.
 */
public class BaseSearchView extends LinearLayout{
    @InjectView(R.id.search_editText)
    EditText search_editText;
    @InjectView(R.id.search_but)
    ImageView search_but;
    PopupWindow pop;
    Context context;
    private boolean isVisiablePop = true;
    public BaseSearchView(Context context) {
        super(context);
        initView(context);
    }

    public BaseSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BaseSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public void setIsVisiablePop(boolean isVisiablePop){
        isVisiablePop = isVisiablePop;
    }

    public EditText getEditText(){
        return search_editText;
    }
    public ImageView getImageView(){
        return search_but;
    }

    private void initView(Context context){
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.search_base_layout, this);
        ButterKnife.inject(this, view);
        initPop(context);
        search_but.setOnClickListener(butOnclickListener);
        search_editText.addTextChangedListener(searchEditTextWatcher);
    }
    TextWatcher searchEditTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            String str = String.valueOf(charSequence);
            if(null != str || !"".equals(str))initPop(context);
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    OnClickListener butOnclickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    private void initPop(Context context) {
        if(!isVisiablePop){
            return;
        }
        View inflate = LayoutInflater.from(context).inflate(R.layout.testviewpagefragment_layout, null);
        pop = new PopupWindow(inflate, search_editText.getWidth(), LayoutParams.WRAP_CONTENT, true);
        pop.setOutsideTouchable(false);
        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.showAsDropDown(search_editText);
    }


}
