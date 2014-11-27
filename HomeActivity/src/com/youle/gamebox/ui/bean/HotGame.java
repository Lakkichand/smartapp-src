package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-24.
 */
public class HotGame {
   private long id;//Long 游戏Id，
   private String iconUrl;//String 图标地址，
   private String name;//String 游戏名，
   private int amount;//Integer 礼包个数
    private int source ;

    public int getSource() {
        return source;
    }

    public long getId() {
        return id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getName() {
        return name;
    }

    public int getAmount() {
        return amount;
    }
}
