package com.trywang.module_base.base;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.trywang.module_base.R;
import com.trywang.module_base.utils.Logger;


/**
 * 基础的fragment
 *
 * @author Try
 * @date 2018/5/3 13:46
 */
public abstract class AbsBaseFragment extends BaseFragment implements IBackListener, IStatusBarModel {
    protected com.trywang.module_base.base.BaseActivity mActivity;
    protected Resources mResources;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mActivity = (com.trywang.module_base.base.BaseActivity) getActivity();
        mResources = getResources();
        super.onActivityCreated(savedInstanceState);
        init(savedInstanceState);
        Logger.i("view", getLogTag() + ";onActivityCreated");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Logger.i("view", getLogTag() + ";setUserVisibleHint = " + isVisibleToUser + "；isAdded() = " + isAdded() +
                "；getActivity() != null :" + (getActivity() != null) + ";!isDetached() = " + !isDetached());
        setStatusBar();
    }

    @Override
    public void onLazyLoad() {
//        init();
        Logger.i("view","onLazyLoad = " + getLogTag());
    }

    protected String getLogTag() {
        return getClass().getSimpleName();
    }

    @Override
    public int getColorForStateBarDefault() {
        if (isAdded() && getActivity() != null) {
            return ContextCompat.getColor(getActivity(), R.color.color_state_bar);
        } else {
            return Color.parseColor("#FFFFFFFF");
        }
    }

    @Override
    public int getColorForStateBar() {
        return getColorForStateBarDefault();
    }

    @Override
    public String getStatusBarMode() {
        return IStatusBarModel.STATUS_BAR_MODE_NOMAL;
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


    /**
     * 显示或隐藏
     *
     * @param isShow 显示
     */
    protected void showOrHide(boolean isShow) {
        FragmentManager fm = getFragmentManager();
        try {
            if (isShow) {
                fm.beginTransaction().show(this).commitNow();
            } else {
                fm.beginTransaction().hide(this).commitNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void init(@Nullable Bundle savedInstanceState) {
        initView(savedInstanceState);
        initData(savedInstanceState);
    }

    private void initData(@Nullable Bundle savedInstanceState) {
        setStatusBar();
        initSubData(savedInstanceState);
    }

    protected boolean isEnableForSetSatusBar(){
        return true;
    }

    private void setStatusBar() {
        if (isEnableForSetSatusBar() && isVisibleToUser && isAdded() && getActivity() != null && !isDetached() && !getActivity().isFinishing()) {
            Activity activity = getActivity();
            if (activity instanceof com.trywang.module_base.base.BaseActivity) {
                com.trywang.module_base.base.BaseActivity baseActivity = (com.trywang.module_base.base.BaseActivity) activity;
                baseActivity.setStatusBar(this);
            }
        }
    }

    protected abstract void initView(@Nullable Bundle savedInstanceState);

    protected abstract void initSubData(@Nullable Bundle savedInstanceState);

}
