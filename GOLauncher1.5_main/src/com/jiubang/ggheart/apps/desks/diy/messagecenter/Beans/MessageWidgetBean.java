package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;

import org.json.JSONObject;

import android.view.View;

import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageDownLoadObserver;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster;
/**
 * 消息中的widget的javaBean基类
 * @date  [2012-9-28]
 */
public class MessageWidgetBean extends BroadCaster {

	public static final String TAG_TYPE = "type";
	public static final String TAG_ACTTYPE = "acttype";
	public static final String TAG_ACTVAULE = "actvalue";
	public static final String TAG_VALUE = "value";

	public static final String TYPE_TEXT = "text";
	public static final String TYPE_IMG = "img";
	public static final String TYPE_BTN = "btn";
	public static final String TYPE_HERF = "href";

	public static final int ACTTYPE_NON = 0;
	public static final int ACTTYPE_CANCLE = 1; // 取消/关闭
	public static final int ACTTYPE_LINK = 2; // 打开连接
	public static final int ACTTYPE_GOSTORE = 3; // 跳至GO精品
	public static final int ACTTYPE_OPENGO = 4; // 打开产品内部模块
	public static final int ACTTYPE_OTHER = 5; // 跳转其他应用
	public static final int ACTTYPE_DOWNLOAD = 6;  //使用ftp下载文件
	public static final int ACTTYPE_NEWACTION = 7;  //1.3新定义的类型，

	/**
	 *  0:无，2：打开web连接，3: 跳至GO精品，4：打开产品内部模块，5：跳至其他应用(market://+包名=>即为跳转至谷歌电子市场)
	 *	6：ftp下载。当为此动作时，actvalue中会带有所下载的应用包名。格式如下：
	 *	http://xxx.apk?packagename=xxx.xxx.xxxx##aaa   ,"aaa"为下载的应用的程序名字
	 *	客户端从这个地址中提取应用包名，和程序的名字 。  详细内容请参考消息中心协议文档
	 *7 新版跳转url，协议版本1.3之后支持
	 **/
	
	//动作7对应的参数
	/** url形式（全部小写）	功能描述	备注
	×	msg://id=xxx	跳转至消息详情页	id参数传入消息id
	×   gui://id=xxx	跳转至主题预览精选页面	id参数代表跳转的页面类型。
	×   1：桌面 2：锁屏
	×   guidetail://id=xxx	跳转至Gui主题预览详情	Id参数代表跳转的主题id
	×   guispec://id=xxx	跳转至GUI专题页	id代表跳转的专题id
	×   market://id=xxx	跳转至电子市场详情页	id代表跳转的包名
	×   gostoretype://id=xxx	跳转至go精品专题或者分类	id代表跳转的分类id
	×   gostoredetail://id=xxx	跳转至go精品详情页面	id代表跳转的应用id
	×   http://xxxx 直接打开外部浏览器访问	
	 */

	public int mActtype;   // 点击按钮的动作类型
	public String mActvaule; // 点击动作对应的参数值
	public String mType; // 控件类型 text img btn href
	public String mValue; // 1.文本控件内容，2.超链接控件名称
	public MessageDownLoadObserver mObserver;

	public void prase(JSONObject obj) {
		return;
	}

	public void initView(View view, MessageDownLoadObserver observer) {
		mObserver = observer;
		return;
	}

	public void recycle() {
		mObserver = null;
		return;
	}
}
