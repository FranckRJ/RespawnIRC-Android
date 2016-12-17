package com.franckrj.respawnirc;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public abstract class AbsShowSomethingActivity extends AppCompatActivity {
    protected Button firstPageButton = null;
    protected Button previousPageButton = null;
    protected Button currentPageButton = null;
    protected Button nextPageButton = null;
    protected Button lastPageButton = null;
    protected String currentLink = "";
    protected ViewPager pagerView = null;
    protected ScreenSlidePagerAdapter adapterForPagerView = null;
    protected boolean showNavigationButtons = true;
    protected int lastPage = 0;

    protected final Button.OnClickListener changePageWithNavigationButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (buttonView == firstPageButton && firstPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(0);
                return;
            } else if (buttonView == previousPageButton && previousPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(pagerView.getCurrentItem() - 1);
                return;
            }  else if (buttonView == nextPageButton && nextPageButton.getVisibility() == View.VISIBLE) {
                pagerView.setCurrentItem(pagerView.getCurrentItem() + 1);
                return;
            } else if (lastPageButton != null) {
                if (buttonView == lastPageButton && lastPageButton.getVisibility() == View.VISIBLE) {
                    pagerView.setCurrentItem(lastPage - 1);
                    return;
                }
            }

            extendPageSelection(buttonView);
        }
    };

    protected final ViewPager.OnPageChangeListener pageChangeOnPagerListener = new ViewPager.OnPageChangeListener() {
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
    };

    protected void extendPageSelection(View buttonView) {
        //rien
    }

    protected void initializeNavigationButtons() {
        firstPageButton.setVisibility(View.GONE);
        firstPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        previousPageButton.setVisibility(View.GONE);
        previousPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        currentPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        nextPageButton.setVisibility(View.GONE);
        nextPageButton.setOnClickListener(changePageWithNavigationButtonListener);

        if (lastPageButton != null) {
            lastPageButton.setVisibility(View.GONE);
            lastPageButton.setOnClickListener(changePageWithNavigationButtonListener);
        }
    }

    protected void updateNavigationButtons() {
        if (showNavigationButtons) {
            firstPageButton.setVisibility(View.GONE);
            previousPageButton.setVisibility(View.GONE);
            currentPageButton.setText(R.string.waitingText);
            nextPageButton.setVisibility(View.GONE);

            if (lastPageButton != null) {
                lastPageButton.setVisibility(View.GONE);
            }

            if (pagerView.getCurrentItem() >= 0 && lastPage > 0) {
                currentPageButton.setText(String.valueOf(pagerView.getCurrentItem() + 1));

                if (pagerView.getCurrentItem() > 0) {
                    firstPageButton.setVisibility(View.VISIBLE);
                    firstPageButton.setText(String.valueOf(1));
                    previousPageButton.setVisibility(View.VISIBLE);
                }
                if (pagerView.getCurrentItem() < lastPage - 1) {
                    nextPageButton.setVisibility(View.VISIBLE);

                    if (lastPageButton != null) {
                        lastPageButton.setVisibility(View.VISIBLE);
                        lastPageButton.setText(String.valueOf(lastPage));
                    }
                }
            }
        }
    }

    protected void updateAdapterForPagerView() {
        adapterForPagerView = new ScreenSlidePagerAdapter(getFragmentManager());
        pagerView.setAdapter(adapterForPagerView);
    }

    protected void updateCurrentItemAndButtonsToCurrentLink() {
        if (!currentLink.isEmpty()) {
            pagerView.setCurrentItem(getShowablePageNumberForThisLink(currentLink) - 1);
            updateNavigationButtons();
        }
    }

    protected void loadPageForThisFragment(int position) {
        if (!currentLink.isEmpty()) {
            AbsShowSomethingFragment currentFragment = adapterForPagerView.getFragment(position);
            if (currentFragment != null) {
                currentFragment.setPageLink(setShowedPageNumberForThisLink(currentLink, position + 1));
            }
        }
    }

    protected void clearPageForThisFragment(int position) {
        AbsShowSomethingFragment currentFragment = adapterForPagerView.getFragment(position);
        if (currentFragment != null) {
            currentFragment.clearContent();
        }
    }

    protected class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<AbsShowSomethingFragment> referenceMap = new SparseArray<>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public AbsShowSomethingFragment getFragment(int key) {
            return referenceMap.get(key);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((AbsShowSomethingFragment) object).clearContent();
            referenceMap.remove(position);
            super.destroyItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == pagerView.getCurrentItem() && !currentLink.isEmpty()) {
                return createNewFragmentForRead(setShowedPageNumberForThisLink(currentLink, position + 1));
            } else {
                return createNewFragmentForRead(null);
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            AbsShowSomethingFragment fragment = (AbsShowSomethingFragment) super.instantiateItem(container, position);
            referenceMap.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            if (lastPage > 0) {
                return lastPage;
            } else {
                return 1;
            }
        }
    }

    protected abstract AbsShowSomethingFragment createNewFragmentForRead(String possibleLink);
    protected abstract int getShowablePageNumberForThisLink(String link);
    protected abstract String setShowedPageNumberForThisLink(String link, int newPageNumber);
}