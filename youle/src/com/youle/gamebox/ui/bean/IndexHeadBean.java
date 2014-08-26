package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-3.
 */
public class IndexHeadBean {
   private int position;//Integer 位置  1：幻灯片 2：幻灯片下面两个焦点图 3：头条
   private String title;//String标题
   private int type;//Integer类型 0：网页  1：应用 2：专题 3：攻略 4：标签分类 5：新闻 6：论坛活动 7：礼包
   private String imgUrl;//String图片地址
   private String target;// String 对于不同的type返回不同的内容

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
