package com.youle.gamebox.ui.bean;

import java.util.List;

/**
 * Created by Administrator on 14-6-25.
 */
public class HomeGonglueBean {
    private List<LikeBean> likes;
    private List<LimitBean> hots;
    private List<HotGame> hotGames;

    public List<LikeBean> getLikes() {
        return likes;
    }

    public List<LimitBean> getHots() {
        return hots;
    }

    public List<HotGame> getHotGames() {
        return hotGames;
    }
}
