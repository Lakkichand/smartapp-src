package com.youle.gamebox.ui.fragment;

import android.widget.TextView;
import android.widget.Toast;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 2014/5/27.
 */
public class PCenterMyVistisFragment extends BaseFragment {
    @InjectView(R.id.pc_layout_bottom_text)
    TextView mPcLayoutBottomText;

    @Override
    protected int getViewId() {
        return R.layout.pcenter_layout_bottom_listview;
    }

    protected void loadData() {
        mPcLayoutBottomText.setText("myVistis");
        Toast.makeText(getActivity(), "myVistis", Toast.LENGTH_SHORT).show();


    }
}
