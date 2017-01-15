package com.franckrj.respawnirc.jvcmsggetters;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

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
    protected NewReasonForTopicLock listenerForNewReasonForTopicLock = null;
    protected String lockReason = "";

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

    public void setListenerForNewReasonForTopicLock(NewReasonForTopicLock thisListener) {
        listenerForNewReasonForTopicLock = thisListener;
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
        lockReason = savedInstanceState.getString(parentActivity.getString(R.string.saveLockReason), "");
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
        savedInstanceState.putString(parentActivity.getString(R.string.saveLockReason), lockReason);
        if (isInFavs != null) {
            savedInstanceState.putBoolean(parentActivity.getString(R.string.saveTopicIsInFav), isInFavs);
        }
    }

    protected TopicPageInfos downloadAndParseTopicPage(String topicLink, String cookies) {
        WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
        TopicPageInfos newPageInfos = null;
        String pageContent;
        currentWebInfos.followRedirects = false;
        pageContent = WebManager.sendRequest(topicLink, "GET", "", cookies, currentWebInfos);

        if (pageContent != null) {
            newPageInfos = new TopicPageInfos();
            newPageInfos.lastPageLink = JVCParser.getLastPageOfTopic(pageContent);
            newPageInfos.nextPageLink = JVCParser.getNextPageOfTopic(pageContent);
            newPageInfos.listOfMessages = JVCParser.getMessagesOfThisPage(pageContent);
            newPageInfos.listOfInputInAString = JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageContent);
            newPageInfos.ajaxInfosOfThisPage = JVCParser.getAllAjaxInfos(pageContent);
            newPageInfos.newNames = JVCParser.getForumAndTopicNameInTopicPage(pageContent);
            newPageInfos.newIsInFavs = JVCParser.getIsInFavsFromPage(pageContent);
            newPageInfos.newTopicID = JVCParser.getTopicIDInThisTopicPage(pageContent);
            newPageInfos.newLockReason = JVCParser.getLockReasonFromPage(pageContent);
        }

        return newPageInfos;
    }

    protected void fillBaseClassInfoFromPageInfo(TopicPageInfos infoOfCurrentPage) {
        latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
        latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;
        isInFavs = infoOfCurrentPage.newIsInFavs;
        topicID = infoOfCurrentPage.newTopicID;

        if (!latestListOfInputInAString.isEmpty()) {
            latestListOfInputInAString = latestListOfInputInAString + "&form_alias_rang=1";
        }

        if (!infoOfCurrentPage.newNames.equals(currentNames)) {
            currentNames = infoOfCurrentPage.newNames;
            if (listenerForNewForumAndTopicName != null) {
                listenerForNewForumAndTopicName.getNewForumAndTopicName(currentNames);
            }
        }

        if (!Utils.compareStrings(infoOfCurrentPage.newLockReason, lockReason)) {
            lockReason = infoOfCurrentPage.newLockReason;
            if (listenerForNewReasonForTopicLock != null) {
                listenerForNewReasonForTopicLock.getNewLockReason(lockReason);
            }
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
        String newLockReason;
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

    public interface NewReasonForTopicLock {
        void getNewLockReason(String newReason);
    }

    public abstract boolean reloadTopic();
}
