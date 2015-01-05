package com.example.hczhang.appriskscore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Main extends Activity {
    // tings displayed to the user
    private ListView lstCategory;
    private ListView lstApplication;
    private ListView lstPermission;

    private TextView tabCategory;
    private TextView tabApplication;
    private TextView tabPermission;

    private enum VIEWS {Category, Application, Permission};
    private VIEWS currentView;

    // Return code of the Preferences screen
    private final int ACTIVITY_RESULT_PREFERENCE = 1000;

    /*
     * OnCreate:
     * Executed in the creation of the activity. created
     * Tabs and load lists after
     * Checking the database
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Loading the GUI
        setContentView(R.layout.main);

        // Retrieving lists and adding click events
        lstCategory = (ListView)findViewById(R.id.listviewcategory);
        lstCategory.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                // Opening a retail category
                Intent intent = new Intent(getBaseContext() , CategoryDetail.class);
                intent.putExtra("categoryId",id);
                startActivity(intent);
            }
        });

        lstApplication = (ListView)findViewById(R.id.listviewapplication);
        lstApplication.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                // Details of opening an application
                Intent intent = new Intent(getBaseContext() , ApplicationDetail.class);
                intent.putExtra("applicationId",id);
                startActivity(intent);
            }
        });

        lstPermission = (ListView)findViewById(R.id.listviewpermission);
        lstPermission.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                // Opening the detail of a permission
                Intent intent = new Intent(getBaseContext() , PermissionDetail.class);
                intent.putExtra("permissionId",id);
                startActivity(intent);
            }
        });

        tabCategory = (TextView)findViewById(R.id.tab_category);
        tabCategory.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (currentView != VIEWS.Category) {
                    switchTo(VIEWS.Category);
                }
            }
        });

        tabApplication = (TextView)findViewById(R.id.tab_application);
        tabApplication.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (currentView != VIEWS.Application) {
                    switchTo(VIEWS.Application);
                }
            }
        });

        tabPermission = (TextView)findViewById(R.id.tab_permission);
        tabPermission.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (currentView != VIEWS.Permission) {
                    switchTo(VIEWS.Permission);
                }
            }
        });

        switchTo(VIEWS.Category);

        // Creating and opening the database
        Tools.database = new Database(this);
        // Checking the database
        Tools.database.isUpToDate();
    }

    private void switchTo(VIEWS newView) {
        currentView = newView;

        if (currentView == VIEWS.Category) {
            lstCategory.setVisibility(View.VISIBLE);
            tabCategory.setTextColor(getResources().getColor(R.color.text_tab_selected));
        } else {
            lstCategory.setVisibility(View.INVISIBLE);
            tabCategory.setTextColor(getResources().getColor(R.color.text_tab));
        }

        if (currentView == VIEWS.Application) {
            lstApplication.setVisibility(View.VISIBLE);
            tabApplication.setTextColor(getResources().getColor(R.color.text_tab_selected));
        } else {
            lstApplication.setVisibility(View.INVISIBLE);
            tabApplication.setTextColor(getResources().getColor(R.color.text_tab));
        }

        if (currentView == VIEWS.Permission) {
            lstPermission.setVisibility(View.VISIBLE);
            tabPermission.setTextColor(getResources().getColor(R.color.text_tab_selected));
        } else {
            lstPermission.setVisibility(View.INVISIBLE);
            tabPermission.setTextColor(getResources().getColor(R.color.text_tab));
        }
    }

    /*
	 * OnDestroy:
     * End of program, access to the base is closed
     * Data
	 */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Tools.database.database.close();
    }

    /*
     * onCreateOptionsMenu
     * Creating the menu according to the menu resource
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    /*
     * onActivityResult
     * Return an activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Code retrieval activity launched
        switch(requestCode) {
            case ACTIVITY_RESULT_PREFERENCE: // If we return preferences, it refreshes the lists
                refreshData();
        }

    }

    /*
     * onOptionsItemSelected
     * Click Management on the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Depending on the button clicked
        switch (item.getItemId()) {
            case R.id.menu_update:
                // The database is refreshes
                Tools.database.updateDatabase(this);
                return true;
            case R.id.menu_preferences:
                // display preferences
                Intent intent = new Intent(getBaseContext() , Preference.class);
                startActivityForResult(intent, ACTIVITY_RESULT_PREFERENCE);
                return true;
            default:
                // Otherwise return
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     * RefreshData
     * It refreshes the category lists, applications and permissions
     */
    private void refreshData()
    {
        // Retrieving preferences
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean categoryOrder = pref.getBoolean("category_order", false); // Order category by true:name / false:count
        boolean applicationName = pref.getBoolean("application_name", true); // Display true:label / false:package
        boolean applicationOrder = pref.getBoolean("application_order", false); // Order app by true:name / false:count
        boolean permissionOrder = pref.getBoolean("permission_order", false); // Order perm by true:name / false:count
        boolean hideSystemApp = pref.getBoolean("hide_system_app", false); // Hide system applications

        String systemAppWhere = "";
        if (hideSystemApp)
            systemAppWhere = "WHERE system = 0 ";

        // Display field for the name
        String nameField;
        if (applicationName)
            nameField = "application.label";
        else
            nameField = "application.name";

        // Field to order the list of applications
        String orderField;
        if (applicationOrder)
            orderField = "name COLLATE NOCASE ASC";
        else
            orderField = "Count(relation_application_permission.permission) COLLATE NOCASE DESC";

        // It retrieves and displays the applications
        Cursor applicationCursor = Tools.database.database.rawQuery("SELECT id AS _id, " + nameField + " || ' (' || Count(permission) || ')' AS name, application.name AS package " +
                "FROM application " +
                "LEFT OUTER JOIN relation_application_permission ON application.id = relation_application_permission.application " +
                systemAppWhere +
                "GROUP BY application.id " +
                "ORDER BY " + orderField + ";", null);
        startManagingCursor(applicationCursor);

        List<ApplicationListItem> items = new ArrayList<ApplicationListItem>();

        PackageManager pm = getPackageManager();
        try {
            for(applicationCursor.moveToFirst(); !applicationCursor.isAfterLast(); applicationCursor.moveToNext()) {
                items.add(new ApplicationListItem(applicationCursor.getLong(0), pm.getApplicationIcon(applicationCursor.getString(2)), applicationCursor.getString(1)));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        ApplicationListAdapter applicationAdapter = new ApplicationListAdapter(this, items);
        lstApplication.setAdapter(applicationAdapter);

        // Field to order the permissions list
        if (permissionOrder)
            orderField = "permission.name COLLATE NOCASE ASC";
        else
            orderField = "Count(relation_application_permission.application) COLLATE NOCASE DESC";

        // It retrieves and displays permissions
        Cursor permissionCursor = Tools.database.database.rawQuery("SELECT permission.id AS _id, permission.name || ' (' || Count(application) || ')' AS name " +
                "FROM permission " +
                "LEFT OUTER JOIN relation_application_permission ON permission.id = relation_application_permission.permission " +
                "LEFT OUTER JOIN application ON application.id = relation_application_permission.application " +
                systemAppWhere +
                "GROUP BY permission.id " +
                "HAVING Count(application) > 0 " +
                "ORDER BY " + orderField + ";", null);
        startManagingCursor(permissionCursor);
        ListAdapter permissionAdapter = new SimpleCursorAdapter(this, R.layout.permission_list_item, permissionCursor, new String[] {"name"}, new int[]{R.id.listviewpermissiontext});
        lstPermission.setAdapter(permissionAdapter);

        // Field to order the list of categories
        if (categoryOrder)
            orderField = "category.name COLLATE NOCASE ASC";
        else
            orderField = "Count(DISTINCT relation_application_permission.application) COLLATE NOCASE DESC";

        // It retrieves and displays categories
        Cursor categoryCursor = Tools.database.database.rawQuery("SELECT category.id AS _id, category.name || ' (' || Count(DISTINCT application) || ')' AS name " +
                "FROM category " +
                "LEFT OUTER JOIN relation_category_permission ON category.id = relation_category_permission.category " +
                "INNER JOIN relation_application_permission ON relation_category_permission.permission = relation_application_permission.permission " +
                "LEFT OUTER JOIN application ON application.id = relation_application_permission.application " +
                systemAppWhere +
                "GROUP BY category.id " +
                "HAVING Count(DISTINCT application) > 0 " +
                "ORDER BY " + orderField + ";", null);
        startManagingCursor(categoryCursor);
        ListAdapter categoryAdapter = new SimpleCursorAdapter(this, R.layout.category_list_item, categoryCursor, new String[] {"name"}, new int[]{R.id.listviewcategorytext});
        lstCategory.setAdapter(categoryAdapter);
    }

    /*
     * databaseUpdated
     * Return of the update function of the database
     */
    public void databaseUpdated()
    {
        // It refreshes the lists
        refreshData();
    }

    /*
     * isUpToDateResult
     * Return the control function of the baes donnénes
     */
    public void isUpToDateResult(boolean upToDate) {
        // We prepared a warning to the user
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // Update db
                        Tools.database.updateDatabase(Main.this);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // Show results without update
                        refreshData();
                        break;
                }
            }
        };

        // If the database is up to date, we display the lists
        if (upToDate) {
            refreshData();
        } else {
            // If we ask the user if he wants to update the database
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage(getString(R.string.alert_database_nottodate_text));
            alert.setPositiveButton(getString(R.string.alert_database_nottodate_yes), dialogClickListener);
            alert.setNegativeButton(getString(R.string.alert_database_nottodate_no), dialogClickListener);
            alert.show();
        }
    }
}