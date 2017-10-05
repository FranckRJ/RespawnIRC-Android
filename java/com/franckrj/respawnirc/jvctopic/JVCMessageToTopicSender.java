package com.franckrj.respawnirc.jvctopic;

import android.app.Activity;
import android.os.Bundle;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class JVCMessageToTopicSender {
    private static final String SAVE_OLD_AJAX_LIST_INFOS = "saveOldAjaxListInfos";
    private static final String SAVE_IS_IN_EDIT = "saveIsInEdit";
    private static final String SAVE_LAST_INFOS_FOR_EDIT = "saveLastInfosForEdit";

    private Activity parentActivity = null;
    private String ajaxListInfos = null;
    private boolean isInEdit = false;
    private String lastInfosForEdit = null;
    private NewMessageWantEditListener listenerForNewMessageWantEdit = null;
    private NewMessagePostedListener listenerForNewMessagePosted = null;
    private PostJVCMessage currentAsyncTaskForSendMessage = null;
    private GetEditJVCMessageInfos currentAsyncTaskForGetEditInfos = null;
    private InfosOfSend infosOfLastSend = new InfosOfSend();

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
                    errorWhenSending = JVCParser.getErrorMessageInJSONMode(reqResult);
                }
            }

            if (isInEdit && !Utils.stringsAreEquals(reqResult, "respawnirc:resendneeded")) {
                isInEdit = false;
                lastInfosForEdit = null;
                ajaxListInfos = null;
            }

            if (listenerForNewMessagePosted != null) {
                listenerForNewMessagePosted.lastMessageIsSended(errorWhenSending);
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
                lastInfosForEdit += ajaxListInfos + "&action=post";
                lastInfosForEdit += JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageResultParsed);
                newMessageEdit = JVCParser.getMessageEdit(pageResultParsed);

                if (newMessageEdit.isEmpty()) {
                    messageIsAnError = true;
                    newMessageEdit = JVCParser.getErrorMessageInJSONMode(reqResult);
                    if (newMessageEdit == null) {
                        newMessageEdit = "";
                    }
                }
            }

            if (newMessageEdit.isEmpty() || messageIsAnError) {
                isInEdit = false;
                lastInfosForEdit = null;
                ajaxListInfos = null;
            }

            if (listenerForNewMessageWantEdit != null) {
                listenerForNewMessageWantEdit.initializeEditMode(newMessageEdit, messageIsAnError);
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
        ajaxListInfos = savedInstanceState.getString(SAVE_OLD_AJAX_LIST_INFOS, null);
        isInEdit = savedInstanceState.getBoolean(SAVE_IS_IN_EDIT, false);
        lastInfosForEdit = savedInstanceState.getString(SAVE_LAST_INFOS_FOR_EDIT, null);
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_OLD_AJAX_LIST_INFOS, ajaxListInfos);
        savedInstanceState.putBoolean(SAVE_IS_IN_EDIT, isInEdit);
        savedInstanceState.putString(SAVE_LAST_INFOS_FOR_EDIT, lastInfosForEdit);
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
        lastInfosForEdit = null;
        ajaxListInfos = null;
    }

    public boolean sendThisMessage(String messageToSend, String urlToSend, String latestListOfInput, String cookieListInAString) {
        if (currentAsyncTaskForSendMessage == null) {
            infosOfLastSend.messageSended = messageToSend;
            infosOfLastSend.urlUsed = urlToSend;
            infosOfLastSend.listOfInputUsed = latestListOfInput;
            infosOfLastSend.cookiesUsed = cookieListInAString;

            currentAsyncTaskForSendMessage = new PostJVCMessage();
            currentAsyncTaskForSendMessage.setRequestIsFinishedListener(postMessageIsFinishedListener);
            currentAsyncTaskForSendMessage.execute(infosOfLastSend);
            return true;
        } else {
            return false;
        }
    }

    public boolean getInfosForEditMessage(String idOfMessage, String oldAjaxListInfos, String cookieListInAString) {
        if (currentAsyncTaskForGetEditInfos == null) {
            isInEdit = true;
            ajaxListInfos = oldAjaxListInfos;
            lastInfosForEdit = "&id_message=" + idOfMessage + "&";

            currentAsyncTaskForGetEditInfos = new GetEditJVCMessageInfos();
            currentAsyncTaskForGetEditInfos.setRequestIsFinishedListener(getEditInfosIsFinishedListener);
            currentAsyncTaskForGetEditInfos.execute(idOfMessage, oldAjaxListInfos, cookieListInAString);
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

                pageContent = WebManager.sendRequestWithMultipleTrys(info[0].urlUsed, "POST", "message_topic=" + Utils.convertStringToUrlString(info[0].messageSended) + info[0].listOfInputUsed, currentWebInfos, 2);

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
        void initializeEditMode(String newMessageToEdit, boolean messageIsAnError);
    }

    public interface NewMessagePostedListener {
        void lastMessageIsSended(String withThisError);
    }
}
