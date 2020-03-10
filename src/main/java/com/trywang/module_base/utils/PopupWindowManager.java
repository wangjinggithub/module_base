package com.trywang.module_base.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.WINDOW_SERVICE;

/**
 * 悬浮框工具类
 *
 * @author Try
 * @date 2018/11/3 09:51
 */
public class PopupWindowManager {
    private static PopupWindowManager sInstance;
    private Context mContext;
    private WindowManager mWindowManager;
    private Map<Integer, PopupWindowItemModel> mViews = new HashMap<>();


    public static void init(Context context) {
        if (sInstance == null) {
            synchronized (PopupWindowManager.class) {
                if (sInstance == null) {
                    sInstance = new PopupWindowManager(context);
                }
            }
        }
    }

    public static PopupWindowManager getInstatnce() throws NullPointerException {
        if (sInstance == null) {
            throw new NullPointerException("请在Application中初始化PopupWindowManager");
        }
        return sInstance;
    }

    private PopupWindowManager(Context context) {
        this.mContext = context.getApplicationContext();
        mWindowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
    }

    public int hidePopupWindow(View view) {
        return hidePopupWindow(view.hashCode());
    }

    public int hidePopupWindow(int key) {
        PopupWindowItemModel model = mViews.get(key);
        if (model != null && model.mView != null) {
            mWindowManager.removeViewImmediate(model.mView.get());
            model.mView = null;
        }
        return key;
    }

    public int removePopupWindow(View view) {
        int key = view.hashCode();
        PopupWindowItemModel model = mViews.remove(key);
        if (model != null && model.mView != null) {
            mWindowManager.removeViewImmediate(model.mView.get());
        }
        return key;
    }

    public int showPopupWindow(View view) {
        return showPopupWindow(view, null);
    }

    public int showPopupWindow(View view, Point point) {
        return showPopupWindow(view, point, true);
    }

    public int showPopupWindow(View view, Point point, boolean allowDrag) {
        int key = view.hashCode();
        if (isShow(key)) {
            return key;
        }

        PopupWindowItemModel model = mViews.get(key);
        final WindowManager.LayoutParams params;

        if (model == null) {
            params = createLayoutParams(point);
            model = new PopupWindowItemModel(params, new WeakReference<View>(view));
            mViews.put(key, model);
        } else {
            params = model.mParams;
//            if (point != null) {
//                params.x = point.x;
//                params.y = point.y;
//            }
            model.mView = new WeakReference<>(view);
        }
        mWindowManager.addView(view, params);
        if (!allowDrag) return key;
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean handered = false;
                PointF[] points = new PointF[2];
                if (v.getTag() != null && v.getTag() instanceof PointF[]) {
                    points = (PointF[]) v.getTag();
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        points[0] = new PointF(event.getRawX(), event.getRawY());
                        points[1] = new PointF(event.getRawX(), event.getRawY());
                        v.setTag(points);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - points[0].x);
                        int dy = (int) (event.getRawY() - points[0].y);
                        DisplayMetrics dm = new DisplayMetrics();
                        mWindowManager.getDefaultDisplay().getMetrics(dm);
                        params.x += dx;
                        params.y += dy;
                        if (params.x < 0) {
                            params.x = 0;
                        } else if (params.x + v.getMeasuredWidth() > dm.widthPixels) {
                            params.x = dm.widthPixels - v.getMeasuredWidth();
                        }

                        if (params.y < 0) {
                            params.y = 0;
                        } else if (params.y + v.getMeasuredHeight() > dm.heightPixels) {
                            params.y = dm.heightPixels - v.getMeasuredHeight();
                        }

                        break;
                    case MotionEvent.ACTION_UP:
                        int touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
                        handered = Math.abs(event.getRawX() - points[1].x) > touchSlop
                                || Math.abs(event.getRawY() - points[1].y) > touchSlop;
                        break;
                }

                points[0].x = event.getRawX();
                points[0].y = event.getRawY();

                mWindowManager.updateViewLayout(v, params);
                return handered;
            }
        });

        return key;
    }

    /**
     * 创建view的参数
     *
     * @param point view初始的点
     * @return WindowManager.LayoutParams
     */
    private WindowManager.LayoutParams createLayoutParams(Point point) {
        if (point == null) {
            point = new Point(0, 0);
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        //设置为系统弹框
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {

        } else
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        //设置为悬浮框不可聚焦（除悬浮框之外的其他地方可操作）
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //左上顶点
        params.gravity = Gravity.TOP | Gravity.LEFT;
        //设置图片格式，效果为背景透明
        params.format = PixelFormat.RGBA_8888;
        //设置位置为左顶点
        params.x = point.x;
        params.y = point.y;

        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        return params;
    }

    static class PopupWindowItemModel {
        WindowManager.LayoutParams mParams;
        WeakReference<View> mView;

        public PopupWindowItemModel(WindowManager.LayoutParams mParams, WeakReference<View> mView) {
            this.mParams = mParams;
            this.mView = mView;
        }
    }


    public boolean isShow(View view) {
        return isShow(view.hashCode());
    }

    public boolean isShow(int key) {
        return isShow(mViews.get(key));
    }

    public boolean isShow(PopupWindowItemModel model) {
        return model != null && model.mView != null;
    }

    public void jumpToSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(mContext)) {
            //检查没有悬浮框的权限则跳转到设置页面
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            mContext.startActivity(i);
        }
    }

}
