package com.franckrj.respawnirc.jvcforumlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.jvcforum.ShowForumActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.base.AbsNavigationViewActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class SelectForumInListActivity extends AbsNavigationViewActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected {
    private static final String SAVE_SEARCH_FORUM_CONTENT = "saveSearchForumContent";
    private static final String SAVE_SEARCH_TEXT_IS_OPENED = "saveSearchTextIsOpened";
    private static final String SAVE_DEFAULT_FORUMLIST_IS_VISIBLE = "saveDefaultForumlistIsVisible";

    private JVCForumListAdapter adapterForForumList = null;
    private EditText textForSearch = null;
    private MenuItem searchExpandableItem = null;
    private GetSearchedForums currentAsyncTaskForGetSearchedForums = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private TextView noResultFoundTextView = null;
    private ScrollView defaultForumListLayout = null;
    private String lastSearchedText = null;
    private boolean searchTextIsOpened = false;

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

    private final AbsWebRequestAsyncTask.RequestIsStarted getSearchedForumsIsStartedListener = new AbsWebRequestAsyncTask.RequestIsStarted() {
        @Override
        public void onRequestIsStarted() {
            adapterForForumList.setNewListOfForums(null);
            noResultFoundTextView.setVisibility(View.GONE);
            defaultForumListLayout.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(true);
        }
    };

    private final AdapterView.OnItemClickListener forumClickedInListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            readNewTopicOrForum(adapterForForumList.getForumLinkAtThisPos(position), false);
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> getSearchedForumsIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            ArrayList<JVCParser.NameAndLink> newListOfForums = null;
            swipeRefresh.setRefreshing(false);

            currentAsyncTaskForGetSearchedForums = null;

            if (reqResult != null) {
                if (reqResult.startsWith("respawnirc:redirect:")) {
                    String newLink = reqResult.substring(("respawnirc:redirect:").length());
                    if (!newLink.isEmpty()) {
                        readNewTopicOrForum("http://www.jeuxvideo.com" + newLink, false);
                        return;
                    }
                } else {
                    newListOfForums = JVCParser.getListOfForumsInSearchPage(reqResult);
                }
            }

            adapterForForumList.setNewListOfForums(newListOfForums);
            if (adapterForForumList.getCount() == 0) {
                noResultFoundTextView.setVisibility(View.VISIBLE);
            } else {
                noResultFoundTextView.setVisibility(View.GONE);
            }
            defaultForumListLayout.setVisibility(View.GONE);
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
                noResultFoundTextView.setVisibility(View.GONE);
                defaultForumListLayout.setVisibility(View.VISIBLE);
            } else if (currentAsyncTaskForGetSearchedForums == null) {
                currentAsyncTaskForGetSearchedForums = new GetSearchedForums();
                currentAsyncTaskForGetSearchedForums.setRequestIsStartedListener(getSearchedForumsIsStartedListener);
                currentAsyncTaskForGetSearchedForums.setRequestIsFinishedListener(getSearchedForumsIsFinishedListener);
                currentAsyncTaskForGetSearchedForums.execute(textForSearch.getText().toString());
            }

            Utils.hideSoftKeyboard(SelectForumInListActivity.this);
        }
    }

    private void readNewTopicOrForum(String linkToTopicOrForum, boolean goToLastPage) {
        if (linkToTopicOrForum != null) {
            Intent newShowForumIntent = new Intent(this, ShowForumActivity.class);
            newShowForumIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, linkToTopicOrForum);
            newShowForumIntent.putExtra(ShowForumActivity.EXTRA_GO_TO_LAST_PAGE, goToLastPage);
            startActivity(newShowForumIntent);
            finish();
        }
    }

    private void stopAllCurrentTasks() {
        if (currentAsyncTaskForGetSearchedForums != null) {
            currentAsyncTaskForGetSearchedForums.clearListenersAndCancel();
            currentAsyncTaskForGetSearchedForums = null;
        }
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        swipeRefresh = findViewById(R.id.swiperefresh_selectforum);
        noResultFoundTextView = findViewById(R.id.text_noresultfound_selectforum);
        defaultForumListLayout = findViewById(R.id.default_forumlist_layout_selectforum);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorAccentThemeLight);
        noResultFoundTextView.setVisibility(View.GONE);

        //TODO: Tout ce bloc c'est du tempo, à changer
        View jvcCatTitle = findViewById(R.id.forumlist_cat_jvc_selectforum);
        jvcCatTitle.setOnClickListener(new View.OnClickListener() {
            private final View jvcContent = findViewById(R.id.forumlist_content_jvc_selectforum);

            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(defaultForumListLayout);
                if (jvcContent.getVisibility() == View.VISIBLE) {
                    jvcContent.setVisibility(View.GONE);
                } else {
                    jvcContent.setVisibility(View.VISIBLE);
                }
            }
        });

        ListView forumListView = findViewById(R.id.forum_list_selectforum);
        adapterForForumList = new JVCForumListAdapter(this);
        forumListView.setAdapter(adapterForForumList);
        forumListView.setOnItemClickListener(forumClickedInListView);

        if (savedInstanceState != null) {
            lastSearchedText = savedInstanceState.getString(SAVE_SEARCH_FORUM_CONTENT, null);
            searchTextIsOpened = savedInstanceState.getBoolean(SAVE_SEARCH_TEXT_IS_OPENED, false);
            adapterForForumList.loadFromBundle(savedInstanceState);
            if (!savedInstanceState.getBoolean(SAVE_DEFAULT_FORUMLIST_IS_VISIBLE, true)) {
                if (adapterForForumList.getCount() == 0) {
                    noResultFoundTextView.setVisibility(View.VISIBLE);
                }
                defaultForumListLayout.setVisibility(View.GONE);
            }
        } else {
            updateMpAndNotifNumberShowed(null, null);
        }

        if (PrefsManager.getBool(PrefsManager.BoolPref.Names.IS_FIRST_LAUNCH)) {
            HelpFirstLaunchDialogFragment firstLaunchDialogFragment = new HelpFirstLaunchDialogFragment();
            firstLaunchDialogFragment.show(getSupportFragmentManager(), "HelpFirstLaunchDialogFragment");
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

        outState.putBoolean(SAVE_SEARCH_TEXT_IS_OPENED, searchTextIsOpened);
        outState.putBoolean(SAVE_DEFAULT_FORUMLIST_IS_VISIBLE, (defaultForumListLayout.getVisibility() == View.VISIBLE));
        outState.putString(SAVE_SEARCH_FORUM_CONTENT, null);
        if (textForSearch != null && searchExpandableItem != null) {
            if (searchExpandableItem.isActionViewExpanded()) {
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
        ImageButton buttonForSearch = rootView.findViewById(R.id.search_button_searchlayout);
        textForSearch = rootView.findViewById(R.id.search_text_searchlayout);
        textForSearch.setHint(R.string.forumSearch);
        textForSearch.setOnEditorActionListener(actionInSearchEditTextListener);
        buttonForSearch.setOnClickListener(searchButtonClickedListener);

        searchExpandableItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchTextIsOpened = false;
                textForSearch.setText("");
                stopAllCurrentTasks();
                adapterForForumList.setNewListOfForums(null);
                noResultFoundTextView.setVisibility(View.GONE);
                defaultForumListLayout.setVisibility(View.VISIBLE);
                Utils.hideSoftKeyboard(SelectForumInListActivity.this);
                return true;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (!searchTextIsOpened) {
                    searchTextIsOpened = true;
                    textForSearch.requestFocus();
                    Utils.showSoftKeyboard(SelectForumInListActivity.this);
                }
                return true;
            }
        });

        if (lastSearchedText != null) {
            textForSearch.setText(lastSearchedText);
            searchExpandableItem.expandActionView();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_topic_or_forum_selectforum:
                ChooseTopicOrForumLinkDialogFragment chooseLinkDialogFragment = new ChooseTopicOrForumLinkDialogFragment();
                chooseLinkDialogFragment.show(getSupportFragmentManager(), "ChooseTopicOrForumLinkDialogFragment");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void initializeViewAndToolbar() {
        setContentView(R.layout.activity_selectforum);
        initToolbar(R.id.custom_toolbar);

        layoutForDrawer = findViewById(R.id.layout_drawer_selectforum);
        navigationMenuList = findViewById(R.id.navigation_menu_selectforum);
    }

    @Override
    public void newForumOrTopicToRead(String link, boolean itsAForum, boolean isWhenDrawerIsClosed, boolean fromLongClick) {
        if (isWhenDrawerIsClosed) {
            readNewTopicOrForum(link, fromLongClick);
        }
    }

    @Override
    public void newTopicOrForumAvailable(String newTopicOrForumLink) {
        readNewTopicOrForum(newTopicOrForumLink, false);
    }

    private static class GetSearchedForums extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 0) {
                String pageResult;
                WebManager.WebInfos currentWebInfos = initWebInfos("", false);

                pageResult = WebManager.sendRequest("http://www.jeuxvideo.com/forums/recherche.php", "GET", "q=" + Utils.encodeStringToUrlString(params[0]), currentWebInfos);

                if (!currentWebInfos.currentUrl.isEmpty() && !currentWebInfos.currentUrl.startsWith("http://www.jeuxvideo.com/forums/recherche.php")) {
                    return "respawnirc:redirect:" + currentWebInfos.currentUrl;
                } else {
                    return pageResult;
                }
            } else {
                return null;
            }
        }
    }
}
