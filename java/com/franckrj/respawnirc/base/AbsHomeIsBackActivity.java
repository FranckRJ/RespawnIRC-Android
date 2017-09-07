package com.franckrj.respawnirc.base;

import android.view.MenuItem;

public class AbsHomeIsBackActivity extends AbsToolbarActivity {
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
