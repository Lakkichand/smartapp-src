package com.youle.gamebox.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.person.MsgboardReplyApi;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.MyTextUtil;
import com.youle.gamebox.ui.util.SoftkeyboardUtil;
import com.youle.gamebox.ui.util.TOASTUtil;
import com.youle.gamebox.ui.view.SoftInputView;

import static com.youle.gamebox.ui.view.SoftInputView.OnSoftInputShowListener;

/**
 * Created by Administrator on 14-7-14.
 */
public class CommentMsgBoardActivity extends BaseActivity implements OnSoftInputShowListener{
    @InjectView(R.id.message_edittext)
    EditText mMessageEdittext;
    @InjectView(R.id.submit_button)
    Button mSubmitButton;
    @InjectView(R.id.messageborad_layout)
    RelativeLayout mMessageboradLayout;
    MsgboardReplyApi msgboardReplyApi ;
    @InjectView(R.id.softView)
    SoftInputView mSoftView;
    private String nikeName ;
    private long id ;
    public static final String NIKE_NAME = "nickName";
    public static final String ID= "id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg_board_layout);
        ButterKnife.inject(this);
        nikeName = getIntent().getStringExtra(NIKE_NAME);
        id = getIntent().getLongExtra(ID,-1);
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        mSoftView.setListener(this);
        mSoftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                SoftkeyboardUtil.hideSoftKeyBoard(CommentMsgBoardActivity.this,mMessageEdittext);
            }
        });
        mMessageEdittext.setHint("对 " + nikeName + "说:");
        SoftkeyboardUtil.showSoftKeyBoard(this, mMessageEdittext);
        msgboardReplyApi = new MsgboardReplyApi();
        msgboardReplyApi.setSid(userInfo.getSid());
        msgboardReplyApi.setMid(id + "");
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String content = mMessageEdittext.getText().toString();
                if (MyTextUtil.isEmpty(content)) {
                    TOASTUtil.showSHORT(CommentMsgBoardActivity.this, "回复内容不能为空");
                    SoftkeyboardUtil.hideSoftKeyBoard(CommentMsgBoardActivity.this, mMessageEdittext);
                    return;
                }
                msgboardReplyApi.setContent(mMessageEdittext.getText().toString());
                ZhidianHttpClient.request(msgboardReplyApi, new JsonHttpListener(CommentMsgBoardActivity.this) {
                    @Override
                    public void onResultFail(String jsonString) {
                        super.onResultFail(jsonString);
                    }

                    @Override
                    public void onRequestSuccess(String jsonString) {
                        super.onRequestSuccess(jsonString);
                        finish();
                        SoftkeyboardUtil.hideSoftKeyBoard(CommentMsgBoardActivity.this,mMessageEdittext);
                    }
                });
            }
        });
    }
    public static  void startMsgComment(Context context,String nikeName,long id ){
        Intent intent = new Intent(context,CommentMsgBoardActivity.class);
        intent.putExtra(NIKE_NAME,nikeName);
        intent.putExtra(ID,id);
        context.startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SoftkeyboardUtil.showSoftKeyBoard(this,mMessageEdittext);
    }

    @Override
    public void onSoftInputShow() {

    }

    @Override
    public void onSoftInputDissmiss() {
        finish();
    }
}
