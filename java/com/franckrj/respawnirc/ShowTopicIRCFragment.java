package com.franckrj.respawnirc;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;

/*TODO: Set focus sur la zone d'écriture des messages après citation ?
* TODO: géré la redirection de lien (changement de nom de topic, suppression de page, etc)
* TODO: Récupérer les deux dernières page si la dernière page contient moins de X messages (et au 1er chargement aussi ?)
* TODO: http://stackoverflow.com/questions/5312592/how-can-i-get-my-listview-to-scroll pour pouvoir scroll le lien du topic ?*/
public class ShowTopicIRCFragment extends Fragment {
    private int maxNumberOfMessagesShowed = 40;
    private int initialNumberOfMessagesShowed = 10;
    private JVCMessagesAdapter adapterForMessages = null;
    private JVCMessageGetter getterForMessages = null;
    private JVCMessageSender senderForMessages = null;
    private SharedPreferences sharedPref = null;
    private ListView jvcMsgList = null;
    private EditText urlEdit = null;
    private EditText messageSendEdit = null;
    private ImageButton messageSendButton = null;
    private String latestMessageQuotedInfo = null;
    private String pseudoOfUser = "";
    private String cookieListInAString = "";
    private String oldUrlForTopic = "";
    private long oldLastIdOfMessage = 0;
    private View loadingLayout = null;
    private AsyncTask<String, Void, String> currentTaskQuoteMessage = null;

