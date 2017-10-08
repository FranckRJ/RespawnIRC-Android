package com.franckrj.respawnirc.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.franckrj.respawnirc.R;

public class ChoosePageNumberDialogFragment extends DialogFragment {
    EditText pageNumberEdit = null;

    private final TextView.OnEditorActionListener actionInEditTextListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                pageNumberChoosed();
                return true;
            }
            return false;
        }
    };

    private void pageNumberChoosed() {
        if (!pageNumberEdit.getText().toString().isEmpty()) {
            Activity currentActivity = getActivity();
            int newPageNumber;

            try {
                newPageNumber = Integer.parseInt(pageNumberEdit.getText().toString());
            } catch (Exception e) {
                newPageNumber = -1;
            }

            if (currentActivity instanceof NewPageNumberSelected) {
                ((NewPageNumberSelected) currentActivity).newPageNumberChoosen(newPageNumber);
            }
        }
        dismiss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog alertToShow;
        Window currentWindow;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        @SuppressLint("InflateParams")
        View mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_choosepagenumber, null);
        pageNumberEdit = mainView.findViewById(R.id.pagenumber_edit_choosepagenumber);
        pageNumberEdit.setOnEditorActionListener(actionInEditTextListener);
        builder.setTitle(R.string.choosePageNumber).setView(mainView)
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    pageNumberChoosed();
                }
            });
        alertToShow = builder.create();
        currentWindow = alertToShow.getWindow();
        if (currentWindow != null) {
            currentWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        return alertToShow;
    }

    public interface NewPageNumberSelected {
        void newPageNumberChoosen(int newPageNumber);
    }
}
