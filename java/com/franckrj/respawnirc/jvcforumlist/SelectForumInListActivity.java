package com.franckrj.respawnirc.jvcforumlist;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.NavigationMenuListView;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.jvcforum.ShowForumActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.AbsNavigationViewActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class SelectForumInListActivity extends AbsNavigationViewActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected,
                                                                              JVCForumListAdapter.NewForumSelected {
    private static final String SAVE_SEARCH_FORUM_CONTENT = "saveSearchForumContent";

    private JVCForumListAdapter adapterForForumList = null;
    private EditText textForSearch = null;
    private MenuItem searchExpandableItem = null;
    private GetSearchedForums currentAsyncTaskForGetSearchedForums = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private String lastSearchedText = null;

    private final View.OnClickListener searchButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            performSearch();
        }
    };

    private final TextView.OnEditorActionListener actionInSearchEditTextListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        }
    };

    public SelectForumInListActivity() {
        idOfBaseActivity = ITEM_ID_HOME;
    }

    private void performSearch() {
        if (textForSearch != null) {
            if (textForSearch.getText().toString().isEmpty()) {
                stopAllCurrentTasks();
                adapterForForumList.setNewListOfForums(null);
            } else if (currentAsyncTaskForGetSearchedForums == null) {
                currentAsyncTaskForGetSearchedForums = new GetSearchedForums();
                currentAsyncTaskForGetSearchedForums.execute(textForSearch.getText().toString());
            }

            Utils.hideSoftKeyboard(SelectForumInListActivity.this);
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
        swipeRefresh.setColorSchemeResources(R.color.colorAccentThemeLight);

        ExpandableListView forumListView = (ExpandableListView) findViewById(R.id.forum_expendable_list_selectforum);
        adapterForForumList = new JVCForumListAdapter(this);
        forumListView.setAdapter(adapterForForumList);
        forumListView.setOnGroupClickListener(adapterForForumList);
        forumListView.setOnChildClickListener(adapterForForumList);

        if (savedInstanceState != null) {
            lastSearchedText = savedInstanceState.getString(SAVE_SEARCH_FORUM_CONTENT, null);
            adapterForForumList.loadFromBundle(savedInstanceState);
        }

        if (PrefsManager.getBool(PrefsManager.BoolPref.Names.IS_FIRST_LAUNCH)) {
            HelpFirstLaunchDialogFragment firstLaunchDialogFragment = new HelpFirstLaunchDialogFragment();
            firstLaunchDialogFragment.show(getFragmentManager(), "HelpFirstLaunchDialogFragment");
            PrefsManager.putBool(PrefsManager.BoolPref.Names.IS_FIRST_LAUNCH, false);
            PrefsManager.applyChanges();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefsManager.putInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED, MainActivity.ACTIVITY_SELECT_FORUM_IN_LIST);
        PrefsManager.applyChanges();
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        adapterForForumList.saveToBundle(outState);

        outState.putString(SAVE_SEARCH_FORUM_CONTENT, null);
        if (textForSearch != null && searchExpandableItem != null) {
            if (MenuItemCompat.isActionViewExpanded(searchExpandableItem)) {
                outState.putString(SAVE_SEARCH_FORUM_CONTENT, textForSearch.getText().toString());
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
                adapterForForumList.setNewListOfForums(null);
                Utils.hideSoftKeyboard(SelectForumInListActivity.this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_topic_or_forum_selectforum:
                ChooseTopicOrForumLinkDialogFragment chooseLinkDialogFragment = new ChooseTopicOrForumLinkDialogFragment();
                chooseLinkDialogFragment.show(getFragmentManager(), "ChooseTopicOrForumLinkDialogFragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        navigationMenuList = (NavigationMenuListView) findViewById(R.id.navigation_menu_selectforum);
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

    @Override
    public void newTopicOrForumAvailable(String newTopicOrForumLink) {
        readNewTopicOrForum(newTopicOrForumLink);
    }

    private class GetSearchedForums extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            adapterForForumList.clearListOfForums();
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
                    adapterForForumList.setNewListOfForums(JVCParser.getListOfForumsInSearchPage(pageResult));
                    return;
                }
            }

            adapterForForumList.setNewListOfForums(new ArrayList<JVCParser.NameAndLink>());
        }
    }
}
