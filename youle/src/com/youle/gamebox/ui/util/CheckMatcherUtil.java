package com.youle.gamebox.ui.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2014/6/11.
 */
public class CheckMatcherUtil {

    /**
     *
     * @param //是否有中文
     * @return
     */
    public static boolean checkfilename(String s){
        s=new String(s.getBytes());//用GBK编码
        String pattern="[\u4e00-\u9fa5]+";
        Pattern p= Pattern.compile(pattern);
        Matcher result=p.matcher(s);
        return result.find(); //是否含有中文字符
    }
}
