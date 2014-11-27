package com.youle.gamebox.ui.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 14-6-4.
 */
public class RankApi extends AbstractApi {
   public enum RankType{
        ALL,NEW,MONTH,WEEK
    }

    @NoteParam
    private RankType rank ;

    @NoteParam
    private static Map<RankType,String>  urlMap = new HashMap<RankType, String>() ;

    public RankApi(RankType rank) {
        this.rank = rank;
        urlMap.put(RankType.ALL,"/gamebox/rank/standings/total");
        urlMap.put(RankType.NEW,"/gamebox/rank/standings/latest");
        urlMap.put(RankType.MONTH,"/gamebox/rank/standings/month");
        urlMap.put(RankType.WEEK,"/gamebox/rank/standings/week");
    }

    @Override
    protected String getPath() {
        return urlMap.get(rank);
    }
}
