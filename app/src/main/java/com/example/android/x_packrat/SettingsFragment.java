package com.example.android.x_packrat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.example.android.x_packrat.data.XPackRatPreferences;
import com.example.android.x_packrat.data.BelongingsContract;
import com.example.android.x_packrat.sync.ReminderUtilities;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;

/**
 * Responsible for displaying settings options and for listening to changes in the
 * settings. The states of all settings are stored in the default shared preferences.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Sets the preference summary for the given preference. The preference summary indicates
     * the current state of a preference.
     *
     * @param preference The preference whose summary we wish to set
     * @param value      The new value to set for the given preference
     */
    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add preferences, defined in the XML file
        addPreferencesFromResource(R.xml.xpackrat_preferences);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        // Iterates through all preferences in SettingsFragment and sets the preference summaries
        // for non-checkbox preferences
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (!(p instanceof CheckBoxPreference)) {
                String value = sharedPreferences.getString(p.getKey(), "");
                setPreferenceSummary(p, value);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Invoked when a shared preference has changed. Executes appropriate methods to respond to the
     * change.
     *
     * @param sharedPreferences Reference to the default shared preferences
     * @param key               The key associated with the preference that has changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();

        // If the user changed the Reminder Frequency setting, update the variables used for
        // scheduling reminder notifications and start the process for rescheduling the reminder
        // notification job
        if (key.equals(getString(R.string.pref_reminder_frequency_key))) {
            Object value = sharedPreferences.getString(key, "");
            String stringValue = value.toString();
            XPackRatPreferences.updateReminderFrequency(activity, stringValue);
        }

        // Sets the new preference summary for a list preference whose set value has changed
        Preference preference = findPreference(key);
        if (null != preference) {
            if (!(preference instanceof CheckBoxPreference)) {
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            }
        }
    }
}
