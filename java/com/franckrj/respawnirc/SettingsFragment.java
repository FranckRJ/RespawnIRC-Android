package com.franckrj.respawnirc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.widget.EditText;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(getString(R.string.preference_file_key));
        addPreferencesFromResource(R.xml.settings);
        initFilter(getPreferenceScreen());
        initSummary(getPreferenceScreen());
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            MinMaxInfos prefMinMax = (MinMaxInfos) editTextPref.getEditText().getTag();
            int prefValue = 0;
            if (!editTextPref.getText().isEmpty()) {
                try {
                    prefValue = Integer.parseInt(editTextPref.getText());
                } catch (Exception e) {
                    prefValue = 999999999;
                }
            }
            if (prefValue < prefMinMax.min) {
                prefValue = prefMinMax.min;
            } else if (prefValue > prefMinMax.max) {
                prefValue = prefMinMax.max;
            }
            editTextPref.setText(String.valueOf(prefValue));
        }

        updatePrefSummary(pref);
    }

    private void initFilter(PreferenceGroup pref) {
        EditText maxNumberOfOverlyQuote = ((EditTextPreference) pref.findPreference(getActivity().getString(R.string.settingsMaxNumberOfOverlyQuote))).getEditText();
        EditText refreshTopicTime = ((EditTextPreference) pref.findPreference(getActivity().getString(R.string.settingsRefreshTopicTime))).getEditText();
        EditText maxNumberOfMessages = ((EditTextPreference) pref.findPreference(getActivity().getString(R.string.settingsMaxNumberOfMessages))).getEditText();
        EditText initialNumberOfMessages = ((EditTextPreference) pref.findPreference(getActivity().getString(R.string.settingsInitialNumberOfMessages))).getEditText();

        maxNumberOfOverlyQuote.setTag(new MinMaxInfos(0, 15));
        refreshTopicTime.setTag(new MinMaxInfos(5000, 60000));
        maxNumberOfMessages.setTag(new MinMaxInfos(1, 100));
        initialNumberOfMessages.setTag(new MinMaxInfos(1, 20));
    }

    private void initSummary(Preference pref) {
        if (pref instanceof PreferenceGroup) {
            PreferenceGroup prefGroup = (PreferenceGroup) pref;
            final int currentPreferenceCount = prefGroup.getPreferenceCount();
            for (int i = 0; i < currentPreferenceCount; i++) {
                initSummary(prefGroup.getPreference(i));
            }
        } else {
            updatePrefSummary(pref);
        }
    }

    private void updatePrefSummary(Preference pref) {
        if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            MinMaxInfos prefMinMax = (MinMaxInfos) editTextPref.getEditText().getTag();
            editTextPref.setSummary("Entre " + String.valueOf(prefMinMax.min) + " et " + String.valueOf(prefMinMax.max) + " : " + editTextPref.getText());
        }
    }

    private static class MinMaxInfos {
        public final int min;
        public final int max;

        MinMaxInfos(int newMin, int newMax) {
            min = newMin;
            max = newMax;
        }
    }
}
