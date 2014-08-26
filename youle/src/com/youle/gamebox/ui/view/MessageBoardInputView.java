package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.api.pcenter.PCSignApi;
import com.youle.gamebox.ui.bean.User;
import com.youle.gamebox.ui.fragment.HomepageFragment;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.CodeCheck;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.TOASTUtil;
import org.json.JSONObject;

/**
 * Created by Administrator on 2014/5/22.
 */
public class MessageBoardInputView extends RelativeLayout implements View.OnClickListener {

    private Context mContext;

    @InjectView(R.id.message_edittext)
    EditText mMessageEditText;
    @InjectView(R.id.submit_button)
    Button mSubmitButton;


    public MessageBoardInputView(Context context) {
        super(context);
        init(context);
    }

    public MessageBoardInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MessageBoardInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.messageboard_input_item, this);
        ButterKnife.inject(this);
        setVisibility(VISIBLE);
        mSubmitButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mSubmitButton) {

        }
    }

}
