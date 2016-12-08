package com.franckrj.respawnirc.jvctopictools;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.JVCParser;

import java.util.ArrayList;

public class ShowForumFragment extends Fragment {
    public static final String ARG_FORUM_LINK = "com.franckrj.respawnirc.showtopicfragment.forum_link";

    private SharedPreferences sharedPref = null;
    private SwipeRefreshLayout swipeRefresh = null;
    private NewTopicWantRead listenerForNewTopicWantRead = null;
    private JVCTopicGetter getterForTopics = null;
    private ListView jvcTopicList = null;
    private JVCTopicsAdapter adapterForTopics;
    private Button firstPageButton = null;
    private Button previousPageButton = null;
    private Button currentPageButton = null;
    private Button nextPageButton = null;
    private int currentPage = 0;
    private boolean clearTopicsOnRefresh = true;

    private final Button.OnClickListener changePageWithNavigationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (buttonView == firstPageButton && firstPageButton.getVisibility() == View.VISIBLE) {
                goToThisNewPage(JVCParser.setPageNumberForThisForumLink(getterForTopics.getUrlForForum(), 1));
            } else if (buttonView == previousPageButton && previousPageButton.getVisibility() == View.VISIBLE) {
                goToThisNewPage(JVCParser.setPageNumberForThisForumLink(getterForTopics.getUrlForForum(), currentPage - 25));
            }  else if (buttonView == nextPageButton && nextPageButton.getVisibility() == View.VISIBLE) {
                goToThisNewPage(JVCParser.setPageNumberForThisForumLink(getterForTopics.getUrlForForum(), currentPage + 25));
            }
        }
    };

    private final AdapterView.OnItemClickListener listenerForItemClickedInListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                listenerForNewTopicWantRead.setReadNewTopic(adapterForTopics.getItem(position).link, adapterForTopics.getItem(position).name);
            }
        }
    };

    private final AdapterView.OnItemLongClickListener listenerForItemLongClickedInListView = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                String realPageToGo = JVCParser.setPageNumberForThisTopicLink(adapterForTopics.getItem(position).link, (Integer.parseInt(adapterForTopics.getItem(position).messages) / 20) + 1);
                listenerForNewTopicWantRead.setReadNewTopic(realPageToGo, adapterForTopics.getItem(position).name);
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
                adapterForTopics.removeAllItems();

                for (JVCParser.TopicInfos thisTopicInfo : listOfNewTopics) {
                    adapterForTopics.addItem(thisTopicInfo);
                }

                adapterForTopics.updateAllItems();
            } else {
                Toast.makeText(getActivity(), R.string.errorDownloadFailed, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private final SwipeRefreshLayout.OnRefreshListener listenerForRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (!reloadAllForum()) {
                swipeRefresh.setRefreshing(false);
            }
        }
    };

    private void updatePageButtons() {
        firstPageButton.setVisibility(View.GONE);
        previousPageButton.setVisibility(View.GONE);
        currentPageButton.setText(R.string.waitingText);
        nextPageButton.setVisibility(View.GONE);

        if (currentPage > 0) {
            currentPageButton.setText(String.valueOf(((currentPage - 1) / 25) + 1));
            nextPageButton.setVisibility(View.VISIBLE);

            if (currentPage > 1) {
                firstPageButton.setVisibility(View.VISIBLE);
                firstPageButton.setText(String.valueOf(1));
                previousPageButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void reloadSettings() {
        getterForTopics.setCookieListInAString(sharedPref.getString(getString(R.string.prefCookiesList), ""));
        clearTopicsOnRefresh = true;
    }

    private boolean reloadAllForum() {
        if (clearTopicsOnRefresh) {
            adapterForTopics.removeAllItems();
            adapterForTopics.updateAllItems();
        }
        return getterForTopics.reloadForum();
    }

    public void goToThisNewPage(String newPageToGo) {
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        String currentPageNumber;

        if (!newPageToGo.isEmpty()) {
            newPageToGo = JVCParser.formatThisUrl(newPageToGo);
        }
        currentPageNumber = JVCParser.getPageNumberForThisForumLink(newPageToGo);

        sharedPrefEdit.putString(getString(R.string.prefForumUrlToFetch), newPageToGo);
        sharedPrefEdit.apply();

        if (!currentPageNumber.isEmpty()) {
            currentPage = Integer.parseInt(currentPageNumber);
        } else {
            currentPage = 0;
        }

        updatePageButtons();
        getterForTopics.stopAllCurrentTask();
        adapterForTopics.removeAllItems();
        adapterForTopics.updateAllItems();
        getterForTopics.startGetMessagesOfThisPage(newPageToGo);
    }

    public void setForumByTopicLink(String topicLink) {
        goToThisNewPage(JVCParser.getForumForTopicLink(JVCParser.formatThisUrl(topicLink)));
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
        firstPageButton = (Button) mainView.findViewById(R.id.firstpage_button_showforum);
        previousPageButton = (Button) mainView.findViewById(R.id.previouspage_button_showforum);
        currentPageButton = (Button) mainView.findViewById(R.id.currentpage_button_showforum);
        nextPageButton = (Button) mainView.findViewById(R.id.nextpage_button_showforum);

        swipeRefresh.setOnRefreshListener(listenerForRefresh);
        firstPageButton.setVisibility(View.GONE);
        firstPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        previousPageButton.setVisibility(View.GONE);
        previousPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        nextPageButton.setVisibility(View.GONE);
        nextPageButton.setOnClickListener(changePageWithNavigationButtonListener);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPref = getActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        getterForTopics = new JVCTopicGetter(getActivity());
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

        swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        jvcTopicList.setAdapter(adapterForTopics);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.TopicInfos> allCurrentTopicsShowed = savedInstanceState.getParcelableArrayList(getString(R.string.saveAllCurrentTopicsShowed));
            getterForTopics.loadFromBundle(savedInstanceState);

            if (allCurrentTopicsShowed != null) {
                for (JVCParser.TopicInfos thisTopicInfo : allCurrentTopicsShowed) {
                    adapterForTopics.addItem(thisTopicInfo);
                }
            }

            adapterForTopics.updateAllItems();
            currentPage = Integer.parseInt(JVCParser.getPageNumberForThisForumLink(getterForTopics.getUrlForForum()));
            updatePageButtons();
        } else {
            Bundle currentArgs = getArguments();

            if (currentArgs != null) {
                String forumLink = currentArgs.getString(ARG_FORUM_LINK, "");
                goToThisNewPage(forumLink);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadSettings();

        if (getterForTopics.getUrlForForum().isEmpty()) {
            goToThisNewPage(sharedPref.getString(getString(R.string.prefForumUrlToFetch), ""));
        } else if (adapterForTopics.getAllItems().isEmpty()) {
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
        outState.putParcelableArrayList(getString(R.string.saveAllCurrentTopicsShowed), adapterForTopics.getAllItems());
        getterForTopics.saveToBundle(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_showforum, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reload_forum_showforum:
                reloadAllForum();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface NewTopicWantRead {
        void setReadNewTopic(String newTopicLink, String newTopicName);
    }
}
