package dar.games.music.capstonekote.ui.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;


public class FixedRatioFrameLayout extends FrameLayout {


    public FixedRatioFrameLayout(Context context)
    {
        super(context);
    }

    public FixedRatioFrameLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FixedRatioFrameLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)
    {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY));
    }
}
