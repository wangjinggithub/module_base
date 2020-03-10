package com.trywang.module_base.base;

import android.app.Application;
import android.content.Context;


/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/9/25 10:14
 */
public class BaseApplication extends Application {
    public static Application sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
