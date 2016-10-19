package com.scand.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.scand.realmbrowser.RealmBrowser;
import com.scand.sample.database.model.Cat;
import com.scand.sample.database.model.DataTypeTest;
import com.scand.sample.database.model.DifferentFileObject;
import com.scand.sample.database.model.Dog;
import com.scand.sample.database.model.Person;

import java.util.ArrayList;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObject;


public class MainActivity extends AppCompatActivity {

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
                .schemaVersion(5)
                .migration(mMigration)
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
            Context c = MainActivity.this;

            RealmConfiguration firstConfig = new RealmConfiguration.Builder().build();

            RealmConfiguration secondRealmConfig = new RealmConfiguration.Builder()
                    .name(SECOND_REALM_NAME)
                    .schemaVersion(5)
                    .migration(mMigration)
                    .build();

            List<Class<? extends RealmObject>> classes = new ArrayList<>();
            classes.add(Cat.class);
            classes.add(Dog.class);
            classes.add(Person.class);
            classes.add(DataTypeTest.class);

            new RealmBrowser.Builder(c)
                    .add(firstConfig, classes)
                    .add(secondRealmConfig, DifferentFileObject.class)
                    .show();
        }
    };

    RealmMigration mMigration = new RealmMigration() {
        @Override
        public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {
            Log.d("TEST", String.format("From %d to %d", oldVersion, newVersion));
        }
    };
}
