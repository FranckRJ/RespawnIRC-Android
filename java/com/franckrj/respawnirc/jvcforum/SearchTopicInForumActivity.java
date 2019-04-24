package com.franckrj.respawnirc.jvcforum;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.ShareActionProvider;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.base.AbsShowSomethingFragment;
import com.franckrj.respawnirc.PageNavigationUtil;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvcforum.jvcforumtools.ShowForumFragment;
import com.franckrj.respawnirc.jvctopic.ShowTopicActivity;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

public class SearchTopicInForumActivity extends AbsHomeIsBackActivity implements ShowForumFragment.NewTopicWantRead, PageNavigationUtil.PageNavigationFunctions {
    public static final String EXTRA_FORUM_LINK = "com.franckrj.respawnirc.EXTRA_FORUM_LINK";
    public static final String EXTRA_SEARCH_LINK = "com.franckrj.respawnirc.EXTRA_SEARCH_LINK";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";

    private static final String SAVE_LAUNCH_SEARCH_ON_RESUME = "saveLaunchSearchOnResume";
    private static final String SAVE_BASE_SEARCH_LINK = "saveBaseSearchLink";
    private static final String SAVE_CURRENT_FORUM_NAME = "saveCurrentForumName";
    private static final String SAVE_SEARCH_FORUM_CONTENT = "saveSearchForumContent";
    private static final String SAVE_TYPE_OF_SEARCH = "saveTypeOfSearch";
    private static final String SAVE_CURRENT_SEARCH_LINK = "saveCurrentSearchLink";

    private EditText textForSearch = null;
    private MenuItem searchExpandableItem = null;
    private RadioGroup searchModeRadioGroup = null;
    private String lastSearchedText = null;
    private PageNavigationUtil pageNavigation;
    private ShareActionProvider shareAction = null;
    private String baseSearchLink = "";
    private String currentForumName = "";
    private int idOfTypeOfSearch = 0;
    private boolean launchSearchOnResumeAndResetPageNumber = false;

