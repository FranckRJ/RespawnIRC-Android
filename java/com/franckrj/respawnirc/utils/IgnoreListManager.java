package com.franckrj.respawnirc.utils;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

public class IgnoreListManager {
    private static HashSet<String> listOfIgnoredPseudosInLC = new HashSet<>();

    public static boolean pseudoInLCIsIgnored(String pseudoToCheckInLC) {
        //noinspection SimplifiableIfStatement
        if (!listOfIgnoredPseudosInLC.isEmpty()) {
            return listOfIgnoredPseudosInLC.contains(pseudoToCheckInLC);
        }

        return false;
    }

    public static boolean addPseudoToIgnoredList(String pseudoToAdd) {
        return listOfIgnoredPseudosInLC.add(pseudoToAdd.toLowerCase());
    }

    public static void removePseudoFromIgnoredList(String pseudoToRemove) {
        listOfIgnoredPseudosInLC.remove(pseudoToRemove.toLowerCase());
    }

    public static void loadListOfIgnoredPseudos() {
        String listOfIgnoredPseudosInLCInAString = PrefsManager.getString(PrefsManager.StringPref.Names.IGNORED_PSEUDOS_IN_LC_LIST);

        if (!Utils.stringIsEmptyOrNull(listOfIgnoredPseudosInLCInAString)) {
            listOfIgnoredPseudosInLC = new HashSet<>(Arrays.asList(listOfIgnoredPseudosInLCInAString.split(",")));
        } else {
            listOfIgnoredPseudosInLC = new HashSet<>();
        }
    }

    public static void saveListOfIgnoredPseudos() {
        String listOfIgnoredPseudosInLCInAString = TextUtils.join(",", listOfIgnoredPseudosInLC);
        PrefsManager.putString(PrefsManager.StringPref.Names.IGNORED_PSEUDOS_IN_LC_LIST, listOfIgnoredPseudosInLCInAString);
        PrefsManager.applyChanges();
    }

    public static String[] getListOfIgnoredPseudosInLCAsArray() {
        return listOfIgnoredPseudosInLC.toArray(new String[listOfIgnoredPseudosInLC.size()]);
    }
}
