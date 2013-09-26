package com.strath.hub;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.view.MenuItem;

/**
 * Created by greig on 25/09/2013.
 */
public class PreferencesActivity extends PreferenceActivity
{
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
        getActionBar().setDisplayHomeAsUpEnabled(true);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(android.R.id.home ==  item.getItemId() ){
            //  try one of these:

            // dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));

            // getFragmentManager().popBackStack();

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs);

            // Modify the placeholder preference for checkout status indication
            EditTextPreference deviceStatus = (EditTextPreference)findPreference("devicestatus");
            Boolean checkoutStatus = false;
            deviceStatus.setSummary("Device Status: " + (checkoutStatus ? "Checked out!": "Checked in!") );
            deviceStatus.setEnabled(false);
            deviceStatus.setSelectable(false);
            deviceStatus.setPersistent(false);
            deviceStatus.setShouldDisableView(false);

            if (checkoutStatus)
            {
                // Device is checked out - allow user to check back in
                deviceStatus.setSummary("Device Status: checked out");
                deviceStatus.setEnabled(true);
                deviceStatus.setSelectable(true);
                deviceStatus.setPersistent(false);
                deviceStatus.setShouldDisableView(false);

                // Device is checked out - don't permit changing patient ID
                Preference patientID = findPreference("patientid");
                ((PreferenceGroup) findPreference("patient")).removePreference(patientID);
            }
            else
            {
                // Device is checked in - permit changing patient ID

                deviceStatus.setSummary("Device Status: checked in");
                deviceStatus.setEnabled(false);
                deviceStatus.setSelectable(false);
                deviceStatus.setPersistent(false);
                deviceStatus.setShouldDisableView(false);
            }

        }
    }
}