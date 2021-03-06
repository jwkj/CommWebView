package com.jwkj;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;


/**
 * 通用webview
 * Created by HDL on 2017/6/23.
 */

public class CommWebView extends LinearLayout {
    /**
     * 是否可以返回上一个页面，默认可以返回上一个页面
     */
    private boolean isCanBack = true;
    /**
     * 当前网页的标题
     */
    private String webTitle = "";
    /**
     * 当前url
     */
    private String curWebUrl = "";
    /**
     * 回调器
     */
    private WebViewCallback callback;
    /**
     * 采用addview(webview)的方式添加到线性布局，可以及时销毁webview
     */
    private WebView webview;
    /**
     * 加载失败时模式显示的页面
     */
    private static final String KEY_DEFAULT_ERROR_URL = "file:///android_asset/web_error.html";

    public CommWebView(Context context) {
        this(context, null);
    }

    public CommWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initConfig(context);
    }

    /**
     * 初始化参数配置
     *
     * @param context
     */
    private void initConfig(Context context) {
        webview = new WebView(context.getApplicationContext());
        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);//设置是否支持与js互相调用
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不使用网络缓存，开启的话容易导致app膨胀导致卡顿
        webview.setWebViewClient(new WebViewClient() {//设置webviewclient,使其不会由第三方浏览器打开新的url
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                loadWebUrl(url);
                return true;//设置为true才有效哦
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (callback != null) {
                    callback.onStart();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webTitle = view.getTitle();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                loadWebUrl(KEY_DEFAULT_ERROR_URL);
                if (callback != null) {
                    callback.onError(errorCode, description, failingUrl);
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();//接受证书
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {//监听加载的过程

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                curWebUrl = view.getUrl();
                webTitle = view.getTitle();
                if (callback != null) {
                    callback.onProgress(newProgress);
                }
            }
        });
        setVisibility(View.VISIBLE);
        requestFocus();//请求获取焦点，防止view不能打开输入法问题
        requestFocusFromTouch();//请求获取焦点，防止view不能打开输入法问题
        setOrientation(LinearLayout.VERTICAL);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webview.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {//3.0以上暂时关闭硬件加速
            webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        /**
         *  Webview在安卓5.0之前默认允许其加载混合网络协议内容
         *  在安卓5.0之后，默认不允许加载http与https混合内容，需要设置webview允许其加载混合网络协议内容
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webview.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        }
        addView(webview);
    }

    /**
     * 销毁当前的页面
     */
    public void onDestroy() {
        curWebUrl = "";
        removeAllViews();
        webview.stopLoading();
        webview.removeAllViews();
        webview.destroy();
        webview = null;
    }

    /**
     * 开始回调
     *
     * @param callback
     */
    public CommWebView startCallback(WebViewCallback callback) {
        this.callback = callback;
        loadWebUrl(curWebUrl);
        return this;
    }

    /**
     * 判断是否可以返回上一个页面
     *
     * @return
     */
    public boolean isCanBack() {
        return isCanBack;
    }

    /**
     * 设置是否可以返回上一个页面
     *
     * @param canBack
     */
    public CommWebView setCanBack(boolean canBack) {
        isCanBack = canBack;
        return this;
    }


    public String getCurWebUrl() {
        return curWebUrl;
    }

    /**
     * 设置当前需要加载的url
     *
     * @param curWebUrl
     */
    public CommWebView setCurWebUrl(String curWebUrl) {
        this.curWebUrl = curWebUrl;
        return this;
    }

    public String getWebTitle() {
        return webTitle;
    }

    /**
     * 加载网页
     *
     * @param url
     */
    private CommWebView loadWebUrl(String url) {
        curWebUrl = url;//记录当前的url
        webview.loadUrl(curWebUrl);//webview加载url
        return this;
    }

    /**
     * 判断是否可以返回上一个页面
     *
     * @return
     */
    public boolean canGoBack() {
        return webview.canGoBack();
    }

    /**
     * 返回到上一个页面
     */
    public void goBack() {
        webview.goBack();
    }

    /**
     * 添加js与java互相调用类.
     * <p>
     * SuppressLint("JavascriptInterface") 表示webview的修复漏洞
     *
     * @param mapClazz js方法与java方法映射类
     * @param objName  对象的名字
     */
    @SuppressLint("JavascriptInterface")
    public CommWebView addJavascriptInterface(Object mapClazz, String objName) {
        webview.addJavascriptInterface(mapClazz, objName);
        return this;
    }

    /**
     * 刷新
     */
    public void refresh() {
        loadWebUrl(curWebUrl);
    }
}
