package com.franckrj.respawnirc.jvcmessagesviewers;

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

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvcgetters.JVCForumMessageGetter;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class ShowTopicForumFragment extends AbsShowTopicFragment {
    private JVCForumMessageGetter getterForMessages = null;

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
            }
        }
    };

    private final Button.OnClickListener changeCurrentTopicLinkListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            String newUrl = baseForChangeTopicLink();

            adapterForMessages.removeAllItems();
            adapterForMessages.updateAllItems();
            getterForMessages.startGetMessagesOfThisPage(newUrl);
        }
    };

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

        topicLinkButton.setOnClickListener(changeCurrentTopicLinkListener);
        messageSendButton.setOnClickListener(sendMessageToTopicListener);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getterForMessages.setListenerForNewMessages(listenerForNewMessages);

        urlEdit.setText(sharedPref.getString(getString(R.string.prefUrlToFetch), ""));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (adapterForMessages.getAllItems().isEmpty()) {
            getterForMessages.startGetMessagesOfThisPage(sharedPref.getString(getString(R.string.prefUrlToFetch), ""));
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
                adapterForMessages.removeAllItems();
                adapterForMessages.updateAllItems();
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