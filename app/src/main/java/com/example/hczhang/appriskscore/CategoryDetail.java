package com.example.hczhang.appriskscore;

/**
 * Created by hczhang on 05/01/15.
 */
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CategoryDetail extends Activity {
    ListView applicationList; // Graphical component managing the permissions list

    /*
     * OnCreate:
     * Executed in the creation of the activity. Recover
     * Information received by the category
     * The Intent and enrolled in components
     * graphics
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creation of GUI
        setContentView(R.layout.category_detail);

        // Retrieving data from the intent
        Intent thisIntent = getIntent();
        String categoryId = Long.toString(thisIntent.getExtras().getLong("categoryId"));

        // Recovery information category
        Cursor data = Tools.database.database.query("category", new String[]{"name"}, "id = ?", new String[]{categoryId}, null, null, null);
        if (data.getCount() == 1) {
            data.moveToFirst();

            // Added information in graphical components
            ((TextView)findViewById(R.id.category_detail_name)).setText(data.getString(0));
            ((TextView)findViewById(R.id.category_detail_description)).setText(Tools.getStringResourceByName("category_" + data.getString(0), getResources(), this));

            // Get the number of applications that are in this category and displays
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            boolean hideSystemApp = pref.getBoolean("hide_system_app", false); // Hide system applications

            String systemAppWhere = "";
            if (hideSystemApp)
                systemAppWhere = " AND system = 0";

            data = Tools.database.database.rawQuery("SELECT Count(DISTINCT application) AS number " +
                    "FROM relation_category_permission " +
                    "INNER JOIN relation_application_permission ON relation_category_permission.permission = relation_application_permission.permission " +
                    "LEFT OUTER JOIN application ON relation_application_permission.application = application.id " +
                    "WHERE category = ?" + systemAppWhere + ";", new String[]{categoryId});
            data.moveToFirst();
            ((TextView)findViewById(R.id.category_detail_application_count)).setText(data.getString(0));

            // Preference for display applications, name or package
            boolean applicationName = pref.getBoolean("application_name", true); //  true:label / false:package

            String nameField;
            if (applicationName)
                nameField = "application.label";
            else
                nameField = "application.name";

            // Retrieving the category and display applications in the list
            Cursor applicationListCursor = Tools.database.database.rawQuery("SELECT DISTINCT application.id AS _id, " + nameField + " AS name, application.name AS package " +
                    "FROM relation_category_permission " +
                    "INNER JOIN relation_application_permission ON relation_category_permission.permission = relation_application_permission.permission INNER JOIN application ON relation_application_permission.application = application.id " +
                    "WHERE category = ?" + systemAppWhere + " " +
                    "ORDER BY " + nameField + " COLLATE NOCASE ASC;", new String[] {categoryId});
            startManagingCursor(applicationListCursor);

            List<ApplicationListItem> items = new ArrayList<ApplicationListItem>();

            PackageManager pm = getPackageManager();
            try {
                for(applicationListCursor.moveToFirst(); !applicationListCursor.isAfterLast(); applicationListCursor.moveToNext()) {
                    items.add(new ApplicationListItem(applicationListCursor.getLong(0), pm.getApplicationIcon(applicationListCursor.getString(2)), applicationListCursor.getString(1)));
                }
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }

            ApplicationListAdapter applicationAdapter = new ApplicationListAdapter(this, items);
            applicationList = (ListView)findViewById(R.id.category_detail_application_list);
            applicationList.setAdapter(applicationAdapter);

            // At the click event on the list
            applicationList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    // Opening the detail of the selected application
                    Intent intent = new Intent(getBaseContext() , ApplicationDetail.class);
                    intent.putExtra("applicationId",id);
                    startActivity(intent);
                }
            });
        } else {
            // The class is not found in the database
            ((TextView)findViewById(R.id.category_detail_name)).setText(getString(R.string.category_detail_nodata));
        }
        // Closing the access to the database
        data.close();
    }
}
