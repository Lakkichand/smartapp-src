package com.youle.gamebox.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.IndexHeadBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 14-6-3.
 */
public class IndexPPtFragment extends BaseFragment {
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
//        mGameBack.setImageResource(R.drawable.ic_launcher);
    }

    public void loadData() {
        if(imageBitmap==null) {
            ImageLoadUtil.displayNotRundomImage(bean.getImgUrl(), mGameBack, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {

                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    imageBitmap = bitmap;
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        }else {
            mGameBack.setImageBitmap(imageBitmap);
        }
    }
}
