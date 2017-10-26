package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.os.Bundle;

import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
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
    private static final String SAVE_LAST_TYPE_OF_ERROR = "saveLastTypeOfError";
    private static final String SAVE_USER_CAN_POST_AS_MODO = "saveUserCanPostAsModo";

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
    private ErrorType lastTypeOfError = ErrorType.NONE_OR_UNKNOWN;
    private boolean userCanPostAsModo = false;

    private final AbsWebRequestAsyncTask.RequestIsStarted getLastTopicsIsStartedListener = new AbsWebRequestAsyncTask.RequestIsStarted() {
        @Override
        public void onRequestIsStarted() {
            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_LOADING);
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<ForumPageInfos> getLastTopicsIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<ForumPageInfos>() {
        @Override
        public void onRequestIsFinished(ForumPageInfos reqResult) {
            currentAsyncTaskForGetTopic = null;
            lastTypeOfError = ErrorType.NONE_OR_UNKNOWN;

            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
            }

            if (reqResult != null) {
                boolean pageDownloadedIsAnalysable = true;

                if (!reqResult.newUrlForForumPage.isEmpty()) {
                    if (!isInSearchMode) {
                        if (JVCParser.checkIfForumAreSame(urlForForum, reqResult.newUrlForForumPage)) {
                            urlForForum = reqResult.newUrlForForumPage;
                            if (listenerForForumLinkChanged != null) {
                                listenerForForumLinkChanged.updateForumLink(urlForForum);
                            }
                        } else {
                            lastTypeOfError = ErrorType.FORUM_DOES_NOT_EXIST;
                            pageDownloadedIsAnalysable = false;
                        }
                    } else {
                        lastTypeOfError = ErrorType.SEARCH_IS_EMPTY_AND_ITS_NOT_A_FAIL;
                        pageDownloadedIsAnalysable = false;
                    }
                }

                if (pageDownloadedIsAnalysable) {
                    latestAjaxInfos = reqResult.newLatestAjaxInfos;
                    isInFavs = reqResult.newIsInFavs;
                    latestListOfInputInAString = reqResult.newListOfInputInAString;
                    userCanPostAsModo = reqResult.newUserCanPostAsModo;

                    if (reqResult.newSearchIsEmpty) {
                        lastTypeOfError = ErrorType.SEARCH_IS_EMPTY_AND_ITS_NOT_A_FAIL;
                    }

                    if (!latestListOfInputInAString.isEmpty()) {
                        latestListOfInputInAString = latestListOfInputInAString + "&spotify_topic=";
                    }

                    if (!reqResult.newForumName.equals(forumName)) {
                        forumName = reqResult.newForumName;
                        if (listenerForNewForumName != null) {
                            listenerForNewForumName.getNewForumName(forumName);
                        }
                    }

                    if (!Utils.stringsAreEquals(latestNumberOfMP, reqResult.newNumberOfMp)) {
                        latestNumberOfMP = reqResult.newNumberOfMp;
                        if (listenerForNewNumberOfMP != null) {
                            listenerForNewNumberOfMP.getNewNumberOfMP(latestNumberOfMP);
                        }
                    }

                    if (listenerForNewTopics != null) {
                        listenerForNewTopics.getNewTopics(reqResult.listOfTopics);
                    }

                    return;
                }
            }

            if (listenerForNewTopics != null) {
                listenerForNewTopics.getNewTopics(new ArrayList<JVCParser.TopicInfos>());
            }
        }
    };

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return latestAjaxInfos;
    }

    public Boolean getIsInFavs() {
        return isInFavs;
    }

    public String getLatestListOfInputInAString(boolean tryToPostAsModo) {
        if (!Utils.stringIsEmptyOrNull(latestListOfInputInAString)) {
            return latestListOfInputInAString + (tryToPostAsModo && userCanPostAsModo ? "&form_alias_rang=2" : "&form_alias_rang=1");
        } else {
            return latestListOfInputInAString;
        }
    }

    public ErrorType getLastTypeOfError() {
        return lastTypeOfError;
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

    public void setUrlForForumWithoutLoading(String newUrlOfPage) {
        urlForForum = newUrlOfPage;
    }

    public void startGetMessagesOfThisPage(String newUrlOfPage) {
        startGetMessagesOfThisPage(newUrlOfPage, false);
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage, boolean useBiggerTimeoutTime) {
        if (currentAsyncTaskForGetTopic == null && !newUrlOfPage.isEmpty()) {
            urlForForum = newUrlOfPage;
            currentAsyncTaskForGetTopic = new GetJVCLastTopics(isInSearchMode, useBiggerTimeoutTime);
            currentAsyncTaskForGetTopic.setRequestIsStartedListener(getLastTopicsIsStartedListener);
            currentAsyncTaskForGetTopic.setRequestIsFinishedListener(getLastTopicsIsFinishedListener);
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
            currentAsyncTaskForGetTopic.clearListenersAndCancel();
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
        lastTypeOfError = (ErrorType) savedInstanceState.getSerializable(SAVE_LAST_TYPE_OF_ERROR);
        userCanPostAsModo = savedInstanceState.getBoolean(SAVE_USER_CAN_POST_AS_MODO);
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
        savedInstanceState.putSerializable(SAVE_LAST_TYPE_OF_ERROR, lastTypeOfError);
        savedInstanceState.putBoolean(SAVE_USER_CAN_POST_AS_MODO, userCanPostAsModo);
        if (isInFavs != null) {
            savedInstanceState.putBoolean(SAVE_FORUM_IS_IN_FAV, isInFavs);
        }
    }

    private static class GetJVCLastTopics extends AbsWebRequestAsyncTask<String, Void, ForumPageInfos> {
        boolean isInSearchMode = false;
        boolean useBiggerTimeoutTime = false;

        public GetJVCLastTopics(boolean newIsInSearchMode, boolean newUseBiggerTimeoutTime) {
            isInSearchMode = newIsInSearchMode;
            useBiggerTimeoutTime = newUseBiggerTimeoutTime;
        }

        @Override
        protected ForumPageInfos doInBackground(String... params) {
            if (params.length > 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[1], true);
                ForumPageInfos newPageInfos = null;
                String pageContent;
                currentWebInfos.useBiggerTimeoutTime = useBiggerTimeoutTime;
                pageContent = WebManager.sendRequest(params[0], "GET", "", currentWebInfos);

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
                    newPageInfos.newUserCanPostAsModo = JVCParser.getUserCanPostAsModo(pageContent);
                }

                return newPageInfos;
            } else {
                return null;
            }
        }
    }

    public enum ErrorType {
        NONE_OR_UNKNOWN, SEARCH_IS_EMPTY_AND_ITS_NOT_A_FAIL, FORUM_DOES_NOT_EXIST
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
        public boolean newUserCanPostAsModo;
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
