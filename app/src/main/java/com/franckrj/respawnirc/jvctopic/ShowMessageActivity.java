package com.franckrj.respawnirc.jvctopic;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.franckrj.respawnirc.NetworkBroadcastReceiver;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.dialogs.LinkMenuDialogFragment;
import com.franckrj.respawnirc.dialogs.MessageMenuDialogFragment;
import com.franckrj.respawnirc.dialogs.ShowImageDialogFragment;
import com.franckrj.respawnirc.jvcforum.SearchTopicInForumActivity;
import com.franckrj.respawnirc.jvcforum.ShowForumActivity;
import com.franckrj.respawnirc.jvctopic.jvctopicviewers.JVCTopicAdapter;
import com.franckrj.respawnirc.utils.AccountManager;
import com.franckrj.respawnirc.utils.ImageDownloader;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.Utils;
import com.franckrj.respawnirc.utils.WebManager;

/* Classe dégueulasse créée rapidement pour la 1.11. En espérant que tout soit mieux fait pour la 2.0. */
public class ShowMessageActivity extends AbsHomeIsBackActivity {
    public static final String EXTRA_MESSAGE_PERMALINK = "com.franckrj.respawnirc.showmessageactivity.EXTRA_MESSAGE_PERMALINK";

    private static final String SAVE_MESSAGE_SHOWED = "saveMessageShowed";

    private TextView backgroundErrorText = null;
    private JVCTopicAdapter adapterForTopic = null;
    private JVCParser.Settings currentSettings = new JVCParser.Settings();
    private PrefsManager.ShowImageType showNoelshackImageType = new PrefsManager.ShowImageType(PrefsManager.ShowImageType.ALWAYS);
    private GetMessageToShow currentTaskForGetMessage = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private ListView jvcMsgList = null;
    private boolean fastRefreshOfImages = false;
    private PrefsManager.LinkType linkTypeForInternalBrowser = new PrefsManager.LinkType(PrefsManager.LinkType.NO_LINKS);
    private boolean convertNoelshackLinkToDirectLink = false;
    private boolean showOverviewOnImageClick = false;
    private MessageShowedStatusInfos messageShowedStatus = new MessageShowedStatusInfos();

    private final JVCTopicAdapter.MenuItemClickedInMessage menuItemClickedInMessageListener = new JVCTopicAdapter.MenuItemClickedInMessage() {
        @Override
        public boolean onMenuItemClickedInMessage(MenuItem item, JVCParser.MessageInfos fromThisMessage) {
            switch (item.getItemId()) {
                case R.id.menu_show_spoil_message:
                    fromThisMessage.listOfSpoilIdToShow.add(-1);
                    adapterForTopic.updateThisItem(fromThisMessage, false);
                    adapterForTopic.notifyDataSetChanged();
                    return true;
                case R.id.menu_hide_spoil_message:
                    fromThisMessage.listOfSpoilIdToShow.clear();
                    adapterForTopic.updateThisItem(fromThisMessage, false);
                    adapterForTopic.notifyDataSetChanged();
                    return true;
                case R.id.menu_show_quote_message:
                    fromThisMessage.showOverlyQuote = true;
                    adapterForTopic.updateThisItem(fromThisMessage, false);
                    adapterForTopic.notifyDataSetChanged();
                    return true;
                case R.id.menu_hide_quote_message:
                    fromThisMessage.showOverlyQuote = false;
                    adapterForTopic.updateThisItem(fromThisMessage, false);
                    adapterForTopic.notifyDataSetChanged();
                    return true;
                case R.id.menu_show_ugly_images_message:
                    fromThisMessage.showUglyImages = true;
                    adapterForTopic.updateThisItem(fromThisMessage, false);
                    adapterForTopic.notifyDataSetChanged();
                    return true;
                case R.id.menu_hide_ugly_images_message:
                    fromThisMessage.showUglyImages = false;
                    adapterForTopic.updateThisItem(fromThisMessage, false);
                    adapterForTopic.notifyDataSetChanged();
                    return true;
                default:
                    return false;
            }
        }
    };

