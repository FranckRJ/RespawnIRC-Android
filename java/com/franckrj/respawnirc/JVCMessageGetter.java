package com.franckrj.respawnirc;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

class JVCMessageGetter {
    final static int STATE_LOADING = 0;
    final static int STATE_NOT_LOADING = 1;

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
    private NewGetterStateListener listenerForNewGetterState = null;
    private ParcelableLongSparseStringArray listOfEditInfos = new ParcelableLongSparseStringArray();

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

    interface NewGetterStateListener {
        void newStateSetted(int newState);
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

    void setListenerForNewGetterState(NewGetterStateListener thisListener) {
        listenerForNewGetterState = thisListener;
    }

    void setNewTopic(String newUrlForTopic, boolean reallyNewTopic) {
        if (reallyNewTopic) {
            firstTimeGetMessages = true;
            latestListOfInputInAString = null;
            lastIdOfMessage = 0;
            listOfEditInfos.clear();
        }
        urlForTopic = JVCParser.getFirstPageForThisLink(newUrlForTopic);
    }

    void setOldTopic(String oldUrlForTopic, long oldLastIdOfMessage) {
        firstTimeGetMessages = false;
        latestListOfInputInAString = null;
        lastIdOfMessage = oldLastIdOfMessage - 1;
        listOfEditInfos.clear();
        urlForTopic = oldUrlForTopic;
    }

    void loadFromBundle(Bundle savedInstanceState) {
        latestListOfInputInAString = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestListOfInputInAString), null);
        latestAjaxInfos.list = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoList), null);
        latestAjaxInfos.mod = savedInstanceState.getString(parentActivity.getString(R.string.saveLatestAjaxInfoMod), null);
        firstTimeGetMessages = savedInstanceState.getBoolean(parentActivity.getString(R.string.saveFirstTimeGetMessages), true);
        lastIdOfMessage = savedInstanceState.getLong(parentActivity.getString(R.string.saveLastIdOfMessage), 0);
        listOfEditInfos = savedInstanceState.getParcelable(parentActivity.getString(R.string.saveListOfEditInfos));
    }

    void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestListOfInputInAString), latestListOfInputInAString);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoList), latestAjaxInfos.list);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLatestAjaxInfoMod), latestAjaxInfos.mod);
        savedInstanceState.putBoolean(parentActivity.getString(R.string.saveFirstTimeGetMessages), firstTimeGetMessages);
        savedInstanceState.putLong(parentActivity.getString(R.string.saveLastIdOfMessage), lastIdOfMessage);
        savedInstanceState.putParcelable(parentActivity.getString(R.string.saveListOfEditInfos), listOfEditInfos);
    }

    private void startGetMessages(int timerBeforeStart) {
        if (!urlForTopic.isEmpty()) {
            messagesNeedToBeGet = true;
            if (currentAsyncTaskForGetMessage == null) {
                currentAsyncTaskForGetMessage = new GetJVCLastMessage();
                timerForFetchUrl.schedule(new LaunchGetJVCLastMessage(), timerBeforeStart);
            }
        }
    }

    void stopGetMessages() {
        messagesNeedToBeGet = false;
        if (currentAsyncTaskForGetMessage != null) {
            currentAsyncTaskForGetMessage.cancel(false);
            currentAsyncTaskForGetMessage = null;
        }

        if (listenerForNewGetterState != null) {
            listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
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
            boolean needToGetNewMessagesEarly = false;
            ArrayList<JVCParser.MessageInfos> listOfNewMessages = new ArrayList<>();

            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
            }

            if (messagesNeedToBeGet) {
                if (infoOfCurrentPage != null) {
                    latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
                    latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;

                    if (!infoOfCurrentPage.listOfMessages.isEmpty() && (infoOfCurrentPage.lastPageLink.isEmpty() || !firstTimeGetMessages)) {
                        for (JVCParser.MessageInfos thisMessageInfo : infoOfCurrentPage.listOfMessages) {
                            String lastEditInfosForThisMessage = listOfEditInfos.get(thisMessageInfo.id);

                            if (lastEditInfosForThisMessage == null) {
                                lastEditInfosForThisMessage = thisMessageInfo.lastTimeEdit;
                            }

                            if (thisMessageInfo.id > lastIdOfMessage || !lastEditInfosForThisMessage.equals(thisMessageInfo.lastTimeEdit)) {
                                if (!lastEditInfosForThisMessage.equals(thisMessageInfo.lastTimeEdit)) {
                                    thisMessageInfo.isAnEdit = true;
                                } else {
                                    thisMessageInfo.isAnEdit = false;
                                    lastIdOfMessage = thisMessageInfo.id;
                                }
                                listOfNewMessages.add(thisMessageInfo);
                                listOfEditInfos.put(thisMessageInfo.id, thisMessageInfo.lastTimeEdit);
                            }
                        }

                        while (listOfEditInfos.size() > 20) {
                            listOfEditInfos.removeAt(0);
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
