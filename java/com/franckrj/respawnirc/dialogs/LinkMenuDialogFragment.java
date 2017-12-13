package com.franckrj.respawnirc.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.franckrj.respawnirc.R;
import com.franckrj.respawnirc.utils.Utils;

public class LinkMenuDialogFragment extends DialogFragment {
    public static final String ARG_URL = "com.franckrj.respawnirc.linkmenudialogfragment.url";

    private static final int POS_OPEN_IN_WEB_BROWSER = 0;
    private static final int POS_OPEN_IN_INTERN_BROWSER = 1;
    private static final int POS_COPY = 2;

    private String urlOfLink;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle currentArgs = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (currentArgs != null) {
            urlOfLink = currentArgs.getString(ARG_URL, getString(R.string.waitingText));
        } else {
            urlOfLink = getString(R.string.waitingText);
        }

        builder.setTitle(urlOfLink);
        builder.setItems(R.array.choicesForLinkMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case POS_OPEN_IN_WEB_BROWSER:
                        Utils.openLinkInExternalBrowser(urlOfLink, getActivity());
                        break;
                    case POS_OPEN_IN_INTERN_BROWSER:
                        Utils.openLinkInInternalBrowser(urlOfLink, getActivity());
                        break;
                    case POS_COPY:
                        Utils.putStringInClipboard(urlOfLink, getActivity());
                        Toast.makeText(getActivity(), R.string.copyDone, Toast.LENGTH_SHORT).show();
                        break;
                }
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
