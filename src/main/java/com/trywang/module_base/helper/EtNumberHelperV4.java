package com.trywang.module_base.helper;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.trywang.module_base.utils.InputMethodManagerUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 输入框带加减符号的逻辑处理
 *  todo 待优化  数量变化的回调 调用多次
 * @author Try
 * @date 2019-08-22 11:35
 */
public class EtNumberHelperV4 {
    private EditText mEt;
    private View mViewPlus;
    private View mViewSub;
    private IViewOption mViewOption;
    private IOnClickListener mOnClickListener;
    private boolean isLong;
    private boolean isCheck = true;

    private BigDecimal min;
    private BigDecimal max;
    private float increment = 1;

    Map<EditText, TextWatcher> mMapTw = new HashMap<>();
    Map<EditText, List<IFilter>> mMapFilters = new HashMap<>();


    public EtNumberHelperV4() {
    }

    public void addFilter(IFilter filter){
        if (mEt == null) {
            throw new IllegalArgumentException("et is null");
        }
        List<IFilter> filters = mMapFilters.get(mEt);
        if (filters == null) {
            filters = new ArrayList<>();
            mMapFilters.put(mEt,filters);
        }
        filters.add(filter);
    }

    public IViewOption getViewOption() {
        return mViewOption;
    }

    public void setViewOption(IViewOption etView) {
        mViewOption = etView;
    }

    public IOnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public void setOnClickListener(IOnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    /**
     * 手动检测值是否在最大最小值区间内
     */
    public void check() {
        if (mEt == null ) {//|| !mEt.hasFocus()
            return;
        }
        String res = mEt.getText().toString();
        mEt.setText(res);
        mEt.setSelection(TextUtils.isEmpty(mEt.getText().toString()) ? 0 : mEt.getText().toString().length());
        InputMethodManagerUtils.hideSoftInput(mEt.getContext(),mEt);
        if (mEt.getParent() != null && mEt.getParent() instanceof ViewGroup) {
            ((ViewGroup) mEt.getParent()).setFocusableInTouchMode(true);
            mEt.clearFocus();
        }
    }

    /**
     * 1.设置view
     *
     * @param et   et
     * @param plus 加号view 没有可不设置 但是与sub必须设置一项 否则无意义
     * @param sub  减view 没有可不设置 但是与plus必须设置一项 否则无意义
     */
    public void withView(EditText et, View plus, View sub) {
        if (et == null) {
            throw new IllegalArgumentException("EditText is Null,please set EditText");
        }
        TextWatcher tw;
        if (mEt != null && mMapTw.containsKey(mEt) && (tw = mMapTw.get(mEt)) != null) {
            mEt.removeTextChangedListener(tw);
        }
        this.mEt = et;
        this.mViewPlus = plus;
        this.mViewSub = sub;

        tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (filter(s.toString())) {
                    return;
                }

                if (mOnClickListener != null) {
                    mOnClickListener.onEditTextChange(s.toString());
                }
                if (!isCheckMaxEnable()) {
                    //最大值不校验时 则只更新视图 不做逻辑校验
                    updateViewV2(mEt.getText().toString());
                    return;
                }
                //校验最大
                if (isCheck) {
                    checkMax();
                } else {
                    isCheck = true;
                }
            }
        };
        mMapTw.put(mEt,tw);
        //判断最大
        mEt.addTextChangedListener(tw);

