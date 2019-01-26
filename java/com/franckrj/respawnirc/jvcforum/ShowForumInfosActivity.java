package com.franckrj.respawnirc.jvcforum;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.base.AbsWebRequestAsyncTask;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.WebManager;

import java.util.ArrayList;

public class ShowForumInfosActivity extends AbsHomeIsBackActivity {
    public static final String EXTRA_FORUM_LINK = "com.franckrj.respawnirc.showforuminfos.EXTRA_FORUM_LINK";
    public static final String EXTRA_COOKIES = "com.franckrj.respawnirc.showforuminfos.EXTRA_COOKIES";

    private static final String SAVE_LIST_OF_SUBFORUMS = "saveListOfSubforums";
    private static final String SAVE_SCROLL_POSITION = "saveScrollPosition";

    private TextView backgroundErrorText = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private ScrollView mainScrollView = null;
    private CardView subforumsCardView = null;
    private LinearLayout layoutListOfSubforums = null;
    private DownloadForumInfos currentTaskForDownload = null;
    private ArrayList<JVCParser.NameAndLink> listOfSubforums = null;

    private final View.OnClickListener subforumButtonClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View button) {
            if (button.getTag() != null && button.getTag() instanceof String) {
                Intent newShowForumIntent = new Intent(ShowForumInfosActivity.this, ShowForumActivity.class);
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_NEW_LINK, (String) button.getTag());
                newShowForumIntent.putExtra(ShowForumActivity.EXTRA_IS_FIRST_ACTIVITY, false);
                startActivity(newShowForumIntent);
            }
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsStarted requestIsStartedListener = new AbsWebRequestAsyncTask.RequestIsStarted() {
        @Override
        public void onRequestIsStarted() {
            backgroundErrorText.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(true);
        }
    };

    private final AbsWebRequestAsyncTask.RequestIsFinished<String> downloadInfosIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<String>() {
        @Override
        public void onRequestIsFinished(String reqResult) {
            swipeRefresh.setRefreshing(false);

            if (reqResult != null) {
                listOfSubforums = JVCParser.getListOfSubforumsInForumPage(reqResult);
                updateDisplayedInfos();
            } else {
                backgroundErrorText.setVisibility(View.VISIBLE);
            }

            currentTaskForDownload = null;
        }
    };

    private void updateDisplayedInfos() {
        if (listOfSubforums != null && !listOfSubforums.isEmpty()) {
            subforumsCardView.setVisibility(View.VISIBLE);
            for (JVCParser.NameAndLink nameAndLink : listOfSubforums) {
                Button newSubforumButton = (Button)getLayoutInflater().inflate(R.layout.button_subforum, layoutListOfSubforums, false);

                newSubforumButton.setText(Undeprecator.htmlFromHtml(nameAndLink.name));
                newSubforumButton.setTag(nameAndLink.link);
                newSubforumButton.setOnClickListener(subforumButtonClickedListener);

                layoutListOfSubforums.addView(newSubforumButton);
            }
        } else {
            subforumsCardView.setVisibility(View.GONE);
        }
    }

    private void stopAllCurrentTasks() {
        if (currentTaskForDownload != null) {
            currentTaskForDownload.clearListenersAndCancel();
            currentTaskForDownload = null;
        }
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showforuminfos);
        initToolbar(R.id.toolbar_showforuminfos);

        backgroundErrorText = findViewById(R.id.text_errorbackgroundmessage_showforuminfos);
        swipeRefresh = findViewById(R.id.swiperefresh_showforuminfos);
        mainScrollView = findViewById(R.id.scrollview_showforuminfos);
        subforumsCardView = findViewById(R.id.subforum_card_showforuminfos);
        layoutListOfSubforums = findViewById(R.id.subforum_list_showforuminfos);

        backgroundErrorText.setVisibility(View.GONE);
        swipeRefresh.setEnabled(false);
        swipeRefresh.setColorSchemeResources(R.color.colorControlHighlightThemeLight);
        subforumsCardView.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            listOfSubforums = savedInstanceState.getParcelableArrayList(SAVE_LIST_OF_SUBFORUMS);
            updateDisplayedInfos();

            mainScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mainScrollView.scrollTo(0, savedInstanceState.getInt(SAVE_SCROLL_POSITION, 0));
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (listOfSubforums == null) {
            if (getIntent() != null) {
                if (getIntent().getStringExtra(EXTRA_FORUM_LINK) != null && getIntent().getStringExtra(EXTRA_COOKIES) != null) {
                    currentTaskForDownload = new DownloadForumInfos();
                    currentTaskForDownload.setRequestIsStartedListener(requestIsStartedListener);
                    currentTaskForDownload.setRequestIsFinishedListener(downloadInfosIsFinishedListener);
                    currentTaskForDownload.execute(getIntent().getStringExtra(EXTRA_FORUM_LINK), getIntent().getStringExtra(EXTRA_COOKIES));
                }
            }

            if (currentTaskForDownload == null) {
                backgroundErrorText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_LIST_OF_SUBFORUMS, listOfSubforums);
        outState.putInt(SAVE_SCROLL_POSITION, mainScrollView.getScrollY());
    }

    private static class DownloadForumInfos extends AbsWebRequestAsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            if (params.length > 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[1], false);
                return WebManager.sendRequest(params[0], "GET", "", currentWebInfos);
            }
            return null;
        }
    }
}
