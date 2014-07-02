package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 2014/5/27.
 */
public class PCenterMyDymaicFragment extends BaseFragment {
    @InjectView(R.id.pc_layout_bottom_text)
    TextView mPcLayoutBottomText;

    @Override
    protected int getViewId() {
        return R.layout.pcenter_layout_bottom_listview;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    protected void loadData() {
        mPcLayoutBottomText.setText("myDymaic");
        Toast.makeText(getActivity(), "myDymaic", Toast.LENGTH_SHORT).show();

    }
}
