package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class JVCForumGetter {
    public static final int STATE_LOADING = 0;
    public static final int STATE_NOT_LOADING = 1;

    private static final String SAVE_FORUM_URL_TO_FETCH = "fgSaveForumUrlToFetch";
    private static final String SAVE_IS_IN_SEARCH_MODE = "fgSaveIsInSearchMode";
    private static final String SAVE_LAST_TYPE_OF_ERROR = "fgSaveLastTypeOfError";
    private static final String SAVE_FORUM_STATUS = "fgSaveForumStatus";

    private String urlForForumPage = "";
    private ForumStatusInfos currentForumStatus = new ForumStatusInfos();
    private GetJVCLastTopics currentAsyncTaskForGetTopic = null;
    private String cookieListInAString = "";
    private ForumLinkChanged listenerForForumLinkChanged = null;
    private NewTopicsListener listenerForNewTopics = null;
    private NewForumStatusListener listenerForNewForumStatus = null;
    private NewGetterStateListener listenerForNewGetterState = null;
    private boolean isInSearchMode = false;
    private ErrorType lastTypeOfError = ErrorType.NONE_OR_UNKNOWN;

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
                        if (JVCParser.checkIfForumAreSame(urlForForumPage, reqResult.newUrlForForumPage)) {
                            urlForForumPage = reqResult.newUrlForForumPage;
                            if (listenerForForumLinkChanged != null) {
                                listenerForForumLinkChanged.updateForumLink(urlForForumPage);
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
                    ForumStatusInfos oldForumStatus = currentForumStatus;
                    currentForumStatus = reqResult.forumStatus;

                    if (currentForumStatus.searchIsEmpty) {
                        lastTypeOfError = ErrorType.SEARCH_IS_EMPTY_AND_ITS_NOT_A_FAIL;
                    }

                    if (listenerForNewForumStatus != null) {
                        listenerForNewForumStatus.getNewForumStatus(new ForumStatusInfos(currentForumStatus), oldForumStatus);
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

    public ErrorType getLastTypeOfError() {
        return lastTypeOfError;
    }

    public boolean getIsInSearchMode() {
        return isInSearchMode;
    }

    public void updateForumStatusInfos(ForumStatusInfos newForumStatusInfos) {
        currentForumStatus = new ForumStatusInfos(newForumStatusInfos);
    }

    public void setIsInSearchMode(boolean newVal) {
        isInSearchMode = newVal;
    }

    public void setCookieListInAString(String newCookieListInAString) {
        cookieListInAString = newCookieListInAString;
    }

    public void setListenerForForumLinkChanged(ForumLinkChanged thisListener) {
        listenerForForumLinkChanged = thisListener;
    }

    public void setListenerForNewTopics(NewTopicsListener thisListener) {
        listenerForNewTopics = thisListener;
    }

    public void setListenerForNewForumStatus(NewForumStatusListener thisListener) {
        listenerForNewForumStatus = thisListener;
    }

    public void setListenerForNewGetterState(NewGetterStateListener thisListener) {
        listenerForNewGetterState = thisListener;
    }

    public void setUrlForForumWithoutLoading(String newUrlOfPage) {
        urlForForumPage = newUrlOfPage;
    }

    public void startGetMessagesOfThisPage(String newUrlOfPage) {
        startGetMessagesOfThisPage(newUrlOfPage, false);
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage, boolean useBiggerTimeoutTime) {
        if (currentAsyncTaskForGetTopic == null && !newUrlOfPage.isEmpty()) {
            urlForForumPage = newUrlOfPage;
            currentAsyncTaskForGetTopic = new GetJVCLastTopics(isInSearchMode, useBiggerTimeoutTime);
            currentAsyncTaskForGetTopic.setRequestIsStartedListener(getLastTopicsIsStartedListener);
            currentAsyncTaskForGetTopic.setRequestIsFinishedListener(getLastTopicsIsFinishedListener);
            currentAsyncTaskForGetTopic.execute(urlForForumPage, cookieListInAString);
            return true;
        } else {
            urlForForumPage = newUrlOfPage;
            return false;
        }
    }

    public boolean reloadForum() {
        return reloadForum(false);
    }

    public boolean reloadForum(boolean useBiggerTimeoutTime) {
        return startGetMessagesOfThisPage(urlForForumPage, useBiggerTimeoutTime);
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
        urlForForumPage = savedInstanceState.getString(SAVE_FORUM_URL_TO_FETCH, "");
        isInSearchMode = savedInstanceState.getBoolean(SAVE_IS_IN_SEARCH_MODE, false);
        lastTypeOfError = (ErrorType) savedInstanceState.getSerializable(SAVE_LAST_TYPE_OF_ERROR);
        currentForumStatus = savedInstanceState.getParcelable(SAVE_FORUM_STATUS);
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_FORUM_URL_TO_FETCH, urlForForumPage);
        savedInstanceState.putBoolean(SAVE_IS_IN_SEARCH_MODE, isInSearchMode);
        savedInstanceState.putSerializable(SAVE_LAST_TYPE_OF_ERROR, lastTypeOfError);
        savedInstanceState.putParcelable(SAVE_FORUM_STATUS, currentForumStatus);
    }

    private static class GetJVCLastTopics extends AbsWebRequestAsyncTask<String, Void, ForumPageInfos> {
        boolean isInSearchMode;
        boolean useBiggerTimeoutTime;

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
                    newPageInfos.forumStatus.forumName = JVCParser.getForumNameInForumPage(pageContent);
                    newPageInfos.forumStatus.ajaxInfos = JVCParser.getAllAjaxInfos(pageContent);
                    newPageInfos.forumStatus.formSession = JVCParser.getFormSession(pageContent, false);
                    newPageInfos.forumStatus.isInFavs = JVCParser.getIsInFavsFromPage(pageContent);
                    newPageInfos.forumStatus.listOfInputInAString = JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageContent);
                    newPageInfos.forumStatus.numberOfMp = JVCParser.getNumberOfMpFromPage(pageContent);
                    newPageInfos.forumStatus.numberOfNotif = JVCParser.getNumberOfNotifFromPage(pageContent);
                    if (isInSearchMode) {
                        newPageInfos.forumStatus.searchIsEmpty = JVCParser.getSearchIsEmptyInPage(pageContent);
                    }
                    newPageInfos.forumStatus.userCanPostAsModo = JVCParser.getUserCanPostAsModo(pageContent);
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
        public String newUrlForForumPage = "";
        public ForumStatusInfos forumStatus = new ForumStatusInfos();
        public ArrayList<JVCParser.TopicInfos> listOfTopics = new ArrayList<>();
    }

    public static class ForumStatusInfos implements Parcelable {
        public String forumName = "";
        public JVCParser.AjaxInfos ajaxInfos = new JVCParser.AjaxInfos();
        public JVCParser.FormSession formSession = new JVCParser.FormSession();
        public Boolean isInFavs = null;
        public String listOfInputInAString = null;
        public String numberOfMp = null;
        public String numberOfNotif = null;
        public boolean searchIsEmpty = false;
        public boolean userCanPostAsModo = false;

        public static final Parcelable.Creator<ForumStatusInfos> CREATOR = new Parcelable.Creator<ForumStatusInfos>() {
            @Override
            public ForumStatusInfos createFromParcel(Parcel in) {
                return new ForumStatusInfos(in);
            }

            @Override
            public ForumStatusInfos[] newArray(int size) {
                return new ForumStatusInfos[size];
            }
        };

        public ForumStatusInfos() {
            //rien
        }

        public ForumStatusInfos(ForumStatusInfos baseForCopy) {
            forumName = baseForCopy.forumName;
            ajaxInfos = new JVCParser.AjaxInfos(baseForCopy.ajaxInfos);
            formSession = new JVCParser.FormSession(baseForCopy.formSession);
            isInFavs = baseForCopy.isInFavs;
            listOfInputInAString = baseForCopy.listOfInputInAString;
            numberOfMp = baseForCopy.numberOfMp;
            numberOfNotif = baseForCopy.numberOfNotif;
            searchIsEmpty = baseForCopy.searchIsEmpty;
            userCanPostAsModo = baseForCopy.userCanPostAsModo;
        }

        private ForumStatusInfos(Parcel in) {
            forumName = in.readString();
            ajaxInfos = in.readParcelable(JVCParser.AjaxInfos.class.getClassLoader());
            formSession = in.readParcelable(JVCParser.FormSession.class.getClassLoader());
            byte tmpIsInFav = in.readByte();
            if (tmpIsInFav == -1) {
                isInFavs = null;
            } else {
                isInFavs = tmpIsInFav == 1;
            }
            listOfInputInAString = in.readString();
            numberOfMp = in.readString();
            numberOfNotif = in.readString();
            searchIsEmpty = (in.readByte() == 1);
            userCanPostAsModo = (in.readByte() == 1);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeString(forumName);
            out.writeParcelable(ajaxInfos, flags);
            out.writeParcelable(formSession, flags);
            if (isInFavs == null) {
                out.writeByte((byte) -1);
            } else {
                out.writeByte((byte)(isInFavs ? 1 : 0));
            }
            out.writeString(listOfInputInAString);
            out.writeString(numberOfMp);
            out.writeString(numberOfNotif);
            out.writeByte((byte)(searchIsEmpty ? 1 : 0));
            out.writeByte((byte)(userCanPostAsModo ? 1 : 0));
        }
    }

    public interface ForumLinkChanged {
        void updateForumLink(String newForumLink);
    }

    public interface NewTopicsListener {
        void getNewTopics(ArrayList<JVCParser.TopicInfos> listOfNewTopics);
    }

    public interface NewForumStatusListener {
        void getNewForumStatus(ForumStatusInfos newForumStatus, ForumStatusInfos oldForumStatus);
    }

    public interface NewGetterStateListener {
        void newStateSetted(int newState);
    }
}
