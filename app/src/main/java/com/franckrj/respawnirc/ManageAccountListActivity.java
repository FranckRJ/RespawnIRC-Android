package com.franckrj.respawnirc;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.franckrj.respawnirc.base.AbsHomeIsBackActivity;
import com.franckrj.respawnirc.utils.AccountManager;

import java.util.ArrayList;

public class ManageAccountListActivity extends AbsHomeIsBackActivity {
    private TextView emptyListMessageText = null;
    private RemovableItemListAdapter adapterForAccountList = null;
    private boolean listHasChanged = false;

    private final RemovableItemListAdapter.OnItemRemovedListener listenerForAccountRemoved = new RemovableItemListAdapter.OnItemRemovedListener() {
        @Override
        public void onItemRemoved(String accountPseudo) {
            AccountManager.removeAccountFromListAndUpdateCurrentAccountIfNeeded(accountPseudo);
            listHasChanged = true;

            if (adapterForAccountList.listIsEmpty()) {
                emptyListMessageText.setVisibility(View.VISIBLE);
            }
        }
    };

    private void generateListOfAccounts() {
        adapterForAccountList.setListOfItemText(new ArrayList<>(AccountManager.getListOfAccountPseudo()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manageaccountlist);
        initToolbar(R.id.toolbar_manageaccountlist);

        adapterForAccountList = new RemovableItemListAdapter(this);
        RecyclerView accountListView = findViewById(R.id.account_list_manageaccountlist);
        emptyListMessageText = findViewById(R.id.text_emptylist_manageaccountlist);

        generateListOfAccounts();
        adapterForAccountList.setOnItemRemovedListener(listenerForAccountRemoved);
        accountListView.setNestedScrollingEnabled(false);
        accountListView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        accountListView.setLayoutManager(new LinearLayoutManager(this));
        accountListView.setAdapter(adapterForAccountList);

        if (adapterForAccountList.listIsEmpty()) {
            emptyListMessageText.setVisibility(View.VISIBLE);
        } else {
            emptyListMessageText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        if (listHasChanged) {
            AccountManager.saveListOfAccounts();
            listHasChanged = false;
        }

        super.onPause();
    }
}
