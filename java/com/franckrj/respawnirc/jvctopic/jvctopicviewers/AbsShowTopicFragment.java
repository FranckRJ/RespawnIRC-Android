package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.franckrj.respawnirc.AbsShowSomethingFragment;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.AbsJVCTopicGetter;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public abstract class AbsShowTopicFragment extends AbsShowSomethingFragment {
    public static final String ARG_TOPIC_LINK = "com.franckrj.respawnirc.showtopicfragment.topic_link";
    public static final int MODE_IRC = 0;
    public static final int MODE_FORUM = 1;

    protected static final String SAVE_ALL_MESSAGES_SHOWED = "saveAllCurrentMessagesShowed";

    protected AbsJVCTopicGetter absGetterForTopic = null;
    protected ListView jvcMsgList = null;
    protected SharedPreferences sharedPref = null;
    protected JVCTopicAdapter adapterForTopic = null;
    protected JVCParser.Settings currentSettings = new JVCParser.Settings();
    protected NewModeNeededListener listenerForNewModeNeeded = null;
    protected SwipeRefreshLayout swipeRefresh = null;
    protected boolean showRefreshWhenMessagesShowed = true;
    protected boolean isInErrorMode = false;

    protected final AbsJVCTopicGetter.NewGetterStateListener listenerForNewGetterState = new AbsJVCTopicGetter.NewGetterStateListener() {
        @Override
        public void newStateSetted(int newState) {
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

    private final View.OnClickListener surveyItemClickedListener = new View.OnClickListener() {
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
        currentSettings.maxNumberOfOverlyQuotes = Integer.parseInt(sharedPref.getString(getString(R.string.settingsMaxNumberOfOverlyQuote), getString(R.string.maxNumberOfOverlyQuoteDefault)));
        currentSettings.showNoelshackImages = sharedPref.getBoolean(getString(R.string.settingsShowNoelshackImage), Boolean.parseBoolean(getString(R.string.showNoelshackImageDefault)));
        currentSettings.transformStickerToSmiley = sharedPref.getBoolean(getString(R.string.settingsTransformStickerToSmiley), Boolean.parseBoolean(getString(R.string.transformStickerToSmileyDefault)));
        currentSettings.pseudoOfUser = sharedPref.getString(getString(R.string.prefPseudoUser), "");
        absGetterForTopic.setCookieListInAString(sharedPref.getString(getString(R.string.prefCookiesList), ""));
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

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

        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        jvcMsgList.setAdapter(adapterForTopic);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.MessageInfos> allCurrentMessagesShowed = savedInstanceState.getParcelableArrayList(SAVE_ALL_MESSAGES_SHOWED);
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
                String topicLink = currentArgs.getString(ARG_TOPIC_LINK, "");
                setPageLink(topicLink);
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
