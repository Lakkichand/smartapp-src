package com.youle.gamebox.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
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
              String sourceName = getContext().getPackageName()+":drawable/"+source;  
              int id = getResources().getIdentifier(sourceName,null,null);  
              if (id != 0){  
                  drawable = getResources().getDrawable(id);  
                  if (drawable!=null){  
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
        setAutoLinkMask(Linkify.ALL);  
    }  
  
    public EmojiShowTextView(Context context, AttributeSet attrs) {
        super(context, attrs);  
        setAutoLinkMask(Linkify.ALL);  
    }  
  
    public EmojiShowTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);  
        setAutoLinkMask(Linkify.ALL);  
    }  
      
//    public void setText(CharSequence text, Map<String,String> faceMap){  
//        this.faceMap = faceMap;  
//        setText(text);  
//    }
    
    public void setFaceText(CharSequence text){
    	faceList = EmojiData.getInstance(getContext()).getEmojiData();
    	setText(text);
    }
  
    @Override  
    public CharSequence getText() {  
        return text==null?"":text;  
    }  
  
    @Override  
    public void setText(CharSequence text, BufferType type) {  
        this.text = text;  
       
        String cs = text.toString();  
        if(null != cs || !"".equals(cs)){
        
        String font1="<font color=#d77e13>";  
        String font2="</font>";  
          
        //找以'@'开头以':'或' '结尾的子串，将其使用font标记进行修饰  
        int start=0;  
        while(true){  
            start=cs.indexOf("回复",start);  
            if (start<cs.length() && start>=0){  
                int end=cs.indexOf(' ',start);  
                if (end<cs.length() && end>0 && end-start<=NAMELENGTH){  
                    CharSequence subcs=new String(cs.subSequence(start, end).toString());  
                    cs=cs.replace(subcs,font1+subcs+font2 );  
                    start+=font1.length()+subcs.length()+font2.length();  
                }  
                else{  
                    end=cs.indexOf('：',start);  
                    if (end<cs.length() && end>0 && end-start<=NAMELENGTH){  
                        CharSequence subcs=new String(cs.subSequence(start+2, end).toString());  
                        cs=cs.replace(subcs,font1+subcs+font2 );  
                        start+=font1.length()+subcs.length()+font2.length();  
                    }  
                }  
                start+=1;  
            }  
            else{  
                break;  
            }  
        }  
          
        if (faceList!=null){  
            //对表情符以img标记进行修饰，改用drawable显示出来  
        	
        	for (HashMap<String, Object> face : faceList) {
        		String codeText = (String)face.get("textItem");
				if(cs.contains(codeText)){
					String faceSrc = (String) face.get("srcItem");
					cs = cs.replace(codeText, "<img src=\""+faceSrc+"\" mce_src=\""+faceSrc+"\">");
				}
			}
        }
//            Set<String> keys = faceMap.keySet();  
//            for(String key : keys){  
//                if (cs.contains(key)){  
//                    cs=cs.replace(key, "<img src=\""+faceMap.get(key)+"\" mce_src=\""+faceMap.get(key)+"\">");  
//                }  
//            }  
        }  
      
        super.setText(Html.fromHtml(cs,imageGetter,null), type);  
    }  
}  
