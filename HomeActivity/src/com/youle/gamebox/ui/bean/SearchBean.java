package com.youle.gamebox.ui.bean;

import java.util.List;

/**
 * Created by Administrator on 14-6-27.
 */
public class SearchBean {
    List<MiniGameBean>  games ;
    List<String> tabs ;

    public List<MiniGameBean> getGames() {
        return games;
    }

    public List<String> getTabs() {
        return tabs;
    }
}
