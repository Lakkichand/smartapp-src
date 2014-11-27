package com.youle.gamebox.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.ta.TAApplication;
import com.ta.util.config.TAIConfig;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.SettingActivity;
import com.youle.gamebox.ui.api.InstallOrDeleteApi;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.greendao.GameBeanDao;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.PhoneInformation;
import de.greenrobot.dao.query.WhereCondition;

import java.io.File;

/**
 * Created by Administrator on 14-7-9.
 */
public class AppBroadcastReceiver extends BroadcastReceiver {
    private final String ADD_APP ="android.intent.action.PACKAGE_ADDED";
    private final String REMOVE_APP ="android.intent.action.PACKAGE_REMOVED";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if (ADD_APP.equals(action)) {
            String packageName = intent.getDataString().split(":")[1];
//            DownLoadUtil.installApp.add(packageName);
            UserInfo userInfo = new UserInfoCache().getUserInfo() ;
            TAIConfig config = TAApplication.getApplication().getPreferenceConfig();
            GameBeanDao dao = DaoManager.getDaoSession().getGameBeanDao();
            WhereCondition whereCondition = GameBeanDao.Properties.PackageName.eq(packageName);
            GameBean bean = dao.queryBuilder().where(whereCondition).unique();
            if(config.getBoolean(SettingActivity.DELETE_AFTERINSTALL,true)){
                //TODO 删除本地文件
                if(bean!=null){
                    String file = bean.getDownloadPath();
                    if(!TextUtils.isEmpty(file)){
                        File f = new File(file);
                        f.delete();
                    }
                }
            }
            if(bean!=null){
                DownLoadUtil.downLoadBeanMap.remove(bean.getDownloadUrl());
                dao.delete(bean);
            }
            if(userInfo!=null) {
                InstallOrDeleteApi api = new InstallOrDeleteApi();
                PhoneInformation phoneInformation = new PhoneInformation(context);
                api.setDeviceCode(phoneInformation.getDeviceCode());
                api.setVersion(phoneInformation.getSdkVersion());
                api.setNetworkType(phoneInformation.getNetworkType());
                api.setResolution(phoneInformation.getResolution());
                api.setPhoneModel(phoneInformation.getPhoneModel());
                api.setReleaseVersion(phoneInformation.getReleaseVersion());
                api.setPackageName(packageName);
                api.setSid(userInfo.getSid());
                api.setType(InstallOrDeleteApi.INSTALL);
                ZhidianHttpClient.request(api,new JsonHttpListener(false));
            }
        }
        if (REMOVE_APP.equals(action)) {
            UserInfo userInfo = new UserInfoCache().getUserInfo() ;
            String packageName = intent.getDataString().split(":")[1];
            DownLoadUtil.installApp.remove(packageName);
//            if(userInfo!=null) {
//                InstallOrDeleteApi api = new InstallOrDeleteApi();
//                PhoneInformation phoneInformation = new PhoneInformation(context);
//                api.setDeviceCode(phoneInformation.getDeviceCode());
//                api.setVersion(phoneInformation.getSdkVersion());
//                api.setNetworkType(phoneInformation.getNetworkType());
//                api.setResolution(phoneInformation.getResolution());
//                api.setPhoneModel(phoneInformation.getPhoneModel());
//                api.setReleaseVersion(phoneInformation.getReleaseVersion());
//                api.setPackageName(packageName);
//                api.setSid(userInfo.getSid());
//                api.setType(InstallOrDeleteApi.DELETE);
//                ZhidianHttpClient.request(api,new JsonHttpListener(false));
//            }
        }
    }
}
