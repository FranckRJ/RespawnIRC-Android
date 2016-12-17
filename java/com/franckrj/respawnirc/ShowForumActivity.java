package com.franckrj.respawnirc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.jvctopictools.JVCTopicGetter;
import com.franckrj.respawnirc.jvctopictools.ShowForumFragment;
import com.franckrj.respawnirc.utils.JVCParser;

public class ShowForumActivity extends AbsShowSomethingActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected,
                                                    ShowForumFragment.NewTopicWantRead, JVCTopicGetter.NewForumNameAvailable,
                                                    JVCTopicGetter.ForumLinkChanged {
    private static final int LIST_DRAWER_POS_HOME = 0;
    private static final int LIST_DRAWER_POS_CONNECT = 1;
    private static final int LIST_DRAWER_POS_SELECT_TOPIC_OR_FORUM = 2;
    private static final int LIST_DRAWER_POS_SETTING = 3;

    private DrawerLayout layoutForDrawer = null;
    private ListView listForDrawer = null;
    private ActionBarDrawerToggle toggleForDrawer = null;
    private int lastNewActivitySelected = -1;
    private SharedPreferences sharedPref = null;
    private String currentTitle = "";

    private ListView.OnItemClickListener itemInDrawerClickedListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            lastNewActivitySelected = position;
            listForDrawer.setItemChecked(position, true);
            layoutForDrawer.closeDrawer(listForDrawer);
        }
    };

    public ShowForumActivity() {
        lastPage = 100;
    }

    protected AbsShowSomethingFragment createNewFragmentForRead(String possibleForumLink) {
        ShowForumFragment currentFragment = new ShowForumFragment();

        if (possibleForumLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(ShowForumFragment.ARG_FORUM_LINK, possibleForumLink);
            currentFragment.setArguments(argForFrag);
        }

        return currentFragment;
    }

    private void setNewForumLink(String newLink) {
        currentLink = newLink;
        updateAdapterForPagerView();
        updateCurrentItemAndButtonsToCurrentLink();
        if (pagerView.getCurrentItem() > 0) {
            clearPageForThisFragment(0);
        }
    }

    private void setTopicOrForum(String link, boolean updateForumFragIfNeeded, String topicName) {
        if (link != null) {
            if (!link.isEmpty()) {
                link = JVCParser.formatThisUrl(link);
            }

            if (JVCParser.checkIfItsForumLink(link)) {
                if (!JVCParser.getPageNumberForThisForumLink(link).isEmpty()) {
                    setNewForumLink(link);
                    return;
                }
            } else if (!JVCParser.getPageNumberForThisTopicLink(link).isEmpty()) {
                Intent newShowTopicIntent = new Intent(this, ShowTopicActivity.class);

                if (updateForumFragIfNeeded) {
                    setNewForumLink(JVCParser.getForumForTopicLink(link));
                }

                if (topicName != null) {
                    newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_NAME, topicName);
                }
                if (!currentTitle.equals(getString(R.string.app_name))) {
                    newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_FORUM_NAME, currentTitle);
                }

                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, link);
                startActivity(newShowTopicIntent);
                return;
            }
        }

        Toast.makeText(this, R.string.errorInvalidLink, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showforum);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_showforum);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        pagerView = (ViewPager) findViewById(R.id.pager_showforum);
        layoutForDrawer = (DrawerLayout) findViewById(R.id.layout_drawer_showforum);
        listForDrawer = (ListView) findViewById(R.id.view_left_drawer_showforum);
        firstPageButton = (Button) findViewById(R.id.firstpage_button_showforum);
        previousPageButton = (Button) findViewById(R.id.previouspage_button_showforum);
        currentPageButton = (Button) findViewById(R.id.currentpage_button_showforum);
        nextPageButton = (Button) findViewById(R.id.nextpage_button_showforum);

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
                        startActivity(new Intent(ShowForumActivity.this, ConnectActivity.class));
                        break;
                    case LIST_DRAWER_POS_SELECT_TOPIC_OR_FORUM:
                        ChooseTopicOrForumLinkDialogFragment chooseLinkDialogFragment = new ChooseTopicOrForumLinkDialogFragment();
                        chooseLinkDialogFragment.show(getFragmentManager(), "ChooseTopicOrForumLinkDialogFragment");
                        listForDrawer.setItemChecked(0, true);
                        break;
                    case LIST_DRAWER_POS_SETTING:
                        startActivity(new Intent(ShowForumActivity.this, SettingsActivity.class));
                        break;
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        updateAdapterForPagerView();
        pagerView.addOnPageChangeListener(pageChangeOnPagerListener);
        listForDrawer.setAdapter(new ArrayAdapter<>(this, R.layout.draweritem_row, getResources().getStringArray(R.array.itemChoiceDrawerList)));
        listForDrawer.setOnItemClickListener(itemInDrawerClickedListener);
        layoutForDrawer.addDrawerListener(toggleForDrawer);
        layoutForDrawer.setDrawerShadow(R.drawable.shadow_drawer, GravityCompat.START);
        initializeNavigationButtons();

        currentLink = sharedPref.getString(getString(R.string.prefForumUrlToFetch), "");
        if (savedInstanceState == null) {
            currentTitle = getString(R.string.app_name);
            updateCurrentItemAndButtonsToCurrentLink();
        } else {
            currentTitle = savedInstanceState.getString(getString(R.string.saveCurrentForumTitle), getString(R.string.app_name));
            updateNavigationButtons();
        }
        setTitle(currentTitle);

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
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        listForDrawer.setItemChecked(0, true);
        sharedPrefEdit.putInt(getString(R.string.prefLastActivityViewed), MainActivity.ACTIVITY_SHOW_FORUM);
        sharedPrefEdit.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!currentLink.isEmpty()) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            sharedPrefEdit.putString(getString(R.string.prefForumUrlToFetch), setShowedPageNumberForThisLink(currentLink, pagerView.getCurrentItem() + 1));
            sharedPrefEdit.apply();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveCurrentForumTitle), currentTitle);
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
    public void newTopicOrForumAvailable(String newTopicOrForumLink) {
        setTopicOrForum(newTopicOrForumLink, true, null);
    }

    @Override
    public void setReadNewTopic(String newTopicLink, String newTopicName) {
        setTopicOrForum(newTopicLink, false, newTopicName);
    }

    @Override
    public void getNewForumName(String newForumName) {
        if (!newForumName.isEmpty()) {
            currentTitle = newForumName;
        } else {
            currentTitle = getString(R.string.app_name);
        }
        setTitle(currentTitle);
    }

    @Override
    public void updateForumLink(String newForumLink) {
        currentLink = newForumLink;
    }

    @Override
    protected int getShowablePageNumberForThisLink(String link) {
        return ((Integer.parseInt(JVCParser.getPageNumberForThisForumLink(link)) - 1) / 25) + 1;
    }

    @Override
    protected String setShowedPageNumberForThisLink(String link, int newPageNumber) {
        return JVCParser.setPageNumberForThisForumLink(link, ((newPageNumber - 1) * 25) + 1);
    }
}
