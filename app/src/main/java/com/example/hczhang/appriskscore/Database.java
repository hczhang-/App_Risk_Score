package com.example.hczhang.appriskscore;

/**
 * Created by hczhang on 05/01/15.
 */
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.List;

public class Database {

    // Application context
    private Context context;

    // Progress bar for long operations
    private ProgressDialog progressDialog;

    // create and manage the database
    public SQLiteDatabase database;


    public Database (Context context)
    {
        this.context = context;
        database = new DatabaseOpenHelper(this.context).getWritableDatabase();
    }

    /*
     * Checks if the database is up to date
     */
    public void isUpToDate()
    {
        // A spinner progress bar
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(context.getString(R.string.dialog_uptodate_text));
        progressDialog.setCancelable(false);

        // Recovery of applications
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_META_DATA);
        progressDialog.setMax(packages.size());

        // Viewing the progress bar
        progressDialog.show();

        // Running parallel task
        new IsUpToDateTask().execute();
    }

    /*
     * IsUpToDateTask Class
     * Provides a task in a separate thread for
     * Long work.
     */
    private class IsUpToDateTask extends AsyncTask<Void, Void, Boolean> {
        /*
         * doInBackground
         * Working method in the separate thread
         */
        protected Boolean doInBackground(Void... params) {
            // Retrieving a list of applications provider
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_META_DATA);

            String packageName;			// Package name
            String packageVersionCode;	// Package version code

            boolean isUpToDate = true;	// The base is, by default, updates
            Cursor packageChange;		// Access to the database

            // Selection of the number of applications in the database
            packageChange = database.rawQuery("SELECT Count(*) FROM application;", null);
            packageChange.moveToFirst();

            // If it is not zero or or if it does not match the number system
            if (packageChange.getInt(0) != packages.size() || packageChange.getInt(0) == 0) {
                // The access is closed and the database is outdated
                packageChange.close();
                return false;
            }

            // It runs every application
            for (PackageInfo pi : packages) {
                // Is incremented progressbar
                progressDialog.incrementProgressBy(1);

                // Retrieves information on the installed application
                packageName = pi.packageName;
                packageVersionCode = Integer.toString(pi.versionCode);

                // Get the number of applications in the database corresponding to the information
                packageChange = database.query("application", new String[]{"id"}, "name = ? AND version_code = ?", new String[]{packageName, packageVersionCode}, null, null, null);
                if (packageChange.getCount() == 0) {
                    // No application in the database ==> outdated
                    isUpToDate = false;
                    packageChange.close();
                    break;
                }
                packageChange.close();
            }

            // Sending results
            return isUpToDate;
        }

        /*
         * onPostExecute
         * Receives result in the main thread
         */
        protected void onPostExecute(Boolean result) {
            // Closing the progress bar and calling the function end
            progressDialog.dismiss();
            ((Main)context).isUpToDateResult(result);
        }
    }

    /*
     * updateDatabase
     * Update method of the database
     */
    public void updateDatabase(Activity activity)
    {
        // Configuring a progress bar
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.dialog_update_text));
        progressDialog.setCancelable(false);

        // Retrieving the number of applications on the system
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        progressDialog.setMax(packages.size());

        // Viewing the progress bar
        progressDialog.show();

        // Performing the task in a parallel thread
        new DatabaseUpdateTask(activity, database, progressDialog).execute();
    }
}
