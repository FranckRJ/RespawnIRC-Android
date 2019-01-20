package com.franckrj.respawnirc.jvcforum;

import android.os.Bundle;
import android.widget.TextView;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;

public class ShowForumInfosActivity extends AbsHomeIsBackActivity {
    public static final String EXTRA_FORUM_LINK = "com.franckrj.respawnirc.showforuminfos.EXTRA_FORUM_LINK";

    private TextView backgroundErrorText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showforuminfos);
        initToolbar(R.id.toolbar_showforuminfos);

        backgroundErrorText = findViewById(R.id.text_errorbackgroundmessage_showforuminfos);

        if (getIntent() != null) {
            if (getIntent().getStringExtra(EXTRA_FORUM_LINK) != null) {
                backgroundErrorText.setText(getIntent().getStringExtra(EXTRA_FORUM_LINK));
            }
        }
    }
}
