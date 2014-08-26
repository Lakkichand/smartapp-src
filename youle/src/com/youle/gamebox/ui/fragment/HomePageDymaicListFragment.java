package com.youle.gamebox.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.ta.util.download.DownloadManager;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.*;
import com.youle.gamebox.ui.adapter.HomPageDymaicListAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.dynamic.DymaicListApi;
import com.youle.gamebox.ui.api.dynamic.DymaicPublishApi;
import com.youle.gamebox.ui.api.pcenter.HomePageDymaicListApi;
import com.youle.gamebox.ui.bean.DynamicSelectGameBean;
import com.youle.gamebox.ui.bean.dynamic.DymaicCommentsBean;
import com.youle.gamebox.ui.bean.dynamic.DymaicListBean;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.RecoderVoice;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.PublishDyView;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2014/6/18.
 */
public class HomePageDymaicListFragment extends NextPageFragment implements PublishDyView.IDPListener,DynamicCommentActivity.ICommentListener, HomPageDymaicListAdapter.OnComentClickListenr {
    HomePageDymaicListApi homePageDymaicListApi;
    DymaicListApi dymaicListApi;
    HomPageDymaicListAdapter homPageDymaicListAdapter;
    private PublishDyView buttomView;
    private Bitmap mSelectImage;
    private String imagePath;
    private String voicePath;
    private DynamicSelectGameBean mSelectGame;
    public static final int RESULT_LOAD_IMAGE = 0x01;
    public static final int RESULT_LOAD_GAME = 100;
    public static final int RESULT_ADD_COMMENT = 200;
    private long uid;
    public static final int OTHER = 2;
    public static final int MY = 1;
    DymaicListBean targetDynamic;
    int type;
    private String linkId ;

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public HomePageDymaicListFragment(long uid, int type) {
        this.uid = uid;
        this.type = type;
    }

    public HomePageDymaicListFragment() {
    }

    @Override
    public AbstractApi getApi() {
        if (homePageDymaicListApi != null) {
            return homePageDymaicListApi;
        } else {
            return dymaicListApi;
        }
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return homPageDymaicListAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(DymaicListBean.class, jsonStr, "data");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PauseOnScrollListener scrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        getListView().setOnScrollListener(scrollListener);
        loadData();
    }

    @Override
    protected String getModelName() {
        return "动态列表";
    }

