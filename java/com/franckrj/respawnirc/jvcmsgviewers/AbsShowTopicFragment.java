package com.franckrj.respawnirc.jvcmsgviewers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.franckrj.respawnirc.jvcviewers.AbsShowSomethingFragment;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvcmsggetters.AbsJVCMessageGetter;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public abstract class AbsShowTopicFragment extends AbsShowSomethingFragment {
    public static final String ARG_TOPIC_LINK = "com.franckrj.respawnirc.showtopicfragment.topic_link";
    public static final int MODE_IRC = 0;
    public static final int MODE_FORUM = 1;

    protected AbsJVCMessageGetter absGetterForMessages = null;
    protected ListView jvcMsgList = null;
    protected SharedPreferences sharedPref = null;
    protected JVCMessagesAdapter adapterForMessages = null;
    protected JVCParser.Settings currentSettings = new JVCParser.Settings();
    protected NewModeNeededListener listenerForNewModeNeeded = null;
    protected SwipeRefreshLayout swipeRefresh = null;
    protected boolean showRefreshWhenMessagesShowed = true;
    protected boolean isInErrorMode = false;

    protected final AbsJVCMessageGetter.NewGetterStateListener listenerForNewGetterState = new AbsJVCMessageGetter.NewGetterStateListener() {
        @Override
        public void newStateSetted(int newState) {
            if (showRefreshWhenMessagesShowed || adapterForMessages.getAllItems().isEmpty()) {
                if (newState == AbsJVCMessageGetter.STATE_LOADING) {
                    swipeRefresh.setRefreshing(true);
                } else if (newState == AbsJVCMessageGetter.STATE_NOT_LOADING) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        }
    };

    protected void reloadSettings() {
        currentSettings.maxNumberOfOverlyQuotes = Integer.parseInt(sharedPref.getString(getString(R.string.settingsMaxNumberOfOverlyQuote), getString(R.string.maxNumberOfOverlyQuoteDefault)));
        currentSettings.showNoelshackImages = sharedPref.getBoolean(getString(R.string.settingsShowNoelshackImage), Boolean.parseBoolean(getString(R.string.showNoelshackImageDefault)));
        currentSettings.transformStickerToSmiley = sharedPref.getBoolean(getString(R.string.settingsTransformStickerToSmiley), Boolean.parseBoolean(getString(R.string.transformStickerToSmileyDefault)));
        currentSettings.pseudoOfUser = sharedPref.getString(getString(R.string.prefPseudoUser), "");
        absGetterForMessages.setCookieListInAString(sharedPref.getString(getString(R.string.prefCookiesList), ""));
    }

    public boolean onMenuItemClick(MenuItem item) {
        JVCParser.MessageInfos currentItem;
        switch (item.getItemId()) {
            case R.id.menu_show_spoil_message:
                currentItem = adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected());
                currentItem.showSpoil = true;
                adapterForMessages.updateThisItem(currentItem);
                adapterForMessages.updateAllItems();
                return true;
            case R.id.menu_hide_spoil_message:
                currentItem = adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected());
                currentItem.showSpoil = false;
                adapterForMessages.updateThisItem(currentItem);
                adapterForMessages.updateAllItems();
                return true;
            case R.id.menu_show_quote_message:
                currentItem = adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected());
                currentItem.showOverlyQuote = true;
                adapterForMessages.updateThisItem(currentItem);
                adapterForMessages.updateAllItems();
                return true;
            case R.id.menu_hide_quote_message:
                currentItem = adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected());
                currentItem.showOverlyQuote = false;
                adapterForMessages.updateThisItem(currentItem);
                adapterForMessages.updateAllItems();
                return true;
            default:
                return false;
        }
    }

    public void reloadTopic() {
        absGetterForMessages.reloadTopic();
    }

    public void clearContent() {
        absGetterForMessages.stopAllCurrentTask();
        adapterForMessages.removeAllItems();
        adapterForMessages.updateAllItems();
        setPageLink("");
    }

    public String getLatestListOfInputInAString() {
        return absGetterForMessages.getLatestListOfInputInAString();
    }

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return absGetterForMessages.getLatestAjaxInfos();
    }

    public JVCParser.MessageInfos getCurrentItemSelected() {
        return adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected());
    }

    public String getCurrentUrlOfTopic() {
        return absGetterForMessages.getUrlForTopic();
    }

    public Boolean getIsInFavs() {
        return absGetterForMessages.getIsInFavs();
    }

    public String getTopicID() {
        return absGetterForMessages.getTopicID();
    }

    public void setIsInFavs(Boolean newVal) {
        absGetterForMessages.setIsInFavs(newVal);
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

        adapterForMessages = new JVCMessagesAdapter(getActivity(), currentSettings);
        initializeGetterForMessages();
        initializeAdapter();
        initializeSettings();
        reloadSettings();
        absGetterForMessages.setListenerForNewGetterState(listenerForNewGetterState);

        if (getActivity() instanceof NewModeNeededListener) {
            listenerForNewModeNeeded = (NewModeNeededListener) getActivity();
        }
        if (getActivity() instanceof AbsJVCMessageGetter.NewForumAndTopicNameAvailable) {
            absGetterForMessages.setListenerForNewForumAndTopicName((AbsJVCMessageGetter.NewForumAndTopicNameAvailable) getActivity());
        }
        if (getActivity() instanceof AbsJVCMessageGetter.NewReasonForTopicLock) {
            absGetterForMessages.setListenerForNewReasonForTopicLock((AbsJVCMessageGetter.NewReasonForTopicLock) getActivity());
        }
        if (getActivity() instanceof PopupMenu.OnMenuItemClickListener) {
            adapterForMessages.setActionWhenItemMenuClicked((PopupMenu.OnMenuItemClickListener) getActivity());
        }
        if (getActivity() instanceof JVCMessagesAdapter.URLClicked) {
            adapterForMessages.setUrlCLickedListener((JVCMessagesAdapter.URLClicked) getActivity());
        }

        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        jvcMsgList.setAdapter(adapterForMessages);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.MessageInfos> allCurrentMessagesShowed = savedInstanceState.getParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed));
            absGetterForMessages.loadFromBundle(savedInstanceState);

            if (allCurrentMessagesShowed != null) {
                for (JVCParser.MessageInfos thisMessageInfo : allCurrentMessagesShowed) {
                    adapterForMessages.addItem(thisMessageInfo);
                }
            }

            adapterForMessages.updateAllItems();
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
        super.onPause();
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        absGetterForMessages.stopAllCurrentTask();
        sharedPrefEdit.putString(getString(R.string.prefOldUrlForTopic), absGetterForMessages.getUrlForTopic());
        sharedPrefEdit.putLong(getString(R.string.prefOldLastIdOfMessage), absGetterForMessages.getLastIdOfMessage());
        sharedPrefEdit.apply();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed), adapterForMessages.getAllItems());
        absGetterForMessages.saveToBundle(outState);
    }

    public interface NewModeNeededListener {
        void newModeRequested(int newMode);
    }

    protected abstract void initializeGetterForMessages();
    protected abstract void initializeSettings();
    protected abstract void initializeAdapter();
}
