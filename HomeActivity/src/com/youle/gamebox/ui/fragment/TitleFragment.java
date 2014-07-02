package com.youle.gamebox.ui.fragment;

import android.widget.TextView;
import butterknife.InjectView;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-4-23.
 */
public class TitleFragment extends BaseFragment{

    @InjectView(R.id.text)
    TextView mText;

    @Override
    protected int getViewId() {
        return R.layout.test;
    }




    protected void loadData() {

    }

    private void initTitle() {
        TextView textView = new TextView(getActivity());
        textView.setText("测试标题");
        setTitleView(textView);
    }

    @Override
    public void onSuccess(TAResponse response) {

    }


}
