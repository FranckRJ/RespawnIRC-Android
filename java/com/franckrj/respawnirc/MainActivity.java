package com.franckrj.respawnirc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.util.ArrayList;

/*TODO: Set focus sur la zone d'écriture des messages après citation ?
* TODO: géré la redirection de lien (changement de nom de topic, suppression de page, etc)
* TODO: Récupérer les deux dernières page si la dernière page contient moins de X messages (et au 1er chargement aussi ?)
* TODO: http://stackoverflow.com/questions/5312592/how-can-i-get-my-listview-to-scroll pour pouvoir scroll le lien du topic ?
* TODO: Convertir l'activité en fragment*/
public class MainActivity extends AppCompatActivity {
    private int maxNumberOfMessagesShowed = 40;
    private int initialNumberOfMessagesShowed = 10;
    private JVCMessagesAdapter adapterForMessages = null;
    private JVCMessageGetter getterForMessages = null;
    private JVCMessageSender senderForMessages = null;
    private SharedPreferences sharedPref = null;
    private ListView jvcMsgList = null;
    private EditText urlEdit = null;
    private EditText messageSendEdit = null;
    private Button messageSendButton = null;
    private String latestMessageQuotedInfo = null;
    private String pseudoOfUser = "";
    private String cookieListInAString = "";
    private String oldUrlForTopic = "";
    private long oldLastIdOfMessage = 0;
    private View loadingLayout = null;

    private PopupMenu.OnMenuItemClickListener listenerForItemClicked = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_quote_message:
                    if (getterForMessages.getLatestAjaxInfos().list != null && latestMessageQuotedInfo == null) {
                        String idOfMessage = Long.toString(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).id);
                        latestMessageQuotedInfo = JVCParser.buildMessageQuotedInfoFromThis(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()));

                        new QuoteJVCMessage().execute(idOfMessage, getterForMessages.getLatestAjaxInfos().list, cookieListInAString);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                case R.id.menu_edit_message:
                    if (messageSendButton.isEnabled() && getterForMessages.getLatestAjaxInfos().list != null) {
                        String idOfMessage = Long.toString(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).id);
                        messageSendButton.setEnabled(false);
                        messageSendButton.setText(R.string.messageEdit);
                        senderForMessages.getInfosForEditMessage(idOfMessage, getterForMessages.getLatestAjaxInfos().list, cookieListInAString);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
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
            }
            return false;
        }
    };

    private JVCMessageGetter.NewMessagesListener listenerForNewMessages = new JVCMessageGetter.NewMessagesListener() {
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

    private JVCMessageGetter.NewGetterStateListener listenerForNewGetterState = new JVCMessageGetter.NewGetterStateListener() {
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

    private JVCMessageSender.NewMessageWantEditListener listenerForNewMessageWantEdit = new JVCMessageSender.NewMessageWantEditListener() {
        @Override
        public void initializeEditMode(String newMessageToEdit) {
            messageSendButton.setEnabled(true);

            if (newMessageToEdit.isEmpty()) {
                messageSendButton.setText(R.string.messagePost);
                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            } else {
                messageSendEdit.setText(newMessageToEdit);
            }
        }
    };

    private JVCMessageSender.NewMessagePostedListener listenerForNewMessagePosted = new JVCMessageSender.NewMessagePostedListener() {
        @Override
        public void lastMessageIsSended(String withThisError) {
            messageSendButton.setEnabled(true);
            messageSendButton.setText(R.string.messagePost);

            if (withThisError != null) {
                Toast.makeText(MainActivity.this, withThisError, Toast.LENGTH_LONG).show();
            } else {
                messageSendEdit.setText("");
                getterForMessages.startEarlyGetMessagesIfNeeded();
            }
        }
    };

    public void changeCurrentTopicLink(View buttonView) {
        String newUrl;
        View focusedView;
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

        focusedView = getCurrentFocus();
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void sendMessageToTopic(View buttonView) {
        if (messageSendButton.isEnabled()) {
            InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            View focusedView;

            if (!pseudoOfUser.isEmpty()) {
                if (!senderForMessages.getIsInEdit()) {
                    if (getterForMessages.getLatestListOfInputInAString() != null) {
                        messageSendButton.setEnabled(false);
                        senderForMessages.sendThisMessage(messageSendEdit.getText().toString(), getterForMessages.getUrlForTopic(), getterForMessages.getLatestListOfInputInAString(), cookieListInAString);
                    } else {
                        Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    messageSendButton.setEnabled(false);
                    senderForMessages.sendEditMessage(messageSendEdit.getText().toString(), cookieListInAString);
                }
            } else {
                Toast.makeText(this, R.string.errorConnectedNeededBeforePost, Toast.LENGTH_LONG).show();
            }

            focusedView = getCurrentFocus();
            if (focusedView != null) {
                inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } else {
            Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public void loadFromOldTopicInfos() {
        getterForMessages.stopGetMessages();
        adapterForMessages.removeAllItems();
        adapterForMessages.updateAllItems();
        getterForMessages.setOldTopic(oldUrlForTopic, oldLastIdOfMessage);
        getterForMessages.startEarlyGetMessagesIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_load_from_old_topic_info_main).setEnabled(JVCParser.checkIfTopicAreSame(getterForMessages.getUrlForTopic(), oldUrlForTopic));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connectToJVC_main:
                startActivity(new Intent(MainActivity.this, ConnectActivity.class));
                return true;
            case R.id.action_load_from_old_topic_info_main:
                loadFromOldTopicInfos();
                return true;
            case R.id.action_settings_main:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);

        jvcMsgList = (ListView) findViewById(R.id.jvcmessage_view_main);
        urlEdit = (EditText) findViewById(R.id.topiclink_text_main);
        messageSendEdit = (EditText) findViewById(R.id.sendmessage_text_main);
        messageSendButton = (Button) findViewById(R.id.sendmessage_button_main);
        loadingLayout = findViewById(R.id.layout_loading_main);

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        getterForMessages = new JVCMessageGetter(MainActivity.this);
        senderForMessages = new JVCMessageSender(MainActivity.this);
        adapterForMessages = new JVCMessagesAdapter(MainActivity.this);
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
                messageSendButton.setText(R.string.messageEdit);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed), adapterForMessages.getAllItems());
        getterForMessages.saveToBundle(outState);
        senderForMessages.saveToBundle(outState);
        outState.putString(getString(R.string.saveOldUrlForTopic), oldUrlForTopic);
        outState.putLong(getString(R.string.saveOldLastIdOfMessage), oldLastIdOfMessage);
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
        sharedPrefEdit.putString(getString(R.string.prefOldUrlForTopic), getterForMessages.getUrlForTopic());
        sharedPrefEdit.putLong(getString(R.string.prefOldLastIdOfMessage), getterForMessages.getLastIdOfMessage());
        sharedPrefEdit.apply();
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
                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
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