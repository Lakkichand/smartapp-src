package com.youle.gamebox.ui.bean.special;


import com.youle.gamebox.ui.bean.LimitBean;
import com.youle.gamebox.ui.greendao.GameBean;

import java.util.List;

/**
 * Created by Administrator on 2014/6/3.
 */
public class SpecialAdetailBean {
    private Long id;//Long专题Id
    private String name;//String名称
    private String explain;//String简介
    private List<LimitBean> sprees;//List<A> 礼包 A;//{title;//String标题，id;//礼包Long Id}
    private List<LimitBean> gonglues;//List<B> 攻略 B;//{title;//String标题，id;//攻略Long Id}
    private List<OtherSpecial> others;//List<D> 相关专题 D;//{id;//Long 专题Id，name;//String专题名}
    private List<GameBean> games;//List<E> E详见游戏基本对象

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExplain() {
        return explain;
    }

    public List<LimitBean> getSprees() {
        return sprees;
    }

    public List<LimitBean> getGonglues() {
        return gonglues;
    }

    public List<OtherSpecial> getOthers() {
        return others;
    }

    public List<GameBean> getGames() {
        return games;
    }
}
