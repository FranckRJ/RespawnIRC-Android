package com.franckrj.respawnirc.jvcmsgviewers;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.franckrj.respawnirc.JVCMessageSender;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.jvcmsggetters.AbsJVCMessageGetter;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public abstract class AbsShowTopicFragment extends Fragment {
    public static final String ARG_TOPIC_LINK = "com.franckrj.respawnirc.showtopicfragment.topic_link";
    public static final int MODE_IRC = 0;
    public static final int MODE_FORUM = 1;

    protected QuoteJVCMessage currentTaskQuoteMessage = null;
    protected AbsJVCMessageGetter absGetterForMessages = null;
    protected ListView jvcMsgList = null;
    protected SharedPreferences sharedPref = null;
    protected JVCMessagesAdapter adapterForMessages = null;
    protected JVCMessageSender senderForMessages = null;
    protected EditText messageSendEdit = null;
    protected String latestMessageQuotedInfo = null;
    protected ImageButton messageSendButton = null;
    protected String pseudoOfUser = "";
    protected String cookieListInAString = "";
    protected JVCParser.Settings currentSettings = new JVCParser.Settings();
    protected NewModeNeededListener listenerForNewModeNeeded = null;
    protected SwipeRefreshLayout swipeRefresh = null;
    protected boolean showRefreshWhenMessagesShowed = true;
    protected boolean isInErrorMode = false;

    protected final PopupMenu.OnMenuItemClickListener listenerForItemClicked = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            JVCParser.MessageInfos currentItem;
            switch (item.getItemId()) {
                case R.id.menu_quote_message:
                    if (absGetterForMessages.getLatestAjaxInfos().list != null && latestMessageQuotedInfo == null && currentTaskQuoteMessage == null && !pseudoOfUser.isEmpty()) {
                        String idOfMessage = Long.toString(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).id);
                        latestMessageQuotedInfo = JVCParser.buildMessageQuotedInfoFromThis(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()));

                        currentTaskQuoteMessage = new QuoteJVCMessage();
                        currentTaskQuoteMessage.execute(idOfMessage, absGetterForMessages.getLatestAjaxInfos().list, cookieListInAString);
                    } else {
                        if (pseudoOfUser.isEmpty()) {
                            Toast.makeText(getActivity(), R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                        } else if (latestMessageQuotedInfo != null || currentTaskQuoteMessage != null) {
                            Toast.makeText(getActivity(), R.string.errorQuoteAlreadyRunning, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
                case R.id.menu_edit_message:
                    boolean infoForEditAreGetted = false;
                    if (messageSendButton.isEnabled() && absGetterForMessages.getLatestAjaxInfos().list != null) {
                        String idOfMessage = Long.toString(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).id);
                        messageSendButton.setEnabled(false);
                        messageSendButton.setImageResource(R.drawable.ic_action_content_edit);
                        infoForEditAreGetted = senderForMessages.getInfosForEditMessage(idOfMessage, absGetterForMessages.getLatestAjaxInfos().list, cookieListInAString);
                    }

                    if (!infoForEditAreGetted) {
                        if (!messageSendButton.isEnabled()) {
                            Toast.makeText(getActivity(), R.string.errorMessageAlreadySending, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
                        }
                    }

                    return true;
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
    };

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

    protected final JVCMessageSender.NewMessageWantEditListener listenerForNewMessageWantEdit = new JVCMessageSender.NewMessageWantEditListener() {
        @Override
        public void initializeEditMode(String newMessageToEdit) {
            messageSendButton.setEnabled(true);

            if (newMessageToEdit.isEmpty()) {
                messageSendButton.setImageResource(R.drawable.ic_action_content_send);
                Toast.makeText(getActivity(), R.string.errorCantGetEditInfos, Toast.LENGTH_SHORT).show();
            } else {
                messageSendEdit.setText(newMessageToEdit);
            }
        }
    };

    protected final Button.OnClickListener sendMessageToTopicListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (messageSendButton.isEnabled()) {
                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                View focusedView;

                if (!pseudoOfUser.isEmpty()) {
                    if (!senderForMessages.getIsInEdit()) {
                        boolean messageIsSended = false;
                        if (absGetterForMessages.getLatestListOfInputInAString() != null) {
                            messageSendButton.setEnabled(false);
                            messageIsSended = senderForMessages.sendThisMessage(messageSendEdit.getText().toString(), absGetterForMessages.getUrlForTopic(), absGetterForMessages.getLatestListOfInputInAString(), cookieListInAString);
                        }

                        if (!messageIsSended) {
                            Toast.makeText(getActivity(), R.string.errorInfosMissings, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), R.string.errorMessageAlreadySending, Toast.LENGTH_SHORT).show();
            }
        }
    };

    protected final JVCMessageSender.NewMessagePostedListener listenerForNewMessagePosted = new JVCMessageSender.NewMessagePostedListener() {
        @Override
        public void lastMessageIsSended(String withThisError) {
            messageSendButton.setEnabled(true);
            messageSendButton.setImageResource(R.drawable.ic_action_content_send);

            if (withThisError != null) {
                Toast.makeText(getActivity(), withThisError, Toast.LENGTH_LONG).show();
            } else {
                messageSendEdit.setText("");
            }
            absGetterForMessages.reloadTopic();
        }
    };

    protected String baseForChangeTopicLink(String thisLink) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();

        if (!thisLink.isEmpty()) {
            thisLink = JVCParser.formatThisUrl(thisLink);
        }

        sharedPrefEdit.putString(getString(R.string.prefTopicUrlToFetch), thisLink);
        sharedPrefEdit.apply();

        return thisLink;
    }

    protected void stopAllCurrentTask() {
        if (currentTaskQuoteMessage != null) {
            currentTaskQuoteMessage.cancel(false);
            currentTaskQuoteMessage = null;
            latestMessageQuotedInfo = null;
        }
    }

    protected void reloadSettings() {
        currentSettings.maxNumberOfOverlyQuotes = Integer.parseInt(sharedPref.getString(getString(R.string.settingsMaxNumberOfOverlyQuote), getString(R.string.maxNumberOfOverlyQuoteDefault)));
        pseudoOfUser = sharedPref.getString(getString(R.string.prefPseudoUser), "");
        currentSettings.pseudoOfUser = pseudoOfUser;
        cookieListInAString = sharedPref.getString(getString(R.string.prefCookiesList), "");
        absGetterForMessages.setCookieListInAString(cookieListInAString);
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

        senderForMessages = new JVCMessageSender(getActivity());
        adapterForMessages = new JVCMessagesAdapter(getActivity(), currentSettings);
        initializeGetterForMessages();
        initializeAdapter();
        initializeSettings();
        reloadSettings();
        absGetterForMessages.setListenerForNewGetterState(listenerForNewGetterState);
        senderForMessages.setListenerForNewMessageWantEdit(listenerForNewMessageWantEdit);
        adapterForMessages.setActionWhenItemMenuClicked(listenerForItemClicked);
        senderForMessages.setListenerForNewMessagePosted(listenerForNewMessagePosted);

        if (getActivity() instanceof NewModeNeededListener) {
            listenerForNewModeNeeded = (NewModeNeededListener) getActivity();
        }
        if (getActivity() instanceof AbsJVCMessageGetter.NewForumAndTopicNameAvailable) {
            absGetterForMessages.setListenerForNewForumAndTopicName((AbsJVCMessageGetter.NewForumAndTopicNameAvailable) getActivity());
        }

        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        jvcMsgList.setAdapter(adapterForMessages);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.MessageInfos> allCurrentMessagesShowed = savedInstanceState.getParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed));
            absGetterForMessages.loadFromBundle(savedInstanceState);
            senderForMessages.loadFromBundle(savedInstanceState);

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
            Bundle currentArgs = getArguments();

            if (currentArgs != null) {
                String topicLink = currentArgs.getString(ARG_TOPIC_LINK, "");
                setNewTopicLink(topicLink);
            }
        }

        messageSendEdit.requestFocus();
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
        senderForMessages.stopAllCurrentTask();
        stopAllCurrentTask();
        sharedPrefEdit.putString(getString(R.string.prefOldUrlForTopic), absGetterForMessages.getUrlForTopic());
        sharedPrefEdit.putLong(getString(R.string.prefOldLastIdOfMessage), absGetterForMessages.getLastIdOfMessage());
        sharedPrefEdit.apply();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed), adapterForMessages.getAllItems());
        absGetterForMessages.saveToBundle(outState);
        senderForMessages.saveToBundle(outState);
    }

    protected class QuoteJVCMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 2) {
                WebManager.WebInfos currentWebInfos = new WebManager.WebInfos();
                String pageContent;
                currentWebInfos.followRedirects = false;
                pageContent = WebManager.sendRequest("http://www.jeuxvideo.com/forums/ajax_citation.php", "POST", "id_message=" + params[0] + "&" + params[1], params[2], currentWebInfos);

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
            } else {
                Toast.makeText(getActivity(), R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }

            latestMessageQuotedInfo = null;
            currentTaskQuoteMessage = null;
        }
    }

    public interface NewModeNeededListener {
        void newModeRequested(int newMode);
    }

    public abstract void setNewTopicLink(String newTopicLink);
    protected abstract void initializeGetterForMessages();
    protected abstract void initializeSettings();
    protected abstract void initializeAdapter();
}