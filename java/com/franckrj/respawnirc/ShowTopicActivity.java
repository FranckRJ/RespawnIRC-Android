package com.franckrj.respawnirc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.franckrj.respawnirc.dialogs.ChoosePageNumberDialogFragment;
import com.franckrj.respawnirc.jvcmsggetters.AbsJVCMessageGetter;
import com.franckrj.respawnirc.jvcmsggetters.JVCForumMessageGetter;
import com.franckrj.respawnirc.jvcmsgviewers.AbsShowTopicFragment;
import com.franckrj.respawnirc.jvcmsgviewers.ShowTopicForumFragment;
import com.franckrj.respawnirc.jvcmsgviewers.ShowTopicIRCFragment;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.WebManager;

public class ShowTopicActivity extends AppCompatActivity implements AbsShowTopicFragment.NewModeNeededListener, AbsJVCMessageGetter.NewForumAndTopicNameAvailable,
                                                                    PopupMenu.OnMenuItemClickListener, JVCForumMessageGetter.NewNumbersOfPagesListener,
                                                                    ChoosePageNumberDialogFragment.NewPageNumberSelected {
    public static final String EXTRA_TOPIC_LINK = "com.franckrj.respawnirc.EXTRA_TOPIC_LINK";
    public static final String EXTRA_TOPIC_NAME = "com.franckrj.respawnirc.EXTRA_TOPIC_NAME";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";

    private SharedPreferences sharedPref = null;
    private JVCParser.ForumAndTopicName currentTitles = new JVCParser.ForumAndTopicName();
    private JVCMessageSender senderForMessages = null;
    private ImageButton messageSendButton = null;
    private EditText messageSendEdit = null;
    private QuoteJVCMessage currentTaskQuoteMessage = null;
    private String latestMessageQuotedInfo = null;
    private ViewPager pagerView = null;
    private ScreenSlidePagerAdapter adapterForPagerView = null;
    private String pseudoOfUser = "";
    private String cookieListInAString = "";
    private Button firstPageButton = null;
    private Button previousPageButton = null;
    private Button currentPageButton = null;
    private Button nextPageButton = null;
    private Button lastPageButton = null;
    private View layoutForAllNavigationButtons = null;
    private View shadowForAllNavigationButtons = null;
    private int lastPage = 0;
    private String currentTopicLink = "";
    private boolean showNavigationButtons = true;
    private String lastMessageSended = "";

    private final JVCMessageSender.NewMessageWantEditListener listenerForNewMessageWantEdit = new JVCMessageSender.NewMessageWantEditListener() {
        @Override
        public void initializeEditMode(String newMessageToEdit) {
            messageSendButton.setEnabled(true);

            if (newMessageToEdit.isEmpty()) {
                messageSendButton.setImageResource(R.drawable.ic_action_content_send);
                Toast.makeText(ShowTopicActivity.this, R.string.errorCantGetEditInfos, Toast.LENGTH_SHORT).show();
            } else {
                messageSendEdit.setText(newMessageToEdit);
            }
        }
    };

    private final JVCMessageSender.NewMessagePostedListener listenerForNewMessagePosted = new JVCMessageSender.NewMessagePostedListener() {
        @Override
        public void lastMessageIsSended(String withThisError) {
            messageSendButton.setEnabled(true);
            messageSendButton.setImageResource(R.drawable.ic_action_content_send);

            if (withThisError != null) {
                Toast.makeText(ShowTopicActivity.this, withThisError, Toast.LENGTH_LONG).show();
            } else {
                messageSendEdit.setText("");
            }
            getCurrentFragment().reloadTopic();
        }
    };

    private final Button.OnClickListener sendMessageToTopicListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (messageSendButton.isEnabled()) {
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                String tmpLastMessageSended = "";
                View focusedView;

                if (!pseudoOfUser.isEmpty()) {
                    if (!senderForMessages.getIsInEdit()) {
                        boolean messageIsSended = false;
                        if (getCurrentFragment().getLatestListOfInputInAString() != null) {
                            messageSendButton.setEnabled(false);
                            tmpLastMessageSended = messageSendEdit.getText().toString();
                            messageIsSended = senderForMessages.sendThisMessage(tmpLastMessageSended, getCurrentFragment().getCurrentUrlOfTopic(), getCurrentFragment().getLatestListOfInputInAString(), cookieListInAString);
                        }

                        if (!messageIsSended) {
                            Toast.makeText(ShowTopicActivity.this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        messageSendButton.setEnabled(false);
                        tmpLastMessageSended = messageSendEdit.getText().toString();
                        senderForMessages.sendEditMessage(tmpLastMessageSended, cookieListInAString);
                    }
                } else {
                    Toast.makeText(ShowTopicActivity.this, R.string.errorConnectedNeededBeforePost, Toast.LENGTH_LONG).show();
                }

                focusedView = getCurrentFocus();
                if (focusedView != null) {
                    inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }

                if (!tmpLastMessageSended.isEmpty()) {
                    SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
                    lastMessageSended = tmpLastMessageSended;
                    sharedPrefEdit.putString(getString(R.string.prefLastMessageSended), lastMessageSended);
                    sharedPrefEdit.apply();
                }
            } else {
                Toast.makeText(ShowTopicActivity.this, R.string.errorMessageAlreadySending, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final Button.OnClickListener changePageWithNavigationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (buttonView == firstPageButton && firstPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(0);
            } else if (buttonView == previousPageButton && previousPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(pagerView.getCurrentItem() - 1);
            } else if (buttonView == currentPageButton) {
                ChoosePageNumberDialogFragment choosePageDialogFragment = new ChoosePageNumberDialogFragment();
                choosePageDialogFragment.show(getFragmentManager(), "ChoosePageNumberDialogFragment");
            } else if (buttonView == nextPageButton && nextPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(pagerView.getCurrentItem() + 1);
            } else if (buttonView == lastPageButton && lastPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(lastPage - 1);
            }
        }
    };

    private AbsShowTopicFragment createNewFragmentForTopicRead(String possibleTopicLink) {
        int currentTopicMode = sharedPref.getInt(getString(R.string.prefCurrentTopicMode), AbsShowTopicFragment.MODE_FORUM);
        AbsShowTopicFragment newFragment;

        if (currentTopicMode == AbsShowTopicFragment.MODE_FORUM) {
            newFragment = new ShowTopicForumFragment();
        } else {
            newFragment = new ShowTopicIRCFragment();
        }

        if (possibleTopicLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(AbsShowTopicFragment.ARG_TOPIC_LINK, possibleTopicLink);
            argForFrag.putString(AbsShowTopicFragment.ARG_PSEUDO, pseudoOfUser);
            argForFrag.putString(AbsShowTopicFragment.ARG_COOKIES, cookieListInAString);
            newFragment.setArguments(argForFrag);
        }

        return newFragment;
    }

    private void updatePageButtons() {
        if (showNavigationButtons) {
            firstPageButton.setVisibility(View.GONE);
            previousPageButton.setVisibility(View.GONE);
            currentPageButton.setText(R.string.waitingText);
            nextPageButton.setVisibility(View.GONE);
            lastPageButton.setVisibility(View.GONE);

            if (pagerView.getCurrentItem() >= 0 && lastPage > 0) {
                currentPageButton.setText(String.valueOf(pagerView.getCurrentItem() + 1));

                if (pagerView.getCurrentItem() > 0) {
                    firstPageButton.setVisibility(View.VISIBLE);
                    firstPageButton.setText(String.valueOf(1));
                    previousPageButton.setVisibility(View.VISIBLE);
                }
                if (pagerView.getCurrentItem() < lastPage - 1) {
                    nextPageButton.setVisibility(View.VISIBLE);
                    lastPageButton.setVisibility(View.VISIBLE);
                    lastPageButton.setText(String.valueOf(lastPage));
                }
            }
        }
    }

    private void stopAllCurrentTask() {
        if (currentTaskQuoteMessage != null) {
            currentTaskQuoteMessage.cancel(true);
            currentTaskQuoteMessage = null;
            latestMessageQuotedInfo = null;
        }
    }

    private void reloadSettings() {
        AbsShowTopicFragment fragment = getCurrentFragment();
        pseudoOfUser = sharedPref.getString(getString(R.string.prefPseudoUser), "");
        cookieListInAString = sharedPref.getString(getString(R.string.prefCookiesList), "");
        lastMessageSended = sharedPref.getString(getString(R.string.prefLastMessageSended), "");

        if (fragment != null) {
            fragment.setPseudoAndCookies(pseudoOfUser, cookieListInAString);
        }
    }

    private void updateShowNavigationButtons() {
        int currentTopicMode = sharedPref.getInt(getString(R.string.prefCurrentTopicMode), AbsShowTopicFragment.MODE_FORUM);

        if (currentTopicMode == AbsShowTopicFragment.MODE_FORUM) {
            showNavigationButtons = ShowTopicForumFragment.getShowNavigationButtons();
        } else {
            showNavigationButtons = ShowTopicIRCFragment.getShowNavigationButtons();
        }

        if (showNavigationButtons) {
            layoutForAllNavigationButtons.setVisibility(View.VISIBLE);
            shadowForAllNavigationButtons.setVisibility(View.VISIBLE);
        } else {
            layoutForAllNavigationButtons.setVisibility(View.GONE);
            shadowForAllNavigationButtons.setVisibility(View.GONE);
            lastPage = 0;
            adapterForPagerView.notifyDataSetChanged();
        }
    }

    private void updateAdapterForPagerView() {
        adapterForPagerView = new ScreenSlidePagerAdapter(getFragmentManager());
        pagerView.setAdapter(adapterForPagerView);
    }

    private void updateLastPageAndCurrentItemAndButtonsToCurrentLink() {
        if (!currentTopicLink.isEmpty()) {
            lastPage = Integer.parseInt(JVCParser.getPageNumberForThisTopicLink(currentTopicLink));
            adapterForPagerView.notifyDataSetChanged();
            pagerView.setCurrentItem(Integer.parseInt(JVCParser.getPageNumberForThisTopicLink(currentTopicLink)) - 1);
            updatePageButtons();
        }
    }

    private AbsShowTopicFragment getCurrentFragment() {
        return adapterForPagerView.getFragment(pagerView.getCurrentItem());
    }

    private void loadPageForThisFragment(int position) {
        if (!currentTopicLink.isEmpty()) {
            AbsShowTopicFragment currentFragment = adapterForPagerView.getFragment(position);
            if (currentFragment != null) {
                currentFragment.setNewTopicLink(JVCParser.setPageNumberForThisTopicLink(currentTopicLink, position + 1));
            }
        }
    }

    private void clearPageForThisFragment(int position) {
        AbsShowTopicFragment currentFragment = adapterForPagerView.getFragment(position);
        if (currentFragment != null) {
            currentFragment.clearTopic();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtopic);

        Drawable arrowDrawable = Undeprecator.resourcesGetDrawable(getResources(), R.drawable.ic_action_navigation_arrow_drop_down);
        arrowDrawable.setBounds(0, 0, arrowDrawable.getIntrinsicWidth() / 2, arrowDrawable.getIntrinsicHeight() / 2);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_showtopic);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        messageSendEdit = (EditText) findViewById(R.id.sendmessage_text_showtopic);
        messageSendButton = (ImageButton) findViewById(R.id.sendmessage_button_showtopic);
        firstPageButton = (Button) findViewById(R.id.firstpage_button_showtopic);
        previousPageButton = (Button) findViewById(R.id.previouspage_button_showtopic);
        currentPageButton = (Button) findViewById(R.id.currentpage_button_showtopic);
        nextPageButton = (Button) findViewById(R.id.nextpage_button_showtopic);
        lastPageButton = (Button) findViewById(R.id.lastpage_button_showtopic);
        layoutForAllNavigationButtons = findViewById(R.id.header_layout_showtopic);
        shadowForAllNavigationButtons = findViewById(R.id.shadow_header_showtopic);
        pagerView = (ViewPager) findViewById(R.id.pager_showtopic);

        currentPageButton.setCompoundDrawables(null, null, arrowDrawable, null);
        currentPageButton.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.sizeBetweenTextAndArrow));

        updateAdapterForPagerView();
        pagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //rien
            }

            @Override
            public void onPageSelected(int position) {
                reloadSettings();
                loadPageForThisFragment(position);
                updatePageButtons();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (pagerView.getCurrentItem() > 0) {
                        clearPageForThisFragment(pagerView.getCurrentItem() - 1);
                    }
                    if (pagerView.getCurrentItem() < adapterForPagerView.getCount() - 1) {
                        clearPageForThisFragment(pagerView.getCurrentItem() + 1);
                    }
                }
            }
        });
        senderForMessages = new JVCMessageSender(this);
        senderForMessages.setListenerForNewMessageWantEdit(listenerForNewMessageWantEdit);
        senderForMessages.setListenerForNewMessagePosted(listenerForNewMessagePosted);
        messageSendButton.setOnClickListener(sendMessageToTopicListener);
        firstPageButton.setVisibility(View.GONE);
        firstPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        previousPageButton.setVisibility(View.GONE);
        previousPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        currentPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        nextPageButton.setVisibility(View.GONE);
        nextPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        lastPageButton.setVisibility(View.GONE);
        lastPageButton.setOnClickListener(changePageWithNavigationButtonListener);

        currentTopicLink = sharedPref.getString(getString(R.string.prefTopicUrlToFetch), "");
        updateShowNavigationButtons();
        if (savedInstanceState == null) {
            if (getIntent() != null) {
                currentTitles.topic = getIntent().getStringExtra(EXTRA_TOPIC_NAME);
                currentTitles.forum = getIntent().getStringExtra(EXTRA_FORUM_NAME);

                if (currentTitles.topic != null) {
                    if (currentTitles.topic.isEmpty()) {
                        currentTitles.topic = getString(R.string.app_name);
                    }
                } else {
                    currentTitles.topic = getString(R.string.app_name);
                }
                if (currentTitles.forum == null) {
                    currentTitles.forum = "";
                }

                if (getIntent().getStringExtra(EXTRA_TOPIC_LINK) != null) {
                    currentTopicLink = getIntent().getStringExtra(EXTRA_TOPIC_LINK);
                }
            } else {
                currentTitles.topic = getString(R.string.app_name);
                currentTitles.forum = "";
            }

            updateLastPageAndCurrentItemAndButtonsToCurrentLink();
        } else {
            currentTitles.forum = savedInstanceState.getString(getString(R.string.saveCurrentForumTitleForTopic), getString(R.string.app_name));
            currentTitles.topic = savedInstanceState.getString(getString(R.string.saveCurrentTopicTitleForTopic), "");
            lastPage = savedInstanceState.getInt(getString(R.string.saveLastPage), pagerView.getCurrentItem() + 1);
            adapterForPagerView.notifyDataSetChanged();

            senderForMessages.loadFromBundle(savedInstanceState);

            if (senderForMessages.getIsInEdit()) {
                messageSendButton.setImageResource(R.drawable.ic_action_content_edit);
            }

            updatePageButtons();
        }
        reloadSettings();

        if (myActionBar != null) {
            myActionBar.setTitle(currentTitles.topic);
            myActionBar.setSubtitle(currentTitles.forum);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        sharedPrefEdit.putInt(getString(R.string.prefLastActivityViewed), MainActivity.ACTIVITY_SHOW_TOPIC);
        sharedPrefEdit.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAllCurrentTask();
        senderForMessages.stopAllCurrentTask();
        if (!currentTopicLink.isEmpty()) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            sharedPrefEdit.putString(getString(R.string.prefTopicUrlToFetch), JVCParser.setPageNumberForThisTopicLink(currentTopicLink, pagerView.getCurrentItem() + 1));
            sharedPrefEdit.apply();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveCurrentForumTitleForTopic), currentTitles.forum);
        outState.putString(getString(R.string.saveCurrentTopicTitleForTopic), currentTitles.topic);
        outState.putInt(getString(R.string.saveLastPage), lastPage);
        senderForMessages.saveToBundle(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_showtopic, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_past_last_message_sended_showtopic).setEnabled(!lastMessageSended.isEmpty());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_past_last_message_sended_showtopic:
                messageSendEdit.setText(lastMessageSended);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void newModeRequested(int newMode) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();

        if (newMode == AbsShowTopicFragment.MODE_IRC || newMode == AbsShowTopicFragment.MODE_FORUM) {
            sharedPrefEdit.putInt(getString(R.string.prefCurrentTopicMode), newMode);

            if (newMode == AbsShowTopicFragment.MODE_IRC && !currentTopicLink.isEmpty()) {
                sharedPrefEdit.putString(getString(R.string.prefTopicUrlToFetch), JVCParser.setPageNumberForThisTopicLink(currentTopicLink, pagerView.getCurrentItem() + 1));
            }

            sharedPrefEdit.apply();
            updateShowNavigationButtons();
            updateAdapterForPagerView();

            if (newMode == AbsShowTopicFragment.MODE_FORUM) {
                currentTopicLink = sharedPref.getString(getString(R.string.prefTopicUrlToFetch), "");
                updateLastPageAndCurrentItemAndButtonsToCurrentLink();
                if (pagerView.getCurrentItem() > 0) {
                    clearPageForThisFragment(0);
                }
            }
        }
    }

    @Override
    public void getNewForumAndTopicName(JVCParser.ForumAndTopicName newNames) {
        ActionBar myActionBar = getSupportActionBar();

        if (!newNames.topic.isEmpty()) {
            currentTitles.topic = newNames.topic;
        } else {
            currentTitles.topic = getString(R.string.app_name);
        }

        if (!newNames.forum.isEmpty()) {
            currentTitles.forum = newNames.forum;
        } else {
            currentTitles.forum = "";
        }

        if (myActionBar != null) {
            myActionBar.setTitle(currentTitles.topic);
            myActionBar.setSubtitle(currentTitles.forum);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_quote_message:
                if (getCurrentFragment().getLatestAjaxInfos().list != null && latestMessageQuotedInfo == null && currentTaskQuoteMessage == null && !pseudoOfUser.isEmpty()) {
                    String idOfMessage = Long.toString(getCurrentFragment().getCurrentItemSelected().id);
                    latestMessageQuotedInfo = JVCParser.buildMessageQuotedInfoFromThis(getCurrentFragment().getCurrentItemSelected());

                    currentTaskQuoteMessage = new QuoteJVCMessage();
                    currentTaskQuoteMessage.execute(idOfMessage, getCurrentFragment().getLatestAjaxInfos().list, cookieListInAString);
                } else {
                    if (pseudoOfUser.isEmpty()) {
                        Toast.makeText(this, R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                    } else if (latestMessageQuotedInfo != null || currentTaskQuoteMessage != null) {
                        Toast.makeText(this, R.string.errorQuoteAlreadyRunning, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            case R.id.menu_edit_message:
                boolean infoForEditAreGetted = false;
                if (messageSendButton.isEnabled() && getCurrentFragment().getLatestAjaxInfos().list != null) {
                    String idOfMessage = Long.toString(getCurrentFragment().getCurrentItemSelected().id);
                    messageSendButton.setEnabled(false);
                    messageSendButton.setImageResource(R.drawable.ic_action_content_edit);
                    infoForEditAreGetted = senderForMessages.getInfosForEditMessage(idOfMessage, getCurrentFragment().getLatestAjaxInfos().list, cookieListInAString);
                }

                if (!infoForEditAreGetted) {
                    if (!messageSendButton.isEnabled()) {
                        Toast.makeText(this, R.string.errorMessageAlreadySending, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            default:
                return getCurrentFragment().onMenuItemClick(item);
        }
    }

    @Override
    public void getNewLastPageNumber(String newNumber) {
        if (!newNumber.isEmpty()) {
            lastPage = Integer.parseInt(newNumber);
        } else {
            lastPage = pagerView.getCurrentItem() + 1;
        }
        adapterForPagerView.notifyDataSetChanged();
        updatePageButtons();
    }

    @Override
    public void newPageNumberChoosen(int newPageNumber) {
        if (!currentTopicLink.isEmpty()) {
            if (newPageNumber > lastPage || newPageNumber < 0) {
                newPageNumber = lastPage;
            } else if (newPageNumber < 1) {
                newPageNumber = 1;
            }

            pagerView.setCurrentItem(newPageNumber - 1);
        }
    }

    protected class QuoteJVCMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                String pageContent;
                currentWebInfos.followRedirects = false;
                pageContent = WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_citation.php", "POST", "id_message=" + params[0] + "&" + params[1], params[2], currentWebInfos);

                if (pageContent != null) {
                    return JVCParser.getMessageQuoted(pageContent);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String messageQuoted) {
            super.onPostExecute(messageQuoted);

            if (messageQuoted != null) {
                String currentMessage = messageSendEdit.getText().toString();

                if (!currentMessage.isEmpty() && !currentMessage.endsWith("\n\n")) {
                    if (!currentMessage.endsWith("\n")) {
                        currentMessage += "\n";
                    }
                    currentMessage += "\n";
                }
                currentMessage += latestMessageQuotedInfo + "\n>" + messageQuoted + "\n\n";

                messageSendEdit.setText(currentMessage);
                messageSendEdit.setSelection(currentMessage.length());
            } else {
                Toast.makeText(ShowTopicActivity.this, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }

            latestMessageQuotedInfo = null;
            currentTaskQuoteMessage = null;
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<AbsShowTopicFragment> referenceMap = new SparseArray<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public AbsShowTopicFragment getFragment(int key) {
            return referenceMap.get(key);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((AbsShowTopicFragment) object).clearTopic();
            referenceMap.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == pagerView.getCurrentItem() && !currentTopicLink.isEmpty()) {
                return createNewFragmentForTopicRead(JVCParser.setPageNumberForThisTopicLink(currentTopicLink, position + 1));
            } else {
                return createNewFragmentForTopicRead(null);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            AbsShowTopicFragment fragment = (AbsShowTopicFragment) super.instantiateItem(container, position);
            referenceMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            if (lastPage > 0) {
                return lastPage;
            } else {
                return 1;
            }
        }
    }
}