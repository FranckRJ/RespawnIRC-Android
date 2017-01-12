package com.franckrj.respawnirc.jvctopictools;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class JVCTopicGetter {
    public static final int STATE_LOADING = 0;
    public static final int STATE_NOT_LOADING = 1;

    private String urlForForum = "";
    private GetJVCLastTopics currentAsyncTaskForGetTopic = null;
    private Activity parentActivity = null;
    private String cookieListInAString = "";
    private NewTopicsListener listenerForNewTopics = null;
    private NewGetterStateListener listenerForNewGetterState = null;
    private NewForumNameAvailable listenerForNewForumName = null;
    private ForumLinkChanged listenerForForumLinkChanged = null;
    private String forumName = "";
    private JVCParser.AjaxInfos latestAjaxInfos = new JVCParser.AjaxInfos();
    private Boolean isInFavs = null;
    private String latestListOfInputInAString = null;

    public JVCTopicGetter(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return latestAjaxInfos;
    }

    public Boolean getIsInFavs() {
        return isInFavs;
    }

    public String getLatestListOfInputInAString() {
        return latestListOfInputInAString;
    }

    public void setIsInFavs(Boolean newVal) {
        isInFavs = newVal;
    }

    public void setCookieListInAString(String newCookieListInAString) {
        cookieListInAString = newCookieListInAString;
    }

    public void setListenerForNewTopics(NewTopicsListener thisListener) {
        listenerForNewTopics = thisListener;
    }

    public void setListenerForNewGetterState(NewGetterStateListener thisListener) {
        listenerForNewGetterState = thisListener;
    }

    public void setListenerForNewForumName(NewForumNameAvailable thisListener) {
        listenerForNewForumName = thisListener;
    }

    public void setListenerForForumLinkChanged(ForumLinkChanged thisListener) {
        listenerForForumLinkChanged = thisListener;
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage) {
        if (currentAsyncTaskForGetTopic == null && !newUrlOfPage.isEmpty()) {
            urlForForum = newUrlOfPage;
            currentAsyncTaskForGetTopic = new GetJVCLastTopics();
            currentAsyncTaskForGetTopic.execute(urlForForum, cookieListInAString);
            return true;
        } else {
            urlForForum = newUrlOfPage;
            return false;
        }
    }

    public boolean reloadForum() {
        return startGetMessagesOfThisPage(urlForForum);
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForGetTopic != null) {
            currentAsyncTaskForGetTopic.cancel(true);
            currentAsyncTaskForGetTopic = null;
        }

        if (listenerForNewGetterState != null) {
            listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        urlForForum = savedInstanceState.getString(parentActivity.getString(R.string.saveForumUrlToFetch), "");
        latestAjaxInfos.pref = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoPref), null);
        latestListOfInputInAString = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestListOfInputInAString), null);
        if (savedInstanceState.containsKey(parentActivity.getString(R.string.saveForumIsInFav))) {
            isInFavs = savedInstanceState.getBoolean(parentActivity.getString(R.string.saveForumIsInFav), false);
        } else {
            isInFavs = null;
        }
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(parentActivity.getString(R.string.saveForumUrlToFetch), urlForForum);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoPref), latestAjaxInfos.pref);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestListOfInputInAString), latestListOfInputInAString);
        if (isInFavs != null) {
            savedInstanceState.putBoolean(parentActivity.getString(R.string.saveForumIsInFav), isInFavs);
        }
    }

    private class GetJVCLastTopics extends AsyncTask<String, Void, ForumPageInfos> {
        @Override
        protected void onPreExecute() {
            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_LOADING);
            }
        }

        @Override
        protected ForumPageInfos doInBackground(String... params) {
            if (params.length > 1) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                ForumPageInfos newPageInfos = null;
                String pageContent;
                currentWebInfos.followRedirects = true;
                pageContent = WebManager.sendRequest(params[0], "GET", "", params[1], currentWebInfos);

                if (pageContent != null) {
                    newPageInfos = new ForumPageInfos();
                    newPageInfos.listOfTopics = JVCParser.getTopicsOfThisPage(pageContent);
                    newPageInfos.newUrlForForumPage = currentWebInfos.currentUrl;
                    newPageInfos.newForumName = JVCParser.getForumNameInForumPage(pageContent);
                    newPageInfos.newLatestAjaxInfos = JVCParser.getAllAjaxInfos(pageContent);
                    newPageInfos.newIsInFavs = JVCParser.getIsInFavsFromPage(pageContent);
                    newPageInfos.newListOfInputInAString = JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageContent);
                }

                return newPageInfos;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ForumPageInfos infoOfCurrentPage) {
            super.onPostExecute(infoOfCurrentPage);
            currentAsyncTaskForGetTopic = null;

            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
            }

            if (infoOfCurrentPage != null) {
                latestAjaxInfos = infoOfCurrentPage.newLatestAjaxInfos;
                isInFavs = infoOfCurrentPage.newIsInFavs;
                latestListOfInputInAString = infoOfCurrentPage.newListOfInputInAString;

                if (!infoOfCurrentPage.newUrlForForumPage.isEmpty()) {
                    urlForForum = infoOfCurrentPage.newUrlForForumPage;
                    if (listenerForForumLinkChanged != null) {
                        listenerForForumLinkChanged.updateForumLink(urlForForum);
                    }
                }

                if (!infoOfCurrentPage.newForumName.equals(forumName)) {
                    forumName = infoOfCurrentPage.newForumName;
                    if (listenerForNewForumName != null) {
                        listenerForNewForumName.getNewForumName(forumName);
                    }
                }

                if (listenerForNewTopics != null) {
                    listenerForNewTopics.getNewTopics(infoOfCurrentPage.listOfTopics);
                }
            } else {
                if (listenerForNewTopics != null) {
                    listenerForNewTopics.getNewTopics(new ArrayList<JVCParser.TopicInfos>());
                }
            }
        }
    }

    private static class ForumPageInfos {
        ArrayList<JVCParser.TopicInfos> listOfTopics;
        String newUrlForForumPage;
        String newForumName;
        JVCParser.AjaxInfos newLatestAjaxInfos;
        Boolean newIsInFavs;
        String newListOfInputInAString;

    }

    public interface NewForumNameAvailable {
        void getNewForumName(String newForumName);
    }

    public interface ForumLinkChanged {
        void updateForumLink(String newForumLink);
    }

    public interface NewTopicsListener {
        void getNewTopics(ArrayList<JVCParser.TopicInfos> listOfNewTopics);
    }

    public interface NewGetterStateListener {
        void newStateSetted(int newState);
    }
}
