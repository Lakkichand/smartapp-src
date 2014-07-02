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
import com.youle.gamebox.ui.bean.GiftBean;
import com.youle.gamebox.ui.bean.HotGiftBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;

/**
 * Created by Administrator on 14-6-24.
 */
public class HotGitfView extends LinearLayout {

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

    public HotGitfView(Context context, HotGiftBean hotGiftBean) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.hot_gift_item, this);
        ButterKnife.inject(this);
        ImageLoadUtil.displayNotRundomImage(hotGiftBean.getIconUrl(), mHotGifitIcon);
        mGiftName.setText(hotGiftBean.getgName());
        mTotal.setText(hotGiftBean.getTotal() + "/");
        String html =  getResources().getString(R.string.gift_number_format3,hotGiftBean.getRest()+"");
        mHaveAccount.setText(Html.fromHtml(html));
        if (hotGiftBean.getStatus() == GiftBean.NOMOR) {
            mGetGift.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "do 领取", Toast.LENGTH_SHORT).show();
                    mGetGift.setText("领取");
                }
            });
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

    public void isLast(){
        mLine.setVisibility(GONE);
    }
}
