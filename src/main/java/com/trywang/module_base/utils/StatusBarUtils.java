package com.trywang.module_base.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.trywang.module_base.base.BaseActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 状态栏工具
 *
 * @author Try
 * @date 2018/10/23 16:59
 */
public class StatusBarUtils {
    /**
     * 全屏显示 不显示状态栏
     *
     * @param activity activity
     */
    public static void setFullScreen(Activity activity) {
        if (activity == null) {
            return;
        }
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 设置带状态栏的全屏模式--内容可伸展到状态栏
     * 4.4及以上有效
     *
     * @param activity activity
     */
    public static void setFullScreenWithStatusBar(Activity activity) {

//        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = activity.getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT)).getChildAt(0);
//            rootView.setFitsSystemWindows(false);
//            rootView.setClipToPadding(true);
////            rootView.setPadding(0,getStatusBarHeight2(activity),0,0);
//        }else
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //将根rootView直接顶上去，和状态栏的顶部对齐。
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT)).getChildAt(0);
//            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            rootView.setFitsSystemWindows(false);
            rootView.setClipToPadding(true);
        }
    }

    /**
     * 设置状态栏背景颜色
     * 4.4及以上有效
     * TODO try 在同activity不同fragment全屏模式下切换到此模式不成功
     *
     * @param activity       activity
     * @param statusBarColor 状态栏背景颜色
     */
    public static void setColorForStatusBarBg(Activity activity, int statusBarColor) {
        if (activity == null) {
            return;
        }
        int statusColor = statusBarColor;
        //5.1及以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //使用setStatusBarColor的前提条件 取消FLAG_TRANSLUCENT_STATUS
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(statusColor);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4以上，5.0及以下
            //原理：只要在decorView去设置一个与状态栏等高的View，设置背景色为我们期望的颜色就可以
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加状态栏背景view
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View view = decorView.findViewWithTag("tag_status_bar_custom");
            if (view == null) {
                view = new View(activity);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight2(activity)));
                view.setTag("tag_status_bar_custom");
                decorView.addView(view);
            }
            view.setBackgroundColor(statusColor);
            //设置activity的view留出状态栏的高度
            ViewGroup activityView = activity.findViewById(android.R.id.content);
            activityView.getChildAt(0).setFitsSystemWindows(true);
        }
    }

    /**
     * 设置状态栏背景颜色
     * 4.4及以上有效 原生系统以及小米5x已经验证有效 魅族未验证
     *
     * @param activity          activity
     * @param statusBarResource 状态栏背景资源
     */
    public static void setColorForStatusBarBgWithDrawable(Activity activity, int statusBarResource) {
        if (activity == null) {
            return;
        }
        //5.1及以上
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//            //使用setStatusBarColor的前提条件 取消FLAG_TRANSLUCENT_STATUS
//            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            通过反射获取到的mStatusColorViewState 里面的view为null。暂时没有找到原因
//            setBackgroundResource(activity,"mStatusColorViewState",statusBarResource);
//        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
//            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
//                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
//            setBackgroundResource(activity,"mStatusColorView",statusBarResource);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //4.4以上，5.0及以下
//            //原理：只要在decorView去设置一个与状态栏等高的View，设置背景色为我们期望的颜色就可以
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //添加状态栏背景view
//            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
//            View view = new View(activity);
//            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity)));
//            view.setBackgroundResource(statusBarResource);
//            decorView.addView(view);
//            //设置activity的view留出状态栏的高度
//            ViewGroup activityView = activity.findViewById(android.R.id.content);
//            activityView.getChildAt(0).setFitsSystemWindows(true);
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //原理：只要在decorView去设置一个与状态栏等高的View，设置背景色为我们期望的颜色就可以
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //添加状态栏背景view
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            View view = decorView.findViewWithTag("tag_status_bar_custom");
            if (view == null) {
                view = new View(activity);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getStatusBarHeight2(activity)));
                view.setTag("tag_status_bar_custom");
                decorView.addView(view);
            }
            view.setBackgroundResource(statusBarResource);
            //设置activity的view留出状态栏的高度
            ViewGroup activityView = activity.findViewById(android.R.id.content);
            activityView.getChildAt(0).setFitsSystemWindows(true);
        }
    }

    private static void setBackgroundResource(Activity activity, String viewFieldName, int statusBarResource) {
        try {
            //获取跟view
            View decorView = activity.getWindow().getDecorView();
            Class c = decorView.getClass();
            Field field = c.getDeclaredField(viewFieldName);
            field.setAccessible(true);
            //获取ColorViewState对象
            Object value = field.get(decorView);
            c = value.getClass();
            field = c.getDeclaredField("view");
            field.setAccessible(true);
            //获取ColorViewState对象里面的View对象
            value = field.get(value);
            if (value instanceof View) {
                View stateBarView = (View) value;
                stateBarView.setBackgroundResource(statusBarResource);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏状态栏（不常用）
     * 更多信息 https://developer.android.com/training/system-ui/immersive?hl=zh-cn#nonsticky
     *
     * @param window window
     */
    public static void hideStatusBar(Window window) {
        if (window == null) {
            return;
        }
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    /**
     * 显示状态栏 （不常用）
     * 更多信息 https://developer.android.com/training/system-ui/immersive?hl=zh-cn#nonsticky
     *
     * @param window window
     */
    private static void showStatusBar(Window window) {
        if (window == null) {
            return;
        }
        // Shows the system bars by removing all the flags
        // except for the ones that make the content appear under the system bars.
        View decorView = window.getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * 设置状态栏字体和图标的颜色
     *
     * @param isDark 是否是黑色
     */
    public static boolean setStatusBarLightMode(Window window, boolean isDark) {
        if (window == null) {
            return false;
        }

        switch (DeviceUtils.getSystem()) {
            case DeviceUtils.SYS_FLYME:
//                return setStatusBarLightModeForFlyme(window, isDark);
                StatusbarColorFlymeUtils.setStatusBarDarkIcon(window, isDark);
                return true;
//                return setStatusBarLightModeForFlyme(window, isDark);
            case DeviceUtils.SYS_MIUI:
                return setStatusBarLightModeForMIUI(window, isDark);
            default:
                return setStatusBarLightModeForNormal(window, isDark);
        }
    }

    /**
     * 调用系统api 设置状态栏字体颜色
     *
     * @param window window
     * @param isDark 是否是黑色
     * @return 成功 true
     */
    public static boolean setStatusBarLightModeForNormal(Window window, boolean isDark) {
        if (window == null) {
            return false;
        }
        View decorView = window.getDecorView();
        int mode = decorView.getSystemUiVisibility();
        if (isDark) {
            mode |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            mode &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decorView.setSystemUiVisibility(mode);
        return true;
    }

    /**
     * 设置状态栏字体图标为深色，需要MIUIV6以上
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean setStatusBarLightModeForMIUI(Window window, boolean dark) {
        boolean result = false;
        if (window == null) {
            return false;
        }

        Class clazz = window.getClass();
        try {

            //开发版 7.7.13之前的使用老方法
            int darkModeFlag = 0;
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
            } else {
                extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
            }
            result = true;

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                result = setStatusBarLightModeForNormal(window, dark);
            }

        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean setStatusBarLightModeForFlyme(Window window, boolean dark) {
        if (window == null) {
            return false;
        }
        boolean result = false;
        try {
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class
                    .getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
            result = true;
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 高度 px
     */
    public static int getStatusBarHeight2(Context context) {
        float densityOld, scaleDensityOld;
        int densityDpiOld;
        float densityNew = BaseActivity.mNoCompatDensity;
        int densityDpiNew = (int) (densityNew * 160);
        float scaleDensityNew = BaseActivity.mNoCompatScaleDensity;

        DisplayMetrics activityDisplayMetrics = context.getResources().getDisplayMetrics();

        densityOld = activityDisplayMetrics.density;
        densityDpiOld = (int) (densityOld * 160);
        scaleDensityOld = activityDisplayMetrics.scaledDensity;

        activityDisplayMetrics.density = densityNew;
        activityDisplayMetrics.densityDpi = densityDpiNew;
        activityDisplayMetrics.scaledDensity = scaleDensityNew;

        int result = getStatusBarHeight(context);
        if (result <= 0) {
            result = ViewUtils.dip2px(context, 20);
        }

        activityDisplayMetrics.density = densityOld;
        activityDisplayMetrics.densityDpi = densityDpiOld;
        activityDisplayMetrics.scaledDensity = scaleDensityOld;
        return result;
    }

    /**
     * 获取状态栏高度
     *
     * @param context context
     * @return 高度 px
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        if (context == null) {
            return result;
        }
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
