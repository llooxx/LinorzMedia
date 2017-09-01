package com.linorz.linorzmedia.main.activity;

/**
 * Created by linorz on 2017/8/29.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.linorz.linorzmedia.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebActivity extends SwipeBackAppCompatActivity {

    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private WebSettings mWebSettings;
    private String search_content;
    @BindView(R.id.web_webview)
    WebView mWebView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.web_swipe)
    SwipeRefreshLayout mSwipe;
    @BindView(R.id.web_progress)
    CircleProgressBar mWebProgress;
    @BindView(R.id.web_appbar)
    AppBarLayout mWebAppBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        search_content = intent.getStringExtra("search_content");
        mToolbar.setTitle(search_content);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //缓存
        mSharedPreferences = getSharedPreferences("LinorzMedia", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();

        initView();
        setWebSettings();

        switch (mSharedPreferences.getInt("search", 0)) {
            case 0:
                //百度
                mWebView.loadUrl("https://m.baidu.com" + (search_content == null ? "" : "/s?word=" + search_content));//https://m.baidu.com/s?word=2333+666
                break;
            case 1:
                //必应
                mWebView.loadUrl("http://www.bing.com" + (search_content == null ? "" : "/search?q=" + search_content));
                break;
        }

        //设置不用系统浏览器打开,直接显示在当前Webview
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mWebAppBar.setExpanded(true, true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        //设置WebChromeClient类
        mWebView.setWebChromeClient(new WebChromeClient() {

            //获取网站标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (search_content == null)
                    mToolbar.setTitle(title);
            }


            //获取加载进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                String progress = newProgress + "";
                mWebProgress.setProgressTextFormatPattern(progress);
                mWebProgress.setProgress(newProgress);
            }
        });
    }

    private void initView() {
        //刷新
        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();  //刷新
                mSwipe.setRefreshing(false);
            }
        });
        //设置样式刷新显示的位置
        mSwipe.setProgressViewOffset(true, -20, 200);
        mSwipe.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        mWebView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                if (mWebView.getScrollY() == 0) {
                    mSwipe.setEnabled(true);
                } else {
                    mSwipe.setEnabled(false);
                }
            }
        });
        mWebAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset >= 0) mSwipe.setEnabled(true);
                else mSwipe.setEnabled(false);

            }
        });
        //处理点击
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //进度条
        mWebProgress.setMax(100);
    }

    private void setWebSettings() {
        mWebSettings = mWebView.getSettings();
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
    }

    //点击返回上一页面而不是退出浏览器
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    //销毁Webview
    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();

            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }
}
