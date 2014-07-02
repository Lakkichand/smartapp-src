package com.youle.gamebox.ui;

import android.content.Context;
import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ta.TAApplication;
import com.youle.gamebox.ui.commond.GetCachCommand;
import com.youle.gamebox.ui.commond.YouleCommand;
import com.youle.gamebox.ui.util.DownLoadUtil;

/**
 * Created by Administrator on 14-4-23.
 */
public class YouleAplication extends TAApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        DaoManager.getInstance().init(this);//初始化数据库
        initImageLoader(getApplicationContext());
        registCommond();
        DownLoadUtil.init();

    }
    public void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .discCacheSize(50 * 1024 * 1020)
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }
    private void registCommond(){
        getApplication().registerCommand(R.string.saveCommond, YouleCommand.class);
        getApplication().registerCommand(R.string.getCommond, GetCachCommand.class);
    }
}
