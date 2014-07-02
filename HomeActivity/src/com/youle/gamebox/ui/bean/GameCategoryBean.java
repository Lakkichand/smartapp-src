package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-17.
 */
public class GameCategoryBean {
    private long id;//Long分类Id
    private String name;//String分类名称
    private String  iconUrl;//String 图片url

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
