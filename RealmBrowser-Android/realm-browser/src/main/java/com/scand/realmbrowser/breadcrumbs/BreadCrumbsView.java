package com.scand.realmbrowser.breadcrumbs;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.scand.realmbrowser.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by amykh on 04.08.2015.
 */
public class BreadCrumbsView extends LinearLayout {

    // TODO move all this constants into xml declaration by applying custom attributes
    private static final String COLLAPSE_INDICATOR = "...";
    private static final int COLLAPSE_INDICATOR_TAG = -1;
    private static final int COLLAPSE_INDICATOR_PADDING_DP = 2;
    private static final int CRUMB_MAX_LINES = 1;
    private static final int CRUMB_TEXT_APP_RESOURCE = R.style.realm_browser_database_bread_crumb_style;
    private static final int INACTIVE_CRUMB_COLOR = Color.rgb(148, 148, 148);
    private static final int ACTIVE_CRUMB_COLOR = Color.rgb(255, 255, 255);


    private int mCollapseIndicatorPaddingPx;
    private int mWidth;
    private List<StateHolder> mCrumbStates;
    private IOnBreadCrumbListener mCrumbStatesListener;

    private Context mContext;

    public BreadCrumbsView(Context context) {
        super(context);
        init(context);
    }

    public BreadCrumbsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BreadCrumbsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BreadCrumbsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mCrumbStates = new ArrayList<>();
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        DisplayMetrics m = getResources().getDisplayMetrics();
        mCollapseIndicatorPaddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                COLLAPSE_INDICATOR_PADDING_DP,
                m);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        updateView(w);
    }

    private void activateCrumb(StateHolder stateHolder) {
        int position = mCrumbStates.indexOf(stateHolder);
        int count = mCrumbStates.size();
        if (count <= position)
            throw new IllegalArgumentException(String.format("BreadcrumbsView: count = %d, position = %d", count, position));

        mCrumbStates = mCrumbStates.subList(0, position + 1);

        updateView(mWidth);

        if (mCrumbStatesListener != null)
            mCrumbStatesListener.onStateChanged(stateHolder);
    }

    private OnClickListener mOnCrumbClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = (int) v.getTag();
            StateHolder holder = (tag == COLLAPSE_INDICATOR_TAG)
                    ? mCrumbStates.get(mCrumbStates.size() - 2)
                    : mCrumbStates.get(tag);
            activateCrumb(holder);
        }
    };

    public void addCrumb(StateHolder state) {
        mCrumbStates.add(state);
        updateView(mWidth);
    }

    public void clearCrumbs() {
        mCrumbStates.clear();
        updateView(mWidth);
    }

    public void setCrumbStates(List<StateHolder> crumbStates) {
        mCrumbStates = crumbStates;
        updateView(mWidth);
        if (mCrumbStatesListener != null && mCrumbStates != null && !mCrumbStates.isEmpty()) {
            mCrumbStatesListener.onStateChanged(mCrumbStates.get(mCrumbStates.size() - 1));
        }
    }

    public List<StateHolder> getCrumbStates() {
        return mCrumbStates;
    }

    private TextView createDefaultView(String text, int position, int totalSize) {
        TextView view = new TextView(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);
        view.setPadding(mCollapseIndicatorPaddingPx, 0, 0, 0);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setMaxLines(CRUMB_MAX_LINES);
        view.setEllipsize(TextUtils.TruncateAt.END);
        if (mContext.getResources().getResourceName(CRUMB_TEXT_APP_RESOURCE) != null) {
            view.setTextAppearance(mContext, CRUMB_TEXT_APP_RESOURCE);
            view.setText(Html.fromHtml(text));
        } else {
            view.setText(Html.fromHtml("<u>" + text + "</u>"));
        }

        view.setOnClickListener(mOnCrumbClickListener);
        view.setTag(position);

        int textColor;
        if (position != totalSize - 1) {
            // it isn't last crumb
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.realm_browser_ic_breadcrumb, 0);
            view.setCompoundDrawablePadding(mCollapseIndicatorPaddingPx);
            textColor = INACTIVE_CRUMB_COLOR;
        } else {
            // it is last crumb
            textColor = ACTIVE_CRUMB_COLOR;
        }
        view.setTextColor(textColor);
        view.setGravity(Gravity.CENTER_VERTICAL);

        return view;
    }

    private String getSimpleName(String text) {
        String[] path = text.split("\\.");
        return path.length > 0 ? path[path.length - 1] : text;
    }

    private void updateView(int parentViewWidth) {
        removeAllViews();
        if (mCrumbStates == null || mCrumbStates.isEmpty())
            return;

        int count = mCrumbStates.size();

        TextView rootView = createDefaultView(getSimpleName(mCrumbStates.get(0).getCaption()), 0, count);
        addView(rootView);

        TextView view;
        for (int i = 1; i < count; i++) {
            view = createDefaultView(getSimpleName(mCrumbStates.get(i).getCaption()), i, count);
            addView(view);

            if (calculateWidth() > parentViewWidth) {
                removeViews(1, getChildCount() - 2);
                view = createDefaultView(COLLAPSE_INDICATOR, COLLAPSE_INDICATOR_TAG, count);
                addView(view, 1);
            }
        }
    }

    public void setOnCrumbClickListener(IOnBreadCrumbListener mCrumbClickListener) {
        this.mCrumbStatesListener = mCrumbClickListener;
    }

    private int calculateWidth() {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        return getMeasuredWidth();
    }
}
