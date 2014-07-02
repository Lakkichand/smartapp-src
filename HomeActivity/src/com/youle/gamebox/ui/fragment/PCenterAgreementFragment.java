package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.view.GameTitleBarView;

/**
 * Created by Administrator on 2014/5/22.
 */
public class PCenterAgreementFragment extends BaseFragment {
    @InjectView(R.id.pcenter_asgreement_but)
    TextView mPcenterAsgreementBut;
    OnClickAggreen onClickAggreen;

    public void setOnClickAggreen(OnClickAggreen onClickAggreen){
        this.onClickAggreen = onClickAggreen;
    }

    @Override
    protected int getViewId() {
        return R.layout.pcenter_agreement_layout;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAppDetailTitleView();
        mPcenterAsgreementBut.setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            FragmentManager supportFragmentManager = ((BaseActivity) getActivity()).getSupportFragmentManager();
            if(supportFragmentManager.getBackStackEntryCount()>1){
                supportFragmentManager.popBackStack();
            }else{
                ((BaseActivity) getActivity()).finish();
            }
            if(onClickAggreen!=null){
                onClickAggreen.onclikAgreen(true);
            }

        }
    };
    private void setAppDetailTitleView(){
        GameTitleBarView customTitleView = new GameTitleBarView(getActivity());
        customTitleView.setTitleBarMiddleView(null, "用户协议");
        customTitleView.setVisiableRightView(View.GONE);
        setTitleView(customTitleView);

    }
}
