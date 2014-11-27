package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.api.GetGift;
import com.youle.gamebox.ui.bean.GiftBean;
import com.youle.gamebox.ui.bean.GiftDetailBean;
import com.youle.gamebox.ui.fragment.GiftDetailFragment;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.UIUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 14-6-24.
 */
public class AllGiftAdapter extends YouleBaseAdapter<GiftBean> {
    public AllGiftAdapter(Context mContext, List<GiftBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GiftBean bean = getItem(position);
        ButterknifeViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gift_all_item, null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holder.mGiftName.setText(bean.getTitle());
        holder.mGiftdesc.setText(bean.getContent().trim());
        holder.mRestNumber.setText(bean.getRest() + "");
        holder.mTotalNumber.setText(bean.getTotal() + "");
        if (bean.getStatus() == GiftBean.NOMOR) {
            holder.mGetGift.setText(R.string.lingqu);
            holder.mGetGift.setEnabled(true);
            holder.mGetGift.setOnClickListener(onClickListener);
            holder.mGetGift.setTag(bean);
        } else if (bean.getStatus() == GiftBean.HAVE_NO) {
            holder.mGetGift.setText(R.string.lingqu_no);
            holder.mGetGift.setEnabled(false);
        } else if (bean.getStatus() == GiftBean.HAS_GOT) {
            holder.mGetGift.setText(R.string.lingqued);
            holder.mGetGift.setEnabled(false);
        } else if (bean.getStatus() == GiftBean.TIME_OUT) {
            holder.mGetGift.setText(R.string.time_out);
            holder.mGetGift.setEnabled(false);
        } else if (bean.getStatus() == GiftBean.NOT_START) {
            holder.mGetGift.setText(R.string.not_start);
            holder.mGetGift.setEnabled(false);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GiftDetailFragment giftDetailFragment = new GiftDetailFragment(bean.getId()+"",new GiftDetailFragment.OnGiftGetListener() {
                    @Override
                    public void onGetGift(GiftDetailBean giftDetailBean) {
                       bean.setRest(bean.getRest()-1);
                        bean.setStatus(GiftBean.HAS_GOT);
                        notifyDataSetChanged();
                    }
                });
                ((BaseActivity)mContext).addFragment(giftDetailFragment,true);
            }
        });
        return convertView;
    }
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GiftBean giftBean = (GiftBean) v.getTag();
            String sid = new UserInfoCache().getSid();
            if(sid==null||sid.trim().length()==0) {
                CommonActivity.startCommonA(mContext, CommonActivity.FRAGMENT_LOGIN, -1);
            }else {
                getGift(giftBean);
            }
        }
    };

    private void getGift(final GiftBean giftBean){
        GetGift getGift = new GetGift();
        getGift.setGiftId(giftBean.getId()+"");
        getGift.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(getGift,new JsonHttpListener(mContext,"正在领取礼包"){
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                giftBean.setStatus(GiftBean.HAS_GOT);
                UIUtil.toast(mContext,R.string.tost_gift_get_success);
                giftBean.setRest(giftBean.getRest()-1);
                notifyDataSetChanged();
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
                switch (code){
                    case 2001:
                        UIUtil.toast(mContext,R.string.tost_gift_has_got);
                        break;
                    case 2002:
                        UIUtil.toast(mContext,R.string.tost_gift_has_no);
                        break;
                    case 2003:
                        UIUtil.toast(mContext,R.string.tost_gift_time_out);
                        break;
                    case 2004:
                        UIUtil.toast(mContext,R.string.tost_gift_not_open);
                        break;
                    case 2005:
                        CommonActivity.startCommonA(mContext, CommonActivity.FRAGMENT_LOGIN, -1);
                        break;
                }
            }
        });
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'null'
     * for easy to all layout elements.
     *
     * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
     */
    static class ButterknifeViewHolder {
        @InjectView(R.id.giftName)
        TextView mGiftName;
        @InjectView(R.id.restNumber)
        TextView mRestNumber;
        @InjectView(R.id.totalNumber)
        TextView mTotalNumber;
        @InjectView(R.id.getGift)
        TextView mGetGift;
        @InjectView(R.id.giftdesc)
        TextView mGiftdesc;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
