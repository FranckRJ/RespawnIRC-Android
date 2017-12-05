package com.franckrj.respawnirc.jvctopic;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class LockTopicActivity extends AbsHomeIsBackActivity {
    public static final String EXTRA_ID_FORUM = "com.franckrj.respawnirc.locktopicactivity.EXTRA_ID_FORUM";
    public static final String EXTRA_ID_TOPIC = "com.franckrj.respawnirc.locktopicactivity.EXTRA_ID_TOPIC";
    public static final String EXTRA_AJAX_MOD = "com.franckrj.respawnirc.locktopicactivity.EXTRA_AJAX_MOD";
    public static final String EXTRA_COOKIES = "com.franckrj.respawnirc.locktopicactivity.EXTRA_COOKIES";

    private ApplyLockToTopic currentTaskForLock = null;
    private EditText reasonEdit = null;
    private LockInfos infosForLock = new LockInfos();

    private final View.OnClickListener lockButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.hideSoftKeyboard(LockTopicActivity.this);

            if (currentTaskForLock == null) {
                if (!reasonEdit.getText().toString().isEmpty()) {
                    currentTaskForLock = new ApplyLockToTopic();
                    infosForLock.reason = reasonEdit.getText().toString();
                    currentTaskForLock.setRequestIsFinishedListener(applyLockIsFinishedListener);
                    currentTaskForLock.execute(new LockInfos(infosForLock));
                } else {
                    Toast.makeText(LockTopicActivity.this, R.string.errorReasonMissing, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LockTopicActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> applyLockIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            currentTaskForLock = null;

            if (!Utils.stringIsEmptyOrNull(reqResult)) {
                String potentialError = JVCParser.getErrorMessageInJsonMode(reqResult);

                if (potentialError != null) {
                    Toast.makeText(LockTopicActivity.this, potentialError, Toast.LENGTH_SHORT).show();
                } else if (!reqResult.startsWith("{") && !reqResult.isEmpty()) {
                    Toast.makeText(LockTopicActivity.this, R.string.unknownErrorPleaseRetry, Toast.LENGTH_SHORT).show();
                } else {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
                return;
            }

            Toast.makeText(LockTopicActivity.this, R.string.noKnownResponseFromJVC, Toast.LENGTH_SHORT).show();
        }
    };

    private void stopAllCurrentTasks() {
        if (currentTaskForLock != null) {
            currentTaskForLock.clearListenersAndCancel();
            currentTaskForLock = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locktopic);
        initToolbar(R.id.custom_toolbar);

        boolean errorWhenFillingInfos = false;

        Button applyLockButton = findViewById(R.id.lock_button_locktopic);
        reasonEdit = findViewById(R.id.reason_edit_locktopic);
        applyLockButton.setOnClickListener(lockButtonClickedListener);

        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_ID_FORUM) != null) {
                infosForLock.idForum = getIntent().getStringExtra(EXTRA_ID_FORUM);
            } else {
                errorWhenFillingInfos = true;
            }
            if (getIntent().getStringExtra(EXTRA_ID_TOPIC) != null) {
                infosForLock.idTopic = getIntent().getStringExtra(EXTRA_ID_TOPIC);
            } else {
                errorWhenFillingInfos = true;
            }
            if (getIntent().getStringExtra(EXTRA_AJAX_MOD) != null) {
                infosForLock.ajaxInfos = getIntent().getStringExtra(EXTRA_AJAX_MOD);
            } else {
                errorWhenFillingInfos = true;
            }
            if (getIntent().getStringExtra(EXTRA_COOKIES) != null) {
                infosForLock.cookies = getIntent().getStringExtra(EXTRA_COOKIES);
            } else {
                errorWhenFillingInfos = true;
            }
        } else {
            errorWhenFillingInfos = true;
        }

        if (errorWhenFillingInfos) {
            Toast.makeText(this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        super.onPause();
    }

    private static class ApplyLockToTopic extends AbsWebRequestAsyncTask<LockInfos, Void, String> {
        @Override
        protected String doInBackground(LockInfos... infoOfLock) {
            if (infoOfLock.length == 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(infoOfLock[0].cookies, false);
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/modal_moderation_topic.php", "GET", "id_forum=" + infoOfLock[0].idForum + "&tab_topic[]=" +  infoOfLock[0].idTopic +
                        "&type=lock&raison_moderation=" + Utils.encodeStringToUrlString(infoOfLock[0].reason) + "&action=post&" + infoOfLock[0].ajaxInfos, currentWebInfos);
            }
            return "erreurlol";
        }
    }

    private static class LockInfos {
        public String reason = "";
        public String idForum = "";
        public String idTopic = "";
        public String ajaxInfos = "";
        public String cookies = "";

        public LockInfos() {}

        public LockInfos(LockInfos baseForCopy) {
            reason = baseForCopy.reason;
            idForum = baseForCopy.idForum;
            idTopic = baseForCopy.idTopic;
            ajaxInfos = baseForCopy.ajaxInfos;
            cookies = baseForCopy.cookies;
        }
    }
}
