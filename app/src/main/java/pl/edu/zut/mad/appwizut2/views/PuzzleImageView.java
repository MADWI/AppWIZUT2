package pl.edu.zut.mad.appwizut2.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.util.Random;

import pl.edu.zut.mad.appwizut2.R;

/**
 * Completely normal ImageView
 * <p/>
 * (Pinch out to activate, tap to start)
 */
public class PuzzleImageView extends ImageView implements ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnGestureListener {

    /**
     * Paint used to draw lines delimiting pieces
     */
    private final Paint mSeparatorLinesPaint;


    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    /**
     * Factor controlling length of pinch required to switch difficulty
     *
     * Must be greater than 1
     */
    private static final float SCALE_FACTOR_TO_SWITCH_SIZE = 1.5f;

    /**
     * Amounts of puzzles for various difficulties
     *
     * Pairs of amounts of pieces along X and Y axis
     *
     * Must start with (1, 1) for deactivated state
     */
    private static final int[] NUM_PIECES = new int[]{1, 1, 2, 2, 4, 2, 5, 3};

    /**
     * Value indicating empty field in {@link #mPiecePositions}
     */
    private static final byte EMPTY_FIELD = -1;

    /**
     * Clip path used when image is not in puzzled state
     *
     * Used for {@link #animateInLastElement()}
     */
    private Path mNotPuzzledClipPath;

