package com.dmsys.dropbox.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.Session.AccessType;

  
    /**
     * @author zhang 
     * @param op
     * @param list
     * p层的接口，直接调用model，通过接口回调回来，再把数据通过handler 传递到View层
     * 接口的回调包括进度，回调由一个返回值，去决定model 层数据获取过程的持续还是中断。
     * （模拟ImageLoder 图片加载取消的机制）
     */

public class DMDropboxAPI extends DropboxAPI  {
	final static private String APP_KEY = "easkalluuwilx42";
	final static private String APP_SECRET = "cd3hve7vxk7zgt6";

	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;

	final static private String ACCOUNT_PREFS_NAME = "dropbox_prefs";
	final static private String ACCESS_KEY_NAME = "oauth2AccessToken";

	private static DMDropboxAPI instance;

	public static Context mApplication;
	private Handler mHandler ;
	


	private DMDropboxAPI(Session session) {
		super(session);
	}

	
	public synchronized static DMDropboxAPI getInstance() {
		if(instance == null) {
			instance = new DMDropboxAPI(buildSession());
			
		}
		return instance;
	}
	
	private static String getAccessToken() {
		SharedPreferences prefs = mApplication.getSharedPreferences(
				ACCOUNT_PREFS_NAME, 0);
		return prefs.getString(ACCESS_KEY_NAME, null);
	}

	private  static AndroidAuthSession buildSession() {
		AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session;

		String accessToken = getAccessToken();
		if (accessToken != null) {
			session = new AndroidAuthSession(appKeyPair, accessToken);
		} else {
			session = new AndroidAuthSession(appKeyPair);
		}
		return session;
	}


    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a
     * local store, rather than storing user name & password, and
     * re-authenticating each time (which is not to be done, ever).
     */
    public void storeAccessToken(String key) {
        // Save the access key for later
        SharedPreferences prefs = mApplication.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.commit();
    }
    private void clearKeys() {
        SharedPreferences prefs = mApplication.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
    private boolean checkAppKeySetup() {
        // Check to make sure that we have a valid app key
        if (APP_KEY.startsWith("CHANGE") || APP_SECRET.startsWith("CHANGE")) {
            return false;
        }

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = mApplication.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            return false;
        }
        return true;
    }
    
    

    
   


}
