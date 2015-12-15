package pl.edu.zut.mad.appwizut2.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import pl.edu.zut.mad.appwizut2.R;

/**
 * Created by mb on 30.11.15.
 */
public class FoldableTextView extends AppCompatTextView {

    public static final int NOT_MEASURED = -13; // Invalid width value

    private int mIsMeasuredForWidth = NOT_MEASURED;

    private int mWidth;
    private int mExpandedHeight;
    private final int mFoldedHeight;
    private View mViewSeeMore;

    private float mExpandness; // 0 - folded, 1 - expanded,

    private ValueAnimator mAnim;

    public FoldableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFoldedHeight = (int) context.getResources().getDimension(R.dimen.body_of_card_min_height);
    }


    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        mIsMeasuredForWidth = NOT_MEASURED;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Measure if we don't have cached value
        if (mIsMeasuredForWidth != widthMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
            mWidth = getMeasuredWidth();
            mExpandedHeight = getMeasuredHeight();
            mIsMeasuredForWidth = widthMeasureSpec;
        }

        // Set our size
        int heightToSet;
        if (mExpandedHeight < mFoldedHeight) {
            // If folding would make this view bigger, always be expanded
            heightToSet = mExpandedHeight;
            mViewSeeMore.setEnabled(false);
        } else {
            heightToSet = (int) (mExpandedHeight * mExpandness + mFoldedHeight * (1 - mExpandness));
            mViewSeeMore.setEnabled(true);
        }
        setMeasuredDimension(mWidth, heightToSet);
    }

    /**
     * Expand or collapse this view
     *
     * @param expanded true to expand, false to fold
     * @param animate true for animation, false for instant set
     */
    public void setExpanded(final boolean expanded, boolean animate) {
        // If animation is running, cancel it
        if (mAnim != null) {
            mAnim.cancel();
        }

        float targetExpandness = expanded ? 1 : 0;

        if (animate) {
            mAnim = ValueAnimator.ofFloat(mExpandness, targetExpandness);
            mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mExpandness = (Float) valueAnimator.getAnimatedValue();
                    requestLayout();
                }

            });

            mAnim.setDuration(500);
            mAnim.start();
        } else {
            mAnim = null;
            mExpandness = targetExpandness;
            requestLayout();
        }
    }

    public void setSeeMoreView(View vSeeMore) {
        this.mViewSeeMore = vSeeMore;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mViewSeeMore.isEnabled()) {
            if (getHeight() > mFoldedHeight) {
                mViewSeeMore.setVisibility(View.GONE);
            } else {
                mViewSeeMore.setVisibility(View.VISIBLE);
            }
        } else {
            mViewSeeMore.setVisibility(View.GONE);
        }
    }
}
