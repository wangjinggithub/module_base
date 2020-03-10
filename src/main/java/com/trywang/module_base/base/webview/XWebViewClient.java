package com.trywang.module_base.base.webview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.trywang.module_base.utils.Logger;

import java.lang.ref.WeakReference;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/10/8 17:25
 */
public class XWebViewClient extends WebViewClient {
    private WeakReference<ProgressBar> mProgressBar;

    private Context mContext;
    private Activity mActivity;
    private WeakReference<WebView> mWebView;

    public XWebViewClient(@Nullable ProgressBar progressBar, WebView webView) {
        mProgressBar = new WeakReference<>(progressBar);
        mWebView = new WeakReference<>(webView);
        mContext = mWebView.get().getContext();
        if (mContext instanceof Activity)
            mActivity = (Activity) mContext;
    }

    private void dismissProgress() {
        Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
        if (mProgressBar.get() != null) {
            mProgressBar.get().startAnimation(animation);
            mProgressBar.get().setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (mProgressBar != null && mProgressBar.get() != null) {
            mProgressBar.get().setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        Log.i("webview", "webview 拦截 shouldOverrideUrlLoading: " + url);
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//        Logger.i("webview", "old api webview 拦截 shouldInterceptRequest: " + url);
        return super.shouldInterceptRequest(view, url);
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            Log.i("webview", "new api webview 拦截 shouldInterceptRequest: " + request.getUrl());
        }
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
        return super.onRenderProcessGone(view, detail);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed(); // 忽略证书错误
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        showEmpty();
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        showEmpty();
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        showEmpty();
    }

    private void showEmpty() {
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        dismissProgress();
        String title = view.getTitle();
        if (mActivity != null && title != null && !title.startsWith("http")
        && !"about:blank".equals(title)) {
            mActivity.setTitle(title);
        }
        Logger.d("webview","onPageFinished view = " + view.getTitle() + ";url = " + url);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        Logger.d("webview","onLoadResource view = " + view.getTitle() + ";url = " + url);
    }

    public void destroy() {
        mProgressBar.clear();
        mProgressBar = null;
        mContext = null;
        mActivity = null;
    }
}
