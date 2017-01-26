package com.franckrj.respawnirc.jvctopictools;

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

import com.franckrj.respawnirc.jvcviewers.AbsShowSomethingFragment;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class ShowForumFragment extends AbsShowSomethingFragment {
    public static final String ARG_FORUM_LINK = "com.franckrj.respawnirc.showtopicfragment.forum_link";

    private static final String SAVE_ALL_TOPICS_SHOWED = "saveAllCurrentTopicsShowed";

    private SharedPreferences sharedPref = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private NewTopicWantRead listenerForNewTopicWantRead = null;
    private JVCTopicGetter getterForTopics = null;
    private ListView jvcTopicList = null;
    private JVCTopicsAdapter adapterForTopics;
    private boolean clearTopicsOnRefresh = true;
    private boolean isInErrorMode = false;

    private final AdapterView.OnItemClickListener listenerForItemClickedInListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                listenerForNewTopicWantRead.setReadNewTopic(adapterForTopics.getItem(position).link, JVCParser.specialCharToNormalChar(adapterForTopics.getItem(position).htmlName));
            }
        }
    };

    private final AdapterView.OnItemLongClickListener listenerForItemLongClickedInListView = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                String realPageToGo = JVCParser.setPageNumberForThisTopicLink(adapterForTopics.getItem(position).link, (Integer.parseInt(adapterForTopics.getItem(position).messages) / 20) + 1);
                listenerForNewTopicWantRead.setReadNewTopic(realPageToGo, JVCParser.specialCharToNormalChar(adapterForTopics.getItem(position).htmlName));
                return true;
            }
            return false;
        }
    };

    private final JVCTopicGetter.NewGetterStateListener listenerForNewGetterState = new JVCTopicGetter.NewGetterStateListener() {
        @Override
        public void newStateSetted(int newState) {
            if (newState == JVCTopicGetter.STATE_LOADING) {
                swipeRefresh.setRefreshing(true);
            } else if (newState == JVCTopicGetter.STATE_NOT_LOADING) {
                swipeRefresh.setRefreshing(false);
            }
        }
    };

    private final JVCTopicGetter.NewTopicsListener listenerForNewTopics = new JVCTopicGetter.NewTopicsListener() {
        @Override
        public void getNewTopics(ArrayList<JVCParser.TopicInfos> listOfNewTopics) {
            if (!listOfNewTopics.isEmpty()) {
                isInErrorMode = false;
                adapterForTopics.removeAllItems();

                for (JVCParser.TopicInfos thisTopicInfo : listOfNewTopics) {
                    adapterForTopics.addItem(thisTopicInfo);
                }

                adapterForTopics.updateAllItems();
            } else {
                if (!isInErrorMode) {
                    getterForTopics.reloadForum();
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
        getterForTopics.setCookieListInAString(sharedPref.getString(getString(R.string.prefCookiesList), ""));
        clearTopicsOnRefresh = true;
    }

    private boolean reloadAllForum(boolean forceDontClear) {
        isInErrorMode = false;
        if (clearTopicsOnRefresh && !forceDontClear) {
            adapterForTopics.removeAllItems();
            adapterForTopics.updateAllItems();
        }
        return getterForTopics.reloadForum();
    }

    public void refreshForum() {
        reloadAllForum(true);
    }

    public void setPageLink(String newForumPageLink) {
        isInErrorMode = false;

        if (!newForumPageLink.isEmpty()) {
            newForumPageLink = JVCParser.formatThisUrl(newForumPageLink);
        }

        getterForTopics.stopAllCurrentTask();
        adapterForTopics.removeAllItems();
        adapterForTopics.updateAllItems();
        getterForTopics.startGetMessagesOfThisPage(newForumPageLink);
    }

    public void clearContent() {
        getterForTopics.stopAllCurrentTask();
        adapterForTopics.removeAllItems();
        adapterForTopics.updateAllItems();
        getterForTopics.startGetMessagesOfThisPage("");
    }

    public JVCParser.AjaxInfos getLatestAjaxInfos() {
        return getterForTopics.getLatestAjaxInfos();
    }

    public Boolean getIsInFavs() {
        return getterForTopics.getIsInFavs();
    }

    public String getLatestListOfInputInAString() {
        return getterForTopics.getLatestListOfInputInAString();
    }

    public void setIsInFavs(Boolean newVal) {
        getterForTopics.setIsInFavs(newVal);
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

        getterForTopics = new JVCTopicGetter();
        adapterForTopics = new JVCTopicsAdapter(getActivity());
        reloadSettings();
        getterForTopics.setListenerForNewGetterState(listenerForNewGetterState);
        getterForTopics.setListenerForNewTopics(listenerForNewTopics);
        adapterForTopics.setAlternateBackgroundColor(true);
        jvcTopicList.setOnItemClickListener(listenerForItemClickedInListView);
        jvcTopicList.setOnItemLongClickListener(listenerForItemLongClickedInListView);

        if (getActivity() instanceof NewTopicWantRead) {
            listenerForNewTopicWantRead = (NewTopicWantRead) getActivity();
        }
        if (getActivity() instanceof JVCTopicGetter.NewForumNameAvailable) {
            getterForTopics.setListenerForNewForumName((JVCTopicGetter.NewForumNameAvailable) getActivity());
        }
        if (getActivity() instanceof JVCTopicGetter.ForumLinkChanged) {
            getterForTopics.setListenerForForumLinkChanged((JVCTopicGetter.ForumLinkChanged) getActivity());
        }

        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        jvcTopicList.setAdapter(adapterForTopics);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.TopicInfos> allCurrentTopicsShowed = savedInstanceState.getParcelableArrayList(SAVE_ALL_TOPICS_SHOWED);
            getterForTopics.loadFromBundle(savedInstanceState);

            if (allCurrentTopicsShowed != null) {
                for (JVCParser.TopicInfos thisTopicInfo : allCurrentTopicsShowed) {
                    adapterForTopics.addItem(thisTopicInfo);
                }
            }

            adapterForTopics.updateAllItems();
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

        if (adapterForTopics.getAllItems().isEmpty()) {
            getterForTopics.reloadForum();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getterForTopics.stopAllCurrentTask();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_ALL_TOPICS_SHOWED, adapterForTopics.getAllItems());
        getterForTopics.saveToBundle(outState);
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
        void setReadNewTopic(String newTopicLink, String newTopicName);
    }
}
