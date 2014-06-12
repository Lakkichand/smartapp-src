package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.go.util.ConvertUtils;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageDownLoadObserver;

/**
 * 
 * @author rongjinsong
 * 
 */

public class MessageListBean {

	public static final String TAG_RESULT = "result"; // response头
	public static final String TAG_TPPES = "types"; // 分类项数据
	public static final String TAG_STATUS = "status"; // 返回状态
	public static final String TAG_NETLOG = "netlog"; // 返回收集网络信息的开关状态
	public static final String TAG_SEVERTIME = "severtime"; // 返回从服务器获取数据时，服务器的时间
	public static final String TAG_KEEPALIVE = "keepalive";	//返回是否使用长连接字段
	public static final String TAG_SHOW_SS = "show_ss";	//返回显示搜索按钮
	public static final String TAG_LTS = "lts"; // 本次获取消息的时间
	public static final String TAG_COUNT = "count"; // 消息数量
	public static final String TAG_MSGS = "msgs"; // 消息数组
	public static final String TAG_APKSIGNNATURES = "apksignatures"; //过滤包签名串
	public static final String TAG_APKNAMES = "apknames"; //过滤包名

	/**
	 * 
	 * 类描述:  MessageHeadBean类
	 * 功能详细描述: 存放头消息的一些参数内容
	 * @date  [2012-9-28]
	 */
	public static class MessageHeadBean extends MessageBaseBean {

		public boolean misReaded = false;  //消息是否已读
		public boolean mClickClosed = false;
		public boolean mIsRemoved = false;	//消息是否已被用户在通知栏移除掉
		public int mViewType;
		public MessageDownLoadObserver mObserver;
		public Bitmap mBitmap;  //主题推送通知栏的Icon

		public void initIconView(MessageDownLoadObserver observer) {
			mObserver = observer;
		}

		public MessageHeadBean(String id, String title, int type, int viewType, String stamp) {
			mId = id;
			mTitle = title;
			mType = type;
			mViewType = viewType;
			mMsgTimeStamp = stamp;
		}

		public MessageHeadBean() {
			// TODO Auto-generated constructor stub
		}

