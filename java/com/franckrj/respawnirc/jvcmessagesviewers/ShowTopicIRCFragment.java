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
import com.franckrj.respawnirc.jvcgetters.JVCIRCMessageGetter;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

/*TODO: Set focus sur la zone d'écriture des messages après citation ?
* TODO: géré la redirection de lien (changement de nom de topic, suppression de page, etc)
* TODO: Récupérer les deux dernières page si la dernière page contient moins de X messages (et au 1er chargement aussi ?)
* TODO: http://stackoverflow.com/questions/5312592/how-can-i-get-my-listview-to-scroll pour pouvoir scroll le lien du topic ?*/
public class ShowTopicIRCFragment extends AbsShowTopicFragment {
    private int maxNumberOfMessagesShowed = 40;
    private int initialNumberOfMessagesShowed = 10;
    private JVCIRCMessageGetter getterForMessages = null;
    private String oldUrlForTopic = "";
    private long oldLastIdOfMessage = 0;

    private final JVCIRCMessageGetter.NewMessagesListener listenerForNewMessages = new JVCIRCMessageGetter.NewMessagesListener() {
        @Override
        public void getNewMessages(ArrayList<JVCParser.MessageInfos> listOfNewMessages) {
            if (!listOfNewMessages.isEmpty()) {
                boolean scrolledAtTheEnd = true;
                boolean firstTimeGetMessages = adapterForMessages.getAllItems().isEmpty();

                loadingLayout.setVisibility(View.GONE);

                if (jvcMsgList.getChildCount() > 0) {
                    scrolledAtTheEnd = (jvcMsgList.getLastVisiblePosition() == jvcMsgList.getCount() - 1) &&
                            (jvcMsgList.getChildAt(jvcMsgList.getChildCount() - 1).getBottom() <= jvcMsgList.getHeight());
                }

                for (JVCParser.MessageInfos thisMessageInfo : listOfNewMessages) {
                    if (!thisMessageInfo.isAnEdit) {
                        adapterForMessages.addItem(thisMessageInfo);
                    } else {
                        adapterForMessages.updateThisItem(thisMessageInfo);
                    }
                }

                if (firstTimeGetMessages) {
                    while (adapterForMessages.getCount() > initialNumberOfMessagesShowed) {
                        adapterForMessages.removeFirstItem();
                    }
                }

                while (adapterForMessages.getCount() > maxNumberOfMessagesShowed) {
                    adapterForMessages.removeFirstItem();
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

            getterForMessages.stopAllCurrentTask();
            adapterForMessages.removeAllItems();
            adapterForMessages.updateAllItems();
            getterForMessages.setNewTopic(newUrl, true);
            getterForMessages.reloadTopic();
        }
    };

    private void loadFromOldTopicInfos() {
        getterForMessages.stopAllCurrentTask();
        adapterForMessages.removeAllItems();
        adapterForMessages.updateAllItems();
        getterForMessages.setOldTopic(oldUrlForTopic, oldLastIdOfMessage);
        getterForMessages.reloadTopic();
    }

    @Override
    protected void initializeGetterForMessages() {
        getterForMessages = new JVCIRCMessageGetter(getActivity());
        absGetterForMessages = getterForMessages;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.fragment_showtopicirc, container, false);

        Button topicLinkButton = (Button) mainView.findViewById(R.id.topiclink_button_showtopicirc);

        jvcMsgList = (ListView) mainView.findViewById(R.id.jvcmessage_view_showtopicirc);
        urlEdit = (EditText) mainView.findViewById(R.id.topiclink_text_showtopicirc);
        messageSendEdit = (EditText) mainView.findViewById(R.id.sendmessage_text_showtopicirc);
        messageSendButton = (ImageButton) mainView.findViewById(R.id.sendmessage_button_showtopicirc);
        loadingLayout = mainView.findViewById(R.id.layout_loading_showtopicirc);

        topicLinkButton.setOnClickListener(changeCurrentTopicLinkListener);
        messageSendButton.setOnClickListener(sendMessageToTopicListener);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getterForMessages.setListenerForNewMessages(listenerForNewMessages);

        if (savedInstanceState != null) {
            oldUrlForTopic = savedInstanceState.getString(getString(R.string.saveOldUrlForTopic), "");
            oldLastIdOfMessage = savedInstanceState.getLong(getString(R.string.saveOldLastIdOfMessage), 0);
        } else {
            oldUrlForTopic = sharedPref.getString(getString(R.string.prefOldUrlForTopic), "");
            oldLastIdOfMessage = sharedPref.getLong(getString(R.string.prefOldLastIdOfMessage), 0);
        }

        getterForMessages.setNewTopic(sharedPref.getString(getString(R.string.prefUrlToFetch), ""), false);
        urlEdit.setText(getterForMessages.getUrlForTopic());
    }

    @Override
    public void onResume() {
        super.onResume();
        maxNumberOfMessagesShowed = Integer.parseInt(sharedPref.getString(getString(R.string.settingsMaxNumberOfMessages), getString(R.string.maxNumberOfMessagesDefault)));
        initialNumberOfMessagesShowed = Integer.parseInt(sharedPref.getString(getString(R.string.settingsInitialNumberOfMessages), getString(R.string.initialNumberOfMessagesDefault)));
        getterForMessages.setTimeBetweenRefreshTopic(Integer.parseInt(sharedPref.getString(getString(R.string.settingsRefreshTopicTime), getString(R.string.refreshTopicTimeDefault))));
        getterForMessages.reloadTopic();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveOldUrlForTopic), oldUrlForTopic);
        outState.putLong(getString(R.string.saveOldLastIdOfMessage), oldLastIdOfMessage);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_showtopicirc, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_load_from_old_topic_info_showtopicirc).setEnabled(JVCParser.checkIfTopicAreSame(getterForMessages.getUrlForTopic(), oldUrlForTopic));
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