    private final View.OnClickListener searchButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            performSearch(true);
        }
    };

    private final TextView.OnEditorActionListener actionInSearchEditTextListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(true);
                return true;
            }
            return false;
        }
    };

    private final RadioGroup.OnCheckedChangeListener searchTypeChangedListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            performSearch(false);
        }
    };

    private static String getSearchTypeInTextForSearchTypeId(int searchTypeId) {
        switch (searchTypeId) {
            case R.id.topicmode_radio_searchtopic:
                return "titre_topic";
            case R.id.authormode_radio_searchtopic:
                return "auteur_topic";
            case R.id.messagemode_radio_searchtopic:
                return "texte_message";
            default:
                return "";
        }
    }

    private static int getSearchTypeIdForSearchTypeInText(String searchTypeText) {
        switch (searchTypeText) {
            case "titre_topic":
                return R.id.topicmode_radio_searchtopic;
            case "auteur_topic":
                return R.id.authormode_radio_searchtopic;
            case "texte_message":
                return R.id.messagemode_radio_searchtopic;
            default:
                return R.id.topicmode_radio_searchtopic;
        }
    }

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

    public void performSearch(boolean initiatedBySearchButton) {
        boolean textSearchedIsValid = false;
        String textToSearch = (textForSearch == null ? lastSearchedText : textForSearch.getText().toString());

        if (!Utils.stringIsEmptyOrNull(textToSearch)) {
            textSearchedIsValid = true;
            idOfTypeOfSearch = searchModeRadioGroup.getCheckedRadioButtonId();
            pageNavigation.setCurrentLink(baseSearchLink + "?search_in_forum=" + Utils.encodeStringToUrlString(textToSearch) +
                    "&type_search_in_forum=" + getSearchTypeInTextForSearchTypeId(idOfTypeOfSearch));
            pageNavigation.updateAdapterForPagerView();
            pageNavigation.updateCurrentItemAndButtonsToCurrentLink();
            lastSearchedText = textToSearch;
        }

        if (initiatedBySearchButton || textSearchedIsValid) {
            Utils.hideSoftKeyboard(SearchTopicInForumActivity.this);
        }
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

        searchModeRadioGroup = findViewById(R.id.radiogroup_layout_searchtopic);
        searchModeRadioGroup.setOnCheckedChangeListener(searchTypeChangedListener);

        if (getIntent() != null && savedInstanceState == null) {
            String newSearchLink = getIntent().getStringExtra(EXTRA_SEARCH_LINK);

            if (!Utils.stringIsEmptyOrNull(newSearchLink)) {
                String suffixForSearch = JVCParser.getSuffixForSearchTopicLink(newSearchLink);
                String textToSearch = JVCParser.getTextToSearchForSearchTopicLink(newSearchLink);
                String typeOfSearch = JVCParser.getTypeOfSearchForSearchTopicLink(newSearchLink);

                if (!suffixForSearch.isEmpty()) {
                    lastSearchedText = textToSearch;
                    idOfTypeOfSearch = getSearchTypeIdForSearchTypeInText(typeOfSearch);
                    baseSearchLink = "http://www.jeuxvideo.com/recherche/forums/" + suffixForSearch;
                    launchSearchOnResumeAndResetPageNumber = true;
                }
            } else {
                String newLinkForSearch = getIntent().getStringExtra(EXTRA_FORUM_LINK);

                if (newLinkForSearch != null) {
                    if (newLinkForSearch.lastIndexOf("/") != -1) {
                        baseSearchLink = "http://www.jeuxvideo.com/recherche/forums/" + newLinkForSearch.substring(newLinkForSearch.lastIndexOf("/") + 1);
                    }
                }
            }

            if (getIntent().getStringExtra(EXTRA_FORUM_NAME) != null) {
                currentForumName = getIntent().getStringExtra(EXTRA_FORUM_NAME);
            }
        }

        if (savedInstanceState != null) {
            launchSearchOnResumeAndResetPageNumber = savedInstanceState.getBoolean(SAVE_LAUNCH_SEARCH_ON_RESUME, false);
            baseSearchLink = savedInstanceState.getString(SAVE_BASE_SEARCH_LINK, "");
            currentForumName = savedInstanceState.getString(SAVE_CURRENT_FORUM_NAME, "");
            lastSearchedText = savedInstanceState.getString(SAVE_SEARCH_FORUM_CONTENT, null);
            idOfTypeOfSearch = savedInstanceState.getInt(SAVE_TYPE_OF_SEARCH, 0);
            pageNavigation.setCurrentLink(savedInstanceState.getString(SAVE_CURRENT_SEARCH_LINK, ""));
        }

        pageNavigation.updateNavigationButtons();

        if (baseSearchLink.isEmpty()) {
            Toast.makeText(this, R.string.errorSearchImpossible, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefsManager.putInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED, MainActivity.ACTIVITY_SHOW_FORUM);
        PrefsManager.applyChanges();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();

        if (launchSearchOnResumeAndResetPageNumber) {
            RadioButton currentRadioButton = findViewById(idOfTypeOfSearch);
            if (textForSearch != null && lastSearchedText != null) {
                textForSearch.setText(lastSearchedText);
            }
            if (currentRadioButton != null) {
                currentRadioButton.setChecked(true);
            }
            performSearch(true);
            baseSearchLink = setShowedPageNumberForThisLink(baseSearchLink, 1);
            launchSearchOnResumeAndResetPageNumber = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        boolean hasSavedSearch = false;

        outState.putBoolean(SAVE_LAUNCH_SEARCH_ON_RESUME, launchSearchOnResumeAndResetPageNumber);
        outState.putString(SAVE_BASE_SEARCH_LINK, baseSearchLink);
        outState.putString(SAVE_CURRENT_FORUM_NAME, currentForumName);
        outState.putString(SAVE_CURRENT_SEARCH_LINK, pageNavigation.getCurrentPageLink());
        outState.putInt(SAVE_TYPE_OF_SEARCH, idOfTypeOfSearch);
        outState.putString(SAVE_SEARCH_FORUM_CONTENT, null);
        if (textForSearch != null && searchExpandableItem != null) {
            if (searchExpandableItem.isActionViewExpanded()) {
                outState.putString(SAVE_SEARCH_FORUM_CONTENT, textForSearch.getText().toString());
                hasSavedSearch = true;
            }
        }
        if (!hasSavedSearch && lastSearchedText != null) {
            outState.putString(SAVE_SEARCH_FORUM_CONTENT, lastSearchedText);
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
                onBackPressed();
                return false;
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
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
        int numberOfResultPerPage = (idOfTypeOfSearch == R.id.messagemode_radio_searchtopic ? 20 : 25);

        try {
            return ((Integer.parseInt(JVCParser.getPageNumberForThisSearchTopicLink(link)) - 1) / numberOfResultPerPage) + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public String setShowedPageNumberForThisLink(String link, int newPageNumber) {
        int numberOfResultPerPage = (idOfTypeOfSearch == R.id.messagemode_radio_searchtopic ? 20 : 25);

        return JVCParser.setPageNumberForThisSearchTopicLink(link, ((newPageNumber - 1) * numberOfResultPerPage) + 1);
    }
}
