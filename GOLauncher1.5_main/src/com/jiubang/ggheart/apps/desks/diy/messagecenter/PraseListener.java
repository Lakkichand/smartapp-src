package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;

/**
 * 
 * 类描述: PraseListener接口类
 * 功能详细描述: 回调方法
 * @date  [2012-9-28]
 */
public interface PraseListener {

	public void listParseFinish(boolean bool, MessageListBean msgs);

//	public void msgParseFinish(boolean bool, MessageContentBean msg);

}
