package com.youle.gamebox.ui;

import android.content.Context;
import android.graphics.Bitmap;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.ta.TAApplication;
import com.youle.gamebox.ui.bean.MessageNumberBean;
import com.youle.gamebox.ui.commond.GetCachCommand;
import com.youle.gamebox.ui.commond.YouleCommand;
import com.youle.gamebox.ui.util.DownLoadUtil;

/**
 * Created by Administrator on 14-4-23.
 */
public class YouleAplication extends TAApplication {
    //安全中心
//    public static final String SAFE_URL = "http://testsdk.y6.cn:8092/account/index";
//    public static final String COUNTRY_RUL = "http://testbbs.y6.cn:8888/discuz/forum.php?source=app";
//    public static final String MY_COUNTRY_URL = "http://testbbs.y6.cn:8888/discuz/forum.php?mod=touch&view=my&mobile=2&source=app";
    //正式
    public static final String SAFE_URL = "http://security.y6.cn/account/index" ;
    public static final String COUNTRY_RUL = "http://bbs.y6.cn/forum.php?source=app";
    public static final String MY_COUNTRY_URL= "http://bbs.y6.cn/forum.php?mod=touch&view=my&mobile=2&source=app";
    public static MessageNumberBean messageNumberBean = new MessageNumberBean();

    @Override
    public void onCreate() {
        super.onCreate();
        DaoManager.getInstance().init(this);//初始化数据库
        initImageLoader(getApplicationContext());
        registCommond();

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

    private void registCommond() {
        getApplication().registerCommand(R.string.saveCommond, YouleCommand.class);
        getApplication().registerCommand(R.string.getCommond, GetCachCommand.class);
    }
}
