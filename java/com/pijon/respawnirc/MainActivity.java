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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*TODO: Lors de la récupération des messages, passer à la page suivante et non à la dernière page
* TODO: Ajouter la possibilité d'afficher tous les messages depuis le dernier lu
* TODO: Ajouter une activité pour les paramètres
* TODO: Gérer les scroll auto (pour l'affichage des messages et pour l'envoie lors des citations).
* TODO: Set focus sur la zone d'écriture des messages après citation ?
* TODO: Gérer le cas d'un changement de topic lors de la récupération des messages
* TODO: Récupérer les messages automatiquement après un post/changement de topic
* TODO: Convertir l'activité en fragment*/
public class MainActivity extends AppCompatActivity {
    private final int maxNumberOfMessagesShowed = 40;
    private final int initialNumberOfMessagesShowed = 10;

    JVCMessagesAdapter adapterForMessages = null;
    SharedPreferences sharedPref = null;
    ListView jvcMsgList = null;
    EditText urlEdit = null;
    EditText messageEdit = null;
    String urlToFetch = "";
    Timer timerForFetchUrl = new Timer();
    String latestListOfInputInAString = null;
    JVCParser.AjaxInfos latestAjaxInfos = new JVCParser.AjaxInfos();
    String latestMessageQuotedInfo = null;
    boolean firstTimeGetMessages = true;
    long lastIdOfMessage = 0;

    PopupMenu.OnMenuItemClickListener listenerForItemClicked = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_quote_message:
                    if (latestAjaxInfos.list != null && latestMessageQuotedInfo == null) {
                        String idOfMessage = Long.toString(adapterForMessages.getItem(adapterForMessages.getCurrentItemSelected()).id);
                        latestMessageQuotedInfo = JVCParser.buildMessageQuotedInfoFromThis(adapterForMessages.getItem(adapterForMessages.getCurrentItemSelected()));

                        new QuoteJVCMessage().execute(idOfMessage, latestAjaxInfos.list, sharedPref.getString(getString(R.string.prefCookiesList), ""));
                    } else {
                        Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    }

                    return true;
            }
            return false;
        }
    };

    /*TODO: Refléchir à déplacer cette classe dans JVCParser ?*/
    static class PageInfos {
        ArrayList<JVCParser.MessageInfos> listOfMessages;
        String lastPageLink;
        String listOfInputInAString;
        JVCParser.AjaxInfos ajaxInfosOfThisPage;
    }

    public void resetTopicInfos() {
        firstTimeGetMessages = true;
        latestListOfInputInAString = null;
        lastIdOfMessage = 0;

        adapterForMessages.removeAllItems();
        adapterForMessages.updateAllItems();
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

        resetTopicInfos();

        sharedPrefEdit.putString(getString(R.string.prefUrlToFetch), urlToFetch);
        sharedPrefEdit.apply();
    }

    public void sendMessageToTopic(View buttonView) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView;

        if (latestListOfInputInAString != null) {
            String messageToSend;
            try {
                messageToSend = URLEncoder.encode(messageEdit.getText().toString(), "UTF-8");
            } catch (Exception e) {
                messageToSend = "";
                e.printStackTrace();
            }
            new PostJVCMessage().execute(urlToFetch, messageToSend, latestListOfInputInAString, sharedPref.getString(getString(R.string.prefCookiesList), ""));
            messageEdit.setText("");
        } else {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
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
        messageEdit = (EditText) findViewById(R.id.sendmessage_text_main);

        adapterForMessages = new JVCMessagesAdapter(MainActivity.this);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.MessageInfos> allCurrentMessagesShowed = savedInstanceState.getParcelableArrayList(getString(R.string.saveAllCurrentMessagesShowed));
            latestListOfInputInAString = savedInstanceState.getString(getString(R.string.saveLatestListOfInputInAString), null);
            latestAjaxInfos.list = savedInstanceState.getString(getString(R.string.saveLatestAjaxInfoList), null);
            latestAjaxInfos.mod = savedInstanceState.getString(getString(R.string.saveLatestAjaxInfoMod), null);
            firstTimeGetMessages = savedInstanceState.getBoolean(getString(R.string.saveFirstTimeGetMessages), true);
            lastIdOfMessage = savedInstanceState.getLong(getString(R.string.saveLastIfOfMessage), 0);

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
        adapterForMessages.setActionWhenItemMenuClicked(listenerForItemClicked);
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
        outState.putLong(getString(R.string.saveLastIfOfMessage), lastIdOfMessage);
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
        new LaunchShowJVCLastMessage().run();
    }

    @Override
    public void onPause() {
        super.onPause();
        timerForFetchUrl.cancel();
        timerForFetchUrl.purge();
        timerForFetchUrl = new Timer();
    }

    private class LaunchShowJVCLastMessage extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new ShowJVCLastMessage().execute(urlToFetch, sharedPref.getString(getString(R.string.prefCookiesList), ""));
                }
            });
        }
    }

    /*TODO: si le lien du topic comporte une faute dans le titre, la récupération des messages fonctionnera (car redirect) mais pas l'envoie.*/
    private class PostJVCMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                return WebManager.sendRequest(params[0], "POST", "message_topic=" + params[1] + params[2] + "&form_alias_rang=1", params[3]);
            } else {
                return null;
            }
        }

        /*TODO: Mieux gérer (afficher) les erreurs d'envoies (en analysant la réponse dans onPostExecute).*/
        @Override
        protected void onPostExecute(String pageResult) {
            super.onPostExecute(pageResult);

            if (pageResult != null) {
                if (!pageResult.isEmpty()) {
                    Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                }
            }
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
                String currentMessage = messageEdit.getText().toString();

                if (!currentMessage.isEmpty()) {
                    currentMessage += "\n\n";
                }
                currentMessage += latestMessageQuotedInfo + "\n>" + messageQuoted + "\n\n";

                messageEdit.setText(currentMessage);
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
                    newPageInfos.listOfMessages = JVCParser.getMessagesOfThisPage(pageContent);
                    newPageInfos.listOfInputInAString = JVCParser.getListOfInputInAString(pageContent);
                    newPageInfos.ajaxInfosOfThisPage = JVCParser.getAllAjaxInfos(pageContent);
                }

                return newPageInfos;
            } else {
                return null;
            }
        }

        /*TODO: changer la manière dont sont affiché les messages.
        * TODO: au lieu d'aller à la dernière page, passer à la page suivante ?*/
        @Override
        protected void onPostExecute(PageInfos infoOfCurrentPage) {
            super.onPostExecute(infoOfCurrentPage);

            if (infoOfCurrentPage != null) {
                latestListOfInputInAString = infoOfCurrentPage.listOfInputInAString;
                latestAjaxInfos = infoOfCurrentPage.ajaxInfosOfThisPage;

                if (!infoOfCurrentPage.listOfMessages.isEmpty() && (infoOfCurrentPage.lastPageLink.isEmpty() || !firstTimeGetMessages)) {
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
                    firstTimeGetMessages = false;
                }

                if (!infoOfCurrentPage.lastPageLink.isEmpty()) {
                    urlToFetch = infoOfCurrentPage.lastPageLink;
                }
            }

            timerForFetchUrl.schedule(new LaunchShowJVCLastMessage(), 2000);
        }
    }
}