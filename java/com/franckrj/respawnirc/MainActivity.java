package com.franckrj.respawnirc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.jvcmsgviewers.AbsShowTopicFragment;
import com.franckrj.respawnirc.jvcmsgviewers.ShowTopicForumFragment;
import com.franckrj.respawnirc.jvcmsgviewers.ShowTopicIRCFragment;
import com.franckrj.respawnirc.jvctopictools.ShowForumFragment;
import com.franckrj.respawnirc.utils.JVCParser;

public class MainActivity extends AppCompatActivity implements AbsShowTopicFragment.NewModeNeededListener,
                                                    ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected,
                                                    ShowForumFragment.NewTopicWantRead {
    private static final int LIST_DRAWER_POS_HOME = 0;
    private static final int LIST_DRAWER_POS_CONNECT = 1;
    private static final int LIST_DRAWER_POS_SELECT_TOPIC_OR_FORUM = 2;
    private static final int LIST_DRAWER_POS_SETTING = 3;
    private static final int FRAG_TOPIC_SHOW = 0;
    private static final int FRAG_FORUM_SHOW = 1;

    private DrawerLayout layoutForDrawer = null;
    private ListView listForDrawer = null;
    private ActionBarDrawerToggle toggleForDrawer = null;
    private int lastNewActivitySelected = -1;
    private SharedPreferences sharedPref = null;
    private int currentFragmentType = FRAG_FORUM_SHOW;

    private ListView.OnItemClickListener itemInDrawerClickedListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            lastNewActivitySelected = position;
            listForDrawer.setItemChecked(position, true);
            layoutForDrawer.closeDrawer(listForDrawer);
        }
    };

    private void createNewFragmentForForumRead(String possibleForumLink) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        ShowForumFragment currentFragment = new ShowForumFragment();

        if (possibleForumLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(ShowForumFragment.ARG_FORUM_LINK, possibleForumLink);
            currentFragment.setArguments(argForFrag);
        }

        getFragmentManager().beginTransaction().replace(R.id.content_frame_main, currentFragment).commit();
        currentFragmentType = FRAG_FORUM_SHOW;
        sharedPrefEdit.putInt(getString(R.string.prefLastFragmentViewed), currentFragmentType);
        sharedPrefEdit.apply();
    }

    private void createNewFragmentForTopicRead(String possibleTopicLink) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        AbsShowTopicFragment currentFragment;
        int currentTopicMode = sharedPref.getInt(getString(R.string.prefCurrentTopicMode), AbsShowTopicFragment.MODE_IRC);

        if (currentTopicMode == AbsShowTopicFragment.MODE_IRC) {
            currentFragment = new ShowTopicIRCFragment();
        } else {
            currentFragment = new ShowTopicForumFragment();
        }

        if (possibleTopicLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(AbsShowTopicFragment.ARG_TOPIC_LINK, possibleTopicLink);
            currentFragment.setArguments(argForFrag);
        }

        getFragmentManager().beginTransaction().replace(R.id.content_frame_main, currentFragment).commit();
        currentFragmentType = FRAG_TOPIC_SHOW;
        sharedPrefEdit.putInt(getString(R.string.prefLastFragmentViewed), currentFragmentType);
        sharedPrefEdit.apply();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        layoutForDrawer = (DrawerLayout) findViewById(R.id.layout_drawer_main);
        listForDrawer = (ListView) findViewById(R.id.view_left_drawer_main);

        toggleForDrawer = new ActionBarDrawerToggle(this, layoutForDrawer, R.string.openDrawerContentDescRes, R.string.closeDrawerContentDescRes) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                super.onDrawerSlide(drawerView, 0);
                lastNewActivitySelected = -1;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                switch (lastNewActivitySelected) {
                    case LIST_DRAWER_POS_HOME:
                        break;
                    case LIST_DRAWER_POS_CONNECT:
                        startActivity(new Intent(MainActivity.this, ConnectActivity.class));
                        break;
                    case LIST_DRAWER_POS_SELECT_TOPIC_OR_FORUM:
                        ChooseTopicOrForumLinkDialogFragment chooseLinkDialogFragment = new ChooseTopicOrForumLinkDialogFragment();
                        chooseLinkDialogFragment.show(getFragmentManager(), "ChooseTopicOrForumLinkDialogFragment");
                        listForDrawer.setItemChecked(0, true);
                        break;
                    case LIST_DRAWER_POS_SETTING:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        currentFragmentType = sharedPref.getInt(getString(R.string.prefLastFragmentViewed), FRAG_FORUM_SHOW);
        listForDrawer.setAdapter(new ArrayAdapter<>(this, R.layout.draweritem_row, getResources().getStringArray(R.array.itemChoiceDrawerList)));
        listForDrawer.setOnItemClickListener(itemInDrawerClickedListener);
        layoutForDrawer.addDrawerListener(toggleForDrawer);
        layoutForDrawer.setDrawerShadow(R.drawable.shadow_drawer, GravityCompat.START);

        if (savedInstanceState == null) {
            if (currentFragmentType == FRAG_FORUM_SHOW) {
                createNewFragmentForForumRead(null);
            } else if (currentFragmentType == FRAG_TOPIC_SHOW) {
                createNewFragmentForTopicRead(null);
            }
        }

        if (sharedPref.getBoolean(getString(R.string.prefIsFirstLaunch), true)) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            HelpFirstLaunchDialogFragment firstLaunchDialogFragment = new HelpFirstLaunchDialogFragment();
            firstLaunchDialogFragment.show(getFragmentManager(), "HelpFirstLaunchDialogFragment");
            sharedPrefEdit.putBoolean(getString(R.string.prefIsFirstLaunch), false);
            sharedPrefEdit.apply();
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggleForDrawer.syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        listForDrawer.setItemChecked(0, true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleForDrawer.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggleForDrawer.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragmentType == FRAG_TOPIC_SHOW) {
            AbsShowTopicFragment currentFragment = (AbsShowTopicFragment) getFragmentManager().findFragmentById(R.id.content_frame_main);
            createNewFragmentForForumRead(JVCParser.getForumForTopicLink(currentFragment.getCurrentTopicLink()));
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void newModeRequested(int newMode) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        boolean errorMode = false;

        if (newMode == AbsShowTopicFragment.MODE_IRC) {
            getFragmentManager().beginTransaction().replace(R.id.content_frame_main, new ShowTopicIRCFragment()).commit();
        } else if (newMode == AbsShowTopicFragment.MODE_FORUM) {
            getFragmentManager().beginTransaction().replace(R.id.content_frame_main, new ShowTopicForumFragment()).commit();
        } else {
            errorMode = true;
        }

        if (!errorMode) {
            sharedPrefEdit.putInt(getString(R.string.prefCurrentTopicMode), newMode);
            sharedPrefEdit.apply();
        }
    }

    @Override
    public void newTopicOrForumAvailable(String newTopicOrForumLink) {
        if (JVCParser.checkIfItsForumLink(newTopicOrForumLink)) {
            if (currentFragmentType == FRAG_FORUM_SHOW) {
                ShowForumFragment currentFragment = (ShowForumFragment) getFragmentManager().findFragmentById(R.id.content_frame_main);
                currentFragment.goToThisNewPage(newTopicOrForumLink);
            } else {
                createNewFragmentForForumRead(newTopicOrForumLink);
            }
        } else {
            if (currentFragmentType == FRAG_TOPIC_SHOW) {
                AbsShowTopicFragment currentFragment = (AbsShowTopicFragment) getFragmentManager().findFragmentById(R.id.content_frame_main);
                currentFragment.setNewTopicLink(newTopicOrForumLink);
            } else {
                createNewFragmentForTopicRead(newTopicOrForumLink);
            }
        }
    }

    @Override
    public void setReadNewTopic(String newTopicLink) {
        newTopicOrForumAvailable(newTopicLink);
    }
}
