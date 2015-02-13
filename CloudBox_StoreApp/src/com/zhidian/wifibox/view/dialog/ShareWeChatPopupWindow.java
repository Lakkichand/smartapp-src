package com.zhidian.wifibox.view.dialog;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.WeChatShareBean;
import com.zhidian.wifibox.util.ShareToWeChatUtil;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * 微信分享PopWindow
 * @author zhaoyl
 *
 */
public class ShareWeChatPopupWindow extends PopupWindow {


	private Button btn_cancel;//取消按钮
	private LinearLayout btnFriends,btnQuan;
	private View mMenuView;
	private WeChatShareBean chatBean;

	public ShareWeChatPopupWindow(Activity context,WeChatShareBean bean) {
		super(context);
		chatBean = bean;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.popipwindow_share_wechat, null);
		btnFriends = (LinearLayout) mMenuView.findViewById(R.id.popup_share_friends);
		btnQuan = (LinearLayout) mMenuView.findViewById(R.id.popup_share_friends_quan);
		btn_cancel = (Button) mMenuView.findViewById(R.id.btn_cancel);
		//取消按钮
		btn_cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//销毁弹出框
				dismiss();
			}
		});
		//设置按钮监听
		btnFriends.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 微信好友
				ShareToWeChatUtil weChatUtil = new ShareToWeChatUtil();
				chatBean.type = 1;
				weChatUtil.sendReq(chatBean);
				dismiss();
			}
		});
		btnQuan.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 微信朋友圈
				ShareToWeChatUtil weChatUtil = new ShareToWeChatUtil();
				chatBean.type = 0;
				weChatUtil.sendReq(chatBean);
				dismiss();
				
			}
		});
		//设置SelectPicPopupWindow的View
		this.setContentView(mMenuView);
		//设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.FILL_PARENT);
		//设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		//设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.AnimBottom);
		//实例化一个ColorDrawable颜色为透明
		//ColorDrawable dw = new ColorDrawable(Color.argb(0, 0, 0, 0));//透明
		ColorDrawable dw = new ColorDrawable(0xb0000000);//半透明
		//设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(dw);
		//mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
		mMenuView.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}				
				return true;
			}
		});

	}

}
