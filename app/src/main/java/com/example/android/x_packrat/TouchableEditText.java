package com.example.android.x_packrat;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

/**
 * Custom EditText used to avoid the warning message to override performClick() when
 * setting a touch listener on a regular EditText
 */
public class TouchableEditText extends AppCompatEditText {
    Context context;

    public TouchableEditText(Context context) {
        super(context);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public TouchableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public TouchableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }
}
