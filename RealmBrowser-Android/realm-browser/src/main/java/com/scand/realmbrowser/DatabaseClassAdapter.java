package com.scand.realmbrowser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scand.realmbrowser.view.RowView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

/**
 * Created by Slabodeniuk on 6/16/15.
 */
class DatabaseClassAdapter extends RecyclerView.Adapter<DatabaseClassAdapter.ItemViewHolder> {

    interface OnCellClickListener {
        void onCellClick(RealmObject obj, Field field, int position);
    }

    private List<Field> mFields;
    private List<? extends RealmObject> mData;

    private HorizontalScrollMediator mScrollMediator;
    private ColumnWidthMediator mWidthMediator;
    private OnCellClickListener mExternalCellClickListener;

//    DatabaseClassAdapter(Context context, Class<? extends RealmObject> clazz) {
//        this(context, clazz, RealmBrowser.getInstance().getRealm(clazz).allObjects(clazz));
//    }

    DatabaseClassAdapter(Context context, Class<? extends RealmObject> clazz,
                         List<? extends RealmObject> data) {
        mData = data;
        updateFields(context, clazz);
    }

    public void updateData(List<? extends RealmObject> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void updateDisplayedFields(Context context, Class<? extends RealmObject> clazz) {
        updateFields(context, clazz);
        notifyDataSetChanged();
    }

    public void updateDisplayedFields(Context context, Class<? extends RealmObject> clazz, List<? extends RealmObject> data) {
        mData = data;
        updateDisplayedFields(context, clazz);
    }

    private void updateFields(Context context, Class<? extends RealmObject> clazz) {
        mFields = new ArrayList<>();
        FieldFilterPreferences prefs = FieldFilterPreferences.getInstance(context);
        for (Field f : RealmUtils.getFields(clazz)) {
            if (prefs.isFieldDisplayed(clazz, f))
                mFields.add(f);
        }
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.realm_browser_database_class_item, parent, false);
        int colNmb = mFields.size();

        ItemViewHolder holder = new ItemViewHolder(v, colNmb);
        holder.mRowView.setCellsGravity(Gravity.CENTER_VERTICAL);
        holder.mRowView.setMinColumnHeight(
                (int) context.getResources().getDimension(R.dimen.realm_browser_database_list_item_min_height));
        //  adding this to list of views that should scroll synchronously
        mScrollMediator.addView(holder.mRowView);
        mWidthMediator.addView(holder.mRowView);
        holder.mRowView.setOnScrollChangedListener(mScrollMediator);

        return holder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        RealmObject obj = mData.get(position);

        if (mFields.size() != holder.mRowView.getColumnsNumber())
            holder.mRowView.setColumnsNumber(mFields.size());

        Context c = holder.mRowView.getContext();
        int backColorId = position % 2 == 0 ? android.R.color.white : R.color.realm_browser_lt_gray;
        for (int i = 0; i < mFields.size(); i++) {
            Field field = mFields.get(i);
            String value = RealmUtils.getFieldDisplayedName(obj, field);
            holder.mRowView.setColumnText(value, i);
            holder.mRowView.setColumnWidth(mWidthMediator.getColWidth(i), i);
        }

        holder.mRowView.post(new Runnable() {
            @Override
            public void run() {
                holder.mRowView.scrollTo(mScrollMediator.getScrollX(), mScrollMediator.getScrollY());
            }
        });
        holder.mRowView.setBackgroundColor(c.getResources().getColor(backColorId));
        holder.mRowView.setTag(position);
        holder.mRowView.setOnCellClickListener(mInternalCellClickListener);
        holder.mRowNumber.setText(Integer.toString(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    void setScrollMediator(HorizontalScrollMediator mediator) {
        mScrollMediator = mediator;
    }

    void setColumnWidthMediator(ColumnWidthMediator mediator) {
        mWidthMediator = mediator;
    }

    void setCellClickListener(OnCellClickListener listener) {
        mExternalCellClickListener = listener;
    }

    private final RowView.OnCellClickListener mInternalCellClickListener = new RowView.OnCellClickListener() {
        @Override
        public void onCellClick(RowView view, int position) {
            if (mExternalCellClickListener != null) {
                int row = (int) view.getTag();

                RealmObject object = mData.get(row);
                Field field = mFields.get(position);
                mExternalCellClickListener.onCellClick(object, field, row);
            }
        }
    };

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView mRowNumber;
        RowView mRowView;

        public ItemViewHolder(View itemView, int colNumber) {
            super(itemView);
            mRowNumber = (TextView) itemView.findViewById(R.id.db_class_list_row_number);
            mRowView = (RowView) itemView.findViewById(R.id.db_class_list_row);
            mRowView.setColumnsNumber(colNumber);
        }

    }
}
