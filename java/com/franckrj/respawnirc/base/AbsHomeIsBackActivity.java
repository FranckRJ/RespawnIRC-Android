package com.franckrj.respawnirc.base;

import android.view.MenuItem;

public class AbsHomeIsBackActivity extends AbsToolbarActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
