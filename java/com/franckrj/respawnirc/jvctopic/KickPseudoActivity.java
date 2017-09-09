package com.franckrj.respawnirc.jvctopic;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class KickPseudoActivity extends AbsHomeIsBackActivity {
    public static final String EXTRA_PSEUDO = "com.franckrj.respawnirc.kickpseudoactivity.EXTRA_PSEUDO";
    public static final String EXTRA_ID_ALIAS = "com.franckrj.respawnirc.kickpseudoactivity.EXTRA_ID_ALIAS";
    public static final String EXTRA_ID_FORUM = "com.franckrj.respawnirc.kickpseudoactivity.EXTRA_ID_FORUM";
    public static final String EXTRA_ID_MESSAGE = "com.franckrj.respawnirc.kickpseudoactivity.EXTRA_ID_MESSAGE";
    public static final String EXTRA_AJAX_MOD = "com.franckrj.respawnirc.kickpseudoactivity.EXTRA_AJAX_MOD";
    public static final String EXTRA_COOKIES = "com.franckrj.respawnirc.kickpseudoactivity.EXTRA_COOKIES";

    private ApplyKickToPseudo currentTaskForKick = null;
    private Spinner motiveSpinner = null;
    private EditText reasonEdit = null;
    private KickInfos infosForKick = new KickInfos();

    private final View.OnClickListener kickButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String motiveValue = null;

            Utils.hideSoftKeyboard(KickPseudoActivity.this);

            if (motiveSpinner.getSelectedItem() != null) {
                //truc moche pour être certain de bien avoir un int sans avoir de warning débile de l'IDE
                try {
                    int realValue;
                    String tempValue = motiveSpinner.getSelectedItem().toString();
                    tempValue = tempValue.substring(0, tempValue.indexOf(' '));
                    realValue = Integer.parseInt(tempValue);
                    motiveValue = String.valueOf(realValue);
                } catch (Exception e) {
                    motiveValue = null;
                }
            }

            if (currentTaskForKick == null) {
                if (motiveValue != null && !reasonEdit.getText().toString().isEmpty()) {
                    currentTaskForKick = new ApplyKickToPseudo();
                    infosForKick.motive = motiveValue;
                    infosForKick.reason = reasonEdit.getText().toString();
                    currentTaskForKick.execute(new KickInfos(infosForKick));
                } else {
                    Toast.makeText(KickPseudoActivity.this, R.string.errorReasonOrMotiveMissingForKick, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(KickPseudoActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void stopAllCurrentTasks() {
        if (currentTaskForKick != null) {
            currentTaskForKick.cancel(true);
            currentTaskForKick = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kickpseudo);
        initToolbar(R.id.toolbar_kickpseudo);

        boolean errorWhenFillingInfos = false;

        Button applyKickButton = findViewById(R.id.kick_button_kickpseudo);
        motiveSpinner = findViewById(R.id.motives_spinner_kickpseudo);
        reasonEdit = findViewById(R.id.reason_edit_kickpseudo);
        applyKickButton.setOnClickListener(kickButtonClickedListener);

        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_PSEUDO) != null) {
                setTitle(getString(R.string.kickPseudo, getIntent().getStringExtra(EXTRA_PSEUDO)));
            }
            if (getIntent().getStringExtra(EXTRA_ID_ALIAS) != null) {
                infosForKick.idAliasPseudo = getIntent().getStringExtra(EXTRA_ID_ALIAS);
            } else {
                errorWhenFillingInfos = true;
            }
            if (getIntent().getStringExtra(EXTRA_ID_FORUM) != null) {
                infosForKick.idForum = getIntent().getStringExtra(EXTRA_ID_FORUM);
            } else {
                errorWhenFillingInfos = true;
            }
            if (getIntent().getStringExtra(EXTRA_ID_MESSAGE) != null) {
                infosForKick.idMessage = getIntent().getStringExtra(EXTRA_ID_MESSAGE);
            } else {
                errorWhenFillingInfos = true;
            }
            if (getIntent().getStringExtra(EXTRA_AJAX_MOD) != null) {
                infosForKick.ajaxInfos = getIntent().getStringExtra(EXTRA_AJAX_MOD);
            } else {
                errorWhenFillingInfos = true;
            }
            if (getIntent().getStringExtra(EXTRA_COOKIES) != null) {
                infosForKick.cookies = getIntent().getStringExtra(EXTRA_COOKIES);
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

    private class ApplyKickToPseudo extends AsyncTask<KickInfos, Void, String> {
        @Override
        protected String doInBackground(KickInfos... infoOfKick) {
            if (infoOfKick.length == 1) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_kick.php", "GET", "action=post&motif_kick=" + infoOfKick[0].motive + "&raison_kick=" + Utils.convertStringToUrlString(infoOfKick[0].reason) +
                        "&duree_kick=3&id_alias_a_kick=" + infoOfKick[0].idAliasPseudo + "&id_forum=" + infoOfKick[0].idForum + "&id_message=" + infoOfKick[0].idMessage + "&" + infoOfKick[0].ajaxInfos, infoOfKick[0].cookies, currentWebInfos);
            }
            return "erreurlol";
        }

        @Override
        protected void onPostExecute(String kickResponse) {
            super.onPostExecute(kickResponse);
            currentTaskForKick = null;

            if (!Utils.stringIsEmptyOrNull(kickResponse)) {
                String potentialError = JVCParser.getErrorMessageInJSONMode(kickResponse);

                if (potentialError != null) {
                    Toast.makeText(KickPseudoActivity.this, potentialError, Toast.LENGTH_SHORT).show();
                } else if (!kickResponse.startsWith("{") && !kickResponse.isEmpty()) {
                    Toast.makeText(KickPseudoActivity.this, R.string.unknownErrorPleaseRetry, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(KickPseudoActivity.this, R.string.kickSuccessful, Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            Toast.makeText(KickPseudoActivity.this, R.string.noKnownResponseFromJVC, Toast.LENGTH_SHORT).show();
        }
    }

    private static class KickInfos {
        public String motive = "";
        public String reason = "";
        public String idAliasPseudo = "";
        public String idForum = "";
        public String idMessage = "";
        public String ajaxInfos = "";
        public String cookies = "";

        public KickInfos() {}

        public KickInfos(KickInfos baseForCopy) {
            motive = baseForCopy.motive;
            reason = baseForCopy.reason;
            idAliasPseudo = baseForCopy.idAliasPseudo;
            idForum = baseForCopy.idForum;
            idMessage = baseForCopy.idMessage;
            ajaxInfos = baseForCopy.ajaxInfos;
            cookies = baseForCopy.cookies;
        }
    }
}
