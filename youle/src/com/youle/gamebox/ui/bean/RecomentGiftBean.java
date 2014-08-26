package com.youle.gamebox.ui.bean;

import java.util.List;

/**
 * Created by Administrator on 14-6-24.
 */
public class RecomentGiftBean {
    private List<LikeBean> likes;
    private List<HotGame> hotGames ;
    private List<HotGiftBean> hotSprees ;

    public List<LikeBean> getLikes() {
        return likes;
    }

    public List<HotGame> getHotGames() {
        return hotGames;
    }

    public List<HotGiftBean> getHotSprees() {
        return hotSprees;
    }
}
