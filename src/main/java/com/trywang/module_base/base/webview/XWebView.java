package com.trywang.module_base.base.webview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.webkit.WebView;

import java.lang.reflect.Field;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/10/8 18:00
 */
public class XWebView extends WebView {
    private static Field sConfigCallback;
    private boolean isAllowIntercepterKeyDown = true;

    public boolean isAllowIntercepterKeyDown() {
        return isAllowIntercepterKeyDown;
    }

    public void setAllowIntercepterKeyDown(boolean allowIntercepterKeyDown) {
        isAllowIntercepterKeyDown = allowIntercepterKeyDown;
    }

    static {
        try {

            sConfigCallback = Class.forName("android.webkit.BrowserFrame")
                    .getDeclaredField("sConfigCallback");
            sConfigCallback.setAccessible(true);
        } catch (Exception e) {
            // ignored
        }

    }

    public XWebView(Context context) {
        super(context);
    }

    public XWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public XWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public XWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public XWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isAllowIntercepterKeyDown && canGoBack() && keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void destroy() {
        clearHistory();
        setTag(null);
        super.destroy();
        try {
            if (sConfigCallback != null)
                sConfigCallback.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
