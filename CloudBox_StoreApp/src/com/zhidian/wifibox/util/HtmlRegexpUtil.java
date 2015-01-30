package com.zhidian.wifibox.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 过虑html标签工具
 * @author zhaoyl
 *
 */
public class HtmlRegexpUtil {

	private final static String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签
	private final static String regxpForImgTag = "<\\s*img\\s+([^>]*)\\s*>"; // 找出IMG标签
	
	 /**
     * 
     * 基本功能：过滤所有以"<"开头以">"结尾的标签
     * <p>
     * 
     * @param str
     * @return String
     */
    public static String filterHtml(String str) {
        Pattern pattern = Pattern.compile(regxpForHtml);
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        while (result1) {
            matcher.appendReplacement(sb, "");
            result1 = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    /**
     * 过滤掉img标签
     */
    public static String filterimgHtml(String str) {
        Pattern pattern = Pattern.compile(regxpForImgTag);
        Matcher matcher = pattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        boolean result1 = matcher.find();
        while (result1) {
            matcher.appendReplacement(sb, "");
            result1 = matcher.find();
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    

}
