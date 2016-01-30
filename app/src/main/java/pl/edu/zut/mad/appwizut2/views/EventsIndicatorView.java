package pl.edu.zut.mad.appwizut2.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import pl.edu.zut.mad.appwizut2.R;

/**
 * View showing lines indicating an events in the day cell
 */
public class EventsIndicatorView extends View {

    public static final int MAX_LINE_COUNT = 5;

    private float mLineHeight;
    private int mLineCount;

    private final Paint mPaint;


    public EventsIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLineHeight = context.getResources().getDimension(R.dimen.event_line_height);

        if (isInEditMode()) {
            mLineCount = 2;
        }

        mPaint = new Paint();
    }

    public void setLineCount(int lineCount) {
        if (lineCount < 0) {
            lineCount = 0;
        }
        if (lineCount > MAX_LINE_COUNT) {
            lineCount = MAX_LINE_COUNT;
        }
        mLineCount = lineCount;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec) / 4;
        setMeasuredDimension(width, ((int) mLineHeight * (2 * MAX_LINE_COUNT - 1)));
    }


    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < mLineCount; i++) {
            canvas.drawRect(
                    0,
                    mLineHeight * (i * 2),
                    getWidth(), mLineHeight * (i * 2 + 1),
                    mPaint
            );
        }

    }

}
