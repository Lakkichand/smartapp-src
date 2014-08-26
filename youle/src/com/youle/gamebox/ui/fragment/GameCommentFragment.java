package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.adapter.GameCommentAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.game.AddGameCommentApi;
import com.youle.gamebox.ui.api.game.GameCommentApi;
import com.youle.gamebox.ui.bean.GameComentBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.CommentEmojiInputFaceView;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 14-6-23.
 */
public class GameCommentFragment extends NextPageFragment {
    private GameCommentApi mGameCommentApi;
    private GameCommentAdapter mAdapter;
    private List<GameComentBean> mGameComentBeanList;
    private CommentEmojiInputFaceView mEmojiInputView ;
    private View  scorView ;
    private TextView mScoreText ;
    private RatingBar mRatingBar ;
    private long id ;
    private int resource ;
    public GameCommentFragment(long id,int res) {
        this.id = id;
        this.resource = res ;
    }

    @Override
    public AbstractApi getApi() {
        return mGameCommentApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGameComentBeanList == null) {
            loadData();
        }
        initInputView() ;
    }

    @Override
    public void showScro(boolean show) {
//        if(show){
//            scorView.setVisibility(View.VISIBLE);
//        }else {
//            scorView.setVisibility(View.GONE);
//        }
    }

    @Override
    protected String getModelName() {
        return "游戏评论";
    }

    private void initInputView() {
        View inputView = LayoutInflater.from(getActivity()).inflate(R.layout.add_comment_layout,null);
        scorView = inputView.findViewById(R.id.scoreLayout);
        mEmojiInputView = (CommentEmojiInputFaceView) inputView.findViewById(R.id.inputView);
        mEmojiInputView.setOnSendListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(new UserInfoCache().getUserInfo()!=null){
                    sendComment(mEmojiInputView.getEditText());
                }else {
                    CommonActivity.startCommonA(getActivity(),CommonActivity.FRAGMENT_LOGIN,-1);
                }
            }
        });
        mRatingBar = (RatingBar) inputView.findViewById(R.id.scoreRatingBar);
       mScoreText = (TextView) inputView.findViewById(R.id.score);
        setBottomView(inputView);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
               mScoreText.setText(rating*2+"分");
            }
        });
        mRatingBar.setRating(3.0f);
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(GameComentBean.class, jsonStr, "data");
    }

    protected void loadData() {
        mGameCommentApi = new GameCommentApi(id+"");
        ZhidianHttpClient.request(mGameCommentApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mGameComentBeanList = pasreJson(jsonString);
                    mAdapter = new GameCommentAdapter(getActivity(), mGameComentBeanList);
                    getListView().setAdapter(mAdapter);
                    if(mGameComentBeanList.size()==0){
                        showNoContentLayout(true);
                    }else {
                        showNoContentLayout(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendComment(String str){
        if(TextUtils.isEmpty(str.trim())){
            UIUtil.toast(getActivity(),R.string.comment_not_null);
            return;
        }
        AddGameCommentApi addGameCommentApi = new AddGameCommentApi();
        addGameCommentApi.setSid(new UserInfoCache().getSid());
        addGameCommentApi.setContent(str);
        addGameCommentApi.setScore(resource);
        addGameCommentApi.setAppId(id);
        addGameCommentApi.setScore(mRatingBar.getRight());
        ZhidianHttpClient.request(addGameCommentApi,new JsonHttpListener(this){
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                UIUtil.toast(getActivity(),R.string.tost_comment_success);
                loadData();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                super.onFailure(statusCode, headers, responseString, error);
                UIUtil.toast(getActivity(), R.string.tost_comment_fail);
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                UIUtil.toast(getActivity(), R.string.tost_comment_fail);
            }
        });
    }
}
