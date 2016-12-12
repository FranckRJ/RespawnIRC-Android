package com.franckrj.respawnirc;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

import java.net.URLEncoder;

public class JVCMessageSender {
    private Activity parentActivity = null;
    private String ajaxListInfos = null;
    private boolean isInEdit = false;
    private String lastInfosForEdit = null;
    private NewMessageWantEditListener listenerForNewMessageWantEdit = null;
    private NewMessagePostedListener listenerForNewMessagePosted = null;
    private AsyncTask<String, Void, String> currentAsyncTaskForSendMessage = null;
    private AsyncTask<String, Void, String> currentAsyncTaskForGetEditInfos = null;

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
        sendThisMessage(messageEditedToSend, "http://www.jeuxvideo.com/forums/ajax_edit_message.php", lastInfosForEdit, cookieListInAString);
    }

    public void stopAllCurrentTask() {
        if (currentAsyncTaskForSendMessage != null) {
            currentAsyncTaskForSendMessage.cancel(true);
            currentAsyncTaskForSendMessage = null;
        }
        if (currentAsyncTaskForGetEditInfos != null) {
            currentAsyncTaskForGetEditInfos.cancel(true);
            currentAsyncTaskForGetEditInfos = null;
        }
    }

    public boolean sendThisMessage(String messageToSend, String urlToSend, String latestListOfInput, String cookieListInAString) {
        if (currentAsyncTaskForSendMessage == null) {
            try {
                messageToSend = URLEncoder.encode(messageToSend, "UTF-8");
            } catch (Exception e) {
                messageToSend = "";
                e.printStackTrace();
            }

            currentAsyncTaskForSendMessage = new PostJVCMessage();
            currentAsyncTaskForSendMessage.execute(urlToSend, messageToSend, latestListOfInput + "&form_alias_rang=1", cookieListInAString);
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

            if (pageResult != null) {
                if (!pageResult.isEmpty()) {
                    lastInfosForEdit += ajaxListInfos + "&action=post";
                    pageResult = JVCParser.parsingAjaxMessages(pageResult);
                    lastInfosForEdit += JVCParser.getListOfInputInAString(pageResult);
                    newMessageEdit = JVCParser.getMessageEdit(pageResult);
                }
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

    private class PostJVCMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                return WebManager.sendRequest(params[0], "POST", "message_topic=" + params[1] + params[2], params[3], currentWebInfos);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String pageResult) {
            super.onPostExecute(pageResult);
            String errorWhenSending = null;

            if (pageResult != null) {
                if (!pageResult.isEmpty()) {
                    if (!isInEdit) {
                        errorWhenSending = JVCParser.getErrorMessage(pageResult);
                    } else {
                        errorWhenSending = JVCParser.getErrorMessageInEditMode(pageResult);
                    }
                }
            }

            if (isInEdit) {
                isInEdit = false;
                lastInfosForEdit = null;
                ajaxListInfos = null;
            }

            if (listenerForNewMessagePosted != null) {
                listenerForNewMessagePosted.lastMessageIsSended(errorWhenSending);
            }

            currentAsyncTaskForSendMessage = null;
        }
    }

    public interface NewMessageWantEditListener {
        void initializeEditMode(String newMessageToEdit);
    }

    public interface NewMessagePostedListener {
        void lastMessageIsSended(String withThisError);
    }
}
