package com.franckrj.respawnirc.jvcviewers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.ConnectActivity;
import com.franckrj.respawnirc.MainActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.SettingsActivity;
import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.dialogs.RefreshFavDialogFragment;
import com.franckrj.respawnirc.jvctopictools.JVCTopicGetter;
import com.franckrj.respawnirc.jvctopictools.ShowForumFragment;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.Undeprecator;

import java.util.ArrayList;

public class ShowForumActivity extends AbsShowSomethingActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected,
                                                    ShowForumFragment.NewTopicWantRead, JVCTopicGetter.NewForumNameAvailable,
                                                    JVCTopicGetter.ForumLinkChanged, RefreshFavDialogFragment.NewForumsFavs {
    private DrawerLayout layoutForDrawer = null;
    private NavigationView navigationForDrawer = null;
    private TextView pseudoTextNavigation = null;
    private ImageView contextConnectImageNavigation = null;
    private ActionBarDrawerToggle toggleForDrawer = null;
    private int lastItemSelected = -1;
    private SharedPreferences sharedPref = null;
    private String currentTitle = "";
    private String pseudoOfUser = "";
    private boolean isInNavigationConnectMode = false;

    private NavigationView.OnNavigationItemSelectedListener itemInNavigationClickedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (item.getItemId() == R.id.action_refresh_favs_navigation) {
                if (!pseudoOfUser.isEmpty()) {
                    Bundle argForFrag = new Bundle();
                    RefreshFavDialogFragment refreshFavsDialogFragment = new RefreshFavDialogFragment();

                    argForFrag.putString(RefreshFavDialogFragment.ARG_PSEUDO, pseudoOfUser);
                    refreshFavsDialogFragment.setArguments(argForFrag);
                    refreshFavsDialogFragment.show(getFragmentManager(), "RefreshFavDialogFragment");
                } else {
                    Toast.makeText(ShowForumActivity.this, R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                }
                return false;
            } else if (item.getGroupId() == R.id.group_forum_fav_navigation) {
                setTopicOrForum(sharedPref.getString(getString(R.string.prefForumFavLink) + String.valueOf(item.getItemId()), ""), true, null);
                layoutForDrawer.closeDrawer(navigationForDrawer);
                return true;
            } else {
                lastItemSelected = item.getItemId();
                layoutForDrawer.closeDrawer(navigationForDrawer);
                return true;
            }
        }
    };

    private View.OnClickListener headerClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!pseudoOfUser.isEmpty()) {
                isInNavigationConnectMode = !isInNavigationConnectMode;
                updateNavigationMenu();
            } else {
                lastItemSelected = R.id.action_connect_navigation;
                layoutForDrawer.closeDrawer(navigationForDrawer);
            }
        }
    };

    public ShowForumActivity() {
        lastPage = 100;
    }

    protected AbsShowSomethingFragment createNewFragmentForRead(String possibleForumLink) {
        ShowForumFragment currentFragment = new ShowForumFragment();

        if (possibleForumLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(ShowForumFragment.ARG_FORUM_LINK, possibleForumLink);
            currentFragment.setArguments(argForFrag);
        }

        return currentFragment;
    }

    private void updateNavigationMenu() {
        if (!pseudoOfUser.isEmpty()) {
            pseudoTextNavigation.setText(pseudoOfUser);
        } else {
            pseudoTextNavigation.setText(R.string.connectToJVC);
        }

        if (pseudoOfUser.isEmpty() || !isInNavigationConnectMode) {
            navigationForDrawer.getMenu().clear();
            navigationForDrawer.inflateMenu(R.menu.menu_navigation_view);
            navigationForDrawer.setCheckedItem(R.id.action_home_navigation);

            if (pseudoOfUser.isEmpty()) {
                contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(getResources(), R.drawable.ic_action_content_add_circle_outline));
            } else {
                contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(getResources(), R.drawable.ic_action_navigation_expand_more));
            }

            updateFavsInNavigationMenu();
        } else {
            navigationForDrawer.getMenu().clear();
            navigationForDrawer.inflateMenu(R.menu.menu_navigation_view_already_connected);
            contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(getResources(), R.drawable.ic_action_navigation_expand_less));
        }
    }

    private void updateFavsInNavigationMenu() {
        SubMenu forumFavSubMenu = navigationForDrawer.getMenu().addSubMenu(Menu.NONE, R.id.group_forum_fav_navigation, Menu.NONE, R.string.forumFav);
        MenuItem refreshForumFav;
        int currentForumFavArraySize = sharedPref.getInt(getString(R.string.prefForumFavArraySize), 0);

        for (int i = 0; i < currentForumFavArraySize; ++i) {
            forumFavSubMenu.add(R.id.group_forum_fav_navigation, i, Menu.NONE, sharedPref.getString(getString(R.string.prefForumFavName) + String.valueOf(i), ""));
        }

        forumFavSubMenu.setGroupCheckable(R.id.group_forum_fav_navigation, true, true);

        refreshForumFav = navigationForDrawer.getMenu().add(Menu.NONE, R.id.action_refresh_favs_navigation, Menu.NONE, R.string.refresh);
        refreshForumFav.setIcon(R.drawable.ic_action_navigation_refresh);
    }

    private void setNewForumLink(String newLink) {
        currentLink = newLink;
        setTitle(R.string.app_name);
        updateAdapterForPagerView();
        updateCurrentItemAndButtonsToCurrentLink();
        if (pagerView.getCurrentItem() > 0) {
            clearPageForThisFragment(0);
        }
    }

    private void setTopicOrForum(String link, boolean updateForumFragIfNeeded, String topicName) {
        if (link != null) {
            if (!link.isEmpty()) {
                link = JVCParser.formatThisUrl(link);
            }

            if (JVCParser.checkIfItsForumLink(link)) {
                if (!JVCParser.getPageNumberForThisForumLink(link).isEmpty()) {
                    setNewForumLink(link);
                    return;
                }
            } else if (!JVCParser.getPageNumberForThisTopicLink(link).isEmpty()) {
                Intent newShowTopicIntent = new Intent(this, ShowTopicActivity.class);

                if (updateForumFragIfNeeded) {
                    setNewForumLink(JVCParser.getForumForTopicLink(link));
                }

                if (topicName != null) {
                    newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_NAME, topicName);
                }
                if (!currentTitle.equals(getString(R.string.app_name))) {
                    newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_FORUM_NAME, currentTitle);
                }

                newShowTopicIntent.putExtra(ShowTopicActivity.EXTRA_TOPIC_LINK, link);
                startActivity(newShowTopicIntent);
                return;
            }
        }

        Toast.makeText(this, R.string.errorInvalidLink, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showforum);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_showforum);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        pagerView = (ViewPager) findViewById(R.id.pager_showforum);
        layoutForDrawer = (DrawerLayout) findViewById(R.id.layout_drawer_showforum);
        navigationForDrawer = (NavigationView) findViewById(R.id.navigation_view_showforum);
        firstPageButton = (Button) findViewById(R.id.firstpage_button_showforum);
        previousPageButton = (Button) findViewById(R.id.previouspage_button_showforum);
        currentPageButton = (Button) findViewById(R.id.currentpage_button_showforum);
        nextPageButton = (Button) findViewById(R.id.nextpage_button_showforum);

        toggleForDrawer = new ActionBarDrawerToggle(this, layoutForDrawer, R.string.openDrawerContentDescRes, R.string.closeDrawerContentDescRes) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                super.onDrawerSlide(drawerView, 0);
                lastItemSelected = -1;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (lastItemSelected != -1) {
                    switch (lastItemSelected) {
                        case R.id.action_home_navigation:
                            break;
                        case R.id.action_connect_new_account_navigation:
                        case R.id.action_connect_navigation:
                            startActivity(new Intent(ShowForumActivity.this, ConnectActivity.class));
                            break;
                        case R.id.action_choose_topic_forum_navigation:
                            ChooseTopicOrForumLinkDialogFragment chooseLinkDialogFragment = new ChooseTopicOrForumLinkDialogFragment();
                            chooseLinkDialogFragment.show(getFragmentManager(), "ChooseTopicOrForumLinkDialogFragment");
                            break;
                        case R.id.action_settings_navigation:
                            startActivity(new Intent(ShowForumActivity.this, SettingsActivity.class));
                            break;
                    }
                }
                navigationForDrawer.setCheckedItem(R.id.action_home_navigation);

                if (isInNavigationConnectMode) {
                    isInNavigationConnectMode = false;
                    updateNavigationMenu();
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        updateAdapterForPagerView();

        View navigationHeader = navigationForDrawer.getHeaderView(0);
        pseudoTextNavigation = (TextView) navigationHeader.findViewById(R.id.pseudo_text_navigation_header);
        contextConnectImageNavigation = (ImageView) navigationHeader.findViewById(R.id.context_connect_image_navigation_header);
        pagerView.addOnPageChangeListener(pageChangeOnPagerListener);
        navigationForDrawer.setItemTextColor(null);
        navigationForDrawer.setItemIconTintList(Undeprecator.resourcesGetColorStateList(getResources(), R.color.navigation_menu_item));
        navigationForDrawer.setNavigationItemSelectedListener(itemInNavigationClickedListener);
        navigationHeader.setOnClickListener(headerClickedListener);
        layoutForDrawer.addDrawerListener(toggleForDrawer);
        layoutForDrawer.setDrawerShadow(R.drawable.shadow_drawer, GravityCompat.START);
        initializeNavigationButtons();

        currentLink = sharedPref.getString(getString(R.string.prefForumUrlToFetch), "");
        if (savedInstanceState == null) {
            currentTitle = getString(R.string.app_name);
            updateCurrentItemAndButtonsToCurrentLink();
        } else {
            currentTitle = savedInstanceState.getString(getString(R.string.saveCurrentForumTitle), getString(R.string.app_name));
            updateNavigationButtons();
        }
        setTitle(currentTitle);

        if (Build.VERSION.SDK_INT > 15) {
            Undeprecator.viewSetBackgroundDrawable(navigationHeader, Undeprecator.resourcesGetDrawable(getResources(), R.drawable.navigation_header_background));
        } else {
            navigationHeader.setBackgroundColor(Undeprecator.resourcesGetColor(getResources(), R.color.colorPrimaryDark));
        }

        if (sharedPref.getBoolean(getString(R.string.prefIsFirstLaunch), true)) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            HelpFirstLaunchDialogFragment firstLaunchDialogFragment = new HelpFirstLaunchDialogFragment();
            firstLaunchDialogFragment.show(getFragmentManager(), "HelpFirstLaunchDialogFragment");
            sharedPrefEdit.putBoolean(getString(R.string.prefIsFirstLaunch), false);
            sharedPrefEdit.apply();
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggleForDrawer.syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
        sharedPrefEdit.putInt(getString(R.string.prefLastActivityViewed), MainActivity.ACTIVITY_SHOW_FORUM);
        sharedPrefEdit.apply();

        pseudoOfUser = sharedPref.getString(getString(R.string.prefPseudoUser), "");
        updateNavigationMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!currentLink.isEmpty()) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            sharedPrefEdit.putString(getString(R.string.prefForumUrlToFetch), setShowedPageNumberForThisLink(currentLink, pagerView.getCurrentItem() + 1));
            sharedPrefEdit.apply();
        }
    }

    @Override
    public void onBackPressed() {
        if (layoutForDrawer.isDrawerOpen(navigationForDrawer)) {
            layoutForDrawer.closeDrawer(navigationForDrawer);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getString(R.string.saveCurrentForumTitle), currentTitle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleForDrawer.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggleForDrawer.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void newTopicOrForumAvailable(String newTopicOrForumLink) {
        setTopicOrForum(newTopicOrForumLink, true, null);
    }

    @Override
    public void setReadNewTopic(String newTopicLink, String newTopicName) {
        setTopicOrForum(newTopicLink, false, newTopicName);
    }

    @Override
    public void getNewForumName(String newForumName) {
        if (!newForumName.isEmpty()) {
            currentTitle = newForumName;
        } else {
            currentTitle = getString(R.string.app_name);
        }
        setTitle(currentTitle);
    }

    @Override
    public void updateForumLink(String newForumLink) {
        currentLink = newForumLink;
    }

    @Override
    protected int getShowablePageNumberForThisLink(String link) {
        return ((Integer.parseInt(JVCParser.getPageNumberForThisForumLink(link)) - 1) / 25) + 1;
    }

    @Override
    protected String setShowedPageNumberForThisLink(String link, int newPageNumber) {
        return JVCParser.setPageNumberForThisForumLink(link, ((newPageNumber - 1) * 25) + 1);
    }

    @Override
    public void getNewForumsFavs(ArrayList<JVCParser.NameAndLink> listOfForumsFavs) {
        if (listOfForumsFavs != null) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            int currentForumFavArraySize = sharedPref.getInt(getString(R.string.prefForumFavArraySize), 0);

            for (int i = 0; i < currentForumFavArraySize; ++i) {
                sharedPrefEdit.remove(getString(R.string.prefForumFavName) + String.valueOf(i));
                sharedPrefEdit.remove(getString(R.string.prefForumFavLink) + String.valueOf(i));
            }

            sharedPrefEdit.putInt(getString(R.string.prefForumFavArraySize), listOfForumsFavs.size());

            for (int i = 0; i < listOfForumsFavs.size(); ++i) {
                sharedPrefEdit.putString(getString(R.string.prefForumFavName) + String.valueOf(i), listOfForumsFavs.get(i).name);
                sharedPrefEdit.putString(getString(R.string.prefForumFavLink) + String.valueOf(i), listOfForumsFavs.get(i).link);
            }

            sharedPrefEdit.apply();
            updateNavigationMenu();
        } else {
            Toast.makeText(this, R.string.errorDuringFetchFavs, Toast.LENGTH_SHORT).show();
        }
    }
}