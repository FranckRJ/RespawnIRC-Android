package com.franckrj.respawnirc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.ThemeManager;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ARG_FILE_TO_LOAD = "com.franckrj.respawnirc.settingsfragment.ARG_FILE_TO_LOAD";

    private final Preference.OnPreferenceClickListener subScreenPreferenceClicked = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (getActivity() instanceof NewSettingsFileNeedALoad) {
                if (preference.getKey().equals(getString(R.string.subScreenSettingsStyle))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.style_settings);
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsImageLink))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.imagelink_settings);
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsBehaviour))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.behaviour_settings);
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int idOfFileToLoad = R.xml.main_settings;

        if (getArguments() != null) {
            idOfFileToLoad = getArguments().getInt(ARG_FILE_TO_LOAD, R.xml.main_settings);
        }

        getPreferenceManager().setSharedPreferencesName(getString(R.string.preference_file_key));
        addPreferencesFromResource(idOfFileToLoad);
        initPrefsInfos(getPreferenceScreen());
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
            if (prefMinMax != null) {
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
        } else if (key.equals(getString(R.string.settingsThemeUsed))) {
            ThemeManager.updateThemeUsed();
            getActivity().recreate();
        }

        updatePrefSummary(pref);
    }

    private void initPrefsInfos(Preference pref) {
        if (pref instanceof PreferenceGroup) {
            PreferenceGroup prefGroup = (PreferenceGroup) pref;
            final int currentPreferenceCount = prefGroup.getPreferenceCount();
            for (int i = 0; i < currentPreferenceCount; i++) {
                initPrefsInfos(prefGroup.getPreference(i));
            }
        } else {
            initClickedListenerIfNeeded(pref);
            initFilterIfNeeded(pref);
            updatePrefDefaultValue(pref);
            updatePrefSummary(pref);
        }
    }

    private void initClickedListenerIfNeeded(Preference pref) {
        if (!pref.isPersistent() && pref.getKey().startsWith("subScreenSettings.")) {
            pref.setOnPreferenceClickListener(subScreenPreferenceClicked);
        }
    }

    private void initFilterIfNeeded(Preference pref) {
        if (pref instanceof EditTextPreference) {
            PrefsManager.StringPref currentPrefsInfos = PrefsManager.getStringInfos(pref.getKey());
            if (currentPrefsInfos.isInt) {
                EditTextPreference editTextPref = (EditTextPreference) pref;
                editTextPref.getEditText().setTag(new MinMaxInfos(currentPrefsInfos.minVal, currentPrefsInfos.maxVal));
            }
        }
    }

    private void updatePrefDefaultValue(Preference pref) {
        if (pref instanceof CheckBoxPreference) {
            CheckBoxPreference checkBoxPref = (CheckBoxPreference) pref;
            checkBoxPref.setChecked(PrefsManager.getBool(checkBoxPref.getKey()));
        } else if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            editTextPref.setText(PrefsManager.getString(editTextPref.getKey()));
        } else if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            listPref.setValue(PrefsManager.getString(listPref.getKey()));
        }
    }

    private void updatePrefSummary(Preference pref) {
        if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            MinMaxInfos prefMinMax = (MinMaxInfos) editTextPref.getEditText().getTag();
            if (prefMinMax != null) {
                editTextPref.setSummary("Entre " + String.valueOf(prefMinMax.min) + " et " + String.valueOf(prefMinMax.max) + " : " + editTextPref.getText());
            }
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

    public interface NewSettingsFileNeedALoad {
        void getNewSettingsFileId(int fileID);
    }
}
