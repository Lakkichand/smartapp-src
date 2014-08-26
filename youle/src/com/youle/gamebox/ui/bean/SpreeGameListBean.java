package com.youle.gamebox.ui.bean;

/** 礼包基本对象
 * Created by Administrator on 2014/5/14.
 */
public class SpreeGameListBean {
   	private Long id; //Long礼包Id
    private String title; //String礼包标题
    private Integer total; //Integer 总数
    private Integer rest; //Integer剩余个数
    private String content; //String 礼包内容
    private Integer status; //Integer 状态 0：正常 1：领取完毕 2：过期 3：没开启 4：已领

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
