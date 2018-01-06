package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.ShareActionProvider;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.base.AbsShowSomethingFragment;
import com.franckrj.respawnirc.NetworkBroadcastReceiver;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.AbsJVCTopicGetter;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public abstract class AbsShowTopicFragment extends AbsShowSomethingFragment {
    public static final String ARG_TOPIC_LINK = "com.franckrj.respawnirc.topic_link";
    public static final int MODE_IRC = 0;
    public static final int MODE_FORUM = 1;

    protected static final String SAVE_ALL_MESSAGES_SHOWED = "saveAllCurrentMessagesShowed";
    protected static final String SAVE_GO_TO_BOTTOM_PAGE_LOADING = "saveGoToBottomPageLoading";
    protected static final String SAVE_SETTINGS_PSEUDO_OF_AUTHOR = "saveSettingsPseudoOfAuthor";
    protected static final String SAVE_MESSAGES_ARE_FROM_IGNORED_PSEUDOS = "saveMessagesAreFromIgnoredPseudos";

    protected AbsJVCTopicGetter absGetterForTopic = null;
    protected TextView errorBackgroundMessage = null;
    protected ListView jvcMsgList = null;
    protected JVCTopicAdapter adapterForTopic = null;
    protected JVCParser.Settings currentSettings = new JVCParser.Settings();
    protected NewModeNeededListener listenerForNewModeNeeded = null;
    protected SwipeRefreshLayout swipeRefresh = null;
    protected ShareActionProvider shareAction = null;
    protected boolean allMessagesShowedAreFromIgnoredPseudos = false;
    protected PrefsManager.ShowImageType showNoelshackImageType = new PrefsManager.ShowImageType(PrefsManager.ShowImageType.ALWAYS);
    protected boolean showRefreshWhenMessagesShowed = true;
    protected boolean isInErrorMode = false;
    protected boolean cardDesignIsEnabled = false;
    protected boolean smoothScrollIsEnabled = true;
    protected boolean userIsConnectedAsModo = false;
    protected boolean hideTotallyMessagesOfIgnoredPseudos = true;

    protected final AbsJVCTopicGetter.NewGetterStateListener listenerForNewGetterState = new AbsJVCTopicGetter.NewGetterStateListener() {
        @Override
        public void newStateSetted(int newState) {
            if (showNoelshackImageType.type == PrefsManager.ShowImageType.WIFI_ONLY && newState == AbsJVCTopicGetter.STATE_LOADING) {
                updateSettingsDependingOnConnection();
            }

            if (newState == AbsJVCTopicGetter.STATE_LOADING) {
                errorBackgroundMessage.setVisibility(View.GONE);
            }

            if (showRefreshWhenMessagesShowed || adapterForTopic.getAllItems().isEmpty()) {
                if (newState == AbsJVCTopicGetter.STATE_LOADING) {
                    swipeRefresh.setRefreshing(true);
                } else if (newState == AbsJVCTopicGetter.STATE_NOT_LOADING) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        }
    };

    protected final AbsJVCTopicGetter.NewSurveyForTopic listenerForNewSurveyForTopic = new AbsJVCTopicGetter.NewSurveyForTopic() {
        @Override
        public void getNewSurveyTitle(String newTitle) {
            if (!newTitle.isEmpty()) {
                adapterForTopic.enableSurvey(newTitle);
            } else {
                adapterForTopic.disableSurvey();
            }
            adapterForTopic.notifyDataSetChanged();
        }
    };

    protected final AbsJVCTopicGetter.NewUserCanPostAsModoInfoAvailable listenerForNewUserCanPostAsModo = new AbsJVCTopicGetter.NewUserCanPostAsModoInfoAvailable() {
        @Override
        public void getNewUserCanPostAsModo(boolean newUserCanPostAsModo) {
            if (userIsConnectedAsModo) {
                adapterForTopic.setUserIsModo(newUserCanPostAsModo);
            } else {
                adapterForTopic.setUserIsModo(false);
            }
        }
    };

    protected final View.OnClickListener surveyItemClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (adapterForTopic.getShowSurvey()) {
                if (getActivity() instanceof NewSurveyNeedToBeShown) {
                    ((NewSurveyNeedToBeShown) getActivity()).getNewSurveyInfos(JVCParser.specialCharToNormalChar(absGetterForTopic.getSurveyTitleInHtml()), absGetterForTopic.getTopicId(), absGetterForTopic.getLatestAjaxInfos().list, absGetterForTopic.getListOfSurveyReplysWithInfos());
                }
            }
        }
    };

    protected void initializeSettings() {
        int avatarSizeInDP;
        int stickerSizeInDP;
        int miniNoelshackWidthInDP;

        try {
            avatarSizeInDP = Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.AVATAR_SIZE));
        } catch (Exception e) {
            avatarSizeInDP = -1;
        }
        try {
            stickerSizeInDP = Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.STICKER_SIZE));
        } catch (Exception e) {
            stickerSizeInDP = -1;
        }
        try {
            miniNoelshackWidthInDP = Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.MINI_NOELSHACK_WIDTH));
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

        currentSettings.maxNumberOfOverlyQuotes = Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.MAX_NUMBER_OF_OVERLY_QUOTE));
        currentSettings.transformStickerToSmiley = PrefsManager.getBool(PrefsManager.BoolPref.Names.TRANSFORM_STICKER_TO_SMILEY);
        currentSettings.shortenLongLink = PrefsManager.getBool(PrefsManager.BoolPref.Names.SHORTEN_LONG_LINK);
        currentSettings.hideUglyImages = PrefsManager.getBool(PrefsManager.BoolPref.Names.HIDE_UGLY_IMAGES);
        currentSettings.pseudoOfUser = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_USER);
        currentSettings.colorPseudoOfUserInInfoLine = PrefsManager.getBool(PrefsManager.BoolPref.Names.COLOR_PSEUDO_OF_USER_IN_INFO);
        currentSettings.colorPseudoOfUserInMessage = PrefsManager.getBool(PrefsManager.BoolPref.Names.COLOR_PSEUDO_OF_USER_IN_MESSAGE);
        absGetterForTopic.setCookieListInAString(PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST));
        smoothScrollIsEnabled = PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_SMOOTH_SCROLL);
        adapterForTopic.setShowSpoilDefault(PrefsManager.getBool(PrefsManager.BoolPref.Names.DEFAULT_SHOW_SPOIL_VAL));
        adapterForTopic.setFastRefreshOfImages(PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_FAST_REFRESH_OF_IMAGES));
        adapterForTopic.setColorDeletedMessages(PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_COLOR_DELETED_MESSAGES));
        userIsConnectedAsModo = PrefsManager.getBool(PrefsManager.BoolPref.Names.USER_IS_MODO);
        hideTotallyMessagesOfIgnoredPseudos = PrefsManager.getBool(PrefsManager.BoolPref.Names.HIDE_TOTALLY_MESSAGES_OF_IGNORED_PSEUDOS);
        adapterForTopic.setMessageFontSizeInSp(Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.MESSAGE_FONT_SIZE)));
        adapterForTopic.setMessageInfosFontSizeInSp(Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.MESSAGE_INFOS_FONT_SIZE)));
        adapterForTopic.setMessageSignatureFontSizeInSp(Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.MESSAGE_SIGNATURE_FONT_SIZE)));
    }

    protected void updateSettingsDependingOnConnection() {
        if (showNoelshackImageType.type != PrefsManager.ShowImageType.WIFI_ONLY) {
            currentSettings.showNoelshackImages = (showNoelshackImageType.type == PrefsManager.ShowImageType.ALWAYS);
        } else {
            currentSettings.showNoelshackImages = NetworkBroadcastReceiver.getIsConnectedWithWifi();
        }
    }

    protected boolean listIsScrolledAtBottom() {
        //noinspection SimplifiableIfStatement
        if (jvcMsgList.getChildCount() > 0) {
            return (jvcMsgList.getLastVisiblePosition() == jvcMsgList.getCount() - 1) &&
                    (jvcMsgList.getChildAt(jvcMsgList.getChildCount() - 1).getBottom() <= jvcMsgList.getHeight());
        }
        return true;
    }

    protected void setErrorBackgroundMessageDependingOnLastError() {
        int idOfErrorTextToShow;

        switch (absGetterForTopic.getLastTypeOfError()) {
            case TOPIC_DOES_NOT_EXIST:
                idOfErrorTextToShow = R.string.errorTopicDoesNotExist;
                break;
            case PAGE_DOES_NOT_EXIST:
                idOfErrorTextToShow = R.string.errorPageDoesNotExist;
                break;
            default:
                idOfErrorTextToShow = R.string.errorDownloadFailed;
                break;
        }

        if (adapterForTopic.getAllItems().isEmpty()) {
            errorBackgroundMessage.setText(idOfErrorTextToShow);
            errorBackgroundMessage.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(getActivity(), idOfErrorTextToShow, Toast.LENGTH_SHORT).show();
        }
    }

    protected void setErrorBackgroundMessageForAllMessageIgnored() {
        allMessagesShowedAreFromIgnoredPseudos = true;
        errorBackgroundMessage.setText(R.string.allMessagesAreFromIgnoredPseudo);
        errorBackgroundMessage.setVisibility(View.VISIBLE);
    }

    protected void updateShareAction() {
        if (shareAction != null && absGetterForTopic != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, absGetterForTopic.getUrlForTopic());
            shareIntent.setType("text/plain");
            shareAction.setShareIntent(shareIntent);
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        JVCParser.MessageInfos currentItem;
        switch (item.getItemId()) {
            case R.id.menu_show_spoil_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIdSelected());
                currentItem.listOfSpoilIdToShow.add(-1);
                adapterForTopic.updateThisItem(currentItem, false);
                adapterForTopic.notifyDataSetChanged();
                return true;
            case R.id.menu_hide_spoil_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIdSelected());
                currentItem.listOfSpoilIdToShow.clear();
                adapterForTopic.updateThisItem(currentItem, false);
                adapterForTopic.notifyDataSetChanged();
                return true;
            case R.id.menu_show_quote_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIdSelected());
                currentItem.showOverlyQuote = true;
                adapterForTopic.updateThisItem(currentItem, false);
                adapterForTopic.notifyDataSetChanged();
                return true;
            case R.id.menu_hide_quote_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIdSelected());
                currentItem.showOverlyQuote = false;
                adapterForTopic.updateThisItem(currentItem, false);
                adapterForTopic.notifyDataSetChanged();
                return true;
            case R.id.menu_show_ugly_images_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIdSelected());
                currentItem.showUglyImages = true;
                adapterForTopic.updateThisItem(currentItem, false);
                adapterForTopic.notifyDataSetChanged();
                return true;
            case R.id.menu_hide_ugly_images_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIdSelected());
                currentItem.showUglyImages = false;
                adapterForTopic.updateThisItem(currentItem, false);
                adapterForTopic.notifyDataSetChanged();
                return true;
            case R.id.menu_show_blacklisted_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIdSelected());
                currentItem.pseudoIsBlacklisted = false;
                adapterForTopic.updateThisItem(currentItem, false);
                adapterForTopic.notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }

    public void reloadTopic() {
        absGetterForTopic.reloadTopic();
    }

    public String getLatestListOfInputInAString(boolean tryToPostAsModo) {
        return absGetterForTopic.getLatestListOfInputInAString(tryToPostAsModo);
    }

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return absGetterForTopic.getLatestAjaxInfos();
    }

    public JVCParser.MessageInfos getCurrentItemSelected() {
        return adapterForTopic.getItem(adapterForTopic.getCurrentItemIdSelected());
    }

    public Boolean getIsInFavs() {
        return absGetterForTopic.getIsInFavs();
    }

    public String getSubId() {
        return absGetterForTopic.getSubId();
    }

    public boolean getUserCanLockTopic() {
        return absGetterForTopic.getUserCanLockTopic();
    }

    public String getTopicId() {
        return absGetterForTopic.getTopicId();
    }

    public boolean getUserCanPostAsModo() {
        return absGetterForTopic.getUserCanPostAsModo();
    }

    public void setIsInFavs(Boolean newVal) {
        absGetterForTopic.setIsInFavs(newVal);
    }

    public void setSubId(String newVal) {
        absGetterForTopic.setSubId(newVal);
    }

    public void setPseudoOfAuthor(String newPseudoOfAuthor) {
        currentSettings.pseudoOfAuthor = newPseudoOfAuthor;
    }

    public void ignoreThisPseudoFromListOfMessages(String pseudoToIgnore) {
        if (hideTotallyMessagesOfIgnoredPseudos) {
            adapterForTopic.removeItemsWithThisPseudo(pseudoToIgnore);
        } else {
            adapterForTopic.blacklistItemsWithThisPseudo(pseudoToIgnore);
        }
        adapterForTopic.notifyDataSetChanged();

        if (adapterForTopic.getAllItems().isEmpty()) {
            setErrorBackgroundMessageForAllMessageIgnored();
        }
    }

    @Override
    public void clearContent() {
        absGetterForTopic.stopAllCurrentTask();
        absGetterForTopic.resetDirectlyShowedInfos();
        adapterForTopic.disableSurvey();
        adapterForTopic.removeAllItems();
        adapterForTopic.notifyDataSetChanged();
        setPageLink("");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.fragment_showtopic, container, false);

        errorBackgroundMessage = mainView.findViewById(R.id.text_errorbackgroundmessage_showtopicfrag);
        jvcMsgList = mainView.findViewById(R.id.jvcmessage_view_showtopicfrag);
        swipeRefresh = mainView.findViewById(R.id.swiperefresh_showtopicfrag);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapterForTopic = new JVCTopicAdapter(getActivity(), currentSettings);
        initializeGetterForMessages();
        initializeAdapter();
        initializeSettings();
        absGetterForTopic.setListenerForNewGetterState(listenerForNewGetterState);
        absGetterForTopic.setListenerForNewSurveyForTopic(listenerForNewSurveyForTopic);
        absGetterForTopic.setListenerForNewUserCanPostAsModo(listenerForNewUserCanPostAsModo);
        adapterForTopic.setOnSurveyClickListener(surveyItemClickedListener);

        if (getActivity() instanceof NewModeNeededListener) {
            listenerForNewModeNeeded = (NewModeNeededListener) getActivity();
        }
        if (getActivity() instanceof AbsJVCTopicGetter.TopicLinkChanged) {
            absGetterForTopic.setListenerForTopicLinkChanged((AbsJVCTopicGetter.TopicLinkChanged) getActivity());
        }
        if (getActivity() instanceof AbsJVCTopicGetter.NewForumAndTopicNameAvailable) {
            absGetterForTopic.setListenerForNewForumAndTopicName((AbsJVCTopicGetter.NewForumAndTopicNameAvailable) getActivity());
        }
        if (getActivity() instanceof AbsJVCTopicGetter.NewReasonForTopicLock) {
            absGetterForTopic.setListenerForNewReasonForTopicLock((AbsJVCTopicGetter.NewReasonForTopicLock) getActivity());
        }
        if (getActivity() instanceof AbsJVCTopicGetter.NewPseudoOfAuthorAvailable) {
            absGetterForTopic.setListenerForNewPseudoOfAuthor((AbsJVCTopicGetter.NewPseudoOfAuthorAvailable) getActivity());
        }
        if (getActivity() instanceof PopupMenu.OnMenuItemClickListener) {
            adapterForTopic.setActionWhenItemMenuClicked((PopupMenu.OnMenuItemClickListener) getActivity());
        }
        if (getActivity() instanceof JVCTopicAdapter.URLClicked) {
            adapterForTopic.setUrlCLickedListener((JVCTopicAdapter.URLClicked) getActivity());
        }
        if (getActivity() instanceof JVCTopicAdapter.PseudoClicked) {
            adapterForTopic.setPseudoClickedListener((JVCTopicAdapter.PseudoClicked) getActivity());
        }

        errorBackgroundMessage.setVisibility(View.GONE);
        swipeRefresh.setColorSchemeResources(R.color.colorAccentThemeLight);
        if (cardDesignIsEnabled) {
            int paddingForMsgList = getResources().getDimensionPixelSize(R.dimen.spaceAroundSingleCard);
            int dividerSizeForMsgList = getResources().getDimensionPixelSize(R.dimen.spaceBetweenTwoCards);

            jvcMsgList.setPadding(paddingForMsgList, paddingForMsgList, paddingForMsgList, paddingForMsgList);
            jvcMsgList.setDivider(null);
            jvcMsgList.setDividerHeight(dividerSizeForMsgList);
        } else {
            jvcMsgList.setPadding(0, 0, 0, 3); //pour corriger un bug de smoothscroll
        }
        jvcMsgList.setClipToPadding(false);
        jvcMsgList.setAdapter(adapterForTopic);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.MessageInfos> allCurrentMessagesShowed = savedInstanceState.getParcelableArrayList(SAVE_ALL_MESSAGES_SHOWED);
            goToBottomAtPageLoading = savedInstanceState.getBoolean(SAVE_GO_TO_BOTTOM_PAGE_LOADING, false);
            currentSettings.pseudoOfAuthor = savedInstanceState.getString(SAVE_SETTINGS_PSEUDO_OF_AUTHOR, "");
            allMessagesShowedAreFromIgnoredPseudos = savedInstanceState.getBoolean(SAVE_MESSAGES_ARE_FROM_IGNORED_PSEUDOS, false);
            absGetterForTopic.loadFromBundle(savedInstanceState);

            if (userIsConnectedAsModo) {
                adapterForTopic.setUserIsModo(absGetterForTopic.getUserCanPostAsModo());
            }

            if (!Utils.stringIsEmptyOrNull(absGetterForTopic.getSurveyTitleInHtml())) {
                adapterForTopic.enableSurvey(absGetterForTopic.getSurveyTitleInHtml());
            }

            if (allCurrentMessagesShowed != null) {
                for (JVCParser.MessageInfos thisMessageInfo : allCurrentMessagesShowed) {
                    adapterForTopic.addItem(thisMessageInfo, false);
                }
            }

            adapterForTopic.notifyDataSetChanged();

            if (adapterForTopic.getAllItems().isEmpty() && allMessagesShowedAreFromIgnoredPseudos) {
                setErrorBackgroundMessageForAllMessageIgnored();
            }
        } else {
            Bundle currentArgs = getArguments();

            if (currentArgs != null) {
                setPageLink(currentArgs.getString(ARG_TOPIC_LINK, ""));
            }
        }

        setHasOptionsMenu(true);

        if (dontLoadOnFirstTime) {
            absGetterForTopic.stopAllCurrentTask();
            dontLoadOnFirstTime = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSettingsDependingOnConnection();
    }

    @Override
    public void onPause() {
        absGetterForTopic.stopAllCurrentTask();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_ALL_MESSAGES_SHOWED, adapterForTopic.getAllItems());
        outState.putBoolean(SAVE_GO_TO_BOTTOM_PAGE_LOADING, goToBottomAtPageLoading);
        outState.putString(SAVE_SETTINGS_PSEUDO_OF_AUTHOR, currentSettings.pseudoOfAuthor);
        outState.putBoolean(SAVE_MESSAGES_ARE_FROM_IGNORED_PSEUDOS, allMessagesShowedAreFromIgnoredPseudos);
        absGetterForTopic.saveToBundle(outState);
    }

    public interface NewModeNeededListener {
        void newModeRequested(int newMode);
    }

    public interface NewSurveyNeedToBeShown {
        void getNewSurveyInfos(String surveyTitle, String topicId, String ajaxInfos, ArrayList<JVCParser.SurveyReplyInfos> listOfReplysWithInfos);
    }

    protected abstract void initializeGetterForMessages();
    protected abstract void initializeAdapter();
}
