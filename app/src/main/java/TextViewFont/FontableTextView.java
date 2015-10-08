package TextViewFont;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.chernov.android.android_paralaxparse.R;

/**
 * Created by Android on 06.10.2015.
 */
public class FontableTextView extends TextView {

    public FontableTextView(Context context) {
        super(context);
    }

    public FontableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        UiUtil.setCustomFont(this, context, attrs,
                R.styleable.TextViewFont_FontableTextView,
                R.styleable.TextViewFont_FontableTextView_font);
    }

    public FontableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        UiUtil.setCustomFont(this, context, attrs,
                R.styleable.TextViewFont_FontableTextView,
                R.styleable.TextViewFont_FontableTextView_font);
    }
}
