package com.franckrj.respawnirc.jvcforum;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
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

    private static final String SAVE_FORUM_INFOS = "saveForumInfos";
    private static final String SAVE_SCROLL_POSITION = "saveScrollPosition";

    private TextView backgroundErrorText = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private ScrollView mainScrollView = null;
    private CardView subforumsCardView = null;
    private LinearLayout layoutListOfSubforums = null;
    private DownloadForumInfos currentTaskForDownload = null;
    private ForumInfos infosForForum = null;

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

    private final AbsWebRequestAsyncTask.RequestIsFinished<ForumInfos> downloadInfosIsFinishedListener = new AbsWebRequestAsyncTask.RequestIsFinished<ForumInfos>() {
        @Override
        public void onRequestIsFinished(ForumInfos newInfos) {
            swipeRefresh.setRefreshing(false);

            if (newInfos != null) {
                infosForForum = newInfos;
                updateDisplayedInfos();
            } else {
                backgroundErrorText.setVisibility(View.VISIBLE);
            }

            currentTaskForDownload = null;
        }
    };

    private void updateDisplayedInfos() {
        if (infosForForum != null && !infosForForum.listOfSubforums.isEmpty()) {
            subforumsCardView.setVisibility(View.VISIBLE);
            for (JVCParser.NameAndLink nameAndLink : infosForForum.listOfSubforums) {
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
            infosForForum = savedInstanceState.getParcelable(SAVE_FORUM_INFOS);
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

        if (infosForForum == null) {
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
        outState.putParcelable(SAVE_FORUM_INFOS, infosForForum);
        outState.putInt(SAVE_SCROLL_POSITION, mainScrollView.getScrollY());
    }

    private static class DownloadForumInfos extends AbsWebRequestAsyncTask<String, Void, ForumInfos> {
        @Override
        protected ForumInfos doInBackground(String... params) {
            if (params.length > 1) {
                WebManager.WebInfos currentWebInfos = initWebInfos(params[1], false);
                String source = WebManager.sendRequest(params[0], "GET", "", currentWebInfos);

                if (source != null && !source.isEmpty()) {
                    ForumInfos newForumInfos = new ForumInfos();

                    newForumInfos.listOfSubforums = JVCParser.getListOfSubforumsInForumPage(source);
                    return newForumInfos;
                }
            }
            return null;
        }
    }

    private static class ForumInfos implements Parcelable {
        public ArrayList<JVCParser.NameAndLink> listOfSubforums = new ArrayList<>();

        public static final Parcelable.Creator<ForumInfos> CREATOR = new Parcelable.Creator<ForumInfos>() {
            @Override
            public ForumInfos createFromParcel(Parcel in) {
                return new ForumInfos(in);
            }

            @Override
            public ForumInfos[] newArray(int size) {
                return new ForumInfos[size];
            }
        };

        public ForumInfos() {
            //rien
        }

        private ForumInfos(Parcel in) {
            in.readTypedList(listOfSubforums, JVCParser.NameAndLink.CREATOR);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeTypedList(listOfSubforums);
        }
    }
}
