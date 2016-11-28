package com.franckrj.respawnirc.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.franckrj.respawnirc.R;

public class ChoosePageNumberDialogFragment extends DialogFragment {
    public static final int REQUEST_CHANGE_PAGE = 42;

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
                Fragment targetFrag = getTargetFragment();
                if (targetFrag != null && !pageNumberEdit.getText().toString().isEmpty()) {
                    try {
                        targetFrag.onActivityResult(getTargetRequestCode(), Integer.parseInt(pageNumberEdit.getText().toString()), getActivity().getIntent());
                    } catch (Exception e) {
                        e.printStackTrace();
                        targetFrag.onActivityResult(getTargetRequestCode(), -REQUEST_CHANGE_PAGE, getActivity().getIntent());
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
}