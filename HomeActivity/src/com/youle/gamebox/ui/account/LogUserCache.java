package com.youle.gamebox.ui.account;

import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.bean.LogAccount;
import com.youle.gamebox.ui.greendao.LogUser;
import com.youle.gamebox.ui.greendao.LogUserDao;
import com.youle.gamebox.ui.util.CoderString;
import com.youle.gamebox.ui.util.LOGUtil;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;

import java.util.List;

/**
 * Created by Administrator on 2014/5/23.
 */
public class LogUserCache {



    //SD --> SQLite  -->UI
    public List<LogUser> getLogUserList(){
        List<LogAccount> accountList = new UserCache().getAccountList();
        if(accountList==null)return null;
        String str="";
        LogUserDao logUserDao = DaoManager.getDaoSession().getLogUserDao();
        for (int i = 0 ; i < accountList.size();i++){
            LogAccount logAccount = accountList.get(i);
            LogUser logUser = new LogUser();
            WhereCondition userName = LogUserDao.Properties.UserName.eq(logAccount.getUserName()) ;
            QueryBuilder<LogUser> queryBuilder = logUserDao.queryBuilder().where(userName) ;
            LogUser isLogUser = queryBuilder.unique() ;
            logUser.setUserName(logAccount.getUserName());
          //  logUser.setPassword(logAccount.getPassword());
            try {
                LOGUtil.d("junjun","---------"+logAccount.getPassword());
                logUser.setPassword(CoderString.encrypt(logAccount.getPassword()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            logUser.setLastLogin(logAccount.getLastLogin());
            if(isLogUser==null){
                LOGUtil.d("junjun", "is null insert");
                //logUser.setOption(logAccount.getOption());
                logUser.setIsdel("1");
                logUserDao.insertOrReplace(logUser);
            }else{
                // 0 游乐账号已被删除，SD木有被删除 ; 1 ：账号没有被删除
                if("1".equals(isLogUser.getIsdel())){
                    logUser.setOption(isLogUser.getOption());
                    logUser.setIsdel(isLogUser.getIsdel());
                    logUserDao.update(logUser);
                    LOGUtil.d("junjun","update table"+isLogUser.getUserName()+"   "+logAccount.getUserName());
                }
            }
            str  =  str +logAccount.getUserName()+" "+logAccount.getPassword()+" "+logAccount.getOption()+" "+logAccount.getLastLogin()+" \n";
        }
        WhereCondition isdel = LogUserDao.Properties.Isdel.eq("1") ;
        QueryBuilder<LogUser> queryBuilder = logUserDao.queryBuilder().where(isdel) ;
        Query<LogUser> build = queryBuilder.build();
        List<LogUser> list = build.list();
        return list;
    }


    // del users moidly isdel 0
    public void delLogUser(String mUserName){
        // 0 游乐账号已被删除，SD木有被删除 ; 1 ：游乐账号没有被删除
        LogUserDao logUserDao = DaoManager.getDaoSession().getLogUserDao();
        WhereCondition userName = LogUserDao.Properties.UserName.eq(mUserName) ;
        QueryBuilder<LogUser> queryBuilder = logUserDao.queryBuilder().where(userName) ;
        LogUser isLogUser = queryBuilder.unique() ;
        if(isLogUser!=null){
            LogUser logUser = new LogUser();
            logUser.setUserName(isLogUser.getUserName());
            logUser.setPassword(isLogUser.getPassword());
            logUser.setLastLogin(isLogUser.getLastLogin());
            logUser.setOption(isLogUser.getOption());
            logUser.setIsdel("0");
            logUserDao.update(logUser);
        }
    }

    //save user info and asyn SD logAccont
    public void SaveLogUser(LogUser mLogUser){
        LogUser logUser = new LogUser();
        logUser.setUserName(mLogUser.getUserName());
        try {
            logUser.setPassword(CoderString.encrypt(mLogUser.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        logUser.setOption("1");
        logUser.setLastLogin(mLogUser.getLastLogin());
        logUser.setIsdel("1");
        long l = DaoManager.getDaoSession().getLogUserDao().insertOrReplace(logUser);
        if(l>0){
            LogAccount logAccount = new LogAccount();
            logAccount.setUserName(logUser.getUserName());
            try {
                logAccount.setPassword(mLogUser.getPassword());
            } catch (Exception e) {
                e.printStackTrace();
            }
            logAccount.setOption("0"); // 0
            logAccount.setLastLogin(logUser.getLastLogin());
            new UserCache().saveAcount(logAccount);
        }

    }
}
