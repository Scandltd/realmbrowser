package com.scand.realmbrowser;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;

/**
 * Created by Slabodeniuk on 6/15/15.
 */
public class RealmBrowser {

    private static RealmBrowser sInstance = new RealmBrowser();

    private Map<Class<? extends RealmObject>, RealmConfiguration> displayedRealmConfigs;
    private List<RealmConfiguration> activeRealmConfigs;

    static RealmBrowser getInstance() {
        return sInstance;
    }

    private RealmBrowser() {
        displayedRealmConfigs = new HashMap<>();
        activeRealmConfigs = new ArrayList<>();
    }

    List<Class<? extends RealmObject>> getDisplayedRealmObjects(RealmConfiguration realm) {
        List<Class<? extends RealmObject>> result = new ArrayList<>();
        for (Class<? extends RealmObject> clazz : displayedRealmConfigs.keySet()) {
            RealmConfiguration config = displayedRealmConfigs.get(clazz);
            if (config.getPath().equals(realm.getPath())) {
                result.add(clazz);
            }
        }
        Collections.sort(result, new Comparator<Class<? extends RealmObject>>() {
            @Override
            public int compare(Class<? extends RealmObject> lhs, Class<? extends RealmObject> rhs) {
                return lhs.getSimpleName().compareTo(rhs.getSimpleName());
            }
        });
        return result;
    }

    RealmConfiguration getRealmConfig(Class<? extends RealmObject> clazz) {
        return displayedRealmConfigs.get(clazz);
    }

    List<RealmConfiguration> getActiveRealmConfigs() {
        return activeRealmConfigs;
    }

    private void addRealmConfig(RealmConfiguration config) {
        if (activeRealmConfigs.isEmpty()) {
            activeRealmConfigs.add(config);
        } else {
            for (RealmConfiguration configuration : activeRealmConfigs) {
                if (configuration.getPath().equals(config.getPath())) {
                    return;
                }
            }
            activeRealmConfigs.add(config);
        }
    }

    public static class Builder {
        private Context mContext;
        private RealmBrowser mBrowser;

        public Builder(Context c) {
            mContext = c;
            mBrowser = RealmBrowser.getInstance();
        }

        public Builder add(Realm realm, List<Class<? extends RealmObject>> classes) {
            for (Class<? extends RealmObject> clazz : classes) {
                add(realm, clazz);
            }
            return this;
        }

        public Builder add(Realm realm, Class<? extends RealmObject> clazz) {
            add(realm.getConfiguration(), clazz);
            return this;
        }

        public Builder add(RealmConfiguration config, List<Class<? extends RealmObject>> classes) {
            for (Class<? extends RealmObject> clazz : classes) {
                add(config, clazz);
            }
            return this;
        }

        public Builder add(RealmConfiguration config, Class<? extends RealmObject> clazz) {
            mBrowser.displayedRealmConfigs.put(clazz, config);
            mBrowser.addRealmConfig(config);
            return this;
        }

        public void show() {
            BrowserActivity.startActivity(mContext);
        }

        public void showNotification() {
            RealmBrowserService.startService(mContext);
        }
    }
}
