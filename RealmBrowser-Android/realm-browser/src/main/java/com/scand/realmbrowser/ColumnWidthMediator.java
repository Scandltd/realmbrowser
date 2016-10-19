package com.scand.realmbrowser;

import android.view.View;

import com.scand.realmbrowser.view.DragOverlayView;
import com.scand.realmbrowser.view.RowView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Slabodeniuk on 3/31/16.
 */
public class ColumnWidthMediator implements
        RowView.OnColumnWidthChangeListener,
        DragOverlayView.OnDragFinished {

    interface ColumnWidthProvider {
        int getColumnWidth(int position);
    }

    private int colPosition;
    private int colLeft;
    private DragOverlayView dragOverlayView;
    private ColumnWidthProvider mColumnWidthProvider;

    private List<RowView> mViews = new ArrayList<>();

    ColumnWidthMediator(DragOverlayView view, ColumnWidthProvider widthProvider) {
        dragOverlayView = view;
        dragOverlayView.setOnDragFinishedListener(this);
        mColumnWidthProvider = widthProvider;
    }

    @Override
    public void startColumnWidthChange(int minX, int currentLeft, int currentRight, int position) {

        colLeft = currentLeft;
        colPosition = position;
        dragOverlayView.setMinLeft(minX);
        dragOverlayView.setShadowPosition(currentRight);
        dragOverlayView.startDrag(null,
                // create empty drag shadow
                // real shadow will be drawn by DragOverlayView itself
                new View.DragShadowBuilder(),
                null,
                0);
    }

    @Override
    public void onDragFinished(int position) {
        int newWidth = position - colLeft;

        for (RowView v : mViews) {
            v.setColumnWidth(newWidth, colPosition);
        }
    }

    public int getColWidth(int position) {
        return mColumnWidthProvider.getColumnWidth(position);
    }

    public void addView(RowView v) {
        mViews.add(v);
    }

    public void removeAllViews() {
        mViews.clear();
    }
}