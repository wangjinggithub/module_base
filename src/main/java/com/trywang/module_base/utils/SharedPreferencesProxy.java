/*
 * Copyright (c) 2017.
 */

package com.trywang.module_base.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.trywang.module_base.base.BaseApplication;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 偏好管理，先定义好偏好的接口，属性方法定义用get、set，清除用clear，清除所有用clearAll()，然后调用{@link #create(Class)} 动态创建实例。示例：<br>
 * <pre><code>
 * public interface ITestConfig {
 *      void setName(String name);
 *      String getName();
 *      void clearName();
 *      void clearAll();
 * }
 * SharedPreferencesProxy preferences = new SharedPreferencesProxy(appContext);
 * ITestConfig config = preferences.create(ITestConfig.class);
 * config.setName("我是测试");
 * </pre></code>
 * Created by Try on 2018/4/15 0015 1:13.
 */
public final class SharedPreferencesProxy {

    private static volatile SharedPreferencesProxy sInstance;

    public static SharedPreferencesProxy getInstance(){
        if (sInstance == null) {
            synchronized (SharedPreferencesProxy.class) {
                if (sInstance == null) {
                    sInstance = new SharedPreferencesProxy(BaseApplication.sInstance);
                }
            }
        }
        return sInstance;
    }

    private Context mContext;

    private SharedPreferencesProxy(Context context) {
        mContext = context;
    }

    private class ConfigHandler implements InvocationHandler {

        private SharedPreferences mPreferences;

        ConfigHandler(Context context, Class<?> cls) {
            mPreferences = context.getSharedPreferences(cls.getSimpleName(), Context.MODE_PRIVATE);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            String key;

            if (name.startsWith("set")) {
                key = name.replace("set", "");
                Object arg = args[0];
                if (arg instanceof String) {
                    mPreferences.edit().putString(key, arg.toString()).apply();
                } else if (arg instanceof Integer) {
                    mPreferences.edit().putInt(key, (Integer) arg).apply();
                } else if (arg instanceof Float) {
                    mPreferences.edit().putFloat(key, (Float) arg).apply();
                } else if (arg instanceof Long) {
                    mPreferences.edit().putLong(key, (Long) arg).apply();
                } else if (arg instanceof Boolean) {
                    mPreferences.edit().putBoolean(key, (Boolean) arg).apply();
                } else {
                    mPreferences.edit().putString(key, new Gson().toJson(arg)).apply();
                }
            }


            if (name.startsWith("get")) {
                key = name.replace("get", "");
                Class<?> type = method.getReturnType();

                if (type == String.class) {
                    return mPreferences.getString(key, (args == null || args.length <= 0) ? null : args[0].toString());
                } else if (type == Integer.class || type == int.class) {
                    return mPreferences.getInt(key, (args == null || args.length <= 0) ? 0 : Integer.parseInt(args[0].toString()));
                } else if (type == Float.class || type == float.class) {
                    return mPreferences.getFloat(key, (args == null || args.length <= 0) ? 0 : Float.parseFloat(args[0].toString()));
                } else if (type == Long.class || type == long.class) {
                    return mPreferences.getLong(key, (args == null || args.length <= 0) ? 0 : Long.parseLong(args[0].toString()));
                } else if (type == Boolean.class || type == boolean.class) {
                    return mPreferences.getBoolean(key, !(args == null || args.length <= 0) && Boolean.parseBoolean(args[0].toString()));
                } else {
                    return new Gson().fromJson(mPreferences.getString(key, ""), method.getGenericReturnType());
//                    return new Gson().fromJson(mPreferences.getString(key, ""), method.getReturnType());
                }
            }


            if (name.startsWith("clear")) {
                key = name.replace("clear", "");
                mPreferences.edit().remove(key).apply();
            }

            if (TextUtils.equals("clearAll", name)) {
                mPreferences.edit().clear().apply();
            }

            return proxy;
        }
    }

    /**
     * 创建偏好
     *
     * @param cls 类型
     */
    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> cls) {
        return (T) Proxy.newProxyInstance(cls.getClassLoader(), new Class[]{cls}, new ConfigHandler(mContext, cls));
    }


}
