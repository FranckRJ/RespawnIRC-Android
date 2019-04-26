package com.franckrj.respawnirc.base;

import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.franckrj.respawnirc.ConnectActivity;
import com.franckrj.respawnirc.ConnectAsModoActivity;
import com.franckrj.respawnirc.ManageAccountListActivity;
import com.franckrj.respawnirc.NavigationMenuAdapter;
import com.franckrj.respawnirc.NavigationMenuListView;
import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.SettingsActivity;
import com.franckrj.respawnirc.jvcforumlist.SelectForumInListActivity;
import com.franckrj.respawnirc.dialogs.RefreshFavDialogFragment;
import com.franckrj.respawnirc.utils.AccountManager;
import com.franckrj.respawnirc.utils.ThemeManager;
import com.franckrj.respawnirc.utils.JVCParser;
import com.franckrj.respawnirc.utils.PrefsManager;
import com.franckrj.respawnirc.utils.Undeprecator;
import com.franckrj.respawnirc.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsNavigationViewActivity extends AbsToolbarActivity implements RefreshFavDialogFragment.NewFavsAvailable {
    protected static final int GROUP_ID_BASIC = 0;
    protected static final int GROUP_ID_FORUM_FAV = 1;
    protected static final int GROUP_ID_TOPIC_FAV = 2;
    protected static final int GROUP_ID_ACCOUNT_LIST = 3;
    protected static final int ITEM_ID_HOME = 0;
    protected static final int ITEM_ID_FORUM = 1;
    protected static final int ITEM_ID_SHOWMP = 2;
    protected static final int ITEM_ID_SHOWNOTIF = 3;
    protected static final int ITEM_ID_SHOWGTA = 4;
    protected static final int ITEM_ID_PREF = 5;
    protected static final int ITEM_ID_FORUM_FAV_SELECTED = 6;
    protected static final int ITEM_ID_REFRESH_FORUM_FAV = 7;
    protected static final int ITEM_ID_TOPIC_FAV_SELECTED = 8;
    protected static final int ITEM_ID_REFRESH_TOPIC_FAV = 9;
    protected static final int ITEM_ID_MANAGE_ACCOUNT = 10;
    protected static final int ITEM_ID_ACCOUNT_SELECTED = 11;
    protected static final int ITEM_ID_CONNECT_AS_MODO = 12;
    protected static final int ITEM_ID_CONNECT = 13;
    protected static final int MODE_HOME = 0;
    protected static final int MODE_FORUM = 1;
    protected static final int MODE_CONNECT = 2;

    protected static final String SAVE_MP_AND_NOTIF_IS_HIDDEN = "saveMpAndNotifIsHidden";

    protected static ArrayList<NavigationMenuAdapter.MenuItemInfo> listOfMenuItemInfoForHome = null;
    protected static ArrayList<NavigationMenuAdapter.MenuItemInfo> listOfMenuItemInfoForForum = null;
    protected static ArrayList<NavigationMenuAdapter.MenuItemInfo> listOfMenuItemInfoForConnect = null;
    protected static NavigationMenuAdapter.MenuItemInfo showGTAMenuItem = null;
    protected DrawerLayout layoutForDrawer = null;
    protected NavigationMenuListView navigationMenuList = null;
    protected NavigationMenuAdapter adapterForNavigationMenu = null;
    protected TextView mpTextNavigation = null;
    protected TextView notifTextNavigation = null;
    protected TextView pseudoTextNavigation = null;
    protected ImageView contextConnectImageNavigation = null;
    protected ActionBarDrawerToggle toggleForDrawer = null;
    protected int lastItemSelected = -1;
    protected AccountManager.AccountInfos currentAccount = new AccountManager.AccountInfos();
    protected boolean isInNavigationConnectMode = false;
    protected String newFavSelected = "";
    protected boolean newFavIsSelectedByLongClick = false;
    protected int idOfBaseActivity = -1;
    protected int currentNavigationMenuMode = -1;
    protected ArrayList<NavigationMenuAdapter.MenuItemInfo> currentListOfMenuItem = null;
    protected boolean backIsOpenDrawer = false;
    protected boolean drawerIsDisabled = false;
    protected boolean mpAndNotifNumberIsHidden = false;

    protected final AdapterView.OnItemClickListener itemInNavigationClickedListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int currentItemId = adapterForNavigationMenu.getItemIdOfRow((int) id);
            int currentGroupId = adapterForNavigationMenu.getGroupIdOfRow((int) id);

            if ((currentItemId == ITEM_ID_REFRESH_FORUM_FAV || currentItemId == ITEM_ID_REFRESH_TOPIC_FAV) && currentGroupId == GROUP_ID_BASIC) {
                if (!currentAccount.pseudo.isEmpty()) {
                    if (!getSupportFragmentManager().isStateSaved()) {
                        Bundle argForFrag = new Bundle();
                        RefreshFavDialogFragment refreshFavsDialogFragment = new RefreshFavDialogFragment();

                        argForFrag.putString(RefreshFavDialogFragment.ARG_PSEUDO, currentAccount.pseudo);
                        argForFrag.putString(RefreshFavDialogFragment.ARG_COOKIE_LIST, currentAccount.cookie);
                        if (currentItemId == ITEM_ID_REFRESH_FORUM_FAV) {
                            argForFrag.putInt(RefreshFavDialogFragment.ARG_FAV_TYPE, RefreshFavDialogFragment.FAV_FORUM);
                        } else {
                            argForFrag.putInt(RefreshFavDialogFragment.ARG_FAV_TYPE, RefreshFavDialogFragment.FAV_TOPIC);
                        }

                        refreshFavsDialogFragment.setArguments(argForFrag);
                        refreshFavsDialogFragment.show(getSupportFragmentManager(), "RefreshFavDialogFragment");
                    }
                } else {
                    Toast.makeText(AbsNavigationViewActivity.this, R.string.errorConnectNeeded, Toast.LENGTH_SHORT).show();
                }
            } else if (currentGroupId == GROUP_ID_FORUM_FAV) {
                lastItemSelected = ITEM_ID_FORUM_FAV_SELECTED;
                newFavIsSelectedByLongClick = false;
                newFavSelected = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.FORUM_FAV_LINK, String.valueOf(currentItemId));
                newForumOrTopicToRead(newFavSelected, true, false, false);
                layoutForDrawer.closeDrawer(GravityCompat.START);
                adapterForNavigationMenu.setRowSelected((int) id);
            } else if (currentGroupId == GROUP_ID_TOPIC_FAV) {
                lastItemSelected = ITEM_ID_TOPIC_FAV_SELECTED;
                newFavIsSelectedByLongClick = false;
                newFavSelected = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.TOPIC_FAV_LINK, String.valueOf(currentItemId));
                newForumOrTopicToRead(newFavSelected, false, false, false);
                layoutForDrawer.closeDrawer(GravityCompat.START);
                adapterForNavigationMenu.setRowSelected((int) id);
            } else if (currentGroupId == GROUP_ID_ACCOUNT_LIST) {
                lastItemSelected = ITEM_ID_ACCOUNT_SELECTED;
                if (!currentAccount.pseudo.toLowerCase().equals(AccountManager.getAccountAtIndex(currentItemId).pseudo.toLowerCase())) {
                    AccountManager.setCurrentAccount(AccountManager.getAccountAtIndex(currentItemId));
                    currentAccount = AccountManager.getCurrentAccount();
                    updateMpAndNotifNumberShowed(null, null);
                    updateAccountDependentInfos();
                    updatePseudoFromCurrentAccount();
                    updateAccountListInNavigationMenu(false);
                    layoutForDrawer.closeDrawer(GravityCompat.START);
                }
                adapterForNavigationMenu.setRowSelected((int) id);
            } else {
                lastItemSelected = currentItemId;
                layoutForDrawer.closeDrawer(GravityCompat.START);
                adapterForNavigationMenu.setRowSelected((int) id);
            }
            adapterForNavigationMenu.notifyDataSetChanged();
        }
    };

    protected final AdapterView.OnItemLongClickListener itemInNavigationLongClickedListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            int currentItemId = adapterForNavigationMenu.getItemIdOfRow((int) id);
            int currentGroupId = adapterForNavigationMenu.getGroupIdOfRow((int) id);

            if (currentGroupId == GROUP_ID_TOPIC_FAV) {
                lastItemSelected = ITEM_ID_TOPIC_FAV_SELECTED;
                newFavIsSelectedByLongClick = true;
                newFavSelected = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.TOPIC_FAV_LINK, String.valueOf(currentItemId));
                newForumOrTopicToRead(newFavSelected, false, false, true);
                layoutForDrawer.closeDrawer(GravityCompat.START);
                adapterForNavigationMenu.setRowSelected((int) id);
                adapterForNavigationMenu.notifyDataSetChanged();
                return true;
            }

            return false;
        }
    };

    private final View.OnClickListener headerClickedListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!currentAccount.pseudo.isEmpty()) {
                isInNavigationConnectMode = !isInNavigationConnectMode;
                updateNavigationMenu();
            } else {
                lastItemSelected = ITEM_ID_CONNECT;
                layoutForDrawer.closeDrawer(GravityCompat.START);
            }
        }
    };

    /* Voir AbsThemedActivity pour plus d'infos. */
    public AbsNavigationViewActivity() {
        statusBarNeedToBeTransparent = true;
    }

    private void initializeListsOfMenuItem() {
        if (listOfMenuItemInfoForHome == null || listOfMenuItemInfoForForum == null || listOfMenuItemInfoForConnect == null) {
            listOfMenuItemInfoForHome = new ArrayList<>();
            listOfMenuItemInfoForForum = new ArrayList<>();
            listOfMenuItemInfoForConnect = new ArrayList<>();
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.home);
                tmpItemInfo.iconResId = R.drawable.ic_home_dark_zoom;
                tmpItemInfo.buttonResId = 0;
                tmpItemInfo.isHeader = false;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_HOME;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForHome.add(tmpItemInfo);
                listOfMenuItemInfoForForum.add(tmpItemInfo);
            }
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.forum);
                tmpItemInfo.iconResId = R.drawable.ic_forum_dark_zoom;
                tmpItemInfo.buttonResId = R.drawable.ictb_about_dark;
                tmpItemInfo.isHeader = false;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_FORUM;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForForum.add(tmpItemInfo);
            }
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.preference);
                tmpItemInfo.iconResId = R.drawable.ic_settings_dark_zoom;
                tmpItemInfo.buttonResId = 0;
                tmpItemInfo.isHeader = false;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_PREF;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForHome.add(tmpItemInfo);
                listOfMenuItemInfoForForum.add(tmpItemInfo);
            }
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.forumFav);
                tmpItemInfo.iconResId = 0;
                tmpItemInfo.buttonResId = R.drawable.ictb_refresh_dark;
                tmpItemInfo.isHeader = true;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_REFRESH_FORUM_FAV;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForHome.add(tmpItemInfo);
                listOfMenuItemInfoForForum.add(tmpItemInfo);
            }
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.topicFav);
                tmpItemInfo.iconResId = 0;
                tmpItemInfo.buttonResId = R.drawable.ictb_refresh_dark;
                tmpItemInfo.isHeader = true;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_REFRESH_TOPIC_FAV;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForHome.add(tmpItemInfo);
                listOfMenuItemInfoForForum.add(tmpItemInfo);
            }

            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.showMp);
                tmpItemInfo.iconResId = R.drawable.ic_mail_dark_zoom;
                tmpItemInfo.buttonResId = 0;
                tmpItemInfo.isHeader = false;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_SHOWMP;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForConnect.add(tmpItemInfo);
            }
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.showNotif);
                tmpItemInfo.iconResId = R.drawable.ic_bell_dark_zoom;
                tmpItemInfo.buttonResId = 0;
                tmpItemInfo.isHeader = false;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_SHOWNOTIF;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForConnect.add(tmpItemInfo);
            }
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.connnectAsModoText);
                tmpItemInfo.iconResId = R.drawable.ic_empty;
                tmpItemInfo.buttonResId = 0;
                tmpItemInfo.isHeader = false;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_CONNECT_AS_MODO;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForConnect.add(tmpItemInfo);
            }
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.accounts);
                tmpItemInfo.iconResId = 0;
                tmpItemInfo.buttonResId = R.drawable.ictb_settings_dark;
                tmpItemInfo.isHeader = true;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_MANAGE_ACCOUNT;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForConnect.add(tmpItemInfo);
            }
            {
                NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
                tmpItemInfo.textContent = getString(R.string.addAnAccount);
                tmpItemInfo.iconResId = R.drawable.ic_add_dark_zoom;
                tmpItemInfo.buttonResId = 0;
                tmpItemInfo.isHeader = false;
                tmpItemInfo.isEnabled = true;
                tmpItemInfo.itemId = ITEM_ID_CONNECT;
                tmpItemInfo.groupId = GROUP_ID_BASIC;
                listOfMenuItemInfoForConnect.add(tmpItemInfo);
            }
        }

        if (showGTAMenuItem == null) {
            showGTAMenuItem = new NavigationMenuAdapter.MenuItemInfo();
            showGTAMenuItem.textContent = getString(R.string.gta);
            showGTAMenuItem.iconResId = R.drawable.ic_report_dark_zoom;
            showGTAMenuItem.buttonResId = 0;
            showGTAMenuItem.isHeader = false;
            showGTAMenuItem.isEnabled = true;
            showGTAMenuItem.itemId = ITEM_ID_SHOWGTA;
            showGTAMenuItem.groupId = GROUP_ID_BASIC;
        }
    }

    private void updatePseudoFromCurrentAccount() {
        if (!currentAccount.pseudo.isEmpty()) {
            pseudoTextNavigation.setText(currentAccount.pseudo);
            if (!mpAndNotifNumberIsHidden) {
                mpTextNavigation.setVisibility(View.VISIBLE);
                notifTextNavigation.setVisibility(View.VISIBLE);
            }
        } else {
            pseudoTextNavigation.setText(R.string.connectToJVC);
            mpTextNavigation.setVisibility(View.GONE);
            notifTextNavigation.setVisibility(View.GONE);
        }

        if (currentAccount.isModo && !currentAccount.pseudo.isEmpty()) {
            pseudoTextNavigation.setTextColor(Undeprecator.resourcesGetColor(getResources(), R.color.colorPseudoModoThemeDark));
        } else {
            pseudoTextNavigation.setTextColor(Color.WHITE);
        }
    }

    private void updateNavigationMenu() {
        int newNavigationMenuMode;

        updatePseudoFromCurrentAccount();
        if (currentAccount.pseudo.isEmpty()) {
            isInNavigationConnectMode = false;
        }

        if (isInNavigationConnectMode) {
            newNavigationMenuMode = MODE_CONNECT;
        } else {
            newNavigationMenuMode = (idOfBaseActivity == ITEM_ID_FORUM ? MODE_FORUM : MODE_HOME);
        }

        if (newNavigationMenuMode != currentNavigationMenuMode) {
            currentNavigationMenuMode = newNavigationMenuMode;
            switch (currentNavigationMenuMode) {
                case MODE_HOME:
                    currentListOfMenuItem = listOfMenuItemInfoForHome;
                    break;
                case MODE_FORUM:
                    currentListOfMenuItem = listOfMenuItemInfoForForum;
                    break;
                case MODE_CONNECT:
                    currentListOfMenuItem = listOfMenuItemInfoForConnect;
                    break;
                default:
                    currentListOfMenuItem = listOfMenuItemInfoForHome;
                    break;
            }
            adapterForNavigationMenu.setListOfMenuItem(currentListOfMenuItem);
        }

        if (!isInNavigationConnectMode) {
            int positionOfShowGTAItem = adapterForNavigationMenu.getPositionDependingOnId(ITEM_ID_SHOWGTA, GROUP_ID_BASIC);

            updateFavsInNavigationMenu(false);
            adapterForNavigationMenu.setRowSelected(adapterForNavigationMenu.getPositionDependingOnId(idOfBaseActivity, GROUP_ID_BASIC));

            if (currentAccount.pseudo.isEmpty()) {
                contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(getResources(), R.drawable.ic_add_circle_outline_dark));
            } else {
                contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(getResources(), R.drawable.ic_expand_more_dark));
            }

            if (currentAccount.isModo && !currentAccount.pseudo.isEmpty()) {
                if (positionOfShowGTAItem == -1) {
                    int positionOfPrefItem = adapterForNavigationMenu.getPositionDependingOnId(ITEM_ID_PREF, GROUP_ID_BASIC);
                    currentListOfMenuItem.add(positionOfPrefItem, showGTAMenuItem);
                }
            } else {
                if (positionOfShowGTAItem != -1) {
                    currentListOfMenuItem.remove(positionOfShowGTAItem);
                }
            }
        } else {
            updateAccountListInNavigationMenu(false);
            contextConnectImageNavigation.setImageDrawable(Undeprecator.resourcesGetDrawable(getResources(), R.drawable.ic_expand_less_dark));
        }

        adapterForNavigationMenu.notifyDataSetChanged();
    }

    private void updateFavsInNavigationMenu(boolean needToUpdateAdapter) {
        int currentForumFavArraySize = PrefsManager.getInt(PrefsManager.IntPref.Names.FORUM_FAV_ARRAY_SIZE);
        int currentTopicFavArraySize = PrefsManager.getInt(PrefsManager.IntPref.Names.TOPIC_FAV_ARRAY_SIZE);
        int positionOfNewFavItem;

        adapterForNavigationMenu.removeAllItemsFromGroup(GROUP_ID_FORUM_FAV);
        positionOfNewFavItem = adapterForNavigationMenu.getPositionDependingOnId(ITEM_ID_REFRESH_FORUM_FAV, GROUP_ID_BASIC) + 1;
        for (int i = 0; i < currentForumFavArraySize; ++i) {
            NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
            tmpItemInfo.textContent = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.FORUM_FAV_NAME, String.valueOf(i));
            tmpItemInfo.iconResId = 0;
            tmpItemInfo.buttonResId = 0;
            tmpItemInfo.isHeader = false;
            tmpItemInfo.isEnabled = true;
            tmpItemInfo.itemId = i;
            tmpItemInfo.groupId = GROUP_ID_FORUM_FAV;
            currentListOfMenuItem.add(positionOfNewFavItem, tmpItemInfo);
            ++positionOfNewFavItem;
        }

        adapterForNavigationMenu.removeAllItemsFromGroup(GROUP_ID_TOPIC_FAV);
        positionOfNewFavItem = adapterForNavigationMenu.getPositionDependingOnId(ITEM_ID_REFRESH_TOPIC_FAV, GROUP_ID_BASIC) + 1;
        for (int i = 0; i < currentTopicFavArraySize; ++i) {
            NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
            tmpItemInfo.textContent = PrefsManager.getStringWithSufix(PrefsManager.StringPref.Names.TOPIC_FAV_NAME, String.valueOf(i));
            tmpItemInfo.iconResId = 0;
            tmpItemInfo.buttonResId = 0;
            tmpItemInfo.isHeader = false;
            tmpItemInfo.isEnabled = true;
            tmpItemInfo.itemId = i;
            tmpItemInfo.groupId = GROUP_ID_TOPIC_FAV;
            currentListOfMenuItem.add(positionOfNewFavItem, tmpItemInfo);
            ++positionOfNewFavItem;
        }

        if (needToUpdateAdapter) {
            adapterForNavigationMenu.notifyDataSetChanged();
        }
    }

    private void updateAccountListInNavigationMenu(@SuppressWarnings("SameParameterValue") boolean needToUpdateAdapter) {
        List<String> listOfAccountPseudo = AccountManager.getListOfAccountPseudo();
        int positionOfAddAnAccount;

        adapterForNavigationMenu.removeAllItemsFromGroup(GROUP_ID_ACCOUNT_LIST);
        positionOfAddAnAccount = adapterForNavigationMenu.getPositionDependingOnId(ITEM_ID_CONNECT, GROUP_ID_BASIC);
        for (int i = 0; i < listOfAccountPseudo.size(); ++i) {
            NavigationMenuAdapter.MenuItemInfo tmpItemInfo = new NavigationMenuAdapter.MenuItemInfo();
            tmpItemInfo.textContent = listOfAccountPseudo.get(i);
            tmpItemInfo.iconResId = 0;
            tmpItemInfo.buttonResId = 0;
            tmpItemInfo.isHeader = false;
            tmpItemInfo.isEnabled = true;
            tmpItemInfo.itemId = i;
            tmpItemInfo.groupId = GROUP_ID_ACCOUNT_LIST;
            currentListOfMenuItem.add(positionOfAddAnAccount, tmpItemInfo);
            if (listOfAccountPseudo.get(i).toLowerCase().equals(currentAccount.pseudo.toLowerCase())) {
                adapterForNavigationMenu.setRowSelected(positionOfAddAnAccount);
            }
            ++positionOfAddAnAccount;
        }

        if (needToUpdateAdapter) {
            adapterForNavigationMenu.notifyDataSetChanged();
        }
    }

    protected void hideMpAndNotifNumber() {
        mpTextNavigation.setVisibility(View.GONE);
        notifTextNavigation.setVisibility(View.GONE);
        mpAndNotifNumberIsHidden = true;
    }

    protected void updateMpAndNotifNumberShowed(String newNumberOfMp, String newNumberOfNotif) {
        if (newNumberOfMp == null) {
            newNumberOfMp = "";
        }
        if (newNumberOfNotif == null) {
            newNumberOfNotif = "";
        }

        if (newNumberOfMp.isEmpty() || newNumberOfMp.equals("0")) {
            mpTextNavigation.setTypeface(null, Typeface.NORMAL);
            mpTextNavigation.setText(newNumberOfMp);
        } else {
            mpTextNavigation.setTypeface(null, Typeface.BOLD);
            mpTextNavigation.setText(getString(R.string.mpOrNotifNumberNonZero, newNumberOfMp));
        }

        if (newNumberOfNotif.isEmpty() || newNumberOfNotif.equals("0")) {
            notifTextNavigation.setTypeface(null, Typeface.NORMAL);
            notifTextNavigation.setText(newNumberOfNotif);
        } else {
            notifTextNavigation.setTypeface(null, Typeface.BOLD);
            notifTextNavigation.setText(getString(R.string.mpOrNotifNumberNonZero, newNumberOfNotif));
        }
    }

    protected void disableDrawerLayout() {
        layoutForDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        toggleForDrawer.setDrawerIndicatorEnabled(false);
        drawerIsDisabled = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeListsOfMenuItem();
        initializeViewAndToolbar();

        if (savedInstanceState != null) {
            mpAndNotifNumberIsHidden = savedInstanceState.getBoolean(SAVE_MP_AND_NOTIF_IS_HIDDEN, false);
        }

        currentAccount = AccountManager.getCurrentAccount();

        toggleForDrawer = new ActionBarDrawerToggle(this, layoutForDrawer, R.string.openDrawerContentDescRes, R.string.closeDrawerContentDescRes) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                lastItemSelected = -1;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (lastItemSelected != -1) {
                    switch (lastItemSelected) {
                        case ITEM_ID_HOME:
                            if (idOfBaseActivity != ITEM_ID_HOME) {
                                Intent newShowForumIntent = new Intent(AbsNavigationViewActivity.this, SelectForumInListActivity.class);
                                newShowForumIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(newShowForumIntent);
                                finish();
                            }
                            break;
                        case ITEM_ID_FORUM:
                            launchShowForumInfos();
                            break;
                        case ITEM_ID_CONNECT:
                            startActivity(new Intent(AbsNavigationViewActivity.this, ConnectActivity.class));
                            break;
                        case ITEM_ID_CONNECT_AS_MODO:
                            startActivity(new Intent(AbsNavigationViewActivity.this, ConnectAsModoActivity.class));
                            break;
                        case ITEM_ID_SHOWMP:
                            Utils.openLinkInInternalBrowser("http://www.jeuxvideo.com/messages-prives/boite-reception.php", AbsNavigationViewActivity.this);
                            break;
                        case ITEM_ID_SHOWNOTIF:
                            Utils.openLinkInInternalBrowser("http://www.jeuxvideo.com/profil/" + currentAccount.pseudo.toLowerCase() + "?mode=abonnements", AbsNavigationViewActivity.this);
                            break;
                        case ITEM_ID_SHOWGTA:
                            Utils.openLinkInInternalBrowser("http://www.jeuxvideo.com/gta/hp_alerte.php", AbsNavigationViewActivity.this);
                            break;
                        case ITEM_ID_PREF:
                            startActivity(new Intent(AbsNavigationViewActivity.this, SettingsActivity.class));
                            break;
                        case ITEM_ID_FORUM_FAV_SELECTED:
                            newForumOrTopicToRead(newFavSelected, true, true, newFavIsSelectedByLongClick);
                            newFavSelected = "";
                            break;
                        case ITEM_ID_TOPIC_FAV_SELECTED:
                            newForumOrTopicToRead(newFavSelected, false, true, newFavIsSelectedByLongClick);
                            newFavSelected = "";
                            break;
                        case ITEM_ID_MANAGE_ACCOUNT:
                            startActivity(new Intent(AbsNavigationViewActivity.this, ManageAccountListActivity.class));
                            break;
                    }
                }
                adapterForNavigationMenu.setRowSelected(adapterForNavigationMenu.getPositionDependingOnId(idOfBaseActivity, GROUP_ID_BASIC));
                adapterForNavigationMenu.notifyDataSetChanged();

                if (isInNavigationConnectMode) {
                    isInNavigationConnectMode = false;
                    updateNavigationMenu();
                }
            }
        };
        toggleForDrawer.setDrawerSlideAnimationEnabled(false);

        View navigationHeader = getLayoutInflater().inflate(R.layout.navigation_view_header, navigationMenuList, false);
        mpTextNavigation = navigationHeader.findViewById(R.id.mp_text_navigation_header);
        notifTextNavigation = navigationHeader.findViewById(R.id.notif_text_navigation_header);
        pseudoTextNavigation = navigationHeader.findViewById(R.id.pseudo_text_navigation_header);
        contextConnectImageNavigation = navigationHeader.findViewById(R.id.context_connect_image_navigation_header);
        adapterForNavigationMenu = new NavigationMenuAdapter(this);
        adapterForNavigationMenu.setBackgroundColors((ThemeManager.currentThemeUseDarkColors() ? Color.WHITE : Color.BLACK), ThemeManager.getColorInt(R.attr.themedNavigationIconColor, this),
                ThemeManager.getColorInt(R.attr.themedControlHighlightColor, this) & 0x40FFFFFF, Color.TRANSPARENT);
        adapterForNavigationMenu.setFontColors((ThemeManager.currentThemeUseDarkColors() ? Color.WHITE : Color.BLACK), ThemeManager.getColorInt(R.attr.themedHeaderTextColor, this));
        navigationMenuList.setHeaderView(navigationHeader);
        navigationMenuList.setAdapter(adapterForNavigationMenu);
        navigationMenuList.setOnItemClickListener(itemInNavigationClickedListener);
        navigationMenuList.setOnItemLongClickListener(itemInNavigationLongClickedListener);
        navigationHeader.setOnClickListener(headerClickedListener);
        layoutForDrawer.addDrawerListener(toggleForDrawer);
        layoutForDrawer.setDrawerShadow(ThemeManager.getDrawable(R.attr.themedShadowDrawer, this), GravityCompat.START);
        updateNavigationMenu();

        if (ThemeManager.getThemeUsed() == ThemeManager.ThemeName.LIGHT_THEME && ThemeManager.getHeaderColorUsedForThemeLight() != Undeprecator.resourcesGetColor(getResources(), R.color.defaultHeaderColorThemeLight)) {
            Drawable newHeaderBackground = Undeprecator.resourcesGetDrawable(getResources(), R.drawable.navigation_header_background_base);
            newHeaderBackground.setColorFilter(ThemeManager.getHeaderColorUsedForThemeLight(), PorterDuff.Mode.OVERLAY);
            navigationHeader.setBackgroundDrawable(newHeaderBackground);
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
        AccountManager.AccountInfos tmpAccount = AccountManager.getCurrentAccount();
        backIsOpenDrawer = PrefsManager.getBool(PrefsManager.BoolPref.Names.BACK_IS_OPEN_DRAWER);

        if (!currentAccount.equals(tmpAccount)) {
            currentAccount = tmpAccount;
            updateNavigationMenu();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_MP_AND_NOTIF_IS_HIDDEN, mpAndNotifNumberIsHidden);
    }

    @Override
    public void onBackPressed() {
        if (layoutForDrawer.isDrawerOpen(GravityCompat.START) && !drawerIsDisabled) {
            layoutForDrawer.closeDrawer(GravityCompat.START);
        } else if (backIsOpenDrawer && !drawerIsDisabled) {
            layoutForDrawer.openDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleForDrawer.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerIsDisabled) {
            if (item.getItemId() == android.R.id.home) {
                super.onBackPressed();
                return true;
            }
        } else {
            if (toggleForDrawer.onOptionsItemSelected(item)) {
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void getNewFavs(ArrayList<JVCParser.NameAndLink> listOfFavs, int typeOfFav) {
        if (listOfFavs != null) {
            final int sizeOfListOfFavs = listOfFavs.size();
            PrefsManager.IntPref.Names prefFavArraySize;
            PrefsManager.StringPref.Names prefFavName;
            PrefsManager.StringPref.Names prefFavLink;
            int currentFavArraySize;

            if (typeOfFav == RefreshFavDialogFragment.FAV_FORUM) {
                prefFavArraySize = PrefsManager.IntPref.Names.FORUM_FAV_ARRAY_SIZE;
                prefFavName = PrefsManager.StringPref.Names.FORUM_FAV_NAME;
                prefFavLink = PrefsManager.StringPref.Names.FORUM_FAV_LINK;
            } else {
                prefFavArraySize = PrefsManager.IntPref.Names.TOPIC_FAV_ARRAY_SIZE;
                prefFavName = PrefsManager.StringPref.Names.TOPIC_FAV_NAME;
                prefFavLink = PrefsManager.StringPref.Names.TOPIC_FAV_LINK;
            }

            currentFavArraySize = PrefsManager.getInt(prefFavArraySize);

            for (int i = 0; i < currentFavArraySize; ++i) {
                PrefsManager.removeStringWithSufix(prefFavName, String.valueOf(i));
                PrefsManager.removeStringWithSufix(prefFavLink, String.valueOf(i));
            }

            PrefsManager.putInt(prefFavArraySize, sizeOfListOfFavs);

            for (int i = 0; i < sizeOfListOfFavs; ++i) {
                PrefsManager.putStringWithSufix(prefFavName, String.valueOf(i), listOfFavs.get(i).name);
                PrefsManager.putStringWithSufix(prefFavLink, String.valueOf(i), listOfFavs.get(i).link);
            }

            PrefsManager.applyChanges();
            updateFavsInNavigationMenu(true);

            if (typeOfFav == RefreshFavDialogFragment.FAV_FORUM && Build.VERSION.SDK_INT >= 25) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                Utils.updateShortcuts(this, shortcutManager, listOfFavs.size());
            }
        } else {
            Toast.makeText(this, R.string.errorDuringFetchFavs, Toast.LENGTH_SHORT).show();
        }
    }

    protected abstract void initializeViewAndToolbar();
    protected abstract void newForumOrTopicToRead(String link, boolean itsAForum, boolean isWhenDrawerIsClosed, boolean fromLongClick);
    protected abstract void launchShowForumInfos();
    protected abstract void updateAccountDependentInfos();
}
