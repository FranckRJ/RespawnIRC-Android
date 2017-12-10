package com.franckrj.respawnirc.jvctopic;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

public class JVCMessageInTopicAction {
    private QuoteJVCMessage currentTaskQuoteMessage = null;
    private DeleteJVCMessage currentTaskDeleteMessage = null;
    private NewMessageIsQuoted messageIsQuotedListener = null;
    private String latestMessageQuotedInfo = null;
    private DeletesInfos lastDeleteInfos = new DeletesInfos();
    private Activity parentActivity = null;

    private final DialogInterface.OnClickListener onClickInDeleteConfirmationPopupListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                currentTaskDeleteMessage = new DeleteJVCMessage();
                currentTaskDeleteMessage.setRequestIsFinishedListener(deleteMessageIsFinishedListener);
                currentTaskDeleteMessage.execute(lastDeleteInfos.idOfMessage, lastDeleteInfos.latestAjaxInfosMod, lastDeleteInfos.cookieListInAString);
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> quoteMessageIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            if (reqResult != null) {
                if (messageIsQuotedListener != null) {
                    messageIsQuotedListener.getNewMessageQuoted(latestMessageQuotedInfo + "\n>" + reqResult + "\n\n");
                }
            } else {
                Toast.makeText(parentActivity, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }

            latestMessageQuotedInfo = null;
            currentTaskQuoteMessage = null;
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> deleteMessageIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            if (reqResult != null) {
                String currentError = JVCParser.getErrorMessageInJsonMode(reqResult);

                if (currentError == null) {
                    Toast.makeText(parentActivity, R.string.supressSuccess, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(parentActivity, currentError, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(parentActivity, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }

            currentTaskDeleteMessage = null;
        }
    };

    public JVCMessageInTopicAction(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    public void setNewMessageIsQuotedListener(NewMessageIsQuoted newListener) {
        messageIsQuotedListener = newListener;
    }

    public void stopAllCurrentTasks() {
        if (currentTaskQuoteMessage != null) {
            currentTaskQuoteMessage.clearListenersAndCancel();
            currentTaskQuoteMessage = null;
            latestMessageQuotedInfo = null;
        }
        if (currentTaskDeleteMessage != null) {
            currentTaskDeleteMessage.clearListenersAndCancel();
            currentTaskDeleteMessage = null;
        }
    }

    public void startQuoteThisMessage(JVCParser.AjaxInfos latestAjaxInfos, JVCParser.MessageInfos currentMessageInfos, String cookieListInAString) {
        if (latestAjaxInfos.list != null && latestMessageQuotedInfo == null && currentTaskQuoteMessage == null) {
            String idOfMessage = Long.toString(currentMessageInfos.id);
            latestMessageQuotedInfo = JVCParser.buildMessageQuotedInfoFromThis(currentMessageInfos);

            currentTaskQuoteMessage = new QuoteJVCMessage();
            currentTaskQuoteMessage.setRequestIsFinishedListener(quoteMessageIsFinishedListener);
            currentTaskQuoteMessage.execute(idOfMessage, latestAjaxInfos.list, cookieListInAString);
        } else {
            if (latestMessageQuotedInfo != null || currentTaskQuoteMessage != null) {
                Toast.makeText(parentActivity, R.string.errorQuoteAlreadyRunning, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(parentActivity, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startDeleteThisMessage(JVCParser.AjaxInfos latestAjaxInfos, JVCParser.MessageInfos currentMessageInfos, String cookieListInAString) {
        if (latestAjaxInfos.mod != null && currentTaskDeleteMessage == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);

            lastDeleteInfos.idOfMessage = Long.toString(currentMessageInfos.id);
            lastDeleteInfos.latestAjaxInfosMod = latestAjaxInfos.mod;
            lastDeleteInfos.cookieListInAString = cookieListInAString;

            builder.setTitle(R.string.deleteMessage).setMessage(R.string.areYouSure)
                    .setPositiveButton(R.string.yes, onClickInDeleteConfirmationPopupListener).setNegativeButton(R.string.no, null);
            builder.show();
        } else {
            if (currentTaskDeleteMessage != null) {
                Toast.makeText(parentActivity, R.string.errorDeleteAlreadyRunning, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(parentActivity, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static class QuoteJVCMessage extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[2], false);
                String pageContent;

                pageContent = WebManager.sendRequestWithMultipleTrys("http://www.jeuxvideo.com/forums/ajax_citation.php", "POST", "id_message=" + params[0] + "&" + params[1], currentWebInfos, 2);

                if (pageContent != null) {
                    return JVCParser.getMessageQuoted(pageContent);
                }
            }
            return null;
        }
    }

    private static class DeleteJVCMessage extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[2], false);
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/modal_del_message.php", "GET", "tab_message[]=" + params[0] + "&type=delete&" + params[1], currentWebInfos);
            }
            return null;
        }
    }

    private class DeletesInfos {
        String idOfMessage = "";
        String latestAjaxInfosMod = "";
        String cookieListInAString = "";
    }

    public interface NewMessageIsQuoted {
        void getNewMessageQuoted(String messageQuoted);
    }
}
