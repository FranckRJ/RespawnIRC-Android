package com.franckrj.respawnirc.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.ConnectActivity;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.SelectForumActivity;
import com.franckrj.respawnirc.SettingsActivity;
import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.RefreshFavDialogFragment;

import java.util.ArrayList;

public class NavigationViewUtil {
    private Activity parentActivity = null;
    private DrawerLayout layoutForDrawer = null;
    private NavigationView navigationForDrawer = null;
    private TextView pseudoTextNavigation = null;
    private ImageView contextConnectImageNavigation = null;
    private ActionBarDrawerToggle toggleForDrawer = null;
    private int lastItemSelected = -1;
    private SharedPreferences sharedPref = null;
    private String pseudoOfUser = "";
    private boolean isInNavigationConnectMode = false;
    private SubMenu forumFavSubMenu = null;
    private MenuItem refreshForumFavExtern = null;
    private SubMenu topicFavSubMenu = null;
    private MenuItem refreshTopicFavExtern = null;
    private String newFavSelected = "";
    private int idOfBaseAcitivy = -1;

    private NavigationView.OnNavigationItemSelectedListener itemInNavigationClickedListener = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if ((item.getItemId() == R.id.action_refresh_forum_fav_navigation || item.getItemId() == R.id.action_refresh_topic_fav_navigation) && item.getGroupId() == Menu.NONE) {
                if (!pseudoOfUser.isEmpty()) {
                    Bundle argForFrag = new Bundle();
                    RefreshFavDialogFragment refreshFavsDialogFragment = new RefreshFavDialogFragment();

                    argForFrag.putString(RefreshFavDialogFragment.ARG_PSEUDO, pseudoOfUser);
                    if (item.getItemId() == R.id.action_refresh_forum_fav_navigation) {
                        argForFrag.putInt(RefreshFavDialogFragment.ARG_FAV_TYPE, RefreshFavDialogFragment.FAV_FORUM);
                    } else {
                        argForFrag.putInt(RefreshFavDialogFragment.ARG_FAV_TYPE, RefreshFavDialogFragment.FAV_TOPIC);
                    }

                    refreshFavsDialogFragment.setArguments(argForFrag);
                    refreshFavsDialogFragment.show(parentActivity.getFragmentManager(), "RefreshFavDialogFragment");
                } else {
                    Toast.makeText(parentActivity, R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                }
                return false;
            } else if (item.getGroupId() == R.id.group_forum_fav_navigation) {
                lastItemSelected = R.id.action_forum_fav_selected;
                newFavSelected = sharedPref.getString(parentActivity.getString(R.string.prefForumFavLink) + String.valueOf(item.getItemId()), "");
                if (parentActivity instanceof NewForumOrTopicNeedToBeRead) {
                    ((NewForumOrTopicNeedToBeRead) parentActivity).newForumOrTopicToRead(newFavSelected, true, false);
                }
                layoutForDrawer.closeDrawer(navigationForDrawer);
                return true;
            } else if (item.getGroupId() == R.id.group_topic_fav_navigation) {
                lastItemSelected = R.id.action_topic_fav_selected;
                newFavSelected = sharedPref.getString(parentActivity.getString(R.string.prefTopicFavLink) + String.valueOf(item.getItemId()), "");
                if (parentActivity instanceof NewForumOrTopicNeedToBeRead) {
                    ((NewForumOrTopicNeedToBeRead) parentActivity).newForumOrTopicToRead(newFavSelected, false, false);
                }
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

    public NavigationViewUtil(Activity newParentActivity, SharedPreferences newSharedPref, int newIdOfBaseActivity) {
        parentActivity = newParentActivity;
        sharedPref = newSharedPref;
        idOfBaseAcitivy = newIdOfBaseActivity;
    }

    private void updateNavigationMenu() {
        if (!pseudoOfUser.isEmpty()) {
            pseudoTextNavigation.setText(pseudoOfUser);
        } else {
            pseudoTextNavigation.setText(R.string.connectToJVC);
            isInNavigationConnectMode = false;
        }

        if (!isInNavigationConnectMode) {
            navigationForDrawer.getMenu().clear();
            navigationForDrawer.inflateMenu(R.menu.menu_navigation_view);
            forumFavSubMenu = navigationForDrawer.getMenu().addSubMenu(Menu.NONE, R.id.group_forum_fav_navigation, Menu.NONE, R.string.forumFav);
            refreshForumFavExtern = navigationForDrawer.getMenu().add(Menu.NONE, R.id.action_refresh_forum_fav_navigation, Menu.NONE, R.string.refresh);
            refreshForumFavExtern.setIcon(R.drawable.ic_action_navigation_refresh);
            topicFavSubMenu = navigationForDrawer.getMenu().addSubMenu(Menu.NONE, R.id.group_topic_fav_navigation, Menu.NONE, R.string.topicFav);
            refreshTopicFavExtern = navigationForDrawer.getMenu().add(Menu.NONE, R.id.action_refresh_topic_fav_navigation, Menu.NONE, R.string.refresh);
            refreshTopicFavExtern.setIcon(R.drawable.ic_action_navigation_refresh);
            updateFavsInNavigationMenu();
            navigationForDrawer.setCheckedItem(idOfBaseAcitivy);

            if (idOfBaseAcitivy != R.id.action_forum_navigation) {
                navigationForDrawer.getMenu().findItem(R.id.action_forum_navigation).setVisible(false);
            }

            if (pseudoOfUser.isEmpty()) {
                contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.ic_action_content_add_circle_outline));
            } else {
                contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.ic_action_navigation_expand_more));
            }
        } else {
            navigationForDrawer.getMenu().clear();
            navigationForDrawer.inflateMenu(R.menu.menu_navigation_view_already_connected);
            contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.ic_action_navigation_expand_less));
        }
    }

    private void updateFavsInNavigationMenu() {
        int currentForumFavArraySize = sharedPref.getInt(parentActivity.getString(R.string.prefForumFavArraySize), 0);
        int currentTopicFavArraySize = sharedPref.getInt(parentActivity.getString(R.string.prefTopicFavArraySize), 0);

        forumFavSubMenu.clear();
        for (int i = 0; i < currentForumFavArraySize; ++i) {
            forumFavSubMenu.add(R.id.group_forum_fav_navigation, i, Menu.NONE, sharedPref.getString(parentActivity.getString(R.string.prefForumFavName) + String.valueOf(i), ""));
        }
        forumFavSubMenu.setGroupCheckable(R.id.group_forum_fav_navigation, true, true);

        topicFavSubMenu.clear();
        for (int i = 0; i < currentTopicFavArraySize; ++i) {
            topicFavSubMenu.add(R.id.group_topic_fav_navigation, i, Menu.NONE, sharedPref.getString(parentActivity.getString(R.string.prefTopicFavName) + String.valueOf(i), ""));
        }
        topicFavSubMenu.setGroupCheckable(R.id.group_topic_fav_navigation, true, true);

        if (forumFavSubMenu.size() == 0) {
            MenuItem refreshForumFavIntern = forumFavSubMenu.add(Menu.NONE, R.id.action_refresh_forum_fav_navigation, Menu.NONE, R.string.refresh);
            refreshForumFavIntern.setIcon(R.drawable.ic_action_navigation_refresh);
            refreshForumFavExtern.setVisible(false);
        } else {
            refreshForumFavExtern.setVisible(true);
        }
        if (topicFavSubMenu.size() == 0) {
            MenuItem refreshTopicFavIntern = topicFavSubMenu.add(Menu.NONE, R.id.action_refresh_topic_fav_navigation, Menu.NONE, R.string.refresh);
            refreshTopicFavIntern.setIcon(R.drawable.ic_action_navigation_refresh);
            refreshTopicFavExtern.setVisible(false);
        } else {
            refreshTopicFavExtern.setVisible(true);
        }
    }

    public void initialize(DrawerLayout newLayoutForDrawer, NavigationView newNavigationForDrawer) {
        pseudoOfUser = sharedPref.getString(parentActivity.getString(R.string.prefPseudoUser), "");

        layoutForDrawer = newLayoutForDrawer;
        navigationForDrawer = newNavigationForDrawer;

        toggleForDrawer = new ActionBarDrawerToggle(parentActivity, layoutForDrawer, R.string.openDrawerContentDescRes, R.string.closeDrawerContentDescRes) {
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
                            if (idOfBaseAcitivy != R.id.action_home_navigation) {
                                Intent newShowForumIntent = new Intent(parentActivity, SelectForumActivity.class);
                                newShowForumIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                parentActivity.startActivity(newShowForumIntent);
                                parentActivity.finish();
                            }
                            break;
                        case R.id.action_connect_new_account_navigation:
                        case R.id.action_connect_navigation:
                            parentActivity.startActivity(new Intent(parentActivity, ConnectActivity.class));
                            break;
                        case R.id.action_choose_topic_forum_navigation:
                            ChooseTopicOrForumLinkDialogFragment chooseLinkDialogFragment = new ChooseTopicOrForumLinkDialogFragment();
                            chooseLinkDialogFragment.show(parentActivity.getFragmentManager(), "ChooseTopicOrForumLinkDialogFragment");
                            break;
                        case R.id.action_settings_navigation:
                            parentActivity.startActivity(new Intent(parentActivity, SettingsActivity.class));
                            break;
                        case R.id.action_forum_fav_selected:
                            if (parentActivity instanceof NewForumOrTopicNeedToBeRead) {
                                ((NewForumOrTopicNeedToBeRead) parentActivity).newForumOrTopicToRead(newFavSelected, true, true);
                            }
                            newFavSelected = "";
                            break;
                        case R.id.action_topic_fav_selected:
                            if (parentActivity instanceof NewForumOrTopicNeedToBeRead) {
                                ((NewForumOrTopicNeedToBeRead) parentActivity).newForumOrTopicToRead(newFavSelected, false, true);
                            }
                            newFavSelected = "";
                            break;
                    }
                }
                navigationForDrawer.setCheckedItem(idOfBaseAcitivy);

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

        View navigationHeader = navigationForDrawer.getHeaderView(0);
        pseudoTextNavigation = (TextView) navigationHeader.findViewById(R.id.pseudo_text_navigation_header);
        contextConnectImageNavigation = (ImageView) navigationHeader.findViewById(R.id.context_connect_image_navigation_header);
        navigationForDrawer.setItemTextColor(null);
        navigationForDrawer.setItemIconTintList(Undeprecator.resourcesGetColorStateList(parentActivity.getResources(), R.color.navigation_menu_item));
        navigationForDrawer.setNavigationItemSelectedListener(itemInNavigationClickedListener);
        navigationHeader.setOnClickListener(headerClickedListener);
        layoutForDrawer.addDrawerListener(toggleForDrawer);
        layoutForDrawer.setDrawerShadow(R.drawable.shadow_drawer, GravityCompat.START);
        updateNavigationMenu();

        if (Build.VERSION.SDK_INT > 15) {
            Undeprecator.viewSetBackgroundDrawable(navigationHeader, Undeprecator.resourcesGetDrawable(parentActivity.getResources(), R.drawable.navigation_header_background));
        } else {
            navigationHeader.setBackgroundColor(Undeprecator.resourcesGetColor(parentActivity.getResources(), R.color.colorPrimaryDark));
        }
    }

    public void syncStateOfToggleForDrawer() {
        toggleForDrawer.syncState();
    }

    public void updateNavigationViewIfNeeded() {
        String tmpPseudoOfUser = sharedPref.getString(parentActivity.getString(R.string.prefPseudoUser), "");

        if (!tmpPseudoOfUser.equals(pseudoOfUser)) {
            pseudoOfUser = tmpPseudoOfUser;
            updateNavigationMenu();
        }
    }

    public boolean closeNavigationView() {
        if (layoutForDrawer.isDrawerOpen(navigationForDrawer)) {
            layoutForDrawer.closeDrawer(navigationForDrawer);
            return true;
        } else {
            return false;
        }
    }

    public void onConfigurationChangedForToggleForDrawer(Configuration newConfig) {
        toggleForDrawer.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelectedForToggleForDrawer(MenuItem item) {
        return toggleForDrawer.onOptionsItemSelected(item);
    }

    public boolean updateFavs(ArrayList<JVCParser.NameAndLink> listOfFavs, int typeOfFav) {
        if (listOfFavs != null) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            String prefFavArrayize;
            String prefFavName;
            String prefFavLink;
            int currentForumFavArraySize;

            if (typeOfFav == RefreshFavDialogFragment.FAV_FORUM) {
                prefFavArrayize = parentActivity.getString(R.string.prefForumFavArraySize);
                prefFavName = parentActivity.getString(R.string.prefForumFavName);
                prefFavLink = parentActivity.getString(R.string.prefForumFavLink);
            } else {
                prefFavArrayize = parentActivity.getString(R.string.prefTopicFavArraySize);
                prefFavName = parentActivity.getString(R.string.prefTopicFavName);
                prefFavLink = parentActivity.getString(R.string.prefTopicFavLink);
            }

            currentForumFavArraySize = sharedPref.getInt(prefFavArrayize, 0);

            for (int i = 0; i < currentForumFavArraySize; ++i) {
                sharedPrefEdit.remove(prefFavName + String.valueOf(i));
                sharedPrefEdit.remove(prefFavLink + String.valueOf(i));
            }

            sharedPrefEdit.putInt(prefFavArrayize, listOfFavs.size());

            for (int i = 0; i < listOfFavs.size(); ++i) {
                sharedPrefEdit.putString(prefFavName + String.valueOf(i), listOfFavs.get(i).name);
                sharedPrefEdit.putString(prefFavLink + String.valueOf(i), listOfFavs.get(i).link);
            }

            sharedPrefEdit.apply();
            updateFavsInNavigationMenu();
            return true;
        } else {
            return false;
        }
    }

    public interface NewForumOrTopicNeedToBeRead {
        void newForumOrTopicToRead(String link, boolean itsAForum, boolean isWhenDrawerIsClosed);
    }
}
