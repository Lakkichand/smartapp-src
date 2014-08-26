package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-27.
 */
public class GiftDetailBean {
   private long id;//Long礼包Id
   private String title;//String礼包标题
   private int total;//Integer 总数
   private int rest;//Integer剩余个数
   private String iconUrl;//String 游戏图标url
   private String content;//String 礼包内容
   private String condition;//String 领取条件
   private String guide;//String 使用方法
   private String receiveFrom;//String领取开始时间
   private String receiveTo;//String领取结束时间
   private String exchangeFrom;//String兑换开始时间
   private String exchangeTo;//String兑换结束时间
   private int status;//Integer 状态 0：正常 1：领取完毕 2：过期 3：没开启 4：已领
   private String activationCode;//String激活码
   private String downloadUrl;//String应用下载路径
   private String packageName;//String游戏包名
   private String version;//String版本
   private String forumUrl;//String 讨论区

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTotal() {
        return total;
    }

    public int getRest() {
        return rest;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getContent() {
        return content;
    }

    public String getCondition() {
        return condition;
    }

    public String getGuide() {
        return guide;
    }

    public String getReceiveFrom() {
        return receiveFrom;
    }

    public String getReceiveTo() {
        return receiveTo;
    }

    public String getExchangeFrom() {
        return exchangeFrom;
    }

    public String getExchangeTo() {
        return exchangeTo;
    }

    public int getStatus() {
        return status;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersion() {
        return version;
    }

    public String getForumUrl() {
        return forumUrl;
    }
}

