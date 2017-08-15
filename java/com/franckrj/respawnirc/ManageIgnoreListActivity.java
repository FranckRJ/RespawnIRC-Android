package com.franckrj.respawnirc;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.franckrj.respawnirc.utils.IgnoreListTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ManageIgnoreListActivity extends ThemedActivity {
    private TextView emptyListMessageText = null;
    private IgnoreListAdapter adapterForIgnoreList = null;
    private ArrayList<String> listOfIgnoredPseudos = new ArrayList<>();
    private boolean listHasChanged = false;

    private void genrateListOfIgnoredPseudos() {
        listOfIgnoredPseudos.clear();
        listOfIgnoredPseudos.addAll(Arrays.asList(IgnoreListTool.getListOfIgnoredPseudosInLCAsArray()));
        Collections.sort(listOfIgnoredPseudos);
    }

    private void removeItem(int position) {
        if (position < listOfIgnoredPseudos.size()) {
            IgnoreListTool.removePseudoFromIgnoredList(listOfIgnoredPseudos.get(position));
            listOfIgnoredPseudos.remove(position);
            adapterForIgnoreList.notifyDataSetChanged();
            listHasChanged = true;

            if (listOfIgnoredPseudos.isEmpty()) {
                emptyListMessageText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageignorelist);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_manageignorelist);
        setSupportActionBar(myToolbar);

        ActionBar myActionBar = getSupportActionBar();
        if (myActionBar != null) {
            myActionBar.setHomeButtonEnabled(true);
            myActionBar.setDisplayHomeAsUpEnabled(true);
        }

        genrateListOfIgnoredPseudos();
        adapterForIgnoreList = new IgnoreListAdapter(this, listOfIgnoredPseudos);
        ListView ignoreListView = (ListView) findViewById(R.id.ignore_list_manageignorelist);
        emptyListMessageText = (TextView) findViewById(R.id.text_emptylist_manageignorelist);
        ignoreListView.setAdapter(adapterForIgnoreList);

        if (listOfIgnoredPseudos.isEmpty()) {
            emptyListMessageText.setVisibility(View.VISIBLE);
        } else {
            emptyListMessageText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        if (listHasChanged) {
            IgnoreListTool.saveListOfIgnoredPseudos();
            listHasChanged = false;
        }

        super.onPause();
    }

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

    private class IgnoreListAdapter extends ArrayAdapter<String> {
        private LayoutInflater serviceInflater = null;

        public IgnoreListAdapter(Context context, List<String> listOfStrings) {
            super(context, R.layout.ignorelist_row, listOfStrings);
            serviceInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;

            if(convertView == null) {
                holder = new ViewHolder();

                convertView = serviceInflater.inflate(R.layout.ignorelist_row, parent, false);
                holder.mainText = (TextView) convertView.findViewById(R.id.text_main_ignorelist_row);
                holder.actionButton = (ImageButton) convertView.findViewById(R.id.image_button_action_ignorelist_row);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mainText.setText(getItem(position));
            holder.actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(position);
                }
            });

            return convertView;
        }

        private class ViewHolder {
            public TextView mainText;
            public ImageButton actionButton;
        }
    }
}
