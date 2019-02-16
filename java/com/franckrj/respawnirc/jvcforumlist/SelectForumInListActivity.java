package com.franckrj.respawnirc.jvcforumlist;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.transition.TransitionManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class SelectForumInListActivity extends AbsNavigationViewActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected {
    private static final String SAVE_SEARCH_FORUM_CONTENT = "saveSearchForumContent";
    private static final String SAVE_SEARCH_TEXT_IS_OPENED = "saveSearchTextIsOpened";
    private static final String SAVE_BASE_FORUMLIST_IS_VISIBLE = "saveBaseForumlistIsVisible";
    private static final String SAVE_BASE_FORUMLIST_CAT_BLABLA_IS_OPENED = "saveBaseForumlistCatBlablaIsOpened";
    private static final String SAVE_BASE_FORUMLIST_CAT_JVC_IS_OPENED = "saveBaseForumlistCatJvcIsOpened";
    private static final String SAVE_BASE_FORUMLIST_CAT_VIDEOGAME_IS_OPENED = "saveBaseForumlistCatVideogameIsOpened";
    private static final String SAVE_BASE_FORUMLIST_CAT_HARDWARE_IS_OPENED = "saveBaseForumlistCatHardwareIsOpened";
    private static final String SAVE_BASE_FORUMLIST_CAT_HOBBIES_IS_OPENED = "saveBaseForumlistCatHobbiesIsOpened";
    private static final String SAVE_BASE_FORUMLIST_SCROLL_POSITION = "saveBaseForumlistScrollPosition";

    private JVCForumListAdapter adapterForForumList = null;
    private EditText textForSearch = null;
    private MenuItem searchExpandableItem = null;
    private GetSearchedForums currentAsyncTaskForGetSearchedForums = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private TextView noResultFoundTextView = null;
    private ScrollView baseForumListLayout = null;
    private BaseForumlistInfos baseForumlistCatBlabla = null;
    private BaseForumlistInfos baseForumlistCatJvc = null;
    private BaseForumlistInfos baseForumlistCatVideogame = null;
    private BaseForumlistInfos baseForumlistCatHardware = null;
    private BaseForumlistInfos baseForumlistCatHobbies = null;
    private String lastSearchedText = null;
    private boolean searchTextIsOpened = false;

    private final View.OnClickListener searchButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            performSearch();
        }
    };

    private final View.OnClickListener baseForumButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null && view.getTag() instanceof String) {
                readNewTopicOrForum((String) view.getTag(), false);
            }
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
            baseForumListLayout.setVisibility(View.GONE);
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
            baseForumListLayout.setVisibility(View.GONE);
        }
    };

    public SelectForumInListActivity() {
        super();
        idOfBaseActivity = ITEM_ID_HOME;
    }

    private void performSearch() {
        if (textForSearch != null) {
            if (textForSearch.getText().toString().isEmpty()) {
                stopAllCurrentTasks();
                adapterForForumList.setNewListOfForums(null);
                noResultFoundTextView.setVisibility(View.GONE);
                baseForumListLayout.setVisibility(View.VISIBLE);
            } else if (currentAsyncTaskForGetSearchedForums == null) {
                currentAsyncTaskForGetSearchedForums = new GetSearchedForums();
                currentAsyncTaskForGetSearchedForums.setRequestIsStartedListener(getSearchedForumsIsStartedListener);
                currentAsyncTaskForGetSearchedForums.setRequestIsFinishedListener(getSearchedForumsIsFinishedListener);
                currentAsyncTaskForGetSearchedForums.execute(textForSearch.getText().toString(), PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST));
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

    private BaseForumlistInfos initForumlistCategoryCard(@IdRes final int categoryId, @IdRes final int contentId, @IdRes final int otherContentId, @IdRes final int arrowId) {
        final ViewGroup contentView = findViewById(contentId);
        final ViewGroup otherContentView = (ViewGroup) (otherContentId == 0 ? null : findViewById(otherContentId));
        final ImageView arrowView = findViewById(arrowId);
        final BaseForumlistInfos currentForumlist = new BaseForumlistInfos(contentView, otherContentView, arrowView);
        View category = findViewById(categoryId);

        final int contentChildCount = currentForumlist.getNumberOfContentChildView();
        for (int i = 0; i < contentChildCount; ++i) {
            View currentContentChild = currentForumlist.getContentChildViewAt(i);

            if (currentContentChild instanceof Button) {
                currentContentChild.setOnClickListener(baseForumButtonClickedListener);
            }
        }

        category.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(baseForumListLayout);
                currentForumlist.setOpened(!currentForumlist.isOpened());
            }
        });

        return currentForumlist;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        swipeRefresh = findViewById(R.id.swiperefresh_selectforum);
        noResultFoundTextView = findViewById(R.id.text_noresultfound_selectforum);
        baseForumListLayout = findViewById(R.id.base_forumlist_layout_selectforum);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorControlHighlightThemeLight);
        noResultFoundTextView.setVisibility(View.GONE);
        hideMpAndNotifNumber();

        baseForumlistCatBlabla = initForumlistCategoryCard(R.id.forumlist_cat_blabla_selectforum, R.id.forumlist_content_blabla_selectforum,
                                                           0, R.id.forumlist_arrow_blabla_selectforum);
        baseForumlistCatJvc = initForumlistCategoryCard(R.id.forumlist_cat_jvc_selectforum, R.id.forumlist_content_jvc_selectforum,
                                                        0, R.id.forumlist_arrow_jvc_selectforum);
        baseForumlistCatVideogame = initForumlistCategoryCard(R.id.forumlist_cat_videogame_selectforum, R.id.forumlist_content_1_videogame_selectforum,
                                                              R.id.forumlist_content_2_videogame_selectforum, R.id.forumlist_arrow_videogame_selectforum);
        baseForumlistCatHardware = initForumlistCategoryCard(R.id.forumlist_cat_hardware_selectforum, R.id.forumlist_content_hardware_selectforum,
                                                             0, R.id.forumlist_arrow_hardware_selectforum);
        baseForumlistCatHobbies = initForumlistCategoryCard(R.id.forumlist_cat_hobbies_selectforum, R.id.forumlist_content_1_hobbies_selectforum,
                                                            R.id.forumlist_content_2_hobbies_selectforum, R.id.forumlist_arrow_hobbies_selectforum);

        ListView forumListView = findViewById(R.id.forum_list_selectforum);
        adapterForForumList = new JVCForumListAdapter(this);
        forumListView.setAdapter(adapterForForumList);
        forumListView.setOnItemClickListener(forumClickedInListView);

        if (savedInstanceState != null) {
            lastSearchedText = savedInstanceState.getString(SAVE_SEARCH_FORUM_CONTENT, null);
            searchTextIsOpened = savedInstanceState.getBoolean(SAVE_SEARCH_TEXT_IS_OPENED, false);
            adapterForForumList.loadFromBundle(savedInstanceState);

            if (!savedInstanceState.getBoolean(SAVE_BASE_FORUMLIST_IS_VISIBLE, true)) {
                if (adapterForForumList.getCount() == 0) {
                    noResultFoundTextView.setVisibility(View.VISIBLE);
                }
                baseForumListLayout.setVisibility(View.GONE);
            }

            if (savedInstanceState.getBoolean(SAVE_BASE_FORUMLIST_CAT_BLABLA_IS_OPENED, false)) {
                baseForumlistCatBlabla.setOpened(true);
            }
            if (savedInstanceState.getBoolean(SAVE_BASE_FORUMLIST_CAT_JVC_IS_OPENED, false)) {
                baseForumlistCatJvc.setOpened(true);
            }
            if (savedInstanceState.getBoolean(SAVE_BASE_FORUMLIST_CAT_VIDEOGAME_IS_OPENED, false)) {
                baseForumlistCatVideogame.setOpened(true);
            }
            if (savedInstanceState.getBoolean(SAVE_BASE_FORUMLIST_CAT_HARDWARE_IS_OPENED, false)) {
                baseForumlistCatHardware.setOpened(true);
            }
            if (savedInstanceState.getBoolean(SAVE_BASE_FORUMLIST_CAT_HOBBIES_IS_OPENED, false)) {
                baseForumlistCatHobbies.setOpened(true);
            }

            baseForumListLayout.post(new Runnable() {
                @Override
                public void run() {
                    baseForumListLayout.scrollTo(0, savedInstanceState.getInt(SAVE_BASE_FORUMLIST_SCROLL_POSITION, 0));
                }
            });
        }

        if (PrefsManager.getBool(PrefsManager.BoolPref.Names.IS_FIRST_LAUNCH)) {
            if (!getSupportFragmentManager().isStateSaved()) {
                HelpFirstLaunchDialogFragment firstLaunchDialogFragment = new HelpFirstLaunchDialogFragment();
                firstLaunchDialogFragment.show(getSupportFragmentManager(), "HelpFirstLaunchDialogFragment");
            }
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
        outState.putBoolean(SAVE_BASE_FORUMLIST_IS_VISIBLE, (baseForumListLayout.getVisibility() == View.VISIBLE));
        outState.putBoolean(SAVE_BASE_FORUMLIST_CAT_BLABLA_IS_OPENED, baseForumlistCatBlabla.isOpened());
        outState.putBoolean(SAVE_BASE_FORUMLIST_CAT_JVC_IS_OPENED, baseForumlistCatJvc.isOpened());
        outState.putBoolean(SAVE_BASE_FORUMLIST_CAT_VIDEOGAME_IS_OPENED, baseForumlistCatVideogame.isOpened());
        outState.putBoolean(SAVE_BASE_FORUMLIST_CAT_HARDWARE_IS_OPENED, baseForumlistCatHardware.isOpened());
        outState.putBoolean(SAVE_BASE_FORUMLIST_CAT_HOBBIES_IS_OPENED, baseForumlistCatHobbies.isOpened());
        outState.putInt(SAVE_BASE_FORUMLIST_SCROLL_POSITION, baseForumListLayout.getScrollY());
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
                baseForumListLayout.setVisibility(View.VISIBLE);
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
                if (!getSupportFragmentManager().isStateSaved()) {
                    ChooseTopicOrForumLinkDialogFragment chooseLinkDialogFragment = new ChooseTopicOrForumLinkDialogFragment();
                    chooseLinkDialogFragment.show(getSupportFragmentManager(), "ChooseTopicOrForumLinkDialogFragment");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void initializeViewAndToolbar() {
        setContentView(R.layout.activity_selectforum);
        initToolbar(R.id.toolbar_selectforum);

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
    protected void launchShowForumInfos() {
        //rien
    }

    @Override
    public void newTopicOrForumAvailable(String newTopicOrForumLink) {
        readNewTopicOrForum(newTopicOrForumLink, false);
    }

    private static class GetSearchedForums extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 1) {
                String pageResult;
                WebManager.WebInfos currentWebInfos = initWebInfos(params[1], false);

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

    private class BaseForumlistInfos {
        private final ViewGroup content;
        private final ViewGroup otherContent;
        private final ImageView arrow;
        private final int baseContentChildCount;

        public BaseForumlistInfos(ViewGroup newContent, ViewGroup newOtherContent, ImageView newArrow) {
            content = newContent;
            otherContent = newOtherContent;
            arrow = newArrow;

            baseContentChildCount = content.getChildCount();
        }

        public boolean isOpened() {
            return content.getVisibility() == View.VISIBLE;
        }

        public void setOpened(boolean newVal) {
            if (newVal) {
                content.setVisibility(View.VISIBLE);
                if (otherContent != null) {
                    otherContent.setVisibility(View.VISIBLE);
                }
                arrow.setImageDrawable(ThemeManager.getDrawable(R.attr.themedForumListDropUpArrow, SelectForumInListActivity.this));
            } else {
                content.setVisibility(View.GONE);
                if (otherContent != null) {
                    otherContent.setVisibility(View.GONE);
                }
                arrow.setImageDrawable(ThemeManager.getDrawable(R.attr.themedForumListDropDownArrow, SelectForumInListActivity.this));
            }
        }

        public int getNumberOfContentChildView() {
            int numberOfContentChildView = content.getChildCount();

            if (otherContent != null) {
                numberOfContentChildView += otherContent.getChildCount();
            }

            return numberOfContentChildView;
        }

        public View getContentChildViewAt(int index) {
            if (index < baseContentChildCount) {
                return content.getChildAt(index);
            } else {
                return otherContent.getChildAt(index - baseContentChildCount);
            }
        }
    }
}
