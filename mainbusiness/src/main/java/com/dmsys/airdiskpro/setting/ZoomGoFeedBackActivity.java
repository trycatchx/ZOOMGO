package com.dmsys.airdiskpro.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;


/**
 * Created by jiong103 on 2017/7/10.
 */
@SuppressLint("SetJavaScriptEnabled")
public class ZoomGoFeedBackActivity  extends Activity implements View.OnClickListener {
   public final String URL = "http://www.zoomgo.tv/support/";
    WebView wv_feedback;
    LinearLayout llyt_feedback_root ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoomgo_feedback);
        initViews();
    }

    @SuppressWarnings("deprecation")
    private void initViews() {

        ((TextView) findViewById(R.id.titlebar_title))
                .setText(R.string.DM_Set_Feedback);
        llyt_feedback_root  = ((LinearLayout) findViewById(R.id.llyt_feedback_root));

        FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
        ImageView titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
        titlebar_left
                .setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
        back.setOnClickListener(this);

        wv_feedback = (WebView) findViewById(R.id.wv_feedback);

        wv_feedback.requestFocus();
        wv_feedback.getSettings().setLoadWithOverviewMode(true);
        wv_feedback.getSettings().setAllowFileAccess(true);
        wv_feedback.getSettings().setUseWideViewPort(true);
        wv_feedback.getSettings().setBuiltInZoomControls(true);
        wv_feedback.getSettings().setSupportZoom(true);
        wv_feedback.getSettings().setDisplayZoomControls(false);
        wv_feedback.getSettings().setJavaScriptEnabled(true);
        wv_feedback.getSettings().setAppCachePath(getCacheDir().getAbsolutePath());
        wv_feedback.getSettings().setAppCacheEnabled(true);
        wv_feedback.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        wv_feedback.loadUrl(URL);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && wv_feedback.canGoBack()) {

            wv_feedback.goBack();
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int i = v.getId();
        if (i == R.id.layout_back) {
            if (wv_feedback.canGoBack()) {
                wv_feedback.goBack();
            } else {
                finish();
            }

        } else {
        }
    }


    @Override
    protected void onDestroy() {
        llyt_feedback_root.removeView(wv_feedback);
        wv_feedback.removeAllViews();
        wv_feedback.destroy();
        super.onDestroy();
    }
}


