package com.example.hczhang.appriskscore;

/**
 * Created by hczhang on 05/01/15.
 */
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preference extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Creating the preferences interface
        addPreferencesFromResource(R.xml.preference);
    }
}
