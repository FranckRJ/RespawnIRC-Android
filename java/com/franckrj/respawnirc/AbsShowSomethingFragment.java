package com.franckrj.respawnirc;

import android.app.Fragment;

public abstract class AbsShowSomethingFragment extends Fragment {
    protected boolean goToBottomAtPageLoading = false;

    public void goToBottomAtPageLoading() {
        goToBottomAtPageLoading = true;
    }

    public abstract void setPageLink(String newForumPageLink);
    public abstract void clearContent();
}