        //最小只能在失去焦点时判断，如果放在上面判断则有问题如：min=100，max=200 没法输入100-200的数
        mEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    return;
                }
                if (min.compareTo(new BigDecimal(Double.MIN_VALUE+"")) == 0) {
                    //最小值不校验 则只更新视图 不做逻辑校验
                    updateViewV2(mEt.getText().toString());
                    return;
                }
                //校验最小
                if (isCheck) {
                    checkMin();
                } else {
                    isCheck = true;
                }

            }
        });
        if (mViewSub != null) {
            mViewSub.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sub();
                }
            });
        }
        if (mViewPlus != null) {
            mViewPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    plus();
                }
            });
        }
    }
    public void setLimit(String max, String min, float increment) throws IllegalArgumentException {
        double ma = Double.MIN_VALUE;
        double mi = Double.MIN_VALUE;
        try {
            ma = Double.parseDouble(max);
            mi = Double.parseDouble(min);
        } catch (Exception e) {
        }
        setLimit(ma,mi,increment);
    }
    /**
     * 2.设置限制
     * 在{@link EtNumberHelperV4#withView}
     * {@link EtNumberHelperV4#setViewOption}
     * 之后调用
     *
     * @param max       不校验 则设置为{@link Double#MIN_VALUE}
     * @param min       不校验 则设置为{@link Double#MIN_VALUE}
     * @param increment
     * @throws IllegalArgumentException 参数异常
     */
    public void setLimit(double max, double min, float increment) throws IllegalArgumentException {
        if (increment <= 0) {
            throw new IllegalArgumentException("increment must positive number");
        }
        if (max != Double.MIN_VALUE && max < min) {
            //校验的最大值小于最小值
            max = min;
//            throw new IllegalArgumentException("max < min !!!");
        }

        int iMax = (int) max;
        int iMin = (int) min;
        this.max = new BigDecimal(iMax == max ? iMax+"" : max+"");
        this.min = new BigDecimal(iMin == min ? iMin+"" : min+"");
        this.increment = increment;

        if (mEt == null) {
            throw new IllegalArgumentException("EditText is Null,please set EditText");
        }

        double result;
        try {
            result = Double.parseDouble(mEt.getText().toString());
        } catch (Exception e) {
            result = Double.MAX_VALUE;
        }
        //校验最大值最小值,且与输入的值相同====》一致则加减按钮均点击无效
        if (max == min && max != Double.MIN_VALUE && max == result) {
            boolean handSub = false, handPlus = false;
            if (mViewOption != null) {
                handPlus = mViewOption.updatePlusView(this.max.toPlainString(), false);
                handSub = mViewOption.updateSubView(this.min.toPlainString(), false);
            }
            if (!handPlus && mViewPlus != null) {
                mViewPlus.setEnabled(false);
            }
            if (!handSub && mViewSub != null) {
                mViewSub.setEnabled(false);
            }
        }

        if (isInteger(increment)) {
            //整数
            isLong = true;
            mEt.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            //小数
            isLong = false;
            mEt.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }
    }

    public void removeView() {
        mEt = null;
        mViewPlus = null;
        mViewSub = null;
    }

    /**
     * 过滤et的输入结果
     * @return true :过滤   false:不过滤消息
     */
    private boolean filter(String result){
        List<IFilter> filters = mMapFilters.get(mEt);
        if (filters == null) {
            return false;
        }

        for (int i = 0; i < filters.size(); i++) {
            if (filters.get(i).filter(mEt,result)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验最大值
     * @return 如果在最大值内 返回true 否则 返回false
     */
    private boolean checkMaxInner(){
        String result = mEt.getText().toString();
        if (TextUtils.isEmpty(result)) {
            result = "0";
        }

        BigDecimal cur = new BigDecimal(result);
        int c = cur.compareTo(new BigDecimal(max + ""));
        if (c >= 1) {
            //> max(大于最大值 )
            return false;
        }
        return true;
    }
    private void checkMaxV2() {
        boolean isInMaxRound = checkMaxInner();
        if (!isInMaxRound) {
            //> max(大于最大值 )
            isCheck = false;
            String r = max.toPlainString();
            mEt.setText(r);
            setInnerSelection();
        }
        updateViewV2(isInMaxRound ? mEt.getText().toString() : max.toPlainString());
    }

    private void checkMax() {
        String result = mEt.getText().toString();
        if (TextUtils.isEmpty(result) || result.startsWith(".")) {
            return;
        }
        BigDecimal cur = new BigDecimal(result);
        int c = cur.compareTo(max);
        if (c >= 1) {
            //> max(大于最大值 )
            isCheck = false;
            String r = max.toPlainString();
            cur = max;
            mEt.setText(r);
            setInnerSelection();
        }
        updateViewV2(cur.toPlainString());
    }

    private boolean checkMinInner() {
        String result = mEt.getText().toString();
        if (TextUtils.isEmpty(result)) {
            result = "0";
        }

        BigDecimal cur = new BigDecimal(result);
        int c = cur.compareTo(min);
        if (c <= -1) {
            //> max(大于最大值 )
            return false;
        }
        return true;
    }

    private void checkMinV2() {
        boolean isInMinRound = checkMinInner();
        if (isInMinRound) {
            //小于最小值
            isCheck = false;
            String r = min.toPlainString();
            mEt.setText(r);
            setInnerSelection();
        }
        updateViewV2(isInMinRound ? mEt.getText().toString() : min.toPlainString());
    }
    private void checkMin() {
        String result = mEt.getText().toString();
        if (TextUtils.isEmpty(result)) {
            result = "0";
        }
        BigDecimal cur = new BigDecimal(result);
        int c = cur.compareTo(min);
        if (c <= -1) {
            //小于最小值
            isCheck = false;
            String r = min.toPlainString();
            cur = min;
            mEt.setText(r);
            setInnerSelection();
        }
        updateViewV2(cur.toPlainString());
    }

    private void sub() {
        String result = change(-increment);
        if (mOnClickListener != null) {
            mOnClickListener.onClickSub(result);
        }
    }

    private void plus() {
        String result = change(increment);
        if (mOnClickListener != null) {
            mOnClickListener.onClickPlus(result);
        }
    }

    private String change(float var) {
        String result = mEt.getText().toString();
        if (TextUtils.isEmpty(result)) {
            result = "0";
        }
        BigDecimal cur = new BigDecimal(result);
        int v = (int) var;
        if (isLong) {
            cur = cur.add(new BigDecimal(v + ""));
        } else {
            cur = cur.add(new BigDecimal(var + ""));
        }
        result = cur.toPlainString();
        //加减按钮点击则在此处校验 阈值
        if (isCheckMinEnable() && (cur.compareTo(min) == -1)) {
            result = min.toPlainString();
        } else if (isCheckMaxEnable() && cur.compareTo(max) == 1) {
            result = max.toPlainString();
        }
        isCheck = false;
        mEt.setText(result);
        setInnerSelection();

        updateViewV2(result);
        return result;
    }

    private void updateViewV2(String result) {
        if (TextUtils.isEmpty(result)) {
            result = "0";
        }
        boolean isSubEnable = true, isPlusEnable = true;
        int cmin = min.compareTo(new BigDecimal(result));
        int cmax = max.compareTo(new BigDecimal(result));
        if (cmin >= 1 || cmin == 0) {
            isSubEnable = false;
        } else if (cmax <= -1 || cmax == 0) {
            isPlusEnable = false;
        }

        //检查减号 todo 加上前后状态不一致才更改的条件 下同理
        if (isCheckMinEnable() &&
                mViewSub != null &&
                (mViewOption == null || !mViewOption.updateSubView(result, isSubEnable))) {
            mViewSub.setEnabled(isSubEnable);
        }
        //检查加号
        if (isCheckMaxEnable() &&
                mViewPlus != null &&
                (mViewOption == null || !mViewOption.updatePlusView(result, isPlusEnable))) {
            mViewPlus.setEnabled(isPlusEnable);
        }
    }

    /**
     * 设置光标位置
     * 因为添加filter 可能会更改txt的长度 所以设置光标位置要以最新的txt长度处理
     * 另一种处理方案则是直接添加EditText的filter 。但是因为是数组长度必须固定
     */
    private void setInnerSelection(){
        if (mEt == null) {
            return;
        }
        String txt;
        if (mEt.getText() == null || TextUtils.isEmpty(txt = mEt.getText().toString())) {
            return;
        }
        mEt.setSelection(txt.length());
    }

    private boolean isCheckMinEnable(){
        return min != null && min.compareTo(new BigDecimal(Double.MIN_VALUE+"")) != 0;
    }
    private boolean isCheckMaxEnable(){
        return max != null && max.compareTo(new BigDecimal(Double.MIN_VALUE+"")) != 0;
    }
    private boolean isInteger(float increment){
        int i = (int) increment;
        return i == increment;
    }

    public interface IViewOption {
        boolean updateSubView(String result, boolean isEnable);

        boolean updatePlusView(String result, boolean isEnable);

    }

    public interface IOnClickListener {
        boolean onClickSub(String result);

        boolean onClickPlus(String result);

        boolean onEditTextChange(String count);
    }

    public interface IFilter{
        boolean filter(EditText et, String result);
    }

}
