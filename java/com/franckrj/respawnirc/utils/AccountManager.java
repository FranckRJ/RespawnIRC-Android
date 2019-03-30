package com.franckrj.respawnirc.utils;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static List<AccountInfos> listOfAccountsInReserve = new ArrayList<>();
    private static AccountInfos currentAccount = null;

    public static void addOrReplaceThisAccountInReserveList(AccountInfos newAccount) {
        removeAccountFromReserveList(newAccount.pseudo);
        listOfAccountsInReserve.add(newAccount);
    }

    public static void removeAccountFromReserveList(String accountPseudo) {
        for (int i = 0; i < listOfAccountsInReserve.size(); ++i) {
            if (listOfAccountsInReserve.get(i).pseudo.toLowerCase().equals(accountPseudo.toLowerCase())) {
                listOfAccountsInReserve.remove(i);
                break;
            }
        }
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

    public static void setCurrentAccount(AccountInfos newCurrentAccount) {
        currentAccount = new AccountInfos(newCurrentAccount.pseudo, newCurrentAccount.cookie, newCurrentAccount.isModo);
        PrefsManager.putString(PrefsManager.StringPref.Names.PSEUDO_OF_USER, currentAccount.pseudo);
        PrefsManager.putString(PrefsManager.StringPref.Names.COOKIES_LIST, currentAccount.cookie);
        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, currentAccount.isModo);
        PrefsManager.applyChanges();
    }

    public static void replaceCurrentAccountAndAddInReserve(AccountInfos newCurrentAccount) {
        addOrReplaceThisAccountInReserveList(getCurrentAccount());
        removeAccountFromReserveList(newCurrentAccount.pseudo);
        setCurrentAccount(newCurrentAccount);
        AccountManager.saveListOfAccountsInReserve();
    }

    public static void setCurrentAccountIsModo(boolean newVal) {
        currentAccount = new AccountInfos(currentAccount.pseudo, currentAccount.cookie, newVal);
        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, currentAccount.isModo);
        PrefsManager.applyChanges();
    }

    public static AccountInfos getReserveAccountAtIndex(int index) {
        if (index >= listOfAccountsInReserve.size()) {
            return new AccountInfos();
        } else {
            return (listOfAccountsInReserve.get(index));
        }
    }

    public static List<String> getListOfReserveAccountPseudo() {
        List<String> listOfPseudo = new ArrayList<>();

        for (AccountInfos thisAccount : listOfAccountsInReserve) {
            listOfPseudo.add(thisAccount.pseudo);
        }

        return listOfPseudo;
    }

    public static void loadListOfAccountsInReserve() {
        int numberOfAccounts = PrefsManager.getInt(PrefsManager.IntPref.Names.RESERVE_ACCOUNT_ARRAY_SIZE);

        listOfAccountsInReserve.clear();
        for (int i = 0; i < numberOfAccounts; ++i) {
            String pseudo = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_PSEUDO, String.valueOf(i));
            String cookie = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_COOKIE, String.valueOf(i));
            boolean isModo = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_IS_MODO, String.valueOf(i)).equals("true");
            listOfAccountsInReserve.add(new AccountInfos(pseudo, cookie, isModo));
        }
    }

    public static void saveListOfAccountsInReserve() {
        int numberOfAccounts = PrefsManager.getInt(PrefsManager.IntPref.Names.RESERVE_ACCOUNT_ARRAY_SIZE);

        for (int i = 0; i < numberOfAccounts; ++i) {
            PrefsManager.removeStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_PSEUDO, String.valueOf(i));
            PrefsManager.removeStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_COOKIE, String.valueOf(i));
            PrefsManager.removeStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_IS_MODO, String.valueOf(i));
        }

        numberOfAccounts = listOfAccountsInReserve.size();
        PrefsManager.putInt(PrefsManager.IntPref.Names.RESERVE_ACCOUNT_ARRAY_SIZE, numberOfAccounts);

        for (int i = 0; i < numberOfAccounts; ++i) {
            AccountInfos accountToSave = listOfAccountsInReserve.get(i);
            PrefsManager.putStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_PSEUDO, String.valueOf(i), accountToSave.pseudo);
            PrefsManager.putStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_COOKIE, String.valueOf(i), accountToSave.cookie);
            PrefsManager.putStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_IS_MODO, String.valueOf(i), (accountToSave.isModo ? "true" : "false"));
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
