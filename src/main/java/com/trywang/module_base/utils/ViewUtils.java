package com.trywang.module_base.utils;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;

/**
 * 视图的工具类
 *
 * @author Try
 * @date 2017/11/4 17:39
 */
public class ViewUtils {

    public static int dp2sp(Context context, float dpVal) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics()));
    }

    public static int sp2dp(Context context, float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 测量View的宽高
     *
     * @param view
     * @return int[0]:view width; int[1] view height
     */
    public static int[] measureView(View view) {
        if (view == null) {
            return null;
        }

        int[] widthAndHeight = new int[2];
        int width = View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        widthAndHeight[0] = view.getMeasuredWidth();
        widthAndHeight[1] = view.getMeasuredHeight();

        return widthAndHeight;
    }

    public static int getStateHeight(Activity activity) {
        int statusBarHeight = -1;
        //获取status_bar_height资源的ID
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }


}
