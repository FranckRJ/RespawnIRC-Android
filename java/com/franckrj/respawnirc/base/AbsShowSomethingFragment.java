package com.franckrj.respawnirc.base;

import android.support.v4.app.Fragment;

public abstract class AbsShowSomethingFragment extends Fragment {
    protected boolean goToBottomAtPageLoading = false;
    protected boolean dontLoadOnFirstTime = false;

    public void enableGoToBottomAtPageLoading() {
        goToBottomAtPageLoading = true;
    }

    public void enableDontLoadOnFirstTime() {
        dontLoadOnFirstTime = true;
    }

    public abstract void setPageLink(String newForumPageLink);
    public abstract void clearContent();
    public abstract void refreshContent();
}
