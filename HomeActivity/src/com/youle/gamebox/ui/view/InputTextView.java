package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 2014/6/6.
 */
public class InputTextView extends LinearLayout{

    @InjectView(R.id.input_left_image)
    ImageView mInputLeftImage;
    @InjectView(R.id.input_middle_edit)
    EditText mInputMiddleEdit;
    @InjectView(R.id.input_right_image)
    ImageView mInputRightImage;

    public InputTextView(Context context) {
        super(context);
        init(context);
    }

    public InputTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public InputTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.input_layout, this);
        ButterKnife.inject(this);
    }

    public ImageView getmInputLeftImage() {
        return mInputLeftImage;
    }

    public EditText getmInputMiddleEdit() {
        return mInputMiddleEdit;
    }

    public ImageView getmInputRightImage() {
        return mInputRightImage;
    }
}
