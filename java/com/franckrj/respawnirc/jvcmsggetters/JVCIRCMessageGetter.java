package com.franckrj.respawnirc.jvcmsggetters;

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

    public void setNewTopic(String newUrlForTopic) {
        firstTimeGetMessages = true;
        latestListOfInputInAString = null;
        lastIdOfMessage = 0;
        listOfEditInfos.clear();
        urlForTopic = JVCParser.getFirstPageForThisTopicLink(newUrlForTopic);
    }

    public void setOldTopic(String oldUrlForTopic, long oldLastIdOfMessage) {
        firstTimeGetMessages = false;
        latestListOfInputInAString = null;
        lastIdOfMessage = oldLastIdOfMessage - 1;
        listOfEditInfos.clear();
        urlForTopic = oldUrlForTopic;
    }

    public boolean startGetMessages(int timerBeforeStart) {
        if (!urlForTopic.isEmpty()) {
            messagesNeedToBeGet = true;
            if (currentAsyncTaskForGetMessage == null) {
                currentAsyncTaskForGetMessage = new GetJVCIRCLastMessages();
                timerForFetchUrl.schedule(new LaunchGetJVCLastMessage(), timerBeforeStart);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean reloadTopic() {
        if (currentAsyncTaskForGetMessage != null) {
            if (!currentAsyncTaskForGetMessage.getStatus().equals(AsyncTask.Status.RUNNING)) {
                stopAllCurrentTask();
                return startGetMessages(0);
            } else {
                return true;
            }
        } else {
            return startGetMessages(0);
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

    private class GetJVCIRCLastMessages extends AbsGetJVCLastMessages {
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
            boolean needToGetNewMessagesEarly = false;
            ArrayList<JVCParser.MessageInfos> listOfNewMessages = new ArrayList<>();
            currentAsyncTaskForGetMessage = null;

            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
            }

            if (messagesNeedToBeGet) {
                if (infoOfCurrentPage != null) {
                    latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
                    latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;

                    if (infoOfCurrentPage.lastPageLink.isEmpty() || !firstTimeGetMessages) {
                        if (!infoOfCurrentPage.listOfMessages.isEmpty()) {
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
                    }

                    if (!infoOfCurrentPage.newNames.equals(currentNames)) {
                        currentNames = infoOfCurrentPage.newNames;
                        if (listenerForNewForumAndTopicName != null) {
                            listenerForNewForumAndTopicName.getNewForumAndTopicName(currentNames);
                        }
                    }

                    if (!infoOfCurrentPage.lastPageLink.isEmpty()) {
                        if (firstTimeGetMessages) {
                            urlForTopic = infoOfCurrentPage.lastPageLink;
                        } else {
                            urlForTopic = infoOfCurrentPage.nextPageLink;
                        }
                        needToGetNewMessagesEarly = true;
                    }
                } else {
                    if (listenerForNewMessages != null) {
                        listenerForNewMessages.getNewMessages(new ArrayList<JVCParser.MessageInfos>());
                    }
                }

                if (needToGetNewMessagesEarly) {
                    reloadTopic();
                } else {
                    startGetMessages(timeBetweenRefreshTopic);
                }
            }
        }
    }
}
