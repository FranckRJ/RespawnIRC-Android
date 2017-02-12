package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.franckrj.respawnirc.AbsShowSomethingFragment;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;

import java.util.ArrayList;

public class ShowForumFragment extends AbsShowSomethingFragment {
    public static final String ARG_FORUM_LINK = "com.franckrj.respawnirc.showtopicfragment.forum_link";

    private static final String SAVE_ALL_TOPICS_SHOWED = "saveAllCurrentTopicsShowed";

    private SharedPreferences sharedPref = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private NewTopicWantRead listenerForNewTopicWantRead = null;
    private JVCForumGetter getterForForum = null;
    private ListView jvcTopicList = null;
    private JVCForumAdapter adapterForForum;
    private boolean clearTopicsOnRefresh = true;
    private boolean isInErrorMode = false;

    private final AdapterView.OnItemClickListener listenerForItemClickedInListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                listenerForNewTopicWantRead.setReadNewTopic(adapterForForum.getItem(position).link, JVCParser.specialCharToNormalChar(adapterForForum.getItem(position).htmlName), false);
            }
        }
    };

    private final AdapterView.OnItemLongClickListener listenerForItemLongClickedInListView = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                String realPageToGo = JVCParser.setPageNumberForThisTopicLink(adapterForForum.getItem(position).link, (Integer.parseInt(adapterForForum.getItem(position).messages) / 20) + 1);
                listenerForNewTopicWantRead.setReadNewTopic(realPageToGo, JVCParser.specialCharToNormalChar(adapterForForum.getItem(position).htmlName), true);
                return true;
            }
            return false;
        }
    };

    private final JVCForumGetter.NewGetterStateListener listenerForNewGetterState = new JVCForumGetter.NewGetterStateListener() {
        @Override
        public void newStateSetted(int newState) {
            if (newState == JVCForumGetter.STATE_LOADING) {
                swipeRefresh.setRefreshing(true);
            } else if (newState == JVCForumGetter.STATE_NOT_LOADING) {
                swipeRefresh.setRefreshing(false);
            }
        }
    };

    private final JVCForumGetter.NewTopicsListener listenerForNewTopics = new JVCForumGetter.NewTopicsListener() {
        @Override
        public void getNewTopics(ArrayList<JVCParser.TopicInfos> listOfNewTopics) {
            if (!listOfNewTopics.isEmpty()) {
                isInErrorMode = false;
                adapterForForum.removeAllItems();

                for (JVCParser.TopicInfos thisTopicInfo : listOfNewTopics) {
                    adapterForForum.addItem(thisTopicInfo);
                }

                adapterForForum.updateAllItems();
            } else {
                if (!isInErrorMode) {
                    getterForForum.reloadForum();
                    isInErrorMode = true;
                } else {
                    Toast.makeText(getActivity(), R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private final SwipeRefreshLayout.OnRefreshListener listenerForRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (!reloadAllForum(false)) {
                swipeRefresh.setRefreshing(false);
            }
        }
    };

    private void reloadSettings() {
        getterForForum.setCookieListInAString(PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST));
        clearTopicsOnRefresh = true;
    }

    private boolean reloadAllForum(boolean forceDontClear) {
        isInErrorMode = false;
        if (clearTopicsOnRefresh && !forceDontClear) {
            adapterForForum.removeAllItems();
            adapterForForum.updateAllItems();
        }
        return getterForForum.reloadForum();
    }

    public void refreshForum() {
        reloadAllForum(true);
    }

    public void setPageLink(String newForumPageLink) {
        isInErrorMode = false;

        if (!newForumPageLink.isEmpty()) {
            newForumPageLink = JVCParser.formatThisUrl(newForumPageLink);
        }

        getterForForum.stopAllCurrentTask();
        adapterForForum.removeAllItems();
        adapterForForum.updateAllItems();
        getterForForum.startGetMessagesOfThisPage(newForumPageLink);
    }

    public void clearContent() {
        getterForForum.stopAllCurrentTask();
        adapterForForum.removeAllItems();
        adapterForForum.updateAllItems();
        getterForForum.startGetMessagesOfThisPage("");
    }

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return getterForForum.getLatestAjaxInfos();
    }

    public Boolean getIsInFavs() {
        return getterForForum.getIsInFavs();
    }

    public String getLatestListOfInputInAString() {
        return getterForForum.getLatestListOfInputInAString();
    }

    public void setIsInFavs(Boolean newVal) {
        getterForForum.setIsInFavs(newVal);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.fragment_showforum, container, false);

        jvcTopicList = (ListView) mainView.findViewById(R.id.jvctopic_view_showforum);
        swipeRefresh = (SwipeRefreshLayout) mainView.findViewById(R.id.swiperefresh_showforum);

        swipeRefresh.setOnRefreshListener(listenerForRefresh);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        getterForForum = new JVCForumGetter();
        adapterForForum = new JVCForumAdapter(getActivity());
        reloadSettings();
        getterForForum.setListenerForNewGetterState(listenerForNewGetterState);
        getterForForum.setListenerForNewTopics(listenerForNewTopics);
        adapterForForum.setAlternateBackgroundColor(true);
        jvcTopicList.setOnItemClickListener(listenerForItemClickedInListView);
        jvcTopicList.setOnItemLongClickListener(listenerForItemLongClickedInListView);

        if (getActivity() instanceof NewTopicWantRead) {
            listenerForNewTopicWantRead = (NewTopicWantRead) getActivity();
        }
        if (getActivity() instanceof JVCForumGetter.NewForumNameAvailable) {
            getterForForum.setListenerForNewForumName((JVCForumGetter.NewForumNameAvailable) getActivity());
        }
        if (getActivity() instanceof JVCForumGetter.ForumLinkChanged) {
            getterForForum.setListenerForForumLinkChanged((JVCForumGetter.ForumLinkChanged) getActivity());
        }
        if (getActivity() instanceof JVCForumGetter.NewNumberOfMPSetted) {
            getterForForum.setListenerForNewNumberOfMP((JVCForumGetter.NewNumberOfMPSetted) getActivity());
        }

        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        jvcTopicList.setAdapter(adapterForForum);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.TopicInfos> allCurrentTopicsShowed = savedInstanceState.getParcelableArrayList(SAVE_ALL_TOPICS_SHOWED);
            getterForForum.loadFromBundle(savedInstanceState);

            if (allCurrentTopicsShowed != null) {
                for (JVCParser.TopicInfos thisTopicInfo : allCurrentTopicsShowed) {
                    adapterForForum.addItem(thisTopicInfo);
                }
            }

            adapterForForum.updateAllItems();
        } else {
            Bundle currentArgs = getArguments();

            if (currentArgs != null) {
                String forumLink = currentArgs.getString(ARG_FORUM_LINK, "");
                setPageLink(forumLink);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadSettings();
        isInErrorMode = false;

        if (adapterForForum.getAllItems().isEmpty()) {
            getterForForum.reloadForum();
        }
    }

    @Override
    public void onPause() {
        getterForForum.stopAllCurrentTask();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_ALL_TOPICS_SHOWED, adapterForForum.getAllItems());
        getterForForum.saveToBundle(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload_forum_showforum:
                reloadAllForum(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface NewTopicWantRead {
        void setReadNewTopic(String newTopicLink, String newTopicName, boolean startAtBottom);
    }
}
