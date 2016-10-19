package com.scand.sample.database.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Created by Slabodeniuk on 6/18/15.
 */
public class DataTypeTest extends RealmObject {

    private boolean boolTest;
    private short shortTest;
    private int intTest;
    private long longTest;
    private float floatTest;
    private double doubleTest;
    private String strTest;
    private Date dateTest;
    private byte[] bytesTest;

    private Person realmObjTest;
    private RealmList<Cat> realmListTest;

    @Ignore
    @Deprecated
    private int hidden;

    public boolean isBoolTest() {
        return boolTest;
    }

    public void setBoolTest(boolean boolTest) {
        this.boolTest = boolTest;
    }

    public short getShortTest() {
        return shortTest;
    }

    public void setShortTest(short shortTest) {
        this.shortTest = shortTest;
    }

    public int getIntTest() {
        return intTest;
    }

    public void setIntTest(int intTest) {
        this.intTest = intTest;
    }

    public long getLongTest() {
        return longTest;
    }

    public void setLongTest(long longTest) {
        this.longTest = longTest;
    }

    public float getFloatTest() {
        return floatTest;
    }

    public void setFloatTest(float floatTest) {
        this.floatTest = floatTest;
    }

    public double getDoubleTest() {
        return doubleTest;
    }

    public void setDoubleTest(double doubleTest) {
        this.doubleTest = doubleTest;
    }

    public String getStrTest() {
        return strTest;
    }

    public void setStrTest(String strTest) {
        this.strTest = strTest;
    }

    public Date getDateTest() {
        return dateTest;
    }

    public void setDateTest(Date dateTest) {
        this.dateTest = dateTest;
    }

    public byte[] getBytesTest() {
        return bytesTest;
    }

    public void setBytesTest(byte[] bytesTest) {
        this.bytesTest = bytesTest;
    }

    public Person getRealmObjTest() {
        return realmObjTest;
    }

    public void setRealmObjTest(Person realmObjTest) {
        this.realmObjTest = realmObjTest;
    }

    public RealmList<Cat> getRealmListTest() {
        return realmListTest;
    }

    public void setRealmListTest(RealmList<Cat> realmListTest) {
        this.realmListTest = realmListTest;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }
}
