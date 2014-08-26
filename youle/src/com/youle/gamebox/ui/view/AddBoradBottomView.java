package com.youle.gamebox.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.util.UIUtil;

/**
 * Created by Administrator on 14-7-28.
 */
public class AddBoradBottomView extends LinearLayout implements View.OnClickListener{
    @InjectView(R.id.message_edittext)
    EditText mMessageEdittext;
    @InjectView(R.id.submit_button)
    Button mSubmitButton;
    @InjectView(R.id.messageborad_layout)
    RelativeLayout mMessageboradLayout;
    private ISendListener sendListener ;
    public interface ISendListener{
        public void send(String content) ;
    }
    public AddBoradBottomView(Context context,ISendListener listener) {
        super(context);
        this.sendListener = listener;
        LayoutInflater.from(context).inflate(R.layout.add_bord_msg_layout, this);
        ButterKnife.inject(this);
        mSubmitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(TextUtils.isEmpty(mMessageEdittext.getText().toString())){
            UIUtil.toast(getContext(),R.string.boad_msg_not_null);
        }else {
            if(sendListener!=null){
                sendListener.send(mMessageEdittext.getText().toString());
            }
        }
    }
    public void clean(){
        mMessageEdittext.setText("");
    }
}
