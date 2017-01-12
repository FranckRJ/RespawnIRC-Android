package com.franckrj.respawnirc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.jvcviewers.ShowForumActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.AbsNavigationViewActivity;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.net.URLEncoder;
import java.util.ArrayList;

public class SelectForumActivity extends AbsNavigationViewActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected,
                                                                              JVCForumsAdapter.NewForumSelected {
    private JVCForumsAdapter adapterForForums = null;
    private EditText textForSearch = null;
    private MenuItem searchExpandableItem = null;
    private GetSearchedForums currentAsyncTaskForGetSearchedForums = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private String lastSearchedText = null;

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

    public SelectForumActivity() {
        idOfBaseActivity = R.id.action_home_navigation;
    }

    private void performSearch() {
        if (textForSearch != null) {
            if (textForSearch.getText().toString().isEmpty()) {
                stopAllCurrentTasks();
                adapterForForums.setNewListOfForums(null);
            } else if (currentAsyncTaskForGetSearchedForums == null) {
                currentAsyncTaskForGetSearchedForums = new GetSearchedForums();
                currentAsyncTaskForGetSearchedForums.execute(textForSearch.getText().toString());
            }

            Utils.hideSoftKeyboard(SelectForumActivity.this);
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

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh_selectforum);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorAccent);

        ExpandableListView forumListView = (ExpandableListView) findViewById(R.id.forum_expendable_list_selectforum);
        adapterForForums = new JVCForumsAdapter(this);
        forumListView.setAdapter(adapterForForums);
        forumListView.setOnGroupClickListener(adapterForForums);
        forumListView.setOnChildClickListener(adapterForForums);

        if (savedInstanceState != null) {
            lastSearchedText = savedInstanceState.getString(getString(R.string.saveSearchForumContent), null);
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
    public void onResume() {
        super.onResume();
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        sharedPrefEdit.putInt(getString(R.string.prefLastActivityViewed), MainActivity.ACTIVITY_SELECT_FORUM);
        sharedPrefEdit.apply();
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

        outState.putString(getString(R.string.saveSearchForumContent), null);
        if (textForSearch != null && searchExpandableItem != null) {
            if (MenuItemCompat.isActionViewExpanded(searchExpandableItem)) {
                outState.putString(getString(R.string.saveSearchForumContent), textForSearch.getText().toString());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_selectforum, menu);
        searchExpandableItem = menu.findItem(R.id.action_search_selectforum);

        View rootView = searchExpandableItem.getActionView();
        ImageButton buttonForSearch = (ImageButton) rootView.findViewById(R.id.searchforum_button_selectforum);
        textForSearch = (EditText) rootView.findViewById(R.id.searchforum_text_selectforum);
        textForSearch.setOnEditorActionListener(actionInSearchEditTextListener);
        buttonForSearch.setOnClickListener(searchButtonClickedListener);

        MenuItemCompat.setOnActionExpandListener(searchExpandableItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                textForSearch.setText("");
                stopAllCurrentTasks();
                adapterForForums.setNewListOfForums(null);
                Utils.hideSoftKeyboard(SelectForumActivity.this);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                textForSearch.requestFocus();
                inputManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                return true;
            }
        });

        if (lastSearchedText != null) {
            textForSearch.setText(lastSearchedText);
            MenuItemCompat.expandActionView(searchExpandableItem);
        }

        return true;
    }

    @Override
    protected void initializeViewAndToolbar() {
        setContentView(R.layout.activity_selectforum);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_selectforum);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        layoutForDrawer = (DrawerLayout) findViewById(R.id.layout_drawer_selectforum);
        navigationForDrawer = (NavigationView) findViewById(R.id.navigation_view_selectforum);
    }

    @Override
    public void newTopicOrForumAvailable(String newTopicOrForumLink) {
        readNewTopicOrForum(newTopicOrForumLink);
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

                pageResult = WebManager.sendRequest("http://www.jeuxvideo.com/forums/recherche.php", "GET", "q=" + Utils.convertStringToUrlString(searchToDo), "", currentWebInfos);

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
