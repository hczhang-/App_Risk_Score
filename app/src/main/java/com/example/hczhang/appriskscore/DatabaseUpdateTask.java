package com.example.hczhang.appriskscore;

/**
 * Created by hczhang on 05/01/15.
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * DatabaseUpdateTask Class
 * Provides a task in a separate thread for
 * Long work.
 */
public class DatabaseUpdateTask extends AsyncTask<Void, Void, Boolean> {
    private Activity activity;
    private SQLiteDatabase database;
    private ProgressDialog progressDialog;

    public DatabaseUpdateTask (Activity activity, SQLiteDatabase database, ProgressDialog progressDialog) {
        this.activity = activity;
        this.database = database;
        this.progressDialog = progressDialog;
    }

    /*
     * doInBackground
     * Working method in the separate thread
     */
    protected Boolean doInBackground(Void... params) {
        // Variable declaration before the loop
        PackageManager pm = activity.getPackageManager();
        PackageInfo pi;
        String permissionName;
        String applicationLabel;
        String packageName;
        int system;
        int packageVersionCode;
        String packageVersionName;
        Map<String, Integer> permissionIds = new HashMap<String, Integer>();
        Map<String, Application> applicationsToAdd = new HashMap<String, Application>();
        Application currentApplication;
        long applicationId;
        ContentValues values;

        // Empty the relevant tables in the database
        database.delete("application", null, null);
        database.delete("relation_application_permission", null, null);

        // A list of permissions and map them to their Id
        Cursor permissionIdsCursor = database.query("permission", new String[] {"id", "name"}, null, null, null, null, null);
        if (permissionIdsCursor.moveToFirst()) {
            while (!permissionIdsCursor.isAfterLast()) {
                permissionIds.put(permissionIdsCursor.getString(1), permissionIdsCursor.getInt(0));
                permissionIdsCursor.moveToNext();
            }
        }
        permissionIdsCursor.close();

        // List of installed applications
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        // Through each package system
        for (ApplicationInfo ai : packages)
        {
            // Increments the progress bar
            progressDialog.incrementProgressBy(1);

            // Get the name of the package and the label if possible
            packageName = ai.packageName;
            try {
                applicationLabel = pm.getApplicationLabel(ai).toString();
            } catch (Exception ex) { // application not found
                applicationLabel = packageName;
            }

            // Recover if possible versions
            try {
                pi = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                packageVersionCode = pi.versionCode;
                packageVersionName = pi.versionName;
            } catch (Exception ex) {
                packageVersionCode = 0;
                packageVersionName = "n/a";
                //Log.e("PM", "Error fetching app version");
            }

            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
                system = 1;
            else
                system = 0;

            if (applicationsToAdd.containsKey(packageName)) {
                currentApplication = applicationsToAdd.get(packageName);
            } else {
                currentApplication = new Application(applicationLabel, packageName, packageVersionCode, packageVersionName, system);
            }

            // Trying to get the permissions of the application
            try {
                pi = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);

                if (pi.requestedPermissions != null && pi.requestedPermissions.length > 0) {
                    //Through each permission
                    for (int i = 0; i < pi.requestedPermissions.length; ++i) {
                        if (pi.requestedPermissions[i].startsWith("android.permission.")) {
                            // if official permission android, recovering his name
                            permissionName = pi.requestedPermissions[i].substring("android.permission.".length());
                            //Log.d("PERMISSION", "Found permission : " + permissionName);

                            // Is recovered id permission
                            currentApplication.addPermission(permissionIds.get(permissionName));
                        }
                    }
                }
            } catch (Exception ex) {
                Log.e("UPDATE", ex.toString());
            }

            applicationsToAdd.put(packageName, currentApplication);
        }

        try {
            database.setLockingEnabled(false);

            database.beginTransaction();
            List<Integer> applicationPermissions;
            progressDialog.setProgress(0);
            for (Application a : applicationsToAdd.values()) {
                progressDialog.incrementProgressBy(1);
                values = new ContentValues();
                values.put("label", a.getLabel());
                values.put("name", a.getName());
                values.put("version_code", a.getVersionCode());
                values.put("version_name", a.getVersionName());
                values.put("system", a.isSystem());
                applicationId = database.insert("application", null, values);

                values = new ContentValues();
                values.put("application", applicationId);

                applicationPermissions = a.getPermissions();
                for (Integer p : applicationPermissions) {
                    values.put("permission", p);
                    database.insert("relation_application_permission", null, values);
                }
            }
            database.setTransactionSuccessful();
            database.endTransaction();
        } finally {
            database.setLockingEnabled(true);
        }

        return true;
    }

    /*
     * onPostExecute
     * Receives result in the main thread
     */
    protected void onPostExecute(Boolean result) {
        // Closing the progress bar and call the result function
        progressDialog.dismiss();
        ((Main)activity).databaseUpdated();
    }
}
