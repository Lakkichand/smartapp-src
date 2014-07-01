package fq.router2.adservice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.smartapp.easyvpn.R;

import fq.router2.MainActivity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class ADService extends IntentService {

	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String PKGNAME = "pkgname";

	private Handler mHandler = new Handler();

	public ADService() {
		super("");
	}

	public ADService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
//		JSONArray array = CloudUtil.getCloudAdInfo();
//		JSONArray _array = new JSONArray();
//		// 过滤掉已安装的应用
//		if (array != null) {
//			for (int i = 0; i < array.length(); i++) {
//				try {
//					JSONObject obj = array.getJSONObject(i);
//					String pkgname = obj.getString(PKGNAME);
//					if (!CloudUtil.checkApkExist(ADService.this, pkgname)) {
//						_array.put(obj);
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		if (_array != null && _array.length() > 0) {
//			int index = (int) (System.currentTimeMillis() % _array.length());
//			try {
//				JSONObject json = _array.getJSONObject(index);
//				int type = json.getInt(TYPE);
//				String name = json.getString(NAME);
//				displayADNotifycation(type, name);
//			} catch (JSONException e) {
//				e.printStackTrace();
//				displayADNotifycation();
//			}
//		} else {
//			displayADNotifycation();
//		}
	}

	private void displayADNotifycation(final int type, final String name) {
//		mHandler.post(new Runnable() {
//
//			@Override
//			public void run() {
//				String msg = getString(R.string.admsg1)
//						+ (type == 1 ? getString(R.string.adapp)
//								: getString(R.string.adgame))
//						+ name
//						+ getString(R.string.admsg2)
//						+ (type == 1 ? getString(R.string.adapptry)
//								: getString(R.string.adgametry));
//				NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//				// nm.cancel(R.string.app_name);
//				Notification n = new Notification(R.drawable.ic_launcher, msg,
//						System.currentTimeMillis());
//				n.defaults |= Notification.DEFAULT_SOUND;
//				n.flags = Notification.FLAG_AUTO_CANCEL;
//				Intent i = new Intent(ADService.this, MainActivity.class);
//				i.putExtra(MainActivity.SHOW_OFFER_KEY, 1);
//				// PendingIntent
//				PendingIntent contentIntent = PendingIntent.getActivity(
//						ADService.this, R.string.app_name, i,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				n.setLatestEventInfo(ADService.this,
//						getString(R.string.adnotifytitle), msg, contentIntent);
//				nm.notify(R.string.app_name, n);
//			}
//		});
	}

	private void displayADNotifycation() {
//		mHandler.post(new Runnable() {
//
//			@Override
//			public void run() {
//				NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//				Notification n = new Notification(R.drawable.ic_launcher,
//						getString(R.string.notip), System.currentTimeMillis());
//				n.defaults |= Notification.DEFAULT_SOUND;
//				n.flags = Notification.FLAG_AUTO_CANCEL;
//				Intent i = new Intent(ADService.this, MainActivity.class);
//				i.putExtra(MainActivity.SHOW_OFFER_KEY, 1);
//				// PendingIntent
//				PendingIntent contentIntent = PendingIntent.getActivity(
//						ADService.this, R.string.app_name, i,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				n.setLatestEventInfo(ADService.this,
//						getString(R.string.adnotifytitle),
//						getString(R.string.notip), contentIntent);
//				nm.notify(R.string.app_name, n);
//			}
//		});
	}
}
