package com.example.hczhang.appriskscore;

/**
 * Created by hczhang on 05/01/15.
 */
import android.content.Context;
import android.content.res.Resources;

public class Tools {
    // Package for the application
    private final static String packageName = "com.carlocriniti.android.permission_explorer";
    // Application database
    public static Database database;
    /*
     * getStringResourceByName
     * Retrieves a string in Resources
     * Anywhere in the application
     */
    public static String getStringResourceByName(String name, Resources res, Context context)
    {
        int resId = res.getIdentifier(name, "string", packageName);
        return context.getString(resId);
    }
}

