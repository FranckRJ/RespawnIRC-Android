package com.franckrj.respawnirc.jvctopic.jvctopicgetters;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.ParcelableLongSparseStringArray;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class JVCTopicModeIRCGetter extends AbsJVCTopicGetter {
    private static final String SAVE_FIRST_TIME_GET_MESSAGES = "tmigSaveFirstTimeGetMessages";
    private static final String SAVE_LIST_OF_EDIT_INFOS = "tmigSaveListOfEditInfos";

    private int timeBetweenRefreshTopic = 10000;
    private Timer timerForFetchUrl = new Timer();
    private boolean firstTimeGetMessages = true;
    private boolean messagesNeedToBeGet = false;
    private ParcelableLongSparseStringArray listOfEditInfos = new ParcelableLongSparseStringArray();
    protected Activity parentActivity;

    private final AbsWebRequestAsyncTask.RequestIsStarted getMessagesIsStartedListener = new AbsWebRequestAsyncTask.RequestIsStarted() {
        @Override
        public void onRequestIsStarted() {
            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_LOADING);
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<TopicPageInfos> getMessagesIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<TopicPageInfos>() {
        @Override
        public void onRequestIsFinished(TopicPageInfos reqResult) {
            boolean needToGetNewMessagesEarly = false;
            ArrayList<JVCParser.MessageInfos> listOfNewMessages = new ArrayList<>();
            currentAsyncTaskForGetMessage = null;
            lastTypeOfError = ErrorType.NONE_OR_UNKNOWN;

            if (listenerForNewGetterState != null) {
                listenerForNewGetterState.newStateSetted(STATE_NOT_LOADING);
            }

            if (messagesNeedToBeGet) {
                if (reqResult != null) {
                    if (fillBaseClassInfoFromPageInfo(reqResult)) {
                        if (reqResult.lastPageLink.isEmpty() || !firstTimeGetMessages) {
                            if (!reqResult.listOfMessages.isEmpty()) {
                                for (JVCParser.MessageInfos thisMessageInfo : reqResult.listOfMessages) {
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
                                listenerForNewMessages.getNewMessages(listOfNewMessages, reqResult.listOfMessages.isEmpty(), false);
                            }
                        }

                        if (!reqResult.lastPageLink.isEmpty()) {
                            if (firstTimeGetMessages) {
                                urlForTopicPage = reqResult.lastPageLink;
                            } else {
                                urlForTopicPage = reqResult.nextPageLink;
                            }
                            isLoadingFirstPage = false;
                            needToGetNewMessagesEarly = true;
                        }
                    } else {
                        if (listenerForNewMessages != null) {
                            listenerForNewMessages.getNewMessages(new ArrayList<JVCParser.MessageInfos>(), true, false);
                        }
                    }
                } else {
                    if (listenerForNewMessages != null) {
                        listenerForNewMessages.getNewMessages(new ArrayList<JVCParser.MessageInfos>(), true, false);
                    }
                }

                if (needToGetNewMessagesEarly) {
                    reloadTopic();
                } else {
                    startGetMessages(timeBetweenRefreshTopic);
                }
            }
        }
    };

    public JVCTopicModeIRCGetter(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    public void setTimeBetweenRefreshTopic(int newTimeBetweenRefreshTopic) {
        timeBetweenRefreshTopic = newTimeBetweenRefreshTopic;
    }

    public void setNewTopic(String newUrlForTopic) {
        firstTimeGetMessages = true;
        currentTopicStatus.listOfInputInAString = null;
        lastIdOfMessage = 0;
        listOfEditInfos.clear();
        urlForTopicPage = JVCParser.getFirstPageForThisTopicLink(newUrlForTopic);
        isLoadingFirstPage = true;
    }

    public void setOldTopic(String oldUrlForTopic, long oldLastIdOfMessage) {
        firstTimeGetMessages = false;
        currentTopicStatus.listOfInputInAString = null;
        lastIdOfMessage = oldLastIdOfMessage - 1;
        listOfEditInfos.clear();
        urlForTopicPage = oldUrlForTopic;
        isLoadingFirstPage = false;
    }

    public void startGetMessages(int timerBeforeStart) {
        startGetMessages(timerBeforeStart, false);
    }

    public boolean startGetMessages(int timerBeforeStart, boolean useBiggerTimeoutTime) {
        if (!urlForTopicPage.isEmpty()) {
            messagesNeedToBeGet = true;
            if (currentAsyncTaskForGetMessage == null) {
                currentAsyncTaskForGetMessage = new GetJVCIRCLastMessages(useBiggerTimeoutTime);
                timerForFetchUrl.schedule(new LaunchGetJVCLastMessage(), timerBeforeStart);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean reloadTopic() {
        return reloadTopic(false);
    }

    @Override
    public boolean reloadTopic(boolean useBiggerTimeoutTime) {
        if (currentAsyncTaskForGetMessage != null) {
            if (!currentAsyncTaskForGetMessage.getStatus().equals(AsyncTask.Status.RUNNING)) {
                timerForFetchUrl.cancel();
                timerForFetchUrl = new Timer();
                stopAllCurrentTask();
                return startGetMessages(0, useBiggerTimeoutTime);
            } else {
                return true;
            }
        } else {
            return startGetMessages(0, useBiggerTimeoutTime);
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
        firstTimeGetMessages = savedInstanceState.getBoolean(SAVE_FIRST_TIME_GET_MESSAGES, true);
        listOfEditInfos = savedInstanceState.getParcelable(SAVE_LIST_OF_EDIT_INFOS);
    }

    @Override
    public void saveToBundle(Bundle savedInstanceState) {
        super.saveToBundle(savedInstanceState);
        savedInstanceState.putBoolean(SAVE_FIRST_TIME_GET_MESSAGES, firstTimeGetMessages);
        savedInstanceState.putParcelable(SAVE_LIST_OF_EDIT_INFOS, listOfEditInfos);
    }

    private class LaunchGetJVCLastMessage extends TimerTask {
        @Override
        public void run() {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentAsyncTaskForGetMessage != null) {
                        if (currentAsyncTaskForGetMessage.getStatus().equals(AsyncTask.Status.PENDING)) {
                            currentAsyncTaskForGetMessage.setRequestIsStartedListener(getMessagesIsStartedListener);
                            currentAsyncTaskForGetMessage.setRequestIsFinishedListener(getMessagesIsFinishedListener);
                            currentAsyncTaskForGetMessage.execute(urlForTopicPage, cookieListInAString);
                        }
                    }
                }
            });
        }
    }

    private static class GetJVCIRCLastMessages extends AbsGetJVCLastMessages {
        private boolean useBiggerTimeoutTime;

        public GetJVCIRCLastMessages(boolean newUseBiggerTimeoutTime) {
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
}
