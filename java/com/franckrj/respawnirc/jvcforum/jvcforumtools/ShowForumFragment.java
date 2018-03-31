package com.franckrj.respawnirc.jvcforum.jvcforumtools;

import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.base.AbsShowSomethingFragment;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.IgnoreListManager;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.ThemeManager;

import java.util.ArrayList;

public class ShowForumFragment extends AbsShowSomethingFragment {
    public static final String ARG_FORUM_LINK = "com.franckrj.respawnirc.showtopicfragment.forum_link";
    public static final String ARG_IS_IN_SEARCH_MODE = "com.franckrj.respawnirc.ARG_IS_IN_SEARCH_MODE";

    private static final String SAVE_ALL_TOPICS_SHOWED = "saveAllCurrentTopicsShowed";
    private static final String SAVE_TOPICS_ARE_FROM_IGNORED_PSEUDOS = "saveTopicsAreFromIgnoredPseudos";

    private String pseudoOfUserInLC = "";
    private SwipeRefreshLayout swipeRefresh = null;
    private TextView errorBackgroundMessage = null;
    private NewTopicWantRead listenerForNewTopicWantRead = null;
    private JVCForumGetter getterForForum = null;
    private ListView jvcTopicList = null;
    private JVCForumAdapter adapterForForum;
    private boolean allTopicsShowedAreFromIgnoredPseudos = false;
    private boolean ignoreTopicToo = true;
    private boolean clearTopicsOnRefresh = true;
    private boolean isInErrorMode = false;
    private @ColorInt int currentTopicNameColor = 0;
    private @ColorInt int currentAltColor = 0;

