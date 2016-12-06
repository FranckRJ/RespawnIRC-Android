package com.franckrj.respawnirc.jvcmsggetters;

import android.app.Activity;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class JVCForumMessageGetter extends AbsJVCMessageGetter {
    private NewNumbersOfPagesListener listenerForNewNumbersOfPages = null;

    public JVCForumMessageGetter(Activity newParentActivity) {
        super(newParentActivity);
    }

    public void setListenerForNewNumbersOfPages(NewNumbersOfPagesListener thisListener) {
        listenerForNewNumbersOfPages = thisListener;
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage) {
        if (currentAsyncTaskForGetMessage == null && !newUrlOfPage.isEmpty()) {
            urlForTopic = newUrlOfPage;
            currentAsyncTaskForGetMessage = new GetJVCForumLastMessages();
            currentAsyncTaskForGetMessage.execute(urlForTopic, cookieListInAString);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean reloadTopic() {
        return startGetMessagesOfThisPage(urlForTopic);
    }

    private class GetJVCForumLastMessages extends AbsGetJVCLastMessages {
        @Override
        protected void onPreExecute() {
            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_LOADING);
            }
        }

        @Override
        protected TopicPageInfos doInBackground(String... params) {
            if (params.length > 1) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                TopicPageInfos newPageInfos = null;
                String pageContent;
                currentWebInfos.followRedirects = false;
                pageContent = WebManager.sendRequest(params[0], "GET", "", params[1], currentWebInfos);

                if (pageContent != null) {
                    newPageInfos = new TopicPageInfos();
                    newPageInfos.lastPageLink = JVCParser.getLastPageOfTopic(pageContent);
                    newPageInfos.nextPageLink = JVCParser.getNextPageOfTopic(pageContent);
                    newPageInfos.listOfMessages = JVCParser.getMessagesOfThisPage(pageContent);
                    newPageInfos.listOfInputInAString = JVCParser.getListOfInputInAString(pageContent);
                    newPageInfos.ajaxInfosOfThisPage = JVCParser.getAllAjaxInfos(pageContent);
                    newPageInfos.newNames = JVCParser.getForumAndTopicNameInTopicPage(pageContent);
                }

                return newPageInfos;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(TopicPageInfos infoOfCurrentPage) {
            super.onPostExecute(infoOfCurrentPage);

            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
            }

            if (infoOfCurrentPage != null) {
                latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
                latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;

                if (!infoOfCurrentPage.listOfMessages.isEmpty()) {
                    lastIdOfMessage = infoOfCurrentPage.listOfMessages.get(infoOfCurrentPage.listOfMessages.size() - 1).id;
                }

                if (!infoOfCurrentPage.newNames.equals(currentNames)) {
                    currentNames = infoOfCurrentPage.newNames;
                    if (listenerForNewForumAndTopicName != null) {
                        listenerForNewForumAndTopicName.getNewForumAndTopicName(currentNames);
                    }
                }

                if (listenerForNewMessages != null) {
                    listenerForNewMessages.getNewMessages(infoOfCurrentPage.listOfMessages);
                }
                if (listenerForNewNumbersOfPages != null) {
                    listenerForNewNumbersOfPages.getNewLastPageNumber(JVCParser.getPageNumberForThisTopicLink(infoOfCurrentPage.lastPageLink));
                }
            } else {
                if (listenerForNewMessages != null) {
                    listenerForNewMessages.getNewMessages(new ArrayList<JVCParser.MessageInfos>());
                }
            }

            currentAsyncTaskForGetMessage = null;
        }
    }

    public interface NewNumbersOfPagesListener {
        void getNewLastPageNumber(String newNumber);
    }
}