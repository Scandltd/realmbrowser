package com.scand.realmbrowser.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scand.realmbrowser.R;

/**
 * Created by Slabodeniuk on 6/17/15.
 */
public class RowView extends HorizontalScrollView {

    // TODO move all this constants into xml declaration by applying custom attributes
    private static final int MIN_COLUMN_WIDTH_PX = 75;
    private static final int ITEM_PADDING_LEFT_RIGHT_DP = 4;

    public interface OnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    public interface OnCellClickListener {
        void onCellClick(RowView view, int position);
    }

    public interface OnColumnWidthChangeListener {
        void startColumnWidthChange(int minX, int currentLeft, int currentRight, int position);
    }

    private LinearLayout mRootTable;

    private int mMinColumnWidth;
    private int mMinColumnHeight = -1;
    private int mColumnsNumber = -1;
    private int mWidth = -1;
    private int mItemPaddingLeftRight;

    private RowView.OnScrollChangedListener mScrollListener;
    private RowView.OnCellClickListener mCellClickListener;
    private OnColumnWidthChangeListener mColumnWidthChangeListener;

    private SparseArray<CharSequence> mTextBuffer = new SparseArray<>();

    private int mTextAppearanceResourceId;
    private int mCellGravity = Gravity.TOP;

    public RowView(Context context) {
        super(context);
        init();
    }

    public RowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setOnScrollChangedListener(RowView.OnScrollChangedListener listener) {
        mScrollListener = listener;
    }

    public void setColumnsNumber(int number) {
        if (number < 0) throw new IllegalArgumentException("Invalid columns number");

        mColumnsNumber = number;
        initColumns();
    }

    public int getMinColumnHeight() {
        return mMinColumnHeight;
    }

    public void setMinColumnHeight(int mMinColumnHeight) {
        this.mMinColumnHeight = mMinColumnHeight;
    }

    public int getColumnsNumber() {
        return mColumnsNumber;
    }

    public void setColumnText(CharSequence text, int position) {

        if (mWidth == -1) {
            mTextBuffer.append(position, text);
        } else {
            checkPositionRange(position);

            TextView tv = (TextView) mRootTable.getChildAt(position);
            tv.setText(text);
        }
    }

    public void setTextAppearance(int resId) {
        mTextAppearanceResourceId = resId;

        if (mRootTable.getChildCount() > 0) {
            for (int i = 0; i < mRootTable.getChildCount(); i++) {
                TextView tv = (TextView) mRootTable.getChildAt(i);
                tv.setTextAppearance(getContext(), mTextAppearanceResourceId);
            }
        }
    }

    public void setCellsGravity(int gravity) {
        mCellGravity = gravity;

        if (mRootTable.getChildCount() > 0) {
            for (int i = 0; i < mRootTable.getChildCount(); i++) {
                TextView tv = (TextView) mRootTable.getChildAt(i);
                tv.setGravity(mCellGravity);
            }
        }
    }

    public void showDividers(boolean showDividers) {

        Resources res = getResources();
        Resources.Theme theme = getContext().getTheme();
        int dividerId = showDividers ? R.drawable.realm_browser_divider_vertical : R.drawable.realm_browser_placeholder_1dp;

        mRootTable.setDividerDrawable(ResourcesCompat.getDrawable(res, dividerId, theme));
    }

    public void setOnCellClickListener(OnCellClickListener listener) {
        mCellClickListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw && w != 0) {
            mWidth = w;
            updateColumns();
        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if (mScrollListener != null) {
            mScrollListener.onScrollChanged(l, t, oldl, oldt);
        }
        //  Turned off for better synchronization while scrolling several views
        //  super.onScrollChanged(l, t, oldl, oldt);
    }

