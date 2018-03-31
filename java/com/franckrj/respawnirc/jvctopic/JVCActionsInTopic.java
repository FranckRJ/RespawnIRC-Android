package com.franckrj.respawnirc.jvctopic;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class JVCActionsInTopic {
    private QuoteJVCMessage currentTaskQuoteMessage = null;
    private DeleteOrRestoreJVCMessage currentTaskDeleteOrRestoreMessage = null;
    private UnlockJVCTopic currentTaskUnlockTopic = null;
    private PinOrUnpinJVCTopic currentTaskPinOrUnpinTopic = null;
    private DekickJVCPseudo currentTaskDekickPseudo = null;
    private NewMessageIsQuoted messageIsQuotedListener = null;
    private TopicNeedToBeReloaded topicNeedToBeReloadedListener = null;
    private String latestMessageQuotedInfo = null;
    private DeletesInfos lastDeleteInfos = new DeletesInfos();
    private Activity parentActivity;

    private final DialogInterface.OnClickListener onClickInDeleteConfirmationPopupListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                if (currentTaskDeleteOrRestoreMessage == null) {
                    currentTaskDeleteOrRestoreMessage = new DeleteOrRestoreJVCMessage(true);
                    currentTaskDeleteOrRestoreMessage.setRequestIsFinishedListener(deleteOrRestoreMessageIsFinishedListener);
                    currentTaskDeleteOrRestoreMessage.execute(lastDeleteInfos.idOfMessage, lastDeleteInfos.latestAjaxInfosMod, lastDeleteInfos.cookieListInAString);
                } else {
                    Toast.makeText(parentActivity, R.string.errorDeleteOrRestoreAlreadyRunning, Toast.LENGTH_SHORT).show();
                }
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

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> deleteOrRestoreMessageIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            if (reqResult != null) {
                String currentError = JVCParser.getErrorMessageInJsonMode(reqResult);

                if (currentError == null) {
                    if (currentTaskDeleteOrRestoreMessage.itsADelete) {
                        Toast.makeText(parentActivity, R.string.supressSuccess, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(parentActivity, R.string.restoreSuccess, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(parentActivity, currentError, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(parentActivity, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }

            currentTaskDeleteOrRestoreMessage = null;
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> unlockTopicIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            currentTaskUnlockTopic = null;

            if (!Utils.stringIsEmptyOrNull(reqResult)) {
                String potentialError = JVCParser.getErrorMessageInJsonMode(reqResult);

                if (potentialError != null) {
                    Toast.makeText(parentActivity, potentialError, Toast.LENGTH_SHORT).show();
                } else if (!reqResult.startsWith("{")) {
                    Toast.makeText(parentActivity, R.string.unknownErrorPleaseRetry, Toast.LENGTH_SHORT).show();
                } else if (topicNeedToBeReloadedListener != null) {
                    topicNeedToBeReloadedListener.onReloadTopicRequested();
                }
                return;
            }

            Toast.makeText(parentActivity, R.string.noKnownResponseFromJVC, Toast.LENGTH_SHORT).show();
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> pinOrUnpinTopicIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            boolean itWasAPin = currentTaskPinOrUnpinTopic.itsAPin;
            currentTaskPinOrUnpinTopic = null;

            if (!Utils.stringIsEmptyOrNull(reqResult)) {
                String potentialError = JVCParser.getErrorMessageInJsonMode(reqResult);

                if (potentialError != null) {
                    Toast.makeText(parentActivity, potentialError, Toast.LENGTH_SHORT).show();
                } else if (!reqResult.startsWith("{")) {
                    Toast.makeText(parentActivity, R.string.unknownErrorPleaseRetry, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(parentActivity, (itWasAPin ? R.string.pinSuccess : R.string.unpinSuccess), Toast.LENGTH_SHORT).show();
                    if (topicNeedToBeReloadedListener != null) {
                        topicNeedToBeReloadedListener.onReloadTopicRequested();
                    }
                }
            } else {
                Toast.makeText(parentActivity, R.string.noKnownResponseFromJVC, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> dekickPseudoIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            String pseudoThatIsDekicked = currentTaskDekickPseudo.pseudoToDekick;
            currentTaskDekickPseudo = null;

            if (!Utils.stringIsEmptyOrNull(reqResult)) {
                String potentialError = JVCParser.getErrorMessageInJsonMode(reqResult);

                if (potentialError != null) {
                    Toast.makeText(parentActivity, potentialError, Toast.LENGTH_SHORT).show();
                } else if (!reqResult.startsWith("{")) {
                    Toast.makeText(parentActivity, R.string.unknownErrorPleaseRetry, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(parentActivity, parentActivity.getString(R.string.dekickOfPseudoSuccessful, pseudoThatIsDekicked), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            Toast.makeText(parentActivity, R.string.noKnownResponseFromJVC, Toast.LENGTH_SHORT).show();
        }
    };

    public JVCActionsInTopic(Activity newParentActivity) {
        parentActivity = newParentActivity;
    }

    public void setNewMessageIsQuotedListener(NewMessageIsQuoted newListener) {
        messageIsQuotedListener = newListener;
    }

    public void setTopicNeedToBeReloadedListener(TopicNeedToBeReloaded newListener) {
        topicNeedToBeReloadedListener = newListener;
    }

    public void stopAllCurrentTasks() {
        if (currentTaskQuoteMessage != null) {
            currentTaskQuoteMessage.clearListenersAndCancel();
            currentTaskQuoteMessage = null;
            latestMessageQuotedInfo = null;
        }
        if (currentTaskDeleteOrRestoreMessage != null) {
            currentTaskDeleteOrRestoreMessage.clearListenersAndCancel();
            currentTaskDeleteOrRestoreMessage = null;
        }
        if (currentTaskUnlockTopic != null) {
            currentTaskUnlockTopic.clearListenersAndCancel();
            currentTaskUnlockTopic = null;
        }
        if (currentTaskPinOrUnpinTopic != null) {
            currentTaskPinOrUnpinTopic.clearListenersAndCancel();
            currentTaskPinOrUnpinTopic = null;
        }
        if (currentTaskDekickPseudo != null) {
            currentTaskDekickPseudo.clearListenersAndCancel();
            currentTaskDekickPseudo = null;
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
        if (latestAjaxInfos.mod != null && currentTaskDeleteOrRestoreMessage == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);

            lastDeleteInfos.idOfMessage = Long.toString(currentMessageInfos.id);
            lastDeleteInfos.latestAjaxInfosMod = latestAjaxInfos.mod;
            lastDeleteInfos.cookieListInAString = cookieListInAString;

            builder.setTitle(R.string.deleteMessage).setMessage(R.string.areYouSure)
                    .setPositiveButton(R.string.yes, onClickInDeleteConfirmationPopupListener).setNegativeButton(R.string.no, null);
            builder.show();
        } else {
            if (currentTaskDeleteOrRestoreMessage != null) {
                Toast.makeText(parentActivity, R.string.errorDeleteOrRestoreAlreadyRunning, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(parentActivity, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startRestoreThisMessage(JVCParser.AjaxInfos latestAjaxInfos, JVCParser.MessageInfos currentMessageInfos, String cookieListInAString) {
        if (latestAjaxInfos.mod != null && currentTaskDeleteOrRestoreMessage == null) {
            currentTaskDeleteOrRestoreMessage = new DeleteOrRestoreJVCMessage(false);
            currentTaskDeleteOrRestoreMessage.setRequestIsFinishedListener(deleteOrRestoreMessageIsFinishedListener);
            currentTaskDeleteOrRestoreMessage.execute(Long.toString(currentMessageInfos.id), latestAjaxInfos.mod, cookieListInAString);
        } else {
            if (currentTaskDeleteOrRestoreMessage != null) {
                Toast.makeText(parentActivity, R.string.errorDeleteOrRestoreAlreadyRunning, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(parentActivity, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startUnlockThisTopic(JVCParser.AjaxInfos latestAjaxInfos, String forumId, String topicId, String cookieListInAString) {
        if (latestAjaxInfos.mod != null && currentTaskUnlockTopic == null) {
            currentTaskUnlockTopic = new UnlockJVCTopic();
            currentTaskUnlockTopic.setRequestIsFinishedListener(unlockTopicIsFinishedListener);
            currentTaskUnlockTopic.execute(forumId, topicId, latestAjaxInfos.mod, cookieListInAString);
        } else {
            if (currentTaskUnlockTopic != null) {
                Toast.makeText(parentActivity, R.string.errorUnlockAlreadyRunning, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(parentActivity, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startPinOrUnpinTopic(boolean itsAPin, JVCParser.AjaxInfos latestAjaxInfos, String forumId, String topicId, String cookieListInAString) {
        if (latestAjaxInfos.mod != null && currentTaskPinOrUnpinTopic == null) {
            currentTaskPinOrUnpinTopic = new PinOrUnpinJVCTopic(itsAPin);
            currentTaskPinOrUnpinTopic.setRequestIsFinishedListener(pinOrUnpinTopicIsFinishedListener);
            currentTaskPinOrUnpinTopic.execute(forumId, topicId, latestAjaxInfos.mod, cookieListInAString);
        } else {
            if (currentTaskPinOrUnpinTopic != null) {
                Toast.makeText(parentActivity, R.string.errorPinOrUnpinAlreadyRunning, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(parentActivity, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void startDekickThisPseudo(String pseudoToDekick, JVCParser.AjaxInfos latestAjaxInfos, String forumId, String idAlias, String cookieListInAString) {
        if (latestAjaxInfos.mod != null && currentTaskDekickPseudo == null) {
            currentTaskDekickPseudo = new DekickJVCPseudo(pseudoToDekick);
            currentTaskDekickPseudo.setRequestIsFinishedListener(dekickPseudoIsFinishedListener);
            currentTaskDekickPseudo.execute(forumId, idAlias, latestAjaxInfos.mod, cookieListInAString);
        } else {
            if (currentTaskDekickPseudo != null) {
                Toast.makeText(parentActivity, R.string.errorDekickAlreadyRunning, Toast.LENGTH_SHORT).show();
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

    private static class DeleteOrRestoreJVCMessage extends AbsWebRequestAsyncTask<String, Void, String> {
        public final boolean itsADelete;

        public DeleteOrRestoreJVCMessage(boolean newItsADelte) {
            itsADelete = newItsADelte;
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                String typeOfAction = (itsADelete ? "delete" : "restore");
                WebManager.WebInfos currentWebInfos = initWebInfos(params[2], false);
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/modal_del_message.php", "POST", "tab_message[]=" + params[0] + "&type=" + typeOfAction + "&" + params[1], currentWebInfos);
            }
            return null;
        }
    }

    private static class UnlockJVCTopic extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[3], false);
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/modal_moderation_topic.php", "GET", "id_forum=" + params[0] + "&tab_topic[]=" + params[1] + "&type=unlock&action=get&" + params[2], currentWebInfos);
            }
            return "erreurlol";
        }
    }

    private static class PinOrUnpinJVCTopic extends AbsWebRequestAsyncTask<String, Void, String> {
        public final boolean itsAPin;

        public PinOrUnpinJVCTopic(boolean newItsAPin) {
            itsAPin = newItsAPin;
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                String typeOfAction = (itsAPin ? "epingle" : "desepingle");
                WebManager.WebInfos currentWebInfos = initWebInfos(params[3], false);
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/modal_moderation_topic.php", "POST", "id_forum=" + params[0] + "&tab_topic[]=" + params[1] + "&type=" + typeOfAction + "&" + params[2], currentWebInfos);
            }
            return "erreurlol";
        }
    }

    private static class DekickJVCPseudo extends AbsWebRequestAsyncTask<String, Void, String> {
        public final String pseudoToDekick;

        public DekickJVCPseudo(String newPseudoToDekick) {
            pseudoToDekick = newPseudoToDekick;
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[3], false);
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_unkick.php", "POST", "id_forum=" + params[0] + "&id_alias_a_unkick=" + params[1] + "&" + params[2], currentWebInfos);
            }
            return "erreurlol";
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

    public interface TopicNeedToBeReloaded {
        void onReloadTopicRequested();
    }
}
