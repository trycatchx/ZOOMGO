package com.dmsys.airdiskpro.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dmsys.mainbusiness.R;

@SuppressLint("SetJavaScriptEnabled")
public class HelpCenterActivity extends Activity implements OnClickListener {

    WebView wv_help_center;
    LinearLayout llyt_help_center;
    public static final String FAQ_URL = "http://app.dmsys.com/pairdisk/faq-cn/view_main.html";
    public static final String FAQ_URL_LOCAL = "file:///android_asset/faq-zoomgo-en/view_main.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help_center);
        initViews();
    }


    @SuppressWarnings("deprecation")
    private void initViews() {

        ((TextView) findViewById(R.id.titlebar_title))
                .setText(R.string.DM_Sidebar_HelpCenter);
        FrameLayout back = (FrameLayout) findViewById(R.id.layout_back);
        llyt_help_center = (LinearLayout) findViewById(R.id.llyt_help_center);
        ImageView titlebar_left = (ImageView) findViewById(R.id.titlebar_left);
        titlebar_left
                .setImageResource(R.drawable.dm_lib_wifi_back_btn_bg_selector);
        back.setOnClickListener(this);

        wv_help_center = (WebView) findViewById(R.id.wv_help_center);

        wv_help_center.requestFocus();
        wv_help_center.getSettings().setLoadWithOverviewMode(true);
        wv_help_center.getSettings().setAllowFileAccess(true);
        wv_help_center.getSettings().setUseWideViewPort(true);
        wv_help_center.getSettings().setBuiltInZoomControls(true);
        wv_help_center.getSettings().setSupportZoom(true);
        wv_help_center.getSettings().setDisplayZoomControls(false);
        wv_help_center.getSettings().setJavaScriptEnabled(true);
        wv_help_center.getSettings().setAppCachePath(getCacheDir().getAbsolutePath());
        wv_help_center.getSettings().setAppCacheEnabled(true);
        wv_help_center.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        wv_help_center.getSettings().setDefaultTextEncodingName("utf-8");
        wv_help_center.setWebViewClient(mWebViewClient);
        checkInternet();
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    };

    private void checkInternet() {

        // 开始进行异步请求
//		new CommonAsync(new CommonAsync.Runnable() {
//			@Override
//			public Object run() {
//				// TODO Auto-generated method stub
//				NetWorkUtil mNetWorkUtil = new NetWorkUtil();
//				List<String> list = new ArrayList() {
//					{
//						add("www.baidu.com");
//						add("app.dmsys.com");
//						// some other add() code......
//					}
//				};
//				return mNetWorkUtil.manyPing(list);
//			}
//		}, new CommonAsyncListener() {
//
//			@Override
//			public void onResult(Object ret) {
//				// TODO Auto-generated method stub
////				if ((boolean) ret) {
////					wv_help_center.loadUrl(FAQ_URL);
////				} else {
////					wv_help_center.loadUrl(FAQ_URL_LOCAL);
////				}
//
////				wv_help_center.loadUrl("about:blank");
//				wv_help_center.loadUrl(FAQ_URL_LOCAL);
//			}
//
//		}).executeOnExecutor((ExecutorService) Executors.newCachedThreadPool());


        wv_help_center.loadUrl(FAQ_URL_LOCAL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && wv_help_center.canGoBack()) {

            wv_help_center.goBack();
            return true;

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        llyt_help_center.removeView(wv_help_center);
        wv_help_center.removeAllViews();
        wv_help_center.destroy();
    }



    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        int i = v.getId();
        if (i == R.id.layout_back) {
            if (wv_help_center.canGoBack()) {
                wv_help_center.goBack();
            } else {
                finish();
            }

        } else {
        }
    }

}
