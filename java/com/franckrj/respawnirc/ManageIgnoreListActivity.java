package com.franckrj.respawnirc;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.utils.IgnoreListManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ManageIgnoreListActivity extends AbsHomeIsBackActivity {
    private TextView emptyListMessageText = null;
    private IgnoreListAdapter adapterForIgnoreList = null;
    private ArrayList<String> listOfIgnoredPseudos = new ArrayList<>();
    private boolean listHasChanged = false;

    private void generateListOfIgnoredPseudos() {
        listOfIgnoredPseudos.clear();
        listOfIgnoredPseudos.addAll(Arrays.asList(IgnoreListManager.getListOfIgnoredPseudosInLCAsArray()));
        Collections.sort(listOfIgnoredPseudos);
    }

    private void removeItem(int position) {
        if (position < listOfIgnoredPseudos.size()) {
            IgnoreListManager.removePseudoFromIgnoredList(listOfIgnoredPseudos.get(position));
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
        initToolbar(R.id.toolbar_manageignorelist);

        generateListOfIgnoredPseudos();
        adapterForIgnoreList = new IgnoreListAdapter(this, listOfIgnoredPseudos);
        ListView ignoreListView = findViewById(R.id.ignore_list_manageignorelist);
        emptyListMessageText = findViewById(R.id.text_emptylist_manageignorelist);
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
            IgnoreListManager.saveListOfIgnoredPseudos();
            listHasChanged = false;
        }

        super.onPause();
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
                holder.mainText = convertView.findViewById(R.id.text_main_ignorelist_row);
                holder.actionButton = convertView.findViewById(R.id.image_button_action_ignorelist_row);

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
