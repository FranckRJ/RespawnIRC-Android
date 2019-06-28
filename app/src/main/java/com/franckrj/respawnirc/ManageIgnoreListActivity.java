package com.franckrj.respawnirc;

import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.utils.IgnoreListManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ManageIgnoreListActivity extends AbsHomeIsBackActivity {
    private TextView emptyListMessageText = null;
    private RemovableItemListAdapter adapterForIgnoreList = null;
    private boolean listHasChanged = false;

    private final RemovableItemListAdapter.OnItemRemovedListener listenerForPseudoRemoved = new RemovableItemListAdapter.OnItemRemovedListener() {
        @Override
        public void onItemRemoved(String pseudo) {
            IgnoreListManager.removePseudoFromIgnoredList(pseudo);
            listHasChanged = true;

            if (adapterForIgnoreList.listIsEmpty()) {
                emptyListMessageText.setVisibility(View.VISIBLE);
            }
        }
    };

    private void generateListOfIgnoredPseudos() {
        ArrayList<String> newListOfIgnoredPseudos = new ArrayList<>(Arrays.asList(IgnoreListManager.getListOfIgnoredPseudosInLCAsArray()));
        Collections.sort(newListOfIgnoredPseudos);
        adapterForIgnoreList.setListOfItemText(newListOfIgnoredPseudos);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageignorelist);
        initToolbar(R.id.toolbar_manageignorelist);

        adapterForIgnoreList = new RemovableItemListAdapter(this);
        RecyclerView ignoreListView = findViewById(R.id.ignore_list_manageignorelist);
        emptyListMessageText = findViewById(R.id.text_emptylist_manageignorelist);

        generateListOfIgnoredPseudos();
        adapterForIgnoreList.setOnItemRemovedListener(listenerForPseudoRemoved);
        ignoreListView.setNestedScrollingEnabled(false);
        ignoreListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        ignoreListView.setLayoutManager(new LinearLayoutManager(this));
        ignoreListView.setAdapter(adapterForIgnoreList);

        if (adapterForIgnoreList.listIsEmpty()) {
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
}
