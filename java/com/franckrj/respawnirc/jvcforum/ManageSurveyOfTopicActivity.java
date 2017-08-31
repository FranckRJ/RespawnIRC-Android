package com.franckrj.respawnirc.jvcforum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.franckrj.respawnirc.AbsThemedActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

//TODO: Pas beau, tout changer
public class ManageSurveyOfTopicActivity extends AbsThemedActivity {
    public static final String EXTRA_SURVEY_TITLE = "com.franckrj.respawnirc.managesurveyactivity.EXTRA_SURVEY_TITLE";
    public static final String EXTRA_SURVEY_REPLYS_LIST = "com.franckrj.respawnirc.managesurveyactivity.EXTRA_SURVEY_REPLYS_LIST";

    private EditText[] replysEditList = new EditText[10];
    private EditText titleEdit = null;

    private final View.OnClickListener validateButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent data = new Intent();
            String title = titleEdit.getText().toString();
            ArrayList<String> replysList = new ArrayList<>();

            for (EditText thisReply : replysEditList) {
                String reply = thisReply.getText().toString();
                if (!Utils.stringIsEmptyOrNull(reply)) {
                    replysList.add(reply);
                }
            }

            data.putExtra(EXTRA_SURVEY_TITLE, title);
            data.putStringArrayListExtra(EXTRA_SURVEY_REPLYS_LIST, replysList);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managesurvey);

        Toolbar myToolbar = findViewById(R.id.toolbar_managesurvey);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button validateButton = findViewById(R.id.validate_button_managesurvey);
        titleEdit = findViewById(R.id.title_edit_managesurvey);
        replysEditList[0] = findViewById(R.id.reply_0_edit_managesurvey);
        replysEditList[1] = findViewById(R.id.reply_1_edit_managesurvey);
        replysEditList[2] = findViewById(R.id.reply_2_edit_managesurvey);
        replysEditList[3] = findViewById(R.id.reply_3_edit_managesurvey);
        replysEditList[4] = findViewById(R.id.reply_4_edit_managesurvey);
        replysEditList[5] = findViewById(R.id.reply_5_edit_managesurvey);
        replysEditList[6] = findViewById(R.id.reply_6_edit_managesurvey);
        replysEditList[7] = findViewById(R.id.reply_4_edit_managesurvey);
        replysEditList[8] = findViewById(R.id.reply_8_edit_managesurvey);
        replysEditList[9] = findViewById(R.id.reply_9_edit_managesurvey);

        validateButton.setOnClickListener(validateButtonClicked);

        if (getIntent() != null && savedInstanceState == null) {
            String title = getIntent().getStringExtra(EXTRA_SURVEY_TITLE);
            ArrayList<String> replysList = getIntent().getStringArrayListExtra(EXTRA_SURVEY_REPLYS_LIST);

            if (!Utils.stringIsEmptyOrNull(title)) {
                titleEdit.setText(title);
            }

            if (replysList != null) {
                for (int i = 0; i < replysList.size(); ++i) {
                    if (!Utils.stringIsEmptyOrNull(replysList.get(i))) {
                        replysEditList[i].setText(replysList.get(i));
                    }
                }
            }
        }
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
}
