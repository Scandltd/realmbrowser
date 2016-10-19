package com.scand.realmbrowser;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.scand.realmbrowser.breadcrumbs.BreadCrumbsView;
import com.scand.realmbrowser.breadcrumbs.IOnBreadCrumbListener;
import com.scand.realmbrowser.breadcrumbs.StateHolder;
import com.scand.realmbrowser.view.DragOverlayView;
import com.scand.realmbrowser.view.RowView;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Slabodeniuk on 3/31/16.
 */
public class DbTableFragment extends Fragment implements
        DatabaseClassAdapter.OnCellClickListener,
        EditDialogFragment.OnFieldEditDialogInteraction,
        FieldFilterDialogFragment.FieldFilterDialogInteraction,
        IOnBreadCrumbListener,
        ColumnWidthMediator.ColumnWidthProvider {

    interface DbTableInteraction {
        List<StateHolder> getNavigationStates();

        void saveNavigationStates(List<StateHolder> states);

        Realm getRealm();
    }

    private static final String LOG_TAG = DbTableFragment.class.getSimpleName();

    private BreadCrumbsView mCrumbsList;
    private RecyclerView mList;
    private DatabaseClassAdapter mAdapter;
    private RowView mTableHeader;
    private RowView mTableHeaderType;
    private View mHintGroup;

    private DragOverlayView mDragView;

    private MenuItem mSearchItem;
    private ActionBar mActionBar;
    private Class<? extends RealmObject> mClazz;

    private SpanHolder mSpanHolder = new SpanHolder();

    private HorizontalScrollMediator mScrollMediator;
    private ColumnWidthMediator mColWidthMediator;

    private DbTableInteraction mListener;

    // this variable is used to provide search throw data
    // which list was originally filled
    private List<RealmObject> mOriginalData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DbTableInteraction) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Context " +
                    context == null ? "null" : context.toString() +
                    "should implement " +
                    DbTableInteraction.class.getSimpleName() +
                    " interface!");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.realm_browser_fragment_db_table, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViews(view);
        init();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        List<StateHolder> states = mListener.getNavigationStates();
        if (states != null) {
            mCrumbsList.setCrumbStates(states);
        }
    }

    private void findViews(View view) {
        mTableHeader = (RowView) view.findViewById(R.id.table_header);
        mTableHeaderType = (RowView) view.findViewById(R.id.table_header_type);
        mList = (RecyclerView) view.findViewById(R.id.databaseList);
        mHintGroup = view.findViewById(R.id.invalid_request_hint_group);
        mCrumbsList = (BreadCrumbsView) view.findViewById(R.id.crumbs_list);
        mDragView = (DragOverlayView) view.findViewById(R.id.drag_view);
    }

    private void init() {
        mList.setLayoutManager(new LinearLayoutManager(getContext()));
        mCrumbsList.setOnCrumbClickListener(this);

        mColWidthMediator = new ColumnWidthMediator(mDragView, this);
        mColWidthMediator.addView(mTableHeader);
        mColWidthMediator.addView(mTableHeaderType);

        mTableHeader.setOnColumnWidthChangeListener(mColWidthMediator);
        mTableHeaderType.setOnColumnWidthChangeListener(mColWidthMediator);

        mScrollMediator = new HorizontalScrollMediator();
        mScrollMediator.addView(mTableHeader);
        mScrollMediator.addView(mTableHeaderType);
    }


    public void setClass(Class<? extends RealmObject> clazz) {
        fillTable(clazz, true);
        mCrumbsList.addCrumb(new StateHolder(clazz.getCanonicalName(), null, null));
    }

    private void fillTable(Class<? extends RealmObject> clazz, boolean resetBreadcrumbs) {
        mClazz = clazz;
        if (resetBreadcrumbs)
            mCrumbsList.clearCrumbs();

        //noinspection unchecked
        mOriginalData = (List<RealmObject>) mListener.getRealm().where(mClazz).findAll();
        updateData(mOriginalData);
    }

    private void fillTable(RealmObject realmObj, Field field) {

        if (RealmUtils.isFieldRealmList(field)) {
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            //noinspection unchecked
            mClazz = (Class<? extends RealmObject>) pType.getActualTypeArguments()[0];
            mOriginalData = RealmUtils.getRealmListFieldValue(realmObj, field);
        } else if (RealmUtils.isFieldRealmObject(field)) {
            //noinspection unchecked
            mClazz = (Class<? extends RealmObject>) field.getType();
            mOriginalData = new ArrayList<>(1);
            mOriginalData.add(RealmUtils.getRealmObjectFieldValue(realmObj, field));
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + field);
        }

        updateData(mOriginalData);
    }

    private void updateData(List<? extends RealmObject> data) {

        initTableHeader(mClazz);

        if (mAdapter == null) {
            if (data == null) {
                data = mListener.getRealm().where(mClazz).findAll();
                mAdapter = new DatabaseClassAdapter(getContext(), mClazz, data);
            } else {
                mAdapter = new DatabaseClassAdapter(getContext(), mClazz, data);
            }

            mAdapter.setScrollMediator(mScrollMediator);
            mAdapter.setColumnWidthMediator(mColWidthMediator);
            mAdapter.setCellClickListener(this);

            mList.setAdapter(mAdapter);
            mList.getRecycledViewPool().setMaxRecycledViews(0, 40);
        }

        if (data != null)
            mAdapter.updateDisplayedFields(getContext(), mClazz, data);
        else
            mAdapter.updateDisplayedFields(getContext(), mClazz);

        if (mActionBar != null) {
            mActionBar.setTitle(String.format("%s (%d)", mClazz.getSimpleName(), mAdapter.getItemCount()));
        }
    }

    @Override
    public void onStateChanged(StateHolder state) {
        if (state.getObject() != null && state.getField() != null) {
            fillTable(state.getObject(), state.getField());
        } else {
            if (state.getCaption() != null) {
                try {
                    //noinspection unchecked
                    fillTable((Class<? extends RealmObject>) Class.forName(state.getCaption()), false);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.realm_browser_database_class_menu, menu);

        mSearchItem = menu.findItem(R.id.database_search);
        SearchView searchView = (SearchView) mSearchItem.getActionView();
        searchView.setOnQueryTextListener(mSearchListener);
        searchView.setQueryHint(getString(R.string.realm_browser_search_hint_short));

        // This code is crunch to fix close button behavior
        View searchButton = searchView.findViewById(R.id.search_close_btn);
        if (searchButton != null) {
            searchButton.setEnabled(false);
            searchButton.setAlpha(0f);
        }
        // end close button fix

        MenuItemCompat.setOnActionExpandListener(mSearchItem, mSearchExpandListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.field_filter) {
            FieldFilterDialogFragment.createInstance(mClazz).show(getChildFragmentManager(),
                    FieldFilterDialogFragment.class.getSimpleName());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListener.saveNavigationStates(mCrumbsList.getCrumbStates());
    }

    private void initTableHeader(Class<? extends RealmObject> clazz) {

        List<Field> fields = new ArrayList<>();
        FieldFilterPreferences prefs = FieldFilterPreferences.getInstance(getContext());
        for (Field f : RealmUtils.getFields(clazz)) {
            if (prefs.isFieldDisplayed(clazz, f))
                fields.add(f);
        }
        mTableHeader.setColumnsNumber(fields.size());
        mTableHeader.setCellsGravity(Gravity.BOTTOM);
        mTableHeader.showDividers(true);

        mTableHeaderType.setColumnsNumber(fields.size());
        mTableHeaderType.setTextAppearance(R.style.realm_browser_database_column_title_type_style);
        mTableHeaderType.showDividers(true);

        int fieldCount = fields.size();
        for (int i = 0; i < fieldCount; i++) {
            Field f = fields.get(i);

            SpannableStringBuilder headerNameBuilder = new SpannableStringBuilder();

            addRealmAnnotations(f, headerNameBuilder);

            int start = headerNameBuilder.length();
            headerNameBuilder.append(f.getName());
            headerNameBuilder.setSpan(mSpanHolder.getHeaderStyle(getContext()),
                    start,
                    headerNameBuilder.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            mTableHeader.setColumnText(headerNameBuilder.subSequence(0, headerNameBuilder.length()), i);
            mTableHeaderType.setColumnText(f.getType().getSimpleName(), i);
        }
    }

    @Override
    public void onCellClick(RealmObject obj, Field field, int position) {
        Class<? extends RealmObject> clazz;
        if (RealmUtils.isFieldRealmObject(field)) {
            //noinspection unchecked
            clazz = (Class<? extends RealmObject>) field.getType();
        } else if (RealmUtils.isFieldRealmList(field)) {
            ParameterizedType pType = (ParameterizedType) field.getGenericType();
            //noinspection unchecked
            clazz = (Class<? extends RealmObject>) pType.getActualTypeArguments()[0];
        } else {
            clazz = null;
        }

        if (RealmUtils.isFieldRealmObject(field)
                && RealmUtils.getRealmObjectFieldValue(obj, field) == null) {
            // if RealmObj.field == null ignore click
            return;
        }

        // collapse search action view
        MenuItemCompat.collapseActionView(mSearchItem);

        RealmConfiguration fieldRealmConfig = RealmBrowser.getInstance().getRealmConfig(clazz);
        Class objClass = obj.getClass().getSuperclass();
        //noinspection unchecked
        RealmConfiguration objRealmConfig = RealmBrowser.getInstance().getRealmConfig(objClass);
        if (clazz == null
                || (fieldRealmConfig != null && fieldRealmConfig.getPath().equals(objRealmConfig.getPath()))) {
            if (RealmUtils.isFieldRealmList(field) ||
                    RealmUtils.isFieldRealmObject(field)) {
                fillTable(obj, field);
                mCrumbsList.addCrumb(new StateHolder(mClazz.getCanonicalName(), obj, field));
            } else {
                EditDialogFragment.createInstance(obj, field, position)
                        .show(getChildFragmentManager(), EditDialogFragment.class.getSimpleName());
            }
        } else {
            if (fieldRealmConfig == null) {
                showInvalidClassInfoDialog(clazz);
            } else {
                showInvalidFileInfoDialog(clazz, fieldRealmConfig);
            }
        }
    }

    private void showInvalidClassInfoDialog(Class<? extends RealmObject> clazz) {
        String messagePattern = getResources().getString(R.string.realm_browser_realm_class_notification);
        new AlertDialog.Builder(getContext())
                .setMessage(String.format(messagePattern, clazz.getSimpleName()))
                .setPositiveButton(R.string.realm_browser_ok, null)
                .show();
    }

    private void showInvalidFileInfoDialog(Class<? extends RealmObject> clazz, RealmConfiguration fieldRealmConfig) {
        String messagePattern = getResources().getString(R.string.realm_browser_realm_file_notification);
        String fileName = new File(fieldRealmConfig.getPath()).getName();
        new AlertDialog.Builder(getContext())
                .setMessage(String.format(messagePattern, clazz.getSimpleName(), fileName))
                .setPositiveButton(R.string.realm_browser_ok, null)
                .show();
    }

    private void addRealmAnnotations(Field f, SpannableStringBuilder builder) {
        List<Class<? extends Annotation>> classes = new ArrayList<>(3);
        classes.add(PrimaryKey.class);
        classes.add(Ignore.class);
        classes.add(Index.class);
        for (Class<? extends Annotation> c : classes) {
            if (f.isAnnotationPresent(c)) {
                addAnnotationName(builder, f.getAnnotation(c));
            }
        }
    }

    private void addAnnotationName(SpannableStringBuilder builder, Annotation a) {
        int start = builder.length();
        builder.append("@");
        builder.append(a.annotationType().getSimpleName());
        builder.setSpan(mSpanHolder.getHeaderAnnotationStyle(getContext()),
                start,
                builder.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(System.getProperty("line.separator"));
    }

    @Override
    public void onRowWasEdit(int position) {
        RecyclerView.Adapter adapter = mList.getAdapter();
        if (adapter != null) {
            adapter.notifyItemChanged(position);
        }
    }

    private MenuItemCompat.OnActionExpandListener mSearchExpandListener = new MenuItemCompat.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            Log.d(LOG_TAG, "onMenuItemActionCollapse() call");
            setSearchHintVisible(false);
            mAdapter.updateData(mOriginalData);
            return true;
        }
    };

    private SearchView.OnQueryTextListener mSearchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            Log.d(LOG_TAG, "onQueryTextSubmit: " + query);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Log.d(LOG_TAG, "onQueryTextChange: " + newText);
            if (!TextUtils.isEmpty(newText))
                makeSearchRequest(newText);
            return false;
        }
    };

    private void makeSearchRequest(@NonNull String query) {
        int separatorIndex = query.indexOf(':');
        if (separatorIndex != -1) {
            // search through entered field
            String fieldName = query.substring(0, separatorIndex).trim();
            String value = query.length() - 1 > separatorIndex ?
                    query.substring(separatorIndex + 1, query.length()).trim() :
                    "";

            Log.d(LOG_TAG, "fieldName: " + fieldName + "; value: " + value);
            try {
                Field field = mClazz.getDeclaredField(fieldName);

                RealmQuery<? extends RealmObject> realmQuery;
                if (mOriginalData instanceof RealmResults) {
                    //noinspection unchecked
                    realmQuery = ((RealmResults) mOriginalData).where();
                } else if (mOriginalData instanceof RealmList) {
                    //noinspection unchecked
                    realmQuery = ((RealmList) mOriginalData).where();
                } else {
                    processInvalidQuery(query);
                    return;
                }

                Class<?> type = field.getType();
                if (type == String.class) {
                    realmQuery.contains(fieldName, value);
                } else if (type == Boolean.class || type == boolean.class) {
                    // this 'if else"'s are necessary because Boolean.valueOf(value) returns "false"
                    // for any String not equals to "true"
                    boolean boolValue;
                    if ("true".equalsIgnoreCase(value)) {
                        boolValue = true;
                    } else if ("false".equalsIgnoreCase(value)) {
                        boolValue = false;
                    } else {
                        processInvalidQuery(query);
                        return;
                    }
                    realmQuery.equalTo(fieldName, boolValue);
                } else if (type == Short.class || type == short.class) {
                    try {
                        realmQuery.equalTo(fieldName, Short.valueOf(value));
                    } catch (NumberFormatException e) {
                        processInvalidQuery(query);
                        return;
                    }
                } else if (type == Integer.class || type == int.class) {
                    try {
                        realmQuery.equalTo(fieldName, Integer.valueOf(value));
                    } catch (NumberFormatException e) {
                        processInvalidQuery(query);
                        return;
                    }
                } else if (type == Long.class || type == long.class) {
                    try {
                        realmQuery.equalTo(fieldName, Long.valueOf(value));
                    } catch (NumberFormatException e) {
                        processInvalidQuery(query);
                        return;
                    }
                } else if (type == Float.class || type == float.class) {
                    try {
                        realmQuery.equalTo(fieldName, Float.valueOf(value));
                    } catch (NumberFormatException e) {
                        processInvalidQuery(query);
                        return;
                    }
                } else if (type == Double.class || type == double.class) {
                    try {
                        realmQuery.equalTo(fieldName, Double.valueOf(value));
                    } catch (NumberFormatException e) {
                        processInvalidQuery(query);
                        return;
                    }
                } else if (type == Date.class) {
                    // TODO implement
                    processInvalidQuery(query);
                    return;
                } else if (type == Byte[].class || type == byte[].class) {
                    // TODO implement
                    processInvalidQuery(query);
                    return;
                } else if (RealmObject.class.isAssignableFrom(type)) {
                    // TODO implement
                    processInvalidQuery(query);
                    return;
                } else if (RealmList.class.isAssignableFrom(type)) {
                    // TODO implement
                    processInvalidQuery(query);
                    return;
                } else {
                    // TODO process invalid value type
                    Toast.makeText(getContext(), "Invalid value type:" + type, Toast.LENGTH_SHORT).show();
                    processInvalidQuery(query);
                    return;
                }

                List<? extends RealmObject> result = realmQuery.findAll();

                setSearchHintVisible(false);

                mAdapter.updateData(result);
            } catch (NoSuchFieldException e) {
                processInvalidQuery(query);
            }
        } else {
            processInvalidQuery(query);
        }
    }

    private void processInvalidQuery(@NonNull String query) {
        Log.d(LOG_TAG, "processInvalidQuery: " + query);
        setSearchHintVisible(true);
    }

    private void setSearchHintVisible(boolean isHintVisible) {
        mList.setVisibility(isHintVisible ? View.GONE : View.VISIBLE);
        mHintGroup.setVisibility(isHintVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onFieldListChange() {
        initTableHeader(mClazz);
        mAdapter.updateDisplayedFields(getContext(), mClazz);
    }

    @Override
    public int getColumnWidth(int position) {
        return mTableHeader.getColumnWidth(position);
    }

    private class SpanHolder {
        private TextAppearanceSpan headerAnnotationStyle;
        private TextAppearanceSpan headerStyle;

        public TextAppearanceSpan getHeaderAnnotationStyle(Context c) {
            if (headerAnnotationStyle == null) {
                headerAnnotationStyle = new TextAppearanceSpan(c,
                        R.style.realm_browser_database_column_title_annotation_style);
            }
            return headerAnnotationStyle;
        }

        public TextAppearanceSpan getHeaderStyle(Context c) {
            if (headerStyle == null) {
                headerStyle = new TextAppearanceSpan(c, R.style.realm_browser_database_column_title_style);
            }
            return headerStyle;
        }
    }

}
