package com.youle.gamebox.ui.commond;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.api.ReCommendGameApi;
import com.youle.gamebox.ui.greendao.JsonEntry;
import com.youle.gamebox.ui.greendao.JsonEntryDao;
import com.youle.gamebox.ui.greendao.RecommendGame;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;

import java.util.List;

/**
 * Created by Administrator on 14-4-23.
 */
public class YouleCommand extends TACommand {
    @Override
    protected final void executeCommand() {
        if(getRequest().getData()!=null) {
            persistance(getRequest());
        }
        TAResponse response = new TAResponse() ;
        response.setData(getDate(getRequest()));
        response.setTag(getRequest().getTag());
        setResponse(response);
    }
    private void persistance(TARequest request){
        JsonEntry jsonEntry = new JsonEntry() ;
        jsonEntry.setResouce(request.getResouce());
        jsonEntry.setJson(request.getData().toString());
        DaoManager.getDaoSession().getJsonEntryDao().insertOrReplace(jsonEntry) ;
    };
    private Object getDate(TARequest request){
        WhereCondition recource = JsonEntryDao.Properties.Resouce.eq(request.getResouce()) ;
        QueryBuilder<JsonEntry> queryBuilder = DaoManager.getDaoSession().getJsonEntryDao().queryBuilder().where(recource) ;
        JsonEntry jsonEntry = queryBuilder.unique() ;
        return jsonEntry ;
    } ;

}
