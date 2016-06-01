package pl.edu.zut.mad.appwizut2.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * RecyclerView variant that will never report AT_MOST widthSpec
 *
 * Used to work-around bug in RecyclerView when used in Dialog
 */
public class NonShrinkingRecyclerView extends RecyclerView {
    public NonShrinkingRecyclerView(Context context) {
        this(context, null);
    }

    public NonShrinkingRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthSpec), MeasureSpec.EXACTLY),
                heightSpec
        );
    }
}
