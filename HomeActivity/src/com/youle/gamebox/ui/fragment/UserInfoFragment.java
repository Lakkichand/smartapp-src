package com.youle.gamebox.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.view.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2014/5/27.
 */
public class UserInfoFragment extends BaseFragment implements View.OnClickListener, BaseDialogView.DialogOnListener {
    @InjectView(R.id.userinfo_photo)
    RoundImageView mUserinfoPhoto;
    @InjectView(R.id.userinfo_photo_modfy)
    TextView mUserinfoPhotoModfy;
    @InjectView(R.id.userinfo_item)
    LinearLayout mUserinfoItem;
    public  static String IMAGE_UNSPECIFIED = "image/*";
    public static final int PHOTOHRAPH = 1;// 拍照
    public static final int PHOTOZOOM = 2; // 缩放
    public static final int PHOTORESOULT = 3;// 结果
    public static final int PHOTORELSH = 4;// 返回刷新
    public static final int NONE = 0;
    public String photoPath = "file:///sdcard/";
    public String photoName="";


    @Override
    protected int getViewId() {
        return R.layout.userinfo_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    protected void loadData() {
        BaseTitleBarView baseTitleBarView = setTitleView();
        baseTitleBarView.setTitleBarMiddleView(null, "修改个人资料");
        baseTitleBarView.setVisiableRightView(View.GONE);
        mUserinfoPhotoModfy.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        initAddItemView();

    }

    private void initAddItemView(){
        mUserinfoItem.removeAllViews();
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if(userInfo==null)return;
        PCenterUserInfoItemView pCenterUserInfoItemView = new PCenterUserInfoItemView(getActivity());
        pCenterUserInfoItemView.setData(-1,"账号",userInfo.getUserName());
        pCenterUserInfoItemView.setBackgroundColor(getActivity().getResources().getColor(R.color.userinfo_item_bg_color));
        mUserinfoItem.addView(pCenterUserInfoItemView);
        PCenterUserInfoItemView pCenterUserInfoItemView1 = new PCenterUserInfoItemView(getActivity());
        pCenterUserInfoItemView1.setData(-1,"昵称",userInfo.getNickName());
        pCenterUserInfoItemView1.setBackgroundColor(getActivity().getResources().getColor(R.color.userinfo_item_bg_nor));

        mUserinfoItem.addView(pCenterUserInfoItemView1);
        PCenterUserInfoItemView pCenterUserInfoItemView2 = new PCenterUserInfoItemView(getActivity());
        pCenterUserInfoItemView2.setData(-1,"邮箱","===@qq.com");
        pCenterUserInfoItemView2.setBackgroundColor(getActivity().getResources().getColor(R.color.userinfo_item_bg_color));

        mUserinfoItem.addView(pCenterUserInfoItemView2);
        PCenterUserInfoItemView pCenterUserInfoItemView3 = new PCenterUserInfoItemView(getActivity());
        pCenterUserInfoItemView3.setData(-1,"QQ",userInfo.getQq());
        pCenterUserInfoItemView3.setBackgroundColor(getActivity().getResources().getColor(R.color.userinfo_item_bg_nor));

        mUserinfoItem.addView(pCenterUserInfoItemView3);
        PCenterUserInfoItemView pCenterUserInfoItemView4 = new PCenterUserInfoItemView(getActivity());
        pCenterUserInfoItemView4.setData(-1,"安全","修改密码及密保");
        pCenterUserInfoItemView4.setBackgroundColor(getActivity().getResources().getColor(R.color.userinfo_item_bg_color));

        mUserinfoItem.addView(pCenterUserInfoItemView4);




    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(userInfoModfyPhotoDialog == null) return ;
        if (resultCode == NONE)
            return;
        if(data ==null){
            return;
        }
        // 读取相册缩放图片
        if (requestCode == PHOTOZOOM) {
            startPhotoZoom(data.getData());
        }
        // 处理结果
        if (requestCode == PHOTORESOULT) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);// (0 -// 100)压缩文件
                InputStream isBm = new ByteArrayInputStream(
                        stream.toByteArray());
                mUserinfoPhoto.setImageBitmap(photo);
                //Bitmap zoomImage = ZoomBitmap.zoomImage(photo, 90, 90);
                //zoomImage=ZoomBitmap.roundCorners(zoomImage, 5f);
               /* photo_image.setImageBitmap(photo);
                IgNamezoom=getStringToday();
                imageFile=new File(FileSaveUtil.getImagePath(), IgNamezoom+".jpeg");
                FileSaveUtil.storeInSD(photo, IgNamezoom);*/

            }

        }
    }




    UserInfoModfyPhotoDialog userInfoModfyPhotoDialog= null;


    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("return-data", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, PHOTORESOULT);

    }


    @Override
    public void onClick(View v) {
        userInfoModfyPhotoDialog = new UserInfoModfyPhotoDialog(getActivity());
        userInfoModfyPhotoDialog.setDialogOnListener(this);


    }

    @Override
    public void onclick(int click) {
        switch (click){
            case 0:
                    photoName =  getStringToday() + ".jpeg";
                    // 调用系统的拍照功能
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(new File(photoPath+photoName)));
                    startActivityForResult(intent, PHOTOHRAPH);
                break;
            case 1:
                // 调用系统的相册
                Intent intent1 = new Intent(Intent.ACTION_PICK, null);
                intent1.setDataAndType(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        IMAGE_UNSPECIFIED);
                // 调用剪切功能
                startActivityForResult(intent1, PHOTOZOOM);
                break;
        }

    }

    public static String getStringToday() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

}
