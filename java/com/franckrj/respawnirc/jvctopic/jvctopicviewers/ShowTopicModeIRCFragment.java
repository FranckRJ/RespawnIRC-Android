package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.franckrj.respawnirc.utils.IgnoreListManager;
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

    public static boolean getShowNavigationButtons() {
        return false;
    }

    private void saveOldTopicInfos() {
        if (!getterForTopic.getUrlForTopicPage().isEmpty()) {
            PrefsManager.putString(PrefsManager.StringPref.Names.OLD_URL_FOR_TOPIC, getterForTopic.getUrlForTopicPage());
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
        adapterForTopic.notifyDataSetChanged();
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
        adapterForTopic.notifyDataSetChanged();
        getterForTopic.setNewTopic(newTopicLink);
        getterForTopic.reloadTopic();
    }

    @Override
    public void clearContent(boolean deleteTemporaryInfos) {
        saveOldTopicInfos();
        super.clearContent(deleteTemporaryInfos);
    }

    @Override
    protected void initializeGetterForMessages() {
        getterForTopic = new JVCTopicModeIRCGetter(requireActivity());
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
        currentSettings.colorPseudoUser = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoUserColor, requireActivity()));
        currentSettings.colorPseudoOther = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoOtherModeIrcColor, requireActivity()));
        currentSettings.colorPseudoModo = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoModoColor, requireActivity()));
        currentSettings.colorPseudoAdmin = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoAdminColor, requireActivity()));
        currentSettings.applyMarkToPseudoAuthor = false;
        currentSettings.secondLineFormat = "<%MESSAGE_MESSAGE%>";
        currentSettings.addBeforeEdit = "";
        currentSettings.addAfterEdit = "";
        showRefreshWhenMessagesShowed = PrefsManager.getBool(PrefsManager.BoolPref.Names.TOPIC_SHOW_REFRESH_WHEN_MESSAGE_SHOWED_MODE_IRC);
        maxNumberOfMessagesShowed = PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.MAX_NUMBER_OF_MESSAGES);
        initialNumberOfMessagesShowed = PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.INITIAL_NUMBER_OF_MESSAGES);
        getterForTopic.setTimeBetweenRefreshTopic(PrefsManager.getStringAsInt(PrefsManager.StringPref.Names.REFRESH_TOPIC_TIME));
    }

    @Override
    public void processAddOfNewMessagesToJvcMsgList(ArrayList<JVCParser.MessageInfos> listOfNewMessages, boolean itsReallyEmpty, boolean dontShowMessages) {
        if (!listOfNewMessages.isEmpty()) {
            String pseudoOfUserInLC = currentSettings.pseudoOfUser.toLowerCase();
            boolean scrolledAtTheEnd = true;
            boolean needASmoothScroll = false;
            boolean firstTimeGetMessages = adapterForTopic.getAllItems().isEmpty();
            boolean anItemHasChanged = false;
            isInErrorMode = false;

            if (!adapterForTopic.getAllItems().isEmpty()) {
                scrolledAtTheEnd = listIsScrolledAtBottom();
                needASmoothScroll = scrolledAtTheEnd;
            }

            for (JVCParser.MessageInfos thisMessageInfo : listOfNewMessages) {
                String pseudoOfMessageInLC = thisMessageInfo.pseudo.toLowerCase();

                if (!pseudoOfMessageInLC.equals(pseudoOfUserInLC) && IgnoreListManager.pseudoInLCIsIgnored(pseudoOfMessageInLC)) {
                    if (hideTotallyMessagesOfIgnoredPseudos) {
                        continue;
                    } else {
                        thisMessageInfo.pseudoIsBlacklisted = true;
                    }
                }

                if (!thisMessageInfo.isAnEdit) {
                    adapterForTopic.addItem(thisMessageInfo, true);
                } else {
                    adapterForTopic.updateThisItem(thisMessageInfo, true);
                }
                anItemHasChanged = true;
            }

            while (adapterForTopic.getCount() > maxNumberOfMessagesShowed || (firstTimeGetMessages && adapterForTopic.getCount() > initialNumberOfMessagesShowed)) {
                adapterForTopic.removeFirstItem();
                anItemHasChanged = true;
            }

            if (anItemHasChanged) {
                adapterForTopic.notifyDataSetChanged();
            }

            if (adapterForTopic.getAllItems().isEmpty()) {
                setErrorBackgroundMessageForAllMessageIgnored();
            } else {
                allMessagesShowedAreFromIgnoredPseudos = false;

                if (scrolledAtTheEnd) {
                    if (smoothScrollIsEnabled && needASmoothScroll) { //s'il y avait des messages affichés avant et qu'on était en bas de page, smoothscroll
                        jvcMsgList.smoothScrollToPosition(adapterForTopic.getCount() - 1);
                    } else {
                        jvcMsgList.setSelection(adapterForTopic.getCount() - 1);
                    }
                }
            }
        } else if (itsReallyEmpty) {
            allMessagesShowedAreFromIgnoredPseudos = false;

            if (!isInErrorMode) {
                getterForTopic.reloadTopic(true);
                isInErrorMode = true;
            } else if (adapterForTopic.getAllItems().isEmpty()) {
                setErrorBackgroundMessageDependingOnLastError();
            }
        } else if (adapterForTopic.getAllItems().isEmpty() && allMessagesShowedAreFromIgnoredPseudos) {
            setErrorBackgroundMessageForAllMessageIgnored();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_OLD_URL_FOR_TOPIC, oldUrlForTopic);
        outState.putLong(SAVE_OLD_LAST_ID_OF_MESSAGE, oldLastIdOfMessage);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_showtopicirc, menu);
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share_showtopicboth));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_load_from_old_topic_info_showtopicirc).setEnabled(JVCParser.checkIfTopicAreSame(getterForTopic.getUrlForTopicPage(), oldUrlForTopic));
        menu.findItem(R.id.action_share_showtopicboth).setEnabled(!getterForTopic.getUrlForTopicPage().isEmpty());
        updateShareAction();
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
