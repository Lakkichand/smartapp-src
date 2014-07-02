package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.GameUpdateManagerAdapter;
import com.youle.gamebox.ui.api.game.GameInstallApi;
import com.youle.gamebox.ui.api.game.GameUpdateApi;
import com.youle.gamebox.ui.bean.AppInfoBean;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.DownLoadUtil;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-16.
 */
public class DownloadManagerItemFragment extends BaseFragment{
    public static int INSTALL=1 ;
    public static int UPDATA=2 ;
    public static int DOWNLOAD=3 ;
    @InjectView(R.id.allcheck)
    CheckBox mAllcheck;
    @InjectView(R.id.updateListView)
    ListView mUpdateListView;
    List<GameBean> gameBeanList ;
    GameUpdateManagerAdapter adapter ;
    @InjectView(R.id.checkTip)
    TextView mCheckTip;
    @InjectView(R.id.indicatorLayout)
    RelativeLayout mIndicatorLayout;
    private int type =0 ;

    public DownloadManagerItemFragment(int type) {
        super();
        this.type = type;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_download_item;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(type ==INSTALL){
            initInstallDate();
            mIndicatorLayout.setVisibility(View.GONE);
        }else if(type==UPDATA) {
           initUpdate();
        }else {
            initDownloadManager();
            mCheckTip.setText(R.string.alldelete);
            mCheckTip.setBackgroundColor(getResources().getColor(R.color.red));
        }
    }


    private void initUpdate(){
        if(gameBeanList==null){
            requestUpdateData();
        }else {
            mUpdateListView.setAdapter(adapter);
        }
    }
    private void initInstallDate(){
        if(gameBeanList==null){
            requestInstallData();
        }else {
            mUpdateListView.setAdapter(adapter);
        }
    }

    private void initDownloadManager(){
        if(adapter==null) {
            adapter = new GameUpdateManagerAdapter(getActivity(), DownLoadUtil.getInstance(null).getDowanLoadList());
        }
        mUpdateListView.setAdapter(adapter);
    }

    private void requestUpdateData(){
        GameUpdateApi gameUpdateApi = new GameUpdateApi() ;
        StringBuilder sb = new StringBuilder();
       List<AppInfoBean> apps= AppInfoUtils.getPhoneAppInfo(getActivity()) ;
        for (AppInfoBean b:apps){
            sb.append(b.getPackageName()+"|"+b.getVersionCode()).append(",");
        }
        gameUpdateApi.setPackageVersions(sb.toString());
        ZhidianHttpClient.request(gameUpdateApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                parseJson(jsonString);
            }
        });
    }
    private void requestInstallData(){
        GameInstallApi gameInstallApi = new GameInstallApi() ;
        StringBuilder sb = new StringBuilder();
        List<AppInfoBean> apps= AppInfoUtils.getPhoneAppInfo(getActivity()) ;
        for (AppInfoBean b:apps){
            sb.append(b.getPackageName()).append(",");
        }
        gameInstallApi.setPackageNames(sb.toString());
        ZhidianHttpClient.request(gameInstallApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                parseJson(jsonString);
            }
        });
    }

    private void parseJson(String json){
        try {
            gameBeanList=jsonToList(GameBean.class,json,"data") ;
             adapter = new GameUpdateManagerAdapter(getActivity(),gameBeanList);
            mUpdateListView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
