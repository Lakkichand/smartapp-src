package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageWidgetBean;

/**
 * 
 * @author rongjinsong
 * 
 */
public class MessageContentAdapter extends BaseAdapter implements MessageDownLoadObserver {

	private Context mContext;
	private ArrayList<MessageWidgetBean> mWidgets;
	private OnClickListener mClickListener;
	private NinePatchDrawable mImgBackGround;
	private int mRes;
	private final static int MSG_REFRASH_VIEW = 0X1;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);
			if (msg.what == MSG_REFRASH_VIEW) {
				MessageContentAdapter.this.notifyDataSetChanged();
			}

		}

	};

	public MessageContentAdapter(Context context, ArrayList<MessageWidgetBean> widgets, int res) {
		mContext = context;
		mWidgets = new ArrayList<MessageWidgetBean>(widgets);
		mImgBackGround = (NinePatchDrawable) context.getResources().getDrawable(
				R.drawable.message_img_bg);
		mRes = res;
	}

	public void setViewClickListener(OnClickListener listener) {
		mClickListener = listener;
	}

	public void setWidgetListData(ArrayList<MessageWidgetBean> widgets) {
		mWidgets = widgets;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mWidgets != null) {
			return mWidgets.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		if (mWidgets != null && position < mWidgets.size()) {
			return mWidgets.get(position);
		}
		return null;
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
			convertView = LayoutInflater.from(mContext).inflate(mRes, null);
		}
		if (mWidgets.size() > position) {
			MessageWidgetBean widget = mWidgets.get(position);
			View widgetView = null;
			View txtView = convertView.findViewById(R.id.message_content);
			txtView.setVisibility(View.GONE);
			View imgView = convertView.findViewById(R.id.message_image);
			imgView.setVisibility(View.GONE);
			if (widget.mType == MessageWidgetBean.TYPE_TEXT) {
				txtView.setVisibility(View.VISIBLE);
				widgetView = txtView;
				widget.initView(widgetView, null);
			} else if (widget.mType.equals(MessageWidgetBean.TYPE_HERF)) {
				txtView.setVisibility(View.VISIBLE);
				widgetView = txtView;
				widget.initView(widgetView, null);
			} else if (widget.mType.equals(MessageWidgetBean.TYPE_IMG)) {
				imgView.setVisibility(View.VISIBLE);
				widgetView = imgView;
				widget.initView(widgetView, this);
				Rect rect = new Rect();
				imgView.getDrawingRect(rect);
				mImgBackGround.setBounds(0, 0, rect.width() + 10, rect.height() + 2);
				imgView.setBackgroundDrawable(mImgBackGround);
			}
			if (widgetView != null) {
				widgetView.setTag(widget);
				widgetView.setOnClickListener(mClickListener);
			}
		}
		return convertView;
	}

	@Override
	public void onDownLoadFinsish() {
		// TODO Auto-generated method stub
		mHandler.sendEmptyMessage(MSG_REFRASH_VIEW);

	}

}
