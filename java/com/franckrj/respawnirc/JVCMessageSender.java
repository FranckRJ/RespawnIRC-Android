package com.franckrj.respawnirc;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class JVCMessageSender {
    private Activity parentActivity = null;
    private String ajaxListInfos = null;
    private boolean isInEdit = false;
    private String lastInfosForEdit = null;
    private NewMessageWantEditListener listenerForNewMessageWantEdit = null;
    private NewMessagePostedListener listenerForNewMessagePosted = null;
    private PostJVCMessage currentAsyncTaskForSendMessage = null;
    private GetEditJVCMessageInfos currentAsyncTaskForGetEditInfos = null;
    private boolean lastMessageSendedIsAResend = false;
    private InfosOfSend infosOfLastSend = new InfosOfSend();

    public JVCMessageSender(Activity newParentActivity) {
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
        ajaxListInfos = savedInstanceState.getString(parentActivity.getString(R.string.saveOldAjaxListInfos), null);
        isInEdit = savedInstanceState.getBoolean(parentActivity.getString(R.string.saveIsInEdit), false);
        lastInfosForEdit = savedInstanceState.getString(parentActivity.getString(R.string.saveLastInfosForEdit), null);
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(parentActivity.getString(R.string.saveOldAjaxListInfos), ajaxListInfos);
        savedInstanceState.putBoolean(parentActivity.getString(R.string.saveIsInEdit), isInEdit);
        savedInstanceState.putString(parentActivity.getString(R.string.saveLastInfosForEdit), lastInfosForEdit);
    }

    public void sendEditMessage(String messageEditedToSend, String cookieListInAString) {
        sendThisMessage(messageEditedToSend, "http://www.jeuxvideo.com/forums/ajax_edit_message.php", lastInfosForEdit, cookieListInAString, false);
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForSendMessage != null) {
            currentAsyncTaskForSendMessage.cancel(true);
            currentAsyncTaskForSendMessage = null;
        }
        stopCurrentEditTask();
    }

    public void stopCurrentEditTask() {
        if (currentAsyncTaskForGetEditInfos != null) {
            currentAsyncTaskForGetEditInfos.cancel(true);
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

    public boolean sendThisMessage(String messageToSend, String urlToSend, String latestListOfInput, String cookieListInAString, boolean itsAResend) {
        if (currentAsyncTaskForSendMessage == null) {
            infosOfLastSend.messageSended = messageToSend;
            infosOfLastSend.urlUsed = urlToSend;
            infosOfLastSend.listOfInputUsed = latestListOfInput;
            infosOfLastSend.cookiesUsed = cookieListInAString;

            lastMessageSendedIsAResend = itsAResend;
            currentAsyncTaskForSendMessage = new PostJVCMessage();
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
            currentAsyncTaskForGetEditInfos.execute(idOfMessage, oldAjaxListInfos, cookieListInAString);
            return true;
        } else {
            return false;
        }
    }

    private class GetEditJVCMessageInfos extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_edit_message.php", "GET", "id_message=" + params[0] + "&" + params[1] + "&action=get", params[2], currentWebInfos);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String pageResult) {
            super.onPostExecute(pageResult);
            String newMessageEdit = "";

            if (!Utils.stringIsEmptyOrNull(pageResult)) {
                lastInfosForEdit += ajaxListInfos + "&action=post";
                pageResult = JVCParser.parsingAjaxMessages(pageResult);
                lastInfosForEdit += JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageResult);
                newMessageEdit = JVCParser.getMessageEdit(pageResult);
            }

            if (newMessageEdit.isEmpty()) {
                isInEdit = false;
                lastInfosForEdit = null;
                ajaxListInfos = null;
            }

            if (listenerForNewMessageWantEdit != null) {
                listenerForNewMessageWantEdit.initializeEditMode(newMessageEdit);
            }

            currentAsyncTaskForGetEditInfos = null;
        }
    }

    private class PostJVCMessage extends AsyncTask<InfosOfSend, Void, String> {
        @Override
        protected String doInBackground(final InfosOfSend... info) {
            if (info.length == 1) {
                String pageContent;
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                pageContent = WebManager.sendRequest(info[0].urlUsed, "POST", "message_topic=" + info[0].messageSended + info[0].listOfInputUsed, info[0].cookiesUsed, currentWebInfos);

                if (info[0].urlUsed.equals(currentWebInfos.currentUrl)) {
                    pageContent = "respawnirc:resendneeded";
                }

                return pageContent;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String pageResult) {
            super.onPostExecute(pageResult);
            String errorWhenSending = null;

            currentAsyncTaskForSendMessage = null;

            if (!Utils.stringIsEmptyOrNull(pageResult)) {
                if (pageResult.equals("respawnirc:resendneeded")) {
                    if (!lastMessageSendedIsAResend) {
                        sendThisMessage(infosOfLastSend.messageSended, infosOfLastSend.urlUsed, infosOfLastSend.listOfInputUsed, infosOfLastSend.cookiesUsed, true);
                        return;
                    } else {
                        errorWhenSending = parentActivity.getString(R.string.unknownErrorPleaseResend);
                    }
                } else if (!isInEdit) {
                    errorWhenSending = JVCParser.getErrorMessage(pageResult);
                } else {
                    errorWhenSending = JVCParser.getErrorMessageInJSONMode(pageResult);
                }
            }

            if (isInEdit && !Utils.compareStrings(errorWhenSending, "respawnirc:resendneeded")) {
                isInEdit = false;
                lastInfosForEdit = null;
                ajaxListInfos = null;
            }

            if (listenerForNewMessagePosted != null) {
                listenerForNewMessagePosted.lastMessageIsSended(errorWhenSending);
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
        void initializeEditMode(String newMessageToEdit);
    }

    public interface NewMessagePostedListener {
        void lastMessageIsSended(String withThisError);
    }
}
