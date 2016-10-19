package com.scand.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.scand.realmbrowser.RealmBrowser;
import com.scand.sample.database.Cat;
import com.scand.sample.database.DataTypeTest;
import com.scand.sample.database.DifferentFileObject;
import com.scand.sample.database.Dog;
import com.scand.sample.database.Person;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;

public class RealmBrowserSampleActivity extends AppCompatActivity {

    private static final String SECOND_REALM_NAME = "second.realm";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button displayBtn = (Button) findViewById(R.id.displayDb);
        displayBtn.setOnClickListener(mDisplayDbClickListener);

        Realm.init(this);

        RealmConfiguration firstConfig = new RealmConfiguration.Builder().build();

        RealmConfiguration secondRealmConfig = new RealmConfiguration.Builder()
                .name(SECOND_REALM_NAME)
                .build();

        List<Class<? extends RealmObject>> classes = new ArrayList<>();
        classes.add(Cat.class);
        classes.add(Dog.class);
        classes.add(Person.class);
        classes.add(DataTypeTest.class);

        new RealmBrowser.Builder(this)
                .add(firstConfig, classes)
                .add(secondRealmConfig, DifferentFileObject.class)
                .showNotification();
    }

    private final View.OnClickListener mDisplayDbClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context c = RealmBrowserSampleActivity.this;
            RealmConfiguration firstConfig = new RealmConfiguration.Builder().build();
            Realm realm = Realm.getInstance(firstConfig);

            RealmConfiguration secondRealmConfig = new RealmConfiguration.Builder()
                    .name(SECOND_REALM_NAME)
                    .build();
            Realm realmSecond = Realm.getInstance(secondRealmConfig);

            List<Class<? extends RealmObject>> classes = new ArrayList<>();
            classes.add(Cat.class);
            classes.add(Dog.class);
            classes.add(Person.class);
            classes.add(DataTypeTest.class);

            new RealmBrowser.Builder(c)
                    .add(realm, classes)
                    .add(realmSecond, DifferentFileObject.class)
                    .show();

        }
    };
}
