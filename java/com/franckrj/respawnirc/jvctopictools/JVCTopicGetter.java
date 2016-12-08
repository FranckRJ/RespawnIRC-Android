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
    private String forumName = "";

    public JVCTopicGetter(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    public String getUrlForForum() {
        return urlForForum;
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

    public boolean startGetMessagesOfThisPage(String newUrlOfPage) {
        if (currentAsyncTaskForGetTopic == null && !newUrlOfPage.isEmpty()) {
            urlForForum = newUrlOfPage;
            currentAsyncTaskForGetTopic = new GetJVCLastTopics();
            currentAsyncTaskForGetTopic.execute(urlForForum, cookieListInAString);
            return true;
        } else {
            return false;
        }
    }

    public boolean reloadForum() {
        return startGetMessagesOfThisPage(urlForForum);
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForGetTopic != null) {
            currentAsyncTaskForGetTopic.cancel(false);
            currentAsyncTaskForGetTopic = null;
        }

        if (listenerForNewGetterState != null) {
            listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        urlForForum = savedInstanceState.getString(parentActivity.getString(R.string.saveForumUrlToFetch), "");
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(parentActivity.getString(R.string.saveForumUrlToFetch), urlForForum);
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
                if (!infoOfCurrentPage.newUrlForForumPage.isEmpty()) {
                    urlForForum = infoOfCurrentPage.newUrlForForumPage;
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

    public static class ForumPageInfos {
        ArrayList<JVCParser.TopicInfos> listOfTopics;
        String newUrlForForumPage;
        String newForumName;
    }

    public interface NewForumNameAvailable {
        void getNewForumName(String newForumName);
    }

    public interface NewTopicsListener {
        void getNewTopics(ArrayList<JVCParser.TopicInfos> listOfNewTopics);
    }

    public interface NewGetterStateListener {
        void newStateSetted(int newState);
    }
}