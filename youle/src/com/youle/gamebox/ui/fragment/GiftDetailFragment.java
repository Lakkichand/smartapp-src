package com.youle.gamebox.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.api.GetGift;
import com.youle.gamebox.ui.api.GetGiftDetailApi;
import com.youle.gamebox.ui.bean.GiftBean;
import com.youle.gamebox.ui.bean.GiftDetailBean;
import com.youle.gamebox.ui.bean.HotGiftBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.UIUtil;
import org.json.JSONException;
import org.json.JSONObject;

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
    protected String getModelName() {
        return "礼包详情";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGiftDetailBean == null) {
            setDefaultTitle("礼包详情");
            loadData();
        }
    }

    private void loadData() {
        GetGiftDetailApi getGiftDetailApi = new GetGiftDetailApi();
        getGiftDetailApi.setId(id);
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo != null) {
            getGiftDetailApi.setSid(userInfo.getSid());
        }
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
        initButtonGetGift();
    }

    private void initButtonGetGift() {
        if (mGiftDetailBean.getStatus() == GiftBean.NOMOR) {
            mGetGift.setTag(mGiftDetailBean);
            mGetGift.setOnClickListener(getOnClickListener);
            mGetGift.setEnabled(true);
        } else if (mGiftDetailBean.getStatus() == GiftBean.HAVE_NO) {
            mGetGift.setText(R.string.lingqu_no);
            mGetGift.setEnabled(false);
        } else if (mGiftDetailBean.getStatus() == GiftBean.TIME_OUT) {
            mGetGift.setText(R.string.time_out);
            mGetGift.setEnabled(false);
        } else if (mGiftDetailBean.getStatus() == GiftBean.NOT_START) {
            mGetGift.setText(R.string.not_start);
            mGetGift.setEnabled(false);
        } else if (mGiftDetailBean.getStatus() == GiftBean.HAS_GOT) {
            mGetGift.setText(R.string.lingqued);
            mGetGift.setEnabled(false);
        }

    }

    private View.OnClickListener getOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            UserInfo userInfon = new UserInfoCache().getUserInfo();
            if (userInfon != null) {
                GiftDetailBean giftBean = (GiftDetailBean) v.getTag();
                getGift(getActivity(), giftBean.getId() + "");
            } else {
                CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_LOGIN, -1);
            }
        }
    };

    private void getGift(final Context mContext, String id) {
        GetGift getGift = new GetGift();
        getGift.setGiftId(id);
        getGift.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(getGift, new JsonHttpListener(mContext) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                mGetGift.setText(R.string.lingqued);
                mGetGift.setEnabled(false);
                UIUtil.toast(mContext, R.string.tost_gift_get_success);
            }

            @Override
            public void onResultFail(String jsonString) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                int code = Integer.parseInt(jsonObject.optString("code"));
                switch (code) {
                    case 2001:
                        UIUtil.toast(mContext, R.string.tost_gift_has_got);
                        break;
                    case 2002:
                        UIUtil.toast(mContext, R.string.tost_gift_has_no);
                        break;
                    case 2003:
                        UIUtil.toast(mContext, R.string.tost_gift_time_out);
                        break;
                    case 2004:
                        UIUtil.toast(mContext, R.string.tost_gift_not_open);
                        break;
                    case 2005:
                        CommonActivity.startCommonA(mContext, CommonActivity.FRAGMENT_LOGIN, -1);
                        break;
                }
            }
        });
    }

}
