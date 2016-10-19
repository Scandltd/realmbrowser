<p>Realm Browser Library is a small, but very helpful library designed for viewing and editing Realm database files on Android devices. Mobile developers using the Realm in their applications are able to view stored data faster and easier, and debug modules that work with the database. The library also provides the ability to generate data automatically.
Nowadays the official Realm browser is only available for Mac OS X users. Moreover, it is necessary to copy the database file from user’s device to a PC. Using Realm Browser Library developers have an access to stored data directly in the program without having to copy the data file.</p>
<p>There are two ways to open the Realm Browser: developers can create a notification or call the appropriate method at the right time. The first method prevents interference in your app's user interface, the second method provides necessary flexibility.</p>

<p><img alt="Realm" src="http://scand.com/products/realmbrowser/images/realm-android.png" border="0" align="center"></p>

<h2>Creating notifications</h2>
<p>The code is recommended to call during the application initialization.</p>

<pre class="brush: java; gutter: true; toolbar: false;">// get Realm (use the same way as in application)
Realm realm = Realm.getInstance(this);

new RealmBrowser.Builder(this)
// add class, you want to view
        .add(realm, Person.class)
// call method showNotification()
        .showNotification();
</pre>

<p>Note: Using Gradle to build the project, developer is able to show a notification only in case of debug-version, Realm Browser notification might not be created in the release-version. Q.v. <a href="https://developer.android.com/tools/building/configuring-gradle.html#workBuildVariants">Work with build variants</a>.</p>

<h2 class="no-border">Direct Call</h2>
<p>RealmBrowser initialization is identical to the example of creation of the notification, except calling show() instead of showNotification().</p>

<pre class="brush: java; gutter: true; toolbar: false;">Realm realm = Realm.getInstance(this);

new RealmBrowser.Builder(this)
        .add(realm, Person.class)
        	   .show();
</pre>


<h2 class="no-border">Additionally</h2>
<p>Realm Browser lets you add either separate class or class lists for view:</p>
<pre class="brush: java; gutter: true; toolbar: false;">Realm realm = ...;

List&lt;Class&lt;? extends RealmObject&gt;&gt; classes = new ArrayList&lt;&gt;();
classes.add(Cat.class);
classes.add(Dog.class);
classes.add(Person.class);
classes.add(DataTypeTest.class);

new RealmBrowser.Builder(c)
        .add(realm, classes)
        .show();
</pre>

<p>In case the project uses multiple database files, simply add new Realm instances indicating, which classes are included in this database.</p>


<pre class="brush: java; gutter: true; toolbar: false;">private static final String SECOND_REALM_NAME = "second.realm";
...
Context c = ...;
Realm realm = Realm.getInstance(c);

RealmConfiguration secondRealmConfig = new RealmConfiguration.Builder(c)
        .name(SECOND_REALM_NAME)
        .build();
Realm realmSecond = Realm.getInstance(secondRealmConfig);

List&lt;Class&lt;? extends RealmObject&gt;&gt; classes = new ArrayList&lt;&gt;();
classes.add(Cat.class);
classes.add(Dog.class);
classes.add(Person.class);
classes.add(DataTypeTest.class);

new RealmBrowser.Builder(c)
        .add(realm, classes)
        .add(realmSecond, DifferentFileObject.class)
        .show();
</pre>

<h2 class="no-border">Feedback</h2>
<p>We hope that you will enjoy using our product and would like to receive any feedback, comments or ideas sent to this e-mail: <a href="mailto:realm@scand.com">realm@scand.com</a></p>

