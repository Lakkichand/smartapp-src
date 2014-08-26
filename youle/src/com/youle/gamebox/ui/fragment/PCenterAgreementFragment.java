package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;

/**
 * Created by Administrator on 2014/5/22.
 */
public class PCenterAgreementFragment extends BaseFragment {
    OnClickAggreen onClickAggreen;
    @InjectView(R.id.pcenter_asgreement_but)
    TextView mPcenterAsgreementBut;
    @InjectView(R.id.proto)
    TextView mProto ;
    public void setOnClickAggreen(OnClickAggreen onClickAggreen) {
        this.onClickAggreen = onClickAggreen;
    }

    @Override
    protected int getViewId() {
        return R.layout.pcenter_agreement_layout;
    }

    @Override
    protected String getModelName() {
        return "用户协议";
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDefaultTitle("用户协议");
        mPcenterAsgreementBut.setOnClickListener(onClickListener);
        mProto.setText(Html.fromHtml(getString(R.string.pcenter_asgreement_text)));
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager supportFragmentManager = ((BaseActivity) getActivity()).getSupportFragmentManager();
            if (supportFragmentManager.getBackStackEntryCount() > 1) {
                supportFragmentManager.popBackStack();
            } else {
                ((BaseActivity) getActivity()).finish();
            }
            if (onClickAggreen != null) {
                onClickAggreen.onclikAgreen(true);
            }

        }
    };
}
