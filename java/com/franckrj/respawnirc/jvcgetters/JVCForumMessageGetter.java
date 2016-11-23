package com.franckrj.respawnirc.jvcgetters;

import android.app.Activity;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

public class JVCForumMessageGetter extends AbsJVCMessageGetter {
    public JVCForumMessageGetter(Activity newParentActivity) {
        super(newParentActivity);
    }

    public boolean startGetMessagesOfThisPage(String newUrlOfPage) {
        if (currentAsyncTaskForGetMessage == null && !newUrlOfPage.isEmpty()) {
            urlForTopic = newUrlOfPage;
            currentAsyncTaskForGetMessage = new GetJVCForumLastMessage();
            currentAsyncTaskForGetMessage.execute(urlForTopic, cookieListInAString);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void reloadTopic() {
        startGetMessagesOfThisPage(urlForTopic);
    }

    private class GetJVCForumLastMessage extends AbsGetJVCLastMessage {
        @Override
        protected void onPreExecute() {
            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_LOADING);
            }
        }

        @Override
        protected PageInfos doInBackground(String... params) {
            if (params.length > 1) {
                PageInfos newPageInfos = null;
                String pageContent = WebManager.sendRequest(params[0], "GET", "", params[1]);

                if (pageContent != null) {
                    newPageInfos = new PageInfos();
                    newPageInfos.lastPageLink = JVCParser.getLastPageOfTopic(pageContent);
                    newPageInfos.nextPageLink = JVCParser.getNextPageOfTopic(pageContent);
                    newPageInfos.listOfMessages = JVCParser.getMessagesOfThisPage(pageContent);
                    newPageInfos.listOfInputInAString = JVCParser.getListOfInputInAString(pageContent);
                    newPageInfos.ajaxInfosOfThisPage = JVCParser.getAllAjaxInfos(pageContent);
                }

                return newPageInfos;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(PageInfos infoOfCurrentPage) {
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

                if (listenerForNewMessages != null) {
                    listenerForNewMessages.getNewMessages(infoOfCurrentPage.listOfMessages);
                }
            }

            currentAsyncTaskForGetMessage = null;
        }
    }
}