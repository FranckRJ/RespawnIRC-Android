package com.franckrj.respawnirc.jvctopic;

import android.app.Activity;
import android.os.Bundle;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Map;

public class JVCMessageToTopicSender {
    private static final String SAVE_LAST_AJAX_LIST_INFOS = "mttsSaveLastAjaxListInfos";
    private static final String SAVE_USE_MESSAGE_TO_EDIT = "mttsSaveUseMessageToEdit";
    private static final String SAVE_IS_IN_EDIT = "mttsSaveIsInEdit";
    private static final String SAVE_LAST_INFOS_FOR_EDIT = "mttsSaveLastInfosForEdit";
    private static final String SAVE_LAST_MESSAGE_ID_USED_FOR_EDIT = "mttsSaveLastMessageIDUsedForEdit";

    private Activity parentActivity;
    private String lastAjaxListInfos = "";
    private boolean useMessageToEdit = true;
    private boolean isInEdit = false;
    private Map<String,String> formData = null;
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

            if (!Utils.stringIsEmptyOrNull(reqResult) && !reqResult.contains("<meta http-equiv=\"refresh\"")) {
                if (reqResult.equals("respawnirc:resendneeded")) {
                    errorWhenSending = parentActivity.getString(R.string.unknownErrorPleaseRetry);
                } else if(reqResult.startsWith("respawnirc:error:")) {
                    errorWhenSending = reqResult.replace("respawnirc:error:", "Erreur : ");
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
                //lastInfosForEdit += lastAjaxListInfos + "&action=post";
                //lastInfosForEdit += JVCParser.getListOfInputInAStringInTopicFormForThisPage(pageResultParsed);
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
            }
            else
            {
                // C'est OK, on récupère les infos session.
                JVCParser.FormSession fs = JVCParser.getFormSession(reqResult, true);
                formData.put("fs_session", fs.session);
                formData.put("fs_timestamp", fs.timestamp);
                formData.put("fs_version", fs.fs_version);
                formData.put(fs.keyHash, fs.valueHash);
            }

            if (listenerForNewMessageWantEdit != null) {
                listenerForNewMessageWantEdit.initializeEditMode(newMessageEdit, messageIsAnError, useMessageToEdit);
            }

            currentAsyncTaskForGetEditInfos = null;
        }
    };

    public static String addPostTypeToListOfInput(String listOfInputToUse, boolean postAsModo) {
        return listOfInputToUse + (postAsModo ? "&form_alias_rang=2" : "&form_alias_rang=1");
    }

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
        formData = (Map<String, String>) savedInstanceState.getSerializable(SAVE_LAST_INFOS_FOR_EDIT);
        lastMessageIDUsedForEdit = savedInstanceState.getString(SAVE_LAST_MESSAGE_ID_USED_FOR_EDIT, "");
    }

    public void saveToBundle(Bundle savedInstanceState) {
        savedInstanceState.putString(SAVE_LAST_AJAX_LIST_INFOS, lastAjaxListInfos);
        savedInstanceState.putBoolean(SAVE_USE_MESSAGE_TO_EDIT, useMessageToEdit);
        savedInstanceState.putBoolean(SAVE_IS_IN_EDIT, isInEdit);
        savedInstanceState.putSerializable(SAVE_LAST_INFOS_FOR_EDIT, (Serializable) formData);
        savedInstanceState.putString(SAVE_LAST_MESSAGE_ID_USED_FOR_EDIT, lastMessageIDUsedForEdit);
    }

    public void sendEditMessage(String messageEditedToSend, String cookieListInAString) {
        formData.put("text", messageEditedToSend);
        String req = Utils.makeMultipartFormFromMap(formData);
        sendThisMessage(messageEditedToSend, "https://www.jeuxvideo.com/forums/message/edit", req, cookieListInAString);
        //sendThisMessage(messageEditedToSend, "https://www.jeuxvideo.com/forums/ajax_edit_message.php", lastInfosForEdit, cookieListInAString);
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
    }

    public boolean sendThisMessage(String messageToSend, String urlToSend, String listOfInputToUse, String cookieListInAString) {
        if (currentAsyncTaskForSendMessage == null) {
            InfosOfSend infosForCurrentSend = new InfosOfSend();
            infosForCurrentSend.messageSended = messageToSend;
            infosForCurrentSend.urlUsed = urlToSend;
            infosForCurrentSend.listOfInputUsed = listOfInputToUse;
            infosForCurrentSend.cookiesUsed = cookieListInAString;

            currentAsyncTaskForSendMessage = new PostJVCMessage();
            currentAsyncTaskForSendMessage.setRequestIsFinishedListener(postMessageIsFinishedListener);
            currentAsyncTaskForSendMessage.execute(infosForCurrentSend);
            return true;
        } else {
            return false;
        }
    }

    public boolean getInfosForEditMessage(String idOfMessage, String ajaxListInfos, String cookieListInAString, Map<String,String> currentFormData, boolean newUseMessageToEdit) {
        if (currentAsyncTaskForGetEditInfos == null) {
            isInEdit = true;
            lastAjaxListInfos = ajaxListInfos;
            formData = currentFormData;
            useMessageToEdit = newUseMessageToEdit;
            lastMessageIDUsedForEdit = idOfMessage;

            currentAsyncTaskForGetEditInfos = new GetEditJVCMessageInfos();
            currentAsyncTaskForGetEditInfos.setRequestIsFinishedListener(getEditInfosIsFinishedListener);
            currentAsyncTaskForGetEditInfos.execute(lastMessageIDUsedForEdit, ajaxListInfos, cookieListInAString);
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
                return WebManager.sendRequest("https://www.jeuxvideo.com/forums/ajax_edit_message.php", "GET", "id_message=" + params[0] + "&" + params[1] + "&action=get", currentWebInfos);
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

                pageContent = WebManager.sendRequestWithMultipleTrys(info[0].urlUsed, "POST", info[0].listOfInputUsed, currentWebInfos, 2);

                // Si le premier caractère est une accolade, c'est probablement
                // du JSON. On vérifie.
                if(pageContent != null && !pageContent.isEmpty() && pageContent.charAt(0) == '{') {

                    try {
                        JSONObject json = new JSONObject(pageContent);
                        if (json.has("redirectUrl")) // Post normal.
                        {
                            String cleanUrl = json.getString("redirectUrl").replaceAll("\\\\", "");
                            currentWebInfos.currentUrl = "https://www.jeuxvideo.com" + cleanUrl;
                            pageContent = "<meta http-equiv=\"refresh\""; // HACK par flemme.
                        } else if (json.has("html")) // Modification de post.
                        {
                            pageContent = "<meta http-equiv=\"refresh\""; // HACK par flemme.
                        } else // Erreurs...
                        {
                            pageContent = "respawnirc:error:";

                            if (json.has("needsCaptcha")) {
                                boolean needsCaptcha = json.getBoolean("needsCaptcha");
                                if (needsCaptcha) {
                                    pageContent += "captcha";
                                }
                            }

                            if (!pageContent.equals("captcha")) {

                                if (json.has("errors")) {
                                    try {
                                        // Certaines erreurs retournent un array...
                                        JSONArray errors = json.getJSONArray("errors");
                                        if(errors.length() > 0)
                                        {
                                            pageContent += errors.getString(0);
                                        }
                                    } catch (JSONException ex) {
                                        // Autres erreurs...
                                        JSONObject errors = json.getJSONObject("errors");
                                        JSONArray errorNames = errors.names();
                                        if(errorNames != null && errorNames.length() > 0)
                                        {
                                            pageContent += errors.getString(errorNames.getString(0));
                                        }
                                    }
                                }
                                else
                                {
                                    pageContent += "Erreur inconnue.";
                                }
                            }
                        }
                    } catch (JSONException e) {
                        pageContent = "respawnirc:resendneeded";
                    }
                }

                if(pageContent == null)
                {
                    pageContent = "respawnirc:error:Erreur inconnue.";
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
