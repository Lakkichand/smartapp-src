package com.youle.gamebox.ui.bean.pcenter;

import com.youle.gamebox.ui.bean.dynamic.MessageBean;

import java.util.List;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCMymsgboardBean {
    private Integer totalPages ; //总页数
    private List<MessageBean> data ;//List<留言板对象>

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public List<MessageBean> getData() {
        return data;
    }

    public void setData(List<com.youle.gamebox.ui.bean.dynamic.MessageBean> data) {
        this.data = data;
    }
}
