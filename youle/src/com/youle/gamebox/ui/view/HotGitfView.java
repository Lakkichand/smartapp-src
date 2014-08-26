package com.youle.gamebox.ui.view;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.api.GetGift;
import com.youle.gamebox.ui.bean.GiftBean;
import com.youle.gamebox.ui.bean.HotGiftBean;
import com.youle.gamebox.ui.fragment.GiftDetailFragment;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.UIUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 14-6-24.
 */
public class HotGitfView extends LinearLayout implements View.OnClickListener {

    @InjectView(R.id.hotGifitIcon)
    ImageView mHotGifitIcon;
    @InjectView(R.id.giftName)
    TextView mGiftName;
    @InjectView(R.id.total)
    TextView mTotal;
    @InjectView(R.id.haveAccount)
    TextView mHaveAccount;
    @InjectView(R.id.getGift)
    TextView mGetGift;
    @InjectView(R.id.line)
    ImageView mLine;
    HotGiftBean hotGiftBean;

    public HotGitfView(Context context, HotGiftBean hotGiftBean) {
        super(context);
        this.hotGiftBean = hotGiftBean;
        LayoutInflater.from(context).inflate(R.layout.hot_gift_item, this);
        ButterKnife.inject(this);
        setOnClickListener(this);
        initUI();
    }

    private void initUI(){
        ImageLoadUtil.displayNotRundomImage(hotGiftBean.getIconUrl(), mHotGifitIcon);
        mGiftName.setText(hotGiftBean.getsName());
        mTotal.setText(hotGiftBean.getTotal() + "/");
        String html = getResources().getString(R.string.gift_number_format3, hotGiftBean.getRest());
        mHaveAccount.setText(Html.fromHtml(html));
        if (hotGiftBean.getStatus() == GiftBean.NOMOR) {
            mGetGift.setTag(hotGiftBean);
            mGetGift.setOnClickListener(getOnClickListener);
            mGetGift.setEnabled(true);
        } else if (hotGiftBean.getStatus() == GiftBean.HAVE_NO) {
            mGetGift.setText(R.string.lingqu_no);
            mGetGift.setEnabled(false);
        } else if (hotGiftBean.getStatus() == GiftBean.TIME_OUT) {
            mGetGift.setText(R.string.time_out);
            mGetGift.setEnabled(false);
        } else if (hotGiftBean.getStatus() == GiftBean.NOT_START) {
            mGetGift.setText(R.string.not_start);
            mGetGift.setEnabled(false);
        } else if (hotGiftBean.getStatus() == GiftBean.HAS_GOT) {
            mGetGift.setText(R.string.lingqued);
            mGetGift.setEnabled(false);
        }
    }

    public void isLast() {
        mLine.setVisibility(GONE);
    }

    @Override
    public void onClick(View v) {
        GiftDetailFragment giftDetailFragment = new GiftDetailFragment(hotGiftBean.getId() + "");
        ((BaseActivity) getContext()).addFragment(giftDetailFragment, true);
    }

    private OnClickListener getOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            HotGiftBean giftBean = (HotGiftBean) v.getTag();
            getGift(getContext(), giftBean.getId() + "");
        }
    };

    private void getGift(final Context mContext, String id) {
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo != null) {
            GetGift getGift = new GetGift();
            getGift.setGiftId(id);
            getGift.setSid(new UserInfoCache().getSid());
            ZhidianHttpClient.request(getGift, new JsonHttpListener(mContext) {
                @Override
                public void onRequestSuccess(String jsonString) {
                    super.onRequestSuccess(jsonString);
                    mGetGift.setText(R.string.lingqued);
                    mGetGift.setEnabled(false);
                    hotGiftBean.setRest(hotGiftBean.getRest()-1);
                    hotGiftBean.setStatus(GiftBean.HAS_GOT);
                    initUI();
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
        }else {
            CommonActivity.startCommonA(getContext(),CommonActivity.FRAGMENT_LOGIN,-1);
        }
    }

}
