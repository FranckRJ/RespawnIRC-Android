package com.franckrj.respawnirc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.dialogs.RefreshFavDialogFragment;
import com.franckrj.respawnirc.jvcviewers.ShowForumActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.NavigationViewUtil;
import com.franckrj.respawnirc.utils.WebManager;

import java.net.URLEncoder;
import java.util.ArrayList;

public class SelectForumActivity extends AppCompatActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected,
                                                                      RefreshFavDialogFragment.NewFavsAvailable, JVCForumsAdapter.NewForumSelected,
                                                                      NavigationViewUtil.NewForumOrTopicNeedToBeRead {
    private NavigationViewUtil navigationView = null;
    private SharedPreferences sharedPref = null;
    private JVCForumsAdapter adapterForForums = null;
    private EditText textForSearch = null;
    private GetSearchedForums currentAsyncTaskForGetSearchedForums = null;
    private SwipeRefreshLayout swipeRefresh = null;

    private View.OnClickListener searchButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            performSearch();
        }
    };

    private TextView.OnEditorActionListener actionInSearchEditTextListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        }
    };

    private void performSearch() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView;

        if (textForSearch.getText().toString().isEmpty()) {
            adapterForForums.setNewListOfForums(null);
        } else if (currentAsyncTaskForGetSearchedForums == null) {
            currentAsyncTaskForGetSearchedForums = new GetSearchedForums();
            currentAsyncTaskForGetSearchedForums.execute(textForSearch.getText().toString());
        }

        focusedView = getCurrentFocus();
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void readNewTopicOrForum(String linkToTopicOrForum) {
        if (linkToTopicOrForum != null) {
            Intent newShowForumIntent = new Intent(this, ShowForumActivity.class);
            newShowForumIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, linkToTopicOrForum);
            startActivity(newShowForumIntent);
            finish();
        }
    }

    private void stopAllCurrentTasks() {
        if (currentAsyncTaskForGetSearchedForums != null) {
            currentAsyncTaskForGetSearchedForums.cancel(true);
            currentAsyncTaskForGetSearchedForums = null;
        }
        swipeRefresh.setRefreshing(false);
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

        ImageButton buttonForSearch = (ImageButton) findViewById(R.id.searchforum_button_selectforum);
        textForSearch = (EditText) findViewById(R.id.searchforum_text_selectforum);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_selectforum);
        buttonForSearch.setOnClickListener(searchButtonClickedListener);
        textForSearch.setOnEditorActionListener(actionInSearchEditTextListener);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);

        ExpandableListView forumListView = (ExpandableListView) findViewById(R.id.forum_expendable_list_selectforum);
        adapterForForums = new JVCForumsAdapter(this);
        forumListView.setAdapter(adapterForForums);
        forumListView.setGroupIndicator(null);
        forumListView.setOnGroupClickListener(adapterForForums);
        forumListView.setOnChildClickListener(adapterForForums);

        if (savedInstanceState != null) {
            adapterForForums.loadFromBundle(savedInstanceState);
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
    public void onPause() {
        super.onPause();
        stopAllCurrentTasks();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        adapterForForums.saveToBundle(outState);
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

    private class GetSearchedForums extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            adapterForForums.clearListOfForums();
            swipeRefresh.setRefreshing(true);
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 0) {
                String pageResult;
                String searchToDo = params[0].replace(" ", "+");
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;

                try {
                    searchToDo = URLEncoder.encode(searchToDo, "UTF-8");
                } catch (Exception e) {
                    searchToDo = "";
                    e.printStackTrace();
                }

                pageResult = WebManager.sendRequest("http://www.jeuxvideo.com/forums/recherche.php", "GET", "q=" + searchToDo, "", currentWebInfos);

                if (!currentWebInfos.currentUrl.isEmpty() && !currentWebInfos.currentUrl.startsWith("http://www.jeuxvideo.com/forums/recherche.php")) {
                    return "respawnirc:redirect:" + currentWebInfos.currentUrl;
                } else {
                    return pageResult;
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String pageResult) {
            super.onPostExecute(pageResult);
            swipeRefresh.setRefreshing(false);

            currentAsyncTaskForGetSearchedForums = null;

            if (pageResult != null) {
                if (pageResult.startsWith("respawnirc:redirect:")) {
                    String newLink = pageResult.substring(("respawnirc:redirect:").length());
                    if (!newLink.isEmpty()) {
                        readNewTopicOrForum("http://www.jeuxvideo.com" + newLink);
                        return;
                    }
                } else {
                    adapterForForums.setNewListOfForums(JVCParser.getListOfForumsInSearchPage(pageResult));
                    return;
                }
            }

            adapterForForums.setNewListOfForums(new ArrayList<JVCParser.NameAndLink>());
        }
    }
}