    public PuzzleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mGestureDetector = new GestureDetector(context, this);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mSeparatorLinesPaint = new Paint();
        mSeparatorLinesPaint.setStrokeWidth(context.getResources().getDimension(R.dimen.puzzle_piece_separator_width));
        mSeparatorLinesPaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * Index of pair in {@link #NUM_PIECES}
     */
    private int mCurrentScaleSetting = 0;
    private byte mPiecePositions[];

    private enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    private Direction mCurrentlyMovedPieceDirection;
    private byte mCurrentlyMovedPiecePos = EMPTY_FIELD;

    private int mMoveXOffset;
    private int mMoveYOffset;

    private OnSolvedListener mOnSolvedListener;

    public void setOnSolvedListener(OnSolvedListener onSolvedListener) {
        mOnSolvedListener = onSolvedListener;
    }

    /**
     * Whether the view is triggered, that is we don't pretend it's plain ImageView anymore
     */
    private boolean isTriggered() {
        return isPuzzled() || mCurrentScaleSetting != 0 || mScaleGestureDetector.isInProgress();
    }

    private boolean isPuzzled() {
        return mPiecePositions != null;
    }

    /**
     * Get {@link #mCurrentScaleSetting} with fraction indicating currently done partial scaling by user
     */
    private float getCurrentScaleWithFraction() {
        // Check if scaling is in progress
        if (!mScaleGestureDetector.isInProgress()) {
            return mCurrentScaleSetting;
        }

        // Add factor from current scaling
        float result = mCurrentScaleSetting;
        float scaleFactor = mScaleGestureDetector.getScaleFactor();
        float A = SCALE_FACTOR_TO_SWITCH_SIZE - 1.0f;
        float B = 1.0f / (A);
        if (scaleFactor < 1) {
            // Scaling down (making more pieces, increasing current scale value)
            result += (1.0f / scaleFactor) / A - B;
        } else {
            // Scaling up (making less pieces, decreasing current scale value)
            result -= (scaleFactor) / A - B;
        }

        // Clamp result
        if (result < 0) {
            return 0;
        }
        if (result > NUM_PIECES.length / 2 - 1) {
            return NUM_PIECES.length / 2 - 1;
        }

        return result;
    }

    private int getPiecesX() {
        return NUM_PIECES[mCurrentScaleSetting * 2];
    }

    private int getPiecesY() {
        return NUM_PIECES[mCurrentScaleSetting * 2 + 1];
    }

    /**
     * Get amount of pieces that can be fraction for drawing field splitting lines when choosing size
     *
     * @param axis 0 for X, 1 for Y
     * @param currentScaleValue Value returned by {@link #getCurrentScaleWithFraction()}
     */
    private float currentScaleFractionToNumPiecesFraction(float currentScaleValue, int axis) {
        int baseScaleValue = (int) currentScaleValue;
        if (baseScaleValue < 0) {
            return NUM_PIECES[axis];
        }
        if (baseScaleValue >= NUM_PIECES.length / 2 - 1) {
            return NUM_PIECES[NUM_PIECES.length - 2 + axis];
        }

        float fr = currentScaleValue - baseScaleValue;
        float a = NUM_PIECES[baseScaleValue * 2 + axis];
        float b = NUM_PIECES[baseScaleValue * 2 + 2 + axis];
        return a + (b - a) * fr;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // If there's no drawable, don't crash
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        Matrix matrix = getImageMatrix();
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Set padding
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());

        // Draw image
        if (!isPuzzled()) {
            canvas.save();
            if (mNotPuzzledClipPath != null) {
                canvas.clipPath(mNotPuzzledClipPath);
            }
            canvas.concat(matrix);
            drawable.draw(canvas);
            canvas.restore();
        }

        if (isTriggered()) {
            if (isPuzzled()) {
                // Draw puzzled elements

                int piecesX = getPiecesX();
                int piecesY = getPiecesY();

                int pieceWidth = viewWidth / piecesX;
                int pieceHeight = viewHeight / piecesY;

                for (int i = 0; i < mPiecePositions.length; i++) {
                    byte piecePosition = mPiecePositions[i];
                    if (piecePosition == EMPTY_FIELD) {
                        continue;
                    }

                    // Calculate positions
                    int viewX = i % piecesX;
                    int viewY = i / piecesX;
                    int imageX = piecePosition % piecesX;
                    int imageY = piecePosition / piecesX;


                    int piecePositionX = viewX * pieceWidth;
                    int piecePositionY = viewY * pieceHeight;

                    if (mCurrentlyMovedPieceDirection != null && i == mCurrentlyMovedPiecePos) {
                        switch (mCurrentlyMovedPieceDirection) {
                            case LEFT:
                                piecePositionX += (mMoveXOffset < -pieceWidth ? -pieceWidth : mMoveXOffset);
                                break;
                            case RIGHT:
                                piecePositionX += (mMoveXOffset > pieceWidth ? pieceWidth : mMoveXOffset);
                                break;
                            case UP:
                                piecePositionY += (mMoveYOffset < -pieceHeight ? -pieceHeight : mMoveYOffset);
                                break;
                            case DOWN:
                                piecePositionY += (mMoveYOffset > pieceHeight ? pieceHeight : mMoveYOffset);
                                break;
                        }
                    }

                    // Draw the piece
                    canvas.save();
                    canvas.clipRect(
                            piecePositionX,
                            piecePositionY,
                            piecePositionX + pieceWidth,
                            piecePositionY + pieceHeight
                    );
                    canvas.translate(
                            piecePositionX - imageX * pieceWidth,
                            piecePositionY - imageY * pieceHeight
                    );
                    canvas.concat(matrix);
                    drawable.draw(canvas);
                    canvas.restore();

                    // Draw overlay
                    canvas.drawRect(
                            piecePositionX,
                            piecePositionY,
                            piecePositionX + pieceWidth,
                            piecePositionY + pieceHeight,
                            mSeparatorLinesPaint
                    );
                }
            } else {
                // Draw size choosing lines
                float currentScaleWithFraction = getCurrentScaleWithFraction();

                // Split by X (vertical lines)
                float linesF = currentScaleFractionToNumPiecesFraction(currentScaleWithFraction, 0);
                float linesOffset = viewWidth / linesF;
                int linesCount = (int) linesF;
                for (int i = 0; i < linesCount; i++) {
                    float offset = (i + 1) * linesOffset;
                    canvas.drawLine(offset, 0, offset, viewHeight, mSeparatorLinesPaint);
                }

                // Split by Y (horizontal lines)
                linesF = currentScaleFractionToNumPiecesFraction(currentScaleWithFraction, 1);
                linesOffset = viewHeight / linesF;
                linesCount = (int) linesF;
                for (int i = 0; i < linesCount; i++) {
                    float offset = (i + 1) * linesOffset;
                    canvas.drawLine(0, offset, viewWidth, offset, mSeparatorLinesPaint);
                }
            }
        }

        // Restore canvas state (padding)
        canvas.restore();
    }

    /**
     * Initialize puzzle array (without shuffling)
     */
    private void makePuzzle() {
        int nElems = getPiecesX() * getPiecesY();
        mPiecePositions = new byte[nElems];
        for (int i = 0; i < nElems - 1; i++) {
            mPiecePositions[i] = (byte) i;
        }
        mPiecePositions[nElems - 1] = EMPTY_FIELD;
    }

    /**
     * Check if puzzle is solved; may be only called when {@link #isPuzzled()} is true
     */
    private boolean isSolved() {
        int nElems = getPiecesX() * getPiecesY();
        for (int i = 0; i < nElems - 1; i++) {
            if (mPiecePositions[i] != (byte) i) {
                return false;
            }
        }
        return true;
    }

