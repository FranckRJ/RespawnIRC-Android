package com.pijon.respawnirc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private final int maxNumberOfMessagesShowed = 40;
    private final int initialNumberOfMessagesShowed = 10;

    SharedPreferences sharedPref = null;
    TextView jvcMsg = null;
    EditText urlEdit = null;
    EditText messageEdit = null;
    /*TODO: Sauvegarder l'url dans les sharedpreference.*/
    String urlToFetch = "http://www.jeuxvideo.com/forums/1-24777-4280756-1-0-1-0-o-blablacraft-o.htm";
    String latestListOfInputInAString = null;
    Timer timerForFetchUrl = new Timer();
    List<String> allCurrentMessagesShowed = new ArrayList<>();
    boolean firstTimeGetMessages = true;
    long lastIdOfMessage = 0;

    /*TODO: Refléchir à déplacer cette classe dans JVCParser ?*/
    static class PageInfos {
        List<JVCParser.MessageInfos> listOfMessages;
        String lastPageLink;
        String listOfInputInAString;
    }

    public void changeCurrentTopicLink(View buttonView) {
        /*TODO: convertir les liens en bons liens (rajouter www. etc etc).*/
        urlToFetch = urlEdit.getText().toString();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        jvcMsg = (TextView) findViewById(R.id.jvcmessage_view_main);
        urlEdit = (EditText) findViewById(R.id.topiclink_text_main);
        messageEdit = (EditText) findViewById(R.id.sendmessage_text_main);

        urlEdit.setText(urlToFetch);
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

    /*TODO: Gérer les erreurs d'envoies (en analysant la réponse dans onPostExecute).*/
    private class PostJVCMessage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 3) {
                return WebManager.sendRequest(params[0], "POST", "message_topic=" + params[1] + params[2] + "&form_alias_rang=1", params[3]);
            } else {
                return null;
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

                if (infoOfCurrentPage.lastPageLink.isEmpty() || !firstTimeGetMessages) {
                    for (JVCParser.MessageInfos thisMessageInfo : infoOfCurrentPage.listOfMessages) {
                        if (thisMessageInfo.id > lastIdOfMessage) {
                            allCurrentMessagesShowed.add(JVCParser.createStringMessageFromInfos(thisMessageInfo));
                            lastIdOfMessage = thisMessageInfo.id;
                        }
                    }

                    if (firstTimeGetMessages) {
                        while (allCurrentMessagesShowed.size() > initialNumberOfMessagesShowed) {
                            allCurrentMessagesShowed.remove(0);
                        }
                    }

                    while (allCurrentMessagesShowed.size() > maxNumberOfMessagesShowed) {
                        allCurrentMessagesShowed.remove(0);
                    }

                    jvcMsg.setText(Html.fromHtml(TextUtils.join("", allCurrentMessagesShowed)));
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