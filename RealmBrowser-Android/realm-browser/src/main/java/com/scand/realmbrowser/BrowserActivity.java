package com.scand.realmbrowser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.scand.realmbrowser.breadcrumbs.StateHolder;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;

/**
 * Created by Slabodeniuk on 3/30/16.
 */
public class BrowserActivity extends AppCompatActivity implements
        DbConfigBrowserFragment.DbConfigInteraction,
        DbTableFragment.DbTableInteraction,
        EditDialogFragment.OnFieldEditDialogInteraction,
        FieldFilterDialogFragment.FieldFilterDialogInteraction {

    private static String STATE_DRAWER_LOCKED = BrowserActivity.class.getSimpleName() + "_drawer_locked";
    private static String STATE_CURRENT_CLASS = BrowserActivity.class.getSimpleName() + "_ class_name";

    private Toolbar mToolbar;

    private DbTableFragment mDbTableFragment;
    private DrawerLayout mDrawer;

    private RetainFragment mRetainFragment;

    private boolean isDrawerLocked = true;

    private Realm mRealm;
    // mSelectedClass variable is necessary to restore instance of Realm
    private Class<? extends RealmObject> mSelectedClass;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, BrowserActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.realm_browser_browser_activity);

        mRetainFragment = getRetainFragment();
        if (mRetainFragment == null) {
            mRetainFragment = new RetainFragment();
            getSupportFragmentManager().beginTransaction().add(mRetainFragment, RetainFragment.TAG).commit();
        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDbTableFragment = (DbTableFragment) getSupportFragmentManager().findFragmentById(R.id.table_fragment);

        if (savedInstanceState != null) {
            isDrawerLocked = savedInstanceState.getBoolean(STATE_DRAWER_LOCKED);
            // set drawer to "locked" state if mRetainFragment doesn't has saved data
            // activity was completely destroyed
            if (getNavigationStates() == null) {
                isDrawerLocked = true;
            }
            String className = savedInstanceState.getString(STATE_CURRENT_CLASS);
            if (className != null) {
                try {
                    //noinspection unchecked
                    mSelectedClass = (Class<? extends RealmObject>) Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                mRealm = Realm.getInstance(RealmBrowser.getInstance().getRealmConfig(mSelectedClass));
            }
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, 0, 0);
        mDrawer.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (isDrawerLocked)
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_DRAWER_LOCKED, isDrawerLocked);
        if (mSelectedClass != null)
            outState.putString(STATE_CURRENT_CLASS, mSelectedClass.getCanonicalName());
    }

    @Override
    public void onClassSelected(Class<? extends RealmObject> clazz) {
        isDrawerLocked = false;
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDrawer.closeDrawers();

        RealmConfiguration clazzConfig = RealmBrowser.getInstance().getRealmConfig(clazz);
        if (mRealm != null && !mRealm.getConfiguration().equals(clazzConfig)) {
            mRealm.close();
        }
        mRealm = Realm.getInstance(clazzConfig);
        mDbTableFragment.setClass(clazz);

        mSelectedClass = clazz;
    }

    @Override
    public void onFieldListChange() {
        mDbTableFragment.onFieldListChange();
    }

    @Override
    public void onRowWasEdit(int position) {
        mDbTableFragment.onRowWasEdit(position);
    }

    private RetainFragment getRetainFragment() {
        RetainFragment result = mRetainFragment != null ?
                mRetainFragment :
                (RetainFragment) getSupportFragmentManager().findFragmentByTag(RetainFragment.TAG);

        mRetainFragment = result;

        return mRetainFragment;
    }

    @Override
    public List<StateHolder> getNavigationStates() {
        return getRetainFragment().getNavigationStates();
    }

    @Override
    public void saveNavigationStates(List<StateHolder> states) {
        getRetainFragment().setNavigationStates(states);
    }

    @Override
    public Realm getRealm() {
        return mRealm;
    }

    public static class RetainFragment extends Fragment {

        static final String TAG = RetainFragment.class.getSimpleName() + "_tag";

        List<StateHolder> mNavigationStates;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }

        void setNavigationStates(List<StateHolder> states) {
            mNavigationStates = states;
        }

        List<StateHolder> getNavigationStates() {
            return mNavigationStates;
        }

    }
}
