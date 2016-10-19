package com.scand.realmbrowser;

import java.lang.reflect.Field;

import io.realm.RealmObject;

/**
 * Created by Slabodeniuk on 6/18/15.
 */
public class RealmObjectHolder {
    private static RealmObjectHolder sInstance = new RealmObjectHolder();

    private RealmObject mObject;
    private Field mField;

    public static RealmObjectHolder getInstance() {
        return sInstance;
    }

    private RealmObjectHolder() {
    }

    public RealmObject getObject() {
        return mObject;
    }

    public void setObject(RealmObject object) {
        this.mObject = object;
    }

    public Field getField() {
        return mField;
    }

    public void setField(Field field) {
        this.mField = field;
    }
}
