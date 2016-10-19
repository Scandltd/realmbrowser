package com.scand.sample.database.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Slabodeniuk on 6/25/15.
 */
public class DifferentFileObject extends RealmObject {

    private int intValue;
    private RealmList<Person> persons;
    private Person person;

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public RealmList<Person> getPersons() {
        return persons;
    }

    public void setPersons(RealmList<Person> persons) {
        this.persons = persons;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
