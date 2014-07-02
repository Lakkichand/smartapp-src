package com.youle.gamebox.ui.http;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.BaseFragment;
import com.youle.gamebox.ui.util.LOGUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 14-4-22.
 */
public class JsonHttpListener extends AsyncHttpResponseHandler implements DialogInterface.OnCancelListener {
    private final String TAG = JsonHttpListener.class.getSimpleName();
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private AsyncHttpClient asyncHttpClient;
    private IUIWhithNetListener listener;

    public JsonHttpListener(Context context) {
        this.mContext = context;
        if (mContext != null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setMessage("正在加载更多数据");
            mProgressDialog.setOnCancelListener(this);
        }
    }

    public JsonHttpListener(boolean isNextPage) {
    }

    public JsonHttpListener(IUIWhithNetListener listener) {
        this.listener = listener;
    }

    public void setAsyncHttpClient(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    @Override
    public final void onSuccess(String content) {
        super.onSuccess(content);
        dismissProgress();
        LOGUtil.e(TAG, content);
        if (dispatchJson(content)) {
            if (listener != null) {
                listener.onSuccess(content);
            }
            onRequestSuccess(content);
        } else {
            onResultFail(content);
        }
    }

    @Override
    public void onFailure(Throwable error) {
        super.onFailure(error);
        if (listener != null) {
            listener.onFailure(error);
        }
        dismissProgress();
        if(mContext!=null) {
            Toast.makeText(mContext, R.string.net_error, Toast.LENGTH_SHORT).show();
        }
        error.printStackTrace();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (listener != null) {
            listener.onLoadStart();
        }
        if (mProgressDialog != null) {
            mProgressDialog.show();
        }
    }

    private void dismissProgress() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (asyncHttpClient != null) {
            asyncHttpClient.cancelRequests(mContext, true);
        }
    }

    public void onRequestSuccess(String jsonString) {

    }

    public void onResultFail(String jsonString) {
        LOGUtil.e("onResultFail",jsonString);
        if(mContext!=null) {
            Toast.makeText(mContext, "服务器返回数据错误：" + jsonString, Toast.LENGTH_SHORT).show();
        }
    }


    private boolean dispatchJson(String json) {
        if (json == null) {
            return false;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            if ("1000".equals(jsonObject.optString("code"))) {
                return true;
            } else {
                if (mContext != null) {
                    Toast.makeText(mContext, jsonObject.optString("errorMsg"), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
