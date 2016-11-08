package com.pijon.respawnirc;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

class JVCMessageGetter {
    private int timeBetweenRefreshTopic = 10000;
    private String urlForTopic = "";
    private Timer timerForFetchUrl = new Timer();
    private String latestListOfInputInAString = null;
    private JVCParser.AjaxInfos latestAjaxInfos = new JVCParser.AjaxInfos();
    private boolean firstTimeGetMessages = true;
    private long lastIdOfMessage = 0;
    private AsyncTask<String, Void, PageInfos> currentAsyncTaskForGetMessage = null;
    private boolean messagesNeedToBeGet = false;
    private Activity parentActivity = null;
    private String cookieListInAString = "";
    private NewMessagesListener listenerForNewMessages = null;

    static class PageInfos {
        ArrayList<JVCParser.MessageInfos> listOfMessages;
        String lastPageLink;
        String nextPageLink;
        String listOfInputInAString;
        JVCParser.AjaxInfos ajaxInfosOfThisPage;
    }

    interface NewMessagesListener {
        void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages);
    }

    JVCMessageGetter(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    String getUrlForTopic() {
        return urlForTopic;
    }

    String getLatestListOfInputInAString() {
        return latestListOfInputInAString;
    }

    JVCParser.AjaxInfos getLatestAjaxInfos() {
        return latestAjaxInfos;
    }

    long getLastIdOfMessage() {
        return lastIdOfMessage;
    }

    void setTimeBetweenRefreshTopic(int newTimeBetweenRefreshTopic) {
        timeBetweenRefreshTopic = newTimeBetweenRefreshTopic;
    }

    void setCookieListInAString(String newCookieListInAString) {
        cookieListInAString = newCookieListInAString;
    }

    void setListenerForNewMessages(NewMessagesListener thisListener) {
        listenerForNewMessages = thisListener;
    }

    void setNewTopic(String newUrlForTopic, boolean reallyNewTopic) {
        if (reallyNewTopic) {
            firstTimeGetMessages = true;
            latestListOfInputInAString = null;
            lastIdOfMessage = 0;
        }
        urlForTopic = JVCParser.getFirstPageForThisLink(newUrlForTopic);
    }

    void setOldTopic(String oldUrlForTopic, long oldLastIdOfMessage) {
        firstTimeGetMessages = false;
        latestListOfInputInAString = null;
        lastIdOfMessage = oldLastIdOfMessage - 1;
        urlForTopic = oldUrlForTopic;
    }

    void loadFromBundle(Bundle savedInstanceState) {
        latestListOfInputInAString = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestListOfInputInAString), null);
        latestAjaxInfos.list = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoList), null);
        latestAjaxInfos.mod = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoMod), null);
        firstTimeGetMessages = savedInstanceState.getBoolean(parentActivity.getString(R.string.saveFirstTimeGetMessages), true);
        lastIdOfMessage = savedInstanceState.getLong(parentActivity.getString(R.string.saveLastIdOfMessage), 0);
    }

    void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestListOfInputInAString), latestListOfInputInAString);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoList), latestAjaxInfos.list);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoMod), latestAjaxInfos.mod);
        savedInstanceState.putBoolean(parentActivity.getString(R.string.saveFirstTimeGetMessages), firstTimeGetMessages);
        savedInstanceState.putLong(parentActivity.getString(R.string.saveLastIdOfMessage), lastIdOfMessage);
    }

    void startGetMessages(int timerBeforeStart) {
        messagesNeedToBeGet = true;
        if (currentAsyncTaskForGetMessage == null) {
            currentAsyncTaskForGetMessage = new GetJVCLastMessage();
            timerForFetchUrl.schedule(new LaunchGetJVCLastMessage(), timerBeforeStart);
        }
    }

    void stopGetMessages() {
        messagesNeedToBeGet = false;
        if (currentAsyncTaskForGetMessage != null) {
            currentAsyncTaskForGetMessage.cancel(false);
            currentAsyncTaskForGetMessage = null;
        }
    }

    void startEarlyGetMessagesIfNeeded() {
        if (currentAsyncTaskForGetMessage != null) {
            if (!currentAsyncTaskForGetMessage.getStatus().equals(AsyncTask.Status.RUNNING)) {
                stopGetMessages();
                startGetMessages(0);
            }
        } else {
            startGetMessages(0);
        }
    }

    private class LaunchGetJVCLastMessage extends TimerTask {
        @Override
        public void run() {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentAsyncTaskForGetMessage != null) {
                        if (currentAsyncTaskForGetMessage.getStatus().equals(AsyncTask.Status.PENDING)) {
                            currentAsyncTaskForGetMessage.execute(urlForTopic, cookieListInAString);
                        }
                    }
                }
            });
        }
    }

    private class GetJVCLastMessage extends AsyncTask<String, Void, PageInfos> {
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
            boolean needToGetNewMessagesEarly = false;
            ArrayList<JVCParser.MessageInfos> listOfNewMessages = new ArrayList<>();

            if (messagesNeedToBeGet) {
                if (infoOfCurrentPage != null) {
                    latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
                    latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;

                    if (!infoOfCurrentPage.listOfMessages.isEmpty() && (infoOfCurrentPage.lastPageLink.isEmpty() || !firstTimeGetMessages)) {
                        for (JVCParser.MessageInfos thisMessageInfo : infoOfCurrentPage.listOfMessages) {
                            if (thisMessageInfo.id > lastIdOfMessage) {
                                listOfNewMessages.add(thisMessageInfo);
                                lastIdOfMessage = thisMessageInfo.id;
                            }
                        }

                        firstTimeGetMessages = false;
                    }

                    if (listenerForNewMessages != null) {
                        listenerForNewMessages.getNewMessages(listOfNewMessages);
                    }

                    if (!infoOfCurrentPage.lastPageLink.isEmpty()) {
                        if (firstTimeGetMessages) {
                            urlForTopic = infoOfCurrentPage.lastPageLink;
                        } else {
                            urlForTopic = infoOfCurrentPage.nextPageLink;
                        }
                        needToGetNewMessagesEarly = true;
                    }
                }

                currentAsyncTaskForGetMessage = null;

                if (needToGetNewMessagesEarly) {
                    startEarlyGetMessagesIfNeeded();
                } else {
                    startGetMessages(timeBetweenRefreshTopic);
                }
            }
        }
    }
}
