package com.franckrj.respawnirc.jvctopic;

import android.app.Activity;
import android.os.Bundle;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class JVCMessageToTopicSender {
    private static final String SAVE_LAST_AJAX_LIST_INFOS = "saveLastAjaxListInfos";
    private static final String SAVE_USE_MESSAGE_TO_EDIT = "saveUseMessageToEdit";
    private static final String SAVE_IS_IN_EDIT = "saveIsInEdit";
    private static final String SAVE_LAST_INFOS_FOR_EDIT = "saveLastInfosForEdit";
    private static final String SAVE_LAST_MESSAGE_ID_USED_FOR_EDIT = "saveLastMessageIDUsedForEdit";

    private Activity parentActivity = null;
    private String lastAjaxListInfos = "";
    private boolean useMessageToEdit = true;
    private boolean isInEdit = false;
    private String lastInfosForEdit = "";
    private String lastMessageIDUsedForEdit = "";
    private NewMessageWantEditListener listenerForNewMessageWantEdit = null;
    private NewMessagePostedListener listenerForNewMessagePosted = null;
    private PostJVCMessage currentAsyncTaskForSendMessage = null;
    private GetEditJVCMessageInfos currentAsyncTaskForGetEditInfos = null;

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> postMessageIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            String errorWhenSending = null;

            currentAsyncTaskForSendMessage = null;

            if (!Utils.stringIsEmptyOrNull(reqResult)) {
                if (reqResult.equals("respawnirc:resendneeded")) {
                    errorWhenSending = parentActivity.getString(R.string.unknownErrorPleaseRetry);
                } else if (!isInEdit) {
                    errorWhenSending = JVCParser.getErrorMessage(reqResult);
                } else {
                    errorWhenSending = JVCParser.getErrorMessageInJsonMode(reqResult);
                }
            }

            if (listenerForNewMessagePosted != null) {
                listenerForNewMessagePosted.lastMessageIsSended(errorWhenSending);
            }

            if (isInEdit && !Utils.stringsAreEquals(reqResult, "respawnirc:resendneeded")) {
                isInEdit = false;
                lastInfosForEdit = "";

                if (listenerForNewMessageWantEdit != null && errorWhenSending != null) {
                    listenerForNewMessageWantEdit.editThisMessage(lastMessageIDUsedForEdit);
                }
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> getEditInfosIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            String newMessageEdit = "";
            boolean messageIsAnError = false;

            if (!Utils.stringIsEmptyOrNull(reqResult)) {
                String pageResultParsed = JVCParser.parsingAjaxMessages(reqResult);
                lastInfosForEdit += lastAjaxListInfos + "&action=post";
                lastInfosForEdit += JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageResultParsed);
                newMessageEdit = JVCParser.getMessageEdit(pageResultParsed);

                if (newMessageEdit.isEmpty()) {
                    messageIsAnError = true;
                    newMessageEdit = JVCParser.getErrorMessageInJsonMode(reqResult);
                    if (newMessageEdit == null) {
                        newMessageEdit = "";
                    }
                }
            }

            if (newMessageEdit.isEmpty() || messageIsAnError) {
                isInEdit = false;
                lastInfosForEdit = "";
            }

            if (listenerForNewMessageWantEdit != null) {
                listenerForNewMessageWantEdit.initializeEditMode(newMessageEdit, messageIsAnError, useMessageToEdit);
            }

            currentAsyncTaskForGetEditInfos = null;
        }
    };

    public JVCMessageToTopicSender(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    public boolean getIsInEdit() {
        return isInEdit;
    }

    public void setListenerForNewMessageWantEdit(NewMessageWantEditListener newListener) {
        listenerForNewMessageWantEdit = newListener;
    }

    public void setListenerForNewMessagePosted(NewMessagePostedListener newListener) {
        listenerForNewMessagePosted = newListener;
    }

    public void loadFromBundle(Bundle savedInstanceState) {
        lastAjaxListInfos = savedInstanceState.getString(SAVE_LAST_AJAX_LIST_INFOS, "");
        useMessageToEdit = savedInstanceState.getBoolean(SAVE_USE_MESSAGE_TO_EDIT, true);
        isInEdit = savedInstanceState.getBoolean(SAVE_IS_IN_EDIT, false);
        lastInfosForEdit = savedInstanceState.getString(SAVE_LAST_INFOS_FOR_EDIT, "");
        lastMessageIDUsedForEdit = savedInstanceState.getString(SAVE_LAST_MESSAGE_ID_USED_FOR_EDIT, "");
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_LAST_AJAX_LIST_INFOS, lastAjaxListInfos);
        savedInstanceState.putBoolean(SAVE_USE_MESSAGE_TO_EDIT, useMessageToEdit);
        savedInstanceState.putBoolean(SAVE_IS_IN_EDIT, isInEdit);
        savedInstanceState.putString(SAVE_LAST_INFOS_FOR_EDIT, lastInfosForEdit);
        savedInstanceState.putString(SAVE_LAST_MESSAGE_ID_USED_FOR_EDIT, lastMessageIDUsedForEdit);
    }

    public void sendEditMessage(String messageEditedToSend, String cookieListInAString) {
        sendThisMessage(messageEditedToSend, "http://www.jeuxvideo.com/forums/ajax_edit_message.php", lastInfosForEdit, cookieListInAString);
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForSendMessage != null) {
            currentAsyncTaskForSendMessage.clearListenersAndCancel();
            currentAsyncTaskForSendMessage = null;
        }
        stopCurrentEditTask();
    }

    public void stopCurrentEditTask() {
        if (currentAsyncTaskForGetEditInfos != null) {
            currentAsyncTaskForGetEditInfos.clearListenersAndCancel();
            currentAsyncTaskForGetEditInfos = null;
            isInEdit = false;
        }
    }

    public void cancelEdit() {
        stopCurrentEditTask();
        isInEdit = false;
        lastInfosForEdit = "";
    }

    public boolean sendThisMessage(String messageToSend, String urlToSend, String latestListOfInput, String cookieListInAString) {
        if (currentAsyncTaskForSendMessage == null) {
            InfosOfSend infosForCurrentSend = new InfosOfSend();
            infosForCurrentSend.messageSended = messageToSend;
            infosForCurrentSend.urlUsed = urlToSend;
            infosForCurrentSend.listOfInputUsed = latestListOfInput;
            infosForCurrentSend.cookiesUsed = cookieListInAString;

            currentAsyncTaskForSendMessage = new PostJVCMessage();
            currentAsyncTaskForSendMessage.setRequestIsFinishedListener(postMessageIsFinishedListener);
            currentAsyncTaskForSendMessage.execute(infosForCurrentSend);
            return true;
        } else {
            return false;
        }
    }

    public boolean getInfosForEditMessage(String idOfMessage, String ajaxListInfos, String cookieListInAString, boolean newUseMessageToEdit) {
        if (currentAsyncTaskForGetEditInfos == null) {
            isInEdit = true;
            lastAjaxListInfos = ajaxListInfos;
            useMessageToEdit = newUseMessageToEdit;
            lastMessageIDUsedForEdit = idOfMessage;
            lastInfosForEdit = "&id_message=" + lastMessageIDUsedForEdit + "&";

            currentAsyncTaskForGetEditInfos = new GetEditJVCMessageInfos();
            currentAsyncTaskForGetEditInfos.setRequestIsFinishedListener(getEditInfosIsFinishedListener);
            currentAsyncTaskForGetEditInfos.execute(lastMessageIDUsedForEdit, lastAjaxListInfos, cookieListInAString);
            return true;
        } else {
            return false;
        }
    }

    private static class GetEditJVCMessageInfos extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[2], false);
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_edit_message.php", "GET", "id_message=" + params[0] + "&" + params[1] + "&action=get", currentWebInfos);
            } else {
                return null;
            }
        }
    }

    private static class PostJVCMessage extends AbsWebRequestAsyncTask<InfosOfSend, Void, String> {
        @Override
        protected String doInBackground(final InfosOfSend... info) {
            if (info.length == 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(info[0].cookiesUsed, false);
                String pageContent;

                pageContent = WebManager.sendRequestWithMultipleTrys(info[0].urlUsed, "POST", "message_topic=" + Utils.encodeStringToUrlString(info[0].messageSended) + info[0].listOfInputUsed, currentWebInfos, 2);

                if (info[0].urlUsed.equals(currentWebInfos.currentUrl)) {
                    pageContent = "respawnirc:resendneeded";
                }

                return pageContent;
            } else {
                return null;
            }
        }
    }

    private class InfosOfSend {
        String messageSended;
        String urlUsed;
        String listOfInputUsed;
        String cookiesUsed;
    }

    public interface NewMessageWantEditListener {
        void editThisMessage(String messageID);
        void initializeEditMode(String newMessageToEdit, boolean messageIsAnError, boolean useMessageToEdit);
    }

    public interface NewMessagePostedListener {
        void lastMessageIsSended(String withThisError);
    }
}
