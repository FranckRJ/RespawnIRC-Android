package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class JVCForumGetter {
    public static final int STATE_LOADING = 0;
    public static final int STATE_NOT_LOADING = 1;

    private static final String SAVE_FORUM_URL_TO_FETCH = "saveForumUrlToFetch";
    private static final String SAVE_LATEST_AJAX_INFO_PREF = "saveLatestAjaxInfoPref";
    private static final String SAVE_LATEST_LIST_OF_INPUT = "saveLatestListOfInputInAString";
    private static final String SAVE_FORUM_IS_IN_FAV = "saveForumIsInFav";
    private static final String SAVE_IS_IN_SEARCH_MODE = "saveIsInSearchMode";
    private static final String SAVE_SEARCH_IS_EMPTY = "saveSearchIsEmpty";

    private String urlForForum = "";
    private GetJVCLastTopics currentAsyncTaskForGetTopic = null;
    private String cookieListInAString = "";
    private NewTopicsListener listenerForNewTopics = null;
    private NewGetterStateListener listenerForNewGetterState = null;
    private NewForumNameAvailable listenerForNewForumName = null;
    private ForumLinkChanged listenerForForumLinkChanged = null;
    private String forumName = "";
    private JVCParser.AjaxInfos latestAjaxInfos = new JVCParser.AjaxInfos();
    private Boolean isInFavs = null;
    private String latestListOfInputInAString = null;
    private String latestNumberOfMP = null;
    private NewNumberOfMPSetted listenerForNewNumberOfMP = null;
    private boolean isInSearchMode = false;
    private boolean searchIsEmptyAndItsNotAFail = false;

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return latestAjaxInfos;
    }

    public Boolean getIsInFavs() {
        return isInFavs;
    }

    public String getLatestListOfInputInAString() {
        return latestListOfInputInAString;
    }

    public boolean getSearchIsEmptyAndItsNotAFail() {
        return searchIsEmptyAndItsNotAFail;
    }

    public boolean getIsInSearchMode() {
        return isInSearchMode;
    }

    public void setIsInFavs(Boolean newVal) {
        isInFavs = newVal;
    }

    public void setIsInSearchMode(boolean newVal) {
        isInSearchMode = newVal;
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

    public void setListenerForNewNumberOfMP(NewNumberOfMPSetted thisListener) {
        listenerForNewNumberOfMP = thisListener;
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage) {
        return startGetMessagesOfThisPage(newUrlOfPage, false);
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage, boolean useBiggerTimeoutTime) {
        if (currentAsyncTaskForGetTopic == null && !newUrlOfPage.isEmpty()) {
            urlForForum = newUrlOfPage;
            currentAsyncTaskForGetTopic = new GetJVCLastTopics(isInSearchMode, useBiggerTimeoutTime);
            currentAsyncTaskForGetTopic.execute(urlForForum, cookieListInAString);
            return true;
        } else {
            urlForForum = newUrlOfPage;
            return false;
        }
    }

    public boolean reloadForum() {
        return reloadForum(false);
    }

    public boolean reloadForum(boolean useBiggerTimeoutTime) {
        return startGetMessagesOfThisPage(urlForForum, useBiggerTimeoutTime);
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
        urlForForum = savedInstanceState.getString(SAVE_FORUM_URL_TO_FETCH, "");
        latestAjaxInfos.pref = savedInstanceState.getString(SAVE_LATEST_AJAX_INFO_PREF, null);
        latestListOfInputInAString = savedInstanceState.getString(SAVE_LATEST_LIST_OF_INPUT, null);
        isInSearchMode = savedInstanceState.getBoolean(SAVE_IS_IN_SEARCH_MODE, false);
        searchIsEmptyAndItsNotAFail = savedInstanceState.getBoolean(SAVE_SEARCH_IS_EMPTY, false);
        if (savedInstanceState.containsKey(SAVE_FORUM_IS_IN_FAV)) {
            isInFavs = savedInstanceState.getBoolean(SAVE_FORUM_IS_IN_FAV, false);
        } else {
            isInFavs = null;
        }
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_FORUM_URL_TO_FETCH, urlForForum);
        savedInstanceState.putString(SAVE_LATEST_AJAX_INFO_PREF, latestAjaxInfos.pref);
        savedInstanceState.putString(SAVE_LATEST_LIST_OF_INPUT, latestListOfInputInAString);
        savedInstanceState.putBoolean(SAVE_IS_IN_SEARCH_MODE, isInSearchMode);
        savedInstanceState.putBoolean(SAVE_SEARCH_IS_EMPTY, searchIsEmptyAndItsNotAFail);
        if (isInFavs != null) {
            savedInstanceState.putBoolean(SAVE_FORUM_IS_IN_FAV, isInFavs);
        }
    }

    private class GetJVCLastTopics extends AsyncTask<String, Void, ForumPageInfos> {
        boolean isInSearchMode = false;
        boolean useBiggerTimeoutTime = false;

        public GetJVCLastTopics(boolean newIsInSearchMode, boolean newUseBiggerTimeoutTime) {
            isInSearchMode = newIsInSearchMode;
            useBiggerTimeoutTime = newUseBiggerTimeoutTime;
        }

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
                currentWebInfos.useBiggerTimeoutTime = useBiggerTimeoutTime;
                pageContent = WebManager.sendRequest(params[0], "GET", "", params[1], currentWebInfos);

                if (pageContent != null) {
                    newPageInfos = new ForumPageInfos();
                    newPageInfos.listOfTopics = JVCParser.getTopicsOfThisPage(pageContent);
                    newPageInfos.newUrlForForumPage = currentWebInfos.currentUrl;
                    newPageInfos.newForumName = JVCParser.getForumNameInForumPage(pageContent);
                    newPageInfos.newLatestAjaxInfos = JVCParser.getAllAjaxInfos(pageContent);
                    newPageInfos.newIsInFavs = JVCParser.getIsInFavsFromPage(pageContent);
                    newPageInfos.newListOfInputInAString = JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageContent);
                    newPageInfos.newNumberOfMp = JVCParser.getNumberOfMPFromPage(pageContent);
                    if (isInSearchMode) {
                        newPageInfos.newSearchIsEmpty = JVCParser.getSearchIsEmptyInPage(pageContent);
                    }
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
                searchIsEmptyAndItsNotAFail = infoOfCurrentPage.newSearchIsEmpty;

                if (!latestListOfInputInAString.isEmpty()) {
                    latestListOfInputInAString = latestListOfInputInAString + "&spotify_topic=&submit_sondage=0&question_sondage=&reponse_sondage[]=&form_alias_rang=1";
                }

                if (!infoOfCurrentPage.newUrlForForumPage.isEmpty()) {
                    if (!isInSearchMode) {
                        urlForForum = infoOfCurrentPage.newUrlForForumPage;
                        if (listenerForForumLinkChanged != null) {
                            listenerForForumLinkChanged.updateForumLink(urlForForum);
                        }
                    } else {
                        searchIsEmptyAndItsNotAFail = true;
                    }
                }

                if (!infoOfCurrentPage.newForumName.equals(forumName)) {
                    forumName = infoOfCurrentPage.newForumName;
                    if (listenerForNewForumName != null) {
                        listenerForNewForumName.getNewForumName(forumName);
                    }
                }

                if (!Utils.stringsAreEquals(latestNumberOfMP, infoOfCurrentPage.newNumberOfMp)) {
                    latestNumberOfMP = infoOfCurrentPage.newNumberOfMp;
                    if (listenerForNewNumberOfMP != null) {
                        listenerForNewNumberOfMP.getNewNumberOfMP(latestNumberOfMP);
                    }
                }

                if (listenerForNewTopics != null) {
                    listenerForNewTopics.getNewTopics(infoOfCurrentPage.listOfTopics);
                }
            } else {
                searchIsEmptyAndItsNotAFail = false;
                if (listenerForNewTopics != null) {
                    listenerForNewTopics.getNewTopics(new ArrayList<JVCParser.TopicInfos>());
                }
            }
        }
    }

    private static class ForumPageInfos {
        public ArrayList<JVCParser.TopicInfos> listOfTopics;
        public String newUrlForForumPage;
        public String newForumName;
        public JVCParser.AjaxInfos newLatestAjaxInfos;
        public Boolean newIsInFavs;
        public String newListOfInputInAString;
        public String newNumberOfMp;
        public boolean newSearchIsEmpty;
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

    public interface NewNumberOfMPSetted {
        void getNewNumberOfMP(String newNumber);
    }
}
