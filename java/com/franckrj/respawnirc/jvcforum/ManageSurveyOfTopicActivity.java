package com.franckrj.respawnirc.jvcforum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvcforum.jvcforumtools.SurveyReplysAdapter;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class ManageSurveyOfTopicActivity extends AbsHomeIsBackActivity {
    public static final String EXTRA_SURVEY_TITLE = "com.franckrj.respawnirc.managesurveyactivity.EXTRA_SURVEY_TITLE";
    public static final String EXTRA_SURVEY_REPLYS_LIST = "com.franckrj.respawnirc.managesurveyactivity.EXTRA_SURVEY_REPLYS_LIST";

    private static final String SAVE_LIST_OF_REPLY_CONTENT = "saveListOfReplyContent";
    private static final long MAX_TIME_USER_HAVE_TO_LEAVE_IN_MS = 3_500;

    private RecyclerView listOfReplys = null;
    private SurveyReplysAdapter adapterForReplys = null;
    private EditText titleEdit = null;
    private long lastTimeUserTryToLeaveInMs = -MAX_TIME_USER_HAVE_TO_LEAVE_IN_MS;

    private final View.OnClickListener validateButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent data = new Intent();
            String title = titleEdit.getText().toString();
            ArrayList<String> replysList = new ArrayList<>();

            for (String thisReply : adapterForReplys.getReplyContentList()) {
                if (!Utils.stringIsEmptyOrNull(thisReply)) {
                    replysList.add(thisReply);
                }
            }

            data.putExtra(EXTRA_SURVEY_TITLE, title);
            data.putStringArrayListExtra(EXTRA_SURVEY_REPLYS_LIST, replysList);
            setResult(Activity.RESULT_OK, data);
            finish();
        }
    };

    private final View.OnClickListener addReplyButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            adapterForReplys.addReply("");
            listOfReplys.smoothScrollToPosition(adapterForReplys.getItemCount());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managesurvey);
        initToolbar(R.id.toolbar_managesurvey);

        Button validateButton = findViewById(R.id.validate_button_managesurvey);
        Button addReplyButton = findViewById(R.id.addreply_button_managesurvey);
        listOfReplys = findViewById(R.id.reply_list_managesurvey);
        titleEdit = findViewById(R.id.title_edit_managesurvey);

        adapterForReplys = new SurveyReplysAdapter(this);
        validateButton.setOnClickListener(validateButtonClicked);
        addReplyButton.setOnClickListener(addReplyButtonClicked);
        listOfReplys.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        listOfReplys.setLayoutManager(new LinearLayoutManager(this));
        listOfReplys.setAdapter(adapterForReplys);

        if (getIntent() != null && savedInstanceState == null) {
            String title = getIntent().getStringExtra(EXTRA_SURVEY_TITLE);
            ArrayList<String> replysList = getIntent().getStringArrayListExtra(EXTRA_SURVEY_REPLYS_LIST);

            if (!Utils.stringIsEmptyOrNull(title)) {
                titleEdit.setText(title);
            }

            if (replysList != null) {
                for (int i = 0; i < replysList.size(); ++i) {
                    if (!Utils.stringIsEmptyOrNull(replysList.get(i))) {
                        adapterForReplys.addReply(replysList.get(i));
                    }
                }
            }
        } else if (savedInstanceState != null) {
            ArrayList<String> newListOfReplysContent = savedInstanceState.getStringArrayList(SAVE_LIST_OF_REPLY_CONTENT);

            if (newListOfReplysContent != null) {
                adapterForReplys.setReplys(newListOfReplysContent);
            }
        }

        while (adapterForReplys.getItemCount() < 2) {
            adapterForReplys.addReply("");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SAVE_LIST_OF_REPLY_CONTENT, adapterForReplys.getReplyContentList());
    }

    @Override
    public void onBackPressed() {
        long currentTimeInMs = System.currentTimeMillis();

        if (currentTimeInMs - lastTimeUserTryToLeaveInMs < MAX_TIME_USER_HAVE_TO_LEAVE_IN_MS) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, getString(R.string.pressBackTwoTimesToLeaveSurvey), Toast.LENGTH_LONG).show();
        }

        lastTimeUserTryToLeaveInMs = currentTimeInMs;
    }
}
