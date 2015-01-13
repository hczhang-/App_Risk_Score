package com.example.hczhang.appriskscore;

/**
 * Created by hczhang on 05/01/15.
 */
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class ApplicationDetail extends Activity {
    private ListView permissionList; // Graphical component managing the permissions list
    private ImageButton manageButton; // open the application manager button
    private String packageName;
    private Context context;
    /*
     * onCreate:
     * Executed in the creation of the activity. Recover
     * The application of information received by
     * The Intent and enrolled in components
     * graphics
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Creating the GUI and recovery of Intent
        setContentView(R.layout.application_detail);

        this.context = this;
        Intent thisIntent = getIntent();
        String applicationId = Long.toString(thisIntent.getExtras().getLong("applicationId"));

        // cursor provides random read-write access to the result set returned by a database query.
        Cursor data = Tools.database.database.query("application", new String[]{"label", "name", "version_code", "version_name", "system"}, "id = ?", new String[]{applicationId}, null, null, null);
        if (data.getCount() == 1) {
            data.moveToFirst();

            packageName = data.getString(1);

            // Display application name, package and version
            ((TextView)findViewById(R.id.application_detail_label)).setText(data.getString(0));
            ((TextView)findViewById(R.id.application_detail_name)).setText(data.getString(1));
            ((TextView)findViewById(R.id.application_detail_version)).setText(data.getString(2) + " / " + data.getString(3));

            if (data.getInt(4) == 1)
                ((TextView)findViewById(R.id.application_detail_system)).setVisibility(View.VISIBLE);
            else
                ((TextView)findViewById(R.id.application_detail_system)).setVisibility(View.GONE);


            manageButton = (ImageButton)findViewById(R.id.application_detail_manage_button);
            manageButton.setImageResource(R.drawable.ic_menu_manage);
            manageButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= 9) {
                        try {
                            Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                            i.addCategory(Intent.CATEGORY_DEFAULT);
                            i.setData(Uri.parse("package:" + packageName));
                            startActivity(i);
                        } catch (ActivityNotFoundException anfe) {
                            Intent i = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                            i.addCategory(Intent.CATEGORY_DEFAULT);
                            startActivity(i);
                        }
                    } else {
                        Toast.makeText(ApplicationDetail.this, context.getText(R.string.application_detail_manager_unavailable), Toast.LENGTH_LONG).show();
                    }

                }
            });

            // Retrieving the number of used and display permissions by querying the application ID
            data = Tools.database.database.rawQuery("SELECT Count(*) AS number " +
                    "FROM relation_application_permission " +
                    "WHERE application = ?;", new String[]{applicationId});
            data.moveToFirst();
            ((TextView)findViewById(R.id.application_detail_permission_count)).setText(data.getString(0));

            // ---------------------------------------------
            // Added by hczhang
            // Risk Score Method
            String riskScore;
            int nameColumn = 0;
            int i = 0;
            riskScore = "100";
            // ((TextView)findViewById(R.id.application_detail_risk_score)).setText(riskScore);
            Cursor permissionQuery = Tools.database.database.rawQuery("SELECT permission.name AS name FROM relation_application_permission INNER JOIN permission ON relation_application_permission.permission = permission.id WHERE relation_application_permission.application = ? ORDER BY permission.name COLLATE NOCASE ASC;", new String[] {applicationId});

            permissionQuery.moveToFirst();
            //permissionQuery.moveToNext();
//            while (permissionQuery.isAfterLast() == false)
//            {
//
//                permissionQuery.moveToNext();
//                nameColumn = permissionQuery.getColumnIndex("ACCESS_NETWORK_STATE");
//            }
//            permissionQuery.moveToFirst();
//            for(permissionQuery.moveToFirst();!permissionQuery.isAfterLast();permissionQuery.moveToNext())
//            {

//                if (nameColumn != -1) {
//                    break;
//                }
//                i++;
//
//            }
 //           ((TextView)findViewById(R.id.application_detail_risk_score)).setText(Integer.toString(nameColumn));
          ((TextView)findViewById(R.id.application_detail_risk_score)).setText(permissionQuery.getString(0));
            // ----------------------------------------------------------------------------------------------

            // Retrieving permissions and creating the list
            Cursor permissionListCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, permission.name AS name FROM relation_application_permission INNER JOIN permission ON relation_application_permission.permission = permission.id WHERE relation_application_permission.application = ? ORDER BY permission.name COLLATE NOCASE ASC;", new String[] {applicationId});

            // Added by hczhang
            //((TextView)findViewById(R.id.application_detail_risk_score)).setText(permissionListCursor.getColumnName(0));

            startManagingCursor(permissionListCursor);
            ListAdapter permissionAdapter = new SimpleCursorAdapter(this, R.layout.permission_list_item, permissionListCursor, new String[] {"name"}, new int[]{R.id.listviewpermissiontext});
            permissionList = (ListView)findViewById(R.id.application_detail_permission_list);
            permissionList.setAdapter(permissionAdapter);

            // At the click event on the list
            permissionList.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                    // Opening the retail activity of the selected permission
                    Intent intent = new Intent(getBaseContext() , PermissionDetail.class);
                    intent.putExtra("permissionId",id);
                    startActivity(intent);
                }
            });


        } else {
            // Application not found in the database
            ((TextView)findViewById(R.id.application_detail_label)).setText(getString(R.string.application_detail_nodata));
        }
        // Closing access to the database
        data.close();
    }
}