		public void downloadDrawable(final MessageHeadBean headBean) {
			new Thread() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
					if (headBean.mFullScreenIcon == null && headBean.mIcon == null) {
						if (mObserver != null) {
							mObserver.onDownLoadFinsish();
						}
						return;
					}
					HttpURLConnection conn = null;
					InputStream is = null;
					try {
						URL url_im = null;
						if (headBean.mFullScreenIcon != null) {
							url_im = new URL(headBean.mFullScreenIcon);
						} else {
							url_im = new URL(headBean.mIcon);
						}
						conn = (HttpURLConnection) url_im.openConnection();
						conn.connect();
						//有时候网络不好，所以设置6次重链接
						for (int i = 0; i < 6; i++) {
							is = conn.getInputStream();
							if (is != null) {
								break;
							}
						}
						mBitmap = BitmapFactory.decodeStream(is);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						mBitmap = null;
					} catch (OutOfMemoryError e) {
						mBitmap = null;
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (conn != null) {
							conn.disconnect();
						}
						if (mObserver != null) {
							mObserver.onDownLoadFinsish();
						}

					}
				}
			}.start();
		}
	}

	private Vector<MessageHeadBean> mMessageHeads; // 从服务器拿到的所有消息头

	public MessageListBean() {
		mMessageHeads = new Vector<MessageHeadBean>();
	}

	public MessageListBean(JSONArray msgs, long dateTimeStamp) {
		// mUpDateTimeStamp = dateTimeStamp;
		mMessageHeads = new Vector<MessageHeadBean>();
		praseMsgsHead(msgs);
	}

	public int getMessageCount() {
		if (mMessageHeads != null) {
			return mMessageHeads.size();
		}
		return 0;
	}

	public Vector<MessageHeadBean> getAllMessagHead() {
		return mMessageHeads;
	}
	
	public MessageHeadBean getMessageHead(String id) {
		if (mMessageHeads != null) {
			for (int i = 0; i < mMessageHeads.size(); i++) {
				MessageHeadBean msg = mMessageHeads.get(i);
				if (msg != null && msg.mId != null && msg.mId.equals(id)) {
					return msg;
				}
			}
		}

		return null;
	}

	public void setReaded(String id, boolean bool) {
		MessageHeadBean msg = getMessageHead(id);
		if (msg != null) {
			msg.misReaded = bool;
		}
	}
	
	public void setClickClosed(String id, boolean bool) {
		MessageHeadBean msg = getMessageHead(id);
		if (msg != null) {
			msg.mClickClosed = bool;
		}
	}
	
	public void setRemoved(String id, boolean bool) {
		MessageHeadBean msg = getMessageHead(id);
		if (msg != null) {
			msg.mIsRemoved = bool;
		}
	}
	/**
	 * 功能简述: 将消息列表封装到ArrayList<MessageHeadBean>中
	 * 功能详细描述: 将JSONArray的消息列表数据转换成ArrayList<MessageHeadBean> mMessageHeads
	 * 注意:
	 * @param msgs
	 */
	public void praseMsgsHead(JSONArray msgs) {
		for (int i = 0; i < msgs.length(); i++) {
			JSONObject obj = (JSONObject) msgs.opt(i);
			if (obj != null) {
				String id = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_ID);
				String title = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_TITLE);
				int type = getJasonIntValue(obj, MessageBaseBean.TAG_MSG_TYPE);
				int viewType = getJasonIntValue(obj, MessageBaseBean.TAG_MSG_VIEWTYPE);
				String timestamp = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_TIME);
				MessageHeadBean msg = new MessageHeadBean(id, title, type, viewType, timestamp);
				msg.mStartTime = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_START);
				msg.mEndTime = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_END);
				msg.mSummery = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_INTRO);
				msg.mActType = getJasonIntValue(obj, MessageBaseBean.TAG_MSG_ACTTYPE);
				msg.mActValue = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_ACTVALUE);
				msg.mZtime = getJasonIntValue(obj, MessageBaseBean.TAG_MSG_ZTIME);
				if ((viewType & MessageBaseBean.VIEWTYPE_DESK_TOP) != 0) {
					msg.mZicon1 = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_ZICON1);
					msg.mZicon2 = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_ZICON2);
					msg.mZpos = getJasonIntValue(obj, MessageBaseBean.TAG_MSG_ZPOS);
					msg.mIsColsed = ConvertUtils.int2boolean(getJasonIntValue(obj, MessageBaseBean.TAG_MSG_ISCLOSED));
				}
				if (type == MessageBaseBean.TYPE_HTML) {
					msg.mUrl = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_URL);
				}
				
				if ((viewType & MessageBaseBean.VIEWTYPE_STATUS_BAR) != 0) {
					msg.mIcon = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_ICON);
					msg.mDynamic = getJasonIntValue(obj, MessageBaseBean.TAG_MSG_DYNAMIC);
					msg.mIconpos = getJasonIntValue(obj, MessageBaseBean.TAG_MSG_ICONPOS);
					msg.mFullScreenIcon = getJasonStringValue(obj, MessageBaseBean.TAG_MSG_FULL_SCREEN_ICON);
				}
				
				JSONArray filterPkgs = getJasonArray(obj, MessageBaseBean.TAG_MSG_FILTET_MSGS);
				if (filterPkgs != null && filterPkgs.length() > 0) {
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < filterPkgs.length(); j++) {
						String pkg;
						try {
							pkg = (String) filterPkgs.get(j);
							if (!pkg.equals("")) {
								sb.append(pkg);
								sb.append(",");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					msg.mFilterPkgs = sb.toString();
				}
				
				JSONArray whitelist = getJasonArray(obj, MessageBaseBean.TAG_MSG_WHITE_LIST);
				if (whitelist != null) {
					StringBuilder sb = new StringBuilder();
					for (int j = 0; j < whitelist.length(); j++) {
						String pkg;
						try {
							pkg = (String) whitelist.get(j);
							if (!pkg.equals("")) {
								sb.append(pkg);
								sb.append(",");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					msg.mWhiteList = sb.toString();
				}
				
				msg.mIsNew = getJasonIntValue(obj, MessageBaseBean.TAG_MSG_IS_NEW);
				mMessageHeads.add(msg);
			}
		}
	}

	private String getJasonStringValue(JSONObject obj, String tag) {
		if (obj == null || tag == null) {
			return null;
		}
		try {
			return obj.getString(tag);
		} catch (JSONException e) {
			// TODO: handle exception
		}
		return null;

	}
	
	private JSONArray getJasonArray(JSONObject obj, String tag) {
		if (obj == null || tag == null) {
			return null;
		}
		try {
			return obj.getJSONArray(tag);
		} catch (JSONException e) {
			// TODO: handle exception
		}
		return null;

	}
	
	private int getJasonIntValue(JSONObject obj, String tag) {
		if (obj == null || tag == null) {
			return -1;
		}
		try {
			return obj.getInt(tag);
		} catch (JSONException e) {
			// TODO: handle exception
		}
		return -1;

	}

	// public String getMsgId(String id){
	// return mMessage.get(id).mId;
	// }

	public String getMsgTitle(String id) {
		for (int i = 0; i < mMessageHeads.size(); i++) {
			MessageHeadBean bean = mMessageHeads.get(i);
			if (bean != null && bean.mId != null && bean.mId.equals(id)) {

				return bean.mTitle;
			}
		}

		return null;
	}

	public int getMsgType(String id) {
		for (int i = 0; i < mMessageHeads.size(); i++) {
			MessageHeadBean bean = mMessageHeads.get(i);
			if (bean != null && bean.mId != null && bean.mId.equals(id)) {

				return bean.mType;
			}
		}
		return -1;
	}

	public int getMsgViewType(String id) {
		for (int i = 0; i < mMessageHeads.size(); i++) {
			MessageHeadBean bean = mMessageHeads.get(i);
			if (bean != null && bean.mId != null && bean.mId.equals(id)) {

				return bean.mViewType;
			}
		}
		return -1;
	}

	public String getMsgTimeStamp(String id) {
		for (int i = 0; i < mMessageHeads.size(); i++) {
			MessageHeadBean bean = mMessageHeads.get(i);
			if (bean != null && bean.mId != null && bean.mId.equals(id)) {

				return bean.mMsgTimeStamp;
			}
		}
		return null;
	}

	public boolean getMsgReaded(String id) {
		for (int i = 0; i < mMessageHeads.size(); i++) {
			MessageHeadBean bean = mMessageHeads.get(i);
			if (bean != null && bean.mId != null && bean.mId.equals(id)) {

				return bean.misReaded;
			}
		}
		return false;
	}
	
	public boolean getMsgRemoveded(String id) {
		for (int i = 0; i < mMessageHeads.size(); i++) {
			MessageHeadBean bean = mMessageHeads.get(i);
			if (bean != null && bean.mId != null && bean.mId.equals(id)) {

				return bean.mIsRemoved;
			}
		}
		return false;
	}

	
	public void clearMsgs() {
		if (mMessageHeads != null && !mMessageHeads.isEmpty()) {
			mMessageHeads.clear();
		}
	}

}
