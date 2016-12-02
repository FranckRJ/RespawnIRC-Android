package com.franckrj.respawnirc.jvctopictools;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    private View loadingLayout = null;
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

    private final AdapterView.OnItemClickListener listenerForItemClickedInListViw = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                listenerForNewTopicWantRead.setReadNewTopic(adapterForTopics.getItem(position).link);
            }
        }
    };

    private final JVCTopicGetter.NewGetterStateListener listenerForNewGetterState = new JVCTopicGetter.NewGetterStateListener() {
        @Override
        public void newStateSetted(int newState) {
            if (adapterForTopics.getAllItems().isEmpty()) {
                if (newState == JVCTopicGetter.STATE_LOADING) {
                    loadingLayout.setVisibility(View.VISIBLE);
                } else if (newState == JVCTopicGetter.STATE_NOT_LOADING) {
                    loadingLayout.setVisibility(View.GONE);
                }
            }
        }
    };

    private final JVCTopicGetter.NewTopicsListener listenerForNewTopics = new JVCTopicGetter.NewTopicsListener() {
        @Override
        public void getNewTopics(ArrayList<JVCParser.TopicInfos> listOfNewTopics) {
            if (!listOfNewTopics.isEmpty()) {
                loadingLayout.setVisibility(View.GONE);
                adapterForTopics.removeAllItems();

                for (JVCParser.TopicInfos thisTopicInfo : listOfNewTopics) {
                    adapterForTopics.addItem(thisTopicInfo);
                }

                adapterForTopics.updateAllItems();
            } else {
                Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof NewTopicWantRead) {
            listenerForNewTopicWantRead = (NewTopicWantRead) activity;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof NewTopicWantRead) {
            listenerForNewTopicWantRead = (NewTopicWantRead) context;
        }
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
        loadingLayout = mainView.findViewById(R.id.layout_loading_showforum);
        firstPageButton = (Button) mainView.findViewById(R.id.firstpage_button_showforum);
        previousPageButton = (Button) mainView.findViewById(R.id.previouspage_button_showforum);
        currentPageButton = (Button) mainView.findViewById(R.id.currentpage_button_showforum);
        nextPageButton = (Button) mainView.findViewById(R.id.nextpage_button_showforum);

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
        jvcTopicList.setOnItemClickListener(listenerForItemClickedInListViw);

        loadingLayout.setVisibility(View.GONE);
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
                if (clearTopicsOnRefresh) {
                    adapterForTopics.removeAllItems();
                    adapterForTopics.updateAllItems();
                }
                getterForTopics.reloadForum();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface NewTopicWantRead {
        void setReadNewTopic(String newTopicLink);
    }
}
