package dar.games.music.capstonekote.ui.customviews;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import dar.games.music.capstonekote.R;

public class KoteButton extends LinearLayout {
    private TextView mTextTv;
    private ImageView miconIv;
    private String mText;
    private Drawable mDrawable;

    public KoteButton(Context context) {
        this(context, null);
    }

    public KoteButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KoteButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public KoteButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /*
        Initialize views
     */
    private void init(Context context, AttributeSet attrs) {
        inflate(getContext(), R.layout.button_layout, this);

        mTextTv = findViewById(R.id.kote_button_text);
        miconIv = findViewById(R.id.kote_button_image);

        super.setBackground(ContextCompat.getDrawable(context, R.drawable.plain_rounded_corners));

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.KoteButton,
                0, 0);

        try {
            mText = ta.getString(R.styleable.KoteButton_buttonText);
            mDrawable = ta.getDrawable(R.styleable.KoteButton_buttonIcon);
        }
        finally {
            ta.recycle();
        }
        setupView();
    }

    private void setupView() {
        mTextTv.setText(mText);
        if (mDrawable != null){
            miconIv.setImageDrawable(mDrawable);
            miconIv.setVisibility(VISIBLE);
        }

        else
            miconIv.setVisibility(GONE);
    }

    public void setupButton(String text, Drawable drawable){
        mText = text;
        mDrawable = drawable;
        setupView();
    }

    public String getText(){
        return (String) mTextTv.getText();
    }

    public Drawable getDrawable(){
        return miconIv.getDrawable();
    }

    public void setDrawable(Drawable mDrawable) {
        this.mDrawable = mDrawable;
        setupView();
    }

    public void setText(String mText) {
        this.mText = mText;
        setupView();
    }

}
