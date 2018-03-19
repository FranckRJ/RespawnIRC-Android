package com.franckrj.respawnirc.jvcforum;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.dialogs.SelectTextDialogFragment;
import com.franckrj.respawnirc.jvctopic.ShowTopicActivity;
import com.franckrj.respawnirc.jvcforum.jvcforumtools.JVCForumGetter;
import com.franckrj.respawnirc.jvcforum.jvcforumtools.ShowForumFragment;
import com.franckrj.respawnirc.base.AbsShowSomethingFragment;
import com.franckrj.respawnirc.PageNavigationUtil;
import com.franckrj.respawnirc.utils.AddOrRemoveThingToFavs;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.base.AbsNavigationViewActivity;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Utils;

public class ShowForumActivity extends AbsNavigationViewActivity implements ShowForumFragment.NewTopicWantRead,
                                                    JVCForumGetter.ForumLinkChanged, PageNavigationUtil.PageNavigationFunctions,
                                                    AddOrRemoveThingToFavs.ActionToFavsEnded, JVCForumGetter.NewForumStatusListener {
    public static final String EXTRA_IS_FIRST_ACTIVITY = "com.franckrj.respawnirc.EXTRA_IS_FIRST_ACTIVITY";
    public static final String EXTRA_NEW_LINK = "com.franckrj.respawnirc.EXTRA_NEW_LINK";
    public static final String EXTRA_FORUM_NAME = "com.franckrj.respawnirc.EXTRA_FORUM_NAME";
    public static final String EXTRA_GO_TO_LAST_PAGE = "com.franckrj.respawnirc.EXTRA_GO_TO_LAST_PAGE";
    public static final String EXTRA_ITS_FIRST_START = "com.franckrj.respawnirc.EXTRA_ITS_FIRST_START";

    private static final int SEND_TOPIC_REQUEST_CODE = 156;
    private static final String SAVE_DRAWER_IS_DISABLED = "saveDrawerIsDisabled";
    private static final String SAVE_FORUM_STATUS = "saveForumStatus";
    private static final String SAVE_CURRENT_FORUM_LINK = "saveCurrentForumLink";
    private static final String SAVE_REFRESH_NEEDED_NEXT_RESUME = "saveRefreshNeededOnNextResume";

    private JVCForumGetter.ForumStatusInfos forumStatus = new JVCForumGetter.ForumStatusInfos();
    private AddOrRemoveThingToFavs currentTaskForFavs = null;
    private PageNavigationUtil pageNavigation = null;
    private ShareActionProvider shareAction = null;
    private boolean refreshNeededOnNextResume = false;
    private boolean dontConsumeRefreshOnNextResume = false;
    private PrefsManager.LinkType linkTypeForInternalBrowser = new PrefsManager.LinkType(PrefsManager.LinkType.NO_LINKS);

    private final View.OnLongClickListener showForumTitleListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Bundle argForFrag = new Bundle();
            SelectTextDialogFragment selectTextDialogFragment = new SelectTextDialogFragment();
            argForFrag.putString(SelectTextDialogFragment.ARG_TEXT_CONTENT, getString(R.string.showForumNames, forumStatus.forumName));
            selectTextDialogFragment.setArguments(argForFrag);
            selectTextDialogFragment.show(getSupportFragmentManager(), "SelectTextDialogFragment");
            return true;
        }
    };

    public ShowForumActivity() {
        super();
        idOfBaseActivity = ITEM_ID_FORUM;
        pageNavigation = new PageNavigationUtil(this);
        pageNavigation.setLastPageNumber(100);
    }

    private void setNewForumLink(String newLink) {
        forumStatus = new JVCForumGetter.ForumStatusInfos();
        forumStatus.forumName = getString(R.string.app_name);
        setTitle(forumStatus.forumName);
        pageNavigation.setCurrentLink(newLink);
        pageNavigation.updateAdapterForPagerView();
        pageNavigation.updateCurrentItemAndButtonsToCurrentLink();
        if (pageNavigation.getCurrentItemIndex() > 0) {
            pageNavigation.clearPageForThisFragment(0);
        }
    }

    private boolean readThisTopic(String link, boolean updateForumFragIfNeeded, String topicName, String pseudoOfAuthor, boolean goToLastPage) {
        if (!JVCParser.getPageNumberForThisTopicLink(link).isEmpty()) {
            Intent newShowTopicIntent = new Intent(this, ShowTopicActivity.class);

            if (updateForumFragIfNeeded) {
                setNewForumLink(JVCParser.getForumForTopicLink(link));
            }

            if (topicName != null) {
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_NAME, topicName);
            }
            if (pseudoOfAuthor != null) {
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_PSEUDO_OF_AUTHOR, pseudoOfAuthor);
            }
            if (!forumStatus.forumName.equals(getString(R.string.app_name))) {
                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_FORUM_NAME, forumStatus.forumName);
            }
            newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_GO_TO_LAST_PAGE, goToLastPage);

            newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, link);
            startActivity(newShowTopicIntent);
            return true;
        }
        return false;
    }

    private boolean readThisForum(String link) {
        if (!JVCParser.getPageNumberForThisForumLink(link).isEmpty()) {
            setNewForumLink(link);
            return true;
        }
        return false;
    }

    private boolean readThisTopicOrForum(String link, boolean goToLastPage) {
        if (link != null) {
            if (!link.isEmpty()) {
                link = JVCParser.formatThisUrlToClassicJvcUrl(link);
            }

            if (JVCParser.checkIfItsForumLink(link)) {
                if (readThisForum(link)) {
                    return false;
                }
            } else if (readThisTopic(link, true, null, null, goToLastPage)) {
                return true;
            }
        }

        Toast.makeText(this, R.string.errorInvalidLink, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void stopAllCurrentTasks() {
        if (currentTaskForFavs != null) {
            currentTaskForFavs.clearListenersAndCancel();
            currentTaskForFavs = null;
        }
    }

    private ShowForumFragment getCurrentFragment() {
        return (ShowForumFragment) pageNavigation.getCurrentFragment();
    }

    /* Retourne vrai si une nouvelle activité a été lancé suite à l'appelle de cette fonction. */
    private boolean consumeIntent(Intent newIntent) {
        boolean newActivityIsLaunched = false;

        if (newIntent != null) {
            String newLinkToGo = newIntent.getStringExtra(EXTRA_NEW_LINK);
            String newForumNameToUse = newIntent.getStringExtra(EXTRA_FORUM_NAME);

            if (!newIntent.getBooleanExtra(EXTRA_IS_FIRST_ACTIVITY, true)) {
                disableDrawerLayout();
            }

            if (getIntent().getBooleanExtra(EXTRA_ITS_FIRST_START, false)) {
                if (PrefsManager.getInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED) == MainActivity.ACTIVITY_SHOW_TOPIC) {
                    startActivity(new Intent(this, ShowTopicActivity.class));
                    newActivityIsLaunched = true;
                }
            } else if (newLinkToGo != null) {
                newActivityIsLaunched = readThisTopicOrForum(newLinkToGo, newIntent.getBooleanExtra(EXTRA_GO_TO_LAST_PAGE, false));
            }

            if (!Utils.stringIsEmptyOrNull(newForumNameToUse)) {
                forumStatus.forumName = newForumNameToUse;
            }
        }

        return newActivityIsLaunched;
    }

    private void updateShareAction() {
        if (shareAction != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, pageNavigation.getCurrentPageLink());
            shareIntent.setType("text/plain");
            shareAction.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean newActivityIsLaunched = false;

        pageNavigation.initializePagerView((ViewPager) findViewById(R.id.pager_showforum));
        pageNavigation.initializeNavigationButtons((Button) findViewById(R.id.firstpage_button_showforum), (Button) findViewById(R.id.previouspage_button_showforum),
                        (Button) findViewById(R.id.currentpage_button_showforum), (Button) findViewById(R.id.nextpage_button_showforum), null);
        pageNavigation.updateAdapterForPagerView();

        if (savedInstanceState == null) {
            forumStatus.forumName = getString(R.string.app_name);
            newActivityIsLaunched = consumeIntent(getIntent());

            /* Si les informations du topic n'étaient pas présentes dans l'Intent ça veut dire qu'il faut les récupérer dans les prefs
             * parce que c'est le ShowForum lancé au démarrage de l'application.
             * Si la première activité affichée est ShowTopic et que le lien du forum dans les prefs n'est pas le forum du ShowTopic on récupère
             * le vrai lien du forum via le lien du topic. On le fait pas dans tous les cas dans le but de préserver le numéro de la page du forum si possible. */
            if (pageNavigation.getCurrentLinkIsEmpty()) {
                if (PrefsManager.getInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED) == MainActivity.ACTIVITY_SHOW_TOPIC) {
                    String savedForumLink = PrefsManager.getString(PrefsManager.StringPref.Names.FORUM_URL_TO_FETCH);
                    String forumLinkFromSavedTopicLink = JVCParser.getForumForTopicLink(PrefsManager.getString(PrefsManager.StringPref.Names.TOPIC_URL_TO_FETCH));

                    if (JVCParser.checkIfForumAreSame(savedForumLink, forumLinkFromSavedTopicLink)) {
                        pageNavigation.setCurrentLink(savedForumLink);
                    } else {
                        pageNavigation.setCurrentLink(forumLinkFromSavedTopicLink);
                    }
                } else {
                    pageNavigation.setCurrentLink(PrefsManager.getString(PrefsManager.StringPref.Names.FORUM_URL_TO_FETCH));
                }
            }

            pageNavigation.updateCurrentItemAndButtonsToCurrentLink();
        } else {
            drawerIsDisabled = savedInstanceState.getBoolean(SAVE_DRAWER_IS_DISABLED);
            forumStatus = savedInstanceState.getParcelable(SAVE_FORUM_STATUS);
            pageNavigation.setCurrentLink(savedInstanceState.getString(SAVE_CURRENT_FORUM_LINK, ""));
            refreshNeededOnNextResume = savedInstanceState.getBoolean(SAVE_REFRESH_NEEDED_NEXT_RESUME, false);

            if (drawerIsDisabled) {
                disableDrawerLayout();
            }
            updateMpAndNotifNumberShowed(forumStatus.numberOfMp, forumStatus.numberOfNotif);
            pageNavigation.updateNavigationButtons();
        }
        setTitle(forumStatus.forumName);

        if (newActivityIsLaunched) {
            pageNavigation.setDontLoadOnFirstTimeForNextFragCreate(true);
        } else if (savedInstanceState == null && !drawerIsDisabled) {
            /* Drawer désactivé == autres activités dans le stack avant, donc l'animation de doit pas être un fade. */
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    @Override
    public void onNewIntent(Intent newIntent) {
        super.onNewIntent(newIntent);
        consumeIntent(newIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean refreshForumOnResume = PrefsManager.getBool(PrefsManager.BoolPref.Names.REFRESH_FORUM_ON_RESUME);
        PrefsManager.putInt(PrefsManager.IntPref.Names.LAST_ACTIVITY_VIEWED, MainActivity.ACTIVITY_SHOW_FORUM);
        PrefsManager.applyChanges();

        if ((refreshForumOnResume || refreshNeededOnNextResume) && !dontConsumeRefreshOnNextResume) {
            refreshNeededOnNextResume = false;
            if (getCurrentFragment() != null) {
                getCurrentFragment().refreshForum();
            }
        }
        dontConsumeRefreshOnNextResume = false;

        linkTypeForInternalBrowser.setTypeFromString(PrefsManager.getString(PrefsManager.StringPref.Names.LINK_TYPE_FOR_INTERNAL_BROWSER));
    }

    @Override
    public void onPause() {
        stopAllCurrentTasks();
        if (!pageNavigation.getCurrentLinkIsEmpty()) {
            PrefsManager.putString(PrefsManager.StringPref.Names.FORUM_URL_TO_FETCH, pageNavigation.getCurrentPageLink());
            PrefsManager.applyChanges();
        }
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_DRAWER_IS_DISABLED, drawerIsDisabled);
        outState.putParcelable(SAVE_FORUM_STATUS, forumStatus);
        outState.putString(SAVE_CURRENT_FORUM_LINK, pageNavigation.getCurrentPageLink());
        outState.putBoolean(SAVE_REFRESH_NEEDED_NEXT_RESUME, refreshNeededOnNextResume);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_showforum, menu);
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menu.findItem(R.id.action_share_showforum));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem favItem = menu.findItem(R.id.action_change_forum_fav_value_showforum);

        menu.findItem(R.id.action_search_topic_showforum).setEnabled(!pageNavigation.getCurrentLinkIsEmpty());
        menu.findItem(R.id.action_share_showforum).setEnabled(!pageNavigation.getCurrentLinkIsEmpty());
        updateShareAction();

        favItem.setEnabled(false);
        if (!pseudoOfUser.isEmpty()) {
            menu.findItem(R.id.action_send_topic_showforum).setEnabled(!Utils.stringIsEmptyOrNull(forumStatus.listOfInputInAString) && !pageNavigation.getCurrentLinkIsEmpty());

            if (forumStatus.isInFavs != null) {
                favItem.setEnabled(true);
                if (forumStatus.isInFavs) {
                    favItem.setTitle(R.string.removeFromFavs);
                } else {
                    favItem.setTitle(R.string.addToFavs);
                }
            }
        } else {
            menu.findItem(R.id.action_send_topic_showforum).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_forum_fav_value_showforum:
                if (currentTaskForFavs == null) {
                    currentTaskForFavs = new AddOrRemoveThingToFavs(!forumStatus.isInFavs, this);
                    currentTaskForFavs.execute(JVCParser.getForumIdOfThisForum(pageNavigation.getCurrentPageLink()), forumStatus.ajaxInfos.pref, PrefsManager.getString(PrefsManager.StringPref.Names.COOKIES_LIST));
                } else {
                    Toast.makeText(ShowForumActivity.this, R.string.errorActionAlreadyRunning, Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.action_search_topic_showforum:
                Intent newSearchTopicIntent = new Intent(this, SearchTopicInForumActivity.class);
                newSearchTopicIntent.putExtra(SearchTopicInForumActivity.EXTRA_FORUM_LINK, pageNavigation.getFirstPageLink());
                newSearchTopicIntent.putExtra(SearchTopicInForumActivity.EXTRA_FORUM_NAME, forumStatus.forumName);
                startActivity(newSearchTopicIntent);
                return true;
            case R.id.action_open_in_browser_showforum:
                Utils.openCorrespondingBrowser(linkTypeForInternalBrowser, pageNavigation.getCurrentPageLink(), this);
                return true;
            case R.id.action_send_topic_showforum:
                Intent newSendTopicIntent = new Intent(this, SendTopicToForumActivity.class);
                newSendTopicIntent.putExtra(SendTopicToForumActivity.EXTRA_FORUM_NAME, forumStatus.forumName);
                newSendTopicIntent.putExtra(SendTopicToForumActivity.EXTRA_FORUM_LINK, pageNavigation.getFirstPageLink());
                newSendTopicIntent.putExtra(SendTopicToForumActivity.EXTRA_INPUT_LIST, forumStatus.listOfInputInAString);
                newSendTopicIntent.putExtra(SendTopicToForumActivity.EXTRA_USER_CAN_POST_AS_MODO, forumStatus.userCanPostAsModo);
                startActivityForResult(newSendTopicIntent, SEND_TOPIC_REQUEST_CODE);
                refreshNeededOnNextResume = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEND_TOPIC_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String newTopicLink = data.getStringExtra(SendTopicToForumActivity.RESULT_EXTRA_TOPIC_LINK_TO_MOVE);

                if (newTopicLink != null) {
                    readThisTopic(newTopicLink, false, null, null, false);
                    //onActivityResult est appelé avant onResume, donc il faut contourner le fait que
                    //onResume sera forcément appelé après cette fonction mais avant que ShowTopicActivity soit lancé
                    if (refreshNeededOnNextResume) {
                        dontConsumeRefreshOnNextResume = true;
                    }
                }
            }
        }
    }

    @Override
    protected void initializeViewAndToolbar() {
        setContentView(R.layout.activity_showforum);
        initToolbar(R.id.toolbar_showforum).setOnLongClickListener(showForumTitleListener);

        layoutForDrawer = findViewById(R.id.layout_drawer_showforum);
        navigationMenuList = findViewById(R.id.navigation_menu_showforum);
    }

    @Override
    protected void newForumOrTopicToRead(String link, boolean itsAForum, boolean isWhenDrawerIsClosed, boolean fromLongClick) {
        if (itsAForum && !isWhenDrawerIsClosed) {
            readThisTopicOrForum(link, fromLongClick);
        } else if (!itsAForum && isWhenDrawerIsClosed) {
            readThisTopicOrForum(link, fromLongClick);
        }
    }

    @Override
    public void setReadNewTopic(String newTopicLink, String newTopicName, String pseudoOfAuthor, boolean fromLongClick) {
        readThisTopic(newTopicLink, false, newTopicName, pseudoOfAuthor, fromLongClick);
    }

    @Override
    public void updateForumLink(String newForumLink) {
        pageNavigation.setCurrentLink(newForumLink);
    }

    @Override
    public void extendPageSelection(View buttonView) {
        //rien
    }

    @Override
    public AbsShowSomethingFragment createNewFragmentForRead(String possibleForumLink) {
        ShowForumFragment currentFragment = new ShowForumFragment();

        if (possibleForumLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(ShowForumFragment.ARG_FORUM_LINK, possibleForumLink);
            currentFragment.setArguments(argForFrag);
        }

        return currentFragment;
    }

    @Override
    public void onNewPageSelected(int position) {
        //rien
    }

    @Override
    public void doThingsBeforeLoadOnFragment(AbsShowSomethingFragment thisFragment) {
        //rien
    }

    @Override
    public int getShowablePageNumberForThisLink(String link) {
        try {
            return ((Integer.parseInt(JVCParser.getPageNumberForThisForumLink(link)) - 1) / 25) + 1;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    public String setShowedPageNumberForThisLink(String link, int newPageNumber) {
        return JVCParser.setPageNumberForThisForumLink(link, ((newPageNumber - 1) * 25) + 1);
    }

    @Override
    public void getActionToFavsResult(String resultInString, boolean itsAnError) {
        if (itsAnError) {
            if (resultInString.isEmpty()) {
                resultInString = getString(R.string.errorInfosMissings);
            }
            Toast.makeText(this, resultInString, Toast.LENGTH_SHORT).show();
        } else {
            ShowForumFragment currentFrag = getCurrentFragment();

            if (currentTaskForFavs.getAddToFavs()) {
                Toast.makeText(this, R.string.favAdded, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.favRemoved, Toast.LENGTH_SHORT).show();
            }

            if (currentFrag != null) {
                forumStatus.isInFavs = currentTaskForFavs.getAddToFavs();
                currentFrag.updateForumStatusInfos(forumStatus);
            }
        }
        currentTaskForFavs = null;
    }

    @Override
    public void getNewForumStatus(JVCForumGetter.ForumStatusInfos newForumStatus, JVCForumGetter.ForumStatusInfos oldForumStatus) {
        /* Pour utiliser le ForumStatus de ShowForum au lieu de JVCForumGetter en tant qu'old ForumStatus. */
        oldForumStatus = forumStatus;
        forumStatus = newForumStatus;

        if (!Utils.stringsAreEquals(forumStatus.numberOfMp, oldForumStatus.numberOfMp) ||
            !Utils.stringsAreEquals(forumStatus.numberOfNotif, oldForumStatus.numberOfNotif)) {
            updateMpAndNotifNumberShowed(forumStatus.numberOfMp, forumStatus.numberOfNotif);
        }

        if (forumStatus.forumName.isEmpty()) {
            forumStatus.forumName = getString(R.string.app_name);
        }
        if (!forumStatus.forumName.equals(oldForumStatus.forumName)) {
            setTitle(forumStatus.forumName);
        }
    }
}
