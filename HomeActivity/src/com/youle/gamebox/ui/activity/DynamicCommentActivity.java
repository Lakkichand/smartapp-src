package com.youle.gamebox.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.dynamic.DymaicCommentPublicApi;
import com.youle.gamebox.ui.bean.dynamic.DymaicCommentsBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.RecoderVoice;
import com.youle.gamebox.ui.util.SoftkeyboardUtil;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.PublishDyView;
import com.youle.gamebox.ui.view.SoftInputView;
import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;

/**
 * 动态提交评论界面
 *
 * @author xiedezhi
 */
public class DynamicCommentActivity extends BaseActivity implements SoftInputView.OnSoftInputShowListener, PublishDyView.IDPListener, View.OnTouchListener {
    FrameLayout frameLayout;
    PublishDyView publishDyView;
    public static final String ID = "id";
    public static final String CID = "cid";
    public static final String MODEL= "model";
    public static final int TEXT = 1 ;
    public static final int VOICE = 2 ;
    private long dId;
    private long cId ;
    private String voicePath;
    private View recodingView;
    private SoftInputView mSoftInputView ;
    private PublishDyView.SendModel model ;
    public static  ICommentListener iCommentListener ;
    public interface ICommentListener{
        public void onCommentSuccess(String json) ;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dId = getIntent().getLongExtra(ID, -1);
        cId = getIntent().getLongExtra(CID,-1);
        int m = getIntent().getIntExtra(MODEL,1);
        if(m==1){
            model= PublishDyView.SendModel.TEXT;
        }else {
            model = PublishDyView.SendModel.VOICE;
        }
        setContentView(R.layout.activity_comment);
        frameLayout = (FrameLayout) findViewById(R.id.contentLayout);
        publishDyView = new PublishDyView(this, PublishDyView.PublisModel.COMMENT);
        publishDyView.setModel(model);
        recodingView = findViewById(R.id.recoding);
        frameLayout.addView(publishDyView);
        mSoftInputView = (SoftInputView) findViewById(R.id.contentView);
        mSoftInputView.setListener(this);
        publishDyView.setListener(this);
        findViewById(R.id.contentView).setOnTouchListener(this);
        frameLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(model== PublishDyView.SendModel.TEXT) {
            publishDyView.showIM(true);
        }
    }

    private boolean canSend = true ;
    @Override
    public void send(String content, PublishDyView.SendModel model) {
        if(!canSend) return;
        canSend = false ;
        final DymaicCommentPublicApi publicApi = new DymaicCommentPublicApi();
        publicApi.setSid(new UserInfoCache().getSid());
        if(model== PublishDyView.SendModel.TEXT) {
            if(TextUtils.isEmpty(content)){
                UIUtil.toast(this,"评论内容不能为空");
            }else {
                publicApi.setContent(content);
            }
        }else {
           if(TextUtils.isEmpty(voicePath)) {
               UIUtil.toast(this,"评论语音不能为空");
           }else {
               publicApi.setVoice(new File(voicePath));
               publicApi.setVoiceTimeLen(endTime-startTime);
           }
        }
        String tip = "";
        if(dId!=-1){
            publicApi.setDid(dId + "");
            tip = "正在评论";
        }
        if(cId!=-1){
            publicApi.setCid(cId+"");
            tip = "正在回复";
        }
        ZhidianHttpClient.request(publicApi, new JsonHttpListener(false) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                UIUtil.toast(DynamicCommentActivity.this, R.string.tost_comment_success);
                canSend=true ;
                voicePath=null ;
                publishDyView.cleanEdite();
                Intent intent = new Intent();
                intent.putExtra("json",jsonString);
                setResult(RESULT_OK,intent);
                if(iCommentListener!=null){
//                    iCommentListener.onCommentSuccess(jsonString);
                }
                finish();
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                canSend=true ;
                UIUtil.toast(DynamicCommentActivity.this, R.string.tost_comment_fail);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                canSend=true ;
                publishDyView.setEnabled(true);
            }
        });
    }

    @Override
    public void cleanImage() {

    }

    @Override
    public void cleanGame() {

    }

    long startTime;
    long endTime;
    final RecoderVoice recoderVoice = new RecoderVoice();
    @Override
    public void startRecoding() {
        recodingView.setVisibility(View.VISIBLE);
        startTime = System.currentTimeMillis();
        new Thread() {
            @Override
            public void run() {
                recoderVoice.start(DynamicCommentActivity.this);
            }
        }.start();
    }

    @Override
    public void endRecoding() {
        recodingView.setVisibility(View.GONE);
        endTime = System.currentTimeMillis();
        voicePath = recoderVoice.stop();
        publishDyView.setVoiceLength(((endTime-startTime)/1000));
    }

    @Override
    public void preFace(boolean show) {
        mSoftInputView.isShowFace = show ;
    }

    @Override
    public void selectGame() {

    }

    @Override
    public void onCleanImage() {

    }

    @Override
    public void onCleanGame() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        publishDyView.cleanEdite();
        finish();
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onSoftInputShow() {

    }

    @Override
    public void onSoftInputDissmiss() {
        finish();
    }
}
