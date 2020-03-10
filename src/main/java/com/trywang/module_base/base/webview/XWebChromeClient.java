package com.trywang.module_base.base.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.trywang.module_base.utils.Logger;

import java.lang.ref.WeakReference;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/10/8 17:50
 */
public class XWebChromeClient extends WebChromeClient {

    private WeakReference<ProgressBar> mProgressBar;
    private WeakReference<Activity> mActivity;
    private WeakReference<WebView> mWebView;

    public XWebChromeClient() {
    }


    public XWebChromeClient(ProgressBar progressBar, WebView webView) {
        mProgressBar = new WeakReference<>(progressBar);
        mWebView = new WeakReference<>(webView);
        Context context = mWebView.get().getContext();
        if (context instanceof Activity)
            mActivity = new WeakReference<>(((Activity)context));
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
        result.cancel();
        return true;
    }

    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
        AlertDialog dialog = new AlertDialog.Builder(view.getContext()).create();
        dialog.setMessage(message);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定" ,(d, which)->{
            dialog.dismiss();
            result.confirm();
        });
        return true;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        showProgress(newProgress);
        Logger.d("webview","newProgress = " + newProgress + ";view = " + view.getTitle());
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        Logger.d("webview","title = " + title + ";view = " + view.getTitle());
        if (mActivity != null && mActivity.get() != null && title != null && !title.startsWith("http")) {
            mActivity.get().setTitle(title);
        }
    }

    private void showProgress(int progress) {
        if (mProgressBar != null && mProgressBar.get() != null) {
            mProgressBar.get().setProgress(progress);
        }
    }

    public void destroy() {
        mProgressBar.clear();
        mProgressBar = null;
        mWebView.clear();
        mWebView = null;
        mActivity.clear();
        mActivity = null;
    }
}
