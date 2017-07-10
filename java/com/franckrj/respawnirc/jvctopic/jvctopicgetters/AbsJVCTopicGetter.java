package com.franckrj.respawnirc.jvctopic.jvctopicgetters;

import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public abstract class AbsJVCTopicGetter {
    public static final int STATE_LOADING = 0;
    public static final int STATE_NOT_LOADING = 1;

    protected static final String SAVE_TOPIC_URL_TO_FETCH = "saveTopicUrlToFetch";
    protected static final String SAVE_IS_LOADING_FIRST_PAGE = "saveIsLoadingFirstPage";
    protected static final String SAVE_LATEST_LIST_OF_INPUT = "saveLatestListOfInputInAString";
    protected static final String SAVE_LATEST_AJAX_INFO_LIST = "saveLatestAjaxInfoList";
    protected static final String SAVE_LATEST_AJAX_INFO_MOD = "saveLatestAjaxInfoMod";
    protected static final String SAVE_LATEST_AJAX_INFO_PREF = "saveLatestAjaxInfoPref";
    protected static final String SAVE_LAST_ID_OF_MESSAGE = "saveLastIdOfMessage";
    protected static final String SAVE_TOPIC_ID = "saveTopicID";
    protected static final String SAVE_HTML_SURVEY_TITLE = "saveHtmlSurveyTitle";
    protected static final String SAVE_SURVEY_REPLYS_WITH_INFOS = "saveSurveyReplysWithInfos";
    protected static final String SAVE_TOPIC_IS_IN_FAV = "saveTopicIsInFav";
    protected static final String SAVE_USER_CAN_POST_AS_MODO = "saveUserCanPostAsModo";

    protected String urlForTopic = "";
    protected boolean isLoadingFirstPage = false;
    protected String latestListOfInputInAString = null;
    protected JVCParser.AjaxInfos latestAjaxInfos = new JVCParser.AjaxInfos();
    protected long lastIdOfMessage = 0;
    protected AbsGetJVCLastMessages currentAsyncTaskForGetMessage = null;
    protected String cookieListInAString = "";
    protected NewMessagesListener listenerForNewMessages = null;
    protected NewGetterStateListener listenerForNewGetterState = null;
    protected NewForumAndTopicNameAvailable listenerForNewForumAndTopicName = null;
    protected JVCParser.ForumAndTopicName currentNames = new JVCParser.ForumAndTopicName();
    protected TopicLinkChanged listenerForTopicLinkChanged = null;
    protected Boolean isInFavs = null;
    protected String topicID = "";
    protected NewReasonForTopicLock listenerForNewReasonForTopicLock = null;
    protected String lockReason = "";
    protected NewSurveyForTopic listenerForNewSurveyForTopic = null;
    protected String htmlSurveyTitle = null;
    protected ArrayList<JVCParser.SurveyReplyInfos> listOfSurveyReplyWithInfos = new ArrayList<>();
    protected NewPseudoOfAuthorAvailable listenerForNewPseudoOfAuthor = null;
    protected boolean userCanPostAsModo = false;
    protected NewUserCanPostAsModoInfoAvailable listenerForNewUserCanPostAsModo = null;

    public String getUrlForTopic() {
        return urlForTopic;
    }

    public String getLatestListOfInputInAString(boolean tryToPostAsModo) {
        if (!Utils.stringIsEmptyOrNull(latestListOfInputInAString)) {
            return latestListOfInputInAString + (tryToPostAsModo && userCanPostAsModo ? "&form_alias_rang=2" : "&form_alias_rang=1");
        } else {
            return latestListOfInputInAString;
        }
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

    public String getSurveyTitleInHtml() {
        return htmlSurveyTitle;
    }

    public ArrayList<JVCParser.SurveyReplyInfos> getListOfSurveyReplysWithInfos() {
        return listOfSurveyReplyWithInfos;
    }

    public boolean getUserCanPostAsModo() {
        return userCanPostAsModo;
    }

    public void setIsInFavs(Boolean newVal) {
        isInFavs = newVal;
    }

    public void setCookieListInAString(String newCookieListInAString) {
        cookieListInAString = newCookieListInAString;
    }

    public void setListenerForTopicLinkChanged(TopicLinkChanged thisListener) {
        listenerForTopicLinkChanged = thisListener;
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

    public void setListenerForNewSurveyForTopic(NewSurveyForTopic thisListener) {
        listenerForNewSurveyForTopic = thisListener;
    }

    public void setListenerForNewPseudoOfAuthor(NewPseudoOfAuthorAvailable thisListener) {
        listenerForNewPseudoOfAuthor = thisListener;
    }

    public void setListenerForNewUserCanPostAsModo(NewUserCanPostAsModoInfoAvailable thisListener) {
        listenerForNewUserCanPostAsModo = thisListener;
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
        urlForTopic = savedInstanceState.getString(SAVE_TOPIC_URL_TO_FETCH, "");
        isLoadingFirstPage = savedInstanceState.getBoolean(SAVE_IS_LOADING_FIRST_PAGE, false);
        latestListOfInputInAString = savedInstanceState.getString(SAVE_LATEST_LIST_OF_INPUT, null);
        latestAjaxInfos.list = savedInstanceState.getString(SAVE_LATEST_AJAX_INFO_LIST, null);
        latestAjaxInfos.mod = savedInstanceState.getString(SAVE_LATEST_AJAX_INFO_MOD, null);
        latestAjaxInfos.pref = savedInstanceState.getString(SAVE_LATEST_AJAX_INFO_PREF, null);
        lastIdOfMessage = savedInstanceState.getLong(SAVE_LAST_ID_OF_MESSAGE, 0);
        topicID = savedInstanceState.getString(SAVE_TOPIC_ID, "");
        htmlSurveyTitle = savedInstanceState.getString(SAVE_HTML_SURVEY_TITLE, "");
        listOfSurveyReplyWithInfos = savedInstanceState.getParcelableArrayList(SAVE_SURVEY_REPLYS_WITH_INFOS);
        userCanPostAsModo = savedInstanceState.getBoolean(SAVE_USER_CAN_POST_AS_MODO);
        if (savedInstanceState.containsKey(SAVE_TOPIC_IS_IN_FAV)) {
            isInFavs = savedInstanceState.getBoolean(SAVE_TOPIC_IS_IN_FAV, false);
        } else {
            isInFavs = null;
        }
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_TOPIC_URL_TO_FETCH, urlForTopic);
        savedInstanceState.putBoolean(SAVE_IS_LOADING_FIRST_PAGE, isLoadingFirstPage);
        savedInstanceState.putString(SAVE_LATEST_LIST_OF_INPUT, latestListOfInputInAString);
        savedInstanceState.putString(SAVE_LATEST_AJAX_INFO_LIST, latestAjaxInfos.list);
        savedInstanceState.putString(SAVE_LATEST_AJAX_INFO_MOD, latestAjaxInfos.mod);
        savedInstanceState.putString(SAVE_LATEST_AJAX_INFO_PREF, latestAjaxInfos.pref);
        savedInstanceState.putLong(SAVE_LAST_ID_OF_MESSAGE, lastIdOfMessage);
        savedInstanceState.putString(SAVE_TOPIC_ID, topicID);
        savedInstanceState.putString(SAVE_HTML_SURVEY_TITLE, htmlSurveyTitle);
        savedInstanceState.putParcelableArrayList(SAVE_SURVEY_REPLYS_WITH_INFOS, listOfSurveyReplyWithInfos);
        savedInstanceState.putBoolean(SAVE_USER_CAN_POST_AS_MODO, userCanPostAsModo);
        if (isInFavs != null) {
            savedInstanceState.putBoolean(SAVE_TOPIC_IS_IN_FAV, isInFavs);
        }
    }

    /* Je savais pas comment l'appeler, en gros ça reset les infos affichées dans la liste des messages
    ** pour que lors d'un refresh qui efface les messages ces infos soient retransmisent via listener.*/
    public void resetDirectlyShowedInfos() {
        htmlSurveyTitle = null;
    }

    protected TopicPageInfos downloadAndParseTopicPage(String topicLink, String cookies, boolean useBiggerTimeoutTime) {
        WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
        TopicPageInfos newPageInfos = null;
        String pageContent;
        currentWebInfos.followRedirects = true;
        currentWebInfos.useBiggerTimeoutTime = useBiggerTimeoutTime;
        pageContent = WebManager.sendRequest(topicLink, "GET", "", cookies, currentWebInfos);

        if (pageContent != null) {
            newPageInfos = new TopicPageInfos();
            newPageInfos.newUrlForTopicPage = currentWebInfos.currentUrl;
            newPageInfos.lastPageLink = JVCParser.getLastPageOfTopic(pageContent);
            newPageInfos.nextPageLink = JVCParser.getNextPageOfTopic(pageContent);
            newPageInfos.listOfMessages = JVCParser.getMessagesOfThisPage(pageContent);
            newPageInfos.listOfInputInAString = JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageContent);
            newPageInfos.ajaxInfosOfThisPage = JVCParser.getAllAjaxInfos(pageContent);
            newPageInfos.newNames = JVCParser.getForumAndTopicNameInTopicPage(pageContent);
            newPageInfos.newIsInFavs = JVCParser.getIsInFavsFromPage(pageContent);
            newPageInfos.newTopicID = JVCParser.getTopicIDInThisTopicPage(pageContent);
            newPageInfos.newLockReason = JVCParser.getLockReasonFromPage(pageContent);
            newPageInfos.newHtmlSurveyTitle = JVCParser.getSurveyHTMLTitleFromPage(pageContent);
            if (!newPageInfos.newHtmlSurveyTitle.isEmpty()) {
                newPageInfos.newListOfSurveyReplyWithInfos = JVCParser.getListOfSurveyReplyWithInfos(pageContent);
            }
            newPageInfos.newUserCanPostAsModo = JVCParser.getUserCanPostAsModo(pageContent);
        }

        return newPageInfos;
    }

    protected void fillBaseClassInfoFromPageInfo(TopicPageInfos infoOfCurrentPage) {
        latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
        latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;
        isInFavs = infoOfCurrentPage.newIsInFavs;
        topicID = infoOfCurrentPage.newTopicID;
        listOfSurveyReplyWithInfos = infoOfCurrentPage.newListOfSurveyReplyWithInfos;

        if (!infoOfCurrentPage.newUrlForTopicPage.isEmpty()) {
            urlForTopic = infoOfCurrentPage.newUrlForTopicPage;
            if (listenerForTopicLinkChanged != null) {
                listenerForTopicLinkChanged.updateTopicLink(urlForTopic);
            }
        }

        if (infoOfCurrentPage.newUserCanPostAsModo != userCanPostAsModo) {
            userCanPostAsModo = infoOfCurrentPage.newUserCanPostAsModo;
            if (listenerForNewUserCanPostAsModo != null) {
                listenerForNewUserCanPostAsModo.getNewUserCanPostAsModo(userCanPostAsModo);
            }
        }

        if (!infoOfCurrentPage.newNames.equals(currentNames)) {
            currentNames = infoOfCurrentPage.newNames;
            if (listenerForNewForumAndTopicName != null) {
                listenerForNewForumAndTopicName.getNewForumAndTopicName(currentNames);
            }
        }

        if (!Utils.stringsAreEquals(infoOfCurrentPage.newLockReason, lockReason)) {
            lockReason = infoOfCurrentPage.newLockReason;
            if (listenerForNewReasonForTopicLock != null) {
                listenerForNewReasonForTopicLock.getNewLockReason(lockReason);
            }
        }

        if (!Utils.stringsAreEquals(infoOfCurrentPage.newHtmlSurveyTitle, htmlSurveyTitle)) {
            htmlSurveyTitle = infoOfCurrentPage.newHtmlSurveyTitle;
            if (listenerForNewSurveyForTopic != null) {
                listenerForNewSurveyForTopic.getNewSurveyTitle(htmlSurveyTitle);
            }
        }

        if (isLoadingFirstPage && infoOfCurrentPage.listOfMessages.size() > 0 && listenerForNewPseudoOfAuthor != null) {
            listenerForNewPseudoOfAuthor.getNewPseudoOfAuthor(infoOfCurrentPage.listOfMessages.get(0).pseudo);
        }
    }

    protected abstract class AbsGetJVCLastMessages extends AsyncTask<String, Void, TopicPageInfos> {
    }

    protected static class TopicPageInfos {
        public ArrayList<JVCParser.MessageInfos> listOfMessages;
        public String newUrlForTopicPage;
        public String lastPageLink;
        public String nextPageLink;
        public String listOfInputInAString;
        public JVCParser.AjaxInfos ajaxInfosOfThisPage;
        public JVCParser.ForumAndTopicName newNames;
        public Boolean newIsInFavs;
        public String newTopicID;
        public String newLockReason;
        public String newHtmlSurveyTitle;
        public ArrayList<JVCParser.SurveyReplyInfos> newListOfSurveyReplyWithInfos = new ArrayList<>();
        public boolean newUserCanPostAsModo;
    }

    public interface TopicLinkChanged {
        void updateTopicLink(String newTopicLink);
    }

    public interface NewForumAndTopicNameAvailable {
        void getNewForumAndTopicName(JVCParser.ForumAndTopicName newNames);
    }

    public interface NewMessagesListener {
        void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages, boolean itsReallyEmpty);
    }

    public interface NewGetterStateListener {
        void newStateSetted(int newState);
    }

    public interface NewReasonForTopicLock {
        void getNewLockReason(String newReason);
    }

    public interface NewSurveyForTopic {
        void getNewSurveyTitle(String newTitle);
    }

    public interface NewPseudoOfAuthorAvailable {
        void getNewPseudoOfAuthor(String newPseudoOfAuthor);
    }

    public interface NewUserCanPostAsModoInfoAvailable {
        void getNewUserCanPostAsModo(boolean newUserCanPostAsModo);
    }

    public abstract boolean reloadTopic();
    public abstract boolean reloadTopic(boolean useBiggerTimeoutTime);
}
