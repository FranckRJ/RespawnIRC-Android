package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.franckrj.respawnirc.AbsShowSomethingFragment;
import com.franckrj.respawnirc.NetworkBroadcastReceiver;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.AbsJVCTopicGetter;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public abstract class AbsShowTopicFragment extends AbsShowSomethingFragment {
    public static final String ARG_TOPIC_LINK = "com.franckrj.respawnirc.showtopicfragment.topic_link";
    public static final int MODE_IRC = 0;
    public static final int MODE_FORUM = 1;

    protected static final String SAVE_ALL_MESSAGES_SHOWED = "saveAllCurrentMessagesShowed";
    protected static final String SAVE_GO_TO_BOTTOM_PAGE_LOADING = "saveGoToBottomPageLoading";

    protected AbsJVCTopicGetter absGetterForTopic = null;
    protected ListView jvcMsgList = null;
    protected JVCTopicAdapter adapterForTopic = null;
    protected JVCParser.Settings currentSettings = new JVCParser.Settings();
    protected NewModeNeededListener listenerForNewModeNeeded = null;
    protected SwipeRefreshLayout swipeRefresh = null;
    protected int showNoelshackImageAdv = 1;
    protected boolean showRefreshWhenMessagesShowed = true;
    protected boolean isInErrorMode = false;
    protected boolean cardDesignIsEnabled = false;

    protected final AbsJVCTopicGetter.NewGetterStateListener listenerForNewGetterState = new AbsJVCTopicGetter.NewGetterStateListener() {
        @Override
        public void newStateSetted(int newState) {
            if (showNoelshackImageAdv == 1 && newState == AbsJVCTopicGetter.STATE_LOADING) {
                updateSettingsDependingOnConnection();
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
            adapterForTopic.updateAllItems();
        }
    };

    protected final View.OnClickListener surveyItemClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (adapterForTopic.getShowSurvey()) {
                if (getActivity() instanceof NewSurveyNeedToBeShown) {
                    ((NewSurveyNeedToBeShown) getActivity()).getNewSurveyInfos(JVCParser.specialCharToNormalChar(absGetterForTopic.getSurveyTitleInHtml()), absGetterForTopic.getTopicID(), absGetterForTopic.getLatestAjaxInfos().list);
                }
            }
        }
    };

    protected void reloadSettings() {
        try {
            showNoelshackImageAdv = Integer.valueOf(PrefsManager.getString(PrefsManager.StringPref.Names.SHOW_NOELSHACK_IMAGE));
        } catch (Exception e) {
            showNoelshackImageAdv = 1;
        }

        updateSettingsDependingOnConnection();
        currentSettings.maxNumberOfOverlyQuotes = Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.MAX_NUMBER_OF_OVERLY_QUOTE));
        currentSettings.transformStickerToSmiley = PrefsManager.getBool(PrefsManager.BoolPref.Names.TRANSFORM_STICKER_TO_SMILEY);
        currentSettings.shortenLongLink = PrefsManager.getBool(PrefsManager.BoolPref.Names.SHORTEN_LONG_LINK);
        currentSettings.hideUglyImages = PrefsManager.getBool(PrefsManager.BoolPref.Names.HIDE_UGLY_IMAGES);
        currentSettings.pseudoOfUser = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_USER);
        absGetterForTopic.setCookieListInAString(PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST));
    }

    protected void updateSettingsDependingOnConnection() {
        if (showNoelshackImageAdv != 1) {
            currentSettings.showNoelshackImages = (showNoelshackImageAdv == 0);
        } else {
            currentSettings.showNoelshackImages = (NetworkBroadcastReceiver.getIsConnectedToInternet() && NetworkBroadcastReceiver.getIsConnectedWithWifi());
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

    public boolean onMenuItemClick(MenuItem item) {
        JVCParser.MessageInfos currentItem;
        switch (item.getItemId()) {
            case R.id.menu_show_spoil_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIDSelected());
                currentItem.showSpoil = true;
                adapterForTopic.updateThisItem(currentItem);
                adapterForTopic.updateAllItems();
                return true;
            case R.id.menu_hide_spoil_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIDSelected());
                currentItem.showSpoil = false;
                adapterForTopic.updateThisItem(currentItem);
                adapterForTopic.updateAllItems();
                return true;
            case R.id.menu_show_quote_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIDSelected());
                currentItem.showOverlyQuote = true;
                adapterForTopic.updateThisItem(currentItem);
                adapterForTopic.updateAllItems();
                return true;
            case R.id.menu_hide_quote_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIDSelected());
                currentItem.showOverlyQuote = false;
                adapterForTopic.updateThisItem(currentItem);
                adapterForTopic.updateAllItems();
                return true;
            case R.id.menu_show_ugly_images_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIDSelected());
                currentItem.showUglyImages = true;
                adapterForTopic.updateThisItem(currentItem);
                adapterForTopic.updateAllItems();
                return true;
            case R.id.menu_hide_ugly_images_message:
                currentItem = adapterForTopic.getItem(adapterForTopic.getCurrentItemIDSelected());
                currentItem.showUglyImages = false;
                adapterForTopic.updateThisItem(currentItem);
                adapterForTopic.updateAllItems();
                return true;
            default:
                return false;
        }
    }

    public void reloadTopic() {
        absGetterForTopic.reloadTopic();
    }

    public String getLatestListOfInputInAString() {
        return absGetterForTopic.getLatestListOfInputInAString();
    }

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return absGetterForTopic.getLatestAjaxInfos();
    }

    public JVCParser.MessageInfos getCurrentItemSelected() {
        return adapterForTopic.getItem(adapterForTopic.getCurrentItemIDSelected());
    }

    public String getCurrentUrlOfTopic() {
        return absGetterForTopic.getUrlForTopic();
    }

    public Boolean getIsInFavs() {
        return absGetterForTopic.getIsInFavs();
    }

    public String getTopicID() {
        return absGetterForTopic.getTopicID();
    }

    public void setIsInFavs(Boolean newVal) {
        absGetterForTopic.setIsInFavs(newVal);
    }

    @Override
    public void clearContent() {
        absGetterForTopic.stopAllCurrentTask();
        absGetterForTopic.resetDirectlyShowedInfos();
        adapterForTopic.disableSurvey();
        adapterForTopic.removeAllItems();
        adapterForTopic.updateAllItems();
        setPageLink("");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.fragment_showtopic, container, false);

        jvcMsgList = (ListView) mainView.findViewById(R.id.jvcmessage_view_showtopicfrag);
        swipeRefresh = (SwipeRefreshLayout) mainView.findViewById(R.id.swiperefresh_showtopicfrag);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapterForTopic = new JVCTopicAdapter(getActivity(), currentSettings);
        initializeGetterForMessages();
        initializeAdapter();
        initializeSettings();
        reloadSettings();
        absGetterForTopic.setListenerForNewGetterState(listenerForNewGetterState);
        absGetterForTopic.setListenerForNewSurveyForTopic(listenerForNewSurveyForTopic);
        adapterForTopic.setOnSurveyClickListener(surveyItemClickedListener);

        if (getActivity() instanceof NewModeNeededListener) {
            listenerForNewModeNeeded = (NewModeNeededListener) getActivity();
        }
        if (getActivity() instanceof AbsJVCTopicGetter.NewForumAndTopicNameAvailable) {
            absGetterForTopic.setListenerForNewForumAndTopicName((AbsJVCTopicGetter.NewForumAndTopicNameAvailable) getActivity());
        }
        if (getActivity() instanceof AbsJVCTopicGetter.NewReasonForTopicLock) {
            absGetterForTopic.setListenerForNewReasonForTopicLock((AbsJVCTopicGetter.NewReasonForTopicLock) getActivity());
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

        swipeRefresh.setColorSchemeResources(R.color.colorAccentThemeLight);
        if (cardDesignIsEnabled) {
            int paddingForMsgList = getResources().getDimensionPixelSize(R.dimen.paddingOfMessageListView);
            jvcMsgList.setPadding(paddingForMsgList, paddingForMsgList, paddingForMsgList, paddingForMsgList);
            jvcMsgList.setClipToPadding(false);
            jvcMsgList.setDivider(null);
            jvcMsgList.setDividerHeight(0);
        }
        jvcMsgList.setAdapter(adapterForTopic);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.MessageInfos> allCurrentMessagesShowed = savedInstanceState.getParcelableArrayList(SAVE_ALL_MESSAGES_SHOWED);
            goToBottomAtPageLoading = savedInstanceState.getBoolean(SAVE_GO_TO_BOTTOM_PAGE_LOADING);
            absGetterForTopic.loadFromBundle(savedInstanceState);

            if (!Utils.stringIsEmptyOrNull(absGetterForTopic.getSurveyTitleInHtml())) {
                adapterForTopic.enableSurvey(absGetterForTopic.getSurveyTitleInHtml());
            }

            if (allCurrentMessagesShowed != null) {
                for (JVCParser.MessageInfos thisMessageInfo : allCurrentMessagesShowed) {
                    adapterForTopic.addItem(thisMessageInfo);
                }
            }

            adapterForTopic.updateAllItems();
        } else {
            Bundle currentArgs = getArguments();

            if (currentArgs != null) {
                setPageLink(currentArgs.getString(ARG_TOPIC_LINK, ""));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadSettings();
    }

    @Override
    public void onPause() {
        absGetterForTopic.stopAllCurrentTask();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_ALL_MESSAGES_SHOWED, adapterForTopic.getAllItems());
        outState.putBoolean(SAVE_GO_TO_BOTTOM_PAGE_LOADING, goToBottomAtPageLoading);
        absGetterForTopic.saveToBundle(outState);
    }

    public interface NewModeNeededListener {
        void newModeRequested(int newMode);
    }

    public interface NewSurveyNeedToBeShown {
        void getNewSurveyInfos(String surveyTitle, String topicID, String ajaxInfos);
    }

    protected abstract void initializeGetterForMessages();
    protected abstract void initializeSettings();
    protected abstract void initializeAdapter();
}
