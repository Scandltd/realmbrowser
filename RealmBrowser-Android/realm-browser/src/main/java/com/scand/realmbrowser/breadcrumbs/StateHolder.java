package com.scand.realmbrowser.breadcrumbs;

import java.lang.reflect.Field;

import io.realm.RealmObject;

/**
 * Created by amykh on 04.08.2015.
 */
public class StateHolder {
    private String caption;
    private RealmObject obj;
    private Field field;

    public StateHolder(String caption, RealmObject obj, Field field) {
        this.caption = caption;
        this.obj = obj;
        this.field = field;
    }

    public String getCaption() {
        return caption;
    }

    public Field getField() {
        return field;
    }

    public RealmObject getObject() {
        return obj;
    }
}
