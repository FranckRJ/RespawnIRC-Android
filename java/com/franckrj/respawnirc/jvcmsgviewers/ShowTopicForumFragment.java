package com.franckrj.respawnirc.jvcmsgviewers;

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
import com.franckrj.respawnirc.jvcmsggetters.JVCForumMessageGetter;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;

public class ShowTopicForumFragment extends AbsShowTopicFragment {
    private JVCForumMessageGetter getterForMessages = null;
    private boolean clearMessagesOnRefresh = true;

    private final JVCForumMessageGetter.NewMessagesListener listenerForNewMessages = new JVCForumMessageGetter.NewMessagesListener() {
        @Override
        public void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages) {
            if (!listOfNewMessages.isEmpty()) {
                boolean scrolledAtTheEnd = false;
                isInErrorMode = false;

                adapterForMessages.removeAllItems();

                if (jvcMsgList.getChildCount() > 0) {
                    scrolledAtTheEnd = (jvcMsgList.getLastVisiblePosition() == jvcMsgList.getCount() - 1) &&
                            (jvcMsgList.getChildAt(jvcMsgList.getChildCount() - 1).getBottom() <= jvcMsgList.getHeight());
                }

                for (JVCParser.MessageInfos thisMessageInfo : listOfNewMessages) {
                    adapterForMessages.addItem(thisMessageInfo);
                }

                adapterForMessages.updateAllItems();

                if (scrolledAtTheEnd && jvcMsgList.getCount() > 0) {
                    jvcMsgList.setSelection(jvcMsgList.getCount() - 1);
                }
            } else {
                if (!isInErrorMode) {
                    getterForMessages.reloadTopic();
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
            adapterForMessages.removeAllItems();
            adapterForMessages.updateAllItems();
        }
        return getterForMessages.reloadTopic();
    }

    @Override
    public void setPageLink(String newTopicLink) {
        isInErrorMode = false;

        getterForMessages.stopAllCurrentTask();
        adapterForMessages.removeAllItems();
        adapterForMessages.updateAllItems();
        getterForMessages.startGetMessagesOfThisPage(newTopicLink);
    }

    @Override
    protected void initializeGetterForMessages() {
        getterForMessages = new JVCForumMessageGetter(getActivity());
        absGetterForMessages = getterForMessages;
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
        adapterForMessages.setIdOfLayoutToUse(R.layout.jvcmessages_rowforum);
    }

    @Override
    protected void reloadSettings() {
        super.reloadSettings();
        clearMessagesOnRefresh = sharedPref.getBoolean(getString(R.string.settingsForumClearOnRefresh), Boolean.parseBoolean(getString(R.string.forumClearOnRefreshDefault)));
        adapterForMessages.setAlternateBackgroundColor(sharedPref.getBoolean(getString(R.string.settingsForumAlternateBackgroundColor), Boolean.parseBoolean(getString(R.string.forumAlternateBackgroundColorDefault))));
        adapterForMessages.updateAllItems();
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

        getterForMessages.setListenerForNewMessages(listenerForNewMessages);

        if (getActivity() instanceof JVCForumMessageGetter.NewNumbersOfPagesListener) {
            getterForMessages.setListenerForNewNumbersOfPages((JVCForumMessageGetter.NewNumbersOfPagesListener) getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isInErrorMode = false;

        if (adapterForMessages.getAllItems().isEmpty()) {
            getterForMessages.reloadTopic();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
