package com.franckrj.respawnirc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.dialogs.RefreshFavDialogFragment;
import com.franckrj.respawnirc.jvcviewers.ShowForumActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.NavigationViewUtil;

import java.util.ArrayList;

public class SelectForumActivity extends AppCompatActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected,
                                                                      RefreshFavDialogFragment.NewFavsAvailable, JVCForumsAdapter.NewForumSelected,
                                                                      NavigationViewUtil.NewForumOrTopicNeedToBeRead {
    private NavigationViewUtil navigationView = null;
    private SharedPreferences sharedPref = null;
    private ExpandableListView forumListView = null;
    private JVCForumsAdapter adapterForForums = null;

    private void readNewTopicOrForum(String linkToTopicOrForum) {
        if (linkToTopicOrForum != null) {
            Intent newShowForumIntent = new Intent(this, ShowForumActivity.class);
            newShowForumIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, linkToTopicOrForum);
            startActivity(newShowForumIntent);
            finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectforum);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_selectforum);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        navigationView = new NavigationViewUtil(this, sharedPref, R.id.action_home_navigation);
        navigationView.initialize((DrawerLayout) findViewById(R.id.layout_drawer_selectforum), (NavigationView) findViewById(R.id.navigation_view_selectforum));

        adapterForForums = new JVCForumsAdapter(this);
        forumListView = (ExpandableListView) findViewById(R.id.forum_expendable_list_selectforum);
        forumListView.setAdapter(adapterForForums);
        forumListView.setGroupIndicator(null);
        forumListView.setOnGroupClickListener(adapterForForums);
        forumListView.setOnChildClickListener(adapterForForums);

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
        navigationView.syncStateOfToggleForDrawer();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        sharedPrefEdit.putInt(getString(R.string.prefLastActivityViewed), MainActivity.ACTIVITY_SELECT_FORUM);
        sharedPrefEdit.apply();

        navigationView.updateNavigationViewIfNeeded();
    }

    @Override
    public void onBackPressed() {
        if (!navigationView.closeNavigationView()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navigationView.onConfigurationChangedForToggleForDrawer(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (navigationView.onOptionsItemSelectedForToggleForDrawer(item)) {
            return true;
        }

        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void newTopicOrForumAvailable(String newTopicOrForumLink) {
        readNewTopicOrForum(newTopicOrForumLink);
    }

    @Override
    public void getNewFavs(ArrayList<JVCParser.NameAndLink> listOfFavs, int typeOfFav) {
        if (!navigationView.updateFavs(listOfFavs, typeOfFav)) {
            Toast.makeText(this, R.string.errorDuringFetchFavs, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void newForumOrTopicToRead(String link, boolean itsAForum, boolean isWhenDrawerIsClosed) {
        if (isWhenDrawerIsClosed) {
            readNewTopicOrForum(link);
        }
    }

    @Override
    public void getNewForumLink(String link) {
        readNewTopicOrForum(link);
    }
}
