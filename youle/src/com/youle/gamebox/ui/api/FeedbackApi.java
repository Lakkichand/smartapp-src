package com.youle.gamebox.ui.api;

public class FeedbackApi extends AbstractApi {

	private String sid;
	private String contact;//联系方式
	private String content;//内容
	
	public void setSid(String sid) {
        this.sid = sid;
    }
	
	public void setContact(String contact){
		this.contact = contact;
	}
	
	public void setContent(String content){
		this.content = content;
	}
	
	@Override
	protected String getPath() {
		// TODO Auto-generated method stub
		return "gamebox/feedback";
	}

}
