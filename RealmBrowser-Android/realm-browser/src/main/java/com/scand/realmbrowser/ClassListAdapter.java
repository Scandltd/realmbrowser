package com.scand.realmbrowser;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Slabodeniuk on 6/16/15.
 */
class ClassListAdapter extends RecyclerView.Adapter<ClassListAdapter.ViewHolder> {

    interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView textView;
        final TextView recordsNumber;
        private final ClassListAdapter.OnItemClickListener mListener;

        public ViewHolder(View itemView, ClassListAdapter.OnItemClickListener listener) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.class_list_item_name);
            itemView.setOnClickListener(this);
            mListener = listener;
            recordsNumber = (TextView) itemView.findViewById(R.id.class_list_counter);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    private static class DataHolder {

        DataHolder(String className, int itemNumber) {
            this.className = className;
            this.itemNumber = itemNumber;
        }

        String className;
        int itemNumber;
    }

    private List<DataHolder> mData;
    private OnItemClickListener mItemClickListener;

    ClassListAdapter(@NonNull List<Class<? extends RealmObject>> data, OnItemClickListener listener) {
        if (mData == null)
            mData = new ArrayList<>(data.size());

        updateData(data);
        mItemClickListener = listener;
    }

    public void updateData(@NonNull List<Class<? extends RealmObject>> data) {
        mData.clear();
        for (Class<? extends RealmObject> clazz : data) {
            Realm realm = Realm.getInstance(RealmBrowser.getInstance().getRealmConfig(clazz));
            int recordsNumber = realm.where(clazz).findAll().size();
            mData.add(new DataHolder(clazz.getSimpleName(), recordsNumber));
            realm.close();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.realm_browser_class_list_item, viewGroup, false);

        return new ViewHolder(v, mItemClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        DataHolder holder = mData.get(position);
        viewHolder.textView.setText(holder.className);
        viewHolder.recordsNumber.setText(Integer.toString(holder.itemNumber));
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }
}
