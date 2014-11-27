package com.youle.gamebox.ui.account;

import com.youle.gamebox.ui.bean.LogAccount;
import com.youle.gamebox.ui.util.CoderString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class UserSAXPraserHelper extends DefaultHandler {

	List<LogAccount> list;
	LogAccount chann;
	private String currentTag;
	private StringBuilder sb;

	public List<LogAccount> getList() {
		return list;
	}

	/*
	 * 接口字符块通知
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String value = new String(ch, start, length);
		sb.append(value);

	}

	/*
	 * 接收文档结束通知
	 */
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	/*
	 * 接收标签结束通知
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals(UserCache.ACCOUNT)) {
			list.add(chann);
		} else if (localName.equals(UserCache.USRE_NAME)) {
			chann.setUserName(sb.toString());
		} else if (localName.equals(UserCache.PASS_WORD)) {
//			chann.setPassword(sb.toString());
			try {
				chann.setPassword(CoderString.decrypt(sb.toString()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (localName.equals(UserCache.LAST_TIME)) {
			chann.setLastLogin(Long.parseLong(sb.toString()));
		} else if(localName.equals(UserCache.OPTION)){
			chann.setOption(sb.toString()) ;
		}
	}

	/*
	 * 文档开始通知
	 */
	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
	}

	/*
	 * 标签开始通知
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		currentTag = localName;
		sb = new StringBuilder();
		if (currentTag == UserCache.ACCOUNT) {
			chann = new LogAccount();
		} else if (currentTag.equals(UserCache.LIST)) {
			list = new ArrayList<LogAccount>();
		}
	}
}