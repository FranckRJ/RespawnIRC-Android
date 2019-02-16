package com.franckrj.respawnirc.base;

import androidx.annotation.IdRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

public abstract class AbsToolbarActivity extends AbsThemedActivity {
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
