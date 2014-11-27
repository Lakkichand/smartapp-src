package com.youle.gamebox.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.emoji.EmojiView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.bean.DynamicSelectGameBean;
import com.youle.gamebox.ui.fragment.HomePageDymaicListFragment;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.SoftkeyboardUtil;

/**
 * Created by Administrator on 14-7-9.
 */
public class PublishDyView extends LinearLayout {
    @InjectView(R.id.speak)
    ImageView mSpeak;
    @InjectView(R.id.edittext)
    EditText mEdittext;
    @InjectView(R.id.facial)
    ImageView mFacial;
    @InjectView(R.id.send)
    TextView mSend;
    @InjectView(R.id.textinput)
    LinearLayout mTextinput;
    @InjectView(R.id.keybord)
    ImageView mKeybord;
    @InjectView(R.id.holdtospeek)
    TextView mHoldtospeek;
    @InjectView(R.id.soundinput)
    LinearLayout mSoundinput;
    @InjectView(R.id.append)
    View mAppend;
    @InjectView(R.id.redpoint)
    ImageView mRedpoint;
    @InjectView(R.id.input)
    LinearLayout mInput;
    @InjectView(R.id.faceview)
    EmojiView mFaceview;
    @InjectView(R.id.faceviewroot)
    LinearLayout mFaceviewroot;
    @InjectView(R.id.addimage)
    LinearLayout mAddimage;
    @InjectView(R.id.addgame)
    LinearLayout mAddgame;
    @InjectView(R.id.clear1)
    ImageView mClear1;
    @InjectView(R.id.clear2)
    ImageView mClear2;
    @InjectView(R.id.appendframe)
    LinearLayout mAppendframe;
    @InjectView(R.id.bottom)
    FrameLayout mBottom;
    @InjectView(R.id.rootframe)
    LinearLayout mRootframe;
    @InjectView(R.id.image_icon)
    ImageView mImageIcon ;
    @InjectView(R.id.game_icon)
    ImageView mGameIcon;
    @InjectView(R.id.gameName)
    TextView mGameName ;
    IDPListener listener;
    private PublisModel model = PublisModel.DYNAMIC;
    private  SendModel sendModel = SendModel.TEXT ;
    public interface IDPListener {
        public void send(String content,SendModel model);

        public void cleanImage();

        public void cleanGame();

        public void startRecoding();

        public void endRecoding();

        public void preFace(boolean show);
        public void selectGame();
        public void onCleanImage();
        public void onCleanGame() ;
    }

    public enum SendModel {
        TEXT, VOICE;
    }

    public enum PublisModel {
        DYNAMIC, COMMENT
    }

