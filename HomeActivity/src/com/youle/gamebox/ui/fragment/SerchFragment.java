package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.api.GetSearTipApi;
import com.youle.gamebox.ui.api.SearchHomeApi;
import com.youle.gamebox.ui.bean.MiniGameBean;
import com.youle.gamebox.ui.bean.SearchBean;
import com.youle.gamebox.ui.bean.SearchTipBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.SoftkeyboardUtil;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.SearchPopView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 14-6-25.
 */
public class SerchFragment extends BaseFragment implements TextWatcher, SearchPopView.SearchItemOnClick {
    @InjectView(R.id.game)
    RadioButton mGame;
    @InjectView(R.id.gift)
    RadioButton mGift;
    @InjectView(R.id.gonglue)
    RadioButton mGonglue;
    @InjectView(R.id.searchRadioGroup)
    RadioGroup mSearchRadioGroup;
    @InjectView(R.id.keyEdit)
    EditText mKeyEdit;
    @InjectView(R.id.search_but)
    LinearLayout mSearchBut;
    @InjectView(R.id.likeGameLayout)
    LinearLayout mLikeGameLayout;
    @InjectView(R.id.hotSerchLayout)
    LinearLayout mHotSerchLayout;
    private SearchBean mSearchBean;
    private final int GAME = 1;//游戏
    private final int STAGRY = 2;//游戏
    private final int GIFT = 3;//游戏
    private int currentSech = GAME;

    @Override
    protected int getViewId() {
        return R.layout.fragment_serch_layout;
    }

    @Override
    protected String getModelName() {
        return "搜索";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSearchRadioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
        mSearchBut.setOnClickListener(onSerchClickListener);
        mKeyEdit.addTextChangedListener(this);
        if (mSearchBean == null) {
            setDefaultTitle(getString(R.string.serch));
            loadData();
        }
    }

