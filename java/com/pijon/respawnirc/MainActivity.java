package com.pijon.respawnirc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*TODO: Ajouter la possibilité d'afficher tous les messages depuis le dernier lu
* TODO: Ajouter une activité pour les paramètres
* TODO: Set focus sur la zone d'écriture des messages après citation ?
* TODO: géré la redirection de lien (changement de nom de topic, suppression de page, etc)
* TODO: Récupérer les deux dernières page si la dernière page contient moins de X messages (et au 1er chargement aussi ?)
* TODO: Convertir l'activité en fragment*/
public class MainActivity extends AppCompatActivity {
    private final int maxNumberOfMessagesShowed = 40;
    private final int initialNumberOfMessagesShowed = 10;

    private JVCMessagesAdapter adapterForMessages = null;
    private SharedPreferences sharedPref = null;
    private ListView jvcMsgList = null;
    private EditText urlEdit = null;
    private EditText messageSendEdit = null;
    private Button messageSendButton = null;
    private String urlToFetch = "";
    private Timer timerForFetchUrl = new Timer();
    private String latestListOfInputInAString = null;
    private JVCParser.AjaxInfos latestAjaxInfos = new JVCParser.AjaxInfos();
    private String latestMessageQuotedInfo = null;
    private String pseudoOfUser = "";
    private String cookieListInAString = "";
    private boolean firstTimeGetMessages = true;
    private long lastIdOfMessage = 0;
    private AsyncTask<String, Void, PageInfos> currentAsyncTaskForGetMessage = null;
    private boolean messagesNeedToBeGet = false;

    private PopupMenu.OnMenuItemClickListener listenerForItemClicked = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_quote_message:
                    if (latestAjaxInfos.list != null && latestMessageQuotedInfo == null) {
                        String idOfMessage = Long.toString(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()).id);
                        latestMessageQuotedInfo = JVCParser.buildMessageQuotedInfoFromThis(adapterForMessages.getItem(adapterForMessages.getCurrentItemIDSelected()));

                        new QuoteJVCMessage().execute(idOfMessage, latestAjaxInfos.list, cookieListInAString);
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

    /*TODO: Refléchir à déplacer cette classe dans JVCParser ?*/
    static class PageInfos {
        ArrayList<JVCParser.MessageInfos> listOfMessages;
        String lastPageLink;
        String nextPageLink;
        String listOfInputInAString;
        JVCParser.AjaxInfos ajaxInfosOfThisPage;
    }

    public void resetTopicInfos() {
        firstTimeGetMessages = true;
        latestListOfInputInAString = null;
        lastIdOfMessage = 0;

        adapterForMessages.removeAllItems();
        adapterForMessages.updateAllItems();
        stopCurrentShowJVCLastMessage();
        launchNewShowJVCLastMessage(0);
    }

    public void changeCurrentTopicLink(View buttonView) {
        String newUrl;
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();

        newUrl = urlEdit.getText().toString();
        urlToFetch = newUrl;

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

        if (!newUrl.equals(urlToFetch)) {
            urlToFetch = newUrl;
            urlEdit.setText(urlToFetch);
        }

        sharedPrefEdit.putString(getString(R.string.prefUrlToFetch), urlToFetch);
        sharedPrefEdit.apply();

        resetTopicInfos();
    }

    public void sendMessageToTopic(View buttonView) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView;

        if (!pseudoOfUser.isEmpty()) {
            if (latestListOfInputInAString != null) {
                String messageToSend;
                try {
                    messageToSend = URLEncoder.encode(messageSendEdit.getText().toString(), "UTF-8");
                } catch (Exception e) {
                    messageToSend = "";
                    e.printStackTrace();
                }
                new PostJVCMessage().execute(urlToFetch, messageToSend, latestListOfInputInAString, cookieListInAString);
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.errorConnectedNeededBeforePost, Toast.LENGTH_LONG).show();
        }

