package com.franckrj.respawnirc.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.franckrj.respawnirc.R;

public class ChoosePageNumberDialogFragment extends DialogFragment {
    EditText pageNumberEdit = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog alertToShow;
        Window currentWindow;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View mainView = getActivity().getLayoutInflater().inflate(R.layout.dialog_choosepagenumber, null);
        pageNumberEdit = (EditText) mainView.findViewById(R.id.pagenumber_edit_choosepagenumber);
        builder.setTitle(R.string.choosePageNumber).setView(mainView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (!pageNumberEdit.getText().toString().isEmpty()) {
                    Activity currentActivity = getActivity();
                    int newPageNumber;

                    try {
                        newPageNumber = Integer.parseInt(pageNumberEdit.getText().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        newPageNumber = -1;
                    }

                    if (currentActivity instanceof NewPageNumberSelected) {
                        ((NewPageNumberSelected) currentActivity).newPageNumberChoosen(newPageNumber);
                    }
                }
                dialog.dismiss();
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
