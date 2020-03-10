package com.trywang.module_base.base;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.trywang.module_base.R;
import com.trywang.module_base.utils.StatusBarUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 基础Activity
 *
 * @author Try
 * @date 2018/9/25 11:32
 */
public abstract class BaseActivity extends AppCompatActivity implements com.trywang.module_base.base.IStatusBarModel {
    public static final String KEY_LAST_PAGE_DATA_JSON_PARAMS = "key_last_page_json_params";
    public static float mNoCompatDensity;
    public static float mNoCompatScaleDensity;
    //上个页面的参数
    protected JSONObject mLastPageData;
    public int mScreenWidth;
    public int mScreenHeight;
    protected FragmentManager mFragmentManager;
    protected Unbinder mUnBinder;

    private long mLastClickTime = 0;
    private long mBlockThreshold = 450;
    PointF mDownPoint = new PointF(-1, -1);

    /**
     * 获取参数
     *
     * @param lastJson 上个页面
     * @return bundle
     */
    public static Bundle getArgumentsBundle(String lastJson) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_LAST_PAGE_DATA_JSON_PARAMS, lastJson);
        return bundle;
    }

    /**
     * 设置状态栏
     *
     * @param statusBar 状态栏model
     */
    public void setStatusBar(com.trywang.module_base.base.IStatusBarModel statusBar) {
        if (statusBar == null) {
            statusBar = this;
        }
        setStatusBarBg(statusBar);
        StatusBarUtils.setStatusBarLightMode(getWindow(), statusBar.isDarkForStateBarText());
    }

    /**
     * 宽高适配是大小。比如设计稿为320 * 720 ，以宽作为标准则设置此值为320f。此值跟{@link #isCompatWidth()}配合使用
     *
     * @return 适配尺寸
     */
    protected float getCompatSize() {
        return 375f;
    }

    protected boolean isCompatWidth() {
        return true;
    }

    protected boolean isNeedToCompat(){
        return true;
    }

    protected abstract int getContextView();

    /**
     * 初始化视图
     */
    protected abstract void initView(@Nullable Bundle savedInstanceState);

    /**
     * 初始化数据
     */
    protected abstract void initDataSub(@Nullable Bundle savedInstanceState);

    /**
     * 是否需要阻止快速点击
     *
     * @return
     */
    protected boolean isNeedBlockFaskClick() {
        return true;
    }

    protected boolean hasTitle() {
        return false;
    }

    @Override
    public String getStatusBarMode() {
        return STATUS_BAR_MODE_NOMAL;
    }

    @Override
    public int getColorForStateBar() {
        return getColorForStateBarDefault();
    }

    @Override
    public int getColorForStateBarDefault() {
        return ContextCompat.getColor(this, R.color.colorPrimary);
    }

    /**
     * 设置状态栏颜色
     *
     * @return true为黑色
     */
    @Override
    public boolean isDarkForStateBarText() {
        return true;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isNeedToCompat()) {
            setCustomDensity(this, BaseApplication.sInstance);
        }

