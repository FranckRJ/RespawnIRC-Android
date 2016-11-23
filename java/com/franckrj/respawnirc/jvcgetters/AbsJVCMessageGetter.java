package com.franckrj.respawnirc.jvcgetters;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.R;

import java.util.ArrayList;

public abstract class AbsJVCMessageGetter {
    public static final int STATE_LOADING = 0;
    public static final int STATE_NOT_LOADING = 1;

    protected String urlForTopic = "";
    protected String latestListOfInputInAString = null;
    protected JVCParser.AjaxInfos latestAjaxInfos = new JVCParser.AjaxInfos();
    protected long lastIdOfMessage = 0;
    protected AbsGetJVCLastMessage currentAsyncTaskForGetMessage = null;
    protected Activity parentActivity = null;
    protected String cookieListInAString = "";
    protected NewMessagesListener listenerForNewMessages = null;
    protected NewGetterStateListener listenerForNewGetterState = null;

    public AbsJVCMessageGetter(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    public String getUrlForTopic() {
        return urlForTopic;
    }

    public String getLatestListOfInputInAString() {
        return latestListOfInputInAString;
    }

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return latestAjaxInfos;
    }

    public long getLastIdOfMessage() {
        return lastIdOfMessage;
    }

    public void setCookieListInAString(String newCookieListInAString) {
        cookieListInAString = newCookieListInAString;
    }

    public void setListenerForNewMessages(NewMessagesListener thisListener) {
        listenerForNewMessages = thisListener;
    }

    public void setListenerForNewGetterState(NewGetterStateListener thisListener) {
        listenerForNewGetterState = thisListener;
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForGetMessage != null) {
            currentAsyncTaskForGetMessage.cancel(false);
            currentAsyncTaskForGetMessage = null;
        }

        if (listenerForNewGetterState != null) {
            listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        latestListOfInputInAString = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestListOfInputInAString), null);
        latestAjaxInfos.list = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoList), null);
        latestAjaxInfos.mod = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoMod), null);
        lastIdOfMessage = savedInstanceState.getLong(parentActivity.getString(R.string.saveLastIdOfMessage), 0);
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestListOfInputInAString), latestListOfInputInAString);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoList), latestAjaxInfos.list);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoMod), latestAjaxInfos.mod);
        savedInstanceState.putLong(parentActivity.getString(R.string.saveLastIdOfMessage), lastIdOfMessage);
    }

    protected abstract class AbsGetJVCLastMessage extends AsyncTask<String, Void, PageInfos> {
    }

    public static class PageInfos {
        ArrayList<JVCParser.MessageInfos> listOfMessages;
        String lastPageLink;
        String nextPageLink;
        String listOfInputInAString;
        JVCParser.AjaxInfos ajaxInfosOfThisPage;
    }

    public interface NewMessagesListener {
        void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages);
    }

    public interface NewGetterStateListener {
        void newStateSetted(int newState);
    }

    public abstract void reloadTopic();
}