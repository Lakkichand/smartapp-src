package com.youle.gamebox.ui.api;

import com.ta.util.http.RequestParams;
import com.youle.gamebox.ui.http.HttpMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;

/**
 * Created by Administrator on 14-4-21.
 */
public abstract class AbstractApi {
    public static final String BASE_URL = "http://192.168.0.38:8086/";
    private final int pageSize = 20 ;
    private int pageNo= 1 ;


    public int getPageSize() {
        return pageSize;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    public String getUrl() {
        return BASE_URL + getPath();
    }


    public RequestParams getParams() {
        RequestParams params = new RequestParams();
        Class clazz = getClass();
        Field[] field = clazz.getDeclaredFields();
        try {
            for (Field f : field) {
                NoteParam p =  f.getAnnotation(NoteParam.class) ;
                if (p!=null)continue;
                f.setAccessible(true);
                if (f.get(this) != null) {
                    if (f.get(this) instanceof File) {
                        params.put(f.getName(), (File) f.get(this));
                    } else {
                        params.put(f.getName(), f.get(this).toString());
                    }
                }
            }
            params.put("pageSize",pageSize+"");
            params.put("pageNo",pageNo+"");
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return params;
    }

    protected abstract String getPath();
}
