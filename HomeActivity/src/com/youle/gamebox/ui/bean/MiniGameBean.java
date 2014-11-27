package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-23.
 */
public class MiniGameBean {
    private long id;//Long 游戏
    private String name;//String游戏名，
    private String iconUrl;//String 图标url
    private int source ;

    public int getSource() {
        return source;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
