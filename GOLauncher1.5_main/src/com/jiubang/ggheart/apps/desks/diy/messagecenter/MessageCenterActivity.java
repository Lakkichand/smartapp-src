package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageBaseBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * 类描述:  消息中心列表内容的入口类
 * 功能详细描述:
 * 
 * @date  [2012-9-28]
 */
public class MessageCenterActivity extends Activity
		implements
			OnItemClickListener,
			OnKeyListener,
			OnClickListener,
			OnCancelListener,
			BroadCasterObserver {

	public final static int GET_MSG_LIST_FINISH = 0x01; // 获得消息列表
	public final static int GET_MSG_LIST_OK = 0x02;
	public final static int GET_MSG_LIST_ERRO = 0x03;
	public final static int GET_MSG_CONTENT_FINISH = 0X04; // 获得一个消息的具体内容
	public final static int GET_MSG_CONTENT_OK = 0x05;
	public final static int GET_MSG_CONTENT_FAILED = 0x06;
	public final static int GET_MSG_NO_NETWORK = 0x07;
	private final static int DIALOG_WAIT = 1;
	private BaseAdapter mAdapter;
	private ListView mListView;
	private TextView mMsgCount;
	private RelativeLayout mEmptyView;
	private Button mClearButton;
	private MessageManager mManager;
	private ImageView mReadTag;
	private LinearLayout mNewTag;
	// 获取系统的日期
	private static String sDate;

	private TextView mNoMsgtextview2;
	private TextView mNoMsgtext;
	private boolean mIsVisiable = false;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if (msg.what == GET_MSG_LIST_OK) {
				removeDialog(DIALOG_WAIT);
				int change = msg.arg1;
				Vector<MessageHeadBean> orginalMsgsList = mManager.getMessageList();
				Vector<MessageHeadBean> msgsList = null;
//				Vector<MessageHeadBean> msgsList = mManager.getMessageList();
				if (orginalMsgsList != null && !orginalMsgsList.isEmpty()) {
					msgsList = (Vector<MessageHeadBean>) orginalMsgsList.clone();
					HttpUtil.sortList(msgsList);
					
					if (!mIsVisiable || mIsVisiable && change == 1) {
						//当消息中心页面打开情况下，如若自动扫描数据也回调这接口，需判断数据是否有更新。
						mIsVisiable = true;
						for (MessageHeadBean bean : msgsList) {
							mManager.saveShowStatisticsData(bean.mId);
						}
						mManager.updateStatisticsData(msgsList, MessageBaseBean.VIEWTYPE_NORMAL, 0, IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES, null);
					}
				}
				
				if (mAdapter == null) {
					mAdapter = new MyAdapter(msgsList);
				} else {
					((MyAdapter) mAdapter).setAdapterData(msgsList);
				}
				mListView.setAdapter(mAdapter);
				mAdapter.notifyDataSetChanged();
				setMsgCount();
			} else if (msg.what == GET_MSG_LIST_ERRO) {
				removeDialog(DIALOG_WAIT);
				Vector<MessageHeadBean> msgsList = (Vector<MessageHeadBean>) mManager.getMessageList().clone();
				if (msgsList != null && msgsList.size() > 0) {
					HttpUtil.sortList(msgsList);
					if (mAdapter == null) {
						mAdapter = new MyAdapter(msgsList);
					} else {
						((MyAdapter) mAdapter).setAdapterData(msgsList);
					}
					mListView.setAdapter(mAdapter);
					mAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(MessageCenterActivity.this, R.string.msgcenter_msg_update_erro,
							500).show();
				}
				setMsgCount();
			} else if (msg.what == GET_MSG_CONTENT_OK) {
				removeDialog(DIALOG_WAIT);
				// mManager.showMessage();
			} else if (msg.what == GET_MSG_CONTENT_FAILED) {
				removeDialog(DIALOG_WAIT);
				Toast.makeText(MessageCenterActivity.this, R.string.msgcenter_msg_update_erro, 500)
						.show();
			} else if (msg.what == GET_MSG_NO_NETWORK) {
				Toast.makeText(MessageCenterActivity.this, R.string.http_exception, 500).show();
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.messagecentermain);

		// 取得系统的时间
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		Date date = calendar.getTime();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 日期格式
		sDate = df.format(date);

		// 空信息
		mEmptyView = (RelativeLayout) findViewById(R.id.empty_msg);
		mNoMsgtextview2 = (TextView) findViewById(R.id.nomsgtextview2);
		mNoMsgtext = (TextView) findViewById(R.id.nomsgtext);

		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(mAdapter);
		mMsgCount = (TextView) findViewById(R.id.count);
		mClearButton = (Button) findViewById(R.id.clear);
		mClearButton.setVisibility(View.GONE);

		mListView.setOnItemClickListener(this);
		setUpList();
	}

	/**
	 * 功能简述:  创建消息列表
	 * 功能详细描述: 判断是否存在网络 ，若存在，则去网络请求数据
	 * 注意:
	 */
	private void setUpList() {
		if (Machine.isNetworkOK(this)) {
			mListView.setVisibility(View.VISIBLE);
			mEmptyView.setVisibility(View.GONE);
			mClearButton.setOnClickListener(this);
			getMessageList();

			String str = this.getResources().getString(R.string.message_center_nomsg);
			mNoMsgtextview2.setText(str);
			mNoMsgtext.setText("");
		} else {
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			mClearButton.setVisibility(View.GONE);
			Toast.makeText(MessageCenterActivity.this, R.string.http_exception, 500).show();
		}
	}

	/**
	 * 功能简述:  网络请求，并注册观察者
	 */
	private void getMessageList() {
		showDialog(DIALOG_WAIT);
		if (mManager == null) {
			mManager = MessageManager.getMessageManager(GOLauncherApp.getContext());
			mManager.registerObserver(this);
		}

		mManager.postUpdateRequest(0);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mAdapter != null && mManager != null && mManager.getMessageList() != null
				&& mManager.getMessageList().size() > 0) {
			setMsgCount();
			HttpUtil.sortList(((MyAdapter) mAdapter).mMsgs);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		removeDialog(DIALOG_WAIT);
		if (mManager != null) {
			mManager.unRegisterObserver(this);
		}
	}

	/**
	 * 
	 * @date  [2012-9-28]
	 */
	public class MyAdapter extends BaseAdapter {

		public Vector<MessageHeadBean> mMsgs;

		public MyAdapter(Vector<MessageHeadBean> msgs) {
			mMsgs = msgs;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (mMsgs == null) {
				return 0;
			}
			return mMsgs.size();
		}

		public void setAdapterData(Vector<MessageHeadBean> msgs) {
			mMsgs = msgs;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(MessageCenterActivity.this).inflate(
						R.layout.messagelistitem, null);
			}
			TextView title = (TextView) convertView.findViewById(R.id.messagetitle);

			mReadTag = (ImageView) convertView.findViewById(R.id.readtag);
			mNewTag = (LinearLayout) convertView.findViewById(R.id.newtag);

			title.setText(getTitle(position));
			if (mMsgs.get(position).misReaded) {
				// 已读
				title.setTextColor(0xFF5E5D5D);
				mReadTag.setBackgroundResource(R.drawable.message_center_read);
				mNewTag.setVisibility(View.GONE);
			} else {
				//未读
				title.setTextColor(0xFF1B1A1A);
				mNewTag.setVisibility(View.VISIBLE);
				mReadTag.setBackgroundResource(R.drawable.message_center_unread);
			}

			TextView date = (TextView) convertView.findViewById(R.id.messagestamp);
			String messageDate = getDate(position);

			if (messageDate != null) {
				String temp = compareDate(messageDate, MessageCenterActivity.this);
				date.setText(temp);
			} else {
				date.setText("");
			}

			return convertView;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return mMsgs.isEmpty();
		}

		public String getTitle(int position) {

			if (mMsgs == null || position > mMsgs.size()) {
				return null;
			}
			return mMsgs.get(position).mTitle;
		}

		public String getDate(int position) {
			if (mMsgs == null || position > mMsgs.size()) {
				return null;
			}
			return mMsgs.get(position).mMsgTimeStamp;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if (((MyAdapter) mAdapter).mMsgs.size() > position) {
			MessageHeadBean bean = ((MyAdapter) mAdapter).mMsgs.get(position);
			mManager.handleMsgClick(bean, MessageBaseBean.VIEWTYPE_NORMAL);
		}
	}

	@Override
	public Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		if (id == DIALOG_WAIT) {
			dialog = ProgressDialog.show(this, "", getString(R.string.msgcenter_dialog_wait_msg),
					true);
			dialog.setOnKeyListener(this);
			dialog.setOnCancelListener(this);
		}
		return dialog;
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
			removeDialog(DIALOG_WAIT);
			if (mManager != null) {
				mManager.abortPost();
			}
			return true;
		}
		return false;
	}

	// @Override
	// public void updateFinish(boolean bool) {
	// // TODO Auto-generated method stub
	// mManager.removeUpdateListener();
	// removeDialog(DIALOG_WAIT);
	// if(bool)
	// {
	// mHandler.sendEmptyMessage(GET_DATE_OK);
	// }
	// else
	// {
	// mHandler.sendEmptyMessage(GET_DATE_ERRO);
	// }
	// }

	// @Override
	// public void getMsgFinish(boolean bool) {
	// // TODO Auto-generated method stub
	// mManager.removeUpdateListener();
	// removeDialog(DIALOG_WAIT);
	// if(bool)
	// {
	// mHandler.sendEmptyMessage(GET_MSG_OK);
	// }
	// else
	// {
	// mHandler.sendEmptyMessage(GET_MSG_FAILED);
	// }
	// }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v == mClearButton) {
			if (mManager != null) {
				mManager.markAllReaded();
				Vector<MessageHeadBean> msgsList = mManager.getMessageList();
				HttpUtil.sortList(msgsList);
				if (mAdapter == null) {
					mAdapter = new MyAdapter(msgsList);
				} else {
					((MyAdapter) mAdapter).setAdapterData(msgsList);
				}
				setMsgCount();
				if (mAdapter != null) {
					mAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		if (mManager != null) {
			mManager.abortPost();
		}
	}
	/**
	 * 功能简述:显示消息未读个数/总数
	 */
	private void setMsgCount() {
		if (mManager.getMessageList() != null && mManager.getMessageList().size() > 0) {
			mMsgCount.setText(String.valueOf("(" + mManager.getUnreadedCnt() + "/"
					+ String.valueOf(mManager.getMessageList().size()))
					+ ")");
		} else {
			mListView.setVisibility(View.GONE);
			mEmptyView.setVisibility(View.VISIBLE);
			mMsgCount.setText(null);
		}
		if (mManager.getUnreadedCnt() > 0) {
			mClearButton.setVisibility(View.VISIBLE);
		} else {
			mClearButton.setVisibility(View.GONE);
		}

		mMsgCount.invalidate();
	}

	// 消息的时间与系统的时间比对，然后返回相应的string 时间
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:今天：today+HH:mm,今年：MM-dd HH:mm 不是今年：yyyy-MM-dd HH:mm
	 * <br>注意:
	 * @param messageDate
	 * @param context
	 * @return
	 */
	public static String compareDate(String messageDate, Context context) {

		if (sDate == null) {
			// 取得系统的时间
			Date date = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 日期格式
			sDate = df.format(date);
		}

		// 标准时间date字符串化后 0~5之间是year ，5~10之间是month和day ,11~16之间是time

		String year = messageDate.substring(0, 5);
		String month_day = messageDate.substring(5, 10);
		String time = messageDate.substring(11, 16);

		String nowDate_year = sDate.substring(0, 5);
		String nowDate_month_day = sDate.substring(5, 10);

		String str = messageDate.substring(0, 10);
		java.text.SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

		String str_today = context.getResources().getString(R.string.today);
		String show = null;
		try {
			Date d = format.parse(str);
			// str_date为 "yyyy年MM月dd日"
			// DateFormat df = new SimpleDateFormat(date_rule);// 日期格式
			show = format.format(d);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (year.equals(nowDate_year)) {
			if (month_day.equals(nowDate_month_day)) {
				return str_today + " " + time;
			} else {
				return show != null ? show.substring(5) + " " + time : time;
			}
		} else {
			return show + " " + time;
		}

	}
	/**
	 * 回调函数的处理，并发消息到Handler中处理
	 */
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		// TODO Auto-generated method stub
		switch (msgId) {
			case GET_MSG_LIST_FINISH :
				boolean bool = ConvertUtils.int2boolean(param);
				if (bool) {
					int change = (Integer) object;
					Message msg = new Message();
					msg.what = GET_MSG_LIST_OK;
					msg.arg1 = change;
					mHandler.sendMessage(msg);
//					mHandler.sendEmptyMessage(GET_MSG_LIST_OK);
				} else {
					mHandler.sendEmptyMessage(GET_MSG_LIST_ERRO);
				}
				break;
			case GET_MSG_NO_NETWORK :
				mHandler.sendEmptyMessage(GET_MSG_NO_NETWORK);
				break;
			case GET_MSG_CONTENT_FINISH :
				bool = ConvertUtils.int2boolean(param);
				if (bool) {
					mHandler.sendEmptyMessage(GET_MSG_CONTENT_OK);
				} else {
					mHandler.sendEmptyMessage(GET_MSG_CONTENT_FAILED);
				}
				break;
			default :
				break;
		}
	}

	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
