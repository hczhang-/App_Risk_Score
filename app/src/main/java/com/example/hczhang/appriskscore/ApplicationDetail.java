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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.Locale;

public class ApplicationDetail extends Activity {
    private ListView permissionList; // Graphical component managing the permissions list
    private ImageButton manageButton; // open the application manager button
    private String packageName;
    private Context context;

// -------------------------------------------------------------------------------------------------
    // Added by hczhang
    // Define the classification of permissions
    // Permissions of Category Location
    private static final String[] permissionLocation = {"ACCESS_ASSISTED_GPS",
            "ACCESS_COARSE_LOCATION",
            "ACCESS_COARSE_UPDATES",
            "ACCESS_FINE_LOCATION",
            "ACCESS_GPS",
            "ACCESS_LOCATION",
            "ACCESS_LOCATION_EXTRA_COMMANDS",
            "ACCESS_NETWORK_LOCATION",
            "LOCATION"};


    // Permissions of Category Identity
    private static final String[] permissionPhoneIdentity = {"AUTHENTICATE_ACCOUNTS",
            "GET_ACCOUNTS",
            "MANAGE_ACCOUNTS",
            "MANAGE_APP_TOKENS",
            "READ_OWNER_DATA",
            "READ_PROFILE",
            "USE_CREDENTIALS",
            "WRITE_OWNER_DATA",
            "WRITE_PROFILE"};

    // Permissions of Category messages
    private static final String[] permissionMessages = {"BROADCAST_SMS",
            "DELETE_SMS",
            "READ_SMS",
            "READ_MMS",
            "RECEIVE_EMAIL_NOTIFICATION",
            "RECEIVE_MMS",
            "RECEIVE_SMS",
            "RECEIVE_WAP_PUSH",
            "WRITE_SMS"};

    // Permissions of Category contacts
    private static final String[] permissionContacts = {"READ_CONTACTS",
            "WRITE_CONTACTS"};

    // Permissions of Category calender
    private static final String[] permissionCalendar = {"READ_CALENDAR",
            "READ_DIARY",
            "WRITE_CALENDAR"};

    // Permissions of Category accessing payment
    private static final String[] permissionPaying = {"CALL",
            "CALL_PHONE",
            "CALL_PRIVILEGED",
            "SEND_SMS",
            "SEND_SMS_NO_CONFIRMATION"};

    // Permissions of Category system
    private static final String[] permissionSystem = {"ACCESS_MTP",
            "BLUETOOTH_ADMIN",
            "BRICK",
            "CHANGE_CONFIGURATION",
            "CHANGE_NETWORK_STATE",
            "CHANGE_WIFI_STATE",
            "CHANGE_WIMAX_STATE",
            "CLEAR_APP_USER_DATA",
            "CONFIRM_FULL_BACKUP",
            "DELETE_CACHE_FILES",
            "DELETE_PACKAGES",
            "DEVICE_POWER",
            "DIAGNOSTIC",
            "DISABLE_KEYGUARD",
            "DUMP",
            "FACTORY_TEST",
            "FORCE_STOP_PACKAGES",
            "GET_TASKS",
            "INSTALL_PACKAGES",
            "INTERNAL_SYSTEM_WINDOW",
            "KILL_BACKGROUND_PROCESSES",
            "MANAGE_USB",
            "MODIFY_AUDIO_SETTINGS",
            "MODIFY_NETWORK_ACCOUNTING",
            "MODIFY_PHONE_STATE",
            "READ_FRAME_BUFFER",
            "READ_LOGS",
            "READ_SETTINGS",
            "READ_SYNC_SETTINGS",
            "READ_SYNC_STATS",
            "READ_NETWORK_USAGE_HISTORY",
            "READ_USER_DICTIONARY",
            "REBOOT",
            "SET_POINTER_SPEED",
            "SET_PROCESS_LIMIT",
            "SET_TIME",
            "SET_TIME_ZONE",
            "SHUTDOWN",
            "WAKE_LOCK",
            "WRITE_APN_SETTINGS",
            "WRITE_MEDIA_STORAGE",
            "WRITE_SECURE_SETTINGS",
            "WRITE_SETTINGS",
            "WRITE_SYNC_SETTINGS"};
// -------------------------------------------------------------------------------------------------


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
            double riskScore = 0;
            double f1 = 0;
            double f2 = 0;

            int firstRuleState = 0;
            int secondRuleState = 0;
            int thirdRuleState = 0;
            int forthRuleState = 0;
            int fifthRuleState = 0;
            int sixthRuleState = 0;
            int seventhRuleState = 0;
            int eighthRuleState = 0;
            int ninthRuleState = 0;

            // Count Permission Category
            int CategoryLocation = 0;
            int CategoryPhoneIdentity = 0;
            int CategoryMessages = 0;
            int CategoryContacts = 0;
            int CategoryCalendar = 0;
            int CategoryPaying = 0;
            int CategorySystem = 0;

