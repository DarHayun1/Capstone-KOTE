package dar.games.music.capstonekote.ui.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import dar.games.music.capstonekote.R;

public class LabelAndDataView extends LinearLayout {

    private TextView mLabelTv, mDataTv;
    private String mLabel;
    private String mValue;
    private int mTextSize;

    public LabelAndDataView(Context context) {
        this(context, null);
    }

    public LabelAndDataView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LabelAndDataView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LabelAndDataView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /*
        Initialize views
     */
    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.labal_and_data, this);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LabelAndDataView,
                0, 0);
        super.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_rounded_corners));
        mLabelTv = findViewById(R.id.l_and_d_label);
        mDataTv = findViewById(R.id.l_and_d_data);

        try {
            mLabel = ta.getString(R.styleable.LabelAndDataView_label);
            mValue = ta.getString(R.styleable.LabelAndDataView_data);
            mTextSize = ta.getInt(R.styleable.LabelAndDataView_textSize, 22);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            ta.recycle();
        }
        initTypeface(context);
    }

    private void initTypeface(Context context) {
        if (mLabel == null)
            mLabel = context.getString(R.string.data_label);
        if (mValue == null)
            mValue = context.getString(R.string.data_value);
        if (mTextSize == 0)
            mTextSize = 22;
        mLabelTv.setText(mLabel);
        mLabelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
        mDataTv.setText(mValue);
        mDataTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, mTextSize);
    }

    public void setData(String label, int value) {
        mLabel = label;
        mValue = String.valueOf(value);
        setupView();
    }

    private void setupView() {
        mLabelTv.setText(mLabel);
        mDataTv.setText(mValue);
    }

    public void setValue(int value) {
        mValue = String.valueOf(value);
        setupView();
    }

    public void setStringData(String stringValue) {
        mValue = stringValue;
        setupView();
    }
}
