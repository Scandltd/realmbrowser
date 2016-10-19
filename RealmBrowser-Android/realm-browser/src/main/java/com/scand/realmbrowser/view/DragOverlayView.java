package com.scand.realmbrowser.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;

import com.scand.realmbrowser.R;

/**
 * Created by Slabodeniuk on 8/4/15.
 */
public class DragOverlayView extends View {

    private Drawable mShadow;
    private int mShortAnimTime;
    private int mPositionX;
    private int mMinLeft;

    private OnDragFinished mDragFinishListener;

    public interface OnDragFinished {
        public void onDragFinished(int position);
    }

    public DragOverlayView(Context context) {
        super(context);
        init();
    }

    public DragOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DragOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int viewHeight = getMeasuredHeight();

        int left = mPositionX;
        mShadow.setBounds(left, 0, left + mShadow.getIntrinsicWidth(), viewHeight);

        mShadow.draw(canvas);
    }

    @Override
    public boolean onDragEvent(DragEvent event) {

        int action = event.getAction();

        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                this.animate()
                        .alpha(1f)
                        .setDuration(mShortAnimTime)
                        .setListener(null);
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                setShadowPosition((int) event.getX());
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                if (mDragFinishListener != null) {
                    mDragFinishListener.onDragFinished(mPositionX);
                }
                mPositionX = 0;
                mMinLeft = 0;
                this.animate()
                        .alpha(0f)
                        .setDuration(mShortAnimTime)
                        .setListener(null);

                break;
        }
        return true;
    }

    public void setMinLeft(int minLeft) {
        mMinLeft = minLeft;
    }

    public void setShadowPosition(int x) {
        mPositionX = Math.max(mMinLeft, x);
        invalidate();
    }

    public void setOnDragFinishedListener(OnDragFinished listener) {
        mDragFinishListener = listener;
    }

    private void init() {
        mShadow = ResourcesCompat.getDrawable(getResources(),
                R.drawable.realm_browser_dummy_drag_divider_vertical,
                getContext().getTheme());

        mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
    }
}