            // Set Different Risk Levels
            double r1 = 5;
            double r2 = 3;
            double r3 = 1;
            double r4 = 0.5;

            // Initialise all the permission weights;
            double locationWeight = 0;
            double phoneIdentityWeight = 0;
            double messagesWeight = 0;
            double contactsWeight = 0;
            double calendarWeight = 0;
            double payingWeight = 0;
            double systemWeight = 0;



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
            else if (permissionQuery.getCount() == 0)
            {
                riskScore = 0;

            } else {

                // ---------------------------------------------------------------------------------
                // Added by hczhang
                // Category Factor
                permissionQuery.moveToFirst();
                while (permissionQuery.isAfterLast() == false) {
                    if (Arrays.asList(permissionLocation).contains(permissionQuery.getString(0))) {
                        CategoryLocation++;

                    }
                    if (Arrays.asList(permissionPhoneIdentity).contains(permissionQuery.getString(0))) {
                        CategoryPhoneIdentity++;

                    }
                    if (Arrays.asList(permissionMessages).contains(permissionQuery.getString(0))) {
                        CategoryMessages++;

                    }
                    if (Arrays.asList(permissionContacts).contains(permissionQuery.getString(0))) {
                        CategoryContacts++;
                    }
                    if (Arrays.asList(permissionCalendar).contains(permissionQuery.getString(0))) {
                        CategoryCalendar++;

                    }
                    if (Arrays.asList(permissionPaying).contains(permissionQuery.getString(0))) {
                        CategoryPaying++;

                    }
                    if (Arrays.asList(permissionSystem).contains(permissionQuery.getString(0))) {
                        CategorySystem++;

                    }
                    permissionQuery.moveToNext();

                }

                // Get an app's category from Google Play
                Document doc = null;
                String category = null;
                Locale locale = null;
                if (locale == null) {
                    locale = Locale.getDefault();
                }

                try {
//                    doc = Jsoup.connect("https://play.google.com/store/apps/details?id="+packageName).get();
                    doc = Jsoup.connect("market://details?id=com.instagram.android"+"&hl=" + locale.getISO3Language()).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (doc != null) {
                    Elements elements = doc.select("span[itemprop=genre]");
                    category = elements.text();
                    ((TextView)findViewById(R.id.android_market_category)).setText(elements.text());
                }else {
                    ((TextView)findViewById(R.id.android_market_category)).setText("unknown");
                }

                // Based on different categories to set each permission categories a proper weight
                switch (category) {
                    case "Business":
                        locationWeight = r1;
                        phoneIdentityWeight = r3;
                        messagesWeight = r1;
                        contactsWeight = r2;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r2;
                        break;
                    case "Communication":
                        locationWeight = r4;
                        phoneIdentityWeight = r3;
                        messagesWeight = r1;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r3;
                        break;
                    case "Entertainment":
                        locationWeight = r4;
                        phoneIdentityWeight = r3;
                        messagesWeight = r1;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r1;
                        break;
                    case "Finance":
                        locationWeight = r4;
                        phoneIdentityWeight = r4;
                        messagesWeight = r1;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r4;
                        break;
                    case "Lifestyle":
                        locationWeight = r4;
                        phoneIdentityWeight = r2;
                        messagesWeight = r1;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r1;
                        break;
                    case "Media":
                        locationWeight = r4;
                        phoneIdentityWeight = r3;
                        messagesWeight = r3;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r1;
                        break;
                    case "Social":
                        locationWeight = r4;
                        phoneIdentityWeight = r3;
                        messagesWeight = r1;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r1;
                        break;
                    case "Tools":
                        locationWeight = r4;
                        phoneIdentityWeight = r3;
                        messagesWeight = r1;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r3;
                        break;
                    case "Games":
                        locationWeight = r4;
                        phoneIdentityWeight = r3;
                        messagesWeight = r1;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r1;
                        break;
                    default:
                        locationWeight = r4;
                        phoneIdentityWeight = r3;
                        messagesWeight = r1;
                        contactsWeight = r1;
                        calendarWeight = r4;
                        payingWeight = r1;
                        systemWeight = r1;
                        break;
                }

                // The formula of risk score

                f2 =  ( CategoryLocation * locationWeight +
                        CategoryPhoneIdentity * phoneIdentityWeight +
                        CategoryMessages * messagesWeight +
                        CategoryContacts * contactsWeight +
                        CategoryCalendar * calendarWeight +
                        CategoryPaying * payingWeight +
                        CategorySystem * systemWeight ) /
                        ( r1 * (CategoryLocation + CategoryPhoneIdentity + CategoryMessages +
                                CategoryContacts + CategoryCalendar + CategoryPaying + CategorySystem ));

                riskScore = f2 * 100;
            }



            ((TextView)findViewById(R.id.application_detail_risk_score)).setText(Double.
                    toString(riskScore));


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
                    Intent intent = new Intent(getBaseContext(), PermissionDetail.class);
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

