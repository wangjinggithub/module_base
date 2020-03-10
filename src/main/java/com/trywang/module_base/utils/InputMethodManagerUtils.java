package com.trywang.module_base.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/10/9 16:27
 */
public class InputMethodManagerUtils {

    public static void showSoftInput(Context context, View view) {
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.showSoftInput(view, InputMethod.SHOW_FORCED);
    }

    public static void hideSoftInput(Context context, View view) {
        if (context == null || view == null || view.getWindowToken() == null) return;
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideSoftInput(Activity context) {
        if (context == null || context.getWindow() == null || context.getWindow().getCurrentFocus() == null) {
            return;
        }
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(context.getWindow().getCurrentFocus().getWindowToken(), 0);
    }
}
