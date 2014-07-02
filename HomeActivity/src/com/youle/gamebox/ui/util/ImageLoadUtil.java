package com.youle.gamebox.ui.util;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.ta.TAActivity;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-4-24.
 */
public class ImageLoadUtil {
    private static String TAG = "ImageLoadUtil" ;
    public static  void displayImage(String url,ImageView imageView){
        ImageLoader.getInstance().displayImage(url,imageView,getOptions());
    }

    public static  void displayNotRundomImage(String url,ImageView imageView){
        ImageLoader.getInstance().displayImage(url,imageView,getBigOptions());
    }
    public static  void displayNotRundomImage(String url,ImageView imageView,ImageLoadingListener listener){
        ImageLoader.getInstance().displayImage(url,imageView,getBigOptions(),listener);
    }
    public static DisplayImageOptions getOptions(){
    DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.left_about_icon)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .displayer(new RoundedBitmapDisplayer(8))
                .build();
    return  options ;
    }
    public static DisplayImageOptions getBigOptions(){
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_launcher)
//                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.left_about_icon)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .considerExifParams(true)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .build();
        return  options ;
    }

}