    @Override
    protected void loadData() {
        if (getActivity() instanceof ComunityActivity) {
            getDynamicList();
            ((ComunityActivity) getActivity()).setFragment(this);
            if (buttomView == null) {
                buttomView = new PublishDyView(getActivity(), PublishDyView.PublisModel.DYNAMIC);
                buttomView.setListener(this);
                setBottomView(buttomView);
            }
        } else {
            UserInfo userInfo = new UserInfoCache().getUserInfo();
            if (uid>-1) {
                getMyDynamicList(uid);
            } else {
                if(userInfo!=null){
                    getMyDynamicList(userInfo.getUid());
                }else {
                    showNoContentLayout(true);
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(type == MY){
            loadData();
        }
    }

    private void getMyDynamicList(long uid) {
        homePageDymaicListApi = new HomePageDymaicListApi();
        homePageDymaicListApi.setSid(new UserInfoCache().getSid());
        homePageDymaicListApi.setUid(uid);
        homePageDymaicListApi.setLatestId(linkId);
        ZhidianHttpClient.request(homePageDymaicListApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    List<DymaicListBean> dymaicListBeans = jsonToList(DymaicListBean.class, jsonString, "data");
                    if (dymaicListBeans.size() > 0) {
                        showNoContentLayout(false);
                        homPageDymaicListAdapter = new HomPageDymaicListAdapter(getActivity(), dymaicListBeans, HomePageDymaicListFragment.this);
                        getListView().setAdapter(homPageDymaicListAdapter);
                    } else {
                        showNoContentLayout(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getDynamicList() {
        dymaicListApi = new DymaicListApi();
        dymaicListApi.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(dymaicListApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    List<DymaicListBean> dymaicListBeans = jsonToList(DymaicListBean.class, jsonString, "data");
                    homPageDymaicListAdapter = new HomPageDymaicListAdapter(getActivity(), dymaicListBeans, HomePageDymaicListFragment.this);
                    getListView().setAdapter(homPageDymaicListAdapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK
                && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            imagePath = picturePath;
            Bitmap bm = BitmapFactory.decodeFile(picturePath);
            mSelectImage = bm;
            buttomView.updateSelect(mSelectImage, mSelectGame);
        } else if (requestCode == RESULT_LOAD_GAME && resultCode == Activity.RESULT_OK
                && null != data) {
            DynamicSelectGameBean bean = data.getParcelableExtra("bean");
            mSelectGame = bean;
            LOGUtil.e("result", bean.name);
            buttomView.updateSelect(mSelectImage, mSelectGame);
        } else if (requestCode == RESULT_ADD_COMMENT && resultCode == Activity.RESULT_OK) {
            String commentBeanJson = data.getStringExtra("json");
            onCommentSuccess(commentBeanJson);
        }
    }

    public void publishDynamic(String text, PublishDyView.SendModel model) {
        DymaicPublishApi publishApi = new DymaicPublishApi();
        publishApi.setSid(new UserInfoCache().getSid());
        if (imagePath != null) {
            publishApi.setImage(new File(imagePath));
        }
        if (model == PublishDyView.SendModel.VOICE) {
            publishApi.setVoice(new File(voicePath));
            publishApi.setVoiceTimeLen(endTime-startTime);
        } else {
            publishApi.setContent(text);
        }
        if (mSelectGame != null) {
            publishApi.setAppId(mSelectGame.id + "");
        }
        ZhidianHttpClient.request(publishApi, new JsonHttpListener(getActivity(), "正在发表动态") {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                UIUtil.toast(getActivity(), "发表动态成功");
                voicePath = null;
                loadData();
                buttomView.cleanEdite();
                mSelectGame=null ;
                mSelectImage=null ;
                imagePath=null ;
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                UIUtil.toast(getActivity(), "发表动态失败");
            }
        });
    }

    @Override
    public void send(String content, PublishDyView.SendModel model) {
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo == null) {
            CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_LOGIN, -1);
        } else {
            if (model == PublishDyView.SendModel.TEXT) {
                if (content.length() > 0) {
                    publishDynamic(content, model);
                } else {
                    UIUtil.toast(getActivity(), "发表内容不能为空");
                }
            } else {
                if (voicePath != null) {
                    publishDynamic(content, model);
                } else {
                    UIUtil.toast(getActivity(), "发表语音不能为空");
                }
            }
        }
    }

    @Override
    public void cleanImage() {
        mSelectGame = null;
        mSelectImage = null;
        imagePath = null;
    }

    @Override
    public void cleanGame() {
        mSelectGame = null;
    }

    long startTime;
    long endTime;
    final RecoderVoice recoderVoice = new RecoderVoice();

    @Override
    public void startRecoding() {
        recodingView.setVisibility(View.VISIBLE);
        startTime = System.currentTimeMillis();
        new Thread() {
            @Override
            public void run() {
                recoderVoice.start(getActivity());
            }
        }.start();
    }

    @Override
    public void endRecoding() {
        recodingView.setVisibility(View.GONE);
        endTime = System.currentTimeMillis();
        voicePath = recoderVoice.stop();
        buttomView.setVoiceLength(((endTime-startTime)/1000));
    }

    @Override
    public void preFace(boolean show) {

    }

    @Override
    public void selectGame() {
        SelectGameFragment selectGameFragment = new SelectGameFragment(onGameSelect);
        ((BaseActivity) getActivity()).addFragment(selectGameFragment, true);
    }

    @Override
    public void onCleanImage() {
        mSelectImage = null ;
    }

    @Override
    public void onCleanGame() {
        mSelectGame = null ;
    }

    private SelectGameFragment.OnGameSelect onGameSelect = new SelectGameFragment.OnGameSelect() {
        @Override
        public void onGameSelect(DynamicSelectGameBean bean) {
            mSelectGame = bean;
            getActivity().onBackPressed();
            buttomView.updateSelect(mSelectImage, mSelectGame);
        }
    };

    @Override
    public void onComentClick(DymaicListBean b,PublishDyView.SendModel model) {
        DynamicCommentActivity.iCommentListener=this ;
        targetDynamic = b;
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo != null) {
            if(buttomView!=null) {
                buttomView.cleanEdite();
            }
            Intent intent = new Intent(getActivity(), DynamicCommentActivity.class);
            intent.putExtra(DynamicCommentActivity.ID, b.getId());
            if(model== PublishDyView.SendModel.TEXT) {
                intent.putExtra(DynamicCommentActivity.MODEL, 1);
            }else {
                intent.putExtra(DynamicCommentActivity.MODEL, 2);
            }
            startActivityForResult(intent, RESULT_ADD_COMMENT);
        } else {
            CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_LOGIN, -1);
        }
    }

    @Override
    public void onRepyClick(DymaicListBean dymaicListBean, DymaicCommentsBean c) {
        targetDynamic = dymaicListBean;
        DynamicCommentActivity.iCommentListener = this ;
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo != null) {
            if(buttomView!=null) {
                buttomView.cleanEdite();
            }
            if (c.getIsOwn()) {
                DyCommentDeletActivity.startDeleteCommentActivity(getActivity(), c.getId());
            } else if (c.isCanReply()) {
                Intent intent = new Intent(getActivity(), DynamicCommentActivity.class);
                intent.putExtra(DynamicCommentActivity.CID, c.getId());
                startActivityForResult(intent, RESULT_ADD_COMMENT);
            }
        } else {
            CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_LOGIN, -1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        recoderVoice.stopVoice();
    }

    @Override
    public void onVoiceClick(String url,RecoderVoice.IPlayListener listener) {
        recoderVoice.setPlayListener(listener);
        DownLoadUtil.getInstance(new DownLoadUtil.IDownLoadListener() {
            @Override
            public void onAdd(String url, Boolean isInterrupt,GameBean gameBean) {

            }

            @Override
            public void onLoading(String url, long totalSize, long currentSize, long speed) {

            }

            @Override
            public void onSuccess(String url, File file) {
                recoderVoice.playMusic(file);
            }

            @Override
            public void onFailure(String url, String strMsg) {

            }

            @Override
            public void onContinue(String url) {

            }

            @Override
            public void onPause(String url) {

            }

            @Override
            public void onDelete(String url) {

            }
        }).downLoadVoice(url);
    }

    @Override
    public void onCommentSuccess(String json) {
        try {
            DymaicCommentsBean bean = jsonToBean(DymaicCommentsBean.class, json);
            if (targetDynamic.getComments() != null) {
                targetDynamic.getComments().add(bean);
            } else {
                List<DymaicCommentsBean> list = new ArrayList<DymaicCommentsBean>();
                list.add(bean);
                targetDynamic.setComments(list);
            }
            if(bean.getType()==1){//文字
                targetDynamic.settAmount(targetDynamic.gettAmount()+1);
            }else {//语音
                 targetDynamic.setvAmount(targetDynamic.getvAmount()+1);
            }
            homPageDymaicListAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DynamicCommentActivity.iCommentListener=null ;
    }
}
