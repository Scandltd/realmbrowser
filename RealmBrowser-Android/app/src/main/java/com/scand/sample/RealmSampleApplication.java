package com.scand.sample;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.crittercism.app.Crittercism;
import com.crittercism.app.CrittercismConfig;

/**
 * Created by amykh on 05.08.2015.
 */
public class RealmSampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        initCrittercism();
    }

    private void initCrittercism() {
        String versionName = "1.0";
        int versionCode = 1;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //do nothing
        }

        CrittercismConfig config = new CrittercismConfig();
        config.setCustomVersionName(versionName + "b" + versionCode);
        Crittercism.initialize(getApplicationContext(), getResources().getString(R.string.crittercism_id), config);
    }
}
