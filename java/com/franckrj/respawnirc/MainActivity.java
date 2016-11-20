package com.franckrj.respawnirc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.content_frame_main, new ShowTopicIRCFragment()).commit();
        }
    }
}
