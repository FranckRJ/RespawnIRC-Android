package com.franckrj.respawnirc.jvctopic.jvctopicgetters;

import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class JVCTopicModeForumGetter extends AbsJVCTopicGetter {
    private NewNumbersOfPagesListener listenerForNewNumbersOfPages = null;

    public void setListenerForNewNumbersOfPages(NewNumbersOfPagesListener thisListener) {
        listenerForNewNumbersOfPages = thisListener;
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage) {
        return startGetMessagesOfThisPage(newUrlOfPage, false);
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage, boolean useBiggerTimeoutTime) {
        if (currentAsyncTaskForGetMessage == null && !newUrlOfPage.isEmpty()) {
            urlForTopic = newUrlOfPage;
            isLoadingFirstPage = JVCParser.getPageNumberForThisTopicLink(urlForTopic).equals("1");
            currentAsyncTaskForGetMessage = new GetJVCForumLastMessages(useBiggerTimeoutTime);
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

    private class GetJVCForumLastMessages extends AbsGetJVCLastMessages {
        private boolean useBiggerTimeoutTime = false;

        GetJVCForumLastMessages(boolean newUseBiggerTimeoutTime) {
            useBiggerTimeoutTime = newUseBiggerTimeoutTime;
        }

        @Override
        protected void onPreExecute() {
            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_LOADING);
            }
        }

        @Override
        protected TopicPageInfos doInBackground(String... params) {
            if (params.length > 1) {
                return downloadAndParseTopicPage(params[0], params[1], useBiggerTimeoutTime);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(TopicPageInfos infoOfCurrentPage) {
            super.onPostExecute(infoOfCurrentPage);
            currentAsyncTaskForGetMessage = null;

            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
            }

            if (infoOfCurrentPage != null) {
                fillBaseClassInfoFromPageInfo(infoOfCurrentPage);

                if (!infoOfCurrentPage.listOfMessages.isEmpty()) {
                    lastIdOfMessage = infoOfCurrentPage.listOfMessages.get(infoOfCurrentPage.listOfMessages.size() - 1).id;
                }

                if (listenerForNewMessages != null) {
                    listenerForNewMessages.getNewMessages(infoOfCurrentPage.listOfMessages, true);
                }
                if (listenerForNewNumbersOfPages != null) {
                    listenerForNewNumbersOfPages.getNewLastPageNumber(JVCParser.getPageNumberForThisTopicLink(infoOfCurrentPage.lastPageLink));
                }
            } else {
                if (listenerForNewMessages != null) {
                    listenerForNewMessages.getNewMessages(new ArrayList<JVCParser.MessageInfos>(), true);
                }
            }
        }
    }

    public interface NewNumbersOfPagesListener {
        void getNewLastPageNumber(String newNumber);
    }
}
