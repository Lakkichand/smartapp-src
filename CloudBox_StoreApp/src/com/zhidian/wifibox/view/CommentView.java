package com.zhidian.wifibox.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Toast;

import com.zhidian.wifibox.R;

/**
 * 发表评论View
 * @author zhaoyl
 *
 */
public class CommentView extends LinearLayout implements OnClickListener{

	private RatingBar ratingBar;// 星级评分
	private EditText etNickName; //昵称
	private EditText etContent; //评论内容
	private Button btnComment; //发表
	private View xianView; //横线
	private OnCallBackListener clickListener;
	private String nick,content,score;
	
	public interface OnCallBackListener{
		void onclick(String nickname, String content, String score);
	}
	
	public CommentView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public CommentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ratingBar = (RatingBar) findViewById(R.id.comment_info_rating);
		etNickName = (EditText) findViewById(R.id.comment_info_name);
		etContent = (EditText) findViewById(R.id.comment_info_content);
		btnComment = (Button) findViewById(R.id.comment_info_button);
		btnComment.setOnClickListener(this);
		xianView = (View) findViewById(R.id.head_comment_xian);
	}
	
	public View getXianView(){
		return xianView;
	}
	
	public void onCallBackClick(OnCallBackListener listener){
		this.clickListener = listener;
	}
	
	public boolean JullNotIsEmpty(){
		nick = etNickName.getText().toString().trim();
		if (TextUtils.isEmpty(nick)) {
			nick = "匿名";
		}
		content = etContent.getText().toString().trim();
		score =String.valueOf(ratingBar.getRating() * 2);
		if (TextUtils.isEmpty(content)) {
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.comment_info_button:
			if (JullNotIsEmpty()) {
				if (clickListener != null) {
					clickListener.onclick(nick,content,score);
					etNickName.setText("");
					etContent.setText("");
				}
			}else {
				Toast.makeText(getContext(), "还没写内容，求吐槽~~", Toast.LENGTH_SHORT).show();
			}
			
			break;

		default:
			break;
		}
		
	}
	

}
