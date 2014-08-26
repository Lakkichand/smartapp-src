package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
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
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.DeleteDialog;
import org.json.JSONException;
import pl.droidsonroids.gif.GifImageView;

import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 14-6-16.
 */
public class DownloadManagerItemFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener, GameUpdateManagerAdapter.OnSelectChange {
    public static int INSTALL = 1;
    public static int UPDATA = 2;
    public static int DOWNLOAD = 3;
    @InjectView(R.id.allcheck)
    CheckBox mAllcheck;
    @InjectView(R.id.updateListView)
    ListView mUpdateListView;
    List<GameBean> gameBeanList;
    GameUpdateManagerAdapter adapter;
    @InjectView(R.id.checkTip)
    TextView mCheckTip;
    @InjectView(R.id.indicatorLayout)
    RelativeLayout mIndicatorLayout;
    @InjectView(R.id.noDate)
    LinearLayout mNoDate;
    @InjectView(R.id.loading)
    GifImageView mLoading;
    @InjectView(R.id.noNet)
    LinearLayout mNoNet;
    private int type = 0;

    public DownloadManagerItemFragment(int type) {
        super();
        this.type = type;
    }

    private void reset() {
        mNoDate.setVisibility(View.GONE);
        mIndicatorLayout.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mNoNet.setVisibility(View.GONE);
    }

    @Override
    public void onLoadStart() {
        reset();
        mLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccess(String content) {
        reset();
        mIndicatorLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFailure(Throwable error) {
        reset();
        mNoNet.setVisibility(View.VISIBLE);
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_download_item;
    }

    @Override
    protected String getModelName() {
        if (type == INSTALL) {
            return "已安装管理";
        } else if (type == UPDATA) {
            return "更新管理";
        } else {
            return "下载管理";
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (gameBeanList == null) {
            initData();
        }
    }

    private void initData() {
        if (type == INSTALL) {
            initInstallDate();
            mCheckTip.setText(R.string.alluninstall);
            mCheckTip.setBackgroundColor(getResources().getColor(R.color.red));
        } else if (type == UPDATA) {
            initUpdate();
        } else {
            initDownloadManager();
            mCheckTip.setText(R.string.alldelete);
            mCheckTip.setBackgroundColor(getResources().getColor(R.color.red));
        }
        mAllcheck.setOnCheckedChangeListener(this);
        mCheckTip.setOnClickListener(onClickListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (adapter.getSelecteGameSet().size() == 0) {
                UIUtil.toast(getActivity(), R.string.no_select);
                return;
            }
            DeleteDialog deleteDialog;
            if (type == DOWNLOAD) {
                deleteDialog = new DeleteDialog(getActivity(), "删除", "确定删除全部？");
            } else if (type == UPDATA) {
                deleteDialog = new DeleteDialog(getActivity(), "升级", "确定升级全部？");
            } else {
                deleteDialog = new DeleteDialog(getActivity(), "卸载", "确定卸载全部？");
            }
            deleteDialog.setListener(new DeleteDialog.IDialogOperationListener() {
                @Override
                public void onSure() {
                    Set<GameBean> gameBeans = adapter.getSelecteGameSet();
                    for (GameBean gameBean : gameBeans) {
                        if (type == INSTALL) {
                            AppInfoUtils.uninstall(getActivity(), gameBean.getPackageName());
                        } else if (type == UPDATA) {
                            gameBean.setDownloadStatus(DownLoadUtil.FAIL);
                            adapter.downLoadBean(gameBean);
                        } else if (type == DOWNLOAD) {
                            adapter.delete(gameBean);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });
            deleteDialog.show();
        }
    };

    private void initUpdate() {
        if (gameBeanList == null) {
            requestUpdateData();
        } else {
            mUpdateListView.setAdapter(adapter);
        }
    }

    private void initInstallDate() {
        requestInstallData();
    }

    private void initDownloadManager() {
        if (DownLoadUtil.getInstance(null).getDowanLoadList().size() == 0) {
            reset();
            mNoDate.setVisibility(View.VISIBLE);
        } else {
            if (adapter == null) {
                adapter = new GameUpdateManagerAdapter(getActivity(), DownLoadUtil.getInstance(null).getDowanLoadList(), type);
                adapter.setOnSelectChange(this);
            }
            mUpdateListView.setAdapter(adapter);
        }
    }

    private void requestUpdateData() {
        GameUpdateApi gameUpdateApi = new GameUpdateApi();
        gameUpdateApi.setPackageVersions(AppInfoUtils.getPkgAndVersion(getActivity()));
        ZhidianHttpClient.request(gameUpdateApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                parseJson(jsonString);
            }
        });
    }

    private void requestInstallData() {
        GameInstallApi gameInstallApi = new GameInstallApi();
        StringBuilder sb = new StringBuilder();
        List<AppInfoBean> apps = AppInfoUtils.getPhoneAppInfo(getActivity());
        for (AppInfoBean b : apps) {
            sb.append(b.getPackageName()).append(",");
        }
        gameInstallApi.setPackageNames(sb.toString());
        ZhidianHttpClient.request(gameInstallApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                parseJson(jsonString);
            }
        });
    }

    private void parseJson(String json) {
        if(getActivity()==null) return;
        try {
            gameBeanList = jsonToList(GameBean.class, json, "data");
            if (gameBeanList.size() > 0) {
                reset();
                mIndicatorLayout.setVisibility(View.VISIBLE);
            } else {
                reset();
                mNoDate.setVisibility(View.VISIBLE);
            }
            adapter = new GameUpdateManagerAdapter(getActivity(), gameBeanList, type);
            adapter.setOnSelectChange(this);
            mUpdateListView.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null) {
            adapter.onDestroy();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            adapter.selectedAll();
        } else {
            adapter.cancelAll();
        }
    }

    @Override
    public void OnAllCheckedCancel() {
        mAllcheck.setChecked(false);
    }

    @Override
    public void onDelete(GameBean b) {
        if (adapter.getCount() < 1) {
            mNoDate.setVisibility(View.VISIBLE);
            mIndicatorLayout.setVisibility(View.GONE);
        }
    }
}