    private void shuffle() {
        Random random = new Random();

        // Find empty position
        int emptyPos = -1;
        for (int i = 0; i < mPiecePositions.length; i++) {
            if (mPiecePositions[i] == EMPTY_FIELD) {
                emptyPos = i;
            }
        }

        // Keep randomly moving pieces
        for (int i = 0; i < 1000 || isSolved(); i++) {
            // Choose random direction
            int toMove = -1;
            switch (Direction.values()[random.nextInt(4)]) {
                case LEFT:
                    if (emptyPos % getPiecesX() != getPiecesX() - 1) {
                        toMove = (byte) (emptyPos + 1);
                    }
                    break;
                case RIGHT:
                    if (emptyPos % getPiecesX() != 0) {
                        toMove = (byte) (emptyPos - 1);
                    }
                    break;
                case UP:
                    if (emptyPos / getPiecesX() != getPiecesY() - 1) {
                        toMove = (byte) (emptyPos + getPiecesX());
                    }
                    break;
                case DOWN:
                    if (emptyPos / getPiecesX() != 0) {
                        toMove = (byte) (emptyPos - getPiecesX());
                    }
                    break;
            }

            // Perform move if possible
            if (toMove != -1) {
                mPiecePositions[emptyPos] = mPiecePositions[toMove];
                mPiecePositions[toMove] = EMPTY_FIELD;
                emptyPos = toMove;
            }
        }
    }

    /**
     * Select piece to move and make it start following finger
     */
    private void startMovingPiece(Direction direction) {
        // If we have direction selected, nothing more to do
        if (mCurrentlyMovedPieceDirection == direction) {
            return;
        }

        // Find empty position
        int emptyPos = -1;
        for (int i = 0; i < mPiecePositions.length; i++) {
            if (mPiecePositions[i] == EMPTY_FIELD) {
                emptyPos = i;
            }
        }

        // Choose piece basing on empty position and move direction
        switch (direction) {
            case LEFT:
                if (emptyPos % getPiecesX() == getPiecesX() - 1) {
                    mCurrentlyMovedPieceDirection = null;
                    return;
                }
                mCurrentlyMovedPiecePos = (byte) (emptyPos + 1);
                break;
            case RIGHT:
                if (emptyPos % getPiecesX() == 0) {
                    mCurrentlyMovedPieceDirection = null;
                    return;
                }
                mCurrentlyMovedPiecePos = (byte) (emptyPos - 1);
                break;
            case UP:
                if (emptyPos / getPiecesX() == getPiecesY() - 1) {
                    mCurrentlyMovedPieceDirection = null;
                    return;
                }
                mCurrentlyMovedPiecePos = (byte) (emptyPos + getPiecesX());
                break;
            case DOWN:
                if (emptyPos / getPiecesX() == 0) {
                    mCurrentlyMovedPieceDirection = null;
                    return;
                }
                mCurrentlyMovedPiecePos = (byte) (emptyPos - getPiecesX());
                break;
        }
        mCurrentlyMovedPieceDirection = direction;
    }

    /**
     * Finish moving piece selected with {@link #startMovingPiece(Direction)}
     * and move it to it's destination
     */
    private void finishMovingPiece() {
        // If we're not moving piece
        if (mCurrentlyMovedPieceDirection == null) {
            // Just clear move offsets
            mMoveXOffset = 0;
            mMoveYOffset = 0;
            return;
        }

        // Position that will be occupied by piece after move
        int newPosition = -1;

        switch (mCurrentlyMovedPieceDirection) {
            case LEFT:
                newPosition = mCurrentlyMovedPiecePos - 1;
                break;
            case RIGHT:
                newPosition = mCurrentlyMovedPiecePos + 1;
                break;
            case UP:
                newPosition = mCurrentlyMovedPiecePos - getPiecesX();
                break;
            case DOWN:
                newPosition = mCurrentlyMovedPiecePos + getPiecesX();
                break;
        }

        // Move piece in array
        mPiecePositions[newPosition] = mPiecePositions[mCurrentlyMovedPiecePos];
        mPiecePositions[mCurrentlyMovedPiecePos] = EMPTY_FIELD;

        // Clear movement
        mCurrentlyMovedPieceDirection = null;
        mMoveXOffset = 0;
        mMoveYOffset = 0;

        // Check if puzzle is solved
        if (isSolved()) {
            mPiecePositions = null;
            animateInLastElement();
            if (mOnSolvedListener != null) {
                mOnSolvedListener.onPuzzleSolved(mCurrentScaleSetting);
            }
        }
    }