    private void checkPositionRange(int position) {
        int childCnt = mRootTable.getChildCount();
        if (position >= childCnt || position < 0) {
            throw new IllegalArgumentException("Invalid position, position "
                    + position + ", child count " + childCnt);
        }
    }

    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);

        DisplayMetrics m = getResources().getDisplayMetrics();
        mItemPaddingLeftRight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                ITEM_PADDING_LEFT_RIGHT_DP,
                m);

        mMinColumnWidth = getResources().getDimensionPixelSize(R.dimen.realm_browser_min_column_width);

        setHorizontalScrollBarEnabled(false);
        setClipChildren(false);
        setClipToPadding(false);

        mRootTable = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mRootTable.setLayoutParams(layoutParams);
        mRootTable.setOrientation(LinearLayout.HORIZONTAL);
        mRootTable.setClipChildren(false);
        mRootTable.setClipToPadding(false);
        mRootTable.setDividerDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.realm_browser_placeholder_1dp,
                getContext().getTheme()));

        mRootTable.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);

        addView(mRootTable);
    }

    private void initColumns() {

        int requiredWidth = mMinColumnWidth * mColumnsNumber;

        mRootTable.removeAllViews();
        if (mColumnsNumber == 0) return;

        int columnWidth;

        if (mWidth == -1 || requiredWidth >= mWidth) {
            columnWidth = mMinColumnWidth;
        } else {
            columnWidth = mWidth / mColumnsNumber;
        }

        for (int i = 0; i < mColumnsNumber; i++) {
            TextView tv = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(columnWidth,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            tv.setLayoutParams(params);
            tv.setGravity(mCellGravity);
            tv.setTextAppearance(getContext(), mTextAppearanceResourceId);
            tv.setMaxLines(2);
            tv.setEllipsize(TextUtils.TruncateAt.END);
            tv.setPadding(mItemPaddingLeftRight, 0, mItemPaddingLeftRight, 0);

            if (mMinColumnHeight > 0)
                tv.setMinimumHeight(mMinColumnHeight);

            tv.setTag(i);
            tv.setOnClickListener(mInternalCellClickListener);

            tv.setOnLongClickListener(mInternalLongClickListener);
            mRootTable.addView(tv);
        }

    }

    private void updateColumns() {
        if (mWidth == -1 || mColumnsNumber == 0) return;

        if (mRootTable.getChildCount() != mColumnsNumber)
            initColumns();

        int requiredWidth = mMinColumnWidth * mColumnsNumber;

        int columnWidth;

        if (requiredWidth >= mWidth) {
            columnWidth = mMinColumnWidth;
        } else {
            columnWidth = mWidth / mColumnsNumber;
        }


        for (int i = 0; i < mColumnsNumber; i++) {
            TextView tv = (TextView) mRootTable.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
            if (params.width != columnWidth) {
                params.width = columnWidth;
                tv.setLayoutParams(params);
            }
            // set text from text buffer
            tv.setText(mTextBuffer.get(i));
        }
    }

    public void setColumnWidth(int width, int position) {
        if (mWidth == -1) return;

        TextView tv = (TextView) mRootTable.getChildAt(position);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                ViewGroup.LayoutParams.MATCH_PARENT);
        tv.setLayoutParams(params);
    }

    public int getColumnWidth(int position) {
        TextView tv = (TextView) mRootTable.getChildAt(position);
        return tv.getWidth();
    }

    public void setOnColumnWidthChangeListener(OnColumnWidthChangeListener listener) {
        mColumnWidthChangeListener = listener;
    }

    private final View.OnClickListener mInternalCellClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mCellClickListener != null) {
                int position = (int) v.getTag();
                mCellClickListener.onCellClick(RowView.this, position);
            }
        }
    };

    private final OnLongClickListener mInternalLongClickListener = new OnLongClickListener() {

        private int[] location = new int[2];

        @Override
        public boolean onLongClick(View v) {
            if (mColumnWidthChangeListener != null) {
                v.getLocationOnScreen(location);
                int position = (int) v.getTag();
                mColumnWidthChangeListener.startColumnWidthChange(location[0] + MIN_COLUMN_WIDTH_PX,
                        location[0],
                        location[0] + v.getWidth(),
                        position);
            }
            return true;
        }
    };

}
