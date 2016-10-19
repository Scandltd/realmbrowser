package com.scand.realmbrowser;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import java.lang.reflect.Field;

/**
 * Created by Slabodeniuk on 7/2/15.
 */
public class FieldFilterPreferences {

    private static final String PREFS_FILE_NAME = "fieldsFilter.prefs";

    private static FieldFilterPreferences ourInstance;

    private SharedPreferences mPrefs;

    public static FieldFilterPreferences getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new FieldFilterPreferences(context);
        return ourInstance;
    }

    private FieldFilterPreferences(Context context) {
        mPrefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    void setFieldDisplayed(@NonNull Class clazz, @NonNull Field field, boolean isFieldDisplayed) {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(getFieldPrefKey(clazz, field), isFieldDisplayed);
        editor.commit();
    }

    boolean isFieldDisplayed(@NonNull Class clazz, @NonNull Field field) {
        String key = getFieldPrefKey(clazz, field);

        // All fields are showing by default
        return mPrefs.getBoolean(key, true);
    }

    private String getFieldPrefKey(@NonNull Class clazz, @NonNull Field field) {
        return clazz.getCanonicalName() + ":" + field.getName();
    }
}