    /**
     * Show animation of last element appearing (after puzzle is solved)
     */
    private void animateInLastElement() {
        // Calculate sizes
        final int viewWidth = getWidth();
        final int viewHeight = getHeight();

        final int pieceWidth = viewWidth / getPiecesX();
        final int pieceHeight = viewHeight / getPiecesY();

        // Make value animator
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.5f, 0f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (Float) animation.getAnimatedValue();
                if (mNotPuzzledClipPath == null) {
                    mNotPuzzledClipPath = new Path();
                } else {
                    mNotPuzzledClipPath.reset();
                }

                // Actually draw clip path of last element appearing
                // Whole image not clipped away (CW - Adds non-clipped region)
                mNotPuzzledClipPath.addRect(
                        0,
                        0,
                        viewWidth,
                        viewHeight,
                        Path.Direction.CW
                );
                // Remove center rectangle in last piece (CCW - Adds clipped-out region)
                mNotPuzzledClipPath.addRect(
                        viewWidth - pieceWidth * (0.5f + animatedValue),
                        viewHeight - pieceHeight * (0.5f + animatedValue),
                        viewWidth - pieceWidth * (0.5f - animatedValue),
                        viewHeight - pieceHeight * (0.5f - animatedValue),
                        Path.Direction.CCW
                );
                invalidate();
            }
        });
        // Reset clip path when animation is finished
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mNotPuzzledClipPath = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mNotPuzzledClipPath = null;
            }
        });

        // Start animation
        valueAnimator.setDuration(700);
        valueAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handledGenericGesture = mGestureDetector.onTouchEvent(event);
        boolean handledScaleGesture = mScaleGestureDetector.onTouchEvent(event);
        boolean handledOtherwise = false;

        if (isPuzzled() && event.getActionMasked() == MotionEvent.ACTION_UP) {
            finishMovingPiece();
            invalidate();
            handledOtherwise = true;
        }

        if (isTriggered()) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return handledGenericGesture || handledScaleGesture || handledOtherwise;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {}

    /**
     * Handle single tap to start the puzzle after choosing size
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mCurrentScaleSetting != 0 && !isPuzzled()) {
            makePuzzle();
            shuffle();
            invalidate();
        }
        return false;
    }

    /**
     * Handle dragging (not actually scrolling as suggested by name from GestureDetector interface)
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // If not in puzzle mode, do nothing
        if (!isPuzzled()) {
            return false;
        }

        // Record movement
        mMoveXOffset -= distanceX;
        mMoveYOffset -= distanceY;

        // Choose piece to move
        if (Math.abs(mMoveYOffset) > Math.abs(mMoveXOffset)) {
            startMovingPiece(mMoveYOffset < 0 ? Direction.UP : Direction.DOWN);
        } else {
            startMovingPiece(mMoveXOffset < 0 ? Direction.LEFT : Direction.RIGHT);
        }

        // Redraw
        invalidate();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {}

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        invalidate();
        if (scaleFactor > SCALE_FACTOR_TO_SWITCH_SIZE && mCurrentScaleSetting > 0) {
            // Scale up (less pieces)
            mCurrentScaleSetting--;
            return true;
        } else if (scaleFactor < 1.0f / SCALE_FACTOR_TO_SWITCH_SIZE && mCurrentScaleSetting < NUM_PIECES.length / 2 - 1) {
            // Scale down (more pieces)
            mCurrentScaleSetting++;
            return true;
        }
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        if (!isPuzzled()) {
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mCurrentScaleSetting = Math.round(getCurrentScaleWithFraction());
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.mCurrentScaleSetting = mCurrentScaleSetting;
        state.mPiecePositions = mPiecePositions;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState myState = (SavedState) state;
        mCurrentScaleSetting = myState.mCurrentScaleSetting;
        mPiecePositions = myState.mPiecePositions;
        super.onRestoreInstanceState(myState.getSuperState());
    }

    public static class SavedState extends BaseSavedState {
        int mCurrentScaleSetting;
        byte[] mPiecePositions;

        public SavedState(Parcel source) {
            super(source);
            mCurrentScaleSetting = source.readInt();
            mPiecePositions = source.createByteArray();

        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mCurrentScaleSetting);
            out.writeByteArray(mPiecePositions);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

    }

    public interface OnSolvedListener {
        void onPuzzleSolved(int difficulty);
    }

}
