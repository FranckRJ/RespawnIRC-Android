package com.franckrj.respawnirc.jvcforum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.base.AbsShowSomethingFragment;
import com.franckrj.respawnirc.PageNavigationUtil;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvcforum.jvcforumtools.ShowForumFragment;
import com.franckrj.respawnirc.jvctopic.ShowTopicActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;

public class SearchTopicInForumActivity extends AbsHomeIsBackActivity implements ShowForumFragment.NewTopicWantRead, PageNavigationUtil.PageNavigationFunctions {
    public static final String EXTRA_FORUM_LINK = "com.franckrj.respawnirc.EXTRA_FORUM_LINK";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";

    private static final String SAVE_SEARCH_FORUM_CONTENT = "saveSearchForumContent";
    private static final String SAVE_CURRENT_SEARCH_LINK = "saveCurrentSearchLink";
    private static final String SAVE_SEARCH_TEXT_IS_OPENED = "saveSearchTextIsOpened";

    private EditText textForSearch = null;
    private MenuItem searchExpandableItem = null;
    private RadioButton topicModeSearchRadioButton = null;
    private String lastSearchedText = null;
    private PageNavigationUtil pageNavigation = null;
    private ShareActionProvider shareAction = null;
    private String currentSearchLink = "";
    private String currentForumName = "";
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

    private void updateShareAction() {
        if (shareAction != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, pageNavigation.getCurrentPageLink());
            shareIntent.setType("text/plain");
            shareAction.setShareIntent(shareIntent);
        }
    }

    public SearchTopicInForumActivity() {
        pageNavigation = new PageNavigationUtil(this);
        pageNavigation.setLastPageNumber(100);
    }

    public void performSearch() {
        if (textForSearch != null) {
            if (!textForSearch.getText().toString().isEmpty()) {
                pageNavigation.setCurrentLink(currentSearchLink + "?search_in_forum=" + Utils.encodeStringToUrlString(textForSearch.getText().toString()) +
                        "&type_search_in_forum=" + (topicModeSearchRadioButton.isChecked() ? "titre_topic" : "auteur_topic"));
                pageNavigation.updateAdapterForPagerView();
                pageNavigation.updateCurrentItemAndButtonsToCurrentLink();
            }
        }

        Utils.hideSoftKeyboard(SearchTopicInForumActivity.this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchtopic);
        initToolbar(R.id.toolbar_searchtopic);

        pageNavigation.initializePagerView((ViewPager) findViewById(R.id.pager_searchtopic));
        pageNavigation.initializeNavigationButtons((Button) findViewById(R.id.firstpage_button_searchtopic), (Button) findViewById(R.id.previouspage_button_searchtopic),
                        (Button) findViewById(R.id.currentpage_button_searchtopic), (Button) findViewById(R.id.nextpage_button_searchtopic), null);
        pageNavigation.updateAdapterForPagerView();

        topicModeSearchRadioButton = findViewById(R.id.topicmode_radio_searchtopic);

        if (getIntent() != null) {
            String newLinkForSearch = getIntent().getStringExtra(EXTRA_FORUM_LINK);

            if (newLinkForSearch != null) {
                if (newLinkForSearch.lastIndexOf("/") != -1) {
                    currentSearchLink = "http://www.jeuxvideo.com/recherche/forums/" + newLinkForSearch.substring(newLinkForSearch.lastIndexOf("/") + 1);
                }
            }

            if (getIntent().getStringExtra(EXTRA_FORUM_NAME) != null) {
                currentForumName = getIntent().getStringExtra(EXTRA_FORUM_NAME);
            }
        }

        if (savedInstanceState != null) {
            lastSearchedText = savedInstanceState.getString(SAVE_SEARCH_FORUM_CONTENT, null);
            searchTextIsOpened = savedInstanceState.getBoolean(SAVE_SEARCH_TEXT_IS_OPENED, false);
            pageNavigation.setCurrentLink(savedInstanceState.getString(SAVE_CURRENT_SEARCH_LINK, ""));
        }

        pageNavigation.updateNavigationButtons();

        if (currentSearchLink.isEmpty()) {
            Toast.makeText(this, R.string.errorSearchImpossible, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SAVE_SEARCH_TEXT_IS_OPENED, searchTextIsOpened);
        outState.putString(SAVE_CURRENT_SEARCH_LINK, pageNavigation.getCurrentPageLink());
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
        getMenuInflater().inflate(R.menu.menu_searchtopic, menu);
        searchExpandableItem = menu.findItem(R.id.action_search_searchtopic);
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share_searchforum));

        View rootView = searchExpandableItem.getActionView();
        ImageButton buttonForSearch = rootView.findViewById(R.id.search_button_searchlayout);
        textForSearch = rootView.findViewById(R.id.search_text_searchlayout);
        textForSearch.setHint(R.string.topicSearch);
        textForSearch.setOnEditorActionListener(actionInSearchEditTextListener);
        buttonForSearch.setOnClickListener(searchButtonClickedListener);

        searchExpandableItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchTextIsOpened = false;
                onBackPressed();
                return false;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (!searchTextIsOpened) {
                    searchTextIsOpened = true;
                }
                return true;
            }
        });

        if (lastSearchedText != null) {
            textForSearch.setText(lastSearchedText);
        }
        searchExpandableItem.expandActionView();
        textForSearch.requestFocus();

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_share_searchforum).setEnabled(!pageNavigation.getCurrentLinkIsEmpty());
        updateShareAction();

        return true;
    }

    @Override
    public void setReadNewTopic(String newTopicLink, String newTopicName, String pseudoOfAuthor, boolean fromLongClick) {
        Intent newShowTopicIntent = new Intent(this, ShowTopicActivity.class);

        if (newTopicName != null) {
            newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_NAME, newTopicName);
        }
        if (pseudoOfAuthor != null) {
            newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_PSEUDO_OF_AUTHOR, pseudoOfAuthor);
        }
        if (!currentForumName.isEmpty() && !currentForumName.equals(getString(R.string.app_name))) {
            newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_FORUM_NAME, currentForumName);
        }
        newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_GO_TO_LAST_PAGE, fromLongClick);

        newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, newTopicLink);
        startActivity(newShowTopicIntent);
    }

    @Override
    public void extendPageSelection(View buttonView) {
        //rien
    }

    @Override
    public AbsShowSomethingFragment createNewFragmentForRead(String possibleSearchTopicLink) {
        Bundle argForFrag = new Bundle();
        ShowForumFragment currentFragment = new ShowForumFragment();

        argForFrag.putBoolean(ShowForumFragment.ARG_IS_IN_SEARCH_MODE, true);

        if (possibleSearchTopicLink != null) {
            argForFrag.putString(ShowForumFragment.ARG_FORUM_LINK, possibleSearchTopicLink);
        }

        currentFragment.setArguments(argForFrag);

        return currentFragment;
    }

    @Override
    public void onNewPageSelected(int position) {
        //rien
    }

    @Override
    public void doThingsBeforeLoadOnFragment(AbsShowSomethingFragment thisFragment) {
        //rien
    }

    @Override
    public int getShowablePageNumberForThisLink(String link) {
        try {
            return ((Integer.parseInt(JVCParser.getPageNumberForThisSearchTopicLink(link)) - 1) / 25) + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public String setShowedPageNumberForThisLink(String link, int newPageNumber) {
        return JVCParser.setPageNumberForThisSearchTopicLink(link, ((newPageNumber - 1) * 25) + 1);
    }
}
