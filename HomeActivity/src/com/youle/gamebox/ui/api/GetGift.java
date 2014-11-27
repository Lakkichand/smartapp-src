package com.youle.gamebox.ui.api;

/**
 * Created by Administrator on 14-7-2.
 */
public class GetGift extends AbstractApi {
    private String giftId;
    private String sid ;

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setGiftId(String giftId) {
        this.giftId = giftId;
    }

    @Override
    protected String getPath() {
        return "/gamebox/spree/" + giftId + "/receive";
    }
}