    private void initGames() {
        if (getActivity() == null) return;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        for (MiniGameBean b : mSearchBean.getGames()) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.litle_game_layout, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.gameIcon);
            TextView textView = (TextView) view.findViewById(R.id.gameName);
            ImageLoadUtil.displayImage(b.getIconUrl(), imageView);
            textView.setText(b.getName());
            mLikeGameLayout.addView(view, layoutParams);
            view.setTag(b);
            view.setOnClickListener(onClickListener);
        }
        if (mSearchBean.getGames().size() < 4) {
            for (int i = mSearchBean.getGames().size(); i < 4; i++) {
                TextView t = new TextView(getActivity());
                mLikeGameLayout.addView(t, layoutParams);
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MiniGameBean b = (MiniGameBean) v.getTag();
            Intent intent = new Intent(getActivity(), GameDetailActivity.class);
            intent.putExtra(GameDetailActivity.GAME_ID, b.getId());
            intent.putExtra(GameDetailActivity.GAME_RESOUCE, b.getSource());
            intent.putExtra(GameDetailActivity.GAME_NAME, b.getName());
            startActivity(intent);
        }
    };


    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.gift) {
                currentSech = GIFT;
            } else if (checkedId == R.id.game) {
                currentSech = GAME;
            } else {
                currentSech = STAGRY;
            }
        }
    };
    private View.OnClickListener onSerchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.search_but) {
                search(mKeyEdit.getText().toString().trim());
            }
        }
    };

    private void search(String keyword) {
        SoftkeyboardUtil.hideSoftKeyBoard(getActivity(),mKeyEdit);
        if (keyword.length() > 0) {
            if (currentSech == GAME) {
                SeachGameFragment searchGameFragment = new SeachGameFragment(keyword);
                ((BaseActivity) getActivity()).addFragment(searchGameFragment, true);
            } else if (currentSech == GIFT) {
                AllGiftFragment all = new AllGiftFragment(keyword);
                ((BaseActivity) getActivity()).addFragment(all, true);
            } else {
                GonglueListFragment g = new GonglueListFragment();
                g.setKeyWorld(keyword);
                ((BaseActivity) getActivity()).addFragment(g, true);
            }
        } else {
            UIUtil.toast(getActivity(), R.string.input_not_nul);
        }
    }

    private void loadData() {
        SearchHomeApi searchHomeApi = new SearchHomeApi();
        searchHomeApi.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(searchHomeApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mSearchBean = jsonToBean(SearchBean.class, jsonString);
                    if (mSearchBean != null && getActivity() != null) {
                        initGames();
                        initHotTabs();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initHotTabs() {
        LinearLayout linearLayout = null;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.tab_height));
        p.weight = 1;
        for (int i = 0; i < mSearchBean.getTabs().size(); i++) {
            if (i % 3 == 0) {
                linearLayout = new LinearLayout(getActivity());
                linearLayout.setWeightSum(3);
                mHotSerchLayout.addView(linearLayout);
            }
            TextView textView = new TextView(getActivity());
            textView.setSingleLine();
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getResources().getColor(R.color.text_press_them));
            textView.setText(mSearchBean.getTabs().get(i));
            if (i % 2 != 0) {
                textView.setBackgroundColor(getResources().getColor(R.color.home_item_bg_1));
            }
            textView.setTag(mSearchBean.getTabs().get(i));
            textView.setOnClickListener(onTabOnClickListener);
            linearLayout.addView(textView, p);
        }
        for (int i = 0; i <= mSearchBean.getTabs().size() % 3; i++) {
            TextView textView = new TextView(getActivity());
            linearLayout.addView(textView, p);
        }
    }

    private View.OnClickListener onTabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String keyword = (String) v.getTag();
            search(keyword);
        }
    };
    boolean canTip = false;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        canTip = false;
    }

    @Override
    public void afterTextChanged(Editable s) {
        canTip = true;
        if (!TextUtils.isEmpty(mKeyEdit.getText())) {
            mKeyEdit.postDelayed(mRunnable, 1000);
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (canTip) {
                loadTip();
            }
        }
    };

    private void loadTip() {
        GetSearTipApi getSearTipApi = new GetSearTipApi();
        getSearTipApi.setKeyword(mKeyEdit.getText().toString());
        getSearTipApi.setType(currentSech);
        getSearTipApi.pageSize = 5;
        ZhidianHttpClient.request(getSearTipApi, new JsonHttpListener(false) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    JSONArray jsonArray = jsonObject.optJSONArray("data");
                    if (jsonArray != null) {
                        List<SearchTipBean> list = new ArrayList<SearchTipBean>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            SearchTipBean b = new SearchTipBean();
                            b.title = jsonArray.optString(i);
                            list.add(b);
                        }
                        if (list.size() > 0) {
                            showPop(list);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    PopupWindow pop;
    SearchPopView mSerchPopView;

    private void showPop(List<SearchTipBean> list) {
        if (!needShowPop()) return;
        if (mSerchPopView == null) {
            mSerchPopView = new SearchPopView(getActivity());
            mSerchPopView.setSearchItemOnClick(this);
        }
        mSerchPopView.setData(list);
        if (pop == null) {
            pop = new PopupWindow(mSerchPopView, mKeyEdit.getMeasuredWidth(), ViewGroup.LayoutParams.WRAP_CONTENT, false);
        }
        pop.setOutsideTouchable(false);
        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.showAsDropDown(mKeyEdit);
//        mKeyEdit.setFocusable(false);
//        mKeyEdit.setFocusableInTouchMode(false);
        mKeyEdit.requestFocus();
    }

    private boolean needShowPop() {
        String tag = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1).getName();
        if(tag.equals(getClass().getSimpleName())) {
            return true;
        }else {
            return false;
        }
    }

    @Override
    public void onclik(String string) {
        mKeyEdit.setText(string);
        pop.dismiss();
        search(string);
    }
}
