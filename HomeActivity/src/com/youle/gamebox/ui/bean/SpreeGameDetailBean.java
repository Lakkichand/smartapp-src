package com.youle.gamebox.ui.bean;

/**礼包详情对象
 * Created by Administrator on 2014/5/14.
 */
public class SpreeGameDetailBean {
    private Long id; //Long礼包Id
    private String title; //String礼包标题
    private Integer total; //Integer 总数
    private Integer rest; //Integer剩余个数
    private String iconUrl; //String 游戏图标url
    private String content; //String 礼包内容
    private String condition; //String 领取条件
    private String guide; //String 使用方法
    private String receiveFrom; //String领取开始时间
    private String receiveTo; //String领取结束时间
    private String exchangeFrom; //String兑换开始时间
    private String exchangeTo; //String兑换结束时间
    private Integer status; //Integer 状态 0:正常 1:领取完毕 2:过期 3:没开启 4:已领
    private String activationCode; //String激活码
    private String downloadUrl; //String应用下载路径
    private String packageName; //String游戏包名
    private String version; //String版本
    private Long forumId; //Long 论坛Id

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getRest() {
        return rest;
    }

    public void setRest(Integer rest) {
        this.rest = rest;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getGuide() {
        return guide;
    }

    public void setGuide(String guide) {
        this.guide = guide;
    }

    public String getReceiveFrom() {
        return receiveFrom;
    }

    public void setReceiveFrom(String receiveFrom) {
        this.receiveFrom = receiveFrom;
    }

    public String getReceiveTo() {
        return receiveTo;
    }

    public void setReceiveTo(String receiveTo) {
        this.receiveTo = receiveTo;
    }

    public String getExchangeFrom() {
        return exchangeFrom;
    }

    public void setExchangeFrom(String exchangeFrom) {
        this.exchangeFrom = exchangeFrom;
    }

    public String getExchangeTo() {
        return exchangeTo;
    }

    public void setExchangeTo(String exchangeTo) {
        this.exchangeTo = exchangeTo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getForumId() {
        return forumId;
    }

    public void setForumId(Long forumId) {
        this.forumId = forumId;
    }
}
