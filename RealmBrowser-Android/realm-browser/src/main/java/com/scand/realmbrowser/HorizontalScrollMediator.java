package com.scand.realmbrowser;

import com.scand.realmbrowser.view.RowView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Slabodeniuk on 3/31/16.
 */
public class HorizontalScrollMediator implements RowView.OnScrollChangedListener {

    private List<RowView> mViews = new ArrayList<>();
    private int mScrollX, mScrollY;

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {

        if (mScrollX == l) return;

        mScrollX = l;
        mScrollY = t;

        for (RowView v : mViews) {
            v.scrollTo(l, t);
        }
    }

    public int getScrollX() {
        return mScrollX;
    }

    public int getScrollY() {
        return mScrollY;
    }

    public void addView(RowView v) {
        v.setOnScrollChangedListener(this);
        mViews.add(v);
    }

    public void removeAllViews() {
        mViews.clear();
    }
}
