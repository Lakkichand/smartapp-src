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
import com.ta.TAApplication;
import com.ta.util.config.TAIConfig;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.SettingActivity;
import com.youle.gamebox.ui.greendao.GameBean;

/**
 * Created by Administrator on 14-4-24.
 */
public class ImageLoadUtil {
    private static String TAG = "ImageLoadUtil";

    public static void displayImage(String url, ImageView imageView) {
        ImageLoader.getInstance().displayImage(url, imageView, getOptions());
    }

    public static void displayAvatarImage(String url, ImageView imageView) {
        ImageLoader.getInstance().displayImage(url, imageView, getAvatarOptions());
    }

    public static void displayNotRundomImage(String url, final ImageView imageView) {
        TAIConfig config = TAApplication.getApplication().getPreferenceConfig();
        if (config.getBoolean(SettingActivity.SHOW_IMAGE, true)) {
            ImageLoader.getInstance().displayImage(url, imageView, getBigOptions(),new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    imageView.setBackgroundResource(R.drawable.pic_loading);
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    imageView.setBackgroundResource(R.drawable.detail_load_fail);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    imageView.setImageBitmap(bitmap);
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        } else {
            imageView.setBackgroundResource(R.drawable.detail_load_fail);
        }
    }



    public static DisplayImageOptions getOptions() {
        return getBaseBuild()
                .showImageOnLoading(R.drawable.game_loding_icon)
                .showImageForEmptyUri(R.drawable.game_loding_icon)
                .showImageOnFail(R.drawable.game_loding_icon)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .displayer(new RoundedBitmapDisplayer(0)).build();
    }

    public static DisplayImageOptions getBigOptions() {
        return getBaseBuild()
                .build();
    }

    public static DisplayImageOptions getAvatarOptions() {
        return getBaseBuild()
                .showImageOnLoading(R.drawable.pc_user_photo)
                .showImageForEmptyUri(R.drawable.pc_user_photo)
                .showImageOnFail(R.drawable.pc_user_photo)
                .build();
    }

    public static DisplayImageOptions.Builder getBaseBuild() {

        DisplayImageOptions.Builder build = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true);
        return build;
    }
}
