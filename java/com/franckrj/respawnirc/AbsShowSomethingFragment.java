package com.franckrj.respawnirc;

import android.app.Fragment;

public abstract class AbsShowSomethingFragment extends Fragment {
    protected boolean goToBottomAtPageLoading = false;

    public void setGoToBottomAtPageLoading(boolean newVal) {
        goToBottomAtPageLoading = newVal;
    }

    public abstract void setPageLink(String newForumPageLink);
    public abstract void clearContent();
}
