package com.franckrj.respawnirc.jvcgetters;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.ParcelableLongSparseStringArray;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class JVCIRCMessageGetter extends AbsJVCMessageGetter {
    private int timeBetweenRefreshTopic = 10000;
    private Timer timerForFetchUrl = new Timer();
    private boolean firstTimeGetMessages = true;
    private boolean messagesNeedToBeGet = false;
    private ParcelableLongSparseStringArray listOfEditInfos = new ParcelableLongSparseStringArray();

    public JVCIRCMessageGetter(Activity newParentActivity) {
        super(newParentActivity);
    }

    public void setTimeBetweenRefreshTopic(int newTimeBetweenRefreshTopic) {
        timeBetweenRefreshTopic = newTimeBetweenRefreshTopic;
    }

    public void setNewTopic(String newUrlForTopic, boolean reallyNewTopic) {
        if (reallyNewTopic) {
            firstTimeGetMessages = true;
            latestListOfInputInAString = null;
            lastIdOfMessage = 0;
            listOfEditInfos.clear();
        }
        urlForTopic = JVCParser.getFirstPageForThisLink(newUrlForTopic);
    }

    public void setOldTopic(String oldUrlForTopic, long oldLastIdOfMessage) {
        firstTimeGetMessages = false;
        latestListOfInputInAString = null;
        lastIdOfMessage = oldLastIdOfMessage - 1;
        listOfEditInfos.clear();
        urlForTopic = oldUrlForTopic;
    }

    public void startGetMessages(int timerBeforeStart) {
        if (!urlForTopic.isEmpty()) {
            messagesNeedToBeGet = true;
            if (currentAsyncTaskForGetMessage == null) {
                currentAsyncTaskForGetMessage = new GetJVCIRCLastMessage();
                timerForFetchUrl.schedule(new LaunchGetJVCLastMessage(), timerBeforeStart);
            }
        }
    }

    @Override
    public void reloadTopic() {
        if (currentAsyncTaskForGetMessage != null) {
            if (!currentAsyncTaskForGetMessage.getStatus().equals(AsyncTask.Status.RUNNING)) {
                stopAllCurrentTask();
                startGetMessages(0);
            }
        } else {
            startGetMessages(0);
        }
    }

    @Override
    public void stopAllCurrentTask() {
        super.stopAllCurrentTask();
        messagesNeedToBeGet = false;
    }

    @Override
    public void loadFromBundle(Bundle savedInstanceState) {
        super.loadFromBundle(savedInstanceState);
        firstTimeGetMessages = savedInstanceState.getBoolean(parentActivity.getString(R.string.saveFirstTimeGetMessages), true);
        listOfEditInfos = savedInstanceState.getParcelable(parentActivity.getString(R.string.saveListOfEditInfos));
    }

    @Override
    public void saveToBundle(Bundle savedInstanceState) {
        super.saveToBundle(savedInstanceState);
        savedInstanceState.putBoolean(parentActivity.getString(R.string.saveFirstTimeGetMessages), firstTimeGetMessages);
        savedInstanceState.putParcelable(parentActivity.getString(R.string.saveListOfEditInfos), listOfEditInfos);
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

    private class GetJVCIRCLastMessage extends AbsGetJVCLastMessage {
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
                    reloadTopic();
                } else {
                    startGetMessages(timeBetweenRefreshTopic);
                }
            }
        }
    }
}
