package com.youle.gamebox.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.widget.TextView;
import com.emoji.EmojiData;

import java.util.ArrayList;
import java.util.HashMap;


public class EmojiShowTextView extends TextView {
    private static final int NAMELENGTH = 15; //假设昵称不超过15个字符 
    private ArrayList<HashMap<String, Object>> faceList;
    //private Map<String,String> faceMap;  
    private CharSequence text;

    private ImageGetter imageGetter = new ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            String sourceName = getContext().getPackageName() + ":drawable/" + source;
            int id = getResources().getIdentifier(sourceName, null, null);
            if (id != 0) {
                drawable = getResources().getDrawable(id);
                if (drawable != null) {
                    drawable.setBounds(0, 0,
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight());
                }
            }
            drawable.setBounds(0, 0, 40, 40);
            return drawable;
        }
    };

    public EmojiShowTextView(Context context) {
        super(context);
//        setAutoLinkMask(Linkify.ALL);
    }

    public EmojiShowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setAutoLinkMask(Linkify.ALL);
    }

    public EmojiShowTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
//        setAutoLinkMask(Linkify.ALL);
    }

//    public void setText(CharSequence text, Map<String,String> faceMap){  
//        this.faceMap = faceMap;  
//        setText(text);  
//    }

    public void setFaceText(CharSequence text) {
        if(TextUtils.isEmpty(text)) return;
        faceList = EmojiData.getInstance(getContext()).getEmojiData();
        setText(text, BufferType.EDITABLE);
    }

    @Override
    public CharSequence getText() {
        return text == null ? "" : text;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        this.text = text;

        String cs = text.toString();
        if (faceList != null) {
            //对表情符以img标记进行修饰，改用drawable显示出来

            for (HashMap<String, Object> face : faceList) {
                String codeText = (String) face.get("textItem");
                if (cs.contains(codeText)) {
                    String faceSrc = (String) face.get("srcItem");
                    cs = cs.replace(codeText, "<img src=\"" + faceSrc + "\" mce_src=\"" + faceSrc + "\">");
                }
            }
        }
        super.setText(Html.fromHtml(cs, imageGetter, null), type);
    }
}  
