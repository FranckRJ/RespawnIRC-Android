package com.franckrj.respawnirc.jvctopic;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.ThemedActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

public class LockTopicActivity extends ThemedActivity {
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
            if (currentTaskForLock == null) {
                if (!reasonEdit.getText().toString().isEmpty()) {
                    currentTaskForLock = new ApplyLockToTopic();
                    infosForLock.reason = reasonEdit.getText().toString();
                    currentTaskForLock.execute(infosForLock);
                } else {
                    Toast.makeText(LockTopicActivity.this, R.string.errorReasonMissing, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LockTopicActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void stopAllCurrentTasks() {
        if (currentTaskForLock != null) {
            currentTaskForLock.cancel(true);
            currentTaskForLock = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locktopic);

        boolean errorWhenFillingInfos = false;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_locktopic);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button applyLockButton = (Button) findViewById(R.id.lock_button_locktopic);
        reasonEdit = (EditText) findViewById(R.id.reason_edit_locktopic);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class ApplyLockToTopic extends AsyncTask<LockInfos, Void, String> {
        @Override
        protected String doInBackground(LockInfos... infoOfLock) {
            if (infoOfLock.length == 1) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/modal_moderation_topic.php", "GET", "id_forum=" + infoOfLock[0].idForum + "&tab_topic[]=" +  infoOfLock[0].idTopic + "&type=lock&raison_moderation=" + infoOfLock[0].reason + "&action=post&" + infoOfLock[0].ajaxInfos, infoOfLock[0].cookies, currentWebInfos);
            }
            return "erreurlol";
        }

        @Override
        protected void onPostExecute(String lockResponse) {
            super.onPostExecute(lockResponse);
            currentTaskForLock = null;

            if (lockResponse != null) {
                String potentialError = JVCParser.getErrorMessageInJSONMode(lockResponse);

                if (potentialError != null) {
                    Toast.makeText(LockTopicActivity.this, potentialError, Toast.LENGTH_SHORT).show();
                } else if (!lockResponse.startsWith("{") && !lockResponse.isEmpty()) {
                    Toast.makeText(LockTopicActivity.this, R.string.unknownErrorPleaseRetry, Toast.LENGTH_SHORT).show();
                } else {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
                return;
            }

            Toast.makeText(LockTopicActivity.this, R.string.noKnownResponseFromJVC, Toast.LENGTH_SHORT).show();
        }
    }

    private static class LockInfos {
        public String reason = "";
        public String idForum = "";
        public String idTopic = "";
        public String ajaxInfos = "";
        public String cookies = "";
    }
}
