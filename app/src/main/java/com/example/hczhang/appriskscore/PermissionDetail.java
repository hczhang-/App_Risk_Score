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

public class PermissionDetail extends Activity {
    // Application list
    ListView applicationList;

    /*
     * OnCreate:
     * Executed in the creation of the activity. Recover
     * Information on permission received by
     * The Intent and enrolled in components
     * graphics
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creating GUI
        setContentView(R.layout.permission_detail);

        // Recovery of intent
        Intent thisIntent = getIntent();
        String permissionId = Long.toString(thisIntent.getExtras().getLong("permissionId"));

        // Retrieving information of the permission
        // find the column permission in database
        Cursor data = Tools.database.database.query("permission", new String[]{"name"}, "id = ?", new String[]{permissionId}, null, null, null);
        if (data.getCount() == 1) {
            data.moveToFirst();

            // Display the permission detail by id
            ((TextView)findViewById(R.id.permission_detail_name)).setText(data.getString(0));
            ((TextView)findViewById(R.id.permission_detail_description)).setText(Tools.getStringResourceByName("permission_" + data.getString(0), getResources(), this));

            // Used to help create Preference hierarchies from activities or XML
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            boolean hideSystemApp = pref.getBoolean("hide_system_app", false); // Hide system applications

            String systemAppWhere = "";
            if (hideSystemApp)
                systemAppWhere = " AND system = 0";

            // Selection and display of the number of applications that use this permission
            data = Tools.database.database.rawQuery("SELECT Count(*) AS number " +
                    "FROM relation_application_permission " +
                    "LEFT OUTER JOIN application ON relation_application_permission.application = application.id " +
                    "WHERE permission = ?" + systemAppWhere + ";", new String[]{permissionId});
            data.moveToFirst();
            ((TextView)findViewById(R.id.permission_detail_application_count)).setText(data.getString(0));

            // Retrieving display preferences
            boolean applicationName = pref.getBoolean("application_name", true); // Display true:label / false:package

            // Display field
            String nameField;
            if (applicationName)
                nameField = "application.label";
            else
                nameField = "application.name";

            // Retrieving the application list and display the list
            Cursor applicationListCursor = Tools.database.database.rawQuery("SELECT application.id AS _id, " + nameField + " AS name, application.name AS package " +
                    "FROM relation_application_permission " +
                    "INNER JOIN application ON relation_application_permission.application = application.id " +
                    "WHERE relation_application_permission.permission = ?" + systemAppWhere + " " +
                    "ORDER BY " + nameField + " COLLATE NOCASE ASC;", new String[] {permissionId});
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

            applicationList = (ListView)findViewById(R.id.permission_detail_application_list);
            applicationList.setAdapter(applicationAdapter);

            // Adding an event on the list
            applicationList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    // Opening the detail of the selected application
                    Intent intent = new Intent(getBaseContext() , ApplicationDetail.class);
                    intent.putExtra("applicationId",id);
                    startActivity(intent);
                }
            });
        } else {
            // No permission is corresspond
            ((TextView)findViewById(R.id.permission_detail_name)).setText(getString(R.string.permission_detail_nodata));
        }
        data.close();
    }
}

