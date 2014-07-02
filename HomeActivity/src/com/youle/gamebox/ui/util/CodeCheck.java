package com.youle.gamebox.ui.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2014/6/11.
 */
public class CodeCheck {

    public static String pcCheckCode(String json){
        String loginCode = "";
        try {
            String code = jsonToCode(json,"code");
            if("1001".equals(code)){
                loginCode = "服务器忙";

            }else if("1002".equals(code)){
                loginCode = "用户名或者密码错误";
            }else if("1003".equals(code)){
                loginCode = "用户已经被禁用，请联系客服";
            }else if("1004".equals(code)){
                loginCode = "用户名已经存在";
            }else if("1005".equals(code)){
                loginCode = "用户名格式不合法（长度、中文等限制）";
            }else if("1006".equals(code)){
                loginCode = "密码格式不合法（长度、中文等限制）";
            }else if("1100".equals(code)){
                loginCode = "sid无效，请登录在操作";
            }else if("1101".equals(code)){
                loginCode = "提交的参数不合法";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loginCode;
    }

    public static String jsonToCode(String json,String key) throws JSONException {
        String code = "";
        JSONObject jsonObject = new JSONObject(json);
        code = jsonObject.optString(key);
        return code;

    }
    public static Object jsonToObject(String json,String key) throws JSONException {
        Object code = "";
        JSONObject jsonObject = new JSONObject(json);
        code = jsonObject.opt(key);
        return code;

    }
}
