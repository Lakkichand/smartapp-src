package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.util.ImageLoadUtil;

/**
 * Created by Administrator on 14-7-21.
 */
public class BigImageFragment extends BaseFragment {
    String imageUrl  ;
    @InjectView(R.id.image)
    ImageView mImage;

    public BigImageFragment(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    protected int getViewId(){
        return R.layout.fragment_big_image;
    }

    @Override
    protected String getModelName() {
        return "查看大图";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageLoadUtil.displayNotRundomImage(imageUrl,mImage);
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity)getActivity()).onBackPressed();
            }
        });
    }
}
