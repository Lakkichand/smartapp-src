package com.youle.gamebox.ui.commond;

import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.greendao.JsonEntry;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by Administrator on 14-6-11.
 */
public class GetCachCommand extends TACommand {
    @Override
    protected void executeCommand() {
        QueryBuilder<JsonEntry> queryBuilder = (QueryBuilder) getRequest().getData();
        JsonEntry jsonEntry = queryBuilder.unique() ;
        TAResponse response = new TAResponse() ;
        response.setData(jsonEntry);
        setResponse(response);
    }
}
