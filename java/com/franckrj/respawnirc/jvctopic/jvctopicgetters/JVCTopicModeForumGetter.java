package com.franckrj.respawnirc.jvctopic.jvctopicgetters;

import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class JVCTopicModeForumGetter extends AbsJVCTopicGetter {
    private NewNumbersOfPagesListener listenerForNewNumbersOfPages = null;

    private final AbsWebRequestAsyncTask.RequestIsStarted getLastMessagesIsStartedListener = new AbsWebRequestAsyncTask.RequestIsStarted() {
        @Override
        public void onRequestIsStarted() {
            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_LOADING);
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<TopicPageInfos> getLastMessagesIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<TopicPageInfos>() {
        @Override
        public void onRequestIsFinished(TopicPageInfos reqResult) {
            currentAsyncTaskForGetMessage = null;
            lastTypeOfError = ErrorType.NONE_OR_UNKNOWN;

            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
            }

            if (reqResult != null) {
                if (fillBaseClassInfoFromPageInfo(reqResult)) {
                    boolean dontShowMessages = false;

                    if (!reqResult.listOfMessages.isEmpty()) {
                        lastIdOfMessage = reqResult.listOfMessages.get(reqResult.listOfMessages.size() - 1).id;
                    }

                    if (listenerForNewNumbersOfPages != null) {
                        dontShowMessages = listenerForNewNumbersOfPages.getNewLastPageNumber(JVCParser.getPageNumberForThisTopicLink(reqResult.lastPageLink));
                    }
                    if (listenerForNewMessages != null) {
                        listenerForNewMessages.getNewMessages(reqResult.listOfMessages, true, dontShowMessages);
                    }

                    return;
                }
            }

            if (listenerForNewMessages != null) {
                listenerForNewMessages.getNewMessages(new ArrayList<JVCParser.MessageInfos>(), true, false);
            }
        }
    };

    public void setListenerForNewNumbersOfPages(NewNumbersOfPagesListener thisListener) {
        listenerForNewNumbersOfPages = thisListener;
    }

    public void startGetMessagesOfThisPage(String newUrlOfPage) {
        startGetMessagesOfThisPage(newUrlOfPage, false);
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage, boolean useBiggerTimeoutTime) {
        if (currentAsyncTaskForGetMessage == null && !newUrlOfPage.isEmpty()) {
            urlForTopic = newUrlOfPage;
            isLoadingFirstPage = JVCParser.getPageNumberForThisTopicLink(urlForTopic).equals("1");
            currentAsyncTaskForGetMessage = new GetJVCForumLastMessages(useBiggerTimeoutTime);
            currentAsyncTaskForGetMessage.setRequestIsStartedListener(getLastMessagesIsStartedListener);
            currentAsyncTaskForGetMessage.setRequestIsFinishedListener(getLastMessagesIsFinishedListener);
            currentAsyncTaskForGetMessage.execute(urlForTopic, cookieListInAString);
            return true;
        } else {
            urlForTopic = newUrlOfPage;
            isLoadingFirstPage = JVCParser.getPageNumberForThisTopicLink(urlForTopic).equals("1");
            return false;
        }
    }

    @Override
    public boolean reloadTopic() {
        return reloadTopic(false);
    }

    @Override
    public boolean reloadTopic(boolean useBiggerTimeoutTime) {
        return startGetMessagesOfThisPage(urlForTopic, useBiggerTimeoutTime);
    }

    private static class GetJVCForumLastMessages extends AbsGetJVCLastMessages {
        private boolean useBiggerTimeoutTime = false;

        public GetJVCForumLastMessages(boolean newUseBiggerTimeoutTime) {
            useBiggerTimeoutTime = newUseBiggerTimeoutTime;
        }

        @Override
        protected TopicPageInfos doInBackground(String... params) {
            if (params.length > 1) {
                return downloadAndParseTopicPage(params[0], initWebInfos(params[1], true), useBiggerTimeoutTime);
            } else {
                return null;
            }
        }
    }

    public interface NewNumbersOfPagesListener {
        boolean getNewLastPageNumber(String newNumber);
    }
}