    private final PopupMenu.OnMenuItemClickListener listenerForItemClicked = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_quote_message:
                    if (getterForMessages.getLatestAjaxInfos().list != null && latestMessageQuotedInfo == null && currentTaskQuoteMessage == null) {
                        String idOfMessage = Long.toString(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).id);
                        latestMessageQuotedInfo = JVCParser.buildMessageQuotedInfoFromThis(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()));

                        currentTaskQuoteMessage = new QuoteJVCMessage();
                        currentTaskQuoteMessage.execute(idOfMessage, getterForMessages.getLatestAjaxInfos().list, cookieListInAString);
                    } else {
                        Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                case R.id.menu_edit_message:
                    boolean infoForEditAreGetted = false;
                    if (messageSendButton.isEnabled() && getterForMessages.getLatestAjaxInfos().list != null) {
                        String idOfMessage = Long.toString(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).id);
                        messageSendButton.setEnabled(false);
                        messageSendButton.setImageResource(R.drawable.ic_action_content_edit);
                        infoForEditAreGetted = senderForMessages.getInfosForEditMessage(idOfMessage, getterForMessages.getLatestAjaxInfos().list, cookieListInAString);
                    }

                    if (!infoForEditAreGetted) {
                        Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                case R.id.menu_show_spoil_message:
                    adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).showSpoil = true;
                    adapterForMessages.updateAllItems();
                    return true;
                case R.id.menu_hide_spoil_message:
                    adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).showSpoil = false;
                    adapterForMessages.updateAllItems();
                    return true;
                default:
                    return false;
            }
        }
    };

    private final JVCMessageGetter.NewMessagesListener listenerForNewMessages = new JVCMessageGetter.NewMessagesListener() {
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

    private final JVCMessageGetter.NewGetterStateListener listenerForNewGetterState = new JVCMessageGetter.NewGetterStateListener() {
        @Override
        public void newStateSetted(int newState) {
            if (adapterForMessages.getCount() == 0) {
                if (newState == JVCMessageGetter.STATE_LOADING) {
                    loadingLayout.setVisibility(View.VISIBLE);
                } else if (newState == JVCMessageGetter.STATE_NOT_LOADING) {
                    loadingLayout.setVisibility(View.GONE);
                }
            }
        }
    };

    private final JVCMessageSender.NewMessageWantEditListener listenerForNewMessageWantEdit = new JVCMessageSender.NewMessageWantEditListener() {
        @Override
        public void initializeEditMode(String newMessageToEdit) {
            messageSendButton.setEnabled(true);

            if (newMessageToEdit.isEmpty()) {
                messageSendButton.setImageResource(R.drawable.ic_action_content_send);
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
            } else {
                messageSendEdit.setText(newMessageToEdit);
            }
        }
    };

    private final JVCMessageSender.NewMessagePostedListener listenerForNewMessagePosted = new JVCMessageSender.NewMessagePostedListener() {
        @Override
        public void lastMessageIsSended(String withThisError) {
            messageSendButton.setEnabled(true);
            messageSendButton.setImageResource(R.drawable.ic_action_content_send);

            if (withThisError != null) {
                Toast.makeText(getActivity(), withThisError, Toast.LENGTH_LONG).show();
            } else {
                messageSendEdit.setText("");
                getterForMessages.startEarlyGetMessagesIfNeeded();
            }
        }
    };

    private final Button.OnClickListener changeCurrentTopicLinkListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            String newUrl;
            View focusedView;
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();

            newUrl = urlEdit.getText().toString();

            if (!newUrl.isEmpty()) {
                if (newUrl.startsWith("https://")) {
                    newUrl = newUrl.replaceFirst("https://", "http://");
                }

                if (!newUrl.startsWith("http://")) {
                    newUrl = "http://" + newUrl;
                }

                if (newUrl.startsWith("http://m.jeuxvideo.com/")) {
                    newUrl = newUrl.replaceFirst("http://m.jeuxvideo.com/", "http://www.jeuxvideo.com/");
                } else if (newUrl.startsWith("http://jeuxvideo.com/")) {
                    newUrl = newUrl.replaceFirst("http://jeuxvideo.com/", "http://www.jeuxvideo.com/");
                }

                if (!newUrl.equals(urlEdit.getText().toString())) {
                    urlEdit.setText(newUrl);
                }
            }

            sharedPrefEdit.putString(getString(R.string.prefUrlToFetch), newUrl);
            sharedPrefEdit.apply();

            getterForMessages.stopGetMessages();
            adapterForMessages.removeAllItems();
            adapterForMessages.updateAllItems();
            getterForMessages.setNewTopic(newUrl, true);
            getterForMessages.startEarlyGetMessagesIfNeeded();

            focusedView = getActivity().getCurrentFocus();
            if (focusedView != null) {
                inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    };

    private final Button.OnClickListener sendMessageToTopicListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (messageSendButton.isEnabled()) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView;

                if (!pseudoOfUser.isEmpty()) {
                    if (!senderForMessages.getIsInEdit()) {
                        boolean messageIsSended = false;
                        if (getterForMessages.getLatestListOfInputInAString() != null) {
                            messageSendButton.setEnabled(false);
                            messageIsSended = senderForMessages.sendThisMessage(messageSendEdit.getText().toString(), getterForMessages.getUrlForTopic(), getterForMessages.getLatestListOfInputInAString(), cookieListInAString);
                        }

                        if (!messageIsSended) {
                            Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        messageSendButton.setEnabled(false);
                        senderForMessages.sendEditMessage(messageSendEdit.getText().toString(), cookieListInAString);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.errorConnectedNeededBeforePost, Toast.LENGTH_LONG).show();
                }

                focusedView = getActivity().getCurrentFocus();
                if (focusedView != null) {
                    inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            } else {
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    };

    public void loadFromOldTopicInfos() {
        getterForMessages.stopGetMessages();
        adapterForMessages.removeAllItems();
        adapterForMessages.updateAllItems();
        getterForMessages.setOldTopic(oldUrlForTopic, oldLastIdOfMessage);
        getterForMessages.startEarlyGetMessagesIfNeeded();
    }

    public void stopAllCurrentTask() {
        if (currentTaskQuoteMessage != null) {
            currentTaskQuoteMessage.cancel(false);
            currentTaskQuoteMessage = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        getterForMessages = new JVCMessageGetter(getActivity());
        senderForMessages = new JVCMessageSender(getActivity());
        adapterForMessages = new JVCMessagesAdapter(getActivity());
        getterForMessages.setListenerForNewMessages(listenerForNewMessages);
        getterForMessages.setListenerForNewGetterState(listenerForNewGetterState);
        senderForMessages.setListenerForNewMessageWantEdit(listenerForNewMessageWantEdit);
        senderForMessages.setListenerForNewMessagePosted(listenerForNewMessagePosted);
        adapterForMessages.setActionWhenItemMenuClicked(listenerForItemClicked);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.MessageInfos> allCurrentMessagesShowed = savedInstanceState.getParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed));
            getterForMessages.loadFromBundle(savedInstanceState);
            senderForMessages.loadFromBundle(savedInstanceState);
            oldUrlForTopic = savedInstanceState.getString(getString(R.string.saveOldUrlForTopic), "");
            oldLastIdOfMessage = savedInstanceState.getLong(getString(R.string.saveOldLastIdOfMessage), 0);

            if (senderForMessages.getIsInEdit()) {
                messageSendButton.setImageResource(R.drawable.ic_action_content_edit);
            }

            if (allCurrentMessagesShowed != null) {
                for (JVCParser.MessageInfos thisMessageInfo : allCurrentMessagesShowed) {
                    adapterForMessages.addItem(thisMessageInfo);
                }
            }

            adapterForMessages.updateAllItems();
        } else {
            oldUrlForTopic = sharedPref.getString(getString(R.string.prefOldUrlForTopic), "");
            oldLastIdOfMessage = sharedPref.getLong(getString(R.string.prefOldLastIdOfMessage), 0);
        }

        getterForMessages.setNewTopic(sharedPref.getString(getString(R.string.prefUrlToFetch), ""), false);

        loadingLayout.setVisibility(View.GONE);
        urlEdit.setText(getterForMessages.getUrlForTopic());
        jvcMsgList.setAdapter(adapterForMessages);
        messageSendEdit.requestFocus();

        if (sharedPref.getBoolean(getString(R.string.prefIsFirstLaunch), true)) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            HelpFirstLaunchDialogFragment firstLaunchDialogFragment = new HelpFirstLaunchDialogFragment();
            firstLaunchDialogFragment.show(getFragmentManager(), "HelpFirstLaunchDialogFragment");
            sharedPrefEdit.putBoolean(getString(R.string.prefIsFirstLaunch), false);
            sharedPrefEdit.apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        maxNumberOfMessagesShowed = Integer.parseInt(sharedPref.getString(getString(R.string.settingsMaxNumberOfMessages), getString(R.string.maxNumberOfMessagesDefault)));
        initialNumberOfMessagesShowed = Integer.parseInt(sharedPref.getString(getString(R.string.settingsInitialNumberOfMessages), getString(R.string.initialNumberOfMessagesDefault)));
        cookieListInAString = sharedPref.getString(getString(R.string.prefCookiesList), "");
        pseudoOfUser = sharedPref.getString(getString(R.string.prefPseudoUser), "");
        getterForMessages.setTimeBetweenRefreshTopic(Integer.parseInt(sharedPref.getString(getString(R.string.settingsRefreshTopicTime), getString(R.string.refreshTopicTimeDefault))));
        adapterForMessages.setCurrentPseudoOfUser(pseudoOfUser);
        getterForMessages.setCookieListInAString(cookieListInAString);
        getterForMessages.startEarlyGetMessagesIfNeeded();
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        getterForMessages.stopGetMessages();
        senderForMessages.stopAllCurrentTask();
        stopAllCurrentTask();
        sharedPrefEdit.putString(getString(R.string.prefOldUrlForTopic), getterForMessages.getUrlForTopic());
        sharedPrefEdit.putLong(getString(R.string.prefOldLastIdOfMessage), getterForMessages.getLastIdOfMessage());
        sharedPrefEdit.apply();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed), adapterForMessages.getAllItems());
        getterForMessages.saveToBundle(outState);
        senderForMessages.saveToBundle(outState);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class QuoteJVCMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                String pageContent = WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_citation.php", "POST", "id_message=" + params[0] + "&" + params[1], params[2]);

                if (pageContent != null) {
                    return JVCParser.getMessageQuoted(pageContent);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String messageQuoted) {
            super.onPostExecute(messageQuoted);

            if (messageQuoted != null) {
                String currentMessage = messageSendEdit.getText().toString();

                if (!currentMessage.isEmpty() && !currentMessage.endsWith("\n\n")) {
                    if (!currentMessage.endsWith("\n")) {
                        currentMessage += "\n";
                    }
                    currentMessage += "\n";
                }
                currentMessage += latestMessageQuotedInfo + "\n>" + messageQuoted + "\n\n";

                messageSendEdit.setText(currentMessage);
                messageSendEdit.setSelection(currentMessage.length());
                latestMessageQuotedInfo = null;
            } else {
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
            }

            currentTaskQuoteMessage = null;
        }
    }

    static public class HelpFirstLaunchDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.welcome).setMessage(R.string.help_firstlaunch)
                    .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).setPositiveButton(R.string.connectToJVC, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            startActivity(new Intent(getActivity(), ConnectActivity.class));
                        }
                    });
            return builder.create();
        }
    }
}