    public void setListener(IDPListener listener) {
        this.listener = listener;
    }
    public EditText getEditText(){
        return mEdittext;
    }
    public PublishDyView(Context context, PublisModel m) {
        super(context);
        this.model = m;
        LayoutInflater.from(context).inflate(R.layout.dynamic_comment, this);
        ButterKnife.inject(this);

        mFaceview.initDefaultEmojiDate(context);
        mFaceview.setTargetEdit(mEdittext);
        if (model == PublisModel.DYNAMIC) {
            mAppend.setVisibility(VISIBLE);
        } else {
            mAppend.setVisibility(GONE);
        }
        mEdittext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mFaceviewroot.setVisibility(View.GONE);
                mAppendframe.setVisibility(View.GONE);
                if(listener!=null){
                    listener.preFace(false);
                }
            }
        });

        mFacial.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mFaceviewroot.setVisibility(View.VISIBLE);
                mAppendframe.setVisibility(View.GONE);
                if(listener!=null){
                    listener.preFace(true);
                }
                showIM(false);
            }
        });

        mAppend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mFaceviewroot.setVisibility(View.GONE);
                mAppendframe.setVisibility(View.VISIBLE);
                if(listener!=null){
                    listener.preFace(false);
                }
                showIM(false);
            }
        });

        mSpeak.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mTextinput.setVisibility(View.GONE);
                mSoundinput.setVisibility(View.VISIBLE);
                mHoldtospeek.setVisibility(VISIBLE);
                sendModel = SendModel.VOICE;
                if(listener!=null){
                    listener.preFace(false);
                }
                showIM(false);
                mFaceviewroot.setVisibility(View.GONE);
                mAppendframe.setVisibility(View.GONE);
                mSend.setVisibility(VISIBLE);
            }
        });

        mKeybord.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mTextinput.setVisibility(View.VISIBLE);
                mSoundinput.setVisibility(View.GONE);
                mFaceviewroot.setVisibility(View.GONE);
                mAppendframe.setVisibility(View.GONE);
                if (listener != null) {
                    listener.preFace(false);
                }
                showIM(true);
                sendModel = SendModel.TEXT;
                mSend.setVisibility(GONE);
            }
        });
        mEdittext.setFilters(new InputFilter[] {
              new InputFilter.LengthFilter(140)});
        mEdittext.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkSendState();
            }
        });

        // 选择图片
        mAddimage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ((BaseActivity) getContext()).startActivityForResult(i, HomePageDymaicListFragment.RESULT_LOAD_IMAGE);
            }
        });
        // 选择游戏
        mAddgame.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(listener!=null){
                    listener.selectGame();
                }
            }
        });

        mSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEdittext.getText().toString();
                if (listener != null) {
                    listener.send(content,sendModel);
                }
            }

        });
        mHoldtospeek.setOnTouchListener(onTouchListener);
        checkSendState();
    }
    public void cleanEdite(){
        mEdittext.setText("");
        SoftkeyboardUtil.hideSoftKeyBoard(getContext(),mEdittext);
        mAppendframe.setVisibility(GONE);
        mSend.setVisibility(VISIBLE);
        mRedpoint.setVisibility(GONE);
        mHoldtospeek.setText(R.string.press_say);
        mGameIcon.setImageResource(R.drawable.icon_addgame);
        mImageIcon.setImageResource(R.drawable.icon_addimage);
        mRedpoint.setVisibility(GONE);
        mClear1.setVisibility(GONE);
        mClear2.setVisibility(GONE);
    }

    public void setModel(SendModel m){
        sendModel = m ;
        if(m==SendModel.TEXT){
            mEdittext.setVisibility(VISIBLE);
            mHoldtospeek.setVisibility(GONE);
            mSend.setVisibility(GONE);
            mFacial.setVisibility(VISIBLE);
        }else {
            mEdittext.setVisibility(GONE);
//            mHoldtospeek.setVisibility(VISIBLE);
            mSpeak.setVisibility(GONE);
            mSoundinput.setVisibility(View.VISIBLE);
            mFacial.setVisibility(GONE);
            mSend.setVisibility(VISIBLE);
        }
    }
    /**
     * 有发送内容就显示发送按钮，没有就不显示
     */
    private void checkSendState() {
        String input = mEdittext.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            mSend.setVisibility(View.GONE);
        } else {
            mSend.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 是否显示输入法
     *
     * @param show
     */
    public void showIM(boolean show) {
        InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show) {
//            if (!mEdittext.isFocused()) {
//                mEdittext.requestFocus();
//            }
//            im.showSoftInput(mEdittext, InputMethodManager.SHOW_IMPLICIT);
            SoftkeyboardUtil.showSoftKeyBoard(getContext(),mEdittext);
        } else {
            if(listener!=null){
                listener.preFace(true);
            }
//            im.hideSoftInputFromWindow(mRootframe.getApplicationWindowToken(),
//                    InputMethodManager.HIDE_NOT_ALWAYS);
            SoftkeyboardUtil.hideSoftKeyBoard(getContext(),mEdittext);
        }
    }

    public void updateSelect(final  Bitmap mSelectImage,final  DynamicSelectGameBean mSelectGame){
        if (mSelectImage == null && mSelectGame == null) {
            mRedpoint.setVisibility(GONE);
        } else {
            mRedpoint.setVisibility(VISIBLE);
        }
        if(mSelectImage==null){
            mClear1.setVisibility(GONE);
            mImageIcon.setImageResource(R.drawable.icon_addimage);
        }else {
            mClear1.setVisibility(VISIBLE);
            mImageIcon.setImageBitmap(mSelectImage);
        }
        if(mSelectGame == null){
            mClear2.setVisibility(GONE);
            mGameIcon.setImageResource(R.drawable.icon_addgame);
        }else {
            mClear2.setVisibility(VISIBLE);
            ImageLoadUtil.displayImage(mSelectGame.iconUrl, mGameIcon);
        }
        mClear1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelect(null,mSelectGame);
                if(listener!=null){
                    listener.cleanImage();
                }
            }
        });
        mClear2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSelect(mSelectImage,null);
                if(listener!=null){
                    listener.cleanGame();
                }
            }
        });
    }

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (listener != null) {
                    listener.startRecoding();
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                listener.endRecoding();
            }
            return true;
        }
    };
    public void setVoiceLength(long length){
        if(length>0) {
            mHoldtospeek.setText(length + " '");
        }
    }
}
