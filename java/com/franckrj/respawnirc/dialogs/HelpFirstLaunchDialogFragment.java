package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.franckrj.respawnirc.ConnectActivity;
import com.franckrj.respawnirc.R;

public class HelpFirstLaunchDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.welcome).setMessage(R.string.help_firstlaunch)
                .setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(R.string.connectToJVC, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                        startActivity(new Intent(getActivity(), ConnectActivity.class));
                    }
                });
        return builder.create();
    }
}
