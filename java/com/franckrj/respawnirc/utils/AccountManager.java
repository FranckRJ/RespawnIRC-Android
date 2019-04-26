package com.franckrj.respawnirc.utils;

import android.webkit.CookieManager;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static List<AccountInfos> listOfAccounts = new ArrayList<>();
    private static AccountInfos currentAccount = null;
    private static String allAccountsPseudoRegex = null;

    /* Retourne true en cas d'ajout, false en cas de replacement ou si le pseudo est invalide. */
    public static boolean addOrReplaceThisAccountInList(AccountInfos newAccount) {
        if (!newAccount.pseudo.isEmpty()) {
            int indexIfExisted = getIndexOfThisAccount(newAccount.pseudo);

            if (indexIfExisted < 0 || indexIfExisted > listOfAccounts.size()) {
                listOfAccounts.add(newAccount);
                allAccountsPseudoRegex = null;
                return true;
            } else {
                listOfAccounts.set(indexIfExisted, newAccount);
                return false;
            }
        } else {
            return false;
        }
    }

    public static int removeAccountFromListAndUpdateCurrentAccountIfNeeded(String accountPseudo) {
        allAccountsPseudoRegex = null;
        for (int i = 0; i < listOfAccounts.size(); ++i) {
            if (listOfAccounts.get(i).pseudo.toLowerCase().equals(accountPseudo.toLowerCase())) {
                listOfAccounts.remove(i);

                if (getCurrentAccount().pseudo.toLowerCase().equals(accountPseudo.toLowerCase())) {
                    if (listOfAccounts.isEmpty()) {
                        setCurrentAccount(new AccountInfos("", "", false));
                    } else {
                        setCurrentAccount(listOfAccounts.get(0));
                    }
                }

                return i;
            }
        }
        return -1;
    }

    public static AccountInfos getCurrentAccount() {
        if (currentAccount == null) {
            String pseudo = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_USER);
            String cookie = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);
            boolean isModo = PrefsManager.getBool(PrefsManager.BoolPref.Names.USER_IS_MODO);

            currentAccount = new AccountInfos(pseudo, cookie, isModo);
        }
        return currentAccount;
    }

    public static String getAllAccountsPseudoRegex() {
        if (allAccountsPseudoRegex == null) {
            StringBuilder strBuilder = new StringBuilder();
            if (!listOfAccounts.isEmpty()) {
                boolean isFirstAccount = true;
                strBuilder.append("(?:");
                for (AccountInfos thisAccount : listOfAccounts) {
                    if (!thisAccount.pseudo.isEmpty()) {
                        if (isFirstAccount) {
                            isFirstAccount = false;
                        } else {
                            strBuilder.append("|");
                        }
                        strBuilder.append(thisAccount.pseudo.toLowerCase().replace("[", "\\[").replace("]", "\\]"));
                    }
                }
                strBuilder.append(")");
            }
            allAccountsPseudoRegex = strBuilder.toString();
        }
        return allAccountsPseudoRegex;
    }

    public static void setCurrentAccount(AccountInfos newCurrentAccount) {
        currentAccount = new AccountInfos(newCurrentAccount.pseudo, newCurrentAccount.cookie, newCurrentAccount.isModo);
        Undeprecator.cookieManagerRemoveAllCookiesAndSetDefault(CookieManager.getInstance());
        PrefsManager.putString(PrefsManager.StringPref.Names.PSEUDO_OF_USER, currentAccount.pseudo);
        PrefsManager.putString(PrefsManager.StringPref.Names.COOKIES_LIST, currentAccount.cookie);
        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, currentAccount.isModo);
        PrefsManager.applyChanges();
        if (!currentAccount.pseudo.isEmpty()) {
            addOrReplaceThisAccountInList(currentAccount);
            AccountManager.saveListOfAccounts();
        }
        allAccountsPseudoRegex = null;
    }

    public static void setCurrentAccountIsModo(boolean newVal) {
        currentAccount = new AccountInfos(currentAccount.pseudo, currentAccount.cookie, newVal);
        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, currentAccount.isModo);
        PrefsManager.applyChanges();
        addOrReplaceThisAccountInList(currentAccount);
        saveListOfAccounts();
    }

    public static AccountInfos getAccountAtIndex(int index) {
        if (index < 0 || index >= listOfAccounts.size()) {
            return new AccountInfos();
        } else {
            return (listOfAccounts.get(index));
        }
    }

    public static List<String> getListOfAccountPseudo() {
        List<String> listOfPseudo = new ArrayList<>();

        for (AccountInfos thisAccount : listOfAccounts) {
            listOfPseudo.add(thisAccount.pseudo);
        }

        return listOfPseudo;
    }

    public static int getIndexOfThisAccount(String pseudoToSearch) {
        pseudoToSearch = pseudoToSearch.toLowerCase();

        for (int i = 0; i < listOfAccounts.size(); ++i) {
            if (pseudoToSearch.equals(listOfAccounts.get(i).pseudo.toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    public static void loadListOfAccounts() {
        /* Si le pseudo du compte actuel est vide c'est qu'il n'y a pas de compte actuel donc on part du principe qu'il a été trouvé. */
        boolean hasFoundCurrentAccount = getCurrentAccount().pseudo.isEmpty();
        int numberOfAccounts = PrefsManager.getInt(PrefsManager.IntPref.Names.ACCOUNT_ARRAY_SIZE);

        listOfAccounts.clear();
        for (int i = 0; i < numberOfAccounts; ++i) {
            String pseudo = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_PSEUDO, String.valueOf(i));
            String cookie = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_COOKIE, String.valueOf(i));
            boolean isModo = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_IS_MODO, String.valueOf(i)).equals("true");
            listOfAccounts.add(new AccountInfos(pseudo, cookie, isModo));
            if (!hasFoundCurrentAccount && pseudo.toLowerCase().equals(getCurrentAccount().pseudo.toLowerCase())) {
                hasFoundCurrentAccount = true;
            }
        }
        if (!hasFoundCurrentAccount) {
            listOfAccounts.add(getCurrentAccount());
            saveListOfAccounts();
        }
        allAccountsPseudoRegex = null;
    }

    public static void saveListOfAccounts() {
        int numberOfAccounts = PrefsManager.getInt(PrefsManager.IntPref.Names.ACCOUNT_ARRAY_SIZE);

        for (int i = 0; i < numberOfAccounts; ++i) {
            PrefsManager.removeStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_PSEUDO, String.valueOf(i));
            PrefsManager.removeStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_COOKIE, String.valueOf(i));
            PrefsManager.removeStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_IS_MODO, String.valueOf(i));
        }

        numberOfAccounts = listOfAccounts.size();
        PrefsManager.putInt(PrefsManager.IntPref.Names.ACCOUNT_ARRAY_SIZE, numberOfAccounts);

        for (int i = 0; i < numberOfAccounts; ++i) {
            AccountInfos accountToSave = listOfAccounts.get(i);
            PrefsManager.putStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_PSEUDO, String.valueOf(i), accountToSave.pseudo);
            PrefsManager.putStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_COOKIE, String.valueOf(i), accountToSave.cookie);
            PrefsManager.putStringWithSufix(PrefsManager.StringPref.Names.ACCOUNT_IS_MODO, String.valueOf(i), (accountToSave.isModo ? "true" : "false"));
        }

        PrefsManager.applyChanges();
    }

    public static class AccountInfos {
        public final String pseudo;
        public final String cookie;
        public final boolean isModo;

        public AccountInfos() {
            this("", "", false);
        }

        public AccountInfos(String newPseudo, String newCookie, boolean newIsModo) {
            pseudo = newPseudo;
            cookie = newCookie;
            isModo = newIsModo;
        }

        @Override
        public boolean equals(Object another) {
            if (another instanceof AccountInfos) {
                AccountInfos otherAccount = ((AccountInfos) another);
                return (pseudo.equals(otherAccount.pseudo) && cookie.equals(otherAccount.cookie) && isModo == otherAccount.isModo);
            }
            return false;
        }
    }
}
