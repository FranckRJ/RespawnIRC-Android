package com.franckrj.respawnirc.utils;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {
    private static List<AccountInfos> listOfAccountsInReserve = new ArrayList<>();

    public static void addThisAccountToReserveList(AccountInfos newAccount) {
        for (int i = 0; i < listOfAccountsInReserve.size(); ++i) {
            if (listOfAccountsInReserve.get(i).pseudo.toLowerCase().equals(newAccount.pseudo.toLowerCase())) {
                return;
            }
        }
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
        AccountInfos currentAccount = new AccountInfos();

        currentAccount.pseudo = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_USER);
        currentAccount.cookie = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);
        currentAccount.isModo = PrefsManager.getBool(PrefsManager.BoolPref.Names.USER_IS_MODO);
        return currentAccount;
    }

    public static void setCurrentAccount(AccountInfos newCurrentAccount) {
        PrefsManager.putString(PrefsManager.StringPref.Names.PSEUDO_OF_USER, newCurrentAccount.pseudo);
        PrefsManager.putString(PrefsManager.StringPref.Names.COOKIES_LIST, newCurrentAccount.cookie);
        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, newCurrentAccount.isModo);
        PrefsManager.applyChanges();
    }

    public static void replaceCurrentAccountAndAddInReserve(AccountInfos newCurrentAccount) {
        addThisAccountToReserveList(getCurrentAccount());
        removeAccountFromReserveList(newCurrentAccount.pseudo);
        setCurrentAccount(newCurrentAccount);
    }

    public static void setCurrentAccountIsModo(boolean newVal) {
        PrefsManager.putBool(PrefsManager.BoolPref.Names.USER_IS_MODO, newVal);
        PrefsManager.applyChanges();
    }

    public static void loadListOfAccountsInReserve() {
        int numberOfAccounts = PrefsManager.getInt(PrefsManager.IntPref.Names.RESERVE_ACCOUNT_ARRAY_SIZE);

        listOfAccountsInReserve.clear();
        for (int i = 0; i < numberOfAccounts; ++i) {
            AccountInfos newAccount = new AccountInfos();
            newAccount.pseudo = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_PSEUDO, String.valueOf(i));
            newAccount.cookie = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_COOKIE, String.valueOf(i));
            newAccount.isModo = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.RESERVE_ACCOUNT_IS_MODO, String.valueOf(i)).equals("true");
            listOfAccountsInReserve.add(newAccount);
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
        public String pseudo = "";
        public String cookie = "";
        public boolean isModo = false;

        public AccountInfos() {
            //vide
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
