package com.youle.gamebox.ui.bean;

/*在接口只返回ID,和title的JSON用LimeitBean来保存
 * Created by li hongbo on 14-6-23.
 */
public class LimitBean {
    private String title;//String标题，

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    private long id;//礼包Long
}
