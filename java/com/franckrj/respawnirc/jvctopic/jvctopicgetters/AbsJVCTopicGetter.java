package com.franckrj.respawnirc.jvctopic.jvctopicgetters;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public abstract class AbsJVCTopicGetter {
    public static final int STATE_LOADING = 0;
    public static final int STATE_NOT_LOADING = 1;

    protected static final String SAVE_TOPIC_URL_TO_FETCH = "tgSaveTopicUrlToFetch";
    protected static final String SAVE_IS_LOADING_FIRST_PAGE = "tgSaveIsLoadingFirstPage";
    protected static final String SAVE_LAST_ID_OF_MESSAGE = "tgSaveLastIdOfMessage";
    protected static final String SAVE_TOPIC_STATUS = "tgSaveTopicStatus";

    protected String urlForTopicPage = "";
    protected TopicStatusInfos currentTopicStatus = new TopicStatusInfos();
    protected long lastIdOfMessage = 0;
    protected boolean isLoadingFirstPage = false;
    protected AbsGetJVCLastMessages currentAsyncTaskForGetMessage = null;
    protected String cookieListInAString = "";
    protected TopicLinkChanged listenerForTopicLinkChanged = null;
    protected NewMessagesListener listenerForNewMessages = null;
    protected NewTopicStatusListener listenerForNewTopicStatus = null;
    protected NewGetterStateListener listenerForNewGetterState = null;
    protected ErrorType lastTypeOfError = ErrorType.NONE_OR_UNKNOWN;

    public String getUrlForTopicPage() {
        return urlForTopicPage;
    }

    public TopicStatusInfos getTopicStatus() {
        return currentTopicStatus;
    }

    public long getLastIdOfMessage() {
        return lastIdOfMessage;
    }

    public ErrorType getLastTypeOfError() {
        return lastTypeOfError;
    }

    public void updateTopicStatusInfos(TopicStatusInfos newTopicStatusInfos) {
        currentTopicStatus = new TopicStatusInfos(newTopicStatusInfos);
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

    public void setListenerForNewTopicStatus(NewTopicStatusListener thisListener) {
        listenerForNewTopicStatus = thisListener;
    }

    public void setListenerForNewGetterState(NewGetterStateListener thisListener) {
        listenerForNewGetterState = thisListener;
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForGetMessage != null) {
            currentAsyncTaskForGetMessage.clearListenersAndCancel();
            currentAsyncTaskForGetMessage = null;
        }

        if (listenerForNewGetterState != null) {
            listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
        }
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        urlForTopicPage = savedInstanceState.getString(SAVE_TOPIC_URL_TO_FETCH, "");
        isLoadingFirstPage = savedInstanceState.getBoolean(SAVE_IS_LOADING_FIRST_PAGE, false);
        lastIdOfMessage = savedInstanceState.getLong(SAVE_LAST_ID_OF_MESSAGE, 0);
        currentTopicStatus = savedInstanceState.getParcelable(SAVE_TOPIC_STATUS);
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_TOPIC_URL_TO_FETCH, urlForTopicPage);
        savedInstanceState.putBoolean(SAVE_IS_LOADING_FIRST_PAGE, isLoadingFirstPage);
        savedInstanceState.putLong(SAVE_LAST_ID_OF_MESSAGE, lastIdOfMessage);
        savedInstanceState.putParcelable(SAVE_TOPIC_STATUS, currentTopicStatus);
    }

    /* Je savais pas comment l'appeler, en gros ça reset les infos affichées dans la liste des messages
    ** pour que lors d'un refresh qui efface les messages ces infos soient retransmisent via listener.*/
    public void resetDirectlyShowedInfos() {
        currentTopicStatus.htmlSurveyTitle = null;
    }

    protected static TopicPageInfos downloadAndParseTopicPage(String topicLink, WebManager.WebInfos currentWebInfos, boolean useBiggerTimeoutTime) {
        TopicPageInfos newPageInfos = null;
        String pageContent;
        currentWebInfos.followRedirects = true;
        currentWebInfos.useBiggerTimeoutTime = useBiggerTimeoutTime;
        pageContent = WebManager.sendRequest(topicLink, "GET", "", currentWebInfos);

        if (pageContent != null) {
            newPageInfos = new TopicPageInfos();
            newPageInfos.newUrlForTopicPage = currentWebInfos.currentUrl;
            newPageInfos.lastPageLink = JVCParser.getLastPageOfTopic(pageContent);
            newPageInfos.nextPageLink = JVCParser.getNextPageOfTopic(pageContent);
            newPageInfos.listOfMessages = JVCParser.getMessagesOfThisPage(pageContent);
            newPageInfos.topicStatus.listOfInputInAString = JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageContent);
            newPageInfos.topicStatus.ajaxInfos = JVCParser.getAllAjaxInfos(pageContent);
            newPageInfos.topicStatus.names = JVCParser.getForumAndTopicNameInTopicPage(pageContent);
            newPageInfos.topicStatus.isInFavs = JVCParser.getIsInFavsFromPage(pageContent);
            newPageInfos.topicStatus.subId = JVCParser.getSubIdInThisTopicPage(pageContent);
            newPageInfos.topicStatus.topicId = JVCParser.getTopicIdInThisTopicPage(pageContent);
            newPageInfos.topicStatus.lockReason = JVCParser.getLockReasonFromPage(pageContent);
            newPageInfos.topicStatus.htmlSurveyTitle = JVCParser.getSurveyHtmlTitleFromPage(pageContent);
            if (!newPageInfos.topicStatus.htmlSurveyTitle.isEmpty()) {
                newPageInfos.topicStatus.listOfSurveyReplyWithInfos = JVCParser.getListOfSurveyReplyWithInfos(pageContent);
            }
            newPageInfos.topicStatus.userCanPostAsModo = JVCParser.getUserCanPostAsModo(pageContent);
            newPageInfos.topicStatus.userCanLockOrUnlockTopic = JVCParser.getUserCanLockOrUnlockTopic(pageContent);
            newPageInfos.topicStatus.userCanPinOrUnpinTopic = JVCParser.getUserCanPinOrUnpinTopic(pageContent);
            newPageInfos.topicStatus.topicIsPinned = JVCParser.getTopicIsPinned(pageContent);
        }

        return newPageInfos;
    }

    protected boolean fillBaseClassInfoFromPageInfo(TopicPageInfos newPageInfos) {
        boolean pageDownloadedIsAnalysable = true;

        if (!newPageInfos.newUrlForTopicPage.isEmpty()) {
            if (JVCParser.checkIfTopicAreSame(urlForTopicPage, newPageInfos.newUrlForTopicPage)) {
                if (JVCParser.getPageNumberForThisTopicLink(urlForTopicPage).equals(JVCParser.getPageNumberForThisTopicLink(newPageInfos.newUrlForTopicPage))) {
                    urlForTopicPage = newPageInfos.newUrlForTopicPage;
                    if (listenerForTopicLinkChanged != null) {
                        listenerForTopicLinkChanged.updateTopicLink(urlForTopicPage);
                    }
                } else {
                    lastTypeOfError = ErrorType.PAGE_DOES_NOT_EXIST;
                    pageDownloadedIsAnalysable = false;
                }
            } else {
                lastTypeOfError = ErrorType.TOPIC_DOES_NOT_EXIST;
                pageDownloadedIsAnalysable = false;
            }
        }

        if (pageDownloadedIsAnalysable) {
            TopicStatusInfos oldTopicStatus = currentTopicStatus;
            currentTopicStatus = newPageInfos.topicStatus;

            if (isLoadingFirstPage && newPageInfos.listOfMessages.size() > 0) {
                currentTopicStatus.pseudoOfAuthor = newPageInfos.listOfMessages.get(0).pseudo;
            }

            if (listenerForNewTopicStatus != null) {
                listenerForNewTopicStatus.getNewTopicStatus(new TopicStatusInfos(currentTopicStatus), oldTopicStatus);
            }
        }

        return pageDownloadedIsAnalysable;
    }

    protected abstract static class AbsGetJVCLastMessages extends AbsWebRequestAsyncTask<String, Void, TopicPageInfos> {
    }

    public enum ErrorType {
        NONE_OR_UNKNOWN, TOPIC_DOES_NOT_EXIST, PAGE_DOES_NOT_EXIST
    }

    protected static class TopicPageInfos {
        public String newUrlForTopicPage = "";
        public TopicStatusInfos topicStatus = new TopicStatusInfos();
        public ArrayList<JVCParser.MessageInfos> listOfMessages = new ArrayList<>();
        public String lastPageLink = "";
        public String nextPageLink = "";
    }

    public static class TopicStatusInfos implements Parcelable {
        public String pseudoOfAuthor = "";
        public String listOfInputInAString = null;
        public JVCParser.AjaxInfos ajaxInfos = new JVCParser.AjaxInfos();
        public JVCParser.ForumAndTopicName names = new JVCParser.ForumAndTopicName();
        public Boolean isInFavs = null;
        public String subId = null;
        public String topicId = "";
        public String lockReason = null;
        public String htmlSurveyTitle = null;
        public ArrayList<JVCParser.SurveyReplyInfos> listOfSurveyReplyWithInfos = new ArrayList<>();
        public boolean userCanPostAsModo = false;
        public boolean userCanLockOrUnlockTopic = false;
        public boolean userCanPinOrUnpinTopic = false;
        public boolean topicIsPinned = false;

        public static final Parcelable.Creator<TopicStatusInfos> CREATOR = new Parcelable.Creator<TopicStatusInfos>() {
            @Override
            public TopicStatusInfos createFromParcel(Parcel in) {
                return new TopicStatusInfos(in);
            }

            @Override
            public TopicStatusInfos[] newArray(int size) {
                return new TopicStatusInfos[size];
            }
        };

        public TopicStatusInfos() {
            //rien
        }

        public TopicStatusInfos(TopicStatusInfos baseForCopy) {
            pseudoOfAuthor = baseForCopy.pseudoOfAuthor;
            listOfInputInAString = baseForCopy.listOfInputInAString;
            ajaxInfos = new JVCParser.AjaxInfos(baseForCopy.ajaxInfos);
            names.forum = baseForCopy.names.forum;
            names.topic = baseForCopy.names.topic;
            isInFavs = baseForCopy.isInFavs;
            subId = baseForCopy.subId;
            topicId = baseForCopy.topicId;
            lockReason = baseForCopy.lockReason;
            htmlSurveyTitle = baseForCopy.htmlSurveyTitle;
            listOfSurveyReplyWithInfos = new ArrayList<>(baseForCopy.listOfSurveyReplyWithInfos);
            userCanPostAsModo = baseForCopy.userCanPostAsModo;
            userCanLockOrUnlockTopic = baseForCopy.userCanLockOrUnlockTopic;
            userCanPinOrUnpinTopic = baseForCopy.userCanPinOrUnpinTopic;
            topicIsPinned = baseForCopy.topicIsPinned;
        }

        private TopicStatusInfos(Parcel in) {
            pseudoOfAuthor = in.readString();
            listOfInputInAString = in.readString();
            ajaxInfos = in.readParcelable(JVCParser.AjaxInfos.class.getClassLoader());
            names.forum = in.readString();
            names.topic = in.readString();
            byte tmpIsInFav = in.readByte();
            if (tmpIsInFav == -1) {
                isInFavs = null;
            } else {
                isInFavs = tmpIsInFav == 1;
            }
            subId = in.readString();
            topicId = in.readString();
            lockReason = in.readString();
            htmlSurveyTitle = in.readString();
            in.readTypedList(listOfSurveyReplyWithInfos, JVCParser.SurveyReplyInfos.CREATOR);
            userCanPostAsModo = (in.readByte() == 1);
            userCanLockOrUnlockTopic = (in.readByte() == 1);
            userCanPinOrUnpinTopic = (in.readByte() == 1);
            topicIsPinned = (in.readByte() == 1);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(pseudoOfAuthor);
            out.writeString(listOfInputInAString);
            out.writeParcelable(ajaxInfos, flags);
            out.writeString(names.forum);
            out.writeString(names.topic);
            if (isInFavs == null) {
                out.writeByte((byte) -1);
            } else {
                out.writeByte((byte)(isInFavs ? 1 : 0));
            }
            out.writeString(subId);
            out.writeString(topicId);
            out.writeString(lockReason);
            out.writeString(htmlSurveyTitle);
            out.writeTypedList(listOfSurveyReplyWithInfos);
            out.writeByte((byte)(userCanPostAsModo ? 1 : 0));
            out.writeByte((byte)(userCanLockOrUnlockTopic ? 1 : 0));
            out.writeByte((byte)(userCanPinOrUnpinTopic ? 1 : 0));
            out.writeByte((byte)(topicIsPinned ? 1 : 0));
        }
    }

    public interface TopicLinkChanged {
        void updateTopicLink(String newTopicLink);
    }

    public interface NewMessagesListener {
        void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages, boolean itsReallyEmpty, boolean dontShowMessages);
    }

    public interface NewTopicStatusListener {
        void getNewTopicStatus(TopicStatusInfos newTopicStatus, TopicStatusInfos oldTopicStatus);
    }

    public interface NewGetterStateListener {
        void newStateSetted(int newState);
    }

    public abstract boolean reloadTopic();
    public abstract boolean reloadTopic(boolean useBiggerTimeoutTime);
}
