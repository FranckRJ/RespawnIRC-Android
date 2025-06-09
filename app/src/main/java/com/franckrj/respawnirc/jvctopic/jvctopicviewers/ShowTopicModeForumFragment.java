package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import static com.franckrj.respawnirc.utils.WebManager.errorStringId;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.franckrj.respawnirc.utils.IgnoreListManager;
import com.franckrj.respawnirc.NetworkBroadcastReceiver;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.JVCTopicModeForumGetter;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class ShowTopicModeForumFragment extends AbsShowTopicFragment {
    private JVCTopicModeForumGetter getterForTopic = null;
    private boolean clearMessagesOnRefresh = true;
    private boolean autoScrollIsEnabled = true;

    private final SwipeRefreshLayout.OnRefreshListener listenerForRefresh = () -> {
        if (!reloadAllTopic()) {
            swipeRefresh.setRefreshing(false);
        }
    };

    public static boolean getShowNavigationButtons() {
        return true;
    }

    private boolean reloadAllTopic() {
        isInErrorMode = false;
        if (clearMessagesOnRefresh) {
            getterForTopic.resetDirectlyShowedInfos();
            adapterForTopic.disableSurvey();
            adapterForTopic.removeAllItems();
            adapterForTopic.notifyDataSetChanged();
        }
        return getterForTopic.reloadTopic();
    }

    @Override
    public void setPageLink(String newTopicLink) {
        isInErrorMode = false;

        getterForTopic.stopAllCurrentTask();
        getterForTopic.resetDirectlyShowedInfos();
        adapterForTopic.disableSurvey();
        adapterForTopic.removeAllItems();
        adapterForTopic.notifyDataSetChanged();
        getterForTopic.startGetMessagesOfThisPage(newTopicLink);
    }

    @Override
    protected void initializeGetterForMessages() {
        getterForTopic = new JVCTopicModeForumGetter();
        absGetterForTopic = getterForTopic;
    }

    @Override
    protected void initializeAdapter() {
        PrefsManager.ShowImageType showAvatarType = new PrefsManager.ShowImageType(PrefsManager.ShowImageType.ALWAYS);

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
    }

    @Override
    protected void initializeSettings() {
        super.initializeSettings();
        showRefreshWhenMessagesShowed = true;
        currentSettings.firstLineFormat = "<b><%PSEUDO_COLOR_START%><%PSEUDO_PSEUDO%><%PSEUDO_COLOR_END%></b><small><%NIVEAU_BLOCK%><%MARK_FOR_PSEUDO%><br>Le <%DATE_COLOR_START%><%DATE_FULL%><%DATE_COLOR_END%></small>";
        currentSettings.colorPseudoUser = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoUserColor, requireActivity()));
        currentSettings.colorPseudoOther = Utils.colorToStringWithAlpha(ThemeManager.getColorInt(R.attr.themedPseudoOtherModeForumColor, requireActivity()));
        currentSettings.colorPseudoModo = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoModoColor, requireActivity()));
        currentSettings.colorPseudoAdmin = Utils.colorToString(ThemeManager.getColorInt(R.attr.themedPseudoAdminColor, requireActivity()));
        currentSettings.secondLineFormat = "<%MESSAGE_MESSAGE%><%EDIT_ALL%>";
        currentSettings.addBeforeEdit = "<br /><br /><small><i>";
        currentSettings.addAfterEdit = "</i></small>";
        currentSettings.applyMarkToPseudoAuthor = PrefsManager.getBool(PrefsManager.BoolPref.Names.MARK_AUTHOR_PSEUDO_MODE_FORUM);
        clearMessagesOnRefresh = PrefsManager.getBool(PrefsManager.BoolPref.Names.TOPIC_CLEAR_ON_REFRESH_MODE_FORUM);
        autoScrollIsEnabled = PrefsManager.getBool(PrefsManager.BoolPref.Names.ENABLE_AUTO_SCROLL_MODE_FORUM);
    }

    @Override
    public void processAddOfNewMessagesToJvcMsgList(ArrayList<JVCParser.MessageInfos> listOfNewMessages, boolean itsReallyEmpty, boolean dontShowMessages) {
        if (dontShowMessages) {
            isInErrorMode = false;
            allMessagesShowedAreFromIgnoredPseudos = false;
            goToBottomAtPageLoading = false;
            anchorForNextLoad = null;
        } else if (!listOfNewMessages.isEmpty()) {
            String pseudoOfUserInLC = currentSettings.pseudoOfUser.toLowerCase();
            boolean scrolledAtTheEnd = false;
            isInErrorMode = false;

            if (!adapterForTopic.getAllItems().isEmpty()) {
                scrolledAtTheEnd = listIsScrolledAtBottom();
            }

            adapterForTopic.removeAllItems();

            for (JVCParser.MessageInfos thisMessageInfo : listOfNewMessages) {
                String pseudoOfMessageInLC = thisMessageInfo.pseudo.toLowerCase();

                if (!pseudoOfMessageInLC.equals(pseudoOfUserInLC) && IgnoreListManager.pseudoInLCIsIgnored(pseudoOfMessageInLC)) {
                    if (hideTotallyMessagesOfIgnoredPseudos) {
                        continue;
                    } else {
                        thisMessageInfo.pseudoIsBlacklisted = true;
                    }
                }

                adapterForTopic.addItem(thisMessageInfo, true);
            }

            adapterForTopic.notifyDataSetChanged();

            if (adapterForTopic.getAllItems().isEmpty()) {
                setErrorBackgroundMessageForAllMessageIgnored();
            } else {
                allMessagesShowedAreFromIgnoredPseudos = false;

                if (anchorForNextLoad != null) {
                    int positionOfAnchor;

                    try {
                        long anchorValue = Long.parseLong(anchorForNextLoad);
                        positionOfAnchor = adapterForTopic.getPositionOfMessageId(anchorValue);
                    } catch (Exception e) {
                        positionOfAnchor = -1;
                    }

                    if (positionOfAnchor > 0) {
                        jvcMsgList.setSelection(positionOfAnchor);
                    }
                } else if (goToBottomAtPageLoading || (autoScrollIsEnabled && scrolledAtTheEnd)) {
                    if (smoothScrollIsEnabled && scrolledAtTheEnd) { //s'il y avait des messages affichés avant et qu'on était en bas de page, smoothscroll
                        jvcMsgList.smoothScrollToPosition(adapterForTopic.getCount() - 1);
                    } else {
                        jvcMsgList.setSelection(adapterForTopic.getCount() - 1);
                    }
                }
            }
            goToBottomAtPageLoading = false;
            anchorForNextLoad = null;
        } else {
            allMessagesShowedAreFromIgnoredPseudos = false;

            if (!isInErrorMode) {
                getterForTopic.reloadTopic(true);
                isInErrorMode = true;
            } else {
                setErrorBackgroundMessageDependingOnLastError();
                if(errorStringId == R.string.errorCloudflare)
                {
                    // On ouvre le navigateur interne pour le captcha CloudFlare.
                    Utils.openCloudflarePage("https://www.jeuxvideo.com/forums.htm", getActivity());
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeRefresh.setOnRefreshListener(listenerForRefresh);

        if (getActivity() instanceof JVCTopicModeForumGetter.NewNumbersOfPagesListener) {
            getterForTopic.setListenerForNewNumbersOfPages((JVCTopicModeForumGetter.NewNumbersOfPagesListener) getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isInErrorMode = false;

        if (adapterForTopic.getAllItems().isEmpty() && !allMessagesShowedAreFromIgnoredPseudos) {
            getterForTopic.reloadTopic();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_showtopicforum, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_share_showtopicboth).setEnabled(!getterForTopic.getUrlForTopicPage().isEmpty());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload_topic_showtopicforum:
                reloadAllTopic();
                return true;
            case R.id.action_switch_to_IRC_mode_showtopicforum:
                if (listenerForNewModeNeeded != null) {
                    listenerForNewModeNeeded.newModeRequested(MODE_IRC);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
