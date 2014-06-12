package com.jiubang.ggheart.apps.desks.appfunc.model;

public class MsgEntity {
	public int mHandlerID;

	public int mMsgId;

	public Object mObj;

	public MsgEntity() {
	}

	public MsgEntity(int handlerID, int id, Object obj) {
		this.mHandlerID = handlerID;
		this.mMsgId = id;
		this.mObj = obj;
	}
}
