package com.youle.gamebox.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.dynamic.DeleteCommentApi;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.UIUtil;

/**
 * Created by Administrator on 14-7-15.
 */
public class DyCommentDeletActivity extends BaseActivity implements View.OnClickListener {
    @InjectView(R.id.delete)
    TextView mDelete;
    @InjectView(R.id.cancel)
    TextView mCancel;
    public static final String CID = "cid" ;
    @InjectView(R.id.content)
    RelativeLayout mContent;
    private long cid ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cid = getIntent().getLongExtra(CID,-1);
        setContentView(R.layout.activity_delet);
        ButterKnife.inject(this);
        mDelete.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mContent.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
           if(v.getId() == R.id.delete){
               deleteComment();
           }else {
               finish();
           }
    }

    public static  void startDeleteCommentActivity(Context context,long cid){
        Intent intent = new Intent(context,DyCommentDeletActivity.class);
        intent.putExtra(CID,cid);
        context.startActivity(intent);
    }

    private void deleteComment() {
        DeleteCommentApi deleteCommentApi = new DeleteCommentApi() ;
        deleteCommentApi.sid = new UserInfoCache().getSid();
        deleteCommentApi.cid = cid +"";
        ZhidianHttpClient.request(deleteCommentApi,new JsonHttpListener(this,"正在删除"){
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                UIUtil.toast(DyCommentDeletActivity.this,R.string.delete_success);
                finish();
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                UIUtil.toast(DyCommentDeletActivity.this,R.string.delete_fail);
                finish();
            }
        });
    }
}
