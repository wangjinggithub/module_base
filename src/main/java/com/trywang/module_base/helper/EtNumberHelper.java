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
import java.util.HashMap;
import java.util.Map;

/**
 * 输入框带加减符号的逻辑处理
 *
 * @author Try
 * @date 2019-08-22 11:35
 */
public class EtNumberHelper {
    private EditText mEt;
    private View mViewPlus;
    private View mViewSub;
    private IViewOption mViewOption;
    private IOnClickListener mOnClickListener;
    private boolean isLong;
    private boolean isCheck;

    private BigDecimal min;
    private BigDecimal max;
    private float increment = 1;

    Map<EditText, TextWatcher> mMapTw = new HashMap<>();

    public EtNumberHelper() {
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
        if (mEt == null || !mEt.hasFocus()) {
            return;
        }
        String res = mEt.getText().toString();
        mEt.setText(res);
        mEt.setSelection(TextUtils.isEmpty(res) ? 0 : res.length());
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
                if (min.compareTo(new BigDecimal(Long.MIN_VALUE+"")) == 0) {
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
        long ma = Long.MIN_VALUE;
        long mi = Long.MIN_VALUE;
        try {
            ma = Long.parseLong(max);
            mi = Long.parseLong(min);
        } catch (Exception e) {
        }
        setLimit(ma,mi,increment);
    }
    /**
     * 2.设置限制
     * 在{@link EtNumberHelper#withView}
     * {@link EtNumberHelper#setViewOption}
     * 之后调用
     *
     * @param max       不校验 则设置为{@link Long#MIN_VALUE}
     * @param min       不校验 则设置为{@link Long#MIN_VALUE}
     * @param increment
     * @throws IllegalArgumentException 参数异常
     */
    public void setLimit(long max, long min, float increment) throws IllegalArgumentException {
        if (increment <= 0) {
            throw new IllegalArgumentException("increment must positive number");
        }
        if (max != Long.MIN_VALUE && max < min) {
            throw new IllegalArgumentException("max < min !!!");
        }

        this.max = new BigDecimal(max+"");
        this.min = new BigDecimal(min+"");
        this.increment = increment;

        //校验最大值最小值====》一致则加减按钮均点击无效
        if (max == min && max != Long.MIN_VALUE) {
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

        if (mEt == null) {
            throw new IllegalArgumentException("EditText is Null,please set EditText");
        }

        int inc = (int) increment;
        if (inc == increment) {
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

    private void checkMax() {
        String result = mEt.getText().toString();
        if (TextUtils.isEmpty(result)) {
            return;
        }
        BigDecimal cur = new BigDecimal(result);
        int c = cur.compareTo(new BigDecimal(max + ""));
        if (c == 1) {
            //> max(大于最大值 )
            isCheck = false;
            String r = max + "";
            mEt.setText(r);
            mEt.setSelection(r.length());
        }
        updateViewV2(cur.toPlainString());

//        if (mViewOption == null) return;
//        // >= max
//        if((c == 1 || c == 0) && !mViewOption.plusIsInvaild(max)) {
//            mViewPlus.setEnabled(false);
//        } else if (!mViewOption.subAndPlusIsVaild(cur.floatValue())) {
//            mViewPlus.setEnabled(true);
//            mViewSub.setEnabled(true);
//        }
    }

    private void checkMin() {
        String result = mEt.getText().toString();
        if (TextUtils.isEmpty(result)) {
            result = "0";
        }
        BigDecimal cur = new BigDecimal(result);
        int c = cur.compareTo(new BigDecimal(min + ""));
        if (c == -1) {
            //小于最小值
            isCheck = false;
            String r = min + "";
            mEt.setText(r);
            mEt.setSelection(r.length());
        }

//        if (mViewOption == null) return;
//        // >= max
//        if((c == -1 || c == 0) && !mViewOption.subIsInvaild(min)) {
//            mViewSub.setEnabled(false);
//        } else if (!mViewOption.subAndPlusIsVaild(cur.floatValue())) {
//            mViewPlus.setEnabled(true);
//            mViewSub.setEnabled(true);
//        }
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
        mEt.setSelection(result.length());

        updateViewV2(result);
        return result;
    }

    private void updateViewV2(String result) {
        boolean isSubEnable = true, isPlusEnable = true;
        int cmin = min.compareTo(new BigDecimal(result));
        int cmax = max.compareTo(new BigDecimal(result));
        if (cmin == 1 || cmin == 0) {
            isSubEnable = false;
        } else if (cmax == -1 || cmax == 0) {
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

    private boolean isCheckMinEnable(){
        return min != null && min.compareTo(new BigDecimal(Long.MIN_VALUE+"")) != 0;
    }
    private boolean isCheckMaxEnable(){
        return max != null && max.compareTo(new BigDecimal(Long.MIN_VALUE+"")) != 0;
    }

    public interface IViewOption {
        boolean updateSubView(String result, boolean isEnable);

        boolean updatePlusView(String result, boolean isEnable);

    }

    public interface IOnClickListener {
        boolean onClickSub(String result);

        boolean onClickPlus(String result);
    }

}
