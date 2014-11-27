package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.dynamic.DymaicCommentPublicApi;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.RecoderVoice;
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
public class PublishFragment extends BaseFragment implements SoftInputView.OnSoftInputShowListener, PublishDyView.IDPListener, View.OnTouchListener {
    PublishDyView publishDyView;
    public static final String ID = "id";
    public static final String CID = "cid";
    public static final String MODEL = "model";
    public static final int TEXT = 1;
    public static final int VOICE = 2;
    @InjectView(R.id.contentLayout)
    FrameLayout mContentLayout;
    @InjectView(R.id.recoding)
    RelativeLayout mRecoding;
    @InjectView(R.id.contentView)
    SoftInputView mContentView;
    private long dId;
    private long cId;
    private String voicePath;
    private PublishDyView.SendModel model;
    public static ICommentListener iCommentListener;

    public interface ICommentListener {
        public void onCommentSuccess(String json);
    }

    public PublishFragment(long dId, long cId, PublishDyView.SendModel model) {
        this.dId = dId;
        this.cId = cId;
        this.model = model;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        publishDyView = new PublishDyView(getActivity(), PublishDyView.PublisModel.COMMENT);
        publishDyView.setModel(model);
        mContentView.setOnTouchListener(this);
        mContentView.setListener(this);
        publishDyView.setListener(this);
        if(model== PublishDyView.SendModel.TEXT) {
            publishDyView.showIM(true);
        }
        mContentLayout.addView(publishDyView);
    }




    @Override
    protected int getViewId() {
        return R.layout.activity_comment;
    }

    @Override
    protected String getModelName() {
        return "发表评论";
    }


    @Override
    public void send(String content, PublishDyView.SendModel model) {
        final DymaicCommentPublicApi publicApi = new DymaicCommentPublicApi();
        publicApi.setSid(new UserInfoCache().getSid());
        if (model == PublishDyView.SendModel.TEXT) {
            if (TextUtils.isEmpty(content)) {
                UIUtil.toast(getActivity(), "评论内容不能为空");
            } else {
                publicApi.setContent(content);
            }
        } else {
            if (TextUtils.isEmpty(voicePath)) {
                UIUtil.toast(getActivity(), "评论语音不能为空");
            } else {
                publicApi.setVoice(new File(voicePath));
                publicApi.setVoiceTimeLen(endTime - startTime);
            }
        }
        String tip = "";
        if (dId != -1) {
            publicApi.setDid(dId + "");
            tip = "正在评论";
        }
        if (cId != -1) {
            publicApi.setCid(cId + "");
            tip = "正在回复";
        }
        ZhidianHttpClient.request(publicApi, new JsonHttpListener(getActivity()) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                if(getActivity()==null) return;
                UIUtil.toast(getActivity(), R.string.tost_comment_success);
                voicePath = null;
                publishDyView.cleanEdite();
                Intent intent = new Intent();
                intent.putExtra("json", jsonString);
                getActivity().setResult(getActivity().RESULT_OK, intent);
                getActivity().finish();
//                if(iCommentListener!=null){
//                    iCommentListener.onCommentSuccess(jsonString);
//                }
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                UIUtil.toast(getActivity(), R.string.tost_comment_fail);
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
        mRecoding.setVisibility(View.VISIBLE);
        startTime = System.currentTimeMillis();
        new Thread() {
            @Override
            public void run() {
                recoderVoice.start(getActivity());
            }
        }.start();
    }

    @Override
    public void endRecoding() {
        mRecoding.setVisibility(View.GONE);
        endTime = System.currentTimeMillis();
        voicePath = recoderVoice.stop();
        publishDyView.setVoiceLength(((endTime - startTime) / 1000));
    }

    @Override
    public void preFace(boolean show) {
        mContentView.isShowFace = show;
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
         getActivity().finish();
        return false;
    }


    @Override
    public void onSoftInputShow() {

    }

    @Override
    public void onSoftInputDissmiss() {
        getActivity().finish();
    }
}
