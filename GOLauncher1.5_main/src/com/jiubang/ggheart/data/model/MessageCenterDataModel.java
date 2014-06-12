package com.jiubang.ggheart.data.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.jiubang.ggheart.data.info.MessageInfo;
/**
 * 消息中心数据库操作类
 * @author liulixia
 *
 */
public class MessageCenterDataModel extends DataModel {

	public MessageCenterDataModel(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public Cursor queryMessages() {
		Cursor cursor = mDataProvider.queryMessages();
		return cursor;
	}

	public Cursor queryReadedMessages() {
		Cursor cursor = mDataProvider.queryReadedMessages();
		return cursor;
	}

	public Cursor queryNeedShowdMessages() {
		Cursor cursor = mDataProvider.queryNeedShowMessages();
		return cursor;
	}

	public void insertRecord(MessageInfo info) {
		if (!isMessageExist(info.mId)) {
			ContentValues values = new ContentValues();
			info.contentValues(values);
			mDataProvider.insertMessage(values);
		} else { //消息重新编辑后改内容
			if (info.mIsNew == 1) {
				info.misReaded = false;
			}
			if (!info.misReaded) {
				updateRecord(info);
			}
		}
	}

	public boolean isMessageExist(String id) {
		boolean bRet = false;
		Cursor cr = mDataProvider.queryMessages(id);
		if (cr != null) {
			bRet = cr.getCount() > 0;
			cr.close();
		}
		return bRet;
	}

	public void deleteAllMessages() {
		mDataProvider.deleteAllMessage();
	}

	public void deleteMessage(String id) {
		mDataProvider.deleteMessage(id);
	}

	public void updateRecord(MessageInfo info) {
		ContentValues values = new ContentValues();
		info.contentValues(values);
		mDataProvider.updateMessage(values, info.mId);
	}

}
