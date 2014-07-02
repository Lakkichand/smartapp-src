package com.youle.gamebox.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import com.google.gson.Gson;
import com.ta.TAActivity;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.greendao.JsonEntry;
import com.youle.gamebox.ui.greendao.JsonEntryDao;
import com.youle.gamebox.ui.http.IUIWhithNetListener;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.BaseTitleBarView;
import com.youle.gamebox.ui.view.GameTitleBarView;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-4-22.
 */
public abstract class BaseFragment extends Fragment implements TAIResponseListener, IUIWhithNetListener {
    protected Gson gson = new Gson();
    protected String TAG = getClass().getSimpleName();
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        LOGUtil.e(TAG, "onViewStateRestored");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(view ==null) {
            view = inflater.inflate(getViewId(), null, false);
            ButterKnife.inject(this,view);
        }else if(view.getParent()!=null){
            ((ViewGroup)view.getParent()).removeView(view);
        }
        return view;
    }

    public void setTitleView(int id) {
        if (id > 0) {
            View view = LayoutInflater.from(getActivity()).inflate(id, null);
            setTitleView(view);
        }
    }

    public void setTitleView(View view) {
        if (view != null) {
            ((BaseActivity)getActivity()).setmTitleView(view);
        }
    }

    public BaseTitleBarView setTitleView() {
        BaseTitleBarView gameTitleBarView = new GameTitleBarView(getActivity());
        setTitleView(gameTitleBarView);
        return gameTitleBarView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LOGUtil.e(TAG, "onViewCreated");
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onLoadStart() {
        if(!(getActivity() instanceof HomeActivity)){
            ((BaseActivity)getActivity()).loadStart();
        }
    }

    @Override
    public void onSuccess(String content) {
        if(!(getActivity() instanceof HomeActivity)){
            ((BaseActivity)getActivity()).loadSuccess();
        }
    }


    @Override
    public void onFailure(Throwable error) {
        if(getActivity() instanceof BaseActivity){
            ((BaseActivity)getActivity()).loadFail();
        }
    }

    protected void cacheJson(long modelId, String json, AbstractApi abstractApi) {
        TARequest request = new TARequest();
        request.setResouce(modelId);
        request.setData(json);
        request.setTag(abstractApi);
        ((TAActivity) getActivity()).doCommand(R.string.saveCommond, request, this, false, false);
    }

    protected void loadCach(long model){
        WhereCondition recource = JsonEntryDao.Properties.Resouce.eq(model);
        QueryBuilder<JsonEntry> queryBuilder = DaoManager.getDaoSession().getJsonEntryDao().queryBuilder().where(recource);
        TARequest request = new TARequest() ;
        request.setData(queryBuilder);
        ((TAActivity) getActivity()).doCommand(R.string.getCommond, request, this, false, false);

    }

    @Override
    public void onSuccess(TAResponse response) {

    }

    @Override
    public void onRuning(TAResponse response) {

    }

    @Override
    public void onFailure(TAResponse response) {

    }

    @Override
    public void onFinish() {

    }


    public void back() {
        if (getFragmentManager().getFragments().size() > 1) {
            getFragmentManager().popBackStack();
        } else {
            getActivity().finish();
        }
    }




    /**
     * @param clazz
     * @param json
     * @param key   jsonObject 里面的key如果是多层可以这样写 如:data.a.b.c
     * @return
     * @throws JSONException
     */
    protected <T> T jsonToBean(Class<T> clazz, String json, String key) throws JSONException {
        if (json == null) return null;
        if (clazz == null) return null;
        JSONObject jsonObject = new JSONObject(json);
        if (key != null) {
            String[] keys = key.split("\\.");
            for (String k : keys) {
                jsonObject = jsonObject.optJSONObject(k);
                if (jsonObject == null) return null;
            }
        }
        T t = gson.fromJson(jsonObject.toString(), clazz);
        return t;
    }


    protected <T> T jsonToBean(Class<T> clazz, String json) throws JSONException {
        return jsonToBean(clazz, json, "data");
    }

    protected <T> List<T> jsonToList(Class<T> clazz, String json, String key) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray array = jsonObject.optJSONArray(key);
        List<T> list = new ArrayList<T>();
        if(array==null||array.length()==0) return list ;
        for (int i = 0; i < array.length(); i++) {
            try {
                T t = gson.fromJson(array.getJSONObject(i).toString(), clazz);
                list.add(t);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return list;
    }

    protected abstract int getViewId();

}
