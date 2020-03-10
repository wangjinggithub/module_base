package com.trywang.module_base.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.trywang.module_base.utils.Logger;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 *
 */
public abstract class BaseFragment extends Fragment implements IFragLifecycle {

    private Unbinder mUnBinder;
    protected View mRootView;

    /**
     * 是否显示给用户
     */
    protected boolean isVisibleToUser;

    /**
     * 是否初始化view
     */
    protected boolean isViewInit;

    /**
     * 是否已经初始化过数据
     */
    protected boolean isInitData;

    protected boolean isResumeUnique;

    /**
     * 是否走setUserVisibleHint方法
     * 一般使用viewpage+fragment会走此方法
     * 普通的使用fragmentmanager.beginTransaction().add()则不走
     */
    protected boolean isWorkSetUserVisibleHint = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(getContextView(), container, false);
        mUnBinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    protected abstract int getContextView();

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 2、在解绑，因为先解绑会导致view找不到
        if (mUnBinder != null) {
            mUnBinder.unbind();
            mUnBinder = null;
        }
    }

    /**
     * 是否处于生命周期中
     */
    protected boolean isInLifecycle() {
        return getActivity() != null && isAdded() && !isDetached() && !isHidden() && isVisible() && !isRemoving();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.isViewInit = true;
        prepareInit();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isWorkSetUserVisibleHint = true;
        this.isVisibleToUser = isVisibleToUser;
        prepareInit();
        onResumePrepare();
        onPausePrepare(false);
    }

    /**
     * 准备初始化
     */
    public void prepareInit() {
        if ((!isWorkSetUserVisibleHint || isVisibleToUser) && isViewInit && !isInitData) {
            onLazyLoad();
            isInitData = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onResumePrepare();
    }

    @Override
    public void onPause() {
        super.onPause();
        onPausePrepare(true);
    }

    /**
     * 懒加载数据
     */
    public void onLazyLoad() {
        Logger.i("view","onLazyLoad" + getLogTag() );
    }

    protected String getLogTag(){
        return getClass().getSimpleName();
    }

    @Override
    public boolean isResumeUnique(){
        return isResumeUnique;
    }

    public void onResumePrepare(){
        if (isWorkSetUserVisibleHint) {
            if (isInLifecycle() && isVisibleToUser) {
                onResumePrepareInner();
            }
        } else {
            onResumePrepareInner();
        }
    }

    public void onPausePrepare(boolean isPauseCalled){
        if (isWorkSetUserVisibleHint) {
            if (getActivity() != null && isAdded() && isViewInit && ((isPauseCalled && isVisibleToUser) || (!isPauseCalled && !isVisibleToUser))) {
                onPausePrepareInner();
            }
        } else {
            onPausePrepareInner();
        }
    }

    public void onResumePrepareInner(){
        Fragment f = getParentFragment();
        if (f == null) {
            onResumeUnique();
        } else {
            try {
                IFragLifecycle fragLifecycle = (IFragLifecycle) f;
                if (fragLifecycle.isResumeUnique()) {
                    onResumeUnique();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onPausePrepareInner(){
        Fragment f = getParentFragment();
        if (f == null) {
            onPauseUnique();
        } else {
            try {
                IFragLifecycle fragLifecycle = (IFragLifecycle) f;
                if (!fragLifecycle.isResumeUnique()) {
                    onPauseUnique();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void onResumeUnique(){
        isResumeUnique = true;
        Logger.i("view","唯一 onResumeUnique" + getLogTag() );
    }

    public void onPauseUnique(){
        isResumeUnique = false;
        Logger.i("view","唯一  onPauseUnique" + getLogTag() );
    }

}
