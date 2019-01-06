package com.franckrj.respawnirc.base;

import android.support.v4.app.Fragment;

public abstract class AbsShowSomethingFragment extends Fragment {
    protected boolean goToBottomAtPageLoading = false;
    protected boolean dontLoadOnFirstTime = false;
    protected String anchorForNextLoad = null;

    protected void clearTemporaryInfos() {
        goToBottomAtPageLoading = false;
        dontLoadOnFirstTime = false;
        anchorForNextLoad = null;
    }

    public void enableGoToBottomAtPageLoading() {
        goToBottomAtPageLoading = true;
    }

    public void enableDontLoadOnFirstTime() {
        dontLoadOnFirstTime = true;
    }

    public void setAnchorForNextLoad(String newVal) {
        if (newVal != null) {
            anchorForNextLoad = newVal;
        }
    }

    public abstract void setPageLink(String newForumPageLink);
    public abstract void clearContent(boolean deleteTemporaryInfos);
    public abstract void refreshContent();
}
