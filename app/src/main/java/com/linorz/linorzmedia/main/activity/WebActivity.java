package com.linorz.linorzmedia.main.activity;

/**
 * Created by linorz on 2017/8/29.
 */

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebActivity extends SwipeBackAppCompatActivity {
    WebView mWebview;
    WebSettings mWebSettings;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWebview = new WebView(this);
        addContentView(mWebview, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mWebSettings = mWebview.getSettings();
        mWebSettings.setUserAgentString(mWebSettings.getUserAgentString() + "app/linorzmedia");//添加UA,  “app/XXX”：是与h5商量好的标识，h5确认UA为app/XXX就认为该请求的终端为App
        mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        mWebSettings.setUseWideViewPort(true);//设置webview推荐使用的窗口
        mWebSettings.setLoadWithOverviewMode(true);//设置webview加载的页面的模式
        mWebSettings.setDisplayZoomControls(false);//隐藏webview缩放按钮
        mWebSettings.setJavaScriptEnabled(true); // 设置支持javascript脚本
        mWebSettings.setAllowFileAccess(true); // 允许访问文件
        mWebSettings.setBuiltInZoomControls(true); // 设置显示缩放按钮
        mWebSettings.setSupportZoom(true); // 支持缩放
        mWebSettings.setAppCacheEnabled(true);// 设置缓存

        Intent intent = getIntent();
        String search_content = intent.getStringExtra("search_content");
//        //百度
//        if (search_content == null)
//            mWebview.loadUrl("https://m.baidu.com");
//        else
//            mWebview.loadUrl("https://m.baidu.com/s?word=" + search_content);//https://m.baidu.com/s?word=2333+666
        //必应
        if (search_content == null)
            mWebview.loadUrl("https://cn.bing.com");
        else
            mWebview.loadUrl("https://cn.bing.com/search?q=" + search_content);


        //设置不用系统浏览器打开,直接显示在当前Webview
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        //设置WebChromeClient类
        mWebview.setWebChromeClient(new WebChromeClient() {

            //获取网站标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
                System.out.println("标题在这里");
            }


            //获取加载进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                String progress = newProgress + "%";
            }
        });
        mWebview.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int x, int y, int oldx, int oldy) {
                if (y > oldy) {
                    System.out.println("向下");
                } else {
                    System.out.println("向上");
                }
            }
        });
    }

    //点击返回上一页面而不是退出浏览器
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()) {
            mWebview.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    //销毁Webview
    @Override
    protected void onDestroy() {
        if (mWebview != null) {
            mWebview.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebview.clearHistory();

            ((ViewGroup) mWebview.getParent()).removeView(mWebview);
            mWebview.destroy();
            mWebview = null;
        }
        super.onDestroy();
    }
}
