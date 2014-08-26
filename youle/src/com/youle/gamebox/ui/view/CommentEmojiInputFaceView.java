package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.emoji.EmojiView;
import com.youle.gamebox.ui.R;

import java.lang.reflect.Method;

public class CommentEmojiInputFaceView extends LinearLayout{
    @InjectView(R.id.btn_emoji)
	ImageView emojiBtn;
    @InjectView(R.id.et_message_input)
	EditText commentEdit;
    @InjectView(R.id.btn_send)
    TextView sendBtn;
    // 表情相关
    @InjectView(R.id.emoji_view)
    EmojiView mEmojiView;

	private Dialog isLoginDialog;
    private OnClickListener sendListener = null;
    Context mContext;

    public CommentEmojiInputFaceView(Context context) {
        super(context);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public CommentEmojiInputFaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    public CommentEmojiInputFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setOnSendListener(OnClickListener sendListener){
        this.sendListener = sendListener;
    }

    private void init(Context context){
        mContext = context;
        View view = LayoutInflater.from(mContext).inflate(R.layout.emoji_comment_input_layout,this);
        LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        );
        view.setLayoutParams(layoutParams);
        ButterKnife.inject(view);
        // 表情相关
        mEmojiView.initDefaultEmojiDate(mContext);
        mEmojiView.setTargetEdit(commentEdit);
        emojiBtn.setOnClickListener(emojiListener);
        sendBtn.setOnClickListener(sendButListener);
      //  sendBtn.setOnTouchListener(mainListener);
        commentEdit.setOnClickListener(editListener);


        //关闭点击输入框时的软件盘代码
//        if (Build.VERSION.SDK_INT <= 10) {//4.0以下 danielinbiti
//            commentEdit.setInputType(InputType.TYPE_NULL);
//        } else {
//            ((Activity)context).getWindow().setSoftInputMode(
//                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//            try {
//                Class<EditText> cls = EditText.class;
//                Method setShowSoftInputOnFocus;
//                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",
//                        boolean.class);
//                setShowSoftInputOnFocus.setAccessible(true);
//                setShowSoftInputOnFocus.invoke(commentEdit, false);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    /**
     * sendBut OnClick
     * @return
     */
    OnClickListener sendButListener  = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(sendListener!=null){
                sendListener.onClick(view);
            }
            closeEdit();
        }
    };


    /**
     * 设置 显示初始化后 editView默认的文字
     * @return
     */
    public void setShowEditViewText(String showText){
        if(commentEdit!=null){
            if(showText!=null && !"".equals(showText))commentEdit.setHint(showText);
        }
    }
    /**
     * 设置 显示初始化后 发送按钮的文字
     * @return
     */
    public void setShowSendButText(String showText){
        if(sendBtn!=null){
            if(showText!=null && !"".equals(showText))sendBtn.setText(showText);
        }
    }


	/**
	 * 得到输入框内容
	 * @return
	 */
	public String getEditText(){
		return commentEdit.getText().toString().trim();
	}
	
	//关团输入框
	public void closeEdit(){
        commentEdit.clearFocus();
        commentEdit.setText("");
        mEmojiView.setVisibility(View.GONE);
        hideSoftInput();
		//mainListener.onTouch(null, null);
	}
	
	//必须调用
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if(mEmojiView.getVisibility() == View.VISIBLE){
            	emojiBtn.setBackgroundResource(R.drawable.emoji_btn_emoji_selector_bottom);
				mEmojiView.setVisibility(View.GONE);
            	return false;
            }
            /*if(commentEdit.getVisibility() == View.VISIBLE){
            	mainListener.onTouch(null, null);
            	return false;
            }*/
        }
		return true;
	}

	OnTouchListener mainListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			commentEdit.clearFocus();
			commentEdit.setText("");
			commentEdit.setVisibility(View.GONE);
			emojiBtn.setVisibility(View.GONE);
			sendBtn.setVisibility(View.GONE);
			mEmojiView.setVisibility(View.GONE);
			hideSoftInput();
			return true;
		}
	};
	
	//显示表情
	OnClickListener emojiListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if(mEmojiView.getVisibility() == View.GONE){
				emojiBtn.setBackgroundResource(R.drawable.emoji__keyboard_selector_bottom);
				hideSoftInput();
				mEmojiView.setVisibility(View.VISIBLE);
			}else {
				emojiBtn.setBackgroundResource(R.drawable.emoji_btn_emoji_selector_bottom);
				mEmojiView.setVisibility(View.GONE);
				showSoftInput();
				
			}
		}
	};
	
	//显示发送按扭
	OnClickListener letMeSendListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
            showEditLayout("我来说两句");
		}

	};
	
	/**
	 * 显示编辑框等内容
	 * @param hint
	 */
	public void showEditLayout(String hint) {
		commentEdit.setVisibility(View.VISIBLE);
		emojiBtn.setVisibility(View.VISIBLE);
		sendBtn.setVisibility(View.VISIBLE);
		commentEdit.clearFocus();
		commentEdit.setFocusable(true);
		commentEdit.setFocusableInTouchMode(true);
		commentEdit.requestFocus();
		commentEdit.setHint(hint);
		showSoftInput();
	}

	
	OnClickListener editListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			showSoftInput();
            emojiBtn.setBackgroundResource(R.drawable.emoji_btn_emoji_selector_bottom);
            mEmojiView.setVisibility(GONE);
		}
	};
	
	private void hideSoftInput(){
		InputMethodManager imm=(InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(commentEdit.getWindowToken(), 0);
	}
	
	private void showSoftInput(){
		InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(commentEdit, InputMethodManager.SHOW_FORCED); 
	}
}
