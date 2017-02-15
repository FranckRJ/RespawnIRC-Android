package com.franckrj.respawnirc.jvctopic.jvctopicviewers;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvctopic.jvctopicgetters.JVCTopicModeForumGetter;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class ShowTopicModeForumFragment extends AbsShowTopicFragment {
    private JVCTopicModeForumGetter getterForTopic = null;
    private boolean clearMessagesOnRefresh = true;

    private final JVCTopicModeForumGetter.NewMessagesListener listenerForNewMessages = new JVCTopicModeForumGetter.NewMessagesListener() {
        @Override
        public void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages, boolean itsReallyEmpty) {
            if (!listOfNewMessages.isEmpty()) {
                boolean scrolledAtTheEnd = false;
                isInErrorMode = false;

                adapterForTopic.removeAllItems();

                if (jvcMsgList.getChildCount() > (adapterForTopic.getShowSurvey() ? 1 : 0)) {
                    scrolledAtTheEnd = (jvcMsgList.getLastVisiblePosition() == jvcMsgList.getCount() - 1) &&
                            (jvcMsgList.getChildAt(jvcMsgList.getChildCount() - 1).getBottom() <= jvcMsgList.getHeight());
                }

                for (JVCParser.MessageInfos thisMessageInfo : listOfNewMessages) {
                    adapterForTopic.addItem(thisMessageInfo);
                }

                adapterForTopic.updateAllItems();

                if ((scrolledAtTheEnd || goToBottomAtPageLoading) && jvcMsgList.getCount() > 0) {
                    jvcMsgList.setSelection(jvcMsgList.getCount() - 1);
                    goToBottomAtPageLoading = false;
                }

            } else {
                if (!isInErrorMode) {
                    getterForTopic.reloadTopic();
                    isInErrorMode = true;
                } else {
                    Toast.makeText(getActivity(), R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private final SwipeRefreshLayout.OnRefreshListener listenerForRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (!reloadAllTopic()) {
                swipeRefresh.setRefreshing(false);
            }
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
            adapterForTopic.updateAllItems();
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
        adapterForTopic.updateAllItems();
        getterForTopic.startGetMessagesOfThisPage(newTopicLink);
    }

    @Override
    protected void initializeGetterForMessages() {
        getterForTopic = new JVCTopicModeForumGetter();
        absGetterForTopic = getterForTopic;
    }

    @Override
    protected void initializeSettings() {
        showRefreshWhenMessagesShowed = true;
        currentSettings.firstLineFormat = "<b><%PSEUDO_COLOR_START%><%PSEUDO_PSEUDO%><%PSEUDO_COLOR_END%></b><br><small>Le <%DATE_COLOR_START%><%DATE_FULL%><%DATE_COLOR_END%></small>";
        currentSettings.colorPseudoUser = Utils.resColorToString(R.color.colorPseudoUser, getActivity());
        currentSettings.colorPseudoOther = "#80000000";
        currentSettings.colorPseudoModo = Utils.resColorToString(R.color.colorPseudoModo, getActivity());
        currentSettings.colorPseudoAdmin = Utils.resColorToString(R.color.colorPseudoAdmin, getActivity());
        currentSettings.secondLineFormat = "<%MESSAGE_MESSAGE%><%EDIT_ALL%>";
        currentSettings.addBeforeEdit = "<br /><br /><small><i>";
        currentSettings.addAfterEdit = "</i></small>";
    }

    @Override
    protected void initializeAdapter() {
        boolean showAvatars = sharedPref.getBoolean(getString(R.string.settingsShowAvatarModeForum), Boolean.parseBoolean(getString(R.string.showAvatarModeForumDefault)));

        if (showAvatars) {
            adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_avatars_rowforum);
            adapterForTopic.setShowAvatars(true);
        } else {
            adapterForTopic.setIdOfLayoutToUse(R.layout.jvcmessages_rowforum);
            adapterForTopic.setShowAvatars(false);
        }

        adapterForTopic.setAlternateBackgroundColor(sharedPref.getBoolean(getString(R.string.settingsTopicAlternateBackgroundColorModeForum), Boolean.parseBoolean(getString(R.string.topicAlternateBackgroundColorModeForumDefault))));
        adapterForTopic.setShowSignatures(sharedPref.getBoolean(getString(R.string.settingsShowSignatureModeForum), Boolean.parseBoolean(getString(R.string.showSignatureModeForumDefault))));
    }

    @Override
    protected void reloadSettings() {
        super.reloadSettings();
        clearMessagesOnRefresh = sharedPref.getBoolean(getString(R.string.settingsForumClearOnRefresh), Boolean.parseBoolean(getString(R.string.forumClearOnRefreshDefault)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.fragment_showtopicforum, container, false);

        jvcMsgList = (ListView) mainView.findViewById(R.id.jvcmessage_view_showtopicforum);
        swipeRefresh = (SwipeRefreshLayout) mainView.findViewById(R.id.swiperefresh_showtopicforum);

        swipeRefresh.setOnRefreshListener(listenerForRefresh);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getterForTopic.setListenerForNewMessages(listenerForNewMessages);

        if (getActivity() instanceof JVCTopicModeForumGetter.NewNumbersOfPagesListener) {
            getterForTopic.setListenerForNewNumbersOfPages((JVCTopicModeForumGetter.NewNumbersOfPagesListener) getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isInErrorMode = false;

        if (adapterForTopic.getAllItems().isEmpty()) {
            getterForTopic.reloadTopic();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_showtopicforum, menu);
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
