package com.trywang.module_base.helper;

import android.text.TextUtils;
import android.widget.EditText;

/**
 * TODO 写清楚此类的作用
 *
 * @author Try
 * @date 2020-01-13 11:16
 */
public class EtDecimalLimitFilter implements EtNumberHelperV4.IFilter {
    int limit = 2;

    public EtDecimalLimitFilter() {
    }

    public EtDecimalLimitFilter(int limit) {
        this.limit = limit;
    }

    @Override
    public boolean filter(EditText et, String result) {
        if (TextUtils.isEmpty(result)) {
            return false;
        }
        String[] s = result.split("\\.");
        if (s.length > 1 && s[1].length() > 2) {
            String replace = String.format("%s.%s",s[0],s[1].substring(0,2));
            et.setText(replace);
            et.setSelection(replace.length());
            return true;
        }

        return false;
    }
}
