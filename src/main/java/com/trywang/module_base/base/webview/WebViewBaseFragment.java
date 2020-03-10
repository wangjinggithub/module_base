package com.trywang.module_base.base.webview;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trywang.module_base.BuildConfig;
import com.trywang.module_base.R;
import com.trywang.module_base.base.AbsBaseFragment;
import com.trywang.module_base.utils.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/10/8 17:24
 */
public class WebViewBaseFragment extends AbsBaseFragment {
    /**
     * form表单数据
     */
    protected String mHtmlText;
    protected String mUrl;
    protected String mRawUrl;
    protected String mReferer;
    protected XWebViewClient mWebViewClient;
    protected XWebChromeClient mWebChromeClient;
    protected WebView mWebView;
    protected ViewGroup mContentLayout;
    @Nullable
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected ProgressBar mProgressBar;

    public static WebViewBaseFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString("url", url);
        WebViewBaseFragment fragment = new WebViewBaseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static WebViewBaseFragment newInstanceByHtml(String html) {
        Bundle args = new Bundle();
        args.putString("html", html);
        WebViewBaseFragment fragment = new WebViewBaseFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static WebViewBaseFragment newInstance(String url, String referer) {
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putString("referer", referer);
        WebViewBaseFragment fragment = new WebViewBaseFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        preLoadUrl();
    }

    @Override
    protected String getLogTag() {
        return "游戏web页面";
    }

    @SuppressLint("JavascriptInterface")
    @Override
    protected void initView(@Nullable Bundle savedInstanceState) {
        mContentLayout = mRootView.findViewById(R.id.content);
        mProgressBar = mRootView.findViewById(R.id.pb_web_view);
        mSwipeRefreshLayout = mRootView.findViewById(R.id.refresh_layout);
        mWebView = getWebView();
        mWebView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mContentLayout.addView(mWebView);

        if (mSwipeRefreshLayout != null) {

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    mWebView.reload();
                }
            });
            mSwipeRefreshLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
                @Override
                public boolean canChildScrollUp(@NonNull SwipeRefreshLayout parent, @Nullable View child) {
                    return mWebView.canScrollVertically(-1);
                }
            });
        }

        WebSettings settings = mWebView.getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        // 设置User-Agent
        String userAgent = settings.getUserAgentString() + " baibei-mall/" + getVersionName();
        settings.setUserAgentString(userAgent);

        Object iDefault = createDefaultInterface(mWebView);
        if (iDefault != null) {
            mWebView.addJavascriptInterface(iDefault, "goal");
        }

        File cacheDir = getContext() == null ? null : getContext().getExternalCacheDir();

        if (cacheDir != null && cacheDir.canRead() && cacheDir.canWrite()) {
            settings.setAppCacheEnabled(true);
            settings.setAppCachePath(cacheDir.getPath());
        }

        mWebViewClient = getWebViewClient();
        mWebChromeClient = getWebChromeClient();
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }
    }

    protected Object createDefaultInterface(WebView webView) {
        return null;
    }

    @Override
    protected void initSubData(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            mRawUrl = getArguments().getString("url");
            mReferer = getArguments().getString("referer");
            mHtmlText = getArguments().getString("html");
            mUrl = mRawUrl;
            Logger.d("mUrl = " + mUrl);
        }
    }

    public void loadData(String url , String html){
        this.mRawUrl = url;
        this.mHtmlText = html;
        preLoadUrl();
    }
    public void preLoadUrl() {
        //加载url
        if (mWebView != null && !TextUtils.isEmpty(mRawUrl)) {
            loadUrl(mRawUrl);
            return;
        }
        //加载form
        if (mWebView != null && !TextUtils.isEmpty(mHtmlText)) {
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setEnabled(false);
            mWebView.loadDataWithBaseURL(null, getHtmlData(mHtmlText), "text/html", "UTF-8", null);
        }
    }

    /**
     * 富文本适配
     */
    private String getHtmlData(String bodyHTML) {
        String head = "<head>"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, user-scalable=no\"> "
                + "<style>img{max-width: 100%; width:auto; height:auto;}</style>"
                + "</head>";
        return "<html>" + head + "<body>" + bodyHTML + "</body></html>";
    }

    @Override
    protected int getContextView() {
        return R.layout.fm_webview;
    }

    @Override
    public void onDestroy() {
        if (mContentLayout != null) {
            mContentLayout.removeAllViews();
        }
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        }
        if (mWebViewClient != null) {
            mWebViewClient.destroy();
        }
        if (mWebChromeClient != null) {
            mWebChromeClient.destroy();
        }
        super.onDestroy();
    }


    public String getUrl() {
        return mWebView.getUrl();
    }

    /**
     * 是否显示加载进度条
     *
     * @return 默认显示
     */
    protected boolean enableProgressBar() {
        return true;
    }

    public XWebChromeClient getWebChromeClient() {
        return new XWebChromeClient(enableProgressBar() ? mProgressBar : null,mWebView);
    }

    public XWebViewClient getWebViewClient() {
        return new XWebViewClient(enableProgressBar() ? mProgressBar : null, mWebView) {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Logger.i("webview", "!TextUtils.isEmpty(url) && url.startsWith(\"http\") = " + (!TextUtils.isEmpty(url) && url.startsWith("http")));
                if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                    return super.shouldOverrideUrlLoading(view, url);
                }
                return true;
            }
        };
    }

    public WebView getWebView() {
        if (mWebView == null) {
            mWebView = new com.trywang.module_base.base.webview.XWebView(getActivity());
        }
        return mWebView;
    }

    public void loadUrl(String url) {
//        url = replaceHttp(url);
        if (!TextUtils.isEmpty(mReferer)) {
            Map<String, String> map = new HashMap<>();
            map.put("Referer", mReferer);
            mWebView.loadUrl(url, map);
        } else {
            mWebView.loadUrl(url);
        }
    }

//    /**
//     * 调用JS方法
//     */
//    public void invokeJavascript(String method) {
//        if (mWebView == null || method == null) return;
//        mWebView.loadUrl("javascript: " + method);
//    }

    public String getVersionName() {
        try {
            if (getContext() == null) return "1.0.0";
            return getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), PackageManager.GET_META_DATA).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1.0.0";
    }

    public String replaceHttp(String url) {
        if (android.text.TextUtils.isEmpty(url)) {
            return url;
        }

        if (url.startsWith("http") && !url.startsWith("https")) {
            url = url.replaceFirst("http", "https");
        }
        return url;
    }

    @Override
    public boolean onHandleBack(String from) {
        return false;
    }
}