        focusedView = getCurrentFocus();
        if (focusedView != null) {
            inputManager.hideSoftInputFromWindow(focusedView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_connectToJVC_main:
                startActivity(new Intent(MainActivity.this, ConnectActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*TODO: http://stackoverflow.com/questions/5312592/how-can-i-get-my-listview-to-scroll pour pouvoir scroll le lien du topic ?*/
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

        adapterForMessages = new JVCMessagesAdapter(MainActivity.this);
        adapterForMessages.setActionWhenItemMenuClicked(listenerForItemClicked);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.MessageInfos> allCurrentMessagesShowed = savedInstanceState.getParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed));
            latestListOfInputInAString = savedInstanceState.getString(getString(R.string.saveLatestListOfInputInAString), null);
            latestAjaxInfos.list = savedInstanceState.getString(getString(R.string.saveLatestAjaxInfoList), null);
            latestAjaxInfos.mod = savedInstanceState.getString(getString(R.string.saveLatestAjaxInfoMod), null);
            firstTimeGetMessages = savedInstanceState.getBoolean(getString(R.string.saveFirstTimeGetMessages), true);
            lastIdOfMessage = savedInstanceState.getLong(getString(R.string.saveLastIdOfMessage), 0);

            if (allCurrentMessagesShowed != null) {
                for (JVCParser.MessageInfos thisMessageInfo : allCurrentMessagesShowed) {
                    adapterForMessages.addItem(thisMessageInfo);
                }
            }

            adapterForMessages.updateAllItems();
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        urlToFetch = sharedPref.getString(getString(R.string.prefUrlToFetch), "");

        urlEdit.setText(urlToFetch);
        jvcMsgList.setAdapter(adapterForMessages);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveLatestListOfInputInAString), latestListOfInputInAString);
        outState.putString(getString(R.string.saveLatestAjaxInfoList), latestAjaxInfos.list);
        outState.putString(getString(R.string.saveLatestAjaxInfoMod), latestAjaxInfos.mod);
        outState.putParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed), adapterForMessages.getAllItems());
        outState.putBoolean(getString(R.string.saveFirstTimeGetMessages), firstTimeGetMessages);
        outState.putLong(getString(R.string.saveLastIdOfMessage), lastIdOfMessage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        messagesNeedToBeGet = true;
        cookieListInAString = sharedPref.getString(getString(R.string.prefCookiesList), "");
        pseudoOfUser = sharedPref.getString(getString(R.string.prefPseudoUser), "");
        adapterForMessages.setCurrentPseudoOfUser(pseudoOfUser);
        launchNewShowJVCLastMessage(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        messagesNeedToBeGet = false;
        stopCurrentShowJVCLastMessage();
    }

    public void launchNewShowJVCLastMessage(int timerBeforeLaunch) {
        if (currentAsyncTaskForGetMessage == null) {
            currentAsyncTaskForGetMessage = new ShowJVCLastMessage();
            timerForFetchUrl.schedule(new LaunchShowJVCLastMessage(), timerBeforeLaunch);
        }
    }

    public void stopCurrentShowJVCLastMessage() {
        if (currentAsyncTaskForGetMessage != null) {
            currentAsyncTaskForGetMessage.cancel(false);
            currentAsyncTaskForGetMessage = null;
        }
    }

    public void launchEarlyNewShowJVCLastMessageIfNeeded() {
        if (currentAsyncTaskForGetMessage != null) {
            if (!currentAsyncTaskForGetMessage.getStatus().equals(AsyncTask.Status.RUNNING)) {
                stopCurrentShowJVCLastMessage();
                launchNewShowJVCLastMessage(0);
            }
        } else {
            launchNewShowJVCLastMessage(0);
        }
    }

    private class LaunchShowJVCLastMessage extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentAsyncTaskForGetMessage != null) {
                        if (currentAsyncTaskForGetMessage.getStatus().equals(AsyncTask.Status.PENDING)) {
                            currentAsyncTaskForGetMessage.execute(urlToFetch, cookieListInAString);
                        }
                    }
                }
            });
        }
    }

    private class PostJVCMessage extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            messageSendButton.setEnabled(false);
        }

        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                return WebManager.sendRequest(params[0], "POST", "message_topic=" + params[1] + params[2] + "&form_alias_rang=1", params[3]);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String pageResult) {
            super.onPostExecute(pageResult);
            messageSendButton.setEnabled(true);

            if (pageResult != null) {
                if (!pageResult.isEmpty()) {
                    Toast.makeText(MainActivity.this, JVCParser.getErrorMessage(pageResult), Toast.LENGTH_LONG).show();
                    return;
                }
            }

            messageSendEdit.setText("");
            launchEarlyNewShowJVCLastMessageIfNeeded();
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
                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ShowJVCLastMessage extends AsyncTask<String, Void, PageInfos> {
        @Override
        protected PageInfos doInBackground(String... params) {
            if (params.length > 1) {
                PageInfos newPageInfos = null;
                String pageContent = WebManager.sendRequest(params[0], "GET", "", params[1]);

                if (pageContent != null) {
                    newPageInfos = new PageInfos();
                    newPageInfos.lastPageLink = JVCParser.getLastPageOfTopic(pageContent);
                    newPageInfos.nextPageLink = JVCParser.getNextPageOfTopic(pageContent);
                    newPageInfos.listOfMessages = JVCParser.getMessagesOfThisPage(pageContent);
                    newPageInfos.listOfInputInAString = JVCParser.getListOfInputInAString(pageContent);
                    newPageInfos.ajaxInfosOfThisPage = JVCParser.getAllAjaxInfos(pageContent);
                }

                return newPageInfos;
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(PageInfos infoOfCurrentPage) {
            super.onPostExecute(infoOfCurrentPage);

            if (infoOfCurrentPage != null) {
                latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
                latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;

                if (!infoOfCurrentPage.listOfMessages.isEmpty() && (infoOfCurrentPage.lastPageLink.isEmpty() || !firstTimeGetMessages)) {

                    boolean scrolledAtTheEnd = true;

                    if (jvcMsgList.getChildCount() > 0) {
                        scrolledAtTheEnd = (jvcMsgList.getLastVisiblePosition() == jvcMsgList.getCount() - 1) &&
                                (jvcMsgList.getChildAt(jvcMsgList.getChildCount() - 1).getBottom() <= jvcMsgList.getHeight());
                    }

                    for (JVCParser.MessageInfos thisMessageInfo : infoOfCurrentPage.listOfMessages) {
                        if (thisMessageInfo.id > lastIdOfMessage) {
                            adapterForMessages.addItem(thisMessageInfo);
                            lastIdOfMessage = thisMessageInfo.id;
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

                    firstTimeGetMessages = false;
                }

                if (!infoOfCurrentPage.lastPageLink.isEmpty()) {
                    if (firstTimeGetMessages) {
                        urlToFetch = infoOfCurrentPage.lastPageLink;
                    } else {
                        urlToFetch = infoOfCurrentPage.nextPageLink;
                    }
                }
            }

            currentAsyncTaskForGetMessage = null;

            if (messagesNeedToBeGet) {
                launchNewShowJVCLastMessage(5000);
            }
        }
    }
}