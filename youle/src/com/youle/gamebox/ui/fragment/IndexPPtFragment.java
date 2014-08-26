package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.MyRelationActivity;
import com.youle.gamebox.ui.bean.IndexHeadBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 14-6-3.
 */
public class IndexPPtFragment extends BaseFragment implements View.OnClickListener {
    public static final int WEB = 0;
    public static final int APP= 1;
    public static final int THME= 2;
    public static final int STAGORY= 3;
    public static final int TAG = 4;
    public static final int NEWS= 5;
    public static final int EVENT= 6;
    public static final int GIFT= 7;
    @InjectView(R.id.gameBack)
    ImageView mGameBack;
    private Bitmap imageBitmap;
    private IndexHeadBean bean;

    public IndexPPtFragment() {
    }

    public IndexPPtFragment(IndexHeadBean bean) {
        this.bean = bean;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_index_ppt_item;
    }

    @Override
    protected String getModelName() {
        return "首页幻灯片";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
        mGameBack.setOnClickListener(this);
    }

    public void loadData() {
        if(imageBitmap==null&&bean!=null) {
            ImageLoadUtil.displayNotRundomImage(bean.getImgUrl(), mGameBack);
        }else {
            mGameBack.setImageBitmap(imageBitmap);
        }
    }

    @Override
    public void onClick(View v) {
        if(bean.getType()==WEB||bean.getType()==EVENT){
            Intent intent = new Intent(getActivity(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.URL,bean.getTarget());
            intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.WEB);
            startActivity(intent);
        }else if(bean.getType() == APP){
            Intent intent = new Intent(getActivity(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.ID,bean.getTarget());
            intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.GAME_DETAIL);
            startActivity(intent);
        }else  if(bean.getType() == GIFT){
            Intent intent = new Intent(getActivity(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.ID,bean.getTarget());
            intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.GIFT_DETAIL);
            startActivity(intent);
        }else  if(bean.getType() == THME ){
            Intent intent = new Intent(getActivity(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.ID,bean.getTarget());
            intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.SPECIAL_DETAIL);
            startActivity(intent);
        }else  if(bean.getType() == NEWS||bean.getType()==STAGORY){
            Intent intent = new Intent(getActivity(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.ID,bean.getTarget());
            intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.STAGRY_DETAIL);
            startActivity(intent);
        }
    }
}
