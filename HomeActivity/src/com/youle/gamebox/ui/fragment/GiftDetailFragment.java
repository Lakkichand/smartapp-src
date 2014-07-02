package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.api.GetGiftDetailApi;
import com.youle.gamebox.ui.bean.GiftDetailBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import org.json.JSONException;

/**
 * Created by Administrator on 14-6-27.
 */
public class GiftDetailFragment extends BaseFragment {
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.name)
    TextView mName;
    @InjectView(R.id.restNumber)
    TextView mRestNumber;
    @InjectView(R.id.totalNumber)
    TextView mTotalNumber;
    @InjectView(R.id.getGift)
    TextView mGetGift;
    @InjectView(R.id.getTime)
    TextView mGetTime;
    @InjectView(R.id.userTime)
    TextView mUserTime;
    @InjectView(R.id.giftContent)
    TextView mGiftContent;
    @InjectView(R.id.giftCondition)
    TextView mGiftCondition;
    @InjectView(R.id.useMethod)
    TextView mUseMethod;
    private String id;
    private GiftDetailBean mGiftDetailBean;

    public GiftDetailFragment(String id) {
        this.id = id;
    }

    @Override
    protected int getViewId() {
        return R.layout.gift_detail_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGiftDetailBean == null) {
            loadData();
        }
    }

    private void loadData() {
        GetGiftDetailApi getGiftDetailApi = new GetGiftDetailApi();
        getGiftDetailApi.setId(id);
        ZhidianHttpClient.request(getGiftDetailApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mGiftDetailBean = jsonToBean(GiftDetailBean.class, jsonString);
                    if (mGiftDetailBean != null) {
                        initGiftView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initGiftView() {
        ImageLoadUtil.displayImage(mGiftDetailBean.getIconUrl(), mGameIcon);
        mName.setText(mGiftDetailBean.getTitle());
        mRestNumber.setText(mGiftDetailBean.getRest() + "");
        mTotalNumber.setText(mGiftDetailBean.getTotal() + "");
        mGetTime.setText(mGiftDetailBean.getReceiveFrom() + "~" + mGiftDetailBean.getReceiveTo());
        mUserTime.setText(mGiftDetailBean.getExchangeFrom() + "~" + mGiftDetailBean.getExchangeTo());
        mGiftContent.setText(mGiftDetailBean.getContent());
        mGiftCondition.setText(mGiftDetailBean.getCondition());
        mUseMethod.setText(mGiftDetailBean.getGuide());
    }
}
