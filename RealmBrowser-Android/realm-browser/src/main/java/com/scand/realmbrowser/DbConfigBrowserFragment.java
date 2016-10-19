package com.scand.realmbrowser;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by Slabodeniuk on 3/30/16.
 */
public class DbConfigBrowserFragment extends Fragment {

    interface DbConfigInteraction {
        void onClassSelected(Class<? extends RealmObject> clazz);
    }

    private static final String SAVED_STATE_SELECTED_FILE_POS = "selected file position";

    private static final int LOADER_ID_FILL_DB = 1;

    private Spinner mFileNameSpinner;
    private TextView mFileNameText;
    private TextView mFileSize;
    private TextView mFilePath;
    private View mFillDataBtn;

    private RecyclerView mClassList;
    private ClassListAdapter mClassListAdapter;

    private List<RealmConfiguration> mRealmConfigurations;
    List<Class<? extends RealmObject>> mClasses;
    private int mSelectedFilePosition = 0;

    private DbConfigInteraction mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (DbConfigInteraction) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Context " +
                    context == null ? "null" : context.toString() +
                    "should implement " +
                    DbConfigInteraction.class.getSimpleName() +
                    " interface!");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.realm_browser_fragment_db_config, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        findViews(view);
        setListeners();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mSelectedFilePosition = savedInstanceState.getInt(SAVED_STATE_SELECTED_FILE_POS);
        }

        mRealmConfigurations = RealmBrowser.getInstance().getActiveRealmConfigs();
        initUI();

        getLoaderManager().initLoader(LOADER_ID_FILL_DB,
                FillDbLoader.getArgsFromSelectedConfigPosition(mSelectedFilePosition),
                mFillDbCallbacks);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_STATE_SELECTED_FILE_POS, mSelectedFilePosition);
    }

    private void findViews(View view) {
        mFillDataBtn = view.findViewById(R.id.fill_btn);

        mClassList = (RecyclerView) view.findViewById(R.id.class_list);
        mFileNameSpinner = (Spinner) view.findViewById(R.id.file_name_spinner);
        mFileNameText = (TextView) view.findViewById(R.id.file_name);
        mFileSize = (TextView) view.findViewById(R.id.file_size);
        mFilePath = (TextView) view.findViewById(R.id.file_path);
    }

    private void setListeners() {
        mFillDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loader<Void> loader = getLoaderManager().restartLoader(LOADER_ID_FILL_DB,
                        FillDbLoader.getArgsFromSelectedConfigPosition(mSelectedFilePosition),
                        mFillDbCallbacks);
                loader.forceLoad();
                showProgress();
            }
        });
    }

    private void initUI() {

        List<String> names = new ArrayList<>(mRealmConfigurations.size());
        for (RealmConfiguration realmConfiguration : mRealmConfigurations) {
            File f = new File(realmConfiguration.getPath());
            names.add(f.getName());
        }

        if (names.size() > 1) {
            mFileNameSpinner.setVisibility(View.VISIBLE);
            mFileNameText.setVisibility(View.GONE);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    R.layout.realm_browser_class_list_file_spinner,
                    names);
            mFileNameSpinner.setAdapter(adapter);
            mFileNameSpinner.setOnItemSelectedListener(mSpinnerSelectedListener);
            mFileNameSpinner.setSelection(mSelectedFilePosition);
        } else if (names.size() > 0) {
            mFileNameSpinner.setVisibility(View.GONE);
            mFileNameText.setVisibility(View.VISIBLE);

            mFileNameText.setText(names.get(0));
            mSelectedFilePosition = 0;

            updateUIData();
        }

        mClassList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateUIData() {
        RealmConfiguration realmConfiguration = mRealmConfigurations.get(mSelectedFilePosition);
        File file = new File(realmConfiguration.getPath());
        mFilePath.setText(file.getParent());
        mFileSize.setText(Long.toString(file.length() / 1024) + " Kb");

        mClasses = RealmBrowser.getInstance()
                .getDisplayedRealmObjects(realmConfiguration);

        if (mClassListAdapter == null) {
            mClassListAdapter = new ClassListAdapter(mClasses, mListItemClickListener);
            mClassList.setAdapter(mClassListAdapter);
        } else {
            mClassListAdapter.updateData(mClasses);
            mClassListAdapter.notifyDataSetChanged();
        }
    }

    private final AdapterView.OnItemSelectedListener mSpinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mSelectedFilePosition = position;
            updateUIData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // nothing to do here
        }
    };

    private final ClassListAdapter.OnItemClickListener mListItemClickListener = new ClassListAdapter.OnItemClickListener() {

        @Override
        public void onItemClick(View v, int position) {
            Class<? extends RealmObject> clazz = mClasses.get(position);
            mListener.onClassSelected(clazz);
        }
    };

    private LoaderManager.LoaderCallbacks<Void> mFillDbCallbacks = new LoaderManager.LoaderCallbacks<Void>() {
        @Override
        public Loader<Void> onCreateLoader(int id, Bundle args) {
            return new FillDbLoader(DbConfigBrowserFragment.this.getContext(), args);
        }

        @Override
        public void onLoadFinished(Loader<Void> loader, Void data) {
            updateUIData();
            hideProgress();
        }

        @Override
        public void onLoaderReset(Loader<Void> loader) {
            updateUIData();
            hideProgress();
        }
    };

    private void showProgress() {
        new ProgressDialogFragment().show(getChildFragmentManager(), ProgressDialogFragment.PROGRESS_DIALOG_TAG);
    }

    private void hideProgress() {
        ProgressDialogFragment dialogFragment = (ProgressDialogFragment) getChildFragmentManager().findFragmentByTag(ProgressDialogFragment.PROGRESS_DIALOG_TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
    }

    static class FillDbLoader extends AsyncTaskLoader<Void> {

        static final String ARGS_CONFIG_POSITION = "config_position";

        private int mConfigPosition;

        static Bundle getArgsFromSelectedConfigPosition(int position) {
            Bundle args = new Bundle();
            args.putInt(ARGS_CONFIG_POSITION, position);
            return args;
        }

        FillDbLoader(Context context, Bundle args) {
            super(context);
            mConfigPosition = args.getInt(ARGS_CONFIG_POSITION);
            Log.d("TAG", "loader: config position " + mConfigPosition);
        }

        @Override
        public Void loadInBackground() {
            RealmConfiguration config = RealmBrowser.getInstance().getActiveRealmConfigs().get(mConfigPosition);
            Realm realm = Realm.getInstance(config);

            List<Class<? extends RealmObject>> mClasses = RealmBrowser.getInstance().getDisplayedRealmObjects(config);

            for (Class<? extends RealmObject> clazz : mClasses) {
                RealmUtils.clearClassData(realm, clazz);
            }
            //  two loops because of generate data can generate data before class turn.
            //      (when that class list is a field of another one)
            for (Class<? extends RealmObject> clazz : mClasses) {
                RealmResults<?> rows = realm.where(clazz).findAll();
                if (rows.size() >= RealmUtils.DEFAULT_ROW_COUNT)
                    continue;
                RealmUtils.generateData(realm, clazz, RealmUtils.DEFAULT_ROW_COUNT);
            }

            realm.close();
            return null;
        }
    }
}
