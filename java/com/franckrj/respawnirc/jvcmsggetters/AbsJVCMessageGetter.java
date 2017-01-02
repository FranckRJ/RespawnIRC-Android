package com.franckrj.respawnirc.jvcmsggetters;

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
    protected AbsGetJVCLastMessages currentAsyncTaskForGetMessage = null;
    protected Activity parentActivity = null;
    protected String cookieListInAString = "";
    protected NewMessagesListener listenerForNewMessages = null;
    protected NewGetterStateListener listenerForNewGetterState = null;
    protected NewForumAndTopicNameAvailable listenerForNewForumAndTopicName = null;
    protected JVCParser.ForumAndTopicName currentNames = new JVCParser.ForumAndTopicName();
    protected Boolean isInFavs = null;
    protected String topicID = "";

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

    public Boolean getIsInFavs() {
        return isInFavs;
    }

    public String getTopicID() {
        return topicID;
    }

    public void setIsInFavs(Boolean newVal) {
        isInFavs = newVal;
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

    public void setListenerForNewForumAndTopicName(NewForumAndTopicNameAvailable thisListener) {
        listenerForNewForumAndTopicName = thisListener;
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForGetMessage != null) {
            currentAsyncTaskForGetMessage.cancel(true);
            currentAsyncTaskForGetMessage = null;
        }

        if (listenerForNewGetterState != null) {
            listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        urlForTopic = savedInstanceState.getString(parentActivity.getString(R.string.saveTopicUrlToFetch), "");
        latestListOfInputInAString = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestListOfInputInAString), null);
        latestAjaxInfos.list = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoList), null);
        latestAjaxInfos.mod = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoMod), null);
        latestAjaxInfos.pref = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoPref), null);
        lastIdOfMessage = savedInstanceState.getLong(parentActivity.getString(R.string.saveLastIdOfMessage), 0);
        topicID = savedInstanceState.getString(parentActivity.getString(R.string.saveTopicID), "");
        if (savedInstanceState.containsKey(parentActivity.getString(R.string.saveTopicIsInFav))) {
            isInFavs = savedInstanceState.getBoolean(parentActivity.getString(R.string.saveTopicIsInFav), false);
        } else {
            isInFavs = null;
        }
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(parentActivity.getString(R.string.saveTopicUrlToFetch), urlForTopic);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestListOfInputInAString), latestListOfInputInAString);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoList), latestAjaxInfos.list);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoMod), latestAjaxInfos.mod);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoPref), latestAjaxInfos.pref);
        savedInstanceState.putLong(parentActivity.getString(R.string.saveLastIdOfMessage), lastIdOfMessage);
        savedInstanceState.putString(parentActivity.getString(R.string.saveTopicID), topicID);
        if (isInFavs != null) {
            savedInstanceState.putBoolean(parentActivity.getString(R.string.saveTopicIsInFav), isInFavs);
        }
    }

    protected abstract class AbsGetJVCLastMessages extends AsyncTask<String, Void, TopicPageInfos> {
    }

    protected static class TopicPageInfos {
        ArrayList<JVCParser.MessageInfos> listOfMessages;
        String lastPageLink;
        String nextPageLink;
        String listOfInputInAString;
        JVCParser.AjaxInfos ajaxInfosOfThisPage;
        JVCParser.ForumAndTopicName newNames;
        Boolean newIsInFavs;
        String newTopicID;
    }

    public interface NewForumAndTopicNameAvailable {
        void getNewForumAndTopicName(JVCParser.ForumAndTopicName newNames);
    }

    public interface NewMessagesListener {
        void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages);
    }

    public interface NewGetterStateListener {
        void newStateSetted(int newState);
    }

    public abstract boolean reloadTopic();
}
