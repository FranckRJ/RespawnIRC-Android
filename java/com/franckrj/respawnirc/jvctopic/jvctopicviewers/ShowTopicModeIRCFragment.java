package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.JVCTopicModeIRCGetter;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class ShowTopicModeIRCFragment extends AbsShowTopicFragment {
    private static final String SAVE_OLD_URL_FOR_TOPIC = "saveOldUrlForTopic";
    private static final String SAVE_OLD_LAST_ID_OF_MESSAGE = "saveOldLastIdOfMessage";

    private int maxNumberOfMessagesShowed = 40;
    private int initialNumberOfMessagesShowed = 10;
    private JVCTopicModeIRCGetter getterForTopic = null;
    private String oldUrlForTopic = "";
    private long oldLastIdOfMessage = 0;

    private final JVCTopicModeIRCGetter.NewMessagesListener listenerForNewMessages = new JVCTopicModeIRCGetter.NewMessagesListener() {
        @Override
        public void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages, boolean itsReallyEmpty) {
            if (!listOfNewMessages.isEmpty()) {
                boolean scrolledAtTheEnd = true;
                boolean needASmoothScroll = false;
                boolean firstTimeGetMessages = adapterForTopic.getAllItems().isEmpty();
                isInErrorMode = false;

                if (adapterForTopic.getCount() > (adapterForTopic.getShowSurvey() ? 1 : 0)) {
                    scrolledAtTheEnd = listIsScrolledAtBottom();
                    needASmoothScroll = scrolledAtTheEnd;
                }

                for (JVCParser.MessageInfos thisMessageInfo : listOfNewMessages) {
                    if (!thisMessageInfo.isAnEdit) {
                        adapterForTopic.addItem(thisMessageInfo, true);
                    } else {
                        adapterForTopic.updateThisItem(thisMessageInfo, true);
                    }
                }

                if (firstTimeGetMessages) {
                    while (adapterForTopic.getCount() > initialNumberOfMessagesShowed) {
                        adapterForTopic.removeFirstItem();
                    }
                }

                while (adapterForTopic.getCount() > maxNumberOfMessagesShowed) {
                    adapterForTopic.removeFirstItem();
                }

                adapterForTopic.updateAllItems();

                if (scrolledAtTheEnd && adapterForTopic.getCount() > 0) {
                    if (smoothScrollIsEnabled && needASmoothScroll) { //s'il y avait des messages affichés avant et qu'on était en bas de page, smoothscroll
                        jvcMsgList.smoothScrollToPosition(adapterForTopic.getCount() - 1);
                    } else {
                        jvcMsgList.setSelection(adapterForTopic.getCount() - 1);
                    }
                }
            } else if (itsReallyEmpty) {
                if (!isInErrorMode) {
                    getterForTopic.reloadTopic(true);
                    isInErrorMode = true;
                } else if (adapterForTopic.getAllItems().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    public static boolean getShowNavigationButtons() {
        return false;
    }

    private void saveOldTopicInfos() {
        if (!getterForTopic.getUrlForTopic().isEmpty()) {
            PrefsManager.putString(PrefsManager.StringPref.Names.OLD_URL_FOR_TOPIC, getterForTopic.getUrlForTopic());
            PrefsManager.putLong(PrefsManager.LongPref.Names.OLD_LAST_ID_OF_MESSAGE, getterForTopic.getLastIdOfMessage());
            PrefsManager.applyChanges();
        }
    }

    private void loadFromOldTopicInfos() {
        isInErrorMode = false;
        getterForTopic.stopAllCurrentTask();
        getterForTopic.resetDirectlyShowedInfos();
        adapterForTopic.disableSurvey();
        adapterForTopic.removeAllItems();
        adapterForTopic.updateAllItems();
        getterForTopic.setOldTopic(oldUrlForTopic, oldLastIdOfMessage);
        getterForTopic.reloadTopic();
    }

    @Override
    public void setPageLink(String newTopicLink) {
        isInErrorMode = false;

        getterForTopic.stopAllCurrentTask();
        getterForTopic.resetDirectlyShowedInfos();
        adapterForTopic.disableSurvey();
        adapterForTopic.removeAllItems();
        adapterForTopic.updateAllItems();
        getterForTopic.setNewTopic(newTopicLink);
        getterForTopic.reloadTopic();
    }

    @Override
    public void clearContent() {
        saveOldTopicInfos();
        super.clearContent();
    }

    @Override
    protected void initializeGetterForMessages() {
        getterForTopic = new JVCTopicModeIRCGetter(getActivity());
        absGetterForTopic = getterForTopic;
    }

    @Override
    protected void initializeAdapter() {
        adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_rowirc);
        adapterForTopic.setAlternateBackgroundColor(PrefsManager.getBool(PrefsManager.BoolPref.Names.TOPIC_ALTERNATE_BACKGROUND_MODE_IRC));
        adapterForTopic.setShowSignatures(PrefsManager.getBool(PrefsManager.BoolPref.Names.SHOW_SIGNATURE_MODE_IRC));
        adapterForTopic.setShowAvatars(false);
    }

    @Override
    protected void initializeSettings() {
        super.initializeSettings();
        currentSettings.firstLineFormat = "[<%DATE_COLOR_START%><%DATE_TIME%><%DATE_COLOR_END%>] &lt;<%PSEUDO_COLOR_START%><%PSEUDO_PSEUDO%><%PSEUDO_COLOR_END%>&gt;";
        currentSettings.colorPseudoUser = Utils.resColorToString(ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PSEUDO_USER), getActivity());
        currentSettings.colorPseudoOther = Utils.resColorToString(ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PSEUDO_OTHER_MODE_IRC), getActivity());
        currentSettings.colorPseudoModo = Utils.resColorToString(ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PSEUDO_MODO), getActivity());
        currentSettings.colorPseudoAdmin = Utils.resColorToString(ThemeManager.getColorRes(ThemeManager.ColorName.COLOR_PSEUDO_ADMIN), getActivity());
        currentSettings.applyMarkToPseudoAuthor = false;
        currentSettings.secondLineFormat = "<%MESSAGE_MESSAGE%>";
        currentSettings.addBeforeEdit = "";
        currentSettings.addAfterEdit = "";
        showRefreshWhenMessagesShowed = PrefsManager.getBool(PrefsManager.BoolPref.Names.TOPIC_SHOW_REFRESH_WHEN_MESSAGE_SHOWED_MODE_IRC);
        maxNumberOfMessagesShowed = Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.MAX_NUMBER_OF_MESSAGES));
        initialNumberOfMessagesShowed = Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.INITIAL_NUMBER_OF_MESSAGES));
        getterForTopic.setTimeBetweenRefreshTopic(Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.REFRESH_TOPIC_TIME)));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getterForTopic.setListenerForNewMessages(listenerForNewMessages);
        swipeRefresh.setEnabled(false);

        if (savedInstanceState != null) {
            oldUrlForTopic = savedInstanceState.getString(SAVE_OLD_URL_FOR_TOPIC, "");
            oldLastIdOfMessage = savedInstanceState.getLong(SAVE_OLD_LAST_ID_OF_MESSAGE, 0);
        } else {
            oldUrlForTopic = PrefsManager.getString(PrefsManager.StringPref.Names.OLD_URL_FOR_TOPIC);
            oldLastIdOfMessage = PrefsManager.getLong(PrefsManager.LongPref.Names.OLD_LAST_ID_OF_MESSAGE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isInErrorMode = false;
        getterForTopic.reloadTopic();
    }

    @Override
    public void onPause() {
        saveOldTopicInfos();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_OLD_URL_FOR_TOPIC, oldUrlForTopic);
        outState.putLong(SAVE_OLD_LAST_ID_OF_MESSAGE, oldLastIdOfMessage);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_showtopicirc, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_load_from_old_topic_info_showtopicirc).setEnabled(JVCParser.checkIfTopicAreSame(getterForTopic.getUrlForTopic(), oldUrlForTopic));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load_from_old_topic_info_showtopicirc:
                loadFromOldTopicInfos();
                return true;
            case R.id.action_switch_to_forum_mode_showtopicirc:
                if (listenerForNewModeNeeded != null) {
                    listenerForNewModeNeeded.newModeRequested(MODE_FORUM);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