    private final ImageDownloader.DownloadFinished listenerForDownloadFinished = new ImageDownloader.DownloadFinished() {
        @Override
        public void newDownloadFinished(int numberOfDownloadRemaining) {
            if (numberOfDownloadRemaining == 0 || fastRefreshOfImages) {
                jvcMsgList.invalidateViews();
            }
        }
    };

    private final JVCTopicAdapter.URLClicked listenerForUrlClicked = new JVCTopicAdapter.URLClicked() {
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
                    Intent newShowTopicIntent = new Intent(ShowMessageActivity.this, ShowTopicActivity.class);
                    newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, possibleNewLink);
                    newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_OPENED_FROM_FORUM, false);
                    startActivity(newShowTopicIntent);
                } else if (JVCParser.checkIfItsForumFormatedLink(possibleNewLink)) {
                    Intent newShowForumIntent = new Intent(ShowMessageActivity.this, ShowForumActivity.class);
                    newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, possibleNewLink);
                    newShowForumIntent.putExtra(ShowForumActivity.EXTRA_IS_FIRST_ACTIVITY, false);
                    startActivity(newShowForumIntent);
                } else if (JVCParser.checkIfItsSearchFormatedLink(possibleNewLink)) {
                    Intent newSearchInForumIntent = new Intent(ShowMessageActivity.this, SearchTopicInForumActivity.class);
                    newSearchInForumIntent.putExtra(SearchTopicInForumActivity.EXTRA_SEARCH_LINK, possibleNewLink);
                    startActivity(newSearchInForumIntent);
                } else if (JVCParser.checkIfItsMessageFormatedLink(possibleNewLink)) {
                    Intent newShowMessageIntent = new Intent(ShowMessageActivity.this, ShowMessageActivity.class);
                    newShowMessageIntent.putExtra(ShowMessageActivity.EXTRA_MESSAGE_PERMALINK, possibleNewLink);
                    startActivity(newShowMessageIntent);
                } else if (showOverviewOnImageClick && JVCParser.checkIfItsNoelshackLink(link)) {
                    if (!getSupportFragmentManager().isStateSaved()) {
                        Bundle argForFrag = new Bundle();
                        ShowImageDialogFragment showImageDialogFragment = new ShowImageDialogFragment();
                        argForFrag.putString(ShowImageDialogFragment.ARG_IMAGE_LINK, JVCParser.noelshackToDirectLink(link));
                        showImageDialogFragment.setArguments(argForFrag);
                        showImageDialogFragment.show(getSupportFragmentManager(), "ShowImageDialogFragment");
                    }
                } else {
                    Utils.openCorrespondingBrowser(linkTypeForInternalBrowser, link, ShowMessageActivity.this);
                }
            } else if (!getSupportFragmentManager().isStateSaved()) {
                Bundle argForFrag = new Bundle();
                LinkMenuDialogFragment linkMenuDialogFragment = new LinkMenuDialogFragment();
                argForFrag.putString(LinkMenuDialogFragment.ARG_URL, link);
                linkMenuDialogFragment.setArguments(argForFrag);
                linkMenuDialogFragment.show(getSupportFragmentManager(), "LinkMenuDialogFragment");
            }
        }
    };

    private final JVCTopicAdapter.PseudoClicked listenerForPseudoClicked = new JVCTopicAdapter.PseudoClicked() {
        @Override
        public void getMessageOfPseudoClicked(JVCParser.MessageInfos messageClicked) {
            if (!getSupportFragmentManager().isStateSaved()) {
                Bundle argForFrag = new Bundle();
                MessageMenuDialogFragment messageMenuDialogFragment = new MessageMenuDialogFragment();
                argForFrag.putString(MessageMenuDialogFragment.ARG_PSEUDO_MESSAGE, messageClicked.pseudo);
                argForFrag.putString(MessageMenuDialogFragment.ARG_PSEUDO_USER, currentSettings.pseudoOfUser);
                argForFrag.putString(MessageMenuDialogFragment.ARG_MESSAGE_ID, String.valueOf(messageClicked.id));
                argForFrag.putInt(MessageMenuDialogFragment.ARG_LINK_TYPE_FOR_INTERNAL_BROWSER, linkTypeForInternalBrowser.type);
                argForFrag.putString(MessageMenuDialogFragment.ARG_MESSAGE_CONTENT, messageClicked.messageNotParsed);
                messageMenuDialogFragment.setArguments(argForFrag);
                messageMenuDialogFragment.show(getSupportFragmentManager(), "MessageMenuDialogFragment");
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsStarted getMessageIsStartedListener = new AbsWebRequestAsyncTask.RequestIsStarted() {
        @Override
        public void onRequestIsStarted() {
            swipeRefresh.setRefreshing(true);
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<MessageShowedStatusInfos> getMessageIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<MessageShowedStatusInfos>() {
        @Override
        public void onRequestIsFinished(MessageShowedStatusInfos reqResult) {
            swipeRefresh.setRefreshing(false);
            messageShowedStatus = reqResult;

            if (messageShowedStatus.message == null) {
                backgroundErrorText.setVisibility(View.VISIBLE);
                backgroundErrorText.setText(R.string.errorDownloadFailed);
            } else {
                ActionBar myActionBar = getSupportActionBar();
                adapterForTopic.addItem(messageShowedStatus.message, true);
                if (myActionBar != null) {
                    myActionBar.setSubtitle(messageShowedStatus.message.pseudo);
                }
            }

            currentTaskForGetMessage = null;
        }
    };

    private void initializeSettingsAndList() {
        PrefsManager.ShowImageType showAvatarType = new PrefsManager.ShowImageType(PrefsManager.ShowImageType.ALWAYS);
        boolean cardDesignIsEnabled;
        int avatarSizeInDP;
        int stickerSizeInDP;
        int miniNoelshackWidthInDP;

        showAvatarType.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.SHOW_AVATAR_MODE_FORUM));

        cardDesignIsEnabled = (ThemeManager.getThemeUsed() != ThemeManager.ThemeName.BLACK_THEME && PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_CARD_DESIGN_MODE_FORUM)) ||
                (ThemeManager.getThemeUsed() == ThemeManager.ThemeName.BLACK_THEME && !PrefsManager.getBool(PrefsManager.BoolPref.Names.SEPARATION_BETWEEN_MESSAGES_BLACK_THEM_MODE_FORUM));

        if (showAvatarType.type == PrefsManager.ShowImageType.ALWAYS ||
                (showAvatarType.type == PrefsManager.ShowImageType.WIFI_ONLY && NetworkBroadcastReceiver.getIsConnectedWithWifi())) {
            if (cardDesignIsEnabled) {
                adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_avatars_card_rowforum);
            } else {
                adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_avatars_rowforum);
            }
            adapterForTopic.setShowAvatars(true);
            adapterForTopic.setMultiplierOfLineSizeForInfoLineIfAvatarIsShowed(1.25f);
        } else {
            if (cardDesignIsEnabled) {
                adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_card_rowforum);
            } else {
                adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_rowforum);
            }
            adapterForTopic.setShowAvatars(false);
        }

        adapterForTopic.setAlternateBackgroundColor(PrefsManager.getBool(PrefsManager.BoolPref.Names.TOPIC_ALTERNATE_BACKGROUND_MODE_FORUM));
        adapterForTopic.setShowSignatures(PrefsManager.getBool(PrefsManager.BoolPref.Names.SHOW_SIGNATURE_MODE_FORUM));

        try {
            avatarSizeInDP = PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.AVATAR_SIZE);
        } catch (Exception e) {
            avatarSizeInDP = -1;
        }
        try {
            stickerSizeInDP = PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.STICKER_SIZE);
        } catch (Exception e) {
            stickerSizeInDP = -1;
        }
        try {
            miniNoelshackWidthInDP = PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.MINI_NOELSHACK_WIDTH);
        } catch (Exception e) {
            miniNoelshackWidthInDP = -1;
        }

        showNoelshackImageType.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.SHOW_NOELSHACK_IMAGE));
        updateSettingsDependingOnConnection();

        if (avatarSizeInDP >= 0) {
            adapterForTopic.setAvatarSize(Utils.roundToInt(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, avatarSizeInDP, getResources().getDisplayMetrics())));
        }
        if (stickerSizeInDP >= 0) {
            adapterForTopic.setStickerSize(Utils.roundToInt(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, stickerSizeInDP, getResources().getDisplayMetrics())));
        }
        if (miniNoelshackWidthInDP >= 0) {
            adapterForTopic.setMiniNoeslahckSizeByWidth(Utils.roundToInt(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, miniNoelshackWidthInDP, getResources().getDisplayMetrics())));
        }

        linkTypeForInternalBrowser.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.LINK_TYPE_FOR_INTERNAL_BROWSER));
        convertNoelshackLinkToDirectLink = PrefsManager.getBool(PrefsManager.BoolPref.Names.USE_DIRECT_NOELSHACK_LINK);
        showOverviewOnImageClick = PrefsManager.getBool(PrefsManager.BoolPref.Names.SHOW_OVERVIEW_ON_IMAGE_CLICK);
        fastRefreshOfImages = PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_FAST_REFRESH_OF_IMAGES);
        currentSettings.firstLineFormat = "<b><%PSEUDO_COLOR_START%><%PSEUDO_PSEUDO%><%PSEUDO_COLOR_END%></b><small><%MARK_FOR_PSEUDO%><br>Le <%DATE_COLOR_START%><%DATE_FULL%><%DATE_COLOR_END%></small>";
        currentSettings.colorPseudoUser = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoUserColor, this));
        currentSettings.colorPseudoOther = Utils.colorToStringWithAlpha(ThemeManager.getColorInt(R.attr.themedPseudoOtherModeForumColor, this));
        currentSettings.colorPseudoModo = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoModoColor, this));
        currentSettings.colorPseudoAdmin = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoAdminColor, this));
        currentSettings.secondLineFormat = "<%MESSAGE_MESSAGE%><%EDIT_ALL%>";
        currentSettings.addBeforeEdit = "<br /><br /><small><i>";
        currentSettings.addAfterEdit = "</i></small>";
        currentSettings.applyMarkToPseudoAuthor = PrefsManager.getBool(PrefsManager.BoolPref.Names.MARK_AUTHOR_PSEUDO_MODE_FORUM);
        currentSettings.maxNumberOfOverlyQuotes = PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.MAX_NUMBER_OF_OVERLY_QUOTE);
        currentSettings.transformStickerToSmiley = PrefsManager.getBool(PrefsManager.BoolPref.Names.TRANSFORM_STICKER_TO_SMILEY);
        currentSettings.shortenLongLink = PrefsManager.getBool(PrefsManager.BoolPref.Names.SHORTEN_LONG_LINK);
        currentSettings.hideUglyImages = PrefsManager.getBool(PrefsManager.BoolPref.Names.HIDE_UGLY_IMAGES);
        currentSettings.enableAlphaInNoelshackMini = PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_ALPHA_IN_NOELSHACK_MINI);
        currentSettings.pseudoOfUser = AccountManager.getCurrentAccount().pseudo;
        currentSettings.typeOfPseudoToColorInInfoLine.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.TYPE_OF_PSEUDO_TO_COLOR_IN_INFO));
        currentSettings.typeOfPseudoToColorInMessage.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.TYPE_OF_PSEUDO_TO_COLOR_IN_MESSAGE));
        adapterForTopic.setShowSpoilDefault(PrefsManager.getBool(PrefsManager.BoolPref.Names.DEFAULT_SHOW_SPOIL_VAL));
        adapterForTopic.setColorDeletedMessages(PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_COLOR_DELETED_MESSAGES));
        adapterForTopic.setMessageFontSizeInSp(PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.MESSAGE_FONT_SIZE));
        adapterForTopic.setMessageInfosFontSizeInSp(PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.MESSAGE_INFOS_FONT_SIZE));
        adapterForTopic.setMessageSignatureFontSizeInSp(PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.MESSAGE_SIGNATURE_FONT_SIZE));
        adapterForTopic.setQuoteBackgroundColor(ThemeManager.getColorInt(R.attr.themedQuoteBackgroundColor, this));
        adapterForTopic.setQuoteStripeColor(ThemeManager.getColorInt(R.attr.themedQuoteStripeColor, this));
        adapterForTopic.setQuoteStripeSize(getResources().getDimensionPixelSize(R.dimen.quoteStripeSize));
        adapterForTopic.setQuoteGapSize(getResources().getDimensionPixelSize(R.dimen.quoteGapSize));
        adapterForTopic.setBaseTitleForSurvey(getString(R.string.titleForSurvey));
        adapterForTopic.setBaseSubTitleForSurvey(getString(R.string.clickHereToSee));
        adapterForTopic.setSurveyMessageBackgroundColor(ThemeManager.getColorInt(R.attr.themedSurveyMessageBackgroundColor, this));
        adapterForTopic.setDeletedMessageBackgroundColor(ThemeManager.getColorInt(R.attr.themedDeletedMessageBackgroundColor, this));
        adapterForTopic.setDefaultMessageBackgroundColor(ThemeManager.getColorInt(R.attr.themedDefaultBackgroundColor, this));
        adapterForTopic.setAltMessageBackgroundColor(ThemeManager.getColorInt(R.attr.themedAltBackgroundColor, this));

        if (cardDesignIsEnabled) {
            int paddingForMsgList = getResources().getDimensionPixelSize(R.dimen.spaceAroundSingleCard);
            int dividerSizeForMsgList = getResources().getDimensionPixelSize(R.dimen.spaceBetweenTwoCards);

            jvcMsgList.setPadding(paddingForMsgList, paddingForMsgList, paddingForMsgList, paddingForMsgList);
            jvcMsgList.setDivider(null);
            jvcMsgList.setDividerHeight(dividerSizeForMsgList);
        }
        jvcMsgList.setClipToPadding(false);
        jvcMsgList.setAdapter(adapterForTopic);
    }

    private void updateSettingsDependingOnConnection() {
        if (showNoelshackImageType.type != PrefsManager.ShowImageType.WIFI_ONLY) {
            currentSettings.showNoelshackImages = (showNoelshackImageType.type == PrefsManager.ShowImageType.ALWAYS);
        } else {
            currentSettings.showNoelshackImages = NetworkBroadcastReceiver.getIsConnectedWithWifi();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showmessage);
        initToolbar(R.id.toolbar_showmessage);

        adapterForTopic = new JVCTopicAdapter(this, currentSettings);
        backgroundErrorText = findViewById(R.id.text_errorbackgroundmessage_showmessage);
        swipeRefresh = findViewById(R.id.swiperefresh_showmessage);
        jvcMsgList = findViewById(R.id.jvcmessage_view_showmessage);

        initializeSettingsAndList();
        adapterForTopic.setActionWhenItemMenuClicked(menuItemClickedInMessageListener);
        adapterForTopic.setDownloadFinishedListener(listenerForDownloadFinished);
        adapterForTopic.setUrlCLickedListener(listenerForUrlClicked);
        adapterForTopic.setPseudoClickedListener(listenerForPseudoClicked);
        backgroundErrorText.setVisibility(View.GONE);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorControlHighlightThemeLight);

        if (savedInstanceState != null) {
            messageShowedStatus = savedInstanceState.getParcelable(SAVE_MESSAGE_SHOWED);

            if (messageShowedStatus == null) {
                messageShowedStatus = new MessageShowedStatusInfos();
            }

            if (messageShowedStatus.message != null) {
                ActionBar myActionBar = getSupportActionBar();
                adapterForTopic.addItem(messageShowedStatus.message, true);
                if (myActionBar != null) {
                    myActionBar.setSubtitle(messageShowedStatus.message.pseudo);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSettingsDependingOnConnection();
        adapterForTopic.resumeTasks();

        if (messageShowedStatus.message == null) {
            if (getIntent() != null) {
                if (getIntent().getStringExtra(EXTRA_MESSAGE_PERMALINK) != null) {
                    currentTaskForGetMessage = new GetMessageToShow();
                    currentTaskForGetMessage.setRequestIsStartedListener(getMessageIsStartedListener);
                    currentTaskForGetMessage.setRequestIsFinishedListener(getMessageIsFinishedListener);
                    currentTaskForGetMessage.execute(getIntent().getStringExtra(EXTRA_MESSAGE_PERMALINK), AccountManager.getCurrentAccount().cookie);
                }
            }

            if (currentTaskForGetMessage == null) {
                backgroundErrorText.setVisibility(View.VISIBLE);
                backgroundErrorText.setText(R.string.errorDownloadFailed);
            }
        }
    }

    @Override
    public void onPause() {
        adapterForTopic.pauseTasks();
        if (currentTaskForGetMessage != null) {
            currentTaskForGetMessage.clearListenersAndCancel();
            currentTaskForGetMessage = null;
        }
        swipeRefresh.setRefreshing(false);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        /* On veut stop le téléchargement des images que si on est sur que cette activité ne sera plus jamais utilisée,
         * donc ici c'est peut-être le meilleur endroit même si c'est pas garanti (que ce soit appelé).
         * Normalement si onDestroy n'est pas appelé ça veut dire que tout le process a été tué (incluant les
         * ImageGetterAsyncTask) mais j'en suis pas sur. */
        adapterForTopic.stopAllCurrentTasks();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_MESSAGE_SHOWED, messageShowedStatus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_showmessage, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_go_to_topic_showmessage).setEnabled(!messageShowedStatus.topicLink.isEmpty());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_go_to_topic_showmessage:
                Intent newShowTopicIntent = new Intent(ShowMessageActivity.this, ShowTopicActivity.class);
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, JVCParser.formatThisUrlToClassicJvcUrl(messageShowedStatus.topicLink));
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_OPENED_FROM_FORUM, false);
                startActivity(newShowTopicIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static class GetMessageToShow extends AbsWebRequestAsyncTask<String, Void, MessageShowedStatusInfos> {
        @Override
        protected MessageShowedStatusInfos doInBackground(String... params) {
            MessageShowedStatusInfos newMessageShowedStatus = new MessageShowedStatusInfos();

            if (params.length > 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[1], true);
                String source = WebManager.sendRequest(params[0], "GET", "", currentWebInfos);

                if (source != null) {
                    newMessageShowedStatus.message = JVCParser.getMessageFromPermalinkPage(source);
                    newMessageShowedStatus.topicLink = JVCParser.getTopicLinkFromPermalinkPage(source);
                }
            }

            return newMessageShowedStatus;
        }
    }

    public static class MessageShowedStatusInfos implements Parcelable {
        JVCParser.MessageInfos message = null;
        String topicLink = "";

        public static final Parcelable.Creator<MessageShowedStatusInfos> CREATOR = new Parcelable.Creator<MessageShowedStatusInfos>() {
            @Override
            public MessageShowedStatusInfos createFromParcel(Parcel in) {
                return new MessageShowedStatusInfos(in);
            }

            @Override
            public MessageShowedStatusInfos[] newArray(int size) {
                return new MessageShowedStatusInfos[size];
            }
        };

        public MessageShowedStatusInfos() {
            //rien
        }

        private MessageShowedStatusInfos(Parcel in) {
            message = in.readParcelable(JVCParser.MessageInfos.class.getClassLoader());
            topicLink = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeParcelable(message, flags);
            out.writeString(topicLink);
        }
    }
}
