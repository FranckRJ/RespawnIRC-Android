package com.franckrj.respawnirc.jvcmessagesviewers;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvcgetters.JVCForumMessageGetter;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class ShowTopicForumFragment extends AbsShowTopicFragment {
    private JVCForumMessageGetter getterForMessages = null;
    private Button firstPageButton = null;
    private Button previousPageButton = null;
    private Button currentPageButton = null;
    private Button nextPageButton = null;
    private Button lastPageButton = null;
    private int currentPage = 0;
    private int lastPage = 0;
    private boolean clearMessagesOnRefresh = true;

    private final JVCForumMessageGetter.NewMessagesListener listenerForNewMessages = new JVCForumMessageGetter.NewMessagesListener() {
        @Override
        public void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages) {
            if (!listOfNewMessages.isEmpty()) {
                boolean scrolledAtTheEnd = false;

                loadingLayout.setVisibility(View.GONE);
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
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final Button.OnClickListener changeCurrentTopicLinkListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            goToThisNewPage(baseForChangeTopicLink(), true);
        }
    };

    private final Button.OnClickListener changePageWithNavigationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (buttonView == firstPageButton && firstPageButton.getVisibility() == View.VISIBLE) {
                goToThisNewPage(JVCParser.setPageNumberForThisLink(getterForMessages.getUrlForTopic(), 1), false);
            } else if (buttonView == previousPageButton && previousPageButton.getVisibility() == View.VISIBLE) {
                goToThisNewPage(JVCParser.setPageNumberForThisLink(getterForMessages.getUrlForTopic(), currentPage - 1), false);
            } else if (buttonView == nextPageButton && nextPageButton.getVisibility() == View.VISIBLE) {
                goToThisNewPage(JVCParser.setPageNumberForThisLink(getterForMessages.getUrlForTopic(), currentPage + 1), false);
            } else if (buttonView == lastPageButton && lastPageButton.getVisibility() == View.VISIBLE) {
                goToThisNewPage(JVCParser.setPageNumberForThisLink(getterForMessages.getUrlForTopic(), lastPage), false);
            }
        }
    };

    private final JVCForumMessageGetter.NewNumbersOfPagesListener listenerForNewNumbersOfPages = new JVCForumMessageGetter.NewNumbersOfPagesListener() {
        @Override
        public void getNewLastPageNumber(String newNumber) {
            if (!newNumber.isEmpty()) {
                lastPage = Integer.parseInt(newNumber);
            } else {
                lastPage = currentPage;
            }
            updatePageButtons();
        }
    };

    private void goToThisNewPage(String newPageUrl, boolean updateLastPage) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        String currentPageNumber = JVCParser.getPageNumberForThisLink(newPageUrl);

        sharedPrefEdit.putString(getString(R.string.prefUrlToFetch), newPageUrl);
        sharedPrefEdit.apply();

        if (!currentPageNumber.isEmpty()) {
            currentPage = Integer.parseInt(currentPageNumber);
        } else {
            currentPage = 0;
        }
        if (updateLastPage) {
            lastPage = currentPage;
        }

        updatePageButtons();
        getterForMessages.stopAllCurrentTask();
        adapterForMessages.removeAllItems();
        adapterForMessages.updateAllItems();
        getterForMessages.startGetMessagesOfThisPage(newPageUrl);
        urlEdit.setText(newPageUrl);
    }

    private void updatePageButtons() {
        firstPageButton.setVisibility(View.GONE);
        previousPageButton.setVisibility(View.GONE);
        currentPageButton.setText(R.string.waitingText);
        nextPageButton.setVisibility(View.GONE);
        lastPageButton.setVisibility(View.GONE);

        if (currentPage > 0 && lastPage > 0) {
            currentPageButton.setText(String.valueOf(currentPage));

            if (currentPage > 1) {
                firstPageButton.setVisibility(View.VISIBLE);
                firstPageButton.setText(String.valueOf(1));
                previousPageButton.setVisibility(View.VISIBLE);
            }
            if (currentPage < lastPage) {
                nextPageButton.setVisibility(View.VISIBLE);
                lastPageButton.setVisibility(View.VISIBLE);
                lastPageButton.setText(String.valueOf(lastPage));
            }
        }
    }

    @Override
    protected void initializeGetterForMessages() {
        getterForMessages = new JVCForumMessageGetter(getActivity());
        absGetterForMessages = getterForMessages;
    }

    @Override
    protected void initializeSettings() {
        currentSettings.firstLineFormat = "<%PSEUDO_COLOR_START%><%PSEUDO_PSEUDO%><%PSEUDO_COLOR_END%> le <%DATE_COLOR_START%><%DATE_FULL%><%DATE_COLOR_END%>";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.fragment_showtopicforum, container, false);

        Button topicLinkButton = (Button) mainView.findViewById(R.id.topiclink_button_showtopicforum);

        jvcMsgList = (ListView) mainView.findViewById(R.id.jvcmessage_view_showtopicforum);
        urlEdit = (EditText) mainView.findViewById(R.id.topiclink_text_showtopicforum);
        messageSendEdit = (EditText) mainView.findViewById(R.id.sendmessage_text_showtopicforum);
        messageSendButton = (ImageButton) mainView.findViewById(R.id.sendmessage_button_showtopicforum);
        loadingLayout = mainView.findViewById(R.id.layout_loading_showtopicforum);
        firstPageButton = (Button) mainView.findViewById(R.id.firstpage_button_showtopicforum);
        previousPageButton = (Button) mainView.findViewById(R.id.previouspage_button_showtopicforum);
        currentPageButton = (Button) mainView.findViewById(R.id.currentpage_button_showtopicforum);
        nextPageButton = (Button) mainView.findViewById(R.id.nextpage_button_showtopicforum);
        lastPageButton = (Button) mainView.findViewById(R.id.lastpage_button_showtopicforum);

        firstPageButton.setVisibility(View.GONE);
        firstPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        previousPageButton.setVisibility(View.GONE);
        previousPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        nextPageButton.setVisibility(View.GONE);
        nextPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        lastPageButton.setVisibility(View.GONE);
        lastPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        topicLinkButton.setOnClickListener(changeCurrentTopicLinkListener);
        messageSendButton.setOnClickListener(sendMessageToTopicListener);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getterForMessages.setListenerForNewMessages(listenerForNewMessages);
        getterForMessages.setListenerForNewNumbersOfPages(listenerForNewNumbersOfPages);

        if (savedInstanceState != null) {
            currentPage = savedInstanceState.getInt(getString(R.string.saveCurrentPage));
            lastPage = savedInstanceState.getInt(getString(R.string.saveLastPage));
            updatePageButtons();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        clearMessagesOnRefresh = sharedPref.getBoolean(getString(R.string.settingsForumClearOnRefresh), Boolean.parseBoolean(getString(R.string.forumClearOnRefreshDefault)));

        if (adapterForMessages.getAllItems().isEmpty()) {
            goToThisNewPage(sharedPref.getString(getString(R.string.prefUrlToFetch), ""), true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getString(R.string.saveCurrentPage), currentPage);
        outState.putInt(getString(R.string.saveLastPage), lastPage);
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
                if (clearMessagesOnRefresh) {
                    adapterForMessages.removeAllItems();
                    adapterForMessages.updateAllItems();
                }
                getterForMessages.reloadTopic();
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