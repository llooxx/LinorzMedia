package com.linorz.linorzmedia.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by linorz on 2017/8/18.
 */

@SuppressLint("AppCompatCustomView")
public class HorizontalScrollTextView extends TextView {
    /**
     * android:ellipsize="marquee"
     * android:singleLine="true"
     * android:marqueeRepeatLimit="marquee_forever"
     */
    public HorizontalScrollTextView(Context context) {
        super(context);
    }

    public HorizontalScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScrollTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}