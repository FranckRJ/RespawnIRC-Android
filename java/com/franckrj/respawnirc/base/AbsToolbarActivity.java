package com.franckrj.respawnirc.base;

import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

public class AbsToolbarActivity extends AbsThemedActivity {
    protected Toolbar initToolbar(@IdRes int idOfToolbar) {
        Toolbar myToolbar = findViewById(idOfToolbar);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        return myToolbar;
    }
}
