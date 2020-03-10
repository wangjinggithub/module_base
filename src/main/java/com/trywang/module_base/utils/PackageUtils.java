package com.trywang.module_base.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;

import com.trywang.module_base.base.BaseApplication;

import java.util.List;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/11/2 17:07
 */
public class PackageUtils {
    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        // Returns a list of application processes that are running on the
        // device
        ActivityManager activityManager = (ActivityManager) context
                .getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = context.getApplicationContext().getPackageName();
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;

    }
    /**
     * 获取版本号名字
     * @return versionName
     */
    public static String getVersionName(){
        String versionName = "";
        try {
            versionName = BaseApplication.sInstance.getPackageManager()
                    .getPackageInfo(BaseApplication.sInstance.getPackageName(), PackageManager.GET_META_DATA)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
    /**
     * 获取版本号
     * @return versionName
     */
    public static int getVersionCode(){
        int versionCode = 0;
        try {
            versionCode = BaseApplication.sInstance.getPackageManager()
                    .getPackageInfo(BaseApplication.sInstance.getPackageName(), PackageManager.GET_META_DATA)
                    .versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }
}
