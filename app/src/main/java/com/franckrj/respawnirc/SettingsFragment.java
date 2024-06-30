package com.franckrj.respawnirc;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.collection.SimpleArrayMap;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreferenceCompat;

import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Utils;
import com.takisoft.preferencex.EditTextPreference;
import com.takisoft.preferencex.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ARG_FILE_TO_LOAD = "com.franckrj.respawnirc.settingsfragment.ARG_FILE_TO_LOAD";

    private SimpleArrayMap<String, MinMaxInfos> listOfMinMaxInfos = new SimpleArrayMap<>();

    private final Preference.OnPreferenceClickListener subScreenPreferenceClicked = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (getActivity() instanceof NewSettingsFileNeedALoad) {
                if (preference.getKey().equals(getString(R.string.subScreenSettingsGeneral))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.general_settings, preference.getTitle().toString());
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsStyle))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.style_settings, preference.getTitle().toString());
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsMessage))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.message_settings, preference.getTitle().toString());
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsBehaviour))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.behaviour_settings, preference.getTitle().toString());
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsImageLink))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.imagelink_settings, preference.getTitle().toString());
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsIgnored))) {
                    ((NewSettingsFileNeedALoad) getActivity()).getNewSettingsFileId(R.xml.ignored_settings, preference.getTitle().toString());
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsManageIgnoreList))) {
                    startActivity(new Intent(getActivity(), ManageIgnoreListActivity.class));
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsHelp))) {
                    if (!isStateSaved()) {
                        HelpSettingsDialogFragment helpDialogFragment = new HelpSettingsDialogFragment();
                        helpDialogFragment.show(getActivity().getSupportFragmentManager(), "HelpSettingsDialogFragment");
                    }
                    return true;
                } else if (preference.getKey().equals(getString(R.string.subScreenSettingsShowWebsite))) {
                    Utils.openLinkInExternalBrowser("https://pijon.fr/RespawnIRC-Android/", getActivity());
                    return true;
                }
            }
            return false;
        }
    };

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        int idOfFileToLoad = R.xml.main_settings;

        if (getArguments() != null) {
            idOfFileToLoad = getArguments().getInt(ARG_FILE_TO_LOAD, R.xml.main_settings);
        }

        getPreferenceManager().setSharedPreferencesName(getString(R.string.preference_file_key));
        setPreferencesFromResource(idOfFileToLoad, rootKey);
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
            MinMaxInfos prefMinMax = listOfMinMaxInfos.get(editTextPref.getKey());
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

            if (getActivity() != null) {
                getActivity().recreate();
            }
        } else if (key.equals(getString(R.string.settingsInvertToolbarTextColor))) {
            ThemeManager.updateToolbarTextColor();

            if (getActivity() != null) {
                getActivity().recreate();
            }
        } else if (key.equals(getString(R.string.settingsPrimaryColorOfLightTheme)) ||
                   key.equals(getString(R.string.settingsTopicNameAndAccentColorOfLightTheme))) {
            if (getActivity() != null) {
                ThemeManager.updateColorsUsed(getResources());
                getActivity().recreate();
            }
        } else if (key.startsWith("settings.customColor.")) {
            if (getActivity() != null) {
                ThemeManager.updateColorsUsed(getResources());
            }
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
                listOfMinMaxInfos.put(pref.getKey(), new MinMaxInfos(currentPrefsInfos.minVal, currentPrefsInfos.maxVal));
            }
        }
    }

    /* Le but de la clef temporaire est de ne pas sauvegarder l'option par défaut si c'est celle ci
     * qui est retourné par "PrefsManager.getX()". La clef temporaire n'est pas vide pour empêcher
     * des possibles crash (des histoires de requireKey() etc). Elle est unique par type de pref pour
     * ne pas causer de crash lors de l'assignation d'un string à ce qui était précédement un bool.
     * La persistance est temporairement à false pour plus de sécurité, au cas où, dans le doute,
     * mais ça reste plutôt assez moche comme solution au final. */
    private static void updatePrefDefaultValue(Preference pref) {
        String realPrefKey = pref.getKey();
        pref.setPersistent(false);
        if (pref instanceof CheckBoxPreference) {
            pref.setKey("tmpKeyBool");
            CheckBoxPreference checkBoxPref = (CheckBoxPreference) pref;
            checkBoxPref.setChecked(PrefsManager.getBool(realPrefKey));
        } else if (pref instanceof SwitchPreferenceCompat) {
            pref.setKey("tmpKeyBool");
            SwitchPreferenceCompat switchPref = (SwitchPreferenceCompat) pref;
            switchPref.setChecked(PrefsManager.getBool(realPrefKey));
        } else if (pref instanceof EditTextPreference) {
            pref.setKey("tmpKeyString");
            EditTextPreference editTextPref = (EditTextPreference) pref;
            editTextPref.setText(PrefsManager.getString(realPrefKey));
        } else if (pref instanceof ListPreference) {
            pref.setKey("tmpKeyString");
            ListPreference listPref = (ListPreference) pref;
            listPref.setValue(PrefsManager.getString(realPrefKey));
        }
        pref.setPersistent(true);
        pref.setKey(realPrefKey);
    }

    private void updatePrefSummary(Preference pref) {
        if (pref instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) pref;
            MinMaxInfos prefMinMax = listOfMinMaxInfos.get(editTextPref.getKey());
            if (prefMinMax != null) {
                CharSequence oldSummary = editTextPref.getSummary();
                String newSummaryStr = "Entre " + String.valueOf(prefMinMax.min) + " et " + String.valueOf(prefMinMax.max) + " : " + editTextPref.getText();
                if(oldSummary != null) {
                    String oldSummaryStr = editTextPref.getSummary().toString();
                    int newLineIndex = oldSummaryStr.indexOf(System.lineSeparator());
                    if(newLineIndex >= 0) {
                        newSummaryStr += oldSummaryStr.substring(newLineIndex);
                    }
                }

                editTextPref.setSummary(newSummaryStr);
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
        void getNewSettingsFileId(int fileID, String newTitle);
    }

    public static class HelpSettingsDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(R.string.help).setMessage(R.string.help_dialog_settings)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        }
    }
}
