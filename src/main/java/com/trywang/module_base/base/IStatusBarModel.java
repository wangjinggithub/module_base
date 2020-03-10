package com.trywang.module_base.base;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2018/10/24 16:49
 */
public interface IStatusBarModel {
    public static final String STATUS_BAR_MODE_FULLSCREEN = "FLAG_FULLSCREEN";//全屏覆盖，没有状态栏
    public static final String STATUS_BAR_MODE_FULLSCREEN_WITH_BAR = "FLAG_FULLSCREEN_WITH_BAR";//有透明状态栏，状态栏会覆盖在布局上方。
    public static final String STATUS_BAR_MODE_NOMAL = "FLAG_NORMAL";//普通模式，可以修改状态栏颜色 只有此标志状态栏颜色才有效
    public static final String STATUS_BAR_MODE_NOMAL2 = "FLAG_NORMAL2";//普通模式，可以修改状态栏颜色，使用的是资源修改 只有此标志状态栏设置资源才有效

    /**
     * 获取状态栏的模式
     * @return 状态栏的模式
     */
    String getStatusBarMode() ;

    /**
     * 获取状态栏背景颜色
     * @return 状态栏背景颜色 默认是{@link #getColorForStateBarDefault()}
     */
    int getColorForStateBar() ;

    /**
     * 获取状态栏背景默认的颜色
     * @return 状态栏背景默认的颜色
     */
    int getColorForStateBarDefault();

    /**
     * 设置状态栏颜色
     *
     * @return true为黑色
     */
    boolean isDarkForStateBarText();
}
