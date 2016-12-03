package com.franckrj.respawnirc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.franckrj.respawnirc.jvcmsggetters.AbsJVCMessageGetter;
import com.franckrj.respawnirc.jvcmsgviewers.AbsShowTopicFragment;
import com.franckrj.respawnirc.jvcmsgviewers.ShowTopicForumFragment;
import com.franckrj.respawnirc.jvcmsgviewers.ShowTopicIRCFragment;
import com.franckrj.respawnirc.utils.JVCParser;

public class ShowTopicActivity extends AppCompatActivity implements AbsShowTopicFragment.NewModeNeededListener, AbsJVCMessageGetter.NewForumAndTopicNameAvailable {
    public static final String EXTRA_TOPIC_LINK = "com.franckrj.respawnirc.EXTRA_TOPIC_LINK";
    public static final String EXTRA_TOPIC_NAME = "com.franckrj.respawnirc.EXTRA_TOPIC_NAME";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";

    private SharedPreferences sharedPref = null;
    private JVCParser.ForumAndTopicName currentTitles = new JVCParser.ForumAndTopicName();

    private void createNewFragmentForTopicRead(String possibleTopicLink) {
        AbsShowTopicFragment currentFragment;
        int currentTopicMode = sharedPref.getInt(getString(R.string.prefCurrentTopicMode), AbsShowTopicFragment.MODE_FORUM);

        if (currentTopicMode == AbsShowTopicFragment.MODE_FORUM) {
            currentFragment = new ShowTopicForumFragment();
        } else {
            currentFragment = new ShowTopicIRCFragment();
        }

        if (possibleTopicLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(AbsShowTopicFragment.ARG_TOPIC_LINK, possibleTopicLink);
            currentFragment.setArguments(argForFrag);
        }

        getFragmentManager().beginTransaction().replace(R.id.content_frame_showtopic, currentFragment).commit();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtopic);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_showtopic);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        if (savedInstanceState == null) {
            if (getIntent() != null) {
                currentTitles.topic = getIntent().getStringExtra(EXTRA_TOPIC_NAME);
                currentTitles.forum = getIntent().getStringExtra(EXTRA_FORUM_NAME);

                if (currentTitles.topic != null) {
                    if (currentTitles.topic.isEmpty()) {
                        currentTitles.topic = getString(R.string.app_name);
                    }
                } else {
                    currentTitles.topic = getString(R.string.app_name);
                }
                if (currentTitles.forum == null) {
                    currentTitles.forum = "";
                }

                createNewFragmentForTopicRead(getIntent().getStringExtra(EXTRA_TOPIC_LINK));
            } else {
                currentTitles.topic = getString(R.string.app_name);
                currentTitles.forum = "";
                createNewFragmentForTopicRead(null);
            }
        } else {
            currentTitles.forum = savedInstanceState.getString(getString(R.string.saveCurrentForumTitleForTopic), getString(R.string.app_name));
            currentTitles.topic = savedInstanceState.getString(getString(R.string.saveCurrentTopicTitleForTopic), "");
        }

        if (myActionBar != null) {
            myActionBar.setTitle(currentTitles.topic);
            myActionBar.setSubtitle(currentTitles.forum);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        sharedPrefEdit.putInt(getString(R.string.prefLastActivityViewed), MainActivity.ACTIVITY_SHOW_TOPIC);
        sharedPrefEdit.apply();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveCurrentForumTitleForTopic), currentTitles.forum);
        outState.putString(getString(R.string.saveCurrentTopicTitleForTopic), currentTitles.topic);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void newModeRequested(int newMode) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();

        if (newMode == AbsShowTopicFragment.MODE_IRC || newMode == AbsShowTopicFragment.MODE_FORUM) {
            sharedPrefEdit.putInt(getString(R.string.prefCurrentTopicMode), newMode);
            sharedPrefEdit.apply();
            createNewFragmentForTopicRead(null);
        }
    }

    @Override
    public void getNewForumAndTopicName(JVCParser.ForumAndTopicName newNames) {
        ActionBar myActionBar = getSupportActionBar();

        if (!newNames.topic.isEmpty()) {
            currentTitles.topic = newNames.topic;
        } else {
            currentTitles.topic = getString(R.string.app_name);
        }

        if (!newNames.forum.isEmpty()) {
            currentTitles.forum = newNames.forum;
        } else {
            currentTitles.forum = "";
        }

        if (myActionBar != null) {
            myActionBar.setTitle(currentTitles.topic);
            myActionBar.setSubtitle(currentTitles.forum);
        }
    }
}