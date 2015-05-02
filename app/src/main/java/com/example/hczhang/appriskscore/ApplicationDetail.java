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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.android.market.api.MarketSession;
import com.gc.android.market.api.model.Market;


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

// -------------------------------------------------------------------------------------------------
            // Added by hczhang
            // Manually Risk Score Method based on Kirin Rules
            int riskScore = 0;

            int firstRuleState = 0;
            int secondRuleState = 0;
            int thirdRuleState = 0;
            int forthRuleState = 0;
            int fifthRuleState = 0;
            int sixthRuleState = 0;
            int seventhRuleState = 0;
            int eighthRuleState = 0;
            int ninthRuleState = 0;


            Cursor permissionQuery = Tools.database.database.rawQuery("SELECT permission.name AS name FROM relation_application_permission INNER JOIN permission ON relation_application_permission.permission = permission.id WHERE relation_application_permission.application = ? ORDER BY permission.name COLLATE NOCASE ASC;", new String[] {applicationId});

            permissionQuery.moveToFirst();
            while (permissionQuery.isAfterLast() == false)
            {
                if (new String("ACCESS_COARSE_LOCATION").equals(permissionQuery.getString(0)))
                {
                    // Rule 5
                    fifthRuleState++;
                }

                if (new String("ACCESS_FINE_LOCATION").equals(permissionQuery.getString(0)))
                {
                    // Rule 4
                    forthRuleState++;
                }

                if (new String("INSTALL_SHORTCUT").equals(permissionQuery.getString(0)))
                {
                    // Rule 8
                    eighthRuleState++;
                }

                if (new String("INTERNET").equals(permissionQuery.getString(0)))
                {
                    //Rule 2, 3, 4, 5
                    secondRuleState++;
                    thirdRuleState++;
                    forthRuleState++;
                    fifthRuleState++;
                }

                if (new String("PHONE_STATE").equals(permissionQuery.getString(0)))
                {
                    // Rule 2
                    secondRuleState++;
                }

                if (new String("PROCESS_OUTGOING_CALL").equals(permissionQuery.getString(0)))
                {
                    // Rule 3
                    thirdRuleState++;
                }

                if (new String("RECEIVE_BOOT_COMPLETE").equals(permissionQuery.getString(0)))
                {
                    // Rule 4, 5
                    forthRuleState++;
                    fifthRuleState++;
                }

                if (new String("RECEIVE_SMS").equals(permissionQuery.getString(0)))
                {
                    // Rule 6
                    sixthRuleState++;
                }
                if (new String("RECORD_AUDIO").equals(permissionQuery.getString(0)))
                {
                    // Rule 2, 3
                    secondRuleState++;
                    thirdRuleState++;
                }

                if (new String("SEND_SMS").equals(permissionQuery.getString(0)))
                {
                    // Rule 7
                    seventhRuleState++;
                }

                if (new String("SET_DEBUG_APP").equals(permissionQuery.getString(0)))
                {
                    //Rule 1
                    firstRuleState++;
                }

                if (new String("SET_PREFERRED_APPLICATION").equals(permissionQuery.getString(0)))
                {
                    // Rule 9
                    ninthRuleState++;
                }

                if (new String("UNINSTALL_SHORTCUT").equals(permissionQuery.getString(0)))
                {
                    // Rule 8
                    eighthRuleState++;
                }

                if (new String("WRITE_SMS").equals(permissionQuery.getString(0)))
                {
                    // Rule 6, 7
                    sixthRuleState++;
                    seventhRuleState++;
                }
                    permissionQuery.moveToNext();
            }

            // Check if the state break the Security Rule
            if (firstRuleState != 0 || secondRuleState >= 3 || thirdRuleState >= 3 || forthRuleState >= 3 ||
                    fifthRuleState >=3 || sixthRuleState >= 2 || seventhRuleState >= 2 || eighthRuleState >= 2 ||
                    ninthRuleState != 0)
            {
                riskScore = 100;
            }
            else {
                riskScore = permissionQuery.getCount();
            }


            ((TextView)findViewById(R.id.application_detail_risk_score)).setText(Integer.toString(riskScore));
            //permissionQuery.moveToNext();
//            while (permissionQuery.isAfterLast() == false)
//            {
//
//                permissionQuery.moveToNext();
//               nameColumn = permissionQuery.getColumnIndex("ACCESS_COARSE_LOCATION");
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

//            ((TextView)findViewById(R.id.application_detail_risk_score)).setText(Integer.toString(nameColumn));
            //((TextView)findViewById(R.id.application_detail_risk_score)).setText(permissionQuery.getString(0));


            // Added for resubmission
            MarketSession session = new MarketSession();
            session.login("zhc0511@gmail.com", "zhc13722747736/");
//            session.getContext().setAndroidId(myAndroidId);
//            String query = "maps";
            Market.AppsRequest appsRequest = Market.AppsRequest.newBuilder()
                    .setQuery(packageName)
                    .setStartIndex(0).setEntriesCount(10)
                    .setWithExtendedInfo(true)
                    .build();

            session.append(appsRequest, new MarketSession.Callback<Market.AppsResponse>() {
                @Override
                public void onResult(Market.ResponseContext context, Market.AppsResponse response) {
                    // Your code here
                    // response.getApp(0).getCreator() ...
                    // see AppsResponse class definition for more infos
                }
            });
            session.flush();


// -------------------------------------------------------------------------------------------------

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

