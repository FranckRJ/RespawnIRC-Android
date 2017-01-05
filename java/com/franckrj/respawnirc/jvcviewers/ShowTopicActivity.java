package com.franckrj.respawnirc.jvcviewers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.franckrj.respawnirc.JVCMessageSender;
import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.dialogs.ChoosePageNumberDialogFragment;
import com.franckrj.respawnirc.dialogs.LinkContextMenuDialogFragment;
import com.franckrj.respawnirc.dialogs.SelectStickerDialogFragment;
import com.franckrj.respawnirc.jvcmsggetters.AbsJVCMessageGetter;
import com.franckrj.respawnirc.jvcmsggetters.JVCForumMessageGetter;
import com.franckrj.respawnirc.jvcmsgviewers.AbsShowTopicFragment;
import com.franckrj.respawnirc.jvcmsgviewers.JVCMessagesAdapter;
import com.franckrj.respawnirc.jvcmsgviewers.ShowTopicForumFragment;
import com.franckrj.respawnirc.jvcmsgviewers.ShowTopicIRCFragment;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

public class ShowTopicActivity extends AbsShowSomethingActivity implements AbsShowTopicFragment.NewModeNeededListener, AbsJVCMessageGetter.NewForumAndTopicNameAvailable,
                                                                    PopupMenu.OnMenuItemClickListener, JVCForumMessageGetter.NewNumbersOfPagesListener,
                                                                    ChoosePageNumberDialogFragment.NewPageNumberSelected, JVCMessagesAdapter.URLClicked,
                                                                    AbsJVCMessageGetter.NewReasonForTopicLock, SelectStickerDialogFragment.StickerSelected {
    public static final String EXTRA_TOPIC_LINK = "com.franckrj.respawnirc.EXTRA_TOPIC_LINK";
    public static final String EXTRA_TOPIC_NAME = "com.franckrj.respawnirc.EXTRA_TOPIC_NAME";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";

    private SharedPreferences sharedPref = null;
    private JVCParser.ForumAndTopicName currentTitles = new JVCParser.ForumAndTopicName();
    private JVCMessageSender senderForMessages = null;
    private ImageButton messageSendButton = null;
    private EditText messageSendEdit = null;
    private QuoteJVCMessage currentTaskQuoteMessage = null;
    private DeleteJVCMessage currentTaskDeleteMessage = null;
    private String latestMessageQuotedInfo = null;
    private String pseudoOfUser = "";
    private String cookieListInAString = "";
    private View layoutForAllNavigationButtons = null;
    private View shadowForAllNavigationButtons = null;
    private String lastMessageSended = "";
    private AddOrRemoveTopicToFavs currentTaskForTopicFavs = null;
    private String reasonOfLock = null;
    private ImageButton selectStickerButton = null;

    private final JVCMessageSender.NewMessageWantEditListener listenerForNewMessageWantEdit = new JVCMessageSender.NewMessageWantEditListener() {
        @Override
        public void initializeEditMode(String newMessageToEdit) {
            if (reasonOfLock == null) {
                messageSendButton.setEnabled(true);

                if (newMessageToEdit.isEmpty()) {
                    messageSendButton.setImageResource(R.drawable.ic_action_content_send);
                    Toast.makeText(ShowTopicActivity.this, R.string.errorCantGetEditInfos, Toast.LENGTH_SHORT).show();
                } else {
                    messageSendEdit.setText(newMessageToEdit);
                }
            }
        }
    };

    private final JVCMessageSender.NewMessagePostedListener listenerForNewMessagePosted = new JVCMessageSender.NewMessagePostedListener() {
        @Override
        public void lastMessageIsSended(String withThisError) {
            if (reasonOfLock == null) {
                messageSendButton.setEnabled(true);
                messageSendButton.setImageResource(R.drawable.ic_action_content_send);

                if (withThisError != null) {
                    Toast.makeText(ShowTopicActivity.this, withThisError, Toast.LENGTH_LONG).show();
                } else {
                    messageSendEdit.setText("");
                }
                getCurrentFragment().reloadTopic();
            }
        }
    };

    private final Button.OnClickListener sendMessageToTopicListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (messageSendButton.isEnabled() && reasonOfLock == null) {
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

    private final Button.OnClickListener selectStickerClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            SelectStickerDialogFragment selectStickerDialogFragment = new SelectStickerDialogFragment();
            selectStickerDialogFragment.show(getFragmentManager(), "SelectStickerDialogFragment");
        }
    };

    @Override
    protected void extendPageSelection(View buttonView) {
        if (buttonView == currentPageButton) {
            ChoosePageNumberDialogFragment choosePageDialogFragment = new ChoosePageNumberDialogFragment();
            choosePageDialogFragment.show(getFragmentManager(), "ChoosePageNumberDialogFragment");
        }
    }

    protected AbsShowSomethingFragment createNewFragmentForRead(String possibleTopicLink) {
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

    private void stopAllCurrentTask() {
        if (currentTaskQuoteMessage != null) {
            currentTaskQuoteMessage.cancel(true);
            currentTaskQuoteMessage = null;
            latestMessageQuotedInfo = null;
        }
        if (currentTaskDeleteMessage != null) {
            currentTaskDeleteMessage.cancel(true);
            currentTaskDeleteMessage = null;
        }
        if (currentTaskForTopicFavs != null) {
            currentTaskForTopicFavs.cancel(true);
            currentTaskForTopicFavs = null;
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

    private void updateLastPageAndCurrentItemAndButtonsToCurrentLink() {
        if (!currentLink.isEmpty()) {
            lastPage = getShowablePageNumberForThisLink(currentLink);
            adapterForPagerView.notifyDataSetChanged();
            updateCurrentItemAndButtonsToCurrentLink();
        }
    }

    private AbsShowTopicFragment getCurrentFragment() {
        return (AbsShowTopicFragment) adapterForPagerView.getFragment(pagerView.getCurrentItem());
    }

    @Override
    protected void loadPageForThisFragment(int position) {
        reloadSettings();
        super.loadPageForThisFragment(position);
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
        selectStickerButton = (ImageButton) findViewById(R.id.selectsticker_button_showtopic);
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
        pagerView.addOnPageChangeListener(pageChangeOnPagerListener);
        senderForMessages = new JVCMessageSender(this);
        senderForMessages.setListenerForNewMessageWantEdit(listenerForNewMessageWantEdit);
        senderForMessages.setListenerForNewMessagePosted(listenerForNewMessagePosted);
        messageSendButton.setOnClickListener(sendMessageToTopicListener);
        selectStickerButton.setOnClickListener(selectStickerClickedListener);
        initializeNavigationButtons();

        currentLink = sharedPref.getString(getString(R.string.prefTopicUrlToFetch), "");
        updateShowNavigationButtons();
        if (savedInstanceState == null) {
            if (getIntent() != null) {
                currentTitles.topic = getIntent().getStringExtra(EXTRA_TOPIC_NAME);
                currentTitles.forum = getIntent().getStringExtra(EXTRA_FORUM_NAME);

                if (currentTitles.topic == null) {
                    currentTitles.topic = "";
                }
                if (Utils.stringIsEmptyOrNull(currentTitles.forum)) {
                    currentTitles.forum = getString(R.string.app_name);
                }

                if (getIntent().getStringExtra(EXTRA_TOPIC_LINK) != null) {
                    currentLink = getIntent().getStringExtra(EXTRA_TOPIC_LINK);
                }
            } else {
                currentTitles.topic = "";
                currentTitles.forum = getString(R.string.app_name);
            }

            updateLastPageAndCurrentItemAndButtonsToCurrentLink();
        } else {
            currentTitles.forum = savedInstanceState.getString(getString(R.string.saveCurrentForumTitleForTopic), getString(R.string.app_name));
            currentTitles.topic = savedInstanceState.getString(getString(R.string.saveCurrentTopicTitleForTopic), "");
            lastPage = savedInstanceState.getInt(getString(R.string.saveLastPage), pagerView.getCurrentItem() + 1);
            getNewLockReason(savedInstanceState.getString(getString(R.string.saveReasonOfLock), null));
            adapterForPagerView.notifyDataSetChanged();

            senderForMessages.loadFromBundle(savedInstanceState);

            if (senderForMessages.getIsInEdit()) {
                messageSendButton.setImageResource(R.drawable.ic_action_content_edit);
            }

            updateNavigationButtons();
        }
        reloadSettings();

        if (myActionBar != null) {
            myActionBar.setTitle(currentTitles.forum);
            myActionBar.setSubtitle(currentTitles.topic);
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
        if (!currentLink.isEmpty()) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            sharedPrefEdit.putString(getString(R.string.prefTopicUrlToFetch), setShowedPageNumberForThisLink(currentLink, pagerView.getCurrentItem() + 1));
            sharedPrefEdit.apply();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveCurrentForumTitleForTopic), currentTitles.forum);
        outState.putString(getString(R.string.saveCurrentTopicTitleForTopic), currentTitles.topic);
        outState.putInt(getString(R.string.saveLastPage), lastPage);
        outState.putString(getString(R.string.saveReasonOfLock), reasonOfLock);
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

        if (!pseudoOfUser.isEmpty()) {
            if (getCurrentFragment().getIsInFavs() != null) {
                menu.findItem(R.id.action_change_topic_fav_value_showtopic).setEnabled(true);
                if (getCurrentFragment().getIsInFavs()) {
                    menu.findItem(R.id.action_change_topic_fav_value_showtopic).setTitle(R.string.removeOfFavs);
                } else {
                    menu.findItem(R.id.action_change_topic_fav_value_showtopic).setTitle(R.string.addToFavs);
                }
                return true;
            }
        }
        menu.findItem(R.id.action_change_topic_fav_value_showtopic).setEnabled(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_change_topic_fav_value_showtopic:
                if (currentTaskForTopicFavs == null) {
                    currentTaskForTopicFavs = new AddOrRemoveTopicToFavs(!getCurrentFragment().getIsInFavs());
                    currentTaskForTopicFavs.execute(JVCParser.getForumIDOfThisTopic(currentLink), getCurrentFragment().getTopicID(), getCurrentFragment().getLatestAjaxInfos().pref, cookieListInAString);
                } else {
                    Toast.makeText(ShowTopicActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.action_past_last_message_sended_showtopic:
                if (reasonOfLock == null) {
                    messageSendEdit.setText(lastMessageSended);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void newModeRequested(int newMode) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();

        if (newMode == AbsShowTopicFragment.MODE_IRC || newMode == AbsShowTopicFragment.MODE_FORUM) {
            sharedPrefEdit.putInt(getString(R.string.prefCurrentTopicMode), newMode);

            if (newMode == AbsShowTopicFragment.MODE_IRC && !currentLink.isEmpty()) {
                sharedPrefEdit.putString(getString(R.string.prefTopicUrlToFetch), setShowedPageNumberForThisLink(currentLink, pagerView.getCurrentItem() + 1));
            }

            sharedPrefEdit.apply();
            updateShowNavigationButtons();
            updateAdapterForPagerView();

            if (newMode == AbsShowTopicFragment.MODE_FORUM) {
                currentLink = sharedPref.getString(getString(R.string.prefTopicUrlToFetch), "");
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
            currentTitles.topic = "";
        }

        if (!newNames.forum.isEmpty()) {
            currentTitles.forum = newNames.forum;
        } else {
            currentTitles.forum = getString(R.string.app_name);
        }

        if (myActionBar != null) {
            myActionBar.setTitle(currentTitles.forum);
            myActionBar.setSubtitle(currentTitles.topic);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_quote_message:
                if (getCurrentFragment().getLatestAjaxInfos().list != null && latestMessageQuotedInfo == null && currentTaskQuoteMessage == null && !pseudoOfUser.isEmpty() && reasonOfLock == null) {
                    String idOfMessage = Long.toString(getCurrentFragment().getCurrentItemSelected().id);
                    latestMessageQuotedInfo = JVCParser.buildMessageQuotedInfoFromThis(getCurrentFragment().getCurrentItemSelected());

                    currentTaskQuoteMessage = new QuoteJVCMessage();
                    currentTaskQuoteMessage.execute(idOfMessage, getCurrentFragment().getLatestAjaxInfos().list, cookieListInAString);
                } else {
                    if (pseudoOfUser.isEmpty()) {
                        Toast.makeText(this, R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                    } else if (latestMessageQuotedInfo != null || currentTaskQuoteMessage != null) {
                        Toast.makeText(this, R.string.errorQuoteAlreadyRunning, Toast.LENGTH_SHORT).show();
                    } else if (reasonOfLock != null) {
                        Toast.makeText(this, R.string.errorTopicIsLocked, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                    }
                }

                return true;
            case R.id.menu_edit_message:
                if (reasonOfLock == null) {
                    if (senderForMessages.getIsInEdit()) {
                        senderForMessages.cancelEdit();
                        messageSendButton.setEnabled(true);
                        messageSendButton.setImageResource(R.drawable.ic_action_content_send);
                        messageSendEdit.setText("");
                    } else {
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
                    }
                } else {
                    Toast.makeText(this, R.string.errorTopicIsLocked, Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.menu_delete_message:
                if (getCurrentFragment().getLatestAjaxInfos().mod != null && currentTaskDeleteMessage == null && !pseudoOfUser.isEmpty()) {
                    String idOfMessage = Long.toString(getCurrentFragment().getCurrentItemSelected().id);

                    currentTaskDeleteMessage = new DeleteJVCMessage();
                    currentTaskDeleteMessage.execute(idOfMessage, getCurrentFragment().getLatestAjaxInfos().mod, cookieListInAString);
                } else {
                    if (pseudoOfUser.isEmpty()) {
                        Toast.makeText(this, R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                    } else if (currentTaskDeleteMessage != null) {
                        Toast.makeText(this, R.string.errorDeleteAlreadyRunning, Toast.LENGTH_SHORT).show();
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
        updateNavigationButtons();
    }

    @Override
    public void newPageNumberChoosen(int newPageNumber) {
        if (!currentLink.isEmpty()) {
            if (newPageNumber > lastPage || newPageNumber < 0) {
                newPageNumber = lastPage;
            } else if (newPageNumber < 1) {
                newPageNumber = 1;
            }

            pagerView.setCurrentItem(newPageNumber - 1);
        }
    }

    @Override
    protected int getShowablePageNumberForThisLink(String link) {
        return Integer.parseInt(JVCParser.getPageNumberForThisTopicLink(link));
    }

    @Override
    protected String setShowedPageNumberForThisLink(String link, int newPageNumber) {
        return JVCParser.setPageNumberForThisTopicLink(link, newPageNumber);
    }

    @Override
    public void getClickedURL(String link, boolean itsLongClick) {
        if (!itsLongClick) {
            String possibleNewLink = JVCParser.formatThisUrl(link);

            if (JVCParser.checkIfTopicAreSame(currentLink, possibleNewLink)) {
                pagerView.setCurrentItem(getShowablePageNumberForThisLink(possibleNewLink) - 1);
            } else if (JVCParser.checkIfItsJVCLink(possibleNewLink)) {
                Intent newShowForumIntent = new Intent(this, ShowForumActivity.class);
                newShowForumIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, possibleNewLink);
                startActivity(newShowForumIntent);
                finish();
            } else {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            Bundle argForFrag = new Bundle();
            LinkContextMenuDialogFragment linkMenuDialogFragment = new LinkContextMenuDialogFragment();
            argForFrag.putString(LinkContextMenuDialogFragment.ARG_URL, link);
            linkMenuDialogFragment.setArguments(argForFrag);
            linkMenuDialogFragment.show(getFragmentManager(), "LinkContextMenuDialogFragment");
        }
    }

    @Override
    public void getNewLockReason(String newReason) {
        reasonOfLock = newReason;
        if (reasonOfLock == null) {
            selectStickerButton.setVisibility(View.VISIBLE);
            messageSendButton.setEnabled(true);
            messageSendEdit.setEnabled(true);
            messageSendEdit.setText("");
        } else {
            selectStickerButton.setVisibility(View.GONE);
            messageSendButton.setEnabled(false);
            messageSendEdit.setEnabled(false);
            messageSendEdit.setText(getString(R.string.topicLockedForReason, Utils.truncateString(reasonOfLock, 60, getString(R.string.waitingText))));
        }
    }

    @Override
    public void getSelectedSticker(String newStickerToAdd) {
        if (reasonOfLock == null) {
            messageSendEdit.append(newStickerToAdd);
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

            if (messageQuoted != null && reasonOfLock == null) {
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

    protected class DeleteJVCMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = false;
                return WebManager.sendRequest("http://www.jeuxvideo.com/forums/modal_del_message.php", "GET", "tab_message[]=" + params[0] + "&type=delete&" + params[1], params[2], currentWebInfos);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String pageContent) {
            super.onPostExecute(pageContent);

            if (pageContent != null) {
                String currentError = JVCParser.getErrorMessageInJSONMode(pageContent);

                if (currentError == null) {
                    Toast.makeText(ShowTopicActivity.this, R.string.supressSuccess, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowTopicActivity.this, currentError, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ShowTopicActivity.this, R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }

            currentTaskDeleteMessage = null;
        }
    }

    private class AddOrRemoveTopicToFavs extends AsyncTask<String, Void, String> {
        final boolean addToFavs;

        AddOrRemoveTopicToFavs(boolean itsAnAdd) {
            addToFavs = itsAnAdd;
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                String actionToDo;
                String pageContent;
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                currentWebInfos.followRedirects = true;

                if (addToFavs) {
                    actionToDo = "add";
                } else {
                    actionToDo = "delete";
                }

                pageContent = WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_forum_prefere.php", "GET", "id_forum=" + params[0] + "&id_topic=" + params[1] + "&action=" + actionToDo + "&type=topic&" + params[2], params[3], currentWebInfos);

                if (pageContent != null) {
                    if (!pageContent.isEmpty()) {
                        return JVCParser.getErrorMessageInJSONMode(pageContent);
                    }
                }
                return null;
            } else {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String errorResult) {
            super.onPostExecute(errorResult);

            currentTaskForTopicFavs = null;

            if (errorResult != null) {
                if (!errorResult.isEmpty()) {
                    Toast.makeText(ShowTopicActivity.this, errorResult, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShowTopicActivity.this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                }
                return;
            }

            if (addToFavs) {
                Toast.makeText(ShowTopicActivity.this, R.string.favAdded, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ShowTopicActivity.this, R.string.favRemoved, Toast.LENGTH_SHORT).show();
            }

            getCurrentFragment().setIsInFavs(addToFavs);
        }
    }
}