    private final AdapterView.OnItemClickListener listenerForItemClickedInListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                JVCParser.TopicInfos currentItem = adapterForForum.getItem(position);
                if (currentItem.type.equals("message")) {
                    listenerForNewTopicWantRead.setReadNewTopic(currentItem.link, "", "", false);
                } else {
                    listenerForNewTopicWantRead.setReadNewTopic(currentItem.link, JVCParser.specialCharToNormalChar(currentItem.htmlName), currentItem.author, false);
                }
            }
        }
    };

    private final AdapterView.OnItemLongClickListener listenerForItemLongClickedInListView = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listenerForNewTopicWantRead != null) {
                JVCParser.TopicInfos currentItem = adapterForForum.getItem(position);
                if (currentItem.type.equals("message")) {
                    listenerForNewTopicWantRead.setReadNewTopic(currentItem.link, "", "", false);
                } else {
                    String realPageToGo = JVCParser.setPageNumberForThisTopicLink(currentItem.link, (Integer.parseInt(currentItem.nbOfMessages) / 20) + 1);
                    listenerForNewTopicWantRead.setReadNewTopic(realPageToGo, JVCParser.specialCharToNormalChar(currentItem.htmlName), currentItem.author, true);
                }
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
                errorBackgroundMessage.setVisibility(View.GONE);
            } else if (newState == JVCForumGetter.STATE_NOT_LOADING) {
                swipeRefresh.setRefreshing(false);
            }
        }
    };

    private final JVCForumGetter.NewTopicsListener listenerForNewTopics = new JVCForumGetter.NewTopicsListener() {
        @Override
        public void getNewTopics(ArrayList<JVCParser.TopicInfos> listOfNewTopics) {
            if (getterForForum.getIsInSearchMode() && getterForForum.getLastTypeOfError() == JVCForumGetter.ErrorType.SEARCH_IS_EMPTY_AND_ITS_NOT_A_FAIL) {
                allTopicsShowedAreFromIgnoredPseudos = false;
                errorBackgroundMessage.setText(R.string.noResultFound);
                errorBackgroundMessage.setVisibility(View.VISIBLE);
            } else if (!listOfNewTopics.isEmpty()) {
                isInErrorMode = false;
                adapterForForum.removeAllItems();

                for (JVCParser.TopicInfos thisTopicInfo : listOfNewTopics) {
                    if (ignoreTopicToo) {
                        String pseudoOfTopicInLC = thisTopicInfo.author.toLowerCase();

                        if (!pseudoOfTopicInLC.equals(pseudoOfUserInLC) && IgnoreListManager.pseudoInLCIsIgnored(pseudoOfTopicInLC)) {
                            continue;
                        }
                    }

                    adapterForForum.addItem(thisTopicInfo);
                }

                adapterForForum.notifyDataSetChanged();

                if (adapterForForum.getAllItems().isEmpty()) {
                    allTopicsShowedAreFromIgnoredPseudos = true;
                    errorBackgroundMessage.setText(R.string.allTopicsAreFromIgnoredPseudo);
                    errorBackgroundMessage.setVisibility(View.VISIBLE);
                } else {
                    allTopicsShowedAreFromIgnoredPseudos = false;
                }
            } else {
                allTopicsShowedAreFromIgnoredPseudos = false;

                if (!isInErrorMode) {
                    getterForForum.reloadForum(true);
                    isInErrorMode = true;
                } else {
                    int idOfErrorTextToShow;

                    switch (getterForForum.getLastTypeOfError()) {
                        case FORUM_DOES_NOT_EXIST:
                            idOfErrorTextToShow = R.string.errorForumDoesNotExist;
                            break;
                        default:
                            idOfErrorTextToShow = R.string.errorForumPageDownloadFailed;
                            break;
                    }

                    if (adapterForForum.getAllItems().isEmpty()) {
                        errorBackgroundMessage.setText(idOfErrorTextToShow);
                        errorBackgroundMessage.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getActivity(), idOfErrorTextToShow, Toast.LENGTH_SHORT).show();
                    }
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

    private void reloadAdapterSettings() {
        adapterForForum.setAlternateBackgroundColor(PrefsManager.getBool(PrefsManager.BoolPref.Names.FORUM_ALTERNATE_BACKGROUND));
        adapterForForum.setTopicTitleSizeInSp(Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_TITLE_FONT_SIZE)));
        adapterForForum.setTopicInfosSizeInSp(Integer.parseInt(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_INFOS_FONT_SIZE)));
    }

    private void reloadSettings() {
        reloadAdapterSettings();
        getterForForum.setCookieListInAString(PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST));
        pseudoOfUserInLC = PrefsManager.getString(PrefsManager.StringPref.Names.PSEUDO_OF_USER).toLowerCase();
        ignoreTopicToo = PrefsManager.getBool(PrefsManager.BoolPref.Names.IGNORE_TOPIC_TOO);
        currentTopicNameColor = ThemeManager.getColorInt(R.attr.themedTopicNameColor, getActivity());
        currentAltColor = ThemeManager.getColorInt(R.attr.themedAltBackgroundColor, getActivity());
        clearTopicsOnRefresh = true;
    }

    private boolean reloadAllForum(boolean forceDontClear) {
        isInErrorMode = false;
        if (clearTopicsOnRefresh && !forceDontClear) {
            adapterForForum.removeAllItems();
            adapterForForum.notifyDataSetChanged();
        }
        return getterForForum.reloadForum();
    }

    private void recreateAdapterForForum() {
        ArrayList<JVCParser.TopicInfos> allCurrentTopicsShowed = adapterForForum.getAllItems();

        adapterForForum = new JVCForumAdapter(getActivity());
        reloadAdapterSettings();
        jvcTopicList.setAdapter(adapterForForum);

        for (JVCParser.TopicInfos thisTopicInfo : allCurrentTopicsShowed) {
            adapterForForum.addItem(thisTopicInfo);
        }
        adapterForForum.notifyDataSetChanged();
    }

    @Override
    public void refreshContent() {
        reloadAllForum(true);
    }

    public void updateForumStatusInfos(JVCForumGetter.ForumStatusInfos newForumStatusInfos) {
        getterForForum.updateForumStatusInfos(newForumStatusInfos);
    }

    @Override
    public void setPageLink(String newForumPageLink) {
        setPageLink(newForumPageLink, true);
    }

    public void setPageLink(String newForumPageLink, boolean startLoadingPage) {
        isInErrorMode = false;

        if (!newForumPageLink.isEmpty()) {
            newForumPageLink = JVCParser.formatThisUrlToClassicJvcUrl(newForumPageLink);
        }

        getterForForum.stopAllCurrentTask();
        adapterForForum.removeAllItems();
        adapterForForum.notifyDataSetChanged();

        if (startLoadingPage) {
            getterForForum.startGetMessagesOfThisPage(newForumPageLink);
        } else {
            getterForForum.setUrlForForumWithoutLoading(newForumPageLink);
        }
    }

    @Override
    public void clearContent() {
        getterForForum.stopAllCurrentTask();
        adapterForForum.removeAllItems();
        adapterForForum.notifyDataSetChanged();
        getterForForum.startGetMessagesOfThisPage("");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mainView = inflater.inflate(R.layout.fragment_showforum, container, false);

        jvcTopicList = mainView.findViewById(R.id.jvctopic_view_showforumfrag);
        swipeRefresh = mainView.findViewById(R.id.swiperefresh_showforumfrag);
        errorBackgroundMessage = mainView.findViewById(R.id.text_errorbackgroundmessage_showforumfrag);

        swipeRefresh.setOnRefreshListener(listenerForRefresh);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getterForForum = new JVCForumGetter();
        adapterForForum = new JVCForumAdapter(getActivity());
        reloadSettings();
        getterForForum.setListenerForNewGetterState(listenerForNewGetterState);
        getterForForum.setListenerForNewTopics(listenerForNewTopics);
        jvcTopicList.setOnItemClickListener(listenerForItemClickedInListView);
        jvcTopicList.setOnItemLongClickListener(listenerForItemLongClickedInListView);

        if (getActivity() instanceof NewTopicWantRead) {
            listenerForNewTopicWantRead = (NewTopicWantRead) getActivity();
        }
        if (getActivity() instanceof JVCForumGetter.ForumLinkChanged) {
            getterForForum.setListenerForForumLinkChanged((JVCForumGetter.ForumLinkChanged) getActivity());
        }
        if (getActivity() instanceof JVCForumGetter.NewForumStatusListener) {
            getterForForum.setListenerForNewForumStatus((JVCForumGetter.NewForumStatusListener) getActivity());
        }

        errorBackgroundMessage.setVisibility(View.GONE);
        swipeRefresh.setColorSchemeResources(R.color.colorControlHighlightThemeLight);
        jvcTopicList.setAdapter(adapterForForum);

        if (savedInstanceState != null) {
            ArrayList<JVCParser.TopicInfos> allCurrentTopicsShowed = savedInstanceState.getParcelableArrayList(SAVE_ALL_TOPICS_SHOWED);
            allTopicsShowedAreFromIgnoredPseudos = savedInstanceState.getBoolean(SAVE_TOPICS_ARE_FROM_IGNORED_PSEUDOS, false);
            getterForForum.loadFromBundle(savedInstanceState);
            swipeRefresh.setEnabled(!getterForForum.getIsInSearchMode());

            if (allCurrentTopicsShowed != null) {
                for (JVCParser.TopicInfos thisTopicInfo : allCurrentTopicsShowed) {
                    adapterForForum.addItem(thisTopicInfo);
                }
            }
            adapterForForum.notifyDataSetChanged();

            if (adapterForForum.getAllItems().isEmpty()) {
                if (getterForForum.getIsInSearchMode() && getterForForum.getLastTypeOfError() == JVCForumGetter.ErrorType.SEARCH_IS_EMPTY_AND_ITS_NOT_A_FAIL) {
                    errorBackgroundMessage.setText(R.string.noResultFound);
                    errorBackgroundMessage.setVisibility(View.VISIBLE);
                } else if (!getterForForum.getIsInSearchMode() && allTopicsShowedAreFromIgnoredPseudos) {
                    errorBackgroundMessage.setText(R.string.allTopicsAreFromIgnoredPseudo);
                    errorBackgroundMessage.setVisibility(View.VISIBLE);
                }
            }
        } else {
            Bundle currentArgs = getArguments();

            if (currentArgs != null) {
                getterForForum.setIsInSearchMode(currentArgs.getBoolean(ARG_IS_IN_SEARCH_MODE, false));
                swipeRefresh.setEnabled(!getterForForum.getIsInSearchMode());
                setPageLink(currentArgs.getString(ARG_FORUM_LINK, ""), !dontLoadOnFirstTime);
                dontLoadOnFirstTime = false;
            }
        }

        setHasOptionsMenu(!getterForForum.getIsInSearchMode());
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean oldAlternateBackgroundColor = adapterForForum.getAlternateBackgroundColor();
        int oldTopicTitleSizeInSp = adapterForForum.getTopicTitleSizeInSp();
        int oldTopicInfosSizeInSp = adapterForForum.getTopicInfosSizeInSp();
        @ColorInt int oldTopicNameColor = currentTopicNameColor;
        @ColorInt int oldAltColor = currentAltColor;
        reloadSettings();
        isInErrorMode = false;

        /* Lors d'un changement de taille de police les vues ne sont pas bien resize. La seule solution qui semble fonctionner
         * c'est de tout recréer, invalider les vues ou faire un requestLayout ne marche pas (au moins sous 4.0.4). */
        if (oldTopicTitleSizeInSp != adapterForForum.getTopicTitleSizeInSp() ||
                oldTopicInfosSizeInSp != adapterForForum.getTopicInfosSizeInSp()) {
            recreateAdapterForForum();
        } else {
            if (oldTopicNameColor != currentTopicNameColor) {
                adapterForForum.recreateAllItems();
            }

            if (oldAlternateBackgroundColor != adapterForForum.getAlternateBackgroundColor() ||
                    oldTopicNameColor != currentTopicNameColor || oldAltColor != currentAltColor) {
                adapterForForum.notifyDataSetChanged();
            }
        }

        if (adapterForForum.getAllItems().isEmpty() && !allTopicsShowedAreFromIgnoredPseudos &&
                (!getterForForum.getIsInSearchMode() || (getterForForum.getIsInSearchMode() && getterForForum.getLastTypeOfError() != JVCForumGetter.ErrorType.SEARCH_IS_EMPTY_AND_ITS_NOT_A_FAIL))) {
            getterForForum.reloadForum();
        }
    }

    @Override
    public void onPause() {
        getterForForum.stopAllCurrentTask();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_ALL_TOPICS_SHOWED, adapterForForum.getAllItems());
        outState.putBoolean(SAVE_TOPICS_ARE_FROM_IGNORED_PSEUDOS, allTopicsShowedAreFromIgnoredPseudos);
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
        void setReadNewTopic(String newTopicLink, String newTopicName, String pseudoOfAuthor, boolean fromLongClick);
    }
}
