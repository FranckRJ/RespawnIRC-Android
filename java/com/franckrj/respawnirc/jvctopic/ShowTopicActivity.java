package com.franckrj.respawnirc.jvctopic;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.ThemedActivity;
import com.franckrj.respawnirc.dialogs.ChoosePageNumberDialogFragment;
import com.franckrj.respawnirc.dialogs.LinkContextMenuDialogFragment;
import com.franckrj.respawnirc.dialogs.MessageContextMenuDialogFragment;
import com.franckrj.respawnirc.dialogs.InsertStuffDialogFragment;
import com.franckrj.respawnirc.dialogs.SelectTextDialogFragment;
import com.franckrj.respawnirc.dialogs.ShowImageDialogFragment;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.AbsJVCTopicGetter;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.JVCTopicModeForumGetter;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.AbsShowTopicFragment;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.JVCTopicAdapter;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.ShowTopicModeForumFragment;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.ShowTopicModeIRCFragment;
import com.franckrj.respawnirc.AbsShowSomethingFragment;
import com.franckrj.respawnirc.PageNavigationUtil;
import com.franckrj.respawnirc.jvcforum.ShowForumActivity;
import com.franckrj.respawnirc.utils.AddOrRemoveThingToFavs;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class ShowTopicActivity extends ThemedActivity implements AbsShowTopicFragment.NewModeNeededListener, AbsJVCTopicGetter.NewForumAndTopicNameAvailable,
                                                                    PopupMenu.OnMenuItemClickListener, JVCTopicModeForumGetter.NewNumbersOfPagesListener,
                                                                    ChoosePageNumberDialogFragment.NewPageNumberSelected, JVCTopicAdapter.URLClicked,
                                                                    AbsJVCTopicGetter.NewReasonForTopicLock, InsertStuffDialogFragment.StuffInserted,
                                                                    PageNavigationUtil.PageNavigationFunctions, AddOrRemoveThingToFavs.ActionToFavsEnded, AbsJVCTopicGetter.TopicLinkChanged,
                                                                    AbsShowTopicFragment.NewSurveyNeedToBeShown, JVCTopicAdapter.PseudoClicked, AbsJVCTopicGetter.NewPseudoOfAuthorAvailable {
    public static final String EXTRA_TOPIC_LINK = "com.franckrj.respawnirc.EXTRA_TOPIC_LINK";
    public static final String EXTRA_TOPIC_NAME = "com.franckrj.respawnirc.EXTRA_TOPIC_NAME";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";
    public static final String EXTRA_PSEUDO_OF_AUTHOR = "com.franckrj.respawnirc.EXTRA_PSEUDO_OF_AUTHOR";
    public static final String EXTRA_GO_TO_LAST_PAGE = "com.franckrj.respawnirc.EXTRA_GO_TO_LAST_PAGE";

    private static final String SAVE_CURRENT_FORUM_TITLE_FOR_TOPIC = "saveCurrentForumTitleForTopic";
    private static final String SAVE_CURRENT_TOPIC_TITLE_FOR_TOPIC = "saveCurrentTopicTitleForTopic";
    private static final String SAVE_LAST_PAGE = "saveLastPage";
    private static final String SAVE_REASON_OF_LOCK = "saveReasonOfLock";
    private static final String SAVE_GO_TO_LAST_PAGE_AFTER_LOADING = "saveGoToLastPageAfterLoading";

    private JVCParser.ForumAndTopicName currentTitles = new JVCParser.ForumAndTopicName();
    private JVCMessageToTopicSender senderForMessages = null;
    private JVCMessageInTopicAction actionsForMessages = null;
    private ImageButton messageSendButton = null;
    private EditText messageSendEdit = null;
    private View messageSendLayout = null;
    private String pseudoOfUser = "";
    private String pseudoOfAuthor = "";
    private String cookieListInAString = "";
    private String lastMessageSended = "";
    private AddOrRemoveThingToFavs currentTaskForFavs = null;
    private String reasonOfLock = null;
    private ImageButton insertStuffButton = null;
    private PageNavigationUtil pageNavigation = null;
    private boolean useInternalNavigatorForDefaultOpening = false;
    private boolean convertNoelshackLinkToDirectLink = false;
    private boolean showOverviewOnImageClick = false;
    private boolean goToLastPageAfterLoading = false;
    private boolean goToBottomOnLoadIsEnabled = true;
    private boolean postAsModoWhenPossible = true;

    private final JVCMessageToTopicSender.NewMessageWantEditListener listenerForNewMessageWantEdit = new JVCMessageToTopicSender.NewMessageWantEditListener() {
        @Override
        public void initializeEditMode(String newMessageToEdit, boolean messageIsAnError) {
            if (reasonOfLock == null) {
                messageSendButton.setEnabled(true);

                if (newMessageToEdit.isEmpty() || messageIsAnError) {
                    if (newMessageToEdit.isEmpty()) {
                        newMessageToEdit = getString(R.string.errorCantGetEditInfos);
                    }
                    messageSendButton.setImageResource(ThemeManager.getDrawableRes(ThemeManager.DrawableName.CONTENT_SEND));
                    Toast.makeText(ShowTopicActivity.this, newMessageToEdit, Toast.LENGTH_SHORT).show();
                } else {
                    messageSendEdit.setText(newMessageToEdit);
                    messageSendEdit.setSelection(newMessageToEdit.length());
                }
            }
        }
    };

    private final JVCMessageToTopicSender.NewMessagePostedListener listenerForNewMessagePosted = new JVCMessageToTopicSender.NewMessagePostedListener() {
        @Override
        public void lastMessageIsSended(String withThisError) {
            if (reasonOfLock == null) {
                messageSendButton.setEnabled(true);
                messageSendButton.setImageResource(ThemeManager.getDrawableRes(ThemeManager.DrawableName.CONTENT_SEND));

                if (withThisError != null) {
                    Toast.makeText(ShowTopicActivity.this, withThisError, Toast.LENGTH_LONG).show();
                } else {
                    messageSendEdit.setText("");
                }

                getCurrentFragment().reloadTopic();
            }
        }
    };

    private final View.OnClickListener sendMessageToTopicListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (messageSendButton.isEnabled() && reasonOfLock == null) {
                String tmpLastMessageSended = "";

                if (!pseudoOfUser.isEmpty() && !messageSendEdit.getText().toString().isEmpty()) {
                    if (!senderForMessages.getIsInEdit()) {
                        boolean messageIsSended = false;
                        if (getCurrentFragment().getLatestListOfInputInAString(false) != null) {
                            messageSendButton.setEnabled(false);
                            tmpLastMessageSended = messageSendEdit.getText().toString();
                            messageIsSended = senderForMessages.sendThisMessage(tmpLastMessageSended, pageNavigation.getCurrentPageLink(), getCurrentFragment().getLatestListOfInputInAString(postAsModoWhenPossible), cookieListInAString);
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
                    if (pseudoOfUser.isEmpty()) {
                        Toast.makeText(ShowTopicActivity.this, R.string.errorConnectedNeededBeforePost, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ShowTopicActivity.this, R.string.errorMessageContentNeededForSend, Toast.LENGTH_LONG).show();
                    }
                }

                Utils.hideSoftKeyboard(ShowTopicActivity.this);
                messageSendLayout.requestFocus();

                if (!tmpLastMessageSended.isEmpty()) {
                    lastMessageSended = tmpLastMessageSended;
                    PrefsManager.putString(PrefsManager.StringPref.Names.LAST_MESSAGE_SENDED, lastMessageSended);
                    PrefsManager.applyChanges();
                }
            } else {
                Toast.makeText(ShowTopicActivity.this, R.string.errorMessageAlreadySending, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final View.OnLongClickListener showForumAndTopicTitleListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Bundle argForFrag = new Bundle();
            SelectTextDialogFragment selectTextDialogFragment = new SelectTextDialogFragment();
            argForFrag.putString(SelectTextDialogFragment.ARG_TEXT_CONTENT, getString(R.string.showForumAndTopicNames, currentTitles.forum, currentTitles.topic));
            selectTextDialogFragment.setArguments(argForFrag);
            selectTextDialogFragment.show(getFragmentManager(), "SelectTextDialogFragment");
            return true;
        }
    };

    private final View.OnLongClickListener refreshFromSendButton = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            getCurrentFragment().reloadTopic();
            return true;
        }
    };

    private final View.OnClickListener selectStickerClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            InsertStuffDialogFragment insertStuffDialogFragment = new InsertStuffDialogFragment();
            insertStuffDialogFragment.show(getFragmentManager(), "InsertStuffDialogFragment");
        }
    };

    private final JVCMessageInTopicAction.NewMessageIsQuoted messageIsQuotedListener = new JVCMessageInTopicAction.NewMessageIsQuoted() {
        @Override
        public void getNewMessageQuoted(String messageQuoted) {
            if (reasonOfLock == null) {
                String currentMessage = messageSendEdit.getText().toString();

                if (!currentMessage.isEmpty() && !currentMessage.endsWith("\n\n")) {
                    if (!currentMessage.endsWith("\n")) {
                        currentMessage += "\n";
                    }
                    currentMessage += "\n";
                }
                currentMessage += messageQuoted;

                messageSendEdit.setText(currentMessage);
                messageSendEdit.setSelection(currentMessage.length());
            }
        }
    };

    private final View.OnClickListener lockReasonCLickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (reasonOfLock != null) {
                Bundle argForFrag = new Bundle();
                SelectTextDialogFragment selectTextDialogFragment = new SelectTextDialogFragment();
                argForFrag.putString(SelectTextDialogFragment.ARG_TEXT_CONTENT, getString(R.string.topicLockedForReason, reasonOfLock));
                selectTextDialogFragment.setArguments(argForFrag);
                selectTextDialogFragment.show(getFragmentManager(), "SelectTextDialogFragment");
            }
        }
    };

    public ShowTopicActivity() {
        pageNavigation = new PageNavigationUtil(this);
    }

    private void stopAllCurrentTask() {
        if (currentTaskForFavs != null) {
            currentTaskForFavs.cancel(true);
            currentTaskForFavs = null;
        }
        actionsForMessages.stopAllCurrentTasks();
        senderForMessages.stopAllCurrentTask();
    }

    private void initializeSettings() {
        pseudoOfUser = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_USER);
        cookieListInAString = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);
        lastMessageSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_MESSAGE_SENDED);
        goToBottomOnLoadIsEnabled = PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_GO_TO_BOTTOM_ON_LOAD);
        useInternalNavigatorForDefaultOpening = PrefsManager.getBool(PrefsManager.BoolPref.Names.USE_INTERNAL_NAVIGATOR);
        convertNoelshackLinkToDirectLink = PrefsManager.getBool(PrefsManager.BoolPref.Names.USE_DIRECT_NOELSHACK_LINK);
        showOverviewOnImageClick = PrefsManager.getBool(PrefsManager.BoolPref.Names.SHOW_OVERVIEW_ON_IMAGE_CLICK);
        postAsModoWhenPossible = PrefsManager.getBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE);
    }

    private void updateShowNavigationButtons() {
        int currentTopicMode = PrefsManager.getInt(PrefsManager.IntPref.Names.CURRENT_TOPIC_MODE);

        if (currentTopicMode == AbsShowTopicFragment.MODE_FORUM) {
            pageNavigation.setShowNavigationButtons(ShowTopicModeForumFragment.getShowNavigationButtons());
        } else {
            pageNavigation.setShowNavigationButtons(ShowTopicModeIRCFragment.getShowNavigationButtons());
        }
    }

    private void updateLastPageAndCurrentItemAndButtonsToCurrentLink() {
        if (!pageNavigation.getCurrentLinkIsEmpty()) {
            pageNavigation.setLastPageNumber(pageNavigation.getLastSupposedPageNumber());
            pageNavigation.notifyDataSetChanged();
            pageNavigation.updateCurrentItemAndButtonsToCurrentLink();
        }
    }

    private AbsShowTopicFragment getCurrentFragment() {
        return (AbsShowTopicFragment) pageNavigation.getCurrentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtopic);

        Drawable arrowDrawable = Undeprecator.resourcesGetDrawable(getResources(), ThemeManager.getDrawableRes(ThemeManager.DrawableName.ARROW_DROP_DOWN));
        arrowDrawable.setBounds(0, 0, arrowDrawable.getIntrinsicWidth() / 2, arrowDrawable.getIntrinsicHeight() / 2);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_showtopic);
        myToolbar.setOnLongClickListener(showForumAndTopicTitleListener);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        messageSendLayout = findViewById(R.id.sendmessage_layout_showtopic);
        messageSendEdit = (EditText) findViewById(R.id.sendmessage_text_showtopic);
        messageSendButton = (ImageButton) findViewById(R.id.sendmessage_button_showtopic);
        insertStuffButton = (ImageButton) findViewById(R.id.insertstuff_button_showtopic);

        pageNavigation.initializeLayoutForAllNavigationButtons(findViewById(R.id.header_layout_showtopic), findViewById(R.id.shadow_header_showtopic));
        pageNavigation.initializePagerView((ViewPager) findViewById(R.id.pager_showtopic));
        pageNavigation.initializeNavigationButtons((Button) findViewById(R.id.firstpage_button_showtopic), (Button) findViewById(R.id.previouspage_button_showtopic),
                (Button) findViewById(R.id.currentpage_button_showtopic), (Button) findViewById(R.id.nextpage_button_showtopic), (Button) findViewById(R.id.lastpage_button_showtopic));

        pageNavigation.setDrawableForCurrentPageButton(arrowDrawable);

        pageNavigation.updateAdapterForPagerView();
        actionsForMessages = new JVCMessageInTopicAction(this);
        actionsForMessages.setNewMessageIsQuotedListener(messageIsQuotedListener);
        senderForMessages = new JVCMessageToTopicSender(this);
        senderForMessages.setListenerForNewMessageWantEdit(listenerForNewMessageWantEdit);
        senderForMessages.setListenerForNewMessagePosted(listenerForNewMessagePosted);
        messageSendButton.setOnClickListener(sendMessageToTopicListener);
        messageSendButton.setOnLongClickListener(refreshFromSendButton);
        insertStuffButton.setOnClickListener(selectStickerClickedListener);

        pageNavigation.setCurrentLink(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_URL_TO_FETCH));
        pseudoOfAuthor = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_AUTHOR_OF_TOPIC);
        updateShowNavigationButtons();
        initializeSettings();
        if (savedInstanceState == null) {
            if (getIntent() != null) {
                goToLastPageAfterLoading = getIntent().getBooleanExtra(EXTRA_GO_TO_LAST_PAGE, false);
                currentTitles.topic = getIntent().getStringExtra(EXTRA_TOPIC_NAME);
                currentTitles.forum = getIntent().getStringExtra(EXTRA_FORUM_NAME);

                if (getIntent().getStringExtra(EXTRA_PSEUDO_OF_AUTHOR) != null) {
                    pseudoOfAuthor = getIntent().getStringExtra(EXTRA_PSEUDO_OF_AUTHOR);
                }

                if (goToBottomOnLoadIsEnabled) {
                    pageNavigation.setGoToBottomOnNextLoad(goToLastPageAfterLoading);
                }
                if (currentTitles.topic == null) {
                    currentTitles.topic = "";
                }
                if (Utils.stringIsEmptyOrNull(currentTitles.forum)) {
                    currentTitles.forum = getString(R.string.app_name);
                }

                if (getIntent().getStringExtra(EXTRA_TOPIC_LINK) != null) {
                    pageNavigation.setCurrentLink(getIntent().getStringExtra(EXTRA_TOPIC_LINK));
                }
            } else {
                currentTitles.topic = "";
                currentTitles.forum = getString(R.string.app_name);
            }

            updateLastPageAndCurrentItemAndButtonsToCurrentLink();
        } else {
            currentTitles.forum = savedInstanceState.getString(SAVE_CURRENT_FORUM_TITLE_FOR_TOPIC, getString(R.string.app_name));
            currentTitles.topic = savedInstanceState.getString(SAVE_CURRENT_TOPIC_TITLE_FOR_TOPIC, "");
            pageNavigation.setLastPageNumber(savedInstanceState.getInt(SAVE_LAST_PAGE, pageNavigation.getCurrentItemIndex() + 1));
            getNewLockReason(savedInstanceState.getString(SAVE_REASON_OF_LOCK, null));
            goToLastPageAfterLoading = savedInstanceState.getBoolean(SAVE_GO_TO_LAST_PAGE_AFTER_LOADING, false);
            pageNavigation.notifyDataSetChanged();

            senderForMessages.loadFromBundle(savedInstanceState);

            if (senderForMessages.getIsInEdit()) {
                messageSendButton.setImageResource(ThemeManager.getDrawableRes(ThemeManager.DrawableName.CONTENT_EDIT));
            }

            pageNavigation.updateNavigationButtons();
        }

        if (myActionBar != null) {
            myActionBar.setTitle(currentTitles.forum);
            myActionBar.setSubtitle(currentTitles.topic);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PrefsManager.putInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED, MainActivity.ACTIVITY_SHOW_TOPIC);
        PrefsManager.applyChanges();
    }

    @Override
    public void onPause() {
        stopAllCurrentTask();
        if (!pageNavigation.getCurrentLinkIsEmpty()) {
            PrefsManager.putString(PrefsManager.StringPref.Names.TOPIC_URL_TO_FETCH, pageNavigation.getCurrentPageLink());
            PrefsManager.putString(PrefsManager.StringPref.Names.PSEUDO_OF_AUTHOR_OF_TOPIC, pseudoOfAuthor);
            PrefsManager.applyChanges();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_CURRENT_FORUM_TITLE_FOR_TOPIC, currentTitles.forum);
        outState.putString(SAVE_CURRENT_TOPIC_TITLE_FOR_TOPIC, currentTitles.topic);
        outState.putInt(SAVE_LAST_PAGE, pageNavigation.getLastPage());
        outState.putString(SAVE_REASON_OF_LOCK, reasonOfLock);
        outState.putBoolean(SAVE_GO_TO_LAST_PAGE_AFTER_LOADING, goToLastPageAfterLoading);
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

        if (!pseudoOfUser.isEmpty() && getCurrentFragment() != null) {
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
                if (currentTaskForFavs == null) {
                    currentTaskForFavs = new AddOrRemoveThingToFavs(!getCurrentFragment().getIsInFavs(), this);
                    currentTaskForFavs.execute(JVCParser.getForumIDOfThisTopic(pageNavigation.getCurrentPageLink()), getCurrentFragment().getTopicID(), getCurrentFragment().getLatestAjaxInfos().pref, cookieListInAString);
                } else {
                    Toast.makeText(ShowTopicActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_open_in_browser_showtopic:
                if (!useInternalNavigatorForDefaultOpening) {
                    Utils.openLinkInExternalNavigator(pageNavigation.getCurrentPageLink(), this);
                } else {
                    Utils.openLinkInInternalNavigator(pageNavigation.getCurrentPageLink(), this);
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
        if (newMode == AbsShowTopicFragment.MODE_IRC || newMode == AbsShowTopicFragment.MODE_FORUM) {
            PrefsManager.putInt(PrefsManager.IntPref.Names.CURRENT_TOPIC_MODE, newMode);
            PrefsManager.applyChanges();
            updateShowNavigationButtons();
            pageNavigation.updateAdapterForPagerView();

            if (newMode == AbsShowTopicFragment.MODE_FORUM) {
                updateLastPageAndCurrentItemAndButtonsToCurrentLink();
                if (pageNavigation.getCurrentItemIndex() > 0) {
                    pageNavigation.clearPageForThisFragment(0);
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
    public void updateTopicLink(String newTopicLink) {
        pageNavigation.setCurrentLink(newTopicLink);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_quote_message:
                if (pseudoOfUser.isEmpty()) {
                    Toast.makeText(this, R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                } else if (reasonOfLock != null) {
                    Toast.makeText(this, R.string.errorTopicIsLocked, Toast.LENGTH_SHORT).show();
                } else {
                    actionsForMessages.startQuoteThisMessage(getCurrentFragment().getLatestAjaxInfos(), getCurrentFragment().getCurrentItemSelected(), cookieListInAString);
                }
                return true;
            case R.id.menu_edit_message:
                if (reasonOfLock == null) {
                    if (senderForMessages.getIsInEdit()) {
                        senderForMessages.cancelEdit();
                        messageSendButton.setEnabled(true);
                        messageSendButton.setImageResource(ThemeManager.getDrawableRes(ThemeManager.DrawableName.CONTENT_SEND));
                        messageSendEdit.setText("");
                    } else {
                        boolean infoForEditAreGetted = false;
                        if (messageSendButton.isEnabled() && getCurrentFragment().getLatestAjaxInfos().list != null) {
                            String idOfMessage = Long.toString(getCurrentFragment().getCurrentItemSelected().id);
                            messageSendButton.setEnabled(false);
                            messageSendButton.setImageResource(ThemeManager.getDrawableRes(ThemeManager.DrawableName.CONTENT_EDIT));
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
                actionsForMessages.startDeleteThisMessage(getCurrentFragment().getLatestAjaxInfos(), getCurrentFragment().getCurrentItemSelected(), cookieListInAString);
                return true;
            case R.id.menu_kick_pseudo_message:
                JVCParser.MessageInfos currentMessage = getCurrentFragment().getCurrentItemSelected();
                Intent newKickPseudoIntent = new Intent(ShowTopicActivity.this, KickPseudoActivity.class);
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_PSEUDO, currentMessage.pseudo);
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_ID_ALIAS, currentMessage.idAlias);
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_ID_FORUM, JVCParser.getForumIDOfThisTopic(pageNavigation.getCurrentPageLink()));
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_ID_MESSAGE, String.valueOf(currentMessage.id));
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_AJAX_MOD, getCurrentFragment().getLatestAjaxInfos().mod);
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_COOKIES, cookieListInAString);
                startActivity(newKickPseudoIntent);
                return true;
            default:
                return getCurrentFragment().onMenuItemClick(item);
        }
    }

    @Override
    public boolean getNewLastPageNumber(String newNumber) {
        if (!newNumber.isEmpty()) {
            pageNavigation.setLastPageNumber(Integer.parseInt(newNumber));
        } else {
            pageNavigation.setLastPageNumber(pageNavigation.getCurrentItemIndex() + 1);
        }
        pageNavigation.notifyDataSetChanged();
        pageNavigation.updateNavigationButtons();

        if (goToLastPageAfterLoading) {
            goToLastPageAfterLoading = false;
            if (pageNavigation.getCurrentItemIndex() < pageNavigation.getLastPage() - 1) {
                if (goToBottomOnLoadIsEnabled) {
                    pageNavigation.setGoToBottomOnNextLoad(true);
                }
                pageNavigation.setCurrentItemIndex(pageNavigation.getLastPage() - 1);
                return true;
            }
        }

        return false;
    }

    @Override
    public void newPageNumberChoosen(int newPageNumber) {
        if (!pageNavigation.getCurrentLinkIsEmpty()) {
            if (newPageNumber > pageNavigation.getLastPage() || newPageNumber < 0) {
                newPageNumber = pageNavigation.getLastPage();
            } else if (newPageNumber < 1) {
                newPageNumber = 1;
            }

            pageNavigation.setCurrentItemIndex(newPageNumber - 1);
        }
    }

    @Override
    public void extendPageSelection(View buttonView) {
        if (pageNavigation.getIdOfThisButton(buttonView) == PageNavigationUtil.ID_BUTTON_CURRENT) {
            ChoosePageNumberDialogFragment choosePageDialogFragment = new ChoosePageNumberDialogFragment();
            choosePageDialogFragment.show(getFragmentManager(), "ChoosePageNumberDialogFragment");
        }
    }

    @Override
    public AbsShowSomethingFragment createNewFragmentForRead(String possibleTopicLink) {
        int currentTopicMode = PrefsManager.getInt(PrefsManager.IntPref.Names.CURRENT_TOPIC_MODE);
        AbsShowTopicFragment newFragment;

        if (currentTopicMode == AbsShowTopicFragment.MODE_FORUM) {
            newFragment = new ShowTopicModeForumFragment();
        } else {
            newFragment = new ShowTopicModeIRCFragment();
        }

        if (possibleTopicLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(AbsShowTopicFragment.ARG_TOPIC_LINK, possibleTopicLink);
            newFragment.setArguments(argForFrag);
        }

        return newFragment;
    }

    @Override
    public void doThingsBeforeLoadOnFragment(AbsShowSomethingFragment thisFragment) {
        ((AbsShowTopicFragment) thisFragment).setPseudoOfAuthor(pseudoOfAuthor);
    }

    @Override
    public int getShowablePageNumberForThisLink(String link) {
        try {
            return Integer.parseInt(JVCParser.getPageNumberForThisTopicLink(link));
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public String setShowedPageNumberForThisLink(String link, int newPageNumber) {
        return JVCParser.setPageNumberForThisTopicLink(link, newPageNumber);
    }

    @Override
    public void getClickedURL(String link, boolean itsLongClick) {
        if (convertNoelshackLinkToDirectLink) {
            if (JVCParser.checkIfItsNoelshackLink(link)) {
                link = JVCParser.noelshackToDirectLink(link);
            }
        }

        if (!itsLongClick) {
            String possibleNewLink = JVCParser.formatThisUrl(link);

            if (JVCParser.checkIfTopicAreSame(pageNavigation.getFirstPageLink(), possibleNewLink)) {
                pageNavigation.setCurrentItemIndex(getShowablePageNumberForThisLink(possibleNewLink) - 1);
            } else if (JVCParser.checkIfItsJVCLink(possibleNewLink)) {
                Intent newShowForumIntent = new Intent(this, ShowForumActivity.class);
                newShowForumIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, possibleNewLink);
                startActivity(newShowForumIntent);
                finish();
            } else if (showOverviewOnImageClick && JVCParser.checkIfItsNoelshackLink(link)) {
                Bundle argForFrag = new Bundle();
                ShowImageDialogFragment showImageDialogFragment = new ShowImageDialogFragment();
                argForFrag.putString(ShowImageDialogFragment.ARG_IMAGE_LINK, JVCParser.noelshackToDirectLink(link));
                showImageDialogFragment.setArguments(argForFrag);
                showImageDialogFragment.show(getFragmentManager(), "ShowImageDialogFragment");
            } else {
                if (!useInternalNavigatorForDefaultOpening) {
                    Utils.openLinkInExternalNavigator(link, this);
                } else {
                    Utils.openLinkInInternalNavigator(link, this);
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
        if (!Utils.stringsAreEquals(reasonOfLock, newReason)) {
            reasonOfLock = newReason;
            if (reasonOfLock == null) {
                insertStuffButton.setVisibility(View.VISIBLE);
                messageSendButton.setVisibility(View.VISIBLE);
                messageSendButton.setEnabled(true);
                messageSendEdit.setFocusable(true);
                messageSendEdit.setFocusableInTouchMode(true);
                messageSendEdit.setAlpha(1f);
                messageSendEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                messageSendEdit.setText("");
                messageSendEdit.setOnClickListener(null);
            } else {
                insertStuffButton.setVisibility(View.GONE);
                messageSendButton.setVisibility(View.GONE);
                messageSendButton.setEnabled(false);
                messageSendEdit.setFocusable(false);
                messageSendEdit.setFocusableInTouchMode(false);
                messageSendEdit.setAlpha(0.33f);
                messageSendEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                messageSendEdit.setText(getString(R.string.topicLockedForReason, Utils.truncateString(reasonOfLock, 80, getString(R.string.waitingText))));
                messageSendEdit.setOnClickListener(lockReasonCLickedListener);
            }
        }
    }

    @Override
    public void getStringInserted(String newStringToAdd, int posOfCenterFromEnd) {
        if (reasonOfLock == null) {
            Utils.insertStringInEditText(messageSendEdit, newStringToAdd, posOfCenterFromEnd);
        }
    }

    @Override
    public void getActionToFavsResult(String resultInString, boolean itsAnError) {
        if (itsAnError) {
            if (resultInString.isEmpty()) {
                resultInString = getString(R.string.errorInfosMissings);
            }
            Toast.makeText(this, resultInString, Toast.LENGTH_SHORT).show();
        } else {
            if (currentTaskForFavs.getAddToFavs()) {
                resultInString = getString(R.string.favAdded);
            } else {
                resultInString = getString(R.string.favRemoved);
            }
            Toast.makeText(this, resultInString, Toast.LENGTH_SHORT).show();
            getCurrentFragment().setIsInFavs(currentTaskForFavs.getAddToFavs());
        }
        currentTaskForFavs = null;
    }

    @Override
    public void getNewSurveyInfos(String surveyTitle, String topicID, String ajaxInfos, ArrayList<JVCParser.SurveyReplyInfos> listOfReplysWithInfos) {
        Intent newShowSurveyIntent = new Intent(ShowTopicActivity.this, ShowSurveyActivity.class);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_SURVEY_TITLE, surveyTitle);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_SURVEY_REPLYS_WITH_INFOS, listOfReplysWithInfos);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_TOPIC_ID, topicID);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_AJAX_INFOS, ajaxInfos);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_COOKIES, cookieListInAString);
        startActivity(newShowSurveyIntent);
    }

    @Override
    public void getMessageOfPseudoClicked(JVCParser.MessageInfos messageClicked) {
        Bundle argForFrag = new Bundle();
        MessageContextMenuDialogFragment messageMenuDialogFragment = new MessageContextMenuDialogFragment();
        argForFrag.putString(MessageContextMenuDialogFragment.ARG_PSEUDO_MESSAGE, messageClicked.pseudo);
        argForFrag.putString(MessageContextMenuDialogFragment.ARG_PSEUDO_USER, pseudoOfUser);
        argForFrag.putString(MessageContextMenuDialogFragment.ARG_MESSAGE_ID, String.valueOf(messageClicked.id));
        argForFrag.putBoolean(MessageContextMenuDialogFragment.ARG_USE_INTERNAL_BROWSER, useInternalNavigatorForDefaultOpening);
        argForFrag.putString(MessageContextMenuDialogFragment.ARG_MESSAGE_CONTENT, messageClicked.messageNotParsed);
        messageMenuDialogFragment.setArguments(argForFrag);
        messageMenuDialogFragment.show(getFragmentManager(), "MessageContextMenuDialogFragment");
    }

    @Override
    public void getNewPseudoOfAuthor(String newPseudoOfAuthor) {
        AbsShowTopicFragment currentFrag = getCurrentFragment();
        pseudoOfAuthor = newPseudoOfAuthor;
        if (currentFrag != null) {
            currentFrag.setPseudoOfAuthor(pseudoOfAuthor);
        }
    }
}
