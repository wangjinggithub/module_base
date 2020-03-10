package com.trywang.module_base.base.webview;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.trywang.module_base.R;
import com.trywang.module_base.R2;
import com.trywang.module_base.base.BaseActivity;
import com.trywang.module_widget.titlebar.XTitleBar;

import butterknife.BindView;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/10/8 17:09
 */
public class BaseWebActivity extends BaseActivity {
    public static final String EXTRA_URL = "url_webview";
    public static final String EXTRA_HTML = "html_webview";
    protected String mUrl;
    protected String mHtml;
    protected String mTitle;


    private boolean isRefresh;

    @Nullable
    @BindView(R2.id.tb)
    protected XTitleBar mTitleBar;

    protected WebViewBaseFragment mWebViewFragment;

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public int getColorForStateBarDefault() {
        return ContextCompat.getColor(this, R.color.color_state_bar);
    }

    @Override
    public boolean isDarkForStateBarText() {
        return true;
    }

    @Override
    protected int getContextView() {
        return R.layout.activity_web_default;
    }

    @Override
    protected void initView(@Nullable Bundle savedInstanceState) {
    }

    @Override
    protected void initDataSub(@Nullable Bundle savedInstanceState) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, onCreateFragment())
                .commit();

        String title = getIntent().getStringExtra(Intent.EXTRA_TITLE);
        if (!TextUtils.isEmpty(title)) {
            setTitle(title);
            mTitle = title;
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        // 不固定标题才设置
        if (TextUtils.isEmpty(mTitle)) {
            mTitleBar.setTextCentent(title.toString());
        }
    }

    protected Fragment onCreateFragment() {
        mUrl = getIntent().getStringExtra(EXTRA_URL);

        //加载url
        if (getIntent().getData() != null) {
            mUrl = getIntent().getData().toString();
        }

        // 网页
        mHtml = getIntent().getStringExtra(EXTRA_HTML);

        return mWebViewFragment = createWebViewFragment(mUrl,mHtml);
    }

    @Override
    public void onBackPressed() {
        if (mWebViewFragment.getWebView().canGoBack()) {
            mWebViewFragment.getWebView().goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (isRefresh) {
//            isRefresh = false;
//            mWebViewFragment.getWebView().reload();
            //更新h5余额
//            if (mWebViewFragment != null && mWebViewFragment.getWebView() != null) {
//                mWebViewFragment.getWebView().loadUrl("javascript:window.BB_JS.updateAccount()");
//            }
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        isRefresh = true;
    }

    protected WebViewBaseFragment createWebViewFragment(String url, String html){
        WebViewBaseFragment frag;
        if (TextUtils.isEmpty(url) && !TextUtils.isEmpty(html)) {
            // 来自于表单
            frag = createWebViewFragmentByHtml(html);
        } else {
            frag = createWebViewFragmentByUrl(url);
        }
        return frag;
    }

    protected WebViewBaseFragment createWebViewFragmentByUrl(String url){
        return com.trywang.module_base.base.webview.WebViewBaseFragment.newInstance(url);
    }
    protected WebViewBaseFragment createWebViewFragmentByHtml(String html){
        return com.trywang.module_base.base.webview.WebViewBaseFragment.newInstanceByHtml(html);
    }
}
