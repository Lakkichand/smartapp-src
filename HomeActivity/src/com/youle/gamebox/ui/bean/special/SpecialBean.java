package com.youle.gamebox.ui.bean.special;

/**
 * Created by Administrator on 2014/6/3.
 */
public class SpecialBean {
    private Long id;  //Long专题Id
    private String name;  //String名称
    private String explain;  //String简介
    private String imageUrl;  //String图片url地址
    private String updateDate;  //String更新时间

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}