//        if (!hasTitle()) {//此方法只有在继承 Activity时有效，继承了AppCompatActivity则使用getSupportActionBar().hide();
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
//        }
        setContentView(getContextView());
        if (!hasTitle() && getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        init(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        mUnBinder = ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        if (mUnBinder != null) {
            mUnBinder.unbind();
            mUnBinder = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null || fragments.size() <= 0) return;
        for (Fragment fragment : fragments) {
            // 通知里面的Fragment
            if (fragment != null && !fragment.isDetached() && fragment.isAdded()) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = mFragmentManager.getFragments();
        for (Fragment fragment : fragments) {
            // 通知里面的Fragment
            if (fragment != null && !fragment.isDetached() && fragment.isAdded() && fragment.getUserVisibleHint()) {
                boolean handled = fragment instanceof IBackListener
                        && ((IBackListener) fragment).onHandleBack(this.getClass().getSimpleName());
                if (!handled) {
//                    mFragmentManager.popBackStackImmediate();
//                    super.onBackPressed();
                    break;
                }
                return;
            }
        }
        super.onBackPressed();
    }

    private void init(Bundle savedInstanceState) {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        mFragmentManager = getSupportFragmentManager();

        initView(savedInstanceState);
        initIntent(getIntent());
        initData(savedInstanceState);
    }

    private void initIntent(Intent intent) {
        if (intent == null) {
            intent = new Intent();
        }

        try {
            String lastData = intent.getStringExtra(KEY_LAST_PAGE_DATA_JSON_PARAMS);
            if (!TextUtils.isEmpty(lastData)) {
                mLastPageData = new JSONObject(lastData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mLastPageData == null) {
                mLastPageData = new JSONObject();
            }
        }
    }

    private void initData(Bundle savedInstanceState) {
        setStatusBar(this);
        initDataSub(savedInstanceState);
    }

    private void setStatusBarBg(com.trywang.module_base.base.IStatusBarModel statusBar) {
        String model = statusBar.getStatusBarMode();
        if (STATUS_BAR_MODE_FULLSCREEN_WITH_BAR.equalsIgnoreCase(model)) {
            StatusBarUtils.setFullScreenWithStatusBar(this);
        } else if (STATUS_BAR_MODE_NOMAL.equalsIgnoreCase(model)) {
            StatusBarUtils.setColorForStatusBarBg(this, statusBar.getColorForStateBar());
        } else if (STATUS_BAR_MODE_NOMAL2.equalsIgnoreCase(model)) {
            try {
                StatusBarUtils.setColorForStatusBarBgWithDrawable(this, statusBar.getColorForStateBar());
            } catch (Resources.NotFoundException e) {
                Log.e("BaseActivity", "BaseActivity#setStatusBarBg: not found resources !");
                StatusBarUtils.setColorForStatusBarBg(this, statusBar.getColorForStateBarDefault());
            }
        } else {
            StatusBarUtils.setFullScreen(this);
        }
    }

    /**
     * 原始android计算px的方式
     * 1.px = density * dp
     * 2.density = dpi / 160
     * 3.dpi = pow(w^2 * h^2) / size(屏幕尺寸)
     * <p>
     * 因为dip是由手机硬件本身决定的，这就造成了计算的px也是根据手机硬件变化，但不尽人意，
     * 因为同样的尺寸的手机可能w=320，有的w=400等等这样就造成了最终计算的px不同。
     * <p>
     * 解决方式：从根本的px计算方式上着手 px = density * dp  ；只要计算出新的density即可。
     * 1.屏幕适配要么按照宽同比缩小放大，要么按照h同比缩小放大。
     * 2.以下按照宽适配（h同理），假如设计稿按照320 * 720  设计，有一个图片的宽是10dp，
     * 在640 * 1440的手机：因为宽变成了640，所以图片的宽度应该按照比例放大2倍设置为20dp才能完美呈现
     * <p>
     * 但是因为dp设置的值是不变的，所以修改density；
     * 320 * 720  ===》   px320 = density320 * 10dp = 10 = w320; ①
     * 640 * 1440 ===>    px640 = density640 * 10dp = 20 = w640; ②
     * 式①    density320     w320    1
     * ———— = ———————————— = ————— = ——
     * 式②    density640     w640    2
     * <p>
     * 所以density640 = 2 * density320 = w640 * density320 / w320
     * 按高兼容同理可得 = densityNew = h1440 * density720 / h720
     * 所以修改系统用到的density即可，分别在acitivity和application里面的DisplayMetrics。
     *
     * @param activity    activity
     * @param application application
     * @notice 此种方式获取状态栏高度也将进行换算后的px算，但是渲染的时候却并没有按照适配的高度
     */
    private void setCustomDensity(Activity activity, final Application application) {
        final DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        if (mNoCompatScaleDensity == 0) {
            mNoCompatDensity = appDisplayMetrics.density;
            mNoCompatScaleDensity = appDisplayMetrics.scaledDensity;
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    //"全局配置改变" 比如手机设置里面修改字体大小
                    if (newConfig != null && newConfig.fontScale > 0) {
                        mNoCompatScaleDensity = appDisplayMetrics.scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {
                }
            });
        }

//        float densityNew = ((float)appDisplayMetrics.widthPixels) / 360f;
        float densityNew = getDensityNew(appDisplayMetrics);
        int densityDpiNew = (int) (densityNew * 160);
        float scaleDensityNew = densityNew / mNoCompatDensity * mNoCompatScaleDensity;
        appDisplayMetrics.density = densityNew;
        appDisplayMetrics.densityDpi = densityDpiNew;
        appDisplayMetrics.scaledDensity = scaleDensityNew;

        DisplayMetrics activityDisplayMetrics = activity.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = densityNew;
        activityDisplayMetrics.densityDpi = densityDpiNew;
        activityDisplayMetrics.scaledDensity = scaleDensityNew;
    }

    private float getDensityNew(DisplayMetrics dm) {
        float dmPixels = isCompatWidth() ? dm.widthPixels : dm.heightPixels;
        return dmPixels / getCompatSize();
    }


    /**
     * 拦截点击事件，防止快速点击
     *
     * @param ev ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = false;
        if (isNeedBlockFaskClick()) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                long current = System.currentTimeMillis();
                long timespace = current - mLastClickTime;
                mDownPoint.set(ev.getX(), ev.getY());
                //如果需要防快速点击，在阀值内则拦截。
                if (timespace >= 0 && timespace <= mBlockThreshold) {
                    handled = true;
                } else {
                    //视为有效点击。
                    mLastClickTime = current;
                }
            } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = ev.getX() - mDownPoint.x;
                float dy = ev.getY() - mDownPoint.y;

                if (Math.sqrt(dx * dx + dy * dy) > ViewConfiguration.get(this).getScaledTouchSlop()) {
                    mLastClickTime = 0;
                }
            }
        }
        return handled || super.dispatchTouchEvent(ev);
    }


}
