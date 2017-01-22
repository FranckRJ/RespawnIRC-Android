package com.franckrj.respawnirc;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

public class ShowSurveyActivity extends AppCompatActivity {
    public static final String EXTRA_SURVEY_TITLE = "com.franckrj.respawnirc.showsurveyactivity.EXTRA_SURVEY_TITLE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showsurvey);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_showsurvey);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        TextView contentText = (TextView) findViewById(R.id.content_showsurvey);

        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_SURVEY_TITLE) != null) {
                contentText.setText(getIntent().getStringExtra(EXTRA_SURVEY_TITLE));
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
