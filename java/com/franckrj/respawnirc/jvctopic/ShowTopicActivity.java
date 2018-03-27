package com.franckrj.respawnirc.jvctopic;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.franckrj.respawnirc.DraftUtils;
import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.dialogs.ChoosePageNumberDialogFragment;
import com.franckrj.respawnirc.dialogs.LinkMenuDialogFragment;
import com.franckrj.respawnirc.dialogs.MessageMenuDialogFragment;
import com.franckrj.respawnirc.dialogs.InsertStuffDialogFragment;
import com.franckrj.respawnirc.dialogs.SelectTextDialogFragment;
import com.franckrj.respawnirc.dialogs.ShowImageDialogFragment;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.AbsJVCTopicGetter;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.JVCTopicModeForumGetter;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.AbsShowTopicFragment;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.JVCTopicAdapter;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.ShowTopicModeForumFragment;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.ShowTopicModeIRCFragment;
import com.franckrj.respawnirc.base.AbsShowSomethingFragment;
import com.franckrj.respawnirc.PageNavigationUtil;
import com.franckrj.respawnirc.jvcforum.ShowForumActivity;
import com.franckrj.respawnirc.utils.AddOrRemoveThingToFavs;
import com.franckrj.respawnirc.utils.AddOrRemoveTopicToSubs;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class ShowTopicActivity extends AbsHomeIsBackActivity implements AbsShowTopicFragment.NewModeNeededListener, AbsJVCTopicGetter.NewTopicStatusListener, JVCActionsInTopic.TopicNeedToBeReloaded,
                                                                        JVCTopicAdapter.MenuItemClickedInMessage, JVCTopicModeForumGetter.NewNumbersOfPagesListener, JVCTopicAdapter.PseudoClicked,
                                                                        ChoosePageNumberDialogFragment.NewPageNumberSelected, JVCTopicAdapter.URLClicked, AbsShowTopicFragment.NewSurveyNeedToBeShown,
                                                                        InsertStuffDialogFragment.StuffInserted, MessageMenuDialogFragment.NewPseudoIgnored, PageNavigationUtil.PageNavigationFunctions,
                                                                        AddOrRemoveThingToFavs.ActionToFavsEnded, AddOrRemoveTopicToSubs.ActionToSubsEnded, AbsJVCTopicGetter.TopicLinkChanged {
    public static final String EXTRA_OPENED_FROM_FORUM = "com.franckrj.respawnirc.EXTRA_OPENED_FROM_FORUM";
    public static final String EXTRA_TOPIC_LINK = "com.franckrj.respawnirc.EXTRA_TOPIC_LINK";
    public static final String EXTRA_TOPIC_NAME = "com.franckrj.respawnirc.EXTRA_TOPIC_NAME";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";
    public static final String EXTRA_PSEUDO_OF_AUTHOR = "com.franckrj.respawnirc.EXTRA_PSEUDO_OF_AUTHOR";
    public static final String EXTRA_GO_TO_LAST_PAGE = "com.franckrj.respawnirc.EXTRA_GO_TO_LAST_PAGE";

    private static final int LOCK_TOPIC_REQUEST_CODE = 1245;
    private static final String SAVE_TOPIC_OPENED_FROM_FORUM = "saveTopicOpenedFromForum";
    private static final String SAVE_LAST_PAGE = "saveLastPage";
    private static final String SAVE_TOPIC_STATUS = "saveTopicStatus";
    private static final String SAVE_CURRENT_TOPIC_LINK = "saveCurrentTopicLink";
    private static final String SAVE_GO_TO_LAST_PAGE_AFTER_LOADING = "saveGoToLastPageAfterLoading";

    private AbsJVCTopicGetter.TopicStatusInfos topicStatus = new AbsJVCTopicGetter.TopicStatusInfos();
    private JVCMessageToTopicSender senderForMessages = null;
    private JVCActionsInTopic actionsForTopic = null;
    private ImageButton messageSendButton = null;
    private EditText messageSendEdit = null;
    private View messageSendLayout = null;
    private String pseudoOfUser = "";
    private String cookieListInAString = "";
    private String lastMessageSended = "";
    private AddOrRemoveThingToFavs currentTaskForFavs = null;
    private AddOrRemoveTopicToSubs currentTaskForSubs = null;
    private ImageButton insertStuffButton = null;
    private PageNavigationUtil pageNavigation = new PageNavigationUtil(this);
    private PrefsManager.LinkType linkTypeForInternalBrowser = new PrefsManager.LinkType(PrefsManager.LinkType.NO_LINKS);
    private boolean convertNoelshackLinkToDirectLink = false;
    private boolean showOverviewOnImageClick = false;
    private boolean goToLastPageAfterLoading = false;
    private boolean goToBottomOnLoadIsEnabled = true;
    private DraftUtils utilsForDraft = new DraftUtils(PrefsManager.SaveDraftType.ALWAYS, PrefsManager.BoolPref.Names.USE_LAST_MESSAGE_DRAFT_SAVED);
    private boolean topicHasBeenOpenedFromAForum = true;

    private final JVCMessageToTopicSender.NewMessageWantEditListener listenerForNewMessageWantEdit = new JVCMessageToTopicSender.NewMessageWantEditListener() {
        @Override
        public void editThisMessage(String messageID) {
            startEditThisMessage(messageID, false);
        }

        @Override
        public void initializeEditMode(String newMessageToEdit, boolean messageIsAnError, boolean useMessageToEdit) {
            if (topicStatus.lockReason == null) {
                messageSendButton.setEnabled(true);

                if (newMessageToEdit.isEmpty() || messageIsAnError) {
                    String errorToShow = newMessageToEdit;
                    if (errorToShow.isEmpty()) {
                        errorToShow = getString(R.string.errorCantGetEditInfos);
                    }
                    messageSendButton.setImageDrawable(ThemeManager.getDrawable(R.attr.themedContentSendIcon, ShowTopicActivity.this));
                    showErrorWhenSendingMessage(errorToShow);
                } else if (useMessageToEdit) {
                    messageSendEdit.setText(newMessageToEdit);
                    messageSendEdit.setSelection(newMessageToEdit.length());
                    messageSendEdit.requestFocus();
                }
            }
        }
    };

    private final JVCMessageToTopicSender.NewMessagePostedListener listenerForNewMessagePosted = new JVCMessageToTopicSender.NewMessagePostedListener() {
        @Override
        public void lastMessageIsSended(String withThisError) {
            if (topicStatus.lockReason == null) {
                messageSendButton.setEnabled(true);
                messageSendButton.setImageDrawable(ThemeManager.getDrawable(R.attr.themedContentSendIcon, ShowTopicActivity.this));

                if (withThisError != null) {
                    showErrorWhenSendingMessage(withThisError);
                } else {
                    messageSendEdit.setText("");
                }

                refreshTopicSafely();
            }
        }
    };

    private final View.OnClickListener sendMessageToTopicListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (messageSendButton.isEnabled() && topicStatus.lockReason == null) {
                String tmpLastMessageSended = "";

                if (!pseudoOfUser.isEmpty() && !messageSendEdit.getText().toString().isEmpty()) {
                    if (!senderForMessages.getIsInEdit()) {
                        boolean messageIsSended = false;
                        if (topicStatus.listOfInputInAString != null) {
                            String tmpListOfInputToUse = JVCMessageToTopicSender.addPostTypeToListOfInput(topicStatus.listOfInputInAString, topicStatus.userCanPostAsModo && PrefsManager.getBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE));
                            messageSendButton.setEnabled(false);
                            tmpLastMessageSended = messageSendEdit.getText().toString();
                            messageIsSended = senderForMessages.sendThisMessage(tmpLastMessageSended, pageNavigation.getCurrentPageLink(), tmpListOfInputToUse, cookieListInAString);
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
            argForFrag.putString(SelectTextDialogFragment.ARG_TEXT_CONTENT, getString(R.string.showForumAndTopicNames, topicStatus.names.forum, topicStatus.names.topic));
            selectTextDialogFragment.setArguments(argForFrag);
            selectTextDialogFragment.show(getSupportFragmentManager(), "SelectTextDialogFragment");
            return true;
        }
    };

    private final View.OnLongClickListener refreshFromSendButton = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            refreshTopicSafely();
            return true;
        }
    };

    private final View.OnClickListener selectStickerClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            InsertStuffDialogFragment insertStuffDialogFragment = new InsertStuffDialogFragment();
            insertStuffDialogFragment.show(getSupportFragmentManager(), "InsertStuffDialogFragment");
        }
    };

    private final View.OnLongClickListener showSendmessageActionListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View buttonView) {
            PopupMenu popup = new PopupMenu(ShowTopicActivity.this, buttonView);
            MenuItem postAsModoItem;

            popup.getMenuInflater().inflate(R.menu.menu_sendmessage_action, popup.getMenu());
            popup.setOnMenuItemClickListener(onSendmessageActionClickedListener);

            postAsModoItem = popup.getMenu().findItem(R.id.enable_postasmodo_sendmessage_action);
            postAsModoItem.setChecked(PrefsManager.getBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE));
            postAsModoItem.setEnabled(topicStatus.userCanPostAsModo);
            popup.getMenu().findItem(R.id.past_last_message_sended_sendmessage_action).setEnabled(!lastMessageSended.isEmpty());

            if (senderForMessages.getIsInEdit()) {
                popup.getMenu().add(Menu.NONE, R.id.cancel_edit_sendmessage_action, Menu.NONE, R.string.cancelEdit);
            }

            popup.show();

            return true;
        }
    };

    private final PopupMenu.OnMenuItemClickListener onSendmessageActionClickedListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.enable_postasmodo_sendmessage_action:
                    /* La valeur de isChecked est inversée car le changement d'état ne se fait pas automatiquement
                     * donc c'est la valeur avant d'avoir cliqué qui est retournée. */
                    PrefsManager.putBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE, !item.isChecked());
                    PrefsManager.applyChanges();
                    updatePostTypeNotice();
                    return true;
                case R.id.delete_message_sendmessage_action:
                    AlertDialog.Builder builder = new AlertDialog.Builder(ShowTopicActivity.this);
                    builder.setTitle(R.string.deleteMessage).setMessage(R.string.deleteCurrentWritedMessageWarning)
                            .setPositiveButton(R.string.yes, onClickInDeleteCurrentWritedMessageConfirmationListener).setNegativeButton(R.string.no, null);
                    builder.show();
                    return true;
                case R.id.past_last_message_sended_sendmessage_action:
                    if (topicStatus.lockReason == null) {
                        messageSendEdit.setText(lastMessageSended);
                    }
                    return true;
                case R.id.cancel_edit_sendmessage_action:
                    cancelEditAndHideKeyboardAndCursor();
                    return true;
                default:
                    return false;
            }
        }
    };

    private final DialogInterface.OnClickListener onClickInDeleteCurrentWritedMessageConfirmationListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                messageSendEdit.setText("");
                Utils.hideSoftKeyboard(ShowTopicActivity.this);
                messageSendLayout.requestFocus();
            }
        }
    };

    private final JVCActionsInTopic.NewMessageIsQuoted messageIsQuotedListener = new JVCActionsInTopic.NewMessageIsQuoted() {
        @Override
        public void getNewMessageQuoted(String messageQuoted) {
            if (topicStatus.lockReason == null) {
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
                messageSendEdit.requestFocus();
            }
        }
    };

    private final View.OnClickListener lockReasonCLickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (topicStatus.lockReason != null) {
                Bundle argForFrag = new Bundle();
                SelectTextDialogFragment selectTextDialogFragment = new SelectTextDialogFragment();
                argForFrag.putString(SelectTextDialogFragment.ARG_TEXT_CONTENT, getString(R.string.topicLockedForReason, topicStatus.lockReason));
                selectTextDialogFragment.setArguments(argForFrag);
                selectTextDialogFragment.show(getSupportFragmentManager(), "SelectTextDialogFragment");
            }
        }
    };

    private void showErrorWhenSendingMessage(String error) {
        if (error.toLowerCase().contains("captcha")) {
            Toast.makeText(ShowTopicActivity.this, R.string.errorThereIsACaptcha, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ShowTopicActivity.this, error, Toast.LENGTH_LONG).show();
        }
    }

    private void refreshTopicSafely() {
        AbsShowTopicFragment currentFrag = getCurrentFragment();
        if (currentFrag != null) {
            currentFrag.refreshContent();
        }
    }

    private void stopAllCurrentTask() {
        if (currentTaskForFavs != null) {
            currentTaskForFavs.clearListenersAndCancel();
            currentTaskForFavs = null;
        }
        if (currentTaskForSubs != null) {
            currentTaskForSubs.clearListenersAndCancel();
            currentTaskForSubs = null;
        }
        actionsForTopic.stopAllCurrentTasks();
        senderForMessages.stopAllCurrentTask();
    }

    private void initializeSettings() {
        pseudoOfUser = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_USER);
        cookieListInAString = PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST);
        lastMessageSended = PrefsManager.getString(PrefsManager.StringPref.Names.LAST_MESSAGE_SENDED);
        goToBottomOnLoadIsEnabled = PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_GO_TO_BOTTOM_ON_LOAD);
        linkTypeForInternalBrowser.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.LINK_TYPE_FOR_INTERNAL_BROWSER));
        convertNoelshackLinkToDirectLink = PrefsManager.getBool(PrefsManager.BoolPref.Names.USE_DIRECT_NOELSHACK_LINK);
        showOverviewOnImageClick = PrefsManager.getBool(PrefsManager.BoolPref.Names.SHOW_OVERVIEW_ON_IMAGE_CLICK);
        utilsForDraft.loadPrefsInfos();
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

    private void updatePostTypeNotice() {
        if (topicStatus.userCanPostAsModo && PrefsManager.getBool(PrefsManager.BoolPref.Names.POST_AS_MODO_WHEN_POSSIBLE)) {
            insertStuffButton.setColorFilter(ThemeManager.getColorInt(R.attr.themedPseudoModoColor, this), PorterDuff.Mode.SRC_IN);
        } else {
            insertStuffButton.clearColorFilter();
        }
    }

    private void lockReasonHasBeenUpdated() {
        int newXPaddingForMessageSend;
        int yPaddingForMessageSend = getResources().getDimensionPixelSize(R.dimen.yPaddingSendMessageEditTextNormal);

        if (topicStatus.lockReason == null) {
            newXPaddingForMessageSend = getResources().getDimensionPixelSize(R.dimen.xPaddingSendMessageEditTextNormal);
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
            newXPaddingForMessageSend = getResources().getDimensionPixelSize(R.dimen.xPaddingSendMessageEditTextTopicLock);
            insertStuffButton.setVisibility(View.GONE);
            messageSendButton.setVisibility(View.GONE);
            messageSendButton.setEnabled(false);
            messageSendEdit.setFocusable(false);
            messageSendEdit.setFocusableInTouchMode(false);
            messageSendEdit.setAlpha(0.33f);
            messageSendEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            messageSendEdit.setText(getString(R.string.topicLockedForReason, Utils.truncateString(topicStatus.lockReason, 80, getString(R.string.waitingText))));
            messageSendEdit.setOnClickListener(lockReasonCLickedListener);
        }

        messageSendEdit.setPadding(newXPaddingForMessageSend, yPaddingForMessageSend, newXPaddingForMessageSend, yPaddingForMessageSend);
    }

    private void startEditThisMessage(String messageID, boolean useMessageToEdit) {
        boolean infoForEditAreGetted = false;

        if (messageSendButton.isEnabled() && topicStatus.ajaxInfos.list != null) {
            messageSendButton.setEnabled(false);
            messageSendButton.setImageDrawable(ThemeManager.getDrawable(R.attr.themedContentEditIcon, this));
            infoForEditAreGetted = senderForMessages.getInfosForEditMessage(messageID, topicStatus.ajaxInfos.list, cookieListInAString, useMessageToEdit);
        }

        if (!infoForEditAreGetted) {
            if (!messageSendButton.isEnabled()) {
                Toast.makeText(this, R.string.errorMessageAlreadySending, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void cancelEditAndHideKeyboardAndCursor() {
        senderForMessages.cancelEdit();
        messageSendButton.setEnabled(true);
        messageSendButton.setImageDrawable(ThemeManager.getDrawable(R.attr.themedContentSendIcon, this));
        messageSendEdit.setText("");
        Utils.hideSoftKeyboard(ShowTopicActivity.this);
        messageSendLayout.requestFocus();
    }

    private AbsShowTopicFragment getCurrentFragment() {
        return (AbsShowTopicFragment) pageNavigation.getCurrentFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showtopic);
        initToolbar(R.id.toolbar_showtopic).setOnLongClickListener(showForumAndTopicTitleListener);

        ActionBar myActionBar = getSupportActionBar();
        Drawable arrowDrawable = ThemeManager.getDrawable(R.attr.themedArrowDropDown, this);
        arrowDrawable.setBounds(0, 0, arrowDrawable.getIntrinsicWidth(), arrowDrawable.getIntrinsicHeight());

        messageSendLayout = findViewById(R.id.sendmessage_layout_showtopic);
        messageSendEdit = findViewById(R.id.sendmessage_text_showtopic);
        messageSendButton = findViewById(R.id.sendmessage_button_showtopic);
        insertStuffButton = findViewById(R.id.insertstuff_button_showtopic);

        pageNavigation.initializeLayoutForAllNavigationButtons(findViewById(R.id.header_layout_showtopic), findViewById(R.id.shadow_header_showtopic));
        pageNavigation.initializePagerView((ViewPager) findViewById(R.id.pager_showtopic));
        pageNavigation.initializeNavigationButtons((Button) findViewById(R.id.firstpage_button_showtopic), (Button) findViewById(R.id.previouspage_button_showtopic),
                (Button) findViewById(R.id.currentpage_button_showtopic), (Button) findViewById(R.id.nextpage_button_showtopic), (Button) findViewById(R.id.lastpage_button_showtopic));

        pageNavigation.setDrawableForCurrentPageButton(arrowDrawable);

        pageNavigation.updateAdapterForPagerView();
        actionsForTopic = new JVCActionsInTopic(this);
        actionsForTopic.setNewMessageIsQuotedListener(messageIsQuotedListener);
        actionsForTopic.setTopicNeedToBeReloadedListener(this);
        senderForMessages = new JVCMessageToTopicSender(this);
        senderForMessages.setListenerForNewMessageWantEdit(listenerForNewMessageWantEdit);
        senderForMessages.setListenerForNewMessagePosted(listenerForNewMessagePosted);
        messageSendButton.setOnClickListener(sendMessageToTopicListener);
        messageSendButton.setOnLongClickListener(refreshFromSendButton);
        insertStuffButton.setOnClickListener(selectStickerClickedListener);
        insertStuffButton.setOnLongClickListener(showSendmessageActionListener);

        updateShowNavigationButtons();
        initializeSettings();
        if (savedInstanceState == null) {
            if (getIntent() != null) {
                String possibleLinkToUse = getIntent().getStringExtra(EXTRA_TOPIC_LINK);
                goToLastPageAfterLoading = getIntent().getBooleanExtra(EXTRA_GO_TO_LAST_PAGE, false);
                topicHasBeenOpenedFromAForum = getIntent().getBooleanExtra(EXTRA_OPENED_FROM_FORUM, true);
                topicStatus.names.forum = getIntent().getStringExtra(EXTRA_FORUM_NAME);
                topicStatus.names.topic = getIntent().getStringExtra(EXTRA_TOPIC_NAME);

                if (getIntent().getStringExtra(EXTRA_PSEUDO_OF_AUTHOR) != null) {
                    topicStatus.pseudoOfAuthor = getIntent().getStringExtra(EXTRA_PSEUDO_OF_AUTHOR);
                }

                if (goToBottomOnLoadIsEnabled) {
                    pageNavigation.setGoToBottomOnNextLoad(goToLastPageAfterLoading);
                }

                if (Utils.stringIsEmptyOrNull(topicStatus.names.forum)) {
                    topicStatus.names.forum = getString(R.string.app_name);
                }
                if (topicStatus.names.topic == null) {
                    topicStatus.names.topic = "";
                }

                if (possibleLinkToUse != null) {
                    if (!possibleLinkToUse.isEmpty()) {
                        possibleLinkToUse = JVCParser.formatThisUrlToClassicJvcUrl(possibleLinkToUse);
                    }
                    pageNavigation.setCurrentLink(possibleLinkToUse);
                }
            } else {
                topicStatus.names.forum = getString(R.string.app_name);
                topicStatus.names.topic = "";
            }

            /* Si les informations du topic n'étaient pas présentes dans l'Intent ça veut dire qu'il faut les récupérer dans les prefs
             * parce que c'est le ShowTopic lancé au démarrage de l'application. */
            if (pageNavigation.getCurrentLinkIsEmpty()) {
                pageNavigation.setCurrentLink(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_URL_TO_FETCH));
                if (topicStatus.pseudoOfAuthor.isEmpty()) {
                    topicStatus.pseudoOfAuthor = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_AUTHOR_OF_TOPIC);
                }
            }

            updateLastPageAndCurrentItemAndButtonsToCurrentLink();

            if (utilsForDraft.lastDraftSavedHasToBeUsed()) {
                messageSendEdit.setText(PrefsManager.getString(PrefsManager.StringPref.Names.MESSAGE_DRAFT));
            }
        } else {
            topicHasBeenOpenedFromAForum = savedInstanceState.getBoolean(SAVE_TOPIC_OPENED_FROM_FORUM, true);
            topicStatus = savedInstanceState.getParcelable(SAVE_TOPIC_STATUS);
            pageNavigation.setCurrentLink(savedInstanceState.getString(SAVE_CURRENT_TOPIC_LINK, ""));
            pageNavigation.setLastPageNumber(savedInstanceState.getInt(SAVE_LAST_PAGE, pageNavigation.getCurrentItemIndex() + 1));
            goToLastPageAfterLoading = savedInstanceState.getBoolean(SAVE_GO_TO_LAST_PAGE_AFTER_LOADING, false);
            pageNavigation.notifyDataSetChanged();

            senderForMessages.loadFromBundle(savedInstanceState);

            if (senderForMessages.getIsInEdit()) {
                messageSendButton.setImageDrawable(ThemeManager.getDrawable(R.attr.themedContentEditIcon, this));
            }

            pageNavigation.updateNavigationButtons();
            lockReasonHasBeenUpdated();
        }

        updatePostTypeNotice();

        if (myActionBar != null) {
            myActionBar.setTitle(topicStatus.names.forum);
            myActionBar.setSubtitle(Utils.applyEmojiCompatIfPossible(topicStatus.names.topic));
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
            PrefsManager.putString(PrefsManager.StringPref.Names.PSEUDO_OF_AUTHOR_OF_TOPIC, topicStatus.pseudoOfAuthor);
        }

        if (topicStatus.lockReason == null) {
            PrefsManager.putString(PrefsManager.StringPref.Names.MESSAGE_DRAFT, messageSendEdit.getText().toString());
            utilsForDraft.afterDraftIsSaved();
        }

        /* Si reasonOfLock != null cela veut dire que la page a chargée et donc que pageNavigation.getCurrentLinkIsEmpty() == false.
         * Donc dans tous les cas il y a des changements de préférences à appliquer.*/
        PrefsManager.applyChanges();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_TOPIC_OPENED_FROM_FORUM, topicHasBeenOpenedFromAForum);
        outState.putParcelable(SAVE_TOPIC_STATUS, topicStatus);
        outState.putString(SAVE_CURRENT_TOPIC_LINK, pageNavigation.getCurrentPageLink());
        outState.putInt(SAVE_LAST_PAGE, pageNavigation.getLastPage());
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

        MenuItem lockItem = menu.findItem(R.id.action_change_lock_topic_value_showtopic);
        MenuItem favItem = menu.findItem(R.id.action_change_topic_fav_value_showtopic);
        MenuItem subItem = menu.findItem(R.id.action_change_topic_sub_value_showtopic);

        menu.findItem(R.id.action_go_to_forum_of_topic_showtopic).setVisible(!topicHasBeenOpenedFromAForum);
        favItem.setEnabled(false);
        subItem.setEnabled(false);
        if (!pseudoOfUser.isEmpty()) {
            lockItem.setVisible(topicStatus.userCanLockOrUnlockTopic);

            if (topicStatus.lockReason == null) {
                lockItem.setTitle(R.string.lockTopic);
            } else {
                lockItem.setTitle(R.string.unlockTopic);
            }

            if (topicStatus.isInFavs != null) {
                favItem.setEnabled(true);
                if (topicStatus.isInFavs) {
                    favItem.setTitle(R.string.removeFromFavs);
                } else {
                    favItem.setTitle(R.string.addToFavs);
                }
            }

            if (topicStatus.subId != null) {
                subItem.setEnabled(true);
                if (topicStatus.subId.isEmpty()) {
                    subItem.setTitle(R.string.subToTopic);
                } else {
                    subItem.setTitle(R.string.unsubFromTopic);
                }
            }
        } else {
            lockItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_go_to_forum_of_topic_showtopic:
                Intent newShowForumIntent = new Intent(this, ShowForumActivity.class);
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, JVCParser.getForumForTopicLink(pageNavigation.getCurrentPageLink()));
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_FORUM_NAME, topicStatus.names.forum);
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_IS_FIRST_ACTIVITY, false);
                startActivity(newShowForumIntent);
                return true;
            case R.id.action_change_topic_fav_value_showtopic:
                if (topicStatus.isInFavs != null) {
                    if (currentTaskForFavs == null) {
                        currentTaskForFavs = new AddOrRemoveThingToFavs(!topicStatus.isInFavs, this);
                        currentTaskForFavs.execute(JVCParser.getForumIdOfThisTopic(pageNavigation.getCurrentPageLink()), topicStatus.topicId, topicStatus.ajaxInfos.pref, cookieListInAString);
                    } else {
                        Toast.makeText(ShowTopicActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShowTopicActivity.this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_change_topic_sub_value_showtopic:
                if (topicStatus.subId != null) {
                    if (currentTaskForSubs == null) {
                        currentTaskForSubs = new AddOrRemoveTopicToSubs(topicStatus.subId.isEmpty(), this);
                        currentTaskForSubs.execute(topicStatus.topicId, topicStatus.subId, topicStatus.ajaxInfos.sub, cookieListInAString);
                    } else {
                        Toast.makeText(ShowTopicActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShowTopicActivity.this, R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_change_lock_topic_value_showtopic:
                if (topicStatus.lockReason == null) {
                    Intent newLockTopicIntent = new Intent(ShowTopicActivity.this, LockTopicActivity.class);
                    newLockTopicIntent.putExtra(LockTopicActivity.EXTRA_ID_FORUM, JVCParser.getForumIdOfThisTopic(pageNavigation.getCurrentPageLink()));
                    newLockTopicIntent.putExtra(LockTopicActivity.EXTRA_ID_TOPIC, topicStatus.topicId);
                    newLockTopicIntent.putExtra(LockTopicActivity.EXTRA_AJAX_MOD, topicStatus.ajaxInfos.mod);
                    newLockTopicIntent.putExtra(LockTopicActivity.EXTRA_COOKIES, cookieListInAString);
                    startActivityForResult(newLockTopicIntent, LOCK_TOPIC_REQUEST_CODE);
                } else {
                    actionsForTopic.startUnlockThisTopic(topicStatus.ajaxInfos, JVCParser.getForumIdOfThisTopic(pageNavigation.getCurrentPageLink()), topicStatus.topicId, cookieListInAString);
                }
                return true;
            case R.id.action_open_in_browser_showtopic:
                Utils.openCorrespondingBrowser(linkTypeForInternalBrowser, pageNavigation.getCurrentPageLink(), this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (topicStatus.lockReason == null && !messageSendEdit.getText().toString().isEmpty()) {
            utilsForDraft.whenUserTryToLeaveWithDraft(R.string.messageDraftSaved, R.string.saveMessageDraftExplained, this);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOCK_TOPIC_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            refreshTopicSafely();
        }
    }

    @Override
    public void newModeRequested(int newMode) {
        if (newMode == AbsShowTopicFragment.MODE_IRC || newMode == AbsShowTopicFragment.MODE_FORUM) {
            PrefsManager.putInt(PrefsManager.IntPref.Names.CURRENT_TOPIC_MODE, newMode);
            PrefsManager.applyChanges();
            updateShowNavigationButtons();
            pageNavigation.setCurrentLink(pageNavigation.getFirstPageLink());
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
    public void updateTopicLink(String newTopicLink) {
        pageNavigation.setCurrentLink(newTopicLink);
    }

    @Override
    public boolean onMenuItemClickedInMessage(MenuItem item, JVCParser.MessageInfos fromThisMessage) {
        switch (item.getItemId()) {
            case R.id.menu_quote_message:
                if (pseudoOfUser.isEmpty()) {
                    Toast.makeText(this, R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                } else if (topicStatus.lockReason != null) {
                    Toast.makeText(this, R.string.errorTopicIsLocked, Toast.LENGTH_SHORT).show();
                } else {
                    actionsForTopic.startQuoteThisMessage(topicStatus.ajaxInfos, fromThisMessage, cookieListInAString);
                }
                return true;
            case R.id.menu_edit_message:
                if (topicStatus.lockReason == null) {
                    if (senderForMessages.getIsInEdit()) {
                        cancelEditAndHideKeyboardAndCursor();
                    } else {
                        startEditThisMessage(Long.toString(fromThisMessage.id), true);
                    }
                } else {
                    Toast.makeText(this, R.string.errorTopicIsLocked, Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.menu_delete_message:
                actionsForTopic.startDeleteThisMessage(topicStatus.ajaxInfos, fromThisMessage, cookieListInAString);
                return true;
            case R.id.menu_restore_message:
                actionsForTopic.startRestoreThisMessage(topicStatus.ajaxInfos, fromThisMessage, cookieListInAString);
                return true;
            case R.id.menu_kick_author_message:
                Intent newKickPseudoIntent = new Intent(ShowTopicActivity.this, KickPseudoActivity.class);
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_PSEUDO, fromThisMessage.pseudo);
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_ID_ALIAS, fromThisMessage.idAlias);
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_ID_FORUM, JVCParser.getForumIdOfThisTopic(pageNavigation.getCurrentPageLink()));
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_ID_MESSAGE, String.valueOf(fromThisMessage.id));
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_AJAX_MOD, topicStatus.ajaxInfos.mod);
                newKickPseudoIntent.putExtra(KickPseudoActivity.EXTRA_COOKIES, cookieListInAString);
                startActivity(newKickPseudoIntent);
                return true;
            case R.id.menu_dekick_author_message:
                actionsForTopic.startDekickThisPseudo(fromThisMessage.pseudo, topicStatus.ajaxInfos, JVCParser.getForumIdOfThisTopic(pageNavigation.getCurrentPageLink()), fromThisMessage.idAlias, cookieListInAString);
                return true;
            default:
                return false;
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
            choosePageDialogFragment.show(getSupportFragmentManager(), "ChoosePageNumberDialogFragment");
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
    public void onNewPageSelected(int position) {
        if (position != pageNavigation.getLastPage() - 1) {
            goToLastPageAfterLoading = false;
            pageNavigation.setGoToBottomOnNextLoad(false);
        }
    }

    @Override
    public void doThingsBeforeLoadOnFragment(AbsShowSomethingFragment thisFragment) {
        ((AbsShowTopicFragment) thisFragment).setPseudoOfAuthor(topicStatus.pseudoOfAuthor);
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
            String possibleNewLink = JVCParser.formatThisUrlToClassicJvcUrl(link);

            if (JVCParser.checkIfItsTopicFormatedLink(possibleNewLink)) {
                Intent newShowTopicIntent = new Intent(this, ShowTopicActivity.class);
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, possibleNewLink);
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_OPENED_FROM_FORUM, false);
                startActivity(newShowTopicIntent);
            } else if (JVCParser.checkIfItsForumFormatedLink(possibleNewLink)) {
                Intent newShowForumIntent = new Intent(this, ShowForumActivity.class);
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, possibleNewLink);
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_IS_FIRST_ACTIVITY, false);
                startActivity(newShowForumIntent);
            } else if (showOverviewOnImageClick && JVCParser.checkIfItsNoelshackLink(link)) {
                Bundle argForFrag = new Bundle();
                ShowImageDialogFragment showImageDialogFragment = new ShowImageDialogFragment();
                argForFrag.putString(ShowImageDialogFragment.ARG_IMAGE_LINK, JVCParser.noelshackToDirectLink(link));
                showImageDialogFragment.setArguments(argForFrag);
                showImageDialogFragment.show(getSupportFragmentManager(), "ShowImageDialogFragment");
            } else {
                Utils.openCorrespondingBrowser(linkTypeForInternalBrowser, link, this);
            }
        } else {
            Bundle argForFrag = new Bundle();
            LinkMenuDialogFragment linkMenuDialogFragment = new LinkMenuDialogFragment();
            argForFrag.putString(LinkMenuDialogFragment.ARG_URL, link);
            linkMenuDialogFragment.setArguments(argForFrag);
            linkMenuDialogFragment.show(getSupportFragmentManager(), "LinkMenuDialogFragment");
        }
    }

    @Override
    public void getStringInserted(String newStringToAdd, int posOfCenterFromEnd) {
        if (topicStatus.lockReason == null) {
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
            AbsShowTopicFragment currentFrag = getCurrentFragment();

            if (currentTaskForFavs.getAddToFavs()) {
                Toast.makeText(this, R.string.favAdded, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.favRemoved, Toast.LENGTH_SHORT).show();
            }

            if (currentFrag != null) {
                topicStatus.isInFavs = currentTaskForFavs.getAddToFavs();
                currentFrag.updateTopicStatusInfos(topicStatus);
            }
        }
        currentTaskForFavs = null;
    }

    @Override
    public void getActionToSubsResult(String resultInString, boolean itsAnError) {
        if (itsAnError) {
            if (resultInString.isEmpty()) {
                resultInString = getString(R.string.errorInfosMissings);
            }
            Toast.makeText(this, resultInString, Toast.LENGTH_SHORT).show();
        } else {
            AbsShowTopicFragment currentFrag = getCurrentFragment();

            if (currentTaskForSubs.getAddToSubs()) {
                Toast.makeText(this, R.string.subAdded, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.subRemoved, Toast.LENGTH_SHORT).show();
            }

            if (currentFrag != null) {
                topicStatus.subId = resultInString;
                currentFrag.updateTopicStatusInfos(topicStatus);
            }
        }
        currentTaskForSubs = null;
    }

    @Override
    public void getNewSurveyInfos(String surveyTitle, String topicId, String ajaxInfos, ArrayList<JVCParser.SurveyReplyInfos> listOfReplysWithInfos) {
        Intent newShowSurveyIntent = new Intent(ShowTopicActivity.this, ShowSurveyActivity.class);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_SURVEY_TITLE, surveyTitle);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_SURVEY_REPLYS_WITH_INFOS, listOfReplysWithInfos);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_TOPIC_ID, topicId);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_AJAX_INFOS, ajaxInfos);
        newShowSurveyIntent.putExtra(ShowSurveyActivity.EXTRA_COOKIES, cookieListInAString);
        startActivity(newShowSurveyIntent);
    }

    @Override
    public void getMessageOfPseudoClicked(JVCParser.MessageInfos messageClicked) {
        Bundle argForFrag = new Bundle();
        MessageMenuDialogFragment messageMenuDialogFragment = new MessageMenuDialogFragment();
        argForFrag.putString(MessageMenuDialogFragment.ARG_PSEUDO_MESSAGE, messageClicked.pseudo);
        argForFrag.putString(MessageMenuDialogFragment.ARG_PSEUDO_USER, pseudoOfUser);
        argForFrag.putString(MessageMenuDialogFragment.ARG_MESSAGE_ID, String.valueOf(messageClicked.id));
        argForFrag.putInt(MessageMenuDialogFragment.ARG_LINK_TYPE_FOR_INTERNAL_BROWSER, linkTypeForInternalBrowser.type);
        argForFrag.putString(MessageMenuDialogFragment.ARG_MESSAGE_CONTENT, messageClicked.messageNotParsed);
        messageMenuDialogFragment.setArguments(argForFrag);
        messageMenuDialogFragment.show(getSupportFragmentManager(), "MessageMenuDialogFragment");
    }

    @Override
    public void onIgnoreNewPseudo(String newPseudoIgnored) {
        AbsShowTopicFragment currentFrag = getCurrentFragment();
        if (currentFrag != null) {
            currentFrag.ignoreThisPseudoFromListOfMessages(newPseudoIgnored);
        }
    }

    @Override
    public void getNewTopicStatus(AbsJVCTopicGetter.TopicStatusInfos newTopicStatus, AbsJVCTopicGetter.TopicStatusInfos oldTopicStatus) {
        /* Pour utiliser le TopicStatus de ShowTopic au lieu de JVCTopicGetter en tant qu'old TopicStatus. */
        oldTopicStatus = topicStatus;
        topicStatus = newTopicStatus;

        if (topicStatus.userCanPostAsModo != oldTopicStatus.userCanPostAsModo) {
            updatePostTypeNotice();
        }

        if (topicStatus.pseudoOfAuthor.isEmpty()) {
            topicStatus.pseudoOfAuthor = oldTopicStatus.pseudoOfAuthor;
        } else if (!topicStatus.pseudoOfAuthor.equals(oldTopicStatus.pseudoOfAuthor)) {
            AbsShowTopicFragment currentFrag = getCurrentFragment();
            if (currentFrag != null) {
                currentFrag.setPseudoOfAuthor(topicStatus.pseudoOfAuthor);
            }
        }

        if (!Utils.stringsAreEquals(topicStatus.lockReason, oldTopicStatus.lockReason)) {
            lockReasonHasBeenUpdated();
        }

        if (!topicStatus.names.equals(oldTopicStatus.names)) {
            ActionBar myActionBar = getSupportActionBar();

            if (topicStatus.names.forum.isEmpty()) {
                topicStatus.names.forum = getString(R.string.app_name);
            }

            if (myActionBar != null) {
                myActionBar.setTitle(topicStatus.names.forum);
                myActionBar.setSubtitle(Utils.applyEmojiCompatIfPossible(topicStatus.names.topic));
            }
        }
    }

    @Override
    public void onReloadTopicRequested() {
        refreshTopicSafely();
    }
}
