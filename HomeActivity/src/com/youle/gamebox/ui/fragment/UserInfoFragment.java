package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.api.pcenter.PersonalApi;
import com.youle.gamebox.ui.api.person.AvatarChangeApi;
import com.youle.gamebox.ui.bean.pcenter.PersonalBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.*;
import com.youle.gamebox.ui.view.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2014/5/27.
 */
public class UserInfoFragment extends BaseFragment implements View.OnClickListener, BaseDialogView.DialogOnListener {
    @InjectView(R.id.userinfo_photo)
    EditRoundImageView mUserinfoPhoto;
    @InjectView(R.id.userinfo_photo_modfy)
    TextView mUserinfoPhotoModfy;
    @InjectView(R.id.userinfo_item)
    LinearLayout mUserinfoItem;
    @InjectView(R.id.userName)
    TextView mNameText ;
    @InjectView(R.id.nickName)
    TextView mNickName ;
    @InjectView(R.id.email)
    TextView mEmail ;
    @InjectView(R.id.qq)
    TextView mQQ ;
    @InjectView(R.id.safe)
    TextView mSafe ;
    @InjectView(R.id.nickNameLayout)
    View mNickNameLayout ;
    @InjectView(R.id.emailLayout)
    View emailLayout;
    @InjectView(R.id.qqLayout)
    View qqLayout ;
    @InjectView(R.id.safeLayout)
    View safeLayout;
    private String pictureUrl = null;
    public File tempFile = null;

    public static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    public static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    public static final int PHOTO_REQUEST_CUT = 3;// 结果

    @Override
    protected int getViewId() {
        return R.layout.userinfo_layout;
    }

    @Override
    protected String getModelName() {
        return "个人详情";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if(getActivity()!=null) {
                    loadData();
                }
            }
        });
    }

    protected void loadData() {
        setDefaultTitle("修改个人资料");
        mUserinfoPhoto.setOnClickListener(this);
        final UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo!=null) {
            PersonalApi personalApi = new PersonalApi();
            personalApi.setSid(userInfo.getSid());
            personalApi.setUid(userInfo.getUid());
            ZhidianHttpClient.request(personalApi,new JsonHttpListener(false){
                @Override
                public void onRequestSuccess(String jsonString) {
                    super.onRequestSuccess(jsonString);
                    try {
                        PersonalBean  personalBean = jsonToBean(PersonalBean.class, jsonString, "account");
                        userInfo.setContact(personalBean.getEmail());
                        DaoManager.getDaoSession().getUserInfoDao().insertOrReplace(userInfo);
                        initAddItemView();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }


    private void initAddItemView() {
        final UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo == null) return;
         mNameText.setText(userInfo.getUserName());
         mNickName.setText(userInfo.getNickName());
        mEmail.setText(userInfo.getContact());
        mQQ.setText(userInfo.getQq());
        ImageLoadUtil.displayAvatarImage(userInfo.getBigAvatarUrl(),mUserinfoPhoto);
        mNickNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfoModfyFragment userInfoModfyFragment = new UserInfoModfyFragment(userInfo.getNickName(), "昵称");
                ((BaseActivity) getActivity()).addFragment(userInfoModfyFragment, true);
            }
        });

        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewFragment webViewFragment = new WebViewFragment("安全中心", YouleAplication.SAFE_URL);
                ((BaseActivity) getActivity()).addFragment(webViewFragment, true);
            }
        });

        qqLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfoModfyFragment userInfoModfyFragment = new UserInfoModfyFragment(userInfo.getQq(), "QQ");
                ((BaseActivity) getActivity()).addFragment(userInfoModfyFragment, true);
            }
        });
        safeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebViewFragment webViewFragment = new WebViewFragment("安全中心", YouleAplication.SAFE_URL);
                ((BaseActivity) getActivity()).addFragment(webViewFragment, true);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
       // TOASTUtil.showSHORT(getActivity(), "resultCode==>" + requestCode + ", requestCode==>" + requestCode);
        switch (requestCode) {
            case PHOTO_REQUEST_TAKEPHOTO:// 当选择拍照时调用

                startPhotoZoom(Uri.fromFile(tempFile));
                break;
            case PHOTO_REQUEST_GALLERY:// 当选择从本地获取图片时
                // 做非空判断，当我们觉得不满意想重新剪裁的时候便不会报异常，下同
                if (data != null)

                    startPhotoZoom(data.getData());
                break;
            case PHOTO_REQUEST_CUT:// 返回的结果
                if (data != null)
                    sentPicToNext(data);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    UserInfoModfyPhotoDialog userInfoModfyPhotoDialog = null;


    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }


    @Override
    public void onClick(View v) {
        userInfoModfyPhotoDialog = new UserInfoModfyPhotoDialog(getActivity());
        userInfoModfyPhotoDialog.setDialogOnListener(this);
    }

    @Override
    public void onclick(int click) {
        switch (click) {
            case 0:
                tempFile = new File(Environment.getExternalStorageDirectory(), getPhotoFileName());
                Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                startActivityForResult(cameraintent, PHOTO_REQUEST_TAKEPHOTO);
                break;
            case 1:
                // 调用系统的相册
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                break;
        }

    }

    // 将进行剪裁后的图片传递到下一个界面上
    private void sentPicToNext(Intent picdata) {

        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
            Bitmap photo = bundle.getParcelable("data");
            if (photo == null) {
            } else {
                mUserinfoPhoto.setImageBitmap(photo);
                writeBitmapToFile(photo);
                uploadAvatar();
            }
        }
    }

    private void writeBitmapToFile(Bitmap bitmap) {
        if (tempFile == null) {
            tempFile = new File(Environment.getExternalStorageDirectory(), getPhotoFileName());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(tempFile);
                fos.write(bitmapdata);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void uploadAvatar() {
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo == null) return;
        AvatarChangeApi avatarChangeApi;
        avatarChangeApi = new AvatarChangeApi();
        avatarChangeApi.setSid(userInfo.getSid());
        avatarChangeApi.setAvatar(tempFile);


        ZhidianHttpClient.request(avatarChangeApi, new JsonHttpListener(getActivity(),"正在上传头像") {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    UIUtil.toast(getActivity(),"头像上传成功");
                    JSONObject jsonObject = new JSONObject(jsonString);
                    pictureUrl = jsonObject.getString("data");
                    UserInfoCache userInfoCache = new UserInfoCache();
                    UserInfo userInfo = userInfoCache.getUserInfo();
                    userInfo.setSmallAvatarUrl(pictureUrl);
                    userInfo.setBigAvatarUrl(pictureUrl);
                    userInfoCache.saveUserInfo(userInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                UIUtil.toast(getActivity(),R.string.save_avata_fail);
            }
        });
    }

    // 使用系统当前日期加以调整作为照片的名称
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

}
