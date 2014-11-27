package com.youle.gamebox.ui.account;

import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.bean.LogAccount;
import com.youle.gamebox.ui.greendao.LogUser;
import com.youle.gamebox.ui.greendao.LogUserDao;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.greendao.UserInfoDao;
import com.youle.gamebox.ui.util.CoderString;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;

import java.util.List;

/**
 * Created by Administrator on 2014/6/12.
 */
public class UserInfoCache {



    //save user info
    public void saveUserInfo(UserInfo userInfo){
//        delUserInfoTable();
        UserInfoDao userInfoDao = DaoManager.getDaoSession().getUserInfoDao();
        userInfo.setLastLogin(System.currentTimeMillis());
        long l = userInfoDao.insertOrReplace(userInfo);
    }


    //del table

    public void delUserInfoTable(){
        UserInfoDao userInfoDao = DaoManager.getDaoSession().getUserInfoDao();
        userInfoDao.deleteAll();
    }



    //get sid
    public  String  getSid(){
        String sid = "" ;
        UserInfoDao userInfoDao = DaoManager.getDaoSession().getUserInfoDao();
        QueryBuilder<UserInfo> queryBuilder = userInfoDao.queryBuilder();
        Query<UserInfo> build = queryBuilder.build();
        List<UserInfo> list = build.list();
        if(list!=null){
            if(list.size()>0){
                UserInfo userInfo = list.get(0);
                sid = userInfo.getSid();
            }
        }
        return sid;
    }

    // get userinfo
    public  UserInfo  getUserInfo(){
        UserInfo userInfo = null;
        UserInfoDao userInfoDao = DaoManager.getDaoSession().getUserInfoDao();
        QueryBuilder<UserInfo> queryBuilder = userInfoDao.queryBuilder();
        Query<UserInfo> build = queryBuilder.build();
        List<UserInfo> list = build.list();
        if(list!=null){
            if(list.size()>0){
                userInfo = list.get(0);
            }
        }
        return userInfo;
    }









}
