package com.franckrj.respawnirc.jvcforum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.ThemedActivity;
import com.franckrj.respawnirc.jvcforum.jvcforumtools.ShowForumFragment;
import com.franckrj.respawnirc.jvctopic.ShowTopicActivity;
import com.franckrj.respawnirc.utils.Utils;

public class SearchTopicInForumActivity extends ThemedActivity implements ShowForumFragment.NewTopicWantRead {
    public static final String EXTRA_FORUM_LINK = "com.franckrj.respawnirc.EXTRA_FORUM_LINK";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";

    private static final String SAVE_SEARCH_FORUM_CONTENT = "saveSearchForumContent";

    private EditText textForSearch = null;
    private MenuItem searchExpandableItem = null;
    private RadioButton topicModeSearchRadioButton = null;
    private String lastSearchedText = null;
    private ShowForumFragment fragForShowForum = null;
    private String currentSearchLink = "";
    private String currentForumName = "";

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

    public void performSearch() {
        if (textForSearch != null) {
            if (!textForSearch.getText().toString().isEmpty()) {
                fragForShowForum.setPageLink(currentSearchLink + "?search_in_forum=" + Utils.convertStringToUrlString(textForSearch.getText().toString()) +
                        "&type_search_in_forum=" + (topicModeSearchRadioButton.isChecked() ? "titre_topic" : "auteur_topic"));
            }
        }

        Utils.hideSoftKeyboard(SearchTopicInForumActivity.this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchtopic);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_searchtopic);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        topicModeSearchRadioButton = (RadioButton) findViewById(R.id.topicmode_radio_searchtopic);

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
            fragForShowForum = (ShowForumFragment) getFragmentManager().findFragmentById(R.id.frame_searchtopic);
        } else {
            Bundle argForFrag = new Bundle();
            fragForShowForum = new ShowForumFragment();
            argForFrag.putBoolean(ShowForumFragment.ARG_IS_IN_SEARCH_MODE, true);
            fragForShowForum.setArguments(argForFrag);
            getFragmentManager().beginTransaction().replace(R.id.frame_searchtopic, fragForShowForum).commit();
        }

        if (currentSearchLink.isEmpty()) {
            Toast.makeText(this, R.string.errorSearchImpossible, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

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
        getMenuInflater().inflate(R.menu.menu_searchtopic, menu);
        searchExpandableItem = menu.findItem(R.id.action_search_searchtopic);

        View rootView = searchExpandableItem.getActionView();
        ImageButton buttonForSearch = (ImageButton) rootView.findViewById(R.id.search_button_searchlayout);
        textForSearch = (EditText) rootView.findViewById(R.id.search_text_searchlayout);
        textForSearch.setHint(R.string.topicSearch);
        textForSearch.setOnEditorActionListener(actionInSearchEditTextListener);
        buttonForSearch.setOnClickListener(searchButtonClickedListener);

        MenuItemCompat.setOnActionExpandListener(searchExpandableItem, new MenuItemCompat.OnActionExpandListener() {
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
        MenuItemCompat.expandActionView(searchExpandableItem);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setReadNewTopic(String newTopicLink, String newTopicName, String pseudoOfAuthor, boolean startAtBottom) {
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
        newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_GO_TO_BOTTOM, startAtBottom);

        newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, newTopicLink);
        startActivity(newShowTopicIntent);
    }
}
