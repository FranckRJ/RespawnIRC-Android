package com.franckrj.respawnirc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.franckrj.respawnirc.dialogs.ChooseTopicOrForumLinkDialogFragment;
import com.franckrj.respawnirc.dialogs.HelpFirstLaunchDialogFragment;
import com.franckrj.respawnirc.jvctopictools.JVCTopicGetter;
import com.franckrj.respawnirc.jvctopictools.ShowForumFragment;
import com.franckrj.respawnirc.utils.JVCParser;

public class ShowForumActivity extends AppCompatActivity implements ChooseTopicOrForumLinkDialogFragment.NewTopicOrForumSelected,
                                                    ShowForumFragment.NewTopicWantRead, JVCTopicGetter.NewForumNameAvailable,
                                                    JVCTopicGetter.ForumLinkChanged {
    private static final int LIST_DRAWER_POS_HOME = 0;
    private static final int LIST_DRAWER_POS_CONNECT = 1;
    private static final int LIST_DRAWER_POS_SELECT_TOPIC_OR_FORUM = 2;
    private static final int LIST_DRAWER_POS_SETTING = 3;

    private DrawerLayout layoutForDrawer = null;
    private ListView listForDrawer = null;
    private ActionBarDrawerToggle toggleForDrawer = null;
    private int lastNewActivitySelected = -1;
    private SharedPreferences sharedPref = null;
    private String currentTitle = "";
    private Button firstPageButton = null;
    private Button previousPageButton = null;
    private Button currentPageButton = null;
    private Button nextPageButton = null;
    private String currentForumLink = "";
    private ViewPager pagerView = null;
    private ScreenSlidePagerAdapter adapterForPagerView = null;

    private final Button.OnClickListener changePageWithNavigationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (buttonView == firstPageButton && firstPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(0);
            } else if (buttonView == previousPageButton && previousPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(pagerView.getCurrentItem() - 1);
            }  else if (buttonView == nextPageButton && nextPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(pagerView.getCurrentItem() + 1);
            }
        }
    };

    private ListView.OnItemClickListener itemInDrawerClickedListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            lastNewActivitySelected = position;
            listForDrawer.setItemChecked(position, true);
            layoutForDrawer.closeDrawer(listForDrawer);
        }
    };

    private void updateNavigationButtons() {
        firstPageButton.setVisibility(View.GONE);
        previousPageButton.setVisibility(View.GONE);
        currentPageButton.setText(R.string.waitingText);
        nextPageButton.setVisibility(View.GONE);

        if (pagerView.getCurrentItem() >= 0) {
            currentPageButton.setText(String.valueOf(pagerView.getCurrentItem() + 1));

            if (pagerView.getCurrentItem() > 0) {
                firstPageButton.setVisibility(View.VISIBLE);
                firstPageButton.setText(String.valueOf(1));
                previousPageButton.setVisibility(View.VISIBLE);
            }
            if (pagerView.getCurrentItem() < adapterForPagerView.getCount() - 1) {
                nextPageButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private ShowForumFragment createNewFragmentForForumRead(String possibleForumLink) {
        ShowForumFragment currentFragment = new ShowForumFragment();

        if (possibleForumLink != null) {
            Bundle argForFrag = new Bundle();
            argForFrag.putString(ShowForumFragment.ARG_FORUM_LINK, possibleForumLink);
            currentFragment.setArguments(argForFrag);
        }

        return currentFragment;
    }

    private void setNewForumLink(String newLink) {
        currentForumLink = newLink;
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
                setNewForumLink(link);
            } else {
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
            }
        }
    }

    private void updateAdapterForPagerView() {
        adapterForPagerView = new ScreenSlidePagerAdapter(getFragmentManager());
        pagerView.setAdapter(adapterForPagerView);
    }

    private void updateCurrentItemAndButtonsToCurrentLink() {
        if (!currentForumLink.isEmpty()) {
            pagerView.setCurrentItem((Integer.parseInt(JVCParser.getPageNumberForThisForumLink(currentForumLink)) - 1) / 25);
            updateNavigationButtons();
        }
    }

    private void loadPageForThisFragment(int position) {
        if (!currentForumLink.isEmpty()) {
            ShowForumFragment currentFragment = adapterForPagerView.getFragment(position);
            if (currentFragment != null) {
                currentFragment.setForumPageLink(JVCParser.setPageNumberForThisForumLink(currentForumLink, (position * 25) + 1));
            }
        }
    }

    private void clearPageForThisFragment(int position) {
        ShowForumFragment currentFragment = adapterForPagerView.getFragment(position);
        if (currentFragment != null) {
            currentFragment.clearForum();
        }
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
        listForDrawer = (ListView) findViewById(R.id.view_left_drawer_showforum);
        firstPageButton = (Button) findViewById(R.id.firstpage_button_showforum);
        previousPageButton = (Button) findViewById(R.id.previouspage_button_showforum);
        currentPageButton = (Button) findViewById(R.id.currentpage_button_showforum);
        nextPageButton = (Button) findViewById(R.id.nextpage_button_showforum);

        toggleForDrawer = new ActionBarDrawerToggle(this, layoutForDrawer, R.string.openDrawerContentDescRes, R.string.closeDrawerContentDescRes) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                super.onDrawerSlide(drawerView, 0);
                lastNewActivitySelected = -1;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                switch (lastNewActivitySelected) {
                    case LIST_DRAWER_POS_HOME:
                        break;
                    case LIST_DRAWER_POS_CONNECT:
                        startActivity(new Intent(ShowForumActivity.this, ConnectActivity.class));
                        break;
                    case LIST_DRAWER_POS_SELECT_TOPIC_OR_FORUM:
                        ChooseTopicOrForumLinkDialogFragment chooseLinkDialogFragment = new ChooseTopicOrForumLinkDialogFragment();
                        chooseLinkDialogFragment.show(getFragmentManager(), "ChooseTopicOrForumLinkDialogFragment");
                        listForDrawer.setItemChecked(0, true);
                        break;
                    case LIST_DRAWER_POS_SETTING:
                        startActivity(new Intent(ShowForumActivity.this, SettingsActivity.class));
                        break;
                }
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        updateAdapterForPagerView();
        pagerView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //rien
            }

            @Override
            public void onPageSelected(int position) {
                loadPageForThisFragment(position);
                updateNavigationButtons();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (pagerView.getCurrentItem() > 0) {
                        clearPageForThisFragment(pagerView.getCurrentItem() - 1);
                    }
                    if (pagerView.getCurrentItem() < adapterForPagerView.getCount() - 1) {
                        clearPageForThisFragment(pagerView.getCurrentItem() + 1);
                    }
                }
            }
        });
        listForDrawer.setAdapter(new ArrayAdapter<>(this, R.layout.draweritem_row, getResources().getStringArray(R.array.itemChoiceDrawerList)));
        listForDrawer.setOnItemClickListener(itemInDrawerClickedListener);
        layoutForDrawer.addDrawerListener(toggleForDrawer);
        layoutForDrawer.setDrawerShadow(R.drawable.shadow_drawer, GravityCompat.START);
        firstPageButton.setVisibility(View.GONE);
        firstPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        previousPageButton.setVisibility(View.GONE);
        previousPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        nextPageButton.setVisibility(View.GONE);
        nextPageButton.setOnClickListener(changePageWithNavigationButtonListener);

        currentForumLink = sharedPref.getString(getString(R.string.prefForumUrlToFetch), "");
        if (savedInstanceState == null) {
            currentTitle = getString(R.string.app_name);
            updateCurrentItemAndButtonsToCurrentLink();
        } else {
            currentTitle = savedInstanceState.getString(getString(R.string.saveCurrentForumTitle), getString(R.string.app_name));
            updateNavigationButtons();
        }
        setTitle(currentTitle);

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
        listForDrawer.setItemChecked(0, true);
        sharedPrefEdit.putInt(getString(R.string.prefLastActivityViewed), MainActivity.ACTIVITY_SHOW_FORUM);
        sharedPrefEdit.apply();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!currentForumLink.isEmpty()) {
            SharedPreferences.Editor sharedPrefEdit = sharedPref.edit();
            sharedPrefEdit.putString(getString(R.string.prefForumUrlToFetch), JVCParser.setPageNumberForThisForumLink(currentForumLink, (pagerView.getCurrentItem() * 25) + 1));
            sharedPrefEdit.apply();
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
        currentForumLink = newForumLink;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<ShowForumFragment> referenceMap = new SparseArray<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public ShowForumFragment getFragment(int key) {
            return referenceMap.get(key);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ShowForumFragment) object).clearForum();
            referenceMap.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == pagerView.getCurrentItem() && !currentForumLink.isEmpty()) {
                return createNewFragmentForForumRead(JVCParser.setPageNumberForThisForumLink(currentForumLink, (position * 25) + 1));
            } else {
                return createNewFragmentForForumRead(null);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ShowForumFragment fragment = (ShowForumFragment) super.instantiateItem(container, position);
            referenceMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return 100;
        }
    }
}
